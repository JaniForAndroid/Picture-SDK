package com.namibox.commonlib.event;

import android.support.v4.app.Fragment;

/**
 * Created by wzp on 2019/8/13
 */
public class FloatWindowPauseEvent {
  public Fragment currentFragment;

  public FloatWindowPauseEvent(Fragment currentFragment) {
    this.currentFragment = currentFragment;
  }
}
