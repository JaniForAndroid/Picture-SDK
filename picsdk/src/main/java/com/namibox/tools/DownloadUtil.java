package com.namibox.tools;

import com.namibox.util.network.NetWorkHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;


/**
 * author : feng
 * creation time : 20-1-6下午5:21
 */
public class DownloadUtil {

  public static void downloadFile(final String url, final File file,
      final OnDownloadListener listener) {
    Observable.create(new ObservableOnSubscribe<Float>() {
      @Override
      public void subscribe(ObservableEmitter<Float> e) throws Exception {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        try {
          File tmpFile = new File(file.getAbsolutePath() + "_tmp");
          OkHttpClient okHttpClient = NetWorkHelper.getOkHttpClient();
          long startPos = tmpFile.exists() ? tmpFile.length() : 0;
          Request request = new Builder()
              .cacheControl(CacheControl.FORCE_NETWORK)
              .url(url)
              .header("RANGE", "bytes=" + startPos + "-")
              .addHeader("Accept-Encoding", "deflate")
              .addHeader("Accept", "*/*")
              .build();
          Response response = okHttpClient
              .newCall(request)
              .execute();
          if (response.isSuccessful() && response.body() != null) {
            inputStream = response.body().byteStream();
            randomAccessFile = new RandomAccessFile(tmpFile, "rw");
            randomAccessFile.seek(startPos);
            byte[] buffer = new byte[10 * 1024];
            long total = startPos + response.body().contentLength();
            long begin = startPos;
            float progress = begin / (float) total;
            int c;
            while ((c = inputStream.read(buffer)) > 0) {
              randomAccessFile.write(buffer, 0, c);
              begin += c;
              if (begin / (float) total - progress > 0.01) {
                progress = begin / (float) total;
                e.onNext(progress);
              }
            }
            if (begin == total) {
              e.onNext(1f);
              //noinspection ResultOfMethodCallIgnored
              tmpFile.renameTo(file);
              e.onComplete();
            }
          } else {
            e.onError(new Exception(response.toString()));
          }
        } catch (Exception ex) {
          e.onError(ex);
        } finally {
          if (inputStream != null) {
            inputStream.close();
          }
          if (randomAccessFile != null) {
            randomAccessFile.close();
          }
        }
      }
    })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Float>() {
          @Override
          public void onSubscribe(Disposable disposable) {
            if (listener != null) {
              listener.onStart();
            }
          }

          @Override
          public void onNext(Float progress) {
            if (listener != null) {
              listener.onProgress(progress);
            }
          }

          @Override
          public void onError(Throwable e) {
            LoggerUtil.e(e, "保存短视频出错");
            if (listener != null) {
              listener.onError();
            }
          }

          @Override
          public void onComplete() {
            if (listener != null) {
              listener.onComplete();
            }
          }
        });

  }

  public interface OnDownloadListener {

    void onStart();

    void onProgress(float progress);

    void onComplete();

    void onError();

  }
}
