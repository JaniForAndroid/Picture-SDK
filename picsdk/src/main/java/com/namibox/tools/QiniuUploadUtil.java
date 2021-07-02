package com.namibox.tools;

import android.util.Log;
import com.namibox.commonlib.event.QiniuEvent;
import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.KeyGenerator;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.storage.persistent.FileRecorder;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;
import java.io.File;
import java.io.IOException;
import org.json.JSONObject;


/**
 * Created by sunha on 2015/12/3 0003.
 */
public class QiniuUploadUtil {

  private static final String TAG = "QiniuUploadUtil";
  private static boolean canceled = false;

  public static void cancel(boolean cancel) {
    canceled = cancel;
  }

  public static Flowable<QiniuEvent> upload(final String dir, final File file, final String key,
      final String token) {
    cancel(false);
    return Flowable.create(new FlowableOnSubscribe<QiniuEvent>() {
      @Override
      public void subscribe(@NonNull final FlowableEmitter<QiniuEvent> integerEmitter)
          throws Exception {
        FileRecorder recorder;
        try {
          recorder = new FileRecorder(dir);
        } catch (IOException e) {
          if (!integerEmitter.isCancelled()) {
            integerEmitter.onError(new IllegalStateException("初始化上传组件失败"));
          }
          return;
        }
        Configuration.Builder builder = new Configuration.Builder()
            .chunkSize(256 * 1024)  //分片上传时，每片的大小。 默认 256K
            .putThreshhold(1024 * 1024)  // 启用分片上传阀值。
            .connectTimeout(10) // 链接超时。默认 10秒
            .responseTimeout(60)// 服务器响应超时。默认 60秒
            .zone(Zone.zone0);// 设置区域，指定不同区域的上传域名、备用域名、备用IP。默认 Zone.zone0
        builder.recorder(recorder)
            .recorder(recorder, new KeyGenerator() {
              @Override
              public String gen(String key, File file) {
                return key + "_._" + new StringBuffer(file.getAbsolutePath()).reverse();
              }
            });
        Configuration config = builder.build();
        UploadManager uploadManager = new UploadManager(config);
        UploadOptions uploadOptions = new UploadOptions(null, null, false, new UpProgressHandler() {
          @Override
          public void progress(String key, double percent) {
            Log.i(TAG, key + ":" + percent + "  THREAD: " + Thread.currentThread());
            integerEmitter.onNext(new QiniuEvent(percent));
          }
        }, new UpCancellationSignal() {
          @Override
          public boolean isCancelled() {
            return canceled;
          }
        });

        UpCompletionHandler completionHandler = new UpCompletionHandler() {
          @Override
          public void complete(String key, ResponseInfo info, JSONObject response) {
            Log.i(TAG, key + ",\r\n " + info + ",\r\n " + response);
            integerEmitter.onNext(new QiniuEvent(key, info, response));
            integerEmitter.onComplete();
          }
        };
        uploadManager.put(file, key, token, completionHandler, uploadOptions);
      }
    }, BackpressureStrategy.LATEST);
  }

}
