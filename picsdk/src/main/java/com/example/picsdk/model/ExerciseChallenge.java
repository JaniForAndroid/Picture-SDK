package com.example.picsdk.model;

import java.util.List;

public class ExerciseChallenge extends BaseObject {

  public Data data;

  public static class Data {
    public long duration;
    public long milesson_id;
    public long item_id;
    public int total;
    public RatingRule rating_rule;
    public List<Exercise> exercises;
    public UserInfo user_id;
    public String eid;
  }

  public static class RatingRule {
    public RuleStar star;
  }

  public static class RuleStar {
    public Star zero_star;
    public Star first_star;
    public Star second_star;
    public Star third_star;
  }

  public static class Star {
    public int max;
    public int min;
  }

}
