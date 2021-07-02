package com.namibox.commonlib.jsbridge;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.WebView;
import com.google.gson.JsonObject;
import com.namibox.util.Logger;
import java.lang.ref.WeakReference;

/**
 * Create time: 2020/4/10.
 */
public class JSCallback {
  private static Handler mHandler = new Handler(Looper.getMainLooper());
  private static final String CALLBACK_JS_FORMAT = "javascript:app_js_hanler('%s');";
  private WeakReference<WebView> mWebViewRef;
  private String mPort;

  public JSCallback(WebView view, String port) {
    mWebViewRef = new WeakReference<>(view);
    mPort = port;
  }

  public void apply(JsonObject jsonObject) {
    if (jsonObject == null) {
      return;
    }
    if (!TextUtils.isEmpty(mPort)) {
      jsonObject.addProperty("command", mPort);
    }
    final String execJs = String.format(CALLBACK_JS_FORMAT, jsonObject.toString());
    Logger.d("js_callback:" + execJs);
    if (mWebViewRef != null && mWebViewRef.get() != null) {
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          mWebViewRef.get().loadUrl(execJs);
        }
      });
    }

  }
}
