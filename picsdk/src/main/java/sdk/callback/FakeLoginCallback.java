package sdk.callback;

import android.support.annotation.Keep;

@Keep
public interface FakeLoginCallback {
  void onSuccess();
  void onFail(Throwable t);
}
