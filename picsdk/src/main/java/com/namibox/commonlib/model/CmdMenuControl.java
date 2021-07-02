package com.namibox.commonlib.model;

import com.google.gson.JsonObject;

/**
 * Create time: 2020/4/13.
 */
public class CmdMenuControl extends BaseCmd {
  public Menu[] menu_action;
  public Menu[] left_action;
  public static class Menu {

    public String id = "";
    public String foregroundcolor;
    public String foregroundcolor_select;
    public String backgroundcolor;
    public boolean strokeStyle;
    public String name;
    public String action;
    public String image;
    public String selectimage;
    public JsonObject command;
    public String open_url;
    public String leftimage;
    public String rightimage;
    public String left_selectimage;
    public String right_selectimage;
    //add by app
    //public boolean isIconStyle;
    //public int iconResId;
    //public int iconDarkResId;
    //活动圈二期发布按钮样式
    public String layer_corner_radius;
    public String frame_size_height;
    public String frame_size_width;
  }
}
