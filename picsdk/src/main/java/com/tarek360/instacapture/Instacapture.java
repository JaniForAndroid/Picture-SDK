package com.tarek360.instacapture;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.tarek360.instacapture.exception.ActivityNotRunningException;
import com.tarek360.instacapture.listener.ScreenCaptureListener;
import com.tarek360.instacapture.screenshot.ScreenshotProvider;
import com.tarek360.instacapture.utility.Logger;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;


/**
 * Created by tarek on 5/20/17.
 */

public class Instacapture {

  private static final String MESSAGE_IS_ACTIVITY_RUNNING = "Is your activity running?";
  private static final String ERROR_SCREENSHOT_CAPTURE_FAILED = "Screenshot capture failed";

  public static void capture(@NonNull Activity activity,
      @NonNull final ScreenCaptureListener screenCaptureListener,
      @Nullable View... ignoredViews) {

    screenCaptureListener.onCaptureStarted();

    captureRx(activity, ignoredViews).subscribe(new DefaultObserver<Bitmap>() {
      @Override
      public void onComplete() {
      }

      @Override
      public void onError(final Throwable e) {
        Logger.e(ERROR_SCREENSHOT_CAPTURE_FAILED);
        e.printStackTrace();
        screenCaptureListener.onCaptureFailed(e);
      }


      @Override
      public void onNext(final Bitmap bitmap) {
        screenCaptureListener.onCaptureComplete(bitmap);
      }
    });

  }

  public static Observable<Bitmap> captureRx(@NonNull Activity activity,
      @Nullable View... ignoredViews) {

    ActivityReferenceManager activityReferenceManager = new ActivityReferenceManager();
    activityReferenceManager.setActivity(activity);

    final Activity validatedActivity = activityReferenceManager.getValidatedActivity();
    if (validatedActivity == null) {
      return Observable.error(new ActivityNotRunningException(MESSAGE_IS_ACTIVITY_RUNNING));
    }

    ScreenshotProvider screenshotProvider = new ScreenshotProvider();

    return screenshotProvider.getScreenshotBitmap(activity, ignoredViews)
        .observeOn(AndroidSchedulers.mainThread());
  }

  /**
   * Get single tone instance.
   *
   * @param activity .
   * @return Instacapture single tone instance.
   */

  /**
   * Enable logging or disable it.
   *
   * @param enable set it true to enable logging.
   */
  public static void enableLogging(boolean enable) {
    if (enable) {
      Logger.enable();
    } else {
      Logger.disable();
    }
  }

}