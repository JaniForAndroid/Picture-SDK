package com.namibox.hfx.bean;

import java.util.List;

public class DubVideoRes {

  public String itemid;
  //同itemid 绘本馆要用
  public String milesson_item_id;
  public List<Audio> audio;
  public String subtype;
  public String video;
  public String thumb_url;
  public String type;
  public String video_name;
  /**
   * 如果该字段存在，则为微课伴学订购关系的 id，该字段有可能为空字符串
   */
  public String tutorable_relation_id;
  /**新增分数计算规则 绘本馆用**/
  public DubVideoRatingRule rating_rule;

  public class Audio {

    //0 初始 1 初始可播放 2 播放中 3录音/播放完成
    public int status = 0;
    public boolean hasTested = false;
    public boolean showAnimate = false;
    //绘本馆要用
    public boolean hasVideoEnd = true;
    public float score = 0;

    public int index;
    public String name;
    public String chinese;
    public String audio_url;
    public float begin_time;
    public String english;
    public float end_time;
    public int startTime;
    public int duration;

  }

}


