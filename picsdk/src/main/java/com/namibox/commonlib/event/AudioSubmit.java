package com.namibox.commonlib.event;

/**
 * @Description 语音题提交
 * @CreateTime: 2020/5/6 15:04
 * @Author: zhangkx
 */
public class AudioSubmit {
  public boolean isAutoSubmit;
  public boolean isAutoStop;
  public int times;

  public AudioSubmit(boolean isAutoSubmit, boolean isAutoStop,int times) {
    this.isAutoStop = isAutoStop;
    this.times = times;
  }
}
