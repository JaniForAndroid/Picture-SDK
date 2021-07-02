package com.uraroji.garage.android.lame;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Create time: 2015/9/18.
 */
public class AudioUtil {

  private static final String TAG = "AudioUtil";
  private static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
  private static final int FRAME_COUNT = 160;
  private static final int BYTES_PER_FRAME = 2;
  private static final int FORMAT_PCM_16 = AudioFormat.ENCODING_PCM_16BIT;
  private File mRecordFile;
  private int mBufferSize;
  private int mSampleRate;
  private int mChannelConfig;
  private AudioRecord mAudioRecord = null;
  private boolean mIsRecording;
  private boolean canceled;
  private DataEncodeThread mEncodeThread;
  private VolumeCallBack volumeCallBack;
  private int maxlength = -1;
  private int minlength = 0;
  public static final int UPDATE_RECORD_TIME = 1;
  private TimeHandle timerHander;
  private long startTime;
  private long recordTimeMs;

  public AudioUtil(Context context, int channelConfig, VolumeCallBack callBack) {
    AudioManager myAudioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    String nativeParam = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
    mSampleRate = Integer.parseInt(nativeParam);
    mChannelConfig = channelConfig;
    volumeCallBack = callBack;
    mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, channelConfig, FORMAT_PCM_16);

    int frameSize = mBufferSize / BYTES_PER_FRAME;
    if (frameSize % FRAME_COUNT != 0) {
      frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
      mBufferSize = frameSize * BYTES_PER_FRAME;
    }
  }

  public AudioUtil(int sampleRate, int channelConfig, VolumeCallBack callBack) {
    mSampleRate = sampleRate;
    mChannelConfig = channelConfig;
    volumeCallBack = callBack;
    mBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, FORMAT_PCM_16);

    int frameSize = mBufferSize / BYTES_PER_FRAME;
    if (frameSize % FRAME_COUNT != 0) {
      frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
      mBufferSize = frameSize * BYTES_PER_FRAME;
    }
  }

  private int getChannels() {
    if (mChannelConfig == AudioFormat.CHANNEL_IN_STEREO) {
      return 2;
    } else {
      return 1;
    }
  }

  public int testAudio() {
    if (mAudioRecord == null) {
      mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE, mSampleRate, mChannelConfig,
          FORMAT_PCM_16, mBufferSize);
    }
    mAudioRecord.startRecording();
    byte[] pcm = new byte[mBufferSize];
    int readSize = mAudioRecord.read(pcm, 0, mBufferSize);
    mAudioRecord.stop();
    return readSize;
  }

  //录pcm
  public void startPcm(File recordFile, final boolean append, final boolean need2wav,
      final File wavFile) {
    if (mIsRecording) {
      return;
    }
    canceled = false;
    mRecordFile = recordFile;
    if (mAudioRecord == null) {
      mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE, mSampleRate, mChannelConfig,
          FORMAT_PCM_16, mBufferSize);
    }
    mAudioRecord.startRecording();
    new Thread() {

      @Override
      public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        mIsRecording = true;
        try {
          OutputStream os = new FileOutputStream(mRecordFile, append);
          byte[] pcm = new byte[mBufferSize];
          while (!canceled) {
            int readSize = mAudioRecord.read(pcm, 0, mBufferSize);
            if (readSize > 0) {
              os.write(pcm, 0, readSize);
            }
          }
          os.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        // release and finalize audioRecord
        if (mAudioRecord != null) {
          try {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        mIsRecording = false;
        if (need2wav) {
          convertWaveFile(wavFile);
        }


      }

    }.start();
  }


  public void startMp3(File recordFile, float minlength, float maxlength)
      throws FileNotFoundException {
    this.maxlength = (int) (maxlength * 1000);
    this.minlength = (int) (minlength * 1000);
    startMp3(recordFile);
  }

  public void startMp3(File recordFile) throws FileNotFoundException {
    startMp3(recordFile, false);
  }

  public void startMp3(File recordFile, boolean append) throws FileNotFoundException {
    if (mIsRecording) {
      return;
    }
    canceled = false;
    timerHander = new TimeHandle();
    startTime = System.currentTimeMillis();
    timerHander.sendEmptyMessage(UPDATE_RECORD_TIME);
    mRecordFile = recordFile;
    if (mAudioRecord == null) {
      mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE, mSampleRate, mChannelConfig,
          FORMAT_PCM_16, mBufferSize);
    }
    FileOutputStream out = new FileOutputStream(mRecordFile, append);
    mEncodeThread = new DataEncodeThread(out, mBufferSize, getChannels());
    mEncodeThread.start();
    mAudioRecord.setRecordPositionUpdateListener(mEncodeThread, mEncodeThread.getHandler());
    mAudioRecord.setPositionNotificationPeriod(FRAME_COUNT);
    mAudioRecord.startRecording();
    new Thread() {

      @Override
      public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        mIsRecording = true;
        SimpleLame.init(mSampleRate, getChannels(), mSampleRate, 128, 5);
        try {
          short[] pcm = new short[mBufferSize / 2];
          while (!canceled) {
            int readSize = mAudioRecord.read(pcm, 0, mBufferSize / 2);
            if (readSize > 0) {
              mEncodeThread.addTask(pcm, readSize);
              double decibel = calculateRealVolume(pcm, readSize);
              if (volumeCallBack != null) {
                volumeCallBack.onCurrentVoice(decibel);
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        // release and finalize audioRecord
        if (mAudioRecord != null) {
          try {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        // stop the encoding thread and try to wait
        // until the thread finishes its job
        Message msg = Message.obtain(mEncodeThread.getHandler(), DataEncodeThread.PROCESS_STOP);
        msg.sendToTarget();
        mIsRecording = false;
      }

      private double calculateRealVolume(short[] buffer, int readSize) {
        double sum = 0;
        for (int i = 0; i < readSize; i++) {
          sum += buffer[i] * buffer[i];
        }
        if (readSize > 0) {
          double amplitude = sum / readSize;
          mVolume = Math.sqrt(amplitude);
          if (amplitude == 0) {
            return 0;
          } else {
            return 10 * Math.log10(amplitude);
          }

        } else {
          return 0;
        }
      }

    }.start();
  }

  private double mVolume;

  public double getVolume() {
    return mVolume;
  }

  public int getSampleRate() {
    return mSampleRate;
  }

  private void stopNow() {
    canceled = true;
  }

  public boolean stop() {
    if (!isRecording()) {
      return true;
    }
    if (minlength == 0 || recordTimeMs > minlength) {
      stopNow();
      return true;
    } else {
      return false;
    }
  }

  public boolean isRecording() {
    return mIsRecording;
  }

  public void release() {
    if (mAudioRecord != null) {
      if (mIsRecording) {
        mAudioRecord.stop();
      }
      mAudioRecord.release();
      mAudioRecord = null;
    }
  }

  public void split(int percent) {
    long size = mRecordFile.length();
    long frames = size / (BYTES_PER_FRAME * getChannels());
    long splitFrames = percent * frames / 100;
    long splitSize = splitFrames * BYTES_PER_FRAME * getChannels();
    File newFile = new File(mRecordFile.getParentFile(), mRecordFile.getName() + "_temp");
    byte[] data = new byte[1024 * 10];
    long total = 0;
    try {
      FileInputStream in = new FileInputStream(mRecordFile);
      FileOutputStream out = new FileOutputStream(newFile);
      int len;
      while (total < splitSize && (len = in.read(data)) != -1) {
        long l = splitSize - total;
        int writeLen = l > len ? len : (int) l;
        out.write(data, 0, writeLen);
        total += writeLen;
      }
      out.close();
      in.close();
      mRecordFile.delete();
      newFile.renameTo(mRecordFile);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

//    public void convertMp3File(File outFile) {
//        if (mIsRecording) return;
//        SimpleLame.init(mSampleRate, getChannels(), mSampleRate, 128, 5);
//        SimpleLame.convert(mRecordFile.getAbsolutePath(), outFile.getAbsolutePath());
//        SimpleLame.close();
//    }

  public void convertWaveFile(File outFile) {
    if (mIsRecording) {
      return;
    }
    Log.i(TAG, "convertWaveFile: start");
    FileInputStream in;
    FileOutputStream out;
    long totalAudioLen;
    long totalDataLen;
    long byteRate = BYTES_PER_FRAME * mSampleRate * getChannels();
    byte[] data = new byte[1024 * 10];
    try {
      in = new FileInputStream(mRecordFile);
      out = new FileOutputStream(outFile);
      totalAudioLen = in.getChannel().size();
      totalDataLen = totalAudioLen + 36;
      WriteWaveFileHeader(out, totalAudioLen, totalDataLen, byteRate, mSampleRate, getChannels());
      while (in.read(data) != -1) {
        out.write(data);
      }
      in.close();
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    Log.i(TAG, "convertWaveFile: end");
  }

  public static void convertWaveFile(File inFile, File outFile, int mSampleRate, int channel) {
    Log.i(TAG, "convertWaveFile: start");
    FileInputStream in;
    FileOutputStream out;
    long totalAudioLen;
    long totalDataLen;
    long byteRate = BYTES_PER_FRAME * mSampleRate * channel;
    byte[] data = new byte[1024 * 10];
    try {
      in = new FileInputStream(inFile);
      out = new FileOutputStream(outFile);
      totalAudioLen = in.getChannel().size();
      totalDataLen = totalAudioLen + 36;
      WriteWaveFileHeader(out, totalAudioLen, totalDataLen, byteRate, mSampleRate, channel);
      while (in.read(data) != -1) {
        out.write(data);
      }
      in.close();
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    Log.i(TAG, "convertWaveFile: end");
  }

  private static void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
      long totalDataLen, long byteRate, int sampleRate, int channel) throws IOException {
    byte[] header = new byte[44];
    header[0] = 'R'; // RIFF
    header[1] = 'I';
    header[2] = 'F';
    header[3] = 'F';
    header[4] = (byte) (totalDataLen & 0xff);//数据大小
    header[5] = (byte) ((totalDataLen >> 8) & 0xff);
    header[6] = (byte) ((totalDataLen >> 16) & 0xff);
    header[7] = (byte) ((totalDataLen >> 24) & 0xff);
    header[8] = 'W';//WAVE
    header[9] = 'A';
    header[10] = 'V';
    header[11] = 'E';
    //FMT Chunk
    header[12] = 'f'; // 'fmt '
    header[13] = 'm';
    header[14] = 't';
    header[15] = ' ';//过渡字节
    //数据大小
    header[16] = 16; // 4 bytes: size of 'fmt ' chunk
    header[17] = 0;
    header[18] = 0;
    header[19] = 0;
    //编码方式 10H为PCM编码格式
    header[20] = 1; // format = 1
    header[21] = 0;
    //通道数
    header[22] = (byte) channel;
    header[23] = 0;
    //采样率，每个通道的播放速度
    header[24] = (byte) (sampleRate & 0xff);
    header[25] = (byte) ((sampleRate >> 8) & 0xff);
    header[26] = (byte) ((sampleRate >> 16) & 0xff);
    header[27] = (byte) ((sampleRate >> 24) & 0xff);
    //音频数据传送速率,采样率*通道数*采样深度/8
    header[28] = (byte) (byteRate & 0xff);
    header[29] = (byte) ((byteRate >> 8) & 0xff);
    header[30] = (byte) ((byteRate >> 16) & 0xff);
    header[31] = (byte) ((byteRate >> 24) & 0xff);
    // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
    int bitPreFrame = 16;
    header[32] = (byte) (channel * bitPreFrame / 8);
    header[33] = 0;
    //每个样本的数据位数
    header[34] = (byte) bitPreFrame;
    header[35] = 0;
    //Data chunk
    header[36] = 'd';//data
    header[37] = 'a';
    header[38] = 't';
    header[39] = 'a';
    header[40] = (byte) (totalAudioLen & 0xff);
    header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
    header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
    header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
    out.write(header, 0, 44);
  }

  public int getMsPerBuffer() {
    return mBufferSize * 1000 / (mSampleRate * getChannels() * BYTES_PER_FRAME);
  }

  public void cutFile(double byteSize) throws IOException {
    if (mIsRecording) {
      return;
    }
    File tempFile = new File(mRecordFile.getParentFile(), "temp.pcm");
    FileInputStream in;
    FileOutputStream out;
    in = new FileInputStream(mRecordFile);
    out = new FileOutputStream(tempFile);
    int offset = 0;
    while (offset < byteSize - 1024) {
      byte[] buffer = new byte[1024];
      in.read(buffer);
      out.write(buffer);
      offset += 1024;
    }

    double leftbytes = byteSize - offset;
    byte[] buffer = new byte[(int) leftbytes];
    in.read(buffer);
    out.write(buffer);

    in.close();
    out.close();

    mRecordFile.delete();
    tempFile.renameTo(mRecordFile);


  }


  public interface VolumeCallBack {

    void onCurrentVoice(double currentVolume);
  }


  class TimeHandle extends Handler {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case UPDATE_RECORD_TIME:
          recordTimeMs = System.currentTimeMillis() - startTime;
          if (maxlength != -1 && recordTimeMs > maxlength) {
            stopNow();
          }
          if (!canceled) {
            queueNextRefresh(UPDATE_RECORD_TIME);
          }
          break;
      }
    }
  }

  private void queueNextRefresh(int msg) {
    timerHander.removeMessages(msg);
    timerHander.sendEmptyMessageDelayed(msg, 1000);
  }
}

