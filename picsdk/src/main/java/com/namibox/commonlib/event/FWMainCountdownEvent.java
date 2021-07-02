package com.namibox.commonlib.event;

/**
 * Created by wzp on 2019/8/9
 */
public class FWMainCountdownEvent {

  public boolean startOrStop;
  public long hideTime;
  public long showTime;

  public FWMainCountdownEvent(boolean startOrStop) {
    this.startOrStop = startOrStop;
  }

  public FWMainCountdownEvent(boolean startOrStop, long hideTime, long showTime) {
    this.startOrStop = startOrStop;
    this.hideTime = hideTime;
    this.showTime = showTime;
  }
}
