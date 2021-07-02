package com.namibox.commonlib.lockscreen;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class LockScreenService extends Service {

  private LockScreenReceiver mLockScreenReceiver;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    mLockScreenReceiver = new LockScreenReceiver();
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_SCREEN_OFF);
    filter.addAction(Intent.ACTION_USER_PRESENT);

    registerReceiver(mLockScreenReceiver, filter);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterReceiver(mLockScreenReceiver);
  }
}
