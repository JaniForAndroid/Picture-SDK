package com.namibox.commonlib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import com.example.picsdk.R;

public class PageIndicator extends View implements ViewPager.OnPageChangeListener,
    View.OnAttachStateChangeListener {

  // defaults
  private static final int DEFAULT_DOT_RADIUS = 3;                      // dp
  private static final int DEFAULT_GAP = 12;                          // dp
  private static final int DEFAULT_UNSELECTED_COLOUR = Color.GRAY;
  private static final int DEFAULT_SELECTED_COLOUR = Color.BLACK;

  private float gap;
  private int unselectedColour;
  private int selectedColour;
  private float dotRadius;

  // ViewPager
  private ViewPager viewPager;
  private boolean isAttachedToWindow;
  private int pageCount;
  private int currentPage;

  private Paint paint;

  public PageIndicator(Context context) {
    this(context, null, 0);
  }

  public PageIndicator(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PageIndicator(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    final int density = (int) context.getResources().getDisplayMetrics().density;

    // Load attributes
    final TypedArray a = getContext().obtainStyledAttributes(
        attrs, R.styleable.PageIndicator, defStyle, 0);

    dotRadius = a.getDimensionPixelSize(R.styleable.PageIndicator_pi_dotRadius,
        DEFAULT_DOT_RADIUS * density);
    gap = a.getDimensionPixelSize(R.styleable.PageIndicator_pi_dotGap,
        DEFAULT_GAP * density);
    unselectedColour = a.getColor(R.styleable.PageIndicator_pi_normalColor,
        DEFAULT_UNSELECTED_COLOUR);
    selectedColour = a.getColor(R.styleable.PageIndicator_pi_currentColor,
        DEFAULT_SELECTED_COLOUR);

    a.recycle();

    paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    addOnAttachStateChangeListener(this);
  }

  public void setViewPager(ViewPager viewPager) {
    this.viewPager = viewPager;
    viewPager.addOnPageChangeListener(this);
    setPageCount(viewPager.getAdapter().getCount());
    viewPager.getAdapter().registerDataSetObserver(new DataSetObserver() {
      @Override
      public void onChanged() {
        setPageCount(PageIndicator.this.viewPager.getAdapter().getCount());
      }
    });
    invalidate();
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
  }

  @Override
  public void onPageSelected(int position) {
    currentPage = position;
    invalidate();
  }

  @Override
  public void onPageScrollStateChanged(int state) {
    // nothing to do
  }

  private void setPageCount(int pages) {
    pageCount = pages;
    requestLayout();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

    int desiredHeight = getDesiredHeight();
    int height;
    switch (MeasureSpec.getMode(heightMeasureSpec)) {
      case MeasureSpec.EXACTLY:
        height = MeasureSpec.getSize(heightMeasureSpec);
        break;
      case MeasureSpec.AT_MOST:
        height = Math.min(desiredHeight, MeasureSpec.getSize(heightMeasureSpec));
        break;
      default: // MeasureSpec.UNSPECIFIED
        height = desiredHeight;
        break;
    }

    int desiredWidth = getDesiredWidth();
    int width;
    switch (MeasureSpec.getMode(widthMeasureSpec)) {
      case MeasureSpec.EXACTLY:
        width = MeasureSpec.getSize(widthMeasureSpec);
        break;
      case MeasureSpec.AT_MOST:
        width = Math.min(desiredWidth, MeasureSpec.getSize(widthMeasureSpec));
        break;
      default: // MeasureSpec.UNSPECIFIED
        width = desiredWidth;
        break;
    }
    setMeasuredDimension(width, height);
  }

  private int getDesiredHeight() {
    return (int) (getPaddingTop() + dotRadius * 2 + getPaddingBottom());
  }

  private int getRequiredWidth() {
    return (int) (pageCount * dotRadius * 2 + (pageCount - 1) * gap);
  }

  private int getDesiredWidth() {
    return getPaddingLeft() + getRequiredWidth() + getPaddingRight();
  }

  @Override
  public void onViewAttachedToWindow(View view) {
    isAttachedToWindow = true;
  }

  @Override
  public void onViewDetachedFromWindow(View view) {
    isAttachedToWindow = false;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (viewPager == null || pageCount == 0) {
      return;
    }
    for (int i = 0; i < pageCount; i++) {
      float cx = getPaddingLeft() + dotRadius + i * (dotRadius * 2 + gap);
      float cy = getHeight() / 2;
      float radius = i == currentPage ? dotRadius : dotRadius * 0.8f;
      paint.setColor(i == currentPage ? selectedColour : unselectedColour);
      canvas.drawCircle(cx, cy, radius, paint);
    }
  }

}