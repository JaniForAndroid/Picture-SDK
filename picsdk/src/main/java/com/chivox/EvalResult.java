package com.chivox;

import com.chivox.ParagraphResult.ResultBean.DetailsBean.SntDetailsBean;
import java.io.Serializable;
import java.util.List;

/**
 * Create time: 2016/7/9.
 */
public class EvalResult implements Serializable {

  public String command = "postaudioscore";
  public String type;
  public String result_type;
  public String content;
  public String scoreDisplay;
  public float score;
  public float pron;
  public float fluency;
  public float integrity;
  public List<Detail> detail;
  public String enginetype;
  public String url;
  public String localpath;

  public static class Detail implements Serializable {

    public String word;
    public String chWord;
    public String score;
    public List<SntDetailsBean> snt_details;
  }
}
