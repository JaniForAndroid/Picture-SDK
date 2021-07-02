package com.example.picsdk;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.carlos.voiceline.mylibrary.VoiceLineView;
import com.chivox.EvalResult;
import com.example.exoaudioplayer.aduio.base.AbstractAudioPlayer;
import com.example.exoaudioplayer.aduio.base.AudioCallBack;
import com.example.exoaudioplayer.aduio.base.AudioPlayerFactory;
import com.example.picsdk.base.BaseActivity;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.model.ProductItem;
import com.example.picsdk.model.RatingRule;
import com.example.picsdk.model.ReportVocabulary;
import com.example.picsdk.model.WordExercise;
import com.example.picsdk.util.AppPicUtil;
import com.example.picsdk.view.RoundViewOutline;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.view.HackyViewPager;
import com.namibox.simplifyspan.SimplifySpanBuild;
import com.namibox.simplifyspan.unit.SpecialTextUnit;
import com.namibox.tools.GlideUtil;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.Utils;
import com.namibox.voice_engine_interface.VoiceEngineContext;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Shelter
 * Create time: 2019/9/17, 16:15.
 */
public class VocabularyActivity extends BaseActivity implements OnClickListener {

  private static final String TAG = "VocabularyActivity";
  private List<WordExercise> exercises;
  private HackyViewPager viewPager;
  private WordAdapter adapter;
  private ImageView ivEvaluate, ivEvaluate_watch, ivAudio_watch;
  private ImageView ivAudio;
  private ImageView ivRecord;
  private TextView tvPageIndex, tv_audio, tv_record;
  private VoiceLineView voiceLine;
  private boolean engineInit;
  private boolean dataInit;
  private WordHolder[] holders;
  private boolean isEvaluating;
  private int currentPage;
  private int evaluatePosition;
  private EvalResult[] evalResults;
  private boolean isAiReading;
  private boolean isUserReading;
  private boolean isFinished;
  private List<ReportVocabulary> reportVocabularyList;
  private List<ReportVocabulary> watchVocabularyList = new ArrayList<>();

  private ImageView ivStar;
  private TextView tvScore;
  private RatingRule ratingRule;
  private TextView tv_evaluate;
  private BookManager bookManager;
  private AbstractAudioPlayer exoAudioPlayer;

  private long milesson_item_id;
  private boolean autoFlip = true;
  private int mPlaybackState;
  private Handler handler;
  static final int MSG_AUTO_FLIP = 100;

  private Handler.Callback callback = new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      if (msg.what == MSG_AUTO_FLIP) {
        if (autoFlip && viewPager.getCurrentItem() < adapter.getCount() - 1) {
          Logger.d("切换下一页: " + (viewPager.getCurrentItem() + 1));
          viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
        return true;
      }
      return false;
    }
  };

  private void showAudioScore(EvalResult result) {
    if (TextUtils.equals(result.result_type, "success")) {
      if (holders != null && holders[evaluatePosition] != null) {
        WordHolder holder = holders[evaluatePosition];
        SimplifySpanBuild ssb = new SimplifySpanBuild(this, holder.tvName);
        for (int i = 0; i < result.detail.size(); i++) {
          EvalResult.Detail detail = result.detail.get(i);
          ssb.appendSpecialUnit(new SpecialTextUnit(detail.word, getScoreColor(
              (int) Float.parseFloat(detail.score), true)));
          if (i != result.detail.size() - 1) {
            ssb.appendNormalText(" ");
          }
        }
        holder.tvName.setText(ssb.build());
        evalResults[evaluatePosition] = result;
      }
    } else {
      toast("评测失败，请重试");
    }
  }

  private int getScoreColor(int score, boolean isWord) {
    if (score >= 75) {
      return getResources().getColor(R.color.eval_result_excellent);
    } else if (score >= 60) {
      return getResources()
          .getColor(isWord ? R.color.eval_result_good : R.color.eval_result_good_blue);
    } else {
      return getResources().getColor(R.color.eval_result_bad);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (exoAudioPlayer != null) {
      exoAudioPlayer.releasePlayer();
      exoAudioPlayer.setPlayerCallBack(null);
      exoAudioPlayer = null;
    }
    if (bookManager != null && bookManager.getBookLearning() != null)
      AppPicUtil.TagEventEnterPush(false, ((ProductItem.BookLearning) bookManager.getBookLearning()).text, "学词汇", bookManager.getProductName());
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    setContentView(R.layout.activity_vocabulary);

    handler = new Handler(callback);
    bookManager = BookManager.getInstance();
    ProductItem.Challenge challenge = bookManager.getBookLearning();
    if (challenge == null) {
      toast("数据异常，无法学习词汇");
      finish();
      return;
    }
    milesson_item_id = challenge.milesson_item_id;

    initExoAudioPlayer();
    initActionBar();
    initView();
    initEngineNoUI(new VoiceEngineContext.VoiceEngineCallback() {
      @Override
      public void onInitResult(boolean success, int errCode, String errMsg) {
        engineInit = success;
        Logger.d(TAG, "engine init result = " + success + ", errCode = " + errCode + ", errMsg = " + errMsg);
        if (dataInit) {
          hideProgress();
        }
        if (!success) {
          toast("引擎初始化失败");
          finish();
        }
      }

      @Override
      public void onVolume(int volume) {
        if (voiceLine != null) {
          voiceLine.setVolume((int) (volume * 1.5f));
          voiceLine.run();
        }
      }

      @Override
      public void onResult(Object result) {
        EvalResult evalResult = (EvalResult) result;
        Logger.d(TAG, "onResult: " + evalResult.score);
        showAudioScore(evalResult);
        isEvaluating = false;
        updateButtons();
        updateReportVocabularyList(evalResult);
        viewPager.setLocked(false);
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
        Logger.d(TAG, "onEvalErr errCode = " + errCode + ", errMsg = " + errMsg);
      }
    });
    if (bookManager != null && bookManager.isHomeWorkWatch()) {
      initPicData();
    } else {
      initData();
    }
    if (bookManager != null && bookManager.getBookLearning() != null)
      AppPicUtil.TagEventEnterPush(true, ((ProductItem.BookLearning) bookManager.getBookLearning()).text, "学词汇", bookManager.getProductName());
  }

  private void initExoAudioPlayer() {
    exoAudioPlayer = AudioPlayerFactory.getInstance().createPlayer(getApplicationContext(), com.example.exoaudioplayer.aduio.base.Constants.EXO);
    exoAudioPlayer.setPlayerCallBack(new AudioCallBack() {
      @Override
      public void playUpdate(long currentTime, long bufferTime, long totalTime) {
      }

      @Override
      public void playStateChange(boolean playWhenReady, int playbackState) {
        super.playStateChange(playWhenReady, playbackState);
        if (bookManager.isHomeWorkWatch()) {
          if (mPlaybackState != playbackState) {
            if (playbackState == Player.STATE_ENDED) {
              if (autoFlip && viewPager.getCurrentItem() < adapter.getCount() - 1) {
                new Handler().postDelayed(new Runnable() {
                  public void run() {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                  }
                }, 3000);
              }
              ivEvaluate_watch.setImageResource(R.drawable.icon_pic_play);
            }
          }
          mPlaybackState = playbackState;
        } else {
          if (playbackState == ExoPlayer.STATE_ENDED) {
            onPlayEnd();
          }
        }
      }
    });
  }

  private void initPicData() {
    if (bookManager == null)
      return;

    String url = AppPicUtil.getPicBaseUrl(this) + "/api/user/pbook/" + bookManager.getTypeList().get(bookManager.getIndex()).id + "/data?stu_hw_id=" + bookManager.getStu_hw_id();
    Disposable disposable = ApiHandler.getBaseApi().commonJsonGet(url)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          String retcode = jsonObject.get("retcode").getAsString();
          if (TextUtils.equals(retcode, "SUCC") || TextUtils.equals(retcode, "success")) {
            Type type = new TypeToken<List<ReportVocabulary>>() {
            }.getType();
            JsonArray data = jsonObject.get("data").getAsJsonArray();
            watchVocabularyList = new Gson().fromJson(data, type);
            initData();
            updateReocrdWatch();
          } else {
            String description = jsonObject.get("description").getAsString();
            toast(description);
          }
        }, throwable -> {
          Logger.e(throwable, throwable.toString());
          toast("数据获取失败");
        });
    compositeDisposable.add(disposable);
  }

  private void initData() {
    try {
      File bookResource = AppPicUtil.getBookResource(getApplication(), PicLoadingActivity.BOOK_LINKS_WORD, milesson_item_id);
      File fileRating = AppPicUtil.getBookResource(getApplication(), PicLoadingActivity.BOOK_LINKS_WORD_RATING + "", milesson_item_id);
      String json = FileUtil.FileToString(bookResource, "UTF-8");
      Logger.d("VocabularyActivity", json);
      exercises = new Gson().fromJson(json, new TypeToken<List<WordExercise>>() {
      }.getType());
      reportVocabularyList = new ArrayList<>();
      for (int i = 0; i < exercises.size(); i++) {
        ReportVocabulary item = new ReportVocabulary();
        item.exercise_id = exercises.get(i).id;
        reportVocabularyList.add(item);
      }
      String ratingString = FileUtil.FileToString(fileRating, "UTF-8");
      Gson gson = new Gson();
      JsonObject jsonObject = gson.fromJson(ratingString, JsonObject.class);
      ratingRule = gson.fromJson(jsonObject.get("star"), RatingRule.class);
    } catch (IOException e) {
      e.printStackTrace();
      showErrorDialog("数据解析失败", true);
      return;
    }
    if (exercises == null || exercises.size() == 0 || ratingRule == null) {
      toast("数据加载失败");
      finish();
      return;
    }
    if (engineInit) {
      hideProgress();
    }
    dataInit = true;
    holders = new WordHolder[exercises.size()];
    evalResults = new EvalResult[exercises.size()];
    adapter = new WordAdapter();
    viewPager.setAdapter(adapter);
    tvPageIndex.setText(Utils.format("1/%d", exercises.size()));
    if (bookManager != null && bookManager.isHomeWorkWatch()) {
      ivEvaluate_watch.performClick();
    } else {
      ivAudio.performClick();
    }
    viewPager.addOnPageChangeListener(new OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (!isFinished && position == exercises.size() - 1 && positionOffset > 0.2) {
          isFinished = true;
          Intent intent = new Intent(VocabularyActivity.this, ResultActivity.class);
          intent.putExtra("reportList", (Serializable) reportVocabularyList);
          intent.putExtra(ResultActivity.ARG_LINK, PicLoadingActivity.BOOK_LINKS_WORD);
          startActivity(intent);
          finish();
        }
      }

      @Override
      public void onPageSelected(int position) {
        if (position < exercises.size()) {
          currentPage = position;
          tvPageIndex.setText(Utils.format("%d/%d", position + 1, exercises.size()));
          isAiReading = false;
          if (bookManager != null && bookManager.isHomeWorkWatch()) {
            ivEvaluate_watch.performClick();
            updateReocrdWatch();
          } else {
            ivAudio.performClick();
          }
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });
  }

  private void initView() {
    tvScore = findViewById(R.id.tvScore);
    ivStar = findViewById(R.id.ivStar);
    viewPager = findViewById(R.id.viewPager);
    ivEvaluate = findViewById(R.id.ivEvaluate);
    tv_evaluate = findViewById(R.id.tv_evaluate);
    ivAudio = findViewById(R.id.ivAudio);
    ivRecord = findViewById(R.id.ivRecord);
    tvPageIndex = findViewById(R.id.tvPageIndex);
    voiceLine = findViewById(R.id.voicLine);
    ivEvaluate.setOnClickListener(this);
    ivAudio.setOnClickListener(this);
    ivRecord.setOnClickListener(this);

    if (bookManager != null && bookManager.isHomeWorkWatch()) {
      ivEvaluate_watch = findViewById(R.id.ivEvaluate_watch);
      ivAudio_watch = findViewById(R.id.ivAudio_watch);
      tv_audio = findViewById(R.id.tv_audio);
      tv_record = findViewById(R.id.tv_record);

      ivEvaluate_watch.setVisibility(View.VISIBLE);
      ivAudio_watch.setVisibility(View.VISIBLE);
      ivEvaluate.setVisibility(View.INVISIBLE);
      ivAudio.setVisibility(View.INVISIBLE);
      tv_evaluate.setText("播放");
      tv_audio.setText("自动翻页");
      tv_record.setText("得分");
      ivEvaluate_watch.setOnClickListener(this);
      ivAudio_watch.setOnClickListener(this);
      ivRecord.setEnabled(false);
    }
  }

  public void updateReocrdWatch() {
    int score;
    if (currentPage < watchVocabularyList.size()) {
      score = (int) watchVocabularyList.get(currentPage).score;
    } else {
      score = 0;
    }
    if (score >= ratingRule.third_star.min) {
      ivStar.setImageResource(R.drawable.icon_star_three);
      ivRecord.setImageResource(R.drawable.ic_score_result_excellent);
    } else if (score >= ratingRule.second_star.min) {
      ivStar.setImageResource(R.drawable.icon_star_two);
      ivRecord.setImageResource(R.drawable.ic_score_result_excellent);
    } else if (score >= ratingRule.first_star.min) {
      ivStar.setImageResource(R.drawable.icon_star_one);
      ivRecord.setImageResource(R.drawable.ic_score_result_good);
    } else {
      ivStar.setImageResource(R.drawable.icon_star_zero);
      ivRecord.setImageResource(R.drawable.ic_score_result_bad);
    }
    ivStar.setVisibility(View.VISIBLE);
    tvScore.setText(String.valueOf(score));
    tvScore.setVisibility(View.VISIBLE);
    ivRecord.setImageResource(getRecordButtonRes(score));
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.ivEvaluate) {
      isAiReading = false;
      isUserReading = false;
      startEvaluate();
    } else if (v.getId() == R.id.ivAudio) {
      if (isEvaluating) {
        return;
      }
      isUserReading = false;
      startAudio();
    } else if (v.getId() == R.id.ivRecord) {
      if (isEvaluating) {
        return;
      }
      isAiReading = false;
      startPlay();
    } else if (v.getId() == R.id.ivAudio_watch) {
      autoFlip = !autoFlip;
      ivAudio_watch.setImageResource(autoFlip ? R.drawable.icon_pic_auto : R.drawable.icon_pic_normal);
      if (mPlaybackState == Player.STATE_IDLE || mPlaybackState == Player.STATE_ENDED || (exoAudioPlayer != null && exoAudioPlayer.getPlayer() != null && !exoAudioPlayer.getPlayer().getPlayWhenReady())) {
        handler.sendEmptyMessageDelayed(MSG_AUTO_FLIP, 3000);
      }
    } else if (v.getId() == R.id.ivEvaluate_watch) {
      playAudioForWatch();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    onPlayEnd();
    exoAudioPlayer.stop();
  }

  private void onPlayEnd() {
    isAiReading = false;
    isUserReading = false;
    updateButtons();
  }

  private void playAudioForWatch() {
    if (exoAudioPlayer.isPlaying()) {
      exoAudioPlayer.stop();
      ivEvaluate_watch.setImageResource(R.drawable.icon_pic_play);
    } else {
      if (currentPage < watchVocabularyList.size()) {
        if (!TextUtils.isEmpty(watchVocabularyList.get(currentPage).oral_audio)) {
          exoAudioPlayer.play(Uri.parse(watchVocabularyList.get(currentPage).oral_audio));
          ivEvaluate_watch.setImageResource(R.drawable.icon_pic_stop);
        } else {
          Utils.toast(this,getString(R.string.player_error_tips1));
        }
      }
    }
  }

  private void startAudio() {
    exoAudioPlayer.setPlayWhenReady(false);
    if (isAiReading) {
      onPlayEnd();
    } else {
      if (!TextUtils.isEmpty(exercises.get(currentPage).question.audio)) {
        exoAudioPlayer.play(Uri.parse(exercises.get(currentPage).question.audio));
        isAiReading = true;
        updateButtons();
      } else {
        Utils.toast(this,getString(R.string.player_error_tips1));
      }
    }
  }

  private void startPlay() {
    exoAudioPlayer.setPlayWhenReady(false);
    if (isUserReading) {
      onPlayEnd();
    } else {
      boolean hasEvaluated = evalResults[currentPage] != null && !TextUtils.isEmpty(evalResults[currentPage].localpath);
      if (hasEvaluated) {
        if (!TextUtils.isEmpty(evalResults[currentPage].localpath)) {
          exoAudioPlayer.play(Uri.parse(evalResults[currentPage].localpath));
          isUserReading = true;
        } else {
          Utils.toast(this,getString(R.string.player_error_tips1));
        }
      }
      updateButtons();
    }
  }

  private int getRecordButtonRes(float score) {
    if (score >= 75) {
      return R.drawable.ic_score_result_excellent;
    } else if (score >= 60) {
      return R.drawable.ic_score_result_good;
    } else {
      return R.drawable.ic_score_result_bad;
    }
  }

  private void startEvaluate() {
    exoAudioPlayer.setPlayWhenReady(false);
    evaluatePosition = currentPage;
    if (isEvaluating) {
      isEvaluating = false;
      stopEngine();
      updateButtons(true);
      viewPager.setLocked(false);
    } else {
      isEvaluating = true;
      startEngine(exercises.get(currentPage).question.vocabulary, 5000, null);
      updateButtons();
      viewPager.setLocked(true);
    }
  }

  private void updateButtons() {
    updateButtons(false);
  }

  private void updateButtons(boolean onlyEvaluate) {
    if (onlyEvaluate) {
      updateEvaluate();
      return;
    }
    updateAudio();
    updateEvaluate();
    updateRecord();
  }

  private void updateEvaluate() {
    if (isEvaluating) {
      voiceLine.setVisibility(View.VISIBLE);
      ivEvaluate.setImageResource(R.drawable.icon_evaluating);
      tv_evaluate.setText("点击停止录音");
    } else {
      voiceLine.setVisibility(View.GONE);
      voiceLine.setVolume(0);
      ivEvaluate.setImageResource(R.drawable.icon_evaluate_pic);
      tv_evaluate.setText("点击开始录音");
    }
  }

  private void updateAudio() {
    if (isEvaluating) {
      ivAudio.setImageResource(R.drawable.icon_audio_disabled);
    } else {
      ivAudio.setImageResource(isAiReading ? R.drawable.icon_audio_playing : R.drawable.icon_audio_enabled);
    }
  }

  private void updateRecord() {
    if (isEvaluating) {
      ivStar.setVisibility(View.INVISIBLE);
      tvScore.setVisibility(View.INVISIBLE);
      ivRecord.setImageResource(R.drawable.icon_record_disabled);
      return;
    }
    if (isUserReading) {
      ivRecord.setImageResource(R.drawable.icon_audio_playing);
      ivStar.setVisibility(View.INVISIBLE);
      tvScore.setVisibility(View.INVISIBLE);
      return;
    }
    if (evalResults[currentPage] == null || TextUtils.isEmpty(evalResults[currentPage].localpath)) {
      ivStar.setVisibility(View.INVISIBLE);
      tvScore.setVisibility(View.INVISIBLE);
      ivRecord.setImageResource(R.drawable.icon_record_disabled);
    } else {
      int score = (int) evalResults[currentPage].score;
      if (score >= ratingRule.third_star.min) {
        ivStar.setImageResource(R.drawable.icon_star_three);
        ivRecord.setImageResource(R.drawable.ic_score_result_excellent);
      } else if (score >= ratingRule.second_star.min) {
        ivStar.setImageResource(R.drawable.icon_star_two);
        ivRecord.setImageResource(R.drawable.ic_score_result_excellent);
      } else if (score >= ratingRule.first_star.min) {
        ivStar.setImageResource(R.drawable.icon_star_one);
        ivRecord.setImageResource(R.drawable.ic_score_result_good);
      } else {
        ivStar.setImageResource(R.drawable.icon_star_zero);
        ivRecord.setImageResource(R.drawable.ic_score_result_bad);
      }
      ivStar.setVisibility(View.VISIBLE);
      tvScore.setText(String.valueOf(score));
      tvScore.setVisibility(View.VISIBLE);
      ivRecord.setImageResource(getRecordButtonRes(evalResults[currentPage].score));
    }
  }

  class WordAdapter extends PagerAdapter {

    @Override
    public int getCount() {
      //多加一页，滑动到最后一页时退出;
      return exercises.size() + 1;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
      return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
      Context context = container.getContext();
      if (position == exercises.size()) {
        View view = new View(context);
        container.addView(view);
        return view;
      }
      View inflate = LayoutInflater.from(context).inflate(R.layout.layout_vocabulary_item, container, false);
      container.addView(inflate);
      WordHolder holder = new WordHolder(inflate);
      WordExercise.Question question = exercises.get(position).question;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        holder.image.setOutlineProvider(new RoundViewOutline(Utils.dp2px(context, 10)));
      }
      GlideUtil.loadImage(context, question.image, holder.image);
      holder.tvName.setText(question.vocabulary);
      holder.tvPron.setText(question.phonetic);
      holder.tvExplain.setText(question.chinese);
      holders[position] = holder;
      return inflate;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
      container.removeView((View) object);
      if (position == exercises.size()) {
        return;
      }
      holders[position] = null;
    }

  }

  class WordHolder {

    private final ImageView image;
    private final TextView tvName;
    private final TextView tvPron;
    private final TextView tvExplain;

    public WordHolder(View itemView) {
      image = itemView.findViewById(R.id.image);
      tvName = itemView.findViewById(R.id.tvName);
      tvPron = itemView.findViewById(R.id.tvPron);
      tvExplain = itemView.findViewById(R.id.tvExplain);
    }
  }

  private void updateReportVocabularyList(EvalResult result) {
    ReportVocabulary item = reportVocabularyList.get(currentPage);
    item.fluency = result.fluency;
    item.localpath = result.localpath;
    item.pron = result.pron;
    item.integrity = result.integrity;
    item.exercise_id = exercises.get(currentPage).id;
    item.text = exercises.get(currentPage).question.vocabulary;
    item.score = result.score;
  }
}
