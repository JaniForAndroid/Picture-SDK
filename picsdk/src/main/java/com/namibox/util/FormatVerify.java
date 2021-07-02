package com.namibox.util;

import android.util.Patterns;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create time: 2018/5/29.
 */
public class FormatVerify {

  public static boolean checkHexColor(String colorStr) {
    boolean flag = false;
    try {
      Pattern pattern = Pattern.compile("^#([0-9a-fA-F]{8}|[0-9a-fA-F]{6}|[0-9a-fA-F]{3})$");
      Matcher matcher = pattern.matcher(colorStr);
      flag = matcher.matches();
    } catch (Exception e) {

    }
    return flag;
  }

  public static boolean checkPhone(String phoneNum) {
    if (phoneNum == null || "".equals(phoneNum)) {
      return false;
    }

    Pattern p = Pattern.compile("^([1])([0-9]{10})$");
    Matcher m = p.matcher(phoneNum);
    return m.matches();
  }

  public static boolean checkUrl(String url){
    return Patterns.WEB_URL.matcher(url).matches();
  }
}
