package com.example.picsdk.view;

import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * author : feng
 * creation time : 19-10-12上午10:21
 */
public class RoundViewOutline extends ViewOutlineProvider {

  private float radius;

  public RoundViewOutline(float radius) {
    this.radius = radius;
  }

  @Override
  public void getOutline(View view, Outline outline) {
    view.setClipToOutline(true);
    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
  }
}
