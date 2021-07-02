package com.namibox.tools;

import android.support.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;

/**
 * Create time: 2017/12/7.
 */

public class RxTimerUtil {

  private Disposable mDisposable;

  /**
   * milliseconds毫秒后执行next操作
   */
  public void timer(long milliseconds, final IRxNext next) {
    Observable.timer(milliseconds, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Long>() {
          @Override
          public void onSubscribe(@NonNull Disposable disposable) {
            mDisposable = disposable;
          }

          @Override
          public void onNext(@NonNull Long number) {
            if (next != null && !mDisposable.isDisposed()) {
              next.onFinish();
            }
          }

          @Override
          public void onError(@NonNull Throwable e) {
            //取消订阅
            cancel();
          }

          @Override
          public void onComplete() {
            //取消订阅
            cancel();
          }
        });
  }


  /**
   * 每隔milliseconds毫秒执行next操作，运行count次(IO线程运行)
   */
  public void interval(final long ms, final IRxNext next) {
    Observable.interval(20, TimeUnit.MILLISECONDS)
        .take(ms / 20)
        .map(new Function<Long, Long>() {
          @Override
          public Long apply(Long index) throws Exception {
            //剩余时间s
            return (ms - (index + 1) * 20) / 1000 + 1;
          }
        })
        .distinct()
        //.subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Long>() {
          @Override
          public void onSubscribe(@NonNull Disposable disposable) {
            mDisposable = disposable;
          }

          @Override
          public void onNext(@NonNull Long s) {
            if (next != null && !mDisposable.isDisposed()) {
              next.onTick(s);
            }
          }

          @Override
          public void onError(@NonNull Throwable e) {

          }

          @Override
          public void onComplete() {
            if (next != null && !mDisposable.isDisposed()) {
              next.onFinish();
            }
          }
        });
  }

  /**
   * 取消订阅
   */
  public void cancel() {
    if (mDisposable != null && !mDisposable.isDisposed()) {
      mDisposable.dispose();
    }
  }

  public interface IRxNext {
    void onTick(Long s);
    void onFinish();
  }

}
