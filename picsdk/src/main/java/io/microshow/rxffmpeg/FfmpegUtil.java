package io.microshow.rxffmpeg;

import android.util.Log;
import com.namibox.util.FileUtil;
import io.reactivex.Flowable;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * RxFFmpeg 命令支持
 * Created by zky on 2020/4/5.
 */
public class FfmpegUtil {

  public static final String TAG = "FFmpegCmd";
  public static final String VIDEO_TYPE = "libx264";
  public static final String AUDIO_TYPE = "libfdk_aac";
  public static final String PRESET = "ultrafast";
  public static final int outWidth = 640;
  public static final int outHeight = 360;

  public static void openDebugLog(boolean open) {
    RxFFmpegInvoke.getInstance().setDebug(open);
  }

  /**
   * 视频添加模糊指令集合
   *
   * @param sourceVideoPath s
   * @param desVideoPath d
   * @return r
   */
  private static String[] getBoxblur(String sourceVideoPath, String desVideoPath) {
    RxFFmpegCommandList cmdList = new RxFFmpegCommandList();
    cmdList.append("-i");
    cmdList.append(sourceVideoPath);
    cmdList.append("-vf");
    cmdList.append("boxblur=5:1");
    cmdList.append("-preset");
    cmdList.append("superfast");
    cmdList.append(desVideoPath);
    return cmdList.build();
  }

  /**
   * 获取指令集合
   *
   * @param cmdString c
   * @return r
   */
  public static String[] getCommands(String cmdString) {
    return cmdString.split(" ");
  }

  /**
   * 视频合成
   *
   * @param videos s
   * @param outVideoPath d
   * @return r
   */
  public static Flowable<RxFFmpegProgress> mergeByLc(List<String> videos, String sourceListPath,
      String outVideoPath) {
    writeTxtToFile(videos, sourceListPath);
    String cmdStr = "ffmpeg -y -f concat -safe 0 -i " + sourceListPath + " -c copy " + outVideoPath;
    String[] commands = getCommands(cmdStr);
    return RxFFmpegInvoke.getInstance().runCommandRxJava(commands);
  }

  /**
   * 视频添加模糊
   *
   * @param sourceVideoPath s
   * @param desVideoPath d
   * @return r
   */
  public static Flowable<RxFFmpegProgress> AddBoxBlur(String sourceVideoPath, String desVideoPath) {
    String[] commands = getBoxblur(sourceVideoPath, desVideoPath);
    return RxFFmpegInvoke.getInstance().runCommandRxJava(commands);
  }

  /**
   * 视频添加动态水印
   *
   * @param sourceVideoPath s
   * @param desVideoPath d
   * @return r
   */
  public static Flowable<RxFFmpegProgress> AddGifMark(String sourceVideoPath, String desVideoPath) {
    RxFFmpegCommandList cmdList = new RxFFmpegCommandList();
    cmdList.append("-i");
    cmdList.append(sourceVideoPath);
//    水印
    cmdList.append("-ignore_loop");
    cmdList.append("0");
    cmdList.append("-i");
    cmdList.append("http://img.zcool.cn/community/017b775935562da8012193a351e64d.gif");
//    cmdlist.append("-i");
//    cmdlist.append(image2);
    cmdList.append("-filter_complex");
    cmdList.append(
        "[1:v]scale=100:100[img1];[2:v]scale=1280:720[img2];[0:v][img1]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2[bkg];[bkg][img2]overlay=0:0");
//    输出文件
    cmdList.append(desVideoPath);
    String[] commands = cmdList.build();
    return RxFFmpegInvoke.getInstance().runCommandRxJava(commands);
  }

  /**
   * 分离音视频
   *
   * @param sourceVideoPath s
   * @param desAudioPath d
   * @return r
   */
  public static Flowable<RxFFmpegProgress> splitVideoAudio(String sourceVideoPath,
      String desAudioPath) {
    RxFFmpegCommandList cmdlist = new RxFFmpegCommandList();
    cmdlist.append("-i");
    cmdlist.append(sourceVideoPath);
    cmdlist.append("-f");
    cmdlist.append("mp3");
    cmdlist.append("-vn");
    cmdlist.append(desAudioPath);
    String[] commands = cmdlist.build();
    return RxFFmpegInvoke.getInstance().runCommandRxJava(commands);
  }

  /**
   * 截取音视频
   *
   * @param sourceMediaPath s
   * @param desMediaPath d
   * @param startTime 12(单位s)或者"00:00:12" 从12秒开始
   * @param endTime 20(单位s) 截取20s或者"00:00:20" 截取到00:20
   */
  public static Flowable<RxFFmpegProgress> subMediaSize(String sourceMediaPath,
      String desMediaPath, String startTime, String endTime) {
    RxFFmpegCommandList cmdList = new RxFFmpegCommandList();
    cmdList.append("-ss");
    cmdList.append(startTime);
    cmdList.append("-t");
    cmdList.append(endTime);
    cmdList.append("-accurate_seek");
    cmdList.append("-i");
    cmdList.append(sourceMediaPath);
    cmdList.append("-codec");
    cmdList.append("copy");
    cmdList.append("-avoid_negative_ts");
    cmdList.append("1");
    cmdList.append(desMediaPath);
    String[] commands = cmdList.build();
    return RxFFmpegInvoke.getInstance().runCommandRxJava(commands);
  }

  public static Flowable<RxFFmpegProgress> wav2Audio(String inFile, String outFile) {
    String[] commands = new String[]{
        "ffmpeg", "-i", inFile, "-acodec", AUDIO_TYPE, outFile};
    return RxFFmpegInvoke.getInstance().runCommandRxJava(commands);
  }


  /**
   * @param inFile 输入文件
   * @param outFile 输出文件
   * @param crf 码率控制(0-51)0最好 23默认
   * @param width 输出宽度
   * @param height 输出高度
   */
  public static Flowable<RxFFmpegProgress> transcode(String inFile, String outFile, int crf,
      int width, int height) {
    String size = "640*360";
    String vf = "";

    //-vf 指定滤镜 pad是为视频加黑边，保证分辨率一致
    if (width / (float) height > 16 / 9f) {
      //上下加黑白
      vf = "pad=iw:iw*9/16:(ow-iw)/2:(oh-ih)/2";
    } else {
      //两边加黑边
      vf = "pad=ih*16/9:ih:(ow-iw)/2:(oh-ih)/2";
    }
    //-crf 取值范围为0~51，其中0为无损模式，数值越大，画质越差，生成的文件越小，18-28是比较合理的值
    //移动设备上不宜设置固定的码率，会导致转码时间过长，用-crf和ultrafast的preset配合可以以文件大小换转码效率
    String[] argv = new String[]{
        "ffmpeg", "-y", "-i", inFile,
        "-vcodec", VIDEO_TYPE, "-acodec", AUDIO_TYPE,
        "-crf", crf + "", "-vf", vf, "-preset", PRESET,
        "-f", "mp4", "-s", size, outFile};
    return RxFFmpegInvoke.getInstance().runCommandRxJava(argv);
  }


  public static void cutVideo(double startTime, double endTime, String inFile,
      String outFile, RxFFmpegSubscriber rxFFmpegSubscriber) {
    double duration = endTime - startTime;
//        String cmd = "ffmpeg -ss " + startTime / 1000 +
//                " -i " + inFile +
//                " -vcodec copy -acodec copy" +
//                " -t " + duration / 1000 + " "
//                + outFile;
    String[] argv = new String[]{"ffmpeg", "-ss", startTime / 1000 + "",
        "-y", "-i", inFile, "-vcodec", "copy", "-acodec", "copy", "-t", duration / 1000 + "",
        outFile};
    RxFFmpegInvoke.getInstance().runCommandRxJava(argv).subscribe(rxFFmpegSubscriber);
  }

  public static Flowable<RxFFmpegProgress> composeVideo(String videoFile, String audioFile,
      String outFile) {
    //拼接音视频
    String[] argv = new String[]{
        "ffmpeg", "-i", videoFile, "-i", audioFile,
        "-c", "copy", "-map", "0:v:0", "-map", "1:a:0", "-f", "mp4", outFile};
    return RxFFmpegInvoke.getInstance().runCommandRxJava(argv);
  }

  public static void composeVideo2(String videoFile, String audioFile,
      String outFile, RxFFmpegSubscriber rxFFmpegSubscriber) {
    //拼接音视频，两个输入
    //-map 0:v:0 第一个输入源的第一个视频流
    //1:a:0 第二个输入源的第一个音频流
    String[] argv = new String[]{
        "ffmpeg", "-y", "-i", videoFile, "-i", audioFile,
//                "-c", "copy",
        "-map", "0:v:0", "-map", "1:a:0", "-vcodec", "copy", "-acodec", "copy", "-f", "mp4",
        outFile};
    RxFFmpegInvoke.getInstance().runCommandRxJava(argv).subscribe(rxFFmpegSubscriber);
  }

  /**
   * 将视频中的音轨转换为单声道pcm数据，声道数也需要和视频配音中录的pcm数据声道数对应
   *
   * @param sampleRate 转换成的pcm的采样率，这个要和视频配音中录的pcm数据的采样率对应（16k），合成的时候才没问题
   */
  public static void video2Pcm(String inFile, String outFile, int sampleRate,
      boolean isBigEndian, RxFFmpegSubscriber rxFFmpegSubscriber) {
    //字节序
    String endian;
    if (isBigEndian) {
      endian = "s16be";
    } else {
      endian = "s16le";
    }
    //-ac指定声道数
    String[] argv = new String[]{
        "ffmpeg", "-y", "-i", inFile, "-f", endian,
        "-ar", sampleRate + "", "-ac", "1", outFile};
    RxFFmpegInvoke.getInstance().runCommandRxJava(argv).subscribe(rxFFmpegSubscriber);
  }

  /**
   * 获取媒体文件信息
   */
  private static String getMediaInfo(String filePath) {
    return RxFFmpegInvoke.getInstance().getMediaInfo(filePath);
  }

  /**
   * 终止ffm操作
   */
  public static void stopFFmpeg() {
    RxFFmpegInvoke.getInstance().exit();
  }


  /**
   * 此类用于生成合并视频所需要的文档
   *
   * @param strcontent 视频路径集合
   * @param strFilePath 生成的文件名地址
   */
  public static void writeTxtToFile(List<String> strcontent, String strFilePath) {
    FileUtil.deleteFile(strFilePath);
    // 每次写入时，都换行写
    StringBuilder strContent = new StringBuilder();
    for (int i = 0; i < strcontent.size(); i++) {
      strContent.append("file \'").append(strcontent.get(i)).append("\'\r\n");
    }
    try {
      File file = new File(strFilePath);
      boolean newFile = file.createNewFile();
      if (newFile) {
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        raf.seek(file.length());
        raf.write(strContent.toString().getBytes());
        raf.close();
        Log.e("TestFile", "写入成功:" + strFilePath);
      }
    } catch (Exception e) {
      Log.e("TestFile", "Error on write File:" + e);
    }
  }

  /**
   * 基本选项:
   * -formats    输出所有可用格式
   * -f  fmt    指定格式(音频或视频格式)
   * -i filename    指定输入文件名，在linux下当然也能指定:0.0(屏幕录制)或摄像头
   * -y    覆盖已有文件
   * -t duration    记录时长为t
   * -fs limit_size    设置文件大小上限
   * -ss time_off    从指定的时间(s)开始，  [-]hh:mm:ss[.xxx]的格式也支持
   * -itsoffset time_off    设置时间偏移(s)，该选项影响所有后面的输入文件。该偏移被加到输入文件的时戳，定义一个正偏移意味着相应的流被延迟了 offset秒。 [-]hh:mm:ss[.xxx]的格式也支持
   * -title string    标题
   * -timestamp time    时间戳
   * -author  string    作者
   * -copyright string    版权信息
   * -comment string    评论
   * -album string    album名
   * -v verbose    与log相关的
   * -target type    设置目标文件类型("vcd", "svcd",  "dvd", "dv", "dv50", "pal-vcd", "ntsc-svcd", ...)
   * -dframes number    设置要记录的帧数
   * 视频选项:
   * -b    指定比特率(bits/s)，似乎ffmpeg是自动VBR的，指定了就大概是平均比特率
   * -bitexact    使用标准比特率
   * -vb    指定视频比特率(bits/s)
   * -vframes  number    设置转换多少桢(frame)的视频
   * -r rate    帧速率(fps) （可以改，确认非标准桢率会导致音画不同步，所以只能设定为15或者29.97）
   * -s size    指定分辨率 (320x240)
   * -aspect aspect    设置视频长宽比(4:3, 16:9 or 1.3333, 1.7777)
   * -croptop  size    设置顶部切除尺寸(in pixels)
   * -cropbottom size    设置底部切除尺寸(in pixels)
   * -cropleft size    设置左切除尺寸 (in pixels)
   * -cropright size    设置右切除尺寸 (in pixels)
   * -padtop size    设置顶部补齐尺寸(in  pixels)
   * -padbottom size    底补齐(in pixels)
   * -padleft size    左补齐(in pixels)
   * -padright size    右补齐(in pixels)
   * -padcolor color    补齐带颜色(000000-FFFFFF)
   * -vn    取消视频
   * -vcodec  codec    强制使用codec编解码方式('copy' to copy stream)
   * -sameq    使用同样视频质量作为源（VBR）
   * -pass n    选择处理遍数（1或者2）。两遍编码非常有用。第一遍生成统计信息，第二遍生成精确的请求的码率
   * -passlogfile file    选择两遍的纪录文件名为file
   * -newvideo    在现在的视频流后面加入新的视频流
   *
   * 高级视频选项
   * -pix_fmt  format    set pixel format, 'list' as argument shows all the pixel formats supported
   * -intra    仅适用帧内编码
   * -qscale q    以<数值>质量为基础的VBR，取值0.01-255，约小质量越好
   * -loop_input    设置输入流的循环数(目前只对图像有效)
   * -loop_output    设置输出视频的循环数，比如输出gif时设为0表示无限循环
   * -g  int    设置图像组大小
   * -cutoff int    设置截止频率
   * -qmin int    设定最小质量，与-qmax（设定最大质量）共用，比如-qmin 10 -qmax 31
   * -qmax int    设定最大质量
   * -qdiff int    量化标度间最大偏差 (VBR)
   * -bf  int    使用frames B 帧，支持mpeg1,mpeg2,mpeg4
   * 音频选项:
   * -ab    设置比特率(单位：bit/s，也许老版是kb/s)前面-ac设为立体声时要以一半比特率来设置，比如192kbps的就设成96，转换 默认比特率都较小，要听到较高品质声音的话建议设到160kbps（80）以上。
   * -aframes number    设置转换多少桢(frame)的音频
   * -aq  quality    设置音频质量 (指定编码)
   * -ar rate    设置音频采样率 (单位：Hz)，PSP只认24000
   * -ac channels    设置声道数，1就是单声道，2就是立体声，转换单声道的TVrip可以用1（节省一半容量），高品质的DVDrip就可以用2
   * -an    取消音频
   * -acodec codec    指定音频编码('copy'  to copy stream)
   * -vol volume    设置录制音量大小(默认为256) <百分比> ，某些DVDrip的AC3轨音量极小，转换时可以用这个提高音量，比如200就是原来的2倍
   * -newaudio    在现在的音频流后面加入新的音频流
   * 字幕选项:
   * -sn    取消字幕
   * -scodec  codec    设置字幕编码('copy' to copy stream)
   * -newsubtitle    在当前字幕后新增
   * -slang code    设置字幕所用的ISO 639编码(3个字母)
   * Audio/Video 抓取选项:
   * -vc channel    设置视频捕获通道(只对DV1394)
   * -tvstd  standard    设
   *
   * 转换为flv:
   *     ffmpeg -i test.mp3 -ab 56 -ar 22050 -b 500 -r 15 -s 320x240 test.flv
   *     ffmpeg -i test.wmv -ab 56 -ar 22050 -b 500 -r 15 -s 320x240 test.flv
   *
   *  转换文件格式的同时抓缩微图：
   *     ffmpeg -i "test.avi" -y -f image2 -ss 8 -t 0.001 -s 350x240 'test.jpg'
   *
   *   对已有flv抓图：
   *     ffmpeg -i "test.flv" -y -f image2 -ss 8 -t 0.001 -s 350x240 'test.jpg'
   *
   *   转换为3gp:
   *     ffmpeg -y -i test.mpeg -bitexact -vcodec h263 -b 128 -r 15 -s 176x144 -acodec aac -ac 2 -ar 22500 -ab 24 -f 3gp test.3gp
   *     ffmpeg -y -i test.mpeg -ac 1 -acodec amr_nb -ar 8000 -s 176x144 -b 128 -r 15 test.3gp
   */
}
