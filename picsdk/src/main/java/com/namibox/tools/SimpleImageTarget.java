package com.namibox.tools;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

/**
 * Create time: 2019/11/11.
 */
public abstract class SimpleImageTarget extends SimpleTarget<Drawable> {
  @Nullable
  private Animatable animatable;

  @Override
  public void onStart() {
    if (animatable != null) {
      animatable.start();
    }
  }

  @Override
  public void onStop() {
    if (animatable != null) {
      animatable.stop();
    }
  }

  private void setResourceInternal(@Nullable Drawable resource) {
    maybeUpdateAnimatable(resource);
    setResource(resource);
  }

  private void maybeUpdateAnimatable(@Nullable Drawable resource) {
    if (resource instanceof Animatable) {
      animatable = (Animatable) resource;
      animatable.start();
    } else {
      animatable = null;
    }
  }

  protected abstract void setResource(@Nullable Drawable resource);

  @Override
  public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
    setResourceInternal(resource);
  }
}
