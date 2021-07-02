package com.example.picsdk.model;

/**
 * author : feng
 * creation time : 19-10-10上午11:38
 */
public class WordExercise {

  public int duration;
  public Question question;
  public int id;

  public static class Question {

    public int index;
    public String vocabulary;
    public String chinese;
    public String image;
    public String phonetic;
    public String audio;
  }
}
