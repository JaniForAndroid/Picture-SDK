package com.namibox.hfx.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by sunha on 2016/12/19 0019.
 */

public class NoFlingRecyclerView extends RecyclerView {

  public NoFlingRecyclerView(Context context) {
    super(context);
  }

  public NoFlingRecyclerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public NoFlingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }


  @Override
  public boolean fling(int velocityX, int velocityY) {
    return false;
  }
}
