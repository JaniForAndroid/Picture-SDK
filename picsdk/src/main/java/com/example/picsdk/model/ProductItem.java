package com.example.picsdk.model;

import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.List;

/**
 * author : feng
 * description ：单本绘本数据结构
 * creation time : 19-9-23上午10:12
 */
public class ProductItem{

  public long milesson_id;
  public long milesson_item_id;
  public String guide_audio;
  public Introduction introduction;
  public Bottom bottom;
  public BookLearning book_learning;
  public List<Challenge> challenge_list;
  public String product_name;
  public long max_star;
  public long user_star_num;

  public static class Introduction{

    public String learning_topic;
    public String story_introduction;
  }

  public static class Bottom{

    public int is_buy;
    public int is_free;
    public String btn_text;
    public JsonObject action;
  }

  public static class Challenge {
    public String challenge_name;
    public String task_type;
    public int is_locked;
    public int is_buy;
    public int star;
    public int progress = -1;
    public int max_progress = 4;
    public long milesson_item_id;
    public String url;
    public String image;
    public Command command_map;
  }

  public static class Command {
    public String matchid;
    public boolean support_oral_evaluation;
    public String transcribe;
    public String submiturl;
    public String book_id;
    public String matchname;
    public String transmissionparam;
    public String command;
    public String name;
    public String content_type;
    public String extra;
  }

  public static class BookLearning extends Challenge{

    public String text;
    public String chinese_name;
    public String thumb_url;
    public List<Property> property_list;
    public int link_num;

    public static class Property{

      public String text;
      public String num_str;
      public String unit;
    }

    public static class Link{

      public long item_id;
      public String type;
      public String title;
      public JsonObject data;
      public String dataString;
    }
  }
}
