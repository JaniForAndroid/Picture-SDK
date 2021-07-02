package com.namibox.hfx.ui;

import android.Manifest.permission;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.picsdk.R;
import com.namibox.commonlib.activity.AbsFunctionActivity;
import com.namibox.commonlib.dialog.DialogUtil;
import com.namibox.commonlib.view.MarqueeTextView;
import com.namibox.hfx.bean.AudioInfo;
import com.namibox.hfx.bean.MatchInfo;
import com.namibox.hfx.utils.HfxFileUtil;
import com.namibox.hfx.utils.HfxUtil;
import com.namibox.hfx.utils.MyPlayer;
import com.namibox.hfx.view.NewWaveView;
import com.namibox.hfx.view.NewWaveView.Mode;
import com.namibox.hfx.view.NewWaveView.WaveViewListener;
import com.namibox.hfx.view.RecordWaveView;
import com.namibox.tools.PermissionUtil;
import com.namibox.tools.PermissionUtil.GrantedCallback;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import com.namibox.util.WeakAsyncTask;
import com.uraroji.garage.android.lame.AudioUtil.VolumeCallBack;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * @author sunha
 */
public class StoryRecordActivity extends AbsFunctionActivity implements Callback,
    VolumeCallBack, OnCompletionListener {

  View statusBarLayout;
  ImageView back;
  MarqueeTextView title;
  RecyclerView voiceBgRecyclerView;
  ImageView enterCutMode;
  RecordWaveView recordWaveView;
  SeekBar seekbar;
  TextView volume;
  NewWaveView waveView;
  ImageView playController;
  TextView playControllerTv;
  TextView recordTimeTv;
  ImageView recImg;
  TextView recText;
  ImageView resetImage;
  ImageView recBtn;
  TextView saveTv;
  ImageView saveImg;
  TextView menuText1;
  FrameLayout menu1;
  ImageView menuImg1;
  LinearLayout recordLayout;
  LinearLayout confirmCutLayout;

  private boolean isRecorded = false;
  private int sampleRate = 44100;
  private MediaPlayer mediaPlayer;
  public static final String BOOK_ID = "booId";
  private static final String TAG = "StoryRecordActivity";

  /**
   * 录制时的文件
   */
  private File recFile;
  private File uploadFile;

  private int markLine;
  private int mStartLine;
  private String bookId;

  private State state = State.INIT;

  private int recordTimeMs = 0;
  /**
   * 播放时变化的时间
   */
  private int playingTimeMs = 0;
  /**
   * 播放结束时间
   */
  private int markTimeMs = 0;
  private static final int UPDATE_RECORD_TIME = 1;
  private static final int UPDATE_PLAY_TIME = 2;
  private Handler timerHander;
  private MyPlayer myPlayer = new MyPlayer();
  private Context context;
  private TelephonyManager telManager;
  private int msPerLine;
  private long startPlayCurrentTime;
  private long tempRecordCurrentTime;
  private CutTask cutTask;
  private int recorderFlag = 1;
  private static final long RECORD_TIME_LIMIT = 30 * 60 * 1000;
  private static final long RECORD_WARNING_TIME = 25 * 60 * 1000;
  private AudioManager mAudioManager;
  private int maxVolume;
  private long tempTime;
  private int indexOfBgAudio = 0;
  private List<VoiceBgBean> voiceBgList = new ArrayList<>();
  private Adapter adapter;
  private CutMode cutMode = CutMode.IDLE;

  private enum CutMode {IDLE, CUT, UNCUT}

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    getWindow().setStatusBarColor(0x00000000);

    getWindow().setFlags(LayoutParams.FLAG_KEEP_SCREEN_ON,
        LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.hfx_activity_audiorecord);
    if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
      AudioManager myAudioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
      String nativeParam = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
      sampleRate = Integer.parseInt(nativeParam);
    }
    initView();
    if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
      LinearLayout.LayoutParams statusBarLp = (LinearLayout.LayoutParams) statusBarLayout
          .getLayoutParams();
      statusBarLp.height = Utils.getStatusBarHeight(this);
    }
    title.setTextColor(0xffffffff);
    title.setText(R.string.hfx_title);
    back.setImageResource(R.drawable.ic_arrow_back_white);
    back.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackPressed();
      }
    });
    setMenu(getString(R.string.hfx_usr_guide), new OnClickListener() {
      @Override
      public void onClick(View v) {
        showIntro();
      }
    });
    //音量控制,初始化定义
    mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    //最大音量
    maxVolume = (int) (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.5f);
    //当前音量
//        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

    boolean showGuidance = PreferenceUtil.getSharePref(this, "show_guidance", true);
    if (showGuidance) {
      showIntro();
      PreferenceUtil.setSharePref(this, "show_guidance", false);

    }

    Intent intent = getIntent();
    context = this;
    final String currentBookId = intent.getStringExtra(BOOK_ID);
    PermissionUtil.requestPermissionWithFinish(this, new GrantedCallback() {
      @Override
      public void action() {
        getDirAndFile(currentBookId);
        showRecordTips(true);
        regTelManager();
      }
    }, permission.RECORD_AUDIO);
    timerHander = new Handler(this.getMainLooper(), this);
    initBg();
    updateView(State.INIT);
    waveView.setListener(new WaveViewListener() {


      @Override
      public void updateIndexLine(int startLine, int indexLine) {
        refreshMsAndLine(startLine, indexLine);
        recordTimeTv.setText(msToString(markTimeMs));
      }


      @Override
      public void markerTouchOutZone(int playLine) {
        startPlay(playLine);
      }


      @Override
      public void onRefresh(int startLine, int indexLine) {
        refreshMsAndLine(startLine, indexLine);
      }

    });

    mediaPlayer = new MediaPlayer();
    mediaPlayer.setOnCompletionListener(this);
  }

  private void initView() {
    statusBarLayout = findViewById(R.id.status_bar_layout);
    back = findViewById(R.id.back);
    title = findViewById(R.id.title);
    voiceBgRecyclerView = findViewById(R.id.voiceBgRecyclerView);
    enterCutMode = findViewById(R.id.enterCutMode);
    recordWaveView = findViewById(R.id.record_wave_view);
    seekbar = findViewById(R.id.seekbar);
    volume = findViewById(R.id.volume);
    waveView = findViewById(R.id.wave_view);
    playController = findViewById(R.id.playController);
    playControllerTv = findViewById(R.id.playControllerTv);
    recordTimeTv = findViewById(R.id.recordTimeTv);
    recImg = findViewById(R.id.recImg);
    recText = findViewById(R.id.recText);
    resetImage = findViewById(R.id.resetImage);
    recBtn = findViewById(R.id.recBtn);
    saveTv = findViewById(R.id.saveTv);
    saveImg = findViewById(R.id.saveImg);
    menuText1 = findViewById(R.id.menu_text1);
    menu1 = findViewById(R.id.menu1);
    menuImg1 = findViewById(R.id.menu_img1);
    recordLayout = findViewById(R.id.recordLayout);
    confirmCutLayout = findViewById(R.id.confirmCutLayout);

    resetImage.setOnClickListener(this::onViewClick);
    saveImg.setOnClickListener(this::onViewClick);
    playController.setOnClickListener(this::onViewClick);
    recBtn.setOnClickListener(this::onViewClick);
    View confirmCutBtn = findViewById(R.id.confirmCutBtn);
    View cancelCatBtn = findViewById(R.id.cancelCatBtn);
    confirmCutBtn.setOnClickListener(this::onViewClick);
    cancelCatBtn.setOnClickListener(this::onViewClick);
    enterCutMode.setOnClickListener(v -> enterCutMode());
  }

  public static void openStoryRecord(Context context) {
    openStoryRecord(context, null);
  }

  public static void openStoryRecord(Context context, String id) {
    Intent intent = new Intent(context, StoryRecordActivity.class);
    if (!TextUtils.isEmpty(id)) {
      intent.putExtra(StoryRecordActivity.BOOK_ID, id);
    }
    context.startActivity(intent);
  }

  @Override
  public boolean handleMessage(Message msg) {
    switch (msg.what) {
      case UPDATE_RECORD_TIME:
        long currentTime = System.currentTimeMillis();
        int timeTemp = (int) (currentTime - tempRecordCurrentTime);
        tempRecordCurrentTime = currentTime;
        recordTimeMs += timeTemp;

        recordTimeTv.setText(msToString(recordTimeMs));
        if (null != telManager && TelephonyManager.CALL_STATE_RINGING == telManager
            .getCallState()) {
          stopRecorder();
        }
        if (recordTimeMs > RECORD_WARNING_TIME) {
          recordTimeTv.setTextColor(ContextCompat.getColor(this, R.color.hfx_red));
        } else {
          recordTimeTv.setTextColor(ContextCompat.getColor(this, R.color.hfx_white));
        }
        if (recordTimeMs > RECORD_TIME_LIMIT) {
          stopRecorder();
          toast(getString(R.string.hfx_limit_30_minute));
        }

        if (state == State.RECORD) {
          queueNextRefresh(UPDATE_RECORD_TIME);
        }
        break;

      case UPDATE_PLAY_TIME:
        long currentPlayTime = System.currentTimeMillis();
        int time = (int) (currentPlayTime - startPlayCurrentTime);
        startPlayCurrentTime = currentPlayTime;
        playingTimeMs += time;

//                int playLine = (int) ((waveViewWidth - 15) * playingTimeMs / recordTimeMs);
        int playLine = playingTimeMs / msPerLine;
        waveView.setPlayLine(playLine);

        recText.setText(msToString(playingTimeMs));
        if (playingTimeMs > markTimeMs) {
          updateView(State.STOP);
          state = State.STOP;
          stopPlay();
        }
        if (state == State.PLAY) {
          queueNextRefresh(UPDATE_PLAY_TIME);
        }
        break;
      default:
        return false;
    }
    return true;
  }

  @Override
  public void onCurrentVoice(double currentVolume) {
    waveView.addVolume(currentVolume);
    recordWaveView.addVolume(currentVolume);
  }

  public void onViewClick(View view) {
    int i = view.getId();
    if (i == R.id.resetImage) {
      showDialog(getString(R.string.hfx_tip),
          getString(R.string.hfx_re_record),
          getString(R.string.hfx_confirm),
          new OnClickListener() {
            @Override
            public void onClick(View v) {
              if (state == State.STOP) {

                resetRecorder();
                startRecorder();
              } else if (state == State.PLAY) {
                stopPlay();
                resetRecorder();
                startRecorder();

              } else if (state == State.PAUSE) {
                stopPlay();
                resetRecorder();
                startRecorder();
              }
            }
          },
          getString(R.string.hfx_cancel),
          null);

    } else if (i == R.id.saveImg) {
      if (recordTimeMs < 60 * 1000) {
        toast(getString(R.string.hfx_limit_1_minute));
        return;
      }
      stopPlay();
      endRecord();
      openSaveAudio(bookId);


    } else if (i == R.id.playController) {
      switch (state) {
        case PAUSE:
          startPlay();
          break;
        case STOP:
          startPlay();
          break;
        case PLAY:
          updateView(State.STOP);
          state = State.STOP;
          stopPlay();
          break;
        default:
          break;
      }

    } else if (i == R.id.recBtn) {
      if (recordTimeMs > RECORD_TIME_LIMIT) {
        toast(getString(R.string.hfx_limit_30_minute));
        return;
      }
      long currentTime = System.currentTimeMillis();
      long delayTime = currentTime - tempTime;
      if (delayTime < 800) {
        toast(getString(R.string.hfx_frequently));
        return;
      }
      tempTime = currentTime;
      if (mediaPlayer.isPlaying()) {
        showDialog(getString(R.string.hfx_tip),
            getString(R.string.hfx_stop),
            getString(R.string.hfx_confirm),
            new OnClickListener() {
              @Override
              public void onClick(View v) {
                changeRecorderState();
              }
            },
            getString(R.string.hfx_cancel),
            null);
      } else {
        changeRecorderState();
      }


    } else if (i == R.id.confirmCutBtn) {

      int cutTime = markLine * msPerLine;
      if (cutTime > 10000) {
        showProgress("正在裁剪");
        cutTask = new CutTask(this);
        cutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        exitCutMode();
        updateView(State.STOP);
        state = State.STOP;
        stopPlay();
        myPlayer.pause();
      } else {
        toast(getString(R.string.hfx_cut_limit_10));
      }


    } else if (i == R.id.cancelCatBtn) {
      waveView.idle(Mode.PLAY);
      recordTimeTv.setText(msToString(recordTimeMs));
      confirmCutLayout.setVisibility(View.INVISIBLE);
      recordLayout.setVisibility(View.VISIBLE);
      exitCutMode();
      updateView(State.STOP);

    }
  }


  private void changeRecorderState() {
    switch (state) {
      case INIT:
        startRecorder();
        break;
      case RECORD:
        stopRecorder();
        break;
      case STOP:
        startRecorder();
        break;
      case PLAY:
        startRecorder();
        break;
      case PAUSE:
        startRecorder();
      default:
        break;
    }
  }


  private void openSaveAudio(String bookId) {
    MatchInfo matchInfo = HfxUtil.getMatchInfo(this, bookId);
    if (matchInfo != null && !TextUtils.isEmpty(matchInfo.realUrl)) {
      PreferenceUtil.setSharePref(this, "story_record_time_" + bookId, recordTimeMs / 1000);
      openView(matchInfo.realUrl);
    } else {
      Intent intent = new Intent(context, SaveAudioActivity.class);
      intent.putExtra(SaveAudioActivity.AUDIO_ID, bookId);
      intent.putExtra(SaveAudioActivity.AUDIO_DURATION, recordTimeMs / 1000);
      startActivity(intent);
    }
    finish();

  }

  private void resetRecorder() {
    if (this.isFinishing()) {
      return;
    }
    recordTimeMs = 0;
    recFile.delete();
    waveView.clear();
    recordWaveView.clear();
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    stopPlay();
  }

  public void enterCutMode() {
    if (state == State.INIT || state == State.RECORD) {
      return;
    }
    if (cutMode == CutMode.CUT) {
      return;
    }
    if (recordTimeMs < 10 * 1000) {
      toast(getString(R.string.hfx_limit_10_second));
      return;
    }
    cutMode = CutMode.CUT;
    waveView.idle(Mode.CUT);
    recordTimeTv.setText(msToString(markTimeMs));
    recordLayout.setVisibility(View.INVISIBLE);
    confirmCutLayout.setVisibility(View.VISIBLE);
    disableEnterCutMode();
    disableSave();
  }

  public void exitCutMode() {
    cutMode = CutMode.UNCUT;

  }

  private enum State {
    PAUSE,
    INIT,
    RECORD,
    STOP,
    PLAY
  }

  private void showIntro() {
    Intent intent = new Intent(this, IntroActivity.class);
    startActivity(intent);
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

  private void showRecordTips(boolean showTips) {
    if (showTips) {
      DialogUtil.showButtonDialog2(this,
          getString(R.string.hfx_tips_freeaudio_title),
          getString(R.string.hfx_tips_freeaudio_message),
          getString(R.string.hfx_confirm),
          null, null, null,
          null, null, new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
          });
    }
  }

  private void refreshMsAndLine(int startLine, int indexLine) {
    mStartLine = startLine;
    markLine = indexLine;
    markTimeMs = indexLine * msPerLine;
  }

  private void getDirAndFile(String currentBookId) {
    if (TextUtils.isEmpty(currentBookId)) {
      String userId = Utils.getLoginUserId(context);
      long timeStamp = System.currentTimeMillis();
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
      String d = format.format(timeStamp);
      bookId = userId + "_freeaudio_" + d;
    } else {
      bookId = currentBookId;
    }
    File audioDir = HfxFileUtil.getUserWorkDir(context, bookId);
    recFile = new File(audioDir, bookId + ".rec");
    uploadFile = new File(audioDir, bookId + HfxFileUtil.AUDIO_TYPE);
    initAudioUtil(this);
    sampleRate = getSampleRate();
    msPerLine = getMsPerBuffer();
    //测试音频可用性
    try {
      recorderFlag = testAudio();
    } catch (RuntimeException e) {

    }
  }

  public void setMenu(String title, OnClickListener listener) {
    menu1.setVisibility(View.VISIBLE);
    menuImg1.setVisibility(View.GONE);
    menuText1.setText(title);
    menuText1.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
    menu1.setOnClickListener(listener);
    int[] attrs = new int[]{R.attr.selectableItemBackground};
    TypedArray typedArray = obtainStyledAttributes(attrs);
    int backgroundResource = typedArray.getResourceId(0, 0);
    typedArray.recycle();
    menuText1.setBackgroundResource(backgroundResource);

  }

  void initBg() {
//        radioGroup.clearCheck();
//        radioGroup.check(R2.id.radioButton0);
    voiceBgList.add(
        new VoiceBgBean(getString(R.string.hfx_music_None), R.drawable.hfx_icon_volume_silent));
    voiceBgList
        .add(new VoiceBgBean(getString(R.string.hfx_music_lyric), R.drawable.hfx_icon_volume_1));
    voiceBgList
        .add(new VoiceBgBean(getString(R.string.hfx_music_rhythm), R.drawable.hfx_icon_volume_2));
    voiceBgList
        .add(new VoiceBgBean(getString(R.string.hfx_music_anthiqu), R.drawable.hfx_icon_volume_3));
    voiceBgList
        .add(new VoiceBgBean(getString(R.string.hfx_music_soft), R.drawable.hfx_icon_volume_4));
    voiceBgList
        .add(new VoiceBgBean(getString(R.string.hfx_music_elegant), R.drawable.hfx_icon_volume_5));
    voiceBgList
        .add(new VoiceBgBean(getString(R.string.hfx_music_relieve), R.drawable.hfx_icon_volume_6));
    voiceBgList
        .add(new VoiceBgBean(getString(R.string.hfx_music_sad), R.drawable.hfx_icon_volume_7));
    voiceBgList
        .add(new VoiceBgBean(getString(R.string.hfx_music_cheerful), R.drawable.hfx_icon_volume_8));
    voiceBgRecyclerView
        .setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    adapter = new Adapter();
    voiceBgRecyclerView.setAdapter(adapter);
    //tempVolume:音量绝对值
    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
    myPlayer.setVolume(0.4f);
    seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        volume.setText(getString(R.string.hfx_volume, progress));
        float fVolume = progress / 100f * 0.8f;
        Log.i(TAG, "onProgressChanged: " + fVolume);
        myPlayer.setVolume(fVolume);

      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
    seekbar.setProgress(50);
  }

  private void queueNextRefresh(int msg) {
    timerHander.removeMessages(msg);
    timerHander.sendEmptyMessageDelayed(msg, msPerLine);
  }


  void startRecorder() {
    if (this.isFinishing()) {
      return;
    }
    if (recorderFlag <= 0) {
      showErrorDialog(getString(R.string.hfx_init_failed), true);
    } else {
      try {

        updateView(State.RECORD);
        this.state = State.RECORD;
        vibrator();
        startMp3(recFile, true);
        tempRecordCurrentTime = System.currentTimeMillis();
        queueNextRefresh(UPDATE_RECORD_TIME);
        stopPlay();
        if (!myPlayer.isPlaying()) {
          myPlayer.start();
        }

        isRecorded = true;


      } catch (RuntimeException e) {
        showErrorDialog(getString(R.string.hfx_init_failed), true);
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }


  }


  void stopRecorder() {
    markTimeMs = recordTimeMs;
    if (myPlayer.isPlaying()) {
      myPlayer.pause();
    }
    updateView(State.STOP);
    this.state = State.STOP;
    stopAudioRecord();
    waveView.idle(Mode.PLAY);


  }


  public String msToString(long ms) {
    StringBuilder sFormatBuilder = new StringBuilder();
    Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    sFormatBuilder.setLength(0);
    int secs = (int) (ms / 1000);
    int arg1 = secs / 60;
    int arg2 = secs % 60;
    return sFormatter.format("%1$02d:%2$02d", arg1, arg2).toString();
  }

  void stopPlay() {
    wavPlayerStop();
    timerHander.removeMessages(UPDATE_PLAY_TIME);
    playingTimeMs = mStartLine * msPerLine;
    waveView.stopPlay();

  }

  void startPlay(int playLine) {
    playingTimeMs = playLine * msPerLine;
    myPlayer.pause();
    wavPlayerStart(recFile, playingTimeMs);
    updateView(State.PLAY);
    this.state = State.PLAY;
    startPlayCurrentTime = System.currentTimeMillis();
    queueNextRefresh(UPDATE_PLAY_TIME);
  }

  void startPlay() {
    startPlay(mStartLine);

  }


  void pausePlay() {
    myPlayer.pause();
    updateView(State.PAUSE);
    this.state = State.PAUSE;
    wavPlayerStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    timerHander.removeMessages(UPDATE_PLAY_TIME);
    releaseRecorder();
    wavPlayerStop();
    wavPlayerRelease();
    if (recFile != null) {
      recFile.delete();
    }
    myPlayer.stop();
    myPlayer.release();


  }

  public void updateView(State state) {

    switch (state) {
      case INIT:
        recordWaveView.setVisibility(View.VISIBLE);
        waveView.setVisibility(View.INVISIBLE);
        recBtn.setImageResource(R.drawable.hfx_ic_rec);
        playController.setImageResource(R.drawable.hfx_ic_play_124_gray);
        playController.setClickable(false);
        playControllerTv.setText(R.string.hfx_play);
        playControllerTv.setTextColor(ContextCompat.getColor(this, R.color.hfx_text_color_disable));
        recImg.setVisibility(View.VISIBLE);
        recImg.setImageResource(R.drawable.hfx_img_gary_40);
        recText.setTextColor(Color.parseColor("#CCCCCC"));
        resetImage.setImageResource(R.drawable.hfx_reset_disable);
        resetImage.setClickable(false);
        disableSave();
        disableEnterCutMode();
        break;
      case RECORD:
        exitCutMode();
        recBtn.setImageResource(R.drawable.hfx_ic_stop);
        recordWaveView.setVisibility(View.VISIBLE);
        waveView.setVisibility(View.INVISIBLE);
        playController.setImageResource(R.drawable.hfx_ic_play_124_gray);
        playController.setClickable(false);
        playControllerTv.setText(R.string.hfx_play);
        playControllerTv.setTextColor(ContextCompat.getColor(this, R.color.hfx_text_color_disable));
        recImg.setVisibility(View.VISIBLE);
        recImg.setImageResource(R.drawable.hfx_img_red);
        recText.setText("REC");
        recText.setTextColor(Color.parseColor("#FF0000"));
        resetImage.setImageResource(R.drawable.hfx_reset_disable);
        resetImage.setClickable(false);
        disableSave();
        confirmCutLayout.setVisibility(View.INVISIBLE);
        recordLayout.setVisibility(View.VISIBLE);
        disableEnterCutMode();
        break;
      case STOP:
        recordWaveView.setVisibility(View.INVISIBLE);
        waveView.setVisibility(View.VISIBLE);
//                tip.setVisibility(View.VISIBLE);
        recBtn.setImageResource(R.drawable.hfx_ic_rec);
        playController.setImageResource(R.drawable.hfx_ic_play_124);
        playController.setClickable(true);
        playControllerTv.setText(R.string.hfx_play);
        playControllerTv.setTextColor(ContextCompat.getColor(this, R.color.theme_color));
        recImg.setVisibility(View.GONE);
        recText.setTextColor(ContextCompat.getColor(this, R.color.hfx_white));
        recText.setText(msToString(0));
        resetImage.setImageResource(R.drawable.hfx_reset_enable);
        resetImage.setClickable(true);
        if (cutMode == CutMode.CUT) {
          disableSave();
        } else {
          enableSave();
        }
        if (cutMode == CutMode.IDLE || cutMode == CutMode.UNCUT) {
          enableEnterCutMode();
        } else {
          disableEnterCutMode();
        }

        break;
      case PLAY:
        recordWaveView.setVisibility(View.INVISIBLE);
        waveView.setVisibility(View.VISIBLE);
//                tip.setVisibility(View.VISIBLE);
        recBtn.setImageResource(R.drawable.hfx_ic_rec);
        playController.setImageResource(R.drawable.hfx_ic_pause_124);
        playController.setClickable(true);
        playControllerTv.setText(R.string.hfx_pause);
        playControllerTv.setTextColor(ContextCompat.getColor(this, R.color.theme_color));
        recImg.setVisibility(View.GONE);
        recText.setTextColor(ContextCompat.getColor(this, R.color.hfx_white));
        resetImage.setImageResource(R.drawable.hfx_reset_enable);
        resetImage.setClickable(true);
        if (cutMode == CutMode.CUT) {
          disableSave();
        } else {
          enableSave();
        }

        break;

      case PAUSE:
        recordWaveView.setVisibility(View.INVISIBLE);
        waveView.setVisibility(View.VISIBLE);
//                tip.setVisibility(View.VISIBLE);
        recBtn.setImageResource(R.drawable.hfx_ic_rec);
        playController.setImageResource(R.drawable.hfx_ic_play_124);
        playController.setClickable(true);
        playControllerTv.setText(R.string.hfx_play);
        playControllerTv.setTextColor(ContextCompat.getColor(this, R.color.theme_color));
        recImg.setVisibility(View.GONE);
        recText.setTextColor(Color.parseColor("#35A4E6"));
        resetImage.setImageResource(R.drawable.hfx_reset_enable);
        resetImage.setClickable(true);
        if (cutMode == CutMode.CUT) {
          disableSave();
        } else {
          enableSave();
        }
        break;
      default:
        break;
    }
    this.state = state;
  }

  private void disableEnterCutMode() {
    enterCutMode.setImageResource(R.drawable.hfx_cut_disable);
    enterCutMode.setClickable(false);
  }

  private void enableEnterCutMode() {
    enterCutMode.setImageResource(R.drawable.hfx_cut_enable);
    enterCutMode.setClickable(true);
  }

  private void disableSave() {
    saveTv.setTextColor(ContextCompat.getColor(this, R.color.hfx_text_color_disable));
    saveImg.setImageResource(R.drawable.hfx_ic_save_disable);
    saveImg.setClickable(false);
  }

  private void enableSave() {
    saveTv.setTextColor(ContextCompat.getColor(this, R.color.theme_color));
    saveImg.setImageResource(R.drawable.hfx_ic_save_enable);
    saveImg.setClickable(true);
  }


  @Override
  public void onBackPressed() {
    if (isRecorded) {
      if (state != State.RECORD) {
        if (recordTimeMs < 60 * 1000) {
          showDialog(getString(R.string.hfx_tip),
              getString(R.string.hfx_exit_tip),
              getString(R.string.hfx_limit_quit)
              , new OnClickListener() {
                @Override
                public void onClick(View v) {
                  stopPlay();
                  finish();
                }
              },
              getString(R.string.hfx_cancel),
              null);
        } else {
          stopPlay();
          endRecord();
          openMyWorkAndFinish(MyWorkActivity.TAB_MAKING);
        }

      } else {
        toast(getString(R.string.hfx_recording));
      }

    } else {
      finish();
    }

  }

  protected void openMyWorkAndFinish(final int tab) {
    boolean needTip = PreferenceUtil.getSharePref(this, "needWorkTips", true);
    if (needTip) {
      new Builder(this)
          .setView(R.layout.hfx_layout_worker_tip)
          .setCancelable(false)
          .setPositiveButton(
              R.string.hfx_confirm,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  MyWorkActivity.openMyWork(StoryRecordActivity.this, tab);
                  finish();
                }
              })
          .setNegativeButton(
              R.string.hfx_never_tip,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  PreferenceUtil.setSharePref(StoryRecordActivity.this, "needWorkTips", false);
                  MyWorkActivity.openMyWork(StoryRecordActivity.this, tab);
                  finish();
                }
              })
          .create().show();
    } else {
      MyWorkActivity.openMyWork(this, tab);
      finish();
    }
  }


  private static class CutTask extends WeakAsyncTask<String, Long, Boolean, StoryRecordActivity> {

    public CutTask(StoryRecordActivity storyRecordActivity) {
      super(storyRecordActivity);
    }

    @Override
    protected Boolean doInBackground(StoryRecordActivity storyRecordActivity, String... params) {
      try {

        storyRecordActivity.cutRecordFile(storyRecordActivity.markTimeMs * 128 / 8);
        return true;
      } catch (IOException e) {
        e.printStackTrace();
      }
      return false;
    }

    @Override
    protected void onCancelled(StoryRecordActivity storyRecordActivity, Boolean aBoolean) {
      super.onCancelled(storyRecordActivity, aBoolean);
      if (storyRecordActivity != null && !storyRecordActivity.isFinishing()) {
        storyRecordActivity.hideProgress();
        storyRecordActivity.cutTask = null;


      }
    }

    @Override
    protected void onPostExecute(StoryRecordActivity storyRecordActivity, Boolean aBoolean) {
      super.onPostExecute(storyRecordActivity, aBoolean);
      if (storyRecordActivity != null && !storyRecordActivity.isFinishing()) {

        storyRecordActivity.recordTimeMs =
            storyRecordActivity.markLine * storyRecordActivity.msPerLine;
        storyRecordActivity.markTimeMs = storyRecordActivity.recordTimeMs;
        storyRecordActivity.waveView.cutWithLine(storyRecordActivity.markLine);

        storyRecordActivity.confirmCutLayout.setVisibility(View.INVISIBLE);
        storyRecordActivity.recordLayout.setVisibility(View.VISIBLE);
        storyRecordActivity.hideProgress();
        storyRecordActivity.cutTask = null;

      }
    }
  }

  public void wavPlayerStart(File file, int startTime) {
    try {
      mediaPlayer.reset();
      mediaPlayer.setDataSource(context, Uri.fromFile(file));
      mediaPlayer.prepare();
    } catch (IOException e) {
      e.printStackTrace();
    }
    mediaPlayer.setLooping(false);
    mediaPlayer.seekTo(startTime);
    mediaPlayer.start();

  }


  public void setVolume(float volume) {
    mediaPlayer.setVolume(volume, volume);
  }

  public void wavPlayerRelease() {
    mediaPlayer.release();
    mediaPlayer = null;
  }

  public void wavPlayerStop() throws IllegalStateException {
    mediaPlayer.stop();
  }

  class VoiceBgBean {

    public VoiceBgBean(String name, int resId) {
      this.name = name;
      this.resId = resId;
    }

    String name;
    int resId;

  }

  class Adapter extends RecyclerView.Adapter<ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(StoryRecordActivity.this)
          .inflate(R.layout.hfx_voice_bg_item, null);
      return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      if (position < voiceBgList.size()) {
        VoiceBgBean voiceBgBean = voiceBgList.get(position);
        if (indexOfBgAudio == position) {
          holder.voiceBgName
              .setTextColor(ContextCompat.getColor(StoryRecordActivity.this, R.color.theme_color));
          holder.checkLayout.setBackgroundResource(R.drawable.hfx_voice_bg_checked_drawable);
        } else {
          if (position == 0) {
            holder.voiceBgName
                .setTextColor(ContextCompat
                    .getColor(StoryRecordActivity.this, R.color.hfx_voice_name_tv_color_0));
          } else {
            holder.voiceBgName
                .setTextColor(ContextCompat.getColor(StoryRecordActivity.this, R.color.hfx_white));
          }

          holder.checkLayout.setBackgroundResource(R.drawable.hfx_voice_bg_drawable);
        }
        ImageView imageView;
        if (position == 0) {
          holder.voiceBgImg.setVisibility(View.GONE);
          imageView = holder.silentBgImg;
          holder.silentBgImg.setVisibility(View.VISIBLE);
          holder.voiceBgName.setBackgroundColor(
              ContextCompat.getColor(StoryRecordActivity.this, R.color.transparent));
        } else {
          imageView = holder.voiceBgImg;
          holder.voiceBgImg.setVisibility(View.VISIBLE);
          holder.silentBgImg.setVisibility(View.GONE);
          holder.voiceBgName.setBackgroundColor(
              ContextCompat.getColor(StoryRecordActivity.this, R.color.hfx_voice_name_tv_bg));
        }
        holder.voiceBgName.setText(voiceBgBean.name);
        Glide.with(StoryRecordActivity.this).load(voiceBgBean.resId).into(imageView);
      }

    }

    @Override
    public int getItemCount() {
      return voiceBgList.size();
    }
  }

  class ViewHolder extends RecyclerView.ViewHolder {

    RelativeLayout checkLayout;
    ImageView voiceBgImg;
    ImageView silentBgImg;
    TextView voiceBgName;

    public ViewHolder(View itemView) {
      super(itemView);
      checkLayout = itemView.findViewById(R.id.checkLayout);
      voiceBgImg = itemView.findViewById(R.id.voiceBgImg);
      voiceBgName = itemView.findViewById(R.id.voiceBgName);
      silentBgImg = itemView.findViewById(R.id.silentBgImg);
      itemView.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          if (indexOfBgAudio == getAdapterPosition()) {
            return;
          }
          if (state == State.PLAY) {
            pausePlay();
          }
          indexOfBgAudio = getAdapterPosition();
          if (indexOfBgAudio == 0) {
            myPlayer.stop();
          } else {
            try {
              AssetFileDescriptor fileDescriptor = getAssets().openFd("bg" +
                  +indexOfBgAudio + ".mp3");
              myPlayer.setDataSource(fileDescriptor);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
          adapter.notifyDataSetChanged();
        }
      });
    }
  }


  public void endRecord() {
    recFile.renameTo(uploadFile);
    AudioInfo audioInfo = new AudioInfo(bookId, recordTimeMs / 1000);
    HfxUtil.saveAudioInfo(this, audioInfo);
  }

}
