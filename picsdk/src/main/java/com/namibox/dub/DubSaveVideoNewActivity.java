package com.namibox.dub;


import static com.namibox.util.Utils.isOldDevice;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.FragmentLifecycleCallbacks;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import sdk.DubbingResultEvent;
import com.example.exoaudioplayer.video.base.Constants;
import com.example.exoaudioplayer.video.fragment.VideoFragment;
import com.example.exoaudioplayer.video.model.MediaBuilder;
import com.example.picsdk.PicGuideActivity;
import com.example.picsdk.R;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namibox.commonlib.activity.BaseActivity;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.event.FinishVoiceActivity;
import com.namibox.commonlib.event.OssEvent;
import com.namibox.commonlib.event.SaveLocalReadingEvent;
import com.namibox.commonlib.event.WorkEvent;
import com.namibox.commonlib.model.OssToken;
import com.namibox.commonlib.model.QiniuToken;
import com.namibox.hfx.utils.TextStyleUtil;
import com.namibox.tools.OssUploadUtil;
import com.namibox.util.AppUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DefaultSubscriber;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.reactivestreams.Publisher;
import sdk.model.DubbingResultBean;


/**
 * Created by sunha on 2016/12/26 0026.
 */

public class DubSaveVideoNewActivity extends BaseActivity {

  private static final String TAG = "DubSaveVideoNewActivity";
  FrameLayout videoLayout;
  ScrollView scrollView;
  EditText introEt;
  TextView videoTitle;
  ImageView scoreImg;
  TextView scoreText;
  ProgressBar fluencyPro;
  ProgressBar pronPro;
  ProgressBar integrityPro;
  TextView fluencyScore;
  TextView pronScore;
  TextView integrityScore;
  LinearLayout buttonLayout;
  LinearLayout scoreLayout;
  Button submitBtn;
  private VideoFragment mVideoFragment;
  private AudioManager mAm;
  private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = focusChange -> {
    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
      abandonFocus();
    }
  };
  private File videoFile;
  private Uri contentUri;
  private String itemId;
  private String subtype;
  private String type;
  private String video_name;
  private String thumb_url, jsonUrl;
  private String relation_id;
  private QiniuToken qiniuToken;
  private Disposable upLoadDisposable;
  private boolean isSubmit = false;

  //是否提交,防止重复点击
  private boolean isSubmiting = false;
  private int score;
  private int pron;
  private int integrity;
  private int fluency;
  private int startType;
  private OssToken ossToken;
  private String userid;
  private String match_id;
  private long homework_id;
  private long milesson_id;
  private String mItem_id;
  private String ossTokenUrl;
  private String reportUrl;
  private String worksubmitUrl;
  private int windowWidth;
  private int windowHeight;
  private long book_id;
  private SoundPool soundPool;
  private int clickID;
  private int winID;
  private int failedID;

  private boolean requestFocus() {
    // Request audio focus for playback
    int result = mAm.requestAudioFocus(audioFocusChangeListener,
        // Use the music stream.
        AudioManager.STREAM_MUSIC,
        // Request permanent focus.
        AudioManager.AUDIOFOCUS_GAIN);
    return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  }

  private int abandonFocus() {
    return mAm.abandonAudioFocus(audioFocusChangeListener);
  }

  public static void openLocalVideo(Context context, File videoFile, String itemId,
      String subtype, String type, String video_name, String thumb_url, String relationId,
      String jsonUrl, int score, int pron, int integrity, int fluency, int startType,
      String mItem_id, long milesson_id, String ossTokenUrl, String reportUrl, String userid,
      long book_id, String match_id, long homework_id, String worksubmitUrl) {

    Intent intent = new Intent(context, DubSaveVideoNewActivity.class)
        .setData(Uri.fromFile(videoFile))
        .putExtra("video_file", videoFile.getAbsolutePath())
        .putExtra("itemId", itemId)
        .putExtra("subtype", subtype)
        .putExtra("type", type)
        .putExtra("video_name", video_name)
        .putExtra("thumb_url", thumb_url)
        .putExtra("json_url", jsonUrl)
        .putExtra("score", score)
        .putExtra("pron", pron)
        .putExtra("integrity", integrity)
        .putExtra("fluency", fluency)
        .putExtra("startType", startType)
        .putExtra("tutorable_relation_id", relationId)
        .putExtra("mItem_id", mItem_id)
        .putExtra("milesson_id", milesson_id)
        .putExtra("ossTokenUrl", ossTokenUrl)
        .putExtra("reportUrl", reportUrl)
        .putExtra("worksubmitUrl", worksubmitUrl)
        .putExtra("userid", userid)
        .putExtra("match_id", match_id)
        .putExtra("homework_id", homework_id)
        .putExtra("book_id", book_id);
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAm = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    requestFocus();
    setContentView(R.layout.hfx_activity_save_dubvideo_new);
    initView();
    contentUri = getIntent().getData();
    Intent intent = getIntent();
    String videoFilePath = intent.getStringExtra("video_file");
    itemId = intent.getStringExtra("itemId");
    subtype = intent.getStringExtra("subtype");
    type = intent.getStringExtra("type");
    video_name = intent.getStringExtra("video_name");
    thumb_url = intent.getStringExtra("thumb_url");
    jsonUrl = intent.getStringExtra("json_url");
    relation_id = intent.getStringExtra("tutorable_relation_id");
    score = intent.getIntExtra("score", 0);
    pron = intent.getIntExtra("pron", 0);
    integrity = intent.getIntExtra("integrity", 0);
    fluency = intent.getIntExtra("fluency", 0);
    startType = intent.getIntExtra("startType", 0);

    mItem_id = intent.getStringExtra("mItem_id");
    milesson_id = intent.getLongExtra("milesson_id", -1L);
    ossTokenUrl = intent.getStringExtra("ossTokenUrl");
    reportUrl = intent.getStringExtra("reportUrl");
    worksubmitUrl = intent.getStringExtra("worksubmitUrl");
    userid = intent.getStringExtra("userid");
    match_id = intent.getStringExtra("match_id");
    homework_id = intent.getLongExtra("homework_id", -1L);
    book_id = intent.getLongExtra("book_id", -1L);

    if (!TextUtils.isEmpty(videoFilePath)) {
      videoFile = new File(videoFilePath);
    } else {
      showErrorDialog("未找到视频文件", true);
    }
    Point point = new Point();
    getWindowManager().getDefaultDisplay().getSize(point);
    windowWidth = point.x;
    windowHeight = point.y;
    float template_ratio = 16f / 9;
    int videoHeight = (int) (windowWidth / template_ratio);
    ViewGroup.LayoutParams layoutParams = videoLayout.getLayoutParams();
    layoutParams.height = videoHeight;
    videoLayout.setLayoutParams(layoutParams);
    videoTitle.setText(video_name);
    initVideoFragment();
    initScore();
    initSound();
  }

  private void initView() {
    videoLayout = findViewById(R.id.video_layout);
    scrollView = findViewById(R.id.scrollView);
    introEt = findViewById(R.id.introEt);
    videoTitle = findViewById(R.id.videoTitle);
    scoreImg = findViewById(R.id.score_img);
    scoreText = findViewById(R.id.score_text);
    fluencyPro = findViewById(R.id.progress1);
    pronPro = findViewById(R.id.progress2);
    integrityPro = findViewById(R.id.progress3);
    fluencyScore = findViewById(R.id.fluency_score);
    pronScore = findViewById(R.id.pron_score);
    integrityScore = findViewById(R.id.integrity_score);
    buttonLayout = findViewById(R.id.button_layout);
    scoreLayout = findViewById(R.id.scoreLayout);
    submitBtn = findViewById(R.id.submitBtn);

    View back_btn = findViewById(R.id.back_btn);
    View saveBtn = findViewById(R.id.saveBtn);
    View submitBtn = findViewById(R.id.submitBtn);
    back_btn.setOnClickListener(this::onViewClick);
    saveBtn.setOnClickListener(this::onViewClick);
    submitBtn.setOnClickListener(this::onViewClick);
  }

  @TargetApi(21)
  private void initSound() {
    soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    clickID = soundPool.load(this, R.raw.click_voice, 1);
    winID = soundPool.load(this, R.raw.challenge_win, 1);
    failedID = soundPool.load(this, R.raw.challenge_lose, 1);
  }

  private void initScore() {
    scoreText.setText(score + "分");
    TextStyleUtil.setTextStytle(scoreText, "#333333", "", "分", 1.2f);
    pronPro.setProgress(pron);
    pronScore.setText(pron + "分");
    integrityPro.setProgress(integrity);
    integrityScore.setText(integrity + "分");
    fluencyPro.setProgress(fluency);
    fluencyScore.setText(fluency + "分");
    switch (startType) {
      case 0:
        playSound(failedID, 700);
        scoreImg.setBackgroundResource(R.drawable.hfx_video_stars_zero);
        if (PreferenceUtil.getLongLoginUserId(this) == -1L && PreferenceUtil
            .getSharePref(this, book_id + "趣味配音" + "star", 0) < 0) {
          PreferenceUtil.setSharePref(this, book_id + "趣味配音" + "star", 0);
        }
        break;
      case 1:
        playSound(winID, 700);
        scoreImg.setBackgroundResource(R.drawable.hfx_video_stars_one);
        if (PreferenceUtil.getLongLoginUserId(this) == -1L && PreferenceUtil
            .getSharePref(this, book_id + "趣味配音" + "star", 0) < 1) {
          PreferenceUtil.setSharePref(this, book_id + "趣味配音" + "star", 1);
        }
        break;
      case 2:
        playSound(winID, 700);
        scoreImg.setBackgroundResource(R.drawable.hfx_video_stars_two);
        if (PreferenceUtil.getLongLoginUserId(this) == -1L && PreferenceUtil
            .getSharePref(this, book_id + "趣味配音" + "star", 0) < 2) {
          PreferenceUtil.setSharePref(this, book_id + "趣味配音" + "star", 2);
        }
        break;
      case 3:
        playSound(winID, 700);
        scoreImg.setBackgroundResource(R.drawable.hfx_video_stars_three);
        if (PreferenceUtil.getLongLoginUserId(this) == -1L && PreferenceUtil
            .getSharePref(this, book_id + "趣味配音" + "star", 0) < 3) {
          PreferenceUtil.setSharePref(this, book_id + "趣味配音" + "star", 3);
        }
        break;
      default:
        break;
    }

    if (PreferenceUtil.getLongLoginUserId(this) == -1L) {
      EventBus.getDefault().post(new SaveLocalReadingEvent());
    }
  }

  private void initVideoFragment() {
    MediaBuilder mediaBuilder = new MediaBuilder.Builder()
        .setType(Constants.VIDEOSHOW_FRAGMENT)
        .setUri(contentUri.toString())
        .setTitle("")
        .build();
    mVideoFragment = new VideoFragment(mediaBuilder);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.video_fragment, mVideoFragment, "video_fragment")
        .commit();
    mVideoFragment.getFragmentManager().registerFragmentLifecycleCallbacks(
        new FragmentLifecycleCallbacks() {
          @Override
          public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v,
              Bundle savedInstanceState) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState);
            mVideoFragment.setOnBackCallBack(() -> onBackPressed());
          }
        }, false);
  }

  private void playSound(final int id, int delay) {
    new Handler().postDelayed(() -> soundPool.play(
        id,
        1f,      //左耳道音量【0~1】
        1f,      //右耳道音量【0~1】
        0,         //播放优先级【0表示最低优先级】
        0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
        1          //播放速度【1是正常，范围从0~2】
    ), delay);
  }

  public CompositeDisposable compositeDisposable = new CompositeDisposable();

  public void submitWork() {
    JsonObject jsonBody = new JsonObject();
    jsonBody.addProperty("homework_id", homework_id);

    Disposable disposable = ApiHandler.getBaseApi().commonJsonPost(worksubmitUrl, jsonBody)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<JsonElement>() {
          @Override
          public void accept(JsonElement jsonElement) throws Exception {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String retcode = jsonObject.get("retcode").getAsString();
            JsonObject data = jsonObject.get("data").getAsJsonObject();
            if (retcode != null && (retcode.equals("SUCC") || retcode.equals("success"))) {
              EventBus.getDefault().post(new FinishVoiceActivity());
              EventBus.getDefault().post(new WorkEvent(homework_id, "done"));
//              ARouter.getInstance().build("/stu/work_result")
//                  .withString("result", data.toString())
//                  .withLong("homework_id", homework_id)
//                  .withBoolean("isHiddenShare", true)
//                  .navigation();
            } else {
            }
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) throws Exception {
            Logger.e(throwable, throwable.toString());
//          showNoInternetDialog();
          }
        });
    compositeDisposable.add(disposable);
  }

  private void reportData(String video_key) {
    JsonObject jsonBody = getSyncBody(video_key);
    Logger.d(TAG, "reportData param:" + jsonBody + "--url:" + reportUrl);
    ApiHandler.getBaseApi().commonJsonElementPost(reportUrl, jsonBody)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doFinally(new Action() {
          @Override
          public void run() throws Exception {
            hideProgress();
          }
        })
        .subscribe(new DefaultSubscriber<JsonElement>() {
          @Override
          public void onNext(JsonElement jsonElement) {
            final JsonObject jsonObject = jsonElement.getAsJsonObject();
            Logger.d(TAG, "reportData result:" + jsonObject);
            DubbingResultBean resultBean = new DubbingResultBean(score, integrity, fluency, pron,
                AppUtil.getOssBaseUrl(DubSaveVideoNewActivity.this) + video_key);
            if (jsonObject.has("data")) {
              JsonObject data = jsonObject.get("data").getAsJsonObject();
              if (data.has("user_report_data")) {
                JsonObject reportData = data.get("user_report_data").getAsJsonObject();
                if (reportData.has("word_num")) {
                  resultBean.setWord_num(reportData.get("word_num").getAsInt());
                }
                if (reportData.has("answer_right_num")) {
                  resultBean.setAnswer_right_num(reportData.get("answer_right_num").getAsInt());
                }
                if (reportData.has("answer_num")) {
                  resultBean.setAnswer_num(reportData.get("answer_num").getAsInt());
                }
                if (reportData.has("text")) {
                  resultBean.setText(reportData.get("text").getAsString());
                }
                if (reportData.has("pb_num")) {
                  resultBean.setPb_num(reportData.get("pb_num").getAsInt());
                }
                if (reportData.has("thumb_url")) {
                  resultBean.setThumb_url(reportData.get("thumb_url").getAsString());
                }
                if (reportData.has("milesson_item_id")) {
                  resultBean.setMilesson_item_id(reportData.get("milesson_item_id").getAsInt());
                }
                if (reportData.has("dubbing_score")) {
                  resultBean.setDubbing_score(reportData.get("dubbing_score").getAsInt());
                }
                if (reportData.has("title")) {
                  resultBean.setTitle(reportData.get("title").getAsString());
                }
                if (reportData.has("chinese_name")) {
                  resultBean.setChinese_name(reportData.get("chinese_name").getAsString());
                }
                if (reportData.has("answer_rate")) {
                  resultBean.setAnswer_rate(reportData.get("answer_rate").getAsInt());
                }
              }
            }
            EventBus.getDefault().post(new DubbingResultEvent(resultBean));
            finish();
          }

          @Override
          public void onError(Throwable t) {
            showErrorDialog("发布作品出错!", true);
            Logger.e(t, "onError");
          }

          @Override
          public void onComplete() {
            Logger.e("success");
          }
        });
  }

  private JsonObject getSyncBody(String video_key) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("milesson_item_id", itemId);
    jsonObject.addProperty("milesson_id", milesson_id);
    jsonObject.addProperty("video_key", video_key);
    jsonObject.addProperty("score_avg", score);
    jsonObject.addProperty("integrity_avg", integrity);
    jsonObject.addProperty("fluency_avg", fluency);
    jsonObject.addProperty("pron_avg", pron);
    if (getPackageName().equals("org.jinxin.appstudent")) {
      jsonObject.addProperty("match_id", match_id);
    }
    return jsonObject;
  }

  private void getOsstoken() {
    if (ossToken == null) {
      upLoadDisposable = ApiHandler.getBaseApi()
          .OssTokenPost(ossTokenUrl, getOsstokenBody())
          .subscribeOn(Schedulers.io())
          .flatMap(new Function<OssToken, Flowable<OssToken>>() {
            @Override
            public Flowable<OssToken> apply(OssToken ossToken) throws Exception {
              List<OssToken> list = new ArrayList<>();
              if (videoFile.exists() && Utils.isLogin(DubSaveVideoNewActivity.this)) {
                OssToken ossToken1 = ossToken.clone();
                ossToken1.objectKey += "/" + videoFile.getName();
                ossToken1.uploadFile = videoFile;
                list.add(ossToken1);
              }
              return Flowable.fromIterable(list);
            }
          })
          .flatMap(new Function<OssToken, Publisher<OssEvent>>() {
            @Override
            public Publisher<OssEvent> apply(@NonNull OssToken ossToken) throws Exception {
              Logger.d(TAG, "ossToken:" + ossToken);
              return OssUploadUtil.getOssObservable(DubSaveVideoNewActivity.this, ossToken);
            }
          })
          .flatMap(new Function<OssEvent, Flowable<OssEvent>>() {
            @Override
            public Flowable<OssEvent> apply(OssEvent ossEvent) throws Exception {
              if (ossEvent.type == OssEvent.OssEventType.RESULT) {
                reportData(ossEvent.objectKey);
                return Flowable.just(ossEvent);
              } else if (ossEvent.type == OssEvent.OssEventType.PROGRESS) {
                return Flowable.empty();
              } else {
                return Flowable.empty();
              }
            }
          })
          .toList()
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Consumer<List<OssEvent>>() {
            @Override
            public void accept(List<OssEvent> ossEvents) throws Exception {

            }

          }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
              uploadFail();
              Logger.e(throwable, "onError");
            }
          });
    }
  }

  private JsonObject getOsstokenBody() {
    userid = Utils.getLoginUserId(this);
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("item_id", itemId);
    jsonObject.addProperty("objectKey",
        "pbook/userwork/userid/" + userid + "/milessionId/" + milesson_id + "/itemId/" + itemId
            + "/match_id/" + match_id);
    jsonObject.addProperty("bucket", "namibox");
    return jsonObject;
  }

  public static final int MODE_PORTRAIT = 0;//竖屏
  public static final int MODE_LANDSCRAPE = 1;//横屏分屏
  public static final int MODE_FULLSCREEN = 2;//横屏全屏
  private boolean isBackPressed;

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    Logger.d("onConfigurationChanged");
    super.onConfigurationChanged(newConfig);
    changeLayout(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT);
  }

  //根据横竖屏更改布局
  private void changeLayout(boolean portrait) {
    if (portrait) {
      setScreenMode(MODE_PORTRAIT);
      showStatusBar(true);
      portScreenAnim();
    } else {
      showStatusBar(false);
      setScreenMode(MODE_FULLSCREEN);
      fullScreenAnim(true);
    }
  }

  //竖屏
  private void portScreenAnim() {
    //直播竖屏布局
    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) videoLayout
        .getLayoutParams();
    lp.width = windowWidth;
    float template_ratio = 16f / 9;
    int videoHeight = (int) (windowWidth / template_ratio);
    lp.height = videoHeight;
    buttonLayout.setVisibility(View.VISIBLE);
    scoreLayout.setVisibility(View.VISIBLE);
    videoLayout.setLayoutParams(lp);
  }

  //全屏
  private void fullScreenAnim(boolean fromPort) {
    if (!fromPort) {
      if (!isOldDevice()) {
        TransitionManager.beginDelayedTransition(videoLayout, new TransitionSet()
            .addTransition(new ChangeBounds()));
      }
    }
    //直播全屏布局
    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) videoLayout
        .getLayoutParams();
    lp.width = windowHeight;
    lp.height = windowWidth;
    buttonLayout.setVisibility(View.GONE);
    scoreLayout.setVisibility(View.GONE);
    videoLayout.setLayoutParams(lp);
  }

  @Override
  public void onBackPressed() {
    Configuration config = getResources().getConfiguration();
    if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    } else {
      if (isSubmit) {
        EventBus.getDefault().post(new FinishVoiceActivity());
      } else {
        String title = "温馨提示";
        String text = "没有发布作品，是否退出？";
        String confirm = "确认";
        String cancel = "取消";
        showDialog(title, text, confirm, v -> finish(), cancel, v -> isBackPressed = false);
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    isSubmiting = false;
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    abandonFocus();
    if (soundPool != null) {
      soundPool.release();
    }
  }

  public void onViewClick(View view) {
    int i = view.getId();
    if (i == R.id.back_btn) {
      onBackPressed();
    } else if (i == R.id.saveBtn) {
      playSound(clickID, 0);
      if (NetworkUtil.isNetworkConnected(this)) {
        Intent intent = new Intent(this, DubVideoNewActivity.class);
        intent.putExtra("json_url", jsonUrl);
        startActivity(intent);
        finish();
      } else {
        toast("您的网络状况较差，请检查网络连接。");
      }
    } else if (i == R.id.submitBtn) {
      playSound(clickID, 0);

      if (isSubmit) {
//        CommonShareHelper
//            .commonShare(DubSaveVideoNewActivity.this, Const.SHARE_TIMELINE, shareObject,
//                new com.namibox.lib.share_pay_login_lib.callback.ShareCallback() {
//                  @Override
//                  public void onResult(boolean isSuccess, String msg) {
//                  }
//                });
        return;
      }

      if (!isSubmiting) {
        if (PreferenceUtil.getLongLoginUserId(this) == -1L) {
          Intent intent = new Intent(this, PicGuideActivity.class);
          startActivity(intent);
        } else {
          if (NetworkUtil.isNetworkConnected(this)) {
            showProgress("发布作品中...");
            isSubmiting = true;
            mVideoFragment.setPlayWhenReady(false);
            syncUpData();
          } else {
            toast("您的网络状况较差，请检查网络连接。");
          }
        }
//        upLoadDisposable = ApiHandler.getBaseApi().getFileUploadToken(itemId, "mp4")
//            .flatMap(new Function<QiniuToken, Flowable<QiniuEvent>>() {
//
//              @Override
//              public Flowable<QiniuEvent> apply(@NonNull QiniuToken token)
//                  throws Exception {
//                qiniuToken = token;
////                                    isUploading = true;
//                String dir = FileUtil.getFileCacheDir(DubSaveVideoNewActivity.this)
//                    .getAbsolutePath();
//                return QiniuUploadUtil.upload(dir, videoFile, token.key, token.token);
//              }
//            })
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .doOnNext(new Consumer<QiniuEvent>() {
//              @Override
//              public void accept(QiniuEvent event) throws Exception {
//                if (event.type == QiniuEventType.RESULT) {
//                  Logger.i(TAG, event.key + ",\r\n " + event.info + ",\r\n " + event.response);
//                  if (event.info == null || !event.info.isOK()) {
//                    uploadFail();
//                  }
//                } else if (event.type == QiniuEventType.PROGRESS) {
//                  updateDeterminateProgress(
//                      "视频上传中... [" + (int) (event.progress * 100) + "/" + 100 + "]",
//                      (int) (event.progress * 100));
//                } else if (event.type == QiniuEventType.ERROR) {
//                  if (Utils.isDev(DubSaveVideoNewActivity.this)) {
//                    toast(event.message);
//                  }
//                  Logger.e(TAG, "ERROR: " + event.message);
//                  uploadFail();
//                }
//              }
//            })
//            .filter(new Predicate<QiniuEvent>() {
//              @Override
//              public boolean test(@NonNull QiniuEvent qiniuEvent) throws Exception {
//                return qiniuEvent.type == QiniuEventType.RESULT;
//              }
//            })
//            .observeOn(Schedulers.io())
//            .flatMap(new Function<QiniuEvent, Flowable<NetResult>>() {
//              @Override
//              public Flowable<NetResult> apply(@NonNull QiniuEvent event) throws Exception {
//                Logger.i(TAG, "apply: getSubmitObservable");
//                return getSubmitObservable();
//              }
//            })
//            .compose(RxLifecycle.bind(this).<NetResult>withFlowable())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(new Consumer<NetResult>() {
//              @Override
//              public void accept(NetResult result) throws Exception {
//                Logger.i(TAG, "onCompleted: " + result.errcode);
//                if (result.errcode == 0) {
//                  isSubmiting = false;
//                  hideDeterminateProgress();
//                  HashMap<String, Object> map = new HashMap<>();
//                  //make_finsh是后端定的,不是拼写错误
//                  map.put("command", "make_finsh");
//                  EventBus.getDefault()
//                      .post(new MessageEvent("", new Gson().toJson(map), "make_finsh"));
//                  EventBus.getDefault().post(new DubExitEvent());
//                  openDubShare(result.wxshare.imgurl,
//                      result.wxshare.doclink,
//                      result.wxshare.grouptitile, result.wxshare.friendtitile,
//                      result.wxshare.groupcontent,result.hearts);
//                  finish();
//                } else {
//                  uploadFail();
//                }
//              }
//            }, new Consumer<Throwable>() {
//              @Override
//              public void accept(Throwable throwable) throws Exception {
//                uploadFail();
//                Logger.e(throwable, "onError");
//              }
//            });
      }

    }
  }

  private void syncUpData() {
    if (NetworkUtil.isNetworkConnected(this)) {
      getOsstoken();
    } else {
      showNoInternetDialog();
    }
  }

  private void showNoInternetDialog() {
    showDialog("提示", "您的网络状况较差，请检查网络连接。若选择退出，将无法保存答题成绩。",
        "退出", view -> {
          Intent intent = new Intent(DubSaveVideoNewActivity.this, PicGuideActivity.class);
          startActivity(intent);
        }, "重新连接", v -> syncUpData());
  }

  private void uploadFail() {
    isSubmiting = false;
    hideDeterminateProgress();
    hideProgress();
    showErrorDialog("提交失败,请重试!", false);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void refreshStoreInfo(FinishVoiceActivity event) {
    finish();
  }
}
