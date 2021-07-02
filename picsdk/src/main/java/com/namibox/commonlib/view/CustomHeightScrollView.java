package com.namibox.commonlib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.ScrollView;
import com.example.picsdk.R;

/**
 * 自定义高度ScrollView
 * 可以通过高度系数（0-1）指定ScrollView的最大高度
 * 最大高度等于屏幕高度 最小高度等于屏幕高度的三分之一 太小了没意义
 * 当ScrollView子view 超过设定最大高度的时候可滚动，小于设定高度 ScrollView高度自适应
 * @author zhangkx
 * @Date 2019/9/30 14:26
 */
public class CustomHeightScrollView extends ScrollView {

  private Context context;
  private int maxHeight;
  private float heightPercent;


  public CustomHeightScrollView(Context context) {
    this(context,null);
  }

  public CustomHeightScrollView(Context context, AttributeSet attrs) {
    this(context, attrs,0);
  }

  public CustomHeightScrollView(Context context, AttributeSet attrs,
      int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.context = context;
    try {
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomHeightScrollView);
      heightPercent = a.getFloat(R.styleable.CustomHeightScrollView_height_percent, 0);
      if (heightPercent <= 0) {
        heightPercent = 0.33f;
      }else if(heightPercent >= 1){
        heightPercent = 1;
      }
      a.recycle();
    }catch (Exception e){
      e.printStackTrace();
    }

    maxHeight = (int) (getScreenHeight(context) * heightPercent);
  }

  public void setHeightPercent(float heightPercent){
    this.heightPercent = heightPercent;
    postInvalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getMaxHeight(), MeasureSpec.AT_MOST));
  }
  private int getScreenHeight(Context context) {
    WindowManager wm = (WindowManager) context
        .getSystemService(Context.WINDOW_SERVICE);
    return wm.getDefaultDisplay().getHeight();
  }

  private int getMaxHeight(){
    return (int) (getScreenHeight(context) * heightPercent);
  }
}
