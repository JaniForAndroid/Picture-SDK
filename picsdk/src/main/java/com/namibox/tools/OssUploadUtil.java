package com.namibox.tools;

import android.content.Context;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.event.OssEvent;
import com.namibox.commonlib.exception.MessageException;
import com.namibox.commonlib.model.BaseNetResult;
import com.namibox.commonlib.model.OssToken;
import com.namibox.util.Logger;
import com.namibox.util.Utils;
import fairy.easy.httpmodel.HttpModelHelper;
import fairy.easy.httpmodel.model.HttpNormalUrlLoader;
import fairy.easy.httpmodel.resource.HttpListener;
import fairy.easy.httpmodel.resource.HttpType;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.json.JSONObject;

/**
 * Create time: 2017/6/14.
 */

public class OssUploadUtil {

  private static final String TAG = "OssUploadUtil";

  public static Flowable<OssEvent> getOssObservable(final Context context,
      final OssToken ossToken) {
    return Flowable.create(new FlowableOnSubscribe<OssEvent>() {
      @Override
      public void subscribe(@NonNull final FlowableEmitter<OssEvent> emitter) throws Exception {
        OSSStsTokenCredentialProvider provider = new OSSStsTokenCredentialProvider(
            ossToken.AccessKeyId, ossToken.AccessKeySecret, ossToken.SecurityToken);
        OSSClient oss = new OSSClient(context, ossToken.endpoint, provider);

        PutObjectRequest put = new PutObjectRequest(ossToken.bucketName, ossToken.objectKey,
            ossToken.uploadFile.getAbsolutePath());
        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
          @Override
          public void onProgress(PutObjectRequest putObjectRequest, long current, long total) {
            emitter.onNext(new OssEvent(ossToken.uploadFile, current, total));
          }
        });
        final OSSAsyncTask<PutObjectResult> task = oss
            .asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {

              @Override
              public void onSuccess(PutObjectRequest putObjectRequest,
                  PutObjectResult putObjectResult) {
                emitter.onNext(new OssEvent(ossToken.uploadFile, putObjectRequest.getObjectKey()));
                emitter.onComplete();
              }

              @Override
              public void onFailure(PutObjectRequest putObjectRequest,
                  ClientException clientException,
                  ServiceException serviceException) {
                Logger.e(TAG,
                    "onFailure, isCancelled=" + emitter.isCancelled() + ", ClientException: "
                        + clientException
                        + ", ServiceException: " + serviceException);
                uploadOssError(clientException, serviceException, context, ossToken);

                if (!emitter.isCancelled()) {
                  emitter.onError(new MessageException("上传失败"));
                }
              }
            });
        emitter.setCancellable(new Cancellable() {
          @Override
          public void cancel() throws Exception {
            if (task != null && !task.isCompleted()) {
              task.cancel();
            }
          }
        });
      }
    }, BackpressureStrategy.LATEST)
        .subscribeOn(Schedulers.newThread());
  }

  public static void uploadOssError(ClientException clientException,
      ServiceException serviceException, Context context, OssToken ossToken) {
    //oss直传失败错误上报
    final JsonObject body = new JsonObject();
    if (Utils.isLogin(context)) {
      body.add("userId", new JsonPrimitive(Utils.getLoginUserId(context)));
    }
    JsonElement data = new Gson().toJsonTree(ossToken);
    body.add("data", data);
    StringBuilder sb = new StringBuilder();
    if (clientException != null) {
      sb.append(clientException.getMessage());
    }
    if (serviceException != null) {
      sb.append(serviceException.toString());
    }
    body.add("errMsg", new JsonPrimitive(sb.toString()));

    HttpModelHelper.getInstance()
        .init(context.getApplicationContext())
        .setChina(true)
        .setModelLoader(new HttpNormalUrlLoader())
        .setFactory()
        .addType(HttpType.NET)
        .addType(HttpType.PING)
        .addType(HttpType.TRACE_ROUTE)
        .build()
        .startAsync("oss-cn-hangzhou.aliyuncs.com", new HttpListener() {

          @Override
          public void onSuccess(HttpType httpType, JSONObject result) {

          }

          @Override
          public void onFail(String data) {
            uploadOssError(body);
          }

          @Override
          public void onFinish(JSONObject result) {
            body.add("checkInfo", new JsonParser().parse(result.toString()));
            uploadOssError(body);
          }
        });
  }

  private static void uploadOssError(JsonObject body) {
    Logger.d(TAG, "上传网络信息: " + body.toString());
    //noinspection unused
    Disposable disposable = ApiHandler.getBaseApi().uploadOssError(body)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<BaseNetResult>() {
          @Override
          public void accept(BaseNetResult baseNetResult) throws Exception {
            Logger.d("OSS", "上传成功 errcode = " + baseNetResult.errcode + ", errmsg = "
                + baseNetResult.errmsg);
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) throws Exception {
            Logger.d("OSS", "上传失败：" + throwable.getMessage());
          }
        });
  }
}
