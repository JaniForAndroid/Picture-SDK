package com.namibox.commonlib.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.example.picsdk.R;
import java.util.Random;

/**
 * @author zhangkx
 * @Date 2019/2/14 11:24
 */
public class PullEggView extends View {

  private Context context;
  /** 画曲线的paint */
  private Paint mLinePaint;
  /** 画锚点的paint */
  private Paint mPointPaint;
  /** 画圆形的paint */
  private Paint mBallPaint;
  /*** 线宽 */
  private float mRopWidth = dip2px(2f);
  /*** 线长 */
  private float mRopHeight = dip2px(50f);
  /*** 圆形半径 */
  private float mRadius = dip2px(8f);
  /** 默认绳子的颜色 */
  private int linePaintColor = 0xFFFF0000;
  private PointF start, end, control;
  private int centerX, centerY;
  private float downX;
  private float downY;
  private boolean isDrag;
  private boolean isComplete;
  private boolean isClicked;
  //左侧初始动画
  private ValueAnimator mAnimatorLeft;
  /** 动画是否在执行 */
  private boolean animationIsPlaying;
  private Bitmap controlBitmap, controlBitmapDown;

  public PullEggView(Context context) {
    super(context, null, 0);
    this.context = context;
    initPaint();
  }

  public PullEggView(Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs, 0);
    this.context = context;
    initPaint();
  }

  public PullEggView(Context context,
      @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.context = context;
    initPaint();
  }

  private void initPaint() {
    mLinePaint = new Paint();
    mLinePaint.setStrokeWidth(mRopWidth);
    mLinePaint.setStyle(Paint.Style.STROKE);
    mLinePaint.setAntiAlias(true);

    mPointPaint = new Paint();
    mPointPaint.setStrokeWidth(mRopWidth);
    mPointPaint.setStyle(Paint.Style.STROKE);
    mPointPaint.setAntiAlias(true);

    mBallPaint = new Paint();
    mBallPaint.setStrokeWidth(mRopWidth);
    mBallPaint.setStyle(Paint.Style.STROKE);
    mBallPaint.setAntiAlias(true);

    //初始化几个锚点
    start = new PointF(0, 0);
    end = new PointF(0, 0);
    control = new PointF(0, 0);
    //初始化控制点
    initControlBitmap();

  }

  /** 初始化控制点bitmap **/
  private void initControlBitmap() {
    if (isClicked) {
      controlBitmap = BitmapFactory
          .decodeResource(context.getResources(), R.drawable.gift_control_point_click);
      controlBitmapDown = BitmapFactory
          .decodeResource(context.getResources(), R.drawable.gift_control_point_click_bottom);
      linePaintColor = 0xFF00B9FF;
    } else {
      controlBitmap = BitmapFactory
          .decodeResource(context.getResources(), R.drawable.gift_control_point_normal);
      controlBitmapDown = BitmapFactory
          .decodeResource(context.getResources(), R.drawable.gift_control_point_normal_bottom);
      linePaintColor = 0xFFFF0000;
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    centerX = w / 2;
    centerY = h / 2;
    start.x = centerX;
    start.y = 0;
    end.x = centerX;
    end.y = mRopHeight;
    control.x = centerX;
    control.y = mRopHeight;
  }

  public void resetView() {
    start.x = centerX;
    start.y = 0;
    isDrag = false;
    isComplete = false;
    Random random = new Random();
    int i = random.nextInt(2);
    //0左侧初始化  否则右侧初始化
    if (i == 0) {
      setReleaseAnim(centerX - mRadius * 2, mRopHeight);
    } else {
      setReleaseAnim(centerX + mRadius * 2, mRopHeight);
    }

  }


  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // 绘制数据点和控制点
    mPointPaint.setColor(Color.TRANSPARENT);
    mLinePaint.setColor(linePaintColor);
    mBallPaint.setColor(Color.RED);
    canvas.drawPoint(start.x, start.y, mPointPaint);
    // 绘制贝塞尔曲线
    Path path = new Path();

    path.moveTo(start.x, start.y);
    path.quadTo(control.x, control.y, end.x, end.y);

    canvas.drawPath(path, mLinePaint);
    //绘制bitmap的矩阵
    try {
      if (controlBitmap != null) {
        Matrix matrixBitmap = new Matrix();
        int bitmapHeight = controlBitmap.getHeight();
        int bitmapWidth = controlBitmap.getWidth();
        matrixBitmap.postScale(1, 1);
        matrixBitmap.postTranslate(end.x - bitmapWidth / 2.0f, end.y - bitmapHeight / 2.0f);
        canvas.drawBitmap(controlBitmap, matrixBitmap, mBallPaint);
        //底部尾坠bitmap
        if (controlBitmapDown != null) {
          int width = controlBitmapDown.getWidth();
          canvas.drawBitmap(controlBitmapDown,end.x - width / 2.0f,end.y + bitmapHeight / 2.0f,mBallPaint);
        }

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (this.isEnabled()) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          //动画正在执行  不响应事件
          if (animationIsPlaying) {
            return false;
          }
          //如果下拉动作已完成 不响应事件
          if (isComplete) {
            return false;
          }
          downX = event.getX();
          downY = event.getY();
          //边界坐标点 扩大点击区域
          float borderLeft = centerX - 6 * mRadius;
          float borderRight = centerX + 6 * mRadius;
          float borderTop = mRopHeight - 6 * mRadius;
          float borderBottom = mRopHeight + 6 * mRadius;

          // 根据触摸位置更新控制点，并提示重绘
          if (downX >= borderLeft && downX <= borderRight && downY >= borderTop
              && downY <= borderBottom) {
            isDrag = true;
            //控制点重置
            control.x = event.getX();
            control.y = event.getY();
            //锚点位置重置
            end.x = event.getX();
            end.y = event.getY();
            if (onPullListener != null) {
              onPullListener.onDown();
            }
            isClicked = true;
            initControlBitmap();
            invalidate();//刷新View，重新绘制
            return true;
          }
          break;
        case MotionEvent.ACTION_MOVE:
          if (isDrag) {
            if (isComplete) {
              return false;
            }
            control.x = event.getX();
            control.y = event.getY();
            //锚点位置重置
            end.x = event.getX();
            end.y = event.getY();
            //垂直移动的距离
            float diffY = event.getY() - mRopHeight;
            if (diffY > 3 * mRopHeight) {
              if (onPullListener != null) {
                onPullListener.onComplete();
              }
              isComplete = true;
              isClicked = false;
              initControlBitmap();
              //完成后
              setReleaseAnim(event.getX(), event.getY());
              invalidate();//刷新View，重新绘制
              return false;
            } else {
              if (onPullListener != null) {
                onPullListener.onMove(diffY);
              }
            }
            invalidate();//刷新View，重新绘制
            return true;
          }

          break;
        case MotionEvent.ACTION_UP:
          if (isDrag) {
            if (isComplete) {
              return false;
            } else {
              //手指松开动画
              setReleaseAnim(event.getX(), event.getY());
            }
            float x = downX - event.getX();
            float y = downY - event.getY();

            if (Math.abs(x) <=5 && Math.abs(y) <= 5) {
              if (onPullListener != null) {
                onPullListener.onClick();
              }
            }else{
              if (onPullListener != null) {
                onPullListener.onUp();
              }
            }
            isDrag = false;
            isClicked = false;
            initControlBitmap();
            invalidate();//刷新View，重新绘制
            return true;
          }

          break;
        case MotionEvent.ACTION_CANCEL:
          // 不处理f
          isDrag = false;
          Log.e("zkx", "ACTION_CANCEL  ");
          break;
        default:
          break;
      }
    }
    return super.onTouchEvent(event);
  }

  private void setReleaseAnim(final float scrollX, float scrollY) {
    animationIsPlaying = true;
    final float pointY = mRopHeight - scrollY / 2;
    ValueAnimator anim = ValueAnimator.ofObject(new PointFEvaluator(),
        new PointF(scrollX, scrollY),
        new PointF(centerX, pointY));
    anim.setDuration(1000);
    //自定义Interpolator差值器达到颤动效果
    anim.setInterpolator(new TimeInterpolator() {
      @Override
      public float getInterpolation(float input) {
        //http://inloop.github.io/interpolator/
        float f = 0.571429f;
        return (float) (Math.pow(2, -4 * input) * Math.sin((input - f / 4) * (2 * Math.PI) / f) + 1);
      }
    });
    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {

        PointF curPoint = (PointF) animation.getAnimatedValue();
        control.x = curPoint.x;
        control.y = curPoint.y;
        //锚点位置重置
        end.x = curPoint.x;
        end.y = curPoint.y;
        invalidate();
      }
    });
    anim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        setRestoreAnim(scrollX, pointY);
      }
    });
    anim.start();
  }

  private void setRestoreAnim(float scrollX, float scrollY) {
    ValueAnimator anim = ValueAnimator.ofObject(new PointFEvaluator(),
        new PointF(centerX, scrollY),
        new PointF(centerX, mRopHeight));
    anim.setDuration(2000);
    //自定义Interpolator差值器达到颤动效果
    anim.setInterpolator(new TimeInterpolator() {
      @Override
      public float getInterpolation(float input) {
        //http://inloop.github.io/interpolator/
        float f = 0.571429f;
        return (float) (Math.pow(2, -4 * input) * Math.sin((input - f / 4) * (2 * Math.PI) / f) + 1);
      }
    });
    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        PointF curPoint = (PointF) animation.getAnimatedValue();
        control.x = curPoint.x;
        control.y = curPoint.y;
        //锚点位置重置
        end.x = curPoint.x;
        end.y = curPoint.y;
        invalidate();
      }
    });
    anim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        animationIsPlaying = false;
      }
    });
    anim.start();
  }


  public float dip2px(Float dp) {
    if (dp != null) {
      final Float scale = getContext().getResources().getDisplayMetrics().density;
      return dp * scale + 0.5f;
    }
    return dp;
  }

  public interface OnPullListener {

    /** 按下的监听 */
    void onDown();

    /***
     * 移动的监听
     * @param distance 移动的距离
     */
    void onMove(float distance);

    /***松开的监听*/
    void onUp();
    /***点击的监听*/
    void onClick();

    /***到达指定位置的监听*/
    void onComplete();
  }

  private OnPullListener onPullListener;

  public void setOnPullListener(OnPullListener listener) {
    this.onPullListener = listener;
  }
}
