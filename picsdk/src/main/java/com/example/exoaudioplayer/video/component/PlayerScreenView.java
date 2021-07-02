package com.example.exoaudioplayer.video.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.exoaudioplayer.video.controller.ControlWrapperI;
import com.example.exoaudioplayer.video.controller.IControlComponent;
import com.example.picsdk.R;

/**
 * 微课进度星级显示
 */
public class PlayerScreenView extends FrameLayout implements IControlComponent {

  private ControlWrapperI mControlWrapper;

  private ImageView iv_screen_capture;

  public PlayerScreenView(@NonNull Context context) {
    super(context);
  }

  public PlayerScreenView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public PlayerScreenView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  {
    setVisibility(GONE);
    LayoutInflater.from(getContext()).inflate(R.layout.player_layout_screen_view, this, true);
    iv_screen_capture = findViewById(R.id.iv_screen_capture);
    iv_screen_capture.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (onScreenCaptureCallBack != null) {
          onScreenCaptureCallBack.onScreenCapture();
        }
      }
    });
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
    if (isVisible) {
      if (getVisibility() == GONE) {
        setVisibility(VISIBLE);
        if (anim != null) {
          startAnimation(anim);
        }
      }
    } else {
      if (getVisibility() == VISIBLE) {
        setVisibility(GONE);
        if (anim != null) {
          startAnimation(anim);
        }
      }
    }
  }

  @Override
  public void onPlayStateChanged(int playState) {
  }

  @Override
  public void onPlayerStateChanged(int playerState) {
  }

  @Override
  public void setProgress(int duration, int position) {

  }

  @Override
  public void onLockStateChanged(boolean isLock) {
    onVisibilityChanged(!isLock, null);
  }

  private OnScreenCaptureCallBack onScreenCaptureCallBack;

  public void setOnScreenCaptureCallBack(final OnScreenCaptureCallBack onScreenCaptureCallBack) {
    this.onScreenCaptureCallBack = onScreenCaptureCallBack;
    iv_screen_capture.setVisibility(VISIBLE);
  }

  public interface OnScreenCaptureCallBack {
    void onScreenCapture();
  }
}
