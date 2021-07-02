package com.namibox.commonlib.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.picsdk.R;
import com.namibox.util.Utils;
import java.util.HashSet;
import java.util.Set;


/**
 * @author CentMeng csdn@vip.163.com on 16/7/19.
 */
public class GlideImageGetter implements Html.ImageGetter, Drawable.Callback {

  private static final String TAG = "GlideImageGetter";

  private final Context mContext;

  private final TextView mTextView;

  private final Set<ImageGetterViewTarget> mTargets;
  private long lastInvalidateTime;

  public static GlideImageGetter get(View view) {
    return (GlideImageGetter) view.getTag(R.id.drawable_callback_tag);
  }

  public void clear() {
    GlideImageGetter prev = get(mTextView);
    if (prev == null) {
      return;
    }

    for (ImageGetterViewTarget target : prev.mTargets) {
      Glide.with(mContext).clear(target);
    }
  }

  public GlideImageGetter(Context context, TextView textView) {
    this.mContext = context;
    this.mTextView = textView;

//        clear(); //屏蔽掉这句在TextView中可以加载多张图片
    mTargets = new HashSet<>();
    mTextView.setTag(R.id.drawable_callback_tag, this);
  }

  @Override
  public Drawable getDrawable(String url) {
    final UrlDrawable_Glide urlDrawable = new UrlDrawable_Glide();

    RequestOptions options = new RequestOptions().skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.NONE);
    Glide.with(mContext)
        .asGif()
        .load(url)
        .apply(options)
        .into(new ImageGetterViewTarget(mTextView, urlDrawable));

    return urlDrawable;

  }

  @Override
  public void invalidateDrawable(Drawable who) {

    if (System.currentTimeMillis() - lastInvalidateTime > 30) {
      lastInvalidateTime = System.currentTimeMillis();
      mTextView.invalidate();
    }
  }

  @Override
  public void scheduleDrawable(Drawable who, Runnable what, long when) {

  }

  @Override
  public void unscheduleDrawable(Drawable who, Runnable what) {

  }

  private class ImageGetterViewTarget extends ViewTarget<TextView, GifDrawable> {

    private final UrlDrawable_Glide mDrawable;

    private ImageGetterViewTarget(TextView view, UrlDrawable_Glide drawable) {
      super(view);
      mTargets.add(this);
      this.mDrawable = drawable;
    }

    @Override
    public void onResourceReady(GifDrawable resource, Transition<? super GifDrawable> transition) {
      int size = Utils.dp2px(mContext, 17);
      Rect rect = new Rect(0, 0, size, size);
      resource.setBounds(rect);
      mDrawable.setBounds(rect);
      mDrawable.setDrawable(resource);
      if (resource instanceof Animatable) {
        mDrawable.setCallback(get(getView()));
        resource.setLoopCount(GifDrawable.LOOP_FOREVER);
        resource.start();
      }

      getView().setText(getView().getText());
      getView().invalidate();
    }

    private Request request;

    @Override
    public Request getRequest() {
      return request;
    }

    @Override
    public void setRequest(Request request) {
      this.request = request;
    }
  }
}