package com.namibox.tools;

import android.content.Context;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.model.StudyRecord;
import com.namibox.util.Logger;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class StudyTimeRecordUtils {

  public static String CLICK_READ = "click_read";
  public static String EVALUATION = "evaluation";
  public static String LEARN = "learn";
  public long today;
  public long total;
  public long yestDayTotal;
  public boolean isRequestSuccess = true;
  private volatile static StudyTimeRecordUtils instance;

  public static StudyTimeRecordUtils getInstance() {
    if (instance == null) {
      synchronized (StudyTimeRecordUtils.class) {
        if (instance == null) {
          instance = new StudyTimeRecordUtils();
        }
      }
    }
    return instance;
  }

  public void reset(){
    today = 0;
    yestDayTotal = 0;
    total = 0;
  }

  public void record(Context context, String type, long duration) {
    if (duration < 3 * 1000) {
      Logger.d("学习时长少于3s不上报：" + duration);
    }
    String userId = Utils.getLoginUserId(context);
    LinkedHashMap<String, String> map = PreferenceUtil
        .getStudyTime(context, userId + type);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    long startTime = System.currentTimeMillis() - duration;
    String startDate = simpleDateFormat.format(new Date(startTime));
    String currentDate = simpleDateFormat.format(new Date(System.currentTimeMillis()));
    List<StudyRecord> studyRecords = new ArrayList<>();

    if (!TextUtils.equals(startDate, currentDate)) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      long pastDuration = calendar.getTimeInMillis() - startTime;
      Logger.d("学习时长上报 pastDuration:" + pastDuration);
      if (pastDuration > 0) {
        StudyRecord studyRecord = new StudyRecord();
        studyRecord.day = startDate;
        studyRecord.duration = pastDuration / 1000;
        studyRecords.add(studyRecord);
        duration = duration - pastDuration;
        if (duration <= 0) {
          Logger.d("学习时长上报 duration数据异常:" + duration);
          return;
        }
      } else {
        return;
      }
    }
    StudyRecord studyRecord = new StudyRecord();
    studyRecord.day = currentDate;
    studyRecord.duration = duration / 1000;
    long currentSeconds = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 * 60
        + Calendar.getInstance().get(Calendar.MINUTE) * 60
        + Calendar.getInstance().get(Calendar.SECOND);
    if (studyRecord.duration > currentSeconds) {
      Logger.d("学习时长上报大于当前时刻点");
      studyRecord.duration = currentSeconds;
    }
    studyRecords.add(studyRecord);
    if (map == null) {
      map = new LinkedHashMap<>();
    }
    for (StudyRecord record : studyRecords) {
      if (map.containsKey(record.day)) {
        map.put(record.day, String.valueOf(Long.parseLong(map.get(record.day)) + record.duration));
      } else {
        map.put(currentDate, String.valueOf(record.duration));
      }
    }
    PreferenceUtil.saveStudyTime(context, userId + type, map);
    report(context, type);

  }

  public void clear(Context context, String type) {
    String userId = Utils.getLoginUserId(context);
    PreferenceUtil.saveStudyTime(context, userId + type, null);
  }

  public void report(final Context context, final String type) {
    //TODO...学习时长上报
    String userId = Utils.getLoginUserId(context);
    if (userId.length() < 3) {
      return;
    }
    List<StudyRecord> studyRecords = new ArrayList<>();
    HashMap<String, String> map = PreferenceUtil.getStudyTime(context, userId + type);
    for (String key : map.keySet()) {
      if (map.get(key) != null) {
        StudyRecord studyRecord = new StudyRecord();
        studyRecord.day = key;
        studyRecord.duration = Long.parseLong(map.get(key));
        studyRecords.add(studyRecord);
      }
    }
    if (studyRecords.size() == 0) {
      return;
    }
    String time_list = new Gson().toJson(studyRecords);
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("type", type);
    jsonObject.addProperty("time_list", time_list);
    jsonObject.addProperty("user_id", userId);
    Logger.d("学习时长上报: " + jsonObject.toString());
    ApiHandler.getBaseApi(context)
        .commonJsonPost("/api/app/learn/updateDuration", jsonObject)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<JsonElement>() {
          @Override
          public void onSubscribe(Disposable d) {
          }

          @Override
          public void onNext(JsonElement reportResponse) {
            Logger.d("学习时长上报成功 " + type);
            clear(context, type);
            if (TextUtils.equals(type, LEARN)) {
              today += UseTimeUtils.getTime(context) / 1000;
              UseTimeUtils.setTime(context, 0);
            }
          }

          @Override
          public void onError(Throwable e) {
            Logger.e(e, "学习时长上报失败");
          }

          @Override
          public void onComplete() {
          }
        });
  }

  public void getStudyTime(Context context) {
    getStudyTime(context, null);
  }

  private boolean isRequesting;

  public void getStudyTime(final Context context,
      final OnGetStudyTimeListener onGetStudyTimeListener) {
    if (isRequesting) {
      return;
    }
    isRequesting = true;
    ApiHandler.getBaseApi()
        .commonJsonGet("/api/app/learn/getDuration")
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<JsonObject>() {

          @Override
          public void onSubscribe(Disposable d) {

          }

          @Override
          public void onNext(JsonObject json) {
            isRequesting = false;
            Logger.d("学习时长获取" + json);
            if (json != null) {
              String retcode = json.get("retcode").getAsString();
              if (TextUtils.equals("SUCC", retcode)) {
                isRequestSuccess = true;
                total = json.has("duration") ? json.get("duration").getAsLong() : 0;
                today = json.has("today") ? json.get("today").getAsLong() : 0;
                yestDayTotal = total - today;
                if (onGetStudyTimeListener != null) {
                  onGetStudyTimeListener.result(true);
                }
                return;
              }
            }
            isRequestSuccess = false;
            if (onGetStudyTimeListener != null) {
              onGetStudyTimeListener.result(false);
            }
          }

          @Override
          public void onError(Throwable e) {
            isRequesting = false;
            Logger.e(e, "学习时长获取失败");
            isRequestSuccess = false;
            if (onGetStudyTimeListener != null) {
              onGetStudyTimeListener.result(false);
            }
          }

          @Override
          public void onComplete() {

          }
        });
  }

  public interface OnGetStudyTimeListener {

    void result(boolean success);
  }

}
