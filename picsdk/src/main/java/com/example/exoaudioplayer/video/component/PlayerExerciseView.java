package com.example.exoaudioplayer.video.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.exoaudioplayer.video.controller.ControlWrapperI;
import com.example.exoaudioplayer.video.controller.IControlComponent;
import com.example.picsdk.R;

/**
 * 巩固练习显示
 */
public class PlayerExerciseView extends FrameLayout implements IControlComponent {

  private ControlWrapperI mControlWrapper;

  private ImageView iv_exercise;
  private TextView tv_exercise;

  public PlayerExerciseView(@NonNull Context context) {
    super(context);
  }

  public PlayerExerciseView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public PlayerExerciseView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  {
    setVisibility(GONE);
    LayoutInflater.from(getContext()).inflate(R.layout.player_layout_exercise_view, this, true);
    iv_exercise = findViewById(R.id.iv_exercise);
    tv_exercise = findViewById(R.id.tv_exercise);
    iv_exercise.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (onExerciseCallBack != null) {
          onExerciseCallBack.onExercise();
        }
      }
    });
    tv_exercise.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (onExerciseCallBack != null) {
          onExerciseCallBack.onExercise();
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

  private OnExerciseCallBack onExerciseCallBack;

  public void setOnExerciseCallBack(final OnExerciseCallBack onExerciseCallBack) {
    this.onExerciseCallBack = onExerciseCallBack;
    iv_exercise.setVisibility(VISIBLE);
    tv_exercise.setVisibility(VISIBLE);
  }

  public interface OnExerciseCallBack {
    void onExercise();
  }
}
