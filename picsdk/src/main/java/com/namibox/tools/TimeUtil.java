package com.namibox.tools;

import android.content.Context;
import android.text.TextUtils;
import com.example.picsdk.R;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

  public static String getFormatTimeDHMS(long timeMillis) {
    timeMillis /= 1000;
    int days = (int) (timeMillis / (24 * 60 * 60));
    int leftSeconds = (int) (timeMillis % (24 * 60 * 60));
    int hours = leftSeconds / (60 * 60);
    leftSeconds = leftSeconds % (60 * 60);
    int minutes = leftSeconds / 60;
    int seconds = leftSeconds % 60;
    return addZeroPrefix(days) + ":" + addZeroPrefix(hours) + ":" + addZeroPrefix(minutes) + ":" + addZeroPrefix(
        seconds);
  }

  public static String getFormatTimeHMS(long timeMillis) {
    timeMillis /= 1000;
    int hours = (int) (timeMillis / (60 * 60));
    int leftSeconds = (int) (timeMillis % (60 * 60));
    int minutes = leftSeconds / 60;
    int seconds = leftSeconds % 60;
    return addZeroPrefix(hours) + ":" + addZeroPrefix(minutes) + ":" + addZeroPrefix(seconds);
  }

  public static String getFormatTimeMS(long timeMillis) {
    timeMillis /= 1000;
    int leftSeconds = (int) (timeMillis % (60 * 60));
    int minutes = leftSeconds / 60;
    int seconds = leftSeconds % 60;
    return addZeroPrefix(minutes) + ":" + addZeroPrefix(seconds);
  }

  private static String addZeroPrefix(int time) {
    if (time < 10) {
      return "0" + time;
    }
    return time + "";
  }

  public static long getTimeMillis(String date) {
    if (TextUtils.isEmpty(date)) {
      return System.currentTimeMillis();
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    try {
      long timeMillis = simpleDateFormat.parse(date).getTime();
      return timeMillis;
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return 0;
  }

  public static String getDate(long timeMillis) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date;
    date = simpleDateFormat.format(new Date(timeMillis));
    return date;
  }

  public static long getBlurTimeMillis(String date) {
    if (TextUtils.isEmpty(date)) {
      return System.currentTimeMillis();
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      long timeMillis = simpleDateFormat.parse(date).getTime();
      return timeMillis / 1000 * 1000;
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * 获取凌晨时间戳
   */
  public static long getDawnTimeMillis() {

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    return calendar.getTimeInMillis() / 1000 * 1000;
  }

  public static boolean compareDate(String big, String small) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
    try {
      Date bigDate = dateFormat.parse(big);
      Date smallDate = dateFormat.parse(small);
      if (bigDate.getTime() >= smallDate.getTime()) {
        return true;
      }
      return false;
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static String getDateString(Date date) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    return simpleDateFormat.format(date);
  }

  public static String getGMT8DawnString(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
    return simpleDateFormat.format(calendar.getTime());
  }

  public static String convertGMT8String(String time) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
    try {
      Date date = simpleDateFormat.parse(time);
      SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
      return simpleDateFormat2.format(date);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return time;
  }

  public static String switchDayOfWeek(int index, Context context) {
    switch (index) {
      case 1:
        return context.getString(R.string.common_sunday);
      case 2:
        return context.getString(R.string.common_monday);
      case 3:
        return context.getString(R.string.common_tuesday);
      case 4:
        return context.getString(R.string.common_wednesday);
      case 5:
        return context.getString(R.string.common_thursday);
      case 6:
        return context.getString(R.string.common_friday);
      case 7:
        return context.getString(R.string.common_saturday);
    }
    return "";
  }

  public static int getIndexOfWeek(String week) {
    switch (week) {
      case "周日":
        return 1;
      case "周一":
        return 2;
      case "周二":
        return 3;
      case "周三":
        return 4;
      case "周四":
        return 5;
      case "周五":
        return 6;
      case "周六":
        return 7;
      default:
        break;
    }
    return 0;
  }

  public static String getMonthString(int month) {
    switch (month) {
      case 0:
        return "Jan";
      case 1:
        return "Feb";
      case 2:
        return "Mar";
      case 3:
        return "Apr";
      case 4:
        return "May";
      case 5:
        return "June";
      case 6:
        return "July";
      case 7:
        return "Aug";
      case 8:
        return "Sept";
      case 9:
        return "Oct";
      case 10:
        return "Nov";
      case 11:
        return "Dec";
      default:
        return "";
    }
  }
}
