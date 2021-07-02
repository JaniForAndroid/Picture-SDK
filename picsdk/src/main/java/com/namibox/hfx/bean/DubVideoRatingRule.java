package com.namibox.hfx.bean;

/**
 * 绘本馆分数计算规则 服务端和IOS端定义下来的
 * @author zhangkx
 * @Date 2019/10/10 10:01
 */
public class DubVideoRatingRule {
  /**分数星级*/
  public Start  star;
  /**计算权重*/
  public RuleModulus  modulus;

  public static class Start{
  public StartInterval zero_star;
  public StartInterval first_star;
  public StartInterval second_star;
  public StartInterval third_star;
  }
  public static class StartInterval{
    public int max;
    public int min;
  }
  public static class RuleModulus{
    public float pron;
    public float integrity;
    public float fluency;
  }
}
