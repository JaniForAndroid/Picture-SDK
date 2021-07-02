package com.namibox.tools;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.picsdk.R;
import com.namibox.commonlib.constant.Events;
import com.namibox.util.Logger;
import com.namibox.util.Utils;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;

public class ThinkingAnalyticsHelper {

  //  private static ThinkingAnalyticsSDK instance;
  public static HashMap<String, Object> readingBasePropertiesMap;
  private static Toast toast;//测试时显示埋点信息；
  private static TextView textView;

  public void init(@NonNull Context context, @NonNull String appId, @NonNull String serverUrl) {
//    instance = ThinkingAnalyticsSDK.sharedInstance(context, appId, serverUrl);
//    if (BuildConfig.DEBUG) {
//      initToast(context);
//      ThinkingAnalyticsSDK.enableTrackLog(true);
//    }
  }

  /*******************************业务相关方法***************************************/
  public static void setSupperProperties(@NonNull String key, @NonNull String value) {
    HashMap<String, String> map = new HashMap<>();
    map.put(key, value);
    setSuperProperties(map);
  }

  /**
   * 增加点读模块基础属性
   */
  public static void addReadingBaseProperties(HashMap<String, Object> map) {
    if (readingBasePropertiesMap != null) {
      map.putAll(readingBasePropertiesMap);
    }
  }

  /**
   * 点读模块点击事件
   */
  public static void trackClickReading(String button_name) {
    trackReadigClickEvent("点读体验", button_name);
  }

  /**
   * 进入点读体验
   */
  public static void enterReadingPage() {
    HashMap<String, Object> map = new HashMap<>();
    addReadingBaseProperties(map);
    map.put("page", "点读体验");
    trackEvent(Events.TA_EVENT_NB_APP_VIEW_ENTER, map);
  }

  /**
   * 退出点读体验页面
   */
  public static void closeReadingPage() {
    HashMap<String, Object> map = new HashMap<>();
    addReadingBaseProperties(map);
    map.put("page", "点读体验");
    map.put("status_name", "页面停留时长");
    trackEvent(Events.TA_EVENT_NB_APP_VIEW_CLOSE, map);
  }

  /**
   * 点读模块点击事件
   */
  public static void trackReadigClickEvent(String pageName, String buttonName) {
    HashMap<String, Object> map = new HashMap<>();
    addReadingBaseProperties(map);
    trackCommonClickEvent(pageName, buttonName, map);
  }

  /**
   * 通用点击事件
   */
  public static void trackCommonClickEvent(String pageName, String buttonName) {
    trackCommonClickEvent(pageName, buttonName, null);
  }

  /**
   * 通用点击事件
   */
  public static void trackCommonClickEvent(String pageName, String buttonName, Map param) {
    HashMap<String, Object> map = new HashMap<>();
    if (param != null) {
      map.putAll(param);
    }
    map.put("button", buttonName);
    map.put("page", pageName);
    trackEvent(Events.TA_EVENT_NB_APP_CLICK, map);
  }

  public static void enterPageEvent(String pageName) {
//    if (TextUtils.isEmpty(pageName)) {
//      Logger.e(Events.TA_EVENT_NB_APP_VIEW_ENTER + ":page Name is null");
//      return;
//    }
//    HashMap<String, String> map = new HashMap<>();
//    map.put("page", pageName);
//    trackEvent(Events.TA_EVENT_NB_APP_VIEW_ENTER, map);
  }

  public static void closePageEvent(String pageName) {
    if (TextUtils.isEmpty(pageName)) {
      Logger.e(Events.TA_EVENT_NB_APP_VIEW_CLOSE + ":page Name is null");
      return;
    }
    HashMap<String, String> map = new HashMap<>();
    map.put("page", pageName);
    trackEvent(Events.TA_EVENT_NB_APP_VIEW_CLOSE, map);
  }

  /**********************************基础方法*******************************************/
  private static JSONObject convertJson(Map map) {
    JSONObject jsonObject = new JSONObject();
    for (Object o : map.entrySet()) {
      Entry next = (Entry) o;
      try {
        jsonObject.put((String) next.getKey(), next.getValue());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return jsonObject;
  }

  /**
   * 开启自动采集功能
   */
  public static void openAutoTrack() {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }

//    List<AutoTrackEventType> eventTypeList = new ArrayList<>();
//    //APP安装事件
//    eventTypeList.add(ThinkingAnalyticsSDK.AutoTrackEventType.APP_INSTALL);
//    //APP启动事件
//    eventTypeList.add(ThinkingAnalyticsSDK.AutoTrackEventType.APP_START);
//    //APP关闭事件
//    eventTypeList.add(ThinkingAnalyticsSDK.AutoTrackEventType.APP_END);
////    //APP浏览页面事件
////    eventTypeList.add(ThinkingAnalyticsSDK.AutoTrackEventType.APP_VIEW_SCREEN);
////    //APP点击控件事件
////    eventTypeList.add(ThinkingAnalyticsSDK.AutoTrackEventType.APP_CLICK);
////    //APP崩溃事件
////    eventTypeList.add(ThinkingAnalyticsSDK.AutoTrackEventType.APP_CRASH);
//    //开启自动采集事件
//    instance.enableAutoTrack(eventTypeList);
  }

  /**
   * 设置用户的账号ID，设置后用户上传的数据中将带有#account_id这一字段
   *
   * @param accout 设置的账号ID
   */
  public static void login(String accout) {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    instance.login(accout);
  }

  /**
   * 清除账号ID，设置后用户上传的数据中将没有#account_id这一字段
   */
  public static void logout() {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    instance.logout();
  }

  /**
   * 设置用户的访客ID，SDK默认以UUID作为用户的访客ID
   *
   * @param id 设置的访客ID
   */
  public static void identify(String id) {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    instance.identify(id);
  }

  /**
   * 设置用户属性
   *
   * @param map 需要设置的用户属性
   */
  public static void setUserProperty(Map map) {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    instance.user_set(convertJson(map));
  }

  /**
   * 设置用户属性，该属性有值则不写入
   *
   * @param map 需要设置的用户属性
   */
  public static void setOnceUserProperty(Map map) {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    instance.user_setOnce(convertJson(map));
  }

  /**
   * 累加用户属性,对数值型的用户属性进行累加操作，输入负值相当于减法操作
   *
   * @param map 需要进行累加操作的用户属性
   */
  public static void setUserAdd(Map map) {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    instance.user_add(convertJson(map));
  }

  /**
   * 对数值型的用户属性进行累加操作，输入负值相当于减法操作，只设置一个属性
   *
   * @param key 需要进行累加操作的用户属性名
   * @param value 累加的属性值，输入负值相当于减法操作
   */
  public static void setUserAdd(String key, Number value) {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    instance.user_add(key, value);
  }

  /**
   * 追踪一个事件，该事件将会先保存在本地，上传按每20条（可配置）或每15秒（可配置）发送一次。
   *
   * @param eventName 事件的名称
   * @param map 事件的属性
   */
  public static void trackEvent(String eventName, Map map) {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    if (BuildConfig.DEBUG && textView != null && toast != null) {
////      textView.setText("eventName:"+eventName+";\nproperties:"+convertJson(map));
////      toast.setView(textView);
////      toast.show();
//    }
//    instance.track(eventName, convertJson(map));
  }

  /**
   * 记录事件时长
   *
   * @param eventName 您需要计时的事件的名称，当使用track上传该事件名的事件时，计时停止，并上传计时数据。
   */
  public static void timeEvent(String eventName) {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    instance.timeEvent(eventName);
  }

  /**
   * 设置公共事件属性
   *
   * @param map 公共事件属性
   */
  public static void setSuperProperties(@NonNull Map map) {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    instance.setSuperProperties(convertJson(map));
  }

  /**
   * 删除已设置的事件公共属性
   *
   * @param superPropertyName 需要删除的事件公共属性的属性名
   */
  public static void unsetSuperProperties(String superPropertyName) {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    instance.unsetSuperProperty(superPropertyName);
  }

  /**
   * 清空所有已设置的事件公共属性
   */
  public static void clearSuperProperties() {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    instance.clearSuperProperties();
  }


  public static void setJsBridge(@NonNull WebView webView) {
//    if (instance == null) {
//      Logger.e("Please init ThinkingAnalyticsSDK first.");
//      return;
//    }
//    instance.setJsBridge(webView);
  }

  public static void initToast(Context context) {
    try {
      textView = new TextView(context.getApplicationContext());
      textView.setBackgroundResource(R.drawable.toast_bg);
      textView.setGravity(Gravity.CENTER);
      int padding1 = Utils.dp2px(context, 10);
      int padding2 = Utils.dp2px(context, 18);
      textView.setPadding(padding2, padding1, padding2, padding1);
      textView.setTextSize(16);
      textView.setTextColor(0xffffffff);

      toast = new Toast(context.getApplicationContext());
      toast.setDuration(Toast.LENGTH_LONG);
      toast.setGravity(Gravity.CENTER, 0, 0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
