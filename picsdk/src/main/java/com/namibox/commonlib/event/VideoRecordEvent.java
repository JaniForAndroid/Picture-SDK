package com.namibox.commonlib.event;

/**
 * Created by Akkun on 2020/4/1.
 * web: http://www.zkyml.com
 * Des:
 */
public class VideoRecordEvent {

  private String msg;

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public VideoRecordEvent(String msg) {
    this.msg = msg;
  }
}
