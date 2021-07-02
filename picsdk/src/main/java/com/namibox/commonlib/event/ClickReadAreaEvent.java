package com.namibox.commonlib.event;

/**
 * @author zhangkx
 * @Date 2019/3/7 11:55
 */
public class ClickReadAreaEvent {

  public int left;
  public int top;

  public ClickReadAreaEvent(int left, int top) {
    this.left = left;
    this.top = top;
  }
}
