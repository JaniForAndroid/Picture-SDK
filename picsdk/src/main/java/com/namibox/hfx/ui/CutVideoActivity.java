package com.namibox.hfx.ui;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.picsdk.R;
import com.google.android.exoplayer.lib.PlayerView;
import com.google.android.exoplayer2.ExoPlayer;
import com.namibox.hfx.bean.MatchInfo;
import com.namibox.hfx.bean.VideoInfo;
import com.namibox.hfx.utils.HfxFileUtil;
import com.namibox.hfx.utils.HfxPreferenceUtil;
import com.namibox.hfx.utils.HfxUtil;
import com.namibox.hfx.view.PressAndTapLinearLayout;
import com.namibox.hfx.view.RangeSeekBar;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.Utils;
import io.microshow.rxffmpeg.FfmpegUtil;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;
import java.io.File;

/**
 * Created by sunha on 2015/12/25 0025.
 */
public class CutVideoActivity extends AbsExoActivity {

  public final static String VIDEO_ID = "videoId";
  public final static String VIDEO_PATH = "videopath";
  public final static String VIDEO_URI = "videouri";
  private ImageView playpause;
  public String videoId;
  private File outFile;
  private String outTempFilePath;
  protected String inFilePath;
  private File outFileTemp;
  private VideoInfo videoInfo;
  public static final long MINTIME = 10000;
  public static final long MAXTIME = 300100;
  TextView playStartTimeTv;
  TextView playEndTimeTv;
  TextView cutDurationTimeTv;
  RangeSeekBar<Long> rangeSeekBar;
  PressAndTapLinearLayout minBackView;
  PressAndTapLinearLayout maxBackView;
  PressAndTapLinearLayout minForwardView;
  PressAndTapLinearLayout maxForwardView;
  protected long startTime;
  protected long endTime;
  protected long cutDuration;
  private int state = 0;
  private double lastSeekTime;
  private static final int SHOW_PROGRESS = 2;
  private Handler mainHandler;

  private Handler.Callback callback = new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case SHOW_PROGRESS:
          long pos = setProgress();
          if (pos >= endTime || !isExoPlaying()) {
            stopProgress();
          } else if (isExoPlaying()) {
            msg = mainHandler.obtainMessage(SHOW_PROGRESS);
            mainHandler.sendMessageDelayed(msg, 100 - (pos % 100));
          }
          return true;
      }
      return false;
    }
  };

  public static void openVideoActivity(Context context, Uri uri, String path, String videoId) {
    Intent intent = new Intent(context, CutVideoActivity.class);
    intent.putExtra(VIDEO_PATH, path);
    intent.putExtra(VIDEO_URI, uri.toString());
    intent.putExtra(VIDEO_ID, videoId);
    context.startActivity(intent);
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
    mainHandler = new Handler(callback);
    setTitle("视频剪裁");
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    Intent intent = getIntent();
    videoId = intent.getStringExtra(VIDEO_ID);
    String user_id = Utils.getLoginUserId(this);
    String d = Utils.formatCurrentTime();
    if (TextUtils.isEmpty(videoId)) {
      videoId = user_id + "_freevideo_" + d;
    }
    contentUri = Uri.parse(intent.getStringExtra(VIDEO_URI));
    inFilePath = intent.getStringExtra(VIDEO_PATH);
    setContentView(R.layout.hfx_activity_cut_video);
    initView();
    File uploadFile = HfxFileUtil.getUploadFile(this, videoId);
    uploadFile.delete();
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    canInitExoplayer = true;
    setMenu("下一步", false, new OnClickListener() {
      @Override
      public void onClick(View v) {
        switch (state) {
          case 0:
            if (rangeSeekBar != null) {
              setStartWithSeek(startTime);
              if (cutDuration > MAXTIME) {
                toast("视频时长超出限制\n请选择10秒到5分钟的视频片段");
                return;
              }
              state = 1;
              videoInfo = new VideoInfo(videoId, videoWidth, videoHeight, (int) cutDuration);
              outFileTemp = HfxFileUtil.getCutTempVideoFile(CutVideoActivity.this, videoId);
              outFile = HfxFileUtil.getVideoFile(CutVideoActivity.this, videoId);
              outTempFilePath = outFileTemp.getAbsolutePath();
              startCut(startTime, endTime, inFilePath, outTempFilePath);
            } else {
              toast("操作太快了,可以预览并剪裁哦!");
            }

            break;
          case 1:
            //下一步点击过快保护
            toast("操作太快了!");
            break;
        }
      }
    });
  }

  private void initView() {
    PlayerView playerView = (PlayerView) findViewById(R.id.player_view);
    initPlayerView(playerView);
    playpause = (ImageView) findViewById(R.id.playpause);
    playStartTimeTv = (TextView) findViewById(R.id.playStartTimeTv);
    playEndTimeTv = (TextView) findViewById(R.id.playEndTimeTv);
    cutDurationTimeTv = (TextView) findViewById(R.id.cutDurationTimeTv);
    minBackView = (PressAndTapLinearLayout) findViewById(R.id.minBackView);
    maxBackView = (PressAndTapLinearLayout) findViewById(R.id.maxBackView);
    minForwardView = (PressAndTapLinearLayout) findViewById(R.id.minForwardView);
    maxForwardView = (PressAndTapLinearLayout) findViewById(R.id.maxForwardView);
    minForwardView.setOnPressAndTapListener(new MyPressAndTapListener(true, true, 100, 800));
    minBackView.setOnPressAndTapListener(new MyPressAndTapListener(true, false, 100, 800));
    maxBackView.setOnPressAndTapListener(new MyPressAndTapListener(false, false, 100, 800));
    maxForwardView.setOnPressAndTapListener(new MyPressAndTapListener(false, true, 100, 800));
  }

  private void initRangeBar() {
    if (rangeSeekBar != null) {
      return;
    }
    setTime(0, getExoDuration());
    ViewGroup layout = (ViewGroup) findViewById(R.id.rangeLayout);
    rangeSeekBar = new RangeSeekBar<>(0L, endTime, MINTIME, this, 0,
        Utils.dp2px(this, 50));
    rangeSeekBar
        .setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {

          @Override
          public void onMinChangeFinished(RangeSeekBar<?> bar, Long minValue) {
            Logger.d("onMinChangeFinished: " + minValue);
            setStartWithSeek(minValue);
            setTime(minValue, endTime);

          }

          @Override
          public void onMinChanged(RangeSeekBar<?> bar, Long minValue) {
            Logger.d("onMinChanged: " + minValue);
            setStartPosition(minValue);
            setTime(minValue, endTime);
          }

          @Override
          public void onMaxChanged(RangeSeekBar<?> bar, Long maxValue) {
            Logger.d("onMaxChanged: " + maxValue);
            setEndPosition(maxValue);
            setTime(startTime, maxValue);
          }

          @Override
          public void onMaxChangeFinished(RangeSeekBar<?> bar, Long maxValue) {
            Logger.d("onMaxChangeFinished: " + maxValue);
            setEndWithSeek(maxValue);
            setTime(startTime, maxValue);
          }

          @Override
          public void onRangeTouch(RangeSeekBar<?> bar, Long rangeValue) {
            Logger.d("onRangeTouch: " + rangeValue);
            seekAndPlay(rangeValue);
          }

        });

    // add RangeSeekBar to pre-defined layout
    layout.addView(rangeSeekBar);
  }


  private void stopProgress() {
    exoPlayerPause();
    exoPlayerSeekTo(startTime);
    mainHandler.removeMessages(SHOW_PROGRESS);
    if (rangeSeekBar != null) {
      rangeSeekBar.setPlaying(false);
    }
    updatePausePlay();

  }

  private long setProgress() {
    long position = getExoCurrentPosition();
    long duration = getExoDuration();
    if (rangeSeekBar != null && duration > 0) {
      rangeSeekBar.setPlayPositionValue(position);
      rangeSeekBar.setPlaying(true);
    }
    return position;
  }

  private void updatePausePlay() {
    if (isExoPlaying()) {
      playpause.setVisibility(View.GONE);
    } else {
      playpause.setVisibility(View.VISIBLE);
    }
  }

  @Override
  protected void doPauseResume() {
    if (isExoPlaying()) {
      stopProgress();
    } else {
      exoPlayerSeekTo(startTime);
      exoPlayerStart();
      mainHandler.removeMessages(SHOW_PROGRESS);
      mainHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    updatePausePlay();
  }

  private void setStartWithSeek(long startPosition) {
    startTime = startPosition;
    pauseAndSeek(startPosition);
  }


  private void setEndWithSeek(long endPosition) {
    endTime = endPosition;
    pauseAndSeek(endPosition);
  }

  /**
   * 剪裁起始位置修改
   * @param startPosition
   */
  private void setStartPosition(long startPosition) {
    double currentTime = System.currentTimeMillis();
    startTime = startPosition;
    //回调很频繁，间隔500ms再处理
    if (currentTime - lastSeekTime > 500) {
      lastSeekTime = currentTime;
      pauseAndSeek(startPosition);
    }
  }

  /**
   * 剪裁终止位置修改
   * @param endPosition
   */
  private void setEndPosition(long endPosition) {
    double currentTime = System.currentTimeMillis();
    endTime = endPosition;
    if (currentTime - lastSeekTime > 500) {
      lastSeekTime = currentTime;
      pauseAndSeek(endPosition);
    }
  }

  private void pauseAndSeek(long startPosition) {
    mainHandler.removeMessages(SHOW_PROGRESS);
    if (rangeSeekBar != null) {
      rangeSeekBar.setPlaying(false);
    }
    exoPlayerPause();
    exoPlayerSeekTo(startPosition);
    updatePausePlay();
  }

  private void seekAndPlay(long position) {
    exoPlayerSeekTo(position);
    exoPlayerStart();
    mainHandler.removeMessages(SHOW_PROGRESS);
    mainHandler.sendEmptyMessage(SHOW_PROGRESS);
    updatePausePlay();
  }

  void setTime(long startTime, long endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
    cutDuration = endTime - startTime;
    playStartTimeTv.setText(Utils.timeStringForTimeMs(startTime));
    playEndTimeTv.setText(Utils.timeStringForTimeMs(endTime));
    cutDurationTimeTv.setText(Utils.secStringForTimeMs(cutDuration) + "秒");
  }

  class MyPressAndTapListener implements PressAndTapLinearLayout.PressAndTapListener {

    private boolean isMin;
    private boolean isForward;
    private int stepOnTap;
    private int stepOnPressing;


    public MyPressAndTapListener(boolean isMin, boolean isForward, int stepOnTap,
        int stepOnPressing) {
      this.isMin = isMin;
      this.isForward = isForward;
      this.stepOnTap = stepOnTap;
      this.stepOnPressing = stepOnPressing;
    }

    @Override
    public void onPressing() {
      if (rangeSeekBar != null) {
        rangeSeekBar.setPressed(true);
        if (isMin) {
          rangeSeekBar.setPressedThumb(RangeSeekBar.Thumb.MIN);
          long min = rangeSeekBar.getSelectedMinValue();
          if (isForward) {
            rangeSeekBar.setSelectedMinValue(min + stepOnPressing);
          } else {
            rangeSeekBar.setSelectedMinValue(min - stepOnPressing);
          }
        } else {
          rangeSeekBar.setPressedThumb(RangeSeekBar.Thumb.MAX);
          long max = rangeSeekBar.getSelectedMaxValue();
          if (isForward) {
            rangeSeekBar.setSelectedMaxValue(max + stepOnPressing);
          } else {
            rangeSeekBar.setSelectedMaxValue(max - stepOnPressing);
          }
        }
      }
    }

    @Override
    public void onTap() {
      if (rangeSeekBar != null) {
        rangeSeekBar.setPressed(true);
        if (isMin) {
          rangeSeekBar.setPressedThumb(RangeSeekBar.Thumb.MIN);
          long min = rangeSeekBar.getSelectedMinValue();
          if (isForward) {
            rangeSeekBar.setSelectedMinValue(min + stepOnTap);
          } else {
            rangeSeekBar.setSelectedMinValue(min - stepOnTap);
          }
        } else {
          rangeSeekBar.setPressedThumb(RangeSeekBar.Thumb.MAX);
          long max = rangeSeekBar.getSelectedMaxValue();
          if (isForward) {
            rangeSeekBar.setSelectedMaxValue(max + stepOnTap);
          } else {
            rangeSeekBar.setSelectedMaxValue(max - stepOnTap);
          }
        }
      }
    }

    @Override
    public void onTouchFinish() {
      if (rangeSeekBar != null) {
        rangeSeekBar.setPressed(false);
        rangeSeekBar.resetPressedThumb();
      }

    }
  }

  private void showControls() {

  }

  @Override
  public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
    if (playbackState == ExoPlayer.STATE_ENDED) {
      Logger.d("STATE_ENDED");
      showControls();
    } else if (playbackState == ExoPlayer.STATE_READY) {
      Logger.d("STATE_READY");
      initRangeBar();
    }
  }

  private void onCutFinished(boolean cutSuccess) {
    Logger.d("onCutFinished: " + cutSuccess);
    hideProgress();
    if (cutSuccess && FileUtil.renameFile(outFileTemp, outFile)) {
      HfxPreferenceUtil.saveVideoCoverTime(this, videoId, -1);
      HfxUtil.saveVideoInfo(this, videoInfo);
      MatchInfo matchInfo = HfxUtil.getMatchInfo(this, videoId);
      if (matchInfo != null && !TextUtils.isEmpty(matchInfo.realUrl)) {
        openView(matchInfo.realUrl);
      } else {
        SaveVideoActivity.openSaveVideo(this, videoId);
      }
      finish();
    } else {
      showErrorDialog("剪裁视频出错,请选择其他视频后重试!", true);
    }

  }

  @Override
  public void onBackPressed() {
    if (state == 0) {
      showDialog("提示", "视频创作未完成,是否退出?", "确认退出", new OnClickListener() {
        @Override
        public void onClick(View v) {
          finish();
        }
      }, "继续创作", null);
    }
  }


  private void startCut(long startTime, long endTime, String inFilePath, String outFilePath) {
    showProgress("正在裁剪视频");
    FfmpegUtil.cutVideo(startTime, endTime, inFilePath, outFilePath, new RxFFmpegSubscriber() {
      @Override
      public void onFinish() {
        onCutFinished(true);
      }

      @Override
      public void onProgress(int progress, long progressTime) {

      }

      @Override
      public void onCancel() {
      }

      @Override
      public void onError(String message) {
        onCutFinished(false);
      }
    });
  }

}
