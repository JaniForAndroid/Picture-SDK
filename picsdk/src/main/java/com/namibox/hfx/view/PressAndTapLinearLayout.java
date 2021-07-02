package com.namibox.hfx.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import java.lang.ref.WeakReference;

/**
 * Created by sunha on 2015/12/17 0017.
 */
public class PressAndTapLinearLayout extends LinearLayout {

  public PressAndTapLinearLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public interface PressAndTapListener {

    void onPressing();

    void onTap();

    void onTouchFinish();

  }

  public static final int LONG_PRESS = 1000;
  public static final int ACTION_UP = 1001;
  public static final int ACTION_CANCEL = 1003;
  boolean isPressing;
  double time;
  private PressAndTapListener listener;
  Handler mHandler = new MyHandler(this);

  static class MyHandler extends Handler {

    WeakReference<PressAndTapLinearLayout> layoutReference;

    public MyHandler(PressAndTapLinearLayout layout) {
      this.layoutReference = new WeakReference<PressAndTapLinearLayout>(layout);
    }

    @Override
    public void handleMessage(Message msg) {
      PressAndTapLinearLayout layout = layoutReference.get();
      switch (msg.what) {
        case LONG_PRESS:
          if (layout.isPressing) {
            if (layout.listener != null) {
              layout.listener.onPressing();
            }
            msg = obtainMessage(LONG_PRESS);
            sendMessageDelayed(msg, 80);
          }
          break;
        case ACTION_UP:
          if (layout.listener != null) {
            layout.listener.onTouchFinish();
          }
          break;
        case ACTION_CANCEL:
          if (layout.listener != null) {
            layout.listener.onTouchFinish();
          }
          break;

      }
    }
  }


  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        time = System.currentTimeMillis();
        isPressing = true;
        mHandler.sendEmptyMessageDelayed(LONG_PRESS, ViewConfiguration.getLongPressTimeout());
        break;
      case MotionEvent.ACTION_UP:
        if (time + ViewConfiguration.getLongPressTimeout() > System.currentTimeMillis()) {
          if (listener != null) {
            listener.onTap();
          }
        }
        mHandler.removeMessages(LONG_PRESS);
        isPressing = false;
        mHandler.sendEmptyMessageDelayed(ACTION_UP, 300);
        break;

      case MotionEvent.ACTION_CANCEL:
        mHandler.removeMessages(LONG_PRESS);
        isPressing = false;
        mHandler.sendEmptyMessageDelayed(ACTION_CANCEL, 300);
        break;
    }
    return true;
  }

  public void setOnPressAndTapListener(PressAndTapListener listener) {
    this.listener = listener;
  }
}
