package com.example.exoaudioplayer.video.component;

import static com.example.exoaudioplayer.video.util.PlayerUtils.stringForTime;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.exoaudioplayer.video.base.Constants;
import com.example.exoaudioplayer.video.controller.ControlWrapperI;
import com.example.exoaudioplayer.video.controller.IControlComponent;
import com.example.exoaudioplayer.video.util.PlayerUtils;
import com.example.exoaudioplayer.video.view.CSeekBar;
import com.example.exoaudioplayer.video.view.ProgressView;
import com.example.picsdk.R;
import com.namibox.util.Utils;

/**
 * 点播底部控制栏
 */
public class PlayerControlView extends FrameLayout implements IControlComponent, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

  protected ControlWrapperI mControlWrapper;

  private TextView mTotalTime, mCurrTime, tv_play_speed;
  private ImageView mFullScreen;
  private LinearLayout mBottomContainer;
  private CSeekBar mVideoProgress;
  private ProgressView mBottomProgress;
  private ImageView mPlayButton;
  //播放倍数
  private PopupWindow pwPlaySpeed;
  private ImageView iv_menu;
  private TextView tv_choose;

  private boolean mIsDragging;

  private boolean mIsShowBottomProgress = true;

  public PlayerControlView(@NonNull Context context) {
    super(context);
  }

  public PlayerControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public PlayerControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }


  {
    setVisibility(GONE);
    LayoutInflater.from(getContext()).inflate(getLayoutId(), this, true);
    mFullScreen = findViewById(R.id.fullscreen);
    mFullScreen.setOnClickListener(this);
    mBottomContainer = findViewById(R.id.bottom_container);
    mVideoProgress = findViewById(R.id.seekBar);
    mVideoProgress.setMax(1000);
    mVideoProgress.setOnSeekBarChangeListener(this);
    mTotalTime = findViewById(R.id.total_time);
    mCurrTime = findViewById(R.id.curr_time);
    mPlayButton = findViewById(R.id.iv_play);
    mPlayButton.setOnClickListener(this);
    mBottomProgress = findViewById(R.id.bottom_progress);
    tv_play_speed = findViewById(R.id.tv_play_speed);
    mBottomProgress.setMax(1000);

    iv_menu = findViewById(R.id.iv_menu);
    tv_choose = findViewById(R.id.tv_choose);
    iv_menu.setOnClickListener(this);
    tv_choose.setOnClickListener(this);

    //5.1以下系统SeekBar高度需要设置成WRAP_CONTENT
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
      mVideoProgress.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    initPlaySpeed();
  }

  protected int getLayoutId() {
    return R.layout.player_layout_control_view;
  }

  /**
   * 是否显示底部进度条，默认显示
   */
  public void BottomProgress(boolean isShow) {
    mIsShowBottomProgress = isShow;
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
      mBottomContainer.setVisibility(VISIBLE);
      if (anim != null) {
        mBottomContainer.startAnimation(anim);
      }
      if (mIsShowBottomProgress) {
        mBottomProgress.setVisibility(GONE);
      }
    } else {
      mBottomContainer.setVisibility(GONE);
      if (anim != null) {
        mBottomContainer.startAnimation(anim);
      }
      if (mIsShowBottomProgress) {
        mBottomProgress.setVisibility(VISIBLE);
        AlphaAnimation animation = new AlphaAnimation(0f, 1f);
        animation.setDuration(300);
        mBottomProgress.startAnimation(animation);
      }
    }
  }

  @Override
  public void onPlayStateChanged(int playState) {
    switch (playState) {
      case Constants.STATE_IDLE:
      case Constants.STATE_PLAYBACK_COMPLETED:
        setVisibility(GONE);
        mBottomProgress.setProgress(0);
        mVideoProgress.setProgress(0);
        mVideoProgress.setSecondaryProgress(0);
        break;
      case Constants.STATE_START_ABORT:
      case Constants.STATE_PREPARING:
      case Constants.STATE_PREPARED:
      case Constants.STATE_ERROR:
        setVisibility(GONE);
        break;
      case Constants.STATE_PLAYING:
        mPlayButton.setSelected(true);
        if (mIsShowBottomProgress) {
          if (mControlWrapper.isShowing()) {
            mBottomProgress.setVisibility(GONE);
            mBottomContainer.setVisibility(VISIBLE);
          } else {
            mBottomContainer.setVisibility(GONE);
            mBottomProgress.setVisibility(VISIBLE);
          }
        } else {
          mBottomContainer.setVisibility(GONE);
        }
        setVisibility(VISIBLE);
        //开始刷新进度
        mControlWrapper.startProgress();
        break;
      case Constants.STATE_PAUSED:
        mPlayButton.setSelected(false);
        break;
      case Constants.STATE_BUFFERING:
      case Constants.STATE_BUFFERED:
        mPlayButton.setSelected(mControlWrapper.isPlaying());
        break;
    }
  }

  @Override
  public void onPlayerStateChanged(int playerState) {
    switch (playerState) {
      case Constants.PLAYER_NORMAL:
        mFullScreen.setSelected(false);
        break;
      case Constants.PLAYER_FULL_SCREEN:
        mFullScreen.setSelected(true);
        break;
    }
  }

  @Override
  public void setProgress(int duration, int position) {
    if (mIsDragging) {
      return;
    }

    if (mVideoProgress != null) {
      if (duration > 0) {
        mVideoProgress.setEnabled(true);
        int pos = (int) (position * 1.0 / duration * mVideoProgress.getMax());
        mVideoProgress.setProgress(pos);
        mBottomProgress.setProgress(pos);
      } else {
        mVideoProgress.setEnabled(false);
      }
      int percent = mControlWrapper.getBufferedPercentage();
      if (percent >= 95) { //解决缓冲进度不能100%问题
        mVideoProgress.setSecondaryProgress(mVideoProgress.getMax());
      } else {
        mVideoProgress.setSecondaryProgress(percent * 10);
      }
    }

    if (mTotalTime != null)
      mTotalTime.setText(stringForTime(duration));
    if (mCurrTime != null)
      mCurrTime.setText(stringForTime(position));
  }

  public void setProgressData(boolean[] playTags) {
    Drawable drawable = getResources().getDrawable(R.drawable.player_seekbar_watched_bg);
    mVideoProgress.setProgressDrawable(drawable);
    mVideoProgress.setProgressData(playTags);
    mBottomProgress.setProgressData(playTags);
  }

  @Override
  public void onLockStateChanged(boolean isLocked) {
    onVisibilityChanged(!isLocked, null);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.fullscreen) {
      toggleFullScreen();
    } else if (id == R.id.iv_play) {
      mControlWrapper.togglePlay();
    } else if (id == R.id.iv_menu || id == R.id.tv_choose) {
      if (onChooseLessonCallBack != null) {
        onChooseLessonCallBack.onChoose();
      }
    }
  }

  /**
   * 横竖屏切换
   */
  private void toggleFullScreen() {
    Activity activity = PlayerUtils.scanForActivity(getContext());
    mControlWrapper.toggleFullScreen(activity);
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    mIsDragging = true;
    mControlWrapper.stopProgress();
    mControlWrapper.stopFadeOut();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    long duration = mControlWrapper.getDuration();
    long newPosition = (duration * seekBar.getProgress()) / mVideoProgress.getMax();
    mControlWrapper.seekTo((int) newPosition);
    mIsDragging = false;
    mControlWrapper.startProgress();
    mControlWrapper.startFadeOut();
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if (!fromUser) {
      return;
    }

    long duration = mControlWrapper.getDuration();
    long newPosition = (duration * progress) / mVideoProgress.getMax();
    if (mCurrTime != null)
      mCurrTime.setText(stringForTime((int) newPosition));
  }

  private OnChangePlaySpeedCallBack onChangePlaySpeedCallBack;

  public void setOnChangePlaySpeedCallBack(OnChangePlaySpeedCallBack onChangePlaySpeedCallBack) {
    this.onChangePlaySpeedCallBack = onChangePlaySpeedCallBack;
  }

  public interface OnChangePlaySpeedCallBack {

    void onChange(float multiple);
  }

  private OnChooseLessonCallBack onChooseLessonCallBack;

  public void setOnChooseLessonCallBack(OnChooseLessonCallBack onChooseLessonCallBack) {
    this.onChooseLessonCallBack = onChooseLessonCallBack;
    iv_menu.setVisibility(VISIBLE);
    tv_choose.setVisibility(VISIBLE);
  }

  public interface OnChooseLessonCallBack {
    void onChoose();
  }

  public void dismissChangePlaySpeedWindow() {
    if (pwPlaySpeed != null && pwPlaySpeed.isShowing()) {
      pwPlaySpeed.dismiss();
    }
  }

  private void initPlaySpeed() {
    tv_play_speed.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (onChangePlaySpeedCallBack != null) {
          mControlWrapper.stopFadeOut();
          if (pwPlaySpeed == null) {
            View view = LayoutInflater
                .from(getContext()).inflate(R.layout.player_pw_item_play_speed, null, false);
            pwPlaySpeed = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            pwPlaySpeed.setOutsideTouchable(true);
            pwPlaySpeed.setFocusable(false);
            pwPlaySpeed.setTouchable(true);
            pwPlaySpeed.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            pwPlaySpeed.setBackgroundDrawable(new ColorDrawable(0x00000000));
            final LinearLayout llOptions = view.findViewById(R.id.ll_options);
            for (int i = 0; i < llOptions.getChildCount(); i++) {
              if (llOptions.getChildAt(i) instanceof TextView) {
                final TextView tv = (TextView) llOptions.getChildAt(i);
                final int index = i;
                tv.setOnClickListener(new OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    if (onChangePlaySpeedCallBack != null) {
                      tv_play_speed.setText(getResources().getString(R.string.player_speed,tv.getText().toString()));
                      String str = tv.getText().toString();
                      str = str.substring(0, str.indexOf("x"));
                      float multiple = Float.parseFloat(str);
                      onChangePlaySpeedCallBack.onChange(multiple);
                      pwPlaySpeed.dismiss();
                    }
                    for (int j = 0; j < llOptions.getChildCount(); j++) {
                      if (llOptions.getChildAt(j) instanceof TextView) {
                        ((TextView) llOptions.getChildAt(j)).setTextColor(index == j ? 0xFF00B9FF : 0xFFFFFFFF);
                      }
                    }
                  }
                });
              }
            }
          } else {
            final LinearLayout llOptions = pwPlaySpeed.getContentView().findViewById(R.id.ll_options);
            String str = tv_play_speed.getText().toString();
            str = str.substring(2);
            for (int i = 0; i < llOptions.getChildCount(); i++) {
              if (llOptions.getChildAt(i) instanceof TextView) {
                TextView tv = (TextView) llOptions.getChildAt(i);
                tv.setTextColor(TextUtils.equals(str, tv.getText().toString()) ? 0xFF00B9FF : 0xFFFFFFFF);
              }
            }
          }
          if (pwPlaySpeed != null) {
            pwPlaySpeed.setOnDismissListener(new PopupWindow.OnDismissListener() {
              @Override
              public void onDismiss() {
                mControlWrapper.startFadeOut();
              }
            });
          }

          int[] location = new int[2];
          v.getLocationOnScreen(location);
          //获取自身的长宽高
          pwPlaySpeed.getContentView().measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
          final LinearLayout llOptions = pwPlaySpeed.getContentView().findViewById(R.id.ll_options);
          int orientation = getResources().getConfiguration().orientation;
          ViewGroup.LayoutParams layoutParams = llOptions.getLayoutParams();
          int optionViewMarginLeft;
          int showXOffset;
          if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutParams.height = Utils.dp2px(getContext(), 125);
            layoutParams.width = Utils.dp2px(getContext(), 55);
            optionViewMarginLeft = Utils.dp2px(getContext(), 10);
            showXOffset = Utils.dp2px(getContext(), 3);
          } else {
            llOptions.getLayoutParams().height = Utils.dp2px(getContext(), 210);
            layoutParams.width = Utils.dp2px(getContext(), 78);
            optionViewMarginLeft = Utils.dp2px(getContext(), 22);
            showXOffset = Utils.dp2px(getContext(), 12);
          }
          for (int i = 0; i < llOptions.getChildCount(); i++) {
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) llOptions.getChildAt(i).getLayoutParams();
            marginLayoutParams.setMargins(optionViewMarginLeft, marginLayoutParams.topMargin, marginLayoutParams.rightMargin, marginLayoutParams.bottomMargin);
          }
          int navigationBarHeight = 0;
          if (Utils.hasNavBar(getContext()) && Utils.isTablet(getContext()))
            navigationBarHeight = Utils.getNavigationBarHeight(getContext());
          pwPlaySpeed.showAtLocation(v, Gravity.NO_GRAVITY, location[0] - showXOffset, location[1] - layoutParams.height - Utils.dp2px(getContext(), 10) - navigationBarHeight);
          mControlWrapper.show();
        }
      }
    });
  }
}
