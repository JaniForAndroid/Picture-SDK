package com.namibox.commonlib.model;

import com.google.gson.JsonElement;
import java.util.List;

/**
 * @author: zbd
 * @time: 2019/5/17
 * @Description:
 */
public class MemberAlertDialogData {

  public String title;
  public String sub_title;
  public String title_bg_color;
  public String html_price;
  public String price_tag;
  public String price_btn_bg;
  public String rule_icon;
  public JsonElement action;
  public List<String> rule_msg;
}
