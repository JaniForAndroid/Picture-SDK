package com.namibox.commonlib.model;

/**
 * Create time: 2020/4/13.
 */
public class CmdImgChoose extends BaseCmd {
  public String url;
  public OssToken oss_token;
  public String site;
  public String obj_id;
  public boolean show_sample;
  public int max_upload = 1;
  public int width;
  public int height;
  public String mode;
  public boolean first_frame;
}
