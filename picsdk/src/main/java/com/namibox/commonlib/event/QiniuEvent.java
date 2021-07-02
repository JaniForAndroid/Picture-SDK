package com.namibox.commonlib.event;


import com.qiniu.android.http.ResponseInfo;
import org.json.JSONObject;

/**
 * Created by sunha on 2017/5/17 0017.
 */

public class QiniuEvent {

  public int type;
  public double progress;
  public String key;
  public ResponseInfo info;
  public JSONObject response;
  public String message;

  public QiniuEvent(String key, ResponseInfo info, JSONObject response) {
    this.type = QiniuEventType.RESULT;
    this.key = key;
    this.info = info;
    this.response = response;
  }

  public QiniuEvent(double progress) {
    this.type = QiniuEventType.PROGRESS;
    this.progress = progress;
  }

  public class QiniuEventType {

    public static final int PROGRESS = 0;
    public static final int RESULT = 1;
    public static final int ERROR = 2;
  }

}

