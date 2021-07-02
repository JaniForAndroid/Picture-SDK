package com.namibox.imageselector.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.net.Uri;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class PowerImageView extends android.support.v7.widget.AppCompatImageView {

  /**
   * 播放GIF动画的关键类
   */
  private Movie mMovie;

  /**
   * 记录动画开始的时间
   */
  private long mMovieStart;

  /**
   * PowerImageView构造函数。
   */
  public PowerImageView(Context context) {
    this(context, null);
  }

  /**
   * PowerImageView构造函数。
   */
  public PowerImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  /**
   * PowerImageView构造函数，在这里完成所有必要的初始化操作。
   */
  public PowerImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    int resourceId = getResourceId(attrs);
    setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    if (resourceId != 0) {
      // 当资源id不等于0时，就去获取该资源的流
      InputStream is = getResources().openRawResource(resourceId);
      mMovie = Movie.decodeStream(is);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mMovie == null) {
      // mMovie等于null，说明是张普通的图片，则直接调用父类的onDraw()方法
      super.onDraw(canvas);
    } else {
      // mMovie不等于null，说明是张GIF图片
      // 如果允许自动播放，就调用playMovie()方法播放GIF动画
      playMovie(canvas);
      invalidate();
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  public void setImageURI(Uri uri) {
    if ("file".equals(uri.getScheme())) {
      try {
        mMovie = Movie.decodeStream(new FileInputStream(uri.getPath()));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        mMovie = null;
      }
    }
    if (mMovie != null) {
      super.setImageURI(uri);
    }
  }

  public boolean isMovie() {
    return mMovie != null;
  }

  private void playMovie(Canvas canvas) {
    long now = SystemClock.uptimeMillis();
    if (mMovieStart == 0) {
      mMovieStart = now;
    }
    int duration = mMovie.duration();
    if (duration == 0) {
      duration = 1000;
    }
    int relTime = (int) ((now - mMovieStart) % duration);
    int height = mMovie.height();
    int width = mMovie.width();
    int sw = getWidth();
    int sh = getHeight();
    float r = 1f * width / height;
    float rb = 1f * sw / sh;
    float left, top;
    float scale;
    if (r < rb) {
      float w = r * sh;
      left = (sw - w) / 2;
      top = 0;
      scale = 1f * sh / height;
    } else {
      float h = sw / r;
      left = 0;
      top = (sh - h) / 2;
      scale = 1f * sw / width;
    }
    mMovie.setTime(relTime);
    canvas.save();
    canvas.scale(scale, scale, sw / 2, sh / 2);
    mMovie.draw(canvas, left + (scale * width - width) / 2, top + (scale * height - height) / 2);
    canvas.restore();
  }

  private int getResourceId(AttributeSet attrs) {
    for (int i = 0; i < attrs.getAttributeCount(); i++) {
      if (attrs.getAttributeName(i).equals("src")) {
        return attrs.getAttributeResourceValue(i, 0);
      }
    }
    return 0;
  }

}
