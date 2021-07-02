package com.example.picsdk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.example.picsdk.R;


/**
 * Create time: 2018/6/14.
 */
public class AspectRatioImageView extends android.support.v7.widget.AppCompatImageView {

  private final static String TAG = "AspectRatioImageView";

  private int widthRatio;
  private int heightRatio;
  private Paint paint = new Paint();
  private PorterDuffXfermode xfermode;
  private Path path = new Path();
  private RectF rectF = new RectF();
  private float[] radii = new float[8];
  private boolean hasRadius;

  public AspectRatioImageView(Context context) {
    this(context, null);
  }

  public AspectRatioImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    xfermode = new PorterDuffXfermode(Mode.DST_OUT);
    paint.setAntiAlias(true);
    paint.setStyle(Paint.Style.FILL);
    path.setFillType(Path.FillType.INVERSE_EVEN_ODD);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioImageView);
    try {
      widthRatio = a.getInteger(R.styleable.AspectRatioImageView_width_ratio, 0);
      heightRatio = a.getInteger(R.styleable.AspectRatioImageView_height_ratio, 0);
      float radius = a.getDimension(R.styleable.AspectRatioImageView_round_radius, 0);
      float leftTopRadius = a.getDimension(R.styleable.AspectRatioImageView_leftTop_radius, 0);
      float topRightRadius = a.getDimension(R.styleable.AspectRatioImageView_topRight_radius, 0);
      float rightBottomRadius = a
          .getDimension(R.styleable.AspectRatioImageView_rightBottom_radius, 0);
      float bottomLeftRadius = a
          .getDimension(R.styleable.AspectRatioImageView_bottomLeft_radius, 0);
      hasRadius =
          radius > 0 || leftTopRadius > 0 || topRightRadius > 0 || rightBottomRadius > 0
              || bottomLeftRadius > 0;

      if (hasRadius) {
        if (radius > 0) {
          for (int i = 0; i < radii.length; i++) {
            radii[i] = radius;
          }
        }
        if (leftTopRadius > 0) {
          radii[0] = radii[1] = leftTopRadius;
        }
        if (topRightRadius > 0) {
          radii[2] = radii[3] = topRightRadius;
        }
        if (rightBottomRadius > 0) {
          radii[4] = radii[5] = rightBottomRadius;
        }
        if (bottomLeftRadius > 0) {
          radii[6] = radii[7] = bottomLeftRadius;
        }
      }

    } finally {
      a.recycle();
    }
  }

  public void setRatio(int widthRatio, int heightRatio) {
    this.widthRatio = widthRatio;
    this.heightRatio = heightRatio;
    //requestLayout();
  }

  public void setRoundRadius(float leftTop, float topRight, float rightBottom, float bottomLeft) {
    radii[0] = radii[1] = leftTop;
    radii[2] = radii[3] = topRight;
    radii[4] = radii[5] = rightBottom;
    radii[6] = radii[7] = bottomLeft;
    hasRadius = true;
    invalidate();
  }

  public void setRoundRadius(float radius) {
    setRoundRadius(radius, radius, radius, radius);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (heightRatio == 0 || widthRatio == 0) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      return;
    }
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = width * heightRatio / widthRatio;
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    rectF.set(0, 0, w, h);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (hasRadius) {
      canvas.saveLayer(rectF, null, Canvas.ALL_SAVE_FLAG);
      super.onDraw(canvas);
      path.reset();
      path.addRoundRect(rectF, radii, Direction.CW);
      paint.setXfermode(xfermode);
      canvas.drawPath(path, paint);
      paint.setXfermode(null);
      canvas.restore();
    } else {
      super.onDraw(canvas);
    }
  }

}
