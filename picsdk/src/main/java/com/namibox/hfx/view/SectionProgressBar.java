package com.namibox.hfx.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.example.picsdk.R;


public class SectionProgressBar extends View {

  private Paint paint;
  private RectF rectF;
  private float dividerWidth = 3;
  private int selectedColor, normalColor, rectColor, bgColor;
  private int progress;
  private boolean[] data;
  private float padding;
  private float strokeWidth;

  public SectionProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public SectionProgressBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }


  public SectionProgressBar(Context context) {
    super(context);
    init();
  }

  private void init() {
    selectedColor = getResources().getColor(R.color.theme_color);
    normalColor = getResources().getColor(R.color.hfx_control_color);
    rectColor = getResources().getColor(R.color.hfx_white);
    bgColor = getResources().getColor(R.color.hfx_section_progress_bg);
    padding = getResources().getDimension(R.dimen.hfx_section_progress_padding);
    strokeWidth = getResources().getDimension(R.dimen.hfx_section_progress_stroke_width);
    dividerWidth = getResources().getDimension(R.dimen.hfx_section_progress_divider);
    paint = new Paint();
    paint.setAntiAlias(false);
    rectF = new RectF();
  }

  public void setProgress(boolean[] data, int progress) {
    this.data = data;
    this.progress = progress;
    invalidate();
  }

  protected void onDraw(Canvas canvas) {
    canvas.drawColor(bgColor);
    float w = getWidth() - padding - padding;
    int h = getHeight();
    if (data == null || data.length == 0) {
      return;
    }
    float rectW = (w - dividerWidth * (data.length - 1)) / data.length;
    int index = 0;
    paint.setStyle(Paint.Style.FILL);
    for (boolean mark : data) {
      paint.setColor(mark ? selectedColor : normalColor);
      rectF.top = padding;
      rectF.bottom = h - padding;
      rectF.left = index * (rectW + dividerWidth) + padding;
      rectF.right = rectF.left + rectW;
      canvas.drawRect(rectF, paint);
      index++;
    }
    if (progress >= 0 && progress < data.length) {
      rectF.top = padding - strokeWidth / 2;
      rectF.bottom = h - padding + strokeWidth / 2;
      rectF.left = progress * (rectW + dividerWidth) + padding - strokeWidth / 2;
      rectF.right = rectF.left + rectW + strokeWidth;
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(strokeWidth);
      paint.setColor(rectColor);
      canvas.drawRect(rectF, paint);
    }

  }
}
