package com.namibox.commonlib.event;

public class AudioPlayStateEvent1 {

  public String audioId;
  public boolean isPlaying;
  public int progress = -1;
  public long currentTime, totalTime;
  private static AudioPlayStateEvent1 instance;

  public static AudioPlayStateEvent1 getInstance() {
    if (instance == null) {
      synchronized (AudioPlayStateEvent1.class) {
        if (instance == null) {
          instance = new AudioPlayStateEvent1();
        }
      }
    }
    return instance;
  }

  public AudioPlayStateEvent1 setData(String audioId, boolean isPlaying, int progress, long totalTime,
      long currentTime) {
    this.audioId = audioId;
    this.isPlaying = isPlaying;
    this.progress = progress;
    this.currentTime = currentTime;
    this.totalTime = totalTime;
    return instance;
  }

}
