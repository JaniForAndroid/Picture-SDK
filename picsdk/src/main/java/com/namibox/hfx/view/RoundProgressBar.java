package com.namibox.hfx.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.example.picsdk.R;


public class RoundProgressBar extends View {

  private Paint paint, textPaint;
  private int roundColor;
  private int textColor;
  private int roundProgressColor;
  private float roundWidth;
  private float textSize;
  private RectF rectF;
  private int max = 100;
  private int progress;
  private boolean showText;

  public RoundProgressBar(Context context) {
    this(context, null);
  }

  public RoundProgressBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RoundProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    roundColor = getResources().getColor(R.color.hfx_white);
    roundProgressColor = getResources().getColor(R.color.theme_color);
    textColor = getResources().getColor(R.color.theme_color);
    roundWidth = getResources().getDimension(R.dimen.hfx_round_progress_width);
    textSize = getResources().getDimension(R.dimen.hfx_round_progress_text_size);

    rectF = new RectF();

    paint = new Paint();
    paint.setStyle(Paint.Style.STROKE);
    paint.setAntiAlias(true);

    textPaint = new Paint();
    textPaint.setTextAlign(Paint.Align.CENTER);
    textPaint.setAntiAlias(true);
  }


  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int cx = getWidth() / 2;
    int cy = getHeight() / 2;
    float radius = (Math.min(cx, cy) - roundWidth / 2);
    paint.setStrokeWidth(roundWidth);
    paint.setColor(roundColor);
    canvas.drawCircle(cx, cy, radius, paint);

    paint.setColor(roundProgressColor);
    rectF.left = roundWidth / 2;
    rectF.top = getHeight() / 2 - radius;
    rectF.right = getWidth() - roundWidth / 2;
    rectF.bottom = getHeight() / 2 + radius;
    canvas.drawArc(rectF, 270, 360 * progress / max, false, paint);

    if (showText) {
      textPaint.setColor(textColor);
      textPaint.setTextSize(textSize);
      Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
      int baseline = (getHeight() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
      String text = 100 * progress / max + "%";
      canvas.drawText(text, getWidth() / 2, baseline, textPaint);
    }
  }

  public void setRoundColor(int roundColor) {
    this.roundColor = roundColor;
  }

  public void setRoundProgressColor(int roundProgressColor) {
    this.roundProgressColor = roundProgressColor;
  }

  public void setTextColor(int textColor) {
    this.textColor = textColor;
  }

  public void setTextSize(float textSize) {
    this.textSize = textSize;
  }

  public void setRoundWidth(float roundWidth) {
    this.roundWidth = roundWidth;
  }

  public void setShowText(boolean showText) {
    this.showText = showText;
  }

  public void setMax(int max) {
    if (max <= 0) {
      throw new IllegalArgumentException("max must > 0");
    }
    this.max = max;
  }

  public void setProgress(int progress) {
    if (progress < 0) {
      progress = 0;
    }
    if (progress > max) {
      progress = max;
    }
    this.progress = progress;
    invalidate();
  }

}
