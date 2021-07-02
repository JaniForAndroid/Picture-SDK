package com.namibox.commonlib.event;

/**
 * @Description app评分对话框通知事件
 * @CreateTime: 2020/1/17 14:51
 * @Author: zhangkx
 */
public class ScoreDialogEvent {

  /**点读触发*/
  public static int EVENT_TYPE_DIAND_DU = 0;
  /**网校触发*/
  public static int EVENT_TYPE_WX = 1;
  /**微课触发*/
  public static int EVENT_TYPE_WK = 2;
  /**口语评测触发*/
  public static int EVENT_TYPE_EVALUATION = 3;
  public int eventType;
  public ScoreDialogEvent(int eventType ) {
    this.eventType = eventType;
  }
}
