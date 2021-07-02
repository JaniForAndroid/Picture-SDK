package com.namibox.commonlib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class CustomWebView extends WebView {

  private OnScrollChangedCallback mOnScrollChangedCallback;

  public CustomWebView(final Context context) {
    super(context);
  }

  public CustomWebView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomWebView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    //getSettings().setJavaScriptEnabled(true);
    //安全防护 需明确移除的接口
    removeJavascriptInterface("searchBoxJavaBridge_");
    removeJavascriptInterface("accessibility");
    removeJavascriptInterface("accessibilityTraversal");
  }

  @Override
  protected void onDetachedFromWindow() {
    ///getSettings().setJavaScriptEnabled(false);
    super.onDetachedFromWindow();
  }

  public void setCacheMode(int cacheMode) {
    WebSettings ws1 = getSettings();
    if (ws1 != null) {
      ws1.setCacheMode(cacheMode);
    }
  }

  @Override
  protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);
    if (mOnScrollChangedCallback != null) {
      mOnScrollChangedCallback.onScroll(l, t, oldl, oldt);
    }
  }

  public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback) {
    mOnScrollChangedCallback = onScrollChangedCallback;
  }

}
