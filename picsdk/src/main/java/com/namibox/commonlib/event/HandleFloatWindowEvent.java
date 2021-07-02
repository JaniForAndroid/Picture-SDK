package com.namibox.commonlib.event;

/**
 * Created by wzp on 2019/8/13
 */
public class HandleFloatWindowEvent {
  public static final int FW_STATUS_VISIBLE = 1;
  public static final int FW_STATUS_ENABLE = 2;

  public int fwStatus;
  public boolean fwFlag;

  public HandleFloatWindowEvent(int fwStatus,boolean fwFlag) {
    this.fwStatus = fwStatus;
    this.fwFlag = fwFlag;
  }
}
