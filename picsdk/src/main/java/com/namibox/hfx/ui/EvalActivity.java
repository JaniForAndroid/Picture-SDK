package com.namibox.hfx.ui;


import static com.namibox.hfx.ui.RecordActivity.ARG_INTRODUCE;
import static com.namibox.hfx.ui.RecordActivity.ARG_IS_MAKING;
import static com.namibox.hfx.ui.RecordActivity.ARG_NEED_SHOWOTHERS;
import static com.namibox.hfx.ui.RecordActivity.ARG_USER_URL;

import android.Manifest.permission;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.carlos.voiceline.mylibrary.VoiceLineView;
import com.chivox.EvalResult;
import com.chivox.EvalResult.Detail;
import com.example.exoaudioplayer.aduio.base.AbstractAudioPlayer;
import com.example.exoaudioplayer.aduio.base.AudioCallBack;
import com.example.exoaudioplayer.aduio.base.AudioPlayerFactory;
import com.example.exoaudioplayer.aduio.base.Constants;
import com.example.picsdk.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.gson.JsonObject;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.model.BaseNetResult;
import com.namibox.commonlib.view.CircleImageView;
import com.namibox.commonlib.view.FocusView;
import com.namibox.commonlib.view.HackyViewPager;
import com.namibox.hfx.bean.EvalBody;
import com.namibox.hfx.bean.Huiben;
import com.namibox.hfx.bean.Huiben.BookAudio;
import com.namibox.hfx.bean.Huiben.BookPage;
import com.namibox.hfx.bean.Huiben.WorkUser;
import com.namibox.hfx.bean.MatchInfo;
import com.namibox.hfx.bean.UploadInfo;
import com.namibox.hfx.bean.VideoInfo;
import com.namibox.hfx.ui.RecordActivity.State;
import com.namibox.hfx.utils.HFXWorksUtil;
import com.namibox.hfx.utils.HfxFileUtil;
import com.namibox.hfx.utils.HfxPreferenceUtil;
import com.namibox.hfx.utils.HfxUtil;
import com.namibox.hfx.view.PageImageView;
import com.namibox.hfx.view.RoundProgressBar;
import com.namibox.simplifyspan.SimplifySpanBuild;
import com.namibox.simplifyspan.unit.SpecialTextUnit;
import com.namibox.tools.PermissionUtil;
import com.namibox.tools.PermissionUtil.GrantedCallback;
import com.namibox.tools.WebViewUtil;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.MD5Util;
import com.namibox.util.NetworkUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import com.namibox.util.WeakAsyncTask;
import com.namibox.util.network.NetWorkHelper;
import com.namibox.voice_engine_interface.VoiceEngineContext.VoiceEngineCallback;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.uraroji.garage.android.lame.AudioUtil.VolumeCallBack;
import com.zhy.view.flowlayout.FlowLayout;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DefaultSubscriber;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import okhttp3.CacheControl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;

/**
 * @author namibox
 */
public class EvalActivity extends BaseCommitActivity implements OnClickListener {

  private static final String TAG = "EvalActivity";
  public boolean engineInitSuccess;
  public static final String ARG_RECORD_URL = "record_url";
  public static final String ARG_BOOK_ID = "workId";
  public static final String ARG_CONTENT_TYPE = "content_type";
  private ImageView ivBack;
  private Button btnCommit;
  private RecyclerView scoreList;
  private TextView tvPageIndex;
  private ConstraintLayout topLayout;
  private HackyViewPager viewPager;
  private TextView tvCountDown;
  //竖屏
  private TextView tvScore;
  private TextView tvExplain;
  private TextView tvEvalContent;
  private FlowLayout scoreResultList;
  private ImageView ivRecord;
  private ImageView ivPlayAudio;
  private ImageView ivPlayRecord;
  private ConstraintLayout bottomLayout;
  //横屏
  private TextView tvScoreLand;
  private TextView tvExplainLand;
  private TextView tvEvalContentLand;
  private FlowLayout scoreResultListLand;
  private ImageView ivRecordLand;
  private ImageView ivPlayAudioLand;
  private ImageView ivPlayRecordLand;
  private ConstraintLayout bottomLayoutLand;
  private SparseArray<EvalResult> evalMap;
  private String engineType;
  private long startTime;
  private VoiceLineView voiceLine;
  private View readyLayout;
  private View listeningLayout;
  private RoundProgressBar listeningProgress;
  private View listeningStop;
  private State state = State.INIT;
  private boolean hasRecord;

  private String url;
  private String userUrl;
  private boolean isMaking;
  protected int currentPage;
  private boolean isChanged = false;
  //默认true，修改作品是传false
  private boolean needShowothers;
  private String user_id;
  protected Huiben book;
  private boolean isOnCommitFragment = false;
  private EvalAdapter evalAdapter;
  private ScoreAdapter scoreAdapter;
  private ConstraintLayout clRoot;
  private VolumeCallBack volumeCallBack;

  private SoundPool soundPool;
  private HashMap<String, Integer> soundIds = new HashMap<>();
  private boolean buttonShowHideAnim;
  private boolean buttonHide;
  private boolean imageFullscreen;
  private boolean canStop = true;

  private static final int MESSAGE_RECORD_UPDATE = 100;
  private static final int MESSAGE_HIDE_SHOW_BUTTON = 200;
  private static final int START_RECORD = 300;
  private static final int HIDE_RESULT_TEXT = 400;
  private static final int MESSAGE_RECORD_MINIMUM = 500;
  private static final long RECORD_TIME_LIMIT = 10 * 60 * 1000;
  private static final int GUIDE_PLAY_AUDIO_PORTRAIT = 1;
  private static final int GUIDE_PLAY_RECORD_PORTRAIT = 2;
  private static final int GUIDE_RECORD_PORTRAIT = 3;
  private static final int GUIDE_SLIDE_PORTRAIT = 4;
  private static final int GUIDE_PLAY_AUDIO_LAND = 5;
  private static final int GUIDE_PLAY_RECORD_LAND = 6;
  private static final int GUIDE_RECORD_LAND = 7;
  private static final int GUIDE_SLIDE_LAND = 8;
  private static final String SOUND_GOOD = "good.mp3";
  private static final String SOUND_GREAT = "great.mp3";
  private static final String SOUND_NOT_BAD = "notbad.mp3";
  private static final String SOUND_PERFECT = "prefect.mp3";
  private static final String SOUND_UNBELIEVABLE = "unbelievable.mp3";
  private static final String SOUND_READY = "ready.mp3";

  private Handler handler;
  private long startRecordTime;

  private InitResTask initResTask;
  private ConstraintLayout guideLayout;
  private FocusView focusView;
  private TextView tvGuideText;
  private TextView tvKnow;
  private ImageView ivGuideArrow;
  private int currentGuide = -1;
  private String matchId;
  private ScrollView scrollView;
  private ScrollView scrollViewLand;
  private ImageView mBgImg;
  private boolean soundInit;
  private boolean jsonInit;
  private Disposable soundDisposable;
  private boolean isOnline;
  private boolean isCustompad;

  private AbstractAudioPlayer exoAudioPlayer;
  private String mType;

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    if (!isCustompad) {
      setLayoutOrientation(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT);
      super.onConfigurationChanged(newConfig);
    }
//    super.onConfigurationChanged(newConfig);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    getWindow().setFlags(LayoutParams.FLAG_KEEP_SCREEN_ON,
        LayoutParams.FLAG_KEEP_SCREEN_ON);
    if (VERSION.SDK_INT >= VERSION_CODES.KITKAT
        && VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
      getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN,
          LayoutParams.FLAG_FULLSCREEN);
      SystemBarTintManager tintManager = getTintManager();
      if (tintManager != null) {
        tintManager.setStatusBarTintEnabled(false);
      }
    }
    //禁止虚拟键盘
//    View decorView = getWindow().getDecorView();
//    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
//    decorView.setSystemUiVisibility(uiOptions);

    setContentView(R.layout.hfx_activity_eval);
    findViews();
    Intent intent = getIntent();
    isCustompad = intent.getBooleanExtra("is_custompad", false);
    if (isCustompad) {
      setLayoutOrientation(false);
    }
    Logger.e("zkx", "isCustompad = " + isCustompad);
    user_id = Utils.getLoginUserId(this);
    url = intent.getStringExtra(ARG_RECORD_URL);
    userUrl = intent.getStringExtra(ARG_USER_URL);
    workId = intent.getStringExtra(ARG_BOOK_ID);
    mType = intent.getStringExtra(ARG_CONTENT_TYPE);
    introduce = intent.getStringExtra(ARG_INTRODUCE);
    matchId = intent.getStringExtra("match_id");
    if (TextUtils.isEmpty(introduce)) {
      String user_id = Utils.getLoginUserId(getApplicationContext());
      introduce = HfxPreferenceUtil.getBookIntroduce(this, user_id, workId);
    }
    isMaking = intent.getBooleanExtra(ARG_IS_MAKING, false);

    needShowothers = intent.getBooleanExtra(ARG_NEED_SHOWOTHERS, true);
    if (TextUtils.isEmpty(workId)) {
      showErrorDialog(getString(R.string.hfx_error_book_id), true);
      return;
    }
    //初始化引擎
    engineType = getIntent().getStringExtra("oral_engine");
    if (TextUtils.isEmpty(engineType)) {
      engineType = TYPE_MIX_XS;
    }
    if (!engineType.equals(TYPE_OFFLINE_XS)) {
      isOnline = true;
    }
    startTime = System.currentTimeMillis();
    initEngineNoUI("phrases", engineType, Utils.getLoginUserId(this), engineCallback, false);

    if (!TextUtils.isEmpty(url)) {
      Logger.d(TAG, "save book url: " + url);
      HfxPreferenceUtil.saveRecordBookUrl(this, workId, url);
    } else {
      url = HfxPreferenceUtil.getRecordBookUrl(this, workId);
      Logger.d(TAG, "read saved book url: " + url);
    }
    initSoundPool();
    handler = new Handler(handlerCallback);
    showProgress(getString(R.string.hfx_res_loading));
    initJsonData();

    initExoAudioPlayer();
  }

  private void initExoAudioPlayer() {
    exoAudioPlayer = AudioPlayerFactory
        .getInstance().createPlayer(getApplicationContext(), Constants.EXO);
    exoAudioPlayer.setPlayerCallBack(new AudioCallBack() {

      @Override
      public void playUpdate(long currentTime, long bufferTime, long totalTime) {
        int progress = totalTime <= 0 ? 0 : (int) (1000 * currentTime / totalTime);
        listeningProgress.setMax(1000);
        listeningProgress.setProgress(progress);
      }

      @Override
      public void playStateChange(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
          setState(State.IDLE);
        }
      }
    });
  }

  private void initSoundPool() {
    soundDisposable = Observable.fromCallable(new Callable<String[]>() {
      @Override
      public String[] call() throws Exception {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        String[] soundNames = getAssets().list("eval_sounds");
        for (String soundName : soundNames) {
          AssetFileDescriptor descriptor = getAssets().openFd("eval_sounds/" + soundName);
          int soundId = soundPool.load(descriptor, 1);
          soundIds.put(soundName, soundId);
        }
        return soundNames;
      }
    }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<String[]>() {
          @Override
          public void accept(String[] strings) throws Exception {
            soundInit = true;
            onResLoadFinish();
          }
        });
  }

  private void findViews() {
    //根布局
    clRoot = findViewById(R.id.clRoot);
    mBgImg = findViewById(R.id.view_pager_bg_img);
    //录音准备
    readyLayout = findViewById(R.id.readyLayout);
    tvCountDown = findViewById(R.id.tvCountDown);
    //听录音布局
    listeningLayout = findViewById(R.id.listening_layout);
    listeningProgress = findViewById(R.id.listening_progress);
    listeningStop = findViewById(R.id.listening_stop);
    listeningLayout.setOnClickListener(this);
    listeningStop.setOnClickListener(this);

    //顶部布局
    topLayout = findViewById(R.id.topLayout);
    ivBack = findViewById(R.id.iv_back);
    btnCommit = findViewById(R.id.submit_btn);
    scoreList = findViewById(R.id.scoreList);
    btnCommit.setOnClickListener(this);
    ivBack.setOnClickListener(this);
    viewPager = findViewById(R.id.viewPager);
    tvPageIndex = findViewById(R.id.tvPageIndex);
    scoreList = findViewById(R.id.scoreList);

    tvScore = findViewById(R.id.tvScore);
    tvExplain = findViewById(R.id.tvExplain);
    tvEvalContent = findViewById(R.id.tvEvalContent);
    tvEvalContent.setMovementMethod(new ScrollingMovementMethod());
    scrollView = findViewById(R.id.scroll_view);
    scrollViewLand = findViewById(R.id.scroll_view_land);
    scoreResultList = findViewById(R.id.scoreResultList);
    ivPlayAudio = findViewById(R.id.ivPlayAudio);
    ivPlayRecord = findViewById(R.id.ivPlayRecord);
    ivRecord = findViewById(R.id.ivRecord);
    ivPlayAudio.setOnClickListener(this);
    ivPlayRecord.setOnClickListener(this);
    ivRecord.setOnClickListener(this);
    bottomLayout = findViewById(R.id.bottomLayoutPortrait);
    //横屏布局
    tvScoreLand = findViewById(R.id.tvScoreLand);
    tvExplainLand = findViewById(R.id.tvExplainLand);
    tvEvalContentLand = findViewById(R.id.tvEvalContentLand);
    tvEvalContentLand.setMovementMethod(new ScrollingMovementMethod());
    scoreResultListLand = findViewById(R.id.scoreResultListLand);
    ivPlayAudioLand = findViewById(R.id.ivPlayAudioLand);
    ivPlayRecordLand = findViewById(R.id.ivPlayRecordLand);
    ivRecordLand = findViewById(R.id.ivRecordLand);
    ivPlayAudioLand.setOnClickListener(this);
    ivPlayRecordLand.setOnClickListener(this);
    ivRecordLand.setOnClickListener(this);
    bottomLayoutLand = findViewById(R.id.bottomLayoutLand);
    voiceLine = findViewById(R.id.voiceLine);
    //引导
    guideLayout = findViewById(R.id.guideLayout);
    focusView = findViewById(R.id.focus_view);
    focusView.setOnClickListener(this);
    tvGuideText = findViewById(R.id.tvGuideText);
    tvKnow = findViewById(R.id.tvKnow);
    ivGuideArrow = findViewById(R.id.ivGuideArrow);
    tvKnow.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    //播放状态下 点击任何地方都要暂停
    if (state == State.LISTEN) {
      stopListen();
      return;
    }
    if (id == R.id.submit_btn) {
      submit();
    } else if (id == R.id.ivRecordLand) {
      stopRecording();
    } else if (id == R.id.ivRecord) {
      stopRecording();
    } else if (id == R.id.ivPlayAudio) {
      playAudio();
    } else if (id == R.id.ivPlayAudioLand) {
      playAudio();
    } else if (id == R.id.ivPlayRecord) {
      playRecord();
    } else if (id == R.id.ivPlayRecordLand) {
      playRecord();
    } else if (id == R.id.iv_back) {
      onBackPressed();
    } else if (id == R.id.listening_stop || id == R.id.listening_layout) {
      stopListen();
    } else if (id == R.id.tvKnow) {
      saveGuide();
      showGuide();
    } else if (id == R.id.focus_view) {
      saveGuide();
      showGuide();
    }
  }

  private void stopListen() {
    if (state == State.LISTEN) {
      exoAudioPlayer.stop();
      setState(State.IDLE);
    }
  }

  private void playAudio() {
    if (state != State.IDLE) {
      return;
    }
    File audio = getBookAudioOfCurrentPage();
    playAudio(audio);
  }

  private void playRecord() {
    if (state != State.IDLE) {
      return;
    }
    File audio = getAudioOfCurrentPage();
    playAudio(audio);
  }

  protected void handleEngineError(int errCode, String err) {
    ivRecord.setImageResource(R.drawable.hfx_ic_eval_record);
    ivRecordLand.setImageResource(R.drawable.hfx_ic_eval_record);
    if (errCode == 30000) {
      stopRecord();
    } else {
      viewPager.setLocked(false);
      handler.removeMessages(MESSAGE_RECORD_UPDATE);
      setState(State.IDLE);
      toast(getString(R.string.hfx_eval_failed));
      hideProgress();
      voiceLine.setVisibility(View.GONE);
      voiceLine.setVolume(0);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (viewPager.isLocked()) {
      stopRecord();
      ivRecord.setImageResource(R.drawable.hfx_ic_eval_record);
      ivRecordLand.setImageResource(R.drawable.hfx_ic_eval_record);
    }
  }

  protected void stopRecording() {
    if (!canStop) {
      Logger.d("SingEngine", "stopRecording,  canStop = false");
      return;
    }
    if (viewPager.isLocked()) {
      stopRecord();
      ivRecord.setImageResource(R.drawable.hfx_ic_eval_record);
      ivRecordLand.setImageResource(R.drawable.hfx_ic_eval_record);
    } else {
      handler.sendEmptyMessageDelayed(START_RECORD, 1000);
      viewPager.setLocked(true);
      ivRecord.setEnabled(false);
      ivRecordLand.setEnabled(false);
      tvCountDown.setText("Ready");
      readyLayout.setVisibility(View.VISIBLE);
      playSound(SOUND_READY);
    }
  }

  public void playSound(String id) {
    if (soundIds.containsKey(id)) {
      soundPool.play(soundIds.get(id), 1f, 1f, 0, 0, 1);
    }
  }


  @Override
  protected void startMediaRecorder(File file) throws Exception {
    if (file != null) {
      if (currentPage < book.bookpage.size()) {
        BookPage bookPage = book.bookpage.get(currentPage);
        String content = bookPage.page_content;
        Logger.d("SingEngine", "content = " + content + ", coreType = " + bookPage.coreType);
        if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(bookPage.coreType)) {
          if (engineInitSuccess) {
            if (Utils.isDev(this)) {
              toast(bookPage.coreType);
            }
            if (bookPage.coreType.startsWith("en")) {
              if (bookPage.coreType.contains("sent") || bookPage.coreType.contains("word")) {
                setEvalType("phrases");
              } else {
                setEvalType("paragraph");
              }
            } else if (bookPage.coreType.startsWith("cn")) {
              if (bookPage.coreType.contains("word")) {
                setEvalType("word_cn");
              } else {
                setEvalType("phrases_cn");
              }
            }
            startEngine(content, 0, file.getAbsolutePath());
            voiceLine.setVisibility(View.VISIBLE);
            exoAudioPlayer.setPlayWhenReady(false);
          }
        } else {
          if (volumeCallBack == null) {
            volumeCallBack = new VolumeCallBack() {
              @Override
              public void onCurrentVoice(final double currentVolume) {
                if (voiceLine != null) {
                  runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      voiceLine.setVolume((int) currentVolume);
                      voiceLine.run();
                    }
                  });
                }
              }
            };
            initAudioUtil(volumeCallBack);
          }
          super.startMediaRecorder(file);
          exoAudioPlayer.setPlayWhenReady(false);
          voiceLine.setVisibility(View.VISIBLE);
        }
      }
    }
  }

  @Override
  protected void stopAudioRecord() {
    if (currentPage < book.bookpage.size()) {
      String content = book.bookpage.get(currentPage).page_content;
      viewPager.setLocked(false);
      if (!TextUtils.isEmpty(content)) {
        if (!engineStopped) {
          stopEngine();
        }
        Logger.d("SingEngine", "引擎stop");
        showProgress(getString(R.string.hfx_evaluating));
        voiceLine.setVisibility(View.GONE);
        voiceLine.setVolume(0);
      } else {
        Logger.d("SingEngine", "系统录音stop");
        super.stopAudioRecord();
        checkAudio();
        voiceLine.setVisibility(View.GONE);
        voiceLine.setVolume(0);
        scoreAdapter.notifyItemChanged(currentPage, -1);
        hasRecord = true;
        showGuide();
        File userAudio = getAudioOfCurrentPage();
        if (userAudio.exists()) {
          long length = userAudio.length();
          if (length < 1024) {
            userAudio.delete();
            toast(getString(R.string.hfx_record_short));
          }
        }
        ivPlayRecord.setEnabled(userAudio.exists());
        ivPlayRecordLand.setEnabled(userAudio.exists());
        update();
      }
    }
  }

  private void updateScore(EvalResult evalResult) {
    String indexText = (currentPage + 1) + "/" + book.bookpage.size();
    tvPageIndex.setText(indexText);
    if (evalResult == null) {
      for (int i = 0; i < evalMap.size(); i++) {
        int key = evalMap.keyAt(i);
        if (key == currentPage) {
          EvalResult result = evalMap.get(key);
          if (result == null) {
            setEvalContentVisibility(true);
            return;
          } else {
            evalResult = result;
            break;
          }
        }
      }
    }
    if (evalResult == null) {
      setEvalContentVisibility(true);
      return;
    }
    setEvalContentVisibility(false);
    int resultScore = (int) evalResult.score;
//    int ava = (int) (evalResult.integrity * book.integrityproportion
//        + evalResult.pron * book.pronproportion
//        + evalResult.fluency * book.fluencyproportion);
    //判断是否有值
    if (TextUtils.isEmpty(book.integrityproportion) && TextUtils.isEmpty(book.pronproportion)
        && TextUtils.isEmpty(book.fluencyproportion)) {
      evalResult.scoreDisplay = getScoreDisplay((int) evalResult.score);
    } else {
      float integrity = getFloatValue(book.integrityproportion);
      float pron = getFloatValue(book.pronproportion);
      float fluency = getFloatValue(book.fluencyproportion);
      int ava = (int) (evalResult.integrity * integrity
          + evalResult.pron * pron
          + evalResult.fluency * fluency);
      evalResult.score = ava;
      evalResult.scoreDisplay = getScoreDisplay(ava);
    }
    Logger.d("zkx 更新成绩展示 scoreDisplay " + evalResult.scoreDisplay);
    String scoreText = String.valueOf((int) evalResult.score);
    if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
      if (TextUtils.isEmpty(evalResult.scoreDisplay)) {
        SimplifySpanBuild ssb = new SimplifySpanBuild(this, tvScore);
        ssb.appendSpecialUnit(new SpecialTextUnit(scoreText).setTextSize(30).useTextBold()).appendNormalText("分");
        tvScore.setText(ssb.build());
      } else {
        SimplifySpanBuild ssb = new SimplifySpanBuild(this, tvScore);
        ssb.appendSpecialUnit(new SpecialTextUnit(evalResult.scoreDisplay).setTextSize(30)
            .useTextBold());
        tvScore.setText(ssb.build());
      }

    } else {
      if (TextUtils.isEmpty(evalResult.scoreDisplay)) {
        SimplifySpanBuild ssb = new SimplifySpanBuild(this, tvScoreLand);
        ssb.appendSpecialUnit(new SpecialTextUnit(scoreText).setTextSize(30).useTextBold()).appendNormalText("分");
        tvScoreLand.setText(ssb.build());
      } else {
        SimplifySpanBuild ssb = new SimplifySpanBuild(this, tvScoreLand);
        ssb.appendSpecialUnit(new SpecialTextUnit(evalResult.scoreDisplay).setTextSize(30).
            useTextBold());
        tvScoreLand.setText(ssb.build());
      }
    }
    if (evalResult.score >= 80) {
      tvExplain.setText(R.string.hfx_eval_excellent);
      tvExplainLand.setText(R.string.hfx_eval_excellent);
    } else if (evalResult.score >= 60) {
      tvExplain.setText(R.string.hfx_eval_good);
      tvExplainLand.setText(R.string.hfx_eval_good);
    } else if (evalResult.score >= 30) {
      tvExplain.setText(R.string.hfx_eval_bad);
      tvExplainLand.setText(R.string.hfx_eval_bad);
    } else {
      tvExplain.setText(R.string.hfx_eval_poor);
      tvExplainLand.setText(R.string.hfx_eval_poor);
    }
    scoreResultList.removeAllViews();
    scoreResultListLand.removeAllViews();
    for (int index = 0; index < evalResult.detail.size(); index++) {
      Detail detail = evalResult.detail.get(index);
      View itemView = getLayoutInflater().inflate(R.layout.hfx_layout_eval_item, scoreResultList, false);
      TextView tvWordScore = itemView.findViewById(R.id.tvWordScore);
      TextView tvWord = itemView.findViewById(R.id.tvWord);
      int score = Integer.valueOf(detail.score);
      int scoreColor;
      if (score >= 85) {
        scoreColor = getResources().getColor(R.color.evaluation_score_exl);
        tvWordScore.setBackgroundResource(R.drawable.hfx_evaluation_word_exl);
      } else if (score >= 60) {
        scoreColor = getResources().getColor(R.color.evaluation_score_fine);
        tvWordScore.setBackgroundResource(R.drawable.hfx_evaluation_word_pass);
      } else {
        scoreColor = getResources().getColor(R.color.evaluation_score_fail);
        tvWordScore.setBackgroundResource(R.drawable.hfx_evaluation_word_fail);
      }
      if (!TextUtils.isEmpty(detail.chWord)) {
        tvWord.setText(detail.chWord);
      } else {
        tvWord.setText(detail.word);
      }
      tvWord.setTextColor(scoreColor);
      tvWordScore.setText(String.valueOf(score));
      if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        scoreResultList.addView(itemView);
      } else {
        scoreResultListLand.addView(itemView);
      }
    }

  }

  private void playResultSound(int resultScore) {
    //播放提示音
    if (resultScore >= 60) {
      readyLayout.setVisibility(View.VISIBLE);
      if (resultScore >= 99) {
        playSound(SOUND_UNBELIEVABLE);
        tvCountDown.setText("Unbelievable");
      } else if (resultScore >= 90) {
        playSound(SOUND_PERFECT);
        tvCountDown.setText("Perfect");
      } else if (resultScore >= 80) {
        playSound(SOUND_GREAT);
        tvCountDown.setText("Great");
      } else if (resultScore >= 70) {
        playSound(SOUND_GOOD);
        tvCountDown.setText("Good");
      } else {
        playSound(SOUND_NOT_BAD);
        tvCountDown.setText("Not Bad");
      }
      handler.sendEmptyMessageDelayed(HIDE_RESULT_TEXT, 1000);
    }
  }

  private void setEvalContentVisibility(boolean isVisible) {
    if (isVisible) {
      scoreResultList.setVisibility(View.INVISIBLE);
      scoreResultListLand.setVisibility(View.INVISIBLE);
      scrollView.setVisibility(View.GONE);
      scrollViewLand.setVisibility(View.GONE);
      tvScore.setVisibility(View.INVISIBLE);
      tvScoreLand.setVisibility(View.INVISIBLE);
      tvExplain.setVisibility(View.INVISIBLE);
      tvExplainLand.setVisibility(View.INVISIBLE);
      tvEvalContent.setVisibility(View.VISIBLE);
      tvEvalContentLand.setVisibility(View.VISIBLE);
      String content = book.bookpage.get(currentPage).page_content;
      if (!TextUtils.isEmpty(content)) {
        tvEvalContent.setText(book.bookpage.get(currentPage).page_content);
        tvEvalContentLand.setText(book.bookpage.get(currentPage).page_content);
        tvEvalContent.scrollTo(0, 0);
        tvEvalContentLand.scrollTo(0, 0);
      } else {
        tvEvalContent.setText(R.string.hfx_eval_no_content_hint);
        tvEvalContentLand.setText(R.string.hfx_eval_no_content_hint);
      }
    } else {
      scoreResultList.setVisibility(View.VISIBLE);
      scoreResultListLand.setVisibility(View.VISIBLE);
      scrollView.setVisibility(View.VISIBLE);
      scrollViewLand.setVisibility(View.VISIBLE);
      tvScore.setVisibility(View.VISIBLE);
      tvScoreLand.setVisibility(View.VISIBLE);
      tvExplain.setVisibility(View.VISIBLE);
      tvExplainLand.setVisibility(View.VISIBLE);
      tvEvalContent.setVisibility(View.INVISIBLE);
      tvEvalContentLand.setVisibility(View.INVISIBLE);
    }
  }


  private void saveAndQuit(boolean commit) {
    //保存评测数据
    try {
      File file = new File(FileUtil.getFileCacheDir(this),
          Utils.getLoginUserId(this) + "_eval_" + book.bookid);
      ArrayList<EvalBody> beanList = getEvalBody();
      if (beanList != null && beanList.size() > 0) {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(beanList);
        fos.close();
        oos.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    showProgress(getString(R.string.hfx_saving));
    new SaveQuitTask(this, commit).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }


  @Override
  protected ArrayList<EvalBody> getEvalBody() {
    if (evalMap != null) {
      ArrayList<EvalBody> evalBodies = new ArrayList<>();
      for (int i = 0; i < evalMap.size(); i++) {
        int key = evalMap.keyAt(i);
        EvalResult result = evalMap.get(key);
        EvalBody evalBody = new EvalBody();
        evalBody.exercise_id = key;
        evalBody.engine_used = result.enginetype;
        evalBody.score = (int) result.score;
        evalBody.pron = (int) result.pron;
        evalBody.integrity = (int) result.integrity;
        evalBody.text = result.content;
        evalBody.fluency = (int) result.fluency;
        evalBody.details = result.detail;
        evalBody.mp3name = getAudioOfCurrentPage(book.bookpage.get(key).page_name).getName();
        evalBodies.add(evalBody);
      }
      return evalBodies;
    }
    return null;
  }

  protected void initEvalData() {
    if (book.bookpage != null) {
      int count = 0;
      for (BookPage bookPage : book.bookpage) {
        if (!TextUtils.isEmpty(bookPage.page_content)) {
          count++;
        }
      }
      evalMap = new SparseArray<>(count);
      File file = new File(FileUtil.getFileCacheDir(this),
          Utils.getLoginUserId(this) + "_eval_" + book.bookid);
      if (file.exists()) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
          fis = new FileInputStream(file);
          ois = new ObjectInputStream(fis);
          Object readObject = ois.readObject();
          if (readObject != null) {
            ArrayList<EvalBody> beanList = (ArrayList<EvalBody>) readObject;
            for (int i = 0; i < beanList.size(); i++) {
              EvalBody evalBody = beanList.get(i);
              EvalResult evalResult = new EvalResult();
              //总分
//              evalResult.score = evalBody.score;
              evalResult.enginetype = evalBody.engine_used;
              //标准度
              evalResult.pron = evalBody.pron;
              //完整度
              evalResult.integrity = evalBody.integrity;
              evalResult.content = evalBody.text;
              //流畅度
              evalResult.fluency = evalBody.fluency;
              evalResult.detail = evalBody.details;
              //判断是否有值
              if (TextUtils.isEmpty(book.integrityproportion) && TextUtils.isEmpty(book.pronproportion)
                  && TextUtils.isEmpty(book.fluencyproportion)) {
                evalResult.score = evalBody.score;
              } else {
                float integrity = getFloatValue(book.integrityproportion);
                float pron = getFloatValue(book.pronproportion);
                float fluency = getFloatValue(book.fluencyproportion);
                int ava = (int) (evalBody.integrity * integrity
                    + evalBody.pron * pron
                    + evalBody.fluency * fluency);
                evalResult.score = ava;
              }
              evalResult.scoreDisplay = getScoreDisplay((int) evalResult.score);

              Logger.i("zkx score = " + evalBody.score
                  + " pron = " + evalBody.pron
                  + " integrity = " + evalBody.integrity
                  + " fluency = " + evalBody.fluency
              );
              evalMap.put(evalBody.exercise_id, evalResult);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          try {
            if (fis != null) {
              fis.close();
            }
            if (ois != null) {
              ois.close();
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  /**
   * 将float字符串转换成 float
   */
  private float getFloatValue(String floatStr) {
    try {
      if (TextUtils.isEmpty(floatStr)) {
        return 0;
      } else {
        return Float.parseFloat(floatStr);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return 0;
  }

  /**
   * 获取成绩显示字段
   *
   * @param ava 当前获取的分数
   * @return 要显示的分数
   */
  private String getScoreDisplay(int ava) {
    String scoreDisplay = "";
    Map<String, String> scoreState = book.scoreState;
    if (scoreState != null) {
      Set<String> keys = scoreState.keySet();
      for (String key : keys) {
        String value = scoreState.get(key);
        Logger.d("zkx key = " + key + " value = " + scoreState.get(key));
        try {
          String[] split = value.split("-");
          if (split.length == 2) {
            String start = split[0];
            String end = split[1];
            int min = Integer.parseInt(start);
            int max = Integer.parseInt(end);
            if (ava >= min && ava <= max) {
              scoreDisplay = key;
              break;
            }
          } else if (split.length == 1) {
            String str = split[0];
            int sca = Integer.parseInt(str);
            if (ava == sca) {
              scoreDisplay = key;
              break;
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          Logger.e("解析成绩显示字段出错了 e = " + e.toString());
        }

      }
    }
    return "无".equals(scoreDisplay) ? "N" : scoreDisplay;
  }

  private VoiceEngineCallback engineCallback = new VoiceEngineCallback() {


    @Override
    public void onInitResult(boolean success, int errCode, String errMsg) {
      engineInitSuccess = success;
      if (!engineInitSuccess) {
        showErrorDialog(getString(R.string.hfx_init_failed2), true);
      } else {
        onResLoadFinish();
      }
    }

    @Override
    public void onVolume(int volume) {
      if (voiceLine != null) {
        voiceLine.setVolume(volume * 2);
        voiceLine.run();
      }
    }

    @Override
    public void onResult(Object result) {
      hideProgress();
      EvalResult evalResult = (EvalResult) result;
      File userAudio = getAudioOfCurrentPage();
      if (userAudio.exists()) {
        long length = userAudio.length();
        if (length < 1024) {
          userAudio.delete();
          toast(getString(R.string.hfx_record_low_voice));
        }
      }
      ivPlayRecord.setEnabled(userAudio.exists());
      ivPlayRecordLand.setEnabled(userAudio.exists());
      update();
      if (evalResult != null && "success".equals(evalResult.result_type)) {
        evalResult.enginetype = engineType;
        evalMap.put(currentPage, evalResult);
        updateScore(evalResult);
        playResultSound((int) evalResult.score);
        scoreAdapter.notifyItemChanged(currentPage);
        hasRecord = true;
        showGuide();
      }
    }

    @Override
    public void onCanceled() {
      hideProgress();
      viewPager.setLocked(false);
    }

    @Override
    public void onRecordStop() {

    }

    @Override
    public void onEvalTimeout() {

    }

    @Override
    public void onEvalErr(int errCode, String errMsg) {
      hideProgress();
      viewPager.setLocked(false);
      handleEngineError(errCode, errMsg);
    }
  };

  protected File getAudioOfCurrentPage() {
    BookPage page = book.bookpage.get(currentPage);
    return HfxFileUtil.getUserAudioTempFileByPage(this, workId, "eval_" + page.page_name);
  }

  protected File getAudioOfCurrentPage(int position) {
    BookPage bookPage = book.bookpage.get(position);
    return HfxFileUtil.getUserAudioTempFileByPage(this, workId, "eval_" + bookPage.page_name);
  }

  protected File getAudioOfCurrentPage(String pageName) {
    return HfxFileUtil.getUserAudioTempFileByPage(this, workId, "eval_" + pageName);
  }

  protected File getUserAudioFileByPage(String pageName) {
    return HfxFileUtil.getUserAudioFileByPage(this, workId, "eval_" + pageName);
  }

  @Override
  public void openView(String url) {
    long endTime = System.currentTimeMillis();
    long lastCostTime = PreferenceUtil
        .getSharePref(this, matchId + "_" + Utils.getLoginUserId(this) + "_cost_time", 0L);
    long totalTime = endTime - startTime + lastCostTime;
    String time = Utils.makeTimeString((int) totalTime);
    int avgScore = 0;
    int avgPronScore = 0;
    int avgIntegrityScore = 0;
    int avgFluencyScore = 0;
    if (evalMap != null && evalMap.size() > 0) {
      int key;
      int totalScore = 0;
      int totalPronScore = 0;
      int totalIntegrityScore = 0;
      int totalFluencyScore = 0;
      for (int i = 0; i < evalMap.size(); i++) {
        key = evalMap.keyAt(i);
        EvalResult result = evalMap.get(key);
        if (result != null) {
          totalScore += result.score;
          totalPronScore += result.pron;
          totalIntegrityScore += result.integrity;
          totalFluencyScore += result.fluency;
        }
      }
      avgScore = totalScore / evalMap.size();
      avgFluencyScore = totalFluencyScore / evalMap.size();
      avgIntegrityScore = totalIntegrityScore / evalMap.size();
      avgPronScore = totalPronScore / evalMap.size();
    }
    String stringBuilder = url + "&time_cost=" + time
        + "&score=" + avgScore
        + "&pron=" + avgPronScore
        + "&integrity=" + avgIntegrityScore
        + "&fluency=" + avgFluencyScore;
    WebViewUtil.openView(stringBuilder, "fullscreen", 0);
  }

  protected File getBookConfigFile(String bookId) {
    return HfxFileUtil.getBookEvalConfigFile(this, bookId);
  }

  public void setLayoutOrientation(boolean portrait) {
    if (portrait) {
      bottomLayout.setVisibility(View.VISIBLE);
      bottomLayoutLand.setVisibility(View.GONE);
    } else {
      bottomLayout.setVisibility(View.GONE);
      bottomLayoutLand.setVisibility(View.VISIBLE);
      ConstraintSet constraintSet = new ConstraintSet();
      //顶部控制栏
      constraintSet.clone(clRoot);
      constraintSet.clear(R.id.topLayout);
      constraintSet.connect(R.id.topLayout, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
      constraintSet.connect(R.id.topLayout, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
      constraintSet.connect(R.id.topLayout, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
      constraintSet.constrainWidth(R.id.topLayout, ConstraintSet.MATCH_CONSTRAINT);
      constraintSet.constrainHeight(R.id.topLayout, Utils.dp2px(this, 65));
      //底部控制栏
      constraintSet.clear(R.id.bottomLayoutLand);
      constraintSet.connect(R.id.bottomLayoutLand, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
      constraintSet.connect(R.id.bottomLayoutLand, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
      constraintSet.connect(R.id.bottomLayoutLand, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
      constraintSet.constrainWidth(R.id.bottomLayoutLand, ConstraintSet.MATCH_CONSTRAINT);
      constraintSet.constrainHeight(R.id.bottomLayoutLand, Utils.dp2px(this, 100));
      //中间图片
      constraintSet.clear(R.id.viewPager);
      constraintSet.connect(R.id.viewPager, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
      constraintSet.connect(R.id.viewPager, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
      constraintSet.connect(R.id.viewPager, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
      constraintSet.connect(R.id.viewPager, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
      constraintSet.constrainWidth(R.id.viewPager, ConstraintSet.MATCH_CONSTRAINT);
      constraintSet.constrainHeight(R.id.viewPager, ConstraintSet.MATCH_CONSTRAINT);
      //中间背景
      constraintSet.clear(R.id.bg_layout);
      constraintSet.connect(R.id.bg_layout, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
      constraintSet.connect(R.id.bg_layout, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
      constraintSet.connect(R.id.bg_layout, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
      constraintSet.connect(R.id.bg_layout, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
      constraintSet.constrainWidth(R.id.bg_layout, ConstraintSet.MATCH_CONSTRAINT);
      constraintSet.constrainHeight(R.id.bg_layout, ConstraintSet.MATCH_CONSTRAINT);

      constraintSet.applyTo(clRoot);
    }
    mBgImg.setBackgroundResource(portrait ? R.drawable.hfx_ic_eval_bg : R.drawable.hfx_ic_eval_bg_land);
  }

  private PageImageView.Callback callback = new PageImageView.Callback() {
    @Override
    public void onImageLoaded(boolean isPortrait, int position) {
      //定制屏 强制横屏处理
      if (isCustompad) {
        setOrientation(false);
      } else {
        setOrientation(isPortrait);
      }
      setImageBounds(position);
      showGuide();
    }
  };

  private ViewPager.OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
      if (currentPage != position) {
        currentPage = position;
        File audio = getBookAudioOfCurrentPage();
        ivPlayAudio.setEnabled(audio.exists());
        ivPlayAudioLand.setEnabled(audio.exists());
        File userAudio = getAudioOfCurrentPage();
        ivPlayRecord.setEnabled(userAudio.exists());
        ivPlayRecordLand.setEnabled(userAudio.exists());
        scoreAdapter.lightItemByIndex(position);
        updateScore(null);
        scoreList.smoothScrollToPosition(position);
        setImageBounds(position);
      }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
  };

  private void setImageBounds(int position) {
    EvalHolder evalHolder = evalAdapter.getEvalHolder(position);
    if (evalHolder != null) {
      if (evalHolder.pageImageView != null && evalHolder.pageImageView.isPortrait()) {
        Bitmap bitmap = evalHolder.pageImageView.getBitmap();
        if (bitmap == null) {
          return;
        }
        int[] screenWidth = Utils.getScreenWidth(EvalActivity.this);
        boolean fullscreen = bitmap.getHeight() > (screenWidth[1] - Utils
            .dp2px(EvalActivity.this, 315));
        if (this.imageFullscreen == fullscreen) {
          return;
        }
        this.imageFullscreen = fullscreen;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(clRoot);
        if (fullscreen) {
          constraintSet.connect(R.id.viewPager, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
          constraintSet.connect(R.id.viewPager, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
          constraintSet.clear(R.id.topLayout);
          constraintSet.connect(R.id.topLayout, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
          constraintSet.connect(R.id.topLayout, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
          constraintSet.connect(R.id.topLayout, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
          constraintSet.constrainWidth(R.id.topLayout, ConstraintSet.MATCH_CONSTRAINT);
          constraintSet.constrainHeight(R.id.topLayout, Utils.dp2px(EvalActivity.this, 65));
          constraintSet.clear(R.id.bottomLayoutPortrait);
          constraintSet
              .connect(R.id.bottomLayoutPortrait, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
          constraintSet
              .connect(R.id.bottomLayoutPortrait, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
          constraintSet
              .connect(R.id.bottomLayoutPortrait, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
                  0);
          constraintSet.constrainWidth(R.id.bottomLayoutPortrait, ConstraintSet.MATCH_CONSTRAINT);
          constraintSet.constrainHeight(R.id.bottomLayoutPortrait, Utils
              .dp2px(EvalActivity.this, 250));
        } else {
          constraintSet.connect(R.id.viewPager, ConstraintSet.BOTTOM, R.id.bottomLayoutPortrait, ConstraintSet.TOP, 0);
          constraintSet.connect(R.id.viewPager, ConstraintSet.TOP, R.id.topLayout, ConstraintSet.BOTTOM, 0);
          constraintSet.clear(R.id.topLayout);
          constraintSet.connect(R.id.topLayout, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
          constraintSet.connect(R.id.topLayout, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
          constraintSet.connect(R.id.topLayout, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
          constraintSet.connect(R.id.topLayout, ConstraintSet.BOTTOM, R.id.viewPager, ConstraintSet.TOP, 0);
          constraintSet.constrainWidth(R.id.topLayout, ConstraintSet.MATCH_CONSTRAINT);
          constraintSet.constrainHeight(R.id.topLayout, Utils.dp2px(EvalActivity.this, 65));
          constraintSet.clear(R.id.bottomLayoutPortrait);
          constraintSet
              .connect(R.id.bottomLayoutPortrait, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
          constraintSet
              .connect(R.id.bottomLayoutPortrait, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
          constraintSet
              .connect(R.id.bottomLayoutPortrait, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
                  0);
          constraintSet
              .connect(R.id.bottomLayoutPortrait, ConstraintSet.TOP, R.id.viewPager, ConstraintSet.BOTTOM,
                  0);
          constraintSet.constrainWidth(R.id.bottomLayoutPortrait, ConstraintSet.MATCH_CONSTRAINT);
          constraintSet.constrainHeight(R.id.bottomLayoutPortrait, Utils
              .dp2px(EvalActivity.this, 250));
        }
        constraintSet.applyTo(clRoot);
        topLayout.setVisibility(buttonHide ? View.GONE : View.VISIBLE);
        bottomLayout.setVisibility(buttonHide ? View.GONE : View.VISIBLE);
      }
    }
  }

  private Handler.Callback handlerCallback = new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MESSAGE_RECORD_UPDATE:
          long time = System.currentTimeMillis() - startRecordTime;
          if (time >= RECORD_TIME_LIMIT) {
            stopRecording();
          } else {
            handler.sendEmptyMessageDelayed(MESSAGE_RECORD_UPDATE, 100);
          }
          return true;
        case MESSAGE_HIDE_SHOW_BUTTON:
          buttonShowHideAnim = false;
          return true;
        case START_RECORD:
          ivRecord.setEnabled(true);
          ivRecordLand.setEnabled(true);
          readyLayout.setVisibility(View.GONE);
          tvCountDown.setText("");
          startRecording();
          return true;
        case HIDE_RESULT_TEXT:
          readyLayout.setVisibility(View.GONE);
          tvCountDown.setText("");
          break;
        case MESSAGE_RECORD_MINIMUM:
          canStop = true;
          break;
        default:
          break;
      }
      return false;
    }
  };

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (isOnCommitFragment) {
      //定制屏 强制横屏处理
      setOrientation(!isCustompad);
    } else {
      if (evalAdapter != null) {
        EvalHolder evalHolder = evalAdapter.getEvalHolder(currentPage);
        if (evalHolder != null) {
          //定制屏 强制横屏处理
          if (isCustompad) {
            setOrientation(false);
          } else {
            setOrientation(evalHolder.pageImageView.isPortrait());
          }

        }
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    long endTime = System.currentTimeMillis();
    long lastCostTime = PreferenceUtil
        .getSharePref(this, matchId + "_" + Utils.getLoginUserId(this) + "_cost_time", 0L);
    long totalTime = endTime - startTime + lastCostTime;
    PreferenceUtil
        .setSharePref(this, matchId + "_" + Utils.getLoginUserId(this) + "_cost_time", totalTime);
    handler.removeCallbacksAndMessages(null);
    if (soundPool != null) {
      soundPool.release();
    }
    if (soundDisposable != null && !soundDisposable.isDisposed()) {
      soundDisposable.dispose();
    }
    if (exoAudioPlayer != null) {
      exoAudioPlayer.releasePlayer();
      exoAudioPlayer.setPlayerCallBack(null);
      exoAudioPlayer = null;
    }
  }

  @Override
  protected void setThemeColor() {
    super.setThemeColor();
    statusbarColor = toolbarColor = ContextCompat.getColor(this, R.color.hfx_gray_bg);
    toolbarContentColor = ContextCompat.getColor(this, R.color.hfx_white);
    darkStatusIcon = false;
  }

  private void initJsonData() {
    new InitJsonTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workId, url);
  }

  private class InitJsonTask extends WeakAsyncTask<String, Void, Void, EvalActivity> {

    Huiben book;

    public InitJsonTask(EvalActivity evalActivity) {
      super(evalActivity);
    }

    @Override
    protected Void doInBackground(EvalActivity evalActivity, String... params) {
      Context context = evalActivity.getApplicationContext();
      OkHttpClient okHttpClient = NetWorkHelper.getOkHttpClient();
      String bookId = params[0];
      String url = params[1];
      File file = getBookConfigFile(bookId);
      if (file.exists()) {
        Logger.d(TAG, "read cache: " + file);
        book = Utils.parseJsonFile(file, Huiben.class);
        if (book != null) {
          publishProgress();
        }
      }
      if (!TextUtils.isEmpty(url) && NetworkUtil.isNetworkAvailable(context)) {
        Logger.d(TAG, "request: " + url);
        Request request = new Builder()
            .cacheControl(CacheControl.FORCE_NETWORK)
            .url(Utils.encodeString(url))
            .build();
        try {
          okhttp3.Response response = okHttpClient.newCall(request).execute();
          if (response != null && response.isSuccessful()) {
            String body = response.body().string();
            book = Utils.parseJsonString(body, Huiben.class);
            if (book != null) {
              FileUtil.StringToFile(body, file, "utf-8");
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      return null;
    }

    @Override
    protected void onProgressUpdate(EvalActivity evalActivity, Void... values) {
      if (evalActivity != null && !evalActivity.isFinishing()) {
        evalActivity.onInitJsonDone(book);
      }
    }

    @Override
    protected void onPostExecute(EvalActivity evalActivity, Void aVoid) {
      if (evalActivity != null && !evalActivity.isFinishing()) {
        evalActivity.onInitJsonDone(book);
      }
    }
  }

  private void onInitJsonDone(Huiben book) {
    if (this.book != null) {
      return;
    }
    jsonInit = true;
    onResLoadFinish();
    if (book == null) {
      showErrorDialog(getString(R.string.hfx_loading_fail), true);
    } else {

      this.book = book;
      contentType = book.content_type;
      initEvalData();
      //修改作品，制作中不显示他人作品弹框
      if (needShowothers && !isMaking && book.workuser != null && !book.workuser.isEmpty()) {
        showWorkersDialog();
      } else {
        checkOrInit();
      }
    }
  }

  private void showWorkersDialog() {
    new AlertDialog.Builder(this)
        .setTitle(R.string.hfx_workers_title)
        .setAdapter(new WorkerAdapter(this, book.workuser), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(EvalActivity.this, ReadBookActivity.class);
            intent.putExtra(ReadBookActivity.ARG_JSON_URL, book.workuser.get(which).url);
            startActivity(intent);
            finish();
          }
        })
        .setPositiveButton(R.string.hfx_workers_btn,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                showWarning();
              }
            })
        .setNegativeButton(R.string.hfx_limit_quit,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                finish();
              }
            })
        .setCancelable(false)
        .create().show();
  }

  private class WorkerAdapter extends ArrayAdapter<WorkUser> {

    LayoutInflater mInflater;

    public WorkerAdapter(Context context, List<WorkUser> data) {
      super(context, 0, data);
      mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = mInflater.inflate(R.layout.hfx_layout_worker_item, parent, false);
      }
      CircleImageView imageView = convertView.findViewById(R.id.speaker_header);
      TextView title = convertView.findViewById(R.id.speaker_title);
      TextView info = convertView.findViewById(R.id.speaker_info);
      TextView time = convertView.findViewById(R.id.speaker_time);
      TextView comment = convertView.findViewById(R.id.speaker_comment);
      WorkUser worker = getItem(position);
      RequestOptions options = new RequestOptions()
          .skipMemoryCache(true);
      Glide.with(getApplicationContext())
          .asBitmap()
          .load(Utils.encodeString(worker.headimage))
          .apply(options)
          .into(imageView);
      title.setText(worker.alias);
      info.setText(worker.introduce);
      time.setText(worker.pubdate);
      String commentcount = Utils.formatCount(getContext(), worker.commentcount);
      String readcount = Utils.formatCount(getContext(), worker.readcount);
      String commentString = "";
      if (worker.commentcount > 0) {
        commentString += commentcount + getString(R.string.hfx_unit_eval);
      }
      if (worker.readcount > 0) {
        commentString += " " + readcount + getString(R.string.hfx_unit_read);
      }
      comment.setText(commentString);
      return convertView;
    }
  }

  private void showWarning() {
    if (book.workuser.size() <= 20) {
      checkOrInit();
    } else {
      showDialog(getString(R.string.hfx_tip),
          getString(R.string.hfx_limit_title, book.workuser.size()),
          getString(R.string.hfx_continue),
          new OnClickListener() {
            @Override
            public void onClick(View v) {
              checkOrInit();
            }
          },
          getString(R.string.hfx_exit),
          new OnClickListener() {
            @Override
            public void onClick(View v) {
              finish();
            }
          });
    }
  }

  private void checkOrInit() {
    if (NetworkUtil.isNetworkAvailable(this)) {
      initRes();
    } else {
      showErrorDialog(getString(R.string.hfx_error_network), true);
    }
  }

  private void initRes() {
    showInitDialog();
    initResTask = new InitResTask(this, true);
    initResTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workId, userUrl);
  }

  private void onResLoadFinish() {
    boolean isPageSuccess = initResTask == null || !initResTask.isPageFail;
    if (jsonInit && engineInitSuccess && soundInit && isPageSuccess) {
      hideProgress();
    }
  }

  private void showInitDialog() {
    showDeterminateProgress(getString(R.string.hfx_init),
        getString(R.string.hfx_res_loading),
        getString(R.string.hfx_cancel),
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (initResTask != null) {
              initResTask.cancel(true);
              initResTask = null;
            }
            finish();
          }
        });

  }

  private void updateInitDialog(int type, int progress, int max) {
    switch (type) {
      case 0:
        updateDeterminateProgress(getString(R.string.hfx_image_loading, progress, max),
            progress * 100 / max);
        break;
      case 1:
        updateDeterminateProgress(getString(R.string.hfx_audio_loading, progress, max),
            progress * 100 / max);

        break;
      case 2:
        updateDeterminateProgress(getString(R.string.hfx_user_audio_loading, progress, max),
            progress * 100 / max);

        break;
      case 3:
        updateDeterminateProgress(getString(R.string.hfx_local_loading, progress, max),
            progress * 100 / max);
        break;
    }
  }

  private void hideInitDialog() {
    hideDeterminateProgress();
  }

  private static class InitResTask extends WeakAsyncTask<String, Integer, Void, EvalActivity> {

    boolean showTips;
    private boolean isPageFail = false;

    public InitResTask(EvalActivity evalActivity, boolean showTips) {
      super(evalActivity);
      this.showTips = showTips;
    }

    @Override
    protected Void doInBackground(EvalActivity evalActivity, String... params) {
      Context context = evalActivity.getApplicationContext();
      OkHttpClient okHttpClient = NetWorkHelper.getOkHttpClient();
      String bookId = params[0];
      String userUrl = params[1];
      Huiben book = evalActivity.book;
      initBookPage(context, bookId, okHttpClient, book.bookpage);
      initBookAudio(context, bookId, okHttpClient, book.bookaudio);
      if (!TextUtils.isEmpty(userUrl) && !evalActivity.isMaking && NetworkUtil
          .isNetworkAvailable(context)) {
        Logger.d(TAG, "request user url: " + userUrl);
        Request request = new Builder()
            .cacheControl(CacheControl.FORCE_NETWORK)
            .url(Utils.encodeString(userUrl))
            .build();
        try {
          okhttp3.Response response = okHttpClient.newCall(request).execute();
          if (response != null && response.isSuccessful()) {
            String body = response.body().string();
            Huiben userData = Utils.parseJsonString(body, Huiben.class);
            if (userData != null) {
              initUserAudio(context, bookId, okHttpClient, userData.bookaudio);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      //异常情况下以temp目录数据为准，相当于异常情况下默认帮用户保存了一下
//      copyTempToWork(context, bookId);
//      copyWorkToTemp(context, bookId);
      return null;
    }

    private void initBookPage(Context context, String bookId, OkHttpClient okHttpClient,
                              List<BookPage> pages) {
      if (pages != null) {
        int i = 0;
        Properties properties = HfxFileUtil.getBookProp(context, bookId);
        for (BookPage page : pages) {
          if (isCancelled()) {
            return;
          }
          publishProgress(0, ++i, pages.size());
          boolean needUpdate = true;
          if (properties != null) {
            String md5Url = properties.getProperty(page.page_name);
            needUpdate =
                md5Url == null || !md5Url.equals(MD5Util.md5(page.page_url));
          }
          File file = HfxFileUtil.getBookImageFile(context, bookId, page.page_name);
          if (!file.exists() || needUpdate) {
            Logger.d(TAG, "request image: " + page.page_name);
            Request request = new Builder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .url(Utils.encodeString(page.page_url))
                .build();
            try {
              okhttp3.Response response = okHttpClient.newCall(request).execute();
              if (response != null && response.isSuccessful()) {
                InputStream is = response.body().byteStream();
                FileUtil.inputStreamToFile(is, file);
                is.close();
                HfxFileUtil.putBookProp(context, bookId, page.page_name,
                    MD5Util.md5(page.page_url));
              } else {
                isPageFail = true;
              }
            } catch (Exception e) {
              isPageFail = true;
              e.printStackTrace();
            }
          } else {

            Logger.d(TAG, "skip image: " + page.page_name);
          }
        }
      }
    }

    private void initBookAudio(Context context, String bookId, OkHttpClient okHttpClient,
                               List<BookAudio> audios) {
      if (audios != null) {
        int i = 0;
        Properties properties = HfxFileUtil.getBookProp(context, bookId);
        for (BookAudio audio : audios) {
          if (isCancelled()) {
            return;
          }
          publishProgress(1, ++i, audios.size());
          boolean needUpdate = true;
          if (properties != null) {
            String md5Url = properties.getProperty(audio.mp3_name);
            needUpdate =
                md5Url == null || !md5Url.equals(MD5Util.md5(audio.mp3_url));
          }
          File file = HfxFileUtil.getBookAudioFile(context, bookId, audio.mp3_name);
          if (!file.exists() || needUpdate) {
            Logger.d(TAG, "request mp3: " + audio.mp3_name);
            Request request = new Builder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .url(Utils.encodeString(audio.mp3_url))
                .build();
            try {
              okhttp3.Response response = okHttpClient.newCall(request).execute();
              if (response != null && response.isSuccessful()) {
                InputStream is = response.body().byteStream();
                FileUtil.inputStreamToFile(is, file);
                is.close();
                HfxFileUtil.putBookProp(context, bookId, audio.mp3_name,
                    MD5Util.md5(audio.mp3_url));
              } else {
                isPageFail = true;
              }
            } catch (Exception e) {
              isPageFail = true;
              e.printStackTrace();


            }
          } else {
            Logger.d(TAG, "skip mp3: " + audio.mp3_name);
          }
        }
      }
    }

    private void initUserAudio(Context context, String bookId, OkHttpClient okHttpClient,
                               List<BookAudio> audios) {
      if (audios != null) {
        int i = 0;
        for (BookAudio audio : audios) {
          if (isCancelled()) {
            return;
          }
          File file = HfxFileUtil.getUserAudioFile(context, bookId, audio.mp3_name);
          if (!file.exists()) {
            Logger.d(TAG, "request user mp3: " + audio.mp3_name);
            Request request = new Builder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .url(Utils.encodeString(audio.mp3_url))
                .build();
            try {
              okhttp3.Response response = okHttpClient.newCall(request).execute();
              if (response != null && response.isSuccessful()) {
                InputStream is = response.body().byteStream();
                FileUtil.inputStreamToFile(is, file);
                is.close();
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
          publishProgress(2, ++i, audios.size());
        }
      }
    }

    @Override
    protected void onCancelled(EvalActivity evalActivity, Void aVoid) {
      Logger.w(TAG, "[InitResTask] onCancelled");
    }

    @Override
    protected void onProgressUpdate(EvalActivity evalActivity, Integer... values) {
      if (evalActivity != null && !evalActivity.isFinishing()) {
        int type = values[0];
        int progress = values[1];
        int max = values[2];
        evalActivity.updateInitDialog(type, progress, max);
      }
    }

    @Override
    protected void onPostExecute(EvalActivity evalActivity, Void aVoid) {
      if (evalActivity != null && !evalActivity.isFinishing()) {
        evalActivity.hideInitDialog();
        if (isPageFail) {
          evalActivity.showErrorDialog(evalActivity.getString(R.string.hfx_loading_res_failed), true);
        } else {
          evalActivity.onResLoadFinish();
          evalActivity.showRecordTips(showTips);
        }

      }
    }
  }

  private void showRecordTips(boolean showTips) {
    if (showTips) {
      View view = LayoutInflater.from(this).inflate(R.layout.hfx_layout_record_step, null);
      TextView mContent = view.findViewById(R.id.notice_content);
      //在线模式提示语
      if (isOnline) {
        mContent.setText(getResources().getString(R.string.hfx_online_tips_message));
      }
      new AlertDialog.Builder(this)
          .setView(view)
          .setCancelable(false)
          .setPositiveButton(R.string.hfx_confirm,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  init();
                }
              }).create().show();
    } else {
      init();
    }
  }

  private void init() {
    PermissionUtil.requestPermissionWithFinish(this, new GrantedCallback() {
      @Override
      public void action() {

      }
    }, permission.RECORD_AUDIO);
    initCurrentPage();
    update();
    setState(State.IDLE);
  }

  private void initCurrentPage() {
    int i = 0;
    for (BookPage page : book.bookpage) {
      File audioTemp = getAudioOfCurrentPage(page.page_name);
      Log.d("delete", "初始化，文件是否存在：" + audioTemp.exists() + ", 文件路径：" + audioTemp.getAbsolutePath());
      if (!audioTemp.exists()) {
        currentPage = i;
        break;
      }
      i++;
    }
    if (currentPage >= book.bookpage.size()) {
      currentPage = book.bookpage.size() - 1;
    }
    if (evalAdapter == null) {
      evalAdapter = new EvalAdapter();
      viewPager.setAdapter(evalAdapter);
      viewPager.addOnPageChangeListener(onPageChangeListener);
    } else {
      evalAdapter.notifyDataSetChanged();
    }
    updateScore(null);
    initTopScoreList();
    viewPager.setCurrentItem(currentPage);
    scoreAdapter.lightItemByIndex(currentPage);
    File userAudio = getAudioOfCurrentPage();
    ivPlayRecord.setEnabled(userAudio.exists());
    ivPlayRecordLand.setEnabled(userAudio.exists());
  }

  private void resetGuide() {
    PreferenceUtil.setSharePref(this, "guide_play_audio", false);
    PreferenceUtil.setSharePref(this, "guide_record", false);
    PreferenceUtil.setSharePref(this, "guide_play_record", false);
    PreferenceUtil.setSharePref(this, "guide_slide", false);
  }

  private void saveGuide() {
    if (currentGuide == GUIDE_PLAY_AUDIO_PORTRAIT || currentGuide == GUIDE_PLAY_AUDIO_LAND) {
      PreferenceUtil.setSharePref(this, "guide_play_audio", true);
    } else if (currentGuide == GUIDE_RECORD_PORTRAIT || currentGuide == GUIDE_RECORD_LAND) {
      PreferenceUtil.setSharePref(this, "guide_record", true);
    } else if (currentGuide == GUIDE_PLAY_RECORD_PORTRAIT || currentGuide == GUIDE_PLAY_RECORD_LAND) {
      PreferenceUtil.setSharePref(this, "guide_play_record", true);
    } else if (currentGuide == GUIDE_SLIDE_PORTRAIT || currentGuide == GUIDE_SLIDE_LAND) {
      PreferenceUtil.setSharePref(this, "guide_slide", true);
    }
  }

  private void showGuide() {
    if (!hasRecord) {
      currentGuide = getFirstGuide();
      if (currentGuide == -1) {
        guideLayout.setVisibility(View.GONE);
        return;
      }
    } else {
      currentGuide = getSecondGuide();
      if (currentGuide == -1) {
        guideLayout.setVisibility(View.GONE);
        return;
      }
    }
    if (ivPlayAudioLand.getWidth() == 0) {
      ivPlayAudioLand.postDelayed(new Runnable() {
        @Override
        public void run() {
          updateGuide();
        }
      }, 300);
    } else {
      updateGuide();
    }
  }

  private int getFirstGuide() {
    boolean isPortrait = getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    //播放原音
    boolean audioGuide = PreferenceUtil.getSharePref(this, "guide_play_audio", false);
    if (!audioGuide) {
      return isPortrait ? GUIDE_PLAY_AUDIO_PORTRAIT : GUIDE_PLAY_AUDIO_LAND;
    }
    //录音按钮
    boolean recordGuide = PreferenceUtil.getSharePref(this, "guide_record", false);
    if (!recordGuide) {
      return isPortrait ? GUIDE_RECORD_PORTRAIT : GUIDE_RECORD_LAND;
    }
    return -1;
  }

  private int getSecondGuide() {
    boolean isPortrait = getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    boolean playRecordGuide = PreferenceUtil.getSharePref(this, "guide_play_record", false);
    if (!playRecordGuide) {
      return isPortrait ? GUIDE_PLAY_RECORD_PORTRAIT : GUIDE_PLAY_RECORD_LAND;
    }
    boolean slideGudie = PreferenceUtil.getSharePref(this, "guide_slide", false);
    if (!slideGudie) {
      return isPortrait ? GUIDE_SLIDE_PORTRAIT : GUIDE_SLIDE_LAND;
    }
    return -1;
  }

  private void updateGuide() {
    guideLayout.setVisibility(View.VISIBLE);
    focusView.reset();
    int[] screenWidth = Utils.getScreenWidth(this);
    ConstraintSet constraintSet = new ConstraintSet();
    if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
      TransitionManager.beginDelayedTransition(guideLayout);
    }
    constraintSet.clone(guideLayout);
    switch (currentGuide) {
      case GUIDE_PLAY_AUDIO_PORTRAIT: {
        float x = ivPlayAudio.getLeft() + (ivPlayAudio.getRight() - ivPlayAudio.getLeft()) / 2f;
        float y = screenWidth[1] - Utils.dp2px(this, 23) - Utils.dp2px(this, 45) / 2f  - Utils.getStatusBarHeight(this);
        focusView.addCircle(x, y, Utils.dp2px(this, 48) / 2f);
        tvGuideText.setText(R.string.hfx_guide_play_audio);
        constraintSet.setVerticalBias(R.id.guideBg, 0.75f);
        constraintSet.setHorizontalBias(R.id.guideBg, 0.4f);
      }
      break;
      case GUIDE_PLAY_AUDIO_LAND: {
        float x = ivPlayAudioLand.getLeft() + ivPlayAudioLand.getWidth() / 2f;
        float y = screenWidth[1] - Utils.dp2px(this, 101) / 2f - Utils.getStatusBarHeight(this);
        focusView.addCircle(x, y, Utils.dp2px(this, 48) / 2f);
        tvGuideText.setText(R.string.hfx_guide_play_audio);
        constraintSet.setVerticalBias(R.id.guideBg, 0.4f);
        constraintSet.setHorizontalBias(R.id.guideBg, 0.7f);
        ivGuideArrow.setImageResource(R.drawable.hfx_guide_bottom_right);
        constraintSet.setHorizontalBias(R.id.ivGuideArrow, 0.7f);
      }
      break;
      case GUIDE_RECORD_PORTRAIT: {
        float x = ivRecord.getLeft() + (ivRecord.getRight() - ivRecord.getLeft()) / 2f;
        float y = screenWidth[1] - Utils.dp2px(this, 13) - Utils.dp2px(this, 65) / 2f - Utils.getStatusBarHeight(this);
        focusView.addCircle(x, y, Utils.dp2px(this, 68) / 2f);
        tvGuideText.setText(R.string.hfx_guide_record);
        constraintSet.setVerticalBias(R.id.guideBg, 0.7f);
        constraintSet.setHorizontalBias(R.id.guideBg, 0.7f);
      }
      break;
      case GUIDE_RECORD_LAND: {
        float x = ivRecordLand.getLeft() + (ivRecordLand.getRight() - ivRecordLand.getLeft()) / 2f;
        float y = screenWidth[1] - Utils.dp2px(this, 101) / 2f - Utils.getStatusBarHeight(this);
        focusView.addCircle(x, y, Utils.dp2px(this, 68) / 2f);
        tvGuideText.setText(R.string.hfx_guide_record);
        constraintSet.setVerticalBias(R.id.guideBg, 0.4f);
        constraintSet.setHorizontalBias(R.id.guideBg, 0.8f);
        ivGuideArrow.setImageResource(R.drawable.hfx_guide_bottom_right);
        constraintSet.setHorizontalBias(R.id.ivGuideArrow, 0.75f);
      }
      break;
      case GUIDE_PLAY_RECORD_PORTRAIT: {
        float x = ivPlayRecord.getLeft() + (ivPlayRecord.getRight() - ivPlayRecord.getLeft()) / 2f;
        float y = screenWidth[1] - Utils.dp2px(this, 23) - Utils.dp2px(this, 45) / 2f - Utils.getStatusBarHeight(this);
        focusView.addCircle(x, y, Utils.dp2px(this, 48) / 2f);
        tvGuideText.setText(R.string.hfx_guide_play_record);
        constraintSet.setVerticalBias(R.id.guideBg, 0.75f);
        constraintSet.setHorizontalBias(R.id.guideBg, 0.6f);
        ivGuideArrow.setImageResource(R.drawable.hfx_guide_bottom_right);
        constraintSet.setHorizontalBias(R.id.ivGuideArrow, 0.7f);
      }
      break;
      case GUIDE_PLAY_RECORD_LAND: {
        float x = ivPlayRecordLand.getLeft() + (ivPlayRecordLand.getRight() - ivPlayRecordLand.getLeft()) / 2f;
        float y = screenWidth[1] - Utils.dp2px(this, 101) / 2f - Utils.getStatusBarHeight(this);
        focusView.addCircle(x, y, Utils.dp2px(this, 48) / 2f);
        tvGuideText.setText(R.string.hfx_guide_play_record);
        constraintSet.setVerticalBias(R.id.guideBg, 0.4f);
        constraintSet.setHorizontalBias(R.id.guideBg, 0.95f);
        ivGuideArrow.setImageResource(R.drawable.hfx_guide_bottom_right);
        constraintSet.setHorizontalBias(R.id.ivGuideArrow, 0.75f);
      }
      break;
      case GUIDE_SLIDE_PORTRAIT: {
        focusView
            .addRect(0, Utils.dp2px(this, 65), screenWidth[0], screenWidth[1] - Utils
                .dp2px(this, 250), 0, 0);
        tvGuideText.setText(R.string.hfx_guide_slide_image);
        constraintSet.setVerticalBias(R.id.guideBg, 0.85f);
        constraintSet.setHorizontalBias(R.id.guideBg, 0.5f);
        ivGuideArrow.setImageResource(R.drawable.hfx_guide_top_left);
        constraintSet.setHorizontalBias(R.id.ivGuideArrow, 0.4f);
        constraintSet.connect(R.id.ivGuideArrow, ConstraintSet.TOP, -1, ConstraintSet.BOTTOM, 0);
        constraintSet
            .connect(R.id.ivGuideArrow, ConstraintSet.BOTTOM, R.id.guideBg, ConstraintSet.TOP, Utils
                .dp2px(this, 15));
        constraintSet.connect(R.id.tvKnow, ConstraintSet.BOTTOM, -1, ConstraintSet.TOP, 0);
        constraintSet
            .connect(R.id.tvKnow, ConstraintSet.TOP, R.id.guideBg, ConstraintSet.BOTTOM, Utils
                .dp2px(this, 15));
      }
      break;
      case GUIDE_SLIDE_LAND: {
        focusView
            .addRect(0, Utils.dp2px(this, 65), screenWidth[0], screenWidth[1] - Utils
                .dp2px(this, 100), 0, 0);
        tvGuideText.setText(R.string.hfx_guide_slide_image);
        constraintSet.setVerticalBias(R.id.guideBg, 0.95f);
        constraintSet.setHorizontalBias(R.id.guideBg, 0.5f);
        ivGuideArrow.setImageResource(R.drawable.hfx_guide_top_left);
        constraintSet.connect(R.id.ivGuideArrow, ConstraintSet.TOP, -1, ConstraintSet.BOTTOM, 0);
        constraintSet.connect(R.id.ivGuideArrow, ConstraintSet.LEFT, -1, ConstraintSet.LEFT, 0);
        constraintSet.connect(R.id.ivGuideArrow, ConstraintSet.RIGHT, -1, ConstraintSet.RIGHT, 0);
        constraintSet
            .connect(R.id.ivGuideArrow, ConstraintSet.RIGHT, R.id.guideBg, ConstraintSet.LEFT, 0);
        constraintSet.connect(R.id.ivGuideArrow, ConstraintSet.BOTTOM, R.id.guideBg, ConstraintSet.BOTTOM, 0);
        constraintSet.setMargin(R.id.ivGuideArrow, ConstraintSet.END, Utils.dp2px(this, 20));
        constraintSet.setMargin(R.id.ivGuideArrow, ConstraintSet.BOTTOM, Utils.dp2px(this, 10));
        constraintSet.connect(R.id.tvKnow, ConstraintSet.BOTTOM, -1, ConstraintSet.TOP, 0);
        constraintSet.connect(R.id.tvKnow, ConstraintSet.RIGHT, -1, ConstraintSet.RIGHT, 0);
        constraintSet.connect(R.id.tvKnow, ConstraintSet.BOTTOM, R.id.guideBg, ConstraintSet.BOTTOM, 0);
        constraintSet
            .connect(R.id.tvKnow, ConstraintSet.TOP, R.id.guideBg, ConstraintSet.TOP, 0);
        constraintSet
            .connect(R.id.tvKnow, ConstraintSet.LEFT, R.id.guideBg, ConstraintSet.RIGHT, 0);
        constraintSet.setMargin(R.id.tvKnow, ConstraintSet.START, Utils.dp2px(this, 10));
      }
      break;
      default:
        break;
    }
    constraintSet.applyTo(guideLayout);
  }

  private void initTopScoreList() {
    if (scoreAdapter == null) {
      scoreAdapter = new ScoreAdapter();
      scoreList.setAdapter(scoreAdapter);
      scoreList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    } else {
      scoreAdapter.notifyDataSetChanged();
    }
  }

  protected void update() {
    int progress = 0;
    for (BookPage page : book.bookpage) {
      File audioTemp = getAudioOfCurrentPage(page.page_name);
      if (audioTemp.exists()) {
        progress++;
      }
    }
    btnCommit.setEnabled(100 * progress / book.bookpage.size() >= 50);
  }

  private File getBookAudioOfCurrentPage() {
    BookPage page = book.bookpage.get(currentPage);
    return HfxFileUtil.getBookAudioFileByPage(this, workId, page.page_name);
  }


  private void playAudio(File audio) {
    if (viewPager.isLocked()) {
      return;
    }
    if (audio.exists()) {
      setState(State.LISTEN);
      exoAudioPlayer.play(Uri.fromFile(audio));
    } else {
      toast(getString(R.string.hfx_error_listen));
    }

  }

  private void deleteAudioOfCurrentPage() {
    File file = getAudioOfCurrentPage();
    if (file.exists()) {
      if (file.delete()) {
        isChanged = true;
        update();
      } else {
        showErrorDialog(getString(R.string.hfx_record_delete_fail), false);
      }
    }
  }

  private void setState(State state) {
    if (this.state == state) {
      Logger.e(TAG, "set state but already in: " + state);
      return;
    }
    this.state = state;
    switch (state) {
      case IDLE:
        listeningLayout.setVisibility(View.GONE);
        scoreList.setEnabled(true);
        viewPager.setEnabled(true);
        break;
      case RECORD:
        scoreList.setEnabled(false);
        viewPager.setEnabled(false);
        btnCommit.setEnabled(false);
        listeningLayout.setVisibility(View.GONE);
        break;
      case LISTEN:
        scoreList.setEnabled(false);
        viewPager.setEnabled(false);
        listeningProgress.setProgress(0);
        listeningLayout.setVisibility(View.VISIBLE);
        break;
      default:
        break;
    }
  }

  protected void startRecording() {
    if (state != State.IDLE) {
      Logger.d("SingEngine", "startRecording,  return");
      return;
    }
    ivRecord.setImageResource(R.drawable.hfx_ic_eval_recording);
    ivRecordLand.setImageResource(R.drawable.hfx_ic_eval_recording);
    setState(State.RECORD);
    File audio = getAudioOfCurrentPage();
    try {
      Logger.d("SingEngine", "startRecording, 开始");
      startMediaRecorder(audio);
      startRecordTime = System.currentTimeMillis();
      handler.sendEmptyMessage(MESSAGE_RECORD_UPDATE);
      canStop = false;
      handler.sendEmptyMessageDelayed(MESSAGE_RECORD_MINIMUM, 2000);
    } catch (Exception e) {
      e.printStackTrace();
      Logger.d("SingEngine", "startRecording,  exception");
      Logger.e("zkx startRecording e = " + e.toString());
      checkAudio();
      isChanged = true;
      update();
      setState(State.IDLE);
    }
  }

  protected void stopRecord() {
    handler.removeMessages(MESSAGE_RECORD_UPDATE);
    stopAudioRecord();
    setState(State.IDLE);
    isChanged = true;
  }

  private void checkAudio() {
    File audio = getAudioOfCurrentPage();
    if (!audio.exists() || audio.length() == 0) {
      showDialog(
          getString(R.string.hfx_tip),
          getString(R.string.hfx_init_failed),
          getString(R.string.hfx_cancel),
          new OnClickListener() {
            @Override
            public void onClick(View v) {
              deleteAudioOfCurrentPage();
            }
          });
    }
  }

  @Override
  public void onBackPressed() {
    if (isChanged) {
      //新逻辑 直接保存 不弹框询问
      saveAndQuit(false);
//      showSaveDialog();
    } else {
      finish();
    }
//    resetGuide();
  }

  void saveIntroduce(String introduce) {
    this.introduce = introduce;
    String user_id = Utils.getLoginUserId(getApplicationContext());
    HfxPreferenceUtil.saveBookIntroduce(this, user_id, workId, introduce);
  }

  private void showSaveDialog() {
    showDialog(getString(R.string.hfx_tip),
        getString(R.string.hfx_save_confirm),
        getString(R.string.hfx_save),
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            HfxPreferenceUtil.setRecordBookInWork(EvalActivity.this, user_id, workId, false);
            saveAndQuit(false);
          }
        },
        getString(R.string.hfx_exit),
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            deleteAndQuit();
          }
        },
        getString(R.string.hfx_cancel),
        null);
  }

  private class SaveQuitTask extends WeakAsyncTask<Void, Void, Void, EvalActivity> {

    boolean commit;

    public SaveQuitTask(EvalActivity evalActivity, boolean commit) {
      super(evalActivity);
      this.commit = commit;
    }

    @Override
    protected Void doInBackground(EvalActivity evalActivity, Void... params) {
      Context context = evalActivity.getApplicationContext();
      String bookId = evalActivity.workId;
      List<BookPage> bookpage = evalActivity.book.bookpage;
      File workDir = HfxFileUtil.getUserWorkDir(context, bookId);
      for (BookPage page : bookpage) {
        File audioTemp = getAudioOfCurrentPage(page.page_name);
        File audio = getUserAudioFileByPage(page.page_name);
        if (audioTemp.exists()) {
          Logger.d(TAG, "copy: " + audioTemp + " -> " + workDir);
          try {
            FileUtil.copyFile(audioTemp, workDir, null, true);
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else if (audio.exists() && audio.delete()) {
          Logger.d(TAG, "del: " + audio);
        }
      }
      return null;
    }

    @Override
    protected void onPostExecute(EvalActivity evalActivity, Void aVoid) {
      if (evalActivity != null && !evalActivity.isFinishing()) {
        evalActivity.hideProgress();
        if (commit) {
          //定制屏 强制横屏处理
          evalActivity.setOrientation(!isCustompad);
//          evalActivity.openCommit();
        } else {
          finish();
        }
      }
    }
  }

  private void openCommit() {
    MatchInfo matchInfo = HfxUtil.getMatchInfo(this, book.bookid);
    if (matchInfo != null && !TextUtils.isEmpty(matchInfo.realUrl)) {
      openView(matchInfo.realUrl);
      finish();
    } else {
      isOnCommitFragment = true;
      CommitFragment commitFragment = CommitFragment
          .newInstance(book.bookid, book.icon, book.bookname, book.subtitle, introduce);
      getSupportFragmentManager().beginTransaction()
          .setCustomAnimations(R.anim.hfx_slide_in_bottom, R.anim.hfx_slide_out_bottom,
              R.anim.hfx_slide_in_bottom, R.anim.hfx_slide_out_bottom)
          .replace(R.id.commit_layout, commitFragment)
          .addToBackStack(null)
          .commitAllowingStateLoss();
    }

  }

  private void submit() {
    saveAndQuit(true);
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("match_id", matchId);
    startCommit(workId, mType, jsonObject.toString(), true);
  }

  private Disposable uploadDisposable;
  private String bookId;
  private String params;
  private String qiniu_persistentId;

  private void startCommit(final String id, final String type, final String params,
                           final boolean isEval) {
    if (isCommiting) {
      toast("正在上传作品,请稍候");
      return;
    }
    getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    if (!NetworkUtil.isWiFi(this)) {
      showDialog("提示", getString(R.string.hfx_commit_network_message),
          "确定", new OnClickListener() {
            @Override
            public void onClick(View view) {
              commit(id, type, params, isEval);
            }
          }, "取消", new OnClickListener() {
            @Override
            public void onClick(View view) {
//              worksUploadResult(false, "uploadcancel");
            }
          });
    } else {
      commit(id, type, params, isEval);
    }
  }


  private void commit(String id, String type, String params, boolean isEval) {
    isCommiting = true;
    this.params = params;
    contentType = type;
    strings = id.split("_");
    if ("freevideo".equals(type)) {
      startVideoProcess(id);
    } else if ("audiobook".equals(type) || "englishbook".equals(type) || "lianhuanhua"
        .equals(type)) {
      bookId = id;
      commitWork(HfxFileUtil.HUIBEN_WORK, isEval);
    } else if ("freeaudio".equals(type)) {
      bookId = id;
      mp3File = HfxFileUtil.getStoryAudioFile(this, id);
      photoFile = HfxFileUtil.getCoverFile(this, id);
      commitWork(HfxFileUtil.AUDIO_WORK, isEval);
    } else {
      isCommiting = false;
      toast("不支持的作品类型");
    }
  }

  private void commitWork(final String workType, boolean isEval) {
    UploadInfo uploadInfo = HfxUtil.getDirectUploadInfo(this, bookId);
    commitWork(uploadInfo != null && uploadInfo.direct_upload, workType, isEval);
  }

  private void commitWork(boolean direct_upload, final String workType, boolean isEval) {
    //    if (isCommiting) {
    //      toast("正在上传作品,请稍候");
    //      return;
    //    }
    if (direct_upload && workType.equals(HfxFileUtil.HUIBEN_WORK)) {
      toast("不支持的上传方式");
      return;
    }
    if (uploadDisposable != null && !uploadDisposable.isDisposed()) {
      uploadDisposable.dispose();
    }
    isCommiting = true;
    showDeterminateProgress("提交作品", "正在上传...", "取消", new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (uploadDisposable != null && !uploadDisposable.isDisposed()) {
          uploadDisposable.dispose();
          uploadDisposable = null;
        }
        isCommiting = false;
//        worksUploadResult(false, "cancel");
      }
    });
    if (direct_upload) {
      int duration = PreferenceUtil.getSharePref(this, "story_record_time_" + bookId, 0);
      uploadDisposable = HFXWorksUtil
          .startUploadAudioToOss(this, duration, bookId, workType, null, params, contentType,
              strings, null, null, null, new UploadCallback(workType, direct_upload));
    } else {
      uploadDisposable = HFXWorksUtil
          .startZipAndUploadToOss(this, bookId, workType, null, params, contentType,
              strings, null, null, null, isEval,
              new UploadCallback(workType, direct_upload));
    }
  }

  class UploadCallback implements HFXWorksUtil.ZipAndOssCallback {

    private String workType;
    private boolean withoutZip;

    public UploadCallback(String workType, boolean withoutZip) {
      this.workType = workType;
      this.withoutZip = withoutZip;
    }

    @Override
    public void onError(String error) {
      hideDeterminateProgress();
      isCommiting = false;
      showErrorDialog(error, false);
//      worksUploadResult(false, "uploadfail");
    }

    @Override
    public void onLoginError() {
      hideDeterminateProgress();
      isCommiting = false;
      login();
//      worksUploadResult(false, "uploadfail");
    }

    @Override
    public void onZipProgress(int current, int total) {
      updateDeterminateProgress("正在打包..." + "[" + current + "/" + total + "]",
          100 * current / total);
    }

    @Override
    public void onUploadProgress(String mediaType, long current, long total) {
      String c = Utils.formatLengthString(current);
      String t = Utils.formatLengthString(total);
      updateDeterminateProgress("正在上传" + mediaType
              + "..." + "[" + c + "/" + t + "]",
          (int) (100 * current / total));
    }

    @Override
    public void onSuccess() {
      isCommiting = false;
      hideDeterminateProgress();
      if (HfxFileUtil.HUIBEN_WORK.equals(workType)) {
        String user_id = Utils.getLoginUserId(getApplicationContext());
        HfxPreferenceUtil
            .setRecordBookInWork(getApplicationContext(), user_id, bookId, true);
        toast(getString(R.string.common_submitsucc_title));
        finish();
      } else if (HfxFileUtil.AUDIO_WORK.equals(workType)) {
        mp3File.delete();
        photoFile.delete();
      }
//      worksUploadResult(true, "success");
    }
  }

  protected VideoInfo videoInfo;

  private void startVideoProcess(String videoId) {
    videoInfo = HfxUtil.getVideoInfo(this, videoId);
    if (videoInfo == null) {
      isCommiting = false;
      showErrorDialog("获取视频信息出错", true);
      return;
    }
    final File upLoadFile = videoInfo.getUpLoadFile(this);
    final File upLoadTempFile = videoInfo.getUpLoadTempFile(this);
    if (!TextUtils.isEmpty(qiniu_persistentId)) {
      doCommit();
    } else {
      if (upLoadFile != null && upLoadFile.exists() && upLoadFile.length() > 0) {
        doUpload();
        upLoadTempFile.delete();
      } else {
        File mp4File = videoInfo.getMp4File(this);
        if (mp4File != null && mp4File.exists() && mp4File.length() > 0) {
          showDeterminateProgress("请耐心等候", "初始化编解码器...");
          HFXWorksUtil.startVideoTransCode(videoInfo.videoWidth, videoInfo.videoHeight,
              mp4File.getAbsolutePath(), upLoadTempFile.getAbsolutePath(),
              new HFXWorksUtil.VideoTransCallback() {

                @Override
                public void onTranscodeProgress(int currentTime) {
                  if (videoInfo.duration <= currentTime) {
                    updateDeterminateProgress("转码中..." + "(已处理" + currentTime / 1000 + "秒)", 0);
                  } else {
                    int progress = (int) (100f * currentTime / videoInfo.duration);
                    updateDeterminateProgress("转码中..." + "（" + currentTime / 1000 + "秒/"
                        + videoInfo.duration / 1000 + "秒）", progress);
                  }
                }

                @Override
                public void onTranscodeFinished(boolean success) {
                  if (success && FileUtil.renameFile(upLoadTempFile, upLoadFile)) {
                    doUpload();
                  } else {
                    isCommiting = false;
//                    worksUploadResult(false, "trancodefail");
                    upLoadTempFile.delete();
                    hideDeterminateProgress();
                    showTipsDialog("很抱歉", "转码出现问题");
                  }
                }
              });
        } else {
          isCommiting = false;
          FileUtil.deleteDir(HfxFileUtil.getUserWorkDir(this, videoId));
          showErrorDialog(getString(R.string.book_dubmixaudiopatherr_tips), true);
        }
      }
    }

  }

  private void doCommit() {
    File upLoadFile = videoInfo.getUpLoadFile(this);
    long filSize = upLoadFile.length();
    MultipartBody.Builder builder = new MultipartBody.Builder();
    builder.addFormDataPart("content_type", "freevideo");
    builder.addFormDataPart("bookid", strings[1] + "_" + strings[2]);
    builder.addFormDataPart("file_size", String.valueOf(filSize));
    builder.addFormDataPart("persistent_id", qiniu_persistentId);
    builder.addFormDataPart("parameters", params);
    builder.setType(MultipartBody.FORM);
    MultipartBody multipartBody = builder.build();
    ApiHandler.getBaseApi(this)
        .uploadWorkInfo(multipartBody)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DefaultSubscriber<BaseNetResult>() {

          @Override
          public void onComplete() {

          }

          @Override
          public void onError(Throwable e) {
            isCommiting = false;
            hideDeterminateProgress();
            showTipsDialog("很抱歉", getString(R.string.common_submitfaile_title));
//            worksUploadResult(false, "apifail");
          }

          @Override
          public void onNext(BaseNetResult result) {
            hideDeterminateProgress();
            if (result != null && result.errcode == 0) {
              //                                toast(getString(R.string.commit_success));
              File videoDir = videoInfo.getVideoDir(getApplicationContext());
              FileUtil.deleteDir(videoDir);
//              worksUploadResult(true, "success");
            } else if (result != null && result.errcode == 1001) {
              isCommiting = false;
//              worksUploadResult(false, "needlogin");
              login();
              finish();
            } else {
              showTipsDialog("很抱歉", getString(R.string.common_submitfaile_title));
              isCommiting = false;
//              worksUploadResult(false, "apifail");
            }
          }
        });
  }

  private void doUpload() {
    File upLoadFile = videoInfo.getUpLoadFile(this);
    String videoId = videoInfo.videoId;
    int coverTime = HfxPreferenceUtil.getVideoCoverTime(this, videoId);
    if (upLoadFile != null && upLoadFile.exists() && upLoadFile.length() > 0) {
      showDeterminateProgress("提交作品", "正在上传...", "取消", new OnClickListener() {
        @Override
        public void onClick(View view) {
          HFXWorksUtil.cancelUpload(true);
        }
      });
      HFXWorksUtil.startUploadQiNiu(this, coverTime, videoId, upLoadFile,
          new HFXWorksUtil.UploadQiNiuCallback() {
            @Override
            public void onError(String error) {
              hideDeterminateProgress();
              isCommiting = false;
//              worksUploadResult(false, "uploadfail");
              toast(error);
            }

            @Override
            public void onLoginError() {
              login();
              finish();
            }

            @Override
            public void onUploadProgress(double progress) {
              updateDeterminateProgress("正在上传..."
                      + Utils.formatFloatString((float) (progress * 100)) + "%",
                  (int) (progress * 100));
            }

            @Override
            public void onSuccess(String persistentId) {
              qiniu_persistentId = persistentId;
              if (videoInfo != null) {
                doCommit();
              }
            }
          });
    } else {
      showTipsDialog("转码失败", "请重试");
    }
  }

  private void deleteAndQuit() {
    showProgress(getString(R.string.hfx_quiting));
    new DeleteQuitTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }


  private OnClickListener onClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      if (buttonShowHideAnim) {
        return;
      }
      boolean isPortrait = getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
      final View targetView = isPortrait ? bottomLayout : bottomLayoutLand;
      int topHeight = Utils.dp2px(EvalActivity.this, 65);
      int bottomHeight = isPortrait ? Utils.dp2px(EvalActivity.this, 250) : Utils
          .dp2px(EvalActivity.this, 100);
      buttonShowHideAnim = true;
      handler.sendEmptyMessageDelayed(MESSAGE_HIDE_SHOW_BUTTON, 300);
      if (!buttonHide) {
        buttonHide = true;
        ObjectAnimator topAnim = ObjectAnimator
            .ofFloat(topLayout, "translationY", 0, -topHeight);
        topAnim.setInterpolator(new AccelerateInterpolator());
        topAnim.setDuration(200);
        ObjectAnimator bottomAnim = ObjectAnimator
            .ofFloat(targetView, "translationY", 0, bottomHeight);
        bottomAnim.setInterpolator(new AccelerateInterpolator());
        bottomAnim.setDuration(200);
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.addListener(new AnimatorListener() {
          @Override
          public void onAnimationStart(Animator animation) {

          }

          @Override
          public void onAnimationEnd(Animator animation) {
            topLayout.setVisibility(View.GONE);
            //定制屏强制横屏逻辑
            if (isCustompad) {
              targetView.setVisibility(View.VISIBLE);
            } else {
              targetView.setVisibility(View.GONE);
            }

          }

          @Override
          public void onAnimationCancel(Animator animation) {

          }

          @Override
          public void onAnimationRepeat(Animator animation) {

          }
        });
        animationSet.play(topAnim).with(bottomAnim);
        animationSet.start();
      } else {
        buttonHide = false;
        topLayout.setVisibility(View.VISIBLE);
        targetView.setVisibility(View.VISIBLE);
        ObjectAnimator topAnim = ObjectAnimator
            .ofFloat(topLayout, "translationY", -topHeight, 0);
        topAnim.setInterpolator(new AccelerateInterpolator());
        topAnim.setDuration(200);
        ObjectAnimator bottomAnim = ObjectAnimator
            .ofFloat(targetView, "translationY", bottomHeight, 0);
        bottomAnim.setInterpolator(new AccelerateInterpolator());
        bottomAnim.setDuration(200);
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.addListener(new AnimatorListener() {
          @Override
          public void onAnimationStart(Animator animation) {

          }

          @Override
          public void onAnimationEnd(Animator animation) {

          }

          @Override
          public void onAnimationCancel(Animator animation) {

          }

          @Override
          public void onAnimationRepeat(Animator animation) {

          }
        });
        animationSet.play(topAnim).with(bottomAnim);
        animationSet.start();
      }
    }
  };

  private static class DeleteQuitTask extends WeakAsyncTask<Void, Void, Void, EvalActivity> {

    public DeleteQuitTask(EvalActivity evalActivity) {
      super(evalActivity);
    }

    @Override
    protected Void doInBackground(EvalActivity evalActivity, Void... params) {
      Context context = evalActivity.getApplicationContext();
      String bookId = evalActivity.workId;
      File temp = HfxFileUtil.getUserTempDir(context, bookId);
      Log.d("delete", "tempDir = " + temp.getAbsolutePath());
      FileUtil.deleteEvalDir(temp, true);
      File file = new File(FileUtil.getFileCacheDir(evalActivity),
          Utils.getLoginUserId(evalActivity) + "_eval_" + evalActivity.book.bookid);
      if (file.exists()) {
        file.delete();
      }
      return null;
    }

    @Override
    protected void onPostExecute(EvalActivity evalActivity, Void aVoid) {
      if (evalActivity != null && !evalActivity.isFinishing()) {
        evalActivity.hideProgress();
        evalActivity.finish();
      }
    }
  }

  class EvalAdapter extends PagerAdapter {

    private EvalHolder[] evalHolders;
    private int currentPosition = -1;

    EvalAdapter() {
      evalHolders = new EvalHolder[book.bookpage.size()];
    }

    @Override
    public int getCount() {
      return book == null || book.bookpage == null ? 0 : book.bookpage.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
      return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
      View inflateView = LayoutInflater.from(container.getContext())
          .inflate(R.layout.layout_hfx_item_eval, container, false);
      container.addView(inflateView);
      evalHolders[position] = new EvalHolder(inflateView);
      final PageImageView pageImageView = evalHolders[position].pageImageView;
      // 设置背景色透明
      pageImageView.setCanvasBackgroundColor(0x00000000);
      if (position == 0) {
        pageImageView.setCallback(callback);
      }
      pageImageView.setOnClickListener(onClickListener);
      String pageName = book.bookpage.get(position).page_name;
      File image = HfxFileUtil.getBookImageFile(EvalActivity.this, workId, pageName);
      RequestOptions options = new RequestOptions()
          .skipMemoryCache(true)
          .diskCacheStrategy(DiskCacheStrategy.NONE);
      Glide.with(EvalActivity.this)
          .asBitmap()
          .load(image)
          .apply(options)
          .into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
              Log.i("wechat", "resource的大小" + (resource.getByteCount() / 1024 / 1024)
                  + "M宽度为" + resource.getWidth() + "高度为" + resource.getHeight());
              int a = 1;
              if (resource.getWidth() > 720) {
                a = resource.getWidth() / 720;
              }
              int height = resource.getHeight() / a;
              Bitmap bm = Bitmap.createScaledBitmap(resource, 720, height, true);
              Log.i("wechat", "bm的大小" + (bm.getByteCount() / 1024 / 1024)
                  + "M宽度为" + bm.getWidth() + "高度为" + bm.getHeight());
              pageImageView.setImageBitmap(bm, position);
            }
          });
      return inflateView;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
      super.setPrimaryItem(container, position, object);
      if (currentPosition == position) {
        return;
      }
      Logger.e("setPrimaryItem: " + position);
      currentPosition = position;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      Logger.i("destroyItem: " + position);
      container.removeView((View) object);
      if (evalHolders[position] != null) {
        evalHolders[position] = null;
      }
    }

    EvalHolder getEvalHolder(int currentPosition) {
      return evalHolders[currentPosition];
    }
  }

  private static class EvalHolder {

    PageImageView pageImageView;

    EvalHolder(View itemView) {
      this.pageImageView = itemView.findViewById(R.id.pageImageView);
    }
  }

  class ScoreAdapter extends RecyclerView.Adapter<ScoreHolder> {

    ScoreHolder[] scoreHolders;
    int currentPosition;

    ScoreAdapter() {
      currentPosition = currentPage;
      scoreHolders = new ScoreHolder[book == null || book.bookpage == null ? 0 : book.bookpage.size()];
    }

    @NonNull
    @Override
    public ScoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View itemView = getLayoutInflater().inflate(R.layout.layout_hfx_score_item, parent, false);
      return new ScoreHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreHolder holder, final int position) {
      scoreHolders[position] = holder;
      holder.itemScoreRoot.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (viewPager.isLocked()) {
            return;
          }
          viewPager.setCurrentItem(position);
        }
      });
      EvalResult evalResult = null;
      for (int i = 0; i < evalMap.size(); i++) {
        int key = evalMap.keyAt(i);
        if (key == position) {
          evalResult = evalMap.get(key);
          break;
        }
      }
      if (evalResult == null) {
        File audioFile = getAudioOfCurrentPage(position);
        holder.ivItemRecord.setVisibility(audioFile.exists() ? View.GONE : View.VISIBLE);
        holder.tvEvalScore.setText("");
      } else {
        holder.ivItemRecord.setVisibility(View.GONE);
//        holder.tvEvalScore.setText(String.valueOf((int) evalResult.score));
        holder.tvEvalScore.setText(TextUtils.isEmpty(evalResult.scoreDisplay) ? String.valueOf((int) evalResult.score)
            : evalResult.scoreDisplay);
      }
      holder.ivEvalItemSelectBg.setVisibility(currentPosition == position ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreHolder holder, int position, @NonNull List<Object> payloads) {
      if (!payloads.isEmpty()) {
        int score = (int) payloads.get(0);
        holder.ivItemRecord.setVisibility(View.GONE);
        if (score == -1) {
          holder.tvEvalScore.setVisibility(getAudioOfCurrentPage().exists() ? View.GONE : View.VISIBLE);
        } else {
          holder.tvEvalScore.setVisibility(View.VISIBLE);
          holder.tvEvalScore.setText(String.valueOf(score));
        }
      } else {
        super.onBindViewHolder(holder, position, payloads);
      }
    }

    @Override
    public int getItemCount() {
      return book == null || book.bookpage == null ? 0 : book.bookpage.size();
    }

    public void lightItemByIndex(int position) {
      if (currentPosition != position) {
        ScoreHolder scoreHolder = scoreHolders[currentPosition];
        if (scoreHolder != null) {
          scoreHolder.ivEvalItemSelectBg.setVisibility(View.GONE);
        }
        ScoreHolder currentHolder = scoreHolders[position];
        if (currentHolder != null) {
          currentPosition = position;
          currentHolder.ivEvalItemSelectBg.setVisibility(View.VISIBLE);
        }
      }
    }
  }

  private static class ScoreHolder extends RecyclerView.ViewHolder {

    ImageView ivEvalItemSelectBg;
    ImageView ivItemRecord;
    TextView tvEvalScore;
    View itemScoreRoot;

    ScoreHolder(View itemView) {
      super(itemView);
      itemScoreRoot = itemView.findViewById(R.id.itemScoreRoot);
      ivEvalItemSelectBg = itemView.findViewById(R.id.ivEvalItemSelectBg);
      ivItemRecord = itemView.findViewById(R.id.ivItemRecord);
      tvEvalScore = itemView.findViewById(R.id.tvEvalScore);
    }
  }

}
