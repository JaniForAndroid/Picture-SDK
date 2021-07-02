package com.example.exoaudioplayer.video.component;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
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
 * 播放器顶部标题栏
 */
public class PlayerTitleView extends FrameLayout implements IControlComponent {

  private ControlWrapperI mControlWrapper;

  private LinearLayout mTitleContainer;
  private TextView mTitle;

  public PlayerTitleView(@NonNull Context context) {
    super(context);
  }

  public PlayerTitleView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public PlayerTitleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  {
    setVisibility(GONE);
    LayoutInflater.from(getContext()).inflate(R.layout.player_layout_title_view, this, true);
    mTitleContainer = findViewById(R.id.title_container);
    ImageView back = findViewById(R.id.back);
    back.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (onBackCallBack != null) {
          onBackCallBack.onBack();
          return;
        }

        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity != null && mControlWrapper.isFullScreen()) {
          if (mControlWrapper.isAlwaysFullScreen()) {
            activity.onBackPressed();
          } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mControlWrapper.stopFullScreen();
          }
        }
      }
    });
    mTitle = findViewById(R.id.title);
  }

  public void setTitle(String title) {
    mTitle.setText(title);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
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
    //只在全屏时才有效
    if (!mControlWrapper.isFullScreen()) return;
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
    switch (playState) {
      case Constants.STATE_IDLE:
      case Constants.STATE_START_ABORT:
      case Constants.STATE_PREPARING:
      case Constants.STATE_PREPARED:
      case Constants.STATE_ERROR:
      case Constants.STATE_PLAYBACK_COMPLETED:
        setVisibility(GONE);
        break;
    }
  }

  @Override
  public void onPlayerStateChanged(int playerState) {
    if (playerState == Constants.PLAYER_FULL_SCREEN) {
      if (mControlWrapper.isShowing() && !mControlWrapper.isLocked()) {
        setVisibility(VISIBLE);
      }
      mTitle.setSelected(true);
    } else {
      setVisibility(GONE);
      mTitle.setSelected(false);
    }
  }

  @Override
  public void setProgress(int duration, int position) {

  }

  @Override
  public void onLockStateChanged(boolean isLocked) {
    if (isLocked) {
      setVisibility(GONE);
    } else {
      setVisibility(VISIBLE);
    }
  }

  private OnBackCallBack onBackCallBack;

  public void setOnBackCallBack(final OnBackCallBack onBackCallBack) {
    this.onBackCallBack = onBackCallBack;
  }

  public interface OnBackCallBack {
    void onBack();
  }
}
