package com.example.exoaudioplayer.video.exo;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.example.exoaudioplayer.aduio.exo.EventLogger;
import com.example.exoaudioplayer.video.base.AbstractVideoPlayer;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.namibox.util.Logger;
import com.namibox.util.network.NetWorkHelper;
import java.util.Map;


public class ExoMediaPlayer extends AbstractVideoPlayer {

  protected Context mAppContext;
  protected SimpleExoPlayer mInternalPlayer;
  private String video_url;

  private PlaybackParameters mSpeedPlaybackParameters;

  private int mLastReportedPlaybackState = Player.STATE_IDLE;
  private boolean mLastReportedPlayWhenReady = false;
  private boolean mIsPreparing;
  private boolean mIsBuffering;

  private DefaultTrackSelector trackSelector;
  private EventLogger eventLogger;
  private BandwidthMeter.EventListener eventListener = new BandwidthMeter.EventListener() {
    @Override
    public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {
      float speed = (bytes * 1000f) / (elapsedMs * 1024);
      Logger.d("onBandwidthSample:elapsedMs=" + elapsedMs + ", bytes=" + bytes + ", speed=" + speed
          + "kb/s");
    }
  };

  private Player.EventListener playerEventListener = new Player.EventListener() {
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      if (mPlayerEventListener != null) {
        Logger
            .w("playStateChange: playWhenReady=" + playWhenReady + ", playbackState=" + playbackState);
        mPlayerEventListener.onPlayerStateChanged(playWhenReady, playbackState);
      }
      if (mPlayerEventListener == null) return;
      if (mIsPreparing) return;
      if (mLastReportedPlayWhenReady != playWhenReady || mLastReportedPlaybackState != playbackState) {
        switch (playbackState) {
          case Player.STATE_BUFFERING:
            mPlayerEventListener.onInfo(MEDIA_INFO_BUFFERING_START, getBufferedPercentage());
            mIsBuffering = true;
            break;
          case Player.STATE_READY:
            if (mIsBuffering) {
              mPlayerEventListener.onInfo(MEDIA_INFO_BUFFERING_END, getBufferedPercentage());
              mIsBuffering = false;
            }
            break;
          case Player.STATE_ENDED:
            mPlayerEventListener.onCompletion();
            break;
        }
        mLastReportedPlaybackState = playbackState;
        mLastReportedPlayWhenReady = playWhenReady;
      }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
      if (mPlayerEventListener != null) {
        mPlayerEventListener.onPlayerError(error);
      }
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
//      if (playbackParameters != null && mPlayerEventListener != null) {
//        Logger.d("speedChanged: " + playbackParameters.speed);
//        mPlayerEventListener.speedChanged(playbackParameters.speed);
//      }
    }
  };

  public ExoMediaPlayer(Context context) {
    mAppContext = context.getApplicationContext();
  }

  @Override
  public void initPlayer() {
    Handler mainHandler = new Handler();
    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(mainHandler, eventListener);
    @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
    DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(mAppContext,
        null, extensionRendererMode);
    TrackSelection.Factory videoTrackSelectionFactory =
        new AdaptiveTrackSelection.Factory(bandwidthMeter);
    trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
    eventLogger = new EventLogger(trackSelector);
    mInternalPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
    mInternalPlayer.addListener(playerEventListener);
    mInternalPlayer.addListener(eventLogger);
    mInternalPlayer.setAudioDebugListener(eventLogger);
    mInternalPlayer.setVideoDebugListener(videoRendererEventListener);
    mInternalPlayer.addMetadataOutput(eventLogger);
    mInternalPlayer.setPlayWhenReady(false);
  }

  private VideoRendererEventListener videoRendererEventListener = new VideoRendererEventListener() {
    @Override
    public void onVideoEnabled(DecoderCounters counters) {

    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs,
                                          long initializationDurationMs) {

    }

    @Override
    public void onVideoInputFormatChanged(Format format) {

    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
      if (mPlayerEventListener != null) {
        mPlayerEventListener.onVideoSizeChanged(width, height);
        if (unappliedRotationDegrees > 0) {
          mPlayerEventListener.onInfo(MEDIA_INFO_VIDEO_ROTATION_CHANGED, unappliedRotationDegrees);
        }
      }
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {
      Logger.d("onRenderedFirstFrame");
//      handler.sendEmptyMessageDelayed(MSG_FIRST_FRAME, 1000);
      if (mPlayerEventListener != null && mIsPreparing) {
        mPlayerEventListener.onInfo(MEDIA_INFO_VIDEO_RENDERING_START, 0);
        mIsPreparing = false;
      }
    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {

    }
  };

  @Override
  public void setDataSource(String path, Map<String, String> headers) {
    try {
      video_url = path;
    } catch (Exception e) {
      mPlayerEventListener.onError();
    }
  }

  @Override
  public void setDataSource(AssetFileDescriptor fd) {
    //no support
  }

  @Override
  public void start() {
    if (mInternalPlayer == null)
      return;
    mInternalPlayer.setPlayWhenReady(true);
  }

  @Override
  public void pause() {
    if (mInternalPlayer == null)
      return;
    mInternalPlayer.setPlayWhenReady(false);
  }

  @Override
  public void stop() {
    if (mInternalPlayer == null)
      return;
    mInternalPlayer.stop();
  }

  @Override
  public void prepareAsync() {
    if (mInternalPlayer == null)
      return;
    if (mSpeedPlaybackParameters != null) {
      mInternalPlayer.setPlaybackParameters(mSpeedPlaybackParameters);
    }
    mIsPreparing = true;
    if (mPlayerEventListener != null && mIsPreparing) {
      mPlayerEventListener.onPrepared();
    }
    mInternalPlayer.prepare(buildMediaSource(video_url), false, false);
  }

  private MediaSource buildMediaSource(String url) {
    // Measures bandwidth during playback. Can be null if not required.
    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
    DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mAppContext,
        NetWorkHelper.getInstance().getUa(), bandwidthMeter);
// Produces Extractor instances for parsing the media data.
    ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
// This is the MediaSource representing the media to be played.
    return new ExtractorMediaSource(Uri.parse(url),
        dataSourceFactory, extractorsFactory, null, null);
// Prepare the player with the source.
  }

  @Override
  public void reset() {
    if (mInternalPlayer != null) {
      mInternalPlayer.stop();
      mInternalPlayer.setVideoSurface(null);
      mIsPreparing = false;
      mIsBuffering = false;
      mLastReportedPlaybackState = Player.STATE_IDLE;
      mLastReportedPlayWhenReady = false;
    }
  }

  @Override
  public boolean isPlaying() {
    if (mInternalPlayer == null)
      return false;
    int state = mInternalPlayer.getPlaybackState();
    switch (state) {
      case Player.STATE_BUFFERING:
      case Player.STATE_READY:
        return mInternalPlayer.getPlayWhenReady();
      case Player.STATE_IDLE:
      case Player.STATE_ENDED:
      default:
        return false;
    }
  }

  @Override
  public void seekTo(long time) {
    if (mInternalPlayer == null)
      return;
    mInternalPlayer.seekTo(time);
  }

  @Override
  public void release() {
    if (mInternalPlayer != null) {
      mInternalPlayer.removeListener(playerEventListener);
      final SimpleExoPlayer player = mInternalPlayer;
      mInternalPlayer = null;
      new Thread() {
        @Override
        public void run() {
          //异步释放，防止卡顿
          player.release();
        }
      }.start();
    }

    mIsPreparing = false;
    mIsBuffering = false;
    mLastReportedPlaybackState = Player.STATE_IDLE;
    mLastReportedPlayWhenReady = false;
    mSpeedPlaybackParameters = null;
  }

  @Override
  public long getCurrentPosition() {
    if (mInternalPlayer == null)
      return 0;
    return mInternalPlayer.getCurrentPosition();
  }

  @Override
  public long getDuration() {
    if (mInternalPlayer == null)
      return 0;
    return mInternalPlayer.getDuration();
  }

  @Override
  public int getBufferedPercentage() {
    return mInternalPlayer == null ? 0 : mInternalPlayer.getBufferedPercentage();
  }

  @Override
  public void setSurface(Surface surface) {
    if (mInternalPlayer != null) {
      mInternalPlayer.setVideoSurface(surface);
    }
  }

  @Override
  public void removeSurface() {
    if (mInternalPlayer != null) {
      mInternalPlayer.clearVideoSurface();
    }
  }

  @Override
  public void setDisplay(SurfaceHolder holder) {
    if (holder == null)
      setSurface(null);
    else
      setSurface(holder.getSurface());
  }

  @Override
  public void setVolume(float leftVolume, float rightVolume) {
    if (mInternalPlayer != null)
      mInternalPlayer.setVolume((leftVolume + rightVolume) / 2);
  }

  @Override
  public void setLooping(boolean isLooping) {
    if (mInternalPlayer != null)
      mInternalPlayer.setRepeatMode(isLooping ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
  }

  @Override
  public void setOptions() {
    //准备好就开始播放
    mInternalPlayer.setPlayWhenReady(true);
  }

  @Override
  public void setSpeed(float speed) {
    PlaybackParameters playbackParameters = new PlaybackParameters(speed, 1f);
    mSpeedPlaybackParameters = playbackParameters;
    if (mInternalPlayer != null) {
      mInternalPlayer.setPlaybackParameters(playbackParameters);
    }
  }

  @Override
  public float getSpeed() {
    if (mSpeedPlaybackParameters != null) {
      return mSpeedPlaybackParameters.speed;
    }
    return 1f;
  }

  @Override
  public long getTcpSpeed() {
    // no support
    return 0;
  }

  public SimpleExoPlayer getPlayer() {
    return mInternalPlayer;
  }

  public void setPlayWhenReady(boolean play){
    mInternalPlayer.setPlayWhenReady(play);
  }
}
