package com.namibox.commonlib.lockscreen;

import android.graphics.Bitmap;

public class AudioPlayEvent {
  public final static int PREVIOUS = 0;
  public final static int NEXT = 1;
  public final static int PAUSE = 2;
  public final static int PLAY = 3;
  public final static int PLAY_PAUSE = 4;
  public final static int LOCK_READY = 5;
  public final static int PREVIOUS_UNENABLED = 6;
  public final static int NEXT_UNENABLED = 7;
  public final static int PREVIOUS_ENABLED = 8;
  public final static int NEXT_ENABLED = 9;
  public final static int AUDIO_INFO = 10;
  public final static int CLEAR = 11;
  public final static int FIRST_INIT = 12;
  public final static int AUDIO_BOOK = 1000;
  public final static int AUDIO_WEB = 1001;
  public final static int AUDIO_ABS_WEB = 1002;
  public final static int AUDIO_MUSIC_PLAY = 1003;
  public int audioType;
  public Bitmap bitmapCover;
  public String audioName;
  public String currentTime;
  public String totalTime;
  public int progress;
  public boolean isPlaying;

  private int operation;

  public AudioPlayEvent(int operation, int audioType){
    this.operation = operation;
    this.audioType = audioType;
  }

  public int getOperation() {
    return operation;
  }
}
