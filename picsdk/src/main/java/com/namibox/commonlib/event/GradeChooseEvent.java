package com.namibox.commonlib.event;

/**
 * 年级选择弹框
 * @author zhangkx
 * @Date 2019/8/27 15:55
 */
public class GradeChooseEvent {
  public boolean isSide;

  public GradeChooseEvent(boolean isSide) {
    this.isSide = isSide;
  }
}
