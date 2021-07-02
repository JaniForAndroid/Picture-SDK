package sdk.callback;


import android.support.annotation.Keep;
import java.util.List;
import sdk.model.PictureBean;
import sdk.model.PictureDetailBean;

@Keep
public interface GetPictureInfoCallback{
  void onSuccess(List<PictureBean> list);
  void onDetailSuccess(List<PictureDetailBean> list);
  void onError();
}
