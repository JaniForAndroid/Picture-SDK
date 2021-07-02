package com.example.picsdk.model;

import java.util.ArrayList;
import java.util.List;

public class WatchPic {
  public int active_start_num;
  public int exercise_correct;
  public int exercise_total;
  public int duration;
  public boolean is_history_best;
  public int first_win_points;
  public int total_points;
  public List<Errors> errors;
  public String title;
  public String url;

  public static class Errors {
    public int exercise_id;
    public String user_answer;
    public ArrayList<String> user_answer_sort;
  }
}
