package com.namibox.commonlib.view.emojicon.gifutil;

import android.widget.TextView;
import com.namibox.commonlib.view.GlideImageGetter;


/**
 * Created by CentMeng on 16/6/1.
 */
public class ImageTextUtil {


  public static GlideImageGetter getImageGetter(TextView mTextView) {
    return new GlideImageGetter(mTextView.getContext(), mTextView);

  }

//    public static Drawable getUrlDrawable(String source, TextView mTextView) {
//        GlideImageGetter imageGetter = new GlideImageGetter(mTextView.getContext(), mTextView);
//        return imageGetter.getDrawable(source);
//
//    }
}
