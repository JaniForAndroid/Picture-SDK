package com.tarek360.instacapture.screenshot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.tarek360.instacapture.exception.IllegalScreenSizeException;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.internal.operators.observable.ObservableFromCallable;
import java.util.concurrent.Callable;


/**
 * Created by tarek on 5/17/16.
 */
public final class ViewsBitmapObservable {

  private ViewsBitmapObservable() {

  }

  @NonNull
  public static Observable<Bitmap> get(@NonNull final Activity activity,
      @Nullable final View[] removedViews) {

    return Observable.defer(new ObservableFromCallable<ObservableSource<? extends Bitmap>>(
        new Callable<ObservableSource<Bitmap>>() {
          @Override
          public ObservableSource<Bitmap> call() throws Exception {
            Bitmap screenBitmap = ScreenshotTaker.getScreenshotBitmap(activity, removedViews);
            if (screenBitmap != null) {
              return Observable.just(screenBitmap);
            } else {
              return Observable.error(new IllegalScreenSizeException());
            }
          }
        }));
  }
}
