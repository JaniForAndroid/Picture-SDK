package sdk.callback;

import android.support.annotation.Keep;

@Keep
public interface AccessTokenCallback {

  void onResult(String token);

  void onError(String msg);
}

