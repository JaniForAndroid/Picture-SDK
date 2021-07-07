package com.namibox.picture_sdk;

import android.app.Application;
import sdk.NBPictureSDK;

public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    NBPictureSDK.init(this,BuildConfig.DEBUG);
  }
}
