package com.namibox.commonlib.event;

/**
 * author : feng
 * description ：
 * creation time : 20-1-7下午4:15
 */
public class AudioPlayStateEvent {

  public String audioId;
  public boolean isPlaying;
  public int progress = -1;
  public long currentTime, totalTime;

  public AudioPlayStateEvent(String audioId, boolean isPlaying) {
    this.audioId = audioId;
    this.isPlaying = isPlaying;
  }

  public AudioPlayStateEvent(String audioId, boolean isPlaying, int progress) {
    this.audioId = audioId;
    this.isPlaying = isPlaying;
    this.progress = progress;
  }

  public AudioPlayStateEvent(String audioId, boolean isPlaying, int progress, long totalTime, long currentTime) {
    this.audioId = audioId;
    this.isPlaying = isPlaying;
    this.progress = progress;
    this.currentTime = currentTime;
    this.totalTime = totalTime;
  }
}
