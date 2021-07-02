package com.namibox.commonlib.lockscreen;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.picsdk.R;
import com.namibox.util.PreferenceUtil;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LockScreenActivity extends AppCompatActivity {

  private View mMoveView;
  private TextView mTvAudioName;
  private ImageView mIvCover;
  private SeekBar mSeekBar;
  private ImageView mIvPlayPause;
  private TextView mTvCurrentTime;
  private TextView mTvTotalTime;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventBus.getDefault().register(this);
    getWindow().addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD
        | LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      getWindow().addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      getWindow().setStatusBarColor(0x00000000);
    }
    setContentView(R.layout.activity_lock_screen);
    UnderView underView = findViewById(R.id.under_view);
    mMoveView = findViewById(R.id.rl_move);
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    bitmap.eraseColor(0xaa000000);
    mMoveView.setBackground(new BitmapDrawable(bitmap));
    mTvAudioName = findViewById(R.id.tv_audio_name);
    mIvCover = findViewById(R.id.iv_cover);
    mSeekBar = findViewById(R.id.seek_bar);
    mSeekBar.setClickable(false);
    mSeekBar.setEnabled(false);
    mSeekBar.setSelected(false);
    mSeekBar.setFocusable(false);
    mSeekBar.setMax(1000);
    mIvPlayPause = findViewById(R.id.iv_play_pause);
    mTvCurrentTime = findViewById(R.id.tv_current_time);
    mTvTotalTime = findViewById(R.id.tv_total_time);
    underView.setMoveView(mMoveView);
    EventBus.getDefault().post(new AudioPlayEvent(AudioPlayEvent.LOCK_READY, 0));
  }

  public void onPrevious(View view) {
    EventBus.getDefault().post(new AudioPlayEvent(AudioPlayEvent.PREVIOUS, 0));
  }

  public void onPlayPause(View view){
    EventBus.getDefault().post(new AudioPlayEvent(AudioPlayEvent.PLAY_PAUSE, 0));
  }

  public void onNext(View view) {
    EventBus.getDefault().post(new AudioPlayEvent(AudioPlayEvent.NEXT, 0));
  }

  @Override
  public void onBackPressed() {
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void audioPlayEvent(AudioPlayEvent audioPlayEvent) {
    if (audioPlayEvent.audioType != PreferenceUtil.getAudioPlayPage(this)) {
      return;
    }
    switch (audioPlayEvent.getOperation()) {
      case AudioPlayEvent.PLAY:
        mIvPlayPause.setImageResource(R.drawable.nb_ic_pause);
        break;
      case AudioPlayEvent.PAUSE:
        mIvPlayPause.setImageResource(R.drawable.nb_ic_play);
        break;
      case AudioPlayEvent.FIRST_INIT:
        mIvPlayPause.setImageResource(audioPlayEvent.isPlaying? R.drawable.nb_ic_pause : R.drawable.nb_ic_play);
        break;
      case AudioPlayEvent.PREVIOUS_UNENABLED:
        ((ImageView) findViewById(R.id.iv_previous)).setImageResource(R.drawable.ic_previous_unenabled);
        break;
      case AudioPlayEvent.NEXT_UNENABLED:
        ((ImageView) findViewById(R.id.iv_next)).setImageResource(R.drawable.ic_next_unenabled);
        break;
      case AudioPlayEvent.PREVIOUS_ENABLED:
        ((ImageView) findViewById(R.id.iv_previous)).setImageResource(R.drawable.ic_previous);
        break;
      case AudioPlayEvent.NEXT_ENABLED:
        ((ImageView) findViewById(R.id.iv_next)).setImageResource(R.drawable.ic_next);
        break;
      case AudioPlayEvent.AUDIO_INFO:
        if (audioPlayEvent.bitmapCover != null) {
          mMoveView.setBackground(new BitmapDrawable(EaseBlurUtils.blurBitmap(cropBitmap(audioPlayEvent.bitmapCover))));
          mIvCover.setImageBitmap(cropBitmap(audioPlayEvent.bitmapCover));
        }
        if (!TextUtils.isEmpty(audioPlayEvent.audioName)) {
          mTvAudioName.setText(audioPlayEvent.audioName);
        }
        if (!TextUtils.isEmpty(audioPlayEvent.currentTime)) {
          mTvCurrentTime.setText(audioPlayEvent.currentTime);
        }
        if (!TextUtils.isEmpty(audioPlayEvent.totalTime)) {
          mTvTotalTime.setText(audioPlayEvent.totalTime);
        }
        mSeekBar.setProgress(audioPlayEvent.progress);
        break;
      default:
         break;
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  private Bitmap cropBitmap(Bitmap bitmap) {
    int minLength = bitmap.getHeight() > bitmap.getWidth() ? bitmap.getWidth() : bitmap.getHeight();
    return Bitmap.createBitmap(bitmap, 0, 0, minLength, minLength, null, false);
  }
}
