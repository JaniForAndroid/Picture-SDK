package com.namibox.commonlib.model;

import java.util.List;

/**
 * Create time: 2015/8/25.
 */
public class Work {

  public List<WorksEntity> works;

  public static class WorksEntity {

    public String sectionname;
    public List<SectionEntity> section;
  }

  public static class SectionEntity {

    public String id;
    public String bookid;
    public String img_src;
    public String title;
    public String status;
    public String pubdate;
    public String link_url;
    public String introduce;
    public int readcount;
    public int commentcount;
    public float star;
    public boolean openview;
    public boolean online;
    public String content_type;
    public String template;
    public String mp4_url;
    public String hls_url;
    public videoSet video_set;
    public float template_ratio;
    public int keeplight;
    public int lighteness;
    public String matchname;
  }

  public class videoSet {

    public float template_ratio;
    public int keeplight;
    public int lighteness;
  }

}
