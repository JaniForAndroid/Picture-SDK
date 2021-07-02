package com.example.exoaudioplayer.video.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个简单的线性进度条
 * Created by ryan on 2015/1/20.
 */
public class ProgressView extends View {

  public static final int MAX_PROGRESS = 1000;
  private static final int MSG_UPDATE = 42;
  private static final int STEPS = 10;
  private static final int DELAY = 40;
  private int mProgressColor;
  private int mCurrentProgress;
  private int mTargetProgress;
  private int max = MAX_PROGRESS;
  private int mIncrement;
  private Handler mHandler;
  private Paint mPaint;
  private boolean[] mPlayTags;
  private List<Integer> mIndexList;

  public ProgressView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs, defStyle);
  }

  public ProgressView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ProgressView(Context context) {
    this(context, null);
  }

  private void init(Context context, AttributeSet attrs, int defStyle) {
    mProgressColor = 0xff80d8ff;
    //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageProgressView, defStyle, 0);
    //mProgressColor = a.getColor(R.styleable.PageProgressView_pp_color, 0xff47c519);
    //a.recycle();
    mPaint = new Paint();
    mPaint.setAntiAlias(false);
    mPaint.setStyle(Paint.Style.FILL);
    mCurrentProgress = 0;
    mTargetProgress = 0;
    mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        if (msg.what == MSG_UPDATE) {
          mCurrentProgress = Math.min(mTargetProgress,
              mCurrentProgress + mIncrement);
          invalidate();
          if (mCurrentProgress < mTargetProgress) {
            sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE), DELAY);
          }
        }
      }
    };
    mIndexList = new ArrayList<>();
  }

  public void setProgressColor(int progressColor) {
    this.mProgressColor = progressColor;
  }

  public void setProgress(int progress) {
    mCurrentProgress = mTargetProgress;
    mTargetProgress = progress;
    mIncrement = (mTargetProgress - mCurrentProgress) / STEPS;
    mHandler.removeMessages(MSG_UPDATE);
    mHandler.sendEmptyMessage(MSG_UPDATE);
  }

  public void setMax(int max) {
    if (max <= 0) {
      throw new IllegalArgumentException("illegal max value");
    }
    this.max = max;
  }

  public void setProgressData(boolean[] playTags) {
    mPlayTags = playTags;
  }

  private void drawLine(Canvas canvas){
    if (mPlayTags == null) {
      return;
    }
    //数据校验，防止出现中断答题后的断点
    mIndexList.clear();
    for (int i = 0; i < mPlayTags.length - 3; i++) {
      if (mPlayTags[i] && !mPlayTags[i + 1] && mPlayTags[i + 2]) {
        mIndexList.add(i + 1);
      } else if (mPlayTags[i] && !mPlayTags[i + 1] && !mPlayTags[i + 2] && mPlayTags[i + 3]) {
        mIndexList.add(i + 1);
        mIndexList.add(i + 2);
      }
    }
    for (int i = 0; i < mIndexList.size(); i++) {
      mPlayTags[mIndexList.get(i)] = true;
    }
    int head = -1;
    int tail = -1;
    for (int i = 0; i < mPlayTags.length; i++) {
      //是第一个1或上一个是0
      if (mPlayTags[i] && (i == 0 || !mPlayTags[i - 1])) {
        head = i;
      }
      //是最后一个1或下一个是0
      if (mPlayTags[i] && (i == mPlayTags.length - 1 || !mPlayTags[i + 1])) {
        tail = i;
        if (head != -1 && tail != -1) {
          canvas.drawRect(getWidth() * head / mPlayTags.length,
              0,
              getWidth() * (tail + 1) / mPlayTags.length,
              getHeight(),
              mPaint
          );
          head = -1;
          tail = -1;
        }
      }
    }
  }

  @Override
  public void onDraw(Canvas canvas) {
    mPaint.setColor(mProgressColor);
    if (mPlayTags == null) {
      canvas.drawRect(0, 0, getWidth() * mCurrentProgress / max, getHeight(), mPaint);
    } else {
      drawLine(canvas);
    }
  }
}
