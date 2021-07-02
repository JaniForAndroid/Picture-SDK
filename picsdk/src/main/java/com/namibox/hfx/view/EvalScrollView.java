package com.namibox.hfx.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.ScrollView;

/**
 * @author: Shelter
 * Create time: 2018/6/28, 15:58.
 */
public class EvalScrollView extends ScrollView {

  private Context mContext;

  public EvalScrollView(Context context) {
    super(context);
    init(context);
  }

  public EvalScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public EvalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    mContext = context;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    try {
      //最大高度显示为屏幕内容高度的一半
      Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
      DisplayMetrics d = new DisplayMetrics();
      display.getMetrics(d);
      //此处是关键，设置控件宽度不能超过屏幕宽度2/3（d.widthPixels * 2 / 3）（在此替换成自己需要的宽度）
      widthMeasureSpec = MeasureSpec.makeMeasureSpec(d.widthPixels * 4 / 5, MeasureSpec.AT_MOST);

    } catch (Exception e) {
      e.printStackTrace();
    }
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
}
