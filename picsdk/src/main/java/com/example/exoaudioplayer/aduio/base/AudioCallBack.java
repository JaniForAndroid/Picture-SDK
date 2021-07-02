package com.example.exoaudioplayer.aduio.base;

import com.example.exoaudioplayer.aduio.listener.AudioPlayerListener;
import com.google.android.exoplayer2.ExoPlaybackException;

public class AudioCallBack implements AudioPlayerListener {
  @Override
  public void onPrepared() {

  }

  @Override
  public void onCompletion() {

  }

  @Override
  public void playUpdate(long currentTime, long bufferTime, long totalTime) {

  }

  @Override
  public void onSeekComplete() {

  }

  @Override
  public void playStateChange(boolean playWhenReady, int playbackState) {

  }

  @Override
  public void playError(ExoPlaybackException error) {

  }

  @Override
  public void onError(int what, int extra) {

  }

  @Override
  public void onInfo(int what, int extra) {

  }

  @Override
  public void onFocusChange(boolean hasFocus) {

  }

  @Override
  public void speedChanged(float speed) {

  }
}
