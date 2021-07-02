package com.namibox.commonlib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.example.picsdk.R;

public class TextProgress extends View {

  private static final String TAG = "CoolDownloading";
  private int vWidth, vHeight;
  private Paint innerPaint;
  private Path cornerRectPath, progressRectPath;
  private int insideColor = Color.parseColor("#00b9ff");
  private int outsideColor = Color.parseColor("#e6e6e6");
  private int textColor = Color.WHITE;
  private int progress = 0;
  private String fraction;
  private boolean progressFractionStyle;


  public TextProgress(Context context) {
    super(context);
  }

  public TextProgress(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  public TextProgress(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr);
  }

  private void init(final Context context, AttributeSet attrs, int defStyleAttr) {
    TypedArray typedArray = context.obtainStyledAttributes(attrs,  R.styleable.TextProgress, defStyleAttr, 0);
    progressFractionStyle = typedArray.getBoolean(R.styleable.TextProgress_progress_fraction_style, false);
    typedArray.recycle();
    innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    innerPaint.setStrokeWidth(5);
    innerPaint.setStyle(Paint.Style.FILL);
    cornerRectPath = new Path();
    progressRectPath = new Path();


  }


  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    drawLoadingBar(canvas);
  }


  /**
   * @param canvas
   */
  private void drawLoadingBar(Canvas canvas) {
    innerPaint.setStyle(Paint.Style.FILL);
    innerPaint.setPathEffect(new CornerPathEffect(vHeight * 3 / 5));
    //完整矩形左上角，右下角的点
    Point lu = new Point(vHeight / 2, 0);
    Point rd = new Point(vWidth - vHeight / 2, vHeight);

    innerPaint.setTextSize(vHeight * 0.6f);
    innerPaint.setColor(outsideColor);
    cornerRectPath.reset();
    cornerRectPath.moveTo(lu.x, lu.y);
    cornerRectPath.lineTo(rd.x, lu.y);
    cornerRectPath.lineTo(rd.x, rd.y);
    cornerRectPath.lineTo(lu.x, rd.y);
    cornerRectPath.close();
    canvas.drawPath(cornerRectPath, innerPaint);

    String text;
    if (progressFractionStyle) {
      if (fraction != null) {
        text = fraction;
      } else {
        text = "";
      }
    } else {
      text = progress + "%";
    }
    float defaultWidth = 1.5f * vHeight;
    float textWidth = vHeight * 0.48f * text.length();
    float realProgressWidth = vWidth - vHeight - defaultWidth;
    //已下载长度
    innerPaint.setColor(insideColor);
    progressRectPath.reset();
    progressRectPath.moveTo(lu.x, lu.y);
    progressRectPath.lineTo(lu.x + progress * 0.01f * realProgressWidth + defaultWidth, lu.y);
    progressRectPath.lineTo(lu.x + progress * 0.01f * realProgressWidth + defaultWidth, rd.y);
    progressRectPath.lineTo(lu.x, rd.y);
    progressRectPath.close();
    canvas.drawPath(progressRectPath, innerPaint);

    innerPaint.setColor(textColor);
    innerPaint.setPathEffect(null);
    canvas.drawText(text, lu.x + progress * 0.01f * realProgressWidth + defaultWidth - textWidth,
        vHeight * 13 / 18,
        innerPaint);
  }


  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    vHeight = getMeasuredHeight();
    vWidth = getMeasuredWidth();
    Log.i(TAG, "drawLoadingBar: " + vHeight);


  }


  /**
   * 设置下载进度
   */
  public void setProgress(int progress) {
    this.progress = progress;
    invalidate();
  }

  /**
   * 设置分数样式下载进度
   */
  public void setFractionProgress(int progress, String fraction) {
    this.progress = progress;
    this.fraction = fraction;
    invalidate();
  }


  private class Point {

    float x, y;

    public Point(float x, float y) {
      this.x = x;
      this.y = y;
    }

    public Point() {
    }
  }
}
