package com.namibox.imageselector.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class LineColorPicker extends View {

  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;
  int[] DEFAULT = new int[]{Color.parseColor("#b8c847"),
      Color.parseColor("#67bb43"), Color.parseColor("#41b691"),
      Color.parseColor("#4182b6"), Color.parseColor("#4149b6"),
      Color.parseColor("#7641b6"), Color.parseColor("#b741a7"),
      Color.parseColor("#c54657"), Color.parseColor("#d1694a"),
      Color.parseColor("#d1904a"), Color.parseColor("#d1c54a")};

  int[] colors = DEFAULT;

  private Paint paint;
  private RectF rect = new RectF();

  // indicate if nothing selected
  boolean isColorSelected = false;

  private int selectedColor = colors[0];

  public interface OnColorChangedListener {

    void onColorChanged(int c);
  }

  private OnColorChangedListener onColorChanged;

  private int cellSize;

  private int mOrientation = HORIZONTAL;
  private float margin;

  public LineColorPicker(Context context, AttributeSet attrs) {
    super(context, attrs);

    float density = getContext().getResources().getDisplayMetrics().density;
    margin = 8 * density;
    paint = new Paint();
    paint.setStyle(Style.FILL);

//		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
//				R.styleable.LineColorPicker, 0, 0);
//
//		try {
//			mOrientation = a.getInteger(
//					R.styleable.LineColorPicker_orientation, HORIZONTAL);
//		} finally {
//			a.recycle();
//		}
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (mOrientation == HORIZONTAL) {
      drawHorizontalPicker(canvas);
    } else {
      drawVerticalPicker(canvas);
    }

  }

  private void drawVerticalPicker(Canvas canvas) {
    rect.left = 0;
    rect.top = 0;
    rect.right = canvas.getWidth();
    rect.bottom = 0;

    for (int i = 0; i < colors.length; i++) {

      paint.setColor(colors[i]);

      rect.top = rect.bottom;
      rect.bottom += cellSize;

      if (isColorSelected && colors[i] == selectedColor) {
        rect.left = 0;
        rect.right = canvas.getWidth();
      } else {
        rect.left = margin;
        rect.right = canvas.getWidth() - margin;
      }

      canvas.drawRect(rect, paint);
    }

  }

  private void drawHorizontalPicker(Canvas canvas) {
    rect.left = 0;
    rect.top = 0;
    rect.right = 0;
    rect.bottom = canvas.getHeight();

    for (int i = 0; i < colors.length; i++) {

      paint.setColor(colors[i]);

      rect.left = rect.right;
      rect.right += cellSize;

      if (isColorSelected && colors[i] == selectedColor) {
        rect.top = 0;
        rect.bottom = canvas.getHeight();
      } else {
        rect.top = margin;
        rect.bottom = canvas.getHeight() - margin;
      }

      canvas.drawRect(rect, paint);
    }
  }

  private void onColorChanged(int color) {
    if (onColorChanged != null) {
      onColorChanged.onColorChanged(color);
    }
  }

  private boolean isClick = false;
  private int screenW;
  private int screenH;

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    int actionId = event.getAction();

    int newColor;

    switch (actionId) {
      case MotionEvent.ACTION_DOWN:
        isClick = true;
        break;
      case MotionEvent.ACTION_UP:
        newColor = getColorAtXY(event.getX(), event.getY());

        setSelectedColor(newColor);

        if (isClick) {
          performClick();
        }

        break;

      case MotionEvent.ACTION_MOVE:
        newColor = getColorAtXY(event.getX(), event.getY());

        setSelectedColor(newColor);

        break;
      case MotionEvent.ACTION_CANCEL:
        isClick = false;
        break;

      case MotionEvent.ACTION_OUTSIDE:
        isClick = false;
        break;

      default:
        break;
    }

    return true;
  }

  /**
   * Return color at x,y coordinate of view.
   */
  private int getColorAtXY(float x, float y) {

    // FIXME: colors.length == 0 -> devision by ZERO.s

    if (mOrientation == HORIZONTAL) {
      int left = 0;
      int right = 0;

      for (int i = 0; i < colors.length; i++) {
        left = right;
        right += cellSize;

        if (left <= x && right >= x) {
          return colors[i];
        }
      }

    } else {
      int top = 0;
      int bottom = 0;

      for (int i = 0; i < colors.length; i++) {
        top = bottom;
        bottom += cellSize;

        if (y >= top && y <= bottom) {
          return colors[i];
        }
      }
    }

    return selectedColor;
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    // begin boilerplate code that allows parent classes to save state
    Parcelable superState = super.onSaveInstanceState();

    SavedState ss = new SavedState(superState);
    // end

    ss.selectedColor = this.selectedColor;
    ss.isColorSelected = this.isColorSelected;

    return ss;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    // begin boilerplate code so parent classes can restore state
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());
    // end

    this.selectedColor = ss.selectedColor;
    this.isColorSelected = ss.isColorSelected;
  }

  static class SavedState extends BaseSavedState {

    int selectedColor;
    boolean isColorSelected;

    SavedState(Parcelable superState) {
      super(superState);
    }

    private SavedState(Parcel in) {
      super(in);
      this.selectedColor = in.readInt();
      this.isColorSelected = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(this.selectedColor);
      out.writeInt(this.isColorSelected ? 1 : 0);
    }

    // required field that makes Parcelables from a Parcel
    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
      @Override
      public SavedState createFromParcel(Parcel in) {
        return new SavedState(in);
      }

      @Override
      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };
  }

  @Override
  public boolean performClick() {
    return super.performClick();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {

    screenW = w;
    screenH = h;

    recalcCellSize();

    super.onSizeChanged(w, h, oldw, oldh);
  }

  // @Override
  // protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
  // int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
  // int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
  // this.setMeasuredDimension(parentWidth, parentHeight);
  // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  // }

  /**
   * Return currently selected color.
   */
  public int getColor() {
    return selectedColor;
  }

  /**
   * Set selected color as color value from palette.
   */
  public void setSelectedColor(int color) {

    // not from current palette
    if (!containsColor(colors, color)) {
      return;
    }

    // do we need to re-draw view?
    if (!isColorSelected || selectedColor != color) {
      this.selectedColor = color;

      isColorSelected = true;

      invalidate();

      onColorChanged(color);
    }
  }

  /**
   * Set selected color as index from palete
   */
  public void setSelectedColorPosition(int position) {
    setSelectedColor(colors[position]);
  }

  /**
   * Set picker palette
   */
  public void setColors(int[] colors) {
    // TODO: selected color can be NOT in set of colors
    // FIXME: colors can be null
    this.colors = colors;

    if (!containsColor(colors, selectedColor)) {
      selectedColor = colors[0];
    }

    recalcCellSize();

    invalidate();
  }

  private int recalcCellSize() {

    if (mOrientation == HORIZONTAL) {
      cellSize = Math.round(screenW / (colors.length * 1f));
    } else {
      cellSize = Math.round(screenH / (colors.length * 1f));
    }

    return cellSize;
  }

  /**
   * Return current picker palete
   */
  public int[] getColors() {
    return colors;
  }

  /**
   * Return true if palette contains this color
   */
  private boolean containsColor(int[] colors, int c) {
    for (int i = 0; i < colors.length; i++) {
      if (colors[i] == c) {
        return true;
      }

    }

    return false;
  }

  /**
   * Set onColorChanged listener
   */
  public void setOnColorChangedListener(OnColorChangedListener l) {
    this.onColorChanged = l;
  }
}
