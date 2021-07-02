package com.namibox.commonlib.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class HighLightTextView extends AppCompatTextView {
  // 存储view的宽度
  private int mTextViewWidth = 0;
  // 画笔
  private Paint mPaint;
  // 线性渲染
  private LinearGradient mLinearGradient;
  // 存储变换的matrix
  private Matrix matrix;
  // 移动距离
  private int mTranslateX = 0;
  // 构造方法
  public HighLightTextView(Context context) {
    this(context, null);
  }
  public HighLightTextView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }
  public HighLightTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }
  /**
   * view的调用过程:构造方法->onFinishInflate->onSizeChanged->onDraw
   */
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    // 获取view的宽度，初始化画笔等初始属性
    if (mTextViewWidth == 0) {
      mTextViewWidth = getMeasuredWidth();
      // 如果宽度大于0的话，则初始化
      if (mTextViewWidth > 0) {
        // 初始化画笔
        mPaint = getPaint();
        // 线性渲染
        mLinearGradient = new LinearGradient(0, getMeasuredHeight(), mTextViewWidth, 0,
            new int[]{0X55999999, 0XFF999999, 0X55999999},
            new float[]{0, 0.5f, 1}, Shader.TileMode.CLAMP);
        mPaint.setShader(mLinearGradient);
        matrix = new Matrix();
      }
    }
  }
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (matrix != null) {
      mTranslateX += mTextViewWidth / 10;
      // 如果移动的距离大于两倍的宽度，则重新开始移动
      if (mTranslateX > 2 * mTextViewWidth) {
        mTranslateX = -mTextViewWidth;
      }
      // 平移matrix
      matrix.setTranslate(mTranslateX, 0);
      // 设置线性变化的matrix
      mLinearGradient.setLocalMatrix(matrix);
      // 延迟50ms重绘 (重绘时会重新调用onDraw)
      postInvalidateDelayed(50);
    }
  }
}
