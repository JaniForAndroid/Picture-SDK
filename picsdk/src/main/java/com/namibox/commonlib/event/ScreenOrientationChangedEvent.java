package com.namibox.commonlib.event;

/**
 * Created by sunha on 2018/3/10 0010.
 */

public class ScreenOrientationChangedEvent {

  public ScreenOrientationChangedEvent(int orientation) {
    this.orientation = orientation;
  }

  public int orientation;
}
