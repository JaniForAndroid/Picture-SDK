package com.namibox.commonlib.activity;

import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.support.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.picsdk.R;
import com.namibox.commonlib.annotation.NoSlideAnim;
import com.namibox.commonlib.dialog.DialogUtil;
import com.namibox.commonlib.dialog.DialogUtil.Action;
import com.namibox.commonlib.dialog.DialogUtil.LoadingDialog;
import com.namibox.commonlib.dialog.DialogUtil.ProgressDialog;
import com.namibox.commonlib.dialog.NamiboxNiceDialog;
import com.namibox.tools.DensityUtils;
import com.namibox.tools.GlideUtil;
import com.namibox.tools.GlideUtil.Callback;
import com.namibox.tools.RxTimerUtil;
import com.namibox.tools.RxTimerUtil.IRxNext;
import com.namibox.util.Logger;
import com.namibox.util.Utils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by sunha on 2017/6/3 0003.
 */

public abstract class AbsFoundationActivity extends AppCompatActivity {

  private static final String TAG = "AbsFoundationActivity";
  protected int themeColor;
  protected int statusbarColor;
  protected int toolbarColor;
  protected int toolbarContentColor;
  public boolean isStopped;
  public NamiboxNiceDialog namiboxNiceDialog;
  public RxTimerUtil mStartLessonDialogTimer;
  private final static int SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT = 0x00000010;


  public interface DestroyListener {

    void onDestroy();
  }

  private List<DestroyListener> destroyListeners = new ArrayList<>();

  public void addDestroyListener(DestroyListener listener) {
    destroyListeners.add(listener);
  }

  public void removeDestroyListener(DestroyListener listener) {
    destroyListeners.remove(listener);
  }

  /**
   * 设置 app 字体不随系统字体设置改变
   */
  @Override
  public Resources getResources() {
    Resources res = super.getResources();
    if (getApplication() != null) {
      DensityUtils.setCustomDensity(getApplication(), getApplication().getResources().getDisplayMetrics(), res.getDisplayMetrics(), Utils.isTablet(getApplication()));
    }
    if (res != null) {
      Configuration config = res.getConfiguration();
      if (config != null && config.fontScale != 1.0f) {
        config.fontScale = 1.0f;
        res.updateConfiguration(config, res.getDisplayMetrics());
      }
    }
    return res;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    if (isSlideAnim() && Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {
      overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
    }
    DensityUtils.setCustomDensity(getApplication(), getApplication().getResources().getDisplayMetrics(), getResources().getDisplayMetrics(), Utils.isTablet(this));
    super.onCreate(savedInstanceState);
    setThemeColor();
  }

  @Override
  protected void onResume() {
    DensityUtils.setCustomDensity(getApplication(), getApplication().getResources().getDisplayMetrics(), getResources().getDisplayMetrics(), Utils.isTablet(this));
    super.onResume();
//    MobclickAgent.onResume(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
//    MobclickAgent.onPause(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    isStopped = false;
  }

  @Override
  protected void onStop() {
    super.onStop();
    isStopped = true;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mStartLessonDialogTimer != null) {
      mStartLessonDialogTimer.cancel();
      mStartLessonDialogTimer = null;
    }
    for (DestroyListener listener : destroyListeners) {
      listener.onDestroy();
    }
  }

  @Override
  public void finish() {
    super.finish();
    if (isSlideAnim() && Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {
      overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
    }
  }

  public boolean isStopped(){
    return isStopped;
  }

  /**
   * 判断activity是否标记了不使用滑入滑出转场动画
   * @return true: 使用 false： 不使用
   */
  private boolean isSlideAnim() {
    Annotation noSlideAnim = this.getClass().getAnnotation(NoSlideAnim.class);
    return noSlideAnim == null;
  }

  private void setOPPOStatusTextColor(boolean lightStatusBar) {
    Window window = getWindow();
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      window.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }
    int vis = window.getDecorView().getSystemUiVisibility();
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      if (lightStatusBar) {
        vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
      } else {
        vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
      }
    } else if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      if (lightStatusBar) {
        vis |= SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT;
      } else {
        vis &= ~SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT;
      }
    }
    window.getDecorView().setSystemUiVisibility(vis);
  }

  public boolean setMiuiStatusBarDarkMode(boolean darkmode) {
    Class<? extends Window> clazz = getWindow().getClass();
    try {
      int darkModeFlag = 0;
      Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
      Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
      darkModeFlag = field.getInt(layoutParams);
      Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
      extraFlagField.invoke(getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
      if (VERSION.SDK_INT >= VERSION_CODES.M && isMIUIV7OrAbove()) {
        //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
        setNativeStatusIcon(darkmode);
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  private boolean isMIUIV7OrAbove() {
    String miuiVersionCodeStr = Utils.getSystemProperty("ro.miui.ui.version.code");
    if (!TextUtils.isEmpty(miuiVersionCodeStr)) {
      try {
        int miuiVersionCode = Integer.parseInt(miuiVersionCodeStr);
        if (miuiVersionCode >= 5) {
          return true;
        }
      } catch (Exception e) {
      }
    }
    return false;
  }

  public void setDarkStatusIcon(boolean bDark) {
    if (Utils.isMIUI() && VERSION.SDK_INT <= VERSION_CODES.N) {
      setMiuiStatusBarDarkMode(bDark);
      return;
    } else if (Build.MANUFACTURER.equalsIgnoreCase("OPPO")) {
      setOPPOStatusTextColor(bDark);
    }
    if (Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {
      setOPPOStatusTextColor(bDark);
    }
    setNativeStatusIcon(bDark);
  }

  private void setNativeStatusIcon(boolean bDark) {
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      getWindow().addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      View decorView = getWindow().getDecorView();
      if (decorView != null) {
        int vis = decorView.getSystemUiVisibility();
        if (bDark) {
          vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
          vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(vis);
      }
    }
  }


  protected void setThemeColor() {
    //自定义主题色
    themeColor = Utils.getThemeColor(this, R.color.theme_color);
    toolbarColor = ContextCompat.getColor(this, R.color.toolbar_color);
    if (VERSION.SDK_INT < VERSION_CODES.M && !Utils.isMIUI()) {
      statusbarColor = ContextCompat.getColor(this, R.color.statusbar_color);
    } else {
      statusbarColor = toolbarColor;
    }
    toolbarContentColor = ContextCompat.getColor(this, R.color.toolbar_content_color);
  }

  public void toast(String msg) {
    if (isStopped) {
      Utils.toast(getApplicationContext(), msg);
    } else {
      Utils.toast(this, msg);
    }
  }

  //获取屏幕方向设置
  public int getScreenOrientation() {
    return getRequestedOrientation();
  }

  //判定横竖屏
  public boolean isPortrait() {
    Configuration configuration = getResources().getConfiguration();
    return configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
  }

  //横屏区分左右方向
  protected void setLandScrape(boolean reverse) {
    setOrientation(reverse ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
  }

  //竖屏只有一个方向
  protected void setPortrait() {
    setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }

  //设置屏幕方向
  protected void setOrientation(int orientation) {
    if (getScreenOrientation() != orientation) {
      setRequestedOrientation(orientation);
    }
  }

  protected void setOrientation(boolean portrait) {
    setRequestedOrientation(portrait ?
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
  }

//  public void showSnackBar(String msg) {
//    View container = findViewById(android.R.id.content);
//    Snackbar.make(container, msg, Snackbar.LENGTH_SHORT).show();
//  }

  public void vibrator() {
    vibrator(30);
  }

  public void vibrator(long ms) {
    //震动
    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    if (vibrator != null && vibrator.hasVibrator()) {
      vibrator.vibrate(ms);
    }
  }

  public void showErrorDialog(String message, OnClickListener clickListener) {
    showDialog(getString(R.string.common_tip), message, getString(R.string.common_confirm), clickListener);
  }

  public void showErrorDialog(String message, final boolean finish) {
    showErrorDialog(message, new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (finish) {
          finish();
        }
      }
    });
  }

  public void showTipsDialog(String title, String message) {
    showDialog(title, message, "确定", null);
  }

  public void showDialog(
      String title, CharSequence message, String action1, OnClickListener listener1) {
    DialogUtil.showButtonDialog(this, title, message, action1, listener1, null);
  }

  public void showDialog(
      String title, CharSequence message, String action1, OnClickListener listener1,
      OnClickListener cancelListener) {
    DialogUtil.showButtonDialog(this, title, message, action1, listener1, cancelListener);
  }

  public void showDialog(
      String title, CharSequence message, String action1, OnClickListener listener1,
      String action2, OnClickListener listener2) {
    DialogUtil.showButtonDialog(this, title, message, action1, listener1, action2, listener2, null);
  }

  public void showDialog(
      String title, CharSequence message, String action1, OnClickListener listener1,
      String action2, OnClickListener listener2, OnClickListener cancelListener) {
    DialogUtil.showButtonDialog(this, title, message, action1, listener1, action2, listener2, cancelListener);
  }

  public void showDialog(
      String title, CharSequence message, String action1, OnClickListener listener1,
      String action2, OnClickListener listener2, String action3, OnClickListener listener3) {
    DialogUtil.showButtonDialog(this, title, message, action1, listener1,
        action2, listener2, action3, listener3, null);
  }

  public void showDialog(
      String title, CharSequence message, String action1, OnClickListener listener1,
      String action2, OnClickListener listener2, String action3, OnClickListener listener3,
      OnClickListener cancelListener) {
    DialogUtil.showButtonDialog(this, title, message, action1, listener1,
        action2, listener2, action3, listener3, cancelListener);
  }

  public void showContentLeftDialog(String title, CharSequence message, String action, OnClickListener listener) {
    DialogUtil.showButtonDialog2(this, title, message, action,
        listener, null, null, null, null, null);
  }

  public void showQueueCommonDialog(AppCompatActivity activity, final String title,
      final CharSequence content,
      final String action1, final OnClickListener listener1, final String action2,
      final OnClickListener listener2,
      final String action3, final OnClickListener listener3, final OnClickListener cancelListener) {
    namiboxNiceDialog = DialogUtil
        .getCommonDialog(activity, title, content, action1, listener1, action2, listener2, action3, listener3,
            cancelListener);
  }

  public void showExtraDialog(
      String title, CharSequence message, String action1, OnClickListener listener1,
                         String action2, OnClickListener listener2) {
    DialogUtil
        .showExtraButtonDialog(this, title, message, action1, listener1, action2, listener2, null);
  }

  /**
   * 拦截通用弹框，处理图片，img支持url\drawable\本地资源（插件中除外）
   */
  public void showImageDialog(final String title,
      final String content, final Object contentImage,
      final Action action, final Action action1, final Action action2,
      final OnClickListener closeListener, boolean isNotify) {
    showImageDialog(AbsFoundationActivity.this, title, content, contentImage, action, action1, action2,
        closeListener);
  }

  /**
   * 拦截通用弹框，处理图片，img支持url\drawable\本地资源（插件中除外）
   */
  protected void showImageDialog(final AppCompatActivity activity, final String title,
      final String content, final Object contentImage,
      final Action action, final Action action1, final Action action2,
      final OnClickListener closeListener) {

    Disposable disposable = createImageLoadObservable(activity, contentImage)
        .subscribe(new Consumer<DrawableResult>() {
          @Override
          public void accept(DrawableResult drawableResult) throws Exception {
            if (mStartLessonDialogTimer != null) {
              mStartLessonDialogTimer.cancel();
              mStartLessonDialogTimer = null;
            }
            if (action != null && TextUtils.equals(action.action, "去上课")) {
              mStartLessonDialogTimer = new RxTimerUtil();
              mStartLessonDialogTimer.timer(30 * 60 * 1000, new IRxNext() {
                @Override
                public void onTick(Long s) {

                }

                @Override
                public void onFinish() {
                  if (namiboxNiceDialog != null) {
                    namiboxNiceDialog.dismissAllowingStateLoss();
                    imageDialogDismiss();
                  }
                }
              });
            }
            namiboxNiceDialog = DialogUtil.showImageDialog(activity, null,
                title, content, drawableResult.drawable, action, action1, action2, new OnClickListener() {
                  @Override
                  public void onClick(View view) {
                    if (closeListener != null) {
                      closeListener.onClick(view);
                    }
                    if (mStartLessonDialogTimer != null) {
                      mStartLessonDialogTimer.cancel();
                      mStartLessonDialogTimer = null;
                    }
                    imageDialogDismiss();
                  }
                });
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) throws Exception {
            Logger.e(throwable.toString());
          }
        });
  }

  private static class DrawableResult {

    Drawable drawable;

    public DrawableResult(Drawable drawable) {
      this.drawable = drawable;
    }
  }

  private Observable<DrawableResult> createImageLoadObservable(final AppCompatActivity activity, final Object image) {
    return Observable.create(new ObservableOnSubscribe<DrawableResult>() {
      @Override
      public void subscribe(final ObservableEmitter<DrawableResult> observableEmitter)
          throws Exception {
        if (image == null) {
          observableEmitter.onNext(new DrawableResult(null));
          observableEmitter.onComplete();
        } else if (image instanceof Drawable) {
          Drawable drawable = (Drawable) image;
          observableEmitter.onNext(new DrawableResult(drawable));
          observableEmitter.onComplete();
        } else {
          GlideUtil.loadDrawable(activity, image, 0, 0, true,
              GlideUtil.DATA, new Callback() {
                @Override
                public void onResourceReady(Drawable resource) {
                  observableEmitter.onNext(new DrawableResult(resource));
                  observableEmitter.onComplete();
                }

                @Override
                public void onLoadFailed(Drawable errorDrawable) {
                  //observableEmitter.onNext(null);
                  if (!observableEmitter.isDisposed()) {
                    observableEmitter.onError(new Throwable("onLoadFailed"));
                  }
                }
              });
        }
      }
    });
  }

  public void showBigImageDialog(final Object large_img, final Action action,
      final OnClickListener closeListener, boolean isNotify) {
    showBigImageDialog(this, large_img, action, closeListener);
  }

  protected void showBigImageDialog(final AppCompatActivity activity, final Object large_img, final Action action,
      final OnClickListener closeListener) {
    Glide.with(activity)
        .load(large_img)
        .listener(new RequestListener<Drawable>() {
          @Override
          public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
              boolean isFirstResource) {
            return false;
          }

          @Override
          public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
              DataSource dataSource,
              boolean isFirstResource) {
            namiboxNiceDialog = DialogUtil
                .showBigImageDialog(activity, large_img, action, new OnClickListener() {
              @Override
              public void onClick(View view) {
                if (closeListener != null) {
                  closeListener.onClick(view);
                }
                imageDialogDismiss();
              }
            });
            return false;
          }
        })
        .preload();
  }

  protected void showEnvelopeDialog(String title, String context, final Action action,
      final OnClickListener closeListener) {
    showEnvelopeDialog(this, title, context, action, closeListener);
  }

  protected void showEnvelopeDialog(final AppCompatActivity activity, String title, String content, final Action action,
      final OnClickListener closeListener) {
    namiboxNiceDialog = DialogUtil.showEnvelopeDialog(activity, title, content,
        action, new OnClickListener() {
          @Override
          public void onClick(View view) {
            if (closeListener != null) {
              closeListener.onClick(view);
            }
            imageDialogDismiss();
          }
        });
  }

  /** 报名版二维码对话框 */
  public void showQRImageDialog(final AppCompatActivity activity, final Object large_img,
      final OnClickListener closeListener) {
    Glide.with(activity)
        .load(large_img)
        .listener(new RequestListener<Drawable>() {
          @Override
          public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
              boolean isFirstResource) {
            return false;
          }

          @Override
          public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
              DataSource dataSource,
              boolean isFirstResource) {
            DialogUtil.showQrImageDialog(activity, large_img, closeListener);
            return false;
          }
        })
        .preload();
  }

  /**
   * 应用内小图推送对话框，img支持url\drawable\本地资源（插件中除外）
   */
  public void showSmallImageDialog(final Object bgImage, final String title,
      final String content, final Object smallImage, final Action action,
      final OnClickListener closeListener, boolean isNotify) {
    showSmallImageDialog(this, bgImage, title, content, smallImage, action, closeListener);
  }

  /**
   * 应用内小图推送对话框，img支持url\drawable\本地资源（插件中除外）
   */
  protected void showSmallImageDialog(final AppCompatActivity activity, final Object bgImage, final String title,
      final String content, final Object smallImage, final Action action,
      final OnClickListener closeListener) {

    Observable<DrawableResult> headerObservable = createImageLoadObservable(activity, bgImage);
    Observable<DrawableResult> contentObservable = createImageLoadObservable(activity, smallImage);

    Disposable disposable = Observable
        .zip(headerObservable, contentObservable, new BiFunction<DrawableResult, DrawableResult, Drawable[]>() {
          @Override
          public Drawable[] apply(DrawableResult headerComplete, DrawableResult contentComplete) throws Exception {
            return new Drawable[]{headerComplete.drawable, contentComplete.drawable};
          }
        })
        .subscribe(new Consumer<Drawable[]>() {
          @Override
          public void accept(Drawable[] complete) throws Exception {
            namiboxNiceDialog = DialogUtil.showSmallImageDialog(activity, complete[0], complete[1],
                title, content, action, new OnClickListener() {
                  @Override
                  public void onClick(View view) {
                    if (closeListener != null) {
                      closeListener.onClick(view);
                    }
                    imageDialogDismiss();
                  }
                });
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) throws Exception {
            Logger.e(throwable.toString());
          }
        });
  }

  protected void imageDialogDismiss() {
    namiboxNiceDialog = null;
  }

  private LoadingDialog loadingDialog;

  protected LoadingDialog getLoadingDialog() {
    return loadingDialog;
  }

  public void showProgress(String message) {
    if (this.isFinishing()) {
      return;
    }
    hideProgress();
    loadingDialog = DialogUtil.showLoadingDialog(this, message, false, null);
  }

  public void showCancelableProgress(String message, final OnCancelListener cancelListener) {
    if (this.isFinishing()) {
      return;
    }
    hideProgress();
    loadingDialog = DialogUtil.showLoadingDialog(this, message, true, cancelListener);
  }

  public void updateProgress(String message) {
    if (this.isFinishing()) {
      return;
    }
    if (loadingDialog != null) {
      loadingDialog.setText(message);
    }
  }

  public void hideProgress() {
    if (this.isFinishing()) {
      return;
    }
    if (loadingDialog != null) {
      loadingDialog.dismissAllowingStateLoss();
      loadingDialog = null;
    }
  }

  ProgressDialog progressDialog;

  public void showDeterminateProgress(String title, String message) {
    showDeterminateProgress(title, message, null, null);
  }

  public void showDeterminateProgress(String title, String message, String action,
      OnClickListener clickListener) {
    if (this.isFinishing()) {
      return;
    }
    hideDeterminateProgress();
    progressDialog = DialogUtil.showProgressDialog(this, title, message, action, clickListener);
  }

  public void hideDeterminateProgress() {
    if (this.isFinishing()) {
      return;
    }
    if (progressDialog != null && !isFinishing()) {
      progressDialog.dismissAllowingStateLoss();
      progressDialog = null;
    }
  }

  public void updateDeterminateProgress(String msg, int percent) {
    if (this.isFinishing()) {
      return;
    }
    if (progressDialog != null && !isFinishing()) {
      progressDialog.setText(msg);
      progressDialog.setProgress(percent);
    }
  }


  DialogUtil.ProgressDialog1 progressDialog1;

  public void showDeterminateProgress1(String title, String message, String action,
                                      OnClickListener clickListener,int width, int height) {
    if (this.isFinishing()) {
      return;
    }
    hideDeterminateProgress1();
    progressDialog1 = DialogUtil
        .showProgressDialog1(this, title, message, action, clickListener,width, height);
  }

  public void hideDeterminateProgress1() {
    if (this.isFinishing()) {
      return;
    }
    if (progressDialog1 != null && !isFinishing()) {
      progressDialog1.dismissAllowingStateLoss();
      progressDialog1 = null;
    }
  }

  public void updateDeterminateProgress1(String msg, int percent) {
    if (this.isFinishing()) {
      return;
    }
    if (progressDialog1 != null && !isFinishing()) {
      progressDialog1.setText(msg);
      progressDialog1.setProgress(percent);
    }
  }

}
