package com.namibox.tools;

import android.content.Context;
import java.util.HashMap;

/**
 * 友盟自定义事件
 *
 * @author zhangkx
 * @Date 2018/11/23 11:31
 */
public class MobUtil {

  /**
   * 无参事件统计
   */
  public static void sendMobClick(Context context, String eventId) {
//    MobclickAgent.onEvent(context, eventId);
  }

  /**
   * 有参自定义事件统计
   */
  public static void sendMobClick(Context context, String eventId, HashMap<String, String> map) {
    //方便测试  可以弹个toast
//    Utils.toast(context, "eventId = " + eventId);
    if (map != null && map.size() > 0) {
//      MobclickAgent.onEvent(context, eventId, map);
    } else {
//      MobclickAgent.onEvent(context, eventId);
    }
  }

  /**
   * 有参自定义事件统计
   */
  public static void sendMobClick(Context context, String eventId, HashMap<String, String> map, int duration) {
//      MobclickAgent.onEventValue(context, eventId, map, duration);
  }

}
