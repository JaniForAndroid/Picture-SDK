package com.namibox.commonlib.common;

import android.content.Context;
import com.namibox.util.network.NetWorkHelper;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Create time: 2015/7/23.
 */
public class ApiHandler {

  private static volatile OkHttpClient internalClient;

  private static OkHttpClient getInternalClient() {
    if (internalClient == null) {
      synchronized (ApiHandler.class) {
        if (internalClient == null) {
          internalClient = NetWorkHelper.getOkHttpClient();
        }
      }
    }
    return internalClient;
  }

  public static <T> T getBaseApi(OkHttpClient okHttpClient, Class<T> cls) {
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(NetWorkHelper.getInstance().getBaseUrl())
        .addConverterFactory(new NullOnEmptyConverterFactory())
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(okHttpClient)
        .build();
    return retrofit.create(cls);
  }

  public static <T> T getBaseApi(Class<T> cls) {
    return getBaseApi(getInternalClient(), cls);
  }

  public static Api getBaseApi() {
    return getBaseApi(Api.class);
  }

  public static Api getBaseApi(Context context) {
    return getBaseApi();
  }
}
