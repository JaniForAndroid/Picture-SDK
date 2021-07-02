package com.namibox.hfx.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sunha on 2016/12/19 0019.
 */

public class AlignTopSnaphelper extends LinearSnapHelper {

  private int[] out = new int[2];
  private static final String TAG = "AlignTopSnaphelper";

  @Override
  public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
      @NonNull View targetView) {
    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) targetView
        .getLayoutParams();
    int height = layoutManager.getDecoratedMeasuredHeight(targetView) + params.bottomMargin
        + params.topMargin;
    int childTop = layoutManager.getDecoratedTop(targetView) - params.topMargin;
    int parentPaddingTop = layoutManager.getPaddingTop();
//        Log.i(TAG, "calculateDistanceToFinalSnap: height:" + height + "   childTop:" + childTop + "   parentPaddingTop:" + parentPaddingTop);
//        if (Math.abs(parentPaddingTop - childTop) < height / 2) {
//            out[1] = childTop - parentPaddingTop;
//            Log.i(TAG, "calculateDistanceToFinalSnap1: " + out[1]);
//        } else if (Math.abs(parentPaddingTop - childTop) >= height / 2) {
//            out[1] = childTop - height - parentPaddingTop;
//            Log.i(TAG, "calculateDistanceToFinalSnap2: " + out[1]);
//        }

    if (childTop >= parentPaddingTop) {
      out[1] = childTop - parentPaddingTop;
    } else if (parentPaddingTop - childTop > height / 2) {
      out[1] = childTop + height - parentPaddingTop;
    } else {
      out[1] = childTop - parentPaddingTop;
    }
    return out;
  }

  @Override
  public View findSnapView(RecyclerView.LayoutManager layoutManager) {
    if (layoutManager instanceof LinearLayoutManager) {
      LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
      int firstVizPosition = linearLayoutManager.findFirstVisibleItemPosition();
      if (firstVizPosition >= 0) {
        return layoutManager.findViewByPosition(firstVizPosition);
      }
    }
    return null;
  }
}
