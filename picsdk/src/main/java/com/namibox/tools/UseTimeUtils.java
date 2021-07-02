package com.namibox.tools;

import android.content.Context;
import android.text.TextUtils;
import com.namibox.util.Logger;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class UseTimeUtils {

  private static boolean mIsBackground;

  public static void update(Context context, boolean isBackground, String curGrade) {
    mIsBackground = isBackground;
    long startUse = PreferenceUtil.getSharePref(context, PreferenceUtil.START_USE, 0l);
    long useDuration = PreferenceUtil.getSharePref(context, PreferenceUtil.USE_DURATION, 0l);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date date = new Date(startUse);
    String day1 = simpleDateFormat.format(date);
    date = new Date(System.currentTimeMillis());
    String day2 = simpleDateFormat.format(date);
    if (isBackground) {
      if (startUse != 0) {
        if (!TextUtils.equals(day1, day2)) {
          trackUseTime(context, useDuration, startUse, curGrade);
          //更新开始时间戳
          startUse = date.getTime();
          PreferenceUtil.setSharePref(context, PreferenceUtil.START_USE, System.currentTimeMillis());
          useDuration = System.currentTimeMillis() - startUse;
        } else {
          useDuration += System.currentTimeMillis() - startUse;
        }
      }
      Logger.e("学习时间： 退到后台-->" + useDuration / 1000 / 60);
      PreferenceUtil.setSharePref(context, PreferenceUtil.USE_DURATION, useDuration);
    } else {
      if (!TextUtils.equals(day1, day2) && startUse != 0) {
        trackUseTime(context, useDuration, startUse, curGrade);
        //重置使用时长
        PreferenceUtil.setSharePref(context, PreferenceUtil.USE_DURATION, 0l);
        StudyTimeRecordUtils.getInstance().today = 0;
        StudyTimeRecordUtils.getInstance().getStudyTime(context);
      }
      Logger.e("学习时间", "上一天: " + day1 + "当前: " + day2);
      Logger.e("学习时间： 进入前台-->" + TimeUtil.getDate(System.currentTimeMillis()));
      PreferenceUtil.setSharePref(context, PreferenceUtil.START_USE, System.currentTimeMillis());
    }
  }

  private static void trackUseTime(Context context, long useDuration, long startUse, String curGrade){
    if (useDuration < 10 * 1000) {
      return;
    }
    HashMap<String, String> taMap = new HashMap<>();
    taMap.put("status_name", "App启动时长");
    taMap.put("lasttime", useDuration / 1000 + "");
    taMap.put("userid", Utils.getLoginUserId(context));
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    taMap.put("date", simpleDateFormat.format(new Date(startUse)));
    taMap.put("grade", curGrade);
    ThinkingAnalyticsHelper.trackEvent(com.namibox.commonlib.constant.Events.TA_STATUS_NB_APP_EVENT, taMap);
  }

  public static boolean isBackground(){
    return mIsBackground;
  }

  public static void setTime(Context context, long duration){
    PreferenceUtil.setSharePref(context, PreferenceUtil.USE_DURATION, duration);
    PreferenceUtil.setSharePref(context, PreferenceUtil.START_USE, System.currentTimeMillis());
  }

  public static long getTime(Context context) {
    long startUse = PreferenceUtil.getSharePref(context, PreferenceUtil.START_USE, 0l);
    long useDuration = PreferenceUtil.getSharePref(context, PreferenceUtil.USE_DURATION, 0l);
    Date date = new Date(startUse);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String day1 = simpleDateFormat.format(date);
    date = new Date(System.currentTimeMillis());
    String day2 = simpleDateFormat.format(date);
    Logger.e("学习时间: 上一天: " + day1 + " useDuration " + useDuration / 1000 / 60 + " 当前: " + day2);
    if (!TextUtils.equals(day1, day2)) {
      StudyTimeRecordUtils.getInstance().today = 0;
      StudyTimeRecordUtils.getInstance().getStudyTime(context);
      startUse = date.getTime();
      useDuration = 0;
    }
    useDuration = useDuration + System.currentTimeMillis() - startUse;
    Logger.e("学习时间：useDuration-->" + useDuration / 1000 / 60);
    PreferenceUtil.setSharePref(context, PreferenceUtil.USE_DURATION, useDuration);
    PreferenceUtil.setSharePref(context, PreferenceUtil.START_USE, System.currentTimeMillis());
    return useDuration;
  }
}
