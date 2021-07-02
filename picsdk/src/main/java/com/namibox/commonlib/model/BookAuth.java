package com.namibox.commonlib.model;

/**
 * Create time: 2019/12/13.
 */
public class BookAuth {
  public int error_code;
  public String errmsg;
  public String auth_user;
  public int useNewAuthInterface;
  public boolean isBuy;

  @Override
  public String toString() {
    return "BookAuth{" +
        "error_code=" + error_code +
        ", errmsg='" + errmsg + '\'' +
        ", auth_user='" + auth_user + '\'' +
        ", useNewAuthInterface='" + useNewAuthInterface + '\'' +
        ", isBuy='" + isBuy + '\'' +
        '}';
  }
}
