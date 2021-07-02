package com.chivox;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.namibox.util.MD5Util;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Callable;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by sunha on 2017/2/18 0018.
 */

public class AIEngineSingleton {

  private static final String TAG = "AIEngineSingleton";
  private volatile static AIEngineSingleton singleton;
  private boolean hasInit = false;
  private Disposable engineInit;
  private String serialNumber = "";
  public long engine = 0;
  private int rv;
  private static final String appKey = "144150708600000a";
  private static final String secretKey = "aa677891a780fb8a3bb0e8f3e06aba1e";
  private AIRecorder aiRecorder = null;
  private String refText;
  private String type;
  //    private boolean onLine;
  private EngineCallback callback;

  private AIRecorder.Callback recorderCallback = new AIRecorder.Callback() {

    @Override
    public void onStarted() {
      start();
    }

    @Override
    public void onData(byte[] data, int size) {
      AIEngine.aiengine_feed(engine, data, size);
    }

    @Override
    public void onStopped() {
      if (rv == 0) {
        AIEngine.aiengine_stop(engine);
      } else {
        if (callback != null) {
          callback.onEngineStartError(rv);
        }
      }
    }

    @Override
    public void onError(String error) {
      if (callback != null) {
        callback.onRecordError(error);
      }
    }
  };
  private String userid;

  private void start() {
    byte[] id = new byte[64];

    JSONObject paramJso = new JSONObject();
    try {
      JSONObject appJso = new JSONObject();
      if(TextUtils.isEmpty(userid)) {
        appJso.put("userId", "namibox");
      } else {
        appJso.put("userId", userid);
      }

      JSONObject audioJso = new JSONObject();
      audioJso.put("audioType", "wav");
      audioJso.put("channel", 1);
      audioJso.put("sampleRate", 16000);
      audioJso.put("sampleBytes", 2);

      JSONObject reqJso = new JSONObject();
      if (type.equals("word")) {
        reqJso.put("coreType", "en.word.score");
        reqJso.put("refText", refText);
      } else if (type.equals("phrases")) {
        reqJso.put("coreType", "en.sent.score");
        reqJso.put("refText", refText);
      } else {
        reqJso.put("coreType", "en.pred.exam");
        JSONObject obj = new JSONObject();
        obj.put("lm", refText);
        obj.put("qid", MD5Util.md5(refText));
        reqJso.put("refText", obj);
      }
      reqJso.put("rank", 100);
      reqJso.put("precision", 0.5);
      reqJso.put("attachAudioUrl", 1);
      JSONObject obj = new JSONObject();
      obj.put("ext_subitem_rank4", 0);
      reqJso.put("client_params", obj);

      paramJso.put("coreProvideType", "cloud");
      paramJso.put("serialNumber", serialNumber);
      paramJso.put("app", appJso);
      paramJso.put("audio", audioJso);
      paramJso.put("request", reqJso);
      paramJso.put("soundIntensityEnable", 1);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    String param = paramJso.toString();
    Log.d(TAG, "param: " + param);
    rv = AIEngine.aiengine_start(engine, param, id, engineCallback, this);
    Log.d(TAG, "engine start: " + rv);
  }

  private AIEngine.aiengine_callback engineCallback = new AIEngine.aiengine_callback() {
    @Override
    public int run(byte[] id, int type, byte[] data, int size) {
      if (type == AIEngine.AIENGINE_MESSAGE_TYPE_JSON) {
        String response = new String(data, 0, size).trim();
        Log.d(TAG, "aiengineCallback: " + response);
        try {
          JSONObject jsonObject = new JSONObject(response);
          if (jsonObject.has("sound_intensity")) {
            double sound = jsonObject.getDouble("sound_intensity");
            if (callback != null) {
              callback.onVolumeChanged((int) sound);
            }
            return 0;
          } else if (jsonObject.has("errId") && jsonObject.has("error")) {
            int errId = jsonObject.getInt("errId");
            String error = "评测失败";
            stopEngine();
            if (callback != null) {
              callback.onEvalError(errId, error);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (callback != null) {
          callback.onResult(response);
        }
      }
      return 0;
    }
  };

  private AIEngineSingleton() {
    aiRecorder = new AIRecorder();
  }

  public static AIEngineSingleton getInstance() {
    if (singleton == null) {
      synchronized (AIEngineSingleton.class) {
        if (singleton == null) {
          singleton = new AIEngineSingleton();
        }
      }
    }
    return singleton;
  }

  public void init(final Context applicationContext, final InitCallback callBack) {
    if (hasInit) {
      callBack.onCompleted();
      return;
    }
    if (engineInit != null && !engineInit.isDisposed()) {
      engineInit.dispose();
    }
    engineInit = Flowable.fromCallable(new Callable<Boolean>() {


      @Override
      public Boolean call() throws Exception {
        try {
          byte buf[] = new byte[1024];
          AIEngine.aiengine_get_device_id(buf, applicationContext);
          String deviceId = new String(buf).trim();
          Log.d(TAG, "deviceId: " + deviceId);

//                    String resourcePath = AIEngineHelper.extractResourceOnce(applicationContext, "aiengine.resource.zip", true);
          String provisionPath = AIEngineHelper
              .extractResourceOnce(applicationContext, "aiengine.provision", false);
          //Log.d(TAG, "resourceDir: " + resourceDir == null ? "" : resourceDir.getAbsolutePath());
          Log.d(TAG, "provisionPath: " + provisionPath);

          String cfg = String.format(
              "{\"appKey\": \"%s\", \"secretKey\": \"%s\", \"provision\": \"%s\", \"cloud\": {\"server\": \"ws://cloud.chivox.com:8080\",\"serverList\":\"\"}}",
              appKey, secretKey,
              provisionPath);
          if (engine == 0) {
            engine = AIEngine.aiengine_new(cfg, applicationContext);
            Log.d(TAG, "aiengine: " + engine);
          }

          serialNumber = AIEngineHelper
              .registerDeviceOnce(applicationContext, appKey, secretKey, engine);
          Log.d(TAG, "serialNumber: " + serialNumber);
          if (TextUtils.isEmpty(serialNumber)) {
            return false;
          }
        } catch (Exception e) {
          e.printStackTrace();
          return false;
        }

        return true;
      }
    })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<Boolean>() {
          @Override
          public void accept(Boolean aBoolean) throws Exception {
            hasInit = true;
            if (callBack != null) {
              callBack.onCompleted();
            }
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) throws Exception {
            throwable.printStackTrace();
            if (callBack != null) {
              callBack.onError();
            }
          }
        });
  }

  public void startEngine(String refText, String type, String wavPath,
      String userid, EngineCallback callback) {
    this.refText = refText;
    this.type = type;
//        this.onLine = onLine;
    this.callback = callback;
    this.userid = userid;
    aiRecorder.start(wavPath, recorderCallback);
  }

  public void stopEngine() {
    if (aiRecorder != null) {
      aiRecorder.stop();
    }
  }

  public void release() {
    if (engineInit != null && !engineInit.isDisposed()) {
      engineInit.dispose();
    }
    if (engine != 0) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          AIEngine.aiengine_delete(engine);
          engine = 0;
          hasInit = false;
          String threadName = Thread.currentThread().getName();
          Log.i(TAG, "engine deleted: " + engine + threadName);
        }
      }).start();
    }
  }
}
