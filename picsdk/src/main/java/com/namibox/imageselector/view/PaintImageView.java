package com.namibox.imageselector.view;

import static com.namibox.imageselector.view.PaintImageView.Text.MODE.DRAG;
import static com.namibox.imageselector.view.PaintImageView.Text.MODE.NORMAL;
import static com.namibox.imageselector.view.PaintImageView.Text.MODE.ZOOM;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.example.picsdk.R;
import com.namibox.util.ImageUtil;
import com.namibox.util.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Create time: 2016/4/7.
 */
public class PaintImageView extends View {

  static final String TAG = "PaintImageView";
  private Context mContext;
  private Paint paint = null;
  private List<Line> lines = new ArrayList<>();
  private Shader shader;
  private Bitmap srcBmp, blurBmp;
  private RectF bmpRect;
  private float bmpScale;
  private int nextPaintColor;
  private float nextPaintSize;
  private static final int MOSAIC_SHADOW_COLOR = 0x40808080;
  private static final int MOSAIC_BLOCK_SIZE = 20;
  private static final int FROG_SHADOW_COLOR = 0x80808080;
  private static final float FROG_BITMAP_SCALE = 4f;
  private static final int FROG_RADIUS = 8;
  private Mode mode = Mode.PAINT;
  private int measuredWidth;
  private int measuredHeight;
  //控制缩放旋转的图标
  private Drawable controlDrawable;
  //删除图标
  private Drawable deleteDrawable;
  //控制图标的宽和高
  private int drawableWidth;
  private int drawableHeight;
  //存放文字的对象
  private ArrayList<Text> textList;
  //记录当前处于编辑状态的文字
  private Text curText;
  //    private ImageView ivBackOut;
  private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      for (Text text : textList) {
        text.isEditable = false;
      }
      invalidate();
    }
  };
  private Text modifiedText;

  /**
   * 点击撤销按钮调用的方法
   */
  public void backOut() {

    if (lines != null && lines.size() > 0) {
      int index = lines.size() - 1;
      Line line = lines.get(index);
      line.path.reset();
      line.paint.reset();
      lines.remove(index);

      invalidate();
    }

  }

  public enum Mode {
    NORMAL, MOSAIC, PAINT, FROG, CROP, TEXT
  }

  static class Line {

    Paint paint;
    Path path;
  }

  public PaintImageView(Context context) {
    super(context);
    this.mContext = context;
    init();
  }

  public PaintImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.mContext = context;
    init();
  }

  public PaintImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.mContext = context;
    init();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public PaintImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    this.mContext = context;
    init();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    measuredWidth = getMeasuredWidth();
    measuredHeight = getMeasuredHeight();
  }

  private void init() {
    setLayerType(LAYER_TYPE_SOFTWARE, null);
    paint = new Paint();
    bmpRect = new RectF();
    textList = new ArrayList<>();
    controlDrawable = mContext.getResources().getDrawable(R.drawable.ic_rotate_icon);
    deleteDrawable = mContext.getResources().getDrawable(R.drawable.ic_delete);
    drawableWidth = controlDrawable.getIntrinsicWidth();
    drawableHeight = controlDrawable.getIntrinsicHeight();
  }


  /**
   * 暴露接口，当调用cleanPath()的时候让撤销按钮隐藏
   */
  public interface OnCleanPathListener {

    void setBackOutGone();
  }

  private OnCleanPathListener onCleanPathListener;

  public void setOnCleanPathListener(OnCleanPathListener onCleanPathListener) {
    this.onCleanPathListener = onCleanPathListener;
  }

  /**
   * 定义接口，监听PaintImageView的触摸事件，用于实现选择器的隐藏和消失
   */
  public interface OnViewTouchListener {

    void onViewTouch(boolean isTouching);
  }

  private OnViewTouchListener onViewTouchListener;

  public void setOnViewTouchListener(OnViewTouchListener onViewTouchListener) {
    this.onViewTouchListener = onViewTouchListener;
  }

  @Override
  public boolean onTouchEvent(MotionEvent motionEvent) {
    if (mode == Mode.NORMAL) {
      return true;
    }

//        int action = motionEvent.getAction();
//        float x = motionEvent.getX();
//        float y = motionEvent.getY();
//        if (action == MotionEvent.ACTION_DOWN) {
//
//            //按下的时候让选择器消失
//            if (onViewTouchListener != null) {
//                onViewTouchListener.onViewTouch(true);
//            }
//
//            Line line = new Line();
//            line.path = new Path();
//            line.path.moveTo(x, y);
//            line.paint = new Paint();
//            line.paint.setStrokeWidth(nextPaintSize);
//            line.paint.setColor(nextPaintColor);
//            line.paint.setAntiAlias(true);
//            line.paint.setDither(true);
//            line.paint.setStyle(Paint.Style.STROKE);
//            //设置线条两端为圆角
//            line.paint.setStrokeJoin(Paint.Join.ROUND);
//            line.paint.setStrokeCap(Paint.Cap.ROUND);
//            line.paint.setShader(shader);
//            //添加到另外一个集合中，用于保存已画涂鸦
//            lines.add(line);
//        } else if (action == MotionEvent.ACTION_MOVE) {
//            lines.get(lines.size() - 1).path.lineTo(x, y);
//            if (onViewTouchListener != null) {
//                onViewTouchListener.onViewTouch(true);
//            }
//        } else if (action == MotionEvent.ACTION_UP) {
//            // 手指离开屏幕的时候让选择器显示
//            if (onViewTouchListener != null) {
//                onViewTouchListener.onViewTouch(false);
//            }
//            lines.get(lines.size() - 1).path.lineTo(x, y);
//        }
//        invalidate();

    float x = motionEvent.getX();
    float y = motionEvent.getY();
    switch (motionEvent.getAction()) {
      case MotionEvent.ACTION_DOWN:
        //按下的时候让选择器消失
        if (onViewTouchListener != null) {
          onViewTouchListener.onViewTouch(true);
        }
        //遍历集合，判断手指落下的位置是否刚好有文字
        for (Text text : textList) {

          //判断是否在删除图标内
          if (text.isEditable && x >= text.deleteBounds.left && x <= text.deleteBounds.right
              && y >= text.deleteBounds.top && y <= text.deleteBounds.bottom) {
            textList.remove(text);
            if (onViewTouchListener != null) {
              onViewTouchListener.onViewTouch(false);
            }
            invalidate();
            return false;
          }

          //判断是否在缩放图标的范围内
          if (text.isEditable && x >= text.controlBounds.left && x <= text.controlBounds.right
              && y >= text.controlBounds.top && y <= text.controlBounds.bottom) {
            handler.removeCallbacksAndMessages(null);
            text.downX = x;
            text.downY = y;
            text.prePoint = new PointF(x, y);
            text.status = ZOOM;
            this.curText = text;
            invalidate();
            return true;
          }

          //判断是否在文本拖动的范围内
          RectF rectF = new RectF();
          text.path.computeBounds(rectF, true);
          text.region.setPath(text.path,
              new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
          if (text.region.contains((int) x, (int) y)) {
            handler.removeCallbacksAndMessages(null);
            text.downX = x;
            text.downY = y;
            text.beforeX = text.x;
            text.beforeY = text.y;
            text.beforeCY = text.centerY;
            if (text.isEditable) {
              text.noMove = true;
              text.firstSelected = false;
            } else {
              text.isEditable = true;
              text.firstSelected = true;
            }
            this.curText = text;
            text.status = DRAG;
            invalidateText(text);
            return true;
          }

        }
        if (mode == Mode.TEXT) {
          return false;
        }

        handler.removeCallbacksAndMessages(null);
        handler.sendEmptyMessage(0);

        Line line = new Line();
        line.path = new Path();
        line.path.moveTo(x, y);
        line.paint = new Paint();
        line.paint.setStrokeWidth(nextPaintSize);
        line.paint.setColor(nextPaintColor);
        line.paint.setAntiAlias(true);
        line.paint.setDither(true);
        line.paint.setStyle(Paint.Style.STROKE);
        //设置线条两端为圆角
        line.paint.setStrokeJoin(Paint.Join.ROUND);
        line.paint.setStrokeCap(Paint.Cap.ROUND);
        line.paint.setShader(shader);
        //添加到另外一个集合中，用于保存已画涂鸦
        lines.add(line);
        break;
      case MotionEvent.ACTION_MOVE:
        if (onViewTouchListener != null) {
          onViewTouchListener.onViewTouch(true);
        }
        if (curText != null) {
          //拖动或者缩放过程中移除隐藏边框的消息
          handler.removeCallbacksAndMessages(null);
          if (curText.status == DRAG) {
            if (!curText.firstSelected) {
              curText.noMove = false;
            }
            int offsetX = (int) (x - curText.downX);
            int offsetY = (int) (y - curText.downY);

            curText.mLTPoint.x += offsetX;
            curText.mLTPoint.y += offsetY;
            curText.mRTPoint.x += offsetX;
            curText.mRTPoint.y += offsetY;
            curText.mRBPoint.x += offsetX;
            curText.mRBPoint.y += offsetY;
            curText.mLBPoint.x += offsetX;
            curText.mLBPoint.y += offsetY;

            //文本移动时更新x和y坐标
            curText.x += offsetX;
            curText.y += offsetY;
            curText.centerY += offsetY;

            //控制图标的边框也要更新
            curText.controlBounds.left = (int) (curText.mRTPoint.x - drawableWidth / 2);
            curText.controlBounds.top = (int) (curText.mRTPoint.y - drawableHeight / 2);
            curText.controlBounds.right = (int) (curText.mRTPoint.x + drawableWidth / 2);
            curText.controlBounds.bottom = (int) (curText.mRTPoint.y + drawableHeight / 2);

            //删除图标的边框也要更新
            curText.deleteBounds.left = (int) (curText.mLTPoint.x - drawableWidth / 2);
            curText.deleteBounds.top = (int) (curText.mLTPoint.y - drawableHeight / 2);
            curText.deleteBounds.right = (int) (curText.mLTPoint.x + drawableWidth / 2);
            curText.deleteBounds.bottom = (int) (curText.mLTPoint.y + drawableHeight / 2);

            //还要更新downX和downY坐标
            curText.downX = x;
            curText.downY = y;
          } else if (curText.status == ZOOM) {
            if (!curText.firstSelected) {
              curText.noMove = false;
            }
            //计算移动的点到中心点的距离
            float disX = x - curText.x;
            float disY = y - curText.centerY;
            float moveToCenterDistance = (float) Math.sqrt(disX * disX + disY * disY);
            float scale = moveToCenterDistance / curText.cornerToCenter;
            if (scale < Text.MIN_SCALE) {
              scale = Text.MIN_SCALE;
            }

            if (scale > Text.MAX_SCALE) {
              scale = Text.MAX_SCALE;
            }
            //记录当前的缩放比例
            curText.scale = scale;

            curText.mLTPoint.set(curText.x - curText.width * curText.scale / 2,
                curText.centerY - curText.height * curText.scale / 2);
            curText.mRTPoint.set(curText.x + curText.width * curText.scale / 2,
                curText.centerY - curText.height * curText.scale / 2);
            curText.mRBPoint.set(curText.x + curText.width * curText.scale / 2,
                curText.centerY + curText.height * curText.scale / 2);
            curText.mLBPoint.set(curText.x - curText.width * curText.scale / 2,
                curText.centerY + curText.height * curText.scale / 2);

            PointF centerPoint = new PointF(curText.x, curText.centerY);
            PointF mCurMovePointF = new PointF(x, y);
            // 角度
            double a = distance4PointF(centerPoint, curText.prePoint);
            double b = distance4PointF(curText.prePoint, mCurMovePointF);
            double c = distance4PointF(centerPoint, mCurMovePointF);

            double cosb = (a * a + c * c - b * b) / (2 * a * c);

            if (cosb >= 1) {
              cosb = 1f;
            }

            double radian = Math.acos(cosb);
            float newDegree = (float) radianToDegree(radian);

            //center -> proMove的向量， 我们使用PointF来实现
            PointF centerToProMove = new PointF((curText.prePoint.x - centerPoint.x),
                (curText.prePoint.y - centerPoint.y));

            //center -> curMove 的向量
            PointF centerToCurMove = new PointF((mCurMovePointF.x - centerPoint.x),
                (mCurMovePointF.y - centerPoint.y));

            //向量叉乘结果, 如果结果为负数， 表示为逆时针， 结果为正数表示顺时针
            float result =
                centerToProMove.x * centerToCurMove.y - centerToProMove.y * centerToCurMove.x;

            if (result < 0) {
              newDegree = -newDegree;
            }
            //更新旋转角度
            curText.degree += newDegree;
            PointF center = new PointF(curText.x, curText.centerY);
            curText.mLTPoint = obtainRotationPoint(center, curText.mLTPoint, curText.degree);
            curText.mRTPoint = obtainRotationPoint(center, curText.mRTPoint, curText.degree);
            curText.mRBPoint = obtainRotationPoint(center, curText.mRBPoint, curText.degree);
            curText.mLBPoint = obtainRotationPoint(center, curText.mLBPoint, curText.degree);

            //控制图标的坐标也要更新
            curText.controlBounds.left = (int) (curText.mRTPoint.x - drawableWidth / 2);
            curText.controlBounds.top = (int) (curText.mRTPoint.y - drawableHeight / 2);
            curText.controlBounds.right = (int) (curText.mRTPoint.x + drawableWidth / 2);
            curText.controlBounds.bottom = (int) (curText.mRTPoint.y + drawableHeight / 2);
            //删除图标更新标
            curText.deleteBounds.left = (int) (curText.mLTPoint.x - drawableWidth / 2);
            curText.deleteBounds.top = (int) (curText.mLTPoint.y - drawableHeight / 2);
            curText.deleteBounds.right = (int) (curText.mLTPoint.x + drawableWidth / 2);
            curText.deleteBounds.bottom = (int) (curText.mLTPoint.y + drawableHeight / 2);

            curText.prePoint.set(x, y);
          }

          //强制刷新
          invalidate();
          return true;
        }

        lines.get(lines.size() - 1).path.lineTo(x, y);

        break;
      case MotionEvent.ACTION_UP:
        // 手指离开屏幕的时候让选择器显示
        if (onViewTouchListener != null) {
          onViewTouchListener.onViewTouch(false);
        }
        //手指离开时，将curText置空
        if (curText != null) {
          if (curText.status == DRAG) {
            if (curText.x <= 0 || curText.x >= measuredWidth || curText.y <= 0
                || curText.y >= measuredHeight) {
              curText.x = curText.beforeX;
              curText.y = curText.beforeY;
              curText.centerY = curText.beforeCY;
              computePoint(curText);
            }
          }
          //2秒后让边框隐藏
          handler.removeCallbacksAndMessages(null);
          handler.sendEmptyMessageDelayed(0, 2000);
          if (curText.noMove && !curText.firstSelected) {
            modifiedText = curText;
            if (showDialogListener != null) {
              showDialogListener.showDialog(curText.content);
            }
          }
          curText.status = NORMAL;
          curText = null;
          invalidate();
          return true;
        }

        lines.get(lines.size() - 1).path.lineTo(x, y);
        break;
    }
    invalidate();

    return true;
  }

  private void invalidateText(Text curText) {
    for (Text text : textList) {
      if (text != curText) {
        text.isEditable = false;
      }
    }
    invalidate();
  }

  public void setPaintColor(int color) {
    nextPaintColor = color;
  }

  public void setPaintSize(float size) {
    nextPaintSize = size;
  }

  public void setBitmap(Bitmap bitmap) {
    srcBmp = bitmap;
    if (blurBmp != null) {
      blurBmp.recycle();
      blurBmp = null;
    }
    cleanPath();
  }

  public Bitmap getBitmap() {
    return srcBmp;
  }

  public void cleanPath() {
    for (Line line : lines) {
      line.path.reset();
      line.paint.reset();
    }
    lines.clear();
//        if (ivBackOut != null){
//            ivBackOut.setVisibility(INVISIBLE);
//        }
    if (this.onCleanPathListener != null) {
      onCleanPathListener.setBackOutGone();
    }
    invalidate();
  }

  public List<Line> getLines() {
    return lines;
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    Log.d(TAG, "setMode:" + mode);
    this.mode = mode;
    if (blurBmp != null) {
      blurBmp.recycle();
      blurBmp = null;
    }
    cleanPath();
  }


  public boolean hasModify() {
    return mode != Mode.NORMAL && !lines.isEmpty() || !textList.isEmpty();
  }

  public Bitmap saveBitmap() {
    int srcBmpWidth = srcBmp.getWidth();
    int srcBmpHeight = srcBmp.getHeight();
    Bitmap dst = Bitmap.createBitmap(srcBmpWidth, srcBmpHeight, Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(dst);
    RectF rectF = new RectF(0, 0, srcBmpWidth, srcBmpHeight);
    if (mode == Mode.FROG) {
      c.drawBitmap(blurBmp, null, rectF, paint);
    } else {
      c.drawBitmap(srcBmp, null, rectF, paint);
    }
    if (mode != Mode.NORMAL) {
      c.save();
      c.scale(1 / bmpScale, 1 / bmpScale);
      c.translate(-bmpRect.left, -bmpRect.top);
      for (Line line : lines) {
        if (!line.path.isEmpty()) {
          c.drawPath(line.path, line.paint);
        }
      }

      for (Text text : textList) {
        //缩放
        c.scale(text.scale, text.scale, text.x, text.centerY);
        //旋转
        c.rotate(text.degree, text.x, text.centerY);
        //绘制文本
        c.drawText(text.content, text.x, text.y, text.paint);
      }
      c.restore();
    }
    //如果点击的是保存按钮，那么添加水印
//        if (isExit) {
//            Bitmap waterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_namibox_logo);
//            int width = waterBitmap.getWidth();
//            int height = waterBitmap.getHeight();
//            c.drawBitmap(waterBitmap, srcBmpWidth - width - 20, srcBmpHeight - height - 20, paint);
//        }

    return dst;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (srcBmp == null) {
      return;
    }

    try {
      calBitmapRect();
      if (mode == Mode.PAINT || mode == Mode.NORMAL || mode == Mode.TEXT) {
        shader = null;
        canvas.drawBitmap(srcBmp, null, bmpRect, paint);
      } else if (mode == Mode.MOSAIC) {
        if (blurBmp == null) {
          Bitmap screen = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
          Canvas c = new Canvas(screen);
          blurBmp = mosaic(srcBmp, MOSAIC_BLOCK_SIZE, MOSAIC_SHADOW_COLOR);
          c.drawBitmap(blurBmp, null, bmpRect, paint);
          shader = new BitmapShader(screen,
              Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        }
        canvas.drawBitmap(srcBmp, null, bmpRect, paint);
      } else if (mode == Mode.FROG) {
        if (blurBmp == null) {
          Bitmap screen = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
          Canvas c = new Canvas(screen);
          c.drawBitmap(srcBmp, null, bmpRect, paint);
          shader = new BitmapShader(screen,
              Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
          blurBmp = ImageUtil.doBlur(srcBmp, FROG_RADIUS, FROG_BITMAP_SCALE, FROG_SHADOW_COLOR);
        }
        canvas.drawBitmap(blurBmp, null, bmpRect, paint);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (mode != Mode.NORMAL) {
      for (Line line : lines) {
        if (!line.path.isEmpty()) {
          canvas.drawPath(line.path, line.paint);
        }
      }
    }

    for (Text text : textList) {
//            //绘制矩形边框
//            canvas.drawRect(text.bounds, text.rectPaint);
      if (text.isEditable) {
        text.path.reset();
        text.path.moveTo(text.mLTPoint.x, text.mLTPoint.y);
        text.path.lineTo(text.mRTPoint.x, text.mRTPoint.y);
        text.path.lineTo(text.mRBPoint.x, text.mRBPoint.y);
        text.path.lineTo(text.mLBPoint.x, text.mLBPoint.y);
        text.path.lineTo(text.mLTPoint.x, text.mLTPoint.y);
        text.path.lineTo(text.mRTPoint.x, text.mRTPoint.y);
        canvas.drawPath(text.path, text.rectPaint);

        //删除图标
        controlDrawable.setBounds(text.controlBounds);
        controlDrawable.draw(canvas);
        //删除图标
        deleteDrawable.setBounds(text.deleteBounds);
        deleteDrawable.draw(canvas);
      }
      canvas.save();
      //缩放
      canvas.scale(text.scale, text.scale, text.x, text.centerY);
      //旋转
      canvas.rotate(text.degree, text.x, text.centerY);
      //绘制文本
      canvas.drawText(text.content, text.x, text.y, text.paint);
      canvas.restore();

//            controlDrawable.setBounds(text.bounds.right - drawableWidth / 2,text.bounds.top - drawableHeight / 2,text.bounds.right + drawableWidth / 2, text.bounds.top + drawableHeight / 2);
//                canvas.drawText(text.content, text.x, text.y, text.paint);
//                canvas.drawRect(text.bounds, text.rectPaint);

    }
  }

  private void calBitmapRect() {
    int bw = srcBmp.getWidth();
    int bh = srcBmp.getHeight();
    int sw = getWidth();
    int sh = getHeight();
    //图片宽高比
    float r = 1f * bw / bh;
    //控件宽高比
    float rb = 1f * sw / sh;
    if (r < rb) {
      //左右留白
      bmpScale = 1f * sh / bh;
      float w = r * sh;
      bmpRect.left = (sw - w) / 2;
      bmpRect.right = sw - bmpRect.left;
      bmpRect.top = 0;
      bmpRect.bottom = sh;
    } else {
      //上下留白
      bmpScale = 1f * sw / bw;
      float h = sw / r;
      bmpRect.left = 0;
      bmpRect.right = sw;
      bmpRect.top = (sh - h) / 2;
      bmpRect.bottom = sh - bmpRect.top;
    }
  }

  static Bitmap mosaic(Bitmap src, int blockSize, int shadowColor) {
    int bitmapWidth = src.getWidth();
    int bitmapHeight = src.getHeight();
    Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
        Bitmap.Config.ARGB_8888);//创建画布
    int row = bitmapWidth / blockSize;// 获得列的切线
    int col = bitmapHeight / blockSize;// 获得行的切线
    int[] block = new int[blockSize * blockSize];
    for (int i = 0; i <= row; i++) {
      for (int j = 0; j <= col; j++) {
        int length = block.length;
        int flag = 0;// 是否到边界标志
        if (i == row && j != col) {
          length = (bitmapWidth - i * blockSize) * blockSize;
          if (length == 0) {
            break;// 边界外已经没有像素
          }
          src.getPixels(block, 0, blockSize, i * blockSize, j
                  * blockSize, bitmapWidth - i * blockSize,
              blockSize);

          flag = 1;
        } else if (i != row && j == col) {
          length = (bitmapHeight - j * blockSize) * blockSize;
          if (length == 0) {
            break;// 边界外已经没有像素
          }
          src.getPixels(block, 0, blockSize, i * blockSize, j
              * blockSize, blockSize, bitmapHeight - j
              * blockSize);
          flag = 2;
        } else if (i == row && j == col) {
          length = (bitmapWidth - i * blockSize)
              * (bitmapHeight - j * blockSize);
          if (length == 0) {
            break;// 边界外已经没有像素
          }
          src.getPixels(block, 0, blockSize, i * blockSize, j
                  * blockSize, bitmapWidth - i * blockSize,
              bitmapHeight - j * blockSize);

          flag = 3;
        } else {
          src.getPixels(block, 0, blockSize, i * blockSize, j
              * blockSize, blockSize, blockSize);//取出像素数组
        }

        int r = 0, g = 0, b = 0, a = 0;
        for (int k = 0; k < length; k++) {
          r += Color.red(block[k]);
          g += Color.green(block[k]);
          b += Color.blue(block[k]);
          a += Color.alpha(block[k]);
        }
        int color = Color.argb(a / length, r / length, g / length, b
            / length);//求块内所有颜色的平均值
        for (int k = 0; k < length; k++) {
          block[k] = color;
        }
        if (flag == 1) {
          bitmap.setPixels(block, 0, bitmapWidth - i * blockSize,
              i * blockSize, j
                  * blockSize, bitmapWidth - i * blockSize,
              blockSize);
        } else if (flag == 2) {
          bitmap.setPixels(block, 0, blockSize, i * blockSize, j
              * blockSize, blockSize, bitmapHeight - j
              * blockSize);
        } else if (flag == 3) {
          bitmap.setPixels(block, 0, blockSize, i * blockSize, j
                  * blockSize, bitmapWidth - i * blockSize,
              bitmapHeight - j * blockSize);
        } else {
          bitmap.setPixels(block, 0, blockSize, i * blockSize, j
              * blockSize, blockSize, blockSize);
        }

      }
    }
    Canvas canvas = new Canvas(bitmap);
    canvas.drawColor(shadowColor);
    return bitmap;
  }

  static Bitmap blur(Context ctx, Bitmap image, float scale, int shadowColor, float radius) {
    int width = Math.round(image.getWidth() * scale);
    int height = Math.round(image.getHeight() * scale);

    Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
    Canvas canvas = new Canvas(inputBitmap);
    canvas.scale(1 / scale, 1 / scale);
    Paint paint = new Paint();
    paint.setFlags(Paint.FILTER_BITMAP_FLAG);
    canvas.drawColor(shadowColor);
    Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

    inputBitmap.recycle();
    return outputBitmap;
  }

  static class Text {

    //文字
    String content;
    //控制图标的矩形
    Rect controlBounds;
    Rect deleteBounds;
    //画笔
    Paint paint;
    Path path;
    //边框的画笔
    Paint rectPaint;
    //drawText时的x坐标
    float x;
    //drawText时的y坐标
    float y;
    //手指落下时x和y的坐标
    float downX;
    float downY;
    //文字的缩放比例
    float scale;
    PointF prePoint;
    //文字旋转的角度
    float degree = 0;
    static final float MIN_SCALE = 0.5f;
    static final float MAX_SCALE = 4.0f;
    MODE status;
    int width;
    int height;
    float cornerToCenter;
    float centerY;
    boolean noMove = false;
    Region region;

    /**
     * 图片四个点坐标
     */
    private PointF mLTPoint;
    private PointF mRTPoint;
    private PointF mRBPoint;
    private PointF mLBPoint;
    boolean isEditable;
    boolean firstSelected;
    float beforeX;
    float beforeY;
    float beforeCY;

    //当前的模式
    enum MODE {
      NORMAL, ZOOM, DRAG
    }
  }

  public void setText(String content) {
    if (modifiedText != null) {
      modifiedText.content = content;
      //重新计算文本的宽和高
      Rect bounds = new Rect();
      modifiedText.paint
          .getTextBounds(modifiedText.content, 0, modifiedText.content.length(), bounds);
      modifiedText.width = bounds.width() + 60;
      modifiedText.height = bounds.height() + 50;
      modifiedText.isEditable = true;
      computePoint(modifiedText);
      modifiedText = null;
    } else {
      //创建Text对象
      Text text = new Text();
      text.scale = 2.0f;
      //设置文字
      text.content = content;
      //创建文字画笔
      text.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      //设置文字画笔
      text.paint.setColor(Color.WHITE);
      //设置文字大小
      text.paint.setTextSize(Utils.dp2px(mContext, 20));
      text.paint.setTextAlign(Paint.Align.CENTER);
      //设置阴影效果
      text.paint.setShadowLayer(Utils.dp2px(mContext, 2), 0, 0, 0x88000000);
      //设置边框画笔
      text.rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
      text.rectPaint.setColor(Color.WHITE);
      text.rectPaint.setShadowLayer(Utils.dp2px(mContext, 3), 0, 0, 0x88000000);
      text.rectPaint.setStyle(Paint.Style.STROKE);
      text.rectPaint.setStrokeWidth(Utils.dp2px(mContext, 2));
      Rect bounds = new Rect();
      text.paint.getTextBounds(text.content, 0, text.content.length(), bounds);
      text.width = bounds.width() + Utils.dp2px(mContext, 20);
      text.height = bounds.height() + Utils.dp2px(mContext, 15);
      //创建Path对象
      text.path = new Path();
      //模式为正常
      text.status = NORMAL;
      text.isEditable = true;
      text.region = new Region();

      //通过FontMetrics来确定y坐标
      Paint.FontMetrics fontMetrics = text.paint.getFontMetrics();

      //赋值坐标
      text.x = measuredWidth / 2;
      text.y =
          measuredHeight / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.bottom;
      text.centerY = measuredHeight / 2;

      //计算矩形四个顶点坐标
      float ltX = text.x - text.width * text.scale / 2;
      float ltY = text.centerY - text.height * text.scale / 2;
      text.mLTPoint = new PointF(ltX, ltY);
      float rtX = text.x + text.width * text.scale / 2;
      float rtY = text.centerY - text.height * text.scale / 2;
      text.mRTPoint = new PointF(rtX, rtY);
      float rbX = text.x + text.width * text.scale / 2;
      float rbY = text.centerY + text.height * text.scale / 2;
      text.mRBPoint = new PointF(rbX, rbY);
      float lbX = text.x - text.width * text.scale / 2;
      float lbY = text.centerY + text.height * text.scale / 2;
      text.mLBPoint = new PointF(lbX, lbY);
      //边框范围
//            text.bounds = new Rect(measuredWidth / 2 - 100, measuredHeight / 2 - 80, measuredWidth / 2 + 100, measuredHeight / 2 + 80);
      text.controlBounds = new Rect((int) (text.mRTPoint.x - drawableWidth / 2),
          (int) (text.mRTPoint.y - drawableHeight / 2), (int) (text.mRTPoint.x + drawableWidth / 2),
          (int) (text.mRTPoint.y + drawableHeight / 2));
      text.deleteBounds = new Rect((int) (text.mLTPoint.x - drawableWidth / 2),
          (int) (text.mLTPoint.y - drawableHeight / 2), (int) (text.mLTPoint.x + drawableWidth / 2),
          (int) (text.mLTPoint.y + drawableHeight / 2));
      int halfWidth = text.width / 2;
      int halfHeight = text.height / 2;
      text.cornerToCenter = (float) Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight);
      //将对象添加到集合
      textList.add(text);

    }

    handler.removeCallbacksAndMessages(null);
    //发送延迟消息，文字边框隐藏
    handler.sendEmptyMessageDelayed(0, 2000);
    invalidate();
  }

  /**
   * 当修改文字后，由于文本的宽和高都变化了，所以需要重新计算坐标
   */
  private void computePoint(Text modifiedText) {
    modifiedText.mLTPoint.set(modifiedText.x - modifiedText.width * modifiedText.scale / 2,
        modifiedText.centerY - modifiedText.height * modifiedText.scale / 2);
    modifiedText.mRTPoint.set(modifiedText.x + modifiedText.width * modifiedText.scale / 2,
        modifiedText.centerY - modifiedText.height * modifiedText.scale / 2);
    modifiedText.mRBPoint.set(modifiedText.x + modifiedText.width * modifiedText.scale / 2,
        modifiedText.centerY + modifiedText.height * modifiedText.scale / 2);
    modifiedText.mLBPoint.set(modifiedText.x - modifiedText.width * modifiedText.scale / 2,
        modifiedText.centerY + modifiedText.height * modifiedText.scale / 2);
    PointF center = new PointF(modifiedText.x, modifiedText.centerY);
    modifiedText.mLTPoint = obtainRotationPoint(center, modifiedText.mLTPoint, modifiedText.degree);
    modifiedText.mRTPoint = obtainRotationPoint(center, modifiedText.mRTPoint, modifiedText.degree);
    modifiedText.mRBPoint = obtainRotationPoint(center, modifiedText.mRBPoint, modifiedText.degree);
    modifiedText.mLBPoint = obtainRotationPoint(center, modifiedText.mLBPoint, modifiedText.degree);

    //控制图标的坐标也要更新
    modifiedText.controlBounds.left = (int) (modifiedText.mRTPoint.x - drawableWidth / 2);
    modifiedText.controlBounds.top = (int) (modifiedText.mRTPoint.y - drawableHeight / 2);
    modifiedText.controlBounds.right = (int) (modifiedText.mRTPoint.x + drawableWidth / 2);
    modifiedText.controlBounds.bottom = (int) (modifiedText.mRTPoint.y + drawableHeight / 2);
    //删除图标更新标
    modifiedText.deleteBounds.left = (int) (modifiedText.mLTPoint.x - drawableWidth / 2);
    modifiedText.deleteBounds.top = (int) (modifiedText.mLTPoint.y - drawableHeight / 2);
    modifiedText.deleteBounds.right = (int) (modifiedText.mLTPoint.x + drawableWidth / 2);
    modifiedText.deleteBounds.bottom = (int) (modifiedText.mLTPoint.y + drawableHeight / 2);
  }

  /**
   * 获取旋转某个角度之后的点
   */
  public static PointF obtainRotationPoint(PointF center, PointF source, float degree) {
    //两者之间的距离
    PointF disPoint = new PointF();
    disPoint.x = source.x - center.x;
    disPoint.y = source.y - center.y;

    //没旋转之前的弧度
    double originRadian = 0;

    //没旋转之前的角度
    double originDegree = 0;

    //旋转之后的角度
    double resultDegree = 0;

    //旋转之后的弧度
    double resultRadian = 0;

    //经过旋转之后点的坐标
    PointF resultPoint = new PointF();

    double distance = Math.sqrt(disPoint.x * disPoint.x + disPoint.y * disPoint.y);
    if (disPoint.x == 0 && disPoint.y == 0) {
      return center;
      // 第一象限
    } else if (disPoint.x >= 0 && disPoint.y >= 0) {
      // 计算与x正方向的夹角
      originRadian = Math.asin(disPoint.y / distance);

      // 第二象限
    } else if (disPoint.x < 0 && disPoint.y >= 0) {
      // 计算与x正方向的夹角
      originRadian = Math.asin(Math.abs(disPoint.x) / distance);
      originRadian = originRadian + Math.PI / 2;

      // 第三象限
    } else if (disPoint.x < 0 && disPoint.y < 0) {
      // 计算与x正方向的夹角
      originRadian = Math.asin(Math.abs(disPoint.y) / distance);
      originRadian = originRadian + Math.PI;
    } else if (disPoint.x >= 0 && disPoint.y < 0) {
      // 计算与x正方向的夹角
      originRadian = Math.asin(disPoint.x / distance);
      originRadian = originRadian + Math.PI * 3 / 2;
    }

    // 弧度换算成角度
    originDegree = radianToDegree(originRadian);
    resultDegree = originDegree + degree;

    // 角度转弧度
    resultRadian = degreeToRadian(resultDegree);

    resultPoint.x = (int) Math.round(distance * Math.cos(resultRadian));
    resultPoint.y = (int) Math.round(distance * Math.sin(resultRadian));
    resultPoint.x += center.x;
    resultPoint.y += center.y;

    return resultPoint;
  }

  public ArrayList<Text> getTextList() {
    return textList;
  }

  /**
   * 角度换算成弧度
   */
  public static double degreeToRadian(double degree) {
    return degree * Math.PI / 180;
  }

  /**
   * 弧度换算成角度
   */
  public static double radianToDegree(double radian) {
    return radian * 180 / Math.PI;
  }

  /**
   * 两个点之间的距离
   */
  private float distance4PointF(PointF pf1, PointF pf2) {
    float disX = pf2.x - pf1.x;
    float disY = pf2.y - pf1.y;
    return (float) Math.sqrt(disX * disX + disY * disY);
  }

  public interface ShowDialogListener {

    void showDialog(String content);
  }

  private ShowDialogListener showDialogListener;

  public void setOnShowDialogListener(ShowDialogListener showDialogListener) {
    this.showDialogListener = showDialogListener;
  }
}
