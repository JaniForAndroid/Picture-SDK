package sdk.callback;


import android.support.annotation.Keep;

@Keep
public interface CleanCacheCallback {

  void onBefore();

  void onSuccess();

  void onFail(Throwable t);
}
