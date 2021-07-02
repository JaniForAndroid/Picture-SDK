package com.namibox.tools;

import android.content.Context;
import android.text.TextUtils;
import com.namibox.util.Logger;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.network.NetWorkHelper;
import okhttp3.HttpUrl;

/**
 * Created by sunha on 2017/6/6 0006.
 */

public class WebViewUtil {

  public static final String PAY_RESULT_ACTION = "namibox.action.pay_result";

  //from BaseWebViewActivity
  public static final String ARG_TEMPLATE = "template";
  public static final String ARG_TEMPLATES = "templates";
  public static final String ARG_URLS = "urls";
  public static final String ARG_VIEW_NAMES = "view_names";
  public static final String ARG_TITLES = "titles";
  public static final String ARG_SELECT_INDEX = "select_index";
  public static final String ARG_NEED_CAPTURE = "need_capture";
  public static final String ARG_ORIENTATION = "orientation";

  //from AbsWebViewActivity
  public static final String ARG_KEEP_LIGHT = "keep_light";
  public static final String ARG_LIGHTNESS = "lightness";
  public static final String ARG_URL = "url";
  public static final String ARG_VIEW_NAME = "view_name";
  public static final String ARG_PARENT_VIEW_NAME = "parent_view_name";
  public static final String ARG_REFERER = "referer";
  public static final String ARG_BOOK_ID = "bookId";

  //from VideoWebViewActivity
  public static final String ARG_TEMPLATE_RATIO = "template_ratio";

  public static void openView(String url) {
    openView(url, null);
  }

  public static void openView(String url, String viewName) {
    if (TextUtils.isEmpty(url)) {
      return;
    }
    openView(url, null, 0, viewName, null, null, 0, 0);
  }

  public static void openView(String url, String template, float template_ratio) {
    openView(url, template, template_ratio, null, null, null, 0, 0);
  }

  public static void openLoginView() {
    openLoginView(null);
  }

  public static void openLoginView(String redirect) {
    if (CommonConfig.USE_NATIVE_LOGIN) {
      if (redirect != null && redirect.startsWith("/")) {
        redirect = NetWorkHelper.getInstance().getBaseUrl() + redirect;
      }
//      ARouter.getInstance().build("/namibox/SYOnkeyLogin")
//          .withString("redirect", redirect)
//          .navigation();
    } else {
      openView(NetWorkHelper.getInstance().getBaseUrl() + "/auth/loginpage");
    }
  }

  public static void openChangePwd(Context context) {
    String phonenum = PreferenceUtil.getSharePref(context, "user_phone", "");
//    ARouter.getInstance().build("/namibox/openLogin")
//        .withString("phone", phonenum)
//        .withInt("state", 1)
//        .navigation();
  }

  public static void openBindPhone(Context context) {
    String phonenum = PreferenceUtil.getSharePref(context, "user_phone", "");
//    ARouter.getInstance().build("/namibox/openLogin")
//        .withString("phone", phonenum)
//        .withInt("state", 2)
//        .navigation();
  }

  public static void openChangePhone(Context context) {
    long loginTime = PreferenceUtil.getSharePref(context, PreferenceUtil.PREF_LOGIN_TIME, 0L);
//    if (System.currentTimeMillis() - loginTime <= 15 * 24 * 60 * 60 * 1000L) {
//      ARouter.getInstance().build("/namibox/openLogin")
//          .withInt("state", 3)
//          .navigation();
//    } else {
//      ARouter.getInstance().build("/namibox/changePhone")
//          .navigation();
//    }
  }

  public static void openMain() {
//    ARouter.getInstance().build("/namibox/openMain")
//        .navigation();
  }

  public static void openSettings() {
//    ARouter.getInstance().build("/namibox/openSettings")
//        .navigation();
  }

  public static void openBookView(String url, String bookId) {
//    ARouter.getInstance().build("/namibox/openBookWebView")
//        .withString(ARG_URL, url)
//        .withString(ARG_TEMPLATE, "fullscreen")
//        .withString(ARG_BOOK_ID, bookId)
//        .navigation();
  }

  public static void openBookView(String url, String bookId, boolean chargeAndUnOrdered,
      String refer, int isBuy) {
    String path = chargeAndUnOrdered ? "/namibox/openBookWebView" : "/namibox/openBookNativeView";
//    ARouter.getInstance().build(path)
//        .withString(ARG_URL, url)
//        .withString(ARG_TEMPLATE, "fullscreen")
//        .withString(ARG_BOOK_ID, bookId)
//        .withString(ARG_REFERER, refer)
//        .withInt("isBuy", isBuy)
//        .navigation(null, new NavCallback() {
//          @Override
//          public void onArrival(Postcard postcard) {
//            EventBus.getDefault().post(new FinishDeepLinkPageEvent());
//          }
//        });
  }

  public static void openHideView(String url, String bookId) {
//    ARouter.getInstance().build("/namibox/openHideWebView")
//        .withString(ARG_URL, url)
//        .withString(ARG_BOOK_ID, bookId)
//        .navigation();
  }

  public static void openTabsView(String parentViewName, String referer, String[] urls,
      String[] viewNames, String[] titles, int selectIndex, boolean needCapture) {
    if (urls == null || urls.length == 0) {
      return;
    }
    String[] templates = new String[urls.length];
    int i = 0;
    for (String url : urls) {
      String[] result = parseParameter(url);
      urls[i] = result[0];
      templates[i] = result[1];
      i++;
    }
//    ARouter.getInstance().build("/namibox/openBaseWebView")
//        .withCharSequenceArray(ARG_URLS, urls)
//        .withCharSequenceArray(ARG_TEMPLATES, templates)
//        .withCharSequenceArray(ARG_VIEW_NAMES, viewNames)
//        .withCharSequenceArray(ARG_TITLES, titles)
//        .withString(ARG_PARENT_VIEW_NAME, parentViewName)
//        .withString(ARG_REFERER, referer)
//        .withInt(ARG_SELECT_INDEX, selectIndex)
//        .withBoolean(ARG_NEED_CAPTURE, needCapture)
//        .navigation();
  }

  public static void openView(String url, String template,
      float template_ratio, String viewName,
      String parentViewName, String referer, int keeplight, int lighteness) {
    openView(url, template, template_ratio, viewName, parentViewName, referer, keeplight,
        lighteness, false);
  }

  public static void openView(String url, String template,
      float template_ratio, String viewName,
      String parentViewName, String referer, int keeplight, int lighteness, boolean needCapture) {
    String[] result = parseParameter(url);
    if (CommonConfig.USE_NATIVE_LOGIN && "/auth/loginpage".equals(result[4])) {
      openLoginView(result[3]);
      return;
    }
    String orientation = result[2];
    if (TextUtils.isEmpty(template)) {
      url = result[0];
      template = result[1];
    }
    if ("video".equals(template) || "fullscreen_video".equals(template)) {
//      ARouter.getInstance().build("/namibox/openVideoWebView")
//          .withString(ARG_URL, url)
//          .withString(ARG_VIEW_NAME, viewName)
//          .withFloat(ARG_TEMPLATE_RATIO, template_ratio)
//          .withString(ARG_PARENT_VIEW_NAME, parentViewName)
//          .withString(ARG_REFERER, referer)
//          .withInt(ARG_KEEP_LIGHT, keeplight)
//          .withInt(ARG_LIGHTNESS, lighteness)
//          .withString(ARG_TEMPLATE, template)
//          .navigation();
    } else if ("audio".equals(template)) {
//      ARouter.getInstance().build("/namibox/openAudioWebView")
//          .withString(ARG_URL, url)
//          .withString(ARG_VIEW_NAME, viewName)
//          .withString(ARG_PARENT_VIEW_NAME, parentViewName)
//          .withString(ARG_REFERER, referer)
//          .withInt(ARG_KEEP_LIGHT, keeplight)
//          .withInt(ARG_LIGHTNESS, lighteness)
//          .navigation();
    } else {
//      ARouter.getInstance().build("/namibox/openBaseWebView")
//          .withString(ARG_URL, url)
//          .withString(ARG_VIEW_NAME, viewName)
//          .withString(ARG_PARENT_VIEW_NAME, parentViewName)
//          .withString(ARG_REFERER, referer)
//          .withInt(ARG_KEEP_LIGHT, keeplight)
//          .withInt(ARG_LIGHTNESS, lighteness)
//          .withString(ARG_TEMPLATE, template)
//          .withBoolean(ARG_NEED_CAPTURE, needCapture)
//          .withString(ARG_ORIENTATION, orientation)
//          .navigation(null, new NavCallback() {
//            @Override
//            public void onArrival(Postcard postcard) {
//              EventBus.getDefault().post(new FinishDeepLinkPageEvent());
//            }
//          });
    }
    Logger.i("WebViewUtil url：" + url
        + "\nviewName:" + viewName
        + "\nparentViewName:" + parentViewName
        + "\nreferer:" + referer
    );
  }

  //[0]: url or original url; [1]: template or null; [2]: orientation or null
  public static String[] parseParameter(String url) {
    HttpUrl httpUrl = HttpUrl.parse(url);
    String[] result = new String[5];
    result[0] = url;
    if (httpUrl != null) {
      result[1] = httpUrl.queryParameter("_app_template");
      result[2] = httpUrl.queryParameter("_app_orientation");
      result[3] = httpUrl.queryParameter("redirect");
      result[4] = httpUrl.encodedPath();
//      if (result[1] != null || result[2] != null) {
//        result[0] = httpUrl.newBuilder()
//            .removeAllQueryParameters("_app_template")
//            .removeAllQueryParameters("_app_orientation")
//            .build().toString();
//      }
    }
    return result;
  }
}
