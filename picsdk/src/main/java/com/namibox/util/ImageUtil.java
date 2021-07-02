package com.namibox.util;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.util.Log;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Author: ryancheng
 * Create time: 2014/12/26 11:23
 */
public class ImageUtil {

  public static Bitmap doBlur(Bitmap resource, int radius, float scaleFactor, int shadowColor) {
    int width = (int) (resource.getWidth() / scaleFactor);
    int height = (int) (resource.getHeight() / scaleFactor);
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    canvas.scale(1 / scaleFactor, 1 / scaleFactor);
    Paint paint = new Paint();
    paint.setFlags(Paint.FILTER_BITMAP_FLAG);
    canvas.drawBitmap(resource, 0, 0, paint);
    canvas.drawColor(shadowColor);
    return doBlur(bitmap, radius, true);
  }

  static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {

    // Stack Blur v1.0 from
    // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
    //
    // Java Author: Mario Klingemann <mario at quasimondo.com>
    // http://incubator.quasimondo.com
    // created Feburary 29, 2004
    // Android port : Yahel Bouaziz <yahel at kayenko.com>
    // http://www.kayenko.com
    // ported april 5th, 2012

    // This is a compromise between Gaussian Blur and Box blur
    // It creates much better looking blurs than Box Blur, but is
    // 7x faster than my Gaussian Blur implementation.
    //
    // I called it Stack Blur because this describes best how this
    // filter works internally: it creates a kind of moving stack
    // of colors whilst scanning through the image. Thereby it
    // just has to add one new block of color to the right side
    // of the stack and remove the leftmost color. The remaining
    // colors on the topmost layer of the stack are either added on
    // or reduced by one, depending on if they are on the right or
    // on the left side of the stack.
    //
    // If you are using this algorithm in your code please add
    // the following line:
    //
    // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

    Bitmap bitmap;
    if (canReuseInBitmap) {
      bitmap = sentBitmap;
    } else {
      bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
    }

    if (radius < 1) {
      return (null);
    }

    int w = bitmap.getWidth();
    int h = bitmap.getHeight();

    int[] pix = new int[w * h];
    bitmap.getPixels(pix, 0, w, 0, 0, w, h);

    int wm = w - 1;
    int hm = h - 1;
    int wh = w * h;
    int div = radius + radius + 1;

    int r[] = new int[wh];
    int g[] = new int[wh];
    int b[] = new int[wh];
    int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
    int vmin[] = new int[Math.max(w, h)];

    int divsum = (div + 1) >> 1;
    divsum *= divsum;
    int dv[] = new int[256 * divsum];
    for (i = 0; i < 256 * divsum; i++) {
      dv[i] = (i / divsum);
    }

    yw = yi = 0;

    int[][] stack = new int[div][3];
    int stackpointer;
    int stackstart;
    int[] sir;
    int rbs;
    int r1 = radius + 1;
    int routsum, goutsum, boutsum;
    int rinsum, ginsum, binsum;

    for (y = 0; y < h; y++) {
      rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
      for (i = -radius; i <= radius; i++) {
        p = pix[yi + Math.min(wm, Math.max(i, 0))];
        sir = stack[i + radius];
        sir[0] = (p & 0xff0000) >> 16;
        sir[1] = (p & 0x00ff00) >> 8;
        sir[2] = (p & 0x0000ff);
        rbs = r1 - Math.abs(i);
        rsum += sir[0] * rbs;
        gsum += sir[1] * rbs;
        bsum += sir[2] * rbs;
        if (i > 0) {
          rinsum += sir[0];
          ginsum += sir[1];
          binsum += sir[2];
        } else {
          routsum += sir[0];
          goutsum += sir[1];
          boutsum += sir[2];
        }
      }
      stackpointer = radius;

      for (x = 0; x < w; x++) {

        r[yi] = dv[rsum];
        g[yi] = dv[gsum];
        b[yi] = dv[bsum];

        rsum -= routsum;
        gsum -= goutsum;
        bsum -= boutsum;

        stackstart = stackpointer - radius + div;
        sir = stack[stackstart % div];

        routsum -= sir[0];
        goutsum -= sir[1];
        boutsum -= sir[2];

        if (y == 0) {
          vmin[x] = Math.min(x + radius + 1, wm);
        }
        p = pix[yw + vmin[x]];

        sir[0] = (p & 0xff0000) >> 16;
        sir[1] = (p & 0x00ff00) >> 8;
        sir[2] = (p & 0x0000ff);

        rinsum += sir[0];
        ginsum += sir[1];
        binsum += sir[2];

        rsum += rinsum;
        gsum += ginsum;
        bsum += binsum;

        stackpointer = (stackpointer + 1) % div;
        sir = stack[(stackpointer) % div];

        routsum += sir[0];
        goutsum += sir[1];
        boutsum += sir[2];

        rinsum -= sir[0];
        ginsum -= sir[1];
        binsum -= sir[2];

        yi++;
      }
      yw += w;
    }
    for (x = 0; x < w; x++) {
      rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
      yp = -radius * w;
      for (i = -radius; i <= radius; i++) {
        yi = Math.max(0, yp) + x;

        sir = stack[i + radius];

        sir[0] = r[yi];
        sir[1] = g[yi];
        sir[2] = b[yi];

        rbs = r1 - Math.abs(i);

        rsum += r[yi] * rbs;
        gsum += g[yi] * rbs;
        bsum += b[yi] * rbs;

        if (i > 0) {
          rinsum += sir[0];
          ginsum += sir[1];
          binsum += sir[2];
        } else {
          routsum += sir[0];
          goutsum += sir[1];
          boutsum += sir[2];
        }

        if (i < hm) {
          yp += w;
        }
      }
      yi = x;
      stackpointer = radius;
      for (y = 0; y < h; y++) {
        // Preserve alpha channel: ( 0xff000000 & pix[yi] )
        pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

        rsum -= routsum;
        gsum -= goutsum;
        bsum -= boutsum;

        stackstart = stackpointer - radius + div;
        sir = stack[stackstart % div];

        routsum -= sir[0];
        goutsum -= sir[1];
        boutsum -= sir[2];

        if (x == 0) {
          vmin[y] = Math.min(y + r1, hm) * w;
        }
        p = x + vmin[y];

        sir[0] = r[p];
        sir[1] = g[p];
        sir[2] = b[p];

        rinsum += sir[0];
        ginsum += sir[1];
        binsum += sir[2];

        rsum += rinsum;
        gsum += ginsum;
        bsum += binsum;

        stackpointer = (stackpointer + 1) % div;
        sir = stack[stackpointer];

        routsum += sir[0];
        goutsum += sir[1];
        boutsum += sir[2];

        rinsum -= sir[0];
        ginsum -= sir[1];
        binsum -= sir[2];

        yi += w;
      }
    }

    bitmap.setPixels(pix, 0, w, 0, 0, w, h);

    return (bitmap);
  }

  public static byte[] bmpToByteArray(Bitmap bmp) {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bmp.compress(CompressFormat.JPEG, 85, output);
    byte[] result = output.toByteArray();
    try {
      output.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }


  /**
   * 从指定的路径下，生成指定宽高的图片
   *
   * @param filePath 文件路径
   * @param reqWidth 宽
   * @param reqHeight 高
   */
  public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(filePath, options);
    int w = reqWidth;
    int h = reqHeight;
    if (options.outHeight == 0 || options.outWidth == 0) {
      //error
    } else if (reqWidth * options.outHeight > options.outWidth * reqHeight) {
      w = options.outWidth * h / options.outHeight;
    } else {
      h = options.outHeight * w / options.outWidth;
    }
    options.inSampleSize = calculateInSampleSize(options, w, h);

    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeFile(filePath, options);
  }

  public static Bitmap decodeSampledBitmapFromResource(Resources res, int id, int reqWidth,
      int reqHeight) {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(res, id, options);
    int w = reqWidth;
    int h = reqHeight;
    if (reqWidth * options.outHeight > options.outWidth * reqHeight) {
      w = options.outWidth * h / options.outHeight;
    } else {
      h = options.outHeight * w / options.outWidth;
    }
    options.inSampleSize = calculateInSampleSize(options, w, h);

    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeResource(res, id, options);
  }

  public static Bitmap decodeSampledBitmapFromBytes(byte[] bytes, int reqWidth, int reqHeight)
      throws IOException {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    int w = reqWidth;
    int h = reqHeight;
    if (reqWidth * options.outHeight > options.outWidth * reqHeight) {
      w = options.outWidth * h / options.outHeight;
    } else {
      h = options.outHeight * w / options.outWidth;
    }
    options.inSampleSize = calculateInSampleSize(options, w, h);

    options.inJustDecodeBounds = false;
    options.inMutable = true;
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
  }

  public static Bitmap zoomBitmap(Bitmap src) {
    // 获取这个图片的宽和高
    float width = src.getWidth();
    float height = src.getHeight();
    // 创建操作图片用的matrix对象
    Matrix matrix = new Matrix();
    // 计算宽高缩放率
    float maxSize = Math.max(width, height);
    if (maxSize < 2048) {
      return src;
    }
    float scale = 2048 / maxSize;
    // 缩放图片动作
    matrix.postScale(scale, scale);
    Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, (int) width, (int) height, matrix, true);
    src.recycle();
    return bitmap;
  }


  public static Bitmap zoomBitmap(Bitmap src, int w, int h, boolean canExpend) {
    // 获取这个图片的宽和高
    float width = src.getWidth();
    float height = src.getHeight();
    // 创建操作图片用的matrix对象
    Matrix matrix = new Matrix();
    if (!canExpend) {
      if (width < w && height < h) {
        return src;
      }
    }

    float scaleX = w / width;
    float scaleY = h / height;
    // 缩放图片动作
    matrix.postScale(scaleX, scaleY);
    Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, (int) width, (int) height, matrix, true);
    if (bitmap != src) {
      src.recycle();
    }

    return bitmap;
  }

  public static Bitmap rotateBitmap(Bitmap src, float degree) {
    float width = src.getWidth();
    float height = src.getHeight();
    Matrix matrix = new Matrix();
    matrix.postRotate(degree, width / 2, height / 2);
    Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, (int) width, (int) height, matrix, true);
    src.recycle();
    return bitmap;
  }

  public static void compressBmpToFile(Bitmap bmp, CompressFormat format, int quality,
      File file) {
    try {
      FileOutputStream fos = new FileOutputStream(file);
      bmp.compress(format, quality, fos);
      fos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void compressBmpToFile(Bitmap bmp, int quality, File file) {
    try {
      FileOutputStream fos = new FileOutputStream(file);
      bmp.compress(CompressFormat.JPEG, quality, fos);
      fos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static File compressBmpToFile(Bitmap bmp, int quality, String filePath) {
    File f = new File(filePath);
    if (f.exists()) {
      f.delete();
    }
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(f);
      bmp.compress(CompressFormat.JPEG, quality, out);
      out.flush();
      return f;
    } catch (Exception e) {
      e.printStackTrace();
      Logger.e("compressBmpToFile is error:" + e.toString());
      return null;
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static boolean isGif(String path) {
    //读取文件的前几个字节来判断图片格式
    byte[] bytes = new byte[6];
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(path);
      int result = fis.read(bytes, 0, bytes.length);
      if (result != -1) {
        String s = Utils.byteArrayToHexString(bytes).toUpperCase();
        return s.startsWith("47494638");
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return false;
  }

  private static String formatPhotoDate(long time) {
    return Utils.timeFormat(time, "yyyy-MM-dd");
  }

  public static String formatPhotoDate(String path) {
    File file = new File(path);
    if (file.exists()) {
      long time = file.lastModified();
      return formatPhotoDate(time);
    }
    return "1970-01-01";
  }


  private static int calculateInSampleSize(BitmapFactory.Options options,
      int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
      if (width > height) {
        inSampleSize = Math.round((float) height / (float) reqHeight);
      } else {
        inSampleSize = Math.round((float) width / (float) reqWidth);
      }
    }
    return inSampleSize;
  }

  /**
   * 从指定的路径下，生成限制宽高max的图片
   *
   * @param filePath 文件路径
   */
  public static Bitmap decodeBitmapFromFile(String filePath, int max) {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(filePath, options);
    int w = max;
    int h = max;
    if (max * options.outHeight > options.outWidth * max) {
      w = options.outWidth * h / options.outHeight;
    } else {
      h = options.outHeight * w / options.outWidth;
    }
    options.inSampleSize = calculateInSampleSize(options, w, h);

    options.inJustDecodeBounds = false;
    options.inMutable = true;
    return BitmapFactory.decodeFile(filePath, options);
  }

  public static Bitmap decodeBitmapFromFileWithCompose(String filePath) {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = false;
    BitmapFactory.decodeFile(filePath, options);
    return BitmapFactory.decodeFile(filePath, options);
  }

  public static Bitmap zoomBitmap(Bitmap src, int max) {
    // 获取这个图片的宽和高
    float width = src.getWidth();
    float height = src.getHeight();
    // 创建操作图片用的matrix对象
    Matrix matrix = new Matrix();
    // 计算宽高缩放率
    float maxSize = Math.max(width, height);
    if (maxSize < max) {
      Log.i("zoomBitmap", "needn't zoom");
      return src;
    }
    float scale = max / maxSize;
    // 缩放图片动作
    matrix.postScale(scale, scale);
    Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, (int) width, (int) height, matrix, true);
    src.recycle();
    return bitmap;
  }

  public static Bitmap createSquareBitmap(Bitmap src) {
    int width = src.getWidth();
    int height = src.getHeight();
    int size = Math.min(width, height);
    size = Math.max(size, 100);
    Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);
    Canvas canvas = new Canvas(bitmap);
    canvas.drawColor(0x00000000);
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final RectF rectF = new RectF(0, 0, size, size);
    float radius = size / 10f;
    canvas.drawRoundRect(rectF, radius, radius, paint);
    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    final Rect rect = new Rect(0, 0, size, size);
    canvas.drawBitmap(src, rect, rectF, paint);
    return bitmap;
  }

  public static Bitmap createSquareBitmap(Bitmap src, Bitmap tag) {
    int width = src.getWidth();
    int height = src.getHeight();
    int size = Math.min(width, height);
    size = Math.max(size, 100);
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
    Canvas canvas = new Canvas(bitmap);
    final RectF rectF = new RectF(0, 0, size, size);
    final Rect rect = new Rect(0, 0, size, size);
    canvas.drawBitmap(src, rect, rectF, paint);
    rect.set(0, 0, tag.getWidth(), tag.getHeight());
    rectF.set(0, 0, size * 0.5f, size * 0.25f);
    canvas.drawBitmap(tag, rect, rectF, paint);
    return bitmap;
  }

  public static Bitmap drawableBitmapOnWhiteBg(Bitmap bitmap, int max) {
    Bitmap drawBitmap = zoomBitmap(bitmap, max);
    int width = drawBitmap.getHeight() * 260 / 204;
    Bitmap newBitmap = Bitmap.createBitmap(width, drawBitmap.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(newBitmap);
    canvas.drawColor(0xffffffff);
    Paint paint = new Paint();
    int left = (width - drawBitmap.getWidth()) / 2;
    canvas.drawBitmap(drawBitmap, left, 0, paint); //将原图使用给定的画笔画到画布上
    drawBitmap.recycle();
    return newBitmap;
  }

  public static Bitmap getScreenShot(Activity activity) {
    View dView = activity.getWindow().getDecorView();
    dView.setDrawingCacheEnabled(true);
    dView.buildDrawingCache();
    return dView.getDrawingCache();
  }


  /**
   * 读取图片的旋转的角度
   *
   * @param path 图片绝对路径
   * @return 图片的旋转角度
   */
  public static int getBitmapDegree(String path) {
    int degree = 0;
    try {
      // 从指定路径下读取图片，并获取其EXIF信息
      ExifInterface exifInterface = new ExifInterface(path);
      // 获取图片的旋转信息
      int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
          ExifInterface.ORIENTATION_NORMAL);
      switch (orientation) {
        case ExifInterface.ORIENTATION_ROTATE_90:
          degree = 90;
          break;
        case ExifInterface.ORIENTATION_ROTATE_180:
          degree = 180;
          break;
        case ExifInterface.ORIENTATION_ROTATE_270:
          degree = 270;
          break;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return degree;
  }

  /**
   * 旋转图片，使图片保持正确的方向。
   *
   * @param bitmap 原始图片
   * @param degrees 原始图片的角度
   * @return Bitmap 旋转后的图片
   */
  public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
    if (degrees == 0 || null == bitmap) {
      return bitmap;
    }
    Matrix matrix = new Matrix();
    matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
    Bitmap bmp = Bitmap
        .createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    bitmap.recycle();
    return bmp;
  }

}
