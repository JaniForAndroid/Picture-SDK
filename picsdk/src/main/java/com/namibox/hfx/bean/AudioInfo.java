package com.namibox.hfx.bean;

import java.io.Serializable;

public class AudioInfo implements Serializable {
  public String audioId;
  public int duration;


  public AudioInfo(String audioId, int duration) {
    this.audioId = audioId;
    this.duration = duration;
  }
}
