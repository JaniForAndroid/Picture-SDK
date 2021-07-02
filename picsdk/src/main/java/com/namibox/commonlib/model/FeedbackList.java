package com.namibox.commonlib.model;

import java.util.List;

/**
 * Create time: 2017/12/9.
 */

public class FeedbackList {
  public int retcode;
  public String error_msg;
  public List<DataBean> data;

  public static class DataBean {
    public String error_type;
    public String error_code;

  }
}
