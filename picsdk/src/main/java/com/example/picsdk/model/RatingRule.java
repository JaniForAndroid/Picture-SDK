package com.example.picsdk.model;

/**
 * @author: Shelter
 * Create time: 2019/10/12, 9:02.
 */
public class RatingRule {

  public Star zero_star;
  public Star first_star;
  public Star second_star;
  public Star third_star;

  public class Star {
    public int max;
    public int min;
  }
}
