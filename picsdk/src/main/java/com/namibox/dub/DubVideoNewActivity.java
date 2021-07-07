package com.namibox.dub;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.State;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.nekocode.rxlifecycle.RxLifecycle;
import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import com.chivox.EvalResult;
import com.example.picsdk.R;
import com.google.android.exoplayer.lib.PlayerView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.namibox.commonlib.constant.Events;
import com.namibox.commonlib.event.FinishVoiceActivity;
import com.namibox.commonlib.view.RollImageView;
import com.namibox.hfx.bean.DubVideoNewRes;
import com.namibox.hfx.bean.DubVideoRatingRule;
import com.namibox.hfx.bean.DubVideoRatingRule.RuleModulus;
import com.namibox.hfx.bean.DubVideoRatingRule.StartInterval;
import com.namibox.hfx.bean.DubVideoRes;
import com.namibox.hfx.bean.RxEvent;
import com.namibox.hfx.event.DubExitEvent;
import com.namibox.hfx.ui.AbsExoActivity;
import com.namibox.hfx.utils.AudioComposeUtil;
import com.namibox.hfx.utils.DubFileUtil;
import com.namibox.hfx.utils.RxFFmpeg;
import com.namibox.hfx.utils.TextStyleUtil;
import com.namibox.hfx.view.WordScrollView;
import com.namibox.tools.PermissionUtil;
import com.namibox.tools.PermissionUtil.GrantedCallback;
import com.namibox.tools.ThinkingAnalyticsHelper;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.PreferenceUtil;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by sunha on 2015/12/25 0025.
 */
public class DubVideoNewActivity extends AbsExoActivity implements
    MediaPlayer.OnCompletionListener {


  public static final int STATUS_INIT = 0;
  public static final int STATUS_INIT_AUDIO = 1;
  public static final int STATUS_PLAYING = 2;
  public static final int STATUS_VIDEO_PLAYING = 3;
  public static final int STATUS_VIDEO_END = 4;
  private static final String TAG = "DubVideoNewActivity";
  public final static String VIDEO_ID = "videoId";
  public final static String VIDEO_PATH = "videopath";
  public final static String VIDEO_URI = "videouri";
  ImageView playpause;
  RecyclerView cardRecyclerView;
  ProgressBar videoProgress;
  ImageView back;
  RelativeLayout videoLayout;
  View step_layout;

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
  private Map<Integer, EvalResult> scoreResultMap = new HashMap<>();
  private Gson gson = new Gson();
  private File scoreFile;
  private View dialogView;
  private View fullScreendialogView;
  private int height;
  private Dialog detailDialog;
  private Dialog loadingDialog;
  private ImageView close;
  private TextView totalScore;
  private WordScrollView wordRecycler;
  private ProgressBar fluencyPro;
  private TextView fluencyScore;
  private ProgressBar pronPro;
  private TextView pronScore;
  private ProgressBar integrityPro;
  private TextView integrityScore;
  private RollImageView rollImg;
  private ProgressBar progressBar;
  private TextView loadingTips;
  private int avePron;
  private int aveIntegrity;
  private int aveFluency;
  private int aveTotalScore;
  private int startType;
  private View loadingView;
  private View stepView;
  private GifDrawable centerDrawable;
  private int currentProgress = -1;
  private String userid;
  private String match_id;
  private long homework_id;
  private String ossTokenUrl;
  private String reportUrl;
  private String worksubmitUrl;
  private String mItem_id;
  private long milesson_id, book_id;
  private long loginTime;
  private String product_name;
  private String title;
  private int audioType = 1;
  private SoundPool soundPool;
  private int clickID;

  @Override
  protected void setThemeColor() {
    super.setThemeColor();
    statusbarColor = toolbarColor = ContextCompat.getColor(this, R.color.transparent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    loginTime = System.currentTimeMillis();
    myHandler = new MyHandler(this);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    Intent intent = getIntent();
    jsonUrl = intent.getStringExtra("json_url");
    userid = intent.getStringExtra("userid");
    match_id = intent.getStringExtra("match_id");
    homework_id = intent.getLongExtra("homework_id", -1L);
    ossTokenUrl = intent.getStringExtra("ossTokenUrl");
    reportUrl = intent.getStringExtra("reportUrl");
    worksubmitUrl = intent.getStringExtra("worksubmitUrl");
    milesson_id = intent.getLongExtra("milesson_id", -1L);
    book_id = intent.getLongExtra("book_id", -1L);
    product_name = intent.getStringExtra("product_name");
    title = intent.getStringExtra("title");

    setContentView(R.layout.hfx_activity_dub_new);
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
    height = Utils.getScreenWidth(this)[1];
    //隐藏视频进度和播放按钮
    videoProgress.setVisibility(View.GONE);
    playpause.setVisibility(View.GONE);
    TagEventEnterPush(true, title, "趣味配音", product_name);
    initSound();
  }

  private void viewInit() {
    playpause = findViewById(R.id.playpause);
    cardRecyclerView = findViewById(R.id.cardRecyclerView);
    videoProgress = findViewById(R.id.videoProgress);
    back = findViewById(R.id.back);
    videoLayout = findViewById(R.id.video_layout);
    step_layout = findViewById(R.id.step_layout);
  }

  @TargetApi(21)
  private void initSound() {
    soundPool = new SoundPool.Builder().build();
    clickID = soundPool.load(this, R.raw.click_voice, 1);
  }

  private void playSound(int id) {
    soundPool.play(
        id,
        1f,      //左耳道音量【0~1】
        1f,      //右耳道音量【0~1】
        0,         //播放优先级【0表示最低优先级】
        0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
        1          //播放速度【1是正常，范围从0~2】
    );
  }

  public void TagEventEnterPush(boolean isEnter, String title, String page, String product_name) {
    HashMap<String, String> map = new HashMap<>();
    map.put("page", page);
    map.put("product_name", product_name);
    map.put("title", title);

    if (isEnter) {
      ThinkingAnalyticsHelper.trackEvent(Events.TA_EVENT_NB_APP_VIEW_ENTER, map);
    } else {
      ThinkingAnalyticsHelper.trackEvent(Events.TA_EVENT_NB_APP_VIEW_CLOSE, map);
    }
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
//    showDeterminateProgress("请稍候", "视频加载中...", "取消", new OnClickListener() {
//      @Override
//      public void onClick(View view) {
//        if (downDisposable != null && !downDisposable.isDisposed()) {
//          downDisposable.dispose();
//          showErrorDialog("资源未下载完成", true);
//        } else if (!chishengInited) {
//          showErrorDialog("测评引擎初始化失败", true);
//        }
//      }
//    });
    String msg = getString(R.string.player_loading);
    if (getResources() != null) {
      msg = getResources().getString(R.string.player_loading);
    }
    showFullScreenDialog("请稍候", msg);
    initResource();
  }

  private void showPlayPauseGuide() {
    if (pageIndex != 0) {
      return;
    }
    new MaterialIntroView.Builder(this)
//        .enableDotAnimation(true)
        .enableIcon(true)
        .setFocusGravity(FocusGravity.CENTER)
        .setFocusType(Focus.NORMAL)
        .enableFadeAnimation(true)
        .dismissOnTouch(true)
        .performClick(false)
        .setDelayMillis(1000)
        .setInfoText("点击看视频\n听听标准的发音吧")
        .setTarget(cardRecyclerView.getChildAt(0).findViewById(R.id.pause))
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

  public ConstraintLayout guideLayout;

  private void showPlayGuide() {
    if (isFinishing()) {
      return;
    }
    if (pageIndex != 0) {
      return;
    }
//    new MaterialIntroView.Builder(this)
//        .enableIcon(true)
//        .setFocusGravity(FocusGravity.CENTER)
//        .setFocusType(Focus.NORMAL)
//        .enableFadeAnimation(true)
//        .dismissOnTouch(true)
//        .performClick(false)
//        .setInfoText("点击这里可以听听自己配的怎么样哦~")
//        .setTarget(cardRecyclerView.getChildAt(0).findViewById(R.id.play))
//        .setUsageId("PlayGuide") //THIS SHOULD BE UNIQUE ID
//        .setListener(new MaterialIntroListener() {
//          @Override
//          public void onUserClicked(String s) {
////            showScoreGuide();
//          }
//        })
//        .show();
    guideLayout = findViewById(R.id.guide_layout);
    guideLayout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        guideLayout.setVisibility(View.GONE);
        PreferenceUtil.setSharePref(DubVideoNewActivity.this, "guide_showVideo", false);
      }
    });
    if (PreferenceUtil.getSharePref(this, "guide_showVideo", true)) {
      guideLayout.setVisibility(View.VISIBLE);
    }
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
              hideFullScreenLoadingProgress();
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
//        updateDeterminateProgress("视频下载中...  [" + c + "/" + t + "]", (int) (event.progress * 0.8f));
        updateFullScreenDialog("视频下载中...  [" + c + "/" + t + "]", (int) (event.progress * 0.9f));
      }
    } else if (event.type == 1) {
      int progress = (int) ((event.index + 1) * 100f / event.size);
      int current = event.index + 1;
//      updateDeterminateProgress("音频下载中...  [" + current + "/" + event.size + "]",
//          (int) (progress * 0.2f + 80));

      if (progress == 100 && System.currentTimeMillis() - loginTime <= 500) {
        updateFullScreenDialog("", 1);
        return;
      }
      updateFullScreenDialog("音频下载中...  [" + current + "/" + event.size + "]",
          (int) (progress * 0.2f + 80));
    } else if (event.type == 2) {
      updateDeterminateProgress("背景音生成中...", event.progress);
    }
  }

  private void showFullScreenDialog(String title, String content) {
    updateFullScreenDialog(title + content, 0);
  }

  private void updateFullScreenDialog(final String tips, final int progress) {
    if (progress == currentProgress || progress == 0) {
      return;
    }
    currentProgress = progress;
    Logger.e("zkx progress = " + progress + " currentProgress = " + currentProgress);
    initFullDialog();

    progressBar.postDelayed(new Runnable() {
      @Override
      public void run() {
        progressBar.setProgress(progress);
      }
    }, 10);
    loadingTips.postDelayed(new Runnable() {
      @Override
      public void run() {
        loadingTips.setText(tips);
      }
    }, 10);

    if (!loadingDialog.isShowing()) {
      loadingDialog.show();
    }
  }

//  private void initFullDialog() {
//    if (fullScreendialogView == null) {
//      fullScreendialogView = getLayoutInflater().inflate(R.layout.hfx_loading_fullscreen, null);
//      rollImg = fullScreendialogView.findViewById(R.id.roll_image);
//      progressBar = fullScreendialogView.findViewById(R.id.player_progress_loading);
//      loadingTips = fullScreendialogView.findViewById(R.id.tv_loading_tip);
//      loadingView = fullScreendialogView.findViewById(R.id.gp_loading);
//      stepView = fullScreendialogView.findViewById(R.id.gp_step);
//    }
//    if (loadingDialog == null) {
//      loadingDialog = new Dialog(this, R.style.fullscreen_dialog_style);
//      loadingDialog.setContentView(fullScreendialogView);
//      loadingDialog.setCanceledOnTouchOutside(true);
//      WindowManager.LayoutParams params = loadingDialog.getWindow().getAttributes();
////      params.height = (int) (height*0.75);
//      params.height = LayoutParams.MATCH_PARENT;
//      params.width = LayoutParams.MATCH_PARENT;
//      loadingDialog.getWindow().setGravity(Gravity.CENTER);
//      loadingDialog.setCanceledOnTouchOutside(false);
//      loadingDialog.setCancelable(false);
//      loadingDialog.getWindow().setAttributes(params);
//    }
//  }

  private void initFullDialog() {
    if (fullScreendialogView == null) {
      fullScreendialogView = getLayoutInflater().inflate(R.layout.hfx_loading_fullscreen, null);
      progressBar = fullScreendialogView.findViewById(R.id.progress_loading);
      loadingTips = fullScreendialogView.findViewById(R.id.tv_loading_tip);
    }
    if (loadingDialog == null) {
      loadingDialog = new Dialog(this, R.style.fullscreen_dialog_style);
      loadingDialog.setContentView(fullScreendialogView);
      loadingDialog.setCanceledOnTouchOutside(true);
      WindowManager.LayoutParams params = loadingDialog.getWindow().getAttributes();
//      params.height = (int) (height*0.75);
      params.height = LayoutParams.WRAP_CONTENT;
      params.width = LayoutParams.MATCH_PARENT;
      loadingDialog.getWindow().setGravity(Gravity.BOTTOM);
      loadingDialog.setCanceledOnTouchOutside(false);
      loadingDialog.setCancelable(false);
      loadingDialog.getWindow().setAttributes(params);
    }
  }

  private void hideFullScreenLoadingProgress() {
    if (this.isFinishing()) {
      step_layout.setVisibility(View.GONE);
      videoIndex = 0;
      onPageChanged(0);
      notifyAdapterStateChange(0, STATUS_VIDEO_PLAYING);
      return;
    }
    if (loadingDialog != null && !isFinishing()) {
//      loadingView.setVisibility(View.GONE);
//      stepView.setVisibility(View.VISIBLE);
      handlerLoading.sendEmptyMessage(MSG_UPDATE);
    }
  }

  private static final int MSG_UPDATE = 0x100;
  private Handler handlerLoading = new Handler() {
    public void handleMessage(Message msg) {
      if (msg.what == MSG_UPDATE) {
        int progress = progressBar.getProgress();
        if (progress < 100) {
          progress++;
          handlerLoading.sendEmptyMessageDelayed(MSG_UPDATE, 30);
        } else {
          loadingDialog.dismiss();
          step_layout.setVisibility(View.GONE);
          loadingDialog = null;
          currentProgress = -1;

//          new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
          videoIndex = 0;
          onPageChanged(0);
          notifyAdapterStateChange(0, STATUS_VIDEO_PLAYING);
//            }
//          }, 800);

//          new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//              showPlayPauseGuide();
//            }
//          }, 1200);
          handlerLoading.removeMessages(MSG_UPDATE);
        }
        progressBar.setProgress(progress);
      }
    }

    ;
  };

  private void transcodeToPcm() {
//        showDubProgressDialog("请稍候", "背景音生成中...");
    RxFFmpeg
        .getVideo2PcmObservable(DubVideoNewActivity.this, DubFileUtil
                .getMp4File(DubVideoNewActivity.this, itemId),
            DubFileUtil.getRawPcmTemp(DubVideoNewActivity.this, itemId), sampleRate, false)
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
            DubFileUtil.getRawPcmFile(DubVideoNewActivity.this, itemId).delete();
            DubFileUtil.getRawPcmTemp(DubVideoNewActivity.this, itemId)
                .renameTo(DubFileUtil.getRawPcmFile(DubVideoNewActivity.this, itemId));
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
        String mp4Url = videoRes.video.split("\\?")[0];
        Logger.e("zkx mp4Url = " + mp4Url);
        File md5File = DubFileUtil.getCacheFile(this, mp4Url);
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

  private boolean checkLoadMp3Downloaded(DubVideoRes videoRes) {
    int downloadNum = 0;
    try {
      if (videoRes.audio != null && !videoRes.audio.isEmpty()) {
        for (int i = 0; i < videoRes.audio.size(); i++) {
          DubVideoRes.Audio audio = videoRes.audio.get(i);
          File audioFile = DubFileUtil.getMp3FileByIndex(this, itemId, i);
          boolean mp3Success = checkFile(audio.audio_url, audioFile, null, null);
          if (mp3Success) {
            downloadNum++;
          }
        }
      }
      return downloadNum == videoRes.audio.size();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private void downLoadMp3(DubVideoRes videoRes,
      ObservableEmitter<? super RxEvent> flowableEmitter) {
    currentProgress = -1;
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
          //服务端返回数据包了一层 需要对原有的数据进行改造处理
          DubVideoNewRes dubVideoNewRes = Utils.parseJsonString(body, DubVideoNewRes.class);
          mItem_id = dubVideoNewRes.data.itemid;
          if (!(TextUtils.equals("SUCC", dubVideoNewRes.retcode) || TextUtils
              .equals("success", dubVideoNewRes.retcode))) {
            if (!flowableEmitter.isDisposed()) {
              flowableEmitter.onError(new Exception("调用接口失败"));
            }
            return;
          }
          dubVideoRes = dubVideoNewRes.data;
          Collections.sort(dubVideoRes.audio, new Comparator<DubVideoRes.Audio>() {
            @Override
            public int compare(DubVideoRes.Audio o1, DubVideoRes.Audio o2) {
              return (int) (o1.begin_time * 1000 - o2.begin_time * 1000);
            }
          });
          if (dubVideoRes != null) {
            itemId = dubVideoRes.milesson_item_id;
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
      scoreResultMap.put(recordAudioIndex, result);
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
    hideFullScreenLoadingProgress();
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

      @Override
      public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
        LinearSmoothScroller smoothScroller =
            new LinearSmoothScroller(recyclerView.getContext()) {
              // 返回：滑过1px时经历的时间(ms)。
              @Override
              protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 100f / displayMetrics.densityDpi;
              }
            };

        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
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
            Log.e("isScrollHalf", "isScrollHalf = " + isScrollHalf());
            scrollStateSettling = false;
            if (currentPosition != linearLayoutManager.findFirstVisibleItemPosition()
                && linearLayoutManager.findFirstVisibleItemPosition() >= 0) {
              currentPosition = linearLayoutManager.findFirstVisibleItemPosition();
            } else if (currentPosition == linearLayoutManager.findFirstVisibleItemPosition()) {
              if (isScrollHalf()) {
                currentPosition = linearLayoutManager.findFirstVisibleItemPosition() + 1;
              } else {
                currentPosition = linearLayoutManager.findFirstVisibleItemPosition();
              }
            }
            onPageChanged(currentPosition);
            break;
          case RecyclerView.SCROLL_STATE_SETTLING:
            scrollStateSettling = true;
            break;
        }

      }
    });
    adapter = new CardAdapter();
    cardRecyclerView.setAdapter(adapter);
    cardRecyclerView.setItemViewCacheSize(20);
//    new AlignTopSnaphelper().attachToRecyclerView(cardRecyclerView);
    audioPlayer = new MediaPlayer();
    audioPlayer.setOnCompletionListener(this);
    initializePlayer();
  }

  public boolean isScrollHalf() {
    View view = linearLayoutManager.getChildAt(0);
    if (null == view) {
      return false;
    }
    Log.e("view.getHeight()", "view.getHeight() = " + view.getHeight());

    int[] location = new int[2];
    view.getLocationOnScreen(location);
    Rect localRect = new Rect();
    view.getLocalVisibleRect(localRect);
    int showHeight = localRect.bottom - localRect.top;
    return showHeight <= view.getHeight() / 2;
  }

  public int getCurrentViewIndex() {
    int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
    int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
    int currentIndex = firstVisibleItem;
    int lastHeight = 0;
    for (int i = firstVisibleItem; i <= lastVisibleItem; i++) {
      View view = linearLayoutManager.getChildAt(i - firstVisibleItem);
      if (null == view) {
        continue;
      }
      int[] location = new int[2];
      view.getLocationOnScreen(location);
      Rect localRect = new Rect();
      view.getLocalVisibleRect(localRect);
      int showHeight = localRect.bottom - localRect.top;
      if (showHeight > lastHeight) {
        currentIndex = i;
        lastHeight = showHeight;
      }
    }

    if (currentIndex < 0) {
      currentIndex = 0;
    }
    return currentIndex;
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
//    cardRecyclerView.smoothScrollToPosition(currentPosition);
    LinearLayoutManager llm = (LinearLayoutManager) cardRecyclerView.getLayoutManager();
    if (llm != null) {
      llm.scrollToPositionWithOffset(currentPosition, Utils.dp2px(this, 0));
      llm.setStackFromEnd(false);
    }

    adapter.notifyItemChanged(pageIndex);
    if (currentPosition < dubVideoRes.audio.size()) {
      pageIndex = currentPosition;
      if (audioPlayer != null && audioPlayer.isPlaying()) {
        stopAudioPlayer();
      }
//      adapter.notifyDataSetChanged();
      adapter.notifyItemChanged(currentPosition);
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
      playpause.setVisibility(View.GONE);
//      playpause.setVisibility(View.VISIBLE);
    }
  }

  //视频的播放暂停
  @Override
  protected void doPauseResume() {
    //不处理视频点击事件
    if (true) {
      return;
    }
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
      audioType = 1;
      playAudio(audioType, videoIndex);
      myHandler.removeMessages(UPDATE_VIDEO_TIME);
      myHandler.sendEmptyMessage(UPDATE_VIDEO_TIME);
    }
    updatePausePlay();
  }

  private void seekAndPlay(int index) {
    isVideoPlaying = true;
    videoIndex = index;
    setExoPlayerVolume(0);
    int seekTime = dubVideoRes.audio.get(index).startTime;
    exoPlayerSeekTo(seekTime);
    exoPlayerStart();

//    new Handler().postDelayed(new Runnable() {
//      public void run() {
//
//      }
//    }, 600);
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
      if (isExoPlaying() && !isRecording && (audioPlayer != null && !audioPlayer.isPlaying())) {
        audioType = 1;
        playAudio(audioType, videoIndex);
      }

      Logger.d("STATE_READY");
    }
  }


  void startRecorder(final int index, ImageView startRecord) {
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
//      if (DubFileUtil.getWavFileById(this, itemId, index).exists()
//          && DubFileUtil.getWavFileById(this, itemId, index).length() > 100) {
//        showDialog("配音已经存在", "是否重新录制", "确认", new OnClickListener() {
//          @Override
//          public void onClick(View v) {
//            prepareRecord(index);
//          }
//        }, "取消", null);
//      } else {
//        prepareRecord(index);
//      }
      prepareRecord(index);

    }

  }

  //设置录音按钮工作状态
  private void initRecordingGif(ImageView startRecord) {
    resetCurrentGif();
    try {
      centerDrawable = new GifDrawable(getResources(), R.drawable.recording);
    } catch (Exception e) {
      e.printStackTrace();
      centerDrawable = (GifDrawable) ContextCompat.getDrawable(this, R.drawable.recording);
    }
    startRecord.setImageDrawable(centerDrawable);
    ((GifDrawable) centerDrawable).start();
  }

  //重置当前gift动画的状态
  private void resetCurrentGif() {
    if (centerDrawable != null) {
      centerDrawable.stop();
      centerDrawable.seekToFrame(0);
    }
  }

  private void prepareRecord(int index) {
    try {
      //一旦有录音 就需要重新合成 以便新的录音能够合成到最终作品中去
      hasMixed = false;
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
      }, 100);

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
    if (status == STATUS_VIDEO_PLAYING) {
      dubVideoRes.audio.get(index).hasVideoEnd = false;
    } else if (status == STATUS_VIDEO_END) {
      dubVideoRes.audio.get(index).hasVideoEnd = true;
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
//        seekAndPlayVideo(index, false);
        playAudioIndex = index;
        notifyAdapterStateChange(index, STATUS_PLAYING);
//                queueNextRefresh(UPDATE_PLAY_TIME);
      } else {
        notifyAdapterStateChange(index, STATUS_VIDEO_PLAYING);
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
      // 计算分数
      calculateScore();
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

  private void calculateScore() {
    if (scoreResultMap != null && !scoreResultMap.keySet().isEmpty()) {
      //先统计出来各项的总分
      int totalPron = 0;
      int totalIntegrity = 0;
      int totalFluency = 0;
      int totalScore = 0;
      Set<Integer> set = scoreResultMap.keySet();
      for (int key : set) {
        EvalResult result = scoreResultMap.get(key);
        totalPron += result.pron;
        totalIntegrity += result.integrity;
        totalFluency += result.fluency;
        totalScore += result.score;
      }
      DubVideoRatingRule rule = dubVideoRes.rating_rule;
      if (rule != null && rule.modulus != null) {
        //然后计算出各项平均分
        avePron = totalPron / set.size();
        aveIntegrity = totalIntegrity / set.size();
        aveFluency = totalFluency / set.size();
        //再根据各个维度的权重 计算出最终的总分
        RuleModulus modulus = rule.modulus;
        float pron = modulus.pron;
        float integrity = modulus.integrity;
        float fluency = modulus.fluency;
        aveTotalScore = (int) (pron * avePron + integrity * aveIntegrity + fluency * aveFluency);
      }
      //评分星级处理 0 - 3 分别对应本次评分的星级
      if (rule != null && rule.star != null) {
        StartInterval first_star = rule.star.first_star;
        StartInterval second_star = rule.star.second_star;
        StartInterval third_star = rule.star.third_star;
        if (aveTotalScore >= first_star.min && aveTotalScore <= first_star.max) {
          startType = 1;
        } else if (aveTotalScore >= second_star.min && aveTotalScore <= second_star.max) {
          startType = 2;
        } else if (aveTotalScore >= third_star.min && aveTotalScore <= third_star.max) {
          startType = 3;
        } else {
          startType = 0;
        }
      }
      Logger.e("zkx avePron = " + avePron + "\n aveIntegrity = " + aveIntegrity
          + "\n aveFluency = " + aveFluency + "\n aveScore = " + aveTotalScore
          + "\n startType = " + startType);
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
            DubSaveVideoNewActivity.openLocalVideo(DubVideoNewActivity.this,
                DubFileUtil.getMixedMp4File(DubVideoNewActivity.this, itemId),
                itemId, dubVideoRes.subtype, dubVideoRes.type, dubVideoRes.video_name,
                dubVideoRes.thumb_url, dubVideoRes.tutorable_relation_id, jsonUrl,
                aveTotalScore, avePron, aveIntegrity, aveFluency, startType, mItem_id,
                milesson_id, ossTokenUrl, reportUrl, userid, book_id, match_id, homework_id,
                worksubmitUrl);
            Logger.e("zkx avePron = " + avePron + "\n aveIntegrity = " + aveIntegrity
                + "\n aveFluency = " + aveFluency + "\n aveScore = " + aveTotalScore
                + "\n startType = " + startType);
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
    } else {
      notifyAdapterStateChange(pageIndex, STATUS_VIDEO_END);
    }
  }

  /**
   * 目标项是否在最后一个可见项之后
   */
  private boolean mShouldScroll;
  /**
   * 记录目标项位置
   */
  private int mToPosition;

  /**
   * 滑动到指定位置
   */

  private void smoothMoveToPosition(RecyclerView mRecyclerView, final int position) {
    // 第一个可见位置
    int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
    // 最后一个可见位置
    int lastItem = mRecyclerView
        .getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));

    if (position < firstItem) {
      // 如果跳转位置在第一个可见位置之前，就smoothScrollToPosition可以直接跳转
      mRecyclerView.smoothScrollToPosition(position);
    } else if (position <= lastItem) {
      // 跳转位置在第一个可见项之后，最后一个可见项之前
      // smoothScrollToPosition根本不会动，此时调用smoothScrollBy来滑动到指定位置
      int movePosition = position - firstItem;
      if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
        int top = mRecyclerView.getChildAt(movePosition).getTop();
        mRecyclerView.smoothScrollBy(0, top);
      }
    } else {
      // 如果要跳转的位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
      // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，执行上一个判断中的方法
      mRecyclerView.smoothScrollToPosition(position);
      mToPosition = position;
      mShouldScroll = true;
    }
  }

  private boolean checkRecordFile() {
    boolean canSubmit = true;

    if (dubVideoRes == null || dubVideoRes.audio == null) {
      return false;
    }

    for (int i = 0; i < dubVideoRes.audio.size(); i++) {
      if (scoreMap.entrySet() != null && scoreMap.entrySet().size() == dubVideoRes.audio.size()
          && DubFileUtil.getWavFileById(this, itemId, i).exists()
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
            .inflate(R.layout.hfx_dub_item_footer_new, parent, false);
        return new FooterViewHolder(itemView);
      } else {
        itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.hfx_dub_item_new, parent, false);
        return new CardViewHolder(itemView);

      }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {

      CardViewHolder holder = (CardViewHolder) viewHolder;
      holder.cardIndex.setText(position + 1 + "/" + dubVideoRes.audio.size());
      String english = dubVideoRes.audio.get(position).english;
      holder.english.setText(english);
      holder.chinese.setText(dubVideoRes.audio.get(position).chinese);
      if (isRecording && recordAudioIndex == position) {
//        holder.startRecord.setImageResource(R.drawable.hfx_icon_record_pause);
        holder.recordText.setVisibility(View.INVISIBLE);
        initRecordingGif(holder.startRecord);
      } else {
        holder.startRecord.setImageResource(R.drawable.hfx_icon_record);
        holder.recordText.setVisibility(View.VISIBLE);
      }
      switch (dubVideoRes.audio.get(position).status) {
        case STATUS_INIT:
          holder.progress.setProgress(0);
          holder.play.setVisibility(View.VISIBLE);
          holder.play.setImageResource(R.drawable.hfx_my_voice_none);
          holder.pause.setImageResource(R.drawable.hfx_my_voice_none);
          break;
        case STATUS_INIT_AUDIO:
          holder.progress.setProgress(100);
          holder.play.setVisibility(View.GONE);
          holder.playText.setVisibility(View.VISIBLE);
          holder.playMyVoiceImg.setVisibility(View.GONE);
          holder.pause.setImageResource(R.drawable.hfx_ic_play_souce);
          break;
        case STATUS_PLAYING:
          holder.progress.setProgress(100);
          holder.play.setVisibility(View.GONE);
          holder.playText.setVisibility(View.GONE);
          holder.playMyVoiceImg.setVisibility(View.VISIBLE);
          holder.pause.setImageResource(R.drawable.hfx_ic_play_souce);
          break;
        case STATUS_VIDEO_PLAYING:
          holder.playMyVoiceImg.setVisibility(View.GONE);
          holder.pause.setImageResource(R.drawable.hfx_my_voice_blue);
          break;
        case STATUS_VIDEO_END:
          holder.pause.setImageResource(R.drawable.hfx_ic_play_souce);
          break;
        default:
          break;
      }
      if (dubVideoRes.audio.get(position).hasTested) {
        holder.play.setVisibility(View.GONE);
        holder.playText.setVisibility(View.VISIBLE);
        if (dubVideoRes.audio.get(position).score < 60) {
          holder.scoreText.setBackgroundResource(R.drawable.hfx_triangle_red);
          holder.playText.setBackgroundResource(R.drawable.hfx_result_round_red);
          holder.playMyVoiceImg.setBackgroundResource(R.drawable.hfx_my_voice_red);
          holder.resultStars.setVisibility(View.VISIBLE);
          holder.resultStars.setBackgroundResource(R.drawable.icon_star_zero);
        } else if (dubVideoRes.audio.get(position).score >= 60 &&
            dubVideoRes.audio.get(position).score < 75) {
          holder.resultStars.setVisibility(View.VISIBLE);
          holder.resultStars.setBackground(null);
          holder.scoreText.setBackgroundResource(R.drawable.hfx_triangle_blue);
          holder.playText.setBackgroundResource(R.drawable.hfx_result_round_blue);
          holder.playMyVoiceImg.setBackgroundResource(R.drawable.hfx_my_voice_blue);
          holder.resultStars.setBackgroundResource(R.drawable.hfx_one_stars);
        } else {
          holder.resultStars.setBackground(null);
          holder.playText.setBackgroundResource(R.drawable.hfx_result_round_green);
          holder.scoreText.setBackgroundResource(R.drawable.hfx_triangle_green);
          holder.playMyVoiceImg.setBackgroundResource(R.drawable.hfx_my_voice_green);
          if (dubVideoRes.audio.get(position).score >= 75 &&
              dubVideoRes.audio.get(position).score < 85) {
            holder.resultStars.setBackgroundResource(R.drawable.hfx_two_stars);
          } else if (dubVideoRes.audio.get(position).score >= 85 &&
              dubVideoRes.audio.get(position).score <= 100) {
            holder.resultStars.setBackgroundResource(R.drawable.hfx_three_stars);
          }
          holder.resultStars.setVisibility(View.VISIBLE);
        }
//        holder.score.setVisibility(View.VISIBLE);
        if (dubVideoRes.audio.get(position).showAnimate) {
//          holder.score.setAlpha(0.5f);
//          holder.score.setScaleX(1.6f);
//          holder.score.setScaleY(1.6f);
//          holder.score.animate().setInterpolator(interpolator)
//              .scaleX(1).scaleY(1).alpha(1).setDuration(600).start();
          dubVideoRes.audio.get(position).showAnimate = false;
        }
        String scoreText = (int) dubVideoRes.audio.get(position).score + "";
        holder.scoreText.setText(scoreText);
        holder.playText.setText(scoreText);
        holder.scoreDetail.setVisibility(View.VISIBLE);
        holder.english.setVisibility(View.GONE);
        holder.wordRecycler.setVisibility(View.VISIBLE);
        //评测过的文本处理
        if (scoreResultMap != null && !scoreResultMap.keySet().isEmpty()) {
          AutoLineFeedLayoutManager layoutManager = new AutoLineFeedLayoutManager(
              holder.itemView.getContext(),
              false);
          layoutManager.setAutoMeasureEnabled(true);
          holder.wordRecycler.setLayoutManager(layoutManager);
          EvalResult result = scoreResultMap.get(position);
          if (result != null && result.detail != null && result.detail.size() > 0) {
            for (int i = 0; i < result.detail.size(); i++) {
              if (i < result.detail.size() - 1) {
                int index0 = result.detail.get(i + 1).word.indexOf("'");
                int index1 = result.detail.get(i).word.indexOf(",");
                int index2 = result.detail.get(i).word.indexOf("!");
                int index3 = result.detail.get(i).word.indexOf("?");
                int index4 = result.detail.get(i).word.indexOf(".");
                if (index1 == result.detail.get(i).word.length() - 1 && index0 == 0 ||
                    index2 == result.detail.get(i).word.length() - 1 && index0 == 0 ||
                    index3 == result.detail.get(i).word.length() - 1 && index0 == 0 ||
                    index4 == result.detail.get(i).word.length() - 1 && index0 == 0
                ) {
                  result.detail.get(i).word = result.detail.get(i).word + "'";
                  result.detail.get(i + 1).word = result.detail.get(i + 1).word
                      .replaceFirst("'", "");
                }
              }
            }
            holder.wordRecycler.setAdapter(new WordAdapter(result.detail, false));
          }
        }
      } else {
        holder.resultStars.setVisibility(View.INVISIBLE);
        holder.play.setVisibility(View.VISIBLE);
        holder.play.setImageResource(R.drawable.hfx_my_voice_none);
        holder.playText.setVisibility(View.GONE);
        holder.english.setVisibility(View.VISIBLE);
        holder.wordRecycler.setVisibility(View.GONE);
//        holder.score.setVisibility(View.INVISIBLE);
      }
      if (position == pageIndex) {
        holder.itemView.setSelected(true);
        holder.indexLayout.setVisibility(View.VISIBLE);
        holder.recordingLayout.setVisibility(View.VISIBLE);
        holder.progress.setVisibility(View.INVISIBLE);
        holder.scoreText.setVisibility(View.GONE);
        holder.diliver.setVisibility(View.GONE);
        holder.scoreDetail
            .setVisibility(
                dubVideoRes.audio.get(position).hasTested ? View.VISIBLE : View.INVISIBLE);
      } else {
        holder.itemView.setSelected(false);
        holder.indexLayout.setVisibility(View.GONE);
        holder.recordingLayout.setVisibility(View.GONE);
        holder.progress.setVisibility(View.INVISIBLE);
        holder.scoreDetail.setVisibility(View.INVISIBLE);
        holder.diliver.setVisibility(View.VISIBLE);
        holder.scoreText
            .setVisibility(dubVideoRes.audio.get(position).hasTested ? View.VISIBLE : View.GONE);
      }
      if (viewHolder instanceof FooterViewHolder) {
        ((FooterViewHolder) viewHolder).submitBtn.setBackgroundResource(
            checkRecordFile() ? R.drawable.hfx_btn_done_new : R.drawable.hfx_btn_down_gray);
        ((FooterViewHolder) viewHolder).submitBtn.setText(
            checkRecordFile() ? getString(R.string.book_dubrecordpreview_title) : getString(R.string.book_dubrecordnotfinish_title));
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
            holder.progress
                .setVisibility(holder.progress.getProgress() == 100 ||
                    holder.progress.getProgress() == 0 ? View.INVISIBLE : View.VISIBLE);
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

      public final TextView cardIndex, scoreText, scoreDetail, recordText, playText;
      public final TextView english;
      public final TextView chinese;
      public final TextView duration;
      public final ProgressBar progress;
      public final ImageView startRecord;
      public final ImageView play, resultStars, pause, playMyVoiceImg;
      public final RelativeLayout indexLayout, recordingLayout;
      public final View diliver;
      public final WordScrollView wordRecycler;

//      public final ImageView score;

      public CardViewHolder(final View itemView) {
        super(itemView);
        cardIndex = (TextView) itemView.findViewById(R.id.cardIndex);
        english = (TextView) itemView.findViewById(R.id.english);
        chinese = (TextView) itemView.findViewById(R.id.chinese);
        duration = (TextView) itemView.findViewById(R.id.duration);
//        score = (ImageView) itemView.findViewById(R.id.score);
        progress = (ProgressBar) itemView.findViewById(R.id.progress);
        startRecord = (ImageView) itemView.findViewById(R.id.startRecord);
        play = (ImageView) itemView.findViewById(R.id.play);
        indexLayout = (RelativeLayout) itemView.findViewById(R.id.index_layout);
        recordingLayout = (RelativeLayout) itemView.findViewById(R.id.recording_layout);
        scoreText = (TextView) itemView.findViewById(R.id.score_text);
        scoreDetail = (TextView) itemView.findViewById(R.id.result_detail);
        recordText = (TextView) itemView.findViewById(R.id.record_text);
        playText = (TextView) itemView.findViewById(R.id.play_text);
        resultStars = (ImageView) itemView.findViewById(R.id.result_stars);
        pause = (ImageView) itemView.findViewById(R.id.pause);
        diliver = itemView.findViewById(R.id.diliver);
        wordRecycler = itemView.findViewById(R.id.world_score_recycler);
        playMyVoiceImg = itemView.findViewById(R.id.play_my_voice);
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
//              int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
//              int lastItem = linearLayoutManager.findLastVisibleItemPosition();
//              if (n <= firstItem) {
//                cardRecyclerView.smoothScrollToPosition(n);
//              } else if (n <= lastItem) {
//                int top = cardRecyclerView.getChildAt(n - firstItem).getTop();
//                cardRecyclerView.smoothScrollBy(0, top);
//              } else {
//                cardRecyclerView.smoothScrollToPosition(n);
//              }
              smoothMoveToPosition(cardRecyclerView, n);
            } else {
//              onPageChanged(pageIndex);
            }
          }
        });
        pause.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (isRecording) {
              return;
            }
            if (scrollStateSettling) {
              return;
            }
            if (audioPlayer.isPlaying()) {
              if (audioType == 0) {
                onPageChanged(pageIndex);
              } else {
                stopAudioPlayer();
                stopVideo();
                pause.setImageResource(R.drawable.hfx_ic_play_souce);
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
              startRecorder(getAdapterPosition(), startRecord);
            }
          }
        });
        scoreDetail.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            showDetailDialog(getAdapterPosition());
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
                  audioType = 0;
                  playAudio(audioType, getAdapterPosition());
                }
              } else {
                audioType = 0;
                playAudio(audioType, getAdapterPosition());
              }
            }
          }
        });
        playText.setOnClickListener(new OnClickListener() {
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
                  audioType = 0;
                  playAudio(audioType, getAdapterPosition());
                }
              } else {
                audioType = 0;
                playAudio(audioType, getAdapterPosition());
              }
            }
          }
        });
        // 播放
        playMyVoiceImg.setOnClickListener(new OnClickListener() {
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
                }
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
        submitBtn = itemView.findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            playSound(clickID);
            if (isRecording) {
              toast("请录制完这段再预览");
              return;
            }
            stopAudioPlayer();
            pauseAndSeek(getAdapterPosition());
            if (hasMixed) {
              DubSaveVideoNewActivity.openLocalVideo(DubVideoNewActivity.this,
                  DubFileUtil.getMixedMp4File(DubVideoNewActivity.this, itemId),
                  itemId, dubVideoRes.subtype, dubVideoRes.type, dubVideoRes.video_name,
                  dubVideoRes.thumb_url, dubVideoRes.tutorable_relation_id, jsonUrl,
                  aveTotalScore, avePron, aveIntegrity, aveFluency, startType, mItem_id,
                  milesson_id, ossTokenUrl, reportUrl, userid, book_id, match_id, homework_id,
                  worksubmitUrl);
              Logger.e("zkx avePron = " + avePron + "\n aveIntegrity = " + aveIntegrity
                  + "\n aveFluency = " + aveFluency + "\n aveScore = " + aveTotalScore
                  + "\n startType = " + startType);
            } else {
              mixVideo();
            }

          }
        });
      }
    }
  }

  private void showDetailDialog(int position) {
    if (dialogView == null) {
      dialogView = getLayoutInflater().inflate(R.layout.hfx_score_detail_layout, null);
      close = dialogView.findViewById(R.id.close);
      totalScore = dialogView.findViewById(R.id.total_score);
      wordRecycler = dialogView.findViewById(R.id.world_score_recycler);
      fluencyPro = dialogView.findViewById(R.id.progress1);
      fluencyScore = dialogView.findViewById(R.id.fluency_score);
      pronPro = dialogView.findViewById(R.id.progress2);
      pronScore = dialogView.findViewById(R.id.pron_score);
      integrityPro = dialogView.findViewById(R.id.progress3);
      integrityScore = dialogView.findViewById(R.id.integrity_score);
      close.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (detailDialog != null) {
            detailDialog.dismiss();
          }
        }
      });
    }
    if (detailDialog == null) {
      detailDialog = new Dialog(this, R.style.custom_dialog_style);
      detailDialog.setContentView(dialogView);
      detailDialog.setCanceledOnTouchOutside(true);
      WindowManager.LayoutParams params = detailDialog.getWindow().getAttributes();
//      params.height = (int) (height*0.75);
      params.height = LayoutParams.WRAP_CONTENT;
      params.width = LayoutParams.MATCH_PARENT;
      detailDialog.getWindow().setGravity(Gravity.BOTTOM);
      detailDialog.getWindow().setAttributes(params);
    }
    if (scoreResultMap != null && !scoreResultMap.keySet().isEmpty()) {
      AutoLineFeedLayoutManager layoutManager = new AutoLineFeedLayoutManager(this, false);
      layoutManager.setAutoMeasureEnabled(true);
      wordRecycler.setLayoutManager(layoutManager);
      EvalResult result = scoreResultMap.get(position);
      if (result.detail != null && result.detail.size() > 0) {
        wordRecycler.setAdapter(new WordAdapter(result.detail, true));
      }
      totalScore.setText(getString(R.string.book_dubscoreresult_title)+" " + (int) result.score + "分");
      TextStyleUtil.setTextStytle(totalScore, "#333333", "本次综合得分 ", "分");
      fluencyPro.setProgress((int) result.fluency);
      fluencyScore.setText((int) result.fluency + "分");
      pronPro.setProgress((int) result.pron);
      pronScore.setText((int) result.pron + "分");
      integrityPro.setProgress((int) result.integrity);
      integrityScore.setText((int) result.integrity + "分");
    }
    if (detailDialog.isShowing()) {
      detailDialog.dismiss();
    } else {
      detailDialog.show();
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
    String msg = getString(R.string.book_dubchallengereturn_title);
//    if (checkRecordFile()) {
//      msg = "你已经录完所有片段了,预览看看效果吧~";
//    } else {
//      msg = "你还没录完哦,确认退出吗?";
//    }
    showDialog(getString(R.string.common_alert_tips1), msg, getString(R.string.hfx_limit_quit), new OnClickListener() {
      @Override
      public void onClick(View v) {
        exit();
      }
    }, getString(R.string.cancel), null);
  }

  private void exit() {
    try {
      //绘本馆 不保存用户之前的配音结果
      String s = gson.toJson(scoreMap);
      FileUtil.StringToFile(s, scoreFile, "utf-8");
    } catch (Exception e) {
      e.printStackTrace();
    }
    finish();
  }

  private static class MyHandler extends Handler {

    private final WeakReference<DubVideoNewActivity> mActivity;

    public MyHandler(DubVideoNewActivity activity) {
      mActivity = new WeakReference<DubVideoNewActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
      DubVideoNewActivity activity = mActivity.get();
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
    TagEventEnterPush(false, title, "趣味配音", product_name);
    if (pool != null) {
      pool.release();
    }
    myHandler.removeMessages(UPDATE_VIDEO_TIME);
    myHandler.removeMessages(UPDATE_RECORD_TIME);
    myHandler.removeMessages(UPDATE_PLAY_TIME);
    handlerLoading.removeCallbacksAndMessages(null);
    if (audioPlayer != null) {
      audioPlayer.stop();
      audioPlayer.release();
      audioPlayer = null;
    }

  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void refreshStoreInfo(FinishVoiceActivity event) {
    finish();
  }
}
