package com.namibox.commonlib.model;

import java.io.Serializable;

/**
 * @author: Shelter
 * Create time: 2018/8/17, 17:07.
 */
public class ChineseEvalBody implements Serializable {

  public String book_id;
  public String mi_item_id;
  public int exs_id;
  public String match_id;
  public String engine_used;
  public String text;
  public String mp3name;
  public int pron;
  public int fluency;
  public int integrity;
  public int score;
}
