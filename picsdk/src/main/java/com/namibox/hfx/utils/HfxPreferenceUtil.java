package com.namibox.hfx.utils;

import android.content.Context;
import com.namibox.util.PreferenceUtil;

/**
 * Created by sunha on 2017/8/9 0009.
 */

public class HfxPreferenceUtil {

  public static final String VIDEO_COVER_TIME = "video_cover_time";
  public static final String BOOK_INTRODUCE = "book_introduce";

  public static int getLastRead(Context context, String bookId) {
    return PreferenceUtil.get(context).getSP("book_last_read").getInt(bookId, 0);
  }

  public static void saveLastRead(Context context, String bookId, int value) {
    PreferenceUtil.get(context).getSP("book_last_read").edit().putInt(bookId, value).apply();
  }

  public static String getRecordBookUrl(Context context, String bookId) {
    return PreferenceUtil.get(context).getSP("record_book_url").getString(bookId, "");
  }

  public static void saveRecordBookUrl(Context context, String bookId, String url) {
    PreferenceUtil.get(context).getSP("record_book_url").edit().putString(bookId, url).apply();

  }

  public static int getVideoCoverTime(Context context, String videoId) {
    return PreferenceUtil.get(context).getSP(VIDEO_COVER_TIME).getInt(videoId, 500);
  }

  public static void saveVideoCoverTime(Context context, String videoId, int time) {
    PreferenceUtil.get(context).getSP(VIDEO_COVER_TIME).edit().putInt(videoId, time).apply();

  }

  public static String getBookIntroduce(Context context, String userId, String bookId) {
    return PreferenceUtil.get(context).getSP(BOOK_INTRODUCE).getString(userId + "_" + bookId, "");
  }

  public static void saveBookIntroduce(Context context, String userId, String bookId, String introduce) {
    PreferenceUtil
        .get(context).getSP(BOOK_INTRODUCE).edit().putString(userId + "_" + bookId, introduce).apply();

  }

  public static boolean isRecordBookInWork(Context context, String userId, String bookId) {
    return PreferenceUtil
        .get(context).getSP("record_book_state").getBoolean(userId + "_" + bookId, false);

  }

  public static boolean isRecordBookInWorkDefTrue(Context context, String userId, String bookId) {
    return PreferenceUtil
        .get(context).getSP("record_book_state").getBoolean(userId + "_" + bookId, true);

  }

  /**
   * 作品删除时设置为true不影响正常的制作，因为这个制作时值是在check的时候取的，check到作品没有提交，就可以直接进行下一步
   * 在绘本退出保存时它又会被设成false，使得制作中可以看到该作品
   * @param context
   * @param userId
   * @param bookId
   * @param value
   */
  public static void setRecordBookInWork(Context context, String userId, String bookId, boolean value) {
    PreferenceUtil
        .get(context).getSP("record_book_state").edit().putBoolean(userId + "_" + bookId, value).apply();
  }
}
