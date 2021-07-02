package com.namibox.commonlib.event;

/**
 * @Description 推送下来的webview对话框
 * @CreateTime: 2020/4/9 9:48
 * @Author: zhangkx
 */
public class ShowWebViewDialog {

  public String webViewUrl;

  public ShowWebViewDialog(String webViewUrl) {
    this.webViewUrl = webViewUrl;
  }
}
