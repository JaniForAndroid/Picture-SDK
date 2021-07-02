package com.chivox;

/**
 * Create time: 2017/4/19.
 */

public interface EngineCallback {

  void onVolumeChanged(int volume);

  void onEngineStartError(int code);

  void onRecordError(String error);

  void onEvalError(int errId, String error);

  void onResult(String result);
}
