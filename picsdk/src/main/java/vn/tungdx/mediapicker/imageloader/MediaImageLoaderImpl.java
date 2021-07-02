package vn.tungdx.mediapicker.imageloader;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.picsdk.R;

/**
 * @author TUNGDX
 */

public class MediaImageLoaderImpl implements MediaImageLoader {

  private RequestManager requestManager;

  public MediaImageLoaderImpl(Context context) {
    requestManager = Glide.with(context);
  }

  @Override
  public void displayImage(Uri uri, ImageView imageView) {
    if (uri != null) {
      RequestOptions options = new RequestOptions()
          .centerCrop()
          .placeholder(R.drawable.ic_picker_imagefail)
          .error(R.drawable.ic_picker_imagefail)
          .diskCacheStrategy(DiskCacheStrategy.NONE);
      requestManager.load(uri)
          .apply(options)
          .into(imageView);
    } else {
      requestManager.load(R.drawable.ic_picker_imagefail).into(imageView);
    }

  }
}