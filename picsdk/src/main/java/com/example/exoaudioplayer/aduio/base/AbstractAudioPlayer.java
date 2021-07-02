package com.example.exoaudioplayer.aduio.base;

import android.net.Uri;
import com.example.exoaudioplayer.aduio.listener.AbstractPlayerListener;
import com.example.exoaudioplayer.aduio.listener.AudioPlayerListener;
import com.google.android.exoplayer2.SimpleExoPlayer;

/**
 * 抽象的播放器，继承此接口扩展自己的播放器
 */
public class AbstractAudioPlayer implements AbstractPlayerListener {

  /**
   * 播放器事件回调
   */
  protected AudioPlayerListener callback;

  /**
   * 绑定VideoView
   */
  public void setPlayerCallBack(AudioPlayerListener playerEventListener) {
    this.callback = playerEventListener;
  }

  @Override
  public void initializePlayer() {

  }

  @Override
  public void play(Uri uri) {

  }

  @Override
  public void pause() {

  }

  @Override
  public void stop() {

  }

  @Override
  public boolean isPlaying() {
    return false;
  }

  @Override
  public void seekTo(long time) {

  }

  @Override
  public void reset() {

  }

  @Override
  public void releasePlayer() {

  }

  @Override
  public long getCurrentPosition() {
    return 0;
  }

  @Override
  public long getDuration() {
    return 0;
  }

  @Override
  public void setPlayWhenReady(boolean playWhenReady) {

  }

  @Override
  public void backward() {

  }

  @Override
  public void fastForward() {

  }

  @Override
  public void setSpeed(float speed) {

  }

  public SimpleExoPlayer getPlayer() {
    return null;
  }

  public void playPause() {

  }

  public void repeatPlay() {

  }

  public void play(Uri uri, boolean autoPlay) {

  }

  public void play(Uri uri, long startMs) {
  }

  public void play(Uri uri, long startMs, long stopMs) {
  }

  public void play(Uri[] uris) {
  }

  public Uri[] getUris() {
    return null;
  }
}
