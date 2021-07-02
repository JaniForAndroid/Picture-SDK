package com.example.exoaudioplayer.video.component;

import static com.example.exoaudioplayer.video.util.PlayerUtils.stringForTime;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.exoaudioplayer.video.base.Constants;
import com.example.exoaudioplayer.video.controller.ControlWrapperI;
import com.example.exoaudioplayer.video.controller.IGestureComponent;
import com.example.picsdk.R;

/**
 * 手势控制
 */
public class PlayerGestureView extends FrameLayout implements IGestureComponent {

  public PlayerGestureView(@NonNull Context context) {
    super(context);
  }

  public PlayerGestureView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public PlayerGestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private ControlWrapperI mControlWrapper;

  private ImageView mIcon;
  private TextView seekTitle;
  private ProgressBar mProgressPercent;
  private TextView mTextPercent;

  private LinearLayout mCenterContainer;

  {
    setVisibility(GONE);
    LayoutInflater
        .from(getContext()).inflate(R.layout.player_layout_gesture_control_view, this, true);
    mIcon = findViewById(R.id.iv_icon);
    seekTitle = findViewById(R.id.seekTitle);
    mProgressPercent = findViewById(R.id.pro_percent);
    mTextPercent = findViewById(R.id.tv_percent);
    mCenterContainer = findViewById(R.id.center_container);
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
  public void onPlayerStateChanged(int playerState) {

  }

  @Override
  public void onStartSlide() {
    mControlWrapper.hide();
    mCenterContainer.setVisibility(VISIBLE);
    mCenterContainer.setAlpha(1f);
  }

  @Override
  public void onStopSlide() {
    mCenterContainer.animate()
        .alpha(0f)
        .setDuration(300)
        .setListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            mCenterContainer.setVisibility(GONE);
          }
        })
        .start();
  }

  @Override
  public void onPositionChange(int slidePosition, int currentPosition, int duration) {
    mProgressPercent.setVisibility(GONE);
    if (slidePosition > currentPosition) {
      mIcon.setImageResource(R.drawable.player_ic_fast_forward);
      seekTitle.setText(R.string.player_forward);
    } else {
      mIcon.setImageResource(R.drawable.player_ic_fast_rewind);
      seekTitle.setText(R.string.player_back);
    }
    mTextPercent.setText(String.format("%s/%s", stringForTime(slidePosition), stringForTime(duration)));
  }

  @Override
  public void onBrightnessChange(int percent) {
    mProgressPercent.setVisibility(VISIBLE);
    seekTitle.setText(R.string.player_brightness);
    if (percent < 35) {
      mIcon.setImageResource(R.drawable.player_ic_brightness_low);
    } else if (percent >= 35 && percent < 70) {
      mIcon.setImageResource(R.drawable.player_ic_brightness_medium);
    } else {
      mIcon.setImageResource(R.drawable.player_ic_brightness_high);
    }
    mTextPercent.setText(percent + "%");
    mProgressPercent.setProgress(percent);
  }

  @Override
  public void onVolumeChange(int percent) {
    seekTitle.setText(R.string.player_volume);
    mProgressPercent.setVisibility(VISIBLE);
    if (percent <= 0) {
      mIcon.setImageResource(R.drawable.player_ic_volume_off);
    } else {
      mIcon.setImageResource(R.drawable.player_ic_volume_up);
    }
    mTextPercent.setText(percent + "%");
    mProgressPercent.setProgress(percent);
  }

  @Override
  public void onPlayStateChanged(int playState) {
    if (playState == Constants.STATE_IDLE
        || playState == Constants.STATE_START_ABORT
        || playState == Constants.STATE_PREPARING
        || playState == Constants.STATE_PREPARED
        || playState == Constants.STATE_ERROR
        || playState == Constants.STATE_PLAYBACK_COMPLETED) {
      setVisibility(GONE);
    } else {
      setVisibility(VISIBLE);
    }
  }

  @Override
  public void setProgress(int duration, int position) {

  }

  @Override
  public void onLockStateChanged(boolean isLock) {

  }

}
