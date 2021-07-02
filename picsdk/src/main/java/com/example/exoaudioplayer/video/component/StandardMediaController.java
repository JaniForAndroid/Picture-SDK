package com.example.exoaudioplayer.video.component;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.exoaudioplayer.video.base.Constants;
import com.example.exoaudioplayer.video.controller.GestureVideoController;
import com.example.exoaudioplayer.video.util.PlayerUtils;
import com.example.picsdk.R;

/**
 * 直播/点播控制器
 * 注意：此控制器仅做一个参考，如果想定制ui，你可以直接继承GestureVideoController或者BaseVideoController实现
 * 你自己的控制器
 */

public class StandardMediaController extends GestureVideoController implements OnClickListener {

  protected ImageView mLockButton;

  protected ProgressBar mLoadingProgress;
  private ImageView back;

  public StandardMediaController(@NonNull Context context) {
    this(context, null);
  }

  public StandardMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public StandardMediaController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.player_layout_standard_controller;
  }

  @Override
  protected void initView() {
    super.initView();
    mLockButton = findViewById(R.id.lock);
    mLockButton.setOnClickListener(this);
    mLoadingProgress = findViewById(R.id.loading);
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
  }

  /**
   * 快速添加各个组件
   *
   * @param title  标题
   * @param isLive 是否为直播
   */
  public void addDefaultControlComponent(String title, boolean isLive) {
    PlayerCompleteView playerCompleteView = new PlayerCompleteView(getContext());
    PlayerErrorView playerErrorView = new PlayerErrorView(getContext());
    PlayerPrepareView playerPrepareView = new PlayerPrepareView(getContext());
    playerPrepareView.setClickStart();
    PlayerTitleView playerTitleView = new PlayerTitleView(getContext());
    playerTitleView.setTitle(title);
    addControlComponent(playerCompleteView, playerErrorView, playerPrepareView, playerTitleView);
    addControlComponent(new PlayerControlView(getContext()));
    addControlComponent(new PlayerGestureView(getContext()));
    setCanChangePosition(!isLive);
  }

  @Override
  public void onClick(View v) {
    int i = v.getId();
    if (i == R.id.lock) {
      mControlWrapper.toggleLockState();
    }
  }

  @Override
  protected void onLockStateChanged(boolean isLocked) {
    if (isLocked) {
      mLockButton.setSelected(true);
    } else {
      mLockButton.setSelected(false);
    }
  }

  @Override
  protected void onVisibilityChanged(boolean isVisible, Animation anim) {
    if (onControlVisibleCallBack != null) {
      onControlVisibleCallBack.onVisible(isVisible);
    }

    if (mControlWrapper.isFullScreen()) {
      if (isVisible) {
        if (mLockButton.getVisibility() == GONE) {
          mLockButton.setVisibility(VISIBLE);
          if (anim != null) {
            mLockButton.startAnimation(anim);
          }
        }
      } else {
        mLockButton.setVisibility(GONE);
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
        break;
      case Constants.PLAYER_FULL_SCREEN:
        if (isShowing()) {
          mLockButton.setVisibility(VISIBLE);
        } else {
          mLockButton.setVisibility(GONE);
        }
        break;
    }
  }

  @Override
  protected void onPlayStateChanged(int playState) {
    super.onPlayStateChanged(playState);
    switch (playState) {
      //调用release方法会回到此状态
      case Constants.STATE_IDLE:
        mLockButton.setSelected(false);
        mLoadingProgress.setVisibility(GONE);
        back.setVisibility(GONE);
        break;
      case Constants.STATE_PLAYING:
      case Constants.STATE_PAUSED:
      case Constants.STATE_PREPARED:
      case Constants.STATE_ERROR:
      case Constants.STATE_BUFFERED:
        mLoadingProgress.setVisibility(GONE);
        back.setVisibility(GONE);
        break;
      case Constants.STATE_PREPARING:
      case Constants.STATE_BUFFERING:
        mLoadingProgress.setVisibility(VISIBLE);
        if (mControlWrapper.isFullScreen())
          back.setVisibility(VISIBLE);
        break;
      case Constants.STATE_PLAYBACK_COMPLETED:
        mLoadingProgress.setVisibility(GONE);
        back.setVisibility(GONE);
        mLockButton.setVisibility(GONE);
        mLockButton.setSelected(false);
        break;
    }
  }

  private OnControlVisibleCallBack onControlVisibleCallBack;

  public void setOnControlVisibleCallBack(OnControlVisibleCallBack onControlVisibleCallBack) {
    this.onControlVisibleCallBack = onControlVisibleCallBack;
  }

  public interface OnControlVisibleCallBack {
    void onVisible(boolean visible);
  }

  @Override
  public boolean onBackPressed() {
    if (isLocked()) {
      show();
      Toast.makeText(getContext(), R.string.dkplayer_lock_tip, Toast.LENGTH_SHORT).show();
      return true;
    }
    if (mControlWrapper.isFullScreen() && !mControlWrapper.isAlwaysFullScreen()) {
      return stopFullScreen();
    }
    return super.onBackPressed();
  }
}
