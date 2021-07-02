package com.namibox.commonlib.event;

/**
 * Created by wzp on 2019/8/9
 */
public class FWDestroyEvent {

  public boolean isDestroy;
  public boolean isRefresh;

  public FWDestroyEvent(boolean isDestroy) {
    this.isDestroy = isDestroy;
  }

  public FWDestroyEvent(boolean isDestroy,boolean isRefresh) {
    this.isDestroy = isDestroy;
    this.isRefresh = isRefresh;
  }
}
