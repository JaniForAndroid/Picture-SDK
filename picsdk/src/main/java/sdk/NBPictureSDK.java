package sdk;

import android.Manifest.permission;
import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import cn.dreamtobe.filedownloader.OkHttp3Connection;
import com.example.picsdk.PicGuideActivity;
import com.google.gson.JsonObject;
import com.liulishuo.filedownloader.FileDownloader;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.event.ExitEvent;
import com.namibox.tools.AndroidLogFormatStrategy;
import com.namibox.tools.DeviceInfoUtil;
import com.namibox.tools.LoggerFormatStrategy;
import com.namibox.tools.PermissionUtil;
import com.namibox.tools.PermissionUtil.UngrantedCallback;
import com.namibox.util.AppUtil;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import com.namibox.util.network.NetWorkHelper;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import sdk.callback.AnalyzeAuthCallback;
import sdk.callback.CleanCacheCallback;
import sdk.callback.RegisterSDKInterface;
import sdk.model.RegisterModel;

@Keep
public class NBPictureSDK implements LifecycleObserver {

  private static final String TAG = "NBPictureSDK";
  private WeakReference<AppCompatActivity> registerActivity;
  private Disposable authTokenDis, loginDis;
  private RegisterSDKInterface registerSDKCallback;
  public static boolean isDebug;

  @OnLifecycleEvent(Event.ON_DESTROY)
  void onDestroy() {
    Logger.d(TAG, "onDestroy");

    if (authTokenDis != null && !authTokenDis.isDisposed()) {
      authTokenDis.dispose();
    }
    if (loginDis != null && !loginDis.isDisposed()) {
      loginDis.dispose();
    }
    if (registerActivity != null && registerActivity.get() != null) {
      registerActivity.get().getLifecycle().removeObserver(this);
    }
  }

  /**
   * 绘本SDK初始化接口
   */
  public static void init(Application application, boolean debug) {
    isDebug = debug;
    AppUtil.init(application);
    initNetwork(application);
    initLog(application);
  }

  /**
   * 绘本SDK注册接口
   *
   * @param activity 调用该接口所在的activity,通过其获取生命周期，便于及时释放对象
   * @param registerModel 注册SDK所需参数
   */
  public void register(AppCompatActivity activity, RegisterModel registerModel,
      RegisterSDKInterface registerSDKInterface) {
    if (activity == null || activity.isFinishing()) {
      Logger.d(TAG, "activity is null or isFinishing");
      if (registerSDKInterface != null) {
        registerSDKInterface.onError(new Throwable("activity is null or isFinishing"), "-1");
      }
      return;
    }
    if (registerModel == null) {
      Logger.d(TAG, "注册信息为空");
      if (registerSDKInterface != null) {
        registerSDKInterface.onError(new Throwable("注册信息为空"), "-1");
      }
      return;
    }

    registerSDKCallback = registerSDKInterface;

    registerActivity = new WeakReference<>(activity);
    if (registerActivity.get() != null) {
      registerActivity.get().getLifecycle().addObserver(this);
    }

    authToken(activity, registerModel, true, registerSDKInterface);
  }

  public void authToken(Activity activity, RegisterModel registerModel, boolean canLogin
      , RegisterSDKInterface registerSDKInterface) {
    String url = AppUtil.getBaseUrl() + "/pbook/authentication";
    JsonObject js = new JsonObject();
    js.addProperty("access_token", registerModel.access_token);
    js.addProperty("product_id", registerModel.product_id);
    js.addProperty("content_id", registerModel.content_id);
    js.addProperty("partner_id", registerModel.partner_id);
    js.addProperty("cellphone", registerModel.cellphone);
    Logger.d(TAG, "authToken 参数:" + js);

    ApiHandler.getBaseApi().commonJsonPost(url, js)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(disposable -> {
          if (authTokenDis != null && !authTokenDis.isDisposed()) {
            authTokenDis.dispose();
          }
          authTokenDis = disposable;
        })
        .subscribe(jsonObject -> {
          Logger.d(TAG, "authToken 结果：" + jsonObject);
          JsonObject result = (JsonObject) jsonObject;
          String retcode = result.get("retcode").getAsString();
          String des = "未获取description";
          if (result.has("description")) {
            des = result.get("description").getAsString();
          }
          if (TextUtils.equals(retcode, "SUCC")) {
            if (result.has("data")) {
              JsonObject data = result.get("data").getAsJsonObject();
              if (data.has("code")) {
                String code = data.get("code").getAsString();
                String finalDes = des;
                analyzeAuthCode(code, new AnalyzeAuthCallback() {
                  @Override
                  public void onSuccess() {
                    enterPicture(activity, registerModel);
                    if (registerActivity != null && registerActivity.get() != null) {
                      registerActivity.get().getLifecycle().removeObserver(NBPictureSDK.this);
                    }
                  }

                  @Override
                  public void onLogin() {
                    if (canLogin) {
                      login(activity, registerModel, registerSDKInterface);
                    } else {
                      callbackError(registerSDKInterface, new Throwable("自动登录失败，请重新登录"), code);
                    }
                  }

                  @Override
                  public void onOtherError(String code) {
                    callbackError(registerSDKInterface, new Throwable(finalDes), code);
                  }
                });
              } else {
                callbackError(registerSDKInterface, new Throwable(des), "-1");
              }
            } else {
              callbackError(registerSDKInterface, new Throwable(des), "-1");
            }
          } else {
            Logger.e(TAG, "auth token报错:" + des);
            callbackError(registerSDKInterface, new Throwable(des), "-1");
          }
        }, throwable -> {
          Logger.e(TAG, "auth token 报错:" + throwable.toString());
          callbackError(registerSDKInterface, throwable, "-2");
        }).isDisposed();
  }

  private void callbackError(RegisterSDKInterface registerSDKInterface, Throwable throwable,
      String code) {
    if (registerActivity != null && registerActivity.get() != null) {
      registerActivity.get().getLifecycle().removeObserver(NBPictureSDK.this);
    }
    if (registerSDKInterface != null) {
      registerSDKInterface.onError(throwable, code);
    }
  }

  private void enterPicture(Activity activity, RegisterModel registerModel) {
    PermissionUtil.requestPermission(activity, () -> {
          if (!EventBus.getDefault().isRegistered(NBPictureSDK.this)) {
            EventBus.getDefault().register(NBPictureSDK.this);
          }
          String url = AppUtil.getBaseUrl() + "/api/guide/" + registerModel.content_id;
          Logger.d(TAG, "enterPicture，guide url:" + url);
          Intent intent = new Intent(activity, PicGuideActivity.class);
          intent.putExtra("url", url);
          activity.startActivity(intent);
        }, (UngrantedCallback) () -> Logger.d(TAG, "enterPicture,用户拒绝授权读取SD卡"),
        permission.WRITE_EXTERNAL_STORAGE);
  }

  private void analyzeAuthCode(String code, AnalyzeAuthCallback callback) {
    switch (code) {
      case "1000"://鉴权成功
        if (callback != null) {
          callback.onSuccess();
        }
        break;
      case "1004"://未登录
        if (callback != null) {
          callback.onLogin();
        }
        break;
      case "1001"://token过期
      case "1002"://token校验错误
      case "1003"://未购买
      case "1005"://缺少必要参数
      case "1006"://用户不存在
      case "1007"://内容不存在
        if (callback != null) {
          callback.onOtherError(code);
        }
        break;
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void DubbingResultReceiver(DubbingResultEvent event) {
    if (registerSDKCallback != null) {
      registerSDKCallback.onDubbingResult(event.dubbingResultBean);
    } else {
      Logger.e(TAG, "返回视频秀结果时，回调为空。");
    }
    EventBus.getDefault().post(new ExitEvent());
    EventBus.getDefault().unregister(NBPictureSDK.this);
  }

  public static void cleanCache(Context context, CleanCacheCallback cacheCallback) {
    if (cacheCallback != null) {
      cacheCallback.onBefore();
    }
    Observable.fromCallable(() -> {
      AppUtil.deleteCache(context);
      return true;
    })
        .retry(1)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DefaultObserver<Boolean>() {
          @Override
          public void onNext(@NonNull Boolean success) {
            if (cacheCallback != null) {
              cacheCallback.onSuccess();
            }
          }

          @Override
          public void onError(@NonNull Throwable e) {
            e.printStackTrace();
            if (cacheCallback != null) {
              cacheCallback.onFail(e);
            }
          }

          @Override
          public void onComplete() {
          }
        });
  }

  public void login(Activity activity, RegisterModel model,
      RegisterSDKInterface registerSDKInterface) {
    String url = AppUtil.getBaseUrl() + "/pbook/login";

    JsonObject js = new JsonObject();
    js.addProperty("access_token", model.access_token);
    js.addProperty("partner_id", model.partner_id);
    js.addProperty("cellphone", model.cellphone);
    js.addProperty("dev_id", DeviceInfoUtil.getDeviceId(activity));
    Logger.d(TAG, "login:" + js);

    ApiHandler.getBaseApi().commonJsonPost(url, js)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(disposable -> {
          if (loginDis != null && !loginDis.isDisposed()) {
            loginDis.dispose();
          }
          loginDis = disposable;
        })
        .subscribe(jsonObject -> {
          Logger.d(TAG, "login 结果：" + jsonObject);
          JsonObject result = (JsonObject) jsonObject;
          String retcode = result.get("retcode").getAsString();
          if (TextUtils.equals(retcode, "SUCC")) {
            String des = "未获取description";
            if (result.has("description")) {
              des = result.get("description").getAsString();
            }

            if (result.has("data")) {
              JsonObject data = result.get("data").getAsJsonObject();
              if (data.has("user_info")) {
                JsonObject userInfo = data.get("user_info").getAsJsonObject();
                if (userInfo.has("userid")) {
                  long userId = userInfo.get("userid").getAsLong();
                  PreferenceUtil.setLongLoginUserId(activity, userId);
                }
                if (userInfo.has("phonenum")) {
                  String userPhone = userInfo.get("phonenum").getAsString();
                  PreferenceUtil.setPhone(activity, userPhone);
                }
                if (userInfo.has("changetime")) {
                  long expireTime = Utils.parseTimeString(userInfo.get("changetime").getAsString());
                  PreferenceUtil.setSharePref(activity, PreferenceUtil.PREF_EXPIRE_TIME, expireTime);
                }

                if (userInfo.has("sessionid")) {
                  String userSession = userInfo.get("sessionid").getAsString();
                  PreferenceUtil.setSessionId(activity, userSession);
                  NetworkUtil.syncCookie(activity, AppUtil.getBaseUrl());
                }
              }
              if (data.has("code")) {
                String code = data.get("code").getAsString();
                if (TextUtils.equals(code, "1000")) {
                  //登录成功，再次鉴权
                  authToken(activity, model, false, registerSDKInterface);
                } else {
                  callbackError(registerSDKInterface, new Throwable(des), code);
                }
              }
            }
          } else {
            Logger.e(TAG, "login接口报错");
            callbackError(registerSDKInterface, new Throwable("登录接口报错：" + retcode + "，请重试"), "-1");
          }
        }, throwable -> {
          Logger.e(TAG, throwable, "login 报错");
          callbackError(registerSDKInterface, throwable, "-2");
        }).isDisposed();
  }

  private static void initNetwork(Application application) {
    if (isDebug) {
      if (PreferenceUtil.getSharePref(application, "isTestEnv", true)) {
        PreferenceUtil.setEnv(application, "wpbook.namibox.com");
      } else {
        PreferenceUtil.setEnv(application, "pbook.namibox.com");
      }
      PreferenceUtil.setSSLEnable(application);
    } else {
      PreferenceUtil.setEnv(application, "pbook.namibox.com");
    }
    String ua = NetworkUtil.getDefaultUserAgent(application) + " npbook/Android/" + Utils
        .getVersionName(application);
    String baseUrl = Utils.getBaseHttpsUrl(application);
    NetWorkHelper.getInstance().init(application, null, ua, "pbook.namibox.com", baseUrl);
    AppUtil.setBaseUrl(baseUrl);
    initFileDownload(application);
  }

  private static void initFileDownload(Application application) {
    FileDownloader.setupOnApplicationOnCreate(application)
        .connectionCreator(new OkHttp3Connection.Creator(NetWorkHelper.getOkHttpBuilder()))
        .connectionCountAdapter((downloadId, url, path, totalLength) -> 1)
        .commit();
  }

  private static void initLog(Application application) {
    PreferenceUtil.setLogDir(application, FileUtil.LOG_FILE);
    FormatStrategy formatStrategy = LoggerFormatStrategy.newBuilder()
        .tag("[NB]")
        .folder(FileUtil.getLogFile(application))
        .build();
    com.orhanobut.logger.Logger.addLogAdapter(new DiskLogAdapter(formatStrategy));
    FormatStrategy formatStrategy2 = AndroidLogFormatStrategy.newBuilder()
        .tag("[NB]")
        .build();
    com.orhanobut.logger.Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy2) {
      @Override
      public boolean isLoggable(int priority, @Nullable String tag) {
        return isDebug;
      }
    });
    initExceptionHandler();
  }

  private static void initExceptionHandler() {
    UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      StackTraceElement[] trace = e.getStackTrace();
      if (trace != null) {
        Logger.e(e, e.toString());
      }
      defaultHandler.uncaughtException(t, e);
    });
  }

}
