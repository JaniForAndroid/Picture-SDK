package com.namibox.hfx.bean;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by ryan on 2015/1/20.
 */
public class Huiben {

  public int readcount;
  public String bookid;
  public String bookname;
  public String icon;
  //public String cdn;
  public String banner_image;
  public ArrayList<WorkUser> workuser;
  public ArrayList<BookAudio> bookaudio;
  public ArrayList<BookPage> bookpage;
  public WXShare wxshare;
  public String comment_url;
  public int comment;
  public boolean autoplay;
  public float starank;
  /**
   * 准确度加权值
   */
  public String pronproportion;
  /**
   * 完整度加权值
   */
  public String integrityproportion;
  /**
   * 流畅度加权值
   */
  public String fluencyproportion;
  public String content_type;
  public String subtitle;
  /**
   * 成绩显示形式 ，
   */
  public Map<String,String> scoreState;

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

  public static class BookAudio {

    public float duration;
    public String mp3_name;
    public String mp3_url;
  }

  public static class BookPage {

    public String page_name;
    public int mp3_index = -1;
    public String page_url;
    public String page_content;
    //add for ui
    public boolean isCommentPage;
    public String coreType;
  }

  public static class Params{
    public String endpoint;
    public String AccessKeyId;
    public String AccessKeySecret;
    public String objectKey;
    public String Expiration;
    public String SecurityToken;
    public String bucketName;
  }
}
