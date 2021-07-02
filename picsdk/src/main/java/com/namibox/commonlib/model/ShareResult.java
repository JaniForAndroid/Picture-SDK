package com.namibox.commonlib.model;

/**
 * Created by sunha on 2017/12/18 0018.
 */

public class ShareResult {

  public int error_code;
  public DataBean data;

  public static class DataBean {

    public String content;
    public String use_url;
  }
}
