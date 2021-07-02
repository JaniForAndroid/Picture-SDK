package com.namibox.util;

/**
 * Author: ryancheng
 * Create time: 2014/12/25 20:01
 */
public class Logger {

  private static String sTag = "logger";

  public static void v(String msg) {
    v(null, msg);
  }

  public static void v(String tag, String msg) {
    com.orhanobut.logger.Logger.t(tag);
    com.orhanobut.logger.Logger.v(msg);
  }

  public static void d(String msg) {
    d(null, msg);
  }

  public static void d(String tag, String msg) {
    com.orhanobut.logger.Logger.t(tag);
    com.orhanobut.logger.Logger.d(msg);
  }

  public static void i(String msg) {
    i(null, msg);
  }

  public static void i(String tag, String msg) {
    com.orhanobut.logger.Logger.t(tag);
    com.orhanobut.logger.Logger.i(msg);
  }

  public static void w(String msg) {
    w(null, msg);
  }

  public static void w(String tag, String msg) {
    com.orhanobut.logger.Logger.t(tag);
    com.orhanobut.logger.Logger.w(msg);
  }

  public static void e(String msg) {
    e(null, null, msg);
  }

  public static void e(String tag, String msg) {
    e(tag, null, msg);
  }

  public static void e(Throwable t, String msg) {
    e(null, t, msg);
  }

  public static void e(String tag, Throwable t, String msg) {
    com.orhanobut.logger.Logger.t(tag);
    com.orhanobut.logger.Logger.e(t, msg);
  }

  public static void json(String json) {
    json(null, json);
  }

  public static void json(String tag, String json) {
    com.orhanobut.logger.Logger.t(tag);
    com.orhanobut.logger.Logger.json(json);
  }

}
