package com.namibox.voice_engine_interface;

import android.content.Context;
import com.namibox.voice_engine_interface.VoiceEngineContext.VoiceEngineCallback;

/**
 * Create time: 2020/4/17.
 */
public interface VoiceEngineStrategy {

  int MSG_VOLUME = 0;
  int MSG_STOP = 1;
  int MSG_ERR = 2;
  int MSG_TIMEOUT = 3;
  int MSG_RECORD_STOP = 4;
  int MSG_EVAL_TIMEOUT = 5;


  void onInit(Context context);

  void onCreate(Context context, String serverType, String evalType, boolean isOralEval,
      String userId, VoiceEngineCallback callback);

  void onStart(String text, String wavPath, long timeOutMillis);

  void onSetServerType(int type);

  void onSetEvalType(String type);

  void onStop();

  void onCancel();

  void onRelease();
}
