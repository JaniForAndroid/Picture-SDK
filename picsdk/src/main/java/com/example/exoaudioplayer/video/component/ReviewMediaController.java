package com.example.exoaudioplayer.video.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.exoaudioplayer.video.base.Constants;
import com.example.picsdk.R;

public class ReviewMediaController extends StandardMediaController implements View.OnClickListener {

  protected ImageView mLockButton;

  protected ProgressBar mLoadingProgress;
  private ImageView iv_screen_capture;

  public ReviewMediaController(@NonNull Context context) {
    this(context, null);
  }

  public ReviewMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ReviewMediaController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.player_layout_review_controller;
  }

  @Override
  protected void initView() {
    super.initView();
    mLockButton = findViewById(R.id.lock);
    mLockButton.setOnClickListener(this);
    mLoadingProgress = findViewById(R.id.loading);

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
  public void onClick(View v) {
    int i = v.getId();
    if (i == R.id.lock) {
      mControlWrapper.toggleLockState();
    }
  }

  @Override
  protected void onVisibilityChanged(boolean isVisible, Animation anim) {
    if (mControlWrapper.isFullScreen()) {
      if (isVisible) {
        if (mLockButton.getVisibility() == GONE) {
          mLockButton.setVisibility(VISIBLE);
          iv_screen_capture.setVisibility(VISIBLE);
          if (anim != null) {
            mLockButton.startAnimation(anim);
          }
        }
      } else {
        mLockButton.setVisibility(GONE);
        iv_screen_capture.setVisibility(GONE);
        if (anim != null) {
          mLockButton.startAnimation(anim);
        }
      }
    }
  }

  @Override
  protected void onPlayerStateChanged(int playerState) {
    super.onPlayerStateChanged(playerState);
    switch (playerState) {
      case Constants.PLAYER_NORMAL:
        setLayoutParams(new LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
        mLockButton.setVisibility(GONE);
        iv_screen_capture.setVisibility(GONE);
        break;
      case Constants.PLAYER_FULL_SCREEN:
        if (isShowing()) {
          mLockButton.setVisibility(VISIBLE);
          iv_screen_capture.setVisibility(VISIBLE);
        } else {
          mLockButton.setVisibility(GONE);
          iv_screen_capture.setVisibility(GONE);
        }
        break;
    }
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
