package com.example.picsdk;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.example.picsdk.base.BaseFragment;
import com.example.picsdk.event.RefreshStoreInfo;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.model.Exercise;
import com.example.picsdk.model.ExerciseChallenge;
import com.example.picsdk.util.AppPicUtil;
import com.example.picsdk.util.PicturePreferenceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseChallengeResultFragment extends BaseFragment {


  private static final String TAG = "ExerciseChallengeResultFragment";
  private ExerciseChallengeActivity mActivity;
  private ConstraintLayout aniLayout;
  private TextView first_text2, tv_again, tv_more_effort;
  private TextView first_text, tv_timer;
  private ImageView score, iv_total;
  private TextView tv_total;
  private ImageView best_icon;
  private LottieAnimationView loadingAnimView;
  private SoundPool soundPool;
  private int clickID;
  private int winID;
  private int failedID;
  private int bestId;
  private boolean isHomeworkSubmit = false;
  private boolean isHomeworkHaveNext = false;
  private boolean isHomeworkWatchHaveNext = false;
  private BookManager bookManager;
  private String host;
  private String workHost;
  private ExerciseChallenge.RatingRule ratingRule;
  private TextView tvAgain;
  private TextView tvContinue;
  private ImageView ivChallengeResult;
  private View rootView;
  private int starCount;

  public ExerciseChallengeResultFragment() {
    // Required empty public constructor
  }

  public static ExerciseChallengeResultFragment newInstance() {
    ExerciseChallengeResultFragment fragment = new ExerciseChallengeResultFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivity = (ExerciseChallengeActivity) activity;
    if (mActivity != null) {
      mActivity.setActionTitle("");
    }
    initSound();

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
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_exercise_challenge_result, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (getActivity().getPackageName().equals("com.jinxin.appstudent")) {
      host = AppPicUtil.getPicBaseUrl(getActivity());
      workHost = AppPicUtil.getPicWorkBaseUrl(activity);
    } else {
      host = Utils.getBaseHttpsUrl(getActivity());
      workHost = Utils.getBaseHttpsUrl(activity);
    }
    initView(view);
  }

  @SuppressLint("NewApi")
  private void initSound() {
    soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    clickID = soundPool.load(getActivity(), R.raw.click_voice, 1);
    winID = soundPool.load(getActivity(), R.raw.challenge_win, 1);
    failedID = soundPool.load(getActivity(), R.raw.challenge_lose, 1);
    bestId = soundPool.load(getActivity(), R.raw.gaizhang, 1);
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

  private void initView(View view) {
    rootView = view;
    ivChallengeResult = view.findViewById(R.id.iv_challenge_result);
    TextView tvCorrectCount = view.findViewById(R.id.tv_correct_count);
    TextView tvAnswerTime = view.findViewById(R.id.tv_answer_time);
    aniLayout = view.findViewById(R.id.animationLayout);
    first_text2 = view.findViewById(R.id.first_text2);
    tv_total = view.findViewById(R.id.tv_total);
    iv_total = view.findViewById(R.id.iv_total);
    first_text = view.findViewById(R.id.first_text);
    score = view.findViewById(R.id.score);
    loadingAnimView = view.findViewById(R.id.loading_anim);
    best_icon = view.findViewById(R.id.best_icon);
    tv_again = view.findViewById(R.id.tv_again);
    tv_more_effort = view.findViewById(R.id.tv_more_effort);
    tv_timer = view.findViewById(R.id.tv_timer);

    tv_timer.setOnClickListener(v -> aniLayout.performClick());
    aniLayout.setOnClickListener(view1 -> {
      if (!baoxiangCanClick) {
        return;
      }
      if (timerDis != null && !timerDis.isDisposed()) {
        timerDis.dispose();
      }
      if (isHideBaoxiang) {
        aniLayout.setVisibility(View.GONE);
      }
    });
    int correctCount = 0;
    List<Exercise> exerciseList = mActivity.getData().data.exercises;
    for (Exercise exercise : exerciseList) {
      if (exercise.answerCorrect) {
        correctCount++;
      }
    }
    tvCorrectCount.setText(correctCount + "/" + exerciseList.size());
    tvAnswerTime.setText(mActivity.getAnswerTime() + "秒");
    ratingRule = mActivity.getData().data.rating_rule;
    int score = 100 * correctCount / mActivity.getData().data.exercises.size();
    tvAgain = view.findViewById(R.id.tv_again);
    tvContinue = view.findViewById(R.id.tv_continue);
    tvAgain.setOnClickListener(v -> {
      playSound(clickID);
      if (NetworkUtil.isNetworkConnected(getActivity())) {
        for (Exercise exercise : exerciseList) {
          exercise.answerCorrect = false;
        }
        mActivity.request();
      } else {
        toast(getString(R.string.common_check_network_tips));
      }
    });

    if (isHomeworkSubmit) {
      tvContinue.setText("提交作业");
    }

    tvContinue.setOnClickListener(v -> {
      playSound(clickID);
      if (NetworkUtil.isNetworkConnected(getActivity())) {
        if (isHomeworkSubmit) {
          submitWork();
          return;
        }

        if (isHomeworkHaveNext) {
          bookManager.setIndex(bookManager.getIndex() + 1);
          mActivity.enterNextChallenge2();
          return;
        }

        if (bookManager != null && bookManager.isHomeWorkWatch()) {
          if (isHomeworkWatchHaveNext) {
            bookManager.setIndex(bookManager.getIndex() + 1);
            mActivity.enterNextChallenge2();
          } else {
            getActivity().finish();
          }
          return;
        }

        mActivity.enterNextChallenge();
      } else {
        toast(getString(R.string.common_check_network_tips));
      }
    });
    saveData(score);
//    if (getActivity().getPackageName().equals("com.jinxin.appstudent")) {
//      tv_total.setVisibility(View.GONE);
//      iv_total.setVisibility(View.GONE);
//      if (bookManager != null && bookManager.isHomeWorkWatch()) {
//        tvAgain.setVisibility(View.GONE);
//        if (isHomeworkWatchHaveNext)
//          tvContinue.setText("继续");
//        else
//          tvContinue.setText("完成");
//
//        WatchPic watchPic = ((ExerciseChallengeActivity) activity).getWatchPic();
//        if (watchPic != null) {
//          tvCorrectCount.setText(watchPic.exercise_correct + "/" + watchPic.exercise_total);
//          tvAnswerTime.setText(watchPic.duration + "秒");
//
//          if (watchPic.exercise_total != 0) {
//            int scoreWatch = 100 * watchPic.exercise_correct / watchPic.exercise_total;
//            initScore(scoreWatch, view);
//          }
//
//          if (watchPic.first_win_points != 0) {
//            showAnimation();
//          }
//          tv_total.setText(watchPic.total_points + "");
//          first_text2.setText(Utils.format("+%d", watchPic.first_win_points));
//          if (watchPic.is_history_best) {
//            best_icon.setVisibility(View.VISIBLE);
//            new Handler().postDelayed(new Runnable() {
//              @Override
//              public void run() {
//                playSound(bestId);
//              }
//            }, 700);
//          } else {
//            best_icon.setVisibility(View.GONE);
//          }
//        }
//        return;
//      }
//    }

    initScore(score, view);
    syncUpData();

    if (PicturePreferenceUtil.getLongLoginUserId(getActivity()) == -1L) {
      AppPicUtil.saveLoaclReading(BookManager.getInstance(), getActivity());
    }
  }

  private void saveData(int score){
    if (PicturePreferenceUtil.getLongLoginUserId(getActivity()) != -1L) {
      Logger.d(TAG, "已登录，不保存数据到本地");
      return;
    }
    if (PicturePreferenceUtil.getLongLoginUserId(getActivity()) == -1L && PreferenceUtil
        .getSharePref(getContext(), BookManager.getInstance().getMilesson_item_id() + mActivity.mExerciseType + "star", 0) < 0)
      PreferenceUtil.setSharePref(getContext(), BookManager.getInstance().getMilesson_item_id() + mActivity.mExerciseType + "star", 0);
    if (score >= ratingRule.star.first_star.min) {
      if (PicturePreferenceUtil.getLongLoginUserId(getActivity()) == -1L && PreferenceUtil.getSharePref(getContext(), BookManager.getInstance().getMilesson_item_id() + mActivity.mExerciseType + "star", 0) < 1)
        PreferenceUtil.setSharePref(getContext(), BookManager.getInstance().getMilesson_item_id() + mActivity.mExerciseType + "star", 1);
    }
    if (score >= ratingRule.star.second_star.min) {
      if (PicturePreferenceUtil.getLongLoginUserId(getActivity()) == -1L && PreferenceUtil.getSharePref(getContext(), BookManager.getInstance().getMilesson_item_id() + mActivity.mExerciseType + "star", 0) < 2)
        PreferenceUtil.setSharePref(getContext(), BookManager.getInstance().getMilesson_item_id() + mActivity.mExerciseType + "star", 2);
    }
    if (score >= ratingRule.star.third_star.min) {
      if (PicturePreferenceUtil.getLongLoginUserId(getActivity()) == -1L && PreferenceUtil.getSharePref(getContext(), BookManager.getInstance().getMilesson_item_id() + mActivity.mExerciseType + "star", 0) < 3)
        PreferenceUtil.setSharePref(getContext(), BookManager.getInstance().getMilesson_item_id() + mActivity.mExerciseType + "star", 3);
    }
  }

  public void initScore(int scores, View view) {
    starCount = 0;
    if (scores >= ratingRule.star.first_star.min) {
      tvContinue.setVisibility(View.VISIBLE);
      tv_more_effort.setVisibility(View.GONE);
      view.findViewById(R.id.iv_star_1).setVisibility(View.VISIBLE);
      ivChallengeResult.setImageResource(R.drawable.ic_challenge_tryhard);
      starCount = 1;
    }
    if (scores >= ratingRule.star.second_star.min) {
      view.findViewById(R.id.iv_star_2).setVisibility(View.VISIBLE);
      tv_more_effort.setVisibility(View.GONE);
      ivChallengeResult.setImageResource(R.drawable.ic_challenge_good);
      starCount = 2;
    }
    if (scores >= ratingRule.star.third_star.min) {
      view.findViewById(R.id.iv_star_3).setVisibility(View.VISIBLE);
      tv_more_effort.setVisibility(View.GONE);
      ivChallengeResult.setImageResource(R.drawable.ic_challenge_excellent);
      tv_again.setVisibility(View.GONE);
      starCount = 3;
    }

    if (scores < ratingRule.star.first_star.min) {
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          playSound(failedID);
        }
      }, 1000);
    } else {
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          playSound(winID);
        }
      }, 1000);
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
            getActivity().finish();
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

  private void syncUpData() {
    if (NetworkUtil.isNetworkConnected(getActivity())) {
      syncUp();
    } else {
      showNoInternetDialog();
    }
  }

  private void showNoInternetDialog() {
    String msg = getString(R.string.book_dubnonereturnalert_title);
    mActivity.showDialog("提示", msg,
        "退出", view -> getActivity().finish(), getString(R.string.common_network_reconnect),
        v -> syncUpData());
  }

  private void syncUp() {
    if (!Utils.isLogin(getActivity())) {
      Logger.d(TAG, "未登录，不上报数据");
      return;
    }
    String url = host + "/api/report_word_progress";
    JsonObject jsonBody = getSyncBody();
    if (TextUtils.equals(mActivity.mExerciseType, AppPicUtil.CHALLENGE_WORD)) {
      url = host + "/api/report_word_progress";
    } else if (TextUtils.equals(mActivity.mExerciseType, AppPicUtil.CHALLENGE_READ)) {
      url = host + "/api/report_readingunderstand_progress";
    } else if (TextUtils.equals(mActivity.mExerciseType, AppPicUtil.CHALLENGE_PLAY)) {
      url = host + "/api/report_fundubbing_progress";
    }

//    switch (mActivity.mExerciseType) {
//      case AppPicUtil.CHALLENGE_WORD:
//        url = host + "/api/report_word_progress";
//        break;
//      case AppPicUtil.CHALLENGE_READ:
//        url = host + "/api/report_readingunderstand_progress";
//        break;
//      case AppPicUtil.CHALLENGE_PLAY:
//        url = host + "/api/report_fundubbing_progress";
//        break;
//    }

    Disposable disposable = ApiHandler.getBaseApi().commonJsonElementPost(url, jsonBody)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonElement -> {
          JsonObject jsonObject = jsonElement.getAsJsonObject();
          String retcode = jsonObject.get("retcode").getAsString();
          if (retcode != null && retcode.equals("SUCC") || retcode.equals("success")) {
            if (jsonObject.has("data")) {
              Logger.d(TAG, "挑战上报数据结果：" + jsonObject);
              int first_win_points = jsonObject.get("data").getAsJsonObject()
                  .get("first_win_points").getAsInt();
              if (first_win_points != 0) {
                showAnimation();
              }
              int total_score = jsonObject.get("data").getAsJsonObject().get("total_points")
                  .getAsInt();
              tv_total.setText(total_score + "");
//              first_text2.setText(Utils.format("+%d", first_win_points));
              boolean isHistoryBest = jsonObject.get("data").getAsJsonObject()
                  .get("is_history_best").getAsBoolean();
              if (isHistoryBest) {
                best_icon.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> playSound(bestId), 700);
              } else {
                best_icon.setVisibility(View.GONE);
              }
            }
          }
          EventBus.getDefault().post(new RefreshStoreInfo());
//          EventBus.getDefault().post(new RefreshGuideInfo());
        }, throwable -> {
          Logger.e("syncUp", "上报数据报错：" + throwable.toString());
//          toast("上报数据失败");
        });
    compositeDisposable.add(disposable);
  }

  private JsonObject getSyncBody() {
    JsonObject jsonObject = new JsonObject();
    if (getActivity().getPackageName().equals("com.jinxin.appstudent")) {
      jsonObject.addProperty("match_id", bookManager.getMatch_id());
    }

    if (TextUtils.equals(mActivity.mExerciseType, AppPicUtil.CHALLENGE_WORD)) {
      jsonObject.addProperty("item_id", mActivity.getData().data.item_id);
      jsonObject.addProperty("milesson_id", mActivity.milesson_id);
      jsonObject.addProperty("duration", mActivity.getData().data.duration);

      JsonArray jsonArray = new JsonArray();
      for (Exercise exercise : mActivity.getData().data.exercises) {
        JsonObject json = new JsonObject();
        json.addProperty("is_correct", exercise.answerCorrect);
        json.addProperty("exercise_id", exercise.id);
        json.addProperty("user_answer", exercise.user_answer);
        jsonArray.add(json);
      }
      jsonObject.add("exercises", jsonArray);
      jsonObject.addProperty("eid", mActivity.getData().data.eid);
      return jsonObject;
    } else if (TextUtils.equals(mActivity.mExerciseType, AppPicUtil.CHALLENGE_READ)) {
      jsonObject.addProperty("item_id", mActivity.getData().data.item_id);
      jsonObject.addProperty("milesson_id", mActivity.milesson_id);
      jsonObject.addProperty("duration", mActivity.getData().data.duration);

      JsonArray jsonArray1 = new JsonArray();
      for (Exercise exercise : mActivity.getData().data.exercises) {
        JsonObject json = new JsonObject();
        json.addProperty("is_correct", exercise.answerCorrect);
        json.addProperty("exercise_id", exercise.id);

        switch (exercise.type) {
          case "选择题":
            json.addProperty("user_answer", exercise.user_answer);
            break;
          case "词汇挑战":
            json.addProperty("user_answer", exercise.user_answer);
            break;
          case "朗读题":
            break;
          case "填空题":
            break;
          case "排序题":
            JsonArray jsonArray2 = new JsonArray();
            for (String sequence : exercise.userAnsweList) {
              jsonArray2.add(sequence);
            }
            json.add("user_answer", jsonArray2);
            break;
          default:
            break;
        }
        jsonArray1.add(json);
      }
      jsonObject.add("exercises", jsonArray1);
      jsonObject.addProperty("eid", mActivity.getData().data.eid);
      return jsonObject;
    }

//    switch (mActivity.mExerciseType) {
//      case AppPicUtil.CHALLENGE_WORD:
//        jsonObject.addProperty("item_id", mActivity.getData().data.item_id);
//        jsonObject.addProperty("milesson_id", mActivity.milesson_id);
//        jsonObject.addProperty("duration", mActivity.getData().data.duration);
//
//        JsonArray jsonArray = new JsonArray();
//        for (Exercise exercise : mActivity.getData().data.exercises) {
//          JsonObject json = new JsonObject();
//          json.addProperty("is_correct", exercise.answerCorrect);
//          json.addProperty("exercise_id", exercise.id);
//          json.addProperty("user_answer", exercise.user_answer);
//          jsonArray.add(json);
//        }
//        jsonObject.add("exercises", jsonArray);
//        jsonObject.addProperty("eid", mActivity.getData().data.eid);
//        return jsonObject;
//      case AppPicUtil.CHALLENGE_READ:
//        jsonObject.addProperty("item_id", mActivity.getData().data.item_id);
//        jsonObject.addProperty("milesson_id", mActivity.milesson_id);
//        jsonObject.addProperty("duration", mActivity.getData().data.duration);
//
//        JsonArray jsonArray1 = new JsonArray();
//        for (Exercise exercise : mActivity.getData().data.exercises) {
//          JsonObject json = new JsonObject();
//          json.addProperty("is_correct", exercise.answerCorrect);
//          json.addProperty("exercise_id", exercise.id);
//
//          switch (exercise.type) {
//            case "选择题":
//              json.addProperty("user_answer", exercise.user_answer);
//              break;
//            case "词汇挑战":
//              json.addProperty("user_answer", exercise.user_answer);
//              break;
//            case "朗读题":
//              break;
//            case "填空题":
//              break;
//            case "排序题":
//              JsonArray jsonArray2 = new JsonArray();
//              for (String sequence : exercise.userAnsweList) {
//                jsonArray2.add(sequence);
//              }
//              json.add("user_answer", jsonArray2);
//              break;
//            default:
//              break;
//          }
//          jsonArray1.add(json);
//        }
//        jsonObject.add("exercises", jsonArray1);
//        jsonObject.addProperty("eid", mActivity.getData().data.eid);
//        return jsonObject;
//      case AppPicUtil.CHALLENGE_PLAY:
//        break;
//    }
    return jsonObject;
  }

  boolean isHideBaoxiang = false;
  boolean baoxiangCanClick = false;

  private void showAnimation() {
    aniLayout.setVisibility(View.VISIBLE);
    if (!isHideBaoxiang) {
      isHideBaoxiang = true;
    }
    loadingAnimView.useHardwareAcceleration(true);
    if (starCount <= 1) {
      loadingAnimView.setImageAssetsFolder("try_harder/images");
      loadingAnimView.setAnimation("try_harder/data.json");
    } else if (starCount <= 2) {
      loadingAnimView.setImageAssetsFolder("good/images");
      loadingAnimView.setAnimation("good/data.json");
    } else if (starCount <= 3) {
      loadingAnimView.setImageAssetsFolder("excellent/images");
      loadingAnimView.setAnimation("excellent/data.json");
    }
    loadingAnimView.enableMergePathsForKitKatAndAbove(true);
    loadingAnimView.playAnimation();
    loadingAnimView.addAnimatorListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        animTimer();
        baoxiangCanClick = true;
        first_text.setVisibility(View.GONE);
//          first_text2.setVisibility(View.VISIBLE);
      }
    });

    loadingAnimView.setOnClickListener(view -> {
      if (!baoxiangCanClick) {
        return;
      }
      if (timerDis != null && !timerDis.isDisposed()) {
        timerDis.dispose();
      }
      first_text.setVisibility(View.GONE);
//      first_text2.setVisibility(View.VISIBLE);
      loadingAnimView.cancelAnimation();
      aniLayout.setVisibility(View.GONE);
    });
  }

  Disposable timerDis;

  private void animTimer() {
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
            tv_timer.setText((2 - s) + "s");
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

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (soundPool != null) {
      soundPool.release();
    }
  }
}
