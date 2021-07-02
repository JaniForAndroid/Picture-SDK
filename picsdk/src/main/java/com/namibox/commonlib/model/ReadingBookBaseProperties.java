package com.namibox.commonlib.model;

public class ReadingBookBaseProperties {

  //  public String product_category = "点读";//固定为 点读
  public String book_id;//点读书的book_id
  public String book_term;//产品对应的教材的学期
  public String book_grade;//教材对应的年级
  public String book_category;//点读课本类型：课本，教辅，非标课本
  public String book_charge;//收费或免费
  public boolean order_state;//false:未订购、true:已订购
}
