package com.namibox.commonlib.model;

/**
 * Created by sunha on 2017/8/3 0003.
 */
public class PushTagBean {

  public String tag_key;
  public String tag_value;
  public String userId;
  public boolean relate_user;

  public PushTagBean(String tag_key, String tag_value, String userId,
      boolean relate_user) {
    this.tag_key = tag_key;
    this.tag_value = tag_value;
    this.userId = userId;
    this.relate_user = relate_user;
  }


}
