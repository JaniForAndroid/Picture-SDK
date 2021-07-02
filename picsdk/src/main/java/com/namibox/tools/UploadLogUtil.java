package com.namibox.tools;

import android.content.Context;
import android.text.TextUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.event.OssEvent;
import com.namibox.commonlib.event.OssEvent.OssEventType;
import com.namibox.commonlib.listener.LogUploadListener;
import com.namibox.commonlib.model.OssToken;
import com.namibox.util.Logger;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import io.reactivex.Flowable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.reactivestreams.Publisher;

/**
 * Create time: 19-1-4.
 */
public class UploadLogUtil {

  private static boolean isUploading;
  private static long uploadTime;
  public static final String DING_HOOK_C = "https://oapi.dingtalk.com/robot/send?access_token=152c83ce00cb4bbf1ea5fb8bc5b90bc92ccd926cb577b36dc9353c5bb6cd93a3";
  public static final String DING_HOOK_B = "https://oapi.dingtalk.com/robot/send?access_token=03d3c44f658c01922eb6d3afe16d2fa4934ad2bfa383776066a3015109ed716a ";
  public static final String LOG_TYPE_AUTO_TEST = "测速后自动上传";
  public static final String LOG_TYPE_AUTO_EXIT = "退出自动上传";
  public static final String LOG_TYPE_PUSH = "推送上传";
  public static final String LOG_TYPE_AUTO_SETTING = "设置主动上传";
  public static final String LOG_TYPE_KEFU = "客服上报";
  public static final String LOG_TYPE_OTHER = "其他";

  private static void toast(Context context, boolean toast, String msg) {
    if (!toast) {
      return;
    }
    Utils.toast(context, msg);
  }

  /**
   * 日志上传
   *
   * @param toast 是否弹toast
   * @param hook 是否上报钉钉
   * @param title log文件名
   * @param bClient 是否是B端日志
   * @param log_type 日志类型 目前仅4种类型 测速后自动上传、退出自动上传、推送上传、设置主动上传
   */
  public static void uploadLog(final Context context, final boolean toast, final boolean hook,
      final String title, final boolean bClient, final String log_type, final File image,
      final LogUploadListener listener) {
    if (isUploading) {
      return ;
    }
    if (System.currentTimeMillis() - uploadTime < 30000) {
      toast(context, toast, "请不要频繁发送日志");
      return ;
    }
    uploadTime = System.currentTimeMillis();
    toast(context, toast, "正在发送日志...");
    isUploading = true;
    ApiHandler.getBaseApi()
        .getOssUtoken("vschool/applog")
        .subscribeOn(Schedulers.io())
        .flatMap(new Function<OssToken, Flowable<OssToken>>() {
          @Override
          public Flowable<OssToken> apply(OssToken ossToken) throws Exception {
            if (TextUtils.isEmpty(ossToken.objectKey)) {
              throw new IllegalArgumentException("获取token失败");
            }
            String info = DeviceInfoUtil.getReportInfo(context);
            Logger.i("上报日志>>>>>>>\n" + info);
            List<OssToken> list = new ArrayList<>();
            final String ws_url = PreferenceUtil.getSharePref(context, "wx_ws_url", null);
            OssToken token1 = ossToken.clone();
            if (!TextUtils.isEmpty(ws_url)) {
              token1.objectKey += "/" + Utils.logFileFromWsUrl(context, ws_url);
            } else {
              if (bClient) {
                token1.objectKey += "/Blog.txt";
              } else {
                token1.objectKey += "/log.txt";
              }
            }
            token1.uploadFile = Utils.getLogFile(context);
            list.add(token1);
            //截图文件
            if (image.exists()) {
              OssToken token2 = ossToken.clone();
              //vschool/applog/109352/109352_vsl-1_vschool_1_lesson_1024.txt
              //vschool/applog/109352/109352/storage/emulated/0/net_cut_img.jpg
              String imgName = Utils.logFileFromWsUrl(context, ws_url).replace(".txt", ".jpg");
              token2.objectKey += "/" + Utils.getLoginUserId(context) + imgName;
              token2.uploadFile = image;
              list.add(token2);
            }
            return Flowable.fromIterable(list);
          }
        })
        .flatMap(new Function<OssToken, Publisher<OssEvent>>() {
          @Override
          public Publisher<OssEvent> apply(OssToken ossToken) throws Exception {
            return OssUploadUtil.getOssObservable(context, ossToken);
          }
        })
        .flatMap(new Function<OssEvent, Flowable<String>>() {
          @Override
          public Flowable<String> apply(OssEvent ossEvent) throws Exception {
            if (ossEvent.type == OssEventType.RESULT) {
              return Flowable.just(ossEvent.objectKey);
            } else {
              return Flowable.empty();
            }
          }
        })
        .toList()
        .toFlowable()
        .flatMap(new Function<List<String>, Publisher<JsonElement>>() {
          @Override
          public Publisher<JsonElement> apply(List<String> tokenKeys) throws Exception {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("log_type", log_type);
            if (tokenKeys != null && tokenKeys.size() > 0) {
              if (tokenKeys.size() == 2 && !TextUtils.isEmpty(tokenKeys.get(0))
                  && tokenKeys.get(0).endsWith(".jpg")) {
                jsonObject.addProperty("error_img", tokenKeys.get(0));
                jsonObject.addProperty("image_dir", tokenKeys.get(1));
              }else{
                if (!TextUtils.isEmpty(tokenKeys.get(0)) && tokenKeys.get(0).endsWith(".txt")) {
                  jsonObject.addProperty("image_dir", tokenKeys.get(0));
                }else{
                  jsonObject.addProperty("error_img", tokenKeys.get(0));
                }
              }
            }
            String url = Utils.getBaseUrl(context) + "/vschool/save_log";
            Logger.d("jsonElement:" + jsonObject);
            return ApiHandler.getBaseApi().commonJsonElementPost(url,jsonObject);
          }
        })
        .flatMap(new Function<JsonElement, Flowable<JsonElement>>() {
          @Override
          public Flowable<JsonElement> apply(final JsonElement result) throws Exception {
            Logger.d("result:" + result);
            if (hook) {
              String url = result.getAsJsonObject().get("file_path").getAsString();
              return ding(title, result.toString(), url, bClient ? DING_HOOK_B : DING_HOOK_C)
                  .map(new Function<JsonElement, JsonElement>() {
                    @Override
                    public JsonElement apply(JsonElement jsonElement) throws Exception {
                      Logger.d("hook result:" + jsonElement);
                      return result;
                    }
                  });
            } else {
              return Flowable.just(result);
            }
          }
        })
        .toList()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<List<JsonElement>>() {
          @Override
          public void onSubscribe(Disposable d) {

          }

          @Override
          public void onSuccess(List<JsonElement> result) {
            isUploading = false;
            if (result != null && result.size() > 0) {
              JsonObject asJsonObject = result.get(0).getAsJsonObject();
              if (asJsonObject != null && asJsonObject.has("retcode")
                  && TextUtils.equals("FAIL", asJsonObject.get("retcode").getAsString())) {
                if (listener != null) {
                  listener.onError();
                }
                Logger.e( "发送日志失败");
                toast(context, toast, "发送日志失败");
                EventUtil.postEvent(new LogEvent("发送日志失败"));
              }else {
                if (listener != null) {
                  listener.onSuccess();
                }
                Logger.d("onSuccess");
                toast(context, toast, "发送日志成功");
                EventUtil.postEvent(new LogEvent(result));
              }

            }
          }


          @Override
          public void onError(Throwable t) {
            isUploading = false;
            if (listener != null) {
              listener.onError();
            }
            Logger.e(t, "发送日志失败");
            if (t instanceof IllegalArgumentException) {
              toast(context, toast, "发送日志失败，请先登录");
            } else {
              toast(context, toast, "发送日志失败");
            }
            EventUtil.postEvent(new LogEvent("发送日志失败"));
          }

        });
  }

  /**
   * 日志上传
   *
   * @param toast 是否弹toast
   * @param hook 是否上报钉钉
   * @param title log文件名
   * @param bClient 是否是B端日志
   * @param log_type 日志类型 目前仅4种类型 测速后自动上传、退出自动上传、推送上传、设置主动上传
   */
  public static void uploadLog(final Context context, final boolean toast, final boolean hook,
      final String title, final boolean bClient, final String log_type) {
    if (isUploading) {
      return;
    }
    if (System.currentTimeMillis() - uploadTime < 30000) {
      toast(context, toast, "请不要频繁发送日志");
      return;
    }
    uploadTime = System.currentTimeMillis();
    toast(context, toast, "正在发送日志...");
    isUploading = true;
    ApiHandler.getBaseApi()
        .getOssUtoken("vschool/applog")
        .subscribeOn(Schedulers.io())
        //        .map(new Function<OssToken, OssToken>() {
        //          @Override
        //          public OssToken apply(OssToken ossToken) throws Exception {
        //            ossToken.objectKey += "/" + WxUtils.logFileFromWsUrl(SettingsActivity.this, ws_url);
        //            ossToken.uploadFile = WxUtils.getLogFile();
        //            return ossToken;
        //          }
        //        })
        .flatMap(new Function<OssToken, Flowable<OssToken>>() {
          @Override
          public Flowable<OssToken> apply(OssToken ossToken) throws Exception {
            if (TextUtils.isEmpty(ossToken.objectKey)) {
              throw new IllegalArgumentException("获取token失败");
            }
            String info = DeviceInfoUtil.getReportInfo(context);
            Logger.i("上报日志>>>>>>>\n" + info);
            List<OssToken> list = new ArrayList<>();
            final String ws_url = PreferenceUtil.getSharePref(context, "wx_ws_url", null);
            OssToken token1 = ossToken.clone();
            if (!TextUtils.isEmpty(ws_url)) {
              token1.objectKey += "/" + Utils.logFileFromWsUrl(context, ws_url);
            } else {
              if (bClient) {
                token1.objectKey += "/Blog.txt";
              } else {
                token1.objectKey += "/log.txt";
              }
            }
            token1.uploadFile = Utils.getLogFile(context);
            list.add(token1);
            File file = new File(context.getExternalFilesDir(null), "SSError.txt");
            if (file.exists()) {
              OssToken token2 = ossToken.clone();
              token2.objectKey += "/" + Utils.getLoginUserId(context) + "_SSError.txt";
              token2.uploadFile = file;
              list.add(token2);
            }
            File crashFile = new File(context.getExternalFilesDir(null), "crash.txt");
            if (crashFile.exists()) {
              OssToken token3 = ossToken.clone();
              token3.objectKey += "/" + Utils.getLoginUserId(context) + "_crash.txt";
              token3.uploadFile = crashFile;
              list.add(token3);
            }
            return Flowable.fromIterable(list);
          }
        })
        .flatMap(new Function<OssToken, Publisher<OssEvent>>() {
          @Override
          public Publisher<OssEvent> apply(OssToken ossToken) throws Exception {
            return OssUploadUtil.getOssObservable(context, ossToken);
          }
        })
        .flatMap(new Function<OssEvent, Flowable<JsonElement>>() {
          @Override
          public Flowable<JsonElement> apply(OssEvent ossEvent) throws Exception {
            if (ossEvent.type == OssEventType.RESULT) {
              String logTime = TimeUtil.getDate(System.currentTimeMillis());
              String url = Utils.getBaseUrl(context)
                  + "/vschool/save_log?image_dir=" + ossEvent.objectKey
                  + "&log_type=" + log_type;
              //                  +"&log_time=" + logTime;
              return ApiHandler.getBaseApi().commonJsonElementGet(url);
            } else {
              return Flowable.empty();
            }
          }
        })
        .flatMap(new Function<JsonElement, Flowable<JsonElement>>() {
          @Override
          public Flowable<JsonElement> apply(final JsonElement result) throws Exception {
            Logger.d("result:" + result);
            if (hook) {
              String url = result.getAsJsonObject().get("file_path").getAsString();
              return ding(title, result.toString(), url, bClient ? DING_HOOK_B : DING_HOOK_C)
                  .map(new Function<JsonElement, JsonElement>() {
                    @Override
                    public JsonElement apply(JsonElement jsonElement) throws Exception {
                      Logger.d("hook result:" + jsonElement);
                      return result;
                    }
                  });
            } else {
              return Flowable.just(result);
            }
          }
        })
        .toList()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<List<JsonElement>>() {
          @Override
          public void onSubscribe(Disposable d) {

          }

          @Override
          public void onSuccess(List<JsonElement> result) {
            isUploading = false;
            Logger.d("onSuccess");
            toast(context, toast, "发送日志成功");
            EventUtil.postEvent(new LogEvent(result));
          }


          @Override
          public void onError(Throwable t) {
            isUploading = false;
            Logger.e(t, "发送日志失败");
            if (t instanceof IllegalArgumentException) {
              toast(context, toast, "发送日志失败，请先登录");
            } else {
              toast(context, toast, "发送日志失败");
            }
            EventUtil.postEvent(new LogEvent("发送日志失败"));
          }

        });
  }


  public static void uploadLog(Context context, boolean toast, boolean hook, boolean bClient,
      String logType) {
    uploadLog(context, toast, hook, "用户" + Utils.getLoginUserId(context) + "上报日志:", bClient,
        logType);
  }



  /**
   * 测速异常上传
   *
   * @param image 异常截图链接地址
   */
  public static void  uploadLog(Context context, boolean toast, boolean hook, String logType,
      File image,LogUploadListener listener) {
    uploadLog(context, toast, hook, "用户" + Utils.getLoginUserId(context) + "上报日志:",
        false, logType,image,listener);
  }
  /***
   * 推送日志上传
   */
  public static void uploadLog(Context context, boolean toast, boolean hook, String logType) {
    uploadLog(context, toast, hook, "用户" + Utils.getLoginUserId(context) + "上报日志:", false, logType);
  }

  public static Flowable<JsonElement> ding(String title, String text, String url, String ding_url) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("msgtype", "link");
    JsonObject link = new JsonObject();
    link.addProperty("title", title);
    link.addProperty("text", text);
    link.addProperty("messageUrl", url);
    jsonObject.add("link", link);
    return ApiHandler.getBaseApi().commonJsonElementPost(ding_url, jsonObject);
  }

  public static class LogEvent {

    public List<JsonElement> result;
    public String error;

    public LogEvent(String error) {
      this.error = error;
    }

    public LogEvent(List<JsonElement> result) {
      this.result = result;
    }
  }
}
