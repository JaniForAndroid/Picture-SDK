package com.namibox.hfx.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import com.google.android.exoplayer.lib.PlayerView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.namibox.commonlib.activity.BaseActivity;

/**
 * Created by sunha on 2017/1/18 0018.
 */

public abstract class AbsExoActivity extends BaseActivity implements Player.EventListener {

  private PlayerView playerView;
  private SimpleExoPlayer player;
  private DefaultTrackSelector trackSelector;
  private boolean needRetrySource;
  private boolean shouldAutoPlay;
  private int resumeWindow;
  private long resumePosition;
  protected Uri contentUri;
  private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
  protected int videoWidth;
  protected int videoHeight;
  protected boolean canInitExoplayer = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    shouldAutoPlay = false;
    clearResumePosition();
  }

  protected void initPlayerView(PlayerView playerView) {
    this.playerView = playerView;
    playerView.setUseController(false);
    playerView.requestFocus();
    playerView.setLockViewVisibility(View.GONE);
    playerView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        doPauseResume();
      }
    });
  }


  protected void initializePlayer() {
    if (!canInitExoplayer) {
      return;
    }
    boolean needNewPlayer = player == null;
    if (needNewPlayer) {
      TrackSelection.Factory videoTrackSelectionFactory =
          new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
      trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
      DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this,
          null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
      player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
      player.addListener(this);
      playerView.setPlayer(player);
      playerView.setVideoListener(new PlayerView.OnVideoListener() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
          videoHeight = height;
          videoWidth = width;
        }
      });
      player.setPlayWhenReady(shouldAutoPlay);
    }
    if (needNewPlayer || needRetrySource) {
      //HttpProxyCacheServer proxy = VideoProxy.getProxy(this);
      //String proxyUrl = proxy.getProxyUrl(videoUrl);
      boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(resumeWindow, resumePosition);
      }
      player.prepare(buildMediaSource(contentUri), !haveResumePosition, false);
      needRetrySource = false;
    }
  }

  protected void releasePlayer() {
    if (player != null) {
      shouldAutoPlay = player.getPlayWhenReady();
      updateResumePosition();
      player.release();
      player = null;
      trackSelector = null;
    }
  }

  private void updateResumePosition() {
    resumeWindow = player.getCurrentWindowIndex();
    resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
        : C.TIME_UNSET;
  }

  private void clearResumePosition() {
    resumeWindow = C.INDEX_UNSET;
    resumePosition = C.TIME_UNSET;
  }

  private MediaSource buildMediaSource(Uri uri) {
    HttpDataSource.Factory httpDataSourceFactory = new OkHttpDataSourceFactory(getOkHttpClient(),
        getUserAgent(), BANDWIDTH_METER);
    DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, BANDWIDTH_METER,
        httpDataSourceFactory);
    return new ExtractorMediaSource(uri, dataSourceFactory, new DefaultExtractorsFactory(), null,
        null);
  }

  @Override
  public void onNewIntent(Intent intent) {
    releasePlayer();
    shouldAutoPlay = false;
    clearResumePosition();
    setIntent(intent);
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      releasePlayer();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (Util.SDK_INT > 23) {
      releasePlayer();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (Util.SDK_INT > 23) {
      initializePlayer();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if ((Util.SDK_INT <= 23 || player == null)) {
      initializePlayer();
    }
  }


  protected void doPauseResume() {

  }

  protected boolean isExoPlaying() {
    return player != null && player.getPlayWhenReady();
  }

  protected void exoPlayerStart() {
    if (player != null) {
      player.setPlayWhenReady(true);
    }
  }

  protected void exoPlayerPause() {
    if (player != null) {
      player.setPlayWhenReady(false);
    }
  }

  protected long getExoCurrentPosition() {
    if (player != null) {
      return player.getCurrentPosition();
    }
    return 0;
  }

  protected long getExoDuration() {
    if (player != null) {
      return player.getDuration();
    }
    return 0;
  }

  protected void exoPlayerSeekTo(long pos) {
    if (player != null) {
      player.seekTo(pos);
    }

  }

  public void setExoPlayerVolume(float audioVolume) {
    if (player != null) {
      player.setVolume(audioVolume);
    }
  }

  @Override
  public void onLoadingChanged(boolean isLoading) {
    // Do nothing.
  }

  @Override
  public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
  }

  @Override
  public void onPositionDiscontinuity() {

  }

  @Override
  public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

  }

  @Override
  public void onTimelineChanged(Timeline timeline, Object manifest) {

  }

  @Override
  public void onRepeatModeChanged(int repeatMode) {

  }

  @Override
  public void onPlayerError(ExoPlaybackException e) {
    needRetrySource = true;
  }

  @Override
  public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

  }
}
