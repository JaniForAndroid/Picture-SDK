package com.chivox;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Create time: 2016/6/15.
 */
public class ChiShengPhrasesResult {

  public ResultBean result;
  public String audioUrl;

  public static class ResultBean {

    public float overall;
    public Fluency fluency;
    public float pron;
    public float integrity;
    public List<DetailsBean> details;
  }

  public static class Fluency {

    public float overall;
    public float pause;
    public float speed;
  }

  public static class DetailsBean {

    @SerializedName("char")
    public String word;
    public float score;
    public String text;

  }

}
