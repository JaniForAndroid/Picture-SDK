package sdk.callback;

import android.support.annotation.Keep;

@Keep
public interface AnalyzeAuthCallback {

  void onSuccess();

  void onLogin();

  void onOtherError(String code);
}
