package com.namibox.dub;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;
import com.namibox.util.Utils;
import java.util.ArrayList;

/**
 * @author: Shelter
 * Create time: 2019/8/12, 9:29.
 */
public class AutoLineFeedLayoutManager extends RecyclerView.LayoutManager {

  private boolean centerHorizontal;
  private Context context;

  public AutoLineFeedLayoutManager(Context context, boolean centerHorizontal) {
    this.context = context;
    this.centerHorizontal = centerHorizontal;
  }

  @Override
  public RecyclerView.LayoutParams generateDefaultLayoutParams() {
    return new RecyclerView.LayoutParams(
        RecyclerView.LayoutParams.WRAP_CONTENT,
        RecyclerView.LayoutParams.WRAP_CONTENT);
  }

  @Override
  public void onLayoutChildren(Recycler recycler, State state) {
    detachAndScrapAttachedViews(recycler);

    int sumWidth = getWidth();

    int curLineWidth = 0, curLineTop = 0;
    int lastLineMaxHeight = 0;
    int marginLeftRight = Utils.dp2px(context, 3);
    int marginTopBottom = Utils.dp2px(context, 3);
    if (centerHorizontal) {
      int curTotalWidth = 0;
      ArrayList<View> list = new ArrayList<>();
      //获取每行显示的Item
      for (int i = 0; i < getItemCount(); i++) {
        View view = recycler.getViewForPosition(i);
        measureChildWithMargins(view, 0, 0);
        int width = view.getMeasuredWidth() + marginLeftRight * 2;
        int height = getDecoratedMeasuredHeight(view);
        if (curTotalWidth + width > sumWidth) {
          int widthUsed = (sumWidth - curTotalWidth) / 2;
          for (int j = 0; j < list.size(); j++) {
            View child = list.get(j);
            addView(child);
            int childWidth = getDecoratedMeasuredWidth(child) + marginLeftRight * 2;
            int childHeight = getDecoratedMeasuredHeight(child);

            curLineWidth += childWidth;
            //不需要换行
            layoutDecorated(child, curLineWidth - childWidth + widthUsed, curLineTop, curLineWidth + widthUsed,
                curLineTop + childHeight);
          }

          list.clear();
          list.add(view);
          curLineTop += (height + marginTopBottom);
          curTotalWidth = width;
          curLineWidth = 0;
          if (i == getItemCount() - 1) {
            widthUsed = (sumWidth - curTotalWidth) / 2;
            for (int j = 0; j < list.size(); j++) {
              View child = list.get(j);
              addView(child);
              int childWidth = getDecoratedMeasuredWidth(child) + marginLeftRight * 2;
              int childHeight = getDecoratedMeasuredHeight(child);

              curLineWidth += childWidth;
              //不需要换行
              layoutDecorated(child, curLineWidth - childWidth + widthUsed, curLineTop, curLineWidth + widthUsed,
                  curLineTop + childHeight);
            }
          }
        } else {
          list.add(view);
          curTotalWidth += width;
          if (i == getItemCount() - 1) {
            int widthUsed = (sumWidth - curTotalWidth) / 2;
            for (int j = 0; j < list.size(); j++) {
              View child = list.get(j);
              addView(child);
              int childWidth = getDecoratedMeasuredWidth(child) + marginLeftRight * 2;
              int childHeight = getDecoratedMeasuredHeight(child);

              curLineWidth += childWidth;
              //不需要换行
              layoutDecorated(child, curLineWidth - childWidth + widthUsed, curLineTop, curLineWidth + widthUsed,
                  curLineTop + childHeight);
            }
          }
        }
      }

    } else {
      for (int i = 0; i < getItemCount(); i++) {
        View view = recycler.getViewForPosition(i);

        addView(view);
        measureChildWithMargins(view, 0, 0);
        int width = getDecoratedMeasuredWidth(view) + marginLeftRight * 2;
        int height = getDecoratedMeasuredHeight(view);

        curLineWidth += width;
        //不需要换行
        if (curLineWidth <= sumWidth) {
          layoutDecorated(view, curLineWidth - width, curLineTop, curLineWidth, curLineTop + height);
          //比较当前行多有item的最大高度
          lastLineMaxHeight = Math.max(lastLineMaxHeight, height);
        } else {//换行
          curLineWidth = width;
          if (lastLineMaxHeight == 0) {
            lastLineMaxHeight = height;
          }
          //记录当前行top
          curLineTop += (lastLineMaxHeight  + marginTopBottom);

          layoutDecorated(view, 0, curLineTop, width, curLineTop + height);
          lastLineMaxHeight = height;
        }
      }
    }

  }

}
