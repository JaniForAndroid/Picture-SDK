package com.namibox.tools;

import android.content.Context;
import android.support.v7.widget.LinearSmoothScroller;

/**
 * author : feng
 * description ： recyclerView平滑滚动到指定位置，item aline recyclerView top
 * creation time : 19-11-10下午4:46
 */
public class TopSmoothScroller extends LinearSmoothScroller {

  private int offset;

  public TopSmoothScroller(Context context) {
    super(context);
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  @Override
  protected int getVerticalSnapPreference() {
    return SNAP_TO_START;
  }

  @Override
  public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd,
      int snapPreference) {
    switch (snapPreference) {
      case SNAP_TO_START:
        return boxStart - viewStart - offset;
      case SNAP_TO_END:
        return boxEnd - viewEnd;
      case SNAP_TO_ANY:
        final int dtStart = boxStart - viewStart;
        if (dtStart > 0) {
          return dtStart;
        }
        final int dtEnd = boxEnd - viewEnd;
        if (dtEnd < 0) {
          return dtEnd;
        }
        break;
      default:
        throw new IllegalArgumentException("snap preference should be one of the"
            + " constants defined in SmoothScroller, starting with SNAP_");
    }
    return 0;
  }
}
