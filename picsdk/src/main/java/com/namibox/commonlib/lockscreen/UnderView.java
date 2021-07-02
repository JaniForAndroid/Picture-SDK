package com.namibox.commonlib.lockscreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class UnderView extends FrameLayout {
  private View mMoveView;
  private float mStartX;

  public UnderView(@NonNull Context context) {
    this(context, null);
  }

  public UnderView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    final int action = event.getAction();
    final float nx = event.getX();
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        mStartX = nx;
        onAnimationEnd();
      case MotionEvent.ACTION_MOVE:
        handleMoveView(nx);
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        doTriggerEvent(nx);
        break;
    }
    return true;
  }

  private void handleMoveView(float x){
    float moveX = x - mStartX;
    moveX = moveX < 0 ? 0 : moveX;
    mMoveView.setTranslationX(moveX);
  }

  private void doTriggerEvent(float x) {
    float moveX = x - mStartX;
    if (moveX > getWidth() * 0.5) {
      moveMoveView(getWidth() - mMoveView.getLeft(), true);
    } else {
      moveMoveView(-mMoveView.getLeft(), false);
    }
  }

  private void moveMoveView(float to, boolean exit) {
    ObjectAnimator animator = ObjectAnimator.ofFloat(mMoveView, "translationX", to);
    animator.setDuration(250).start();
    if (exit) {
      animator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          ((Activity) getContext()).finish();
          super.onAnimationEnd(animation);
        }
      });
    }
  }

  public void setMoveView(View moveView) {
    mMoveView = moveView;
  }

}

