package com.namibox.hfx.view;

import android.animation.TimeAnimator;
import android.animation.TimeAnimator.TimeListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.picsdk.R;
import com.namibox.util.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunha on 2017/10/26 0026.
 */

public class NewWaveView extends View {

  private static final String TAG = "NewWaveView";
  //常量
  private int screenCapacity;
  private int waveIndexColor;
  private int waveColor;
  private int mPaddingBottom;
  private int topRedPointRadius;
  private int mPaddingTop;
  private int backwardAreaWidth;
  private int forwardAreaWidth;
  private int cutReactWidth;
  private int soildPixels;
  private int blankPixels;
  private int linePixels;
  private int mHeight;
  private int mWidth;
  private int waveMaskColor;
  private Paint publicPaint;
  private Path publicPath;


  private int publicOffset = 0;
  private int cutIndexPosition = 0;
  private List<Integer> rawVolumeList = new ArrayList();
  private CutStates cutState = CutStates.IDLE;
  private Mode mode = Mode.PLAY;
  private TimeAnimator valueAnimator;
  private int playLine = -1;

  private WaveViewListener mListener;
  private List<Integer> waveSourceInts = new ArrayList<>();
  private int middleBlank;
  private int wavePlayColor;


  public interface WaveViewListener {

    /**
     * startLine或者indexLine发生变化时回调，用来处理变化时，
     * 界面上显示的时间的刷新，同时记录数据，在需要剪裁时使用
     */
    void updateIndexLine(int startLine, int indexLine);

    /**
     * 点击剪裁区域之外的位置时调用，用来触发外部在playLine对应时间点进行播放，播放的进度需要外部将时间转化成对应的线的index并主动设置
     *
     * @param playLine 点击位置对应的数据的index
     * @see #setPlayLine(int)
     */
    void markerTouchOutZone(int playLine);

    /**
     * 和updateIndexLine类似，但是只刷新外部存储的数据，而不刷新时间显示
     * 现在感觉没什么必要区分两个方法，可能是之前改的时候有什么问题需要这样区分
     */
    void onRefresh(int startLine, int indexLine);

  }

  private enum CutStates {IDLE, SLIDE, STOP, BACKWARD, FORWARD}

  public enum Mode {PLAY, CUT}

  public NewWaveView(Context context) {
    super(context);
    init(context);


  }

  public NewWaveView(Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public NewWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    setFocusable(false);
    blankPixels = Utils.dp2px(context, 1.2f);
    soildPixels = Utils.dp2px(context, 2.4f);
    linePixels = blankPixels + soildPixels;
    middleBlank = Utils.dp2px(context, 1.3f);
    forwardAreaWidth = Utils.dp2px(context, 30f);
    backwardAreaWidth = Utils.dp2px(context, 30f);
    mPaddingTop = Utils.dp2px(context, 6f);
    mPaddingBottom = Utils.dp2px(context, 10f);
    topRedPointRadius = mPaddingTop / 2;
    cutReactWidth = Utils.dp2px(context, 10f);
    publicPaint = new Paint();
    publicPaint.setAntiAlias(false);
    publicPaint.setStyle(Paint.Style.FILL);
    publicPath = new Path();
    waveColor = ContextCompat.getColor(context, R.color.hfx_wave_color);
    waveIndexColor = ContextCompat.getColor(context, R.color.hfx_wave_index_color);
    wavePlayColor = ContextCompat.getColor(context, R.color.hfx_wave_play_color);
    waveMaskColor = ContextCompat.getColor(context, R.color.hfx_wave_mask);

  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    mHeight = MeasureSpec.getSize(heightMeasureSpec);
    mWidth = MeasureSpec.getSize(widthMeasureSpec);
    screenCapacity = (mWidth) / (blankPixels + soildPixels) + 1;
  }

  private void initPublicOffset() {
    if (rawVolumeList.size() * linePixels < (mWidth - forwardAreaWidth)) {
      publicOffset = 0;
    } else {
      publicOffset = rawVolumeList.size() * linePixels - mWidth + forwardAreaWidth;
    }
  }


  private int playPositionToLine(int position) {
    if (mode == Mode.CUT) {
      return positionToLineNum(position);
    } else {
      if (rawVolumeList.size() <= screenCapacity) {
        int line = positionToLineNum(position);
        if (line > rawVolumeList.size()) {
          return -1;
        } else {
          return line;
        }
      } else {
        return rawVolumeList.size() * position / mWidth;
      }
    }
  }

  private int playLineToRealPlayLine(int playLine) {
    if (mode == Mode.CUT) {
      return playLine;
    } else {
      if (rawVolumeList.size() <= screenCapacity) {
        if (playLine > rawVolumeList.size()) {
          return -1;
        } else {
          return playLine;
        }
      } else {
        return playLine * screenCapacity / rawVolumeList.size();
      }
    }
  }

  private int playLineToPosition(int playLine) {
    if (mode == Mode.CUT) {
      return lineToPosition(playLine);
    } else {
      if (rawVolumeList.size() <= screenCapacity) {
        if (playLine > rawVolumeList.size()) {
          return -1;
        } else {
          return lineToPosition(playLine);
        }
      } else {
        return playLine * mWidth / rawVolumeList.size();
      }
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        float x = event.getX();
        requestFocus();
        if (mode == Mode.CUT) {
          if (cutIndexPosition > 0 && cutIndexPosition - cutReactWidth < x
              && x < cutIndexPosition + cutReactWidth) {
            cutIndexPosition = (int) x;
            cutState = CutStates.SLIDE;
          } else if (x < cutIndexPosition) {
            if (mListener != null) {
              mListener.markerTouchOutZone(playPositionToLine((int) x));
            }
          }
        }
        if (mode == Mode.PLAY) {
          if (mListener != null) {
            mListener.markerTouchOutZone(playPositionToLine((int) x));
          }
        }

        break;
      case MotionEvent.ACTION_MOVE:
        float x2 = event.getX();
        if (mode == Mode.CUT) {
          if (cutState == CutStates.SLIDE || cutState == CutStates.FORWARD
              || cutState == CutStates.BACKWARD) {
            CutStates tempStatus;
            if (x2 < forwardAreaWidth) {
              tempStatus = CutStates.FORWARD;
            } else if (x2 > mWidth - backwardAreaWidth
                && rawVolumeList.size() * linePixels > mWidth - backwardAreaWidth) {
              tempStatus = CutStates.BACKWARD;
            } else {
              tempStatus = CutStates.SLIDE;
            }
            if (cutState == CutStates.SLIDE) {
              if (tempStatus == CutStates.FORWARD) {
                startForwardScroll();
              } else if (tempStatus == CutStates.BACKWARD) {
                startBackwardScroll();
              }
            } else if (cutState == CutStates.FORWARD && tempStatus != CutStates.FORWARD) {
              stopForwardScroll();
              tempStatus = CutStates.SLIDE;
            } else if (cutState == CutStates.BACKWARD && tempStatus != CutStates.BACKWARD) {
              stopBackwardScroll();
              tempStatus = CutStates.SLIDE;
            }
            cutIndexPosition = (int) x2;
            cutState = tempStatus;
            calculateCutIndexPosition();
            if (mListener != null) {
              int indexLine = positionToLineNum(cutIndexPosition);
              mListener.updateIndexLine(publicOffset / (soildPixels + blankPixels), indexLine);
            }
            this.invalidate();
            return true;
          }
        }

        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        if (mode == Mode.CUT) {
          if (cutState == CutStates.SLIDE || cutState == CutStates.FORWARD
              || cutState == CutStates.BACKWARD) {
            cutState = CutStates.STOP;
            this.invalidate();
          }
        }

        cancelValueAnimator();
        break;
      default:
        break;

    }
    return true;
  }


  private void stopBackwardScroll() {
    cancelValueAnimator();
  }


  private void stopForwardScroll() {
    cancelValueAnimator();
  }

  private void cancelValueAnimator() {
    if (valueAnimator != null && valueAnimator.isRunning()) {
      valueAnimator.cancel();

    }
  }

  private void startBackwardScroll() {
    if (valueAnimator == null) {
      valueAnimator = new TimeAnimator();
    }
    if (!valueAnimator.isRunning()) {
      valueAnimator.setTimeListener(new TimeListener() {
        @Override
        public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
          publicOffset += (int) (linePixels / 2 + totalTime / 100);
          if (publicOffset > rawVolumeList.size() * linePixels - mWidth + forwardAreaWidth) {
            publicOffset = rawVolumeList.size() * linePixels - mWidth + forwardAreaWidth;
            cancelValueAnimator();
          }
          calculateCutIndexPosition();
          if (mListener != null) {
            int indexLine = positionToLineNum(cutIndexPosition);
            mListener.updateIndexLine(publicOffset / (soildPixels + blankPixels), indexLine);
          }
          postInvalidate();
        }
      });
      valueAnimator.start();
    }


  }


  private void startForwardScroll() {

    valueAnimator = new TimeAnimator();
    valueAnimator.setTimeListener(new TimeListener() {
      @Override
      public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {

        publicOffset -= (int) (linePixels / 2 + totalTime / 100);
        if (publicOffset <= 0) {
          publicOffset = 0;
          cancelValueAnimator();
        }
        calculateCutIndexPosition();
        if (mListener != null) {
          int indexLine = positionToLineNum(cutIndexPosition);
          mListener.updateIndexLine(publicOffset / (soildPixels + blankPixels), indexLine);
        }
        postInvalidate();
      }
    });
    valueAnimator.start();

  }


  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mode == Mode.CUT) {
      drawWave(canvas);
      drawPlayWave(canvas);
      drawPlayIndex(canvas);
      drawCutMask(canvas);
      drawCutIndex(canvas);
    } else {
      drawPlayModeWave(canvas);
      drawPlayModeWaveUnPlayed(canvas);
      drawPlayModeIndex(canvas);
    }


  }

  private void drawPlayModeWaveUnPlayed(Canvas canvas) {
    publicPaint.setColor(waveColor);
    int realLine = playLineToRealPlayLine(playLine);
    int offset = 0;
    for (int i = 0; i < waveSourceInts.size(); i++) {
      if (i >= realLine) {
        int lineHeight = waveSourceInts.get(i);
        canvas.drawRect(offset, mHeight / 2 + middleBlank, offset + soildPixels,
            mHeight / 2 + lineHeight, publicPaint);

        canvas.drawRect(offset, mHeight / 2 - lineHeight, offset + soildPixels,
            mHeight / 2 - middleBlank, publicPaint);
      }
      offset += linePixels;
    }
  }

  private void drawPlayModeIndex(Canvas canvas) {
    if (playLine < 0) {
      return;
    }
    if (playLine < rawVolumeList.size()) {
      publicPaint.setColor(waveIndexColor);
      int pos = playLineToPosition(playLine);
      canvas.drawRect(pos - 1, mPaddingTop, pos + 1, mHeight - mPaddingBottom, publicPaint);
      publicPaint.setAntiAlias(true);
      canvas.drawCircle(pos, topRedPointRadius + 1, topRedPointRadius, publicPaint);
      canvas.drawCircle(pos, mHeight - mPaddingBottom + topRedPointRadius, topRedPointRadius,
          publicPaint);
    }
  }

  private void drawPlayModeWave(Canvas canvas) {
    if (playLine < 0) {
      return;
    }
    publicPaint.setColor(wavePlayColor);
    int realLine = playLineToRealPlayLine(playLine);
    if (realLine < 0) {
      return;
    }
    int offset = 0;
    for (int i = 0; i < waveSourceInts.size(); i++) {
      if (i > realLine) {
        break;
      }
      if (i < waveSourceInts.size()) {
        int lineHeight = waveSourceInts.get(i);
        canvas.drawRect(offset, mHeight / 2 + middleBlank, offset + soildPixels,
            mHeight / 2 + lineHeight, publicPaint);
        canvas.drawRect(offset, mHeight / 2 - lineHeight, offset + soildPixels,
            mHeight / 2 - middleBlank, publicPaint);
      } else {
        return;
      }
      offset += linePixels;

    }
  }

  private int positionToLineNum(int position) {
    int startNum = publicOffset / (soildPixels + blankPixels);
    int offset = -publicOffset % (soildPixels + blankPixels);
    return (position - offset) / linePixels + startNum;
  }


  private int lineToPosition(int line) {
    int startNum = publicOffset / (soildPixels + blankPixels);
    int offset = -publicOffset % (soildPixels + blankPixels);
    return (line - startNum) * linePixels + offset;
  }

  public void idle(Mode mode) {
    this.mode = mode;
    cutState = CutStates.IDLE;
    if (mode == Mode.PLAY) {
      publicOffset = 0;
      initPlayWave();
      if (mListener != null) {
        mListener.onRefresh(0, rawVolumeList.size());
      }
    } else {
      initPublicOffset();
      calculateCutIndexPosition();
      if (mListener != null) {
        if (rawVolumeList.size() <= screenCapacity) {
          mListener.onRefresh(0, rawVolumeList.size());
        } else {
          mListener.onRefresh(rawVolumeList.size() - screenCapacity, rawVolumeList.size());
        }
      }
    }

    postInvalidate();
  }

  private void initPlayWave() {
    if (rawVolumeList.size() <= screenCapacity) {
//            realWidth = rawVolumeList.size()*linePixels;
      lessWay();
    } else {
      moreWay();
    }
  }


  private void calculateCutIndexPosition() {
    switch (cutState) {
      case IDLE:
        if (rawVolumeList.size() * linePixels > (mWidth - backwardAreaWidth)) {
          cutIndexPosition = mWidth - backwardAreaWidth;
        } else if (rawVolumeList.size() * linePixels < forwardAreaWidth) {
          cutIndexPosition = -1;
        } else {
          cutIndexPosition = rawVolumeList.size() * linePixels;
        }
        break;
      case SLIDE:
      case STOP:
        if (cutIndexPosition > rawVolumeList.size() * linePixels) {
          cutIndexPosition = rawVolumeList.size() * linePixels;
        } else if (cutIndexPosition < forwardAreaWidth) {
          cutIndexPosition = forwardAreaWidth;
        } else if (cutIndexPosition > mWidth - backwardAreaWidth) {
          cutIndexPosition = mWidth - backwardAreaWidth;
        }
        break;
      case FORWARD:
        cutIndexPosition = forwardAreaWidth;
        break;
      case BACKWARD:
        cutIndexPosition = mWidth - backwardAreaWidth;
        break;
    }
  }

  private void drawCutMask(Canvas canvas) {
    if (cutIndexPosition <= 0) {
      return;
    }
    publicPaint.setColor(waveMaskColor);
    int left = cutIndexPosition;
    canvas.drawRect(left, mPaddingTop, mWidth, mHeight - mPaddingBottom, publicPaint);
  }

  private void drawCutIndex(Canvas canvas) {
    if (cutIndexPosition <= 0) {
      return;
    }
    publicPaint.setColor(waveIndexColor);
    int left = cutIndexPosition;
    canvas.drawRect(left - 1, mPaddingTop, left + 1, mHeight - mPaddingBottom, publicPaint);
    publicPaint.setAntiAlias(true);
    canvas.drawCircle(left, topRedPointRadius + 1, topRedPointRadius, publicPaint);
    publicPaint.setAntiAlias(false);
    publicPath.reset();
    publicPath.moveTo(left, mHeight - mPaddingBottom * 3f);
    publicPath.lineTo(left - mPaddingBottom, mHeight - 2 * mPaddingBottom);
    publicPath.lineTo(left - mPaddingBottom, mHeight);
    publicPath.lineTo(left + mPaddingBottom, mHeight);
    publicPath.lineTo(left + mPaddingBottom, mHeight - 2 * mPaddingBottom);
    publicPath.close();
    canvas.drawPath(publicPath, publicPaint);
  }

  private void drawPlayIndex(Canvas canvas) {
    if (playLine < 0) {
      return;
    }
    int startNum = publicOffset / (soildPixels + blankPixels);
    if (playLine < rawVolumeList.size() && playLine >= startNum) {
      publicPaint.setColor(waveIndexColor);
      int offset = -publicOffset % (soildPixels + blankPixels);
      int pos = (playLine - startNum) * linePixels + offset;
      canvas.drawRect(pos - 1, mPaddingTop, pos + 1, mHeight - mPaddingBottom, publicPaint);
      publicPaint.setAntiAlias(true);
      canvas.drawCircle(pos, topRedPointRadius + 1, topRedPointRadius, publicPaint);
      canvas.drawCircle(pos, mHeight - mPaddingBottom + topRedPointRadius, topRedPointRadius,
          publicPaint);
    }

  }

  private void lessWay() {
    waveSourceInts.clear();
    waveSourceInts.addAll(rawVolumeList);
  }


  private void moreWay() {
    waveSourceInts.clear();
    float step = rawVolumeList.size() / (float) screenCapacity;
    int volume = 0;
    float rawCount = step;
    for (int i = 0; i < rawVolumeList.size() - 1; ) {
      int tempVolume = rawVolumeList.get(i);
      if (tempVolume > volume) {
        volume = tempVolume;
      }
      i++;
      if (i > rawCount) {
        rawCount += step;
        waveSourceInts.add(volume);
        volume = 0;
      }
    }
  }


  private void drawPlayWave(Canvas canvas) {
    if (playLine < 0) {
      return;
    }
    publicPaint.setColor(wavePlayColor);
    int startNum = publicOffset / (soildPixels + blankPixels);
    int offset = -publicOffset % (soildPixels + blankPixels);
    for (int i = startNum; i < startNum + screenCapacity + 1; i++) {
      if (i > playLine) {
        break;
      }
      if (i < rawVolumeList.size()) {
        int lineHeight = rawVolumeList.get(i);
        canvas.drawRect(offset, mHeight / 2 - lineHeight, offset + soildPixels,
            mHeight / 2 - middleBlank, publicPaint);
        canvas.drawRect(offset, mHeight / 2 + middleBlank, offset + soildPixels,
            mHeight / 2 + lineHeight, publicPaint);
      } else {
        return;
      }
      offset += linePixels;

    }
  }

  private void drawWave(Canvas canvas) {
    publicPaint.setColor(waveColor);
    int startNum = publicOffset / (soildPixels + blankPixels);
    int offset = -publicOffset % (soildPixels + blankPixels);
    for (int i = startNum; i < startNum + screenCapacity + 1; i++) {
      if (i > playLine) {
        if (i < rawVolumeList.size()) {
          int lineHeight = rawVolumeList.get(i);
          canvas.drawRect(offset, mHeight / 2 - lineHeight, offset + soildPixels,
              mHeight / 2 - middleBlank, publicPaint);
          canvas.drawRect(offset, mHeight / 2 + middleBlank, offset + soildPixels,
              mHeight / 2 + lineHeight, publicPaint);
        } else {
          return;
        }
      }
      offset += linePixels;
    }
  }


  public void addVolume(double volume) {
    double bili = (volume) / (90);
    //让分贝显示高度比较合理
    int lineHeight = (int) (mHeight * bili * bili * bili * bili + middleBlank);
    if (lineHeight < 2) {
      lineHeight = 2;
    }
    rawVolumeList.add(lineHeight / 2);
  }

  public void setPlayLine(int playLine) {
    this.playLine = playLine;
    postInvalidate();
  }

  public void stopPlay() {
    this.playLine = -1;
    postInvalidate();
  }

  public void setListener(WaveViewListener mListener) {
    this.mListener = mListener;
  }

  public void clear() {
    rawVolumeList.clear();
  }

  public void cutWithLine(int cutLine) {
//        realWidth = mWidth - rightoffset;
//        lineNum = realWidth / linePixels;
    int size = rawVolumeList.size();
    if (size > cutLine) {
      rawVolumeList.subList(cutLine, size).clear();
    }
    mode = Mode.PLAY;
    initPlayWave();
    calculateCutIndexPosition();
    mListener.onRefresh(0, rawVolumeList.size());
    postInvalidate();
  }
}
