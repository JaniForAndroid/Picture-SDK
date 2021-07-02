package com.example.picsdk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Create time: 2019/10/14.
 */
public class AutoOrientationPicImageView extends AppCompatImageView {

  public AutoOrientationPicImageView(Context context) {
    super(context);
  }

  public AutoOrientationPicImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AutoOrientationPicImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (callback != null && getDrawable() != null) {
      callback.onImageSize(getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
    }
  }

  public interface Callback {
    void onImageSize(int w, int h);
  }
  private Callback callback;

  public void setCallback(Callback callback) {
    this.callback = callback;
  }
}
