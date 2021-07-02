package com.namibox.hfx.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.todoroo.aacenc.AACEncoder;
import com.uraroji.garage.android.lame.AudioUtil;
import com.uraroji.garage.android.lame.DataEncodeThread;
import com.uraroji.garage.android.lame.SimpleLame;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;


/**
 * Created by sunha on 2016/11/29 0029.
 */

public class AudioComposeUtil {

  private static final String TAG = "AudioComposeUtil";
  private static DataEncodeThread mEncodeThread;


  public static Flowable<Integer> getAudioComposeObservable(final String baseAudioFilePath,
      final String composeAudioFilePath, final String[] filePathList, final int[] delayTimeList) {
    return Flowable.create(new FlowableOnSubscribe<Integer>() {
      @Override
      public void subscribe(@NonNull FlowableEmitter<Integer> e) throws Exception {

        composePcm(baseAudioFilePath, composeAudioFilePath, filePathList, delayTimeList, e);
      }
    }, BackpressureStrategy.LATEST);
  }

  public static Flowable<Integer> getPcm2Mp3Observable(final String pcmFilePath,
      final String mp3FilePath) {
    return Flowable.create(new FlowableOnSubscribe<Integer>() {
      @Override
      public void subscribe(@NonNull FlowableEmitter<Integer> e) throws Exception {
        pcm2Mp3(pcmFilePath, mp3FilePath, e);
      }
    }, BackpressureStrategy.LATEST);
  }

  public static Flowable<Integer> getPcm2AACObservable(final Context context,
      final String pcmFilePath, final String aacFilePath, final String m4aFilePath) {
    return Flowable.create(new FlowableOnSubscribe<Integer>() {
      @Override
      public void subscribe(@NonNull FlowableEmitter<Integer> e) throws Exception {
        pcm2AAC(pcmFilePath, aacFilePath, m4aFilePath, e);
      }
    }, BackpressureStrategy.LATEST);
  }


  public static void pcm2AAC(String pcmFilePath, String aacFilePath, String m4aFilePath,
      FlowableEmitter<? super Integer> emitter) {
    Log.i(TAG, "pcm2AAC: ");
    AACEncoder encoder = new AACEncoder();
    FileInputStream inputStream = GetFileInputStreamFromFile(pcmFilePath);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    boolean finish = false;
    int readSize;
    //        int sampleRate = (int) (baos.size() * 1000 / delta);
    int sampleRate = 16000;
    Log.i(TAG, "wav2Aac: start");
    encoder.init(64000, 1, sampleRate, 16, aacFilePath);
    try {
      while (!finish) {
        readSize = inputStream.read(buffer);
        if (readSize < 0) {
          finish = true;
        } else {
          baos.reset();
          baos.write(buffer, 0, readSize);
          encoder.encode(baos.toByteArray());
        }

      }

      // can i has calculate?
      encoder.uninit();
      convert(aacFilePath, m4aFilePath);
      Log.i(TAG, "wav2Aac: end");
      emitter.onComplete();

    } catch (IOException e) {
      e.printStackTrace();
      emitter.onError(e);
    }


  }

  private static void convert(String infile, String outfile) throws IOException {

//        InputStream input = new FileInputStream(infile);

//        PushbackInputStream pbi = new PushbackInputStream(input, 100);

//        System.err.println("well you got " + input.available());
    Movie movie = new Movie();

    Track audioTrack = new AACTrackImpl(new FileDataSourceImpl(infile));
    movie.addTrack(audioTrack);

    Container mp4file = new DefaultMp4Builder().build(movie);
//        FileOutputStream output = new FileOutputStream(outfile);
    FileChannel fc = new FileOutputStream(new File(outfile)).getChannel();
    mp4file.writeContainer(fc);
    fc.close();
//        out.getBox(output.getChannel());
//        output.close();
  }

  /**
   * 基于pcm数据混合音频
   *
   * @param baseAudioFilePath 背景因的pcm文件路径
   * @param pcmFilePath 混合后的pcm文件路径
   * @param filePathList 配音的pcm音频文件路径数组
   * @param delayTimeList 每一段配音的偏移时间
   */
  public static void composePcm(String baseAudioFilePath, String pcmFilePath, String[] filePathList,
      int[] delayTimeList, FlowableEmitter<? super Integer> subscriber) {
    Log.i(TAG, "composePcm: ");
    boolean isBigEnding;
    if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
      isBigEnding = true;
    } else {
      isBigEnding = false;
    }
    boolean baseAudioFinish = false;
    byte[] baseAudioByteBuffer;
    byte[] extraAudioByteBuffer;
    byte[] outputByteArray;
    int baseAudioReadNumber;
    int extraAudioReadNumber;
    final int byteBufferSize = 1024;
    //减小背景音音量
    //放大人声，正常混合应该是0.5f
    final float bgScale = 0.3f;
    final float soundScale = 0.7f;
    baseAudioByteBuffer = new byte[byteBufferSize];
    extraAudioByteBuffer = new byte[byteBufferSize];
    outputByteArray = new byte[byteBufferSize];
    FileInputStream baseAudioInputStream = GetFileInputStreamFromFile(baseAudioFilePath);

    FileOutputStream composeAudioOutputStream = GetFileOutputStreamFromFile
        (pcmFilePath);
    boolean[] finishFlag = new boolean[filePathList.length];
    long[] audioOffsets = transTimeToOffset(delayTimeList);
    long audioCursor = 0;
    //大致思路是遍历背景音的数据，没有配音的部分就将数据乘0.3，有配音的数据就按3：7的比例合并
    //合并的数据基于short而不是byte

    try {
//            baseAudioInputStream.skip(55);
      for (int audioIndex = 0; audioIndex < filePathList.length + 1; audioIndex++) {

        //只读取baseAudio
        while (!baseAudioFinish) {
          baseAudioReadNumber = baseAudioInputStream.read(baseAudioByteBuffer);
          if (baseAudioReadNumber > 0) {
            for (int index = 0; index < baseAudioReadNumber / 2; index++) {
              short firstShort = GetShort(baseAudioByteBuffer[index * 2],
                  baseAudioByteBuffer[index * 2 + 1], isBigEnding);
              byte[] temp = GetBytes((short) (firstShort * bgScale), isBigEnding);
              outputByteArray[index * 2] = temp[0];
              outputByteArray[index * 2 + 1] = temp[1];
            }
            composeAudioOutputStream.write(outputByteArray, 0, baseAudioReadNumber);
          }
          audioCursor += baseAudioReadNumber;

          //baseAudio读取结束
          if (baseAudioReadNumber < 0) {
            baseAudioFinish = true;
            break;
          }
          //读取到需要混合extraAudio的位置
          if (audioIndex < audioOffsets.length && audioOffsets[audioIndex] < audioCursor) {
            break;
          }
        }

        //到配音的音频要插入的位置了
        if (!baseAudioFinish && audioOffsets[audioIndex] < audioCursor) {
          FileInputStream extraAudioInputStream = GetFileInputStreamFromFile(
              filePathList[audioIndex]);
          //混合两段音频
          while (!baseAudioFinish && !finishFlag[audioIndex]) {

            baseAudioReadNumber = baseAudioInputStream.read(baseAudioByteBuffer);
            extraAudioReadNumber = extraAudioInputStream.read(extraAudioByteBuffer);

            int minAudioReadNumber = Math.min(baseAudioReadNumber, extraAudioReadNumber);
            int maxAudioReadNumber = Math.max(baseAudioReadNumber, extraAudioReadNumber);

            if (baseAudioReadNumber < 0) {
              baseAudioFinish = true;
            }

            if (extraAudioReadNumber < 0) {
              finishFlag[audioIndex] = true;
            }

            int halfMinAudioReadNumber = minAudioReadNumber / 2;
            for (int index = 0; index < halfMinAudioReadNumber; index++) {
              short firstShort = GetShort(baseAudioByteBuffer[index * 2],
                  baseAudioByteBuffer[index * 2 + 1], isBigEnding);
              short secondShort = GetShort(extraAudioByteBuffer[index * 2],
                  extraAudioByteBuffer[index * 2 + 1], isBigEnding);
              byte[] temp = GetBytes((short) (firstShort * bgScale + secondShort * soundScale),
                  isBigEnding);
              outputByteArray[index * 2] = temp[0];
              outputByteArray[index * 2 + 1] = temp[1];
            }
            //一次buffer读取，可能背景音读的数据和配音读的数据不一致正常情况下应该是baseAudioReadNumber > extraAudioReadNumber
            if (baseAudioReadNumber != extraAudioReadNumber) {//现在的视频配音没有问题，不知道是不是这个分支都没走
              if (baseAudioReadNumber > extraAudioReadNumber) {//这一次读取的数据中，背景音多过配音
                //index应当是上面两个数据都有的情况下处理过的index，但是这里是0，感觉有问题
                for (int index = 0; index < maxAudioReadNumber / 2; index++) {
                  short firstShort = GetShort(baseAudioByteBuffer[index * 2],
                      baseAudioByteBuffer[index * 2 + 1], isBigEnding);
                  byte[] temp = GetBytes((short) (firstShort * bgScale), isBigEnding);
                  outputByteArray[index * 2] = temp[0];
                  outputByteArray[index * 2 + 1] = temp[1];
                }
              } else {//这一次读取的数据中，配音多过背景音
                //同上index有问题，maxAudioReadNumber没除以2，1short=2byte，感觉也有问题
                for (int index = 0; index < maxAudioReadNumber; index++) {
                  short firstShort = GetShort(extraAudioByteBuffer[index * 2],
                      extraAudioByteBuffer[index * 2 + 1], isBigEnding);
                  byte[] temp = GetBytes((short) (firstShort * soundScale), isBigEnding);
                  outputByteArray[index * 2] = temp[0];
                  outputByteArray[index * 2 + 1] = temp[1];
                }
              }
            }
            composeAudioOutputStream.write(outputByteArray, 0, maxAudioReadNumber);

            audioCursor += maxAudioReadNumber;
          }

          if (finishFlag[audioIndex]) {
            extraAudioInputStream.close();
          }
        }


      }
      baseAudioInputStream.close();
      composeAudioOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
      if (!subscriber.isCancelled()) {
        subscriber.onError(e);
      }
      return;
    }
    //将pcm转换成wav，现在这一步好像没用了
    AudioUtil.convertWaveFile(new File(pcmFilePath), new File(pcmFilePath.replace(".raw", ".wav")),
        AudioConstant.RecordSampleRate, 1);
    Log.i(TAG, "composePcm: finish");
    if (!subscriber.isCancelled()) {
      subscriber.onComplete();
    }
  }


  public static void pcm2Mp3(String baseAudioFilePath, String mp3FilePath,
      FlowableEmitter<? super Integer> emitter) {
    boolean isBigEnding;
    if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
      isBigEnding = true;
    } else {
      isBigEnding = false;
    }
    boolean baseAudioFinish = false;
    byte[] baseAudioByteBuffer;
    byte[] extraAudioByteBuffer;
    byte[] mp3Buffer;

    short resultShort;
    short[] outputShortArray;

    int baseAudioReadNumber;
    int extraAudioReadNumber;
    int outputShortArrayLength;
    final int byteBufferSize = 1024;
    long audioCursor = 0;

    baseAudioByteBuffer = new byte[byteBufferSize];
    extraAudioByteBuffer = new byte[byteBufferSize];
    mp3Buffer = new byte[(int) (7200 + (byteBufferSize * 1.25))];

    outputShortArray = new short[byteBufferSize / 2];
    File baseAudioFile = new File(baseAudioFilePath);
    FileInputStream baseAudioInputStream = GetFileInputStreamFromFile
        (baseAudioFile);

    FileOutputStream composeAudioOutputStream = GetFileOutputStreamFromFile
        (mp3FilePath);

    SimpleLame.init(AudioConstant.RecordSampleRate, AudioConstant.LameBehaviorChannelNumber,
        AudioConstant.BehaviorSampleRate, AudioConstant.LameBehaviorBitRate,
        AudioConstant.LameMp3Quality);
    long baseAudioLength = baseAudioFile.length();
    try {

      //只读取baseAudio
      while (!baseAudioFinish) {
        if (emitter.isCancelled()) {
          int flushResult = SimpleLame.flush(mp3Buffer);
          if (flushResult > 0) {
            composeAudioOutputStream.write(mp3Buffer, 0, flushResult);
          }
          composeAudioOutputStream.close();
          SimpleLame.close();
          baseAudioInputStream.close();
          return;
        }
        baseAudioReadNumber = baseAudioInputStream.read(baseAudioByteBuffer);
        //baseAudio读取结束
        if (baseAudioReadNumber < 0) {
          baseAudioFinish = true;
          baseAudioInputStream.close();
          break;
        }
        outputShortArrayLength = baseAudioReadNumber / 2;
        for (int index = 0; index < outputShortArrayLength; index++) {
          resultShort = GetShort(baseAudioByteBuffer[index * 2],
              baseAudioByteBuffer[index * 2 + 1], isBigEnding);

          outputShortArray[index] = (short) (resultShort * 0.5f);
        }

        if (outputShortArrayLength > 0) {
          int encodedSize = SimpleLame.encode(outputShortArray, outputShortArray,
              outputShortArrayLength, mp3Buffer);

          if (encodedSize > 0) {
            composeAudioOutputStream.write(mp3Buffer, 0, encodedSize);
          }
        }

        audioCursor += baseAudioReadNumber;
        Log.i(TAG, "pcm2Mp3: " + audioCursor);
        if (!emitter.isCancelled()) {
          int progress = (int) (audioCursor * 100f / baseAudioLength);
          emitter.onNext(progress);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      if (!emitter.isCancelled()) {
        emitter.onError(e);
      }

      return;
    }

    Log.i(TAG, "pcm2Mp3: flush");

    try {
      int flushResult = SimpleLame.flush(mp3Buffer);

      if (flushResult > 0) {
        composeAudioOutputStream.write(mp3Buffer, 0, flushResult);
      }
    } catch (Exception e) {
      Log.e(TAG, "释放ComposeAudio LameUtil异常");
    } finally {
      try {
        composeAudioOutputStream.close();
      } catch (Exception e) {
        Log.e(TAG, "关闭合成输出音频流异常");
      }

      SimpleLame.close();
    }

//        if (deleteSource) {
//            //TODO deleteSource
////            DeleteFile(firstAudioFilePath);
////            DeleteFile(secondAudioFilePath);
//        }
    if (!emitter.isCancelled()) {
      emitter.onComplete();
    }
  }

  public interface ComposeAudioInterface {

    void composeFail();

    void composeSuccess();
  }

  public static void compose(String baseAudioFilePath, String mp3FilePath, String[] filePathList,
      int[] delayTimeList, final ComposeAudioInterface composeAudioInterface) {
    boolean isBigEnding;
    if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
      isBigEnding = true;
    } else {
      isBigEnding = false;
    }
    boolean baseAudioFinish = false;
    byte[] baseAudioByteBuffer;
    byte[] extraAudioByteBuffer;
    byte[] mp3Buffer;

    short resultShort;
    short[] outputShortArray;

    int baseAudioReadNumber;
    int extraAudioReadNumber;
    int outputShortArrayLength;
    final int byteBufferSize = 1024;

    baseAudioByteBuffer = new byte[byteBufferSize];
    extraAudioByteBuffer = new byte[byteBufferSize];
    mp3Buffer = new byte[(int) (7200 + (byteBufferSize * 1.25))];

    outputShortArray = new short[byteBufferSize / 2];

    Handler handler = new Handler(Looper.getMainLooper());

    FileInputStream baseAudioInputStream = GetFileInputStreamFromFile
        (baseAudioFilePath);

    FileOutputStream composeAudioOutputStream = GetFileOutputStreamFromFile
        (mp3FilePath);

    SimpleLame.init(AudioConstant.RecordSampleRate, AudioConstant.LameBehaviorChannelNumber,
        AudioConstant.BehaviorSampleRate, AudioConstant.LameBehaviorBitRate,
        AudioConstant.LameMp3Quality);
    boolean[] finishFlag = new boolean[filePathList.length];
    long[] audioOffsets = transTimeToOffset(delayTimeList);
    long audioCursor = 0;

    try {
//            baseAudioInputStream.skip(55);
      for (int audioIndex = 0; audioIndex < filePathList.length + 1; audioIndex++) {

        //只读取baseAudio
        while (!baseAudioFinish) {
          baseAudioReadNumber = baseAudioInputStream.read(baseAudioByteBuffer);
          outputShortArrayLength = baseAudioReadNumber / 2;
          for (int index = 0; index < outputShortArrayLength; index++) {
            resultShort = GetShort(baseAudioByteBuffer[index * 2],
                baseAudioByteBuffer[index * 2 + 1], isBigEnding);

            outputShortArray[index] = (short) (resultShort * 0.5f);
          }

          if (outputShortArrayLength > 0) {
            int encodedSize = SimpleLame.encode(outputShortArray, outputShortArray,
                outputShortArrayLength, mp3Buffer);

            if (encodedSize > 0) {
              composeAudioOutputStream.write(mp3Buffer, 0, encodedSize);
            }
          }

          audioCursor += baseAudioReadNumber;

          //baseAudio读取结束
          if (baseAudioReadNumber < 0) {
            baseAudioFinish = true;
            break;
          }
          //读取到需要混合extraAudio的位置
          if (audioIndex < audioOffsets.length && audioOffsets[audioIndex] < audioCursor) {
            break;
          }
        }

        if (!baseAudioFinish && audioOffsets[audioIndex] < audioCursor) {
          FileInputStream extraAudioInputStream = GetFileInputStreamFromFile(
              filePathList[audioIndex]);
          //混合两段音频
          while (!baseAudioFinish && !finishFlag[audioIndex]) {

            baseAudioReadNumber = baseAudioInputStream.read(baseAudioByteBuffer);
            extraAudioReadNumber = extraAudioInputStream.read(extraAudioByteBuffer);

            int minAudioReadNumber = Math.min(baseAudioReadNumber, extraAudioReadNumber);
            int maxAudioReadNumber = Math.max(baseAudioReadNumber, extraAudioReadNumber);

            if (baseAudioReadNumber < 0) {
              baseAudioFinish = true;
            }

            if (extraAudioReadNumber < 0) {
              finishFlag[audioIndex] = true;
            }

            int halfMinAudioReadNumber = minAudioReadNumber / 2;

            outputShortArrayLength = maxAudioReadNumber / 2;

            for (int index = 0; index < halfMinAudioReadNumber; index++) {
              resultShort = AverageShort(baseAudioByteBuffer[index * 2],
                  baseAudioByteBuffer[index * 2 + 1], extraAudioByteBuffer[index * 2],
                  extraAudioByteBuffer[index * 2 + 1], isBigEnding);
              outputShortArray[index] = resultShort;
            }

            if (baseAudioReadNumber != extraAudioReadNumber) {
              if (baseAudioReadNumber > extraAudioReadNumber) {
                for (int index = 0; index < outputShortArrayLength; index++) {
                  resultShort = GetShort(baseAudioByteBuffer[index * 2],
                      baseAudioByteBuffer[index * 2 + 1], isBigEnding);

                  outputShortArray[index] = (short) (resultShort * 0.5f);
                }
              } else {
                for (int index = 0; index < outputShortArrayLength; index++) {
                  resultShort = GetShort(extraAudioByteBuffer[index * 2],
                      extraAudioByteBuffer[index * 2 + 1], isBigEnding);

                  outputShortArray[index] = (short) (resultShort * 0.5f);
                }
              }
            }

            if (outputShortArrayLength > 0) {
              int encodedSize = SimpleLame.encode(outputShortArray, outputShortArray,
                  outputShortArrayLength, mp3Buffer);

              if (encodedSize > 0) {
                composeAudioOutputStream.write(mp3Buffer, 0, encodedSize);
              }
            }

            audioCursor += maxAudioReadNumber;
          }

          if (finishFlag[audioIndex]) {
            extraAudioInputStream.close();
          }
        }


      }

    } catch (Exception e) {
      e.printStackTrace();
      handler.post(new Runnable() {
        @Override
        public void run() {
          if (composeAudioInterface != null) {
            composeAudioInterface.composeFail();
          }
        }
      });

      return;
    }

    handler.post(new Runnable() {
      @Override
      public void run() {
        //TODO composeProgress
      }
    });

    try {
      final int flushResult = SimpleLame.flush(mp3Buffer);

      if (flushResult > 0) {
        composeAudioOutputStream.write(mp3Buffer, 0, flushResult);
      }
    } catch (Exception e) {
      Log.e(TAG, "释放ComposeAudio LameUtil异常");
    } finally {
      try {
        composeAudioOutputStream.close();
      } catch (Exception e) {
        Log.e(TAG, "关闭合成输出音频流异常");
      }

      SimpleLame.close();
    }

//        if (deleteSource) {
//            //TODO deleteSource
////            DeleteFile(firstAudioFilePath);
////            DeleteFile(secondAudioFilePath);
//        }

    try {
      baseAudioInputStream.close();
    } catch (IOException e) {
      Log.e(TAG, "关闭合成输入音频流异常");
    }
    handler.post(new Runnable() {
      @Override
      public void run() {
        if (composeAudioInterface != null) {
          composeAudioInterface.composeSuccess();
        }
      }
    });
  }

  private static long[] transTimeToOffset(int[] delayTimeList) {
    long[] offset = new long[delayTimeList.length];
    for (int i = 0; i < delayTimeList.length; i++) {
      offset[i] = delayTimeList[i] / 1000 * AudioConstant.RecordDataNumberInOneSecond;
      Log.i(TAG, "transTimeToOffset: " + offset[i]);
    }
    return offset;
  }

  public static double timeToByteOffest(int delayTime) {

    return delayTime * byteNumPerMs();
  }

  public static int byteNumPerMs() {
    //TODO 计算每毫秒pcm大小
    return 0;
  }


  public static byte[] GetBytes(short shortValue, boolean bigEnding) {
    byte[] byteArray = new byte[2];

    if (bigEnding) {
      byteArray[1] = (byte) (shortValue & 0x00ff);
      shortValue >>= 8;
      byteArray[0] = (byte) (shortValue & 0x00ff);
    } else {
      byteArray[0] = (byte) (shortValue & 0x00ff);
      shortValue >>= 8;
      byteArray[1] = (byte) (shortValue & 0x00ff);
    }

    return byteArray;
  }

  public static short GetShort(byte firstByte, byte secondByte, boolean bigEnding) {
    short shortValue = 0;

    if (bigEnding) {
      shortValue |= (firstByte & 0x00ff);
      shortValue <<= 8;
      shortValue |= (secondByte & 0x00ff);
    } else {
      shortValue |= (secondByte & 0x00ff);
      shortValue <<= 8;
      shortValue |= (firstByte & 0x00ff);
    }

    return shortValue;
  }

  public static byte[] AverageShortByteArray(byte firstShortHighByte, byte firstShortLowByte,
      byte secondShortHighByte, byte secondShortLowByte,
      boolean bigEnding) {
    short firstShort = GetShort(firstShortHighByte, firstShortLowByte, bigEnding);
    short secondShort = GetShort(secondShortHighByte, secondShortLowByte, bigEnding);
    return GetBytes((short) (firstShort / 2 + secondShort / 2), bigEnding);
  }

  public static short AverageShort(byte firstShortHighByte, byte firstShortLowByte,
      byte secondShortHighByte, byte secondShortLowByte,
      boolean bigEnding) {
    short firstShort = GetShort(firstShortHighByte, firstShortLowByte, bigEnding);
    short secondShort = GetShort(secondShortHighByte, secondShortLowByte, bigEnding);
    return (short) (firstShort / 2 + secondShort / 2);
  }

  public static short WeightShort(byte firstShortHighByte, byte firstShortLowByte,
      byte secondShortHighByte, byte secondShortLowByte,
      float firstWeight, float secondWeight, boolean bigEnding) {
    short firstShort = GetShort(firstShortHighByte, firstShortLowByte, bigEnding);
    short secondShort = GetShort(secondShortHighByte, secondShortLowByte, bigEnding);
    return (short) (firstShort * firstWeight + secondShort * secondWeight);
  }

  public static FileInputStream GetFileInputStreamFromFile(String fileUrl) {
    FileInputStream fileInputStream = null;

    try {
      File file = new File(fileUrl);

      fileInputStream = new FileInputStream(file);
      if (fileUrl.toLowerCase().endsWith(".wav")) {
        fileInputStream.skip(44);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return fileInputStream;
  }

  public static FileInputStream GetFileInputStreamFromFile(File fileUrl) {
    FileInputStream fileInputStream = null;

    try {
      fileInputStream = new FileInputStream(fileUrl);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return fileInputStream;
  }

  public static FileOutputStream GetFileOutputStreamFromFile(String fileUrl) {
    FileOutputStream bufferedOutputStream = null;

    try {
      File file = new File(fileUrl);

      if (file.exists()) {
        file.delete();
      }

      file.createNewFile();

      bufferedOutputStream = new FileOutputStream(file);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return bufferedOutputStream;
  }
}
