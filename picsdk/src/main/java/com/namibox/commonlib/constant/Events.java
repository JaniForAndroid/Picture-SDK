package com.namibox.commonlib.constant;

public interface Events {

  String OPEN_TAPE_BOOK = "open_tape_book";
  String OPEN_CLICK_BOOK = "open_click_book";

  String TA_EVENT_NB_APP_CLICK = "nb_app_click";//namibox点击事件
  String TA_EVENT_NB_APP_VIEW_ENTER = "nb_app_view_enter";//namibox进入页面事件
  String TA_EVENT_NB_APP_VIEW_CLOSE = "nb_app_view_close";//namibox退出页面事件
  String TA_STATUS_NB_APP_EVENT = "nb_app_status";//namibox App状态上报
  String TA_STATUS_NB_APP_PRIVACY_SHOW = "nb_app_view_enter";//隐私弹框出现
  String TA_STATUS_NB_APP_PRIVACY_HIDE = "nb_app_view_close";//隐私弹框消失
  String TA_STATUS_NB_APP_PRIVACY_CLICK = "nb_app_click  ";//隐私弹框点击事件
}
