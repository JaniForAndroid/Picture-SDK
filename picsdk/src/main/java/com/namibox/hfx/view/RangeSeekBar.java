package com.namibox.hfx.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import com.example.picsdk.R;
import com.namibox.util.ImageUtil;
import java.math.BigDecimal;


/**
 * Widget that lets users select a minimum and maximum value on a given
 * numerical range. The range value types can be one of Long, Double, Integer,
 * Float, Short, Byte or BigDecimal.<br />
 * <br />
 * Improved {@link MotionEvent} handling for smoother use, anti-aliased painting
 * for improved aesthetics.
 *
 * @param <T> The Number type of the range values. One of Long, Double, Integer, Float, Short, Byte
 * or BigDecimal.
 * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
 * @author Peter Sinnott (psinnott@gmail.com)
 * @author Thomas Barrasso (tbarrasso@sevenplusandroid.org)
 */
public class RangeSeekBar<T extends Number> extends android.support.v7.widget.AppCompatImageView {

  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final TextPaint textPaint = new TextPaint(
      Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
  private final TextPaint strokePaint = new TextPaint(
      Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
  //    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  // private final Bitmap thumbLeftImage = BitmapFactory.decodeResource(
  // getResources(), R.drawable.seek_thumb_normal);
  private final Bitmap thumbLeftImage;
  private final Bitmap thumbRightImage;
  // private final Bitmap thumbPressedImage = BitmapFactory.decodeResource(
  // getResources(), R.drawable.seek_thumb_pressed);
  private final Bitmap thumbPressedImage;
  private final float thumbWidth;
  private final float thumbHalfWidth;
  private final float thumbHalfHeight;
  private final float lineHeight;
  private final float padding;
  private final float thumbHeight;
  private final T absoluteMinValue, absoluteMaxValue, absoluteMinRange;
  private final NumberType numberType;
  private final double absoluteMinValuePrim, absoluteMaxValuePrim, absoluteMinRangePrim;
  private final double minRangeNormalized;
  private double normalizedMinValue = 0d;
  private double normalizedMaxValue = 1d;
  private Thumb pressedThumb = null;
  private boolean notifyWhileDragging = true;
  public final boolean IS_MULTI_COLORED;
  public final int SINGLE_COLOR;
  public final int LEFT_COLOR;
  public final int MIDDLE_COLOR;
  public final int RIGHT_COLOR;
  public static final int BACKGROUND_COLOR = Color.GRAY;
  private OnRangeSeekBarChangeListener<T> listener;
  private double normalizedPlayPosition = 0d;
  private boolean isPlaying = false;
  private float deviation;
  private boolean inRange = false;


  private void initTextPainter() {
    textPaint.setTextAlign(Paint.Align.CENTER);
    textPaint.setColor(0xffffffff);
    textPaint.setTextSize(49);
    textPaint.bgColor = 0xff000000;
    textPaint.setStyle(Style.FILL);
    textPaint.setStrokeWidth(1);//设置画笔宽度

    strokePaint.setTextAlign(Paint.Align.CENTER);
    strokePaint.setColor(0xff35a4e6);
    strokePaint.setTextSize(50);
    strokePaint.bgColor = 0xff000000;
    strokePaint.setStyle(Style.STROKE);
    strokePaint.setStrokeWidth(1);//设置画笔宽度

  }

  /**
   * Default color of a {@link RangeSeekBar}, #FF33B5E5. This is also known as
   * "Ice Cream Sandwich" blue.
   */
  public static final int DEFAULT_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5);

  /**
   * Callback listener interface to notify about changed range values.
   *
   * @param <T> The Number type the RangeSeekBar has been declared with.
   * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
   */
  public interface OnRangeSeekBarChangeListener<T> {

    //        public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,
//                                                T minValue, T maxValue,boolean isMin);
//        public void onRangeSeekBarValuesFinished(RangeSeekBar<?> bar,
//                                                T minValue, T maxValue);
    void onMinChangeFinished(RangeSeekBar<?> bar,
        T minValue);

    void onMaxChangeFinished(RangeSeekBar<?> bar,
        T maxValue);

    void onMinChanged(RangeSeekBar<?> bar,
        T minValue);

    void onMaxChanged(RangeSeekBar<?> bar,
        T maxValue);

    void onRangeTouch(RangeSeekBar<?> bar,
        T rangeValue);
  }

  /**
   * Registers given listener callback to notify about changed selected
   * values.
   *
   * @param listener The listener to notify about changed selected values.
   */
  public void setOnRangeSeekBarChangeListener(
      OnRangeSeekBarChangeListener<T> listener) {
    this.listener = listener;
  }

  /**
   * An invalid pointer id.
   */
  public static final int INVALID_POINTER_ID = 255;

  // Localized constants from MotionEvent for compatibility
  // with API < 8 "Froyo".
  public static final int ACTION_POINTER_UP = 0x6,
      ACTION_POINTER_INDEX_MASK = 0x0000ff00,
      ACTION_POINTER_INDEX_SHIFT = 8;

  private float mDownMotionX;
  private float mDownMotionY;
  private int mActivePointerId = INVALID_POINTER_ID;

  /**
   * On touch, this offset plus the scaled value from the position of the
   * touch will form the progress value. Usually 0.
   */
  float mTouchProgressOffset;

  private int mScaledTouchSlop;
  private boolean mIsDragging;


  /**
   * Creates a new RangeSeekBar.
   *
   * @param absoluteMinValue The minimum value of the selectable range.
   * @param absoluteMaxValue The maximum value of the selectable range.
   * @throws IllegalArgumentException Will be thrown if min/max value type is not one of Long,
   * Double, Integer, Float, Short, Byte or BigDecimal.
   */
  public RangeSeekBar(T absoluteMinValue, T absoluteMaxValue, T absoluteMinRange,
      Context context, int paddingPx, int heightPx)
      throws IllegalArgumentException {
    super(context);
    this.absoluteMinValue = absoluteMinValue;
    this.absoluteMaxValue = absoluteMaxValue;
    this.absoluteMinRange = absoluteMinRange;
    absoluteMinValuePrim = absoluteMinValue.doubleValue();
    absoluteMaxValuePrim = absoluteMaxValue.doubleValue();
    absoluteMinRangePrim = absoluteMinRange.doubleValue();
    minRangeNormalized = valueToNormalized(absoluteMinRange);

    numberType = NumberType.fromNumber(absoluteMinValue);

    // Added so we can draw right colors
    IS_MULTI_COLORED = true;
    SINGLE_COLOR = 0;
    LEFT_COLOR = Color.argb(0x99, 0x44,
        0x44, 0x44);
    MIDDLE_COLOR = Color.argb(0xff, 0x27,
        0x41, 0x47);
    RIGHT_COLOR = Color.argb(0x99, 0x44,
        0x44, 0x44);
//        LEFT_COLOR = leftColor > 0 ? leftColor : Color.argb(0x44, 0x44,
//                0x44, 0x44);
//        MIDDLE_COLOR = middleColor > 0 ? middleColor : Color.argb(0xFF, 0x00,
//                0xFF, 0x00);
//        RIGHT_COLOR = rightColor > 0 ? rightColor : Color.argb(0x44, 0x44,
//                0x44, 0x44);

//        LEFT_COLOR = leftColor;
//        MIDDLE_COLOR = middleColor;
//        RIGHT_COLOR = rightColor;
    Bitmap tempImg1 = ImageUtil
        .decodeSampledBitmapFromResource(getResources(), R.drawable.hfx_ic_drag_blue, 10000, heightPx);
    Bitmap tempImg3 = ImageUtil
        .decodeSampledBitmapFromResource(getResources(), R.drawable.hfx_ic_drag_red, 10000, heightPx);
    Bitmap tempImg2 = ImageUtil
        .decodeSampledBitmapFromResource(getResources(), R.drawable.hfx_ic_drag_yellow, 10000,
            heightPx);
    double height = tempImg1.getHeight();
    double width = tempImg1.getWidth();
    double widthPx = heightPx * (width / height);

    thumbLeftImage = ImageUtil.zoomBitmap(tempImg1, (int) widthPx, heightPx, true);
    thumbRightImage = ImageUtil.zoomBitmap(tempImg3, (int) widthPx, heightPx, true);
    thumbPressedImage = ImageUtil.zoomBitmap(tempImg2, (int) widthPx, heightPx, true);
    thumbWidth = thumbLeftImage.getWidth();
    thumbHalfWidth = 0.5f * thumbWidth;
    thumbHalfHeight = 0.5f * thumbLeftImage.getHeight();
    lineHeight = 0.3f * thumbHalfHeight;
    padding = paddingPx;
    thumbHeight = thumbLeftImage.getHeight();
    initTextPainter();

    // make RangeSeekBar focusable. This solves focus handling issues in
    // case EditText widgets are being used along with the RangeSeekBar
    // within ScollViews.
    setFocusable(true);
    setFocusableInTouchMode(true);
    init();
  }


  private final void init() {
    mScaledTouchSlop = ViewConfiguration.get(getContext())
        .getScaledTouchSlop();
  }

  public boolean isNotifyWhileDragging() {
    return notifyWhileDragging;
  }

  /**
   * Should the widget notify the listener callback while the user is still
   * dragging a thumb? Default is false.
   */
  public void setNotifyWhileDragging(boolean flag) {
    this.notifyWhileDragging = flag;
  }

  /**
   * Returns the absolute minimum value of the range that has been set at
   * construction time.
   *
   * @return The absolute minimum value of the range.
   */
  public T getAbsoluteMinValue() {
    return absoluteMinValue;
  }

  /**
   * Returns the absolute maximum value of the range that has been set at
   * construction time.
   *
   * @return The absolute maximum value of the range.
   */
  public T getAbsoluteMaxValue() {
    return absoluteMaxValue;
  }

  /**
   * Returns the currently selected min value.
   *
   * @return The currently selected min value.
   */
  public T getSelectedMinValue() {
    return normalizedToValue(normalizedMinValue);
  }

  /**
   * Sets the currently selected minimum value. The widget will be invalidated
   * and redrawn.
   *
   * @param value The Number value to set the minimum value to. Will be clamped to given absolute
   * minimum/maximum range.
   */
  public void setSelectedMinValue(T value) {
    // in case absoluteMinValue == absoluteMaxValue, avoid division by zero
    // when normalizing.

    if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
      setNormalizedMinValue(0d);
    } else {
      double min = value.doubleValue();
      double max = getSelectedMaxValue().doubleValue();
      if (max - min > absoluteMinRangePrim) {
        setNormalizedMinValue(valueToNormalized(value));
      } else {
        setNormalizedMinValue(doubleToNormalized(max - absoluteMinRangePrim));
      }

    }
    if (listener != null) {
      listener.onMinChanged(this,
          getSelectedMinValue());
    }

  }

  public void setPlayPositionValue(T value) {
    if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
      setNormalizedPlayPosition(0d);
    } else {
      setNormalizedPlayPosition(valueToNormalized(value));
    }
  }

  private void setNormalizedPlayPosition(double value) {

    normalizedPlayPosition = Math.max(0d,
        Math.min(1d, Math.min(value, normalizedMaxValue)));

    invalidate();

  }

  public void setPlaying(boolean playing) {
    isPlaying = playing;
    invalidate();
  }

  /**
   * Returns the currently selected max value.
   *
   * @return The currently selected max value.
   */
  public T getSelectedMaxValue() {
    return normalizedToValue(normalizedMaxValue);
  }

  /**
   * Sets the currently selected maximum value. The widget will be invalidated
   * and redrawn.
   *
   * @param value The Number value to set the maximum value to. Will be clamped to given absolute
   * minimum/maximum range.
   */
  public void setSelectedMaxValue(T value) {
    // in case absoluteMinValue == absoluteMaxValue, avoid division by zero
    // when normalizing.

    if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
      setNormalizedMaxValue(1d);
    } else {
      double min = getSelectedMinValue().doubleValue();
      double max = value.doubleValue();
      if (max - min > absoluteMinRangePrim) {
        setNormalizedMaxValue(valueToNormalized(value));
      } else {
        setNormalizedMaxValue(doubleToNormalized(min + absoluteMinRangePrim));
      }

    }
    if (listener != null) {
      listener.onMaxChanged(this,
          getSelectedMaxValue());
    }

  }

  public void setPressedThumb(Thumb thumb) {
    pressedThumb = thumb;
  }

  public void resetPressedThumb() {
    pressedThumb = null;
    invalidate();

  }

  /**
   * Handles thumb selection and movement. Notifies listener callback on
   * certain events.
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {

    if (!isEnabled()) {
      return false;
    }

    int pointerIndex;

    final int action = event.getAction();
    switch (action & MotionEvent.ACTION_MASK) {

      case MotionEvent.ACTION_DOWN:
        // Remember where the motion event started
        mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
        pointerIndex = event.findPointerIndex(mActivePointerId);
        mDownMotionX = event.getX(pointerIndex);
        mDownMotionY = event.getY(pointerIndex);

        boolean minThumbPressed = isInThumbRange(mDownMotionX, mDownMotionY, normalizedMinValue,
            true);
        boolean maxThumbPressed = isInThumbRange(mDownMotionX, mDownMotionY, normalizedMaxValue,
            false);
        if (!minThumbPressed && !maxThumbPressed) {
          if (isInBlankRange(mDownMotionX)) {
            inRange = true;
            if (listener != null) {
              double rangeNormalized = screenToNormalized(mDownMotionX);
              listener.onRangeTouch(this, normalizedToValue(rangeNormalized));
            }
          }
          return true;
        }

        pressedThumb = evalPressedThumb(mDownMotionX, mDownMotionY);

        // Only handle thumb presses.
        if (pressedThumb == null) {
          return super.onTouchEvent(event);
        }
        if (Thumb.MIN.equals(pressedThumb)) {
          float mPositon = normalizedToScreen(normalizedMinValue);
          deviation = mPositon - mDownMotionX;
        } else {
          float mPositon = normalizedToScreen(normalizedMaxValue);
          deviation = mPositon - mDownMotionX;
        }

        setPressed(true);
//                setPlaying(false);
        invalidate();
        onStartTrackingTouch();
        trackTouchEvent(event);
        attemptClaimDrag();

        break;
      case MotionEvent.ACTION_MOVE:
        if (inRange) {
          return true;
        }
        if (pressedThumb != null) {

          if (mIsDragging) {
            trackTouchEvent(event);
          } else {
            // Scroll to follow the motion event
            pointerIndex = event.findPointerIndex(mActivePointerId);
            final float x = event.getX(pointerIndex);

            if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
              setPressed(true);
              invalidate();
              onStartTrackingTouch();
              trackTouchEvent(event);
              attemptClaimDrag();
            }
          }

          if (notifyWhileDragging && listener != null) {
            if (Thumb.MIN.equals(pressedThumb)) {
              listener.onMinChanged(this,
                  getSelectedMinValue());
            } else {
              listener.onMaxChanged(this,
                  getSelectedMaxValue());
            }
          }
        }
        break;
      case MotionEvent.ACTION_UP:
        if (mIsDragging) {
          trackTouchEvent(event);
          onStopTrackingTouch();
          setPressed(false);
        } else {
          // Touch up when we never crossed the touch slop threshold
          // should be interpreted as a tap-seek to that location.
          onStartTrackingTouch();
          trackTouchEvent(event);
          onStopTrackingTouch();
        }
        if (!inRange) {
          if (listener != null) {
            if (Thumb.MIN.equals(pressedThumb)) {
              listener.onMinChangeFinished(this, getSelectedMinValue());
            } else {
              listener.onMaxChangeFinished(this, getSelectedMaxValue());
            }
          }

        }
        inRange = false;
        pressedThumb = null;
        invalidate();
        break;
      case MotionEvent.ACTION_POINTER_DOWN: {
        final int index = event.getPointerCount() - 1;
        // final int index = ev.getActionIndex();
        mDownMotionX = event.getX(index);
        mActivePointerId = event.getPointerId(index);
        invalidate();
        break;
      }
      case MotionEvent.ACTION_POINTER_UP:
        onSecondaryPointerUp(event);
        invalidate();
        break;
      case MotionEvent.ACTION_CANCEL:
        if (mIsDragging) {
          onStopTrackingTouch();
          setPressed(false);
        }
        inRange = false;
        invalidate(); // see above explanation
        break;
    }
    return true;
  }

  private final void onSecondaryPointerUp(MotionEvent ev) {
    final int pointerIndex =
        (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;

    final int pointerId = ev.getPointerId(pointerIndex);
    if (pointerId == mActivePointerId) {
      // This was our active pointer going up. Choose
      // a new active pointer and adjust accordingly.
      // TODO: Make this decision more intelligent.
      final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
      mDownMotionX = ev.getX(newPointerIndex);
      mActivePointerId = ev.getPointerId(newPointerIndex);
    }
  }


  private final void trackTouchEvent(MotionEvent event) {
    final int pointerIndex = event.findPointerIndex(mActivePointerId);
    final float x = event.getX(pointerIndex);
    double normalized = screenToNormalized(x + deviation);

    if (Thumb.MIN.equals(pressedThumb)) {
      if (normalizedMaxValue - normalized > minRangeNormalized) {
        setNormalizedMinValue(normalized);
      } else {
        setNormalizedMinValue(normalizedMaxValue - minRangeNormalized);
      }

    } else if (Thumb.MAX.equals(pressedThumb)) {
      if (normalized - normalizedMinValue > minRangeNormalized) {
        setNormalizedMaxValue(normalized);
      } else {
        setNormalizedMaxValue(normalizedMinValue + minRangeNormalized);
      }
    }
  }

  /**
   * Tries to claim the user's drag motion, and requests disallowing any
   * ancestors from stealing events in the drag.
   */
  private void attemptClaimDrag() {
    if (getParent() != null) {
      getParent().requestDisallowInterceptTouchEvent(true);
    }
  }

  /**
   * This is called when the user has started touching this widget.
   */
  void onStartTrackingTouch() {
    mIsDragging = true;
  }

  /**
   * This is called when the user either releases his touch or the touch is
   * canceled.
   */
  void onStopTrackingTouch() {
    mIsDragging = false;
  }

  /**
   * Ensures correct size of the widget.
   */
  @Override
  protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = 200;
    if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
      width = MeasureSpec.getSize(widthMeasureSpec);
    }
//        int height = thumbLeftImage.getHeight() + thumbHeight;
    int height = thumbLeftImage.getHeight();
    if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
      height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
    }
    setMeasuredDimension(width, height);
  }

  /**
   * Draws the widget on the given canvas.
   */
  @Override
  protected synchronized void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    paint.setStyle(Style.FILL);
    paint.setAntiAlias(true);

    final RectF rectR = new RectF(0, 0, normalizedToScreen(normalizedMinValue), thumbHeight);
    paint.setColor(LEFT_COLOR);
    canvas.drawRect(rectR, paint);

    final RectF rectM = new RectF(normalizedToScreen(normalizedMinValue), 0,
        normalizedToScreen(normalizedMaxValue), thumbHeight);
    paint.setColor(MIDDLE_COLOR);
    canvas.drawRect(rectM, paint);

    final RectF rectG = new RectF(normalizedToScreen(normalizedMaxValue), 0, getWidth(),
        thumbHeight);
    paint.setColor(RIGHT_COLOR);
    canvas.drawRect(rectG, paint);

    //标记线
//            final RectF rectMax = new RectF(normalizedToScreen(normalizedMaxValue) - 1, 0, normalizedToScreen(normalizedMaxValue) + 1, thumbHeight);
//            paint.setColor(0xff35a4e6);
//            canvas.drawRect(rectMax, paint);

    // draw minimum thumb
    drawThumb(normalizedToScreen(normalizedMinValue),
        Thumb.MIN.equals(pressedThumb), canvas, true);

    // draw maximum thumb
    drawThumb(normalizedToScreen(normalizedMaxValue),
        Thumb.MAX.equals(pressedThumb), canvas, false);
    if (isPlaying) {
      final RectF rectPlay = new RectF(normalizedToScreen(normalizedPlayPosition) - 3, 0,
          normalizedToScreen(normalizedPlayPosition) + 3, thumbHeight);
      paint.setColor(0xffff5722);
      canvas.drawRect(rectPlay, paint);
    }
  }

  /**
   * Overridden to save instance state when device orientation changes. This
   * method is called automatically if you assign an id to the RangeSeekBar
   * widget using the {@link #setId(int)} method. Other members of this class
   * than the normalized min and max values don't need to be saved.
   */
  @Override
  protected Parcelable onSaveInstanceState() {
    final Bundle bundle = new Bundle();
    bundle.putParcelable("SUPER", super.onSaveInstanceState());
    bundle.putDouble("MIN", normalizedMinValue);
    bundle.putDouble("MAX", normalizedMaxValue);
    return bundle;
  }

  /**
   * Overridden to restore instance state when device orientation changes.
   * This method is called automatically if you assign an id to the
   * RangeSeekBar widget using the {@link #setId(int)} method.
   */
  @Override
  protected void onRestoreInstanceState(Parcelable parcel) {
    final Bundle bundle = (Bundle) parcel;
    super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
    normalizedMinValue = bundle.getDouble("MIN");
    normalizedMaxValue = bundle.getDouble("MAX");
  }

  /**
   * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
   *
   * @param screenCoord The x-coordinate in screen space where to draw the image.
   * @param pressed Is the thumb currently in "pressed" state?
   * @param canvas The canvas to draw upon.
   */
  private void drawThumb(float screenCoord, boolean pressed, Canvas canvas, boolean isMin) {
    paint.setColor(0xff000000);
    if (isMin) {
      canvas.drawBitmap(pressed ? thumbPressedImage : thumbLeftImage,
          screenCoord - thumbWidth, 0, paint);
    } else {
      canvas.drawBitmap(pressed ? thumbPressedImage : thumbRightImage,
          screenCoord, 0, paint);
    }

    //画提示文字
//        if (pressed) {
//            Paint.FontMetrics fm = textPaint.getFontMetrics();// 得到系统默认字体属性
//            int mFontHeight = (int) (Math.ceil(fm.descent - fm.top) + 2);// 获得字体高度
//            canvas.drawText(isMin ? "起点" : "终点", screenCoord, -20 - mFontHeight, textPaint);
//            canvas.drawText("00:00:00.00", screenCoord, -20, textPaint);
//            canvas.drawText(isMin ? "起点" : "终点", screenCoord, -20 - mFontHeight, strokePaint);
//            canvas.drawText("00:00:00.00", screenCoord, -20, strokePaint);
//        }

  }


  /**
   * Decides which (if any) thumb is touched by the given x-coordinate.
   *
   * @param touchX The x-coordinate of a touch event in screen space.
   * @return The pressed thumb or null if none has been touched.
   */
  private Thumb evalPressedThumb(float touchX, float touchY) {
    Thumb result = null;
    boolean minThumbPressed = isInThumbRange(touchX, touchY, normalizedMinValue, true);
    boolean maxThumbPressed = isInThumbRange(touchX, touchY, normalizedMaxValue, false);

    if (minThumbPressed && maxThumbPressed) {
      // if both thumbs are pressed (they lie on top of each other),
      // choose the one with more room to drag. this avoids "stalling" the
      // thumbs in a corner, not being able to drag them apart anymore.
      double averageValue = (normalizedMaxValue + normalizedMinValue) * 0.5d;
      result = (touchX < normalizedToScreen(averageValue)) ? Thumb.MIN : Thumb.MAX;
    } else if (minThumbPressed) {
      result = Thumb.MIN;
    } else if (maxThumbPressed) {
      result = Thumb.MAX;
    }
    return result;
  }

  /**
   * Decides if given x-coordinate in screen space needs to be interpreted as
   * "within" the normalized thumb x-coordinate.
   *
   * @param touchX The x-coordinate in screen space to check.
   * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
   * @return true if x-coordinate is in thumb range, false otherwise.
   */
//    private boolean isInThumbRange(float touchX, float touchY, double normalizedThumbValue) {
//
//        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth + 50;
//    }
  private boolean isInThumbRange(float touchX, float touchY, double normalizedThumbValue,
      boolean isMin) {
    if (isMin) {
      return touchX < normalizedToScreen(normalizedThumbValue);
    } else {
      return touchX > normalizedToScreen(normalizedThumbValue);
    }

  }


  private boolean isInBlankRange(float touchX) {

    return touchX > normalizedToScreen(normalizedMinValue) && touchX < normalizedToScreen(
        normalizedMaxValue);
  }


  /**
   * Sets normalized min value to value so that 0 <= value <= normalized max
   * value <= 1. The View will get invalidated when calling this method.
   *
   * @param value The new normalized min value to set.
   */
  public void setNormalizedMinValue(double value) {

    normalizedMinValue = Math.max(0d,
        Math.min(1d, Math.min(value, normalizedMaxValue)));

    invalidate();

  }

  /**
   * Sets normalized max value to value so that 0 <= normalized min value <=
   * value <= 1. The View will get invalidated when calling this method.
   *
   * @param value The new normalized max value to set.
   */
  public void setNormalizedMaxValue(double value) {

    normalizedMaxValue = Math.max(0d,
        Math.min(1d, Math.max(value, normalizedMinValue)));
    invalidate();

  }

  /**
   * Converts a normalized value to a Number object in the value space between
   * absolute minimum and maximum.
   */
  @SuppressWarnings("unchecked")
  private T normalizedToValue(double normalized) {
    return (T) numberType.toNumber(absoluteMinValuePrim + normalized
        * (absoluteMaxValuePrim - absoluteMinValuePrim));
  }

  /**
   * Converts the given Number value to a normalized double.
   *
   * @param value The Number value to normalize.
   * @return The normalized double.
   */
  private double valueToNormalized(T value) {
    if (0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
      // prevent division by zero, simply return 0.
      return 0d;
    }
    return (value.doubleValue() - absoluteMinValuePrim)
        / (absoluteMaxValuePrim - absoluteMinValuePrim);
  }

  private double doubleToNormalized(double value) {
    if (0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
      // prevent division by zero, simply return 0.
      return 0d;
    }
    return (value - absoluteMinValuePrim)
        / (absoluteMaxValuePrim - absoluteMinValuePrim);
  }

  /**
   * Converts a normalized value into screen space.
   *
   * @param normalizedCoord The normalized value to convert.
   * @return The converted value in screen space.
   */
  private float normalizedToScreen(double normalizedCoord) {
    return (float) (padding + thumbWidth + normalizedCoord * (getWidth() - 2 * padding
        - 2 * thumbWidth));
  }

  /**
   * Converts screen space x-coordinates into normalized values.
   *
   * @param screenCoord The x-coordinate in screen space to convert.
   * @return The normalized value.
   */
  private double screenToNormalized(float screenCoord) {
    int width = getWidth();
    if (width <= (2 * padding + 2 * thumbWidth)) {
      // prevent division by zero, simply return 0.
      return 0d;
    } else {
      double result = (screenCoord - padding - thumbWidth) / (width - 2 * padding - 2 * thumbWidth);
      return Math.min(1d, Math.max(0d, result));
    }
  }

  /**
   * Thumb constants (min and max).
   */
  public static enum Thumb {
    MIN, MAX
  }


  /**
   * Utility enumaration used to convert between Numbers and doubles.
   *
   * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
   */
  private static enum NumberType {
    LONG, DOUBLE, INTEGER, FLOAT, SHORT, BYTE, BIG_DECIMAL;

    public static <E extends Number> NumberType fromNumber(E value)
        throws IllegalArgumentException {
      if (value instanceof Long) {
        return LONG;
      }
      if (value instanceof Double) {
        return DOUBLE;
      }
      if (value instanceof Integer) {
        return INTEGER;
      }
      if (value instanceof Float) {
        return FLOAT;
      }
      if (value instanceof Short) {
        return SHORT;
      }
      if (value instanceof Byte) {
        return BYTE;
      }
      if (value instanceof BigDecimal) {
        return BIG_DECIMAL;
      }
      throw new IllegalArgumentException("Number class '"
          + value.getClass().getName() + "' is not supported");
    }

    public Number toNumber(double value) {
      switch (this) {
        case LONG:
          return new Long((long) value);
        case DOUBLE:
          return value;
        case INTEGER:
          return new Integer((int) value);
        case FLOAT:
          return new Float(value);
        case SHORT:
          return new Short((short) value);
        case BYTE:
          return new Byte((byte) value);
        case BIG_DECIMAL:
          return new BigDecimal(value);
      }
      throw new InstantiationError("can't convert " + this
          + " to a Number object");
    }
  }


}