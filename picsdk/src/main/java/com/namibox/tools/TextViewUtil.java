package com.namibox.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namibox.commonlib.fragment.AbsFragment;
import com.namibox.simplifyspan.SimplifySpanBuild;
import com.namibox.simplifyspan.customspan.CustomAbsoluteSizeSpan;
import com.namibox.simplifyspan.other.SpecialGravity;
import com.namibox.simplifyspan.unit.SpecialImageUnit;
import com.namibox.util.Utils;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.xml.sax.XMLReader;

/**
 * Create time: 2018/11/7.
 */
public class TextViewUtil {

  /***
   *  扩展方法 htmljson 进行点击事件添加
   * @param textView TextView目标控件
   * @param source htmljson 数据
   */
  public static void handleHtmlJson(AbsFragment absFragment, final TextView textView, String source) {
    if (TextUtils.isEmpty(source)) {
      textView.setText(null);
      return;
    }
    JsonObject jsonObject;
    try {
      jsonObject = new JsonParser().parse(source).getAsJsonObject();
    } catch (Exception e) {
      e.printStackTrace();
      textView.setText(null);
      return;
    }
    int line_space = jsonObject.has("line_space") ? jsonObject.get("line_space").getAsInt() : 0;
    if (line_space != 0) {
      textView.setLineSpacing(Utils.dp2px(textView.getContext(), line_space), 1f);
    }
    boolean center = jsonObject.has("center") && jsonObject.get("center").getAsBoolean();
    //字体大小这里要调整 这里使用新的 android_font_size 来设置字体大小取到就用 取不到就使用老字段的
    int font_size_all = jsonObject.has("android_font_size") ? jsonObject.get("android_font_size").getAsInt() :
        jsonObject.has("font_size") ? jsonObject.get("font_size").getAsInt() : 0;
//    int font_size_all = jsonObject.has("font_size") ? jsonObject.get("font_size").getAsInt() : 0;
    String color_all = jsonObject.has("color") ? jsonObject.get("color").getAsString() : null;
    String bg_color_all = jsonObject.has("bg_color") ? jsonObject.get("bg_color").getAsString() : null;
    String font_face_all = jsonObject.has("font_face") ? jsonObject.get("font_face").getAsString() : null;
    boolean b_all = jsonObject.has("b") && jsonObject.get("b").getAsBoolean();
    boolean it_all = jsonObject.has("i") && jsonObject.get("i").getAsBoolean();
    boolean u_all = jsonObject.has("u") && jsonObject.get("u").getAsBoolean();
    boolean del_all = jsonObject.has("del") && jsonObject.get("del").getAsBoolean();
    boolean sub_all = jsonObject.has("sub") && jsonObject.get("sub").getAsBoolean();
    boolean sup_all = jsonObject.has("sup") && jsonObject.get("sup").getAsBoolean();
    JsonArray contents = jsonObject.get("contents").getAsJsonArray();
    final SpannableStringBuilder sb = new SpannableStringBuilder();
    for (int i = 0; i < contents.size(); i++) {
      JsonObject jsonObj = (JsonObject) contents.get(i);
      String text = jsonObj.has("text") ? jsonObj.get("text").getAsString() : null;
//      int font_size = jsonObj.has("font_size") ? jsonObj.get("font_size").getAsInt() : 0;
      int font_size = jsonObj.has("android_font_size") ? jsonObj.get("android_font_size").getAsInt() :
          jsonObj.has("font_size") ? jsonObj.get("font_size").getAsInt() : 0;
      if (font_size == 0) {
        font_size = font_size_all;
      }
      String icon = jsonObj.has("icon") ? jsonObj.get("icon").getAsString() : null;
      if (!TextUtils.isEmpty(icon)) {
        sb.append(" ");
        final int start = sb.length() - 1;
        final int end = sb.length();
        RequestOptions options = new RequestOptions()
            .transform(new FillSpace(Utils.dp2px(textView.getContext(), font_size)))
            .diskCacheStrategy(DiskCacheStrategy.DATA);
        // TODO: 2021/6/29 暂时没用到，先不解决
//        GlideApp.with(textView.getContext()).asBitmap().load(icon).apply(options).into(new SimpleTarget<Bitmap>() {
//          @Override
//          public void onLoadFailed(@Nullable Drawable errorDrawable) {
//          }
//
//          @Override
//          public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
//            sb.setSpan(new CenteredImageSpan(textView.getContext(), resource),
//                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            textView.setText(sb);
//          }
//        });
        continue;
      }else if (TextUtils.isEmpty(icon) && TextUtils.isEmpty(text)) {
        continue;
      }
      sb.append(text);
      if (font_size > 0) {
        if (sb.length() > text.length() && center) {
          sb.setSpan(new CustomAbsoluteSizeSpan(text, text, font_size, textView, SpecialGravity.CENTER),
              sb.length() - text.length(), sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
          sb.setSpan(new AbsoluteSizeSpan(font_size, true), sb.length() - text.length(), sb.length(),
              Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
      }
      String color = jsonObj.has("color") ? jsonObj.get("color").getAsString() : null;
      if (!TextUtils.isEmpty(color)) {
        sb.setSpan(new ForegroundColorSpan(Color.parseColor(color)), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      } else if (!TextUtils.isEmpty(color_all)) {
        sb.setSpan(new ForegroundColorSpan(Color.parseColor(color_all)), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      String bg_color = jsonObj.has("bg_color") ? jsonObj.get("bg_color").getAsString() : null;
      if (!TextUtils.isEmpty(bg_color)) {
        sb.setSpan(new BackgroundColorSpan(Color.parseColor(bg_color)), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      } else if (!TextUtils.isEmpty(bg_color_all)) {
        sb.setSpan(new BackgroundColorSpan(Color.parseColor(bg_color_all)), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      String font_face = jsonObj.has("font_face") ? jsonObj.get("font_face").getAsString() : null;
      if (!TextUtils.isEmpty(font_face)) {
        sb.setSpan(new TypefaceSpan(font_face), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      } else if (!TextUtils.isEmpty(font_face_all)) {
        sb.setSpan(new TypefaceSpan(font_face_all), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      boolean bold = jsonObj.has("b") ? jsonObj.get("b").getAsBoolean() : b_all;
      boolean italic = jsonObj.has("i") ? jsonObj.get("i").getAsBoolean() : it_all;
      if (bold && italic) {
        sb.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      } else if (bold) {
        sb.setSpan(new StyleSpan(Typeface.BOLD), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      } else if (italic) {
        sb.setSpan(new StyleSpan(Typeface.ITALIC), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      boolean u = jsonObj.has("u") ? jsonObj.get("u").getAsBoolean() : u_all;
      if (u) {
        sb.setSpan(new UnderlineSpan(), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      boolean del = jsonObj.has("del") ? jsonObj.get("del").getAsBoolean() : del_all;
      if (del) {
        sb.setSpan(new StrikethroughSpan(), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      boolean sub = jsonObj.has("sub") ? jsonObj.get("sub").getAsBoolean() : sub_all;
      if (sub) {
        sb.setSpan(new SubscriptSpan(), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      boolean sup = jsonObj.has("sup") ? jsonObj.get("sup").getAsBoolean() : sup_all;
      if (sup) {
        sb.setSpan(new SuperscriptSpan(), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      //如果该text文本有action
      if (absFragment != null && jsonObj.has("action")) {
        JsonObject action = jsonObj.get("action").getAsJsonObject();
        setClickSpan(absFragment, sb, u, TextUtils.isEmpty(color) ? color_all : color, text, action);
      }
    }
    textView.setText(sb);
  }

  /***
   *  扩展方法 首行缩进
   * @param textView TextView目标控件
   * @param source htmljson 数据
   */
  public static void handleHtmlJson(AbsFragment absFragment, final TextView textView, String source, LeadingMarginSpan leadingMarginSpan) {
    if (TextUtils.isEmpty(source)) {
      textView.setText(null);
      return;
    }
    JsonObject jsonObject;
    try {
      jsonObject = new JsonParser().parse(source).getAsJsonObject();
    } catch (Exception e) {
      e.printStackTrace();
      textView.setText(null);
      return;
    }
    int line_space = jsonObject.has("line_space") ? jsonObject.get("line_space").getAsInt() : 0;
    if (line_space != 0) {
      textView.setLineSpacing(Utils.dp2px(textView.getContext(), line_space), 1f);
    }
    boolean center = jsonObject.has("center") && jsonObject.get("center").getAsBoolean();
    //字体大小这里要调整 这里使用新的 android_font_size 来设置字体大小取到就用 取不到就使用老字段的
    int font_size_all = jsonObject.has("android_font_size") ? jsonObject.get("android_font_size").getAsInt() :
        jsonObject.has("font_size") ? jsonObject.get("font_size").getAsInt() : 0;
//    int font_size_all = jsonObject.has("font_size") ? jsonObject.get("font_size").getAsInt() : 0;
    String color_all = jsonObject.has("color") ? jsonObject.get("color").getAsString() : null;
    String bg_color_all = jsonObject.has("bg_color") ? jsonObject.get("bg_color").getAsString() : null;
    String font_face_all = jsonObject.has("font_face") ? jsonObject.get("font_face").getAsString() : null;
    boolean b_all = jsonObject.has("b") && jsonObject.get("b").getAsBoolean();
    boolean it_all = jsonObject.has("i") && jsonObject.get("i").getAsBoolean();
    boolean u_all = jsonObject.has("u") && jsonObject.get("u").getAsBoolean();
    boolean del_all = jsonObject.has("del") && jsonObject.get("del").getAsBoolean();
    boolean sub_all = jsonObject.has("sub") && jsonObject.get("sub").getAsBoolean();
    boolean sup_all = jsonObject.has("sup") && jsonObject.get("sup").getAsBoolean();
    JsonArray contents = jsonObject.get("contents").getAsJsonArray();
    final SpannableStringBuilder sb = new SpannableStringBuilder();
    if (leadingMarginSpan.getLeadingMargin(true) > 0) {
      sb.append(" ");
      sb.setSpan(leadingMarginSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    for (int i = 0; i < contents.size(); i++) {
      JsonObject jsonObj = (JsonObject) contents.get(i);
      String text = jsonObj.has("text") ? jsonObj.get("text").getAsString() : null;
//      int font_size = jsonObj.has("font_size") ? jsonObj.get("font_size").getAsInt() : 0;
      int font_size = jsonObj.has("android_font_size") ? jsonObj.get("android_font_size").getAsInt() :
          jsonObj.has("font_size") ? jsonObj.get("font_size").getAsInt() : 0;
      if (font_size == 0) {
        font_size = font_size_all;
      }
      String icon = jsonObj.has("icon") ? jsonObj.get("icon").getAsString() : null;
      if (!TextUtils.isEmpty(icon)) {
        sb.append(" ");
        final int start = sb.length() - 1;
        final int end = sb.length();
        RequestOptions options = new RequestOptions()
            .transform(new FillSpace(Utils.dp2px(textView.getContext(), font_size)))
            .diskCacheStrategy(DiskCacheStrategy.DATA);
        // TODO: 2021/6/29 暂时没用到，先不解决
//        GlideApp.with(textView.getContext()).asBitmap().load(icon).apply(options).into(new SimpleTarget<Bitmap>() {
//          @Override
//          public void onLoadFailed(@Nullable Drawable errorDrawable) {
//          }
//
//          @Override
//          public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
//            sb.setSpan(new CenteredImageSpan(textView.getContext(), resource),
//                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            textView.setText(sb);
//          }
//        });
        continue;
      }
      sb.append(text);
      if (font_size > 0) {
        if (sb.length() > text.length() && center) {
          sb.setSpan(new CustomAbsoluteSizeSpan(text, text, font_size, textView, SpecialGravity.CENTER),
              sb.length() - text.length(), sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
          sb.setSpan(new AbsoluteSizeSpan(font_size, true), sb.length() - text.length(), sb.length(),
              Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
      }
      String color = jsonObj.has("color") ? jsonObj.get("color").getAsString() : null;
      if (!TextUtils.isEmpty(color)) {
        sb.setSpan(new ForegroundColorSpan(Color.parseColor(color)), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      } else if (!TextUtils.isEmpty(color_all)) {
        sb.setSpan(new ForegroundColorSpan(Color.parseColor(color_all)), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      String bg_color = jsonObj.has("bg_color") ? jsonObj.get("bg_color").getAsString() : null;
      if (!TextUtils.isEmpty(bg_color)) {
        sb.setSpan(new BackgroundColorSpan(Color.parseColor(bg_color)), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      } else if (!TextUtils.isEmpty(bg_color_all)) {
        sb.setSpan(new BackgroundColorSpan(Color.parseColor(bg_color_all)), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      String font_face = jsonObj.has("font_face") ? jsonObj.get("font_face").getAsString() : null;
      if (!TextUtils.isEmpty(font_face)) {
        sb.setSpan(new TypefaceSpan(font_face), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      } else if (!TextUtils.isEmpty(font_face_all)) {
        sb.setSpan(new TypefaceSpan(font_face_all), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      boolean bold = jsonObj.has("b") ? jsonObj.get("b").getAsBoolean() : b_all;
      boolean italic = jsonObj.has("i") ? jsonObj.get("i").getAsBoolean() : it_all;
      if (bold && italic) {
        sb.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      } else if (bold) {
        sb.setSpan(new StyleSpan(Typeface.BOLD), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      } else if (italic) {
        sb.setSpan(new StyleSpan(Typeface.ITALIC), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      boolean u = jsonObj.has("u") ? jsonObj.get("u").getAsBoolean() : u_all;
      if (u) {
        sb.setSpan(new UnderlineSpan(), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      boolean del = jsonObj.has("del") ? jsonObj.get("del").getAsBoolean() : del_all;
      if (del) {
        sb.setSpan(new StrikethroughSpan(), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      boolean sub = jsonObj.has("sub") ? jsonObj.get("sub").getAsBoolean() : sub_all;
      if (sub) {
        sb.setSpan(new SubscriptSpan(), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      boolean sup = jsonObj.has("sup") ? jsonObj.get("sup").getAsBoolean() : sup_all;
      if (sup) {
        sb.setSpan(new SuperscriptSpan(), sb.length() - text.length(), sb.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      //如果该text文本有action
      if (absFragment != null && jsonObj.has("action")) {
        JsonObject action = jsonObj.get("action").getAsJsonObject();
        setClickSpan(absFragment, sb, u, TextUtils.isEmpty(color) ? color_all : color, text, action);
      }
    }
    textView.setText(sb);
  }

  /** 添加点击事件 **/
  private static void setClickSpan(final AbsFragment absFragment, SpannableStringBuilder sb, final boolean u,
      final String color,
      String text, final JsonObject action) {
    ClickableSpan clickableSpan = new ClickableSpan() {
      @Override
      public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        //设置文件颜色
        if (TextUtils.isEmpty(color)) {
          ds.setColor(Color.parseColor("#333333"));
        } else {
          ds.setColor(Color.parseColor(color));
        }
        //设置下划线
        ds.setUnderlineText(u);
      }

      @Override
      public void onClick(@NonNull View widget) {
        absFragment.handleJsonMessage(action.toString());
      }
    };
    sb.setSpan(clickableSpan, sb.length() - text.length(), sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
  }

  public static void handleHtmlJson(final TextView textView, String source) {
    handleHtmlJson(null, textView, source);
  }

  public static Spanned handleHtml(String source) {
    if (TextUtils.isEmpty(source)) {
      return null;
    }
    return Html.fromHtml(source/*.replaceAll("font", "mfont")*/, null, new Html.TagHandler() {
      HashMap<String, Integer> startTag = new HashMap<>();
      final HashMap<String, String> attributes = new HashMap<>();

      @Override
      public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.equalsIgnoreCase("del")) {
          if (opening) {
            startTag.put(tag, output.length());
          } else {
            int endTag = output.length();
            output.setSpan(new StrikethroughSpan(), startTag.get(tag), endTag, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          }
        } else if (tag.equalsIgnoreCase("mfont")) {
          parseAttributes(xmlReader);
          if (opening) {
            startTag.put(tag, output.length());
          } else {
            int endTag = output.length();
            // 获取对应的属性值
            String color = attributes.get("color");
            String size = attributes.get("size");
            size = TextUtils.isEmpty(size) ? null : size.split("px")[0];

            // 设置颜色
            if (!TextUtils.isEmpty(color)) {
              output.setSpan(new ForegroundColorSpan(Color.parseColor(color)), startTag.get(tag), endTag,
                  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            // 设置字体大小
            if (!TextUtils.isEmpty(size)) {
              output.setSpan(new AbsoluteSizeSpan(Integer.parseInt(size), true), startTag.get(tag), endTag,
                  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
          }
        }
      }

      /**
       * 解析所有属性值
       *
       * @param xmlReader
       */
      private void parseAttributes(final XMLReader xmlReader) {
        try {
          Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
          elementField.setAccessible(true);
          Object element = elementField.get(xmlReader);
          Field attsField = element.getClass().getDeclaredField("theAtts");
          attsField.setAccessible(true);
          Object atts = attsField.get(element);
          Field dataField = atts.getClass().getDeclaredField("data");
          dataField.setAccessible(true);
          String[] data = (String[]) dataField.get(atts);
          Field lengthField = atts.getClass().getDeclaredField("length");
          lengthField.setAccessible(true);
          int len = (Integer) lengthField.get(atts);

          for (int i = 0; i < len; i++) {
            attributes.put(data[i * 5 + 1], data[i * 5 + 4]);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public static void setSpecialText(final Context context, final TextView textView, String icon, final String text) {
    textView.setText(text);
    RequestOptions options = new RequestOptions()
        .transform(new FillSpace(getFontHeight(textView.getTextSize(), text) - 3))//比文字稍微小点
        .diskCacheStrategy(DiskCacheStrategy.DATA);
    // TODO: 2021/6/29 暂时没用到，先不解决
//    GlideApp.with(context).asBitmap().load(icon).apply(options).into(new SimpleTarget<Bitmap>() {
//      @Override
//      public void onLoadFailed(@Nullable Drawable errorDrawable) {
//      }
//
//      @Override
//      public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
//        SimplifySpanBuild ssb = new SimplifySpanBuild(context, textView, text);
//        ssb.appendSpecialUnitToFirst(new SpecialImageUnit(resource).setGravity(SpecialGravity.CENTER));
//        textView.setText(ssb.build());
//      }
//    });
  }

  private static int getFontHeight(float textSize, String text) {
    Paint paint = new Paint();
    paint.setTextSize(textSize);
    Rect rect = new Rect();
    paint.getTextBounds(text, 0, text.length(), rect);
    return rect.height();
  }

  /**
   * 格式化时间
   *
   * @param timeMillis 要格式的时间毫秒值
   * @param tag 格式化格式 eg: yyyy-MM-dd or yyyy.MM.dd HH:mm:ss
   * @return 格式化后的时间串
   */
  @SuppressLint("SimpleDateFormat")
  public static String dateForamt(long timeMillis, String tag) {
    SimpleDateFormat format = new SimpleDateFormat(tag);
    Date date = new Date(timeMillis);
    String dayStr = format.format(date);
    return dayStr;
  }

  /**
   * 空字符串非null处理
   */
  public static String strFormat(String str) {
    if ("".equals(str) || null == str) {
      return "";
    }
    return str;
  }

  /**
   * 判断字符串是否为空
   */
  public static boolean emptyStr(String str) {
    return "".equals(str) || null == str;
  }
}
