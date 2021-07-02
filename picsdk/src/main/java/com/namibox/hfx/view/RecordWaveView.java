package com.namibox.hfx.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.example.picsdk.R;
import com.namibox.util.Utils;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sunha on 2015/9/23 0023.
 */
public class RecordWaveView extends View {

  private int middleBlank;
  private int mPaddingBottom;
  private int mPaddingTop;
  /**
   * 控件高度
   */
  private int mHeight;
  /**
   * 控件宽度
   */
  private int mWidth;
  private Paint mPlayedLinePaint;

  /**
   * 波形条数
   */
  private int lineNum = 0;
  /**
   * 播放位置
   */
  private int playPos = 0;
  /**
   * 每条波形头尾空白像素
   */
  private int blankPixels;
  /**
   * 每条波形头尾实心像素
   */
  private int soildPixels;

  /**
   * 每条波形总像素
   */
  private int linePixels;
  private FIFOImpl<Integer> data = new FIFOImpl<>(100);

  public RecordWaveView(Context context, AttributeSet attrs) {
    super(context, attrs);
    blankPixels = Utils.dp2px(context, 1.2f);
    soildPixels = Utils.dp2px(context, 2.4f);
    middleBlank = Utils.dp2px(context, 1.3f);
    mPaddingTop = Utils.dp2px(context, 6f);
    mPaddingBottom = Utils.dp2px(context, 10f);
    linePixels = blankPixels + soildPixels;

    mPlayedLinePaint = new Paint();
    mPlayedLinePaint.setAntiAlias(false);
    mPlayedLinePaint.setColor(getResources().getColor(R.color.hfx_white));
    mPlayedLinePaint.setStyle(Paint.Style.STROKE);
    mPlayedLinePaint.setStrokeWidth(soildPixels);

  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mHeight = h;
    mWidth = w;
    lineNum = mWidth / linePixels;
    Log.d("onSizeChanged", "lineNum=" + lineNum);
    data.setMaxSize(lineNum);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int offset = mWidth;

    //Path path = new Path();
//        canvas.drawPath(path, mMidLinePaint);
    if (!data.isEmpty()) {
      for (int i = data.size() - 1; i >= 0; i--) {
        //TODO IndexOutOfBoundsException
        int volume = data.getSafe(i);
        if (volume > 0) {
          float vol_top = mHeight / 2 - volume / 2;
          float vol_bottom = mHeight / 2 + volume / 2;
          canvas.drawLine(offset - soildPixels / 2, vol_top,
              offset - soildPixels / 2, mHeight / 2 - middleBlank, mPlayedLinePaint);
          canvas.drawLine(offset - soildPixels / 2, mHeight / 2 + middleBlank,
              offset - soildPixels / 2, vol_bottom, mPlayedLinePaint);
        }
        offset -= linePixels;
      }
    }
  }

  public void addVolume(double volume) {
    double bili = (volume) / (90);
    int lineHeight = (int) ((mHeight - mPaddingBottom - mPaddingTop) * bili * bili * bili * bili
        + middleBlank);
    if (lineHeight < 2) {
      lineHeight = 2;
    }
    data.addLastSafe(lineHeight);
    postInvalidate();

  }

  public void clear() {
    data.clear();
  }

  interface FIFO<T> extends List<T>, Deque<T>, Cloneable, java.io.Serializable {

    /**
     * 向最后添加一个新的，如果长度超过允许的最大值，则弹出一个
     */
    T addLastSafe(T addLast);

    /**
     * 弹出head，如果Size = 0返回null。而不同于pop抛出异常
     */
    T pollSafe();

    /**
     * 获得最大保存
     */
    int getMaxSize();

    /**
     * 设置最大存储范围
     *
     * @return 返回的是，因为改变了队列大小，导致弹出的head
     */
    List<T> setMaxSize(int maxSize);

    T getSafe(int i);

  }

  class FIFOImpl<T> extends LinkedList<T> implements FIFO<T> {

    private int maxSize = Integer.MAX_VALUE;
    private final Object synObj = new Object();

    public FIFOImpl() {
      super();
    }

    public FIFOImpl(int maxSize) {
      super();
      this.maxSize = maxSize;
    }

    @Override
    public T addLastSafe(T addLast) {
      synchronized (synObj) {
        T head = null;
        while (size() >= maxSize) {
          head = poll();
        }
        addLast(addLast);
        return head;
      }
    }

    @Override
    public T pollSafe() {
      synchronized (synObj) {
        return poll();
      }
    }

    @Override
    public List<T> setMaxSize(int maxSize) {
      List<T> list = null;
      if (maxSize < this.maxSize) {
        list = new ArrayList<>();
        synchronized (synObj) {
          while (size() > maxSize) {
            list.add(poll());
          }
        }
      }
      this.maxSize = maxSize;
      return list;
    }

    @Override
    public T getSafe(int i) {
      synchronized (synObj) {
        return get(i);
      }

    }

    @Override
    public int getMaxSize() {
      return this.maxSize;
    }
  }
}
