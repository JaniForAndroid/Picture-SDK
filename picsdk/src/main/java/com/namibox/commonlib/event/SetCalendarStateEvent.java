package com.namibox.commonlib.event;

/**
 * @Description 日历开关事件
 * @CreateTime: 2020/3/27 13:48
 * @Author: zhangkx
 */
public class SetCalendarStateEvent {

  public boolean isOpen;

  public SetCalendarStateEvent(boolean isOpen) {
    this.isOpen = isOpen;
  }
}
