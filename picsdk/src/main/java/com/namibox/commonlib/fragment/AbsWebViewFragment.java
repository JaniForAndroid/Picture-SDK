package com.namibox.commonlib.fragment;

import static android.webkit.WebView.enableSlowWholeDocumentDraw;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.FileChooserParams;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.chivox.EvalResult;
import com.example.picsdk.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.namibox.commonlib.activity.AbsFoundationActivity;
import com.namibox.commonlib.activity.AbsFunctionActivity;
import com.namibox.commonlib.model.CmdMenuControl;
import com.namibox.commonlib.view.CustomWebView;
import com.namibox.commonlib.view.OnScrollChangedCallback;
import com.namibox.tools.CommonConfig;
import com.namibox.tools.WebViewUtil;
import com.namibox.util.FileUtil;
import com.namibox.util.LanguageUtil;
import com.namibox.util.Logger;
import com.namibox.util.MD5Util;
import com.namibox.util.NetworkUtil;
import com.namibox.util.Utils;
import com.namibox.util.cache.DiskLruCache;
import com.namibox.util.network.NetWorkHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Create time: 2015/7/18.
 */
public class AbsWebViewFragment extends AbsFragment {

  private static final String TAG = "AbsWebViewFragment";
  private boolean debug = true;

  private DiskLruCache diskLruCache;
  protected CustomWebView customWebView;
  //private View loadingBg;
  private boolean errorReceived;
  protected String referer;
  protected String pageTitle;
  protected String subPageTitle;
  private long refreshTime;
  protected int maxOffset;
  protected boolean needCapture;
  private String htmlString;
  private boolean bridge;
  private static final int MSG_INIT_BRIDGE = 100;
  private static final int MSG_INIT_BRIDGE_FINAL = 101;

  @Override
  protected void setContentScrollListener() {
    customWebView.setOnScrollChangedCallback(new OnScrollChangedCallback() {
      @Override
      public void onScroll(int l, int t, int oldl, int oldt) {
        if (currentTopOffset == t) {
          return;
        }
        currentTopOffset = t;
        onContentScroll(t, oldt, maxOffset);
      }
    });
  }

  private boolean isSsl = true;
  private boolean syncCookie;

  public interface PageListener {

    void onPageFinished();

    void onPageLoadError();
  }

  private PageListener pageListener;

  public void setPageListener(PageListener pageListener) {
    this.pageListener = pageListener;
  }

  public interface ViewCreatedListener {

    void onViewCreated();
  }

  private ViewCreatedListener viewCreatedListener;

  public void setViewCreatedListener(ViewCreatedListener listener) {
    viewCreatedListener = listener;
  }

  private AbsFunctionActivity.DestroyListener destroyListener = new AbsFoundationActivity.DestroyListener() {
    @Override
    public void onDestroy() {
      if (customWebView != null) {
        customWebView.getSettings().setJavaScriptEnabled(false);
      }
    }
  };

  public AbsWebViewFragment() {
  }

  public void setSyncCookie(boolean syncCookie) {
    this.syncCookie = syncCookie;
  }

  public void setReferer(String referer) {
    this.referer = referer;
  }

  public void setNeedCapture(boolean needCapture) {
    this.needCapture = needCapture;
  }

  @Override
  public void onWebPop() {
    if (customWebView != null && videoLayout != null && videoScroll) {
      customWebView.scrollTo(0, videoHeight);
    }
  }

  @Override
  protected void scrollToOffset(int offset) {
    if (customWebView != null) {
      customWebView.scrollTo(0, offset);
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    activity.addDestroyListener(destroyListener);
  }

  @Override
  public void onDetach() {
    if (activity != null) {
      activity.removeDestroyListener(destroyListener);
    }
    super.onDetach();
  }

  @Override
  public void onPause() {
    super.onPause();
    debug = false;
    if (customWebView != null) {
      customWebView.onPause();
    }
    try {
      if (diskLruCache != null) {
        diskLruCache.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    debug = true;
    if (customWebView != null) {
      customWebView.onResume();
    }
  }

  @Override
  protected View createContentView(LayoutInflater inflater, ViewGroup container) {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//      WebView.enableSlowWholeDocumentDraw();
//    }
    customWebView = new CustomWebView(container.getContext());
    //html canvas截图需要关闭硬件加速
    if (needCapture) {
      customWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
    return customWebView;
  }

  @Override
  protected void initContentView() {
    if (viewCreatedListener != null) {
      viewCreatedListener.onViewCreated();
    }
    customWebView.setWebChromeClient(mWebChromeClient);
    customWebView.setWebViewClient(mWebViewClient);
    customWebView.setVerticalScrollBarEnabled(false);
    if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
      WebView.setWebContentsDebuggingEnabled(true);
    }
    setWebBackgroundColor();
    WebSettings ws = customWebView.getSettings();
    ws.setDomStorageEnabled(true);
    ws.setAppCacheEnabled(true);
    //ws.setAppCachePath(FileUtil.getFileCacheDir(mActivity).getAbsolutePath());
    ws.setAllowFileAccess(true);
    //ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
    ws.setUserAgentString(activity.getUserAgent());
    ws.setJavaScriptEnabled(true);
    ws.setGeolocationEnabled(true);
    //安全防护
    ws.setSavePassword(false);
    ws.setTextZoom(100);
    ws.setPluginState(WebSettings.PluginState.ON);
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
      CookieManager.getInstance().setAcceptThirdPartyCookies(customWebView, true);
    }
    if (syncCookie) {
      NetworkUtil.syncCookie(activity, getOriginUrl());
    }
  }

  protected void setWebBackgroundColor() {
    customWebView.setBackgroundColor(0x00000000);
  }

  @Override
  protected void setFragmentBackground(ViewGroup contentLayout) {
    contentLayout.setBackgroundResource(R.drawable.transparent);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      enableSlowWholeDocumentDraw();
    }
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    try {
      if (customWebView != null) {
        customWebView.stopLoading();
        //customWebView.loadUrl("about:blank");
        ViewParent parent = customWebView.getParent();
        if (parent != null) {
          ((ViewGroup) parent).removeView(customWebView);
        }
        customWebView.removeAllViews();
        customWebView.clearHistory();
        customWebView.destroy();
        customWebView = null;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    maxOffset = Utils.dp2px(activity, 200);
  }

  private String getLogTag() {
    return Utils.format("%s[%s]", TAG, getViewName());
  }

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.what == MSG_INIT_BRIDGE) {
      if (!bridge) {
        Logger.i(getLogTag(), "尝试建立bridge");
        loadJs("javascript:android_app_ready()");
        handler.sendEmptyMessageDelayed(MSG_INIT_BRIDGE, 2000);
      }
      return true;
    } else if (msg.what == MSG_INIT_BRIDGE_FINAL) {
      if (!bridge) {
        Logger.i(getLogTag(), "最终尝试建立bridge");
        loadJs("javascript:android_app_ready()");
      }
      return true;
    }
    return super.handleMessage(msg);
  }

  @Override
  public void appReadySucc() {
    bridge = true;
    Logger.i(getLogTag(), "bridge已建立");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    try {
      if (diskLruCache != null) {
        diskLruCache.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (customWebView != null) {
      customWebView.invalidate();
    }
  }

  @Override
  protected boolean onMenuClick(CmdMenuControl.Menu menu) {
    if (!super.onMenuClick(menu)) {
      appJS("{\"command\" : \"menuclicked\",\"id\" : " + menu.id + "}");
    }
    return true;
  }

  public String getTitle() {
    return customWebView.getTitle();
  }

  @Override
  public String getUrl() {
    return customWebView.getUrl();
  }

  @Override
  public void refresh(boolean original) {
    long time = System.currentTimeMillis();
    if (time - refreshTime < 1500) {
      hideRefresh();
      Logger.w("refresh time gap < 1500ms, just return");
      return;
    }
    refreshTime = time;
    if (customWebView != null) {
      //customWebView.setVisibility(errorReceived ? View.INVISIBLE : View.VISIBLE);
      hideErrorPage();
      //progressBar.setVisibility(View.GONE);

      String url = getUrl();
      if (original || TextUtils.isEmpty(url) || !url.startsWith("http") || url.startsWith("data")) {
        //url = webView.getOriginalUrl();
        url = getOriginUrl();
      }
      Logger.d(getLogTag(), "refresh: " + url);
      customWebView.stopLoading();
      doLoadData(url, true);
    }
  }

  @Override
  protected void showErrorPage() {
    onSetTitle("出错啦");
    super.showErrorPage();
  }

  public void cleanCache(boolean includeDiskFile) {
    customWebView.clearCache(includeDiskFile);
  }

  public void loadNewUrl(String url) {
    //useStatic = false;
    setOriginUrl(url);
    refresh(true);
  }

  @Override
  protected void doLoadData(String url, boolean forceNet) {
    errorReceived = false;
    if (!TextUtils.isEmpty(htmlString)) {
      customWebView.loadDataWithBaseURL(null, htmlString, "text/html", "utf-8", null);
    }
    Logger.d("load: " + url);
    if (TextUtils.isEmpty(url)) {
      Logger.e("load url is empty");
      return;
    }
    Map<String, String> extraHeaders = new HashMap<>();
    if (!TextUtils.isEmpty(referer)) {
      Logger.i("Referer: " + referer);
      extraHeaders.put("Referer", referer);
    }
    if (url.startsWith("file://")) {
      customWebView.loadUrl(url, extraHeaders);
      return;
    }
    HttpUrl httpUrl = HttpUrl.parse(url);
    //boolean ssl = true;
    if (httpUrl != null) {
      httpUrl = NetWorkHelper.getInstance().applySsl(httpUrl);
      if (httpUrl != null) {
        url = httpUrl.toString();
        //ssl = true;
      }
    }
    //NetworkUtil.syncCookies(mActivity, url);
    String decodeUrl = null;
    String noParamUrl = null;
    String param = "?appos=Android&appver=" + Utils.getVersionName(activity);
    try {
      decodeUrl = URLDecoder.decode(url, "utf-8");
      if (url.contains("ignore_query=yes")) {
        String[] s = url.split("\\?");
        if (s.length > 1) {
          noParamUrl = s[0];
          param += "&";
          param += s[1];
        }
        url = url.replace("&ignore_query=yes", "");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    File dir = FileUtil.getStaticAppPageDir(activity);
    String page = FileUtil.getPage(dir, url);
    if (!TextUtils.isEmpty(page)) {
      loadPage(isSsl, dir, page, param, extraHeaders);
      return;
    } else {
      page = FileUtil.getPage(dir, decodeUrl);
      if (!TextUtils.isEmpty(page)) {
        loadPage(isSsl, dir, page, param, extraHeaders);
        return;
      } else {
        page = FileUtil.getPage(dir, noParamUrl);
        if (!TextUtils.isEmpty(page)) {
          loadPage(isSsl, dir, page, param, extraHeaders);
          return;
        }
      }
    }
    File localDir = FileUtil.getLocalAppPageDir(activity);
    String localPage = FileUtil.getPage(localDir, url);
    if (!TextUtils.isEmpty(localPage)) {
      loadPage(isSsl, localDir, localPage, param, extraHeaders);
      return;
    } else {
      localPage = FileUtil.getPage(localDir, decodeUrl);
      if (!TextUtils.isEmpty(localPage)) {
        loadPage(isSsl, localDir, localPage, param, extraHeaders);
        return;
      } else {
        localPage = FileUtil.getPage(localDir, noParamUrl);
        if (!TextUtils.isEmpty(localPage)) {
          loadPage(isSsl, localDir, localPage, param, extraHeaders);
          return;
        }
      }
    }
    //customWebView.setCacheMode(Utils.isNetworkAvailable(mActivity) ?
    //        WebSettings.LOAD_DEFAULT : WebSettings.LOAD_NO_CACHE);
    customWebView.setCacheMode(WebSettings.LOAD_DEFAULT);
    if (NetworkUtil.isNetworkConnected(activity)) {
      url = Uri.parse(url)
          .buildUpon()
          .appendQueryParameter("nblang", LanguageUtil.getLanguage(activity))
          .build()
          .toString();
      customWebView.loadUrl(url, extraHeaders);
    } else {
      Logger.i("load: hideRefresh");
      hideRefresh(1000);
      showErrorPage();
      //progressLayout.setVisibility(View.GONE);
    }
  }

  public void setHtmlString(String htmlString) {
    this.htmlString = htmlString;
  }

  private void loadPage(boolean ssl, File dir, String page, String param,
      Map<String, String> extraHeaders) {
    Logger.i("loadPage: hideRefresh");
    hideRefresh(1000);
    File file = new File(dir, page + "/index.html");
    File ssl_file = new File(dir, page + "/index_ssl.html");
    customWebView.setCacheMode(WebSettings.LOAD_NO_CACHE);
    if (ssl && ssl_file.exists()) {
      Logger.d("loadPageSSL:" + ssl_file);
      customWebView.loadUrl("file://" + ssl_file.getAbsolutePath() + param, extraHeaders);
    } else {
      Logger.d("loadPage:" + file);
      customWebView.loadUrl("file://" + file.getAbsolutePath() + param, extraHeaders);
    }
  }

  public void loadJs(String js) {
    if (customWebView == null) {
      return;
    }
    try {
      customWebView.loadUrl(js);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void appJS(String message) {
    //if (debug) {
    Logger.d(getLogTag(), "app2js上报页面: " + message);
    //}
    if (message != null) {
      loadJs("javascript:app_js_hanler('" + message + "')");
    }
  }

  @Override
  protected void backControlClick(int chooseindex) {
    appJS("{\"command\" : \"backcontrol_callback\",\"chooseindex\" : " + chooseindex + "}");
  }

  @Override
  public void doSendMessage(String message) {
    appJS(message);
  }

  @Override
  public void requestShare(String type) {
    super.requestShare(type);
    appJS("{\"command\" : \"wxshare\"}");
  }

  private void doSendMessage(HashMap<String, Object> map) {
    Gson gson = new Gson();
    String s = gson.toJson(map);
    doSendMessage(s);
  }

  @Override
  public void shareResult(boolean success, String type) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("command", "nmshareresult");
    map.put("success", String.valueOf(success));
    map.put("type", type);
    doSendMessage(map);
  }

  public void setShareContent(String from, String imgUrl, String webpageUrl,
      String title, String titleFriend, String content) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("command", "nmshare");
    map.put("share_from", from);
    map.put("url_image", imgUrl);
    map.put("url_link", webpageUrl);
    map.put("share_content", content);
    map.put("share_title", title);
    map.put("share_friend", titleFriend);
    doSendMessage(map);
  }

  @Override
  public void postViewState(String state) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("command", "viewstate");
    map.put("state", state);
    doSendMessage(map);
  }

  @Override
  public void onAudioScoreResult(EvalResult result) {
    super.onAudioScoreResult(result);
    Gson gson = new Gson();
    String s = gson.toJson(result);
    appJS(s);
  }

  @Override
  public void onAudioScoreInitResult(boolean success) {
    super.onAudioScoreInitResult(success);
    HashMap<String, Object> msg = new HashMap<>();
    msg.put("command", "audioscoreresult");
    msg.put("result", success);
    doSendMessage(msg);
  }

  @Override
  public void onAudioScoreUploadResult(boolean success, String activityId) {
    super.onAudioScoreUploadResult(success, activityId);
    HashMap<String, Object> msg = new HashMap<>();
    msg.put("command", "audiouploadresult");
    msg.put("result", success);
    msg.put("activityid", activityId);
    doSendMessage(msg);
  }

  @Override
  public void notifyStatus(String url, String status) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("command", "audionotification");
    map.put("url", url);
    map.put("status", status);
    doSendMessage(map);
  }

  @Override
  public void notifyReply(JsonObject replyMsg, String replyContent) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("command", "sendreply");
    map.put("reply_msg", replyMsg);
    map.put("reply_content", replyContent);
    doSendMessage(map);
  }

  @Override
  public void notifyPlayUpdate(long current, String url) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("command", "audionotification");
    map.put("url", url);
    map.put("status", "timeupdate");
    map.put("current_time", current / 1000f);
    doSendMessage(map);
  }

  @Override
  public void notifyPlayControl(int index, boolean forward) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("command", "playcontrol");
    map.put("index", index);
    map.put("direction", forward ? "forward" : "reward");
    doSendMessage(map);
  }

  @Override
  public void notifyInterrupt(int dataId, int[] size) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("command", "showview");
    map.put("data_id", dataId);
    map.put("size", size);
    doSendMessage(map);
  }

  @Override
  public void notifyInterruptHide() {
    HashMap<String, Object> map = new HashMap<>();
    map.put("command", "hideviewtoweb");
    doSendMessage(map);
  }

  @Override
  public void postVideoPlay(String url, String type, String duration, boolean result) {
    Logger.d(getLogTag(), "duration=" + duration + ",result=" + result + ",url=" + url);
    HashMap<String, Object> msg = new HashMap<>();
    msg.put("command", "postvideoplay");
    msg.put("playurl", url);
    msg.put("type", type);
    msg.put("duration", duration);
    msg.put("result", result);
    doSendMessage(msg);
  }

  @Override
  public void postVideoComplete(String url, String type, String duration) {
    Logger.d(getLogTag(), "duration=" + duration + ",url=" + url);
    HashMap<String, Object> msg = new HashMap<>();
    msg.put("command", "videoplayend");
    msg.put("playurl", url);
    msg.put("type", type);
    msg.put("duration", duration);
    doSendMessage(msg);
  }

  @Override
  public void payResult(String type, String return_code, String description, String return_url,
      String order_info) {
    HashMap<String, Object> msg = new HashMap<>();
    msg.put("command", type);
    msg.put("return_code", return_code);
    msg.put("description", description);
    msg.put("return_url", return_url);
    msg.put("order_info", order_info);
    doSendMessage(msg);
  }

  private WebChromeClient mWebChromeClient = new WebChromeClient() {

    @Override
    public boolean onConsoleMessage(@NonNull ConsoleMessage consoleMessage) {
      return super.onConsoleMessage(consoleMessage);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
        JsPromptResult result) {
      if (debug) {
        if (!TextUtils.isEmpty(message) && message.length() > 2048) {
          Logger.d(getLogTag(), "收到页面消息，总长度: " + message.length());
        } else {
          Logger.d(getLogTag(), "收到页面消息: " + message);
        }
      }
      if (activity == null || activity.isFinishing()) {
        result.confirm("{}");
        return true;
      }
      String s = handleJsonMessage(message);
      result.confirm(s);
      if (debug) {
        Logger.d("result confirm");
      }
      return true;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
      result.confirm();
      Logger.d("onJsAlert:" + message);
      if (activity != null) {
        activity.toast(message);
      }
      return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
      return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin,
        GeolocationPermissions.Callback callback) {
      super.onGeolocationPermissionsShowPrompt(origin, callback);
      if (debug) {
        Logger.d(getLogTag(), "onGeolocationPermissionsShowPrompt: " + origin);
      }
      callback.invoke(origin, true, false);
    }

    @Override
    public void onPermissionRequest(PermissionRequest request) {
      if (debug) {
        Logger.d(getLogTag(), "onPermissionRequest: " + request);
      }
      super.onPermissionRequest(request);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback,
        FileChooserParams fileChooserParams) {
      filePathCallback = callback;
      Intent intent = fileChooserParams.createIntent();
      if (intent.getType() != null && intent.getType().startsWith("image")) {
        intent.setType("image/*");
      }
      try {
        startActivityForResult(intent, 1999);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return true;
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
      if (debug) {
        Logger.d(getLogTag(), "onReceivedTitle: " + title);
      }
      tv_loading.setVisibility(View.GONE);
      pageTitle = title == null || title.equals(getOriginUrl()) ? "" : title;
    }
  };

  ValueCallback<Uri[]> filePathCallback;

  @TargetApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 1999) {
      Uri[] result = FileChooserParams.parseResult(resultCode, data);
      filePathCallback.onReceiveValue(result);
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private static final int MAX_SIZE = 100 * 1024 * 1024;
  private static final String DISK_LRU_CACHE = "disk_lru_cache";

  private DiskLruCache getDiskLruCache() {
    if (diskLruCache == null) {
      try {
        File cacheDir = new File(FileUtil.getFileCacheDir(activity), DISK_LRU_CACHE);
        diskLruCache = DiskLruCache.open(cacheDir, 1, 1, MAX_SIZE);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return diskLruCache;
  }

  private void addCache(final String url) {
    if (debug) {
      Logger.i(getLogTag(), "add cache: " + url);
    }
    OkHttpClient okHttpClient = activity.getOkHttpClient();
    Request request = new Request.Builder()
        .url(Utils.encodeString(url))
        .build();
    okHttpClient.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        e.printStackTrace();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful() && activity != null && isAdded()) {
          InputStream is = response.body().byteStream();
          String key = MD5Util.md5(url);
          try {
            DiskLruCache diskLruCache = getDiskLruCache();
            if (!diskLruCache.isClosed()) {
              if (debug) {
                Logger.i(getLogTag(), "save cache: " + url);
              }
              DiskLruCache.Editor editor = diskLruCache.edit(key);
              if (editor != null) {
                OutputStream os = editor.newOutputStream(0);
                FileUtil.inputStreamToOutputStream(is, os);
                os.close();
                editor.commit();
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            is.close();
          }
        }
      }
    });
  }

  private InputStream readCache(String url) {
    if (debug) {
      Logger.i(getLogTag(), "try read cache: " + url);
    }
    String key = MD5Util.md5(url);
    try {
      DiskLruCache diskLruCache = getDiskLruCache();
      if (!diskLruCache.isClosed()) {
        DiskLruCache.Snapshot snapShot = diskLruCache.get(key);
        if (snapShot != null) {
          if (debug) {
            Logger.i(getLogTag(), "cache found: " + url);
          }
          return snapShot.getInputStream(0);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (activity != null) {
      addCache(url);
    }
    return null;
  }

  private WebViewClient mWebViewClient = new WebViewClient() {

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);
      //v.setVisibility(View.VISIBLE);
      //customWebView.setCacheMode(NetUtil.isNetworkConnected(mActivity) ? WebSettings.LOAD_DEFAULT : WebSettings.LOAD_CACHE_ONLY);
      //int cacheMode = customWebView.getSettings().getCacheMode();
      Logger.d(getLogTag(), "onPageStarted: " + url);
      //progressBar.setVisibility(View.VISIBLE);
      //onBackShowControl(true);
      //onStatusBarControl(false);
      onSetMenu(null, null);
      onSetTitle(getString(R.string.common_loading));
      tv_loading.setVisibility(View.VISIBLE);
      startLoadGif();
      bridge = false;
      handler.removeMessages(MSG_INIT_BRIDGE);
      handler.sendEmptyMessageDelayed(MSG_INIT_BRIDGE, 1000);
    }

    @Override
    public void onPageFinished(WebView view, final String url) {
      super.onPageFinished(view, url);
      Logger.d(getLogTag(), "onPageFinished: " + url);
      tv_loading.setVisibility(View.GONE);
      stopLoadGif();
      //progressBar.setVisibility(View.GONE);
      //loadingBg.setVisibility(View.GONE);
      if (customWebView == null) {
        return;
      }
      Logger.i(getLogTag(), "onPageFinished: hideRefresh");
      hideRefresh();
      customWebView.setVisibility(errorReceived ? View.INVISIBLE : View.VISIBLE);
      //errorPage.setVisibility(errorReceived ? View.VISIBLE : View.GONE);
      if (url.startsWith("data")) {
        Logger.w(getLogTag(), "error page");
        showErrorPage();
      } else if (!url.startsWith("http") && !url.startsWith("file")) {
        Logger.w(getLogTag(), "not http or file, just return");
      } else {
        onSetTitle(!TextUtils.isEmpty(pageTitle) && !TextUtils.isEmpty(url)
            && Utils.encodeString(url).equals(Utils.encodeString(pageTitle)) ? "" : pageTitle);
        //CookieManager cookieManager = CookieManager.getInstance();
        //String cookieStr = cookieManager.getCookie(originUrl);
        //Logger.i(TAG, originUrl + ", cookieStr: " + cookieStr);
        //if (mActivity != null && !TextUtils.isEmpty(cookieStr))
        //    Utils.saveCookie(mActivity, url, cookieStr);
        handler.removeMessages(MSG_INIT_BRIDGE);
        handler.sendEmptyMessageDelayed(MSG_INIT_BRIDGE_FINAL, 0);
        if (pageListener != null) {
          pageListener.onPageFinished();
        }
      }

      if (contentHeightCallBack != null) {
        contentHeightCallBack
            .contentHeight((int) (customWebView.getContentHeight() * customWebView.getScale()));
        customWebView.invalidate();
        customWebView.postDelayed(new Runnable() {
          @Override
          public void run() {
            if (customWebView != null) {
              contentHeightCallBack.contentHeight(
                  (int) (customWebView.getContentHeight() * customWebView.getScale()));
            }
          }
        }, 500);
      }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description,
        String failingUrl) {
      Logger.e(getLogTag(),
          "onReceivedError: " + errorCode + ", description: " + description + ", failingUrl: "
              + failingUrl);
      if (customWebView == null) {
        return;
      }
      if (pageListener != null) {
        pageListener.onPageLoadError();
      }
      errorReceived = true;
      customWebView.setVisibility(View.GONE);
      Logger.i(getLogTag(), "onReceivedError: hideRefresh");
      hideRefresh();
      showErrorPage();
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
      Logger.e(getLogTag(), "onReceivedSslError: " + error);
      //super.onReceivedSslError(view, handler, error);
      handler.proceed();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      if (debug) {
        Logger.d(getLogTag(), "shouldOverrideUrlLoading: " + url);
      }
      if (activity == null) {
        return false;
      }
      if (url.startsWith("http")) {
        String[] result = WebViewUtil.parseParameter(url);
        if (CommonConfig.USE_NATIVE_LOGIN && "/auth/loginpage".equals(result[4])) {
          WebViewUtil.openLoginView(result[3]);
          onCloseView(null);
          return true;
        }
      } else if (url.startsWith("file")) {

      } else {
        try {
          Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setData(Uri.parse(url));
          startActivity(intent);
          return true;
        } catch (ActivityNotFoundException e) {
          e.printStackTrace();
          activity.toast(getString(R.string.cant_start));
        }
      }
      return false;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
      super.onLoadResource(view, url);
      //Logger.d(TAG, "onLoadResource: " + url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
      WebResourceResponse response = interceptRequest(url, null);
      if (response != null) {
        return response;
      }
      return super.shouldInterceptRequest(view, url);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
      if (request != null && request.getUrl() != null && request.getMethod()
          .equalsIgnoreCase("get")) {
        String url = request.getUrl().toString();
        Map<String, String> header = request.getRequestHeaders();
        WebResourceResponse response = interceptRequest(url, header);
        if (response != null) {
          return response;
        }
      }
      return super.shouldInterceptRequest(view, request);
    }

    static final String ENCODING = "UTF-8";
    static final String MIMETYPE_CSS = "text/css";
    static final String MIMETYPE_ALL = "*/*";

    private WebResourceResponse interceptRequest(String url, Map<String, String> headers) {
//      if (debug) {
//        Logger.d(TAG, "interceptRequest: " + url);
//      }
      if (activity == null) {
        return null;
      }
      if (!url.startsWith("http") || url.length() > 1000) {
        //don't intercept
        return null;
      }
      String mimeType;
      if (url.endsWith(".css")) {
        mimeType = MIMETYPE_CSS;
      } else {
        mimeType = MIMETYPE_ALL;
      }
      try {
        URL u = new URL(url);
        String host = u.getHost();
        String path = URLDecoder.decode(u.getPath(), ENCODING);
        File local_dir = new File(FileUtil.getLocalAppCacheDir(activity), host);
        File localfile = new File(local_dir, path);
        if (localfile.exists()) {
          if (debug) {
            Logger.d(getLogTag(), "use local cache: " + localfile);
          }
          InputStream is = new FileInputStream(localfile);
          return new WebResourceResponse(mimeType, ENCODING, is);
        }
        File dir = new File(FileUtil.getStaticAppCacheDir(activity), host);
        File file = new File(dir, path);
        if (file.exists()) {
          if (debug) {
            Logger.d(getLogTag(), "use download cache: " + file);
          }
          InputStream is = new FileInputStream(file);
          return new WebResourceResponse(mimeType, ENCODING, is);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (url.contains("cache=true")) {
        InputStream is = readCache(url);
        if (is != null) {
          return new WebResourceResponse(mimeType, ENCODING, is);
        }
      }
      return null;
    }
  };

  @Override
  public WebView getWebView() {
    return customWebView;
  }

  public void setSubPageTitle(String subPageTitle) {
    this.subPageTitle = subPageTitle;
  }

  public String getAudioName() {
    if (!TextUtils.isEmpty(subPageTitle)) {
      return subPageTitle;
    } else {
      return pageTitle;
    }
  }

  private ContentHeightCallBack contentHeightCallBack;

  public void setContentHeightCallBack(ContentHeightCallBack contentHeightCallBack) {
    this.contentHeightCallBack = contentHeightCallBack;
  }

  public interface ContentHeightCallBack {

    void contentHeight(int height);
  }
}
