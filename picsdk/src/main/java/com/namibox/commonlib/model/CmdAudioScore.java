package com.namibox.commonlib.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Create time: 2020/4/13.
 */
public class CmdAudioScore extends BaseCmd {
  public String type;
  public String userid;
  public String enginetype = "offline_xs";
  public String coreType;
  public String parameters;
  public String content;
  public ArrayList<Page> pages;
  public String score_type;
  public boolean direct_upload;
  public float leftMargin;
  public float topMargin;
  public float widthPercent;
  public float heightPercent;
  public float alpha;
  public String localpath;
  public String activityid;
  public OssToken parmers;
  public String book_id;
  public int index;
  public String title;
  public String match_id;
  public String url;
  public String page_no;
  public String extra;

  public static class Page implements Serializable {
    public boolean isFinish;
    public String url;
    public int page_no;
    public String desc_text;
    public boolean is_submit;
    public String mi_item_id;
  }
}
