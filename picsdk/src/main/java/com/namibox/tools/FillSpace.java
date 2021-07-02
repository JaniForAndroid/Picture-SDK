package com.namibox.tools;

import android.graphics.Bitmap;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.namibox.util.Logger;
import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * Create time: 2018/9/25.
 */
public class FillSpace extends BitmapTransformation {

  private final String ID;
  private final byte[] ID_BYTES;
  private int height;

  public FillSpace(int height) {
    this.height = height;
    ID = "com.jinxin.namibox.transformations.FillSpace" + height;
    ID_BYTES = ID.getBytes(Charset.forName("UTF-8"));
  }

  @Override
  public Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
    if (toTransform.getHeight() == height) {
      return toTransform;
    }

    int width = height * toTransform.getWidth() / toTransform.getHeight();
    Logger.e(toTransform.getWidth() + "->" + width + ", " + toTransform.getHeight() + "->" + height);
    return Bitmap.createScaledBitmap(toTransform, width, height, /*filter=*/ true);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof FillSpace && ((FillSpace) o).ID.equals(ID);
  }

  @Override
  public int hashCode() {
    return ID.hashCode();
  }

  @Override
  public void updateDiskCacheKey(MessageDigest messageDigest) {
    messageDigest.update(ID_BYTES);
  }
}
