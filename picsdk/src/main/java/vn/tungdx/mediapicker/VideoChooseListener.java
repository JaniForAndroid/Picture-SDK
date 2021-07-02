package vn.tungdx.mediapicker;

import android.net.Uri;

/**
 * Created by sunha on 2017/8/10 0010.
 */

public interface VideoChooseListener {

  int CANCEL = 1;
  int PARSER_FAIL = 2;
  int LESS_THAN_MIN = 3;

  void onVideoChoose(Uri videoUri, String path);

  void onError(int status, String message);

}
