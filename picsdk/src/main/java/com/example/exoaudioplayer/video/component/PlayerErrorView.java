package com.example.exoaudioplayer.video.component;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.exoaudioplayer.video.base.Constants;
import com.example.exoaudioplayer.video.controller.ControlWrapperI;
import com.example.exoaudioplayer.video.controller.IControlComponent;
import com.example.exoaudioplayer.video.util.PlayerUtils;
import com.example.picsdk.R;

/**
 * 播放出错提示界面
 */
public class PlayerErrorView extends LinearLayout implements IControlComponent {

  private float mDownX;
  private float mDownY;

  private ControlWrapperI mControlWrapper;
  private ImageView back;
  private TextView status_btn;

  public PlayerErrorView(Context context) {
    this(context, null);
  }

  public PlayerErrorView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public PlayerErrorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  {
    setVisibility(GONE);
    LayoutInflater.from(getContext()).inflate(R.layout.player_layout_error_view, this, true);
    findViewById(R.id.status_btn).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        setVisibility(GONE);
        mControlWrapper.replay(false);
      }
    });
    findViewById(R.id.connect_kefu).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        setVisibility(GONE);
        mControlWrapper.replay(false);
      }
    });
    status_btn = findViewById(R.id.status_btn);
    back = findViewById(R.id.back);
    back.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mControlWrapper.isFullScreen()) {
          Activity activity = PlayerUtils.scanForActivity(getContext());
          if (activity != null && !activity.isFinishing()) {
            if (mControlWrapper.isAlwaysFullScreen()) {
              activity.onBackPressed();
            } else {
              activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
              mControlWrapper.stopFullScreen();
            }
          }
        }
      }
    });
    setClickable(true);
  }

  @Override
  public void attach(@NonNull ControlWrapperI controlWrapper) {
    mControlWrapper = controlWrapper;
  }

  @Override
  public View getView() {
    return this;
  }

  @Override
  public void onVisibilityChanged(boolean isVisible, Animation anim) {

  }


  @Override
  public void onPlayStateChanged(int playState) {
    if (playState == Constants.STATE_ERROR) {
      if (!PlayerUtils.isNetConnect(getContext())) {
        status_btn.setText(R.string.player_no_network);
      }
      bringToFront();
      setVisibility(VISIBLE);
      back.setVisibility(mControlWrapper.isFullScreen() ? VISIBLE : GONE);
    } else if (playState == Constants.STATE_IDLE) {
      setVisibility(GONE);
    }
  }

  @Override
  public void onPlayerStateChanged(int playerState) {
    back.setVisibility(mControlWrapper.isFullScreen() ? VISIBLE : GONE);
  }

  @Override
  public void setProgress(int duration, int position) {

  }

  @Override
  public void onLockStateChanged(boolean isLock) {

  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    switch (ev.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mDownX = ev.getX();
        mDownY = ev.getY();
        // True if the child does not want the parent to intercept touch events.
        getParent().requestDisallowInterceptTouchEvent(true);
        break;
      case MotionEvent.ACTION_MOVE:
        float absDeltaX = Math.abs(ev.getX() - mDownX);
        float absDeltaY = Math.abs(ev.getY() - mDownY);
        if (absDeltaX > ViewConfiguration.get(getContext()).getScaledTouchSlop() ||
            absDeltaY > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
          getParent().requestDisallowInterceptTouchEvent(false);
        }
      case MotionEvent.ACTION_UP:
        break;
    }
    return super.dispatchTouchEvent(ev);
  }
}
