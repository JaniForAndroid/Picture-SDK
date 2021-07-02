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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import com.namibox.util.network.DownloadResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sunha on 2017/6/1 0001.
 */

public class NetworkUtil {

  public static final String NETWORKTYPE_INVALID = "UNKNOWN";// 没有网络
  public static final String NETWORKTYPE_WAP = "WAP"; // wap网络
  public static final String NETWORKTYPE_WIFI = "WIFI"; // wifi网络
  private static final String DEFAULT_UA1 = "Mozilla/5.0 (Linux; Android 4.0.4; en-us; Nexus 4 Build/JOP40D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36";
  private static final String DEFAULT_UA2 = "Mozilla/5.0 (Linux; Android 4.1; en-us; Nexus 4 Build/JOP40D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36";
  private static final String DEFAULT_UA3 = "Mozilla/5.0 (Linux; Android 4.2; en-us; Nexus 4 Build/JOP40D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36";
  private static final String DEFAULT_GIONEE = "Mozilla/5.0 (Linux; Android 4.1; en-us; F100S Build/JOP40D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36";
//    private static PersistentCookieStore cookieStore;

//    public static void syncCookies(Context context, String url) {
//        //CookieSyncManager.createInstance(context);
//        List<Cookie> cookies = getCookies(context, url);
//        if (cookies == null) {
//            return;
//        }
//        if (Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(context)) {
//            return;
//        }
//        for (Cookie cookie : cookies) {
//            Logger.i("syncCookie: " + cookie.toString());
//            android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
//            cookieManager.setAcceptCookie(true);
//            cookieManager.setCookie(url, cookie.toString());
//        }
//        //CookieSyncManager.getInstance().sync();
//    }
//
//    public static void removeCookie(Context context) {
//        getPersistentCookieStore(context).removeAll();
//        if (Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(context)) {
//            return;
//        }
//        android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
//        cookieManager.setAcceptCookie(true);
//        if (Build.VERSION.SDK_INT < 21) {
//            cookieManager.removeSessionCookie();
//        } else {
//            cookieManager.removeSessionCookies(new ValueCallback<Boolean>() {
//                @Override
//                public void onReceiveValue(Boolean value) {
//                    Logger.d("onReceiveValue:" + value);
//                }
//            });
//        }
//    }
//
//    public static List<Cookie> getCookies(Context context, String url) {
//        HttpUrl httpUrl = HttpUrl.parse(url);
//        if (httpUrl != null) {
//            return getPersistentCookieStore(context).getCookies(httpUrl);
//        }
//        return null;
//    }
//
//    public static PersistentCookieStore getPersistentCookieStore(Context context) {
//        if (cookieStore == null) {
//            cookieStore = new PersistentCookieStore(context.getApplicationContext());
//        }
//        return cookieStore;
//    }

  public static void syncCookie(Context context, String url) {
    String sessionid = PreferenceUtil.getSessionId(context);
    String expirestime = PreferenceUtil.getSessionTime(context);
    if (TextUtils.isEmpty(sessionid)) {
      return;
    }
    syncCookie(context, url, sessionid, expirestime);
  }

  public static void syncCookie(Context context, String url, String sessionid, String expirestime) {
    String cookie = "sessionid=" + sessionid;
    cookie += "; Domain=.namibox.com";
    if (!TextUtils.isEmpty(expirestime)) {
      cookie += "; expires=";
      cookie += expirestime;
    }
    cookie += "; httponly; Path=/";
    syncCookie(context, url, cookie);
  }

  public static void syncCookie(Context context, String url, String cookie) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      CookieSyncManager.createInstance(context);
    }
    CookieManager cookieManager = CookieManager.getInstance();
//    Log.e("tag", "======\ncookie: " + cookieManager.getCookie(url));
    Logger.d("NetworkUtil", "syncCookie: " + url + "\ncookie: " + cookie);
//    Log.e("tag", "cookie: " + cookieManager.getCookie(url) + "\n=====");
    try {
      cookieManager.setCookie(url, cookie);
      cookieManager.setCookie(url, getNbtzCookie());
      cookieManager.setCookie(url, getNblangCookie(context));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static String getNbtzCookie() {
    String nbtz = TimeZone.getDefault().getID();
    String nbtzCookie = "nbtz=" + nbtz;
    nbtzCookie += "; Domain=.namibox.com; httponly; Path=/";
    return nbtzCookie;
  }

  private static String getNblangCookie(Context context) {
    String nblang = LanguageUtil.getLanguage(context);
    String nblangCookie = "nblang=" + nblang;
    nblangCookie += "; Domain=.namibox.com; httponly; Path=/";
    return nblangCookie;
  }

  public static String getCookie(Context context, String url) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      CookieSyncManager.createInstance(context);
    }
    CookieManager cookieManager = CookieManager.getInstance();
    try {
      return cookieManager.getCookie(url);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void removeCookie(Context context) {
    Logger.d("NetworkUtil", "removeCookie");
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      CookieSyncManager.createInstance(context);
    }
    CookieManager cookieManager = CookieManager.getInstance();
    try {
      cookieManager.removeAllCookie();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static boolean isNetworkConnected(Context context) {
    if (context != null) {
      ConnectivityManager mConnectivityManager = (ConnectivityManager) context
          .getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
      if (mNetworkInfo != null) {
        return mNetworkInfo.isConnected();
      }
    }
    return false;
  }

  public static boolean isWiFi(Context context) {
    ConnectivityManager connectMgr = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo info = connectMgr.getActiveNetworkInfo();
    return info != null && info.getType() == ConnectivityManager.TYPE_WIFI;
  }

  public static boolean isMobile(Context context) {
    ConnectivityManager connectMgr = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo info = connectMgr.getActiveNetworkInfo();
    return info != null && info.getType() == ConnectivityManager.TYPE_MOBILE;
  }

  public static boolean isNetworkAvailable(Context context) {
    if (context != null) {
      ConnectivityManager mConnectivityManager = (ConnectivityManager) context
          .getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
      if (mNetworkInfo != null) {
        return mNetworkInfo.isAvailable();
      }
    }
    return false;
  }

  public static String getNetTypeString(Context context) {
    ConnectivityManager connectMgr = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo info = connectMgr.getActiveNetworkInfo();
    if (info == null) {
      return "unknown";
    }
    return info.getTypeName();
  }

  public static String getActiveNetworkInfo(Context context) {
    ConnectivityManager connectMgr = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo info = connectMgr.getActiveNetworkInfo();
    if (info == null) {
      return "unknown";
    }
    return info.toString();
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

  public static String getMobileOperator(Context context) {
    TelephonyManager telManager = (TelephonyManager) context
        .getSystemService(Context.TELEPHONY_SERVICE);
    if (telManager == null) {
      return "未知";
    }
    String operator = telManager.getSimOperator();
    if (operator != null) {
      if (operator.equals("46000") || operator.equals("46002")
          || operator.equals("46007")) {
        return "中国移动";
      } else if (operator.equals("46001")) {
        return "中国联通";
      } else if (operator.equals("46003")) {
        return "中国电信";
      }
    }
    return "未知";
  }

  public static final int MOBILE_UNKNOWN = 0;//未知
  public static final int MOBILE_1 = 1;//移动
  public static final int MOBILE_2 = 2;//联通
  public static final int MOBILE_3 = 3;//电信

  public static int getMobileOperatorCode(Context context) {
    TelephonyManager telManager = (TelephonyManager) context
        .getSystemService(Context.TELEPHONY_SERVICE);
    if (telManager == null) {
      return MOBILE_UNKNOWN;
    }
    String operator = telManager.getSimOperator();
    if (operator != null) {
      if (operator.equals("46000") || operator.equals("46002")
          || operator.equals("46007")) {
        return MOBILE_1;
      } else if (operator.equals("46001")) {
        return MOBILE_2;
      } else if (operator.equals("46003")) {
        return MOBILE_3;
      }
    }
    return MOBILE_UNKNOWN;
  }

  public static String getDefaultUserAgent(Context context) {
    if (Utils.isGIONEE()) {
      return DEFAULT_GIONEE;
    }
    String ua = PreferenceUtil.getSharePref(context, "default_ua", null);
    if (!TextUtils.isEmpty(ua)) {
      return ua;
    }
    try {
      String s = formatHeaderString(WebSettings.getDefaultUserAgent(context));
      PreferenceUtil.setSharePref(context, "default_ua", s);
      return s;
    } catch (Exception e) {
      e.printStackTrace();
      return DEFAULT_UA3;
    }
  }

  public static String formatHeaderString(String text) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0, length = text.length(); i < length; i++) {
      char c = text.charAt(i);
      if (c > '\u001f' && c < '\u007f') {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * 获取本机IP(wifi)
   */
  public static String getLocalIpByWifi(Context context) {
    WifiManager wifiManager = (WifiManager) context
        .getSystemService(Context.WIFI_SERVICE);
    if (wifiManager == null) {
      return "wifiManager not found";
    }
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    if (wifiInfo == null) {
      return "wifiInfo not found";
    }
    int ipAddress = wifiInfo.getIpAddress();
    return String.format("%d.%d.%d.%d", (ipAddress & 0xff),
        (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
        (ipAddress >> 24 & 0xff));
  }

  /**
   * 获取本机IP(2G/3G/4G)
   */
  public static String getLocalIpBy3G() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface
          .getNetworkInterfaces(); en.hasMoreElements(); ) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
            .hasMoreElements(); ) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          if (!inetAddress.isLoopbackAddress()
              && inetAddress instanceof Inet4Address) {
            // if (!inetAddress.isLoopbackAddress() && inetAddress
            // instanceof Inet6Address) {
            return inetAddress.getHostAddress().toString();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String getLocalDns(String dns) {
    Process process = null;
    String str = "";
    BufferedReader reader = null;
    try {
      process = Runtime.getRuntime().exec("getprop net." + dns);
      reader = new BufferedReader(new InputStreamReader(
          process.getInputStream()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        str += line;
      }
      reader.close();
      process.waitFor();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
        process.destroy();
      } catch (Exception e) {
      }
    }
    return str.trim();
  }

  public static Map<String, Object> getDomainIp(String _dormain) {
    Map<String, Object> map = new HashMap<String, Object>();
    long start = 0;
    long end = 0;
    String time = null;
    InetAddress[] remoteInet = null;
    try {
      start = System.currentTimeMillis();
      remoteInet = InetAddress.getAllByName(_dormain);
      if (remoteInet != null) {
        end = System.currentTimeMillis();
        time = (end - start) + "";
      }
    } catch (UnknownHostException e) {
      end = System.currentTimeMillis();
      time = (end - start) + "";
      remoteInet = null;
      e.printStackTrace();
    } finally {
      map.put("remoteInet", remoteInet);
      map.put("useTime", time);
    }
    return map;
  }

  //todo 优化
  public static String getNetworkType(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isAvailable()) {
      int type = networkInfo.getType();
      if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
        return "ETHERNET";
      } else if (type == ConnectivityManager.TYPE_WIFI) {
        return "WIFI";
      } else {
        TelephonyManager telephonyManager = (TelephonyManager) context
            .getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getNetworkType()) {
          case NETWORK_TYPE_GPRS:
          case NETWORK_TYPE_EDGE:
          case NETWORK_TYPE_CDMA:
          case NETWORK_TYPE_1xRTT:
          case NETWORK_TYPE_IDEN:
            return "2G";
          case NETWORK_TYPE_UMTS:
          case NETWORK_TYPE_EVDO_0:
          case NETWORK_TYPE_EVDO_A:
          case NETWORK_TYPE_HSDPA:
          case NETWORK_TYPE_HSUPA:
          case NETWORK_TYPE_HSPA:
          case NETWORK_TYPE_EVDO_B:
          case NETWORK_TYPE_EHRPD:
          case NETWORK_TYPE_HSPAP:
            return "3G";
          case NETWORK_TYPE_LTE:
            return "4G";
          default:
            return "unkonw network";
        }
      }
    } else {
      return "no network";
    }
  }

  /**
   * Unknown network class
   */
  public static final int NETWORK_CLASS_UNKNOWN = 0;

  /**
   * wifi net work
   */
  public static final int NETWORK_WIFI = 1;

  /**
   * "2G" networks
   */
  public static final int NETWORK_CLASS_2_G = 2;

  /**
   * "3G" networks
   */
  public static final int NETWORK_CLASS_3_G = 3;

  /**
   * "4G" networks
   */
  public static final int NETWORK_CLASS_4_G = 4;

  public static int getNetWorkClass(Context context) {
    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

    switch (telephonyManager.getNetworkType()) {
      case TelephonyManager.NETWORK_TYPE_GPRS:
      case TelephonyManager.NETWORK_TYPE_EDGE:
      case TelephonyManager.NETWORK_TYPE_CDMA:
      case TelephonyManager.NETWORK_TYPE_1xRTT:
      case TelephonyManager.NETWORK_TYPE_IDEN:
        return NETWORK_CLASS_2_G;

      case TelephonyManager.NETWORK_TYPE_UMTS:
      case TelephonyManager.NETWORK_TYPE_EVDO_0:
      case TelephonyManager.NETWORK_TYPE_EVDO_A:
      case TelephonyManager.NETWORK_TYPE_HSDPA:
      case TelephonyManager.NETWORK_TYPE_HSUPA:
      case TelephonyManager.NETWORK_TYPE_HSPA:
      case TelephonyManager.NETWORK_TYPE_EVDO_B:
      case TelephonyManager.NETWORK_TYPE_EHRPD:
      case TelephonyManager.NETWORK_TYPE_HSPAP:
        return NETWORK_CLASS_3_G;

      case TelephonyManager.NETWORK_TYPE_LTE:
        return NETWORK_CLASS_4_G;

      default:
        return NETWORK_CLASS_UNKNOWN;
    }
  }

  public static DownloadResult download(OkHttpClient okHttpClient, String url, File file, File tmpFile) {
    long startPos = tmpFile.exists() ? tmpFile.length() : 0;
    Request request = new Request.Builder()
        .cacheControl(CacheControl.FORCE_NETWORK)
        .url(Utils.encodeString(url))
        .header("RANGE", "bytes=" + startPos + "-")
        .build();
    RandomAccessFile randomAccessFile = null;
    DownloadResult result = new DownloadResult();
    try {
      Response response = okHttpClient
          .newCall(request)
          .execute();
      if (response.isSuccessful()) {
        randomAccessFile = new RandomAccessFile(tmpFile, "rw");
        randomAccessFile.seek(startPos);
        InputStream is = response.body().byteStream();
        byte[] buffer = new byte[10 * 1024];
        int count;
        long total = response.body().contentLength();
        long startTime = System.currentTimeMillis();
        while ((count = is.read(buffer)) > 0) {
          randomAccessFile.write(buffer, 0, count);
          //current += count;
          //Logger.d("download: " + current + "/" + total);
        }
        long currentTime = System.currentTimeMillis();
        long period = (currentTime - startTime) < 1000 ? 1 : (currentTime - startTime) / 1000;
        result.duration = period;
        result.speed = total / period / 1024;
        result.host = response.request().url().host();
        result.originUrl = url;
        result.responseUrl = response.request().url().toString();
        result.eTag = response.header("Etag");
        result.contentType = response.header("Content-Type");
        result.contentLength = Long.parseLong(response.header("Content-Length"));
        if (file.exists()) {
          file.delete();
        }
        tmpFile.renameTo(file);
        is.close();
        result.success = true;
        return result;
      }
      if (response.body() != null) {
        response.body().close();
      }
    } catch (Exception e) {
      if (file.exists()) {
        file.delete();
      }
      Logger.e(e, "download error in catch case");
    } finally {
      if (randomAccessFile != null) {
        try {
          randomAccessFile.close();
        } catch (IOException e) {
          Logger.e(e, "download error in finally case");
        }
      }
    }
    return result;
  }

  public static boolean downloadFile(OkHttpClient okHttpClient, String url, File file) {
    Request request = new Request.Builder()
        .cacheControl(CacheControl.FORCE_NETWORK)
        .url(Utils.encodeString(url))
        .build();
    try {
      Response response = okHttpClient
          .newCall(request)
          .execute();
      if (response.isSuccessful()) {
        InputStream is = response.body().byteStream();
        File tmpFile = new File(file.getAbsolutePath() + "_tmp");
        FileUtil.inputStreamToFile(is, tmpFile);
        is.close();
        if (file.exists()) {
          file.delete();
        }
        return tmpFile.renameTo(file);
      }
      if (response.body() != null) {
        response.body().close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

}
