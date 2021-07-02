package com.example.exoaudioplayer.aduio.listener;

import android.net.Uri;

/**
 * 抽象的播放器，继承此接口扩展自己的播放器
 */
public interface AbstractPlayerListener {

  /**
   * 初始化播放器实例
   */
  void initializePlayer();

  /**
   * 播放
   */
  void play(Uri uri);

  /**
   * 停止
   */
  void pause();

  /**
   * 停止
   */
  void stop();

  /**
   * 是否正在播放
   */
  boolean isPlaying();

  /**
   * 调整进度
   */
  void seekTo(long time);

  /**
   * 重置播放器
   */
  void reset();

  /**
   * 释放播放器
   */
  void releasePlayer();

  /**
   * 获取当前播放的位置
   */
  long getCurrentPosition();

  /**
   * 获取音频总时长
   */
  long getDuration();

  /**
   * 设置播放状态
   */
  void setPlayWhenReady(boolean playWhenReady);

  /**
   * 后退
   */
  void backward();

  /**
   * 前进
   */
  void fastForward();

  /**
   * 设置播放速度
   */
  void setSpeed(float speed);

//    /**
//     * 获取播放速度
//     */
//    float getSpeed();
}
