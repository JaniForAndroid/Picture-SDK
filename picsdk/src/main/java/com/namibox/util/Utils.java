package com.namibox.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityOptions;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import com.example.picsdk.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.zhy.base.fileprovider.FileProvider7;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import okhttp3.HttpUrl;
import org.joor.Reflect;

/**
 * Created by sunha on 2017/5/31 0031.
 */

public class Utils {

  public final static int MINTIMEMS = 10000;
  public final static int MAXTIME = 300;
  private static final String TAG = "Utils";
  private static StringBuilder sb = new StringBuilder();
  private static Formatter mFormatter = new Formatter(sb, Locale.getDefault());
  private static StringBuilder sFormatBuilder = new StringBuilder();
  private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());

  public static String getSalt() {
    return "668b82885319d75f052da766d03f22f7";
  }

  public static String getSalt2() {
    return "bba1302d7feb996b358dff38ab77ae7a";
  }

  public static String getSalt3() {
    return "34e6004164ae47e4311fbfae8d077aaa";
  }

  //==================DeviceUtil=====================

  public static int getVersionCode(Context context) {
    try {
      PackageManager manager = context.getPackageManager();
      PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
      return info.versionCode;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 100;
  }

  public static String getVersionName(Context context) {
    try {
      PackageManager manager = context.getPackageManager();
      PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
      return info.versionName;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "3.0";
  }

  public static synchronized String getAppName(Context context) {
    try {
      PackageManager packageManager = context.getPackageManager();
      PackageInfo packageInfo = packageManager.getPackageInfo(
          context.getPackageName(), 0);
      int labelRes = packageInfo.applicationInfo.labelRes;
      return context.getResources().getString(labelRes);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static boolean checkMobile(String mobile) {
    String regex = "(\\+\\d+)?1[34578]\\d{9}$";
    return Pattern.matches(regex, mobile);
  }

  public static boolean checkEmail(String email) {
    String regex = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
    return Pattern.matches(regex, email);
  }

  public static String formatAccount(String account) {
    String text;
    if (Utils.checkMobile(account)) {
      text = Utils.format("%s****%s", account.substring(0, 3), account.substring(7, 11));
    } else if (Utils.checkEmail(account)) {
      String[] split = account.split("@");
      if (split[0].length() < 3) {
        text = split[0];
      } else {
        text = split[0].substring(0, 3) + "***";
      }
//      text += split[1];
    } else {
      text = account;
    }
    return text;
  }

  //==================ScreenUtil=====================

  public static int dp2px(Context var0, float var1) {
    float var2 = var0.getResources().getDisplayMetrics().density;
    return (int) (var1 * var2 + 0.5F);
  }

  public static int px2dip(Context var0, float var1) {
    float var2 = var0.getResources().getDisplayMetrics().density;
    return (int) (var1 / var2 + 0.5F);
  }

  public static int sp2px(Context var0, float var1) {
    float var2 = var0.getResources().getDisplayMetrics().scaledDensity;
    return (int) (var1 * var2 + 0.5F);
  }

  public static int px2sp(Context var0, float var1) {
    float var2 = var0.getResources().getDisplayMetrics().scaledDensity;
    return (int) (var1 / var2 + 0.5F);
  }

  /***
   * 判断是平板 还是手机
   * @param context 上下文
   * @return true 平板 false 手机
   */
  public static boolean isTablet(Context context) {
    return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >=
        Configuration.SCREENLAYOUT_SIZE_LARGE;
  }

  /**
   * 获得屏幕宽高度
   */
  public static int[] getScreenWidth(Context context) {
    WindowManager wm = (WindowManager) context
        .getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics outMetrics = new DisplayMetrics();
    wm.getDefaultDisplay().getMetrics(outMetrics);
    return new int[]{outMetrics.widthPixels, outMetrics.heightPixels};
  }

  public static int getScreenHeightDp(Context context) {
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics dm = new DisplayMetrics();
    wm.getDefaultDisplay().getRealMetrics(dm);
    return (int) (dm.heightPixels / dm.density);
  }

  /**
   * 获得状态栏的高度
   */
  public static int getStatusHeight(Context context) {

    int statusHeight = -1;
    try {
      Class<?> clazz = Class.forName("com.android.internal.R$dimen.xml");
      Object object = clazz.newInstance();
      int height = Integer.parseInt(clazz.getField("status_bar_height")
          .get(object).toString());
      statusHeight = context.getResources().getDimensionPixelSize(height);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return statusHeight;
  }

  /**
   * 获取虚拟功能键高度
   */
  public static int getVirtualBarHeigh(Context context) {
    int vh = 0;
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    DisplayMetrics dm = new DisplayMetrics();
    try {
      @SuppressWarnings("rawtypes")
      Class c = Class.forName("android.view.Display");
      @SuppressWarnings("unchecked")
      Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
      method.invoke(display, dm);
      vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return vh;
  }

  //==================StringUtil=====================

  public static String encodeString(String text) {
    if (TextUtils.isEmpty(text)) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == ' ') {
        result.append("%20");
      } else if (c > 0 && c <= 126) {
        result.append(c);
      } else {
        byte[] b = new byte[0];
        try {
          b = Character.toString(c).getBytes("UTF-8");
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        for (byte aB : b) {
          int k = aB;
          if (k < 0) {
            k += 256;
          }
          result.append('%').append(Integer.toHexString(k).toUpperCase());
        }
      }
    }
    return result.toString();
  }


  public static String formatCount(Context c, int number) {
    if (number < 10000) {
      return String.valueOf(number);
    } else {
      return c.getString(R.string.number_format, number / 10000f);
    }
  }

  public static String formatLengthString(long length) {
    float unit = 1024f;
    if (length < unit) {
      return length + "B";
    } else if (length < unit * unit) {
      DecimalFormat decimalFormat = new DecimalFormat(".00");
      float n = length / unit;
      return decimalFormat.format(n) + "K";
    } else if (length < unit * unit * unit) {
      DecimalFormat decimalFormat = new DecimalFormat(".00");
      float n = length / (unit * unit);
      return decimalFormat.format(n) + "M";
    } else {
      DecimalFormat decimalFormat = new DecimalFormat(".00");
      float n = length / (unit * unit * unit);
      return decimalFormat.format(n) + "G";
    }
  }

  /**
   * 字符串形式的空间大小转化为M为单位的int值
   */
  public static int formatLengthM(String length) {
    float unit = 1024f;
    String countString = length.substring(0, length.length() - 1);
    if (length.contains("G")) {
      return (int) (Float.valueOf(countString) * unit);
    } else if (length.contains("M")) {
      return (int) Float.valueOf(countString).floatValue();
    } else if (length.contains("K")) {
      return (int) (Float.valueOf(countString) / unit);
    } else {
      return (int) (Float.valueOf(countString) / unit / unit);
    }
  }

  public static String formatFloatString(float num) {
    DecimalFormat decimalFormat = new DecimalFormat("######0.00");
    return decimalFormat.format(num);
  }

  //==================TimeUtil=====================

  public static String timeFormat(long timeMillis, String pattern) {
    SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
    return format.format(new Date(timeMillis));
  }


  public static String makeTimeString(int hour, int minute) {
    sFormatBuilder.setLength(0);
    return sFormatter.format("%1$02d:%2$02d", hour, minute).toString();
  }


  public static String makeSmallTimeString(int ms) {
    sFormatBuilder.setLength(0);
    int arg1 = ms / 1000;
    int arg2 = (ms % 1000) / 100;
    return sFormatter.format("%1$02d:%2$d", arg1, arg2).toString();
  }

  public static String format(String pattern, Object... args) {
    return String.format(Locale.US, pattern, args);
  }


  static public String timeStringForTimeMs(long timeMs) {
    long totalSeconds = timeMs / 1000;
    long seconds = totalSeconds % 60;
    long minutes = (totalSeconds / 60) % 60;
    long hours = totalSeconds / 3600;
    long ms = timeMs % 1000;
    long tenthSecond = ms / 100;
    sb.setLength(0);
    if (hours > 0) {
      return mFormatter.format("%d:%02d:%02d.%01d", hours, minutes, seconds, tenthSecond)
          .toString();
    } else {
      return mFormatter.format("%02d:%02d.%01d", minutes, seconds, tenthSecond).toString();
    }
  }

  static public String secStringForTimeMs(long timeMs) {
    long totalSeconds = timeMs / 1000;
    long seconds = totalSeconds % 60;
    long minutes = (totalSeconds / 60) % 60;
    long hours = totalSeconds / 3600;
    long ms = timeMs % 1000;
    long tenthSecond = ms / 100;
    sb.setLength(0);
    return mFormatter.format("%d.%01d", totalSeconds, tenthSecond).toString();

  }

  static public String stringForTimeSec(int timeMs, boolean isHanziFormat) {
    int totalSeconds = timeMs / 1000;
    int seconds = totalSeconds % 60;
    int minutes = (totalSeconds / 60) % 60;
    int hours = totalSeconds / 3600;
//        int ms = timeMs % 1000;
//        int tenthSecond = ms / 100;
    sb.setLength(0);
    if (hours > 0) {
      if (isHanziFormat) {
        return mFormatter.format("%d时%02d分%02d秒", hours, minutes, seconds).toString();
      } else {
        return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
      }
    } else {
      if (isHanziFormat) {
        return mFormatter.format("%02d分%02d秒", minutes, seconds).toString();
      } else {
        return mFormatter.format("%02d:%02d", minutes, seconds).toString();
      }
    }
  }

  public static String formatCountDownTime(long timeMs) {
    long totalSeconds = timeMs / 1000;
    long seconds = totalSeconds % 60;
    String secondString = seconds < 10 ? "0" + seconds : String.valueOf(seconds);
    long minutes = (totalSeconds / 60) % 60;
    String minuteString = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
    long hours = totalSeconds / 3600;
    String hourString = hours < 10 ? "0" + hours : String.valueOf(hours);
    return format("%s:%s:%s", hourString, minuteString, secondString);
  }

  public static String formatCurrentTime() {
    return formatCurrentTime("yyyyMMddHHmmss");
  }

  public static String formatCurrentTime(String pattern) {
    SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
    return format.format(new Date());
  }

  public static String formatTimeString(String pattern, long time) {
    SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
    return format.format(new Date(time));
  }

  public static Long getIntervalFromZeroTime() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.MILLISECOND, 0);
    Calendar curCal = Calendar.getInstance();
    long calInMillis = cal.getTimeInMillis();
    return curCal.getTimeInMillis() - calInMillis;
  }


  public static long parseTimeMinString(String time) {
    return parseTimeString("yyyyMMddHHmm", time);
  }

  public static long parseTimeString(String time) {
    return parseTimeString("yyyyMMddHHmmss", time);
  }

  public static long parseTimeString2(String time) {
    return parseTimeString("yyyy-MM-dd HH:mm:ss", time);
  }

  public static long parseTimeString(String pattern, String time) {
    try {
      if (!TextUtils.isEmpty(time)) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        Date date = sdf.parse(time);
        return date.getTime();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  public static String makeTimeString(int ms) {
    StringBuilder sFormatBuilder = new StringBuilder();
    Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    sFormatBuilder.setLength(0);
    int secs = ms / 1000;
    if (secs < 3600) {
      int arg1 = secs / 60;
      int arg2 = secs % 60;
      return sFormatter.format("%1$d:%2$02d", arg1, arg2).toString();
    } else {
      int arg1 = secs / 3600;
      int arg2 = (secs / 60) % 60;
      int arg3 = secs % 60;
      return sFormatter.format("%1$d:%2$02d:%3$02d", arg1, arg2, arg3).toString();
    }
  }

  //==================JsonUtil=====================

  public static <T> T parseJsonFile(File f, Class<T> clazz) {
    try {
      InputStreamReader fr = new InputStreamReader(new FileInputStream(f), "utf-8");
      Gson gson = new Gson();
      return gson.fromJson(fr, clazz);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static <T> T parseJsonString(String s, Class<T> clazz) {
    if (TextUtils.isEmpty(s)) {
      return null;
    }
    try {
      Gson gson = new Gson();
      return gson.fromJson(s, clazz);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static <T> Map<String, T> parseJsonFile(File f) {
    try {
      InputStreamReader fr = new InputStreamReader(new FileInputStream(f), "utf-8");
      Gson gson = new Gson();
      return gson.fromJson(fr,
          new TypeToken<Map<String, T>>() {
          }.getType());
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static JsonObject loadAssetsJson(Context context, String fileName) throws Exception {
    InputStream inputStream = context.getAssets().open(fileName);
    int size = inputStream.available();
    byte[] buffer = new byte[size];
    inputStream.read(buffer);
    inputStream.close();
    String json = new String(buffer, "UTF-8");
    JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
    return jsonObject;
  }

  //==================ActionBarUtil=====================

  public static int getActionBarHeight(Context c) {
    TypedValue t = new TypedValue();
    c.getTheme().resolveAttribute(android.R.attr.actionBarSize, t, true);
    return TypedValue.complexToDimensionPixelSize(t.data, c.getResources().getDisplayMetrics());
  }

//  public static Drawable getDrawableTint(Drawable drawable, int color) {
//    drawable = DrawableCompat.wrap(drawable.mutate());
//    DrawableCompat.setTint(drawable, color);
//    //DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
//    return drawable;
//  }

//  public static Drawable getDrawableTint(Drawable drawable, ColorStateList colors) {
//    final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
//    DrawableCompat.setTintList(wrappedDrawable, colors);
//    return wrappedDrawable;
//  }

  public static int getNavigationBarHeight(Context context) {
    Resources resources = context.getResources();
    int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
    if (resourceId > 0) {
      return resources.getDimensionPixelSize(resourceId);
    }
    return 0;
  }

  public static int getStatusBarHeight(Context context) {
    int result = 0;
    try {
      Class<?> clazz = null;
      clazz = Class.forName("com.android.internal.R$dimen");
      Object object = clazz.newInstance();
      int id = Integer.parseInt(clazz.getField("status_bar_height")
          .get(object).toString());
      result = context.getResources().getDimensionPixelSize(id);
    } catch (Exception e) {
      e.printStackTrace();
    }
//    int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
//    if (resourceId > 0) {
//      result = context.getResources().getDimensionPixelSize(resourceId);
//    }
    if (result == 0) {
      result = dp2px(context, 25);
    }
    return result;
  }

  //==================CameraUtil=====================

  /**
   * 通过传入的宽高算出最接近于宽高值的相机大小
   */
  public static Size calBestPreviewSize(Camera.Parameters camPara,
      final int width, final int height) {
    List<Size> allSupportedSize = camPara.getSupportedPreviewSizes();
    ArrayList<Size> widthLargerSize = new ArrayList<>();
    for (Size tmpSize : allSupportedSize) {
      Log.w("ceshi", "tmpSize.width===" + tmpSize.width
          + ", tmpSize.height===" + tmpSize.height);
      if (tmpSize.width > tmpSize.height) {
        widthLargerSize.add(tmpSize);
      }
    }

    Collections.sort(widthLargerSize, new Comparator<Size>() {
      @Override
      public int compare(Size lhs, Size rhs) {
        int off_one = Math.abs(lhs.width * lhs.height - width * height);
        int off_two = Math.abs(rhs.width * rhs.height - width * height);
        return off_one - off_two;
      }
    });

    return widthLargerSize.get(0);
  }


  public static String getProperty(String key, String defaultValue) {
    String value = defaultValue;
    try {
      Class<?> c = Class.forName("android.os.SystemProperties");
      Method get = c.getMethod("get", String.class, String.class);
      value = (String) (get.invoke(c, key, defaultValue));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return value;
  }

  public static boolean isGIONEE() {
    return Build.MANUFACTURER.equals("GIONEE") || Build.BRAND.equals("GIONEE");
  }

  public static boolean isMIUI() {
    //return Build.MANUFACTURER.equals("Xiaomi");
    try {
      return !TextUtils.isEmpty(getProperty("ro.miui.ui.version.name", null))
          || !TextUtils.isEmpty(getProperty("ro.miui.ui.version.code", null))
          || !TextUtils.isEmpty(getProperty("ro.miui.internal.storage", null));
    } catch (Exception e) {
      return false;
    }
  }

  public static boolean isEMUI() {
    //return Build.MANUFACTURER.equals("HUAWEI");
    try {
      return !TextUtils.isEmpty(getProperty("ro.build.version.emui", null))
          || !TextUtils.isEmpty(getProperty("ro.confg.hw_systemversion", null));
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * @return 只要返回不是""，则是EMUI版本
   */
  public static String getEmuiVersion() {
    String emuiVerion = "";
    Class<?>[] clsArray = new Class<?>[]{String.class};
    Object[] objArray = new Object[]{"ro.build.version.emui"};
    try {
      Class<?> SystemPropertiesClass = Class.forName("android.os.SystemProperties");
      Method get = SystemPropertiesClass.getDeclaredMethod("get", clsArray);
      String version = (String) get.invoke(SystemPropertiesClass, objArray);
      Log.d(TAG, "get EMUI version is:" + version);
      if (!TextUtils.isEmpty(version)) {
        return version;
      }
    } catch (ClassNotFoundException e) {
      Log.e(TAG, " getEmuiVersion wrong, ClassNotFoundException");
    } catch (LinkageError e) {
      Log.e(TAG, " getEmuiVersion wrong, LinkageError");
    } catch (NoSuchMethodException e) {
      Log.e(TAG, " getEmuiVersion wrong, NoSuchMethodException");
    } catch (NullPointerException e) {
      Log.e(TAG, " getEmuiVersion wrong, NullPointerException");
    } catch (Exception e) {
      Log.e(TAG, " getEmuiVersion wrong");
    }
    return emuiVerion;
  }

  public static int getThemeColor(Context context, int defaultColorRes) {
    String theme = PreferenceUtil.getSharePref(context, PreferenceUtil.PREF_THEME_COLOR, null);
    int themeColor;
    if (TextUtils.isEmpty(theme)) {
      if (Build.VERSION.SDK_INT >= 23) {
        themeColor = context.getColor(defaultColorRes);
      } else {
        themeColor = context.getResources().getColor(defaultColorRes);
      }
    } else {
      themeColor = Color.parseColor(theme);
    }
    return themeColor;
  }


  public static String byteArrayToHexString(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte element : bytes) {
      int v = element & 0xff;
      if (v < 16) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(v));
    }
    return sb.toString().toUpperCase(Locale.US);
  }

  public static String getBaseUrl(Context context) {
    return "http://" + PreferenceUtil.getEnv(context);
  }

  public static String getBaseHttpsUrl(Context context) {
    return "https://" + PreferenceUtil.getEnv(context);
  }

  public static boolean isDev(Context context) {
    String env = PreferenceUtil.getEnv(context);
    return !env.equals("namibox.com");
  }

  public static boolean isCustomPadDev(Context context) {
    String env = PreferenceUtil.getEnv(context);
    return !env.equals("tailor.namibox.com");
  }

  public static boolean isLogin(Context context) {
    String sessionId = PreferenceUtil.getSessionId(context);
    if (TextUtils.isEmpty(sessionId)) {
      return false;
    }
    return true;

//        long expireTime = PreferenceUtil.getSharePref(context, PreferenceUtil.PREF_EXPIRE_TIME, 0L);
//        long current = System.currentTimeMillis();
//        return expireTime > current;
  }

  public static boolean isBindHuaWeiAccount(Context context, String userId) {
    String hwUnionId = PreferenceUtil.getHwUnionId(context, userId);
    if (TextUtils.isEmpty(hwUnionId)) {
      return false;
    }
    return true;
  }

  public static long getLongLoginUserId(Context context) {
    return PreferenceUtil.getLongLoginUserId(context);
  }

  public static String getLoginUserId(Context context) {
    long userId = getLongLoginUserId(context);
    if (userId != -1L) {
      return String.valueOf(userId);
    }
    return "0";
  }

  public static void showKeyboard(Context context, View view) {
    view.requestFocus();
    InputMethodManager imm = (InputMethodManager) context
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput(view, 0);
  }

  public static void hideKeyboard(Context context, View view) {
    InputMethodManager imm = (InputMethodManager) context
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  public static void openApp(Context c, String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return;
    }
    PackageManager pm = c.getPackageManager();
    Intent intent = pm.getLaunchIntentForPackage(packageName);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    c.startActivity(intent);
  }

  public static boolean isAppInstalled(Context context, String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return false;
    }
    PackageManager pm = context.getPackageManager();
    try {
      ApplicationInfo activityInfo = pm.getApplicationInfo(packageName, 0);
      return activityInfo != null;
    } catch (Exception e) {
      //e.printStackTrace();
    }
    return false;
  }

  public static boolean isAppAlive(Context context, String packageName) {
    ActivityManager activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<RunningAppProcessInfo> processInfos
        = activityManager.getRunningAppProcesses();
    for (int i = 0; i < processInfos.size(); i++) {
      if (processInfos.get(i).processName.equals(packageName)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isForeground(Context context) {
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
    if (appProcesses != null && !appProcesses.isEmpty()) {
      for (RunningAppProcessInfo appProcess : appProcesses) {
        if (appProcess.processName.equals(context.getPackageName())
            && appProcess.importance
            == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
          return true;
        }
      }
    }
    return false;
  }

  public static void openMarket(Context context) {
//        try {
//            String mAddress = "market://details?id=" + context.getPackageName();
//            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
//            marketIntent.setData(Uri.parse(mAddress));
//            context.startActivity(marketIntent);
//        } catch (ActivityNotFoundException e) {
//            Toast.makeText(context, "无法启动应用", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }

    String channel = PreferenceUtil.getChannel(context);
    if ("Huawei".equals(channel)) {//华为渠道使用deeplink打开评论页面
      openHuaweiMarket(context);
    } else {
      openMarket(context, context.getPackageName());
    }
  }

  static final String HUAWEI_MARKET_DEEPLINK = "hiapp://com.huawei.appmarket?activityName=activityUri|appdetail.activity&params={\"params\":[{\"name\":\"uri\",\"type\":\"String\",\"value\":\"package|com.jinxin.namibox\"}]}&channelId=1234567";

  private static void openHuaweiMarket(Context context) {
    try {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(HUAWEI_MARKET_DEEPLINK));
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
    } catch (Exception e) {
      Log.e("deeplink打开华为应用商店报错", e.toString());
      e.printStackTrace();
      openMarket(context, context.getPackageName());
    }
  }

  public static void openMarket(Context context, String packageName) {
    try {
      String mAddress = "market://details?id=" + packageName;
      Intent marketIntent = new Intent(Intent.ACTION_VIEW);
      marketIntent.setData(Uri.parse(mAddress));
      context.startActivity(marketIntent);
    } catch (ActivityNotFoundException e) {
      toast(context, "无法启动应用");
      e.printStackTrace();
    }
  }

  public static String readMetaDataFromApplication(Context context, String key) {
    try {
      ApplicationInfo appInfo = context.getPackageManager()
          .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      return appInfo.metaData.getString(key);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void toast(Context context, int resId) {
    String msg = context.getString(resId);
    toast(context, msg);
  }

  public static void toast(Context context, String msg) {
    if (!areNotificationsEnabled(context) && context instanceof Activity) {
      Activity activity = (Activity) context;
      NToast nToast = activity.findViewById(R.id.ntoast_layout_id);
      if (nToast == null) {
        nToast = new NToast(activity, NToast.TYPE_1);
        nToast.setId(R.id.ntoast_layout_id);
      }
      nToast.show(msg);
    } else {
      NToast.toast(context, msg);
    }
  }

  public static void toast2(Activity activity, String msg) {
    NToast nToast = activity.findViewById(R.id.ntoast_layout_id2);
    if (nToast == null) {
      nToast = new NToast(activity, NToast.TYPE_2);
      nToast.setId(R.id.ntoast_layout_id2);
    }
    nToast.show(msg);
  }

  private static boolean areNotificationsEnabled(Context context) {
    if (Build.VERSION.SDK_INT >= 24) {
      NotificationManager nm = (NotificationManager) context
          .getSystemService(Context.NOTIFICATION_SERVICE);
      return nm != null && nm.areNotificationsEnabled();
    } else if (Build.VERSION.SDK_INT >= 19) {
      AppOpsManager appOps =
          (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
      ApplicationInfo appInfo = context.getApplicationInfo();
      String pkg = context.getApplicationContext().getPackageName();
      int uid = appInfo.uid;
      try {
        Class<?> appOpsClass = Class.forName(AppOpsManager.class.getName());
        Method checkOpNoThrowMethod = appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE,
            Integer.TYPE, String.class);
        Field opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION");
        int value = (int) opPostNotificationValue.get(Integer.class);
        return ((int) checkOpNoThrowMethod.invoke(appOps, value, uid, pkg)
            == AppOpsManager.MODE_ALLOWED);
      } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException
          | InvocationTargetException | IllegalAccessException | RuntimeException e) {
        return true;
      }
    } else {
      return true;
    }
  }

  public static void installApp(Context c, File apkFile) {
    try {
      Intent installIntent = new Intent(Intent.ACTION_VIEW);
      installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      FileProvider7.setIntentDataAndType(c,
          installIntent, "application/vnd.android.package-archive", apkFile, true);
      c.startActivity(installIntent);
    } catch (Exception e) {
      e.printStackTrace();
      toast(c, "暂时无法安装应用");
    }
  }

  public static String getSystemSimpleMessage(Context context) {
    HashMap<String, HashMap<String, String>> info = new HashMap<>();
    HashMap<String, String> sys_env = new HashMap<>();
    info.put("sys_env", sys_env);
    HashMap<String, String> hardware = new HashMap<>();
    info.put("hardware", hardware);
    HashMap<String, String> app_env = new HashMap<>();
    info.put("app_env", app_env);
    sys_env.put("os_version", Build.VERSION.RELEASE);
    sys_env.put("os_name", "Android");
    sys_env.put("network", NetworkUtil.getNetTypeString(context));
    hardware.put("brand", Build.BRAND);
    hardware.put("model", Build.MODEL);
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    am.getMemoryInfo(mi);
    long availMem = mi.availMem / (1024 * 1024);
    if (Build.VERSION.SDK_INT >= 16) {
      long totalMem = mi.totalMem / (1024 * 1024);
      hardware.put("memory", availMem + "M/" + totalMem + "M");
    } else {
      hardware.put("memory", availMem + "M");
    }
    String storage = PreferenceUtil.getSelectedStorage(context);
    File file = new File(storage);
    if (file.exists()) {
      String total = formatLengthString(file.getTotalSpace());
      String free = formatLengthString(file.getFreeSpace());
      storage += " (" + free + "/" + total + ")";
    }
    hardware.put("storage", storage);
    String uuid = new DeviceUuidFactory(context).getDeviceUuid().toString();
    hardware.put("dev_id", uuid);
    app_env.put("app_version", getVersionName(context));
    app_env.put("app_version_code", String.valueOf(getVersionCode(context)));
    String channel = PreferenceUtil.getChannel(context);
    app_env.put("app_channel", channel);
    app_env.put("cache_size", formatLengthString(FileUtil.getDirSize(context.getCacheDir())));
    //Logger.d(TAG, "sys_info: " + msg);
    Gson gson = new Gson();
    return gson.toJson(info);
  }

  public static String getSystemMessage(Context context) {
    HashMap<String, HashMap<String, String>> info = new HashMap<>();
    HashMap<String, String> sys_env = new HashMap<>();
    info.put("sys_env", sys_env);
    HashMap<String, String> hardware = new HashMap<>();
    info.put("hardware", hardware);
    HashMap<String, String> app_env = new HashMap<>();
    info.put("app_env", app_env);
    sys_env.put("os_version", Build.VERSION.RELEASE);
    sys_env.put("os_name", "Android");
    sys_env.put("network", NetworkUtil.getNetTypeString(context));
    hardware.put("brand", Build.BRAND);
    hardware.put("model", Build.MODEL);
    WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics displayMetrics = new DisplayMetrics();
    mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
    hardware.put("resolution", displayMetrics.widthPixels + "x" + displayMetrics.heightPixels);
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    am.getMemoryInfo(mi);
    long availMem = mi.availMem / (1024 * 1024);
    if (Build.VERSION.SDK_INT >= 16) {
      long totalMem = mi.totalMem / (1024 * 1024);
      hardware.put("memory", availMem + "M/" + totalMem + "M");
    } else {
      hardware.put("memory", availMem + "M");
    }
    String storage = PreferenceUtil.getSelectedStorage(context);
    File file = new File(storage);
    if (file.exists()) {
      String total = formatLengthString(file.getTotalSpace());
      String free = formatLengthString(file.getFreeSpace());
      storage += " (" + free + "/" + total + ")";
    }
    hardware.put("storage", storage);
    String uuid = new DeviceUuidFactory(context).getDeviceUuid().toString();
    hardware.put("dev_id", uuid);
    hardware.put("isTablet", isTablet(context) + "");
    app_env.put("app_version", getVersionName(context));
    app_env.put("app_version_code", String.valueOf(getVersionCode(context)));
    String channel = PreferenceUtil.getChannel(context);
    app_env.put("app_channel", channel);
    app_env.put("cache_size", formatLengthString(FileUtil.getDirSize(context.getCacheDir())));
    Gson gson = new Gson();
    return gson.toJson(info);
  }


  public static String getLocalDNS() {
    return getSystemProperty("net.dns1");
  }

  public static String getSystemProperty(String propName) {
    String line = "";
    BufferedReader input = null;
    Process p = null;
    try {
      p = Runtime.getRuntime().exec("getprop " + propName);
      input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
      line = input.readLine();
      input.close();
    } catch (IOException ex) {
      return line;
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
        }
      }
      if (p != null) {
        p.destroy();
      }
    }
    return line;
  }

  public static boolean checkSupportV7a() {
    boolean isV7a = false;
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      String[] ABIS = Build.SUPPORTED_ABIS;
      for (String i : ABIS) {
        if (i.equals("armeabi-v7a")) {
          isV7a = true;
        }
      }
    } else {
      if (Build.CPU_ABI.equals("armeabi-v7a")) {
        isV7a = true;
      }
    }
    return isV7a;
  }

  public static boolean checkIsX86() {
    if (Build.VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
      String abi = getProperty("ro.product.cpu.abi", null);
      if ("x86".equals(abi)) {
        return true;
      }
    }
    return false;
  }

  public static int checkValidVideo(Context context, Uri videoUri) {
    if (videoUri == null) {
      return -2;
    }
    long duration = MediaUtils.getDuration(context,
        MediaUtils.getRealVideoPathFromURI(context.getContentResolver(), videoUri));
    if (duration == 0) {
      duration = MediaUtils
          .getDuration(context, videoUri);
    }
    if (duration < MINTIMEMS) {
      return -1;
    }
    return 1;
  }

  public static void sendImplicitBroadcast(Context ctxt, Intent i) {
    i.setPackage(ctxt.getPackageName());
    ctxt.sendBroadcast(i);
//    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//      ctxt.sendBroadcast(i);
//      return;
//    }
//    PackageManager pm = ctxt.getPackageManager();
//    List<ResolveInfo> matches = pm.queryBroadcastReceivers(i, 0);
//
//    for (ResolveInfo resolveInfo : matches) {
//      Intent explicit = new Intent(i);
//      ComponentName cn =
//          new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
//              resolveInfo.activityInfo.name);
//
//      explicit.setComponent(cn);
//      ctxt.sendBroadcast(explicit);
//    }
  }

  public static File getLogFile(Context context) {
    //与NamiboxApp保持一致
    File folder = PreferenceUtil.getLogDir(context);
    //获取所有文件并排序
    File[] files = getSortFiles(folder);
    //合并后的汇总日志文件
    File summaryFile = new File(Environment.getExternalStorageDirectory(), "summaryLog.txt");
    //如果文件已经存在 删除 防止多次上传 日志累加
    if (summaryFile.exists()) {
      summaryFile.delete();
    }
    //取合并后的日志进行上传
    File file = mergeFiles(files, summaryFile);
    //如果合并后日志为空 取当天日志上传 否则上传合并后的日志
    if (file == null) {
      Date date = new Date();
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
      date.setTime(System.currentTimeMillis());
      String dateStr = format.format(date);
      return new File(folder, String.format(Locale.US, "log_%s.txt", dateStr));
    } else {
      return file;
    }
  }

  /**
   * 将日志排序
   */
  private static File[] getSortFiles(File folder) {
    String[] list = folder.list();
    if (list != null) {
      File[] files = new File[list.length];
      for (int i = 0; i < list.length; i++) {
        files[i] = new File(folder, list[i]);
      }
      Arrays.sort(files, new Comparator<File>() {
        @Override
        public int compare(File f1, File f2) {
          long diff = f1.lastModified() - f2.lastModified();
          if (diff > 0) {
            return 1;
          } else if (diff == 0) {
            return 0;
          } else {
            return -1;
          }
        }

        public boolean equals(Object obj) {
          return true;
        }

      });
      if (files.length > 3) {
        File[] sortFiles = new File[3];
        sortFiles[0] = files[files.length - 3];
        sortFiles[1] = files[files.length - 2];
        sortFiles[2] = files[files.length - 1];
        return sortFiles;
      } else {
        return files;
      }

    }
    return new File[0];
  }

  /**
   * 文件合并
   */
  public static File mergeFiles(File[] files, File tempFile) {
    FileChannel mFileChannel;
    try {
      FileOutputStream fos = new FileOutputStream(tempFile);
      mFileChannel = fos.getChannel();
      FileChannel inFileChannel;
      for (File file : files) {
        inFileChannel = new FileInputStream(file).getChannel();
        //下面应该根据不同文件减去相应的文件头（这里没有剪去文件头，实际应用中应当减去）
        inFileChannel.transferTo(0, inFileChannel.size(), mFileChannel);
        inFileChannel.close();
      }
      fos.close();
      mFileChannel.close();
      return tempFile;
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e("zkx Exception =" + e.toString());
    }
    return null;
  }


  //解析ws地址，生成log文件名
  public static String logFileFromWsUrl(Context context, String ws_url) {
    StringBuilder fileName = new StringBuilder();
    fileName.append(Utils.getLoginUserId(context));
    HttpUrl httpUrl = HttpUrl.parse(ws_url.replaceFirst("ws", "http"));
    if (httpUrl != null) {
      List<String> pathSegments = httpUrl.pathSegments();
      if (pathSegments != null) {
        for (String path : pathSegments) {
          if (!TextUtils.isEmpty(path)) {
            fileName.append('_');
            fileName.append(path);
          }
        }
      }
    }
    fileName.append(".txt");
    return fileName.toString();
  }

  public static Context getContext(Context context) {
    String clsName = context.getClass().getName();
    if ("com.qihoo360.loader2.PluginContext".equals(clsName)) {
      return ((ContextWrapper) context).getBaseContext();
    } else {
      Context base = ((ContextWrapper) context).getBaseContext();
      String baseClsName = base.getClass().getName();
      if ("com.qihoo360.loader2.PluginContext".equals(baseClsName)) {
        return Reflect.on("com.qihoo360.replugin.RePlugin").call("getHostContext").get();
      }
      return context.getApplicationContext();
    }
  }

  public static boolean isPluginContext(Context context) {
    String clsName = context.getClass().getName();
    return "com.qihoo360.loader2.PluginContext".equals(clsName);
  }

  public static double getPhySicsScreenSize(Context context) {
    WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Point point = new Point();
    manager.getDefaultDisplay().getRealSize(point);
    DisplayMetrics dm = context.getResources().getDisplayMetrics();
    double x = Math.pow(point.x / dm.xdpi, 2);//dm.xdpi是屏幕x方向的真实密度值，比上面的densityDpi真实。
    double y = Math.pow(point.y / dm.ydpi, 2);//dm.xdpi是屏幕y方向的真实密度值，比上面的densityDpi真实。
    double screenInches = Math.sqrt(x + y);
    return screenInches;
  }

  public static boolean isOldDevice() {
    return Build.VERSION.SDK_INT < VERSION_CODES.M;
  }

  public static String getParam(String url, String name) {
    Uri uri = Uri.parse(url);
    return uri.getQueryParameter(name);
  }

  /**
   * Convert a translucent themed Activity
   * {@link android.R.attr#windowIsTranslucent} to a fullscreen opaque
   * Activity.
   * <p>
   * Call this whenever the background of a translucent Activity has changed
   * to become opaque. Doing so will allow the {@link android.view.Surface} of
   * the Activity behind to be released.
   * <p>
   * This call has no effect on non-translucent activities or on activities
   * with the {@link android.R.attr#windowIsFloating} attribute.
   */
  public static void convertActivityFromTranslucent(Activity activity) {
    try {
      Method method = Activity.class.getDeclaredMethod("convertFromTranslucent");
      method.setAccessible(true);
      method.invoke(activity);
    } catch (Throwable t) {
    }
  }

  /**
   * Convert a translucent themed Activity
   * {@link android.R.attr#windowIsTranslucent} back from opaque to
   * translucent following a call to
   * {@link #convertActivityFromTranslucent(Activity)} .
   * <p>
   * Calling this allows the Activity behind this one to be seen again. Once
   * all such Activities have been redrawn
   * <p>
   * This call has no effect on non-translucent activities or on activities
   * with the {@link android.R.attr#windowIsFloating} attribute.
   */
  public static void convertActivityToTranslucent(Activity activity) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      convertActivityToTranslucentAfterL(activity);
    } else {
      convertActivityToTranslucentBeforeL(activity);
    }
  }

  /**
   * Calling the convertToTranslucent method on platforms before Android 5.0
   */
  private static void convertActivityToTranslucentBeforeL(Activity activity) {
    try {
      Class<?>[] classes = Activity.class.getDeclaredClasses();
      Class<?> translucentConversionListenerClazz = null;
      for (Class clazz : classes) {
        if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
          translucentConversionListenerClazz = clazz;
        }
      }
      Method method = Activity.class.getDeclaredMethod("convertToTranslucent",
          translucentConversionListenerClazz);
      method.setAccessible(true);
      method.invoke(activity, new Object[]{
          null
      });
    } catch (Throwable t) {
    }
  }

  /**
   * Calling the convertToTranslucent method on platforms after Android 5.0
   */
  private static void convertActivityToTranslucentAfterL(Activity activity) {
    try {
      Method getActivityOptions = Activity.class.getDeclaredMethod("getActivityOptions");
      getActivityOptions.setAccessible(true);
      Object options = getActivityOptions.invoke(activity);

      Class<?>[] classes = Activity.class.getDeclaredClasses();
      Class<?> translucentConversionListenerClazz = null;
      for (Class clazz : classes) {
        if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
          translucentConversionListenerClazz = clazz;
        }
      }
      Method convertToTranslucent = Activity.class.getDeclaredMethod("convertToTranslucent",
          translucentConversionListenerClazz, ActivityOptions.class);
      convertToTranslucent.setAccessible(true);
      convertToTranslucent.invoke(activity, null, options);
    } catch (Throwable t) {
    }
  }

  @TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
  public static boolean hasNavBar(Context context) {
    Resources res = context.getResources();
    int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
    if (resourceId != 0) {
      boolean hasNav = res.getBoolean(resourceId);
      // check override flag
      String sNavBarOverride = getNavBarOverride();
      if ("1".equals(sNavBarOverride)) {
        hasNav = false;
      } else if ("0".equals(sNavBarOverride)) {
        hasNav = true;
      }
      return hasNav;
    } else { // fallback
      return !ViewConfiguration.get(context).hasPermanentMenuKey();
    }
  }

  /**
   * 判断虚拟按键栏是否重写
   */
  private static String getNavBarOverride() {
    String sNavBarOverride = null;
    if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
      try {
        Class c = Class.forName("android.os.SystemProperties");
        Method m = c.getDeclaredMethod("get", String.class);
        m.setAccessible(true);
        sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
      } catch (Throwable e) {
      }
    }
    return sNavBarOverride;
  }
}
