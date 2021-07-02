package com.namibox.commonlib.model;

import java.util.List;

/**
 * Created by Roy.chen on 2017/6/28.
 */

public class Exercise {


  /**
   * error_desc : 成功获取评测题目！ exercises : [{"question":{"content":"Unit One","content_display":"Unit
   * One","audio":"https://wr.namibox.com/tina/static/lesson/milesson73/audio/box_0001.mp3"},"id":154041},{"question":{"content":"Let's
   * count!","content_display":"Let's count!","audio":"https://wr.namibox.com/tina/static/lesson/milesson73/audio/box_0002.mp3"},"id":154043},{"question":{"content":"Look!","content_display":"Look!","audio":"https://wr.namibox.com/tina/static/lesson/milesson73/audio/box_0004.mp3"},"id":154045},{"question":{"content":"Cool!","content_display":"Cool!","audio":"https://wr.namibox.com/tina/static/lesson/milesson73/audio/box_0005.mp3"},"id":154047},{"question":{"content":"How
   * many red marbles?","content_display":"How many red marbles?","audio":"https://wr.namibox.com/tina/static/lesson/milesson73/audio/box_0006.mp3"},"id":154049},{"question":{"content":"One,
   * two, three. Three red marbles.","content_display":"One, two, three. Three red
   * marbles.","audio":"https://wr.namibox.com/tina/static/lesson/milesson73/audio/box_0007.mp3"},"id":154051}]
   * error_code : 0 userinfo : {"experience_count":100,"userid":3127953,"isbuy":false,"join_count":9999,"buy_url":"http://google.com"}
   * params : {"endpoint":"oss-cn-hangzhou.aliyuncs.com","AccessKeyId":"STS.EpQmPKbYqihUEncJKVDyRFc1u","AccessKeySecret":"8juWQmYMgAXnJW8dmvtK55LqvJWXHyjFdZtTZT7LBAqG","objectKey":"user/3127953/oral_test/tape1b_001002/","Expiration":"2017-07-05T12:13:36Z","SecurityToken":"CAISngJ1q6Ft5B2yfSjIpLPkJur/j4ZQ3qq+R0jSrksDSPZ+qaaalzz2IH5Lf3ZuBuEdsfUwlWxU6P4SlqZ9QpleRUvbWpAptMsGrFj58mcHRnX2v9I+k5SANTW5EnyShb3WAYjQSNfaZY3iCTTtnTNyxr3XbCirW0ffX7SClZ9gaKZwPGy/diEUPMpKAQFgpcQGTymzU8ygKRn3mGHdIVN1sw5n8wNF5L+439eX52io7inzwfRHoJ/qcNr2LZtqNZJ+WdClmfMxa7HMymkSyWATr/oq0vQUoW2X54jNWAYB2XjcbbqIqO8IBRRie603F5RDqPXBjvBisoTR7d+plE0SY74ECHuOG9H8nJWcSPnIMN88dLz8KzHSwlI9k2M5W99OGoABP4Z8gEHv9RJYSFqOj2qysbpQ5Y9UJQAwL7bOPG09j7butmZEAeCjSCNQFXOm8msEiypMbh0p471AnhWqrpRLj7jMCYO/6QctTJhN4Z6JX9PXSZvNZgUVdcv483RSYtNfmEWR6lNtjU3bP6iY4pC8YVAjyngw46M4q1CQ3DIU0n8=","bucketName":"namibox-w"}
   */

  private String error_desc;
  private int error_code;
  private UserinfoBean userinfo;
  private String next_page_url;
  private String next_page_id;
  private String book_name;
  private String unit_title;
  private ParamsBean params;
  private List<ExercisesBean> exercises;

  public String getBook_name() {
    return book_name;
  }

  public void setBook_name(String book_name) {
    this.book_name = book_name;
  }

  public String getUnit_title() {
    return unit_title;
  }

  public void setUnit_title(String unit_title) {
    this.unit_title = unit_title;
  }

  public String getNext_page_id() {
    return next_page_id;
  }

  public void setNext_page_id(String next_page_id) {
    this.next_page_id = next_page_id;
  }

  public String getNext_page_url() {
    return next_page_url;
  }

  public void setNext_page_url(String next_page_url) {
    this.next_page_url = next_page_url;
  }

  public String getError_desc() {
    return error_desc;
  }

  public void setError_desc(String error_desc) {
    this.error_desc = error_desc;
  }

  public int getError_code() {
    return error_code;
  }

  public void setError_code(int error_code) {
    this.error_code = error_code;
  }

  public UserinfoBean getUserinfo() {
    return userinfo;
  }

  public void setUserinfo(UserinfoBean userinfo) {
    this.userinfo = userinfo;
  }

  public ParamsBean getParams() {
    return params;
  }

  public void setParams(ParamsBean params) {
    this.params = params;
  }

  public List<ExercisesBean> getExercises() {
    return exercises;
  }

  public void setExercises(List<ExercisesBean> exercises) {
    this.exercises = exercises;
  }

  public static class UserinfoBean {

    /**
     * experience_count : 100
     * userid : 3127953
     * isbuy : false
     * join_count : 9999
     * buy_url : http://google.com
     */

    private int experience_count;
    private int userid;
    private boolean isbuy;
    private int join_count;
    private String buy_url;
    private String rule_url;
    private String enginetype;
    private String product_id;
    private String period;
    private String btn_text;
    private String member_buy_url;
    private String member_btn_text;
    private String member_price;
    private boolean ordered_before;

    public String getBtn_text() {
      return btn_text;
    }

    public void setBtn_text(String btn_text) {
      this.btn_text = btn_text;
    }

    public boolean isOrdered_before() {
      return ordered_before;
    }

    public void setOrdered_before(boolean ordered_before) {
      this.ordered_before = ordered_before;
    }

    public String getPeriod() {
      return period;
    }

    public void setPeriod(String period) {
      this.period = period;
    }

    public int getExperience_count() {
      return experience_count;
    }

    public void setExperience_count(int experience_count) {
      this.experience_count = experience_count;
    }

    public int getUserid() {
      return userid;
    }

    public void setUserid(int userid) {
      this.userid = userid;
    }

    public boolean isIsbuy() {
      return isbuy;
    }

    public void setIsbuy(boolean isbuy) {
      this.isbuy = isbuy;
    }

    public int getJoin_count() {
      return join_count;
    }

    public void setJoin_count(int join_count) {
      this.join_count = join_count;
    }

    public String getBuy_url() {
      return buy_url;
    }

    public String getMember_buy_url() {
      return member_buy_url;
    }

    public String getMember_btn_text() {
      return member_btn_text;
    }

    public String getMember_price() {
      return member_price;
    }

    public void setBuy_url(String buy_url) {
      this.buy_url = buy_url;
    }

    public boolean isbuy() {
      return isbuy;
    }

    public String getRule_url() {
      return rule_url;
    }

    public void setRule_url(String rule_url) {
      this.rule_url = rule_url;
    }

    public String getEnginetype() {
      return enginetype;
    }

    public void setEnginetype(String enginetype) {
      this.enginetype = enginetype;
    }

    public String getProduct_id() {
      return product_id;
    }

    public void setProduct_id(String product_id) {
      this.product_id = product_id;
    }

    @Override
    public String toString() {
      return "UserinfoBean{" +
          "experience_count=" + experience_count +
          ", userid=" + userid +
          ", isbuy=" + isbuy +
          ", join_count=" + join_count +
          ", buy_url='" + buy_url + '\'' +
          ", rule_url='" + rule_url + '\'' +
          ", enginetype='" + enginetype + '\'' +
          ", product_id='" + product_id + '\'' +
          ", period='" + period + '\'' +
          ", ordered_before=" + ordered_before +
          '}';
    }
  }

  public static class ParamsBean {

    /**
     * endpoint : oss-cn-hangzhou.aliyuncs.com AccessKeyId : STS.EpQmPKbYqihUEncJKVDyRFc1u
     * AccessKeySecret : 8juWQmYMgAXnJW8dmvtK55LqvJWXHyjFdZtTZT7LBAqG objectKey :
     * user/3127953/oral_test/tape1b_001002/ Expiration : 2017-07-05T12:13:36Z SecurityToken :
     * CAISngJ1q6Ft5B2yfSjIpLPkJur/j4ZQ3qq+R0jSrksDSPZ+qaaalzz2IH5Lf3ZuBuEdsfUwlWxU6P4SlqZ9QpleRUvbWpAptMsGrFj58mcHRnX2v9I+k5SANTW5EnyShb3WAYjQSNfaZY3iCTTtnTNyxr3XbCirW0ffX7SClZ9gaKZwPGy/diEUPMpKAQFgpcQGTymzU8ygKRn3mGHdIVN1sw5n8wNF5L+439eX52io7inzwfRHoJ/qcNr2LZtqNZJ+WdClmfMxa7HMymkSyWATr/oq0vQUoW2X54jNWAYB2XjcbbqIqO8IBRRie603F5RDqPXBjvBisoTR7d+plE0SY74ECHuOG9H8nJWcSPnIMN88dLz8KzHSwlI9k2M5W99OGoABP4Z8gEHv9RJYSFqOj2qysbpQ5Y9UJQAwL7bOPG09j7butmZEAeCjSCNQFXOm8msEiypMbh0p471AnhWqrpRLj7jMCYO/6QctTJhN4Z6JX9PXSZvNZgUVdcv483RSYtNfmEWR6lNtjU3bP6iY4pC8YVAjyngw46M4q1CQ3DIU0n8=
     * bucketName : namibox-w
     */

    private String endpoint;
    private String AccessKeyId;
    private String AccessKeySecret;
    private String objectKey;
    private String Expiration;
    private String SecurityToken;
    private String bucketName;

    public String getEndpoint() {
      return endpoint;
    }

    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
      return AccessKeyId;
    }

    public void setAccessKeyId(String AccessKeyId) {
      this.AccessKeyId = AccessKeyId;
    }

    public String getAccessKeySecret() {
      return AccessKeySecret;
    }

    public void setAccessKeySecret(String AccessKeySecret) {
      this.AccessKeySecret = AccessKeySecret;
    }

    public String getObjectKey() {
      return objectKey;
    }

    public void setObjectKey(String objectKey) {
      this.objectKey = objectKey;
    }

    public String getExpiration() {
      return Expiration;
    }

    public void setExpiration(String Expiration) {
      this.Expiration = Expiration;
    }

    public String getSecurityToken() {
      return SecurityToken;
    }

    public void setSecurityToken(String SecurityToken) {
      this.SecurityToken = SecurityToken;
    }

    public String getBucketName() {
      return bucketName;
    }

    public void setBucketName(String bucketName) {
      this.bucketName = bucketName;
    }
  }

  public static class ExercisesBean {

    /**
     * question : {"content":"Unit One","content_display":"Unit One","audio":"https://wr.namibox.com/tina/static/lesson/milesson73/audio/box_0001.mp3"}
     * id : 154041
     */

    private QuestionBean question;
    private int id;

    private String unit_title;

    public String getUnit_title() {
      return unit_title;
    }

    public void setUnit_title(String unit_title) {
      this.unit_title = unit_title;
    }

    public QuestionBean getQuestion() {
      return question;
    }

    public void setQuestion(QuestionBean question) {
      this.question = question;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public static class QuestionBean {

      /**
       * content : Unit One
       * content_display : Unit One
       * audio : https://wr.namibox.com/tina/static/lesson/milesson73/audio/box_0001.mp3
       */

      private String content;
      private String content_display;
      private String audio;

      public String getContent() {
        return content;
      }

      public void setContent(String content) {
        this.content = content;
      }

      public String getContent_display() {
        return content_display;
      }

      public void setContent_display(String content_display) {
        this.content_display = content_display;
      }

      public String getAudio() {
        return audio;
      }

      public void setAudio(String audio) {
        this.audio = audio;
      }
    }
  }
}
