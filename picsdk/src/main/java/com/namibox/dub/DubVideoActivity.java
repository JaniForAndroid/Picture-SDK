package com.namibox.dub;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.nekocode.rxlifecycle.RxLifecycle;
import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import com.bumptech.glide.Glide;
import com.chivox.EvalResult;
import com.example.picsdk.R;
import com.google.android.exoplayer.lib.PlayerView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.namibox.hfx.bean.DubVideoRes;
import com.namibox.hfx.bean.RxEvent;
import com.namibox.hfx.event.DubExitEvent;
import com.namibox.hfx.ui.AbsExoActivity;
import com.namibox.hfx.utils.AudioComposeUtil;
import com.namibox.hfx.utils.DubFileUtil;
import com.namibox.hfx.utils.RxFFmpeg;
import com.namibox.hfx.view.AlignTopSnaphelper;
import com.namibox.tools.PermissionUtil;
import com.namibox.tools.PermissionUtil.GrantedCallback;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.Utils;
import com.namibox.voice_engine_interface.VoiceEngineContext.VoiceEngineCallback;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DefaultSubscriber;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by sunha on 2015/12/25 0025.
 */
public class DubVideoActivity extends AbsExoActivity implements MediaPlayer.OnCompletionListener {

  public static final int STATUS_INIT = 0;
  public static final int STATUS_INIT_AUDIO = 1;
  public static final int STATUS_PLAYING = 2;
  private static final String TAG = "DubVideoActivity";
  public final static String VIDEO_ID = "videoId";
  public final static String VIDEO_PATH = "videopath";
  public final static String VIDEO_URI = "videouri";
  ImageView playpause;
  RecyclerView cardRecyclerView;
  ProgressBar videoProgress;
  ImageView back;
  RelativeLayout videoLayout;

  private MyHandler myHandler;
  private static final int UPDATE_VIDEO_TIME = 2000;
  private static final int UPDATE_RECORD_TIME = 1001;
  private static final int UPDATE_PLAY_TIME = 1002;
  private MediaPlayer audioPlayer;
  private int audioPermissionFlag = 1;
  private long tempRecordCurrentTime;
  private TelephonyManager telManager;
  private int recordTimeMs;
  private int recordAudioIndex = -1;
  //播录音的标识
  private int playAudioIndex = -1;
  private int videoIndex = -1;
  private int pageIndex = 0;
  private boolean isRecording = false;
  private boolean isVideoPlaying = false;
  private int sampleRate = 16000;
  private CardAdapter adapter;
  private boolean initVideoProgress;
  private boolean mixing;
  private String itemId;
  private DubVideoRes dubVideoRes;
  private String jsonUrl;
  private boolean needTranscode;
  private Disposable downDisposable;
  private boolean isPcmReady = false;
  private boolean pcmFail;
  private boolean chishengInited;
  private boolean downLoadFinish;
  private boolean useChisheng;
  private int failCount = 0;
  private SoundPool pool;
  private int soundId;
  private LinearLayoutManager linearLayoutManager;
  private static final Interpolator interpolator = new DecelerateInterpolator();
  private boolean scrollStateSettling = false;
  private boolean hasMixed = false;
  private Map<Integer, Float> scoreMap = new HashMap<>();
  private Gson gson = new Gson();
  private File scoreFile;

  @Override
  protected void setThemeColor() {
    super.setThemeColor();
    statusbarColor = toolbarColor = ContextCompat.getColor(this, R.color.transparent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    myHandler = new MyHandler(this);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    Intent intent = getIntent();
    jsonUrl = intent.getStringExtra("json_url");
    setContentView(R.layout.hfx_activity_dub);
    viewInit();
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    PermissionUtil.requestPermissionWithFinish(this, new GrantedCallback() {
      @Override
      public void action() {
        init();
        regTelManager();
      }
    }, permission.RECORD_AUDIO);
    pool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    soundId = pool.load(this, R.raw.music, 100);
    initVideoRatio();
  }

  private void viewInit() {
    playpause = findViewById(R.id.playpause);
    cardRecyclerView = findViewById(R.id.cardRecyclerView);
    videoProgress = findViewById(R.id.videoProgress);
    back = findViewById(R.id.back);
    videoLayout = findViewById(R.id.video_layout);
  }

  private void init() {
    initEngineNoUI(new VoiceEngineCallback() {
      @Override
      public void onInitResult(boolean success, int errCode, String errMsg) {
        initEngineResult(success);
      }

      @Override
      public void onVolume(int volume) {
      }

      @Override
      public void onResult(Object result) {
        updateAudioScore((EvalResult) result);
      }

      @Override
      public void onCanceled() {

      }

      @Override
      public void onRecordStop() {

      }

      @Override
      public void onEvalTimeout() {

      }

      @Override
      public void onEvalErr(int errCode, String errMsg) {

      }
    });
    initAudioUtilWith16k();
    String msg = getString(R.string.player_loading);
    if (getResources() != null) {
      msg = getResources().getString(R.string.player_loading);
    }
    showDeterminateProgress("请稍候", msg, "取消", new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (downDisposable != null && !downDisposable.isDisposed()) {
          downDisposable.dispose();
          showErrorDialog("资源未下载完成", true);
        } else if (!chishengInited) {
          showErrorDialog(getString(R.string.book_dubevainitfailed_tips), true);
        }
      }
    });
    initResource();
  }

  private void showPlayPauseGuide() {
    if (pageIndex != 0) {
      return;
    }
    new MaterialIntroView.Builder(this)
        .enableDotAnimation(true)
        .enableIcon(true)
        .setFocusGravity(FocusGravity.CENTER)
        .setFocusType(Focus.NORMAL)
        .enableFadeAnimation(true)
        .dismissOnTouch(true)
        .performClick(false)
        .setDelayMillis(500)
        .setInfoText("点击看视频\n听听标准的发音吧")
        .setTarget(playpause)
        .setUsageId("PlayPauseGuide") //THIS SHOULD BE UNIQUE ID
        .setListener(new MaterialIntroListener() {
          @Override
          public void onUserClicked(String s) {
            showRecordGuide();
          }
        })
        .show();
  }


  private void showRecordGuide() {
    if (pageIndex != 0) {
      return;
    }
    new MaterialIntroView.Builder(this)
        .enableIcon(true)
        .setFocusGravity(FocusGravity.CENTER)
        .setFocusType(Focus.NORMAL)
        .enableFadeAnimation(true)
        .dismissOnTouch(true)
        .performClick(false)
        .setInfoText("点击这里会在\"嘟\"的一声后开始配音\n请在进度条结束前完成录音")
        .setTarget(cardRecyclerView.getChildAt(0).findViewById(R.id.startRecord))
        .setUsageId("RecordGuide") //THIS SHOULD BE UNIQUE ID
        .show();

  }

  private void showPlayGuide() {
    if (isFinishing()) {
      return;
    }
    if (pageIndex != 0) {
      return;
    }
    new MaterialIntroView.Builder(this)
        .enableIcon(true)
        .setFocusGravity(FocusGravity.CENTER)
        .setFocusType(Focus.NORMAL)
        .enableFadeAnimation(true)
        .dismissOnTouch(true)
        .performClick(false)
        .setInfoText("点击这里可以听听自己配的怎么样哦~")
        .setTarget(cardRecyclerView.getChildAt(0).findViewById(R.id.play))
        .setUsageId("PlayGuide") //THIS SHOULD BE UNIQUE ID
        .setListener(new MaterialIntroListener() {
          @Override
          public void onUserClicked(String s) {
            showScoreGuide();
          }
        })
        .show();

  }


  private void showScoreGuide() {
    if (pageIndex != 0) {
      return;
    }
    new MaterialIntroView.Builder(this)
        .enableIcon(true)
        .setFocusGravity(FocusGravity.CENTER)
        .setFocusType(Focus.NORMAL)
        .enableFadeAnimation(true)
        .dismissOnTouch(true)
        .performClick(false)
        .setInfoText("如果你的发音标准,你会得到一个笑脸哦\n不满意可以对这个片段重复配音")
        .setTarget(cardRecyclerView.getChildAt(pageIndex).findViewById(R.id.score))
        .setUsageId("ScoreGuide") //THIS SHOULD BE UNIQUE ID
        .show();

  }

  private void initResource() {
    //测试音频可用性
    try {
      audioPermissionFlag = testAudio();
    } catch (RuntimeException e) {

    }
    downDisposable = getJsonObservable()
        .flatMap(new Function<DubVideoRes, Observable<RxEvent>>() {
          @Override
          public Observable<RxEvent> apply(
              @io.reactivex.annotations.NonNull DubVideoRes dubVideoRes)
              throws Exception {
            return getResHandleObservable(dubVideoRes);
          }

        })
        .compose(RxLifecycle.bind(this).<RxEvent>withObservable())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<RxEvent>() {
          @Override
          public void accept(RxEvent resEvent) throws Exception {
            updateDubProgressDialog(resEvent);
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) throws Exception {
            throwable.printStackTrace();
            failCount++;
            if (failCount > 3) {
              hideDeterminateProgress();
              showErrorDialog("下载配音资源失败,请退出重试", true);
            } else {
              initResource();
            }
//                        hideDubProgressDialog();
//                        showErrorDialog("下载配音资源失败,请退出重试", true);
          }
        }, new Action() {
          @Override
          public void run() throws Exception {
            canInitExoplayer = true;
            if (needTranscode) {
              transcodeToPcm();
            } else {
              isPcmReady = true;
            }
            downLoadFinish = true;
            if (chishengInited) {
              initView();
            }
          }
        });
  }

  private void updateDubProgressDialog(RxEvent event) {
    if (event.type == 0) {
      if (event.status == 0) {
        String c = Utils.formatLengthString(event.currentSize);
        String t = Utils.formatLengthString(event.totalSize);
        updateDeterminateProgress("视频下载中...  [" + c + "/" + t + "]", (int) (event.progress * 0.8f));
      }
    } else if (event.type == 1) {
      int progress = (int) ((event.index + 1) * 100f / event.size);
      int current = event.index + 1;
      updateDeterminateProgress("音频下载中...  [" + current + "/" + event.size + "]",
          (int) (progress * 0.2f + 80));
    } else if (event.type == 2) {
      updateDeterminateProgress("背景音生成中...", event.progress);
    }
  }

  private void transcodeToPcm() {
//        showDubProgressDialog("请稍候", "背景音生成中...");
    RxFFmpeg
        .getVideo2PcmObservable(DubVideoActivity.this, DubFileUtil
                .getMp4File(DubVideoActivity.this, itemId),
            DubFileUtil.getRawPcmTemp(DubVideoActivity.this, itemId), sampleRate, false)
        .sample(300, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DefaultSubscriber<RxEvent>() {

          @Override
          public void onError(Throwable e) {
//                        hideDubProgressDialog();
            pcmFail = true;
            showErrorDialog("生成背景音失败", true);
          }

          @Override
          public void onComplete() {
//            toast("可以开始配音咯~");
            DubFileUtil.getRawPcmFile(DubVideoActivity.this, itemId).delete();
            DubFileUtil.getRawPcmTemp(DubVideoActivity.this, itemId)
                .renameTo(DubFileUtil.getRawPcmFile(DubVideoActivity.this, itemId));
            isPcmReady = true;
            //mixing标志了用户已经配完了音，点击了上传,背景在这之后完成，所以要再触发一次mixVideoNow
            if (mixing) {
              mixVideoNow();
            }
          }


          @Override
          public void onNext(RxEvent event) {
//                        updateDubProgressDialog(event);
          }
        });
  }

  private Observable<DubVideoRes> getJsonObservable() {
    return Observable.create(new ObservableOnSubscribe<DubVideoRes>() {
      @Override
      public void subscribe(ObservableEmitter<DubVideoRes> e) throws Exception {
        getJson(e);
      }
    });
  }


  /**
   * 下载资源
   */
  private Observable<RxEvent> getResHandleObservable(final DubVideoRes res) {
    //下载MP4
    return Observable.create(new ObservableOnSubscribe<RxEvent>() {
      @Override
      public void subscribe(ObservableEmitter<RxEvent> e) throws Exception {
        downLoadMp4(res, e);
      }
    }).concatWith(Observable.create(new ObservableOnSubscribe<RxEvent>() {
      @Override
      public void subscribe(ObservableEmitter<RxEvent> e) throws Exception {
        downLoadMp3(res, e);
      }
    }));
  }


  private void downLoadMp4(DubVideoRes videoRes, ObservableEmitter<? super RxEvent> emitter) {
    try {
      if (!TextUtils.isEmpty(videoRes.video)) {
        File mp4File = DubFileUtil.getMp4File(this, itemId);
        File pcmFile = DubFileUtil.getRawPcmFile(this, itemId);
        RxEvent event = new RxEvent();
        event.type = 0;
        event.index = 0;
        File md5File = DubFileUtil.getCacheFile(this, videoRes.video);
        if (md5File.exists() && md5File.length() > 0) {
          FileUtil.copyFile(md5File, mp4File.getParentFile(), mp4File.getName(), true);
          if (!pcmFile.exists() || pcmFile.length() <= 0) {
            RxEvent rxEvent = new RxEvent();
            rxEvent.type = 0;
            rxEvent.index = 0;
            needTranscode = true;
            emitter.onNext(rxEvent);
          }
          emitter.onComplete();
          return;
        }
        FileUtil.deleteDir(DubFileUtil.getDubItemDir(this, itemId));
        DubFileUtil.getDubItemDir(this, itemId).createNewFile();
        needTranscode = true;
        boolean downLoadResdult = downloadFile(md5File, videoRes.video, event, emitter);
        if (downLoadResdult) {
          FileUtil.copyFile(md5File, mp4File.getParentFile(), mp4File.getName(), true);
          RxEvent rxEvent = new RxEvent();
          rxEvent.type = 0;
          rxEvent.index = 0;
          emitter.onNext(rxEvent);
          emitter.onComplete();
          return;
        } else {
          if (!emitter.isDisposed()) {
            emitter.onError(new Exception("视频下载失败"));
          }
        }
      } else {
        if (!emitter.isDisposed()) {
          emitter.onError(new Exception("视频地址获取失败"));
        }

      }
    } catch (Exception e) {
      e.printStackTrace();
      if (!emitter.isDisposed()) {
        emitter.onError(e);
      }
    }
  }

  private void downLoadMp3(DubVideoRes videoRes,
      ObservableEmitter<? super RxEvent> flowableEmitter) {
    try {
      RxEvent event = new RxEvent();
      if (videoRes.audio != null && !videoRes.audio.isEmpty()) {
        for (int i = 0; i < videoRes.audio.size(); i++) {
          DubVideoRes.Audio audio = videoRes.audio.get(i);
          audio.startTime = (int) (audio.begin_time * 1000);
          audio.duration = (int) (audio.end_time * 1000 - audio.begin_time * 1000);
          if (audio.duration <= 1000) {
            audio.duration = 1001;
          }
          event.type = 1;
          event.index = i;
          event.size = videoRes.audio.size();
          event.progress = (int) ((i + 1) * 100f / videoRes.audio.size());
          flowableEmitter.onNext(event);
          File audioFile = DubFileUtil.getMp3FileByIndex(this, itemId, i);
          boolean mp3Success = checkFile(audio.audio_url, audioFile, event, flowableEmitter);
          if (!mp3Success) {
            if (!flowableEmitter.isDisposed()) {
              flowableEmitter.onError(new Exception("原声下载失败"));
            }
            return;
          }
        }
        flowableEmitter.onComplete();
      } else {
        if (!flowableEmitter.isDisposed()) {
          flowableEmitter.onError(new Exception("配音信息获取失败"));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (!flowableEmitter.isDisposed()) {
        flowableEmitter.onError(e);
      }
    }
  }


  private boolean checkFile(String url, File dstFile, RxEvent event,
      ObservableEmitter<? super RxEvent> flowableEmitter) throws Exception {
    if (TextUtils.isEmpty(url)) {
      return false;
    }
    File md5File = DubFileUtil.getCacheFile(this, url);
    if (md5File.exists() && md5File.length() > 0) {
      FileUtil.copyFile(md5File, dstFile.getParentFile(), dstFile.getName(), true);
      return true;
    }
    boolean downLoadResdult = downloadFile(md5File, url, event, flowableEmitter);
    if (downLoadResdult) {
      FileUtil.copyFile(md5File, dstFile.getParentFile(), dstFile.getName(), true);
    }
    return downLoadResdult;

  }


  private boolean downloadFile(File dstFile, String downUrl, RxEvent event,
      ObservableEmitter<? super RxEvent> emitter) {
    if (!dstFile.getParentFile().exists()) {
      dstFile.getParentFile().mkdirs();
    }
    File tmpFile = new File(dstFile.getAbsolutePath() + ".tmp");
    OkHttpClient okHttpClient = getOkHttpClient();
    try {
      long startPos = tmpFile.exists() ? tmpFile.length() : 0;
      Logger.d("download: " + downUrl);
      Request request = new Request.Builder()
          .cacheControl(CacheControl.FORCE_NETWORK)
          .url(Utils.encodeString(downUrl))
          .header("RANGE", "bytes=" + startPos + "-")
          .addHeader("Accept-Encoding", "deflate")
          .addHeader("Accept", "*/*")
          .build();
      Response response = okHttpClient
          .newCall(request)
          .execute();
      if (response.isSuccessful()) {
        byte[] buffer = new byte[10 * 1024];
        int count;
        long readSize = 0;
        long total = startPos + response.body().contentLength();
        InputStream is = response.body().byteStream();
        RandomAccessFile randomAccessFile = new RandomAccessFile(tmpFile, "rw");
        randomAccessFile.seek(startPos);
        while (!emitter.isDisposed() && (count = is.read(buffer)) > 0) {
          randomAccessFile.write(buffer, 0, count);
          readSize += count;
          if (event.type == 0) {
            long current = startPos + readSize;
            event.currentSize = current;
            event.totalSize = total;
            event.progress = (int) (100 * current / total);
            if (!emitter.isDisposed()) {
              emitter.onNext(event);
            }
          }

        }
        randomAccessFile.close();
        if (dstFile.exists()) {
          dstFile.delete();
        }
        boolean renameSuccess = false;
        if (!emitter.isDisposed()) {
          renameSuccess = tmpFile.renameTo(dstFile);
        }

        is.close();
        return renameSuccess;

      }
      if (response.body() != null) {
        response.body().close();
      }
    } catch (Exception e) {
      if (dstFile.exists()) {
        dstFile.delete();
      }
      e.printStackTrace();
    }
    return false;

  }

  /**
   * 获取配音相关资源
   */
  private void getJson(ObservableEmitter<? super DubVideoRes> flowableEmitter) {
    Request request = new Request.Builder()
        .cacheControl(CacheControl.FORCE_NETWORK)
        .url(Utils.encodeString(jsonUrl))
        .build();
    if (NetworkUtil.isNetworkAvailable(this)) {
      try {
        Response response = getOkHttpClient().newCall(request).execute();
        if (response != null && response.isSuccessful()) {
          String body = response.body().string();
          dubVideoRes = Utils.parseJsonString(body, DubVideoRes.class);
          Collections.sort(dubVideoRes.audio, new Comparator<DubVideoRes.Audio>() {
            @Override
            public int compare(DubVideoRes.Audio o1, DubVideoRes.Audio o2) {
              return (int) (o1.begin_time * 1000 - o2.begin_time * 1000);
            }
          });
          if (dubVideoRes != null) {
            itemId = dubVideoRes.itemid;
            flowableEmitter.onNext(dubVideoRes);
          } else {
            if (!flowableEmitter.isDisposed()) {
              flowableEmitter.onError(new Exception("调用接口失败"));
            }

          }
        } else {
          if (!flowableEmitter.isDisposed()) {
            flowableEmitter.onError(new Exception("调用接口失败"));
          }
        }
      } catch (Exception e) {
        if (!flowableEmitter.isDisposed()) {
          flowableEmitter.onError(new Exception("调用接口失败"));
        }
        e.printStackTrace();
      }

      flowableEmitter.onComplete();
    } else {
      if (!flowableEmitter.isDisposed()) {
        flowableEmitter.onError(new Exception("没有网络"));
      }
    }
  }

  private void initEngineResult(boolean success) {
    if (success) {
      chishengInited = true;
      useChisheng = true;
      if (downLoadFinish) {
        initView();
      }
    } else {
      toast(getString(R.string.book_dubevainitfailed_tips));
      chishengInited = true;
      useChisheng = false;
      if (downLoadFinish) {
        initView();
      }
    }
  }

  private void updateAudioScore(EvalResult result) {
    if ("success".equals(result.result_type) && recordAudioIndex >= 0 && recordTimeMs > 1000) {
      recordSuccess(recordAudioIndex, result.score);
      scoreMap.put(recordAudioIndex, result.score);
    } else {
      if (recordTimeMs <= 1000) {
        toast(getString(R.string.base_recordtimeshort_tips));
      }
      if (DubFileUtil.getWavFileById(this, itemId, recordAudioIndex).exists()) {
        DubFileUtil.getWavFileById(this, itemId, recordAudioIndex).delete();
      }
      notifyAdapterStateChange(recordAudioIndex, STATUS_INIT);
    }
    isRecording = false;
    recordTimeMs = 0;
    recordAudioIndex = -1;
    //Log.i(TAG, "onResult: " + result.result_type + result.content + result.score);
  }

  private void initView() {
    contentUri = Uri.fromFile(DubFileUtil.getMp4File(this, itemId));
    scoreFile = DubFileUtil.getScoreFile(this, itemId);
    try {
      String mapString = FileUtil.FileToString(scoreFile, "utf-8");
      scoreMap = gson.fromJson(mapString, new TypeToken<Map<Integer, Float>>() {
      }.getType());
    } catch (Exception e) {
      e.printStackTrace();
    }

    hideDeterminateProgress();
    PlayerView playerView = (PlayerView) findViewById(R.id.player_view);
    initPlayerView(playerView);

    back.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackPressed();
      }
    });
    checkRecordFile();
    linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
      @Override
      public boolean canScrollVertically() {
        return !isRecording;
      }
    };
    cardRecyclerView.setLayoutManager(linearLayoutManager);
    cardRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      int currentPosition = 0;

      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
      }

      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        switch (newState) {
          case RecyclerView.SCROLL_STATE_DRAGGING:
            scrollStateSettling = true;
            break;
          case RecyclerView.SCROLL_STATE_IDLE:
            scrollStateSettling = false;
            if (currentPosition != linearLayoutManager.findFirstVisibleItemPosition()
                && linearLayoutManager.findFirstVisibleItemPosition() >= 0) {
              currentPosition = linearLayoutManager.findFirstVisibleItemPosition();
              onPageChanged(currentPosition);
            }
            break;
          case RecyclerView.SCROLL_STATE_SETTLING:
            scrollStateSettling = true;
            break;
        }

      }
    });
    adapter = new CardAdapter();
    cardRecyclerView.setAdapter(adapter);
    new AlignTopSnaphelper().attachToRecyclerView(cardRecyclerView);
    audioPlayer = new MediaPlayer();
    audioPlayer.setOnCompletionListener(this);
    initializePlayer();
    showPlayPauseGuide();
  }

  private void initVideoRatio() {
    Point point = new Point();
    getWindowManager().getDefaultDisplay().getSize(point);
    int width = point.x;
    float template_ratio = 16f / 9;
    int videoHeight = (int) (width / template_ratio);
    ViewGroup.LayoutParams layoutParams = videoLayout.getLayoutParams();
    layoutParams.height = videoHeight;
    videoLayout.setLayoutParams(layoutParams);
  }

  private void onPageChanged(int currentPosition) {
    if (currentPosition < dubVideoRes.audio.size()) {
      pageIndex = currentPosition;
      if (audioPlayer.isPlaying()) {
        stopAudioPlayer();
      }
      adapter.notifyDataSetChanged();
      seekAndPlay(pageIndex);
    }
  }


  private void regTelManager() {
    boolean canRegTelManager = true;
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      canRegTelManager =
          checkSelfPermission(permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }
    if (canRegTelManager) {
      // 对电话的来电状态进行监听
      telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
      // 注册一个监听器对电话状态进行监听
      telManager.listen(new PhoneStateListener(),
          PhoneStateListener.LISTEN_CALL_STATE);
      telManager.getCallState();
    }
  }


  private void stopVideo() {
    exoPlayerPause();
    isVideoPlaying = false;
    videoIndex = -1;
    int seekTime = dubVideoRes.audio.get(pageIndex).startTime;
    exoPlayerSeekTo(seekTime);
    myHandler.removeMessages(UPDATE_VIDEO_TIME);
    updatePausePlay();
  }

  private long setVideoProgress() {
    long position = getExoCurrentPosition();
    long duration = getExoDuration();
    videoProgress.setProgress((int) (position * 100f / duration));
    return position;
  }


  private void updatePausePlay() {
    if (isExoPlaying()) {
      playpause.setVisibility(View.GONE);
    } else {
      playpause.setVisibility(View.VISIBLE);
    }
  }

  //视频的播放暂停
  @Override
  protected void doPauseResume() {
    if (isRecording) {
      return;
    }
    if (isExoPlaying()) {
      stopVideo();
      stopAudioPlayer();
    } else {
      isVideoPlaying = true;
      setExoPlayerVolume(1);
      int seekTime = dubVideoRes.audio.get(pageIndex).startTime;
      exoPlayerSeekTo(seekTime);
      exoPlayerStart();
      videoIndex = pageIndex;
      playAudio(1, videoIndex);
      myHandler.removeMessages(UPDATE_VIDEO_TIME);
      myHandler.sendEmptyMessage(UPDATE_VIDEO_TIME);
    }
    updatePausePlay();
  }

  private void seekAndPlay(int index) {
    isVideoPlaying = true;
    setExoPlayerVolume(1);
    int seekTime = dubVideoRes.audio.get(index).startTime;
    exoPlayerSeekTo(seekTime);
    exoPlayerStart();
    videoIndex = index;
    playAudio(1, videoIndex);
    myHandler.removeMessages(UPDATE_VIDEO_TIME);
    myHandler.sendEmptyMessage(UPDATE_VIDEO_TIME);
    updatePausePlay();
  }


  //播放录制音频时被调用
  private void seekAndPlayVideo(int index, boolean silence) {
    if (silence) {
      setExoPlayerVolume(0);
    } else {
      setExoPlayerVolume(1);
    }
    int seekTime = dubVideoRes.audio.get(index).startTime;
    exoPlayerSeekTo(seekTime);
    exoPlayerStart();
    myHandler.removeMessages(UPDATE_VIDEO_TIME);
    updatePausePlay();
  }


  private void pauseAndSeek(int index) {
    int seekTime = dubVideoRes.audio.get(index).startTime;
//        exoPlayerPause();
    exoPlayerSeekTo(seekTime);
    exoPlayerPause();
    myHandler.removeMessages(UPDATE_VIDEO_TIME);
    updatePausePlay();
    setVideoProgress();
  }


  @Override
  protected void releasePlayer() {
    super.releasePlayer();
    myHandler.removeMessages(UPDATE_VIDEO_TIME);
  }


  @Override
  public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
    if (playbackState == ExoPlayer.STATE_ENDED) {
      Logger.d("STATE_ENDED");
    } else if (playbackState == ExoPlayer.STATE_READY) {
      if (!initVideoProgress) {
        exoPlayerSeekTo(dubVideoRes.audio.get(0).startTime);
        setVideoProgress();
        initVideoProgress = true;
      }
      Logger.d("STATE_READY");
    }
  }


  void startRecorder(final int index) {
    if (this.isFinishing()) {
      return;
    }
    if (isRecording) {
      toast("请专心录完这一段吧");
      return;
    }

    if (audioPermissionFlag <= 0) {
      showErrorDialog("麦克风初始化失败,有其它应用正在录音或录音权限被禁用,请确认无以上问题后重试", true);
    } else {
      if (DubFileUtil.getWavFileById(this, itemId, index).exists()
          && DubFileUtil.getWavFileById(this, itemId, index).length() > 100) {
        showDialog("配音已经存在", "是否重新录制", "确认", new OnClickListener() {
          @Override
          public void onClick(View v) {
            prepareRecord(index);
          }
        }, "取消", null);
      } else {
        prepareRecord(index);
      }

    }

  }

  private void prepareRecord(int index) {
    try {
      isRecording = true;
      recordAudioIndex = index;
      stopAudioPlayer();
      pauseAndSeek(index);
//                startRecordImmediately(index);
      pool.play(soundId, 1, 1, 100, 0, 1);
      vibrator();
      videoLayout.postDelayed(new Runnable() {
        @Override
        public void run() {

          startRecordImmediately(recordAudioIndex);
        }
      }, 500);

    } catch (RuntimeException e) {
      showErrorDialog("初始化录音设备失败,可能有其他应用正在使用录音设备,请确认无以上问题后重试", true);
      e.printStackTrace();
    }
  }


  private void queueNextRefresh(int msg) {
    myHandler.removeMessages(msg);
    myHandler.sendEmptyMessageDelayed(msg, 100);
  }

  private void notifyAdapterProgress(int index, int currentTime) {
    adapter.notifyItemChanged(index, new ProgressEvent(currentTime));
  }

  private void notifyAdapterStateChange(int index, int status) {
    notifyAdapterStateChange(index, status, false);
  }


  private void notifyAdapterStateChange(int index, int status, boolean global) {
    if (status == STATUS_INIT) {
      dubVideoRes.audio.get(index).hasTested = false;
      dubVideoRes.audio.get(index).showAnimate = false;
      dubVideoRes.audio.get(index).score = 0;
    }
    dubVideoRes.audio.get(index).status = status;
    if (global) {
      adapter.notifyDataSetChanged();
    } else {
      adapter.notifyItemChanged(index);
    }

  }

  private void recordSuccess(int index, float score) {
    dubVideoRes.audio.get(index).score = score;
    dubVideoRes.audio.get(index).hasTested = true;
    dubVideoRes.audio.get(index).showAnimate = true;
    notifyAdapterStateChange(index, STATUS_INIT_AUDIO);
  }
//    private void notifyAdapterScore(int recordAudioIndex, boolean hasTested, int score) {
//        dubVideoRes.audio.get(recordAudioIndex).score = score;
//        dubVideoRes.audio.get(recordAudioIndex).hasTested = hasTested;
//        adapter.notifyItemChanged(recordAudioIndex);

//    }


  //type 0 录音文件播放  1原声播放
  private void playAudio(int type, int index) {

    try {
      audioPlayer.reset();
      String audioPath;
      if (type == 0) {
        audioPath = DubFileUtil.getWavFileById(this, itemId, index).getAbsolutePath();
      } else {
        audioPath = DubFileUtil.getMp3FileByIndex(this, itemId, index).getAbsolutePath();
      }
      audioPlayer.setDataSource(audioPath);
      audioPlayer.prepare();
      audioPlayer.setLooping(false);
      audioPlayer.start();
      if (type == 0) {
        seekAndPlayVideo(index, false);
        playAudioIndex = index;
        notifyAdapterStateChange(index, STATUS_PLAYING);
//                queueNextRefresh(UPDATE_PLAY_TIME);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, 100, 0, "下一步").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * 用户点击了上传按钮
   */
  private void mixVideo() {
    //转换背景音失败，需要退出重进
    if (pcmFail) {
      showErrorDialog("生成背景音失败,请退出重试", true);
    }
    if (mixing) {
      return;
    }

    if (checkRecordFile()) {
      showProgress("正在合成视频");
      //mixing标志了用户已经配完了音，点击了上传
      mixing = true;
      //isPcmReady表示背景音转换完成
      if (!isPcmReady) {
        return;
      }
      mixVideoNow();
    } else {
      toast("还有片段没有配完哦~");
      adapter.notifyDataSetChanged();
    }

  }


  private void mixVideoNow() {

    int[] audioStartTimeGroup = new int[dubVideoRes.audio.size()];
    String[] filePaths = new String[dubVideoRes.audio.size()];
    for (int i = 0; i < dubVideoRes.audio.size(); i++) {
      audioStartTimeGroup[i] = dubVideoRes.audio.get(i).startTime;
      filePaths[i] = DubFileUtil.getWavFileById(this, itemId, i).getAbsolutePath();
    }
    AudioComposeUtil
        //合并背景音和配音
        .getAudioComposeObservable(DubFileUtil.getRawPcmFile(this, itemId).getAbsolutePath(),
            DubFileUtil.getMixedPcmFile(this, itemId).getAbsolutePath(), filePaths,
            audioStartTimeGroup)
        //将合成的音频转换成aac，不用mp3是因为lame很慢，这里转aac很快，转成aac之后再加个头转成m4a，否则下一步ffmpeg识别不了
        .concatWith(AudioComposeUtil
            .getPcm2AACObservable(this, DubFileUtil.getMixedPcmFile(this, itemId).getAbsolutePath(),
                DubFileUtil.getAACFile(this, itemId).getAbsolutePath(),
                DubFileUtil.getM4aFile(this, itemId).getAbsolutePath()))
        //将生成的音频和之前的视频结合
        .concatWith(RxFFmpeg
            .getComposeVideoObservable(DubFileUtil.getMp4File(this, itemId).getAbsolutePath(),
                DubFileUtil.getM4aFile(this, itemId).getAbsolutePath(),
                DubFileUtil.getMixedMp4File(this, itemId).getAbsolutePath()))
        .sample(100, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DefaultSubscriber<Integer>() {

          @Override
          public void onError(Throwable e) {
            mixing = false;
            hideProgress();
            showErrorDialog("合并视频出错!", true);
            e.printStackTrace();

          }

          @Override
          public void onComplete() {
            mixing = false;
            hasMixed = true;
            hideProgress();
            DubSaveVideoActivity.openLocalVideo(DubVideoActivity.this,
                DubFileUtil.getMixedMp4File(DubVideoActivity.this, itemId),
                itemId, dubVideoRes.subtype, dubVideoRes.type, dubVideoRes.video_name,
                dubVideoRes.thumb_url, dubVideoRes.tutorable_relation_id);
          }


          @Override
          public void onNext(Integer integer) {
          }
        });
  }

  private void stopAudioRecorder() {
    Logger.i(TAG, "stopAudioRecorder: " + useChisheng);
    if (useChisheng) {
      stopEngine();
    } else {
      stopAudioRecord();
      if (recordAudioIndex >= 0 && recordTimeMs > 1000) {
        notifyAdapterStateChange(recordAudioIndex, STATUS_INIT_AUDIO);
      } else {
        if (recordTimeMs <= 1000) {
          toast(getString(R.string.base_recordtimeshort_tips));
        }
        notifyAdapterStateChange(recordAudioIndex, STATUS_INIT);
      }
      isRecording = false;
      recordTimeMs = 0;
      recordAudioIndex = -1;
    }
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    if (playAudioIndex >= 0) {
      stopVideo();
      notifyAdapterStateChange(playAudioIndex, STATUS_INIT_AUDIO);
      playAudioIndex = -1;
    }
  }

  private boolean checkRecordFile() {
    boolean canSubmit = true;
    for (int i = 0; i < dubVideoRes.audio.size(); i++) {
      if (DubFileUtil.getWavFileById(this, itemId, i).exists()
          && DubFileUtil.getWavFileById(this, itemId, i).length() > 100) {
        if (scoreMap.containsKey(i)) {
          dubVideoRes.audio.get(i).score = scoreMap.get(i);
          dubVideoRes.audio.get(i).hasTested = true;
        }
        dubVideoRes.audio.get(i).status = STATUS_INIT_AUDIO;
      } else {
        dubVideoRes.audio.get(i).status = STATUS_INIT;
        canSubmit = false;
      }
    }
    return canSubmit;
  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  public void exitEvent(DubExitEvent event) {
    exit();
  }

  class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View itemView;
      if (viewType == 1) {
        itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.hfx_dub_item_footer, parent, false);
        return new FooterViewHolder(itemView);
      } else {
        itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.hfx_dub_item, parent, false);
        return new CardViewHolder(itemView);

      }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {

      CardViewHolder holder = (CardViewHolder) viewHolder;
      holder.cardIndex.setText(position + 1 + "/" + dubVideoRes.audio.size());
      holder.english.setText(dubVideoRes.audio.get(position).english);
      holder.chinese.setText(dubVideoRes.audio.get(position).chinese);
      if (isRecording && recordAudioIndex == position) {
        Glide.with(DubVideoActivity.this)
            .asGif()
            .load(R.drawable.hfx_card_record_gif)
            .into(holder.startRecord);
      } else {
        holder.startRecord.setImageResource(R.drawable.hfx_card_recod_selector);
      }
      switch (dubVideoRes.audio.get(position).status) {
        case STATUS_INIT:
          holder.progress.setProgress(0);
          holder.play.setVisibility(View.INVISIBLE);
          holder.duration.setText(String
              .format(Locale.CHINA, "%.1fs", dubVideoRes.audio.get(position).duration / 1000f));
          break;
        case STATUS_INIT_AUDIO:
          holder.progress.setProgress(100);
          holder.play.setVisibility(View.VISIBLE);
          holder.play.setImageResource(R.drawable.hfx_card_play_selector);
          holder.duration.setText(String
              .format(Locale.CHINA, "%.1fs", dubVideoRes.audio.get(position).duration / 1000f));
          break;
        case STATUS_PLAYING:
          holder.progress.setProgress(100);
          holder.play.setVisibility(View.VISIBLE);
          holder.play.setImageResource(R.drawable.hfx_card_pause_selected);
          holder.duration.setText(String
              .format(Locale.CHINA, "%.1fs", dubVideoRes.audio.get(position).duration / 1000f));
          break;
      }
      if (dubVideoRes.audio.get(position).hasTested) {
        if (dubVideoRes.audio.get(position).score > 70) {
          holder.score.setImageResource(R.drawable.hfx_card_good_score);
        } else {
          holder.score.setImageResource(R.drawable.hfx_card_score_bad_selector);
        }
        holder.score.setVisibility(View.VISIBLE);
        if (dubVideoRes.audio.get(position).showAnimate) {
          holder.score.setAlpha(0.5f);
          holder.score.setScaleX(1.6f);
          holder.score.setScaleY(1.6f);
          holder.score.animate().setInterpolator(interpolator)
              .scaleX(1).scaleY(1).alpha(1).setDuration(600).start();
          dubVideoRes.audio.get(position).showAnimate = false;
        }
      } else {
        holder.score.setVisibility(View.INVISIBLE);
      }
      if (position == pageIndex) {
        holder.itemView.setSelected(true);
      } else {
        holder.itemView.setSelected(false);
      }
      if (viewHolder instanceof FooterViewHolder) {

      }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position,
        List<Object> payloads) {
      if (payloads == null || payloads.isEmpty()) {
        onBindViewHolder(viewHolder, position);
      } else {
        if (viewHolder instanceof CardViewHolder) {
          CardViewHolder holder = (CardViewHolder) viewHolder;
          Object object = payloads.get(payloads.size() - 1);
          if (object instanceof ProgressEvent) {
            ProgressEvent event = (ProgressEvent) payloads.get(0);
            int progress = (int) (event.time / (float) dubVideoRes.audio.get(position).duration
                * 100);
            holder.progress.setProgress(progress);
          }
        }
      }
    }

    @Override
    public int getItemCount() {
      return dubVideoRes.audio.size();
    }

    @Override
    public int getItemViewType(int position) {
      if (position < dubVideoRes.audio.size() - 1) {
        return 0;
      } else {
        return 1;
      }
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {

      public final TextView cardIndex;
      public final TextView english;
      public final TextView chinese;
      public final TextView duration;
      public final ProgressBar progress;
      public final ImageView startRecord;
      public final ImageView play;

      public final ImageView score;

      public CardViewHolder(final View itemView) {
        super(itemView);
        cardIndex = (TextView) itemView.findViewById(R.id.cardIndex);
        english = (TextView) itemView.findViewById(R.id.english);
        chinese = (TextView) itemView.findViewById(R.id.chinese);
        duration = (TextView) itemView.findViewById(R.id.duration);
        score = (ImageView) itemView.findViewById(R.id.score);
        progress = (ProgressBar) itemView.findViewById(R.id.progress);
        startRecord = (ImageView) itemView.findViewById(R.id.startRecord);
        play = (ImageView) itemView.findViewById(R.id.play);
        itemView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (isRecording) {
              return;
            }
            if (scrollStateSettling) {
              return;
            }
            if (pageIndex != getAdapterPosition()) {
              int n = getAdapterPosition();
              if (n < 0) {
                return;
              }
              int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
              int lastItem = linearLayoutManager.findLastVisibleItemPosition();
              if (n <= firstItem) {
                cardRecyclerView.smoothScrollToPosition(n);
              } else if (n <= lastItem) {
                int top = cardRecyclerView.getChildAt(n - firstItem).getTop();
                cardRecyclerView.smoothScrollBy(0, top);
              } else {
                cardRecyclerView.smoothScrollToPosition(n);
              }
            } else {
              onPageChanged(pageIndex);
            }
          }
        });
        startRecord.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (pageIndex == getAdapterPosition()) {
              startRecorder(getAdapterPosition());
            }
          }
        });
        play.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (isRecording) {
              toast("请录制当前片段再试听");
              return;
            }

            if (pageIndex == getAdapterPosition()) {
              if (playAudioIndex == getAdapterPosition()) {
                if (audioPlayer.isPlaying()) {
                  stopAudioPlayer();
                  stopVideo();
                } else {
                  playAudio(0, getAdapterPosition());
                }
              } else {
                playAudio(0, getAdapterPosition());
              }
            }
          }
        });
      }


    }

    public class FooterViewHolder extends CardViewHolder {

      public final Button submitBtn;

      public FooterViewHolder(View itemView) {
        super(itemView);
        submitBtn = (Button) itemView.findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (isRecording) {
              toast("请录制完这段再预览");
              return;
            }
            stopAudioPlayer();
            pauseAndSeek(getAdapterPosition());
            if (hasMixed) {
              DubSaveVideoActivity.openLocalVideo(DubVideoActivity.this,
                  DubFileUtil.getMixedMp4File(DubVideoActivity.this, itemId),
                  itemId, dubVideoRes.subtype, dubVideoRes.type, dubVideoRes.video_name,
                  dubVideoRes.thumb_url, dubVideoRes.tutorable_relation_id);
            } else {
              mixVideo();
            }

          }
        });
      }

    }

  }

  private void stopAudioPlayer() {
    audioPlayer.stop();
    if (isRecording) {
      if (recordAudioIndex >= 0) {
        notifyAdapterStateChange(recordAudioIndex, STATUS_INIT);
      }
    } else {
      if (playAudioIndex >= 0) {
        notifyAdapterStateChange(playAudioIndex, STATUS_INIT_AUDIO);
      }
    }
  }

  class ProgressEvent {

    int time;

    public ProgressEvent(int time) {
      this.time = time;
    }

  }


  @Override
  public void onBackPressed() {
    if (isRecording) {
      toast(getString(R.string.recording_tips));
      return;
    }
    String msg =  getString(R.string.book_dubchallengereturn_title);
//    if (checkRecordFile()) {
//      msg = getString(R.string.book_dubchallengereturn_title);
//    } else {
//      msg = "你还没录完哦,确认退出吗?";
//    }
    showDialog("确认退出", msg, "继续配音", null, "退出", new OnClickListener() {
      @Override
      public void onClick(View v) {
        exit();
      }
    });
  }

  private void exit() {
    try {
      String s = gson.toJson(scoreMap);
      FileUtil.StringToFile(s, scoreFile, "utf-8");
    } catch (Exception e) {
      e.printStackTrace();
    }
    finish();
  }

  private static class MyHandler extends Handler {

    private final WeakReference<DubVideoActivity> mActivity;

    public MyHandler(DubVideoActivity activity) {
      mActivity = new WeakReference<DubVideoActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
      DubVideoActivity activity = mActivity.get();
      if (activity != null && !activity.isFinishing()) {
        switch (msg.what) {
          case UPDATE_RECORD_TIME:
            if (activity.recordAudioIndex < 0) {
              activity.stopAudioRecorder();
              activity.stopVideo();
              return;
            }
            long currentTime = System.currentTimeMillis();
            int timeTemp = (int) (currentTime - activity.tempRecordCurrentTime);
            activity.tempRecordCurrentTime = currentTime;
            activity.recordTimeMs += timeTemp;
            if (activity.isRecording && activity.recordAudioIndex >= 0) {
              activity.notifyAdapterProgress(activity.recordAudioIndex, activity.recordTimeMs);
            }
            if (activity.recordTimeMs >= activity.dubVideoRes.audio
                .get(activity.recordAudioIndex).duration) {
              activity.showPlayGuide();
              activity.stopAudioRecorder();
              activity.stopVideo();
              return;
            }
            if (null != activity.telManager
                && TelephonyManager.CALL_STATE_RINGING == activity.telManager.getCallState()) {
              activity.stopAudioRecorder();
              activity.stopVideo();
              return;
            }
            if (activity.isRecording) {
              activity.queueNextRefresh(UPDATE_RECORD_TIME);
            }
            break;

          case UPDATE_PLAY_TIME:
            //播录音
            if (activity.playAudioIndex >= 0 && activity.audioPlayer.isPlaying()) {
              int time = activity.audioPlayer.getCurrentPosition();
              activity.notifyAdapterProgress(activity.playAudioIndex, time);
            }
            activity.queueNextRefresh(UPDATE_PLAY_TIME);
            break;
          case UPDATE_VIDEO_TIME:
            if (activity.isVideoPlaying) {
              if (activity.videoIndex >= 0) {
                long currentPosition = activity.getExoCurrentPosition();
                long playTime =
                    currentPosition - activity.dubVideoRes.audio.get(activity.videoIndex).startTime;
                if (playTime >= 0) {
                  activity.setVideoProgress();
                  activity.queueNextRefresh(UPDATE_VIDEO_TIME);
                }
                if (playTime >= activity.dubVideoRes.audio.get(activity.videoIndex).duration) {
                  activity.stopVideo();
                  activity.stopAudioPlayer();
                }

              }

            }
            break;
          default:
            return;
        }
        return;
      }
    }


  }

  private void startRecordImmediately(int index) {
    notifyAdapterStateChange(index, STATUS_INIT);
    tempRecordCurrentTime = System.currentTimeMillis();
    queueNextRefresh(UPDATE_RECORD_TIME);
    seekAndPlayVideo(index, true);
    if (useChisheng) {
      startEngine(dubVideoRes.audio.get(index).english, dubVideoRes.audio.get(index).duration,
          DubFileUtil.getWavFileById(this, itemId, index).getAbsolutePath());
    } else {
      startPcm(DubFileUtil.getPcmItemFileById(this, itemId, index), false, true,
          DubFileUtil.getWavFileById(this, itemId, index));
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (pool != null) {
      pool.release();
    }
    myHandler.removeMessages(UPDATE_VIDEO_TIME);
    myHandler.removeMessages(UPDATE_RECORD_TIME);
    myHandler.removeMessages(UPDATE_PLAY_TIME);
    if (audioPlayer != null) {
      audioPlayer.stop();
      audioPlayer.release();
      audioPlayer = null;
    }

  }
}
