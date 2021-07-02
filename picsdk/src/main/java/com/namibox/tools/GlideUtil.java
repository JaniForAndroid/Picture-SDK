package com.namibox.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Create time: 2018/11/1.
 */
public class GlideUtil {

  public static final int ALL = 0;
  public static final int DATA = 1;
  public static final int NONE = 2;
  public static final int AUTOMATIC = 3;
  public static final int RESOURCE = 4;

  private static DiskCacheStrategy[] strategies = {
      DiskCacheStrategy.ALL,
      DiskCacheStrategy.DATA,
      DiskCacheStrategy.NONE,
      DiskCacheStrategy.AUTOMATIC,
      DiskCacheStrategy.RESOURCE
  };

//  private static GlideRequest<Drawable> glideRequest(Context context, Object obj,
//      RequestOptions options) {
//    if (context == null || context instanceof Activity && ((Activity) context).isDestroyed()) {
//      return null;
//    }
//    if (options == null) {
//      options = new RequestOptions();
//    }
//    if (Utils.isPluginContext(context)) {
//      if (obj instanceof Integer) {
//        obj = context.getResources().openRawResource((Integer) obj);
//      }
//      context = ((ContextWrapper) context).getBaseContext();
//    }
//    return GlideApp.with(context)
//        .load(obj)
//        .apply(options);
//  }

//  private static GlideRequest<GifDrawable> glideRequest2(Context context, Object obj,
//      RequestOptions options) {
//    if (context == null || context instanceof Activity && ((Activity) context).isDestroyed()) {
//      return null;
//    }
//    if (options == null) {
//      options = new RequestOptions();
//    }
//    if (Utils.isPluginContext(context)) {
//      if (obj instanceof Integer) {
//        obj = context.getResources().openRawResource((Integer) obj);
//      }
//      context = ((ContextWrapper) context).getBaseContext();
//    }
//    return GlideApp.with(context).asGif()
//        .load(obj)
//        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//        .apply(options);
//  }

//  private static GlideRequest<Bitmap> glideRequest3(Context context, Object obj,
//      RequestOptions options) {
//    if (context == null || context instanceof Activity && ((Activity) context).isDestroyed()) {
//      return null;
//    }
//    if (options == null) {
//      options = new RequestOptions();
//    }
//    if (Utils.isPluginContext(context)) {
//      if (obj instanceof Integer) {
//        obj = context.getResources().openRawResource((Integer) obj);
//      }
//      context = ((ContextWrapper) context).getBaseContext();
//    }
//    return GlideApp.with(context).asBitmap()
//        .load(obj)
//        .apply(options);
//  }

  public static void loadImage(Context context, Object obj, ImageView view) {
//    GlideRequest glideRequest = glideRequest(context, obj, null);
//    if (glideRequest == null) {
//      return;
//    }
//    glideRequest.into(view);
    RequestOptions options = new RequestOptions();
    if (obj instanceof Integer) {
      obj = context.getResources().openRawResource((Integer) obj);
    }
    Glide.with(context)
        .load(obj)
        .apply(options)
        .into(view);
  }

  public static void preload(Context context, Object obj) {
//    GlideRequest glideRequest = glideRequest(context, obj, null);
//    if (glideRequest == null) {
//      return;
//    }
//    glideRequest.preload();
    RequestOptions options = new RequestOptions();
    if (obj instanceof Integer) {
      obj = context.getResources().openRawResource((Integer) obj);
    }
    Glide.with(context)
        .load(obj)
        .apply(options)
        .preload();
  }

  public static void loadGif(Context context, Object obj, ImageView view) {
//    GlideRequest glideRequest = glideRequest2(context, obj, null);
//    if (glideRequest == null) {
//      return;
//    }
//    glideRequest.into(view);
    Glide.with(view)
        .asGif()
        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
        .load(obj)
        .into(view);
  }

  public static void loadImage(Context context, Object obj, Drawable placeholder, Drawable error,
      boolean skipMemoryCache,
      int diskCacheStrategy, ImageView view) {
    RequestOptions options = new RequestOptions()
        .skipMemoryCache(skipMemoryCache)
        .diskCacheStrategy(strategies[diskCacheStrategy])
        //.dontAnimate()
        .dontTransform();
    if (placeholder != null) {
      options.placeholder(placeholder);
    }
    if (error != null) {
      options.error(error);
    }
//    GlideRequest glideRequest = glideRequest(context, obj, options);
//    if (glideRequest == null) {
//      return;
//    }
//    glideRequest.into(view);

    if (obj instanceof Integer) {
      obj = context.getResources().openRawResource((Integer) obj);
    }
    Glide.with(context)
        .load(obj)
        .apply(options)
        .into(view);
  }

  public static void loadImage(Context context, Object obj, int placeholder, int error,
      boolean skipMemoryCache,
      int diskCacheStrategy, ImageView view) {
    Drawable placeholderDrawable = null;
    if (placeholder != 0) {
      placeholderDrawable = context.getResources().getDrawable(placeholder);
    }
    Drawable errorDrawable = null;
    if (error != 0) {
      errorDrawable = context.getResources().getDrawable(error);
    }
    loadImage(context, obj, placeholderDrawable, errorDrawable, skipMemoryCache, diskCacheStrategy,
        view);
  }

  public static void loadDrawable(Context context, Object obj, Drawable placeholder, Drawable error,
      boolean skipMemoryCache, int diskCacheStrategy, Callback callback) {
    loadDrawable(context, obj, placeholder, error, skipMemoryCache, diskCacheStrategy,
        new WeakTarget<>(callback));
  }

  public static void loadDrawable(Context context, Object obj, Drawable placeholder, Drawable error,
      boolean skipMemoryCache, int diskCacheStrategy, WeakTarget<Drawable> target) {
    RequestOptions options = new RequestOptions()
        .skipMemoryCache(skipMemoryCache)
        .diskCacheStrategy(strategies[diskCacheStrategy])
        //.dontAnimate()
        .dontTransform();
    if (placeholder != null) {
      options.placeholder(placeholder);
    }
    if (error != null) {
      options.error(error);
    }
//    GlideRequest glideRequest = glideRequest(context, obj, options);
//    if (glideRequest == null) {
//      return;
//    }
//    glideRequest.into(target);
    if (obj instanceof Integer) {
      obj = context.getResources().openRawResource((Integer) obj);
    }
    Glide.with(context)
        .load(obj)
        .apply(options)
        .into(target);
  }

  public static void loadDrawable(Context context, Object obj, int placeholder, int error,
      boolean skipMemoryCache,
      int diskCacheStrategy, final Callback callback) {
    Drawable placeholderDrawable = null;
    if (placeholder != 0) {
      placeholderDrawable = context.getResources().getDrawable(placeholder);
    }
    Drawable errorDrawable = null;
    if (error != 0) {
      errorDrawable = context.getResources().getDrawable(error);
    }
    loadDrawable(context, obj, placeholderDrawable, errorDrawable, skipMemoryCache,
        diskCacheStrategy, callback);
  }

  public static void loadDrawable(Context context, Object obj, final Callback callback) {
    loadDrawable(context, obj, 0, 0, false, DATA, callback);
  }

  public static void loadBitmap(Context context, Object obj, Drawable placeholder, Drawable error,
      boolean skipMemoryCache,
      int diskCacheStrategy, Callback2 callback) {
    loadBitmap(context, obj, placeholder, error, skipMemoryCache, diskCacheStrategy,
        new WeakTarget<>(callback));
  }

  public static void loadBitmap(Context context, Object obj, Drawable placeholder, Drawable error,
      boolean skipMemoryCache,
      int diskCacheStrategy, WeakTarget<Bitmap> target) {
    RequestOptions options = new RequestOptions()
        .skipMemoryCache(skipMemoryCache)
        .diskCacheStrategy(strategies[diskCacheStrategy])
        //.dontAnimate()
        .dontTransform();
    if (placeholder != null) {
      options.placeholder(placeholder);
    }
    if (error != null) {
      options.error(error);
    }
//    GlideRequest<Bitmap> glideRequest = glideRequest3(context, obj, options);
//    if (glideRequest == null) {
//      return;
//    }
//    glideRequest.into(target);
    Glide.with(context).asBitmap()
        .load(obj)
        .apply(options)
        .into(target);
  }

  public static void loadBitmap(Context context, Object obj, int placeholder, int error,
      boolean skipMemoryCache,
      int diskCacheStrategy, final Callback2 callback) {
    Drawable placeholderDrawable = null;
    if (placeholder != 0) {
      placeholderDrawable = context.getResources().getDrawable(placeholder);
    }
    Drawable errorDrawable = null;
    if (error != 0) {
      errorDrawable = context.getResources().getDrawable(error);
    }
    loadBitmap(context, obj, placeholderDrawable, errorDrawable, skipMemoryCache, diskCacheStrategy,
        callback);
  }

  public static void loadRoundImage(Context context, ImageView imageView, Object obj, int radius) {
    RequestOptions options = new RequestOptions()
        .transform(new RoundedCornersTransformation(radius, 0));
//    GlideRequest glideRequest = glideRequest(context, obj, options);
//    if (glideRequest == null) {
//      return;
//    }
//    glideRequest.into(imageView);

    if (obj instanceof Integer) {
      obj = context.getResources().openRawResource((Integer) obj);
    }
    Glide.with(context)
        .load(obj)
        .apply(options)
        .into(imageView);
  }

  public static void loadBlurImage(Context context, Object obj, ImageView imageView, int radius,
      int sampling) {
    RequestOptions options = RequestOptions
        .bitmapTransform(new BlurTransformation(radius, sampling));
//    GlideRequest glideRequest = glideRequest(context, obj, options);
//    if (glideRequest == null) {
//      return;
//    }
//    glideRequest.into(imageView);

    if (obj instanceof Integer) {
      obj = context.getResources().openRawResource((Integer) obj);
    }
    Glide.with(context)
        .load(obj)
        .apply(options)
        .into(imageView);
  }

  public static void loadRoundedCornersImage(Context context, ImageView imageView, Object obj,
      int radius) {
    loadRoundedCornersImage(context, imageView, obj, 0, 0, radius);
  }

  public static void loadRoundedCornersImage(Context context, ImageView imageView, Object obj,
      int placeholder, int error,
      int radius) {
    RequestOptions options = new RequestOptions()
        .transform(new MultiTransformation<>(new CenterCrop(),
            new RoundedCorners(radius)));
    if (placeholder != 0) {
      Drawable placeholderDrawable = context.getResources().getDrawable(placeholder);
      options.placeholder(placeholderDrawable);
    }
    if (error != 0) {
      Drawable errorDrawable = context.getResources().getDrawable(error);
      options.error(errorDrawable);
    }
//    GlideRequest glideRequest = glideRequest(context, obj, options);
//    if (glideRequest == null) {
//      return;
//    }
//    glideRequest.into(imageView);

    if (obj instanceof Integer) {
      obj = context.getResources().openRawResource((Integer) obj);
    }
    Glide.with(context)
        .load(obj)
        .apply(options)
        .into(imageView);
  }

  public static void loadCircleImage(Context context, ImageView imageView, Object obj,
      int placeholder, int error) {
    RequestOptions options = new RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.DATA);
    if (placeholder != 0) {
      Drawable placeholderDrawable = context.getResources().getDrawable(placeholder);
      options.placeholder(placeholderDrawable);
    }
    if (error != 0) {
      Drawable errorDrawable = context.getResources().getDrawable(error);
      options.error(errorDrawable);
    }
//    GlideRequest glideRequest = glideRequest(context, obj, options);
//    if (glideRequest == null) {
//      return;
//    }
//    glideRequest.centerCrop().circleCrop().into(imageView);

    if (obj instanceof Integer) {
      obj = context.getResources().openRawResource((Integer) obj);
    }
    Glide.with(context)
        .load(obj)
        .apply(options)
        .centerCrop()
        .circleCrop()
        .into(imageView);
  }

  public static void loadCircleImageWithBorder(Context context, ImageView imageView, Object obj,
      int placeholder, int borderWidth, String colorString, int error) {
    RequestOptions options = new RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.DATA);
    if (placeholder != 0) {
      Drawable placeholderDrawable = context.getResources().getDrawable(placeholder);
      options.placeholder(placeholderDrawable);
    }
    if (error != 0) {
      Drawable errorDrawable = context.getResources().getDrawable(error);
      options.error(errorDrawable);
    }
//    GlideRequest glideRequest = glideRequest(context, obj, options);
//    if (glideRequest == null) {
//      return;
//    }
//    glideRequest.centerCrop()
//        .transform(new GlideCircleWithBorder(context, borderWidth, Color.parseColor(colorString)))
//        .into(imageView);

    if (obj instanceof Integer) {
      obj = context.getResources().openRawResource((Integer) obj);
    }
    Glide.with(context)
        .load(obj)
        .apply(options)
        .centerCrop()
        .transform(new GlideCircleWithBorder(context, borderWidth, Color.parseColor(colorString)))
        .into(imageView);
  }

  public static void loadCircleImage(Context context, ImageView imageView, Object obj) {
    loadCircleImage(context, imageView, obj, 0, 0);
  }

  public static class WeakTarget<Z> extends SimpleTarget<Z> {

    private CallbackImpl<Z> callback;

    public WeakTarget() {
    }

    public WeakTarget(CallbackImpl<Z> callback) {
      this.callback = callback;
    }

    public CallbackImpl<Z> getCallback() {
      return callback;
    }

    @Override
    public void onLoadStarted(@Nullable Drawable placeholder) {
      if (getCallback() != null) {
        getCallback().onLoadStarted(placeholder);
      }
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
      if (getCallback() != null) {
        getCallback().onLoadFailed(errorDrawable);
      }
    }

    public void onResourceReady(Z resource) {
    }

    @Override
    public void onResourceReady(Z resource, Transition<? super Z> transition) {
      if (getCallback() != null) {
        getCallback().onResourceReady(resource);
      }
      onResourceReady(resource);
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      callback = null;
    }
  }

  public static class Callback implements CallbackImpl<Drawable> {

    @Override
    public void onLoadStarted(Drawable placeholder) {
    }

    @Override
    public void onLoadFailed(Drawable errorDrawable) {
    }

    @Override
    public void onResourceReady(Drawable resource) {
    }
  }

  public static class Callback2 implements CallbackImpl<Bitmap> {

    @Override
    public void onLoadStarted(Drawable placeholder) {
    }

    @Override
    public void onLoadFailed(Drawable errorDrawable) {
    }

    @Override
    public void onResourceReady(Bitmap resource) {
    }
  }

  private interface CallbackImpl<T> {

    void onLoadStarted(Drawable placeholder);

    void onLoadFailed(Drawable errorDrawable);

    void onResourceReady(T resource);
  }
}
