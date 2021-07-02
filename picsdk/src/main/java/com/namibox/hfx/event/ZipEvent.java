package com.namibox.hfx.event;

import java.io.File;

/**
 * Created by sunha on 2017/5/19 0019.
 */

public class ZipEvent {

  public ZipEventType type;
  public int current;
  public int total;
  public File zipFile;

  public ZipEvent(int current, int total) {
    this.current = current;
    this.total = total;
    type = ZipEventType.PROGRESS;
  }

  public ZipEvent(File zipFile) {
    this.zipFile = zipFile;
    type = ZipEventType.RESULT;
  }

  public enum ZipEventType {
    PROGRESS,
    RESULT
  }
}
