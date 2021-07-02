package com.namibox.commonlib.model;

/**
 * Create time: 2020/4/13.
 */
public class CmdImgUpload extends BaseCmd {
  public String url;
  public OssToken oss_token;
  public String site;
  public String obj_id;
  public boolean show_sample;
  /**
   * 1: 作业拍照 2: 头像拍照
   */
  public int type = 1;
  public int max_upload = 1;
  public int width;
  public int height;
}
