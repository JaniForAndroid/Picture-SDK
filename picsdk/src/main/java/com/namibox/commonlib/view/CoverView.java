package com.namibox.commonlib.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 透明的遮罩层 用来屏幕屏幕的点击事件
 * @author zhangkx
 * @Date 2019/2/20 9:44
 */
public class CoverView extends View {

  public CoverView(Context context) {
    super(context,null,0);
  }

  public CoverView(Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs,0);
  }

  public CoverView(Context context, @Nullable AttributeSet attrs,
      int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return true;
  }
}
