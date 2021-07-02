package com.namibox.tools;

import com.namibox.util.Logger;

/**
 * Create time: 2018/11/10.
 */
public class LoggerUtil {

  public static void v(String msg) {
    Logger.v(msg);
  }

  public static void v(String tag, String msg) {
    Logger.v(tag, msg);
  }

  public static void d(String msg) {
    Logger.d(msg);
  }

  public static void d(String tag, String msg) {
    Logger.d(tag, msg);
  }

  public static void i(String msg) {
    Logger.i(msg);
  }

  public static void i(String tag, String msg) {
    Logger.i(tag, msg);
  }

  public static void w(String msg) {
    Logger.w(msg);
  }

  public static void w(String tag, String msg) {
    Logger.w(tag, msg);
  }

  public static void e(String msg) {
    Logger.e(msg);
  }

  public static void e(String tag, String msg) {
    Logger.e(tag, msg);
  }

  public static void e(Throwable t, String msg) {
    Logger.e(t, msg);
  }

  public static void json(String msg) {
    Logger.json(msg);
  }

  public static void json(String tag, String msg) {
    Logger.json(tag, msg);
  }
}
