package com.namibox.commonlib.model;

import com.google.gson.JsonObject;

/**
 * Create time: 2015/7/28.
 */
public class BaseNetResult {

  public int errcode;
  public String errmsg;
  public String status;
  public int error_code;
  public String error_desc;
  public String retcode;
  public String description;
  public String result_url;
  public String wx_share;
  public JsonObject data;

  @Override
  public String toString() {
    return "BaseNetResult{" +
        "errcode=" + errcode +
        ", errmsg='" + errmsg + '\'' +
        ", status='" + status + '\'' +
        ", error_code=" + error_code +
        ", error_desc='" + error_desc + '\'' +
        ", retcode='" + retcode + '\'' +
        ", description='" + description + '\'' +
        ", result_url='" + result_url + '\'' +
        ", wx_share='" + wx_share + '\'' +
        '}';
  }
}
