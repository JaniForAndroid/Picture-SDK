/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.namibox.commonlib.view.emojicon;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.widget.TextView;
import com.namibox.commonlib.view.emojicon.domain.IconSet;
import com.namibox.commonlib.view.emojicon.domain.TimDefaultEmojiconDatas;
import com.namibox.commonlib.view.emojicon.domain.TimEmojicon;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pl.droidsonroids.gif.GifDrawable;

public class TimSmileUtils {

  private static final String TAG = "EaseSmileUtils";
  public static final String DELETE_KEY = "em_delete_delete_expression";
  private static final Factory spannableFactory = Factory
      .getInstance();

  private static final Map<Pattern, Object> emoticons = new HashMap<Pattern, Object>();
  private static HashMap<String, GifDrawable> gifDrawableHashMap = new HashMap<>();


  static {
    TimEmojicon[] emojicons = TimDefaultEmojiconDatas.getData();
    for (int i = 0; i < emojicons.length; i++) {
      addPattern(emojicons[i].getEmojiText(), emojicons[i].getIconSet());
    }
  }

  /**
   * 添加文字表情mapping
   *
   * @param emojiText emoji文本内容
   * @param icon 图片资源id或者本地路径
   */
  public static void addPattern(String emojiText, Object icon) {
    emoticons.put(Pattern.compile(Pattern.quote(emojiText)), icon);
  }

  public static void addHttp(Context context, Spannable spannable) {
    Pattern p = Pattern.compile(
        "((http|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?",
        Pattern.CASE_INSENSITIVE);
    Matcher matcher = p.matcher(spannable);
    while (matcher.find()) {
      spannable.setSpan(new URLSpan(matcher.group()), matcher.start(), matcher.end(),
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  /**
   * replace existing spannable with smiles
   */
  public static boolean addSmiles(Context context, Spannable spannable) {
    boolean hasChanges = false;
    for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
      Matcher matcher = entry.getKey().matcher(spannable);
      while (matcher.find()) {
        boolean set = true;
        for (ImageSpan span : spannable.getSpans(matcher.start(),
            matcher.end(), ImageSpan.class)) {
          if (spannable.getSpanStart(span) >= matcher.start()
              && spannable.getSpanEnd(span) <= matcher.end()) {
            spannable.removeSpan(span);
          } else {
            set = false;
            break;
          }
        }
        if (set) {
          hasChanges = true;
          Object value = entry.getValue();
          if (value instanceof String && !((String) value).startsWith("http")) {
            File file = new File((String) value);
            if (!file.exists() || file.isDirectory()) {
              return false;
            }
            spannable.setSpan(new ImageSpan(context, Uri.fromFile(file)),
                matcher.start(), matcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
          } else if (value instanceof IconSet) {
            IconSet iconSet = (IconSet) value;
            if (iconSet.iconPng.exists()) {
              ImageSpan imageSpan = new ImageSpanAlignCenter(context,
                  Uri.fromFile(((IconSet) value).iconPng));
              spannable.setSpan(imageSpan,
                  matcher.start(), matcher.end(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
              return false;
            }
          } else {
            spannable.setSpan(new ImageSpan(context, (Integer) value),
                matcher.start(), matcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
          }
        }
      }
    }

    return hasChanges;
  }

  /**
   * replace existing spannable with smiles
   */
  public static Spannable getSpannable(Context context, String emojiText) {
    for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
      Matcher matcher = entry.getKey().matcher(emojiText);
      while (matcher.find()) {
        Spannable spannable = spannableFactory.newSpannable(emojiText);
        Object value = entry.getValue();
        IconSet iconSet = (IconSet) value;
        if (iconSet.iconPng.exists()) {
          ImageSpan imageSpan = new ImageSpanAlignCenter(context,
              Uri.fromFile(((IconSet) value).iconPng));
          spannable.setSpan(imageSpan,
              matcher.start(), matcher.end(),
              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
          return spannable;
        }
      }
    }
    return null;
  }

  public static Spannable getGifSmiles(CharSequence text, TextView textView) {
    Spannable spannable = spannableFactory.newSpannable(text);
//        GlideImageGetter imageGetter = ImageTextUtil.getImageGetter(textView);
//        GifDrawableCallBack callBack = new GifDrawableCallBack(textView);
    for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
      Matcher matcher = entry.getKey().matcher(spannable);
      while (matcher.find()) {
        boolean set = true;
        for (ImageSpan span : spannable.getSpans(matcher.start(),
            matcher.end(), ImageSpan.class)) {
          if (spannable.getSpanStart(span) >= matcher.start()
              && spannable.getSpanEnd(span) <= matcher.end()) {
            spannable.removeSpan(span);
          } else {
            set = false;
            break;
          }
        }
        if (set) {
          Object value = entry.getValue();
          if (value instanceof IconSet) {
//                        Drawable gifDrawable = ImageTextUtil.getUrlDrawable(((IconSet) value).iconGif.getAbsolutePath(), textView);
//                        Drawable gifDrawable = imageGetter.getDrawable(((IconSet) value).iconGif.getAbsolutePath());
            try {
              ImageSpan imageSpan;
              GifDrawable gifDrawable1;
              if (gifDrawableHashMap.containsKey(((IconSet) value).iconGif.getAbsolutePath())) {
                gifDrawable1 = gifDrawableHashMap.get(((IconSet) value).iconGif.getAbsolutePath());
                imageSpan = new ImageSpanAlignCenter(gifDrawable1,
                    ((IconSet) value).iconGif.getAbsolutePath());

              } else {
                gifDrawable1 = new GifDrawable(((IconSet) value).iconGif);
//                            gifDrawable1.start();
                imageSpan = new ImageSpanAlignCenter(gifDrawable1,
                    ((IconSet) value).iconGif.getAbsolutePath());
                gifDrawableHashMap.put(((IconSet) value).iconGif.getAbsolutePath(), gifDrawable1);
              }
              spannable.setSpan(imageSpan,
                  matcher.start(), matcher.end(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (IOException e) {
              e.printStackTrace();
            }


          }
        }
      }
    }
    return spannable;
  }

  private static class GifDrawableCallBack implements Drawable.Callback {

    TextView textView;

    GifDrawableCallBack(TextView textView) {
      this.textView = textView;
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
      textView.invalidate();
      Log.i(TAG, "invalidateDrawable: ");
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {

    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {

    }
  }

  public static void clearGifMap() {
    gifDrawableHashMap.clear();
  }

  public static Spannable getPngSmiledText(Context context, CharSequence text) {
    Spannable spannable = spannableFactory.newSpannable(text);
//        addHttp(context, spannable);
    addSmiles(context, spannable);
    return spannable;
  }

  public static boolean containsKey(String key) {
    boolean b = false;
    for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
      Matcher matcher = entry.getKey().matcher(key);
      if (matcher.find()) {
        b = true;
        break;
      }
    }

    return b;
  }

  public static int getSmilesSize() {
    return emoticons.size();
  }


}
