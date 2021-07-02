package com.namibox.tools;

import android.os.Handler;
import android.text.TextUtils;
import com.example.picsdk.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.namibox.commonlib.event.ScoreDialogEvent;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import org.greenrobot.eventbus.EventBus;

/**
 * @Description 弹框评分工具类
 * @CreateTime: 2020/1/16 13:56
 * @Author: zhangkx
 */
public class ScoreDialogUtil {

  private static final String TAG = "ScoreDialogUtil";
  /** 评分文件名称 **/
  public static final String FILE_NAME = ".Score.text";
  /** 点读使用次数 **/
  public static final String KEY_DIANDU = "key_dian_du_use_times";
  /** 网校课程结束 **/
  public static final String KEY_WX = "key_wx_first_complete";
  /** 微课使用次数 **/
  public static final String KEY_WK = "key_wk_use_times";
  /** 口语评测使用次数 **/
  public static final String KEY_EVALUATION = "key_evaluation_use_times";
  /** 评分弹框出现的时间 **/
  public static final String KEY_DIALOG_SHOW_TIME = "key_dialog_show_time";
  /** 当前版本号 **/
  public static final String KEY_BUILD_VERSION = "key_build_version";
  /** 40天的时长毫秒 **/
  public static final long KEY_40_DAY_TIME_MILLIS = 40 * 24 * 60 * 60 * 1000l;


  public static void saveDianduTime(int time) {
    String scoreInfo = getScoreInfo();
    try {
      Logger.e(TAG, "   scoreInfo = " + scoreInfo);
      JsonObject jsonObject = new Gson().fromJson(scoreInfo, JsonObject.class);
      if (jsonObject != null && jsonObject.has(KEY_DIANDU)) {
        jsonObject.remove(KEY_DIANDU);
        jsonObject.addProperty(KEY_DIANDU, time);
        //刷新保存的点读的次数
        Logger.e(TAG, "time = " + time);
        updateScoreInfo(jsonObject.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(TAG, " 设置点读次数出错");
    }
  }

  /** 更新本地保存的评分 */
  private static void updateScoreInfo(String scoreInfo) {
    FileUtil.deleteFile(FILE_NAME);
    FileUtil.saveDeviceFile(scoreInfo, FILE_NAME);
  }

  /***
   * 获取 点读 使用的次数
   * @return
   */
  public static int getDianduSaveTimes() {
    String scoreInfo = getScoreInfo();
    try {
      Logger.e(TAG, "   scoreInfo = " + scoreInfo);
      JsonObject jsonObject = new Gson().fromJson(scoreInfo, JsonObject.class);
      if (jsonObject != null && jsonObject.has(KEY_DIANDU)) {
        long showTime = jsonObject.get(KEY_DIANDU).getAsLong();
        Logger.e(TAG, "showTime = " + showTime);
        return jsonObject.get(KEY_DIANDU).getAsInt();
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(TAG, " 获取点读次数出错");
    }
    return 0;
  }

  /** 保存微课使用次数 */
  public static void saveWKTime(int time) {
    String scoreInfo = getScoreInfo();
    try {
      Logger.e(TAG, "   scoreInfo = " + scoreInfo);
      JsonObject jsonObject = new Gson().fromJson(scoreInfo, JsonObject.class);
      if (jsonObject != null && jsonObject.has(KEY_WK)) {
        jsonObject.remove(KEY_WK);
        jsonObject.addProperty(KEY_WK, time);
        //刷新保存的微课的次数
        Logger.e(TAG, "time = " + time);
        updateScoreInfo(jsonObject.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(TAG, " 设置微课次数出错");
    }
  }

  /***
   * 获取 微课 使用的次数
   * @return
   */
  public static int getWKSaveTimes() {
    String scoreInfo = getScoreInfo();
    try {
      Logger.e(TAG, "   scoreInfo = " + scoreInfo);
      JsonObject jsonObject = new Gson().fromJson(scoreInfo, JsonObject.class);
      if (jsonObject != null && jsonObject.has(KEY_WK)) {
        long showTime = jsonObject.get(KEY_WK).getAsLong();
        Logger.e(TAG, "showTime = " + showTime);
        return jsonObject.get(KEY_WK).getAsInt();
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(TAG, " 获取微课使用次数出错");
    }
    return 0;
  }

  /***
   * 获取 口语评测 使用的次数
   * @return
   */
  public static int getEvaluationSaveTimes() {
    String scoreInfo = getScoreInfo();
    try {
      Logger.e(TAG, "   scoreInfo = " + scoreInfo);
      JsonObject jsonObject = new Gson().fromJson(scoreInfo, JsonObject.class);
      if (jsonObject != null && jsonObject.has(KEY_EVALUATION)) {
        long showTime = jsonObject.get(KEY_EVALUATION).getAsLong();
        Logger.e(TAG, "showTime = " + showTime);
        return jsonObject.get(KEY_EVALUATION).getAsInt();
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(TAG, " 获取口语评测次数出错");
    }
    return 0;
  }

  /** 保存口语评测使用次数 */
  public static void saveEvaluationTime(int time) {
    String scoreInfo = getScoreInfo();
    try {
      Logger.e(TAG, "   scoreInfo = " + scoreInfo);
      JsonObject jsonObject = new Gson().fromJson(scoreInfo, JsonObject.class);
      if (jsonObject != null && jsonObject.has(KEY_EVALUATION)) {
        jsonObject.remove(KEY_EVALUATION);
        jsonObject.addProperty(KEY_EVALUATION, time);
        //刷新保存的口语评测的次数
        Logger.e(TAG, "time = " + time);
        updateScoreInfo(jsonObject.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(TAG, " 设置口语评测次数出错");
    }
  }

  /** 获取评分弹框是否展示过 */
  public static boolean getDialogIsShow() {
    return getDialogShowTime() <= 0 ? false : true;
  }

  /***
   * 获取 弹框出现的时间
   * @return
   */
  public static long getDialogShowTime() {
    String scoreInfo = getScoreInfo();
    try {
      Logger.e(TAG, "   scoreInfo = " + scoreInfo);
      JsonObject jsonObject = new Gson().fromJson(scoreInfo, JsonObject.class);
      if (jsonObject != null && jsonObject.has(KEY_DIALOG_SHOW_TIME)) {
        long showTime = jsonObject.get(KEY_DIALOG_SHOW_TIME).getAsLong();
        Logger.e(TAG, "showTime = " + showTime);
        return jsonObject.get(KEY_DIALOG_SHOW_TIME).getAsLong();
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(TAG, " 获取评分弹框出现的时间出错");
    }

    return 0;
  }

  /***
   * 获取 app
   * @return
   */
  public static String getAppVersion() {
    String scoreInfo = getScoreInfo();
    try {
      Logger.e(TAG, "   scoreInfo = " + scoreInfo);
      JsonObject jsonObject = new Gson().fromJson(scoreInfo, JsonObject.class);
      if (jsonObject != null && jsonObject.has(KEY_BUILD_VERSION)) {
        String version = jsonObject.get(KEY_BUILD_VERSION).getAsString();
        Logger.e(TAG, "version = " + version);
        return jsonObject.get(KEY_BUILD_VERSION).getAsString();
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(TAG, " 获取评分弹框出现的当前版本出错");
    }

    return "";
  }

  /** 保存版本信息 */
  public static void saveScoreDialogVersion(String version) {
    String scoreInfo = getScoreInfo();
    try {
      Logger.e(TAG, "   scoreInfo = " + scoreInfo);
      JsonObject jsonObject = new Gson().fromJson(scoreInfo, JsonObject.class);
      if (jsonObject != null && jsonObject.has(KEY_BUILD_VERSION)) {
        jsonObject.remove(KEY_BUILD_VERSION);
        jsonObject.addProperty(KEY_BUILD_VERSION, version);
        //刷新保存的版本信息
        Logger.e(TAG, "version = " + version);
        updateScoreInfo(jsonObject.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(TAG, " 设置版本信息出错");
    }
  }

  /** 保存网校使用过的信息 */
  public static void saveWxComplete() {
    String scoreInfo = getScoreInfo();
    try {
      Logger.e(TAG, "   scoreInfo = " + scoreInfo);
      JsonObject jsonObject = new Gson().fromJson(scoreInfo, JsonObject.class);
      if (jsonObject != null && jsonObject.has(KEY_WX)) {
        jsonObject.remove(KEY_WX);
        jsonObject.addProperty(KEY_WX, true);
        //刷新保存的版本信息
        Logger.e(TAG, "网校使用过  ");
        updateScoreInfo(jsonObject.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(TAG, " 设置网校使用过出错");
    }
  }

  /** 获取网校是否完成过 */
  public static boolean getWxComplete() {
    String scoreInfo = getScoreInfo();
    try {
      Logger.e(TAG, "   scoreInfo = " + scoreInfo);
      JsonObject jsonObject = new Gson().fromJson(scoreInfo, JsonObject.class);
      if (jsonObject != null && jsonObject.has(KEY_WX)) {
        return jsonObject.get(KEY_WX).getAsBoolean();
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(TAG, " 设置网校使用过出错");
    }
    return false;
  }

  public static void clearScore(String appVersion) {
    //判断版本是否一致
    if (TextUtils.isEmpty(getAppVersion())) {
      saveScoreDialogVersion(appVersion);
      Logger.e(TAG, " 版本数据为空  第一次使用该功能或者被重置清零过 ");
    } else if (TextUtils.equals(appVersion, getAppVersion())) {
      //版本相同 判断时间
      long showTime = getDialogShowTime();
      long currentTimeMillis = System.currentTimeMillis();
      if (showTime != 0 && currentTimeMillis - showTime > KEY_40_DAY_TIME_MILLIS) {
        FileUtil.deleteFile(FILE_NAME);
        Logger.e(TAG, " 版本相同但弹框出现的时间大于40天 重置清零");
      } else {
        Logger.e(TAG, " 版本相同但是弹框出现的时间小于40天 不能重置清零");
      }
    } else {
      FileUtil.deleteFile(FILE_NAME);
      Logger.e(TAG, " 版本发生变化 重置清零");
    }
  }

  /** 获取本地保存评分文件 */
  public static String getScoreInfo() {
    if (TextUtils.isEmpty(FileUtil.getDeviceFile(".Score.text"))) {
      String scoreInfo = createJson();
      FileUtil.saveDeviceFile(scoreInfo, FILE_NAME);
      Logger.e(TAG, "  本地未保存评分文件 新增并保存 scoreInfo = " + scoreInfo);
      return scoreInfo;
    }
    return FileUtil.getDeviceFile(FILE_NAME);
  }

  /** 处理评分弹框逻辑 */
  public static void handleScoreEvent(final int type) {
    //    评分弹框是否展示过
    if (getDialogIsShow()) {
      return;
    }
    switch (type) {
      case 0:
        int saveTimes = getDianduSaveTimes();
        int currentTime = saveTimes + 1;
        //对话框没有展示过 并且书本使用次数超过10次
        if (currentTime >= 10) {
          new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              //延时发送广播 保证当前页面被关闭
              EventBus.getDefault().post(new ScoreDialogEvent(type));
            }
          }, 500);

        }
        Logger.e(" zkx 点读使用过的次数 currentTime = " + currentTime);
        saveDianduTime(currentTime);
        break;
      case 1:
        if (getWxComplete()) {
          return;
        }
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            //延时发送广播 保证当前页面被关闭
            EventBus.getDefault().post(new ScoreDialogEvent(type));
          }
        }, 500);
        Logger.e(" zkx 网校使用过 ");
        saveWxComplete();
        break;
      case 2:
        int wkTimes = getWKSaveTimes();
        int currentWkTime = wkTimes + 1;
        //对话框没有展示过 并且微课使用次数超过3次
        if (currentWkTime >= 3) {
          new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              //延时发送广播 保证当前页面被关闭
              EventBus.getDefault().post(new ScoreDialogEvent(type));
            }
          }, 500);

        }
        saveWKTime(currentWkTime);
        Logger.e(" zkx 微课使用过的次数 currentWkTime = " + currentWkTime);
        break;
      case 3:
        int evaluationTimes = getEvaluationSaveTimes();
        int currentevaluationTime = evaluationTimes + 1;
        //对话框没有展示过 并且口语评测使用次数超过3次
        if (currentevaluationTime >= 3) {
          new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              //延时发送广播 保证当前页面被关闭
              EventBus.getDefault().post(new ScoreDialogEvent(type));
            }
          }, 500);

        }
        saveEvaluationTime(currentevaluationTime);
        Logger.e(" zkx 口语评测使用过的次数 currentevaluationTime = " + currentevaluationTime);
        break;
      default:
        break;
    }
  }

  public static void saveDialogShowTime(long timeMills) {
    if (timeMills <= 0) {
      return;
    }
    String scoreInfo = getScoreInfo();
    try {
      Logger.e(TAG, "   scoreInfo = " + scoreInfo);
      JsonObject jsonObject = new Gson().fromJson(scoreInfo, JsonObject.class);
      if (jsonObject != null && jsonObject.has(KEY_DIALOG_SHOW_TIME)) {
        jsonObject.remove(KEY_DIALOG_SHOW_TIME);
        jsonObject.addProperty(KEY_DIALOG_SHOW_TIME, timeMills);
        //刷新弹框出现得时间
        Logger.e(TAG, " timeMills = " + timeMills);
        updateScoreInfo(jsonObject.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(TAG, " 保存弹框出现时间出错");
    }
  }

  private static String createJson() {
    JsonObject scoreData = new JsonObject();
    scoreData.addProperty(KEY_DIANDU, 0);
    scoreData.addProperty(KEY_WX, false);
    scoreData.addProperty(KEY_WK, 0);
    scoreData.addProperty(KEY_EVALUATION, 0);
    scoreData.addProperty(KEY_DIALOG_SHOW_TIME, 0);
    scoreData.addProperty(KEY_BUILD_VERSION, BuildConfig.VERSION_NAME);
    return scoreData.toString();
  }
}
