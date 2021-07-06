package com.example.picsdk;


import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.picsdk.base.BaseExerciseFragment;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.model.Exercise;
import com.example.picsdk.model.Option;
import com.example.picsdk.model.WatchPic;
import com.example.picsdk.util.AppPicUtil;
import com.example.picsdk.view.GridSpacingItemDecoration;
import com.example.picsdk.view.TextInputView;
import com.example.picsdk.view.progress.CircleProgress;
import com.namibox.tools.GlideUtil;
import com.namibox.tools.RxTimerUtil;
import com.namibox.util.Utils;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseChallengeFragment extends BaseExerciseFragment {

  private DiscreteScrollView mDsvExercise;
  private List<View> mViewQuestions;
  private ExerciseChallengeActivity mActivity;
  private List<Exercise> mExerciseList;
  private ViewGroup mLayoutExercise;
  ProgressBar mProgressBar;
  private int mCurrentExercisePosition;
  private Map<Exercise, OnStartAnswerCallBack> mOnStartAnswerCallBackMap = new HashMap<>();
  private int[] challengeDrawableResIds = new int[]{R.drawable.ic_question_1,
      R.drawable.ic_question_2, R.drawable.ic_question_3,
      R.drawable.ic_question_4, R.drawable.ic_question_5,
      R.drawable.ic_question_6, R.drawable.ic_question_7,
      R.drawable.ic_question_8, R.drawable.ic_question_9,
      R.drawable.ic_question_10};
  private int[] COLORS = new int[]{0xffE5E5E5, 0xffE5E5E5, 0xffE5E5E5, 0xffE5E5E5};
  private RxTimerUtil mCountDownTimer;
  private long duration = 0L;
  private int successId;
  private int wrongId;
  private SoundPool soundPool;
  private TextView tv_numMax, tv_num, tv_next, tv_last, tvCountDown;
  private BookManager bookManager;
  private int mPosition;
  private CircleProgress mCircleProgress;

  public ExerciseChallengeFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_word_chanllenge_pic, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mDsvExercise = view.findViewById(R.id.recycler_view);
    tv_numMax = view.findViewById(R.id.tv_numMax);
    tv_num = view.findViewById(R.id.tv_num);
    tv_last = view.findViewById(R.id.tv_last);
    tv_next = view.findViewById(R.id.tv_next);
    tvCountDown = view.findViewById(R.id.tv_count_down);
    mCircleProgress = view.findViewById(R.id.circle_progress_bar);
    mProgressBar = view.findViewById(R.id.progress);

    bookManager = BookManager.getInstance();
    mDsvExercise.setOrientation(DSVOrientation.HORIZONTAL);
    mDsvExercise.setItemTransitionTimeMillis(150);
    mDsvExercise.setAdapter(new ExerciseAdapter());
    mDsvExercise.addScrollStateChangeListener(new DiscreteScrollView.ScrollStateChangeListener<ViewHolder>() {
      @Override
      public void onScrollStart(@NonNull ViewHolder currentItemHolder, int adapterPosition) {

      }

      @Override
      public void onScrollEnd(@NonNull ViewHolder currentItemHolder, int adapterPosition) {
        mDsvExercise.requestLayout();
      }

      @Override
      public void onScroll(float scrollPosition, int currentPosition, int newPosition,
                           @Nullable ViewHolder currentHolder, @Nullable ViewHolder newCurrent) {

      }
    });
    mActivity = (ExerciseChallengeActivity) activity;

    parseCurrentExercise();

    mCircleProgress.setMaxValue(mActivity.getData().data.duration);
    initCutDown(mActivity.getData().data.duration * 1000);
    initSound();
    if (bookManager != null && bookManager.isHomeWorkWatch()) {
      initWatchView();
    }
  }

  public void initCutDown(long time) {
    mCountDownTimer = new RxTimerUtil();
    mCountDownTimer.interval(time, new RxTimerUtil.IRxNext() {
      @Override
      public void onTick(Long s) {
        duration = s;
        tvCountDown.setText(s + "s");

        mCircleProgress.setGradientColors(COLORS);
        mCircleProgress.setValue(mActivity.getData().data.duration - duration);
      }

      @Override
      public void onFinish() {
        if (bookManager != null && bookManager.isHomeWorkWatch()) {
          return;
        }
        mActivity.getData().data.duration = mActivity.getData().data.duration - duration + 1;
        mActivity.enterChallengeResult();
      }
    });
  }

  public void initWatchView() {
    tv_last.setVisibility(View.VISIBLE);
    tv_next.setVisibility(View.VISIBLE);
    tvCountDown.setVisibility(View.GONE);
    mCircleProgress.setVisibility(View.GONE);
    tv_last.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mCurrentExercisePosition % 2 != 0 && mPosition - 1 >= 0)
          lastExercise(mExerciseList.get(mPosition - 1));
      }
    });
    tv_next.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mCurrentExercisePosition % 2 != 0 && mPosition - 1 >= 0)
          nextExercise(mExerciseList.get(mPosition - 1));
      }
    });
  }

  @SuppressLint("NewApi")
  private void initSound() {
    soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
//        soundPool = new SoundPool.Builder().build();
    successId = soundPool.load(getActivity(), R.raw.good_job, 1);
    wrongId = soundPool.load(getActivity(), R.raw.try_again, 1);
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

  private void parseCurrentExercise() {
    if (mActivity.getData() != null && mActivity.getData().data != null && mActivity.getData().data.exercises.size() != 0)
      for (Exercise exercise : mActivity.getData().data.exercises) {
        if (exercise.options != null && exercise.options.size() > 0)
          AppPicUtil.randomList(exercise.options);
      }
    mExerciseList = mActivity.getData().data.exercises;
    mViewQuestions = new ArrayList<>();
    for (int i = 0; i < mExerciseList.size(); i++) {
      View viewIndex = LayoutInflater.from(activity).inflate(R.layout.vp_challenge_index, null, false);
      ImageView ivIndex = viewIndex.findViewById(R.id.iv_index);
      ivIndex.setImageResource(challengeDrawableResIds[i % 10]);
      mViewQuestions.add(viewIndex);
      View viewContent = LayoutInflater.from(activity).inflate(R.layout.vp_word_challenge, null, false);
      mLayoutExercise = viewContent.findViewById(R.id.layout_exercise);
      inflateExercise(mExerciseList.get(i));
      mViewQuestions.add(viewContent);
    }
    mDsvExercise.addOnItemChangedListener((viewHolder, adapterPosition) -> {
      mPosition = (adapterPosition + 1) / 2;
      if (mAnswerProgressListener != null) {
        if (adapterPosition == 1) {
          mActivity.setCurrentExercise(mExerciseList.get(0));
        }
        if (mCurrentExercisePosition % 2 != 0) {
          mProgressBar.setProgress(mPosition * 100 / mExerciseList.size());
          tv_num.setText(mPosition + "");
          tv_numMax.setText("/" + mExerciseList.size());
        }
      }
      if (adapterPosition % 2 == 1 && mOnStartAnswerCallBackMap.get(mExerciseList.get(adapterPosition / 2)) != null) {
        mOnStartAnswerCallBackMap.get(mExerciseList.get(adapterPosition / 2)).onStartAnswer();
      }
    });
  }

  protected void inflateExercise(Exercise exercise) {
    switch (exercise.type) {
      case "选择题":
        inflateQuestion(exercise);
        inflateAnswer(exercise);
        break;
      case "词汇挑战":
        inflateWordChallengeQuestion(exercise);
        inflateAnswer(exercise);
        break;
      case "朗读题":
        break;
      case "填空题":
        break;
      case "排序题":
        inflateSortExercise(exercise);
        break;
      default:
        break;
    }
  }

  private void inflateSortExercise(Exercise exercise) {
    switch (exercise.sub_type) {
      case "文字":
        inflateTextSort(exercise, false);
        break;
      case "回填":
        inflateTextSort(exercise, true);
        break;
      case "图片":
        inflateImgSort(exercise);
        break;
    }
  }

  private void inputImageSort(String content, Exercise exercise) {
    for (int i = 0; i < exercise.destination_sequence.size(); i++) {
      if (exercise.destination_sequence.get(i).isCheck) {
        exercise.userAnsweList.set(i, content);
      }
    }
  }

  private void clearImageSort(String content, Exercise exercise) {
    for (int i = 0; i < exercise.userAnsweList.size(); i++) {
      if (exercise.userAnsweList.get(i).equals(content)) {
        exercise.userAnsweList.set(i, "");
      }
    }

    for (int i = 0; i < exercise.destination_sequence.size(); i++) {
      if (exercise.destination_sequence.get(i).content.equals(content)) {
        exercise.destination_sequence.get(i).isSelect = false;
      }
    }
  }

  private boolean initSortSubmit(Exercise exercise) {
    boolean isSubmit = true;
    for (int i = 0; i < exercise.userAnsweList.size(); i++) {
      if (exercise.userAnsweList.get(i).equals("")) {
        isSubmit = false;
      }
    }
    return isSubmit;
  }

  private void inflateImgSort(Exercise exercise) {
    TextView tvSubmit;
    ExerciseSortImgAdapter mAnswerAdapter = new ExerciseSortImgAdapter();
    ExerciseSortImgAdapter1 mQuestionAdapter = new ExerciseSortImgAdapter1();

    if (exercise.destination_sequence.size() != 0) {
      exercise.destination_sequence.get(0).isCheck = true;
    }

    ArrayList<String> userAnsweList = new ArrayList<>();
    View viewGroup = addExerciseView(R.layout.exercise_question_voice_sort);

    tvSubmit = viewGroup.findViewById(R.id.tv_submit);
    mAnswerAdapter.setExercise(exercise, tvSubmit, mAnswerAdapter, mQuestionAdapter);
    mQuestionAdapter.setExercise(exercise, tvSubmit, mAnswerAdapter, mQuestionAdapter);

    int position = mExerciseList.indexOf(exercise);
    if (position == mExerciseList.size() - 1) {
      if (tvSubmit != null)
        tvSubmit.setText(getString(R.string.hfx_commit));
    } else {
      if (tvSubmit != null)
        tvSubmit.setText(getString(R.string.book_readingtext_title));
    }
    tvSubmit.setOnClickListener(v -> {
      boolean isCorrect = true;
      tvSubmit.setClickable(false);
      for (int i = 0; i < exercise.userAnsweList.size(); i++) {
        for (int j = 0; j < exercise.destination_sequence.size(); j++) {
          if (exercise.userAnsweList.get(i).equals(exercise.destination_sequence.get(j).content)) {
            if (i == exercise.destination_sequence.get(j).index) {
              mAnswerAdapter.notifyItemChanged(i, true);
            } else {
              isCorrect = false;
              mAnswerAdapter.notifyItemChanged(i, false);
            }
          }
        }
      }
//      exercise.userAnsweList = tvAttach.getUserAnswer();
      exercise.answerCorrect = isCorrect;
      if (isCorrect) {
        playSound(successId);
      } else {
        playSound(wrongId);
      }
      tvSubmit.postDelayed(() -> nextExercise(exercise), 2000);
    });

    ImageView ivVoice = viewGroup.findViewById(R.id.iv_voice);
    List<String> urls = new ArrayList<>();
    if (exercise.source_sequence.size() != 0) {
      for (int i = 0; i < exercise.source_sequence.size(); i++) {
        userAnsweList.add("");
        int pos = i + 1;
        urls.add("https://f.namibox.com/pbook/app/resource/number_audio/number_audio_" + pos + ".mp3");
        urls.add(exercise.source_sequence.get(i).content);
      }
      exercise.userAnsweList = userAnsweList;
    }
    TextView tvTitle = viewGroup.findViewById(R.id.tv_title);
    tvTitle.setText(exercise.question.text);
    if (urls.size() != 0) {
      ivVoice.setOnClickListener(v -> playAudioList(urls));
      mOnStartAnswerCallBackMap.put(exercise, () -> playAudioList(urls));
    }

    RecyclerView recyclerView = viewGroup.findViewById(R.id.recycler_question);
    recyclerView.setHasFixedSize(true);
    recyclerView.setNestedScrollingEnabled(false);
    if (exercise.destination_sequence.size() > 4) {
      recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));
      recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, Utils.dp2px(activity, 15), false));
    } else {
      recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
      recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, Utils.dp2px(activity, 15), false));
    }
    recyclerView.setAdapter(mQuestionAdapter);

    RecyclerView answerList = viewGroup.findViewById(R.id.recycler_answer);
    answerList.setHasFixedSize(true);
    answerList.setNestedScrollingEnabled(false);

    if (exercise.destination_sequence.size() >= 4) {
      answerList.setLayoutManager(new GridLayoutManager(activity, 4));
      answerList.addItemDecoration(new GridSpacingItemDecoration(4, Utils.dp2px(activity, 15), false));
    } else if (exercise.destination_sequence.size() == 3) {
      answerList.setLayoutManager(new GridLayoutManager(activity, 3));
      answerList.addItemDecoration(new GridSpacingItemDecoration(3, Utils.dp2px(activity, 20), false));
    } else {
      answerList.setLayoutManager(new GridLayoutManager(activity, 2));
      answerList.addItemDecoration(new GridSpacingItemDecoration(2, Utils.dp2px(activity, 15), false));
    }

    answerList.setAdapter(mAnswerAdapter);

    WatchPic watchPic = ((ExerciseChallengeActivity) activity).getWatchPic();
    if (watchPic != null && watchPic.errors != null) {
      exercise.userAnsweList.clear();
      tvSubmit.setVisibility(GONE);
      boolean isError = false;
      for (WatchPic.Errors error : watchPic.errors) {
        if (exercise.id.equals(error.exercise_id + "") && error.user_answer_sort.size() != 0) {
          for (String sort : error.user_answer_sort) {
            exercise.userAnsweList.add(sort);
          }
          isError = true;
        }
      }
      if (!isError) {
        for (int i = 0; i < exercise.destination_sequence.size(); i++) {
          for (Exercise.Sequence sequence : exercise.destination_sequence) {
            if (i == sequence.index) {
              exercise.userAnsweList.add(sequence.content);
            }
          }
        }
      }

      for (int i = 0; i < exercise.destination_sequence.size(); i++) {
        exercise.destination_sequence.get(i).isSelect = true;
        exercise.destination_sequence.get(i).isCheck = false;
      }

      for (int i = 0; i < exercise.userAnsweList.size(); i++) {
        for (int j = 0; j < exercise.destination_sequence.size(); j++) {
          if (exercise.userAnsweList.get(i).equals(exercise.destination_sequence.get(j).content)) {
            if (i == exercise.destination_sequence.get(j).index)
              exercise.destination_sequence.get(i).isCorrect = true;
            else {
              exercise.destination_sequence.get(i).isCorrect = false;
            }
          }
        }
      }
    }
  }

  class ExerciseSortImgAdapter1 extends RecyclerView.Adapter<ItemViewHolder> {
    private Exercise exercise;

    private TextView tvSubmit;
    private ExerciseSortImgAdapter mAnswerAdapter;
    private ExerciseSortImgAdapter1 mQuestionAdapter;

    void setExercise(Exercise item, TextView tv, ExerciseSortImgAdapter adapter, ExerciseSortImgAdapter1 adapter1) {
      exercise = item;
      tvSubmit = tv;
      mAnswerAdapter = adapter;
      mQuestionAdapter = adapter1;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(activity).inflate(R.layout.rv_item_exercise_answer_word_challenge_choice, null);
      if (exercise.destination_sequence.size() <= 4) {
        View childView = view.findViewById(R.id.iv_option);
        childView.getLayoutParams().width = Utils.dp2px(parent.getContext(), 120);
      }
      return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
      Exercise.Sequence item = exercise.destination_sequence.get(position);
      GlideUtil.loadImage(activity, item.content, holder.ivOption);
      holder.ivOption.setAlpha(exercise.destination_sequence.get(position).isSelect ? 0.4f : 1f);

      holder.ivFlag.setVisibility(GONE);
      WatchPic watchPic = ((ExerciseChallengeActivity) activity).getWatchPic();
      if (watchPic != null && watchPic.errors != null) {
        holder.itemView.setEnabled(false);
      }

      holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (exercise.destination_sequence.get(position).isSelect) {
            exercise.destination_sequence.get(position).isSelect = false;
            initCheckItem(exercise, initCheckPosition1(exercise, item.content));
            clearImageSort(item.content, exercise);

            mAnswerAdapter.notifyDataSetChanged();
            tvSubmit.setEnabled(initSortSubmit(exercise));
            mQuestionAdapter.notifyDataSetChanged();
          } else {
            exercise.destination_sequence.get(position).isSelect = true;
            inputImageSort(item.content, exercise);
            initCheckItem(exercise, initCheckPosition(exercise));

            mAnswerAdapter.notifyDataSetChanged();
            tvSubmit.setEnabled(initSortSubmit(exercise));
            mQuestionAdapter.notifyDataSetChanged();
          }
        }
      });
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull List<Object> payloads) {
      super.onBindViewHolder(holder, position, payloads);
      if (!payloads.isEmpty()) {
      }
    }

    @Override
    public int getItemCount() {
      return exercise.destination_sequence == null ? 0 : exercise.destination_sequence.size();
    }
  }

  public void initCheckItem(Exercise exercise, int position) {
    for (int i = 0; i < exercise.destination_sequence.size(); i++) {
      if (position == i) {
        exercise.destination_sequence.get(i).isCheck = true;
      } else {
        exercise.destination_sequence.get(i).isCheck = false;
      }
    }
  }

  public int initCheckPosition1(Exercise exercise, String content) {
    boolean isInput = false;
    int position = -1;
    for (int i = 0; i < exercise.userAnsweList.size(); i++) {
      if (exercise.userAnsweList.get(i).equals(content) && !isInput) {
        isInput = true;
        position = i;
      }
    }
    return position;
  }

  public int initCheckPosition(Exercise exercise) {
    boolean isInput = false;
    int position = -1;
    for (int i = 0; i < exercise.userAnsweList.size(); i++) {
      if (exercise.userAnsweList.get(i).equals("") && !isInput) {
        isInput = true;
        position = i;
      }
    }
    return position;
  }

  class ExerciseSortImgAdapter extends RecyclerView.Adapter<ItemViewHolderSort> {
    private Exercise exercise;
    private TextView tvSubmit;
    private ExerciseSortImgAdapter mAnswerAdapter;
    private ExerciseSortImgAdapter1 mQuestionAdapter;

    void setExercise(Exercise item, TextView tv, ExerciseSortImgAdapter adapter, ExerciseSortImgAdapter1 adapter1) {
      exercise = item;
      tvSubmit = tv;
      mAnswerAdapter = adapter;
      mQuestionAdapter = adapter1;
    }

    @NonNull
    @Override
    public ItemViewHolderSort onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(activity).inflate(R.layout.rv_item_exercise_answer_word_challenge_choice_sort, null);
      if (exercise.destination_sequence.size() <= 4) {
        View childView = view.findViewById(R.id.iv_option);
        childView.getLayoutParams().width = Utils.dp2px(parent.getContext(), 75);
      }
      return new ItemViewHolderSort(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolderSort holder, int position) {
      String content = exercise.userAnsweList.get(position);
      GlideUtil.loadImage(activity, content, holder.ivOption);
      holder.ivFlag.setVisibility(GONE);
      if (exercise.destination_sequence.get(position).isCheck) {
        holder.ivOption.setBackgroundResource(R.drawable.sp_rect_radius_white_10_check);
      } else {
        holder.ivOption.setBackgroundResource(R.drawable.sp_rect_radius_white_10_uncheck);
      }

      int index = position + 1;
      holder.index.setText(index + "");

      WatchPic watchPic = ((ExerciseChallengeActivity) activity).getWatchPic();
      if (watchPic != null && watchPic.errors != null) {
        holder.itemView.setEnabled(false);

        holder.ivFlag.setVisibility(View.VISIBLE);
        if (exercise.destination_sequence.get(position).isCorrect) {
          holder.ivFlag.setImageResource(R.drawable.ic_correct_img_flag_1);
        } else {
          holder.ivFlag.setImageResource(R.drawable.ic_error_img_flag);
        }
      }

      holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (content.equals("")) {
            initCheckItem(exercise, position);
            mAnswerAdapter.notifyDataSetChanged();
            return;
          }
          clearImageSort(content, exercise);
          initCheckItem(exercise, position);

          mAnswerAdapter.notifyDataSetChanged();
          tvSubmit.setEnabled(initSortSubmit(exercise));
          mQuestionAdapter.notifyDataSetChanged();
        }
      });
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolderSort holder, int position, @NonNull List<Object> payloads) {
      super.onBindViewHolder(holder, position, payloads);
      if (!payloads.isEmpty()) {
        if (payloads.get(0).equals(true)) {
          holder.ivFlag.setVisibility(View.VISIBLE);
          holder.ivFlag.setImageResource(R.drawable.ic_correct_img_flag_1);
        } else if (payloads.get(0).equals(false)) {
          holder.ivFlag.setVisibility(View.VISIBLE);
          holder.ivFlag.setImageResource(R.drawable.ic_error_img_flag);
        }

      }
    }

    @Override
    public int getItemCount() {
      return exercise.destination_sequence == null ? 0 : exercise.destination_sequence.size();
    }
  }

  public void initSequenceList(Exercise exercise) {
    if (exercise.source_sequence.size() != 0) {
      String[] sourceStrArray = exercise.source_sequence.get(0).content.split("\\[\\]");
      if (sourceStrArray.length > 2) {
        for (int i = 0; i < sourceStrArray.length; i++) {
          Exercise.Sequence sequence = new Exercise.Sequence();
          if (i == sourceStrArray.length - 2)
            sequence.content = sourceStrArray[i] + "[]" + sourceStrArray[i + 1];
          else
            sequence.content = sourceStrArray[i] + "[]";
          sequence.index = i;
          sequence.content_type = exercise.source_sequence.get(0).content_type;
          if (i < sourceStrArray.length - 1)
            exercise.source_sequence.add(sequence);
        }
        exercise.source_sequence.remove(0);
      }
    }
  }

  private void inflateTextSort(Exercise exercise, boolean b) {
    initSequenceList(exercise);
    View viewGroup = addExerciseView(R.layout.exercise_question_text_sort);
    ImageView ivVoice = viewGroup.findViewById(R.id.iv_voice);
    String videoUrl = null;
    if (!TextUtils.isEmpty(exercise.question.reference)) {
      videoUrl = exercise.question.reference;
    } else if (TextUtils.equals("语音", exercise.question.attach_type)
        && !TextUtils.isEmpty(exercise.question.attach)) {
      videoUrl = exercise.question.attach;
    } else {
      ivVoice.setVisibility(GONE);
    }
    TextView tvTitle = viewGroup.findViewById(R.id.tv_title);
    tvTitle.setText(exercise.question.text);
    TextInputView tvAttach = viewGroup.findViewById(R.id.tv_attach);
    final String videoUrlTemp = videoUrl;
    if (!TextUtils.isEmpty(videoUrl)) {
      ivVoice.setOnClickListener(v -> playAudio(videoUrlTemp));
      mOnStartAnswerCallBackMap.put(exercise, () -> playAudio(videoUrlTemp));
    }
    TextView tvSubmit = viewGroup.findViewById(R.id.tv_submit);
    int position = mExerciseList.indexOf(exercise);
    if (position == mExerciseList.size() - 1) {
      if (tvSubmit != null)
        tvSubmit.setText(getString(R.string.hfx_commit));
    } else {
      if (tvSubmit != null)
        tvSubmit.setText(getString(R.string.book_readingtext_title));
    }
    tvSubmit.setOnClickListener(v -> {
      tvSubmit.setClickable(false);
      tvAttach.showResult(true);
      exercise.userAnsweList = tvAttach.getUserAnswer();
      exercise.answerCorrect = tvAttach.getUserCorrect();

      if (tvAttach.getUserCorrect()) {
        playSound(successId);
      } else {
        playSound(wrongId);
      }
      tvSubmit.postDelayed(() -> nextExercise(exercise), 2000);
    });
    RecyclerView recyclerView = viewGroup.findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(activity));
    recyclerView.setHasFixedSize(true);
    recyclerView.setNestedScrollingEnabled(false);
    class TextSortViewHolder extends ViewHolder {

      TextView tvText;
      LinearLayout tv_text_layout;

      public TextSortViewHolder(View itemView) {
        super(itemView);
        tvText = itemView.findViewById(R.id.tv_text);
        tv_text_layout = itemView.findViewById(R.id.tv_text_layout);
      }
    }
    recyclerView.setAdapter(new RecyclerView.Adapter<TextSortViewHolder>() {

      @NonNull
      @Override
      public TextSortViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.rv_item_exercise_answer_text_sort, null);
        return new TextSortViewHolder(view);
      }

      @Override
      public void onBindViewHolder(@NonNull TextSortViewHolder holder, int position) {
        Exercise.Sequence item = exercise.destination_sequence.get(position);
        holder.tvText.setText(item.content);
        WatchPic watchPic = ((ExerciseChallengeActivity) activity).getWatchPic();
        if (watchPic != null && watchPic.errors != null) {
          boolean isError = false;
          for (WatchPic.Errors error : watchPic.errors) {
            if (exercise.id.equals(error.exercise_id + "") && error.user_answer_sort.size() != 0) {
              for (String sort : error.user_answer_sort) {
                holder.tv_text_layout.setSelected(false);
                tvAttach.inputText(sort);
              }
              isError = true;
            }
          }
          if (!isError) {
            for (int i = 0; i < exercise.destination_sequence.size(); i++) {
              for (Exercise.Sequence sequence : exercise.destination_sequence) {
                if (i == sequence.index) {
                  holder.tv_text_layout.setSelected(false);
                  tvAttach.inputText(sequence.content);
                }
              }
            }
          }
          tvAttach.showResult(true);
          tvAttach.showClickAble(false);
          tvSubmit.setVisibility(View.GONE);
          holder.tv_text_layout.setEnabled(false);
          holder.tv_text_layout.setSelected(true);
        }

        holder.tv_text_layout.setOnClickListener(v -> {
          if (holder.tv_text_layout.isSelected()) {
            holder.tv_text_layout.setSelected(false);
            tvAttach.clearText(item.content);
          } else {
            holder.tv_text_layout.setSelected(true);
            tvAttach.inputText(item.content);
          }
        });
      }

      @Override
      public void onBindViewHolder(@NonNull TextSortViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (!payloads.isEmpty()) {
          holder.tvText.setSelected(false);
        }
      }

      @Override
      public int getItemCount() {
        return exercise.destination_sequence.size();
      }
    });
    tvAttach.setMovementMethod(LinkMovementMethod.getInstance());
    tvAttach.setData(exercise.source_sequence, exercise.destination_sequence);
    tvAttach.setOnTextInPutListener(new TextInputView.OnTextInPutListener() {
      @Override
      public void clearText(String text) {
        for (int j = 0; j < exercise.destination_sequence.size(); j++) {
          if (TextUtils.equals(text, exercise.destination_sequence.get(j).content)) {
            recyclerView.getAdapter().notifyItemChanged(j, 0);
          }
        }
      }

      @Override
      public void progress(boolean isFinish, boolean result) {
        exercise.answerCorrect = result;
        tvSubmit.setEnabled(isFinish);
      }
    });
  }

  private void inflateQuestion(Exercise exercise) {
    View viewGroup = addExerciseView(R.layout.exercise_question);
    ImageView ivVoice1 = viewGroup.findViewById(R.id.iv_voice_1);
    String videoUrl = null;
    if (!TextUtils.isEmpty(exercise.question.reference)) {
      videoUrl = exercise.question.reference;
    } else if (TextUtils.equals("语音", exercise.question.attach_type)
        && !TextUtils.isEmpty(exercise.question.attach)) {
      videoUrl = exercise.question.attach;
    }
    final String videoUrlTemp = videoUrl;
    if (!TextUtils.isEmpty(videoUrl)) {
      ivVoice1.setVisibility(View.VISIBLE);
      ivVoice1.setOnClickListener(v -> playAudio(videoUrlTemp));
      mOnStartAnswerCallBackMap.put(exercise, () -> playAudio(videoUrlTemp));
    }
    TextView tvTitle = viewGroup.findViewById(R.id.tv_title);
    if (!TextUtils.isEmpty(exercise.question.text)) {
      tvTitle.setText(Html.fromHtml(exercise.question.text.replaceAll("\n", "<br>")));
    }
    ImageView ivAttach = viewGroup.findViewById(R.id.iv_attach);
    if (TextUtils.equals("图片", exercise.question.attach_type)) {
      ivAttach.setVisibility(View.VISIBLE);
      GlideUtil.loadImage(activity, exercise.question.attach, ivAttach);
    }
    TextView tvAttach = viewGroup.findViewById(R.id.tv_attach);
    if (TextUtils.equals("文字", exercise.question.attach_type)
        && !TextUtils.isEmpty(exercise.question.attach)) {
      tvAttach.setVisibility(View.VISIBLE);
      tvAttach.setText(Html.fromHtml(exercise.question.attach.replaceAll("\n", "<br>")));
    }
    TextView tvInput = viewGroup.findViewById(R.id.tv_input);
    if (TextUtils.equals("文字", exercise.question.input_type) && !TextUtils.isEmpty(exercise.question.input)) {
      tvInput.setVisibility(View.VISIBLE);
      tvInput.setText(Html.fromHtml(exercise.question.input.replaceAll("\n", "<br>")));
    }
  }

  private void inflateWordChallengeQuestion(Exercise exercise) {
    View viewGroup = addExerciseView(R.layout.exercise_question_word_challenge);
    TextView tvTitle = viewGroup.findViewById(R.id.tv_title);
    tvTitle.setText(exercise.question.text);
    ImageView ivVoice = viewGroup.findViewById(R.id.iv_voice);
    ivVoice.setOnClickListener(v -> playAudio(exercise.question.attach));
    mOnStartAnswerCallBackMap.put(exercise, () -> playAudio(exercise.question.attach));
  }

  private void inflateAnswer(Exercise exercise) {
    Option options = exercise.options.get(0);
    switch (options.content_type) {
      case "文字":
        //String[] s = optionsBean.content.split("\\s");
        if (exercise.options.size() == 2 && exercise.options.get(0).content.length() < 2
            && exercise.options.get(1).content.length() < 2) {
          inflateJudgeAnswer(exercise);
        } else {
          inflateTextChoiceAnswer(exercise);
        }
        break;
      case "图片":
        inflateImgAnswer(exercise);
        break;
    }
  }

  private void inflateJudgeAnswer(Exercise exercise) {
    ViewGroup viewGroup = addExerciseView(R.layout.rv_item_exercise_answer_jduge_choice);
    ImageView ivCorrect = viewGroup.findViewById(R.id.iv_correct);
    ImageView ivError = viewGroup.findViewById(R.id.iv_error);
    ImageView ivCorrectBg = viewGroup.findViewById(R.id.iv_correct_bg);
    ImageView ivErrorBg = viewGroup.findViewById(R.id.iv_error_bg);

    if (bookManager != null && bookManager.isHomeWorkWatch()) {
      ivCorrect.setEnabled(false);
      ivError.setEnabled(false);

      WatchPic watchPic = ((ExerciseChallengeActivity) activity).getWatchPic();
      if (watchPic != null && watchPic.errors != null) {
        for (WatchPic.Errors error : watchPic.errors) {
          if (exercise.id.equals(error.exercise_id + "")) {
            if (TextUtils.equals("T", error.user_answer) || TextUtils.equals("Y", error.user_answer)) {
              ivCorrectBg.setVisibility(View.VISIBLE);
              ivCorrectBg.setImageResource(R.drawable.bg_judge_error);
            } else if (TextUtils.equals("F", error.user_answer) || TextUtils.equals("N", error.user_answer)) {
              ivErrorBg.setVisibility(View.VISIBLE);
              ivErrorBg.setImageResource(R.drawable.bg_judge_error);
            }
          }
        }
      }

      for (Option option : exercise.options) {
        if (TextUtils.equals("T", option.content) || TextUtils.equals("Y", option.content)) {
          if (option.is_correct) {
            ivCorrectBg.setVisibility(View.VISIBLE);
            ivCorrectBg.setImageResource(R.drawable.bg_judge_correct);
          }
        }
        if (TextUtils.equals("F", option.content) || TextUtils.equals("N", option.content)) {
          if (option.is_correct) {
            ivErrorBg.setVisibility(View.VISIBLE);
            ivErrorBg.setImageResource(R.drawable.bg_judge_correct);
          }
        }
      }
    }

    ivCorrect.setOnClickListener(v -> {
      ivCorrect.setClickable(false);
      ivError.setClickable(false);
      for (Option option : exercise.options) {
        if (TextUtils.equals("T", option.content) || TextUtils.equals("Y", option.content)) {
          if (option.is_correct) {
            exercise.answerCorrect = true;
            exercise.user_answer = option.content;
            playSound(successId);
            ivCorrectBg.setVisibility(View.VISIBLE);
            ivCorrectBg.setImageResource(R.drawable.bg_judge_correct);
            ivCorrectBg.postDelayed(() -> nextExercise(exercise), 500);
          } else {
            ivCorrectBg.postDelayed(() -> {
              if (ivCorrectBg != null) {
                ivCorrectBg.setVisibility(View.VISIBLE);
                ivCorrectBg.setImageResource(R.drawable.bg_judge_error);
              }
            }, 300);
            exercise.answerCorrect = false;
            exercise.user_answer = option.content;
            playSound(wrongId);
            ivErrorBg.setVisibility(View.VISIBLE);
            ivErrorBg.setImageResource(R.drawable.bg_judge_correct);
            ivErrorBg.postDelayed(() -> nextExercise(exercise), 1000);
          }
        }
      }
    });
    ivError.setOnClickListener(v -> {
      ivCorrect.setClickable(false);
      ivError.setClickable(false);
      for (Option option : exercise.options) {
        if (TextUtils.equals("F", option.content) || TextUtils.equals("N", option.content)) {
          if (option.is_correct) {
            exercise.answerCorrect = true;
            exercise.user_answer = option.content;
            playSound(successId);
            ivErrorBg.setVisibility(View.VISIBLE);
            ivErrorBg.postDelayed(() -> {
              if (ivErrorBg != null) {
                nextExercise(exercise);
              }
            }, 500);
            ivErrorBg.setImageResource(R.drawable.bg_judge_correct);
          } else {
            exercise.answerCorrect = false;
            exercise.user_answer = option.content;
            playSound(wrongId);
            ivErrorBg.setVisibility(View.VISIBLE);
            ivErrorBg.setImageResource(R.drawable.bg_judge_error);
            ivCorrectBg.postDelayed(() -> {
              if (ivCorrectBg != null) {
                ivCorrectBg.setVisibility(View.VISIBLE);
                ivCorrectBg.setImageResource(R.drawable.ic_judge_correct_side);
              }
            }, 300);
            ivError.postDelayed(() -> nextExercise(exercise), 1000);
          }
        }
      }
    });
  }

  private void nextExercise(Exercise exercise) {
    if (exercise == null) {
      return;
    }
    stopAudio();
    int position = mExerciseList.indexOf(exercise);
    if (position + 1 < mExerciseList.size()) {
      mActivity.setCurrentExercise(mExerciseList.get(position + 1));
    }
    if (position == mExerciseList.size() - 1) {
      mActivity.getData().data.duration = mActivity.getData().data.duration - duration;
      mActivity.enterChallengeResult();
    } else {
      if (mDsvExercise.getCurrentItem() < mViewQuestions.size() - 1) {
        Log.e("shenxj", "mDsvExercise.getCurrentItem() next= " + mDsvExercise.getCurrentItem());
        mCurrentExercisePosition = mDsvExercise.getCurrentItem() + 1;
        mDsvExercise.smoothScrollToPosition(mCurrentExercisePosition);
      } else {
        mActivity.getData().data.duration = mActivity.getData().data.duration - duration;
        mActivity.enterChallengeResult();
      }
    }
  }

  private void lastExercise(Exercise exercise) {
    if (exercise == null) {
      return;
    }
    stopAudio();
    int position = mExerciseList.indexOf(exercise);
    if (position - 1 >= 0) {
      mActivity.setCurrentExercise(mExerciseList.get(position - 1));
    }
    if (mDsvExercise.getCurrentItem() - 3 >= 0) {
      Log.e("shenxj", "mDsvExercise.getCurrentItem() last= " + mDsvExercise.getCurrentItem());
      mCurrentExercisePosition = mDsvExercise.getCurrentItem() - 3;
      mDsvExercise.smoothScrollToPosition(mCurrentExercisePosition);
    }
  }

  private void inflateTextChoiceAnswer(Exercise exercise) {
    ViewGroup viewGroup = addExerciseView(R.layout.exercise_answer_text_choice);
    RecyclerView recyclerView = viewGroup.findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(activity));
    recyclerView.setHasFixedSize(true);
    recyclerView.setNestedScrollingEnabled(false);
    class TextChoiceViewHolder extends ViewHolder {

      private TextView tvText;
      private View viewCorrectFlag, viewErrorFlag;
      private ImageView ivCorrectFlag;
      private LinearLayout tv_text_layout;

      public TextChoiceViewHolder(View itemView) {
        super(itemView);
        tvText = itemView.findViewById(R.id.tv_text);
        viewCorrectFlag = itemView.findViewById(R.id.view_correct_flag);
        viewErrorFlag = itemView.findViewById(R.id.view_error_flag);
        ivCorrectFlag = itemView.findViewById(R.id.iv_correct_flag);
        tv_text_layout = itemView.findViewById(R.id.tv_text_layout);
      }
    }
    recyclerView.setAdapter(new RecyclerView.Adapter<TextChoiceViewHolder>() {

      private boolean hasAnswer;

      @NonNull
      @Override
      public TextChoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.rv_item_exercise_answer_text_choice, null);
        return new TextChoiceViewHolder(view);
      }

      @Override
      public void onBindViewHolder(@NonNull TextChoiceViewHolder holder, int position) {
        Option option = exercise.options.get(position);
        holder.tvText.setText(Html.fromHtml(option.content.replaceAll("\n", "<br>")));
        if (bookManager != null && bookManager.isHomeWorkWatch()) {
          WatchPic watchPic = ((ExerciseChallengeActivity) activity).getWatchPic();
          if (watchPic != null && watchPic.errors != null) {
            for (WatchPic.Errors error : watchPic.errors) {
              if (error.user_answer.equals(option.content) && exercise.id.equals(error.exercise_id + "")) {
                holder.viewErrorFlag.setVisibility(View.VISIBLE);
              }
            }
          }
          if (option.is_correct) {
            holder.viewCorrectFlag.setVisibility(View.VISIBLE);
          }
          holder.itemView.setEnabled(false);
        }

        holder.tv_text_layout.setOnClickListener(v -> {
          if (hasAnswer) {
            return;
          }
          hasAnswer = true;
          exercise.user_answer = option.content;
          if (option.is_correct) {
            exercise.answerCorrect = true;
            playSound(successId);
            holder.viewCorrectFlag.setVisibility(View.VISIBLE);
            holder.viewCorrectFlag.postDelayed(() -> nextExercise(exercise), 500);
          } else {
            playSound(wrongId);
            holder.viewErrorFlag.setVisibility(View.VISIBLE);
            holder.viewErrorFlag.postDelayed(() -> {
              if (holder.viewErrorFlag != null) {
                for (int i = 0; i < exercise.options.size(); i++) {
                  if (exercise.options.get(i).is_correct) {
                    notifyItemChanged(i, true);
                  }
                }
              }
            }, 300);
            holder.viewErrorFlag.postDelayed(() -> nextExercise(exercise), 1000);
          }
        });
      }

      @Override
      public void onBindViewHolder(@NonNull TextChoiceViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (!payloads.isEmpty()) {
          holder.ivCorrectFlag.setVisibility(View.VISIBLE);
        }
      }

      @Override
      public int getItemCount() {
        return exercise.options.size();
      }
    });
  }

  private void inflateImgAnswer(Exercise exercise) {
    View viewGroup = addExerciseView(R.layout.exercise_answer_image_options);
    RecyclerView recyclerView = viewGroup.findViewById(R.id.recycler_view);
    recyclerView.setHasFixedSize(true);
    recyclerView.setNestedScrollingEnabled(false);
    if (exercise.options.size() > 4) {
      recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));
      recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, Utils.dp2px(activity, 15), false));
    } else {
      recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
      recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, Utils.dp2px(activity, 15), false));
    }
    recyclerView.setAdapter(new RecyclerView.Adapter<ItemViewHolder>() {
      private boolean hasAnswer;

      @NonNull
      @Override
      public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.rv_item_exercise_answer_word_challenge_choice, null);
        if (exercise.options.size() <= 4) {
          View childView = view.findViewById(R.id.iv_option);
          childView.getLayoutParams().width = Utils.dp2px(parent.getContext(), 124);
        }
        return new ItemViewHolder(view);
      }

      @Override
      public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Option item = exercise.options.get(position);
        GlideUtil.loadImage(activity, item.content, holder.ivOption);
        holder.ivFlag.setVisibility(GONE);
        if (bookManager != null && bookManager.isHomeWorkWatch()) {
          if (holder.ivFlag != null) {
            WatchPic watchPic = ((ExerciseChallengeActivity) activity).getWatchPic();
            if (watchPic != null && watchPic.errors != null) {
              for (WatchPic.Errors error : watchPic.errors) {
                if (error.user_answer.equals(item.content) && exercise.id.equals(error.exercise_id + "")) {
                  holder.ivFlag.setVisibility(View.VISIBLE);
                  holder.ivFlag.setImageResource(R.drawable.ic_error_img_flag);
                }
              }
            }
            if (item.is_correct) {
              holder.ivFlag.postDelayed(() -> {
                holder.ivFlag.setVisibility(View.VISIBLE);
                holder.ivFlag.setImageResource(R.drawable.ic_correct_img_flag_1);
              }, 300);

//              if (holder.ivFlag2.getVisibility() == View.GONE) {
//                holder.ivFlag2.postDelayed(() -> {
//                  notifyItemChanged(position, true);
//                }, 300);
//              }
            }
          }
          holder.itemView.setEnabled(false);
        }
        holder.itemView.setOnClickListener(v -> {
          if (hasAnswer) {
            return;
          }
          holder.ivFlag.setVisibility(View.VISIBLE);
          hasAnswer = true;
          exercise.user_answer = item.content;
          if (item.is_correct) {
            exercise.answerCorrect = true;
            playSound(successId);
            holder.ivFlag.setImageResource(R.drawable.ic_correct_img_flag_1);
            holder.ivFlag.postDelayed(() -> nextExercise(exercise), 500);
          } else {
            playSound(wrongId);
            holder.ivFlag.postDelayed(() -> {
              if (holder.ivFlag != null) {
                for (int i = 0; i < exercise.options.size(); i++) {
                  if (exercise.options.get(i).is_correct) {
                    notifyItemChanged(i, true);
                  }
                }
              }
            }, 300);
            holder.ivFlag.setImageResource(R.drawable.ic_error_img_flag);
            holder.ivFlag.postDelayed(() -> nextExercise(exercise), 1000);
          }
        });
      }

      @Override
      public void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (!payloads.isEmpty()) {
          holder.ivFlag2.setVisibility(View.VISIBLE);
          holder.ivFlag2.setImageResource(R.drawable.ic_correct_img_flag_2);
        }

      }

      @Override
      public int getItemCount() {
        return exercise.options == null ? 0 : exercise.options.size();
      }

    });

  }

  @Override
  public void onResume() {
    super.onResume();
    if (mCountDownTimer == null) {
      initCutDown(duration * 1000);
    }
  }

  //将布局添加到答题界面
  protected ViewGroup addExerciseView(int resId) {
    ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext())
        .inflate(resId, mLayoutExercise, true);
    return viewGroup;
  }

  class ItemViewHolder extends ViewHolder {

    public ImageView ivOption, ivFlag, ivFlag2;

    public ItemViewHolder(View itemView) {
      super(itemView);
      ivOption = itemView.findViewById(R.id.iv_option);
      ivFlag = itemView.findViewById(R.id.iv_flag);
      ivFlag2 = itemView.findViewById(R.id.iv_flag_2);
    }
  }

  class ItemViewHolderSort extends ViewHolder {

    public ImageView ivOption, ivFlag, ivFlag2;
    public TextView index;

    public ItemViewHolderSort(View itemView) {
      super(itemView);
      ivOption = itemView.findViewById(R.id.iv_option);
      index = itemView.findViewById(R.id.answer_num);
      ivFlag = itemView.findViewById(R.id.iv_flag);
      ivFlag2 = itemView.findViewById(R.id.iv_flag_2);
    }
  }

  class ExerciseViewHolder extends ViewHolder {
    public ExerciseViewHolder(View itemView) {
      super(itemView);
    }
  }


  class ExerciseAdapter extends RecyclerView.Adapter {
    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = new FrameLayout(parent.getContext());
      view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
      ExerciseViewHolder holder = new ExerciseViewHolder(view);
      holder.setIsRecyclable(false);
      return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      if (position != mCurrentExercisePosition) {
        return;
      }
      ViewGroup viewGroup = (ViewGroup) holder.itemView;
      viewGroup.removeAllViews();
      if (mViewQuestions.get(position).getParent() != null) {
        ViewGroup view = (ViewGroup) mViewQuestions.get(position).getParent();
        view.removeAllViews();
      }
      viewGroup.addView(mViewQuestions.get(position));
      if (position % 2 == 0 && position < mViewQuestions.size()) {
        new RxTimerUtil().timer(1500, new RxTimerUtil.IRxNext() {
          @Override
          public void onTick(Long s) {

          }

          @Override
          public void onFinish() {
            if (isVisible()) {
              if (position + 1 <= mViewQuestions.size() - 1) {
                mCurrentExercisePosition = position + 1;
                mDsvExercise.smoothScrollToPosition(position + 1);
              }
            }
          }
        });
      }
    }

    @Override
    public int getItemCount() {
      return mViewQuestions.size();
    }
  }

  AnswerProgressListener mAnswerProgressListener;

  public void setAnswerProgressListener(AnswerProgressListener answerProgressListener) {
    mAnswerProgressListener = answerProgressListener;
  }

  interface AnswerProgressListener {
    void answerProgress(int position);
  }

  interface OnStartAnswerCallBack {
    void onStartAnswer();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mCountDownTimer != null) {
      mCountDownTimer.cancel();
    }
    if (soundPool != null) {
      soundPool.release();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    stopAudio();
    if (mCountDownTimer != null) {
      mCountDownTimer.cancel();
      mCountDownTimer = null;
    }
  }
}
