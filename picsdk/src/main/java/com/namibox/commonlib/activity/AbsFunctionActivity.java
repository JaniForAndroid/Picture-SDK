package com.namibox.commonlib.activity;

import static com.namibox.commonlib.event.ScoreDialogEvent.EVENT_TYPE_WK;

import android.Manifest;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.support.annotation.Nullable;
import com.example.picsdk.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.event.CoinTipEvent;
import com.namibox.commonlib.event.ExitEvent;
import com.namibox.commonlib.event.ImageSelectResultEvent;
import com.namibox.commonlib.event.MessageEvent;
import com.namibox.commonlib.model.OrderMessageEntity;
import com.namibox.hfx.ui.HfxPlayAudioActivity;
import com.namibox.qr_code.ScannerActivity;
import com.namibox.tools.GlobalConstants;
import com.namibox.tools.PermissionUtil;
import com.namibox.tools.PermissionUtil.GrantedCallback;
import com.namibox.tools.ScoreDialogUtil;
import com.namibox.tools.WebViewUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.Utils;
import com.namibox.util.network.NetWorkHelper;
import com.namibox.voice_engine_interface.VoiceEngineContext;
import com.namibox.voice_engine_interface.VoiceEngineContext.VoiceEngineCallback;
import com.uraroji.garage.android.lame.AudioUtil;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import okhttp3.OkHttpClient;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Create time: 2017/6/12.
 */

public abstract class AbsFunctionActivity extends AbsFoundationActivity {

  private AudioUtil recorder;

  public static final String TYPE_ONLINE_CS = "online_cs";
  public static final String TYPE_ONLINE_XF = "online_xf";
  public static final String TYPE_ONLINE_XS = "online_xs";
  public static final String TYPE_OFFLINE_XS = "offline_xs";
  public static final String TYPE_MIX_XS = "mix_xs";
  protected boolean engineStopped;


  private String eval_type = "phrases";
  //    private String enginetype = TYPE_OFFLINE_CS;
  public boolean engineCanceled;


  private OkHttpClient okHttpClient;
  protected boolean switchToNative;
  protected boolean isChEval;

  public static final int REQUEST_WEB_IMAGE_UPLOAD = 300;
  public static final int REQUEST_CODE_SCANNER = 500;
  public static final int REQUEST_VIDEO_PLAY = 600;
  public static final int REQUEST_VIDEO_IMAGE_CHOOSER = 700;
  //  public static final int REQUEST_SHARE_CHOOSER = 800;
  public final static int MEDIA_PICKER = 900;
  public final static int MEDIA_RECORDER = 901;
  public final static int OPEN_REPLY = 1000;
  private VoiceEngineContext voiceEngineContext;

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void finish() {
    super.finish();
    Logger.d("finish: " + this);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventBus.getDefault().register(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
    releaseEngine();
    if (recorder != null) {
      recorder.release();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void exit(ExitEvent event) {
    finish();
  }

  public String getUserAgent() {
    return NetWorkHelper.getInstance().getUa();
  }

  public OkHttpClient getOkHttpClient() {
    if (okHttpClient == null) {
      okHttpClient = NetWorkHelper.getOkHttpClient();
    }
    return okHttpClient;
  }

  protected void resetEval() {

  }

  public void setServerType(String engineType) {
    int type;
    if (TYPE_ONLINE_XS.equals(engineType)) {
      type = VoiceEngineContext.SERVER_TYPE_ONLINE;
    } else if (TYPE_MIX_XS.equals(engineType)) {
      type = VoiceEngineContext.SERVER_TYPE_MIX;
    } else {
      type = VoiceEngineContext.SERVER_TYPE_OFFLINE;
    }
    voiceEngineContext.setServerType(type);
  }


  protected void startEngine(String text) {
    long length;
    if ("word_cn".equals(eval_type) || "phrases_cn".equals(eval_type)) {
      length = calCnTextLength(text);
      if (length > 299 * 1000) {
        length = 299 * 1000;
      }
    } else {
      length = calTextLength(text);
    }
    startEngine(text, length, null);
  }

  public void startEngine(String text, long timeOutMillis, String wavFilePath) {
    voiceEngineContext.startEngine(text, wavFilePath, timeOutMillis);
    engineStopped = false;
    engineCanceled = false;
  }

  public void setEvalType(String evalType) {
    voiceEngineContext.setEvalType(evalType);
  }

  public void stopEngine() {
    Logger.d("stopEngine");
    engineStopped = true;
    voiceEngineContext.stop();
  }

  public void cancelEngine() {
    Logger.d("cancelEngine");
    engineCanceled = true;
    voiceEngineContext.cancel();
  }

  protected long calTextLength(String text) {
    String[] words = text.trim().split("\\s+");
    //return words.length * 600 + 2000;
    float y = (float) (words.length * 0.5 + 2);
    return (int) Math.ceil(y) * 1000;
  }

  protected long calCnTextLength(String text) {
    if (text == null) {
      return 0;
    }
    return text.length() * 1000;
  }

  public void initEngineNoUI(VoiceEngineCallback aiEngineCallback) {
//        initEngineNoUI("phrases", TYPE_OFFLINE_CS, aiEngineCallback);
    initEngineNoUI("phrases", TYPE_OFFLINE_XS, "namibox", aiEngineCallback, false);
  }

  public void initEngineNoUI(String type, String enginetype, String userid,
      VoiceEngineCallback aiEngineCallback, boolean isOralEval) {
    this.eval_type = type;
    if (TextUtils.isEmpty(enginetype)) {
      enginetype = TYPE_OFFLINE_XS;
    }
    Logger.d("Shelter", "initEngineNoUI, engineType = " + enginetype);
    try {
      voiceEngineContext = new VoiceEngineContext(enginetype);
      voiceEngineContext.init(this);
      voiceEngineContext.create(this, enginetype, eval_type, isOralEval, userid, aiEngineCallback);
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e(e, "init engine failed");
      return;
    }
    if (Utils.isDev(this)) {
//      toast("引擎类型：" + enginetype);
    }
  }

  public void releaseEngine() {
    if (voiceEngineContext != null) {
      voiceEngineContext.release();
    }
  }


  public void login() {
    WebViewUtil.openLoginView();
  }

  public void openView(String url) {
    WebViewUtil.openView(url);
  }


  public void initAudioUtil(AudioUtil.VolumeCallBack callBack) {
    if (recorder == null) {
      recorder = new AudioUtil(this, AudioFormat.CHANNEL_IN_MONO, callBack);
    }
  }

  protected void initAudioUtilWith16k() {
    if (recorder == null) {
      recorder = new AudioUtil(16000, AudioFormat.CHANNEL_IN_MONO, null);
    }
  }

  public void cutRecordFile(double byteSize) throws IOException {
    recorder.cutFile(byteSize);
  }

  public int getMsPerBuffer() {
    return recorder.getMsPerBuffer();
  }

  public int getSampleRate() {
    return recorder.getSampleRate();
  }

  public void releaseRecorder() {
    if (recorder != null) {
      recorder.release();
    }
  }

  public void startMp3(File recordFile, boolean append) throws FileNotFoundException {
    recorder.startMp3(recordFile, append);
  }

  public void startPcm(File recordFile, boolean append) {
    recorder.startPcm(recordFile, append, false, null);
  }

  public void startPcm(File recordFile, boolean append, boolean need2wav, File wavFile) {
    recorder.startPcm(recordFile, append, need2wav, wavFile);
  }

  public int testAudio() {
    return recorder.testAudio();
  }

  protected void startMediaRecorder(File file) throws Exception {
    if (recorder == null) {
      recorder = new AudioUtil(this, AudioFormat.CHANNEL_IN_MONO, null);
    }
    recorder.startMp3(file);
  }

  protected void stopAudioRecord() {
    if (recorder != null) {
      recorder.stop();
    }
  }

  protected double getVolume() {
    if (recorder != null) {
      return recorder.getVolume();
    } else {
      return 0;
    }
  }


  private VideoPlayCallback videoPlayCallback;
  private Uri videoPlayUri;

  public interface VideoPlayCallback {

    void onPlayResult(String url, String duration, boolean result, String type);
  }

  public void onVideoPlay(String url, String title,
      boolean autoPlay, int seekTime, int duration, long size, int heartNum, String notify_url,
      String jsonInterrupt,
      VideoPlayCallback videoPlayCallback) {
//    this.videoPlayCallback = videoPlayCallback;
//    videoPlayUri = Uri.parse(url);
//    Logger.i("zkx: heartNum :" + heartNum);
//    ARouter.getInstance().build("/namibox/openVideoPlayer")
//        .withString(GlobalConstants.VIDEO_URI, videoPlayUri.toString())
//        .withBoolean(GlobalConstants.AUTO_PLAY, autoPlay)
//        .withBoolean(GlobalConstants.LOCAL_FILE, false)
//        .withInt(GlobalConstants.SEEK_TIME, seekTime)
//        .withInt(GlobalConstants.DURATION, duration)
//        .withLong(GlobalConstants.SIZE, size)
//        .withInt(GlobalConstants.HEART_NUM, heartNum)
//        .withString(GlobalConstants.INTERRUPT, jsonInterrupt)
//        .withString(GlobalConstants.NOTIFY_URL, notify_url)
//        .withString(GlobalConstants.TITLE, title)
//        .navigation(this, REQUEST_VIDEO_PLAY);

  }

  //全屏播放视频
  public void onVideoPlay(String url, String title) {
//    ARouter.getInstance().build("/namibox/openVideoPlayer")
//        .withString(GlobalConstants.VIDEO_URI, Uri.parse(url).toString())
//        .withBoolean(GlobalConstants.AUTO_PLAY, true)
//        .withString(GlobalConstants.TITLE, title)
//        .navigation();
  }

  public void onVideoPlay(File file, String title, boolean autoPlay, int seekTime,
      VideoPlayCallback videoPlayCallback) {
//    this.videoPlayCallback = videoPlayCallback;
//    videoPlayUri = Uri.fromFile(file);
//    ARouter.getInstance().build("/namibox/openVideoPlayer")
//        .withString(GlobalConstants.VIDEO_URI, videoPlayUri.toString())
//        .withBoolean(GlobalConstants.AUTO_PLAY, autoPlay)
//        .withBoolean(GlobalConstants.LOCAL_FILE, true)
//        .withInt(GlobalConstants.SEEK_TIME, seekTime)
//        .withString(GlobalConstants.TITLE, title)
//        .navigation(this, REQUEST_VIDEO_PLAY);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    switch (requestCode) {
      case REQUEST_VIDEO_PLAY:
        if (resultCode == RESULT_OK && intent != null) {
          String duration = intent.getStringExtra(GlobalConstants.RESULT_DURATION);
          boolean result = intent.getBooleanExtra(GlobalConstants.RESULT_STATE, true);
          if (videoPlayCallback != null) {
            videoPlayCallback.onPlayResult(videoPlayUri.toString(), duration, result, "mp4");
          }
          String notifyUrl = intent.getStringExtra(GlobalConstants.NOTIFY_URL);
          float complete = intent.getFloatExtra(GlobalConstants.RESULT_COMPLETE, 0f);
          String integrity = intent.getStringExtra(GlobalConstants.INTEGRITY);
          if (TextUtils.isEmpty(notifyUrl)) {
            Logger.i("zkx 不需要上报 ");
          } else {
            Logger.i("zkx 需要上报 ");
            //上报视频播放进度
            reportVideoComplete(notifyUrl, complete, integrity);
          }
          Logger.i("zkx duration:" + duration + " complete : " + (int) complete);
        }
        return;
//      case REQUEST_SHARE_CHOOSER:
//        if (resultCode == RESULT_OK) {
//          if (shareCallback != null) {
//            boolean success = intent.getBooleanExtra("success", false);
//            String type = intent.getStringExtra("type");
//            shareCallback.onResult(success, type);
//          }
//        }
//        break;
      case REQUEST_CODE_SCANNER:
        if (resultCode == RESULT_OK) {
          String qrCode = intent.getStringExtra("result");
          String format = intent.getStringExtra("result_format");
          if (scannerCallback != null) {
            scannerCallback.onScannerResult(qrCode, format);
          }
        }
        break;
      default:
        break;

    }
    super.onActivityResult(requestCode, resultCode, intent);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onImageSelectResult(ImageSelectResultEvent event) {
    // do nothing
  }

  /**
   * 将视频播放进度上报至服务器
   *
   * @param notifyUrl 上报进度接口
   * @param complete 上报进度
   */
  private void reportVideoComplete(final String notifyUrl, float complete, final String integrity) {
    if (!NetworkUtil.isNetworkConnected(this)) {
      return;
    }
    final int percent = (int) complete;
    String reqUrl = notifyUrl + "?percent=" + percent + "&integrity=" + integrity;
    ApiHandler.getBaseApi().commonJsonGet(reqUrl)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Observer<JsonObject>() {
          @Override
          public void onSubscribe(Disposable d) {
          }

          @Override
          public void onNext(JsonObject data) {
            Logger.d("onNext: " + data);
            if (data.has("coin")) {
              String coin = data.get("coin").getAsString();
              EventBus.getDefault().post(new CoinTipEvent(coin));
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("command", "message");
            map.put("message", "broadcast_refresh_tutorable");
            EventBus.getDefault().post(new MessageEvent("", new Gson().toJson(map),
                "broadcast_refresh_tutorable"));
            ScoreDialogUtil.handleScoreEvent(EVENT_TYPE_WK);
          }

          @Override
          public void onError(Throwable e) {
            e.printStackTrace();
            Logger.e("zkx 进度上报异常:" + e.toString());
          }

          @Override
          public void onComplete() {
          }
        });
  }

  //参数是json对象字符串
  public void connectKefu(String scene, String message, boolean directTransferKefu,
      boolean sendLog) {
//    connectKefu(null, "", message, directTransferKefu, sendLog);
    connectKefu(scene, "", "", null, "", message, directTransferKefu, sendLog);
  }

  public void connectKefu(String secen, OrderMessageEntity order, String robotMessage) {
//    connectKefu(order, robotMessage, null, false, false);
    connectKefu(secen, "", "", order, robotMessage, null, false, false);
  }

  public void connectKefu(String secen, OrderMessageEntity order, String robotMessage,
      String errorMessage,
      boolean directTransferKefu, boolean sendLog) {
    connectKefu(secen, "", "", order, robotMessage, errorMessage, directTransferKefu, sendLog);
  }

  public void connectKefu(String scene, String phoneNum, String order_url, OrderMessageEntity order,
      String robotMessage, String errorMessage, boolean directTransferKefu, boolean sendLog) {
    if (!NetworkUtil.isNetworkAvailable(this)) {
      toast(getString(R.string.common_network_none_tips));
    } else {
//      ARouter.getInstance().build("/namibox/dispatchKefu")//跳转区分环信还是七陌客服的activity
//          .withSerializable("kefu_order", order)
//          .withString("scene", scene)
//          .withString("phoneNum", phoneNum)
//          .withString("order_url", order_url)
//          .withString("robot_message", robotMessage)
//          .withString("error_message", errorMessage)
//          .withBoolean("directTransferKefu", directTransferKefu)
//          .withBoolean("sendLog", sendLog)
//          .navigation();
    }
  }

  public void openPlayLocalAudio(String audioId) {
//    ARouter.getInstance().build("/hfx/hfxPlayAudio")
//        .withString("audio_type", "local_audio")
//        .withString("audio_id", audioId)
//        .navigation();
    Intent intent = new Intent(this, HfxPlayAudioActivity.class);
    intent.putExtra("audio_type", "local_audio");
    intent.putExtra("audio_id", audioId);
    startActivity(intent);
  }

  public void openPlayOnlineAudio(String url) {
//    ARouter.getInstance().build("/hfx/hfxPlayAudio")
//        .withString("audio_type", "net_audio")
//        .withString("json_url", url)
//        .navigation();

    Intent intent = new Intent(this, HfxPlayAudioActivity.class);
    intent.putExtra("audio_type", "net_audio");
    intent.putExtra("json_url", url);
    startActivity(intent);
  }

//  public void openDubShare(String imgUrl, String webpageUrl,
//      String title, String share_titleFriend, String content, int hearts) {
//    ARouter.getInstance().build("/shareLib/dubShare")
//        .withString("share_imgUrl", imgUrl)
//        .withString("share_webpageUrl", webpageUrl)
//        .withString("share_title", title)
//        .withString("share_titleFriend", share_titleFriend)
//        .withString("share_content", content)
//        .withInt("share_hearts", hearts)
//        .withTransition(R.anim.fade_in,R.anim.fade_out)
//        .navigation(this);
//  }

//  public void showShareImage(File imageFile) {
//    ShareData shareData = new ShareData();
//    shareData.shareImageFile = imageFile;
//    shareData.shareImage = true;
////    showShare(shareData, null);
//    showShareActivity(shareData,null);
//  }

//  public void showShare(File imageFile, String imgUrl, String webpageUrl,
//      String title, String shareFriend, String content, ShareCallback callback) {
//    ShareData shareData = new ShareData();
//    shareData.share_imgUrl = imgUrl;
//    shareData.share_webpageUrl = webpageUrl;
//    shareData.share_title = title;
//    shareData.share_titleFriend = shareFriend;
//    shareData.share_content = content;
////    showShare(shareData, callback);
//    showShareActivity(shareData,callback);
//  }

//  public void shortVideoShare(String imgUrl, String webpageUrl,
//      String title, String shareFriend, String content, ShareCallback callback) {
//    ShareData shareData = new ShareData();
//    shareData.share_imgUrl = imgUrl;
//    shareData.share_webpageUrl = webpageUrl;
//    shareData.share_title = title;
//    shareData.share_titleFriend = shareFriend;
//    shareData.share_content = content;
//    shareData.dim = true;
//    shareData.albumShare = true;
////    showShare(shareData, callback);
//    showShareActivity(shareData,callback);
//  }

//  public void showShare(String shareType, JsonObject jsonObj, ShareCallback callback) {
//    ShareData shareData = new ShareData();
//    shareData.shareType = shareType;
//    shareData.share_imgUrl = jsonObj.get("url_image").getAsString();
//    shareData.share_webpageUrl = jsonObj.get("url_link").getAsString();
//    shareData.share_title = jsonObj.get("share_title").getAsString();
//    shareData.share_titleFriend = jsonObj.get("share_friend").getAsString();
//    shareData.share_content = jsonObj.get("share_content").getAsString();
//    shareData.mini_program_id = jsonObj.has("mini_program_id") ?
//        jsonObj.get("mini_program_id").getAsString() : null;
//    shareData.mini_program_path = jsonObj.has("mini_program_path") ?
//        jsonObj.get("mini_program_path").getAsString() : null;
//    shareData.mini_program_type = jsonObj.has("mini_program_type") ?
//        jsonObj.get("mini_program_type").getAsString() : null;
//    shareData.ignoreresult = jsonObj.has("ignoreresult")
//        && jsonObj.get("ignoreresult").getAsBoolean();
////    showShare(shareData, callback);
//    showShareActivity(shareData,callback);
//  }

//  public interface ShareCallback {
//
//    void onResult(boolean success, String type);
//  }

//  private ShareCallback shareCallback;

//  public void setShareCallback(ShareCallback shareCallback) {
//    this.shareCallback = shareCallback;
//  }

//  public void showShare(ShareData shareData, ShareCallback callback) {
//    showShareActivity(shareData,callback);
//  }

//  protected void showShareActivity(ShareData shareData, final ShareCallback callback) {
//    Gson gson = new Gson();
//    JsonObject jsonObject = gson.fromJson(gson.toJson(shareData), JsonObject.class);
//    CommonShareHelper.share(PLAT_TYPE.ALL, this, jsonObject, new com.namibox.lib.share_pay_login_lib.callback.ShareCallback() {
//      @Override
//      public void onResult(boolean isSuccess, String msg) {
//        if (callback != null) {
//          callback.onResult(isSuccess,msg);
//        }
//      }
//    });
//  }

//  protected void reflectInvokeShareMethod(JsonObject jsonObject, final ShareCallback shareCallback) {
//
//    try {
//      Class clazz = Class.forName("com.namibox.lib.share_pay_login_lib.share.CommonShareHelper");
//      Class typeClazz = Class.forName("com.namibox.lib.share_pay_login_lib.entity.PLAT_TYPE");
//      Class callbackClazz = Class.forName("com.namibox.lib.share_pay_login_lib.callback.ShareCallback");
//
//      Object object = clazz.newInstance();
//      Method method = object.getClass().getMethod("share", typeClazz,
//          FragmentActivity.class, JsonObject.class, callbackClazz);
//
//      Object type = reflectInvokeEnum(typeClazz);
//      Object shareCallbakce = relectInvokeCallback(callbackClazz, shareCallback);
//      if (method != null && type != null && shareCallbakce != null) {
//        method.invoke(object, type, this, jsonObject, shareCallbakce);
//      }
//
//    } catch (Exception e) {
//      e.printStackTrace();
//      if (BuildConfig.DEBUG) {
//        toast("分享报错了，请检查是否配置了不同的分享类型");
//      }
//      Logger.e("wzp-reflectShare", "反射调用分享报错。" + e.toString());
//    }
//  }

//  private Object reflectInvokeEnum(Class typeClazz) throws Exception {
//    //根据反射获取常量类
//    if (typeClazz.isEnum()) {
//      //反射获取枚举类
//      //获取所有枚举实例
//      Enum[] enumConstants = ((Class<Enum>) typeClazz).getEnumConstants();
//      for (Enum enum1 : enumConstants) {
//        //得到枚举实例名
//        String name = enum1.name();
//        if (name.equals("ALL")) {
//          return enum1;
//        }
//      }
//      return null;
//    } else {
//      return null;
//    }
//  }
//
//  private Object relectInvokeCallback(Class callbackClazz, ShareCallback callback) throws Exception {
//    ReflectCallbackHandle reflectCallbackHandle = new ReflectCallbackHandle(callback);
//    Object myCallback = Proxy.newProxyInstance(
//        callbackClazz.getClassLoader(),
//        new Class[]{callbackClazz},
//        reflectCallbackHandle);
//    return myCallback;
//  }
//
//  class ReflectCallbackHandle implements InvocationHandler {
//
//    private ShareCallback callback;
//
//    public ReflectCallbackHandle(ShareCallback callback) {
//      this.callback = callback;
//    }
//
//    @Override
//    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//      if (callback != null && args.length == 2) {
//        callback.onResult((boolean) args[0], (String) args[1]);
//      }
//      return null;
//    }
//  }

  protected int getCameraId() {
    //有多少个摄像头
    int cameraId = -1;
    int numberOfCameras = Camera.getNumberOfCameras();
    for (int i = 0; i < numberOfCameras; ++i) {
      final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

      Camera.getCameraInfo(i, cameraInfo);
      //后置摄像头
      if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
        cameraId = i;
        break;
      }
    }
    return cameraId;
  }


  public interface ScannerCallback {

    void onScannerResult(String result, String format);
  }

  private ScannerCallback scannerCallback;

  public void onStartScanner(ScannerCallback scannerCallback) {
    this.scannerCallback = scannerCallback;
    PermissionUtil.requestPermission(this, new GrantedCallback() {
      @Override
      public void action() {
        startScanner();
      }
    }, Manifest.permission.CAMERA);
  }

  private void startScanner() {
//    ARouter.getInstance().build("/namiboxqr/scanner")
//        .withTransition(-1, -1)
//        .navigation(this, REQUEST_CODE_SCANNER);

    Intent intent = new Intent(this, ScannerActivity.class);
    startActivityForResult(intent,REQUEST_CODE_SCANNER);
    overridePendingTransition(0,0);
  }

}
