package com.namibox.hfx.bean;

/**
 * Created by sunha on 2016/12/30 0030.
 */

public class RxEvent {

  public int index;
  public int size;
  //0 视频下载 1 音频下载 2 视频转码
  public int type;
  public int progress;
  //0 进行中 1 成功 2 失败
  public int status;
  //视频下载大小
  public long totalSize;
  public long currentSize;

  @Override
  public String toString() {
    return "RxEvent{" +
        "currentSize=" + currentSize +
        ", index=" + index +
        ", size=" + size +
        ", type=" + type +
        ", progress=" + progress +
        ", status=" + status +
        ", totalSize=" + totalSize +
        '}';
  }
}
