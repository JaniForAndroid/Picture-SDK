package com.namibox.hfx.bean;

/**
 * Created by ryan on 2015/1/20.
 */
public class FreeAudio {

  public double readcount;

  public int comment;

  public String subtitle;

  public int praise;

  public String workid;

  public String author;

  public String workname;

  public float starank;

  public int share;

  public String userwork;

  public boolean is_favorite;

  public String content_type;

  public boolean is_praise;

  public String icon;

  public String author_icon;

  public WXShare wxshare;

  public String comment_url;

  public String obj_type;

  public boolean autoplay;

  public class WXShare {

    public String groupcontent;
    public String grouptitile;
    public String doclink;
    public String imgurl;
    public String friendtitile;
  }

  public static class WorkUser {

    public int readcount;
    public String headimage;
    public float starankcount;
    public String pubdate;
    public String recommend;
    public String introduce;
    public String nickname;
    public int commentcount;
    public String cid;
    public String url;
    public String alias;
    public String v;
  }

}
