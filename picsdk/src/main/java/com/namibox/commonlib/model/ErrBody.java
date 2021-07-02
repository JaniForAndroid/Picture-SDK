package com.namibox.commonlib.model;

import java.io.Serializable;

/**
 * @author: Shelter
 * Create time: 2018/6/19, 11:16.
 */
public class ErrBody implements Serializable {
  public String device_brand;
  public String device_model;
  public String userid;
  public String network;
  public String os_version;
  public String app_version;
  public String channel;
  public String cache_size;
  public String storage;
  public String memory;
  public String page_no;
  public String bookid;
  public int errcode;
  public String errmsg;
  public String time;
  public String enginetype;
  public String coretype;
  public boolean switchtonative;
  public boolean isbuy;
  public boolean isSingEngineInit;
}
