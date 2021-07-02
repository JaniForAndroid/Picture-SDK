package com.example.exoaudioplayer.video.model;

public class MediaBuilder {

  public final boolean mute_play;
  public final boolean autoPlay;
  public final int seekTime;
  public final String title;
  public final String uri;
  public final String thumbnail;
  public final int duration;
  public final long size;
  public final boolean canSave;
  public final int heartNum;
  public final String notifyUrl;
  public final boolean showLock;
  public final String remoteLogo;
  public final boolean localLogo;
  public final int type;

  private MediaBuilder(Builder builer) {
    this.mute_play = builer.mute_play;
    this.autoPlay = builer.autoPlay;
    this.seekTime = builer.seekTime;
    this.title = builer.title;
    this.uri = builer.uri;
    this.thumbnail = builer.thumbnail;
    this.duration = builer.duration;
    this.size = builer.size;
    this.canSave = builer.canSave;
    this.heartNum = builer.heartNum;
    this.notifyUrl = builer.notifyUrl;
    this.showLock = builer.showLock;
    this.remoteLogo = builer.logo;
    this.type = builer.type;
    this.localLogo = builer.localLogo;
  }

  public static class Builder {

    private boolean mute_play;
    private boolean autoPlay;
    private int seekTime;
    private String title;
    private String uri;
    private String thumbnail;
    private int duration;
    private long size;
    private boolean canSave;
    private int heartNum;
    private String notifyUrl;
    private boolean showLock;
    private String logo;
    private boolean localLogo;
    private int type;

    public Builder setType(int type) {
      this.type = type;
      return this;
    }

    public Builder setMute_play(boolean mute_play) {
      this.mute_play = mute_play;
      return this;
    }

    public Builder setAutoPlay(boolean autoPlay) {
      this.autoPlay = autoPlay;
      return this;
    }

    public Builder setSeekTime(int seekTime) {
      this.seekTime = seekTime;
      return this;
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder setUri(String uri) {
      this.uri = uri;
      return this;
    }

    public Builder setThumbnail(String thumbnail) {
      this.thumbnail = thumbnail;
      return this;
    }

    public Builder setDuration(int duration) {
      this.duration = duration;
      return this;
    }

    public Builder setSize(long size) {
      this.size = size;
      return this;
    }

    public Builder setCanSave(boolean canSave) {
      this.canSave = canSave;
      return this;
    }

    public Builder setHeartNum(int heartNum) {
      this.heartNum = heartNum;
      return this;
    }

    public Builder setNotifyUrl(String notifyUrl) {
      this.notifyUrl = notifyUrl;
      return this;
    }

    public Builder setShowLock(boolean showLock) {
      this.showLock = showLock;
      return this;
    }

    public Builder setLogo(String logo) {
      this.logo = logo;
      return this;
    }

    public Builder setLocalLogo(boolean localLogo) {
      this.localLogo = localLogo;
      return this;
    }

    public MediaBuilder build() {
      return new MediaBuilder(this);
    }
  }
}
