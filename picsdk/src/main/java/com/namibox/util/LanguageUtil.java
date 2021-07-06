package com.namibox.util;

import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;

/**
 * author : feng
 * creation time : 20-8-10下午6:07
 */
public class LanguageUtil {

  public static String getLanguage(Context context) {
    Configuration configuration = context.getResources().getConfiguration();
    String language = configuration.locale.getLanguage();
    String country = configuration.locale.getCountry().toLowerCase();
    String countryDisplayName = configuration.locale.getDisplayCountry();
    if ("en".equals(language)) {
      return "en";
    }
    if ("zh".equals(language)) {
      if ("tw".equals(country) || "hk".equals(country)|| "mo".equals(country) || "中國".equals(countryDisplayName)) {
        return "zh-hk";
      } else {
        return "zh";
      }
    }
    return "zh";
  }

  public static boolean isForeign(Context context) {
    return !"zh".equals(getLanguage(context));
  }

  public static String getForeignGroup(String name) {
    String foreign;
    switch (name) {
      case "翼蝶":
        foreign = "Tiger";
        break;
      case "珊瑚":
        foreign = "Bear";
        break;
      case "孔雀":
        foreign = "Zebra";
        break;
      case "留鸟":
        foreign = "Dog";
        break;
      case "海星":
        foreign = "Eagle";
        break;
      case "白鲨":
        foreign = "Deer";
        break;
      case "狮子":
        foreign = "Otter";
        break;
      case "琴鸟":
        foreign = "Lion";
        break;
      case "海鱼":
        foreign = "Wolf";
        break;
      case "白鹤":
        foreign = "Puma";
        break;
      case "飞鱼":
        foreign = "Cat";
        break;
      case "扇贝":
        foreign = "Fox";
        break;
      case "恐龙":
        foreign = "Swan";
        break;
      case "雪豹":
        foreign = "Seal";
        break;
      default:
        foreign = name;
        break;
    }
    return foreign;
  }
}
