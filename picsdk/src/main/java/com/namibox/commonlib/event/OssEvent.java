package com.namibox.commonlib.event;

import java.io.File;

/**
 * Created by sunha on 2017/5/19 0019.
 */

public class OssEvent {

  public String objectKey;
  public OssEventType type;
  public File file;
  public long current;
  public long total;

  public OssEvent(File uploadFile, long current, long total) {
    type = OssEventType.PROGRESS;
    this.current = current;
    this.total = total;
    this.file = uploadFile;
  }

  public OssEvent(File file, String objectKey) {
    type = OssEventType.RESULT;
    this.file = file;
    this.objectKey = objectKey;
  }

  public enum OssEventType {
    PROGRESS,
    RESULT
  }
}
