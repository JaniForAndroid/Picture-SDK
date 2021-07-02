package com.example.picsdk;

import static com.example.picsdk.util.AppPicUtil.CHALLENGE_WORD;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.example.picsdk.base.BaseActivity;
import com.example.picsdk.event.RefreshStoreInfo;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.model.ProductItem;
import com.example.picsdk.model.ReportVocabulary;
import com.example.picsdk.music.MusicPicActivity;
import com.example.picsdk.util.AppPicUtil;
import com.example.picsdk.util.BarUtil;
import com.example.picsdk.util.PicturePreferenceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.event.OssEvent;
import com.namibox.commonlib.model.OssToken;
import com.namibox.tools.OssUploadUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;
import org.reactivestreams.Publisher;
import pl.droidsonroids.gif.GifDrawable;

public class ResultActivity extends BaseActivity {

  public static final String ARG_LINK = "link";
  public static final int STEP1 = 1;
  public static final int STEP2 = 2;
  public static final int STEP3 = 3;
  public static final int STEP4 = 4;

  ImageView image;
  TextView text1,tv_timer;
  TextView text2;
  TextView index1;
  TextView index2;
  TextView index3;
  TextView index4;
  TextView index_text1;
  TextView index_text2;
  TextView index_text3;
  TextView index_text4;
  TextView line1;
  TextView line2;
  TextView line3;
  Button btn;
  ConstraintLayout aniLayout;
  TextView first_text;
  TextView first_text2;
  ImageView score;

  private BookManager bookManager;
  private long milesson_id;
  private long item_id;
  private int mStep = 1;
  private int successNum = 0;

  private ArrayList<ReportVocabulary> reportVocabularies;
  private OssToken ossToken;
  String userid;
  private SoundPool soundPool;
  private int clickID;
  private int completedID;
  private String host;
  private String ossHost;
  private String workHost;
  private boolean isHomeworkSubmit = false;
  private boolean isHomeworkHaveNext = false;
  private boolean isHomeworkWatchHaveNext = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_result_pic);
    initView();
    if (getPackageName().equals("com.jinxin.appstudent")) {
      host = AppPicUtil.getPicBaseUrl(this);
      ossHost = AppPicUtil.getPicOssUrl(this);
      workHost = AppPicUtil.getPicWorkBaseUrl(this);
    } else {
      host = Utils.getBaseHttpsUrl(this);
      ossHost = Utils.getBaseHttpsUrl(this);
      workHost = Utils.getBaseHttpsUrl(this);
    }

    initSound();
    initActionBar();
    initData();

    BarUtil.transparentStatusBar(this);
    BarUtil.setStatusBarLightMode(this, true);
  }

  private void initView() {
    tv_timer = findViewById(R.id.tv_timer);
    image = findViewById(R.id.image);
    text1 = findViewById(R.id.text1);
    text2 = findViewById(R.id.text2);
    index1 = findViewById(R.id.index1);
    index2 = findViewById(R.id.index2);
    index3 = findViewById(R.id.index3);
    index4 = findViewById(R.id.index4);
    index_text1 = findViewById(R.id.index_text1);
    index_text2 = findViewById(R.id.index_text2);
    index_text3 = findViewById(R.id.index_text3);
    index_text4 = findViewById(R.id.index_text4);
    line1 = findViewById(R.id.line1);
    line2 = findViewById(R.id.line2);
    line3 = findViewById(R.id.line3);
    btn = findViewById(R.id.btn);
    aniLayout = findViewById(R.id.animationLayout);
    first_text = findViewById(R.id.first_text);
    first_text2 = findViewById(R.id.first_text2);
    score = findViewById(R.id.score);
    btn.setOnClickListener(v -> nextLink());
    aniLayout.setOnClickListener(v -> updateLatter());
    tv_timer.setOnClickListener(v -> aniLayout.performClick());
  }

  private void initData() {
    bookManager = BookManager.getInstance();
    //判断当前作业是否需要提交
    if (bookManager != null) {
      isHomeworkSubmit = bookManager.isHomeWork() && bookManager.getTypeList() != null
          && bookManager.getTypeList().size() == bookManager.getIndex() + 1;
      isHomeworkHaveNext = bookManager.isHomeWork() && bookManager.getTypeList() != null
          && bookManager.getTypeList().size() > bookManager.getIndex() + 1;
      isHomeworkWatchHaveNext = bookManager.isHomeWorkWatch() && bookManager.getTypeList() != null
          && bookManager.getTypeList().size() > bookManager.getIndex() + 1;
    }

    Intent intent = getIntent();
    String type = intent.getStringExtra(ARG_LINK);
    int index = bookManager.linkIndex(type);
    if (index == -1) {
      toast("学习环节配置错误");
      finish();
      return;
    }
    milesson_id = bookManager.getMilesson_id();
    List<ProductItem.BookLearning.Link> links = bookManager.getLinks();
    ProductItem.BookLearning.Link link = links.get(index);
    if (NetworkUtil.isNetworkConnected(this)) {
      PreferenceUtil.setSharePref(this, link.item_id + link.type, 1);
    }
    item_id = link.item_id;
    mStep = index + 1;
    initView(links);
    if (NetworkUtil.isNetworkConnected(this) && PreferenceUtil
        .getSharePref(this, BookManager.getInstance().getMilesson_item_id() + "progress", 0)
        <= mStep) {
      PreferenceUtil
          .setSharePref(this, BookManager.getInstance().getMilesson_item_id() + "progress", mStep);
    }
    if (PicturePreferenceUtil.getLongLoginUserId(this) == -1L) {
      AppPicUtil.saveLoaclReading(bookManager, this);
    }
  }

  @SuppressLint("NewApi")
  private void initSound() {
    soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    clickID = soundPool.load(this, R.raw.click_voice, 1);
    completedID = soundPool.load(this, R.raw.mission_complete, 1);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (soundPool != null) {
      soundPool.release();
    }
  }

  private void playSound(int id) {
    soundPool.play(
        id,
        1,      //左耳道音量【0~1】
        1,      //右耳道音量【0~1】
        0,         //播放优先级【0表示最低优先级】
        0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
        1          //播放速度【1是正常，范围从0~2】
    );
  }

  private void initView(List<ProductItem.BookLearning.Link> links) {
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        playSound(completedID);
      }
    }, 700);
    String type1 = links.get(0).type;
    String type2 = links.get(1).type;
    String type3 = links.get(2).type;
    index_text1.setText(linkConvert(type1));
    index_text2.setText(linkConvert(type2));
    index_text3.setText(linkConvert(type3));

    String type4 = "";
    if (links.size() == 4) {
      type4 = links.get(3).type;
      index_text4.setText(linkConvert(type4));
      index4.setVisibility(View.VISIBLE);
      line3.setVisibility(View.VISIBLE);
      index_text4.setVisibility(View.VISIBLE);
    }

    switch (mStep) {
      case STEP1:
        text1.setText(tipConvert(type1));
        btn.setText(btnConvert(type2));
        index1.setEnabled(true);
        index_text1.setEnabled(true);
        index2.setEnabled(false);
        index_text2.setEnabled(false);
        index3.setEnabled(false);
        index_text3.setEnabled(false);
        line1.setEnabled(false);
        line2.setEnabled(false);

        if (links.size() == 4) {
          line3.setEnabled(false);
          index_text4.setEnabled(false);
          index4.setEnabled(false);
        }
        syncUpData(mStep);
        if (bookManager.isHomeWorkWatch()) {
          btn.setText("下一步");
        }
        break;
      case STEP2:
        text1.setText(tipConvert(type2));
        btn.setText("就差一步完成学习");
        btn.setTag(type3);
        index1.setEnabled(true);
        index_text1.setEnabled(true);
        index2.setEnabled(true);
        index_text2.setEnabled(true);
        index3.setEnabled(false);
        index_text3.setEnabled(false);
        line1.setEnabled(true);
        line2.setEnabled(false);

        if (links.size() == 4) {
          line3.setEnabled(false);
          index_text4.setEnabled(false);
          index4.setEnabled(false);
          btn.setText(btnConvert(type3));
        }
        if (PicturePreferenceUtil.getLongLoginUserId(this) != -1L) {
          if (NetworkUtil.isNetworkConnected(this)) {
            getOsstoken();
          } else {
            showNoInternetDialogOSToken();
          }
        } else {
          btn.setEnabled(true);
        }
        if (bookManager.isHomeWorkWatch()) {
          btn.setText("下一步");
        }
        break;
      case STEP3:
        text1.setText("恭喜你完成了学习");
        showStudyTime(bookManager.getWordNumber());
        btn.setText("接下来开始挑战吧");
        index1.setEnabled(true);
        index_text1.setEnabled(true);
        index2.setEnabled(true);
        index_text2.setEnabled(true);
        index3.setEnabled(true);
        index_text3.setEnabled(true);
        line1.setEnabled(true);
        line2.setEnabled(true);
        syncUpData(mStep);

        if (isHomeworkSubmit) {
          btn.setText("提交作业");
        }

        if (bookManager.isHomeWorkWatch()) {
          if (isHomeworkWatchHaveNext) {
            btn.setText("下一步");
          } else {
            btn.setText("完成");
          }
        }

        if (links.size() == 4) {
          line3.setEnabled(false);
          index_text4.setEnabled(false);
          index4.setEnabled(false);
          btn.setText("就差一步完成学习");
          btn.setTag(type4);
          text1.setText("");
          text2.setText("");
        }
        break;
      case STEP4:
        text1.setText("恭喜你完成了学习");
        showStudyTime(bookManager.getWordNumber());
        btn.setText("接下来开始挑战吧");
        index1.setEnabled(true);
        index_text1.setEnabled(true);
        index2.setEnabled(true);
        index_text2.setEnabled(true);
        index3.setEnabled(true);
        index_text3.setEnabled(true);
        line1.setEnabled(true);
        line2.setEnabled(true);
        line3.setEnabled(true);
        index_text4.setEnabled(true);
        index4.setEnabled(true);
        syncUpData(mStep);

        if (isHomeworkSubmit) {
          btn.setText("提交作业");
        }

        if (bookManager.isHomeWorkWatch()) {
          if (isHomeworkWatchHaveNext) {
            btn.setText("下一步");
          } else {
            btn.setText("完成");
          }
        }
        break;
      default:
        break;
    }
    if (bookManager.isHomeWorkWatch()) {
      text1.setText("");
      text2.setText("");
    }
  }

  private void getOsstoken() {
    if (ossToken == null) {
      String url = ossHost + "/api/get_oss_token/";
      ApiHandler.getBaseApi()
          .OssTokenPost(url, getOsstokenBody())
          .subscribeOn(Schedulers.io())
          .flatMap(new Function<OssToken, Flowable<OssToken>>() {
            @Override
            public Flowable<OssToken> apply(OssToken ossToken) throws Exception {
              List<OssToken> list = new ArrayList<>();
              reportVocabularies = (ArrayList<ReportVocabulary>) getIntent()
                  .getSerializableExtra("reportList");
              for (ReportVocabulary reportVocabulary : reportVocabularies) {
                if (reportVocabulary.localpath != null) {
                  File file = new File(new URI(Uri.parse(reportVocabulary.localpath).toString()));
                  if (file.exists() && Utils.isLogin(ResultActivity.this)) {
                    OssToken ossToken1 = ossToken.clone();
                    ossToken1.objectKey += "/" + file.getName();
                    ossToken1.uploadFile = file;
                    reportVocabulary.mp3name = ossToken1.objectKey;
                    list.add(ossToken1);
                  }
                }
              }
              if (list.size() == 0) {
                return Flowable.empty();
              }
              return Flowable.fromIterable(list);
            }
          })
          .flatMap(new Function<OssToken, Publisher<OssEvent>>() {
            @Override
            public Publisher<OssEvent> apply(OssToken ossToken) throws Exception {
              return OssUploadUtil.getOssObservable(ResultActivity.this, ossToken);
            }
          })
          .flatMap(new Function<OssEvent, Flowable<OssEvent>>() {
            @Override
            public Flowable<OssEvent> apply(OssEvent ossEvent) throws Exception {
              if (ossEvent.type == OssEvent.OssEventType.RESULT) {
                return Flowable.just(ossEvent);
              } else {
                return Flowable.empty();
              }
            }
          })
          .toList()
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new SingleObserver<List<OssEvent>>() {
            @Override
            public void onSubscribe(Disposable d) {
              Logger.d("onSuccess");
            }

            @Override
            public void onSuccess(List<OssEvent> result) {
//                isUploading = false;
              syncUpData(mStep);
              Logger.d("onSuccess", result.size() + "");
//                toast(context, toast, "发送日志成功");
//                EventUtil.postEvent(new UploadLogUtil.LogEvent(result));
            }


            @Override
            public void onError(Throwable t) {
//                isUploading = false;
              Logger.e(t, "发送日志失败");
              showNoInternetDialogOSToken();
//                EventUtil.postEvent(new UploadLogUtil.LogEvent("发送日志失败"));
            }

          });
    }
  }

  private JsonObject getOsstokenBody() {
    userid = Utils.getLoginUserId(this);
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("item_id", item_id);
    if (getPackageName().equals("com.jinxin.appstudent")) {
      jsonObject.addProperty("objectKey",
          "pbook/userwork/userid/" + userid + "/milessionId/" + milesson_id + "/itemId/" + item_id
              + "/match_id/" + bookManager.getMatch_id());
    } else {
      jsonObject.addProperty("objectKey",
          "pbook/userwork/userid/" + userid + "/milessionId/" + milesson_id + "/itemId/" + item_id);
    }
    jsonObject.addProperty("bucket", "namibox");
    return jsonObject;
  }

  private void syncUpData(int type) {
    if (PicturePreferenceUtil.getLongLoginUserId(this) != -1L) {
      if (NetworkUtil.isNetworkConnected(this)) {
        syncUp(type);
      } else {
        showNoInternetDialog(type);
      }
    } else {
      btn.setEnabled(true);
    }
  }

  private void showNoInternetDialog(int type) {
    showDialog("提示", "您的网络状况较差，请检查网络连接。若选择退出，将无法保存答题成绩。", "退出", new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
      }
    }, "重新连接", new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        syncUpData(type);
      }
    });
  }

  private void showNoInternetDialogOSToken() {
    showDialog("提示", "您的网络状况较差，请检查网络连接。若选择退出，将无法保存答题成绩。", "退出", new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
      }
    }, "重新连接", new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getOsstoken();
      }
    });
  }

  private void syncUp(int type) {
    if (bookManager != null && bookManager.isHomeWorkWatch()) {
      btn.setEnabled(true);
      return;
    }

    String url = host + "/api/report_reading_progress";
    JsonObject jsonBody = getSyncBody(type);
    switch (type) {
      case STEP1:
        url = host + "/api/report_reading_progress";
        break;
      case STEP2:
        url = host + "/api/report_vocabulary_progress";
        break;
      case STEP3:
        url = host + "/api/report_video_progress";
        break;
      case STEP4:
        url = host + "/api/report_audio_progress";
        break;
    }

    Disposable disposable = ApiHandler.getBaseApi().commonJsonElementPost(url, jsonBody)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonElement -> {
          JsonObject jsonObject = jsonElement.getAsJsonObject();
          String retcode = jsonObject.get("retcode").getAsString();
          if (retcode != null && (retcode.equals("SUCC") || retcode.equals("success"))) {
            btn.setEnabled(true);
            if (jsonObject.has("data")) {
              JsonObject data = jsonObject.get("data").getAsJsonObject();
              int first_win_points = 0;
              if (data.get("first_win_points") != null) {
                first_win_points = data.get("first_win_points").getAsInt();
              }
              if (first_win_points != 0) {
                showAnimation();
//            EventBus.getDefault().post(new AnimationEvent());
              }
              String words = jsonObject.get("data").getAsJsonObject().get("count_exercise")
                  .getAsString();
              showStudyTime(words);
              first_text2.setText(Utils.format("+%d", first_win_points));
            }
//              EventBus.getDefault().post(new RefreshStoreInfo());
//              EventBus.getDefault().post(new RefreshGuideInfo());
          } else {
            showNoInternetDialog(type);
          }
        }, throwable -> {
          Logger.e(throwable, throwable.toString());
          showNoInternetDialog(type);
//          toast("上报数据失败");
        });
    compositeDisposable.add(disposable);
  }

  public void showStudyTime(String words) {
    long endTime = System.currentTimeMillis();
    long time = endTime - AppPicUtil.getStartTime();
    int minute = ((int) time) / 1000 / 60;
    text2.setText(Utils.format("耗时%d分钟完成，阅读了%s个单词", minute > 0 ? minute : 1, words));
  }

  private JsonObject getSyncBody(int type) {
    JsonObject jsonObject = new JsonObject();
    if (getPackageName().equals("com.jinxin.appstudent")) {
      jsonObject.addProperty("match_id", bookManager.getMatch_id());
    }
    switch (type) {
      case STEP1:
        jsonObject.addProperty("item_id", item_id);
        jsonObject.addProperty("milesson_id", milesson_id);
        return jsonObject;
      case STEP2:
        float scoreAVG = 0;
        JsonArray jsonArray = new JsonArray();
        for (ReportVocabulary item : reportVocabularies) {
          JsonObject json = new JsonObject();
          json.addProperty("exercise_id", item.exercise_id);
          json.addProperty("engine_used", item.engine_used);
          json.addProperty("fluency", item.fluency);
          json.addProperty("integrity", item.integrity);
          json.addProperty("text", item.text);
          json.addProperty("mp3name", item.mp3name);
          json.addProperty("pron", item.pron);
          json.addProperty("score", item.score);
          scoreAVG += item.score;
          jsonArray.add(json);
        }
        scoreAVG = scoreAVG / reportVocabularies.size();
        jsonObject.add("detail", jsonArray);
        jsonObject.addProperty("score_avg", scoreAVG);
        jsonObject.addProperty("item_id", item_id);
        jsonObject.addProperty("milesson_id", milesson_id);
        return jsonObject;
      case STEP3:
      case STEP4:
        jsonObject.addProperty("item_id", item_id);
        jsonObject.addProperty("milesson_id", milesson_id);
        break;
    }
    return jsonObject;
  }

  private String linkConvert(String type) {
    if (TextUtils.equals(type, PicLoadingActivity.BOOK_LINKS_READ)) {
      return "读绘本";
    } else if (TextUtils.equals(type, PicLoadingActivity.BOOK_LINKS_WORD)) {
      return "学词汇";
    } else if (TextUtils.equals(type, PicLoadingActivity.BOOK_LINKS_MUSIC)) {
      return "听音乐";
    } else {
      return "看动画";
    }
  }

  private String tipConvert(String type) {
    if (TextUtils.equals(type, PicLoadingActivity.BOOK_LINKS_READ)) {
      return "恭喜你完成了阅读";
    } else if (TextUtils.equals(type, PicLoadingActivity.BOOK_LINKS_WORD)) {
      return "恭喜你完成了词汇学习";
    } else if (TextUtils.equals(type, PicLoadingActivity.BOOK_LINKS_MUSIC)) {
      return "恭喜你完成了听音乐";
    } else {
      return "恭喜你完成了看动画";
    }
  }

  private String btnConvert(String type) {
    if (TextUtils.equals(type, PicLoadingActivity.BOOK_LINKS_READ)) {
      return "开始阅读";
    } else if (TextUtils.equals(type, PicLoadingActivity.BOOK_LINKS_WORD)) {
      return "开始词汇学习";
    } else {
      return "开始看动画";
    }
  }

  public void nextLink() {
    playSound(clickID);
    String text = btn.getText().toString();
    if (!TextUtils.isEmpty(text)) {
      Intent intent = null;
      switch (text) {
        case "开始阅读":
          intent = new Intent(this, ReadBookActivity.class);
          break;
        case "开始词汇学习":
          intent = new Intent(this, VocabularyActivity.class);
          break;
        case "开始看动画":
          intent = new Intent(this, VideoActivity.class);
          break;
        case "就差一步完成学习":
          String type = (String) btn.getTag();
          if (TextUtils.equals(type, PicLoadingActivity.BOOK_LINKS_READ)) {
            intent = new Intent(this, ReadBookActivity.class);
          } else if (TextUtils.equals(type, PicLoadingActivity.BOOK_LINKS_WORD)) {
            intent = new Intent(this, VocabularyActivity.class);
          } else if (TextUtils.equals(type, PicLoadingActivity.BOOK_LINKS_VIDEO)) {
            if (NetworkUtil.isNetworkConnected(this)) {
              intent = new Intent(this, VideoActivity.class);
            } else {
              toast("您的网络状况较差，请检查网络连接。");
              return;
            }
          } else if (TextUtils.equals(type, PicLoadingActivity.BOOK_LINKS_MUSIC)) {
            intent = new Intent(this, MusicPicActivity.class);
          }
          break;
        case "接下来开始挑战吧":
          gonextExercise();
          break;
        case "提交作业":
          if (NetworkUtil.isNetworkConnected(this)) {
            //TODO 提交作业
            submitWork();
          } else {
            toast("您的网络状况较差，请检查网络连接。");
          }
          break;
        case "下一步":
          if (mStep == 1) {
            intent = new Intent(this, VocabularyActivity.class);
          } else if (mStep == 2) {
            intent = new Intent(this, VideoActivity.class);
          } else if (mStep == 3) {
            gonextExercise();
          }
          break;
        case "完成":
          finish();
          break;
        default:
          break;
      }
      if (intent != null) {
        startActivity(intent);
        finish();
      }
    }
  }

  public void gonextExercise() {
    if (NetworkUtil.isNetworkConnected(this)) {
      if (isHomeworkHaveNext) {
        bookManager.setIndex(bookManager.getIndex() + 1);
        finish();
        AppPicUtil.jumpChallenge(bookManager.getIndex(), this);
        return;
      }

      if (isHomeworkWatchHaveNext) {
        bookManager.setIndex(bookManager.getIndex() + 1);
        finish();
        AppPicUtil.jumpChallenge(bookManager.getIndex(), this);
        return;
      }

      ProductItem.Challenge challenge = bookManager.getWordChallenge();
      if (challenge != null) {
        Intent intent = new Intent(this, ExerciseChallengeActivity.class);
        intent.putExtra("exercise_type", CHALLENGE_WORD);
        startActivity(intent);
        finish();
      }
    } else {
      toast("您的网络状况较差，请检查网络连接。");
    }
  }

  public void submitWork() {
    String url = workHost + "/homework/submit/pbook/work/";
    JsonObject jsonBody = new JsonObject();
    jsonBody.addProperty("homework_id", bookManager.getHomeworkId());

    Disposable disposable = ApiHandler.getBaseApi().commonJsonPost(url, jsonBody)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonElement -> {
          JsonObject jsonObject = jsonElement.getAsJsonObject();
          String retcode = jsonObject.get("retcode").getAsString();
          JsonObject data = jsonObject.get("data").getAsJsonObject();
          if (retcode != null && (retcode.equals("SUCC") || retcode.equals("success"))) {
            finish();
            AppPicUtil.gotoWorkResult(data.toString(), bookManager.getHomeworkId());
          } else {
//            showNoInternetDialog();
          }
        }, throwable -> {
          Logger.e(throwable, throwable.toString());
//          showNoInternetDialog();
        });
    compositeDisposable.add(disposable);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    EventBus.getDefault().post(new RefreshStoreInfo());
//    EventBus.getDefault().post(new RefreshGuideInfo());
  }

  boolean isHideBaoxiang = false;
  boolean baoxiangCanClick = false;

  private void showAnimation() {
    isHideBaoxiang = false;
    aniLayout.setVisibility(View.VISIBLE);
    LottieAnimationView loadingAnimView = findViewById(R.id.loading_anim);
    loadingAnimView.setVisibility(View.VISIBLE);
    loadingAnimView.enableMergePathsForKitKatAndAbove(true);
    loadingAnimView.playAnimation();
    loadingAnimView.addAnimatorListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        animTimer();
        baoxiangCanClick = true;
        first_text.setVisibility(View.VISIBLE);
      }
    });

    loadingAnimView.setOnClickListener(view -> {
      if (!baoxiangCanClick) {
        return;
      }
      isHideBaoxiang = true;
      if (timerDis != null && !timerDis.isDisposed()) {
        timerDis.dispose();
      }
      first_text.setVisibility(View.VISIBLE);
      aniLayout.setVisibility(View.GONE);
    });
  }

  Disposable timerDis;
  private void animTimer(){
    tv_timer.setVisibility(View.VISIBLE);

    Observable.interval(1, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Long>() {
          @Override
          public void onSubscribe(@NonNull Disposable disposable) {
            if (timerDis != null && !timerDis.isDisposed()) {
              timerDis.dispose();
            }
            timerDis = disposable;
          }

          @Override
          public void onNext(@NonNull Long s) {
            tv_timer.setText((2-s)+"s");
            if (s == 2) {
              aniLayout.setVisibility(View.GONE);
              tv_timer.setVisibility(View.GONE);
              if (timerDis != null && !timerDis.isDisposed()) {
                timerDis.dispose();
              }
            }
          }

          @Override
          public void onError(@NonNull Throwable e) {

          }

          @Override
          public void onComplete() {

          }
        });
  }

  public void updateLatter() {
    if (!baoxiangCanClick) {
      return;
    }
    if (isHideBaoxiang) {
      aniLayout.setVisibility(View.GONE);
    }
  }
}
