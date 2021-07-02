package com.example.picsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.picsdk.base.BaseActivity;
import com.example.picsdk.event.RefreshStoreInfo;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.model.Exercise;
import com.example.picsdk.model.ExerciseChallenge;
import com.example.picsdk.model.ProductItem;
import com.example.picsdk.model.WatchPic;
import com.example.picsdk.util.AppPicUtil;
import com.example.picsdk.util.BarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.tools.LoggerUtil;
import com.namibox.tools.RxTimerUtil;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ExerciseChallengeActivity extends BaseActivity {

  private ProgressBar mProgressBar;
  private ExerciseChallenge mWordChallenge;
  private long mEnterExerciseChallengeTime;
  private boolean mIsExercising;
  private Exercise mCurrentExercise;

  public String mExerciseType;
  private BookManager bookManager;
  public long milesson_id;
  public long milesson_item_id;
  public ConstraintLayout guideLayout;
  private String host;
  private String ossHost;
  private WatchPic mWatchPic;

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (TextUtils.equals(mExerciseType, AppPicUtil.CHALLENGE_WORD)) {
      if (bookManager != null && bookManager.getBookLearning() != null)
        AppPicUtil.TagEventEnterPush(false, ((ProductItem.BookLearning) bookManager.getBookLearning()).text, "词汇挑战", bookManager.getProductName());
    } else {
      if (bookManager != null && bookManager.getBookLearning() != null)
        AppPicUtil.TagEventEnterPush(false, ((ProductItem.BookLearning) bookManager.getBookLearning()).text, "阅读理解", bookManager.getProductName());
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_exercise_challengs);

    if (getPackageName().equals("com.jinxin.appstudent")) {
      host = AppPicUtil.getPicBaseUrl(this);
      ossHost = AppPicUtil.getPicOssUrl(this);
    } else {
      host = Utils.getBaseHttpsUrl(this);
      ossHost = Utils.getBaseHttpsUrl(this);
    }

    BarUtil.transparentStatusBar(this);
    BarUtil.setStatusBarLightMode(this, true);

    initView();
    initData();
    if (TextUtils.equals(mExerciseType, AppPicUtil.CHALLENGE_WORD)) {
      if (bookManager != null && bookManager.getBookLearning() != null)
        AppPicUtil.TagEventEnterPush(true, ((ProductItem.BookLearning) bookManager.getBookLearning()).text, "词汇挑战", bookManager.getProductName());
    } else {
      if (bookManager != null && bookManager.getBookLearning() != null)
        AppPicUtil.TagEventEnterPush(true, ((ProductItem.BookLearning) bookManager.getBookLearning()).text, "阅读理解", bookManager.getProductName());
    }
  }

  private void initView() {
    initActionBar();
    mProgressBar = findViewById(R.id.progress);
    guideLayout = findViewById(R.id.guide_layout);
    guideLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        guideLayout.setVisibility(View.GONE);
        PreferenceUtil.setSharePref(ExerciseChallengeActivity.this, "guide_show_study", false);
      }
    });
  }

  public ExerciseChallenge getData() {
    return mWordChallenge;
  }

  private void initData() {
    bookManager = BookManager.getInstance();
    mExerciseType = getIntent().getStringExtra("exercise_type");
    ProductItem.Challenge challenge;
    if (TextUtils.equals(mExerciseType, AppPicUtil.CHALLENGE_WORD)) {
      challenge = bookManager.getWordChallenge();
    } else {
      challenge = bookManager.getReadChallenge();
    }
    if (challenge == null) {
      return;
    }
    milesson_id = bookManager.getMilesson_id();
    milesson_item_id = challenge.milesson_item_id;
    setActionTitle(challenge.challenge_name);
    ExerciseChallengeWelcomeFragment fragment = new ExerciseChallengeWelcomeFragment();
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ft.replace(R.id.frame, fragment);
    ft.commit();
    new RxTimerUtil().timer(3000, new RxTimerUtil.IRxNext() {
      @Override
      public void onTick(Long s) {

      }

      @Override
      public void onFinish() {
        if (!isFinishing()) {
          request();
          if (bookManager != null && bookManager.isHomeWorkWatch()) {
            getWatchDetail();
          }
        }
      }
    });
  }

  public void getWatchDetail() {
    String type = bookManager.getTypeList().get(bookManager.getIndex()).text;
    int id = bookManager.getTypeList().get(bookManager.getIndex()).id;
    String url = host + "/api/user/pbook/" + id + "/data?stu_hw_id=" + bookManager.getStu_hw_id();
    Disposable disposable = ApiHandler.getBaseApi().commonJsonGet(url)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          String retcode = jsonObject.get("retcode").getAsString();
          if (retcode != null && retcode.equals("success")) {
            JsonObject data = jsonObject.get("data").getAsJsonObject();
            mWatchPic = new Gson().fromJson(data, WatchPic.class);
          }
        }, throwable -> {
          Logger.e(throwable, throwable.toString());
          toast("数据获取失败");
        });
    compositeDisposable.add(disposable);
  }

  public WatchPic getWatchPic() {
    return mWatchPic;
  }

  public void request() {
    String url = getIntent().getStringExtra("url");
    if (TextUtils.isEmpty(url)) {
      if (TextUtils.equals(mExerciseType, AppPicUtil.CHALLENGE_WORD)) {
        url = host + "/api/wordchallenge" + "/" + milesson_item_id;
      } else {
        url = host + "/api/readingunderstand" + "/" + milesson_item_id;
      }
    }
    LoggerUtil.d("url: " + url);
    Disposable disposable = ApiHandler.getBaseApi().commonJsonGet(url)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          ExerciseChallenge wordChallenge = new Gson().fromJson(jsonObject, ExerciseChallenge.class);
          mWordChallenge = wordChallenge;
          if (TextUtils.equals(wordChallenge.retcode, "SUCC") || TextUtils.equals(wordChallenge.retcode, "success")) {
            //TODO 隐藏对话框
            //预加载音频资源
            for (Exercise exercise : wordChallenge.data.exercises) {
              if (TextUtils.equals("语音", exercise.question.attach_type)) {
                File file = FileUtil.getCachedFile(getApplicationContext(), exercise.question.attach);
                if (!file.exists()) {
                  Logger.d("downloadFile:" + exercise.question.attach);
                  NetworkUtil.downloadFile(getOkHttpClient(), exercise.question.attach, file);
                }
              }
            }
            enterChallenge();
          } else {
            toast(wordChallenge.description);
          }
        }, throwable -> {
          Logger.e(throwable, throwable.toString());
          toast("数据获取失败");
        });
    compositeDisposable.add(disposable);
  }

  public void enterNextChallenge() {
    List<ProductItem.Challenge> challenges = bookManager.getChallenges();
    if (challenges != null && !challenges.isEmpty()) {
      String nextChallenge = null;
      switch (mExerciseType) {
        case AppPicUtil.CHALLENGE_WORD:
          nextChallenge = AppPicUtil.CHALLENGE_READ;
          break;
        case AppPicUtil.CHALLENGE_READ:
          nextChallenge = bookManager.getChallenges().get(bookManager.getChallenges().size() - 1).task_type;
          break;
        default:
          break;
      }
      for (ProductItem.Challenge challenge : challenges) {
        if (TextUtils.equals(nextChallenge, challenge.task_type)) {
          if (TextUtils.equals(nextChallenge, AppPicUtil.CHALLENGE_PLAY)) {
            AppPicUtil.gotoPlayChallenge(challenge.url, bookManager, this);
          } else if (TextUtils.equals(nextChallenge, AppPicUtil.CHALLENGE_PIC)) {
            AppPicUtil.gotoPicChallenge(challenge, this, bookManager);
          } else {
            gotoReadChallenge(nextChallenge);
          }
          break;
        }
      }
    }
    finish();
  }

  public void enterNextChallenge2() {
    AppPicUtil.jumpChallenge(bookManager.getIndex(), this);
    finish();
  }

  private void gotoReadChallenge(String exerciseType) {
    Intent intent = new Intent(this, ExerciseChallengeActivity.class);
    intent.putExtra("exercise_type", exerciseType);
    startActivity(intent);
  }

  public void enterChallenge() {
    mIsExercising = true;
    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) findViewById(R.id.frame)
        .getLayoutParams();
    layoutParams.setMargins(layoutParams.leftMargin, Utils.dp2px(this, 44), layoutParams.rightMargin,
        layoutParams.bottomMargin);
    mEnterExerciseChallengeTime = System.currentTimeMillis();
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ExerciseChallengeFragment wordChallengeFragment = new ExerciseChallengeFragment();
    wordChallengeFragment.setAnswerProgressListener(
        position -> mProgressBar.setProgress(position * 100 / mWordChallenge.data.exercises.size()));
    ft.replace(R.id.frame, wordChallengeFragment);
    ft.commitAllowingStateLoss();
  }

  public String getExerciseType() {
    return mExerciseType;
  }

  public void enterChallengeResult() {
    mIsExercising = false;
    if (TextUtils.equals(mExerciseType, AppPicUtil.CHALLENGE_READ)) {
      setRightText("");
    }
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ft.replace(R.id.frame, ExerciseChallengeResultFragment.newInstance());
    ft.commitAllowingStateLoss();
  }

  @Override
  public void onBackPressed() {
    if (mIsExercising) {
      if (bookManager != null && bookManager.isHomeWorkWatch()) {
        super.onBackPressed();
        return;
      }
      showDialog("温馨提示", "退出后再次进入需要重新开始挑战", "退出", v -> {
        EventBus.getDefault().post(new RefreshStoreInfo());
//        EventBus.getDefault().post(new RefreshGuideInfo());
        super.onBackPressed();
      }, "取消", null);
    } else {
      EventBus.getDefault().post(new RefreshStoreInfo());
//      EventBus.getDefault().post(new RefreshGuideInfo());
      super.onBackPressed();
    }
  }

  @Override
  public void onRightTextClick() {
    Intent intent = new Intent(this, ReadBookActivity.class);
    intent.putStringArrayListExtra("cartoon", mCurrentExercise.cartoon);
    intent.putExtra("milesson_item_id", getIntent().getLongExtra("milesson_item_id", 0));
    startActivity(intent);
  }

  public String getAnswerTime() {
    long delta = System.currentTimeMillis() - mEnterExerciseChallengeTime;
    if (delta > mWordChallenge.data.duration * 1000) {
      return mWordChallenge.data.duration + ".00";
    }
    return delta / 1000 + "." + delta % 1000 / 10;
  }

  public void setCurrentExercise(Exercise exercise) {
    if (TextUtils.equals(mExerciseType, AppPicUtil.CHALLENGE_READ)) {
      mCurrentExercise = exercise;
      if (!exercise.cartoon.isEmpty()) {
        setRightText("查询原文");
        if (PreferenceUtil.getSharePref(this, "guide_show_study", true)) {
          guideLayout.setVisibility(View.VISIBLE);
        }
      } else {
        setRightText("");
      }
    }
  }
}

