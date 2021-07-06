package com.namibox.hfx.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.exoaudioplayer.aduio.base.AbstractAudioPlayer;
import com.example.exoaudioplayer.aduio.base.AudioCallBack;
import com.example.exoaudioplayer.aduio.base.AudioPlayerFactory;
import com.example.exoaudioplayer.aduio.base.Constants;
import com.example.picsdk.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.model.NetResult;
import com.namibox.commonlib.view.CircleImageView;
import com.namibox.hfx.bean.Huiben;
import com.namibox.hfx.bean.MatchInfo;
import com.namibox.hfx.utils.HfxFileUtil;
import com.namibox.hfx.utils.HfxPreferenceUtil;
import com.namibox.hfx.utils.HfxUtil;
import com.namibox.hfx.view.PageImageView;
import com.namibox.hfx.view.RoundProgressBar;
import com.namibox.hfx.view.SectionProgressBar;
import com.namibox.tools.PermissionUtil;
import com.namibox.tools.PermissionUtil.GrantedCallback;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.Utils;
import com.namibox.util.WeakAsyncTask;
import com.namibox.util.network.NetWorkHelper;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Create time: 2015/8/19.
 */
public class RecordActivity extends BaseCommitActivity {

  public static final String TAG = "RecordActivity";
  public static final String ARG_RECORD_URL = "record_url";
  public static final String ARG_USER_URL = "user_url";
  public static final String ARG_BOOK_ID = "workId";
  public static final String ARG_CONTENT_TYPE = "content_type";
  public static final String ARG_INTRODUCE = "introduce";
  public static final String ARG_NEED_CHECK = "need_check";
  public static final String ARG_IS_MAKING = "is_making";
  public static final String ARG_NEED_SHOWOTHERS = "need_showothers";
  private String url;
  private String userUrl;
  private boolean isMaking;
  protected int currentPage;
  private State state = State.INIT;
  private boolean[] pageState;
  private boolean isChanged = false;
  private boolean needCheck;
  //默认true，修改作品是传false
  private boolean needShowothers;
  private String user_id;
  protected Huiben book;
  private boolean isOnCommitFragment = false;

  private AbstractAudioPlayer exoAudioPlayer;

  public enum State {
    INIT,
    IDLE,
    COUNTDOWN,
    RECORD,
    LISTEN
  }

  PageImageView imageView;
  View pageLayout;
  View pagePrev;
  View pageNext;
  TextView countdownText;
  View recordingLayout;
  ImageView recordingVolume;
  TextView recordingTime;
  View listeningLayout;
  RoundProgressBar listeningProgress;
  View controlBar;
  SectionProgressBar pageProgress;
  View controlLayout;
  RoundProgressBar recordProgress;
  TextView recordProgressText;
  TextView recordBtn;
  TextView listenBtn;
  TextView deleteBtn;
  TextView submitBtn;
  private boolean isCustompad;
  private PageImageView.Callback callback = (isPortrait, position) -> setOrientation(isPortrait);

  private SoundPool pool;
  private int soundId;
  private CountDownTimer countDownTimer = new CountDownTimer(2000, 100) {

    @Override
    public void onTick(long millisUntilFinished) {
      String time = String.valueOf(1 + millisUntilFinished / 1000);
      CharSequence currentText = countdownText.getText();
      if (!currentText.equals(time)) {
        countdownText.setText(time);
        pool.play(soundId, 1, 1, 100, 0, 1);
      }
    }

    @Override
    public void onFinish() {
      startRecording();
    }
  };

  private static final int MESSAGE_RECORD_UPDATE = 100;
  private static final long RECORD_TIME_LIMIT = 10 * 60 * 1000;
  private Handler.Callback handlerCallback = new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MESSAGE_RECORD_UPDATE:
          long time = System.currentTimeMillis() - startRecordTime;
          if (time >= RECORD_TIME_LIMIT) {
            stopRecording();
          } else {
            updateRecordingLayout(time);
            handler.sendEmptyMessageDelayed(MESSAGE_RECORD_UPDATE, 100);
          }
          return true;
      }
      return false;
    }
  };

  private Handler handler;
  private long startRecordTime;

  private InitResTask initResTask;
  static final int REQUEST_MODIFY = 10000;

  @Override
  protected void onResume() {
    super.onResume();
    if (isOnCommitFragment) {
      setOrientation(true);
    } else {
      if (imageView != null) {
        setOrientation(imageView.isPortrait());
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (pool != null) {
      pool.release();
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    getWindow().setFlags(LayoutParams.FLAG_KEEP_SCREEN_ON,
        LayoutParams.FLAG_KEEP_SCREEN_ON);
    Intent intent = getIntent();
    user_id = Utils.getLoginUserId(RecordActivity.this);
    url = intent.getStringExtra(ARG_RECORD_URL);
    userUrl = intent.getStringExtra(ARG_USER_URL);
    workId = intent.getStringExtra(ARG_BOOK_ID);
    introduce = intent.getStringExtra(ARG_INTRODUCE);
    if (TextUtils.isEmpty(introduce)) {
      String user_id = Utils.getLoginUserId(getApplicationContext());
      introduce = HfxPreferenceUtil.getBookIntroduce(this, user_id, workId);
    }
    isMaking = intent.getBooleanExtra(ARG_IS_MAKING, false);

    isCustompad = intent.getBooleanExtra("is_custompad", false);
    if (isCustompad) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    needCheck = intent.getBooleanExtra(ARG_NEED_CHECK, true);
    needShowothers = intent.getBooleanExtra(ARG_NEED_SHOWOTHERS, true);
    if (TextUtils.isEmpty(workId)) {
      showErrorDialog(getString(R.string.hfx_error_book_id), true);
      return;
    }

    if (!TextUtils.isEmpty(url)) {
      Logger.d(TAG, "save book url: " + url);
      HfxPreferenceUtil.saveRecordBookUrl(this, workId, url);
    } else {
      url = HfxPreferenceUtil.getRecordBookUrl(this, workId);
      Logger.d(TAG, "read saved book url: " + url);
    }
    setContentView(R.layout.hfx_activity_record);
    initView();
    recordProgress.setShowText(true);
    listeningProgress.setRoundWidth(getResources().getDimension(R.dimen.hfx_listen_progress_width));
    imageView.setCallback(callback);
    pool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    soundId = pool.load(this, R.raw.music, 100);
    handler = new Handler(handlerCallback);
    showProgress(getString(R.string.hfx_res_loading));
    initJsonData();

    initExoAudioPlayer();
  }

  private void initView() {
    imageView = findViewById(R.id.image);
    pageLayout = findViewById(R.id.page_layout);
    pagePrev = findViewById(R.id.page_prev);
    pageNext = findViewById(R.id.page_next);
    countdownText = findViewById(R.id.countdown_text);
    recordingLayout = findViewById(R.id.recording_layout);
    recordingVolume = findViewById(R.id.recording_volume);
    recordingTime = findViewById(R.id.recording_time);
    listeningLayout = findViewById(R.id.listening_layout);
    listeningProgress = findViewById(R.id.listening_progress);
    controlBar = findViewById(R.id.control_bar);
    pageProgress = findViewById(R.id.section_progress);
    controlLayout = findViewById(R.id.control_layout);
    recordProgress = findViewById(R.id.round_progress);
    recordProgressText = findViewById(R.id.progress_text);
    recordBtn = findViewById(R.id.record_btn);
    listenBtn = findViewById(R.id.listen_btn);
    deleteBtn = findViewById(R.id.delete_btn);
    submitBtn = findViewById(R.id.submit_btn);

    imageView.setOnClickListener(v -> toggleIdle());
    pageNext.setOnClickListener(v -> pageNext());
    pagePrev.setOnClickListener(v -> pagePrev());
    pagePrev.setOnClickListener(v -> pagePrev());
    listenBtn.setOnClickListener(v -> listenCurrentPage());
    deleteBtn.setOnClickListener(v -> deleteCurrentPage());
    recordBtn.setOnClickListener(v -> recordCurrentPage());
    submitBtn.setOnClickListener(v -> submit());

    View recording_stop = findViewById(R.id.recording_stop);
    recording_stop.setOnClickListener(v -> stopRecording());
    View listening_stop = findViewById(R.id.listening_stop);
    listening_stop.setOnClickListener(v -> stopListening());
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

  private void check() {
    showProgress(getString(R.string.hfx_loading));
    String extra = HfxUtil.getExtraInfo(this, workId);
    ApiHandler.getBaseApi()
        .checkBook(workId, extra)
        .enqueue(new Callback<NetResult>() {
          @Override
          public void onResponse(Call<NetResult> call, Response<NetResult> response) {
            if (response.isSuccessful()) {
              if (!isFinishing()) {
                NetResult result = response.body();
                if (result == null) {
                  hideProgress();
                  showErrorDialog(getString(R.string.common_network_none_tips), true);
                } else if (result.errcode != 0) {
                  if (result.errcode == 1001) {
                    login();
                    finish();
                  } else {
                    hideProgress();
                    showErrorDialog(result.errmsg, true);
                  }

                } else if (result.result != 0) {
                  hideProgress();

                  switch (result.status) {
                    case "doing":
                      //审核中和审核通过可能会变成审核未过，所以按照审核未过处理
                      HfxPreferenceUtil
                          .setRecordBookInWork(getApplicationContext(), user_id, workId, true);
                      showMyWorkDialog(MyWorkActivity.TAB_CHECKING);
                      break;
                    case "pass":
                      HfxPreferenceUtil
                          .setRecordBookInWork(getApplicationContext(), user_id, workId, true);
                      showPersonalAlbumDialog(result.album_url);
//                                            showMyWorkDialog(MyWorkActivity.TAB_PASS);
                      break;
                    case "block":
                      boolean isInBlock = HfxPreferenceUtil
                          .isRecordBookInWorkDefTrue(getApplicationContext(), user_id, workId);
                      if (isInBlock) {
                        //如果已经提交过，说明没有修改作品，则应当跳转到审核未过tab
                        //点击修改作品会将已提交过在本地置成false
                        showMyWorkDialog(MyWorkActivity.TAB_BLOCK);
                      } else {
                        //如果本地boolean标识没提交过，说明修改了作品
                        //此时应当认为作品是制作中状态
                        updateProgress(getString(R.string.hfx_res_loading));
                        initRes();
                      }
                      break;
                    default:
                      HfxPreferenceUtil
                          .setRecordBookInWork(getApplicationContext(), user_id, workId, false);
                      updateProgress(getString(R.string.hfx_res_loading));
                      initRes();
                      break;
                  }
                } else {
                  HfxPreferenceUtil
                      .setRecordBookInWork(getApplicationContext(), user_id, workId, false);
                  updateProgress(getString(R.string.hfx_res_loading));
                  initRes();
                }
              }
            } else {
              if (!isFinishing()) {
                hideProgress();
                showErrorDialog(getString(R.string.hfx_loading_fail), true);
              }
            }
          }

          @Override
          public void onFailure(Call<NetResult> call, Throwable t) {
            if (!isFinishing()) {
              hideProgress();
              showErrorDialog(getString(R.string.hfx_loading_fail), true);
            }
          }
        });
  }

  private void showMyWorkDialog(final int tab) {
    showDialog(getString(R.string.hfx_tip),
        getString(R.string.hfx_person_album),
        getString(R.string.hfx_confirm),
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            MyWorkActivity.openMyWork(RecordActivity.this, tab);
            finish();
          }
        },
        getString(R.string.hfx_cancel),
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (needShowothers && book.workuser != null && !book.workuser.isEmpty()) {
              showWorkersDialog();
            } else {
              finish();
            }
          }
        });
  }

  private void showPersonalAlbumDialog(final String url) {
    showDialog(getString(R.string.hfx_tip),
        getString(R.string.hfx_person_album),
        getString(R.string.hfx_confirm),
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            openView(url);
            finish();
          }
        },
        getString(R.string.hfx_cancel),
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (needShowothers && book.workuser != null && !book.workuser.isEmpty()) {
              showWorkersDialog();
            } else {
              finish();
            }
          }
        });
  }

  private void initJsonData() {
    new InitJsonTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workId, url);
  }

  protected File getBookConfigFile(String bookId) {
    return HfxFileUtil.getBookConfigFile(this, bookId);
  }

  private class InitJsonTask extends WeakAsyncTask<String, Void, Void, RecordActivity> {

    Huiben book;

    public InitJsonTask(RecordActivity recordActivity) {
      super(recordActivity);
    }

    @Override
    protected Void doInBackground(RecordActivity recordActivity, String... params) {
      Context context = recordActivity.getApplicationContext();
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
        Request request = new Request.Builder()
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
    protected void onProgressUpdate(RecordActivity recordActivity, Void... values) {
      if (recordActivity != null && !recordActivity.isFinishing()) {
        recordActivity.onInitJsonDone(book);
      }
    }

    @Override
    protected void onPostExecute(RecordActivity recordActivity, Void aVoid) {
      if (recordActivity != null && !recordActivity.isFinishing()) {
        recordActivity.onInitJsonDone(book);
      }
    }
  }

  private void onInitJsonDone(Huiben book) {
    if (this.book != null) {
      return;
    }
    hideProgress();
    if (book == null) {
      showErrorDialog(getString(R.string.hfx_loading_fail), true);
    } else {

      this.book = book;
      contentType = book.content_type;
      setTitle(book.bookname);
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
            Intent intent = new Intent(RecordActivity.this, ReadBookActivity.class);
            intent.putExtra(ReadBookActivity.ARG_JSON_URL, book.workuser.get(which).url);
            startActivity(intent);
            finish();
          }
        })
        .setPositiveButton(R.string.hfx_workers_btn, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            showWarning();
          }
        })
        .setNegativeButton(R.string.hfx_limit_quit, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            finish();
          }
        })
        .setCancelable(false)
        .create().show();
  }

  private class WorkerAdapter extends ArrayAdapter<Huiben.WorkUser> {

    LayoutInflater mInflater;

    public WorkerAdapter(Context context, List<Huiben.WorkUser> data) {
      super(context, 0, data);
      mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = mInflater.inflate(R.layout.hfx_layout_worker_item, parent, false);
      }
      CircleImageView imageView = (CircleImageView) convertView.findViewById(R.id.speaker_header);
      TextView title = (TextView) convertView.findViewById(R.id.speaker_title);
      TextView info = (TextView) convertView.findViewById(R.id.speaker_info);
      TextView time = (TextView) convertView.findViewById(R.id.speaker_time);
      TextView comment = (TextView) convertView.findViewById(R.id.speaker_comment);
      Huiben.WorkUser worker = getItem(position);
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
        commentString += commentcount + "评";
      }
      if (worker.readcount > 0) {
        commentString += " " + readcount + "阅";
      }
      comment.setText(commentString);
//            comment.setText(getString(R.string.workers_comment, commentcount, readcount));
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
    if (needCheck && NetworkUtil.isNetworkAvailable(this)) {
      check();
    } else {
      initRes();
    }
  }

  private void initRes() {
    showInitDialog();
    initResTask = new InitResTask(this, true);
    initResTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, workId, userUrl);
  }

  private void showInitDialog() {
    showDeterminateProgress(
        getString(R.string.hfx_init),
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

  private static class InitResTask extends WeakAsyncTask<String, Integer, Void, RecordActivity> {

    boolean showTips;
    private boolean isPageFail = false;

    public InitResTask(RecordActivity recordActivity, boolean showTips) {
      super(recordActivity);
      this.showTips = showTips;
    }

    @Override
    protected Void doInBackground(RecordActivity recordActivity, String... params) {
      Context context = recordActivity.getApplicationContext();
      OkHttpClient okHttpClient = NetWorkHelper.getOkHttpClient();
      String bookId = params[0];
      String userUrl = params[1];
      Huiben book = recordActivity.book;
      initBookPage(context, bookId, okHttpClient, book.bookpage);
      initBookAudio(context, bookId, okHttpClient, book.bookaudio);
      if (!TextUtils.isEmpty(userUrl) && !recordActivity.isMaking && NetworkUtil
          .isNetworkAvailable(context)) {
        Logger.d(TAG, "request user url: " + userUrl);
        Request request = new Request.Builder()
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
      copyTempToWork(context, bookId);
      copyWorkToTemp(context, bookId);
      return null;
    }

    private void initBookPage(Context context, String bookId, OkHttpClient okHttpClient,
        List<Huiben.BookPage> pages) {
      if (pages != null) {
        int i = 0;
        Properties properties = HfxFileUtil.getBookProp(context, bookId);
        for (Huiben.BookPage page : pages) {
          if (isCancelled()) {
            return;
          }
          publishProgress(0, ++i, pages.size());
          boolean needUpdate = true;
          if (properties != null) {
            String md5Url = properties.getProperty(page.page_name);
            needUpdate =
                md5Url == null || !md5Url.equals(com.namibox.util.MD5Util.md5(page.page_url));
          }
          File file = HfxFileUtil.getBookImageFile(context, bookId, page.page_name);
          if (!file.exists() || needUpdate) {
            Logger.d(TAG, "request image: " + page.page_name);
            Request request = new Request.Builder()
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
                    com.namibox.util.MD5Util.md5(page.page_url));
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
        List<Huiben.BookAudio> audios) {
      if (audios != null) {
        int i = 0;
        Properties properties = HfxFileUtil.getBookProp(context, bookId);
        for (Huiben.BookAudio audio : audios) {
          if (isCancelled()) {
            return;
          }
          publishProgress(1, ++i, audios.size());
          boolean needUpdate = true;
          if (properties != null) {
            String md5Url = properties.getProperty(audio.mp3_name);
            needUpdate =
                md5Url == null || !md5Url.equals(com.namibox.util.MD5Util.md5(audio.mp3_url));
          }
          File file = HfxFileUtil.getBookAudioFile(context, bookId, audio.mp3_name);
          if (!file.exists() || needUpdate) {
            Logger.d(TAG, "request mp3: " + audio.mp3_name);
            Request request = new Request.Builder()
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
                    com.namibox.util.MD5Util.md5(audio.mp3_url));
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
        List<Huiben.BookAudio> audios) {
      if (audios != null) {
        int i = 0;
        for (Huiben.BookAudio audio : audios) {
          if (isCancelled()) {
            return;
          }
          File file = HfxFileUtil.getUserAudioFile(context, bookId, audio.mp3_name);
          if (!file.exists()) {
            Logger.d(TAG, "request user mp3: " + audio.mp3_name);
            Request request = new Request.Builder()
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

    private void copyWorkToTemp(Context context, String bookId) {
      File workDir = HfxFileUtil.getUserWorkDir(context, bookId);
      File tempDir = HfxFileUtil.getUserTempDir(context, bookId);
      try {
        File[] files = workDir.listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().endsWith(HfxFileUtil.AUDIO_TYPE);
          }
        });
        int i = 0;
        for (File file : files) {
          if (isCancelled()) {
            return;
          }
          Logger.d(TAG, "copy: " + file + " -> " + tempDir);
          FileUtil.copyFile(file, tempDir, null, false);
          publishProgress(3, ++i, files.length);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    private void copyTempToWork(Context context, String bookId) {
      File workDir = HfxFileUtil.getUserWorkDir(context, bookId);
      File tempDir = HfxFileUtil.getUserTempDir(context, bookId);
      try {
        File[] files = tempDir.listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().endsWith(HfxFileUtil.AUDIO_TYPE)
                && !pathname.getName().startsWith("eval");
          }
        });
        int i = 0;
        for (File file : files) {
          if (isCancelled()) {
            return;
          }

          FileUtil.copyFile(file, workDir, null, true);
          publishProgress(3, ++i, files.length);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    protected void onCancelled(RecordActivity recordActivity, Void aVoid) {
      Logger.w(TAG, "[InitResTask] onCancelled");
    }

    @Override
    protected void onProgressUpdate(RecordActivity recordActivity, Integer... values) {
      if (recordActivity != null && !recordActivity.isFinishing()) {
        int type = values[0];
        int progress = values[1];
        int max = values[2];
        recordActivity.updateInitDialog(type, progress, max);
      }
    }

    @Override
    protected void onPostExecute(RecordActivity recordActivity, Void aVoid) {
      if (recordActivity != null && !recordActivity.isFinishing()) {
        recordActivity.hideInitDialog();
        if (isPageFail) {
          recordActivity
              .showErrorDialog(recordActivity.getString(R.string.hfx_loading_res_failed), true);
        } else {
          recordActivity.hideProgress();
          recordActivity.showRecordTips(showTips);
        }

      }
    }
  }

  private void showRecordTips(boolean showTips) {
    if (showTips) {
      new AlertDialog.Builder(this)
          .setView(R.layout.hfx_layout_record_step)
          .setCancelable(false)
          .setPositiveButton(R.string.hfx_confirm, new DialogInterface.OnClickListener() {
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
    }, Manifest.permission.RECORD_AUDIO);
    controlBar.setVisibility(View.VISIBLE);
    setState(State.IDLE);
    initCurrentPage();
    update();
  }

  private void initCurrentPage() {
    int i = 0;
    for (Huiben.BookPage page : book.bookpage) {
      File audioTemp = getAudioOfCurrentPage(page.page_name);
      if (!audioTemp.exists()) {
        currentPage = i;
        break;
      }
      i++;
    }
    if (currentPage >= book.bookpage.size()) {
      currentPage = book.bookpage.size() - 1;
    }
  }

  protected void update() {
    pageState = new boolean[book.bookpage.size()];
    int i = 0;
    int progress = 0;
    boolean hasAudio = false;
    for (Huiben.BookPage page : book.bookpage) {
      File audioTemp = getAudioOfCurrentPage(page.page_name);
      if (audioTemp.exists()) {
        progress++;
        pageState[i] = true;
        if (i == currentPage) {
          hasAudio = true;
        }
      }
      i++;
    }
    updateImage();
    updatePageLayout();
    updatePageProgress(pageState);
    updateControlBar(hasAudio, progress);
    updateRecordProgress(progress);
    if (getBookAudioOfCurrentPage().exists()) {
      setMenu(getString(R.string.hfx_original), false, new OnClickListener() {
        @Override
        public void onClick(View v) {
          File audio = getBookAudioOfCurrentPage();
          playAudio(audio);
        }
      });
    } else {
      hideMenu();
    }
  }

  protected File getAudioOfCurrentPage() {
    Huiben.BookPage page = book.bookpage.get(currentPage);
    return HfxFileUtil.getUserAudioTempFileByPage(this, workId, page.page_name);
  }

  protected File getAudioOfCurrentPage(String pageName) {
    return HfxFileUtil.getUserAudioTempFileByPage(this, workId, pageName);
  }

  protected File getUserAudioFileByPage(String pageName) {
    return HfxFileUtil.getUserAudioFileByPage(this, workId, pageName);
  }

  private File getBookAudioOfCurrentPage() {
    Huiben.BookPage page = book.bookpage.get(currentPage);
    return HfxFileUtil.getBookAudioFileByPage(this, workId, page.page_name);
  }

  private void updateImage() {
    String pageName = book.bookpage.get(currentPage).page_name;
    File image = HfxFileUtil.getBookImageFile(this, workId, pageName);
    RequestOptions options = new RequestOptions()
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.NONE);
    Glide.with(this)
        .asBitmap()
        .load(image)
        .apply(options)
        .into(new SimpleTarget<Bitmap>() {
          @Override
          public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
            imageView.setImageBitmap(resource, 0);
          }
        });
  }

  private void updatePageLayout() {
    pagePrev.setVisibility(currentPage == 0 ? View.GONE : View.VISIBLE);
    pageNext.setVisibility(currentPage == book.bookpage.size() - 1 ? View.GONE : View.VISIBLE);
  }

  private void updatePageProgress(boolean[] state) {
    pageProgress.setProgress(state, currentPage);
  }

  private void updateControlBar(boolean hasAudio, int progress) {
    listenBtn.setVisibility(hasAudio ? View.VISIBLE : View.GONE);
    deleteBtn.setVisibility(hasAudio ? View.VISIBLE : View.GONE);
    recordBtn.setText(hasAudio ? R.string.hfx_record_retry : R.string.hfx_record_new);
    submitBtn.setEnabled(100 * progress / book.bookpage.size() >= 50);
  }

  private void updateRecordProgress(int progress) {
    int total = book.bookpage.size();
    recordProgress.setMax(total);
    recordProgress.setProgress(progress);
    recordProgressText.setText(getString(R.string.hfx_record_progress, progress, total));
  }

  private void updateRecordingLayout(long time) {
    double volume = getVolume();
    int level = (int) (volume * 5 / 10000);
    if (level > 5) {
      level = 5;
    }
    recordingVolume.setImageLevel(level);
    recordingTime.setText(Utils.makeTimeString((int) time));
  }

  void pagePrev() {
    if (currentPage > 0 && currentPage <= book.bookpage.size() - 1) {
      currentPage--;
      update();
    }
  }

  void pageNext() {
    if (currentPage >= 0 && currentPage < book.bookpage.size() - 1) {
      currentPage++;
      update();
    }
  }

  void toggleIdle() {
    if (state == State.IDLE) {
      if (controlLayout.isShown()) {
        getSupportActionBar().hide();
        controlLayout.setVisibility(View.GONE);
      } else {
        getSupportActionBar().show();
        controlLayout.setVisibility(View.VISIBLE);
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
        getSupportActionBar().show();
        pageLayout.setVisibility(View.VISIBLE);
        controlLayout.setVisibility(View.VISIBLE);
        countdownText.setVisibility(View.GONE);
        recordingLayout.setVisibility(View.GONE);
        listeningLayout.setVisibility(View.GONE);
        break;
      case COUNTDOWN:
        getSupportActionBar().hide();
        pageLayout.setVisibility(View.GONE);
        controlLayout.setVisibility(View.GONE);
        countdownText.setVisibility(View.VISIBLE);
        recordingLayout.setVisibility(View.GONE);
        listeningLayout.setVisibility(View.GONE);
        break;
      case RECORD:
        getSupportActionBar().hide();
        pageLayout.setVisibility(View.GONE);
        controlLayout.setVisibility(View.GONE);
        countdownText.setVisibility(View.GONE);
        recordingLayout.setVisibility(View.VISIBLE);
        listeningLayout.setVisibility(View.GONE);
        break;
      case LISTEN:
        listeningProgress.setProgress(0);
        getSupportActionBar().hide();
        pageLayout.setVisibility(View.GONE);
        controlLayout.setVisibility(View.GONE);
        countdownText.setVisibility(View.GONE);
        recordingLayout.setVisibility(View.GONE);
        listeningLayout.setVisibility(View.VISIBLE);
        break;
    }
  }

  void listenCurrentPage() {
    File audio = getAudioOfCurrentPage();
    playAudio(audio);
  }

  private void playAudio(File audio) {
    if (state != State.IDLE) {
      return;
    }
    if (audio.exists()) {
      setState(State.LISTEN);
      exoAudioPlayer.play(Uri.fromFile(audio));
    } else {
      toast(getString(R.string.hfx_error_listen));
    }

  }

  void deleteCurrentPage() {
    if (state != State.IDLE) {
      return;
    }
    showDialog(getString(R.string.hfx_tip),
        getString(R.string.hfx_record_delete_confirm),
        getString(R.string.hfx_confirm),
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            deleteAudioOfCurrentPage();
          }
        },
        getString(R.string.hfx_cancel),
        null);
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

  void recordCurrentPage() {
    if (state != State.IDLE) {
      return;
    }
    File audio = getAudioOfCurrentPage();
    if (audio.exists()) {
      showDialog(
          getString(R.string.hfx_tip),
          getString(R.string.hfx_re_record_page),
          getString(R.string.hfx_confirm),
          new OnClickListener() {
            @Override
            public void onClick(View v) {
              startCountDown();
            }
          },
          getString(R.string.hfx_cancel),
          null);
    } else {
      startCountDown();
    }
  }

  private void startCountDown() {
    setState(State.COUNTDOWN);
    countDownTimer.start();
  }

  protected void startRecording() {
    setState(State.RECORD);
    File audio = getAudioOfCurrentPage();
    try {
      startMediaRecorder(audio);
      startRecordTime = System.currentTimeMillis();
      handler.sendEmptyMessage(MESSAGE_RECORD_UPDATE);
    } catch (Exception e) {
      e.printStackTrace();
      setState(State.IDLE);
//            showErrorDialog(getString(R.string.record_fail), false);
      checkAudio();
      isChanged = true;
      update();
    }
  }

  protected void stopRecording() {
    stopRecord();
  }

  protected void stopRecord() {
    if (state == State.RECORD) {
      handler.removeMessages(MESSAGE_RECORD_UPDATE);
      stopAudioRecord();
      setState(State.IDLE);
      checkAudio();
      isChanged = true;
      update();
    }
  }

  private void checkAudio() {
    File audio = getAudioOfCurrentPage();
    if (!audio.exists() || audio.length() == 0) {
      showDialog(getString(R.string.hfx_record_init_failed),
          getString(R.string.hfx_init_failed),
          getString(R.string.hfx_confirm),
          new OnClickListener() {
            @Override
            public void onClick(View v) {
              deleteAudioOfCurrentPage();
            }
          });
    }
  }

  void stopListening() {
    if (state == State.LISTEN) {
      exoAudioPlayer.stop();
      setState(State.IDLE);
    }
  }

  @Override
  public void onBackPressed() {

    if (state != State.IDLE) {
      return;
    }
    if (isChanged) {
      showSaveDialog();
    } else {
      finish();
    }


  }

  void saveIntroduce(String introduce) {
    this.introduce = introduce;
    String user_id = Utils.getLoginUserId(getApplicationContext());
    HfxPreferenceUtil.saveBookIntroduce(this, user_id, workId, introduce);
  }

  private void showSaveDialog() {
    showDialog(getString(R.string.hfx_tip),
        getString(R.string.hfx_save_uncomplete),
        getString(R.string.hfx_save),
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            HfxPreferenceUtil.setRecordBookInWork(RecordActivity.this, user_id, workId, false);
            saveAndQuit(false);
          }
        },
        getString(R.string.hfx_exit),
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            deleteAndQuit();
          }
        }, new OnClickListener() {
          @Override
          public void onClick(View view) {
          }
        });
  }

  protected void saveAndQuit(boolean commit) {
    showProgress(getString(R.string.hfx_saving));
    new SaveQuitTask(this, commit).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  private class SaveQuitTask extends WeakAsyncTask<Void, Void, Void, RecordActivity> {

    boolean commit;

    public SaveQuitTask(RecordActivity recordActivity, boolean commit) {
      super(recordActivity);
      this.commit = commit;
    }

    @Override
    protected Void doInBackground(RecordActivity recordActivity, Void... params) {
      Context context = recordActivity.getApplicationContext();
      String bookId = recordActivity.workId;
      List<Huiben.BookPage> bookpage = recordActivity.book.bookpage;
      File workDir = HfxFileUtil.getUserWorkDir(context, bookId);
      for (Huiben.BookPage page : bookpage) {
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
    protected void onPostExecute(RecordActivity recordActivity, Void aVoid) {
      if (recordActivity != null && !recordActivity.isFinishing()) {
        recordActivity.hideProgress();
        if (commit) {
          recordActivity.setOrientation(true);
          recordActivity.openCommit();
//                    recordActivity.isChanged = false;
        } else {
          recordActivity.finish();
          //不再需要提示框
//          recordActivity.openMyWorkAndFinish(MyWorkActivity.TAB_MAKING);

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

  private void deleteAndQuit() {
    showProgress(getString(R.string.hfx_quiting));
    new DeleteQuitTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  private static class DeleteQuitTask extends WeakAsyncTask<Void, Void, Void, RecordActivity> {

    public DeleteQuitTask(RecordActivity recordActivity) {
      super(recordActivity);
    }

    @Override
    protected Void doInBackground(RecordActivity recordActivity, Void... params) {
      Context context = recordActivity.getApplicationContext();
      String bookId = recordActivity.workId;
      File temp = HfxFileUtil.getUserTempDir(context, bookId);
      Logger.d(TAG, "del: " + temp);
      FileUtil.deleteEvalDir(temp, false);
      return null;
    }

    @Override
    protected void onPostExecute(RecordActivity recordActivity, Void aVoid) {
      if (recordActivity != null && !recordActivity.isFinishing()) {
        recordActivity.hideProgress();
        recordActivity.finish();
      }
    }
  }

  void submit() {
    saveAndQuit(true);
  }

}
