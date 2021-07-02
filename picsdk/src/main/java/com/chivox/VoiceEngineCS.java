package com.chivox;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import com.namibox.util.FileUtil;
import com.namibox.util.Utils;
import com.namibox.voice_engine_interface.EngineUtils;
import com.namibox.voice_engine_interface.VoiceEngineContext.VoiceEngineCallback;
import com.namibox.voice_engine_interface.VoiceEngineStrategy;
import java.io.File;
import java.util.ArrayList;

/**
 * Create time: 2020/4/17.
 */
public class VoiceEngineCS implements VoiceEngineStrategy {

  private AIEngineSingleton engineSingleton;//驰声引擎
  private boolean csEngineInited;
  private String wavPath;
  private Context context;
  private String evalType;
  private String userId;
  private Handler messageHandler;
  private VoiceEngineCallback voiceEngineCallback;
  private boolean isOralEval;

  @Override
  public void onInit(Context context) {

  }

  @Override
  public void onCreate(Context context, String serverType, String evalType, boolean isOralEval,
      String userId, final VoiceEngineCallback callback) {
    this.context = context;
    this.evalType = evalType;
    this.userId = userId;
    this.voiceEngineCallback = callback;
    this.isOralEval = isOralEval;
    messageHandler = new Handler(messageCallback);
    if (!csEngineInited) {
      engineSingleton = AIEngineSingleton.getInstance();
      engineSingleton.init(context.getApplicationContext(), new InitCallback() {
        @Override
        public void onCompleted() {
          csEngineInited = true;
          if (voiceEngineCallback != null) {
            voiceEngineCallback.onInitResult(true, 0, null);
          }
        }

        @Override
        public void onError() {
          if (voiceEngineCallback != null) {
            voiceEngineCallback.onInitResult(false, -1, null);
          }
        }
      });
    }
  }

  @Override
  public void onStart(String text, String path, long timeOutMillis) {
    if (path == null) {
      wavPath = EngineUtils.initWavPath(context);
    } else {
      wavPath = path;
    }
    engineSingleton.startEngine(text, evalType, wavPath, userId, engineCallback);
    if (timeOutMillis > 0) {
      if ("paragraph".equals(evalType)) {
        timeOutMillis = 119 * 1000;
      }
      messageHandler.removeMessages(MSG_TIMEOUT);
      messageHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, timeOutMillis);
    }
  }

  @Override
  public void onSetEvalType(String type) {
    this.evalType = type;
  }

  @Override
  public void onSetServerType(int type) {

  }


  private EngineCallback engineCallback = new EngineCallback() {
    @Override
    public void onVolumeChanged(int volume) {
      Message msg = messageHandler.obtainMessage(MSG_VOLUME);
      msg.arg1 = volume;
      messageHandler.sendMessage(msg);
    }

    @Override
    public void onEngineStartError(int code) {
      Message msg = messageHandler.obtainMessage(MSG_ERR);
      msg.obj = "引擎启动失败，请退出并重试";
      msg.arg1 = code;
      messageHandler.sendMessage(msg);
      FileUtil.deleteDir(AIEngineHelper.getFilesDir(context));
    }

    @Override
    public void onRecordError(String error) {
      Message msg = messageHandler.obtainMessage(MSG_ERR);
      msg.obj = "录音失败！请检查录音权限是否开启，或者录音系统是否被其他应用占用，或重启应用";
      messageHandler.sendMessage(msg);
    }

    @Override
    public void onEvalError(int errId, String error) {
      messageHandler.removeMessages(MSG_TIMEOUT);
      if (isOralEval && errId == 40092) {
        Utils.toast(context, "评测超时，已自动停止评测");
      } else {
        Message msg = messageHandler.obtainMessage(MSG_ERR);
        msg.obj = error;
        msg.arg1 = errId;
        messageHandler.sendMessage(msg);
      }
      if (errId == 60015) {
        FileUtil.deleteDir(AIEngineHelper.getFilesDir(context));
      }
    }

    @Override
    public void onResult(String result) {
      Message msg = messageHandler.obtainMessage(MSG_STOP);
      msg.obj = result;
      messageHandler.sendMessage(msg);
    }
  };

  private void handleResult(String result) {
    if (voiceEngineCallback == null) {
      return;
    }
    if (TextUtils.isEmpty(result)) {
      EvalResult evalResult = new EvalResult();
      evalResult.result_type = "exception";
      voiceEngineCallback.onResult(evalResult);
    } else {
      ChiShengResult r;
      ChiShengPhrasesResult phrasesResult;
      if ("word".equals(evalType)) {
        ChiShengWordResult wordResult = Utils.parseJsonString(result, ChiShengWordResult.class);
        if (wordResult != null && wordResult.getResult() != null) {
          EvalResult evalResult = new EvalResult();
          evalResult.result_type = "success";
          evalResult.score = wordResult.getResult().getOverall();
          evalResult.pron = wordResult.getResult().getPron();
          evalResult.fluency = wordResult.getResult().getOverall();
          evalResult.integrity = wordResult.getResult().getOverall();
          if (!TextUtils.isEmpty(wordResult.getAudioUrl())) {
            if (wordResult.getAudioUrl().startsWith("http")) {
              evalResult.url = wordResult.getAudioUrl() + ".mp3";
            } else {
              evalResult.url = "http://" + wordResult.getAudioUrl() + ".mp3";
            }
          }
          evalResult.localpath = Uri.fromFile(new File(wavPath)).toString();
          if (wordResult.getResult().getDetails() != null && !wordResult.getResult()
              .getDetails().isEmpty()) {
            evalResult.detail = new ArrayList<>();
            for (ChiShengWordResult.ResultBean.DetailsBean detailsBean : wordResult.getResult()
                .getDetails()) {
              EvalResult.Detail detail = new EvalResult.Detail();
              detail.word = detailsBean.getCharX();
              detail.score = String.valueOf(detailsBean.getScore());
              evalResult.detail.add(detail);
            }
          }
          voiceEngineCallback.onResult(evalResult);
        } else {
          EvalResult evalResult = new EvalResult();
          evalResult.result_type = "exception";
          voiceEngineCallback.onResult(evalResult);
        }
      } else if (evalType.equals("phrases")) {
        phrasesResult = Utils.parseJsonString(result, ChiShengPhrasesResult.class);
        if (phrasesResult != null && phrasesResult.result != null) {
          EvalResult evalResult = new EvalResult();
          evalResult.result_type = "success";
          evalResult.score = phrasesResult.result.overall;
          evalResult.pron = phrasesResult.result.pron;
          evalResult.fluency = phrasesResult.result.fluency.overall;
          evalResult.integrity = phrasesResult.result.integrity;
          if (!TextUtils.isEmpty(phrasesResult.audioUrl)) {
            if (phrasesResult.audioUrl.startsWith("http")) {
              evalResult.url = phrasesResult.audioUrl + ".mp3";
            } else {
              evalResult.url = "http://" + phrasesResult.audioUrl + ".mp3";
            }
          }
          evalResult.localpath = Uri.fromFile(new File(wavPath)).toString();
          if (phrasesResult.result.details != null && !phrasesResult.result.details.isEmpty()) {
            evalResult.detail = new ArrayList<>();
            for (ChiShengPhrasesResult.DetailsBean detailsBean : phrasesResult.result.details) {
              EvalResult.Detail detail = new EvalResult.Detail();
              detail.word =
                  TextUtils.isEmpty(detailsBean.word) ? detailsBean.text : detailsBean.word;
              detail.score = String.valueOf(detailsBean.score);
              evalResult.detail.add(detail);
            }
          }
          voiceEngineCallback.onResult(evalResult);
        } else {
          EvalResult evalResult = new EvalResult();
          evalResult.result_type = "exception";
          voiceEngineCallback.onResult(evalResult);
        }
      } else {
        r = Utils.parseJsonString(result, ChiShengResult.class);
        if (r != null && r.result != null) {
          EvalResult evalResult = new EvalResult();
          evalResult.result_type = "success";
          evalResult.score = r.result.overall;
          evalResult.pron = r.result.pron;
          evalResult.fluency = r.result.fluency;
          evalResult.integrity = r.result.integrity;
          if (!TextUtils.isEmpty(r.audioUrl)) {
            if (r.audioUrl.startsWith("http")) {
              evalResult.url = r.audioUrl + ".mp3";
            } else {
              evalResult.url = "http://" + r.audioUrl + ".mp3";
            }
          }
          evalResult.localpath = Uri.fromFile(new File(wavPath)).toString();
          if (r.result.details != null && !r.result.details.isEmpty()) {
            evalResult.detail = new ArrayList<>();
            for (ChiShengResult.DetailsBean detailsBean : r.result.details) {
              EvalResult.Detail detail = new EvalResult.Detail();
              detail.word =
                  TextUtils.isEmpty(detailsBean.word) ? detailsBean.text : detailsBean.word;
              detail.score = String.valueOf(detailsBean.score);
              evalResult.detail.add(detail);
            }
          }
          voiceEngineCallback.onResult(evalResult);
        } else {
          EvalResult evalResult = new EvalResult();
          evalResult.result_type = "exception";
          voiceEngineCallback.onResult(evalResult);
        }
      }
    }
  }

  private Callback messageCallback = new Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_VOLUME:
          int volume = msg.arg1;
          if (voiceEngineCallback != null) {
            voiceEngineCallback.onVolume(volume);
          }
          return true;
        case MSG_STOP:
          handleResult((String) msg.obj);
          return true;
        case MSG_TIMEOUT:
          onStop();
          return true;
        case MSG_ERR:
          handleError(msg.arg1, (String) msg.obj);
          break;
        default:
          break;
      }
      return false;
    }
  };

  @Override
  public void onStop() {
    messageHandler.removeMessages(MSG_TIMEOUT);
    if (engineSingleton != null) {
      engineSingleton.stopEngine();
    }
  }

  @Override
  public void onCancel() {
    onStop();
  }

  @Override
  public void onRelease() {
    if (engineSingleton != null) {
      engineSingleton.release();
      engineSingleton = null;
    }
  }

  private void handleError(int errorCode, String errMsg) {
    if (voiceEngineCallback != null) {
      if (isOralEval) {
        voiceEngineCallback.onEvalErr(errorCode, errMsg);
      } else {
        EvalResult evalResult = new EvalResult();
        evalResult.result_type = "exception";
        voiceEngineCallback.onResult(evalResult);
      }
    }
  }

}
