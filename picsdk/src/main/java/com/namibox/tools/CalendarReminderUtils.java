package com.namibox.tools;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.text.TextUtils;
import com.namibox.util.Logger;
import com.namibox.util.Utils;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @Description 日历提醒工具类
 * @CreateTime: 2020/3/24 13:58
 * @Author: zhangkx
 */
public class CalendarReminderUtils {

  //日历相关URI
  private static String CALENDER_URL = "content://com.android.calendar/calendars";
  private static String CALENDER_EVENT_URL = "content://com.android.calendar/events";
  private static String CALENDER_REMINDER_URL = "content://com.android.calendar/reminders";

  // 日历账户相关
  private static String CALENDARS_NAME = "namibox";
  private static String CALENDARS_ACCOUNT_NAME = "admin@namibox.com";
  private static String CALENDARS_ACCOUNT_TYPE = "com.android.namibox";
  private static String CALENDARS_DISPLAY_NAME = "namibox";
  private static String calendarName;

  /**
   * 检查是否已经添加了日历账户，如果没有添加先添加一个日历账户再查询
   * 获取账户成功返回账户id，否则返回-1
   */
  private static int checkAndAddCalendarAccount(Context context) {
    calendarName = CALENDARS_NAME+ (Utils.isLogin(context) ? Utils.getLoginUserId(context) : "");
    int oldId = checkCalendarAccount(context);
    if( oldId >= 0 ){
      return oldId;
    }else{
      long addId = addCalendarAccount(context);
      if (addId >= 0) {
        return checkCalendarAccount(context);
      } else {
        return -1;
      }
    }

  }

  /**
   * 检查是否存在账户namibox，存在则返回账户id，否则返回-1
   */
  private static int checkCalendarAccount(Context context) {
    Cursor userCursor = context.getContentResolver().query(Uri.parse(CALENDER_URL), null, null, null, null);
    try {
      if (userCursor == null) { //查询返回空值
        return -1;
      }
      if (userCursor.getCount() > 0) {
        //查询日历账户中是否有namibox账户 有则返回该账户id
        for (userCursor.moveToFirst(); !userCursor.isAfterLast(); userCursor.moveToNext()) {
          if (TextUtils.equals(calendarName,userCursor.getString(userCursor.getColumnIndex(
              Calendars.NAME)))) {
            return userCursor.getInt(userCursor.getColumnIndex(Calendars._ID));
          }
        }

      }
    } finally {
      if (userCursor != null) {
        userCursor.close();
      }
    }
    return -1;
  }
  /**
   * 添加日历账户，账户创建成功则返回账户id，否则返回-1
   * 创建namibox独立账户 防止入侵本地账户
   */
  private static long addCalendarAccount(Context context) {
    TimeZone timeZone = TimeZone.getDefault();
    ContentValues value = new ContentValues();
    value.put(Calendars.NAME, calendarName);
    value.put(Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
    value.put(Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
    value.put(Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
    value.put(Calendars.VISIBLE, 1);
    value.put(Calendars.CALENDAR_COLOR, Color.BLUE);
    value.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
    value.put(Calendars.SYNC_EVENTS, 1);
    value.put(Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
    value.put(Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
    value.put(Calendars.CAN_ORGANIZER_RESPOND, 0);

    Uri calendarUri = Uri.parse(CALENDER_URL);
    calendarUri = calendarUri.buildUpon()
        .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
        .appendQueryParameter(Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
        .appendQueryParameter(Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
        .build();

    Uri result = context.getContentResolver().insert(calendarUri, value);
    long id = result == null ? -1 : ContentUris.parseId(result);
    return id;
  }

  /**
   * 添加日历提醒事件 事件两分钟后发生 提前一分钟提醒（一分钟后提醒该事件 测试/调试 时调用）
   * @param context
   * @param title
   * @param description
   */
  public static void addCalendarEvent(Context context, String title, String description){
    addCalendarEvent(context,title,description, System.currentTimeMillis() +  2 * 60 * 1000 ,1);
  }

  /**
   * 添加一个默认持续时间为10分钟的日历提醒事件
   * @param context
   * @param title
   * @param description
   * @param reminderTime 提醒时间（单位毫秒）
   * @param previousDate 提前多久提醒（单位  分钟）
   */
  public static void addCalendarEvent(Context context, String title, String description, long reminderTime, int previousDate) {
    addCalendarEvent(context,title,description,reminderTime ,10,previousDate);
  }

  /**
   *  添加日历事件
   * @param context
   * @param title
   * @param description
   * @param reminderTime 提醒时间（单位毫秒）
   * @param duration  持续事件 （单位毫秒）
   * @param previousMinutes 提前多久提醒（单位  分钟）
   */
  public static void addCalendarEvent(Context context, String title, String description,long reminderTime, long duration,int previousMinutes){
    if (context == null) {
      return;
    }
    int calId = checkAndAddCalendarAccount(context); //获取日历账户的id
    if (calId < 0) { //获取账户id失败直接返回，添加日历事件失败
      return;
    }

    //添加日历事件
    Calendar mCalendar = Calendar.getInstance();
    mCalendar.setTimeInMillis(reminderTime);//设置开始时间
    long start = mCalendar.getTime().getTime();
    mCalendar.setTimeInMillis(start + duration);//设置终止时间，开始时间加持续时间
    long end = mCalendar.getTime().getTime();
    ContentValues event = new ContentValues();
    event.put("title", title);
    event.put("description", description);
    event.put("calendar_id", calId); //插入账户的id
    event.put(CalendarContract.Events.DTSTART, start);
    event.put(CalendarContract.Events.DTEND, end);
    event.put(CalendarContract.Events.HAS_ALARM, 1);//设置有闹钟提醒
    event.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai");//这个是时区，必须有
    Uri newEvent = context.getContentResolver().insert(Uri.parse(CALENDER_EVENT_URL), event); //添加事件
    if (newEvent == null) { //添加日历事件失败直接返回
      return;
    }

    //事件提醒的设定
    ContentValues values = new ContentValues();
    values.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
    values.put(CalendarContract.Reminders.MINUTES, previousMinutes );// 提前previousMinutes分钟有提醒
    values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
    Uri uri = context.getContentResolver().insert(Uri.parse(CALENDER_REMINDER_URL), values);
    if(uri == null) { //添加事件提醒失败直接返回
      Logger.e("zkx 添加日历事件失败");
      return;
    }else{
//      Logger.e("zkx 添加日历事件成功");
    }
  }


  /**
   * 删除 namibox 账户 （清除所有日历提醒事件）
   * @param context
   */
  public static void deleteCalendarAccount(Context context){
    calendarName = CALENDARS_NAME+ (Utils.isLogin(context) ? Utils.getLoginUserId(context) : "");
    Cursor userCursor = context.getContentResolver().query(Uri.parse(CALENDER_URL), null, null, null, null);
    if (userCursor != null && userCursor.getCount() > 0) { //存在现有账户，取第一个账户的id返回
      for (userCursor.moveToFirst(); !userCursor.isAfterLast(); userCursor.moveToNext()) {
        if (TextUtils.equals(calendarName,userCursor.getString(userCursor.getColumnIndex(Calendars.NAME)))) {
          int rownum = context.getContentResolver().delete(Uri.parse(CALENDER_URL), "_id=" + userCursor.getInt(userCursor.getColumnIndex(
              Calendars._ID)), null);
          if(rownum == -1) {
            Logger.e("zkx 删除日历账户失败");
            return;
          }else{
            Logger.e("zkx 删除日历账户成功");
          }
        }

      }

    }
  }

  /**
   * 根据日历事件的title 删除该title对应的所有事件
   * @param context
   * @param title
   */
  public static void deleteCalendarEvent(Context context, String title) {
    if (context == null) {
      return;
    }
    Cursor eventCursor = context.getContentResolver().query(Uri.parse(CALENDER_EVENT_URL), null, null, null, null);
    try {
      if (eventCursor == null) { //查询返回空值
        return;
      }
      if (eventCursor.getCount() > 0) {
        //遍历所有事件，找到title跟需要查询的title一样的项
        for (eventCursor.moveToFirst(); !eventCursor.isAfterLast(); eventCursor.moveToNext()) {
          String eventTitle = eventCursor.getString(eventCursor.getColumnIndex("title"));
          if (!TextUtils.isEmpty(title) && title.equals(eventTitle)) {
            int id = eventCursor.getInt(eventCursor.getColumnIndex(Calendars._ID));//取得id
            Uri deleteUri = ContentUris.withAppendedId(Uri.parse(CALENDER_EVENT_URL), id);
            int rows = context.getContentResolver().delete(deleteUri, null, null);
            if (rows == -1) { //事件删除失败
              Logger.e("zkx 删除日历事件失败");
              return;
            }else{
              Logger.e("zkx 删除日历事件成功");
            }
          }
        }
      }
    } finally {
      if (eventCursor != null) {
        eventCursor.close();
      }
    }
  }
}
