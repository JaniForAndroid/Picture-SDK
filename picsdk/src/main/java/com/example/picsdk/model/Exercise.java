package com.example.picsdk.model;

import java.util.ArrayList;
import java.util.List;

public class Exercise {
  public String type;
  public String sub_type;
  public Question question;
  public String id;
  public List<Option> options;
  public boolean answerCorrect;
  public List<Sequence> destination_sequence;
  public List<Sequence> source_sequence;
  public ArrayList<String> cartoon;
  public ArrayList<String> userAnsweList;
  public String user_answer;

  public static class Sequence {
    public String content;
    public int index;
    public String content_type;
    public int correctStart;
    public boolean isSelect = false;
    public boolean isCheck = false;
    public boolean isCorrect = false;
  }

}
