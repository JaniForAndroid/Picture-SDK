package com.namibox.commonlib.model;

/**
 * @author: Shelter
 * Create time: 2020/4/24, 16:43.
 */
public class CdnInfo {
  public String host;
  public long speed;
  public long duration;
  public String videoType;
  public String contentType;
  public long contentLength;
  public String eTag;
  public String responseUrl;
  public String originUrl;
  public String signal;
  public long delay = -1;

  @Override
  public String toString() {
    return "CdnInfo{" +
        "host='" + host + '\'' +
        ", speed=" + speed +
        ", duration=" + duration +
        ", videoType='" + videoType + '\'' +
        ", contentType='" + contentType + '\'' +
        ", contentLength=" + contentLength +
        ", eTag='" + eTag + '\'' +
        ", responseUrl='" + responseUrl + '\'' +
        ", originUrl='" + originUrl + '\'' +
        ", signal=" + signal +
        ", delay=" + delay +
        '}';
  }
}
