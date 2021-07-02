package com.namibox.tools;

import org.greenrobot.eventbus.EventBus;

/**
 * Create time: 2018/11/9.
 */
public class EventUtil {

  public static void postEvent(Object obj) {
    EventBus.getDefault().post(obj);
  }

  public static void register(Object o) {
    EventBus.getDefault().register(o);
  }

  public static void unregister(Object o) {
    EventBus.getDefault().unregister(o);
  }
}
