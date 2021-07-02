package com.namibox.imageselector.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.namibox.util.Utils;

/**
 * Created by Roy.chen on 2017/1/7.
 */

public class VerticalColorSeekBar extends View {

  private static final String TAG = VerticalColorSeekBar.class.getSimpleName();
  //    private int startColor;
//    private int secondColor;
//    private int thirdColor;
//    private int fourthColor;
//    private int fifthColor;
//    private int sixthColor;
//    private int seventhColor;
  private LinearGradient linearGradient;
  private int w;
  private int h;

  private int endColor = Color.WHITE;
  private int thumbColor = Color.BLACK;
  //    private int thumbBorderColor = Color.WHITE;
//    private int colorArray[] = {startColor, secondColor, thirdColor, fourthColor, fifthColor, sixthColor, seventhColor, endColor};
  private int colorArray[];
  private float x, y;
  private float mRadius;
  private float progress;
  //    private float maxCount = 100f;
  private float sLeft, sTop, sRight, sBottom;
  private float sWidth, sHeight;
  private Paint paint = new Paint();
  private Context context;
  //定义布尔型变量，用于判断当前控件是否被正在被触摸
  private boolean isTouching;
  //    protected VerticalColorSeekBar.OnStateChangeListener onStateChangeListener;
  private float circleX;


  public VerticalColorSeekBar(Context context) {
    this(context, null);
  }

  public VerticalColorSeekBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    initVpbColor();
    this.context = context;
  }

  @Override
  protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    h = getMeasuredHeight();
    //Log.e(TAG, "View的高度 = " + h);
    w = getMeasuredWidth();
    //Log.e(TAG, "View的宽度 = " + w);
    //滑块的半径
    circleX = Utils.dp2px(context, 15);
    sTop = Utils.dp2px(context, 15);
  }

  private void initVpbColor() {
//        int orange = Color.rgb(255, 125, 0);
//        int indigo = Color.rgb(0, 255, 255);
//        int purple = Color.rgb(255, 0, 255);
    colorArray = new int[361];
    for (int i = 0; i <= 360; i++) {
      float[] hsv = {i, 1, 1};
      colorArray[i] = Color.HSVToColor(hsv);
    }
//        colorArray[0] = Color.RED;
//        colorArray[1] = orange;
//        colorArray[2] = Color.YELLOW;
//        colorArray[3] = Color.GREEN;
//        colorArray[4] = Color.BLUE;
//        colorArray[5] = indigo;
//        colorArray[6] = purple;
//        colorArray[7] = Color.RED;
    this.thumbColor = Color.RED;
//        this.thumbBorderColor = Color.TRANSPARENT;

    //设置初始进度
    setProgress(0);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    h = getMeasuredHeight();
    //Log.e(TAG, "View的高度 = " + h);
    w = getMeasuredWidth();
    //Log.e(TAG, "View的宽度 = " + w);
    mRadius = (float) w / 10;
    sLeft = w * 3 / 4; // 背景左边缘坐标
    sRight = w * 17 / 20;// 背景右边缘坐标
    sBottom = h - circleX;
    sWidth = sRight - sLeft; // 背景宽度
    sHeight = h - circleX * 2; // 背景高度
    x = (float) w * 4 / 5;//圆心的x坐标
    y = (float) (sTop + (1 - 0.01 * progress) * sHeight);//圆心y坐标

    //如果滑块圆心的y坐标小于圆的半径
    y = y < sTop ? sTop : y;
    //如果滑块圆心的坐标大于sHeight - mRadius
//        y = y > sBottom - mRadius ? sBottom - mRadius : y;
    y = y > sBottom ? sBottom : y;

    drawBackground(canvas);
    drawCircle(canvas);
    paint.reset();
    //如果当前控件正被触摸，那么绘制指示器
    if (isTouching) {
      //画指示器
      int halfX = w / 2;
      paint.setColor(thumbColor);
      canvas.drawCircle(halfX - Utils.dp2px(context, 21.21f), y, circleX, paint);

      RectF oval = new RectF(halfX - circleX, y - circleX, halfX + circleX, y + circleX);
      canvas.drawArc(oval, 135, 90, true, paint);
    }


  }

  private void drawBackground(Canvas canvas) {
    RectF rectBlackBg = new RectF(sLeft, sTop, sRight, sBottom);

    paint.setAntiAlias(true);
    paint.setStyle(Paint.Style.FILL);

    //设置渲染器
    linearGradient = new LinearGradient(sLeft, sTop, sRight, sBottom, colorArray, null,
        Shader.TileMode.MIRROR);
    paint.setShader(linearGradient);
    canvas.drawRoundRect(rectBlackBg, sWidth / 2, sWidth / 2, paint);
  }

  private void drawCircle(Canvas canvas) {
    Paint thumbPaint = new Paint();
    thumbPaint.setAntiAlias(true);
    thumbPaint.setStyle(Paint.Style.FILL);
    thumbPaint.setColor(thumbColor);
    canvas.drawCircle(x, y, mRadius, thumbPaint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int eventAction = event.getAction();
    switch (eventAction) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_MOVE:
        if (eventAction == MotionEvent.ACTION_DOWN) {
          if (event.getX() < w * 7 / 10 || event.getX() > w * 9 / 10) {
            return false;
          }
          isTouching = true;
        }
        //改变进度
        this.y = event.getY();
        progress = (sHeight + circleX - y) / sHeight * 100;
        setProgress(progress);
        float ratio = progress / 100;
        int h = (int) ((1 - ratio) * 360);

        thumbColor = Color.HSVToColor(new float[]{h, 1, 255});

        //将结果回传给PaintImageView
        if (this.listener != null) {
          listener.onPaintColorChangedListener(thumbColor, y);
        }
        break;
      case MotionEvent.ACTION_UP:
        isTouching = false;
        break;
    }
    this.invalidate();

    return true;
  }


  public void setProgress(float progress) {
    this.progress = progress;
    invalidate();
  }

  //定义接口，当颜色改变时把颜色回传给画笔
  public interface PaintColorChangedListener {

    void onPaintColorChangedListener(int color, float y);
  }

  private PaintColorChangedListener listener;

  public void setOnPaintColorChangedListener(PaintColorChangedListener listener) {
    this.listener = listener;
  }


}
