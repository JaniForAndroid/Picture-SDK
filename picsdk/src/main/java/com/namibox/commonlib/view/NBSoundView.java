package com.namibox.commonlib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import com.example.picsdk.R;

/**
 * author : feng
 * description ：音频播放显示
 * creation time : 19-12-23上午11:20
 */
public class NBSoundView extends View {

  private Paint paint;
  private float startX1;
  private float startX2;
  private float startX3;
  private float startY;
  private float maxY;
  private float minY;
  private float[] dys = new float[3];
  private boolean[] increments = new boolean[]{true, false, true};

  private int speed;
  private boolean animator;

  public NBSoundView(Context context) {
    this(context, null);
  }

  public NBSoundView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public NBSoundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NBSoundView);
    int defValue = Color.parseColor("#00B9FF");
    int color = typedArray.getColor(R.styleable.NBSoundView_sound_color, defValue);
    speed = typedArray.getInt(R.styleable.NBSoundView_sound_speed, 10);
    animator = typedArray.getBoolean(R.styleable.NBSoundView_animator, true);
    typedArray.recycle();
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(color);
    paint.setStrokeCap(Cap.ROUND);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    float strokeWidth = w / 5f;
    paint.setStrokeWidth(strokeWidth);
    startX1 = strokeWidth / 2f;
    startX2 = strokeWidth * 5 / 2f;
    startX3 = strokeWidth * 9 / 2f;
    maxY = h - strokeWidth;
    //noinspection SuspiciousNameCombination
    minY = strokeWidth;
    startY = h - strokeWidth / 2f;
    dys[0] = maxY / 3f;
    dys[1] = maxY;
    dys[2] = maxY * 4 / 5f;
  }

  public void setColor(int color) {
    paint.setColor(color);
    invalidate();
  }

  public void setAnimator(boolean enable) {
    this.animator = enable;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    changeDy();
    canvas.drawLine(startX1, startY, startX1, startY - dys[0], paint);
    canvas.drawLine(startX2, startY, startX2, startY - dys[1], paint);
    canvas.drawLine(startX3, startY, startX3, startY - dys[2], paint);
    if (animator) {
      invalidate();
    }
  }

  private void changeDy() {
    for (int i = 0; i < dys.length; i++) {
      if (increments[i]) {
        dys[i] += speed;
        if (dys[i] > maxY) {
          dys[i] = maxY;
          increments[i] = false;
        }
      } else {
        dys[i] -= speed;
        if (dys[i] < minY) {
          dys[i] = minY;
          increments[i] = true;
        }
      }
    }

  }
}
