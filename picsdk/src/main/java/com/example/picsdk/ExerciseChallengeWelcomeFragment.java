package com.example.picsdk;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.picsdk.base.BaseFragment;
import com.example.picsdk.util.AppPicUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseChallengeWelcomeFragment extends BaseFragment {
  private ProgressBar progressBar;
  private static final int MSG_UPDATE = 0x100;

  public ExerciseChallengeWelcomeFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_exercise_challenge_welcome, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ImageView ivType = view.findViewById(R.id.iv_type);
    TextView tvTitle = view.findViewById(R.id.tv_title);
    progressBar = view.findViewById(R.id.progress_loading);
    handler.sendEmptyMessage(MSG_UPDATE);
    ExerciseChallengeActivity exerciseChallengeActivity = (ExerciseChallengeActivity) activity;
    if (TextUtils.equals(exerciseChallengeActivity.getExerciseType(), AppPicUtil.CHALLENGE_READ)) {
      ivType.setImageResource(R.drawable.cover_read_understand_1);
      tvTitle.setText(getString(R.string.book_progress_cover_title2));
    }
  }

  private Handler handler = new Handler(){
    public void handleMessage(android.os.Message msg) {
      if (msg.what == MSG_UPDATE) {
        int progress = progressBar.getProgress();
        if (progress < 100) {
          progress ++;
          handler.sendEmptyMessageDelayed(MSG_UPDATE, 30);
        } else {
          handler.removeMessages(MSG_UPDATE);
        }
        progressBar.setProgress(progress);
      }
    };
  };

  @Override
  public void onDestroy() {
    super.onDestroy();
    handler.removeCallbacksAndMessages(null);
  }
}
