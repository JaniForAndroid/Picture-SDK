package com.namibox.commonlib.event;

import com.namibox.commonlib.model.SysConfig.GradeItem;

/**
 * Create time: 19-5-11.
 */
public class GradeChangeEvent {
  public GradeItem item;

  public GradeChangeEvent(GradeItem item) {
    this.item = item;
  }
}
