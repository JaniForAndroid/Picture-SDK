package com.namibox.util;

import static android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT;
import static android.telephony.TelephonyManager.NETWORK_TYPE_CDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_IDEN;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;
import static com.example.picsdk.util.PicturePreferenceUtil.USER_INFORMATION;
import static com.namibox.util.NetworkUtil.NETWORKTYPE_INVALID;
import static com.namibox.util.NetworkUtil.NETWORKTYPE_WAP;
import static com.namibox.util.NetworkUtil.NETWORKTYPE_WIFI;
import static com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout.TAG;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import sdk.NBPictureSDK;
import com.example.picsdk.BuildConfig;
import com.example.picsdk.PicGuideActivity;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.model.MyStore;
import com.example.picsdk.model.PictureBook;
import com.example.picsdk.model.ProductItem;
import com.example.picsdk.util.PicturePreferenceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.constant.Events;
import com.namibox.commonlib.event.LoginStatusEvent;
import com.namibox.tools.ThinkingAnalyticsHelper;
import com.namibox.util.network.NetWorkHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import okhttp3.CacheControl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.greenrobot.eventbus.EventBus;

/**
 * author : feng
 * description ： app相关工具类
 * creation time : 18-7-16下午2:06
 */
public class AppUtil {

  private static final String BOOKS_DIR = "books_dir";

  private static Application sApplication;
  private static String baseUrl;

  public static void init(Application application) {
    if (sApplication == null) {
      sApplication = application;
    }
  }

  public static Application getApp() {
    if (sApplication != null) {
      return sApplication;
    }
    throw new NullPointerException("should init first");
  }

  public static void setBaseUrl(String baseUrl) {
    AppUtil.baseUrl = baseUrl;
  }

  public static String getBaseUrl() {
    return baseUrl;
  }

  public static String getOssBaseUrl(Context context) {
    if (Utils.isDev(context)) {
      return "https://wu.namibox.com/";
    } else {
      return "https://u.namibox.com/";
    }
  }

  public static File getBookResource(String url, long milesson_item_id) {
    return new File(getBookCacheDir(milesson_item_id), MD5Util.md5(url));
  }

  private static File getBookCacheDir(long milesson_item_id) {
    File booksDir = new File(sApplication.getFilesDir(), BOOKS_DIR);
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

  public static boolean isMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }

  public static boolean isAppForeground() {
    ActivityManager am = (ActivityManager) getApp().getSystemService(Context.ACTIVITY_SERVICE);
    if (am == null) {
      return false;
    }
    List<RunningAppProcessInfo> info = am.getRunningAppProcesses();
    if (info == null || info.size() == 0) {
      return false;
    }
    for (RunningAppProcessInfo aInfo : info) {
      if (aInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
        return aInfo.processName.equals(getApp().getPackageName());
      }
    }
    return false;
  }

  public static void onLogin(Context context, JsonObject jsonObj) {
    //将用户登录的数据保存至本地 ********注意 会涉及到一系列的 PreferenceUtil 中用户信息相关的数据的取用
    FileUtil.saveDeviceFile(jsonObj.toString(), USER_INFORMATION);
//    PictureStoreApp.isLogin = true;
    String userid = jsonObj.has("userid") ? jsonObj.get("userid").getAsString() : "";
    PreferenceUtil.setLongLoginUserId(context, Long.valueOf(userid));
//    JpushUtil.setAlias(context, userid);
//    GetuiUtil.setAlias(context, userid);
    if (jsonObj.has("im_user")) {
      PreferenceUtil.setSharePref(context, PicturePreferenceUtil.PREF_IM_USAR,
          jsonObj.get("im_user").getAsString());
    }
    if (jsonObj.has("role")) {
      String role = jsonObj.get("role").getAsString();
      PreferenceUtil.setSharePref(context, PicturePreferenceUtil.PREF_USER_ROLE, role);
    }
    //绑定手机号码的依据
    if (jsonObj.has("phonenum")) {
      String phonenum = jsonObj.get("phonenum").getAsString();
      PreferenceUtil.setSharePref(context, PicturePreferenceUtil.PREF_USER_PHONE, phonenum);
      FileUtil.saveDeviceFile(phonenum, "device_PicBook.txt");
      String md5 = MD5Util.md5(phonenum + "namibox");
      FileUtil.saveDeviceFile(md5, ".phonekey.txt");
      PreferenceUtil.setBindPhone(context, !TextUtils.isEmpty(phonenum));
    }
    if (jsonObj.has("changetime")) {
      long expireTime = Utils.parseTimeString(jsonObj.get("changetime").getAsString());
      PreferenceUtil.setSharePref(context, PreferenceUtil.PREF_EXPIRE_TIME, expireTime);
    }
    if (jsonObj.has("sessionid")) {
      PreferenceUtil.setSharePref(context, PreferenceUtil.PREF_SESSION_ID,
          jsonObj.get("sessionid").getAsString(), true);
    }
    if (jsonObj.has("user_auth")) {
      PicturePreferenceUtil.saveUserAuth(context, userid, jsonObj.get("user_auth").getAsString());
    }
    if (jsonObj.has("head_image")) {
      FileUtil.saveDeviceFile(jsonObj.get("head_image").getAsString(), ".head.txt");
      PreferenceUtil.saveHeadImage(context, userid, jsonObj.get("head_image").getAsString());
    }
    if (jsonObj.has("nick_name")) {
      PreferenceUtil.saveNickName(context, userid, jsonObj.get("nick_name").getAsString());
    }
    if (jsonObj.has("regtime")) {
      PreferenceUtil.saveRegTime(context, userid, jsonObj.get("regtime").getAsString());
    }
//        Utils.saveCookie(mActivity, Utils.getBaseUrl(mActivity), "sessionid=" + cmd.sessionid);
//    if (jsonObj.has("attr")) {
//      JsonArray jsonArray = jsonObj.get("attr").getAsJsonArray();
//      for (JsonElement jsonElement : jsonArray) {
//        if ("IM".equals(jsonElement.getAsString())) {
//          IMHelper.getInstance().setAutoLogin(true);
//          //被踢下线不会自动登录，防止踢来踢去
//          if (!IMHelper.getInstance().isForceOffline()) {
//            onLoginIM(context);
//          }
//        }
//        //实名认证
//        if ("CERTIFICATION".equals(jsonElement.getAsString())) {
//          PreferenceUtil.setSharePref(context, PreferenceUtil.PREF_CERTIFICATION, true);
//        }
//      }
//    }
    if (PreferenceUtil.getSharePref(context, PreferenceUtil.PREF_LOGIN_TIME, 0L) == 0) {
      PreferenceUtil
          .setSharePref(context, PreferenceUtil.PREF_LOGIN_TIME, System.currentTimeMillis());
    }
    EventBus.getDefault().post(new LoginStatusEvent(true));
  }

  public static <T> void randomList(List<T> list) {
    int size = list.size();
    Random random = new Random();
    for (int i = 0; i < size; i++) {
      int randomPos = random.nextInt(size);
      Collections.swap(list, i, randomPos);
    }
  }

  public static void regToken(Context context) {
//        String token = JPushInterface.getRegistrationID(context);
    String token = new DeviceUuidFactory(context).getDeviceUuid().toString();
    if (TextUtils.isEmpty(token)) {
      Logger.e("token not ready, can't regToken");
      //JPushInterface.resumePush(context);
      return;
    }
    Logger.i("getRegistrationID:" + token);
    //JPushInterface.stopPush(context);
    if (!NetworkUtil.isNetworkConnected(context)) {
      return;
    }
    String uuid = new DeviceUuidFactory(context).getDeviceUuid().toString();
    String timestamp = Utils.formatCurrentTime();
    String key = "411f9e5f9c2f11767e14d6fa74ddb581";
    List<String> array = new ArrayList<>();
    array.add(token);
    array.add(key);
    array.add(timestamp);
    array.add(uuid);
    Collections.sort(array);
    StringBuilder sb = new StringBuilder();
    for (String s : array) {
      sb.append(s);
    }
    String sign = MD5Util.SHA1(sb.toString());
    String url = AppUtil.getBaseUrl() + "/app/regtoken?token=" + token
        + "&uuid=" + uuid + "&timestamp=" + timestamp + "&sign=" + sign;
    String la = PreferenceUtil.getSharePref(context, PicturePreferenceUtil.PREF_LA, null);
    String lo = PreferenceUtil.getSharePref(context, PicturePreferenceUtil.PREF_LO, null);
    if (!TextUtils.isEmpty(la) && !TextUtils.isEmpty(lo)) {
      url += "&location={" + la + "," + lo + "}";
    }
    Logger.w("regtoken: " + url);
    ApiHandler.getBaseApi(context)
        .commonRequest(url).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DefaultObserver<ResponseBody>() {
          @Override
          public void onNext(ResponseBody response) {
            try {
              String result = response.string();
              Logger.w("regtoken result:" + result);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onError(Throwable e) {
            e.printStackTrace();
          }

          @Override
          public void onComplete() {

          }
        });
  }

  public static long getCacheSize(Context context) {
    long size = FileUtil.getDirSize(context.getCacheDir());
//        long extSize = FileUtil.getDirSize(context.getExternalCacheDir());
    long fileSize = FileUtil.getDirSize(FileUtil.getFileCacheDir(context));
    return size + /*extSize +*/ fileSize;
  }

  public static void deleteCache(Context context) throws IOException {
    FileUtil.cleanDirectory(context.getCacheDir());
    FileUtil.cleanDirectory(context.getExternalCacheDir());
    FileUtil.cleanDirectory(FileUtil.getFileCacheDir(context));
  }

  public static String getMD5Str(String sourceStr) {
    byte[] source = sourceStr.getBytes();
    // 用来将字节转换成 16 进制表示的字符
    char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
        'd', 'e', 'f'};
    java.security.MessageDigest md = null;
    try {
      md = java.security.MessageDigest.getInstance("MD5");
    } catch (Throwable e) {
      Log.d(TAG, e.toString());
    }

    if (md == null) {
      return null;
    }

    md.update(source);
    byte[] tmp = md.digest(); // MD5 的计算结果是一个 128 位的长整数，
    // 用字节表示就是 16 个字节
    char[] str = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
    // 所以表示成 16 进制需要 32 个字符
    int k = 0; // 表示转换结果中对应的字符位置

    // 从第一个字节开始，对 MD5 的每一个字节, 转换成 16 进制字符的转换
    for (int i = 0; i < 16; i++) {
      byte byte0 = tmp[i]; // 取第 i 个字节
      str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
      // >>> 为逻辑右移，将符号位一起右移
      str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
    }

    return new String(str); // 换后的结果转换为字符串
  }

  public interface DownloadCallback {

    void onStarted();

    void onCanceled();

    void onProgress(long current, long total);

    void onFinished(boolean success);
  }

  public static AsyncTask download(Context c, String url, String filePath,
      DownloadCallback callback) {
    Logger.i("download: " + url);
    return new DownloadTask(c, callback)
        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, filePath);
  }

  private static class DownloadTask extends WeakAsyncTask<String, Long, Boolean, Context> {

    DownloadCallback callback;

    DownloadTask(Context context, DownloadCallback callback) {
      super(context);
      this.callback = callback;
    }

    @Override
    protected void onPreExecute(Context context) {
      if (callback != null) {
        callback.onStarted();
      }
    }

    @Override
    protected Boolean doInBackground(Context context, String... params) {
      String url = params[0];
      String filePath = params[1];
      File file = new File(filePath);
      File tmpFile = new File(file.getAbsolutePath() + "_tmp");
      long startPos = tmpFile.exists() ? tmpFile.length() : 0;
      try {
        Request request = new Request.Builder()
            .cacheControl(CacheControl.FORCE_NETWORK)
            .url(Utils.encodeString(url))
            .header("RANGE", "bytes=" + startPos + "-")
            .build();
        Response response = NetWorkHelper.getOkHttpClient()
            .newCall(request)
            .execute();
        if (response.isSuccessful()) {
          RandomAccessFile randomAccessFile = new RandomAccessFile(tmpFile, "rw");
          randomAccessFile.seek(startPos);
          InputStream is = response.body().byteStream();
          byte[] buffer = new byte[10 * 1024];
          int count;
          long total = startPos + response.body().contentLength();
          long current = startPos;
          while (!isCancelled() && (count = is.read(buffer)) > 0) {
            randomAccessFile.write(buffer, 0, count);
            current += count;
            publishProgress(current, total);
          }
          randomAccessFile.close();
          if (file.exists()) {
            file.delete();
          }
          if (!isCancelled()) {
            tmpFile.renameTo(file);
          }
          is.close();
          return true;
        }
        if (response.body() != null) {
          response.body().close();
        }
      } catch (Exception e) {
        if (file.exists()) {
          file.delete();
        }
        e.printStackTrace();
      }
      return false;
    }

    @Override
    protected void onProgressUpdate(Context context, Long... values) {
      if (callback != null) {
        callback.onProgress(values[0], values[1]);
      }
    }

    @Override
    protected void onCancelled(Context context, Boolean aBoolean) {
      if (callback != null) {
        callback.onCanceled();
      }
    }

    @Override
    protected void onPostExecute(Context context, Boolean result) {
      if (callback != null) {
        callback.onFinished(result);
      }
    }
  }

  public static void TagEventClickPush(JsonObject jsonObject) {
//    if (jsonObject == null) {
//      return;
//    }
//    if (jsonObject.has("tga_event")) {
//      TGAEvent tga_event = new Gson().fromJson(jsonObject.get("tga_event"), TGAEvent.class);
//
//      HashMap<String, String> map = new HashMap<>();
//      map.put("page", tga_event.properties.page);
//      map.put("button", tga_event.properties.button);
//      map.put("product_name", tga_event.properties.product_name);
//      map.put("product_subtitle", tga_event.properties.product_subtitle);
//      map.put("title", tga_event.properties.title);
//
//      if (tga_event.event_name.equals(Events.TA_EVENT_NB_APP_CLICK)) {
//        ThinkingAnalyticsHelper.trackEvent(Events.TA_EVENT_NB_APP_CLICK, map);
//      }
//    }
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

  public static String getMZUrl() {
    String url;
    if (NBPictureSDK.isDebug) {
      url = "https://wpbook.namibox.com/app/responsible/";
    } else {
      url = "https://pbook.namibox.com/app/responsible/";
    }
    return url;
  }

  public static String getYHUrl() {
    String url;
    if (NBPictureSDK.isDebug) {
      url = "https://wpbook.namibox.com/app/protocol/";
    } else {
      url = "https://pbook.namibox.com/app/protocol/";
    }
    return url;
  }

  public static String getUserUrl() {
    String url;
    if (NBPictureSDK.isDebug) {
      url = "https://wpbook.namibox.com/app/license";
    } else {
      url = "https://pbook.namibox.com/app/license";
    }
    return url;
  }

  public static String getprivacyUrl() {
    String url;
    if (NBPictureSDK.isDebug) {
      url = "https://wpbook.namibox.com/app/privacy";
    } else {
      url = "https://pbook.namibox.com/app/privacy";
    }
    return url;
  }

  public static String getNetWorkType(Context context) {
    String mNetWorkType = null;
    ConnectivityManager manager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    if (manager == null) {
      return "ConnectivityManager not found";
    }
    NetworkInfo networkInfo = manager.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      String type = networkInfo.getTypeName();
      if (type.equalsIgnoreCase("WIFI")) {
        mNetWorkType = NETWORKTYPE_WIFI;
      } else if (type.equalsIgnoreCase("MOBILE")) {
        String proxyHost = android.net.Proxy.getDefaultHost();
        if (TextUtils.isEmpty(proxyHost)) {
          mNetWorkType = mobileNetworkType(context);
        } else {
          mNetWorkType = NETWORKTYPE_WAP;
        }
      }
    } else {
      mNetWorkType = NETWORKTYPE_INVALID;
    }
    return mNetWorkType;
  }

  private static String mobileNetworkType(Context context) {
    TelephonyManager telephonyManager = (TelephonyManager) context
        .getSystemService(Context.TELEPHONY_SERVICE);
    if (telephonyManager == null) {
      return "TM==null";
    }
    switch (telephonyManager.getNetworkType()) {
      case NETWORK_TYPE_1xRTT:// ~ 50-100 kbps
        return "2G";
      case NETWORK_TYPE_CDMA:// ~ 14-64 kbps
        return "2G";
      case NETWORK_TYPE_EDGE:// ~ 50-100 kbps
        return "2G";
      case NETWORK_TYPE_EVDO_0:// ~ 400-1000 kbps
        return "3G";
      case NETWORK_TYPE_EVDO_A:// ~ 600-1400 kbps
        return "3G";
      case NETWORK_TYPE_GPRS:// ~ 100 kbps
        return "2G";
      case NETWORK_TYPE_HSDPA:// ~ 2-14 Mbps
        return "3G";
      case NETWORK_TYPE_HSPA:// ~ 700-1700 kbps
        return "3G";
      case NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
        return "3G";
      case NETWORK_TYPE_UMTS:// ~ 400-7000 kbps
        return "3G";
      case NETWORK_TYPE_EHRPD:// ~ 1-2 Mbps
        return "3G";
      case NETWORK_TYPE_EVDO_B: // ~ 5 Mbps
        return "3G";
      case NETWORK_TYPE_HSPAP:// ~ 10-20 Mbps
        return "3G";
      case NETWORK_TYPE_IDEN:// ~25 kbps
        return "2G";
      case NETWORK_TYPE_LTE:// ~ 10+ Mbps
        return "4G";
      case TelephonyManager.NETWORK_TYPE_UNKNOWN:
        return "UNKNOWN";
      default:
        return "4G";
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

  public static void saveLoaclReading(BookManager bookManager, Context context) {
    if (bookManager != null && bookManager.getBookLearning() != null) {
      int star = PreferenceUtil.getSharePref(context,
          bookManager.getMilesson_item_id() + PicGuideActivity.CHALLENGE_WORD + "star", 0) +
          PreferenceUtil.getSharePref(context,
              bookManager.getMilesson_item_id() + PicGuideActivity.CHALLENGE_READ + "star", 0) +
          PreferenceUtil.getSharePref(context,
              bookManager.getMilesson_item_id() + PicGuideActivity.CHALLENGE_PLAY + "star", 0);

      String storeStr = PreferenceUtil.getSharePref(context, "local_reading", "");
      if (storeStr.equals("")) {
        MyStore myStore = new MyStore();
        MyStore.Data data = new MyStore.Data();
        MyStore.DataList dataList = new MyStore.DataList();
        dataList.category = "myreading";
        JsonObject jsonObject = new JsonObject();
        JsonObject action = new JsonObject();
        String url = AppUtil.getBaseUrl() + "/api/guide/" + bookManager.getMilesson_item_id();
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
          action.addProperty("url", AppUtil.getBaseUrl() + "/api/guide/" + readings.get(i).id);
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
          String url = AppUtil.getBaseUrl() + "/api/guide/" + bookManager.getMilesson_item_id();
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

  public static boolean isForeground(Activity activity) {
    return isForeground(activity, activity.getClass().getName());
  }

  public static void restartAPP(Context context) {
    Intent LaunchIntent = context.getPackageManager()
        .getLaunchIntentForPackage(context.getPackageName());
    LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    context.startActivity(LaunchIntent);

    android.os.Process.killProcess(android.os.Process.myPid());
  }
}
