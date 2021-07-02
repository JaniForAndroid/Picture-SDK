package com.chivox;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.chivox.ParagraphResult.ResultBean;
import com.chivox.ParagraphResult.ResultBean.DetailsBean;
import com.constraint.CoreProvideTypeEnum;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.Utils;
import com.namibox.voice_engine_interface.EngineUtils;
import com.namibox.voice_engine_interface.VoiceEngineContext;
import com.namibox.voice_engine_interface.VoiceEngineContext.VoiceEngineCallback;
import com.namibox.voice_engine_interface.VoiceEngineStrategy;
import com.xs.BaseSingEngine.AudioErrorCallback;
import com.xs.SingEngine;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import org.json.JSONObject;

/**
 * Create time: 2020/4/17.
 */
public class VoiceEngineXS implements VoiceEngineStrategy {

  //先声评测类型
  public static final String TYPE_SENT = "en.sent.score";//单词+句子
  public static final String TYPE_PRED = "en.pred.score";//段落
  public static final String TYPE_SENT_CN = "cn.sent.score";//句子+段落
  public static final String TYPE_WORD_CN = "cn.word.score";//单词
  private Context context;
  private VoiceEngineCallback callback;
  private SingEngine singEngine;//先声引擎
  private String wavPath;
  private String userId;
  private boolean isSingEngineStopped;
  private boolean isSingEngineCancel;
  public boolean evalErr;
  public boolean isRunning;

  private String eval_type = VoiceEngineContext.EVAL_TYPE_SENT_EN;

  private boolean isOffline;
  private boolean switchToNative;
  private boolean isOralEval;
  private boolean isChEval;
  private Handler messageHandler;
  private static final int MSG_VOLUME = 0;
  private static final int MSG_RESULT = 1;
  private static final int MSG_ERR = 2;
  private static final int MSG_TIMEOUT = 3;
  private static final int MSG_RECORD_STOP = 4;
  private static final int MSG_EVAL_TIMEOUT = 5;
  private static final int MSG_INIT = 6;
  private static final int MSG_BEGIN = 7;
  private static final int MSG_END = 8;

  public VoiceEngineXS() {
    messageHandler = new Handler(messageCallback);
  }

  private Handler.Callback messageCallback = new Handler.Callback() {

    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_INIT:
          if (callback != null) {
            callback.onInitResult(true, 0, null);
          }
          return true;
        case MSG_VOLUME:
          int volume = msg.arg1;
          if (callback != null) {
            callback.onVolume(volume);
          }
          return true;
        case MSG_BEGIN:
          return true;
        case MSG_RESULT:
          handleEngineResult(msg.obj);
          return true;
        case MSG_END:
          if (msg.arg1 != 0) {
            evalErr = true;
            Message message = messageHandler.obtainMessage(MSG_ERR);
            message.obj = msg.obj;
            message.arg1 = msg.arg1;
            messageHandler.sendMessage(message);
          } else if (isOralEval) {
            messageHandler.removeMessages(MSG_EVAL_TIMEOUT);
          }
          return true;
        case MSG_ERR:
          evalErr = true;
          String err = msg.obj == null ? null : (String) msg.obj;
          int errCode = msg.arg1;
          if (isChEval && errCode == 28700 && !NetworkUtil
              .isNetworkAvailable(context)) {
            Utils.toast(context, "您当前是中文评测，需要联网，请检查网络连接");
          } else if (errCode == 16385 || errCode == 16386 || errCode == 16387 || errCode == 16388
              || errCode == 16389) {
            switchToNative = true;
            if (callback != null) {
              callback.onEvalTimeout();
            }
            messageHandler.removeMessages(MSG_EVAL_TIMEOUT);
            messageHandler.sendEmptyMessage(MSG_EVAL_TIMEOUT);
          } else if (errCode != 0 && err != null) {
            if (isOralEval && !isOffline && !switchToNative) {
              messageHandler.removeMessages(MSG_EVAL_TIMEOUT);
              messageHandler.sendEmptyMessage(MSG_EVAL_TIMEOUT);
            }
          } else if (errCode == 60001) {
            if (callback != null) {
              callback.onInitResult(false, errCode, err);
            }
          }
          handleEngineError(errCode, err);
          return true;
        case MSG_TIMEOUT:
          onStop();
          return true;
        case MSG_RECORD_STOP:
          if (callback != null) {
            callback.onRecordStop();
          }
          break;
        case MSG_EVAL_TIMEOUT:
          switchToNative = true;
          if (callback != null) {
            callback.onEvalTimeout();
          }
          if (!isSingEngineCancel && !isSingEngineStopped) {
            onCancel();
          }
          break;
        default:
          break;
      }
      return false;
    }
  };

  private void handleEngineError(int errCode, String err) {
    Logger.i(" handleEngineError() msg = " + err + ", errorCode = " + errCode);
    if (callback != null) {
      if (isOralEval) {
        callback.onEvalErr(errCode, err);
      } else {
        EvalResult evalResult = new EvalResult();
        evalResult.result_type = "exception";
        callback.onResult(evalResult);
      }
    }
  }

  private void handleEngineResult(Object obj) {
    Logger.i("handleEngineResult");
    if (callback != null) {
      if (isSingEngineCancel) {
        Logger.i("Shelter", "handleEngineStop() engineCanceled ");
        callback.onCanceled();
      } else if (obj == null) {
        EvalResult evalResult = new EvalResult();
        evalResult.result_type = "exception";
        callback.onResult(evalResult);
      } else {
        Logger.i("Shelter", "handleEngineStop() : 正常处理返回结果");
        callback.onResult(parseResult(obj));
      }
    }
  }

  /**
   * 解析先声引擎评测结果
   *
   * @param obj 为json对象
   * @return 封装后的评测结果
   */
  private EvalResult parseResult(Object obj) {
    EvalResult evalResult = new EvalResult();
    if ("word".equals(eval_type) || "phrases".equals(eval_type) || "phrases_cn"
        .equals(eval_type)) {
      XSResult result = Utils
          .parseJsonString(obj.toString(), XSResult.class);
      if (result != null && result.getResult() != null) {
        XSResult.ResultBean resultBean = result.getResult();
        evalResult.result_type = "success";
        if (TextUtils.isEmpty(result.getRefText())) {
          if (result.getParams() != null && result.getParams().getRequest() != null) {
            evalResult.content = result.getParams().getRequest().getRefText();
          }
        } else {
          evalResult.content = result.getRefText();
        }
        evalResult.score = resultBean.getOverall();
        evalResult.pron = resultBean.getAccuracy();
        evalResult.fluency = resultBean.getFluency().getOverall();
        evalResult.integrity = resultBean.getIntegrity();
        if (!TextUtils.isEmpty(result.getAudioUrl())) {
          if (result.getAudioUrl().startsWith("http")) {
            evalResult.url = result.getAudioUrl() + ".mp3";
          } else {
            evalResult.url = "http://" + result.getAudioUrl() + ".mp3";
          }
        }
        evalResult.localpath = Uri.fromFile(new File(wavPath)).toString();
        if (resultBean.getDetails() != null && !resultBean.getDetails().isEmpty()) {
          evalResult.detail = new ArrayList<>();
          for (XSResult.ResultBean.DetailsBean detailsBean : resultBean
              .getDetails()) {
            EvalResult.Detail detail = new EvalResult.Detail();
            detail.word = detailsBean.getCharX();
            detail.chWord = detailsBean.getChn_char();
            detail.score = String.valueOf(detailsBean.getScore());
            evalResult.detail.add(detail);
          }
        }
      } else {
        evalResult.result_type = "exception";
      }
    } else if ("word_cn".equals(eval_type)) {
      XSWordResult result = Utils
          .parseJsonString(obj.toString(), XSWordResult.class);
      if (result != null && result.getResult() != null) {
        XSWordResult.ResultBean resultBean = result.getResult();
        evalResult.result_type = "success";
        evalResult.content = result.getRefText();
        evalResult.score = resultBean.getOverall();
        evalResult.pron = resultBean.getPron();
        if (!TextUtils.isEmpty(result.getAudioUrl())) {
          if (result.getAudioUrl().startsWith("http")) {
            evalResult.url = result.getAudioUrl() + ".mp3";
          } else {
            evalResult.url = "http://" + result.getAudioUrl() + ".mp3";
          }
        }
        evalResult.localpath = Uri.fromFile(new File(wavPath)).toString();
        if (resultBean.getDetails() != null && !resultBean.getDetails().isEmpty()) {
          evalResult.detail = new ArrayList<>();
          for (XSWordResult.ResultBean.DetailsBean detailsBean : resultBean
              .getDetails()) {
            EvalResult.Detail detail = new EvalResult.Detail();
            detail.word = detailsBean.getCharX();
            detail.chWord = detailsBean.getChn_char();
            detail.score = String.valueOf(detailsBean.getScore());
            evalResult.detail.add(detail);
          }
        }
      } else {
        evalResult.result_type = "exception";
      }
    } else if ("paragraph".equals(eval_type)) {
      ParagraphResult paragraphResult = Utils
          .parseJsonString(obj.toString(), ParagraphResult.class);
      if (paragraphResult != null) {
        ResultBean result = paragraphResult.getResult();
        evalResult.result_type = "success";
        evalResult.score = result.getOverall();
        evalResult.content = paragraphResult.getRefText();
        evalResult.pron = result.getPron();
        evalResult.fluency = result.getFluency();
        evalResult.integrity = result.getIntegrity();
        String audioUrl = paragraphResult.getAudioUrl();
        //在线音频路径
        if (!TextUtils.isEmpty(audioUrl)) {
          evalResult.url = audioUrl;
        }
        //传入本地音频路径
        evalResult.localpath = Uri.fromFile(new File(wavPath)).toString();
        //封装句子详细得分
        if (result.getDetails() != null && !result.getDetails().isEmpty()) {
          evalResult.detail = new ArrayList<>();
          for (DetailsBean detailsBean : result
              .getDetails()) {
            EvalResult.Detail detail = new EvalResult.Detail();
            detail.word = detailsBean.getText();
            detail.score = String.valueOf(detailsBean.getScore());
            detail.snt_details = detailsBean.getSnt_details();
            evalResult.detail.add(detail);
          }
        }
      } else {
        evalResult.result_type = "exception";
      }
    }
    return evalResult;
  }

  /**
   * 先声评测监听
   */
  private SingEngine.ResultListener mResultListener = new SingEngine.ResultListener() {
    @Override
    public void onBegin() {

    }

    @Override
    public void onResult(final JSONObject result) {
      Logger.i("onResult() result = " + result);
      messageHandler.removeMessages(MSG_TIMEOUT);
      Message message = messageHandler.obtainMessage(MSG_RESULT);
      message.obj = result;
      messageHandler.removeMessages(MSG_EVAL_TIMEOUT);
      messageHandler.sendMessage(message);
    }

    @Override
    public void onEnd(final int Code, final String msg) {
      Logger.i("onEnd() Code = " + Code + ", msg = " + msg);
      messageHandler.removeMessages(MSG_END);
      Message message = messageHandler.obtainMessage(MSG_END);
      message.arg1 = Code;
      message.obj = msg;
      messageHandler.sendMessage(message);
    }

    @Override
    public void onUpdateVolume(final int volume) {
      Message msg = messageHandler.obtainMessage(MSG_VOLUME);
      msg.arg1 = volume;
      messageHandler.sendMessage(msg);
    }

    @Override
    public void onFrontVadTimeOut() {

    }

    @Override
    public void onBackVadTimeOut() {

    }

    @Override
    public void onRecordingBuffer(byte[] bytes, int i) {

    }

    @Override
    public void onRecordLengthOut() {
      if (!VoiceEngineContext.EVAL_TYPE_SENT_CN.equals(eval_type)) {
        Logger.d("onRecordLengthOut");
        onStop();
        Message message = messageHandler.obtainMessage(MSG_ERR);
        message.obj = "录音过长，已自动停止评测";
        message.arg1 = 30000;
        messageHandler.sendMessage(message);
      }
    }

    @Override
    public void onReady() {
      Message message = messageHandler.obtainMessage(MSG_INIT);
      message.arg1 = 0;
      messageHandler.sendMessage(message);
    }

    @Override
    public void onPlayCompeleted() {

    }

    @Override
    public void onRecordStop() {
      Logger.i("onRecordStop()");
      messageHandler.sendEmptyMessage(MSG_RECORD_STOP);
    }
  };

  @Override
  public void onInit(Context context) {

  }

  @Override
  public void onCreate(final Context context, final String serverType, String evalType,
      boolean isOralEval,
      String userId, final VoiceEngineCallback callback) {
    this.context = context;
    this.isOralEval = isOralEval;
    this.userId = userId;
    this.callback = callback;
    onSetEvalType(evalType);
    if (singEngine != null) {
      Logger.d("Shelter", "initSingEngine, singEngine != null return");
      return;
    }
    Single.fromCallable(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        try {
          //  获取引擎实例,设置测评监听对象
          singEngine = SingEngine.newInstance(context);
          singEngine.setListener(mResultListener);
          singEngine.setOpenWriteLog(true);
          singEngine.setAudioErrorCallback(new AudioErrorCallback() {
            @Override
            public void onAudioError(final int errCode) {
              Message message = messageHandler.obtainMessage(MSG_ERR);
              message.arg1 = 10000;
              messageHandler.sendMessage(message);
            }
          });
          //  设置引擎类型
          int engineType;
          if (VoiceEngineContext.TYPE_OFFLINE_XS.equals(serverType)) {
            engineType = VoiceEngineContext.SERVER_TYPE_OFFLINE;
            isOffline = true;
          } else {
            engineType = VoiceEngineContext.SERVER_TYPE_MIX;
            isOffline = false;
          }
          onSetServerType(engineType);
          //   构建引擎初始化参数
          singEngine.setLogLevel(4);
          if (VoiceEngineContext.EVAL_TYPE_PRED_EN.equals(eval_type)
              || VoiceEngineContext.EVAL_TYPE_SENT_CN.equals(eval_type)) {
            singEngine.setServerTimeout(30);
          } else {
            singEngine.setServerTimeout(5);
          }
          JSONObject cfg_init = singEngine
              .buildInitJson("a132", "7078e74d910d4db88ad0816b07afd79b");
          //   设置引擎初始化参数
          singEngine.setNewCfg(cfg_init);
          singEngine.setNativeZip("resource_2_en.zip");
          File externalFilesDir = context.getExternalFilesDir(null);
          if (externalFilesDir != null) {
            final File[] files = externalFilesDir.listFiles(new FileFilter() {
              @Override
              public boolean accept(File pathname) {
                String fileName = pathname.getName();
                return TextUtils.equals(fileName, "resource") || TextUtils
                    .equals(fileName, "resources") || TextUtils
                    .equals(fileName, "resource_en") || TextUtils.equals(fileName, "resource_1_en");
              }
            });
            if (files != null && files.length != 0) {
              for (File file : files) {
                FileUtil.deleteDir(file);
              }
            }
          }
          //   引擎初始化
          singEngine.createEngine();
          return true;
        } catch (Exception e) {
          e.printStackTrace();
        }
        return false;
      }
    })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<Boolean>() {
          @Override
          public void onSubscribe(@NonNull Disposable d) {

          }

          @Override
          public void onSuccess(@NonNull Boolean result) {
            callback.onInitResult(result, 0, null);
          }

          @Override
          public void onError(@NonNull Throwable e) {
            callback.onInitResult(false, 0, null);
          }
        });
  }

  @Override
  public void onSetServerType(int type) {
    CoreProvideTypeEnum typeEnum;
    if (type == VoiceEngineContext.SERVER_TYPE_ONLINE) {
      typeEnum = CoreProvideTypeEnum.CLOUD;
    } else if (type == VoiceEngineContext.SERVER_TYPE_MIX) {
      typeEnum = CoreProvideTypeEnum.AUTO;
    } else {
      typeEnum = CoreProvideTypeEnum.NATIVE;
    }
    singEngine.setServerType(typeEnum);
  }

  @Override
  public void onSetEvalType(String type) {
    this.eval_type = type;
  }

  @Override
  public void onStart(String text, String wavPath, long timeOutMillis) {
    if (singEngine != null) {
      try {
        JSONObject request = new JSONObject();
        isChEval = false;
        if (VoiceEngineContext.EVAL_TYPE_WORD_EN.equals(eval_type)
            || VoiceEngineContext.EVAL_TYPE_SENT_EN.equals(eval_type)) {
          request.put("coreType", TYPE_SENT);
        } else if (VoiceEngineContext.EVAL_TYPE_PRED_EN.equals(eval_type)) {
          request.put("coreType", TYPE_PRED);
        } else if (VoiceEngineContext.EVAL_TYPE_WORD_CN.equals(eval_type)) {
          request.put("coreType", TYPE_WORD_CN);
          isChEval = true;
        } else {
          request.put("coreType", TYPE_SENT_CN);
          isChEval = true;
        }
        request.put("symbol", 1);
        if (!isChEval && switchToNative) {
          onSetServerType(VoiceEngineContext.SERVER_TYPE_OFFLINE);
        }
        request.put("refText", text);
        Logger.i("Shelter", "startSingEngine() 评测文本 =" + text);
        request.put("rank", 100);
        request.put("typeThres", 2);
        //构建评测请求参数
        JSONObject startCfg;
        if (TextUtils.isEmpty(userId)) {
          startCfg = singEngine.buildStartJson("guest", request);
        } else {
          startCfg = singEngine.buildStartJson(userId, request);
        }
        //设置评测请求参数
        singEngine.setStartCfg(startCfg);
        if (wavPath == null) {
          this.wavPath = EngineUtils.initWavPath(context);
        } else {
          this.wavPath = wavPath;
        }
        singEngine.setWavPath(this.wavPath);
        messageHandler.removeMessages(MSG_EVAL_TIMEOUT);
        //开始测评
        singEngine.start();
        isSingEngineStopped = false;
        isSingEngineCancel = false;
        if (timeOutMillis > 0) {
          if (eval_type.equals(VoiceEngineContext.EVAL_TYPE_PRED_EN)
              || eval_type.equals(VoiceEngineContext.EVAL_TYPE_SENT_CN)) {
            timeOutMillis = 300 * 1000;
          }
          messageHandler.removeMessages(MSG_TIMEOUT);
          messageHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, timeOutMillis);
        } else if (timeOutMillis == -1) {
          if (VoiceEngineContext.EVAL_TYPE_WORD_CN.equals(eval_type)
              || VoiceEngineContext.EVAL_TYPE_SENT_CN.equals(eval_type)) {
            timeOutMillis = EngineUtils.calCnTextLength(text);
            if (timeOutMillis > 299 * 1000) {
              timeOutMillis = 299 * 1000;
            }
          } else {
            timeOutMillis = EngineUtils.calTextLength(text);
          }
          messageHandler.removeMessages(MSG_TIMEOUT);
          messageHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, timeOutMillis);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onStop() {
    messageHandler.removeMessages(MSG_TIMEOUT);
    if (singEngine != null) {
      Logger.i("Shelter", "stopSingEngine()");
      singEngine.stop();
      isSingEngineStopped = true;
      if (!isOffline && !switchToNative && isOralEval) {
        messageHandler.sendEmptyMessageDelayed(MSG_EVAL_TIMEOUT, 6000);
      }
    }
  }

  @Override
  public void onCancel() {
    //先声取消没回调,主动重置界面
    singEngine.cancelQuiet();
    isSingEngineCancel = true;
    handleEngineResult(null);
  }

  @Override
  public void onRelease() {
    if (singEngine != null) {
      singEngine.deleteSafe();
    }
    callback = null;
  }
}
