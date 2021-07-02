package com.namibox.util.network;

import com.namibox.util.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Create time: 2016/12/21.
 */

public class HttpDns {

  private static HttpDns instance;

  private HttpDns() {
  }

  public void init(String account, String ua) {
    instance.account = account;
    instance.ua = ua;
  }

  public static HttpDns getInstance() {
    if (instance == null) {
      synchronized (HttpDns.class) {
        if (instance == null) {
          instance = new HttpDns();
        }
      }
    }
    return instance;
  }

  private String account;
  private String ua = "namibox";
  private OkHttpClient okHttpClient;
  private ConcurrentMap<String, HostObject> hostCache = new ConcurrentHashMap<>();
  private ConcurrentMap<String, Future<List<String>>> futures = new ConcurrentHashMap<>();
  private ExecutorService pool = Executors.newSingleThreadExecutor();
  private static final int EMPTY_RESULT_HOST_TTL = 30;
  private static final int MAX_HOLD_HOST_NUM = 100;

  public synchronized List<String> getIpsByHostAsync(String hostName) {
    HostObject host = hostCache.get(hostName);
    if (host == null || host.isExpired()) {
      Future<List<String>> future = futures.get(hostName);
      if (future != null && !future.isDone()) {
        Logger.d("[getIpByHostAsync] - future is doing, host: " + hostName);
        return null;
      }
      Logger.d("[getIpByHostAsync] - fetch result from network, host: " + hostName);
      future = pool.submit(new HostTask(hostName));
      futures.put(hostName, future);
      return null;
    }
    Logger.d("[getIpByHostAsync] - fetch result from cache, host: " + hostName);
    return host.getIps();
  }

  public void cleanCache() {
    hostCache.clear();
  }

  private class HostTask implements Callable<List<String>> {

    String hostName;

    HostTask(String hostName) {
      this.hostName = hostName;
    }

    @Override
    public List<String> call() throws Exception {
      HttpUrl httpUrl = new HttpUrl.Builder()
          .scheme("https")
          .host("203.107.1.1")
          .addPathSegment(account)
          .addPathSegment("d")
          .addQueryParameter("host", hostName)
          .build();
      Request request = new Request.Builder()
          .url(httpUrl)
          .header("User-Agent", ua)
          .get()
          .build();
      if (okHttpClient == null) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(10000, TimeUnit.MILLISECONDS)
            .readTimeout(10000, TimeUnit.MILLISECONDS);
        Interceptor stetho = NetWorkHelper.getStethoInterceptor();
        if (stetho != null) {
          builder.addNetworkInterceptor(stetho);
        }
        okHttpClient= builder.build();
      }
      try {
        String s = okHttpClient.newCall(request).execute().body().string();
        Logger.d("host result: " + s);
        JSONObject json = new JSONObject(s);
        String host = json.getString("host");
        int ttl = json.getInt("ttl");
        JSONArray ips = json.getJSONArray("ips");
        if (host != null && host.equals(hostName)) {
          if (ttl == 0 && (ips == null || ips.length() == 0)) {
            // 如果有结果返回，但是ip列表为空，ttl也为空，那默认没有ip就是解析结果，并设置ttl为一个较长的时间
            // 避免一直请求同一个ip冲击sever
            ttl = EMPTY_RESULT_HOST_TTL;
          }
          List<String> list = jsonArray2List(ips);
          HostObject hostObject = new HostObject();
          hostObject.setHostName(host);
          hostObject.setTtl(ttl);
          hostObject.setIps(list);
          hostObject.setQueryTime(System.currentTimeMillis());
          if (hostCache.size() < MAX_HOLD_HOST_NUM) {
            hostCache.put(hostName, hostObject);
          }
          return list;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }
  }

  private static List<String> jsonArray2List(JSONArray jsonArray) {
    if (jsonArray == null || jsonArray.length() == 0) {
      return null;
    }
    List<String> list = new ArrayList<>();
    try {
      for (int i = 0; i < jsonArray.length(); i++) {
        list.add(jsonArray.getString(i));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }

  private class HostObject {

    @Override
    public String toString() {
      return "HostObject [hostName=" + hostName + ", ips=" + ips + ", ttl=" + ttl + ", queryTime="
          + queryTime + "]";
    }

    void setHostName(String hostName) {
      this.hostName = hostName;
    }

    void setIps(List<String> ips) {
      this.ips = ips;
    }

    void setTtl(int ttl) {
      this.ttl = ttl;
    }

    void setQueryTime(long queryTime) {
      this.queryTime = queryTime;
    }

    public String getHostName() {
      return hostName;
    }

    public List<String> getIps() {
      return ips;
    }

    public int getTtl() {
      return ttl;
    }

    public long getQueryTime() {
      return queryTime;
    }

    String hostName;
    List<String> ips;
    int ttl;
    long queryTime;

    boolean isExpired() {
      return queryTime + (ttl - 3) * 1000 < System.currentTimeMillis();
    }
  }
}
