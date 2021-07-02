package com.example.exoaudioplayer.video.controller;

import android.graphics.Bitmap;

public interface IMediaPlayerControl {

  void start();

  void pause();

  long getDuration();

  long getDuration1();

  long getSize();

  long getCurrentPosition();

  void seekTo(long pos);

  boolean isPlaying();

  boolean isEnded();

  int getBufferedPercentage();

  void startFullScreen();

  void stopFullScreen();

  boolean isFullScreen();

  boolean isAlwaysFullScreen();

  void setMute(boolean isMute);

  boolean isMute();

  void setScreenScaleType(int screenScaleType);

  void setSpeed(float speed);

  float getSpeed();

  long getTcpSpeed();

  void replay(boolean resetPosition);

  void setMirrorRotation(boolean enable);

  Bitmap doScreenShot();

  int[] getVideoSize();

  void setRotation(float rotation);
}