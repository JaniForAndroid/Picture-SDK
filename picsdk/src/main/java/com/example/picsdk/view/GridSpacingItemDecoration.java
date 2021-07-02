package com.example.picsdk.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Create time: 2018/6/21.
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
  private int spanCount;
  private int spacing1, spacing2;
  private boolean includeEdge;

  public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
    this.spanCount = spanCount;
    this.spacing1 = spacing;
    this.spacing2 = spacing;
    this.includeEdge = includeEdge;
  }

  public GridSpacingItemDecoration(int spanCount, int spacing1, int spacing2, boolean includeEdge) {
    this.spanCount = spanCount;
    this.spacing1 = spacing1;
    this.spacing2 = spacing2;
    this.includeEdge = includeEdge;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    int position = parent.getChildAdapterPosition(view);
    int column = position % spanCount;

    if (includeEdge) {
      outRect.left = spacing1 - column * spacing1 / spanCount;
      outRect.right = (column + 1) * spacing1 / spanCount;

      if (position < spanCount) {
        outRect.top = spacing2;
      }
      outRect.bottom = spacing2;
    } else {
      outRect.left = column * spacing1 / spanCount;
      outRect.right = spacing1 - (column + 1) * spacing1 / spanCount;
      if (position >= spanCount) {
        outRect.top = spacing2;
      }
    }
  }
}
