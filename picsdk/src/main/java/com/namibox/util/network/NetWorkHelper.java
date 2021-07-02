package com.namibox.util.network;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import com.namibox.util.DeviceUuidFactory;
import com.namibox.util.LanguageUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.PreferenceUtil;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by sunha on 2017/6/1 0001.
 */

public class NetWorkHelper {

  private static final int OKHTTP_CACHE_SIZE = 150 * 1024 * 1024;
  private static final String OKHTTP_CACHE_DIR = "okhttp_cache";
  private static NetWorkHelper instance;
  private HttpDns httpDns;
  private String ua;
  private String deviceid;
  private String domain;
  private List<String> domain_https;
  private Context context;
  private String baseUrl;

  private final static String[] default_domains = {
      "vsra.namibox.com", "ra.namibox.com", "r.namibox.com", "i.namibox.com", "f.namibox.com",
      "dev.namibox.com",
      "u.namibox.com", "wu.namibox.com", "of.namibox.com", "wr.namibox.com", "cr.namibox.com",
      "owu.namibox.com",
      "wweb.namibox.com", "owf.namibox.com", "game.namibox.com", "v.namibox.com", "ou.namibox.com",
      "x.namibox.com",
      "w.namibox.com", "namibox.com", "wf.namibox.com", "itunes.namibox.com", "main.namibox.com",
      "vf.namibox.com",
      "c.namibox.com", "150.namibox.com", "ali.namibox.com", "tencent.namibox.com",
      "wangsu.namibox.com"
  };


  private NetWorkHelper() {
  }

  public void init(Context context, String dnsAccountId, String ua, String domain, String baseUrl) {
    instance.ua = ua;
    instance.deviceid = new DeviceUuidFactory(context).getDeviceUuid().toString();
    instance.domain = domain;
    instance.baseUrl = baseUrl;
    instance.context = context.getApplicationContext();
    instance.httpDns = HttpDns.getInstance();
    instance.httpDns.init(dnsAccountId, ua);
    instance.domain_https = Arrays.asList(default_domains);
  }

  public static NetWorkHelper getInstance() {
    if (instance == null) {
      synchronized (NetWorkHelper.class) {
        if (instance == null) {
          instance = new NetWorkHelper();
        }
      }
    }
    return instance;
  }

  public static OkHttpClient.Builder getOkHttpBuilder() {
    return getInstance().getOkHttpBuilder(true, true);
  }


  public static OkHttpClient getOkHttpClient() {
    return getOkHttpBuilder().build();
  }

  public static OkHttpClient getOkHttpClient(
      final ProgressResponseBody.ProgressListener progressListener) {
    return getOkHttpBuilder()
        .addNetworkInterceptor(new Interceptor() {
          @Override
          public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                .build();
          }
        })
        .build();
  }

  public HttpDns getHttpDns() {
    return httpDns;
  }

  public String getUa() {
    return ua;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getDeviceid() {
    return deviceid;
  }

  public void updateHttpsDomains(List<String> domain_https) {
    if (domain_https == null || domain_https.isEmpty()) {
      return;
    }
    for (String domain : domain_https) {
      if (!domain_https.contains(domain)) {
        Logger.d("add domain: " + domain);
        this.domain_https.add(domain);
      }
    }
  }

  public OkHttpClient.Builder getOkHttpBuilder(boolean ssl, boolean retry) {
    return getOkHttpBuilder(ssl, retry, new File(context.getCacheDir(), OKHTTP_CACHE_DIR));
  }

  public OkHttpClient.Builder getOkHttpBuilder(final boolean ssl, final boolean retry,
      File cacheDir) {
    Cache cache = new Cache(cacheDir, OKHTTP_CACHE_SIZE);
    OkHttpClient.Builder builder = new OkHttpClient.Builder()
        .connectTimeout(10000, TimeUnit.MILLISECONDS)
        .readTimeout(15000, TimeUnit.MILLISECONDS)
        .writeTimeout(15000, TimeUnit.MILLISECONDS)
        //.sslSocketFactory(sslSocketFactory, trustManager)
        .cache(cache)
        .addInterceptor(new Interceptor() {
          @Override
          public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            // Customize the request
            Request.Builder builder = original.newBuilder()
                .header("User-Agent", ua)
                .header("deviceid", deviceid)
                .header("nbtz", TimeZone.getDefault().getID())
                .header("nblang", LanguageUtil.getLanguage(context))
                .url(ssl ? applySsl(original.url()) : original.url())
                .method(original.method(), original.body());
            if (Build.VERSION.SDK_INT < 19) {
              @SuppressWarnings("unused")
              CookieSyncManager cs = CookieSyncManager.createInstance(context);
            }
            String cookieStr = null;
            try {
              CookieManager cookieManager = CookieManager.getInstance();
              cookieStr = cookieManager.getCookie(original.url().toString());
            } catch (Exception e) {
              e.printStackTrace();
            }
            if (!TextUtils.isEmpty(cookieStr)) {
              builder.header("Cookie", cookieStr);
            }
            Request request = builder.build();
            Logger.i(">>>>>>>>url: " + request.url().toString());
            Logger.i(">>>>>cookie: " + cookieStr);
            String requestUrl = request.url().toString();

            // try the request
            Response response = chain.proceed(request);
            if (retry && original.method().equals("GET")) {
              int tryCount = 0;
              int maxLimit = 1;

              while (!response.isSuccessful() && tryCount < maxLimit) {
                Logger.e("Request failed, retry " + tryCount + ", code=" + response.code()
                    + ", message=" + response.message());
                tryCount++;
                // retry the request
                response = chain.proceed(request);
              }
            }

            // otherwise just pass the original response on
            Logger.i(">>>>>>>>resp: " + response);
            if (response.header("set-cookie") != null) {
              Logger.e(">>>>>>>>set-cookie: " + response.header("set-cookie"));
              NetworkUtil.syncCookie(context, requestUrl, response.header("set-cookie"));
            }
            return response;
          }
        })
        .dns(new Dns() {
          @Override
          public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            if (hostname == null) {
              throw new UnknownHostException("hostname == null");
            }
            if (PreferenceUtil.isHttpDnsEnable(context) && !hostname.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
              List<String> ips = httpDns.getIpsByHostAsync(hostname);
              if (ips != null && !ips.isEmpty()) {
                List<InetAddress> result = new ArrayList<>();
                Logger.d(hostname + " ---httpdns--->");
                for (String ip : ips) {
                  result.add(InetAddress.getByName(ip));
                  Logger.d("address: " + ip);
                }
                return result;
                //return Arrays.asList(InetAddress.getAllByName(ip));
              }
            }
            Logger.d(hostname + " ---localdns---> ");
            List<InetAddress> result = Dns.SYSTEM.lookup(hostname);
            if (result != null) {
              for (InetAddress address : result) {
                Logger.d("address: " + address.getHostAddress());
              }
            }
            return result;
          }
//                })
//                .cookieJar(new CookieJar() {
//                    @Override
//                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
//                        for (Cookie cookie : cookies) {
//                            Logger.d("saveFromResponse: " + url + "\ncookie:" + cookie.toString());
//                            getPersistentCookieStore(context).add(url, cookie);
//                        }
//                    }
//
//                    @Override
//                    public List<Cookie> loadForRequest(HttpUrl url) {
//                        Logger.d("loadForRequest: " + url);
//                        CookieManager cookieManager = CookieManager.getInstance();
//                        String cookieStr = cookieManager.getCookie(url.toString());
//                        Logger.i("cookieStr: " + cookieStr);
//                        List<Cookie> cookies = new ArrayList<>();
//                        List<Cookie> persistentCookies = NetworkUtil.getPersistentCookieStore(context).getCookies(url);
//                        boolean needAddSessionid = url.host().endsWith(domain);
//                        if (needAddSessionid) {
//                            String sessionId = Utils.getSessionId(context);
//                            if (!TextUtils.isEmpty(sessionId)) {
//                                Cookie cookie = new Cookie.Builder()
//                                        .name("sessionid")
//                                        .value(sessionId)
//                                        .domain(domain)
//                                        .build();
//                                cookies.add(cookie);
//                                Logger.w("login, add loginstatus cookie: " + cookie);
//                            }
//                        }
//                        if (cookies.isEmpty()) {
//                            cookies.addAll(persistentCookies);
//                        } else {
//                            for (Cookie cookie : persistentCookies) {
//                                if (!cookie.name().equals("sessionid")) {
//                                    cookies.add(cookie);
//                                }
//                            }
//                        }
//                        return cookies;
//                    }
        });
    if (PreferenceUtil.isStethoEnable(context)) {
      //builder.addInterceptor(new MokeInterceptor());
      //builder.addNetworkInterceptor(new StethoInterceptor());
      //通过反射减少baseutil依赖
      Interceptor stetho = getStethoInterceptor();
      if (stetho != null) {
        builder.addNetworkInterceptor(stetho);
      }
    }
    if (PreferenceUtil.isSSLEnable(context)) {
      X509TrustManager trustManager;
      SSLSocketFactory sslSocketFactory;
      try {
        trustManager = new UnSafeTrustManager();
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{trustManager}, null);
        sslSocketFactory = sslContext.getSocketFactory();
      } catch (GeneralSecurityException e) {
        throw new RuntimeException(e);
      }
      builder.sslSocketFactory(sslSocketFactory, trustManager);
    }
    return builder;
  }

  static Interceptor getStethoInterceptor() {
    try {
      Class<?> clazz = Class.forName("com.facebook.stetho.okhttp3.StethoInterceptor");
      Interceptor stetho = (Interceptor) clazz.newInstance();
      return stetho;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public HttpUrl applySsl(HttpUrl httpUrl) {
    if (httpUrl != null && httpUrl.scheme().equals("http")) {
      for (String domain : domain_https) {
        if (httpUrl.host().equals(domain)) {
          Logger.w("apply https to: " + domain);
          return httpUrl.newBuilder().scheme("https").build();
        }
      }
    }
    return httpUrl;
  }

  private static class UnSafeTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      Logger.d("checkClientTrusted: " + authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      Logger.d("checkServerTrusted: " + authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[]{};
    }
  }

}
