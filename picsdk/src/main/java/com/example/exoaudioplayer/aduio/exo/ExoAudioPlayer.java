package com.example.exoaudioplayer.aduio.exo;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Keep;
import android.util.Log;
import com.example.exoaudioplayer.aduio.base.AbstractAudioPlayer;
import com.example.picsdk.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
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
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheEvictor;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.namibox.util.Logger;
import com.namibox.util.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;

/**
 * Create time: 2017/5/10.
 */

@Keep
public class ExoAudioPlayer extends AbstractAudioPlayer {

  private static final String TAG = "ExoAudioPlayer";
  public static final int STATE_IDLE = Player.STATE_IDLE;
  public static final int STATE_BUFFERING = Player.STATE_BUFFERING;
  public static final int STATE_READY = Player.STATE_READY;
  public static final int STATE_ENDED = Player.STATE_ENDED;
  private SimpleExoPlayer player;
  private Handler mainHandler;
  private Uri[] uris;
  private Context context;
  private DataSource.Factory defaultDataSourceFactory;
  private DataSource.Factory cacheDataSourceFactory;
  private static Cache cache;
  private boolean shouldAutoPlay;
  private boolean needPrepare;
  private long startPosition = C.INDEX_UNSET;
  private long stopPosition = C.INDEX_UNSET;
  private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
  private static final int MSG_UPDATE_PROGRESS = 0;
  private DefaultTrackSelector trackSelector;
  private EventLogger eventLogger;
  private long lastPlayTime;
  private AudioManager mAm;
  private boolean hasFocus;

  private int mLastReportedPlaybackState = Player.STATE_IDLE;
  private boolean mLastReportedPlayWhenReady = false;

  private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
    @Override
    public void onAudioFocusChange(int focusChange) {
      Log.i(TAG, "audio focus change: " + focusChange);
      if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
        // Stop playback
        stop();
        hasFocus = false;
      } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
        hasFocus = true;
      }
      if (callback != null) {
        Logger.w("focusChange: " + hasFocus);
        callback.onFocusChange(hasFocus);
      }
    }
  };

  private void tryRequestFocus() {
    // Request audio focus for playback
    if (!hasFocus) {
      int result = mAm.requestAudioFocus(audioFocusChangeListener,
          // Use the music stream.
          AudioManager.STREAM_MUSIC,
          // Request permanent focus.
          AudioManager.AUDIOFOCUS_GAIN);
      Log.i(TAG, "requestFocus:" + result);
      if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        hasFocus = true;
      }
    }
  }

  private void tryGiveUpFocus() {
    if (hasFocus) {
      int result = mAm.abandonAudioFocus(audioFocusChangeListener);
      Log.i(TAG, "abandonAudioFocus:" + result);
      if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        hasFocus = false;
      }
    }
  }

  private Handler.Callback msgCallback = new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_UPDATE_PROGRESS:
          if (player != null) {
            long currentTime = player.getCurrentPosition();
            long bufferTime = player.getBufferedPosition();
            long totalTime = player.getDuration();
            if (callback != null) {
              callback.playUpdate(currentTime, bufferTime, totalTime);
            }
            if (stopPosition != C.INDEX_UNSET && stopPosition != 0 && lastPlayTime < stopPosition
                && stopPosition < currentTime) {
              player.setPlayWhenReady(false);
            }
            lastPlayTime = currentTime;
          }
          mainHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 50);
          return true;
      }
      return false;
    }
  };

  public ExoAudioPlayer(Context context, OkHttpClient okHttpClient, String ua) {
    this.context = context;
    mainHandler = new Handler(context.getMainLooper(), msgCallback);
    mAm = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    HttpDataSource.Factory httpDataSourceFactory = new OkHttpDataSourceFactory(okHttpClient, ua,
        BANDWIDTH_METER, CacheControl.FORCE_NETWORK);
//    HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSourceFactory(ua,
//        BANDWIDTH_METER,
//        DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
//        DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true);
    defaultDataSourceFactory = new DefaultDataSourceFactory(context, BANDWIDTH_METER,
        httpDataSourceFactory);
    if (cache == null) {
      CacheEvictor cacheEvictor = new LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024);
      cache = new SimpleCache(new File(context.getCacheDir(), "audio_cache"), cacheEvictor);
    }
    cacheDataSourceFactory = new CacheDataSourceFactory(cache, defaultDataSourceFactory,
        CacheDataSource.FLAG_BLOCK_ON_CACHE, 200 * 1024 * 1024);
  }

  private Player.EventListener eventListener = new Player.EventListener() {
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
      if (callback != null) {
        Logger
            .w("playStateChange: playWhenReady=" + playWhenReady + ", playbackState=" + playbackState);
        callback.playStateChange(playWhenReady, playbackState);
      }

      if (playbackState == ExoPlayer.STATE_READY && playWhenReady) {
        mainHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
        tryRequestFocus();
      } else {
        mainHandler.removeMessages(MSG_UPDATE_PROGRESS);
        tryGiveUpFocus();
      }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
      needPrepare = true;
      Logger.e(error, "playError");

      Exception message;
      if (error.type == ExoPlaybackException.TYPE_SOURCE) {
        message = error.getSourceException();
        if (message instanceof FileDataSource.FileDataSourceException) {
          Utils.toast(context,context.getString(R.string.player_error_tips1));
        } else if (message instanceof HttpDataSource.InvalidResponseCodeException) {
          HttpDataSource.InvalidResponseCodeException e = (HttpDataSource.InvalidResponseCodeException) message;
          Utils.toast(context,context.getString(R.string.player_error_tips2) + e.responseCode);
        } else if (message instanceof HttpDataSource.HttpDataSourceException) {
          //HttpDataSourceException e = (HttpDataSourceException) message;
          Utils.toast(context,context.getString(R.string.player_error_tips3));
        } else {
          Utils.toast(context,context.getString(R.string.player_error_tips4));
        }
      } else if (error.type == ExoPlaybackException.TYPE_RENDERER) {
        //message = error.getRendererException();
        //TODO
//        MobclickAgent.reportError(context, error);
        Utils.toast(context,context.getString(R.string.player_error_tips5));
      } else {
        //message = error.getUnexpectedException();
        Utils.toast(context,context.getString(R.string.player_error));
      }

      if (callback != null) {
        callback.playError(error);
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
      if (playbackParameters != null && callback != null) {
        Logger.d("speedChanged: " + playbackParameters.speed);
        callback.speedChanged(playbackParameters.speed);
      }
    }
  };

  //部分mtk设备硬解wav播放有问题，使用google解码器
  private class MyRenderersFactory extends DefaultRenderersFactory {


    public MyRenderersFactory(Context context,
                              DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                              @ExtensionRendererMode int extensionRendererMode) {
      super(context, drmSessionManager, extensionRendererMode);
    }

    @Override
    protected void buildAudioRenderers(Context context,
                                       DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AudioProcessor[] audioProcessors,
                                       Handler eventHandler, AudioRendererEventListener eventListener,
                                       @ExtensionRendererMode int extensionRendererMode, ArrayList<Renderer> out) {
      out.add(new MediaCodecAudioRenderer(mediaCodecSelector, drmSessionManager, true,
          eventHandler, eventListener, AudioCapabilities.getCapabilities(context),
          audioProcessors));
      if (extensionRendererMode == EXTENSION_RENDERER_MODE_OFF) {
        return;
      }
      int extensionRendererIndex = out.size();
      if (extensionRendererMode == EXTENSION_RENDERER_MODE_PREFER) {
        extensionRendererIndex--;
      }
      Renderer renderer = new FfmpegAudioRenderer(eventHandler, eventListener, audioProcessors);
      out.add(extensionRendererIndex++, renderer);
      Logger.i("Loaded FfmpegAudioRenderer.");
    }
  }

  private static MediaCodecSelector mediaCodecSelector = new MediaCodecSelector() {

    @Override
    public MediaCodecInfo getDecoderInfo(String mimeType, boolean requiresSecureDecoder)
        throws MediaCodecUtil.DecoderQueryException {
      //mtk解码修改为google
      List<MediaCodecInfo> decoderInfos = MediaCodecUtil
          .getDecoderInfos(mimeType, requiresSecureDecoder);
      Logger.e("###mimeType: " + mimeType);
      for (MediaCodecInfo mediaCodecInfo : decoderInfos) {
        Logger.e("###mediaCodecInfo: " + mediaCodecInfo.name);
        if (mimeType.equals("audio/raw") && mediaCodecInfo.name
            .equals("OMX.MTK.AUDIO.DECODER.RAW")) {
          Logger.e("###change to: OMX.google.raw.decoder");
          return MediaCodecInfo.newInstance("OMX.google.raw.decoder", "audio/raw", null);
        }
//        if (mimeType.equals("audio/mpeg") && (mediaCodecInfo.name.equals("OMX.MTK.AUDIO.DECODER.MP3")
//            || mediaCodecInfo.name.equals("OMX.Meizu.MP3"))) {
//          Logger.e("###change to: OMX.google.mp3.decoder");
//          return MediaCodecInfo.newInstance("OMX.google.mp3.decoder", "audio/mpeg", null);
//        }
      }
      return decoderInfos.isEmpty() ? null : decoderInfos.get(0);
    }

    @Override
    public MediaCodecInfo getPassthroughDecoderInfo() throws MediaCodecUtil.DecoderQueryException {
      return MediaCodecUtil.getPassthroughDecoderInfo();
    }

  };

  public void initializePlayer() {
    boolean needNewPlayer = player == null;
    if (needNewPlayer) {
      DefaultRenderersFactory renderersFactory = new MyRenderersFactory(context,
          null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);

      TrackSelection.Factory videoTrackSelectionFactory =
          new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
      trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

      player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
      player.addListener(eventListener);

      eventLogger = new EventLogger(trackSelector);
      player.addListener(eventLogger);
      player.setAudioDebugListener(eventLogger);
      player.setVideoDebugListener(eventLogger);
      player.addMetadataOutput(eventLogger);
    }
    player.setPlayWhenReady(shouldAutoPlay);
    if (needNewPlayer || needPrepare) {
      preparePlayer();
      needPrepare = false;
    }
  }

  private void preparePlayer() {
    MediaSource[] mediaSources = new MediaSource[uris.length];
    for (int i = 0; i < uris.length; i++) {
      mediaSources[i] = buildMediaSource(uris[i]);
    }
    MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
        : new ConcatenatingMediaSource(mediaSources);
    player.prepare(mediaSource, true, true);
    player.seekTo(0, startPosition);
  }

  private MediaSource buildMediaSource(Uri uri) {
    if (Util.isLocalFileUri(uri)) {
      return new ExtractorMediaSource(uri, defaultDataSourceFactory, new DefaultExtractorsFactory(),
          null, null);
    } else {
      return new ExtractorMediaSource(uri, cacheDataSourceFactory, new DefaultExtractorsFactory(),
          null, null);
    }
  }

  public void reset() {
    if (player != null) {
      player.stop();
      mLastReportedPlaybackState = Player.STATE_IDLE;
      mLastReportedPlayWhenReady = false;
    }
  }

  public void releasePlayer() {
    if (player != null) {
      player.release();
      player = null;
      trackSelector = null;
      eventLogger = null;
    }
  }

  @Override
  public long getCurrentPosition() {
    if (player != null) {
      return player.getCurrentPosition();
    }
    return 0;
  }

  @Override
  public long getDuration() {
    if (player != null) {
      return player.getDuration();
    }
    return 0;
  }

  public SimpleExoPlayer getPlayer() {
    return player;
  }

  public void play(Uri uri) {
    play(uri, true);
  }

  public void play(Uri uri, boolean autoPlay) {
    Uri[] uris = new Uri[1];
    uris[0] = uri;
    play(uris, C.TIME_UNSET, C.TIME_UNSET, autoPlay);
  }

  public void play(Uri uri, long startMs) {
    play(uri, startMs, C.TIME_UNSET);
  }

  public void play(Uri uri, long startMs, long stopMs) {
    Uri[] uris = new Uri[1];
    uris[0] = uri;
    play(uris, startMs, stopMs, true);
  }

  public void play(Uri[] uris) {
    play(uris, C.TIME_UNSET, C.TIME_UNSET, true);
  }

  private void play(Uri[] uris, long resumePosition, long stopPosition, boolean autoPlay) {
//    Log.i(TAG, "play: " + uris[0].toString());
    checkVolume();
    this.uris = uris;
    this.startPosition = resumePosition;
    this.stopPosition = stopPosition;
    needPrepare = true;
    shouldAutoPlay = autoPlay;
    initializePlayer();
  }

  private void checkVolume() {
    AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    if (max > 0 && 100 * current / max < 15) {
      com.namibox.util.Utils.toast(context, context.getString(R.string.low_volume));
    }
  }

  public Uri[] getUris() {
    return uris;
  }

  public void playPause() {
    if (player == null) {
      return;
    }
    if (player.getPlaybackState() == Player.STATE_ENDED
        || player.getPlaybackState() == Player.STATE_IDLE) {
      repeatPlay();
    } else {
      if (player != null) {
        player.setPlayWhenReady(!player.getPlayWhenReady());
      }
    }
  }

  @Override
  public void pause() {
    if (player != null) {
      player.setPlayWhenReady(false);
    }
  }

  public void setPlayWhenReady(boolean playWhenReady) {
    if (player != null) {
      player.setPlayWhenReady(playWhenReady);
    }
  }

  public void repeatPlay() {
    if (uris != null) {
      play(uris);
    }
  }

  public void stop() {
    if (player != null) {
      player.stop();
    }
  }

  @Override
  public boolean isPlaying() {
    if (player != null) {
      return player.isPlayingAd();
    }
    return false;
  }

  public void seekTo(long positionMs) {
    if (player != null) {
      player.seekTo(positionMs);
    }
  }

  public void setSpeed(float speed) {
    if (player != null) {
      PlaybackParameters parameters = player.getPlaybackParameters();
      if (parameters.speed != speed) {
        player.setPlaybackParameters(new PlaybackParameters(speed, 1f));
      }
    }
  }

  public void backward() {
    if (player != null) {
      long seekToPosition = player.getCurrentPosition() - 3000;
      if (seekToPosition < 0) {
        seekToPosition = 0;
      }
      seekTo(seekToPosition);
      setPlayWhenReady(true);
    }
  }

  public void fastForward() {
    if (player != null) {
      long seekToPosition = player.getCurrentPosition() + 3000;
      if (seekToPosition > player.getDuration()) {
        seekToPosition = player.getDuration();
      }
      seekTo(seekToPosition);
      setPlayWhenReady(true);
    }
  }
}
