package com.namibox.commonlib.audioplay;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.example.picsdk.R;

public class AudioPlayUtils {

  public static void initNotificationAudioPlay(Context context, boolean isPlay,
      boolean previousEnabled, boolean nextEnabled, Bitmap bitmap, String audioName) {
    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(context.NOTIFICATION_SERVICE);
    Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.setAction(Intent.ACTION_MAIN);
    String destCls = ((Activity) context).getLocalClassName();
    if (!destCls.contains("booksdk")) {
      destCls = context.getPackageName() + "." + destCls;
    }
    intent.setComponent(new ComponentName(context.getPackageName(), destCls));
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
    RemoteViews normalRemoteViews = getRemoteViews(R.layout.notice_normal_audio_play, context, isPlay, previousEnabled,
        nextEnabled, bitmap, audioName);
    RemoteViews bigRemoteViews = getRemoteViews(R.layout.notice_big_audio_play, context, isPlay, previousEnabled,
        nextEnabled, bitmap, audioName);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel("namibox1", "namibox1", NotificationManager
          .IMPORTANCE_DEFAULT);
      channel.setSound(null, null);
      builder.setChannelId("namibox1");
      notificationManager.createNotificationChannel(channel);
    }
    builder.setContentIntent(pi);
    builder.setCustomContentView(normalRemoteViews);
    builder.setCustomBigContentView(bigRemoteViews);
    builder.setOngoing(true);
    builder.setSmallIcon(R.drawable.ic_notification);
    builder.setPriority(NotificationCompat.PRIORITY_MAX);
    Notification notification = builder.build();
    notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
    notificationManager.notify(10, notification);
  }

  public static void clearNotification(Context context) {
    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(context.NOTIFICATION_SERVICE);
    notificationManager.cancel(10);
  }

  private static RemoteViews getRemoteViews(int resId, Context context, boolean isPlay,
      boolean previousEnabled, boolean nextEnabled, Bitmap bitmap, String audioName) {
    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), resId);
    if (bitmap != null) {
      remoteViews.setImageViewBitmap(R.id.iv_cover, cropBitmap(bitmap));
    }
    remoteViews
        .setImageViewResource(R.id.iv_play_pause, isPlay ? R.drawable.ic_notify_pause : R.drawable.ic_notify_play);
    if (previousEnabled) {
      Intent intentForward = new Intent(AudioPlayReceiver.PREVIOUS);
      PendingIntent piForward = PendingIntent
          .getBroadcast(context, 100, intentForward, PendingIntent.FLAG_UPDATE_CURRENT);
      remoteViews.setOnClickPendingIntent(R.id.iv_previous, piForward);
    } else {
      remoteViews.setImageViewResource(R.id.iv_previous, R.drawable.ic_notify_previous_unenabled);
    }
    Intent intentPlayPause = new Intent(AudioPlayReceiver.PLAY_PAUSE);
    PendingIntent piPlayPause = PendingIntent
        .getBroadcast(context, 100, intentPlayPause, PendingIntent.FLAG_UPDATE_CURRENT);
    remoteViews.setOnClickPendingIntent(R.id.iv_play_pause, piPlayPause);
    if (nextEnabled) {
      Intent intentNext = new Intent(AudioPlayReceiver.NEXT);
      PendingIntent piNext = PendingIntent
          .getBroadcast(context, 100, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
      remoteViews.setOnClickPendingIntent(R.id.iv_next, piNext);
    } else {
      remoteViews.setImageViewResource(R.id.iv_next, R.drawable.ic_notify_next_unenabled);
    }
    Intent intentClear = new Intent(AudioPlayReceiver.CLEAR);
    PendingIntent piClear = PendingIntent
        .getBroadcast(context, 100, intentClear, PendingIntent.FLAG_UPDATE_CURRENT);
    remoteViews.setOnClickPendingIntent(R.id.iv_close, piClear);
    if (!TextUtils.isEmpty(audioName)) {
      remoteViews.setTextViewText(R.id.tv_audio_name, audioName);
    }
    return remoteViews;
  }

  private static Bitmap cropBitmap(Bitmap bitmap) {
    int minLength = bitmap.getHeight() > bitmap.getWidth() ? bitmap.getWidth() : bitmap.getHeight();
    return Bitmap.createBitmap(bitmap, 0, 0, minLength, minLength, null, false);
  }

}
