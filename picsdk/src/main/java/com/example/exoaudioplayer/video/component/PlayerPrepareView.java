package com.example.exoaudioplayer.video.component;

import static com.example.exoaudioplayer.video.util.PlayerUtils.FormetFileSize;
import static com.example.exoaudioplayer.video.util.PlayerUtils.stringForTime;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.exoaudioplayer.video.base.Constants;
import com.example.exoaudioplayer.video.base.VideoViewManager;
import com.example.exoaudioplayer.video.controller.ControlWrapperI;
import com.example.exoaudioplayer.video.controller.IControlComponent;
import com.example.exoaudioplayer.video.util.PlayerUtils;
import com.example.picsdk.R;


/**
 * 准备播放界面
 */
public class PlayerPrepareView extends FrameLayout implements IControlComponent {

  private ControlWrapperI mControlWrapper;

  private ImageView mThumb;
  private ImageView mStartPlay;
  private ProgressBar mLoading;
  private RelativeLayout mNetWarning;
  private TextView message2;

  public PlayerPrepareView(@NonNull Context context) {
    super(context);
  }

  public PlayerPrepareView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public PlayerPrepareView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  {
    LayoutInflater.from(getContext()).inflate(R.layout.player_layout_prepare_view, this, true);
    mThumb = findViewById(R.id.thumb);
    mStartPlay = findViewById(R.id.start_play);
    mLoading = findViewById(R.id.loading);
    mNetWarning = findViewById(R.id.net_warning_layout);
    message2 = findViewById(R.id.message2);
    findViewById(R.id.status_btn).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mNetWarning.setVisibility(GONE);
        VideoViewManager.instance().setPlayOnMobileNetwork(true);
        mControlWrapper.start();
      }
    });
    findViewById(R.id.tv_exit_play).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mNetWarning.setVisibility(GONE);
        VideoViewManager.instance().setPlayOnMobileNetwork(false);
        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity != null) {
          activity.onBackPressed();
        }
      }
    });
    mStartPlay.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        mControlWrapper.start();
      }
    });
  }

  /**
   * 设置点击此界面开始播放
   */
  public void setClickStart() {
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mControlWrapper.start();
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

  }

  @Override
  public void onPlayStateChanged(int playState) {
    switch (playState) {
      case Constants.STATE_PREPARING:
        bringToFront();
        setVisibility(VISIBLE);
        mStartPlay.setVisibility(View.GONE);
        mNetWarning.setVisibility(GONE);
        mLoading.setVisibility(View.VISIBLE);
        break;
      case Constants.STATE_PLAYING:
      case Constants.STATE_PAUSED:
      case Constants.STATE_ERROR:
      case Constants.STATE_BUFFERING:
      case Constants.STATE_BUFFERED:
      case Constants.STATE_PLAYBACK_COMPLETED:
        setVisibility(GONE);
        break;
      case Constants.STATE_IDLE:
//        setVisibility(VISIBLE);
//        bringToFront();
//        mLoading.setVisibility(View.GONE);
//        mNetWarning.setVisibility(GONE);
//        mStartPlay.setVisibility(View.VISIBLE);
//        mThumb.setVisibility(View.VISIBLE);
        break;
      case Constants.STATE_START_ABORT:
        setVisibility(VISIBLE);
        mStartPlay.setVisibility(View.GONE);
        mNetWarning.setVisibility(VISIBLE);
        mNetWarning.bringToFront();
        message2.setText(getContext().getString(R.string.player_message_tips,FormetFileSize(mControlWrapper.getSize()), stringForTime((int) mControlWrapper.getDuration1())));
        break;
    }
  }

  @Override
  public void onPlayerStateChanged(int playerState) {

  }

  @Override
  public void setProgress(int duration, int position) {

  }

  @Override
  public void onLockStateChanged(boolean isLocked) {

  }
}
