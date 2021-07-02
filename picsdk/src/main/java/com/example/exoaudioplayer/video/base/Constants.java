package com.example.exoaudioplayer.video.base;

public class Constants {
  //播放器的各种状态
  public static final int STATE_ERROR = -1;
  public static final int STATE_IDLE = 0;
  public static final int STATE_PREPARING = 1;
  public static final int STATE_PREPARED = 2;
  public static final int STATE_PLAYING = 3;
  public static final int STATE_PAUSED = 4;
  public static final int STATE_PLAYBACK_COMPLETED = 5;
  public static final int STATE_BUFFERING = 6;
  public static final int STATE_BUFFERED = 7;
  public static final int STATE_START_ABORT = 8;//开始播放中止

  public static final int PLAYER_NORMAL = 10;        // 普通播放器
  public static final int PLAYER_FULL_SCREEN = 11;   // 全屏播放器

  public static final int SCREEN_SCALE_DEFAULT = 0;
  public static final int SCREEN_SCALE_16_9 = 1;
  public static final int SCREEN_SCALE_4_3 = 2;
  public static final int SCREEN_SCALE_MATCH_PARENT = 3;
  public static final int SCREEN_SCALE_ORIGINAL = 4;
  public static final int SCREEN_SCALE_CENTER_CROP = 5;

  public static final int EXO = 0;
  public static final int MEDIAPLAYER = 1;

  public static final int SURFACE = 0;
  public static final int TEXTURE = 1;

  public static final String GUIDE = "guide";
  public static final String HEART_GUIDE = "heart_guide";


  //区分不同的type 初始化不同的播放器界面
  public static final int NOCONTROLLER_FRAGMENT = 0;
  public static final int WK_FRAGMENT = 1;
  public static final int NORMAL_FRAGMENT = 2;
  public static final int REVIEW_FRAGMENT = 3;
  public static final int VIDEOSHOW_FRAGMENT = 4;
  public static final int WXAD_FRAGMENT = 5;
  public static final int WK_YUNXIAO_FRAGMENT = 6;
  public static final int PIC_FRAGMENT = 7;
}
