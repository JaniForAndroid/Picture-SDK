package com.example.picsdk.music;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.exoaudioplayer.aduio.base.AbstractAudioPlayer;
import com.example.exoaudioplayer.aduio.base.AudioCallBack;
import com.example.exoaudioplayer.aduio.base.AudioPlayerFactory;
import com.example.picsdk.PicLoadingActivity;
import com.example.picsdk.R;
import com.example.picsdk.ReadBookActivity;
import com.example.picsdk.ResultActivity;
import com.example.picsdk.base.BaseActivity;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.util.AppPicUtil;
import com.example.picsdk.util.BarUtil;
import com.google.android.exoplayer2.Player;
import com.namibox.commonlib.constant.Events;
import com.namibox.commonlib.dialog.NamiboxNiceDialog;
import com.namibox.commonlib.lockscreen.AudioPlayEvent;
import com.namibox.greendao.entity.AudioInfo;
import com.namibox.tools.ThinkingAnalyticsHelper;
import com.namibox.tools.ViewUtil;
import com.namibox.util.Logger;
import com.othershe.nicedialog.BaseNiceDialog;
import com.othershe.nicedialog.ViewConvertListener;
import com.othershe.nicedialog.ViewHolder;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

public class MusicPicActivity extends BaseActivity implements View.OnClickListener {

  boolean hasInitPlayer = false;
  private boolean isPlaying;
  private NamiboxNiceDialog musicPlayListDialog;
  String pageName = "音频播放器全屏";
  String albumTitle = "";
  String audioTitle = "";
  private AbstractAudioPlayer exoAudioPlayer;
  private BookManager bookManager;

  TextView tvMusicTitle;
  TextView tvPlayConut;
  SeekBar musicSeekBar;
  View bg_sbar;
  TextView tvCurrentTime, tv_skip_pic;
  TextView tvTotalTime;
  ImageView ivPlay;
  ImageView ivPre;
  ImageView ivNext;
  ImageView ivMusicList;
  TextView tvSeekUserTime;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bookManager = BookManager.getInstance();
    setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);
    setContentView(R.layout.activity_music_play);
    initView();
    continuePlaying = getIntent().getBooleanExtra("continuePlay", false);
    bg_sbar.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent event) {
        Rect seekRect = new Rect();
        musicSeekBar.getHitRect(seekRect);

        if ((event.getY() >= (seekRect.top - 100)) && (event.getY() <= (seekRect.bottom + 100))) {

          float y = seekRect.top + seekRect.height() / 2;
          //seekBar only accept relative x
          float x = event.getX() - seekRect.left;
          if (x < 0) {
            x = 0;
          } else if (x > seekRect.width()) {
            x = seekRect.width();
          }
          MotionEvent me = MotionEvent.obtain(event.getDownTime(), event.getEventTime(),
              event.getAction(), x, y, event.getMetaState());
          return musicSeekBar.onTouchEvent(me);
        }
        return false;
      }
    });

    initActionBar();
    initExoAudioPlayer();
    if (bookManager != null)
      setActionTitle(bookManager.getLesson_name());
    initUI();
    expandTouchView();
  }

  public void initView() {
    tvMusicTitle = findViewById(R.id.tvMusicTitle);
    tvPlayConut = findViewById(R.id.tvPlayConut);
    musicSeekBar = findViewById(R.id.music_seek_bar);
    bg_sbar = findViewById(R.id.bg_sbar);
    tvCurrentTime = findViewById(R.id.tvCurrentTime);
    tvTotalTime = findViewById(R.id.tvTotalTime);
    ivPlay = findViewById(R.id.ivPlay);
    ivPre = findViewById(R.id.ivPre);
    ivNext = findViewById(R.id.ivNext);
    ivMusicList = findViewById(R.id.ivMusicList);
    tvSeekUserTime = findViewById(R.id.tvSeekUserTime);
    tv_skip_pic = findViewById(R.id.tv_skip_pic);

    ivPlay.setOnClickListener(this);
    ivPre.setOnClickListener(this);
    ivNext.setOnClickListener(this);
    ivMusicList.setOnClickListener(this);
    tv_skip_pic.setOnClickListener(v -> skip());
  }

  private void skip() {
    Intent intent = new Intent(this, ResultActivity.class);
    intent.putExtra(ResultActivity.ARG_LINK, PicLoadingActivity.BOOK_LINKS_MUSIC);
    startActivity(intent);
    finish();
  }

  private void initExoAudioPlayer() {
    exoAudioPlayer = AudioPlayerFactory.getInstance().createPlayer(getApplicationContext(), com.example.exoaudioplayer.aduio.base.Constants.EXO);
    exoAudioPlayer.setPlayerCallBack(new AudioCallBack() {
      @Override
      public void playUpdate(long currentTime, long bufferTime, long totalTime) {
        if (!isTracking) {
          musicSeekBar.setProgress((int) currentTime);
        }
        musicSeekBar.setMax((int) totalTime);
        musicSeekBar.setSecondaryProgress((int) bufferTime);
        tvCurrentTime.setText(AppPicUtil.formatTime(currentTime));
        tvTotalTime.setText(AppPicUtil.formatTime(totalTime));
      }

      @Override
      public void playStateChange(boolean playWhenReady, int playbackState) {
        super.playStateChange(playWhenReady, playbackState);
        Log.e("shenxj", "playWhenReady = " + playWhenReady + "-------" + "playbackState = " + playbackState);
        if (playbackState == Player.STATE_ENDED
            || playbackState == Player.STATE_IDLE || !playWhenReady) {
          ivPlay.setImageResource(R.drawable.zky_pause_icon);
        } else if (playbackState == Player.STATE_READY && playWhenReady) {
          ivPlay.setImageResource(R.drawable.zky_play_icon);
        }

        if (playbackState == Player.STATE_ENDED && playWhenReady) {
          if (bookManager == null)
            return;

          AudioInfo preAudio = bookManager.getNextAudio();
          if (preAudio != null) {
            bookManager.setCurrentAudio(preAudio);
            initPlayUI();
            if (adapter != null)
              adapter.notifyDataSetChanged();
          } else {
            Intent intent = new Intent(MusicPicActivity.this, ResultActivity.class);
            intent.putExtra("milesson_item_id", bookManager.getMilesson_item_id());
            intent.putExtra(ResultActivity.ARG_LINK, PicLoadingActivity.BOOK_LINKS_MUSIC);
            startActivity(intent);
            finish();
          }
        }
      }
    });
  }


  private void expandTouchView() {
    ViewUtil.expandTouchArea(ivMusicList, 20);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    return super.dispatchTouchEvent(ev);
  }

  private void initUI() {
    musicSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    bookManager.setCurrentAudio(bookManager.getPlaylist().get(0));
    initPlayUI();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  private boolean isTracking;
  private long mLastSeekEventTime;
  private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      if (fromUser && isTracking) {
        tvSeekUserTime.setVisibility(View.VISIBLE);
        float x = musicSeekBar.getX() + musicSeekBar.getWidth() * 1.0f * progress / seekBar
            .getMax() - tvSeekUserTime.getWidth() / 2f;
        Logger.d("--zkyml", "omusicSeekBar.x :" + x);
        tvSeekUserTime.setX(x);
        tvSeekUserTime.setText(
            AppPicUtil.formatTime(musicSeekBar.getProgress()) + "/" + AppPicUtil
                .formatTime(musicSeekBar.getMax()));
      }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
      isTracking = true;
      Logger.d("--zkyml", "onStartTrackingTouch");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
      tvSeekUserTime.setVisibility(View.GONE);
      isTracking = false;
      long now = SystemClock.elapsedRealtime();
      if (now - mLastSeekEventTime > 100) {
        mLastSeekEventTime = now;
        int p = musicSeekBar.getProgress();
        Logger.d("--zkyml", "stoptrack:" + p);
        exoAudioPlayer.seekTo(p);
        tvCurrentTime.setText(AppPicUtil.formatTime(p));

        AudioInfo currentAudio = bookManager.getCurrentAudio();
        if (currentAudio != null) {
          currentAudio.progress = p;
//          AudioManager.getInstance().updateAudio(currentAudio);
        }
      }
    }
  };

  /**
   * 播放Audio以及相关界面相关UI
   */
  private void initPlayUI() {
    AudioInfo audioInfo = bookManager.getCurrentAudio();
    if (audioInfo == null) {
      toast("数据错误");
      finish();
      return;
    }
    audioTitle = audioInfo.title;
    tvMusicTitle.setText(audioTitle);
    tvPlayConut.setText(audioInfo.content);

    if (!hasInitPlayer
        && exoAudioPlayer.getPlayer() != null
        && (exoAudioPlayer.getPlayer().getPlaybackState() == Player.STATE_READY
        || exoAudioPlayer.getPlayer().getPlaybackState() == Player.STATE_IDLE)) {
      if (exoAudioPlayer.getPlayer().getPlayWhenReady()) {
        ivPlay.setImageResource(R.drawable.zky_play_icon);
        isPlaying = true;
      } else {
        isPlaying = false;
        ivPlay.setImageResource(R.drawable.zky_pause_icon);
      }
      long duration = exoAudioPlayer.getPlayer().getDuration();
      long currentPosition = exoAudioPlayer.getPlayer().getCurrentPosition();
      musicSeekBar.setMax((int) duration);
      musicSeekBar.setProgress((int) currentPosition);
      tvCurrentTime.setText(AppPicUtil.formatTime(currentPosition));
      tvTotalTime.setText(AppPicUtil.formatTime(duration));
    } else {
      isPlaying = true;
      ivPlay.setImageResource(R.drawable.zky_play_icon);
    }
    exoAudioPlayer.play(Uri.parse(audioInfo.media_url));
    bookManager.setCurrentAudio(audioInfo);

    if (continuePlaying) {
      exoAudioPlayer.seekTo(audioInfo.progress * audioInfo.duration * 1000 / 100);
    }
    continuePlaying = false;
    hasInitPlayer = true;
    setNextPreStatus();
  }

  boolean continuePlaying = false;

  public void onClick(View view) {
    if (view.getId() == R.id.ivPlay) {
      play();
    } else if (view.getId() == R.id.ivPre) {
      playPre();
    } else if (view.getId() == R.id.ivNext) {
      playNext();
    } else if (view.getId() == R.id.ivMusicList) {
      showMusicListDialog();
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }

  private AudioInfo getAudioByAudioId(List<AudioInfo> playlist, String audioId) {
    for (AudioInfo audio : playlist) {
      if (TextUtils.equals(audio.audioId, audioId)) {
        return audio;
      }
    }
    return null;
  }

  MultiTypeAdapter adapter;

//  @Subscribe(threadMode = ThreadMode.MAIN)
//  public void updatePlayState(AudioPlayStateEvent event) {
//    List<AudioInfo> playlist = bookManager.getPlaylist();
//    if (playlist != null && playlist.size() > 0 && musicPlayListDialog != null
//        && musicPlayListDialog.isResumed() && adapter != null) {
//      AudioInfo audio = getAudioByAudioId(playlist, event.audioId);
//      if (audio == null) {
//        return;
//      }
//      int index = adapter.getItems().indexOf(audio);
//      if (event.progress == -1) {
//        adapter.notifyItemChanged(index, AudioChangeState.STATE_PLAY);
//      } else {
//        adapter.notifyItemChanged(index, AudioChangeState.STATE_PROGRESS);
//      }
//    }
//  }

  /**
   * 底部弹出音乐列表弹窗
   */
  private void showMusicListDialog() {
    if (musicPlayListDialog != null) {
      musicPlayListDialog.dismissAllowingStateLoss();
    }

    musicPlayListDialog = (NamiboxNiceDialog) NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_bottom_music_list)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            RecyclerView recyclerView = holder.getView(R.id.dialog_list);
            TextView tv_num = holder.getView(R.id.tv_num);
            recyclerView.setLayoutManager(new LinearLayoutManager(dialog.getContext()));
            adapter = new MultiTypeAdapter();
            adapter
                .register(AudioInfo.class, new AudioHistoryViewBinder(new OnAudioClickListener() {
                  @Override
                  public void onAudioPlay(AudioInfo audioInfo) {
                    if (!bookManager.getCurrentAudio().audioId
                        .equals(audioInfo.audioId)) {
                      bookManager.setCurrentAudio(audioInfo);
                      initPlayUI();
                      musicPlayListDialog.dismissAllowingStateLoss();
                    }
                  }

                  @Override
                  public void onAudioDownload(AudioInfo audioInfo) {

                  }
                }));

            Items items = new Items();
            List<AudioInfo> playlist = bookManager.getPlaylist();
            if (playlist != null && playlist.size() > 0) {
              tv_num.setText("共" + playlist.size() + "首");
              items.addAll(playlist);
              adapter.setItems(items);
              recyclerView.setAdapter(adapter);
            } else {
            }
            ImageView ivCloseDialog = holder.getView(R.id.ivCloseDialog);
            ViewUtil.expandTouchArea(ivCloseDialog, 20);
            ivCloseDialog.setOnClickListener(view -> dialog.dismissAllowingStateLoss());
          }
        })
        .setShowBottom(true)
        .setMargin(0)
        .show(getSupportFragmentManager());
    musicPlayListDialog.setCancelListener(() -> adapter = null);
  }

  /**
   * 播放暂停轮换
   */
  private void play() {
    if (exoAudioPlayer != null) {
      if (!exoAudioPlayer.getPlayer().getPlayWhenReady()
          && exoAudioPlayer.getPlayer().getPlaybackState() == Player.STATE_ENDED
          && exoAudioPlayer.getPlayer().getRepeatMode() == Player.REPEAT_MODE_OFF) {
        exoAudioPlayer.seekTo(0);
      }
      exoAudioPlayer.playPause();
      isPlaying = exoAudioPlayer.getPlayer().getPlayWhenReady();
      EventBus.getDefault().post(new AudioPlayEvent(isPlaying ?
          AudioPlayEvent.PLAY : AudioPlayEvent.PAUSE, AudioPlayEvent.AUDIO_MUSIC_PLAY));
      ivPlay.setImageResource(isPlaying ? R.drawable.zky_play_icon : R.drawable.zky_pause_icon);
    }
  }

  /**
   * 上一曲
   */
  private void playPre() {
    AudioInfo preAudio = bookManager.getPreAudio();
    if (preAudio != null) {
      bookManager.setCurrentAudio(preAudio);
      initPlayUI();
    }
  }

  /**
   * 下一曲
   */
  private void playNext() {
    AudioInfo nextAudio = bookManager.getNextAudio();
    if (nextAudio != null) {
      bookManager.setCurrentAudio(nextAudio);
      initPlayUI();
    }
  }

  /**
   * 设置上一曲下一曲按钮状态
   * 触发条件:切换播放音乐/切换播放模式
   */
  private void setNextPreStatus() {
    AudioInfo nextAudio = bookManager.getNextAudio();
    ivNext.setImageResource(
        nextAudio == null ? R.drawable.zky_next_music_dis : R.drawable.zky_next_music);
    ivNext.setEnabled(nextAudio != null);
    AudioInfo preAudio = bookManager.getPreAudio();
    ivPre.setImageResource(
        preAudio == null ? R.drawable.zky_pre_music_dis : R.drawable.zky_pre_music);
    ivPre.setEnabled(preAudio != null);
    EventBus.getDefault().post(
        new AudioPlayEvent(nextAudio != null ?
            AudioPlayEvent.NEXT_ENABLED : AudioPlayEvent.NEXT_UNENABLED,
            AudioPlayEvent.AUDIO_MUSIC_PLAY));
    EventBus.getDefault().post(
        new AudioPlayEvent(preAudio != null ?
            AudioPlayEvent.PREVIOUS_ENABLED : AudioPlayEvent.PREVIOUS_UNENABLED,
            AudioPlayEvent.AUDIO_MUSIC_PLAY));
  }

  /**
   * 跳转到专辑跳回
   */
  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    AudioInfo currentAudio = bookManager.getCurrentAudio();
    if (currentAudio != null && currentAudio.audioId.equals(bookManager.getCurrentAudio().audioId)) {

    } else {
      initPlayUI();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (exoAudioPlayer != null) {
      if (isPlaying)
        exoAudioPlayer.playPause();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (isPlaying)
      exoAudioPlayer.playPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (exoAudioPlayer != null) {
      exoAudioPlayer.releasePlayer();
      exoAudioPlayer.setPlayerCallBack(null);
      exoAudioPlayer = null;
    }
  }
}
