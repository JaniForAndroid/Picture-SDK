package com.namibox.commonlib.event;

/**
 * Created by wzp on 2019/8/9
 */
public class FWMustHideEvent {

  public boolean isMustHide;
  public String hideFlag;
  public int hideActivityHashCode;

  public FWMustHideEvent(boolean isMustHide, String hideFlag,int hideActivityHashCode) {
    this.isMustHide = isMustHide;
    this.hideFlag = hideFlag;
    this.hideActivityHashCode = hideActivityHashCode;
  }
}
