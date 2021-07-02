package com.namibox.commonlib.model;

/**
 * Create time: 19-2-21.
 */
public class LoginResult {
  public String retcode;
  public String message;
  public String msg;
  public String img_code;
  public String captcha_login_times;

  @Override
  public String toString() {
    return "{retcode: " + retcode + ", message: " + message
        + ", msg: " + msg + ", img_code: " + img_code + ", captcha_login_times: " + captcha_login_times + "}";
  }
}
