package com.namibox.commonlib.event;

/**
 * Create time: 2019/11/18.
 */
public class ViewStateEvent {
  public static final int ENTER = 0;
  public static final int EXIT = 1;
  public static final int STAY = 2;
  public static final int TAB_STAY = 3;
  public static final int COMPOUND_TAB_STAY = 4;
  public int type;
  public boolean isDefault;
  public String tag;
  public long stayTime;

  public String formatType() {
    switch (type) {
      case ENTER:
        return "进入";
      case EXIT:
        return "退出";
      case STAY:
        return "停留";
      case TAB_STAY:
        return "TAB停留";
      case COMPOUND_TAB_STAY:
        return "COMPOUND_TAB_STAY";
      default:
        return "-";
    }
  }

  public ViewStateEvent(int type, boolean isDefault, String tag) {
    this.type = type;
    this.isDefault = isDefault;
    this.tag = tag;
  }

  public ViewStateEvent(int type, boolean isDefault, String tag, long stayTime) {
    this.type = type;
    this.isDefault = isDefault;
    this.tag = tag;
    this.stayTime = stayTime;
  }
}
