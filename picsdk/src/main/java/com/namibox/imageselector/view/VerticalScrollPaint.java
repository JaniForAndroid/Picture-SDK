package com.namibox.imageselector.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.namibox.util.Utils;

/**
 * Created by Roy.chen on 2017/1/9.
 */

public class VerticalScrollPaint extends View {

  private int width;
  private int height;
  private Context context;
  //给圆的x坐标一个初始值
  private float y;
  private float circleX;
  private boolean isTouching;
  private Paint scrollPaint;
  private Paint indicatorPaint;
  private RectF oval;
  private int halfX;
  private Paint textPaint;
  private boolean isFirstDraw = true;
  private Paint strokePaint;

  public VerticalScrollPaint(Context context) {
    this(context, null);
  }

  public VerticalScrollPaint(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    scrollPaint = new Paint();
    //绘制指示器的画笔
    indicatorPaint = new Paint();
    indicatorPaint.setColor(Color.BLACK);
    indicatorPaint.setAntiAlias(true);
    //绘制文字的画笔
    textPaint = new Paint();
    textPaint.setColor(Color.WHITE);
    textPaint.setAntiAlias(true);
    textPaint.setTextAlign(Paint.Align.CENTER);
    textPaint.setTextSize(Utils.dp2px(context, 18));
    textPaint.setStyle(Paint.Style.STROKE);
    textPaint.setTextSize(Utils.dp2px(context, 15));
    //滑块边框的画笔
    strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    strokePaint.setStyle(Paint.Style.STROKE);
    strokePaint.setStrokeWidth(Utils.dp2px(this.context, 1));
    strokePaint.setColor(Color.parseColor("#55000000"));
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    //从布局中获取的测量结果，保存
    setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    //获取自定义View的高度和宽度
    width = getMeasuredWidth();
    height = getMeasuredHeight();
    circleX = Utils.dp2px(context, 15);
    halfX = width / 2;

  }


  @Override
  protected void onDraw(Canvas canvas) {

    float left = width / 4;
    float top = circleX;
    float right = width * 5 / 16;
    float bottom = height - circleX;
    float sHeight = bottom - top;
    if (isFirstDraw) {
      y = bottom;
      isFirstDraw = false;
    }

    scrollPaint.setColor(0x60000000);
    scrollPaint.setAntiAlias(true);
    //1.画垂直线
    canvas.drawRect(left, top, right, bottom, scrollPaint);
    scrollPaint.setColor(Color.WHITE);
    scrollPaint.setStyle(Paint.Style.FILL);
    //如果滑块圆心的坐标小于圆的半径
    y = y <= top ? top : y;
    //如果滑块圆心的坐标大于sHeight - mRadius
    y = y > bottom ? bottom : y;

    int progress = (int) ((sHeight - y + top) / sHeight * 100);

    //计算圆的半径
    int mRadius = progress / 10 + 10;

    if (this.listener != null) {
      listener.onProgressChanged(mRadius);
    }
    //2.画滑块
    int x = width * 9 / 32;
//        canvas.drawCircle(width / 2, y, mRadius, scrollPaint);
    canvas.drawCircle(x, y, mRadius, scrollPaint);
    //画滑块的边框
    canvas.drawCircle(x, y, mRadius, strokePaint);

    //画指示器
    if (isTouching) {
//            int halfX = width / 2;
      //画圆

      //这里好像好想避免不了要创建对象？？
      oval = new RectF(halfX - circleX, y - circleX, halfX + circleX, y + circleX);
      canvas.drawCircle(halfX + Utils.dp2px(context, 21.21F), y, circleX, indicatorPaint);

      //画弧
      canvas.drawArc(oval, 315, 90, true, indicatorPaint);
      //画文本
//            Paint textPaint = new Paint();
//            textPaint.setColor(Color.WHITE);
//            textPaint.setAntiAlias(true);
//            textPaint.setTextAlign(Paint.Align.CENTER);
//            textPaint.setTextSize(DensityUtil.dp2px(context, 18));
//            textPaint.setStyle(Paint.Style.STROKE);
//            textPaint.setTextSize(DensityUtil.dp2px(context, 15));
      String text = progress / 5 + 1 + "";
      Paint.FontMetrics fm = textPaint.getFontMetrics();
      float y = this.y - fm.descent + (fm.descent - fm.ascent) / 2;
//            canvas.drawText(text, halfX + DensityUtil.dp2px(context, 14.21F), y + DensityUtil.dp2px(context, 2.21F), textPaint);
      canvas.drawText(text, halfX + Utils.dp2px(context, 20.21F), y, textPaint);
    }
//        scrollPaint.reset();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getAction();
    switch (action) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_MOVE:
        if (action == MotionEvent.ACTION_DOWN) {
          if (event.getX() > width / 2 || event.getX() < width / 5) {
            return false;
          }
          isTouching = true;
        }
        //得到圆的x轴的坐标
        y = event.getY();
//                y = y <= circleX ? circleX : y;
//                //如果滑块圆心的坐标大于sHeight - mRadius
//                y = y >= sHeight + circleX ? sHeight + circleX : y;
////                progress =(sHeight - y + circleX) / sHeight * 100;
////                this.y = event.getY();
        break;
      case MotionEvent.ACTION_UP:
        isTouching = false;
        break;
    }
    //强制重绘
    this.invalidate();

    return true;
  }

  public interface OnProgressChangedListener {

    void onProgressChanged(int radius);
  }

  private OnProgressChangedListener listener;

  public void setOnProgressChangedListener(OnProgressChangedListener listener) {
    this.listener = listener;
  }


}