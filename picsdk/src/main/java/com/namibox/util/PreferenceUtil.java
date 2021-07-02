package com.namibox.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.google.gson.Gson;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by sunha on 2017/6/1 0001.
 */

public class PreferenceUtil {

  public static final String PREF_SESSION_ID = "sessionid";
  public static final String PREF_SESSION = "session";
  public static final String PREF_PHONENUM = "phonenum";
  public static final String PREF_CHANNEL = "app_channel";
  /** 闪验开关 */
  public static final String SY_ENABLE = "sy_enable";
  /** 闪验初始化状态 **/
  public static final String SY_INIT_STATE = "sy_init_state";
  /** 闪验预取号 */
  public static final String SY_INIT_PRE = "sy_init_pre";
  /** 是否弹隐私弹框 */
  public static final String PREF_SHOW_PRIVACY = "show_privacy";
  public static final String PREF_EXPIRE_TIME = "sessionid_change_time";
  public static final String PREF_LOGIN_TIME = "pref_login_time";
  public static final String PREF_THEME_COLOR = "theme_color";
  //public static final String PREF_DEV_ENV = "is_dev_env";
  public static final String PREF_ENV_TYPE = "pref_env_type";
  public static final String PREF_SSL_ALL = "pref_accept_all_ssl";
  public static final String PREF_STETHO = "pref_stetho";
  public static final String PREF_STORAGE_SELECTED = "storage_select";
  private static final String HEAD_IMAGE = "head_image";
  private static final String NICK_NAME = "nick_name";
  private static final String PREF_USER_PHONE = "user_phone";
  private static final String UNREAD_KEFU_MSG = "unread_kefu_msg";
  public static final String PREF_CLICK_READ_TRANSLATE = "click_read_translate";
  public static final String PREF_CLICK_READ_SHOW = "click_read_area_show";
  public static final String GUIDE = "wx_guide";
  public static final String GUIDE_FACE = "guide_face";
  private static final String UNREAD_IM_MSG = "unread_im_msg";
  private static final String USER_AUTH = "user_auth";
  public static final String PACKAGE_LIST = "package_list";
  public static final String AUDIO_PLAY_PAGE = "audio_play_page";
  public static final String VIDEO_PROGRESS = "video_progress";
  public static final String ACTUAL_WATCH_PROGRESS = "actual_watch_progress";
  public static final String AUDIO_INDEX = "audio_index";
  public static final String PREF_USER_ID = "user_id";
  public static final String REG_TIME = "reg_time";
  /** 是否绑定过手机 */
  public static final String PREF_BIND_PHONE = "bind_phone";
  /** 是否选择过年级 */
  public static final String PREF_GRADE_CHOOSED = "grade_choosed";
  /** 是否 同步cookie */
  public static final String PREF_SYNC_COOKIE = "sync_cookie";
  public static final String GUIDE_CLICK_BOOK = "guide_click_book";
  public static final String NATIVE_JSON_CACHED = "native_json_cached2";
  public static final String NATIVE_TOOL_JSON_CACHED = "native_tool_json_cached2";
  public static final String NATIVE_JSON_PSCHOOL = "native_json_pschool";
  public static final String NATIVE_JSON_PMEMBER = "native_json_pmember";
  public static final String NATIVE_JSON_PFISH = "native_json_pfish";
  public static final String NATIVE_JSON_PWORLD = "native_json_pworld";
  public static final String NATIVE_JSON_PUSER = "native_json_puser";
  /** 第一次打开app */
  public static final String IS_FIRST_OPEN = "is_first_open";
  /** 点读指引打开的次数 */
  public static final String CLICK_READ_GUIDE_SHOW_TIMES = "click_read_show_times";
  /** 年级指引的次数 */
  public static final String GRADE_TIPS_GUIDE_SHOW_TIMES = "grade_tips_show_times";
  /** 新人礼包指引 */
  public static final String IS_GIFT_GUIDE_SHOW = "is_gift_guide_show";
  /** 用户礼包数据 */
  public static final String GIFT_USER_INFO = "gift_user_info";
  /** Pshool ReferenceTips */
  public static final String REFERENCE_TIPS = "reference_tips";
  /** 报名版 menu */
  public static final String TEACHER_TAB_MENU = "teacher_tab_menu";
  /** 报名版 用户头像 */
  public static final String TEACHER_USER_AVATOR = "teacher_user_avator";
  /** 报名版 用户信息部分背景 */
  public static final String TEACHER_USER_BG = "teacher_user_bg";
  /** 报名版 用户姓名 */
  public static final String TEACHER_USER_NAME = "teacher_user_name";
  /** 报名版 用户机构 */
  public static final String TEACHER_USER_PART = "teacher_user_part";
  /** 报名版 用户电话 */
  public static final String TEACHER_USER_PHONE = "teacher_user_phone";

  /** 新人启动指引 */
  public static final String SPLASH_GUIDE = "user_splash_guide";
  /** 使用网校答题老布局 */
  public static final String USE_WX_OLD_VERSION = "use_wx_old_version";
  /** 使用年级选择对话框 */
  public static final String USE_GRADE_DIALOG_STYLE = "grade_dialog_style";
  /** 是否自动打开书架列表 */
  public static final String AUTO_OPEN_BOOK_LIST = "auto_open_book_list";
  /** 是否自动打开过书架列表 */
  public static final String HAS_AUTO_OPEN_BOOK_LIST = "has_auto_open_book_list";
  /** 当前splash 组件 */
  public static final String SPLASH_ALIAS = "splash_alias";
  /**********************************权限相关**********************************/
  /** 手机状态权限 */
  public static final String P_PHONE_STATE = "permission_phone_state";
  /** 手机读写权限 */
  public static final String P_WRITE_READ = "permission_write_read";
  /** 定制屏WIFI是否自动更新 */
  public static final String C_AUTO_UPDATE = "custom_auto_update";
  /** 年级建议弹框时间戳 */
  public static final String REFERENCE_GRADE_DIALOG_SHOW = "reference_grade_dialog_show";
  /** 会员卡片切换引导 */
  public static final String GUIDE_SWITCH_MEMBER_CARD = "guide_switch_member_card";

  public static final String CUSTOMPAD = "custompad";
  /** 记录网校上课浮窗点击的url */
  public static final String FW_CLICK_URL = "fw_click_url";
  /** 口语评测新用户体验次数 */
  public static final String NEW_USER_EVAL_COUNT = "new_user_eval_count_";
  /** 口语评测引导 */
  public static final String EVALUATION_GUIDE = "evaluation_guide";

  /**点读缩放**/
  public static final String CLICK_SCALE_GUIDE = "click_scale_guide";
    /**书本下载状态更新**/
  public static final String BOOK_STATUS_UPDATE = "book_status_update";

  /**当天使用app计时开始时间戳**/
  public static final String START_USE = "start_use";
  /**当天使用app时长**/
  public static final String USE_DURATION = "use_duration";

  /** 华为unionId，是否绑定了华为账号的标志 **/
  public static final String PREF_USER_HW_UNIONID = "user_hw_unionid";

  /** 点读指引的次数 */
  public static final String CLICK_GUIDE_SHOW_TIMES = "click_guide_show_times";
  public static final String USE_SELF_RENJIAO = "use_self_renjiao";
  /** 短视频是否引导 */
  public static final String SHORT_VIDEO_GUIDE_FLAG = "short_video_guide_flag";
  /** 是否绑同意隐私政策 */
  public static final String PREF_PRIVACY_AGREE = "privacy_agree";

  public static final String DELETE_GRADE = "delete_grade";
  /** 是否仅浏览 */
  public static final String PREF_ONLY_LOOK = "only_look";

  //学习记录
  public static final String STUDY_TIME = "study_time";

  /** 特训营时间检验差 */
  public static final String PREF_STUDY_CAMP_DURATION = "pref_study_camp_duration";
  /** 日历提醒状态  */
  public static final String PREF_CALENDAR_STATUS = "calendar_status";
  public static final String TOOLS_SCROLL = "tools_scroll";

  //休息时间
  public static final String PRE_REST_TIME = "pre_rest_time";
  public static final String PRE_LEFT_REST_STAMP = "pre_left_rest_stamp";


  private Context context;
  private static volatile PreferenceUtil sInstance;

  public static PreferenceUtil get(Context c) {
    if (sInstance == null) {
      synchronized (PreferenceUtil.class) {
        if (sInstance == null) {
          sInstance = initInstance(c);
        }
      }
    }

    return sInstance;
  }

  private static PreferenceUtil initInstance(Context c) {
    return new PreferenceUtil(Utils.getContext(c));
  }

  private PreferenceUtil(Context c) {
    this.context = c;
  }

  public SharedPreferences getDefaultSP() {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  public SharedPreferences getSP(String s) {
    return context.getSharedPreferences(s, Context.MODE_PRIVATE);
  }

  public static boolean getSharePref(Context context, String key, boolean defaultValue) {
    return get(context).getDefaultSP().getBoolean(key, defaultValue);
  }

  public static String getSharePref(Context context, String key, String defaultValue) {
    return get(context).getDefaultSP().getString(key, defaultValue);
  }

  public static Set<String> getSharePrefSet(Context context, String key, Set<String> defaultValue) {
    return get(context).getDefaultSP().getStringSet(key, defaultValue);
  }

  public static int getSharePref(Context context, String key, int defaultValue) {
    return get(context).getDefaultSP().getInt(key, defaultValue);
  }

  public static long getSharePref(Context context, String key, long defaultValue) {
    return get(context).getDefaultSP().getLong(key, defaultValue);
  }

  public static float getSharePref(Context context, String key, float defaultValue) {
    return get(context).getDefaultSP().getFloat(key, defaultValue);
  }

  public static void setSharePref(Context context, String key, boolean value) {
    get(context).getDefaultSP().edit().putBoolean(key, value).apply();
  }

  public static void setSharePref(Context context, String key, int value) {
    get(context).getDefaultSP().edit().putInt(key, value).apply();
  }

  public static void setSharePref(Context context, String key, long value) {
    get(context).getDefaultSP().edit().putLong(key, value).apply();
  }

  public static void setSharePref(Context context, String key, float value) {
    get(context).getDefaultSP().edit().putFloat(key, value).apply();
  }

  public static void setSharePref(Context context, String key, String value) {
    get(context).getDefaultSP().edit().putString(key, value).apply();
  }

  public static void setSharePrefSet(Context context, String key, Set<String> value) {
    get(context).getDefaultSP().edit().putStringSet(key, value).apply();
  }

  public static boolean getUseSelfRJ(Context context) {
    return get(context).getDefaultSP().getBoolean(USE_SELF_RENJIAO, true);
  }

  public static void setUseSelfRJ(Context context, boolean useSelfRJ) {
    get(context).getDefaultSP().edit().putBoolean(USE_SELF_RENJIAO, useSelfRJ).apply();
  }


  public static boolean setSharePref(Context context, String key, String value, boolean isSync) {
    if (isSync) {
      return get(context).getDefaultSP().edit().putString(key, value).commit();
    } else {
      setSharePref(context, key, value);
      return true;
    }

  }

  public static String getDownloadFileName(Context context, String packageName) {
    return get(context).getSP("package_download").getString("package_download_file_" + packageName, null);
  }

  public static void setDownloadFileName(Context context, String packageName, String url) {
    get(context).getSP("package_download").edit().putString("package_download_file_" + packageName, MD5Util.md5(url))
        .apply();
  }

  public static int getDownloadProgress(Context context, String packageName) {
    return get(context).getSP("package_download").getInt("package_download_progress_" + packageName, 0);
  }

  public static void setDownloadProgress(Context context, String packageName, int progress) {
    get(context).getSP("package_download").edit().putInt("package_download_progress_" + packageName, progress).apply();
  }

  public static int getDownloadState(Context context, String packageName) {
    return get(context).getSP("package_download").getInt("package_download_state_" + packageName, 0);
  }

  //state 0-未下载，1-下载中，2-已安装，3-已下载未安装
  public static void setDownloadState(Context context, String packageName, int state) {
    get(context).getSP("package_download").edit().putInt("package_download_state_" + packageName, state).apply();
  }

  public static String getConfig(Context context, String key) {
    return get(context).getSP("web_config").getString(key, "");
  }

  public static void saveConfig(Context context, String key, String value) {
    get(context).getSP("web_config").edit().putString(key, value).apply();
  }

  public static void cleanConfig(Context context) {
    get(context).getSP("web_config").edit().clear().apply();
  }

  public static String getBookLastModify(Context context, String key) {
    return get(context).getSP("book_download_info").getString(key + "_last_modify", null);
  }

  public static void setBookLastModify(Context context, String key, String lastModify) {
    get(context).getSP("book_download_info").edit().putString(key + "_last_modify", lastModify).apply();
  }

  public static String getBookAuth(Context context, String bookId) {
    return get(context).getSP("book_auth").getString(bookId, null);
  }

  public static void setBookAuth(Context context, String bookId, String bookAuth) {
    get(context).getSP("book_auth").edit().putString(bookId, bookAuth).apply();
  }

  public static void clearBookAuth(Context context) {
    get(context).getSP("book_auth").edit().clear().apply();
  }

  public static String getCheckSdk(Context context, String bookId) {
    return get(context).getSP("check_sdk").getString(bookId, null);
  }

  public static void setCheckSdk(Context context, String bookId, String json) {
    get(context).getSP("check_sdk").edit().putString(bookId, json).apply();
  }

  public static void clearCheckSdk(Context context) {
    get(context).getSP("check_sdk").edit().clear().apply();
  }

  public static void saveRJBook(Context context, String bookId, String json) {
    get(context).getSP("rj_book").edit().putString(bookId, json).apply();
  }

  public static String getRJBook(Context context, String bookId) {
    return get(context).getSP("rj_book").getString(bookId, null);
  }

  public static String getHeadImage(Context context, String userId) {
    return get(context).getSP(HEAD_IMAGE).getString(userId + "_headImage", "");
  }

  public static void saveHeadImage(Context context, String userId, String headImage) {
    get(context).getSP(HEAD_IMAGE).edit().putString(userId + "_headImage", headImage).apply();
  }

  public static String getNickName(Context context, String userId) {
    return get(context).getSP(NICK_NAME).getString(userId + "_nickName", "");
  }
  /**获取用户手机号码**/
  public static String getUserPhone(Context context) {
    return get(context).getDefaultSP().getString(PREF_USER_PHONE, "");
  }
  /**获取用户角色**/
  public static String getUserRole(Context context, String key) {
    return get(context).getDefaultSP().getString(key, "");
  }
  /**获取PREF_EXPIRE_TIME**/
  public static long getExpireTime(Context context) {
    return getSharePref(context, PREF_EXPIRE_TIME, 0L);

  }
  public static void saveNickName(Context context, String userId, String nickName) {
    get(context).getSP(NICK_NAME).edit().putString(userId + "_nickName", nickName).apply();
  }

  /** 获取用户的登录密码 **/
  public static String getUserPwd(Context context) {
    return get(context).getDefaultSP().getString("user_pwd", "");
  }

  /** 保存用戶的登录密码 **/
  public static void saveUserPwd(Context context, String pwd) {
    get(context).getDefaultSP().edit().putString("user_pwd", pwd).apply();
  }

  public static String getRegTime(Context context, String userId) {
    return get(context).getSP(REG_TIME).getString(userId + "_regTime", "");
  }

  public static void saveRegTime(Context context, String userId, String regTime) {
    get(context).getSP(REG_TIME).edit().putString(userId + "_regTime", regTime).apply();
  }

  public static void setUnreadKefuMsg(Context context, String userId, int count) {
    get(context).getDefaultSP().edit().putInt(UNREAD_KEFU_MSG + userId, count).apply();
  }

  public static int getUnreadKefuMsg(Context context, String userId) {
    return get(context).getDefaultSP().getInt(UNREAD_KEFU_MSG + userId, 0);
  }

  public static void setUnreadImMsg(Context context, String userId, int count) {
    get(context).getDefaultSP().edit().putInt(UNREAD_IM_MSG + userId, count).apply();
  }

  public static int getUnreadImMsg(Context context, String userId) {
    return get(context).getDefaultSP().getInt(UNREAD_IM_MSG + userId, 0);
  }

  public static void setUnreadImGroupMsg(Context context, String groupId, String userId, int count) {
    get(context).getDefaultSP().edit().putInt(UNREAD_IM_MSG + userId + "_" + groupId, count).apply();
  }

  public static int getUnreadImGroupMsg(Context context, String groupId, String userId) {
    return get(context).getDefaultSP().getInt(UNREAD_IM_MSG + userId + "_" + groupId, 0);
  }

  public static String getUserAuth(Context context, String userId) {
    return get(context).getSP(USER_AUTH).getString(userId + "_userAuth", "");
  }

  public static int getLastRead(Context context, String bookId) {
    return get(context).getSP("book_last_read").getInt(bookId, 0);
  }

  public static void saveLastRead(Context context, String bookId, int value) {
    get(context).getSP("book_last_read").edit().putInt(bookId, value).apply();
  }

  public static String getLastReadUnit(Context context, String bookId) {
    return get(context).getSP("book_last_read_unit_1").getString(bookId, null);
  }

  public static void saveLastReadUnit(Context context, String bookId, String value) {
    get(context).getSP("book_last_read_unit_1").edit().putString(bookId, value).apply();
  }

  public static long getReferenceStartTime(Context context, String id) {
    return get(context).getSP("reference_time").getLong(id, -1L);
  }

  public static void saveReferenceStartTime(Context context, String id, long value) {
    get(context).getSP("reference_time").edit().putLong(id, value).apply();
  }

  public static int getReferenceCount(Context context, String id) {
    return get(context).getSP("reference_count").getInt(id, -1);
  }

  public static void saveReferenceCount(Context context, String id, int value) {
    get(context).getSP("reference_count").edit().putInt(id, value).apply();
  }

  public static String getReference(Context context, String id, String key) {
    return get(context).getSP(id).getString(key, null);
  }

  public static void saveReference(Context context, String id, String key, String reference) {
    get(context).getSP(id).edit().putString(key, reference).apply();
  }

  public static int getAudioPlayPage(Context context) {
    return get(context).getSP(AUDIO_PLAY_PAGE).getInt(AUDIO_PLAY_PAGE, 0);
  }

  public static void setAudioPlayPage(Context context, int page) {
    get(context).getSP(AUDIO_PLAY_PAGE).edit().putInt(AUDIO_PLAY_PAGE, page).apply();
  }

  public static HashSet<String> getGuideByKey(Context context, String key) {
    String jsonStr = get(context).getSP(GUIDE).getString(key, null);
    if (jsonStr != null) {
      Gson gson = new Gson();
      return gson.fromJson(jsonStr, HashSet.class);
    }
    return null;
  }

  public static void saveGuideByKey(Context context, String key, HashSet hashSet) {
    Gson gson = new Gson();
    String jsonStr = gson.toJson(hashSet);
    get(context).getSP(GUIDE).edit().putString(key, jsonStr).apply();
  }

  public static void saveStudyTime(
      Context context, String key, LinkedHashMap<String, String> hashMap){
    Gson gson = new Gson();
    String jsonStr = gson.toJson(hashMap);
    get(context).getSP(STUDY_TIME).edit().putString(key, jsonStr).apply();
  }

  public static LinkedHashMap<String, String> getStudyTime(Context context, String key) {
    String jsonStr = get(context).getSP(STUDY_TIME).getString(key, null);
    if (jsonStr != null) {
      Gson gson = new Gson();
      return gson.fromJson(jsonStr, LinkedHashMap.class);
    }
    return null;
  }

  /**
   * 保存网校 中面部识别指引
   *
   * @param key 网课id
   * @param times 展示的次数
   */
  public static void setWxFaceGuideTimes(Context context, String key, int times) {
    HashMap<String, String> hashMap;
    Gson gson = new Gson();
    String guideStr = get(context).getSP(GUIDE_FACE).getString(key, null);
    try {
      if (!TextUtils.isEmpty(guideStr)) {
        hashMap = gson.fromJson(guideStr, HashMap.class);
      } else {
        hashMap = new HashMap<>();
      }
      hashMap.put(key, times + "");
      String jsonStr = gson.toJson(hashMap);
      get(context).getSP(GUIDE_FACE).edit().putString(key, jsonStr).apply();
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e("zkx  数据转换错误 e = " + e.toString());
    }
  }

  /**
   * 获取对用的 网课展示面部指引的次数
   *
   * @param key 网课id
   * @return 展示的次数
   */
  public static int getWxFaceGuideTimes(Context context, String key) {
    String jsonStr = get(context).getSP(GUIDE_FACE).getString(key, null);
    if (!TextUtils.isEmpty(jsonStr)) {
      try {
        Gson gson = new Gson();
        HashMap<String, String> hashMap = gson.fromJson(jsonStr, HashMap.class);
        String timeStr = hashMap.get(key);
        return Integer.parseInt(timeStr);
      } catch (Exception e) {
        e.printStackTrace();
        Logger.e("zkx  数据转换错误 e = " + e.toString());
        return 0;
      }
    }
    return 0;
  }

  public static HashMap<String, Double> getVideoProgress(Context context) {
    String jsonStr = get(context).getSP(VIDEO_PROGRESS).getString(VIDEO_PROGRESS, null);
    if (jsonStr != null) {
      Gson gson = new Gson();
      return gson.fromJson(jsonStr, HashMap.class);
    }
    return null;
  }

  public static void setVideoProgress(Context context, HashMap<String, Double> hashMap) {
    Gson gson = new Gson();
    String jsonStr = gson.toJson(hashMap);
    get(context).getSP(VIDEO_PROGRESS).edit().putString(VIDEO_PROGRESS, jsonStr).apply();
  }

  public static HashMap<String, String> getAudioIndex(Context context) {
    String jsonStr = get(context).getSP(AUDIO_INDEX).getString(AUDIO_INDEX, null);
    if (jsonStr != null) {
      Gson gson = new Gson();
      return gson.fromJson(jsonStr, HashMap.class);
    }
    return null;
  }

  public static void setAudioIndex(Context context, HashMap<String, String> hashMap) {
    Gson gson = new Gson();
    String jsonStr = gson.toJson(hashMap);
    get(context).getSP(AUDIO_INDEX).edit().putString(AUDIO_INDEX, jsonStr).apply();
  }

  public static HashMap<String, String> getActualWatchProgress(Context context) {
    String jsonStr = get(context).getSP(ACTUAL_WATCH_PROGRESS).getString(ACTUAL_WATCH_PROGRESS, null);
    if (jsonStr != null) {
      Gson gson = new Gson();
      return gson.fromJson(jsonStr, HashMap.class);
    }
    return null;
  }

  public static void setActualWatchProgress(Context context, HashMap<String, String> hashMap) {
    Gson gson = new Gson();
    String jsonStr = gson.toJson(hashMap);
    get(context).getSP(ACTUAL_WATCH_PROGRESS).edit().putString(ACTUAL_WATCH_PROGRESS, jsonStr).apply();
  }

  public static void setSelectedStorage(Context context, String selected) {
    setSharePref(context, PREF_STORAGE_SELECTED, selected);
  }

  public static String getSelectedStorage(Context context) {
    return getSharePref(context, PREF_STORAGE_SELECTED, "");
  }

  public static boolean isHttpDnsEnable(Context context) {
    return getSharePref(context, "pref_use_httpdns", true);
  }

  public static void setHttpDnsEnable(Context context, boolean enable) {
    setSharePref(context, "pref_use_httpdns", enable);
  }

  public static boolean isStethoEnable(Context context) {
    return getSharePref(context, PREF_STETHO, false);
  }

  public static boolean isSSLEnable(Context context) {
    return getSharePref(context, PREF_SSL_ALL, false);
  }

  public static void setSSLEnable(Context context) {
    setSharePref(context, PREF_SSL_ALL, true);
  }

  public static String getChannel(Context context) {
    return getSharePref(context, PREF_CHANNEL, "defaultChannel");
  }

  public static void setChannel(Context context, String channel) {
    setSharePref(context, PREF_CHANNEL, channel);
  }

  /** 设置闪验是否可用 */
  public static void setSYEnable(Context context, boolean enable) {
    setSharePref(context, SY_ENABLE, enable);
  }

  /** 获取是否能用闪验 默认不能 */
  public static boolean isSYEnable(Context context) {
    return getSharePref(context, SY_ENABLE, false);
  }

  /** 闪验初始化是否成功 */
  public static void setSYInitState(Context context, boolean enable) {
    setSharePref(context, SY_INIT_STATE, enable);
  }

  /** 获取闪验初始化是否成功 默认false */
  public static boolean isSYInitState(Context context) {
    return getSharePref(context, SY_INIT_STATE, false);
  }

  /** 闪验预取号是否成功 */
  public static void setSyInitPre(Context context, boolean enable) {
    setSharePref(context, SY_INIT_PRE, enable);
  }

  /** 获取闪验预取号是否成功 默认false */
  public static boolean isSyInitPre(Context context) {
    return getSharePref(context, SY_INIT_PRE, false);
  }
  /** 设置是否弹隐私弹框 */
  public static void setShowPrivacy(Context context, boolean enable) {
    setSharePref(context, PREF_SHOW_PRIVACY, enable);
  }

  /** 获取设置是否弹隐私弹框 默认true */
  public static boolean isShowPrivacy(Context context) {
    return getSharePref(context, PREF_SHOW_PRIVACY, true);
  }

  /** 是否使用网校老布局 */
  public static void setUseWxOldVersion(Context context, boolean enable) {
    setSharePref(context, USE_WX_OLD_VERSION, enable);
  }

  /** 获取是否使用对话框样式年级选择 默认false */
  public static boolean getUseDialogGrade(Context context) {
    return getSharePref(context, USE_GRADE_DIALOG_STYLE, false);
  }
  /** 是否使用对话框样式年级选择 */
  public static void setUseDialogGrade(Context context, boolean enable) {
    setSharePref(context, USE_GRADE_DIALOG_STYLE, enable);
  }

  /** 获取是否使用网校老布局 默认false */
  public static boolean getUseWxOldVersion(Context context) {
    return getSharePref(context, USE_WX_OLD_VERSION, false);
  }

  /** 是否自动打开书架列表 */
  public static void setAutoOpenBookList(Context context, boolean enable) {
    setSharePref(context, AUTO_OPEN_BOOK_LIST, enable);
  }

  /** 获取是否自动打开书架列表 默认false */
  public static boolean getAutoOpenBookList(Context context) {
    return getSharePref(context, AUTO_OPEN_BOOK_LIST, false);
  }

  public static String getSplashGuideStr(Context context) {
    return getSharePref(context, SPLASH_GUIDE, "");
  }

  /** 获取闪验预取号是否成功 默认false */
  public static boolean getGuideSwitchMemberCard(Context context) {
    return getSharePref(context, GUIDE_SWITCH_MEMBER_CARD, false);
  }

  public static void setGuideSwitchMemberCard(Context context, boolean enable) {
    setSharePref(context, GUIDE_SWITCH_MEMBER_CARD, enable);
  }

  public static void setSplashGuideStr(Context context, String guideStr) {
    setSharePref(context, SPLASH_GUIDE, guideStr);
  }

  public static void setSplashAlias(Context context, String guideStr) {
    setSharePref(context, SPLASH_ALIAS, guideStr);
  }

  public static String getSplashAlias(Context context) {
    return getSharePref(context, SPLASH_ALIAS, "");
  }

  public static void setLongLoginUserId(Context context, long userid) {
    setSharePref(context, PREF_USER_ID, userid);
  }

  public static long getLongLoginUserId(Context context) {
    return getSharePref(context, PREF_USER_ID, -1L);
  }
  /**获取sessionid*/
  public static String getSessionId(Context context) {
    return getSharePref(context, PREF_SESSION_ID, "");
  }

  public static void setSessionId(Context context, String sessionId) {
    setSharePref(context, PREF_SESSION_ID, sessionId);
  }

  public static String getSession(Context context) {
    return getSharePref(context, PREF_SESSION, "");
  }


  public static void setSession(Context context, String session) {
    setSharePref(context, PREF_SESSION, session);
  }

  public static String getPhone(Context context) {
    return getSharePref(context, PREF_PHONENUM, "");
  }

  public static void setPhone(Context context, String sessionId) {
    setSharePref(context, PREF_PHONENUM, sessionId);
  }

  public static String getHwUnionId(Context context, String userId) {
    return getSharePref(context, PREF_USER_HW_UNIONID + userId, "");
  }

  public static void setHwUnionId(Context context, String userId, String hwUnionId) {
    setSharePref(context, PREF_USER_HW_UNIONID + userId, hwUnionId);
  }

  public static String getSessionTime(Context context) {
    long time = getExpireTime(context);
    if (time != 0) {
      return new Date(time).toGMTString();
    }
    return null;
  }

  public static String getEnv(Context context, String def) {
    return getSharePref(context, PREF_ENV_TYPE, def);
  }

  public static String getEnv(Context context) {
    return getEnv(context, "namibox.com");
  }

  public static void setEnv(Context context, String env) {
    setSharePref(context, PREF_ENV_TYPE, env);
  }

  public static File getLogDir(Context context) {
    String dir = getLogDirString(context);
    return new File(Environment.getExternalStorageDirectory(), dir);
  }

  public static String getLogDirString(Context context) {
    return getSharePref(context, "pref_log_dir", "namiboxlog");
  }

  public static void setLogDir(Context context, String env) {
    setSharePref(context, "pref_log_dir", env);
  }

  public static int getGuideClickBook(Context context) {
    return getSharePref(context, GUIDE_CLICK_BOOK, 0);
  }

  public static void setGuideClickBook(Context context, int value) {
    setSharePref(context, GUIDE_CLICK_BOOK, value);
  }


  /***
   * 获取当前设备展示点读指引的次数
   */
  public static int getReadClickShowTimes(Context context) {
    return getSharePref(context, CLICK_READ_GUIDE_SHOW_TIMES, 0);
  }

  /***
   * 设置当前设备展示点读指引的次数
   */
  public static void setReadClickShowTimes(Context context, int time) {
    setSharePref(context, CLICK_READ_GUIDE_SHOW_TIMES, time);
  }

  /***
   * 获取当前设备年级展示指引的次数
   */
  public static int getGradeShowTimes(Context context) {
    return getSharePref(context, GRADE_TIPS_GUIDE_SHOW_TIMES, 0);
  }

  /***
   * 设置当前设备年级展示指引指引的次数
   */
  public static void setGradeShowTimes(Context context, int time) {
    setSharePref(context, GRADE_TIPS_GUIDE_SHOW_TIMES, time);
  }

  /***
   * 获取当前设备点读展示指引的次数
   */
  public static int getClickShowTimes(Context context) {
    return getSharePref(context, CLICK_GUIDE_SHOW_TIMES, 0);
  }

  /***
   * 设置当前设备点读展示指引指引的次数
   */
  public static void setClikShowTimes(Context context, int time) {
    setSharePref(context, CLICK_GUIDE_SHOW_TIMES, time);
  }

  /***
   * 是否绑定过手机 默认false
   */
  public static boolean isBindPhone(Context context) {
    return getSharePref(context, PREF_BIND_PHONE, false);
  }

  /***
   * 设置当前用户是否绑定过手机号码
   */
  public static void setBindPhone(Context context, boolean flag) {
    setSharePref(context, PREF_BIND_PHONE, flag);
  }
  /***
   * 是否同意过隐私政策 默认false
   */
  public static boolean isPrivacyAgree(Context context) {
    return getSharePref(context, PREF_PRIVACY_AGREE, false);
  }

  /***
   * 设置当前用户是否同意隐私政策
   */
  public static void setPrivacyAgree(Context context, boolean flag) {
    setSharePref(context, PREF_PRIVACY_AGREE, flag);
  }
  /***
   * 是否仅浏览 默认false
   */
  public static boolean isOnlyLook(Context context) {
    return getSharePref(context, PREF_ONLY_LOOK, false);
  }

  /***
   * 设置当前用户是否仅浏览
   */
  public static void setOnlyLook(Context context, boolean flag) {
    setSharePref(context, PREF_ONLY_LOOK, flag);
  }
  /***
   * 是否选择过年级 默认false
   */
  public static boolean isGradeChoosed(Context context) {
    return getSharePref(context, PREF_GRADE_CHOOSED, false);
  }

  /***
   * 设置当前用户是否选择过年级
   */
  public static void setGradeChoosed(Context context, boolean flag) {
    setSharePref(context, PREF_GRADE_CHOOSED, flag);
  }
  /***
   * 是否同步過cookie 默认false
   */
  public static boolean isSyncCookie(Context context) {
    return getSharePref(context, PREF_SYNC_COOKIE, false);
  }

  /***
   * 设置当前用户是否同步過cookie
   */
  public static void setSyncCookie(Context context, boolean flag) {
    setSharePref(context, PREF_SYNC_COOKIE, flag);
  }

  /***
   * 手机状态权限是否被永远禁止 默认false
   */
  public static boolean isPhoneStateBanForver(Context context) {
    return getSharePref(context, P_PHONE_STATE, false);
  }

  /***
   * 设置当前用户手机状态权限是否被永远禁止
   */
  public static void setPhoneStateBanForver(Context context, boolean flag) {
    setSharePref(context, P_PHONE_STATE, flag);
  }

  /***
   * 获取WIFI下是否自动更新
   */
  public static boolean isCustomAutoUpdate(Context context) {
    return getSharePref(context, C_AUTO_UPDATE, false);
  }

  /***
   * 设置WIFI下是否自动更新
   */
  public static void setCustomAutoUpdate(Context context, boolean flag) {
    setSharePref(context, C_AUTO_UPDATE, flag);
  }

  /***
   * 手机读写权限是否被永远禁止 默认false
   */
  public static boolean isReadWriteBanForver(Context context) {
    return getSharePref(context, P_WRITE_READ, false);
  }

  /***
   * 设置当前用户手机读写权限是否被永远禁止
   */
  public static void setReadWriteBanForver(Context context, boolean flag) {
    setSharePref(context, P_WRITE_READ, flag);
  }

  /***
   * 获取当前设备是否指引过新人礼包
   */
  public static boolean getGiftGuide(Context context) {
    return getSharePref(context, IS_GIFT_GUIDE_SHOW, false);
  }

  /***
   * 设置当前设备是否指引过新人礼包
   */
  public static void setGiftGuide(Context context, boolean flag) {
    setSharePref(context, IS_GIFT_GUIDE_SHOW, flag);
  }

  /***
   * 获取当前设备是否自动打开过书架
   * 注意与 getAutoOpenBookList 不同
   */
  public static boolean hasOpenBookList(Context context) {
    return getSharePref(context, HAS_AUTO_OPEN_BOOK_LIST, false);
  }

  /***
   * 设置当前设备是否自动打开过书架
   * 注意与 setAutoOpenBookList 不同
   */
  public static void autoOpenBookList(Context context, boolean flag) {
    setSharePref(context, HAS_AUTO_OPEN_BOOK_LIST, flag);
  }

  /** 保存新人礼包需要的用户信息 */
  public static void setGiftUserInfo(Context context, String json) {
//    setSharePref(context, GIFT_USER_INFO, json);
    //新人礼包暂时取消
    setSharePref(context, GIFT_USER_INFO, "");
  }

  /***获取新人礼包需要的信息**/
  public static String getReferenceTips(Context context) {
    return getSharePref(context, REFERENCE_TIPS, "");
  }

  /** 保存pschool tips信息 */
  public static void setReferenceTips(Context context, String json) {
    setSharePref(context, REFERENCE_TIPS, json);
  }

  /***获取新人礼包需要的信息**/
  public static String getGiftUserInfo(Context context) {
    return getSharePref(context, GIFT_USER_INFO, "");
  }

  /** 保存报名版tab menu 数据 */
  public static void setTeacherTabMenu(Context context, String menu) {
    setSharePref(context, TEACHER_TAB_MENU, menu);
  }

  /***获取报名版tab menu 数据**/
  public static String getTeacherTabMenu(Context context) {
    return getSharePref(context, TEACHER_TAB_MENU, "招生,上课");
  }

  /** 保存报名版用户头像 数据 */
  public static void setTeacherUserAvator(Context context, String avator) {
    setSharePref(context, TEACHER_USER_AVATOR, avator);
  }

  /***获取报名版用户头像 数据**/
  public static String getTeacherUserAvator(Context context) {
    return getSharePref(context, TEACHER_USER_AVATOR, "");
  }

  /** 保存报名版用户姓名 数据 */
  public static void setTeacherUserName(Context context, String name) {
    setSharePref(context, TEACHER_USER_NAME, name);
  }

  /***获取报名版用户姓名 数据**/
  public static String getTeacherUserName(Context context) {
    return getSharePref(context, TEACHER_USER_NAME, "");
  }

  /** 保存报名版用户机构 数据 */
  public static void setTeacherUserPart(Context context, String part) {
    setSharePref(context, TEACHER_USER_PART, part);
  }

  /***获取报名版用户电话 数据**/
  public static String getTeacherUserPhone(Context context) {
    return getSharePref(context, TEACHER_USER_PHONE, "");
  }

  /** 保存报名版用户电话 数据 */
  public static void setTeacherUserPhone(Context context, String part) {
    setSharePref(context, TEACHER_USER_PHONE, part);
  }

  /***获取报名版用户机构 数据**/
  public static String getTeacherUserPart(Context context) {
    return getSharePref(context, TEACHER_USER_PART, "");
  }

  /** 保存报名版用户信息背景 数据 */
  public static void setTeacherUserBg(Context context, String bg) {
    setSharePref(context, TEACHER_USER_BG, bg);
  }

  /***获取报名版用户信息背景 数据**/
  public static String getTeacherUserBg(Context context) {
    return getSharePref(context, TEACHER_USER_BG, "");
  }

  public static long getVideoSize(Context context, String video) {
    return PreferenceUtil.get(context).getSP("video_size").getLong(video, 0L);
  }

  public static void saveVideoSize(Context context, String video, long size) {
    PreferenceUtil.get(context).getSP("video_size").edit().putLong(video, size).apply();
  }

  public static long getVideoSizeHD(Context context, String video) {
    return PreferenceUtil.get(context).getSP("video_size").getLong(video + "_HD", 0L);
  }

  public static void saveVideoSizeHD(Context context, String video, long size) {
    PreferenceUtil.get(context).getSP("video_size").edit().putLong(video + "_HD", size).apply();
  }

  /***
   * 是否绑定过手机 默认false
   */
  public static boolean getCustomPad(Context context) {
    return getSharePref(context, CUSTOMPAD, false);
  }

  /***
   * 设置当前用户是否绑定过手机号码
   */
  public static void setCustomPad(Context context, boolean flag) {
    setSharePref(context, CUSTOMPAD, flag);
  }

  /***
   * 日历提醒是否开启
   */
  public static boolean isCalendarOpen(Context context) {
    return getSharePref(context, PREF_CALENDAR_STATUS, true);
  }

  /***
   * 设置当前设备是开启日历提醒
   */
  public static void setOpenCalendar(Context context, boolean flag) {
    setSharePref(context, PREF_CALENDAR_STATUS, flag);
  }
  public static void setShortVideoGuideFlag(Context context, boolean flag){
    setSharePref(context, SHORT_VIDEO_GUIDE_FLAG, flag);
  }

  public static boolean getShortVideoGuideFlag(Context context){
    return getSharePref(context, SHORT_VIDEO_GUIDE_FLAG, false);
  }

  public static String getClickreadUrlParam(Context context) {
    String param = PreferenceUtil.getSharePref(context, "clickread_url_param", "");
    PreferenceUtil.setSharePref(context, "clickread_url_param", "");
    return param;
  }
}
