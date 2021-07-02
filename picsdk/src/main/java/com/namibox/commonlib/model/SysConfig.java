package com.namibox.commonlib.model;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ryancheng on 14-8-1.
 */
public class SysConfig {

  public SafetyCheck safety_check;
  public boolean check_open;
  public boolean shanyan_login;
  /**是否使用老的网校答题布局 默认false  使用新的 */
  public boolean useOldVersion;
  /**是否弹隐私弹框（登录时） 默认返回true  弹框*/
  public boolean show_privacy_popup = true;
  /**是否自动打开书架**/
  public boolean new_dev_clickread;
  public boolean useLocalWeb;
  public boolean usePepSDK;
  public AppPage app_page;
  //public AppCache app_cache;
  public Plugins native_update;
  public String customer_service_type;//表示客服系统使用七陌还是环信；"huanxin"--使用环信；其他使用七陌，如空或"qimoor"
  public String theme_color;
  //动态图标
  public String appicon;
  public Update update_info;
  //public String enginetype;
  public HashMap<String, Boolean> not_go_book;
  public HashMap<String, String> IM_animationtext;
  public Ad[] ads;
  public boolean support_diff_download = true;
  public EaseMob easemob_cs;
  public ArrayList<String> domain_https;
  public boolean httpdns = true;
  public boolean session_hold = true;
  public ArrayList<Theme> theme;
  public List<Push> apppush;
  public List<Section> grade_list_data;//年级列表数据
  public String cur_grade; //下发的GradeItem key
  public ReferenceGrade reference_grade;
  public List<String> guide;
  public HashMap<String, List<Contact>> phonebook;
  public boolean exercise_commit_only_by_http = false;// 默认false或不传，表示支持双通道提交答案，如果为true表示仅支持http提交答案
  public boolean force_register_login;//true:强制登录，否则不可进入app
  /**是否是对话框样式年级选择*/
  public boolean grade_dialog_style;
  public Tabs tabs;
  public Tabs attend_class_tab;
  public Tabs worldTab;

  public static class Tabs {
    public int index;
    public ArrayList<TabIndex> indexes;

    @Override
    public String toString() {
      return "index=" + index + " indexes=" + indexes;
    }
  }

  public static class TabIndex {
    public String url;
    public String name;
    public String icon;
    public String select_icon;
    public String dot;
    public String badge_img;
    public String id;
    public boolean isweb;
    public String tab_type;//栏目类型，如“视频秀”
    public String album;//视频秀的专辑名
    //第二层导航
    public List<InnerTab> tabs;
  }

  public static class InnerTab{
    public String url;
    public String name;
    public String id;
  }
  public static class Contact {

    public String name;
    public String phone;
  }

  public static class Section {

    public String sectionname;
    public int display;
    public List<Grade> section;
  }

  public static class Grade {

    public String gradename;
    public List<GradeItem> grade;
  }

  public static class GradeItem {

    public String itemname;
    public String path;
    public String key;
    public String page;
  }

  public static class ReferenceGrade {

    public JsonObject remind;
    public String grade;
    public String title;
    public String deadline;
  }

  public static class ContentItem {

    public String text;
    public String color;
  }

  public static class Theme {

    public String theme_bg;
    public String expire_time;
    public ArrayList<Tab> tabs;
  }

  public static class Tab {

    public String normal_icon;
    public String select_icon;
    public String normal_txt;
    public String select_txt;
    public String normal_color;
    public String select_color;
    public String web_name;
    public String back_icon;
    public String select_back_icon;
  }

  public static class EaseMob {

    public String im_serverid;
    public HashMap cs_group;
    public boolean app_feedback = false;

  }

  public static class AppPage {

    public List<Page> app_new;
    public List<Page> app_update;
    public List<Page> app_delete;
  }

  public static class Page {

    public String url;
    public String version;
    public String page;
    public boolean force;
  }

//  public static class AppCache {
//
//    public List<Cache> app_new;
//    public List<Cache> app_update;
//    public List<Cache> app_delete;
//  }

//  public static class Cache {
//
//    public String url;
//    public String version;
//    public String domain;
//    public boolean force;
//  }

  public static class SafetyCheck {

    public ArrayList<Item> static_ck;
    public ArrayList<Item> html_ck;
    public ArrayList<Domain> domain_ck;
  }

  public static class Item {

    public String url;
    public String md5;
  }

  public static class Domain {

    public String ip;
    public String domain;
  }

  public static class Update {

    public boolean has_update;
    public boolean force_update;
    public String log;
    public long size;
    public String version;
    public String url;//第三方
    public String backup_url;//nms
    public String diff_url;
    public String md5;
    public boolean silent;
    public long diff_size;
  }

  public static class Ad {

    public String fromthirdpart;
    public String img;
    public String url;
    public String video_url;
    public String monitor_url;
    public int start_time;
    public int end_time;
    public int background_time;
    public int countdown_time;
    public String expire_time;
    public MiFu miFu;
  }

  public static class MiFu {

    public String adid;
    public String adtype;
    public int adformat;//1-静态，2-动态，6-mp4
    public String lp;
    public ArrayList<String> img;
    public ArrayList<String> pm;
    public ArrayList<String> cm;
  }

  public static class Push {

    public String title;
    public String content;
    public String url;
    public String monitor;
    public String push_id;
    public String start_time;
    public String end_time;
    public String view;
    public String s_image;
    public String s_image_bg;
    public String b_image;
    //web  弹框推送
    public String popup_url;
    public int showtime = 5;
    public JsonObject action;
    public String click_monitor;//url，执行action的时候需要再执行该字段
  }

  /** 插件列表 */
  public static class Plugins {

    public List<NativePluginInfo> android;
  }

  /** 插件信息 */
  public static class NativePluginInfo {

    /** 插件下载地址 */
    public String resourceurl;
    /** 更新日志 */
    public String native_update_log;
    /** 插件版本号 */
    public int updateversion;
    /** 插件包名 */
    public String apkid;
    /** 文件大小 */
    public int native_update_size;
  }
}
