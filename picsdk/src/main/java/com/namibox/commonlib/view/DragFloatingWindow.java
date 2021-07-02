package com.namibox.commonlib.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.picsdk.R;
import com.namibox.tools.GlideUtil;
import com.namibox.util.Utils;
import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.Locale;

/**
 * @author: wzp
 */
public class DragFloatingWindow extends FrameLayout {

  private int parentWidth;
  private int parentHeight;
  private int screenWidthHalf;
  private int statusHeight;
  private int bottomStatusHeight;//底部虚拟导航栏高度
  private boolean firstMove = true;
  private FWFinalLocationListener locationListener;
  private FWCountdownGoLessonListener goLessonListener;
  private FWAnimTypeListener animChangeListener;
  FWCountdownFinishListener finishListener;
  private TextView tv_main_tips;
  private ImageView iv_bg;
  private Context context;
  private CountDownTimer timer;//浮框倒计时
  private int lastX;
  private int lastY;
  private boolean isDrag;
  boolean isConfigurationChanged = false;
  int lastWidth = 0;
  float appX = -1, appY = -1;
  boolean isSetLocation = false;
  boolean isSetLocationAndOriChanged = false;
  int lastHeight = 0;
  private int marginBottom = 0;
  private boolean isAminInfinite;

  public DragFloatingWindow(Context context) {
    super(context);
    init(context);
  }

  public DragFloatingWindow(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public DragFloatingWindow(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    this.context = context;
    marginBottom = Utils.dp2px(context, 90);
    statusHeight = Utils.getStatusBarHeight(context);
    bottomStatusHeight = getBottomStatusHeight(context);
    View view = LayoutInflater.from(context).inflate(R.layout.fw_layout, this);
    tv_main_tips = view.findViewById(R.id.tv_main_tips);
    iv_bg = view.findViewById(R.id.iv_bg);
    initFwText();
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    parentHeight = ((ViewGroup) getParent()).getMeasuredHeight();
    parentWidth = ((ViewGroup) getParent()).getMeasuredWidth();
    screenWidthHalf = parentWidth / 2;
    if (bottomStatusHeight != 0) {
      int orientation = getResources().getConfiguration().orientation;
      if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        parentHeight = parentHeight - bottomStatusHeight;
      } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        parentWidth = parentWidth - bottomStatusHeight;
      }
    }

    if ((getX() > (parentWidth - getWidth()) || getY() > (parentHeight - getHeight()))
        || (isConfigurationChanged && lastWidth != parentWidth)
        || isSetLocationAndOriChanged) {
      setX(parentWidth - getWidth());
      setY(parentHeight - getHeight() - marginBottom);
      isConfigurationChanged = false;
      isSetLocationAndOriChanged = false;
      lastWidth = parentWidth;
    } else if (isSetLocation) {
      if ((appX > (parentWidth - getWidth()) || appY > (parentHeight - getHeight()))
          || (appX == -1 && appY == -1)) {
        setX(parentWidth - getWidth());
        setY(parentHeight - getHeight() - marginBottom);
      } else {
        setX(appX);
        setY(appY);
      }
      isSetLocation = false;
    }
  }

  @Override
  protected void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    bottomStatusHeight = getBottomStatusHeight(context);
    isConfigurationChanged = true;
    lastWidth = parentWidth;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int rawX = (int) event.getRawX();
    int rawY = (int) event.getRawY();
    switch (event.getAction() & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:
        isDrag = false;
        firstMove = true;
        getParent().requestDisallowInterceptTouchEvent(true);
        lastX = rawX;
        lastY = rawY;
        break;
      case MotionEvent.ACTION_MOVE:
        isDrag = true;
        //计算手指移动了多少
        int dx = rawX - lastX;
        int dy = rawY - lastY;
        //这里修复一些手机无法触发点击事件的问题
        int distance = (int) Math.sqrt(dx * dx + dy * dy);
//        LoggerUtil.e("distance---->", distance + "");
        //给个容错范围，不然有部分手机还是无法点击
        if (distance < 3 && firstMove) {
          isDrag = false;
          break;
        }
        if (firstMove) {
          firstMove = false;
        }
        float x = getX() + dx;
        float y = getY() + dy;

        //检测是否到达边缘 左上右下
        x = x < 0 ? 0 : (x > parentWidth - getWidth() ? parentWidth - getWidth() : x);
        // y = y < statusHeight ? statusHeight : (y + getHeight() >= parentHeight ? parentHeight - getHeight() : y);
        if (y < statusHeight) {
          y = statusHeight;
        }
        if (y > parentHeight - getHeight()) {
          y = parentHeight - getHeight();
        }
        setX(x);
        setY(y);

        lastX = rawX;
        lastY = rawY;
        break;
      case MotionEvent.ACTION_UP:
        if (isDrag) {
          //恢复按压效果
          setPressed(false);
          if (rawX >= screenWidthHalf) {
            long time = (long) ((parentWidth - getWidth() - getX()) * 500 / screenWidthHalf);
//            Log.e("wzp", "time:" + time);
            animate().setInterpolator(new DecelerateInterpolator())
                .setDuration(time)
                .xBy(parentWidth - getWidth() - getX())
                .setListener(new AnimatorListener() {
                  @Override
                  public void onAnimationStart(Animator animation) {

                  }

                  @Override
                  public void onAnimationEnd(Animator animation) {
//                    Log.e("wzp", "Animator1 eventX =" + getX() + ", eventY = " + getY());
//                    Log.e("wzp", "this view width:" + getWidth() + "--height:" + getHeight());
                    if (locationListener != null) {
                      locationListener.onFinalLocation(getX(), getY());
                    }
                  }

                  @Override
                  public void onAnimationCancel(Animator animation) {
                  }

                  @Override
                  public void onAnimationRepeat(Animator animation) {
                  }
                })
                .start();
          } else {
            long time = (long) (getX() * 500 / screenWidthHalf);
//            Log.e("wzp", "time:" + time);
            ObjectAnimator oa = ObjectAnimator.ofFloat(this, "x", getX(), 0);
            oa.setInterpolator(new DecelerateInterpolator());
            oa.setDuration(time);
            oa.addListener(new AnimatorListener() {
              @Override
              public void onAnimationStart(Animator animation) {

              }

              @Override
              public void onAnimationEnd(Animator animation) {
//                Log.e("wzp", "Animator2 eventX =" + getX() + ", eventY = " + getY());
//                Log.e("wzp", "this view width:" + getWidth() + "--height:" + getHeight());
                if (locationListener != null) {
                  locationListener.onFinalLocation(getX(), getY());
                }
              }

              @Override
              public void onAnimationCancel(Animator animation) {

              }

              @Override
              public void onAnimationRepeat(Animator animation) {

              }
            });
            oa.start();
          }
        } else {
          performClick();
        }
        break;
      default:
        break;
    }
    //如果是拖拽则消耗事件，否则正常传递即可。
    return true;
  }

  public void setLastOriAndLocation(int lastOri, float x, float y) {
//    Logger.e("wzp location", "fw window:x" + x + "--y:" + y);
    this.appX = x;
    this.appY = y;
    if (getResources() != null && getResources().getConfiguration() != null) {
      if (lastOri != getResources().getConfiguration().orientation && lastOri != -1) {
        isSetLocationAndOriChanged = true;
      } else {
        isSetLocation = true;
        lastHeight = parentHeight;
      }
    } else {
      lastHeight = parentHeight;
      isSetLocation = true;
    }
    requestLayout();
  }

  boolean isFirstInCountdown = true;

  public void setCountdown(final long countdownTime, final long hideTime) {
    if (countdownTime + hideTime > 0) {
      if (timer != null) {
        timer.cancel();
      }
      timer = new CountDownTimer(countdownTime + hideTime, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
          long countdown = millisUntilFinished - hideTime;
          if (countdown > 0) {
            String timeStr = stringForTimeSec(countdown);
//            Logger.e("wzp countdowntime:" + DragFloatingWindow.this.hashCode(),
//                "onTick:" + countdown + "--time:" + timeStr);
            isAminInfinite = false;
            setComingLesson(timeStr);
            startFWAnim();
          } else {
//            if (countdown > -1000) {
//              if (goLessonListener != null) {
//                goLessonListener.onGoLesson();
//              }
//            }
            //发送刷新pme页面命令
            setGoLessonIcon();
          }
        }

        @Override
        public void onFinish() {
          if (finishListener != null) {
            finishListener.onFinish();
          }
        }
      };
      timer.start();
    } else {
      if (finishListener != null) {
        finishListener.onFinish();
      }
      if (timer != null) {
        timer.cancel();
      }
    }
  }

  private void initFwText() {
    if (tv_main_tips != null) {
      iv_bg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.fw_coming_lesson_bg));
      tv_main_tips.setShadowLayer(3.0f, 0, 1, ContextCompat.getColor(context, R.color.fw_shadow_color_purple));
      tv_main_tips.setTextSize(18);
    }
  }

  private void setComingLesson(String timeStr) {
    if (tv_main_tips != null) {
      String mainText = timeStr + "\n即将上课";
      SpannableString spannableString = new SpannableString(mainText);
//      设置字体大小，true表示前面的字体大小20单位为dip
      spannableString.setSpan(new AbsoluteSizeSpan(18, true), 0, timeStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      spannableString.setSpan(new AbsoluteSizeSpan(9, true), timeStr.length() + 1, mainText.length(),
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//      设置字体，BOLD为粗体
      spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      tv_main_tips.setText(spannableString);
      if (isFirstInCountdown) {
        tv_main_tips.setTextSize(18);
        tv_main_tips.setShadowLayer(3.0f, 0, 1, ContextCompat.getColor(context, R.color.fw_shadow_color_purple));
//        iv_bg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.fw_coming_lesson_bg));
      }
      GlideUtil.loadImage(context, R.drawable.fw_coming_lesson_bg, iv_bg);
    }
  }

  boolean isFirstGoLesson = true;

  private void setGoLessonIcon() {
    tv_main_tips.setTextSize(15);
    tv_main_tips.setText(" 上课啦！");
//    iv_bg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.fw_go_lesson));
    GlideUtil.loadImage(context, R.drawable.fw_go_lesson, iv_bg);
    isAminInfinite = true;
    isFirstInCountdown = true;
    if (isFirstGoLesson) {
      startFWAnim();
    }
    isFirstGoLesson = false;
  }

  private void startFWAnim() {
    if (!isFirstInCountdown && !isAminInfinite) {
      return;
    }
    if (!isAminInfinite) {
      isFirstInCountdown = false;
    }
    if (animChangeListener != null) {
      animChangeListener.onChange(isAminInfinite, isFirstInCountdown);
    }
    final Animation rotateAnimation = getRotateAnim(iv_bg, isAminInfinite);
    final Animation textScale = getScaleAnim(tv_main_tips, isAminInfinite);

    if (isAminInfinite) {
      textScale.setAnimationListener(new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
          rotateAnimation.setStartOffset(1500);
          iv_bg.startAnimation(rotateAnimation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
      });

      rotateAnimation.setAnimationListener(new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
          tv_main_tips.startAnimation(textScale);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
      });
    }
  }

  private Animation getRotateAnim(View view, boolean isInfinite) {
    Animation rotateAnimation = new RotateAnimation(0, 10, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f);
    rotateAnimation.setInterpolator(new CycleInterpolator(1f));
    rotateAnimation.setRepeatMode(Animation.REVERSE);
    if (!isInfinite) {
      rotateAnimation.setStartOffset(1000);
    }
    rotateAnimation.setDuration(500);
    view.setAnimation(rotateAnimation);

    return rotateAnimation;
  }

  private Animation getScaleAnim(View view, boolean isInfinite) {
    Animation textScale = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    textScale.setDuration(350);
    if (isInfinite) {
      textScale.setStartOffset(500);
    } else {
      textScale.setStartOffset(1500);
    }
    textScale.setRepeatMode(Animation.REVERSE);
    view.setAnimation(textScale);
    return textScale;
  }

  public void cancelTimer() {
    if (timer != null) {
      timer.cancel();
    }
  }

  public void onDestroy() {
    if (timer != null) {
      timer.cancel();
    }
  }

  public interface FWAnimTypeListener {

    void onChange(boolean isInfinite, boolean isFirstInCountdown);
  }

  public void setAnimChangeListener(FWAnimTypeListener animChangeListener) {
    this.animChangeListener = animChangeListener;
  }

  public interface FWCountdownFinishListener {

    void onFinish();
  }

  public void setFinishListener(FWCountdownFinishListener finishListener) {
    this.finishListener = finishListener;
  }

  public interface FWClicklistener {

    void onClick(int type);
  }

  public void setClicklistener(final FWClicklistener clicklistener) {
    this.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (clicklistener != null) {
          clicklistener.onClick(0);
        }
      }
    });
  }

  public interface FWFinalLocationListener {

    void onFinalLocation(float x, float y);
  }

  public void setLocationListener(
      FWFinalLocationListener locationListener) {
    this.locationListener = locationListener;
  }

  public interface FWCountdownGoLessonListener {

    void onGoLesson();
  }

  public void setCountdownGoLessonListener(
      FWCountdownGoLessonListener goLessonListener) {
    this.goLessonListener = goLessonListener;
  }

  public void setAminInfinite(boolean aminInfinite, boolean isFirstInCountdown) {
    isAminInfinite = aminInfinite;
    this.isFirstInCountdown = isFirstInCountdown;
  }

  private StringBuilder sb = new StringBuilder();
  private Formatter mFormatter = new Formatter(sb, Locale.getDefault());

  public String stringForTimeSec(long timeMs) {
    long totalSeconds = Math.round(timeMs / 1000d);
    long seconds = totalSeconds % 60;
    long minutes = (totalSeconds / 60) % 60;
    long hours = totalSeconds / 3600;
    sb.setLength(0);
    if (hours > 0) {
      return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
    } else {
      return mFormatter.format("%02d:%02d", minutes, seconds).toString();
    }
  }

  /**
   * 获取 虚拟按键的高度
   */
  public static int getBottomStatusHeight(Context context) {
    if (checkNavigationBarShow(context)) {
      int orientation = context.getResources().getConfiguration().orientation;
      int totalHeight = 0;
      int contentHeight = 0;
      if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        totalHeight = getDpi(context)[0];
        contentHeight = getScreenHeight(context)[0];
      } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        totalHeight = getDpi(context)[1];
        contentHeight = getScreenHeight(context)[1];
      }
//      Log.e("wzp", "--显示虚拟导航了--");
      return totalHeight - contentHeight;
    } else {
//      Log.e("wzp", "--没有虚拟导航 或者虚拟导航隐藏--");
      return 0;
    }
  }

  /**
   * 判断虚拟导航栏是否显示
   *
   * @param context 上下文对象
   * @return true(显示虚拟导航栏)，false(不显示或不支持虚拟导航栏)
   */
  public static boolean checkNavigationBarShow(@NonNull Context context) {
    boolean hasNavigationBar = false;
    Resources rs = context.getResources();
    int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
    if (id > 0) {
      hasNavigationBar = rs.getBoolean(id);
    }
    try {
      Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
      Method m = systemPropertiesClass.getMethod("get", String.class);
      String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
      //判断是否隐藏了底部虚拟导航
      int navigationBarIsMin = 0;
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        navigationBarIsMin = Settings.System.getInt(context.getContentResolver(),
            "navigationbar_is_min", 0);
      } else {
        navigationBarIsMin = Settings.Global.getInt(context.getContentResolver(),
            "navigationbar_is_min", 0);
      }
      if ("1".equals(navBarOverride) || 1 == navigationBarIsMin) {
        hasNavigationBar = false;
      } else if ("0".equals(navBarOverride)) {
        hasNavigationBar = true;
      }
    } catch (Exception e) {
    }
    return hasNavigationBar;
  }


  //获取屏幕原始尺寸高度，包括虚拟功能键高度
  public static int[] getDpi(Context context) {
    int dpi[] = new int[2];
    WindowManager windowManager = (WindowManager)
        context.getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    DisplayMetrics displayMetrics = new DisplayMetrics();
    @SuppressWarnings("rawtypes")
    Class c;
    try {
      c = Class.forName("android.view.Display");
      @SuppressWarnings("unchecked")
      Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
      method.invoke(display, displayMetrics);
      dpi[0] = displayMetrics.heightPixels;
      dpi[1] = displayMetrics.widthPixels;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return dpi;
  }

  //获取屏幕高度 不包含虚拟按键=
  public static int[] getScreenHeight(Context context) {
    int[] height = new int[2];
    DisplayMetrics dm = context.getResources().getDisplayMetrics();
    height[0] = dm.heightPixels;
    height[1] = dm.widthPixels;
    return height;
  }
}

