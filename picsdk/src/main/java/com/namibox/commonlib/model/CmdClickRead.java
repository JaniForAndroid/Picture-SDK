package com.namibox.commonlib.model;

import com.google.gson.JsonElement;

/**
 * Create time: 2020/4/29.
 */
public class CmdClickRead extends BaseCmd {
  public boolean forcetape;
  public String bookid;
  public int isbuy;
  public boolean needcloseself;
  public String url;
  public String sdkid;
  public boolean experience;
  public boolean evaluation;
  public int oral_test_ready;
  //A：点读 B：磁带 C：动画 D：口语训练 E：评测 F：情景对话
  public String product_type;
  public boolean simple;
  public String params;
  public JsonElement alert_info;
  public String select_tab;
}
