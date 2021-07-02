package com.namibox.commonlib.model;

/**
 * author : feng
 * description ：logo水印
 * creation time : 19-9-7下午5:13
 */
public class LogoInfo {

  public String assets_url;
  public int assets_type;
  public LogoPosition point;

  public class LogoPosition {

    public float x;
    public float y;
    public float width;
    public float height;

    @Override
    public String toString() {
      return "LogoPosition{" +
          "x=" + x +
          ", y=" + y +
          ", width=" + width +
          ", height=" + height +
          '}';
    }
  }
}
