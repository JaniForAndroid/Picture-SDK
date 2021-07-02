package com.namibox.commonlib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.example.picsdk.R;

/**
 * Created by sunha on 2017/11/15 0015.
 */

public class FixedAspectRatioFrameLayout extends FrameLayout {

  private float mAspectRatio;

  public FixedAspectRatioFrameLayout(Context context) {
    super(context);
  }

  public FixedAspectRatioFrameLayout(Context context, AttributeSet attrs) {
    super(context, attrs);

    init(context, attrs);
  }

  public FixedAspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedAspectRatioFrameLayout);

    mAspectRatio = a
        .getFloat(R.styleable.FixedAspectRatioFrameLayout_aspectRatio, 4f / 3);

    a.recycle();
  }

  public void setAspectRatio(float mAspectRatio) {
    this.mAspectRatio = mAspectRatio;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int originalWidth = MeasureSpec.getSize(widthMeasureSpec);

    int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

    int calculatedHeight = (int) (originalWidth * 1f / mAspectRatio);

    int finalWidth, finalHeight;

    boolean baseOnWidth = originalWidth > mAspectRatio * originalHeight;

    if (baseOnWidth) {

      finalWidth = originalWidth;
      finalHeight = calculatedHeight;


    } else {
      finalWidth = (int) (originalHeight * mAspectRatio);
      finalHeight = originalHeight;
    }

    super.onMeasure(
        MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
  }
}
