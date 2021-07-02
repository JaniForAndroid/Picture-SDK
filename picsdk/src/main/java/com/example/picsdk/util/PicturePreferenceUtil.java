package com.example.picsdk.util;

import android.content.Context;
import android.content.SharedPreferences;

import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namibox.commonlib.event.LoginStatusEvent;
import com.namibox.tools.ThinkingAnalyticsHelper;
import com.namibox.util.FileUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;

import org.greenrobot.eventbus.EventBus;

import static com.namibox.util.PreferenceUtil.PREF_USER_ID;

/**
 * author : feng
 * description ：
 * creation time : 19-10-31上午10:01
 */
public class PicturePreferenceUtil {

  public static final String PREF_PROVINCE = "user_province";
  public static final String PREF_CITY = "user_city";
  public static final String PREF_STREET = "user_street";
  public static final String PREF_LA = "user_latitude";
  public static final String PREF_LO = "user_longitude";
  public static final String PREF_DISTRICT = "user_district";
  public static final String PREF_ADDRESS = "user_address";
  public static final String PREF_STREET_NUMBER = "user_street_number";
  private static final String USER_AUTH = "user_auth";
  public static final String PREF_IM_USAR = "im_user";
  public static final String PREF_USER_PHONE = "user_phone";
  public static final String PREF_USER_ROLE = "user_role";
  public static final String PREF_CACHE_SIZE = "cache_size";

  public static void saveUserAuth(Context context, String userId, String pswd) {
    SharedPreferences pref = context.getSharedPreferences(USER_AUTH, Context.MODE_PRIVATE);
    pref.edit().putString(userId + "_userAuth", pswd).apply();
  }

  public static String getUserAuth(Context context, String userId) {
    SharedPreferences pref = context.getSharedPreferences(USER_AUTH, Context.MODE_PRIVATE);
    return pref.getString(userId+"_userAuth","");
  }

  public static void onLogout(Context context) {
    //退出的时候删除保存至本地的用户数据
    FileUtil.deleteFile(USER_INFORMATION);
    PreferenceUtil.setSharePref(context, PreferenceUtil.PREF_LOGIN_TIME, 0L);
    String uid = Utils.getLoginUserId(context);
    PreferenceUtil.setLongLoginUserId(context, -1L);
    PreferenceUtil.setSharePref(context, PreferenceUtil.PREF_EXPIRE_TIME, 0L);
    //是否绑定手机号码
    PreferenceUtil.setBindPhone(context, false);
    PreferenceUtil.setSharePref(context, PreferenceUtil.PREF_SESSION_ID, "", true);
    PicturePreferenceUtil.saveUserAuth(context, uid, "");
    PreferenceUtil.saveHeadImage(context, uid, "");
    EventBus.getDefault().post(new LoginStatusEvent(false));
    ThinkingAnalyticsHelper.logout();
  }
  public static final String USER_INFORMATION = ".AndroidInformationPic.txt";

  public static boolean getPageEnable(Context context, String name) {
    return PreferenceUtil.get(context).getSP("page_state").getBoolean(name, true);
  }

  public static void setPageEnable(Context context, String name, boolean value) {
    PreferenceUtil.get(context).getSP("page_state").edit().putBoolean(name, value).apply();
  }

  public static long getLongLoginUserId(Context context) {
    return PreferenceUtil.getSharePref(context, PREF_USER_ID, -1L);
  }
}
