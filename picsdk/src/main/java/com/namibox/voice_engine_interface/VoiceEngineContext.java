package com.namibox.voice_engine_interface;

import android.content.Context;

/**
 * Create time: 2020/4/17.
 */
public class VoiceEngineContext {
  public static final String EVAL_TYPE_SENT_EN = "phrases";//英句
  public static final String EVAL_TYPE_SENT_CN = "phrases_cn";//中句+段
  public static final String EVAL_TYPE_PRED_EN = "paragraph";//英段落
  public static final String EVAL_TYPE_WORD_EN = "word";//英单词
  public static final String EVAL_TYPE_WORD_CN = "word_cn";//中单词
  public static final int SERVER_TYPE_ONLINE = 0;
  public static final int SERVER_TYPE_OFFLINE = 1;
  public static final int SERVER_TYPE_MIX = 2;
  public static final String TYPE_ONLINE_CS = "online_cs";
  public static final String TYPE_ONLINE_XF = "online_xf";
  public static final String TYPE_ONLINE_XS = "online_xs";
  public static final String TYPE_OFFLINE_XS = "offline_xs";
  public static final String TYPE_MIX_XS = "mix_xs";
  private VoiceEngineStrategy strategy;
  private VoiceEngineCallback callback;

  public interface VoiceEngineCallback {
    void onInitResult(boolean success, int code, String msg);
    void onVolume(int volume);
    void onResult(Object result);
    void onCanceled();
    void onRecordStop();
    void onEvalTimeout();
    void onEvalErr(int errCode, String errMsg);
  }

  public VoiceEngineContext(String engineType) throws Exception {
    String clsName;
    if (TYPE_ONLINE_XS.equals(engineType) || TYPE_OFFLINE_XS.equals(engineType) || TYPE_MIX_XS.equals(engineType)) {
      clsName = "com.chivox.VoiceEngineXS";
    } else if (TYPE_ONLINE_CS.equals(engineType)) {
      clsName = "com.chivox.VoiceEngineCS";
    } else if (TYPE_ONLINE_XF.equals(engineType)) {
      clsName = "com.namibox.xunfei.VoiceEngineXF";
    } else {
      throw new IllegalStateException("未实现该引擎策略");
    }
    Class<?> cls = Class.forName(clsName);
    boolean hasInterface = false;
    for (Class<?> intrface : cls.getInterfaces()) {
      if (intrface != null && VoiceEngineStrategy.class.isAssignableFrom(intrface)) {
        hasInterface = true;
        break;
      }
    }
    if (!hasInterface) {
      throw new IllegalStateException("引擎策略未实现VoiceEngineStrategy接口");
    }
    strategy = (VoiceEngineStrategy) cls.newInstance();
  }

  public void init(Context context) {
    strategy.onInit(context);
  }

  public void create(
      Context context, String serverType, String evalType, boolean isOralEval, String userId, VoiceEngineCallback callback) {
    strategy.onCreate(context, serverType, evalType, isOralEval, userId, callback);
  }

  public void setServerType(int type) {
    strategy.onSetServerType(type);
  }

  public void setEvalType(String evalType) {
    strategy.onSetEvalType(evalType);
  }

  public void start(String text) {
    startEngine(text, null, -1);
  }

  public void startEngine(String text, String wavFilePath, long timeOutMillis) {
    strategy.onStart(text, wavFilePath, timeOutMillis);
  }

  public void stop() {
    strategy.onStop();
  }

  public void cancel() {
    strategy.onCancel();
  }

  public void release() {
    strategy.onRelease();
  }
}
