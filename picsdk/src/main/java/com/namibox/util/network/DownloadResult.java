package com.namibox.util.network;

/**
 * @author: Shelter
 * Create time: 2020/1/3, 17:19.
 */
public class DownloadResult {

  public boolean success;
  public String host;
  public long speed;
  public long duration;
  public String originUrl;
  public String responseUrl;
  public long contentLength;
  public String contentType;
  public String eTag;

  @Override
  public String toString() {
    return "DownloadResult{" +
        "success=" + success +
        ", host='" + host + '\'' +
        ", speed=" + speed +
        ", duration=" + duration +
        ", originUrl='" + originUrl + '\'' +
        ", responseUrl='" + responseUrl + '\'' +
        ", contentLength=" + contentLength +
        ", contentType='" + contentType + '\'' +
        ", eTag='" + eTag + '\'' +
        '}';
  }
}
