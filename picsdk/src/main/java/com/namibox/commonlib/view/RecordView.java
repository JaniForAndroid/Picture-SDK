package com.namibox.commonlib.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.picsdk.R;
import java.util.Formatter;
import java.util.Locale;

/**
 * Create time: 2015/9/15.
 */
public class RecordView extends View implements Handler.Callback {

  private Paint paint;
  private String time;
  private float textSize;
  private float timeSize;
  private float padding;
  private int textColor;
  private int timeColor;
  private int bgColor;
  private int alpha;
  private boolean pressed;
  private Drawable normalDrawable, pressDrawable;
  private Drawable[] volumeDrawable;
  private int volume;
  private float initY;
  private boolean userCancel;
  private int dw, dh, vdw, vdh;
  private Vibrator vibrator;
  private Handler handler;
  private int pressTime;
  private String handingText;

  public interface Callback {

    void start();

    void stop();

    void cancel(boolean userCancel);
  }

  private Callback callback;

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public RecordView(Context context) {
    super(context);
    init(context);
  }

  public RecordView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public RecordView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public RecordView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context);
  }

  private void init(Context context) {
    paint = new Paint();
    paint.setAntiAlias(true);
    textSize = context.getResources().getDimension(R.dimen.aiengine_title_size);
    timeSize = context.getResources().getDimension(R.dimen.aiengine_time_size);
    padding = context.getResources().getDimension(R.dimen.aiengine_top_padding);
    textColor = ContextCompat.getColor(context, R.color.aiengine_title_color);
    timeColor = ContextCompat.getColor(context, R.color.aiengine_time_color);
    bgColor = ContextCompat.getColor(context, R.color.aiengine_bg_color);
    normalDrawable = ContextCompat.getDrawable(context, R.drawable.ic_aiengine_tape);
    pressDrawable = ContextCompat.getDrawable(context, R.drawable.ic_aiengine_tape_down);
    volumeDrawable = new Drawable[6];
    volumeDrawable[0] = ContextCompat.getDrawable(context, R.drawable.ic_aiengine_volume1);
    volumeDrawable[1] = ContextCompat.getDrawable(context, R.drawable.ic_aiengine_volume2);
    volumeDrawable[2] = ContextCompat.getDrawable(context, R.drawable.ic_aiengine_volume3);
    volumeDrawable[3] = ContextCompat.getDrawable(context, R.drawable.ic_aiengine_volume4);
    volumeDrawable[4] = ContextCompat.getDrawable(context, R.drawable.ic_aiengine_volume5);
    volumeDrawable[5] = ContextCompat.getDrawable(context, R.drawable.ic_aiengine_volume6);
    dh = normalDrawable.getIntrinsicHeight();
    dw = normalDrawable.getIntrinsicWidth();
    vdh = volumeDrawable[0].getIntrinsicHeight();
    vdw = volumeDrawable[0].getIntrinsicWidth();
    vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    handler = new Handler(this);

  }

  private void vibrator() {
    if (vibrator != null) {
      vibrator.vibrate(30);
    }
  }

  public void setBgAlpha(float alpha) {
    this.alpha = (int) (alpha * 255);
    invalidate();
  }

  private static StringBuilder sFormatBuilder = new StringBuilder();
  private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());

  private static String makeSmallTimeString(int ms) {
    sFormatBuilder.setLength(0);
    int arg1 = ms / 1000;
    int arg2 = (ms % 1000) / 100;
    return sFormatter.format("%1$02d:%2$d", arg1, arg2).toString();
  }

  @Override
  public boolean handleMessage(Message msg) {
    pressTime += 100;
    setTime(makeSmallTimeString(pressTime));
    handler.sendEmptyMessageDelayed(0, 100);
    return true;
  }

  public void setTime(String time) {
    this.time = time;
    invalidate();
  }

  public void setVolume(int volume) {
    this.volume = volume;
    invalidate();
  }

  public void stop() {
    pressed = false;
    handler.removeMessages(0);
    time = null;
    volume = 0;
    invalidate();
  }

  public void showText(String handingText) {
    this.handingText = handingText;
    setEnabled(false);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (!isEnabled()) {
      return true;
    }
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      float x = event.getX();
      float y = event.getY();
      if (Math.abs(x - getWidth() / 2) > dw / 2 || Math.abs(y - getHeight() / 2) > dh) {
        return true;
      }
      userCancel = false;
      initY = y;
      pressed = true;
      vibrator();
      invalidate();
      if (callback != null) {
        callback.start();
        pressTime = 0;
        handler.sendEmptyMessageDelayed(0, 100);
      }
    } else if (event.getAction() == MotionEvent.ACTION_UP) {
      if (!pressed) {
        return true;
      }
      stop();
      if (callback != null) {
        if (userCancel) {
          callback.cancel(true);
        } else {
          callback.stop();
        }
      }
    } else if (event.getAction() == MotionEvent.ACTION_CANCEL
        || event.getAction() == MotionEvent.ACTION_OUTSIDE) {
      if (!pressed) {
        return true;
      }
      stop();
      if (callback != null) {
        callback.cancel(false);
      }
    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
      if (!pressed) {
        return true;
      }
      float y = event.getY();
      if (initY - y > dh / 2) {
        if (!userCancel) {
          userCancel = true;
          invalidate();
        }
      } else {
        if (userCancel) {
          userCancel = false;
          invalidate();
        }
      }
    }
    return true;
  }

  private Drawable getVolumeDrawable() {
    if (volume <= 5) {
      return volumeDrawable[0];
    } else if (volume < 20) {
      return volumeDrawable[1];
    } else if (volume < 40) {
      return volumeDrawable[2];
    } else if (volume < 60) {
      return volumeDrawable[3];
    } else if (volume < 80) {
      return volumeDrawable[4];
    } else {
      return volumeDrawable[5];
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    paint.setColor(bgColor);
    paint.setAlpha(alpha);
    canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    Drawable vd = getVolumeDrawable();
    vd.setBounds(0, 0, vdw, vdh);
    float vl = (getWidth() - vdw) / 2;
    float vt = (getHeight() - vdh) / 2;
    canvas.save();
    canvas.translate(vl, vt);
    vd.draw(canvas);
    canvas.restore();
    paint.setTextSize(textSize);
    paint.setColor(textColor);
    paint.setTextAlign(Paint.Align.CENTER);
    float x = getWidth() / 2;
    Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
    int fontHeight = fontMetrics.bottom - fontMetrics.top;
    String text;
    Drawable drawable;
    if (!isEnabled()) {
      text = handingText;
      drawable = normalDrawable;
    } else if (pressed) {
      text = userCancel ? getContext().getString(R.string.common_release_cancel)
          : getContext().getString(R.string.common_release_completed);
      drawable = pressDrawable;
    } else {
      text = getContext().getString(R.string.common_press_read);
      drawable = normalDrawable;
    }
    drawable.setBounds(0, 0, dw, dh);
    float left = (getWidth() - dw) / 2;
    float top = (getHeight() - dh) / 2;
    canvas.drawText(text, x, fontHeight, paint);
    canvas.save();
    canvas.translate(left, top);
    drawable.draw(canvas);
    canvas.restore();
    if (!TextUtils.isEmpty(time) && pressed && isEnabled()) {
      paint.setTextSize(timeSize);
      paint.setColor(timeColor);
      paint.setTextAlign(Paint.Align.RIGHT);
      fontHeight = fontMetrics.bottom - fontMetrics.top;
      canvas.drawText(time, getWidth() - padding, fontHeight + padding, paint);
    }
  }
}
