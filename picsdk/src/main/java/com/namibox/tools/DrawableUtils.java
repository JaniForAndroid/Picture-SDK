package com.namibox.tools;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.LayerDrawable;

/**
 * @author: zbd
 * @time: 2019-07-04
 * @Description:
 */
public class DrawableUtils {

  public static LayerDrawable adjustLayerDrawable(LayerDrawable layerDrawable, String[] colors, int items){
    int length = colors.length;
    int[] bg_colors = new int[length];
    for (int i = 0; i < length; i++) {
      bg_colors[i] = Color.parseColor(colors[i]);
    }
    GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(items - 1);
    gradientDrawable.setOrientation(Orientation.LEFT_RIGHT);
    gradientDrawable.setColors(bg_colors);
    return layerDrawable;
  }
}
