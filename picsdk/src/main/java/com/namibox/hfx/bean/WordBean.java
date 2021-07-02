package com.namibox.hfx.bean;

import java.io.Serializable;

/**
 * Created by Roy.chen on 2017/6/26.
 */

public class WordBean implements Serializable{

  private String word;
  private int score;

  public WordBean(String word, int score) {
    this.word = word;
    this.score = score;
  }

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }


  @Override
  public String toString() {
    return "WordBean{" +
        "word='" + word + '\'' +
        ", score='" + score + '\'' +
        '}';
  }
}
