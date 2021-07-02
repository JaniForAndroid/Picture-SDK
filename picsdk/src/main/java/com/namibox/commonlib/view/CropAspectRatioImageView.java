package com.namibox.commonlib.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import com.namibox.util.Utils;

/**
 * 图片从上至下展示 并且设置原件
 */

public class CropAspectRatioImageView extends AppCompatImageView {

  private static final int TOP_CENTER = 1;
  private static final int BOTTOM_CENTER = 0;
  private int mMatrixType = TOP_CENTER;
  private int radius = 3;
  float width, height;
  public CropAspectRatioImageView(Context context) {
    super(context);
    init(context, null);
  }

  public CropAspectRatioImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public CropAspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }
  private void init(Context context, AttributeSet attrs) {
    radius = Utils.dp2px(context,3);
    if (Build.VERSION.SDK_INT < 18) {
      setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
  }
  @Override
  protected boolean setFrame(int frameLeft, int frameTop, int frameRight, int frameBottom) {
    Drawable drawable = getDrawable();
    if (drawable != null) {
      float frameWidth = frameRight - frameLeft;
      float frameHeight = frameBottom - frameTop;

      float originalImageWidth = (float) getDrawable().getIntrinsicWidth();
      float originalImageHeight = (float) getDrawable().getIntrinsicHeight();

      float usedScaleFactor = 1;

      if ((frameWidth > originalImageWidth) || (frameHeight > originalImageHeight)) {
        // If frame is bigger than image
        // => Crop it, keep aspect ratio and position it at the bottom
        // and
        // center horizontally
        float fitHorizontallyScaleFactor = frameWidth / originalImageWidth;
        float fitVerticallyScaleFactor = frameHeight / originalImageHeight;

        usedScaleFactor = Math.max(fitHorizontallyScaleFactor, fitVerticallyScaleFactor);
      }

      float newImageWidth = originalImageWidth * usedScaleFactor;
      float newImageHeight = originalImageHeight * usedScaleFactor;

      Matrix matrix = getImageMatrix();
      matrix.setScale(usedScaleFactor, usedScaleFactor, 0, 0);

      switch (mMatrixType) {
        case TOP_CENTER:
          matrix.postTranslate((frameWidth - newImageWidth) / 2, 0);
          break;
        case BOTTOM_CENTER:
          matrix.postTranslate((frameWidth - newImageWidth) / 2, frameHeight - newImageHeight);
          break;

        default:
          break;
      }

      setImageMatrix(matrix);
    }
    return super.setFrame(frameLeft, frameTop, frameRight, frameBottom);
  }
  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    width = getWidth();
    height = getHeight();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (width >= 12 && height > 12) {
      Path path = new Path();
      //四个圆角
      path.moveTo(radius, 0);
      path.lineTo(width - radius, 0);
      path.quadTo(width, 0, width, radius);
      path.lineTo(width, height - radius);
      path.quadTo(width, height, width - radius, height);
      path.lineTo(radius, height);
      path.quadTo(0, height, 0, height - radius);
      path.lineTo(0, radius);
      path.quadTo(0, 0, radius, 0);
      canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
      canvas.clipPath(path);

    }
    super.onDraw(canvas);
  }

}
