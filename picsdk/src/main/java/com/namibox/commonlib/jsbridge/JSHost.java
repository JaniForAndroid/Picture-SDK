package com.namibox.commonlib.jsbridge;

import android.webkit.WebView;

/**
 * Create time: 2020/4/10.
 */
public interface JSHost<T> {
  WebView getWebView();
  T getJsHost();
}
