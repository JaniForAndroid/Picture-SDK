package com.namibox.hfx.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 绘本查看数据bean
 *
 * @author zhangkx
 * @Date 2018/9/20 16:50
 */
public class HuibenShowWork implements Serializable {

  /** 绘本ID */
  public String bookId;
  /** 绘本名 */
  public String bookName;
  /** 绘本类型 */
  public String contentType;
  /** 作者 */
  public String author;
  /** 评论URL */
  public String commentUrl;
  /**  */
  public long comment;
  /**  */
  public float starank;
  /** 配音数据 */
  public List<PagerDetail> data;

  /**
   * 绘本内容详情
   */
  public static class PagerDetail {
    /** 图片地址 */
    public String page_bg_img;
    /** 绘本内容 */
    public String exercise_text;
    /** 音频url */
    public String audio_url;
    /** 音频时长 */
    public float duration;
    /** 绘本评分 */
    public String score;
    /** 是否是评论页*/
    public boolean isCommentPage;
  }
}
