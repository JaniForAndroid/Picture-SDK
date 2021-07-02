package com.chivox;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Create time: 2016/6/15.
 */
public class ChiShengResult {

  public ResultBean result;
  public String audioUrl;

  public static class ResultBean {

    public float overall;
    public float fluency;
    public float pron;
    public float integrity;
    public List<DetailsBean> details;
  }

  public static class DetailsBean {

    @SerializedName("char")
    public String word;
    public float score;
    public String text;

  }

}
