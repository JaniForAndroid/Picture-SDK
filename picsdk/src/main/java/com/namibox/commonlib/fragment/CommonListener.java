package com.namibox.commonlib.fragment;

/**
 * Create time: 2018/6/5.
 */
public interface CommonListener {
  void onMessageError(Exception e);
  boolean onMenuClick(String menuId);
  void onBackControl();
}
