package sdk;

import android.support.annotation.Keep;
import sdk.model.DubbingResultBean;

@Keep
public class DubbingResultEvent {
  public DubbingResultBean dubbingResultBean;

  public DubbingResultEvent(DubbingResultBean dubbingResultBean) {
    this.dubbingResultBean = dubbingResultBean;
  }
}
