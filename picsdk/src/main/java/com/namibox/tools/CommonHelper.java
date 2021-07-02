package com.namibox.tools;

import android.content.Context;

/**
 * Created by sunha on 2017/12/23 0023.
 */

public class CommonHelper {

  private volatile static CommonHelper instance;
  private Context context;

  private CommonHelper() {
  }

  public static CommonHelper getInstance() {
    if (instance == null) {
      synchronized (CommonHelper.class) {
        if (instance == null) {
          instance = new CommonHelper();
        }
      }
    }
    return instance;
  }

  public void init(Context context) {
    this.context = context.getApplicationContext();
  }

  public Context getContext() {
    return context;
  }
}
