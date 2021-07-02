package sdk;

import android.app.Activity;
import android.arch.lifecycle.LifecycleObserver;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.example.picsdk.event.RefreshStoreInfo;
import com.example.picsdk.util.PicturePreferenceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.event.PicRefreshUserInfo;
import com.namibox.tools.DeviceInfoUtil;
import com.namibox.util.AppUtil;
import com.namibox.util.Logger;
import com.namibox.util.PreferenceUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import sdk.callback.AccessTokenCallback;
import sdk.callback.FakeLoginCallback;
import sdk.callback.GetPictureInfoCallback;
import sdk.model.PictureBean;
import sdk.model.PictureDetailBean;
import sdk.model.RegisterModel;

@Keep
public class SDKDemoHelper implements LifecycleObserver {

  private static final String TAG = "SDKDemoHelper";

  public void login(Activity activity, RegisterModel model, FakeLoginCallback callback) {
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
                if (userInfo.has("sessionid")) {
                  String userSession = userInfo.get("sessionid").getAsString();
                  PreferenceUtil.setSessionId(activity, userSession);
                }
              }
              if (data.has("code")) {
                String code = data.get("code").getAsString();
                if (TextUtils.equals(code, "1000")) {
                  if (callback != null) {
                    callback.onSuccess();
                  }
                } else {
                  if (callback != null) {
                    callback.onFail(new Throwable(des));
                  }
                }
              } else {
                if (callback != null) {
                  callback.onFail(new Throwable(des));
                }
              }
            }
          } else {
            Logger.e(TAG, "login接口报错");
            if (callback != null) {
              callback.onFail(new Throwable("登录接口报错：" + retcode + "，请重试"));
            }
          }
        }, throwable -> {
          Logger.e(TAG, throwable, "login 报错");
          if (callback != null) {
            callback.onFail(throwable);
          }
        }).isDisposed();
  }

  public void logout(Activity activity) {
    String url = AppUtil.getBaseUrl() + "/auth/logout";
    ApiHandler.getBaseApi().commonJsonPost(url)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonElement -> {
          JsonObject jsonObject = jsonElement.getAsJsonObject();
          String retcode = jsonObject.get("retcode").getAsString();
          if (TextUtils.equals(retcode, "SUCC")) {
            PicturePreferenceUtil.onLogout(activity);
            Toast.makeText(activity,"退出登录成功",Toast.LENGTH_SHORT).show();
          } else {
            String description = jsonObject.get("description").getAsString();
            Toast.makeText(activity,description,Toast.LENGTH_SHORT).show();
          }
        }, throwable -> {
          Logger.e(throwable, throwable.toString());
          Toast.makeText(activity,"退出登录失败",Toast.LENGTH_SHORT).show();
        }).isDisposed();
  }

  public void getAccessToken(String appCode, String phoneNum, long timestamp, String chk
      , AccessTokenCallback callback) {
    String url = AppUtil.getBaseUrl() + "/auth/access-token";
    JsonObject js = new JsonObject();
    js.addProperty("app_code", appCode);
    js.addProperty("phone", phoneNum);
    js.addProperty("timestamp", timestamp);
    js.addProperty("chk", chk);
    Logger.d(TAG, "access token 参数:" + js);

    ApiHandler.getBaseApi().commonJsonPost(url, js)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          Logger.d(TAG, "getAccessToken 结果：" + jsonObject);
          JsonObject result = (JsonObject) jsonObject;
          String retcode = result.get("retcode").getAsString();
          if (TextUtils.equals(retcode, "SUCC")) {
            String token = "";
            if (result.has("data")) {
              JsonObject data = result.get("data").getAsJsonObject();
              if (data.has("access_token")) {
                token = data.get("access_token").getAsString();
              }
            }

            if (callback != null) {
              callback.onResult(token);
            }
          } else {
            Log.e(TAG, "access token报错");
            if (callback != null) {
              callback.onError("access token 报错");
            }
          }
        }, throwable -> {
          Logger.e(throwable, throwable.toString());
          if (callback != null) {
            callback.onError("access token 报错：" + throwable.toString());
          }
        }).isDisposed();
  }

  public void getPictureBookList(Activity activity, String token, GetPictureInfoCallback callback) {
    String url = AppUtil.getBaseUrl() + "/pbook/picbook/list?access_token="+token;
    Logger.d(TAG, "getPictureBookList url:" + url);

    ApiHandler.getBaseApi().commonJsonGet(url)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          Logger.d(TAG, "getPictureBookList 结果：" + jsonObject);
          JsonObject result = (JsonObject) jsonObject;
          String retcode = result.get("retcode").getAsString();
          if (TextUtils.equals(retcode, "SUCC")) {
            List<PictureBean> list = new ArrayList<>();
            if (result.has("data")) {
              JsonObject data = result.get("data").getAsJsonObject();
              if (data.has("启蒙")) {
                JsonArray pre = data.get("启蒙").getAsJsonArray();
                List<PictureBean> preList = new Gson().fromJson(pre,new TypeToken<List<PictureBean>>() {
                }.getType());
                for (PictureBean pictureBean : preList) {
                  pictureBean.level = "启蒙";
                }
                list.addAll(preList);
              }
              if (data.has("起步1段")) {
                JsonArray pre1 = data.get("起步1段").getAsJsonArray();
                List<PictureBean> preList = new Gson().fromJson(pre1,new TypeToken<List<PictureBean>>() {
                }.getType());
                for (PictureBean pictureBean : preList) {
                  pictureBean.level = "起步1段";
                }
                list.addAll(preList);
              }
              if (data.has("起步2段")) {
                JsonArray pre2 = data.get("起步2段").getAsJsonArray();
                List<PictureBean> preList = new Gson().fromJson(pre2,new TypeToken<List<PictureBean>>() {
                }.getType());
                for (PictureBean pictureBean : preList) {
                  pictureBean.level = "起步2段";
                }
                list.addAll(preList);
              }
              if (data.has("起步3段")) {
                JsonArray pre3 = data.get("起步3段").getAsJsonArray();
                List<PictureBean> preList = new Gson().fromJson(pre3,new TypeToken<List<PictureBean>>() {
                }.getType());
                for (PictureBean pictureBean : preList) {
                  pictureBean.level = "起步3段";
                }
                list.addAll(preList);
              }

              if (callback != null) {
                callback.onSuccess(list);
              }
            }
          } else {
            Logger.e(TAG, "getPictureBookList接口报错");
          }
        }, throwable -> {
          Logger.e(TAG, throwable, "getPictureBookList 报错");
        }).isDisposed();
  }

  public void getPictureDetail(Activity activity, String token,String product_id, GetPictureInfoCallback callback) {
    String url = AppUtil.getBaseUrl() + "/pbook/picbook/detail?access_token="+token+"&product_id="+product_id;
    Logger.d(TAG, "getPictureDetail url:" + url);

    ApiHandler.getBaseApi().commonJsonGet(url)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          Logger.d(TAG, "getPictureDetail 结果：" + jsonObject);
          JsonObject result = (JsonObject) jsonObject;
          String retcode = result.get("retcode").getAsString();
          if (TextUtils.equals(retcode, "SUCC")) {
            if (result.has("data")) {
              JsonObject data = result.get("data").getAsJsonObject();
              if (data.has("structure")) {
                JsonArray structure = data.get("structure").getAsJsonArray();
                List<PictureDetailBean> list = new Gson()
                    .fromJson(structure, new TypeToken<List<PictureDetailBean>>() {
                    }.getType());
                if (callback != null) {
                  callback.onDetailSuccess(list);
                }
              }
            }
          } else {
            Logger.e(TAG, "getPictureDetail接口报错");
          }
        }, throwable -> {
          Logger.e(TAG, throwable, "getPictureDetail 报错");

        }).isDisposed();
  }


}
