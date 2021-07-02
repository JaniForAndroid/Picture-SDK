package com.namibox.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.model.UserIpInfo;
import com.namibox.util.DeviceUuidFactory;
import com.namibox.util.FileUtil;
import com.namibox.util.NetworkUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by sunha on 2016/12/6 0006.
 */

public class DeviceInfoUtil {

  public static String getDeviceId(Context context) {
    return new DeviceUuidFactory(context).getDeviceUuid().toString();
  }

  public static String getReportInfo(Context context) throws Exception {
    UserIpInfo userIpInfo = ApiHandler.getBaseApi().getUserIpInfo().execute().body();
    String ip = userIpInfo == null ? null : userIpInfo.ip;
    if (ip != null) {
      PreferenceUtil.setSharePref(context, "user_ip", ip);
    }
    String city = userIpInfo == null ? null : userIpInfo.province + userIpInfo.city;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
    String storage = PreferenceUtil.getSelectedStorage(context);
    File file = new File(storage);
    if (file.exists()) {
      String total = Utils.formatLengthString(file.getTotalSpace());
      String free = Utils.formatLengthString(file.getFreeSpace());
      storage += " (" + free + "/" + total + ")";
    }
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    am.getMemoryInfo(mi);
    long availMem = mi.availMem / (1024 * 1024);
    long totalMem = mi.totalMem / (1024 * 1024);
    return "设备品牌:" + Build.BRAND +
        "\n设备型号:" + Build.MODEL +
        "\nUUID:" + new DeviceUuidFactory(context).getDeviceUuid().toString() +
        "\n用户id:" + Utils.getLoginUserId(context) +
        "\n注册手机号:" + PreferenceUtil.getSharePref(context, "user_phone", "") +
        "\n设备分辨率:" + displayMetrics.widthPixels + "x" + displayMetrics.heightPixels +
        "\n存储空间:" + storage +
        "\n内存:" + availMem + "M/" + totalMem + "M" +
        "\n已用缓存:" + Utils.formatLengthString(FileUtil.getDirSize(context.getCacheDir())) +
        "\n系统版本:" + "Android-" + Build.VERSION.RELEASE +
        "\n版本号:" + Build.VERSION.SDK_INT +
        "\napp版本:" + Utils.getVersionName(context) + "-" + Utils.getVersionCode(context) +
        "\napp渠道:" + PreferenceUtil.getChannel(context) +
        "\n网络类型:" + NetworkUtil.getNetTypeString(context) +
        "\nip地址:" + ip +
        "\n地理位置:" + city +
        "\nDNS:" + Utils.getLocalDNS() +
        "\n" + getIP("r") +
        "\n" + getIP("u") +
        "\n" + getIP("f") +
        "\n" + getIP("v") +
        "\n" + getIP("ali") +
        "\n" + getIP("tencent") +
        "\n" + getIP("wangsu") +
        "\n" + getIP("ra");
  }

  /***
   * 带错误信息的上报
   * @param context
   * @param errorMessage
   * @return
   * @throws Exception
   */
  public static String getReportInfo(Context context, String errorMessage) throws Exception {
    UserIpInfo userIpInfo = ApiHandler.getBaseApi().getUserIpInfo().execute().body();
    String ip = userIpInfo == null ? null : userIpInfo.ip;
    if (ip != null) {
      PreferenceUtil.setSharePref(context, "user_ip", ip);
    }
    String city = userIpInfo == null ? null : userIpInfo.province + userIpInfo.city;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
    String storage = PreferenceUtil.getSelectedStorage(context);
    File file = new File(storage);
    if (file.exists()) {
      String total = Utils.formatLengthString(file.getTotalSpace());
      String free = Utils.formatLengthString(file.getFreeSpace());
      storage += " (" + free + "/" + total + ")";
    }
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    am.getMemoryInfo(mi);
    long availMem = mi.availMem / (1024 * 1024);
    long totalMem = mi.totalMem / (1024 * 1024);
    return "设备品牌:" + Build.BRAND +
        "\n设备型号:" + Build.MODEL +
        "\nUUID:" + new DeviceUuidFactory(context).getDeviceUuid().toString() +
        "\n用户id:" + Utils.getLoginUserId(context) +
        "\n注册手机号:" + PreferenceUtil.getSharePref(context, "user_phone", "") +
        "\n设备分辨率:" + displayMetrics.widthPixels + "x" + displayMetrics.heightPixels +
        "\n存储空间:" + storage +
        "\n内存:" + availMem + "M/" + totalMem + "M" +
        "\n已用缓存:" + Utils.formatLengthString(FileUtil.getDirSize(context.getCacheDir())) +
        "\n系统版本:" + "Android-" + Build.VERSION.RELEASE +
        "\n版本号:" + Build.VERSION.SDK_INT +
        "\napp版本:" + Utils.getVersionName(context) + "-" + Utils.getVersionCode(context) +
        "\napp渠道:" + PreferenceUtil.getChannel(context) +
        "\n网络类型:" + NetworkUtil.getNetTypeString(context) +
        "\nip地址:" + ip +
        "\n地理位置:" + city +
        "\nerrorMessage:" + errorMessage +
        "\nDNS:" + Utils.getLocalDNS() +
        "\n" + getIP("r") +
        "\n" + getIP("u") +
        "\n" + getIP("f") +
        "\n" + getIP("v") +
        "\n" + getIP("ali") +
        "\n" + getIP("tencent") +
        "\n" + getIP("wangsu") +
        "\n" + getIP("ra"
    );
  }
  /**
   * 根据hostName 获取ip地址
   * 注意该操作需要在子线程中进行
   */
  private static String getIP(String hostName) {
    String dnsContent = hostName + "域名IP:";
    try {
      String ip = InetAddress.getByName(hostName + ".namibox.com").getHostAddress();
      dnsContent = dnsContent + ip;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return dnsContent;
  }

  public static String getMacAddress(Context c) {
    String mac = "02:00:00:00:00:00";
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      mac = getMacDefault(c);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      mac = getMacFromFile();
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      mac = getMacFromHardware();
    }
    return mac;
  }

  public static String getMacFromFile() {
    String WifiAddress = "02:00:00:00:00:00";
    try {
      WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address")))
          .readLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return WifiAddress;
  }

  /**
   * Android  6.0 之前（不包括6.0）
   * 必须的权限  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
   */
  public static String getMacDefault(Context context) {
    String mac = "02:00:00:00:00:00";
    if (context == null) {
      return mac;
    }

    WifiManager wifi = (WifiManager) context.getApplicationContext()
        .getSystemService(Context.WIFI_SERVICE);
    if (wifi == null) {
      return mac;
    }
    WifiInfo info = null;
    try {
      info = wifi.getConnectionInfo();
    } catch (Exception e) {
    }
    if (info == null) {
      return null;
    }
    mac = info.getMacAddress();
    if (!TextUtils.isEmpty(mac)) {
      mac = mac.toUpperCase(Locale.ENGLISH);
    }
    return mac;
  }


  /**
   * 遍历循环所有的网络接口，找到接口是 wlan0
   * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
   */
  public static String getMacFromHardware() {
    try {
      List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface nif : all) {
        if (!nif.getName().equalsIgnoreCase("wlan0")) {
          continue;
        }

        byte[] macBytes = nif.getHardwareAddress();
        if (macBytes == null) {
          return "";
        }

        StringBuilder res1 = new StringBuilder();
        for (byte b : macBytes) {
          res1.append(String.format("%02X:", b));
        }

        if (res1.length() > 0) {
          res1.deleteCharAt(res1.length() - 1);
        }
        return res1.toString();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "02:00:00:00:00:00";
  }
}
