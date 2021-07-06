package com.example.picsdk.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.example.picsdk.ExerciseChallengeActivity;
import com.example.picsdk.PicLoadingActivity;
import com.example.picsdk.R;
import com.example.picsdk.VideoActivity;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.model.MyStore;
import com.example.picsdk.model.PictureBook;
import com.example.picsdk.model.ProductItem;
import com.example.picsdk.model.WatchPic;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.namibox.commonlib.activity.AbsFoundationActivity;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.constant.Events;
import com.namibox.commonlib.event.WorkEvent;
import com.namibox.dub.DubVideoNewActivity;
import com.namibox.hfx.ui.EvalActivity;
import com.namibox.hfx.utils.HfxUtil;
import com.namibox.tools.ThinkingAnalyticsHelper;
import com.namibox.util.Logger;
import com.namibox.util.MD5Util;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.greenrobot.eventbus.EventBus;


/**
 * author : feng
 * creation time : 19-9-9下午3:01
 */
public class AppPicUtil {

  private static final String BOOKS_DIR = "books_dir";
  public static String CHALLENGE_STUDY = "绘本学习";
  public static String CHALLENGE_WORD = "词汇挑战";
  public static String CHALLENGE_READ = "阅读理解";
  public static String CHALLENGE_PLAY = "趣味配音";
  public static String CHALLENGE_PIC = "评测绘本";

  private static long startTime = 0L;

  public static void init(Context context) {
    CHALLENGE_STUDY = context.getString(R.string.book_booklearn_title);
    CHALLENGE_WORD = context.getString(R.string.book_wordchallenge_title);
    CHALLENGE_READ = context.getString(R.string.book_readunderstand_title);
    CHALLENGE_PLAY = context.getString(R.string.book_dubbing_title);
    CHALLENGE_PIC = context.getString(R.string.book_pic_eval_title);
  }

  public static long getStartTime() {
    return startTime;
  }

  public static void setStartTime(long time) {
    startTime = time;
  }

  public static File getBookResource(Context context, String url, long milesson_item_id) {
    return new File(getBookCacheDir(context, milesson_item_id), MD5Util.md5(url));
  }

  private static File getBookCacheDir(Context context, long milesson_item_id) {
    File booksDir = new File(context.getFilesDir(), BOOKS_DIR);
    if (!booksDir.exists()) {
      //noinspection ResultOfMethodCallIgnored
      booksDir.mkdir();
    }
    File bookDir = new File(booksDir, String.valueOf(milesson_item_id));
    if (!bookDir.exists()) {
      //noinspection ResultOfMethodCallIgnored
      bookDir.mkdir();
    }
    return bookDir;
  }

  public static void TagEventEnterPush(boolean isEnter, String title, String page,
      String product_name) {
    HashMap<String, String> map = new HashMap<>();
    map.put("page", page);
    map.put("product_name", product_name);
    map.put("title", title);

    if (isEnter) {
      ThinkingAnalyticsHelper.trackEvent(Events.TA_EVENT_NB_APP_VIEW_ENTER, map);
    } else {
      ThinkingAnalyticsHelper.trackEvent(Events.TA_EVENT_NB_APP_VIEW_CLOSE, map);
    }
  }

  public static void saveLoaclReading(BookManager bookManager, Context context) {
    if (bookManager != null && bookManager.getBookLearning() != null) {
      int star = PreferenceUtil
          .getSharePref(context, bookManager.getMilesson_item_id() + CHALLENGE_WORD + "star", 0) +
          PreferenceUtil
              .getSharePref(context, bookManager.getMilesson_item_id() + CHALLENGE_READ + "star", 0)
          +
          PreferenceUtil
              .getSharePref(context, bookManager.getMilesson_item_id() + CHALLENGE_PLAY + "star",
                  0);

      String storeStr = PreferenceUtil.getSharePref(context, "local_reading", "");
      if (storeStr.equals("")) {
        MyStore myStore = new MyStore();
        MyStore.Data data = new MyStore.Data();
        MyStore.DataList dataList = new MyStore.DataList();
        dataList.category = "myreading";
        JsonObject jsonObject = new JsonObject();
        JsonObject action = new JsonObject();
        String url =
            Utils.getBaseHttpsUrl(context) + "/api/guide/" + bookManager.getMilesson_item_id();
        action.addProperty("url", url);
        jsonObject.add("action", action);
        jsonObject.addProperty("id", bookManager.getMilesson_item_id());
        jsonObject.addProperty("chinese_name",
            ((ProductItem.BookLearning) bookManager.getBookLearning()).chinese_name);
        jsonObject.addProperty("star", star);
        jsonObject
            .addProperty("text", ((ProductItem.BookLearning) bookManager.getBookLearning()).text);
        jsonObject.addProperty("thumb_url",
            ((ProductItem.BookLearning) bookManager.getBookLearning()).thumb_url);
        jsonObject.addProperty("milesson_item_id",
            ((ProductItem.BookLearning) bookManager.getBookLearning()).milesson_item_id);
        jsonObject.addProperty("max_star", 9);
        jsonObject.addProperty("time", System.currentTimeMillis());
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonObject);
        dataList.list = jsonArray;
        List<MyStore.DataList> data_list = new ArrayList<>();
        data_list.add(dataList);
        data.data_list = data_list;
        myStore.data = data;
        PreferenceUtil.setSharePref(context, "local_reading", new Gson().toJson(myStore));
      } else {
        MyStore myStore = new Gson().fromJson(storeStr, MyStore.class);
        List<PictureBook> readings = new Gson()
            .fromJson(myStore.data.data_list.get(0).list, new TypeToken<List<PictureBook>>() {
            }.getType());
        boolean isExist = false;
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < readings.size(); i++) {
          JsonObject jsonObject = new JsonObject();
          JsonObject action = new JsonObject();
          action.addProperty("url",
              Utils.getBaseHttpsUrl(context) + "/api/guide/" + readings.get(i).id);
          jsonObject.add("action", action);
          jsonObject.addProperty("id", readings.get(i).id);
          jsonObject.addProperty("chinese_name", readings.get(i).chinese_name);
          jsonObject.addProperty("star", readings.get(i).star);
          jsonObject.addProperty("text", readings.get(i).text);
          jsonObject.addProperty("thumb_url", readings.get(i).thumb_url);
          jsonObject.addProperty("milesson_item_id", readings.get(i).milesson_item_id);
          jsonObject.addProperty("max_star", readings.get(i).max_star);

          if (readings.get(i).milesson_item_id == ((ProductItem.BookLearning) bookManager
              .getBookLearning()).milesson_item_id) {
            isExist = true;
            jsonObject.addProperty("time", System.currentTimeMillis());
            jsonObject.addProperty("star", star);
          } else {
            jsonObject.addProperty("time", readings.get(i).time);
          }
          jsonArray.add(jsonObject);
        }
        if (!isExist) {
          JsonObject jsonObject = new JsonObject();
          JsonObject action = new JsonObject();
          String url =
              Utils.getBaseHttpsUrl(context) + "/api/guide/" + bookManager.getMilesson_item_id();
          action.addProperty("url", url);
          jsonObject.add("action", action);
          jsonObject.addProperty("id", bookManager.getMilesson_item_id());
          jsonObject.addProperty("chinese_name",
              ((ProductItem.BookLearning) bookManager.getBookLearning()).chinese_name);
          jsonObject.addProperty("star", star);
          jsonObject
              .addProperty("text", ((ProductItem.BookLearning) bookManager.getBookLearning()).text);
          jsonObject.addProperty("thumb_url",
              ((ProductItem.BookLearning) bookManager.getBookLearning()).thumb_url);
          jsonObject.addProperty("milesson_item_id",
              ((ProductItem.BookLearning) bookManager.getBookLearning()).milesson_item_id);
          jsonObject.addProperty("max_star", 9);
          jsonObject.addProperty("time", System.currentTimeMillis());
          jsonArray.add(jsonObject);
        }
        myStore.data.data_list.get(0).list = jsonArray;
        PreferenceUtil.setSharePref(context, "local_reading", new Gson().toJson(myStore));
      }
    }
  }

  public static <T> void randomList(List<T> list) {
    int size = list.size();
    Random random = new Random();
    for (int i = 0; i < size; i++) {
      int randomPos = random.nextInt(size);
      Collections.swap(list, i, randomPos);
    }
  }

  public static boolean isForeground(Context context, String className) {
    if (context == null || TextUtils.isEmpty(className)) {
      return false;
    }
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
    if (list != null && list.size() > 0) {
      ComponentName cpn = list.get(0).topActivity;
      if (className.equals(cpn.getClassName())) {
        return true;
      }
    }
    return false;
  }

  public static boolean isForeground(Activity activity) {
    return isForeground(activity, activity.getClass().getName());
  }

  public static void jumpChallenge(int index, AbsFoundationActivity activity) {
    BookManager bookManager = BookManager.getInstance();
    String nextType = CHALLENGE_STUDY;
    if (bookManager != null && bookManager.getTypeList().size() > index
        && bookManager.getTypeList().get(index) != null) {
      nextType = bookManager.getTypeList().get(index).text;
    }
    if (TextUtils.equals(nextType, CHALLENGE_STUDY)) {
      AppPicUtil.gotoLoading(false, activity);
    } else if (TextUtils.equals(nextType, CHALLENGE_WORD)) {
      AppPicUtil.gotoWordChallenge(activity);
    } else if (TextUtils.equals(nextType, CHALLENGE_READ)) {
      AppPicUtil.gotoReadChallenge(activity);
    } else if (TextUtils.equals(nextType, CHALLENGE_PLAY)) {
      if (bookManager != null && bookManager.getChallenges().size() > 3) {
        if (bookManager.isHomeWorkWatch()) {
          String type = bookManager.getTypeList().get(bookManager.getIndex()).text;
          int id = bookManager.getTypeList().get(bookManager.getIndex()).id;
          String url =
              getPicBaseUrl(activity) + "/api/user/pbook/" + id + "/data?stu_hw_id=" + bookManager
                  .getStu_hw_id();
          Disposable disposable = ApiHandler.getBaseApi().commonJsonGet(url)
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(jsonObject -> {
                String retcode = jsonObject.get("retcode").getAsString();
                if (retcode != null && retcode.equals("success")) {
                  JsonObject data = jsonObject.get("data").getAsJsonObject();
                  WatchPic mWatchPic = new Gson().fromJson(data, WatchPic.class);
                  Intent intent = new Intent(activity, VideoActivity.class);
                  intent.putExtra("video", mWatchPic.url);
                  intent.putExtra("title", mWatchPic.title);
                  intent.putExtra("isHideSkip", true);
                  activity.startActivity(intent);
                }
              }, throwable -> {
                Logger.e(throwable, throwable.toString());
              });
          new CompositeDisposable().add(disposable);
        } else {
          AppPicUtil
              .gotoPlayChallenge(bookManager.getChallenges().get(3).url, bookManager, activity);
        }
      }
    }
//    switch (nextType) {
//      case CHALLENGE_STUDY:
//        AppPicUtil.gotoLoading(false, activity);
//        break;
//      case CHALLENGE_WORD:
//        AppPicUtil.gotoWordChallenge(activity);
//        break;
//      case CHALLENGE_READ:
//        AppPicUtil.gotoReadChallenge(activity);
//        break;
//      case CHALLENGE_PLAY:
//        if (bookManager != null && bookManager.getChallenges().size() > 3) {
//          if (bookManager.isHomeWorkWatch()) {
//            String type = bookManager.getTypeList().get(bookManager.getIndex()).text;
//            int id = bookManager.getTypeList().get(bookManager.getIndex()).id;
//            String url =
//                getPicBaseUrl(activity) + "/api/user/pbook/" + id + "/data?stu_hw_id=" + bookManager
//                    .getStu_hw_id();
//            Disposable disposable = ApiHandler.getBaseApi().commonJsonGet(url)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(jsonObject -> {
//                  String retcode = jsonObject.get("retcode").getAsString();
//                  if (retcode != null && retcode.equals("success")) {
//                    JsonObject data = jsonObject.get("data").getAsJsonObject();
//                    WatchPic mWatchPic = new Gson().fromJson(data, WatchPic.class);
//                    Intent intent = new Intent(activity, VideoActivity.class);
//                    intent.putExtra("video", mWatchPic.url);
//                    intent.putExtra("title", mWatchPic.title);
//                    intent.putExtra("isHideSkip", true);
//                    activity.startActivity(intent);
//                  }
//                }, throwable -> {
//                  Logger.e(throwable, throwable.toString());
//                });
//            new CompositeDisposable().add(disposable);
//          } else {
//            AppPicUtil
//                .gotoPlayChallenge(bookManager.getChallenges().get(3).url, bookManager, activity);
//          }
//        }
//        break;
//      default:
//        break;
//    }

  }

  public static void gotoLoading(boolean isShow, Context context) {
//    ARouter.getInstance().build("/namiboxpic/loading")
//        .withBoolean("isShowWifiDialog", isShow)
//        .navigation();

    Intent intent = new Intent(context, PicLoadingActivity.class);
    intent.putExtra("isShowWifiDialog", isShow);
    context.startActivity(intent);
  }

  public static void gotoWordChallenge(Context context) {
//    ARouter.getInstance().build("/namiboxpic/challenge")
//        .withString("exercise_type", CHALLENGE_WORD)
//        .navigation();

    Intent intent = new Intent(context, ExerciseChallengeActivity.class);
    intent.putExtra("exercise_type", CHALLENGE_WORD);
    context.startActivity(intent);
  }

  public static void gotoReadChallenge(Context context) {
//    ARouter.getInstance().build("/namiboxpic/challenge")
//        .withString("exercise_type", CHALLENGE_READ)
//        .navigation();
    Intent intent = new Intent(context, ExerciseChallengeActivity.class);
    intent.putExtra("exercise_type", CHALLENGE_READ);
    context.startActivity(intent);
  }

  public static void gotoPlayChallenge(String url, BookManager bookManager,
      AbsFoundationActivity activity) {
    String ossHost;
    String host;
    String workHost;
    if (activity.getPackageName().equals("com.jinxin.appstudent")) {
      host = AppPicUtil.getPicBaseUrl(activity);
      ossHost = AppPicUtil.getPicOssUrl(activity);
      workHost = AppPicUtil.getPicWorkBaseUrl(activity);
    } else {
      host = Utils.getBaseHttpsUrl(activity);
      ossHost = Utils.getBaseHttpsUrl(activity);
      workHost = Utils.getBaseHttpsUrl(activity);
    }
    if (!Utils.checkIsX86() && Utils.checkSupportV7a()) {
//      ARouter.getInstance().build("/hfx/dubVideops")
//          .withString("title", ((ProductItem.BookLearning) bookManager.getBookLearning()).text)
//          .withString("product_name", bookManager.getProductName())
//          .withString("json_url", url)
//          .withString("userid", Utils.getLoginUserId(activity))
//          .withString("ossTokenUrl", ossHost + "/api/get_oss_token")
//          .withString("reportUrl", host + "/api/report_fundubbing_progress")
//          .withString("match_id", bookManager.getMatch_id())
//          .withLong("milesson_id", bookManager.getMilesson_id())
//          .withLong("book_id", bookManager.getMilesson_item_id())
//          .withLong("homework_id", bookManager.getHomeworkId())
//          .withString("worksubmitUrl", workHost + "/homework/submit/pbook/work/")
//          .navigation();

      Intent intent = new Intent(activity, DubVideoNewActivity.class);
      intent.putExtra("title", ((ProductItem.BookLearning) bookManager.getBookLearning()).text);
      intent.putExtra("product_name", bookManager.getProductName());
      intent.putExtra("json_url", url);
      intent.putExtra("userid", Utils.getLoginUserId(activity));
      intent.putExtra("ossTokenUrl", ossHost + "/api/get_oss_token");
      intent.putExtra("reportUrl", host + "/api/report_fundubbing_progress");
      intent.putExtra("match_id", bookManager.getMatch_id());
      intent.putExtra("milesson_id", bookManager.getMilesson_id());
      intent.putExtra("book_id", bookManager.getMilesson_item_id());
      intent.putExtra("homework_id", bookManager.getHomeworkId());
      intent.putExtra("worksubmitUrl", workHost + "/homework/submit/pbook/work/");
      activity.startActivity(intent);
    } else {
      activity.showErrorDialog("检测到您的设备过于陈旧，不支持本功能", false);
    }
  }

  public static void gotoPicChallenge(ProductItem.Challenge challenge,
      AbsFoundationActivity activity, BookManager bookManager) {
    saveExtInfo(challenge, activity, bookManager.getMatch_id());
    Intent intent = new Intent();
    intent.setClass(activity, EvalActivity.class);
    intent.putExtra(EvalActivity.ARG_RECORD_URL, challenge.url);
    intent.putExtra(EvalActivity.ARG_BOOK_ID, challenge.command_map.book_id);
    intent.putExtra(EvalActivity.ARG_CONTENT_TYPE, challenge.command_map.content_type);
    intent.putExtra("homework_id", bookManager.getHomeworkId());
    intent.putExtra("match_workId", bookManager.getMatch_id());
    intent.putExtra("worksubmitUrl",
        AppPicUtil.getPicWorkBaseUrl(activity) + "/homework/submit/pbook/work/");
    if (!TextUtils.isEmpty(challenge.command_map.matchid)) {
      intent.putExtra("match_id", challenge.command_map.matchid);
    }
    activity.startActivity(intent);
  }

  private static void saveExtInfo(ProductItem.Challenge challenge, AbsFoundationActivity activity,
      String match_id) {
    try {
      HfxUtil.saveMatchInfo(activity, challenge.command_map.book_id, challenge.command_map.matchid,
          challenge.command_map.matchname, challenge.command_map.submiturl);
    } catch (IOException e) {
      activity.toast("保存活动信息失败,将作为普通作品提交");
      e.printStackTrace();
    }
    try {
      HfxUtil.saveClassInfo(activity, challenge.command_map.book_id,
          challenge.command_map.transmissionparam, 0);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      HfxUtil.saveDirectUploadInfo(activity, challenge.command_map.book_id, false);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      HfxUtil.saveExtraInfo(activity, challenge.command_map.book_id, challenge.command_map.extra);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getPicBaseUrl(Context context) {
    String env;
    if (Utils.isDev(context)) {
      env = "mcloudw.namibox.com/picbook";
    } else {
      env = "mcloud.namibox.com/picbook";
    }
    return Utils.format("https://%s", env);
  }

  public static String getPicWorkBaseUrl(Context context) {
    String env;
    if (Utils.isDev(context)) {
      env = "mcloudw.namibox.com";
    } else {
      env = "mcloud.namibox.com";
    }
    return Utils.format("https://%s", env);
  }


  public static void gotoWorkResult(String json, long homework_id) {
    EventBus.getDefault().post(new WorkEvent(homework_id, "done"));
//    ARouter.getInstance().build("/stu/work_result")
//        .withString("result", json)
//        .withLong("homework_id", homework_id)
//        .withBoolean("isHiddenShare", true)
//        .navigation();
  }

  public static String getPicOssUrl(Context context) {
    String env;
    if (Utils.isDev(context)) {
      env = "wpbook.namibox.com";
    } else {
      env = "pbook.namibox.com";
    }
    return Utils.format("https://%s", env);
  }

  private static final int MIN_CLICK_DELAY_TIME = 1000;
  private static long lastClickTime;

  public static boolean isFastClick() {
    boolean flag = false;
    long curClickTime = System.currentTimeMillis();
    if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
      flag = true;
    }
    lastClickTime = curClickTime;
    return flag;
  }

  public static String formatTime(long length) {
    Date date = new Date(length);
    //时间格式化工具
    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    return sdf.format(date);
  }
}
