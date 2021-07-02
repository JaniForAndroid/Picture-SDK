package sdk.callback;

import android.support.annotation.Keep;
import sdk.model.DubbingResultBean;

@Keep
public interface RegisterSDKInterface {

  void onDubbingResult(DubbingResultBean result);

  void onError(Throwable throwable, String code);
}
