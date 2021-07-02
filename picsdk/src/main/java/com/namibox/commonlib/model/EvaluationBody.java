package com.namibox.commonlib.model;

import java.io.Serializable;

/**
 * Created by Roy.chen on 2017/7/6.
 */

public class EvaluationBody implements Serializable {

  public long homework_id;
  public String book_id;
  public String page_id;
  public Detail[] detail;
  public int experience_count;
  public int avg_score;
  public String extra;
  public String engineType;
  public String share_image;

  public static class Detail implements Serializable {

    public int exercise_id;
    public int pron;
    public int fluency;
    public int integrity;
    public String text;
    public int score;
    public String mp3name;
    public String engine_used;
  }

}
