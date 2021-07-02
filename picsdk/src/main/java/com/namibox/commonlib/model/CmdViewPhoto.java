package com.namibox.commonlib.model;

import java.util.ArrayList;

/**
 * Create time: 2020/4/13.
 */
public class CmdViewPhoto extends BaseCmd {
  public int[] pic_ids;
  public boolean support_del;
  public String[] thumbnail;
  public String[] large_pic;
  public String[] original_pic;
  public int start_pos;
  public String object_id;
  public ArrayList<PhotoViewBtnConfig> status_bar;
  public String user_id;
  //public String post_content;
  public String post_url;
  public String head_img;
  public String quick_comment_object_type;
}
