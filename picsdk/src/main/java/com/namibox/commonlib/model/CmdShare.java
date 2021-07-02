package com.namibox.commonlib.model;

/**
 * Create time: 2020/4/29.
 */
public class CmdShare extends BaseCmd {
  public ShareInfo share_info;

  public static class ShareInfo {
    public String cover;
    public String report_url;
    public String title;
    public String subtitle;
    public WxShare wxshare;
  }

  public static class WxShare {
    public String share_content;
    public String share_friend;
    public String share_title;
    public String url_image;
    public String url_link;
    public String imgurl;
  }

}
