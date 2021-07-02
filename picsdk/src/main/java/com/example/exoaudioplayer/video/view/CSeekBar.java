package com.example.exoaudioplayer.video.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.List;

public class CSeekBar extends AppCompatSeekBar {

  private Paint mPaint;
  private boolean[] mPlayTags;
  private int mSecondaryProgress;
  private List<Integer> mIndexList;

  public CSeekBar(Context context) {
    this(context, null);
  }

  public CSeekBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    mPaint = new Paint();
    mPaint.setColor(0xff00b9ff);
    mIndexList = new ArrayList<>();
  }

  public void setProgressData(boolean[] playTags) {
    if (playTags == null) {
      return;
    }
    mPlayTags = playTags;
    invalidate();
  }

  @Override
  public synchronized void setSecondaryProgress(int secondaryProgress) {
    mSecondaryProgress = secondaryProgress;
    invalidate();
  }

  private void drawLine(Canvas canvas) {
    mPaint.setColor(0xffd0d0d0);
    int progressLength = getWidth() - getPaddingLeft() - getPaddingRight();
    canvas.drawRect(getPaddingLeft(),
        getHeight() / 2 - com.namibox.util.Utils.dp2px(getContext(), 1),
        getPaddingLeft() + progressLength,
        getHeight() / 2 + com.namibox.util.Utils.dp2px(getContext(), 1),
        mPaint);
    mPaint.setColor(0xffaaafab);
    canvas.drawRect(getPaddingLeft(),
        getHeight() / 2 - com.namibox.util.Utils.dp2px(getContext(), 1),
        getPaddingLeft() + progressLength * mSecondaryProgress / getMax(),
        getHeight() / 2 + com.namibox.util.Utils.dp2px(getContext(), 1),
        mPaint);
    mPaint.setColor(0xff00b9ff);
    if (mPlayTags == null) {
      return;
    }
    //数据校验，防止出现中断答题后的断点
    mIndexList.clear();
    for (int i = 0; i < mPlayTags.length - 2; i++) {
      if (mPlayTags[i] && !mPlayTags[i + 1] && mPlayTags[i + 2]) {
        mIndexList.add(i + 1);
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
          canvas.drawRect(getPaddingLeft() + progressLength * head / mPlayTags.length,
              getHeight() / 2 - com.namibox.util.Utils.dp2px(getContext(), 1),
              getPaddingLeft() + progressLength * (tail + 1) / mPlayTags.length,
              getHeight() / 2 + com.namibox.util.Utils.dp2px(getContext(), 1),
              mPaint
          );
          head = -1;
          tail = -1;
        }
      }
    }
  }

  @Override
  public void draw(Canvas canvas) {
    drawLine(canvas);
    super.draw(canvas);
  }
}
