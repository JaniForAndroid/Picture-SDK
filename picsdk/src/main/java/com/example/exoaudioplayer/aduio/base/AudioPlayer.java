package com.example.exoaudioplayer.aduio.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.namibox.util.Logger;
import java.io.IOException;

/**
 * 封装了mediaPlayer相关方法，加入了状态检测
 * Create time: 2018/10/19.
 */
public class AudioPlayer implements OnCompletionListener, OnErrorListener, OnInfoListener,
    OnPreparedListener,
    OnSeekCompleteListener {
  private boolean debug = false;
  private MediaPlayer player;
  private State state = State.STATE_IDLE;
  private boolean seeking;
  private PlaybackParams playbackParameters;

  public interface Callback {
    void onBufferingUpdate(int percent);

    void onCompletion();

    void onError(int what, int extra);

    void onInfo(int what, int extra);

    void onPrepared();

    void onSeekComplete();

    void onVideoSizeChanged(int width, int height);
  }

  private Callback callback;

  private void setState(State state) {
    Logger.v(this.state + " -> " + state);
    this.state = state;
  }

  public State getState() {
    return state;
  }

//  @Override
//  public void onBufferingUpdate(MediaPlayer mp, int percent) {
//    if (callback != null) {
//      callback.onBufferingUpdate(percent);
//    }
//  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    if (state != State.STATE_COMPLETED && state != State.STATE_ERROR) {
      setState(State.STATE_COMPLETED);
      if (callback != null) {
        callback.onCompletion();
      }
    }
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    if (state != State.STATE_ERROR) {
      setState(State.STATE_ERROR);
      if (callback != null) {
        callback.onError(what, extra);
      }
    }
    return true;
  }

  @Override
  public boolean onInfo(MediaPlayer mp, int what, int extra) {
    if (callback != null) {
      callback.onInfo(what, extra);
    }
    return true;
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    setState(State.STATE_PREPARED);
    if (VERSION_CODES.M <= Build.VERSION.SDK_INT
        && playbackParameters != null
        && (player.getPlaybackParams() == null
        || player.getPlaybackParams().getSpeed() != playbackParameters.getSpeed())) {
      player.setPlaybackParams(playbackParameters);
    }
    if (callback != null) {
      callback.onPrepared();
    }
  }

  @Override
  public void onSeekComplete(MediaPlayer mp) {
    seeking = false;
    if (callback != null) {
      callback.onSeekComplete();
    }
  }

//  @Override
//  public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//    if (callback != null) {
//      callback.onVideoSizeChanged(width, height);
//    }
//  }

  public enum State {
    STATE_IDLE,
    STATE_INITIALIZED,
    STATE_PREPARING,
    STATE_PREPARED,
    STATE_STARTED,
    STATE_STOPPED,
    STATE_PAUSED,
    STATE_COMPLETED,
    STATE_ERROR,
    STATE_END
  }

  public AudioPlayer(int streamType, Callback callback) {
    this.callback = callback;
    player = new MediaPlayer();
    player.setAudioStreamType(streamType);
    player.setOnCompletionListener(this);
    player.setOnErrorListener(this);
    player.setOnInfoListener(this);
    player.setOnPreparedListener(this);
    player.setOnSeekCompleteListener(this);
//    player.setOnVideoSizeChangedListener(this);
//    player.setOnBufferingUpdateListener(this);
  }

  public void release() {
    setState(State.STATE_END);
    player.release();
    player = null;
  }

  public void reset() {
    if (state != State.STATE_END) {
      setState(State.STATE_IDLE);
      player.reset();
    }
  }

  public void setDataSource(String url) {
    if (state == State.STATE_IDLE) {
      try {
        player.setDataSource(url);
        setState(State.STATE_INITIALIZED);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void setDataSource(Context context, Uri url) {
    if (state == State.STATE_IDLE) {
      try {
        player.setDataSource(context, url);
        setState(State.STATE_INITIALIZED);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void prepareAsync() {
    if (state == State.STATE_INITIALIZED || state == State.STATE_STOPPED) {
      setState(State.STATE_PREPARING);
      player.prepareAsync();
    }
  }

  public void prepare() {
    if (state == State.STATE_INITIALIZED || state == State.STATE_STOPPED) {
      try {
        player.prepare();
        setState(State.STATE_PREPARED);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void start() {
    if (debug) Logger.d("start ");
    if (state == State.STATE_PREPARED || state == State.STATE_STARTED || state == State.STATE_PAUSED || state == State.STATE_COMPLETED) {
      setState(State.STATE_STARTED);
      player.start();
    }
  }

  public void stop() {
    if (debug) Logger.d("stop ");
    if (state == State.STATE_PREPARED || state == State.STATE_STARTED || state == State.STATE_PAUSED || state == State.STATE_STOPPED
        || state == State.STATE_COMPLETED) {
      setState(State.STATE_STOPPED);
      player.stop();
    }
  }

  public void pause() {
    if (debug) Logger.d("pause ");
    if (state == State.STATE_STARTED || state == State.STATE_PAUSED || state == State.STATE_COMPLETED) {
      setState(State.STATE_PAUSED);
      player.pause();
    }
  }

  public boolean isPlaying() {
    if (debug) Logger.d("isPlaying");
    if (state == State.STATE_IDLE || state == State.STATE_INITIALIZED || state == State.STATE_PREPARED || state == State.STATE_STARTED
        || state == State.STATE_PAUSED || state == State.STATE_STOPPED || state == State.STATE_COMPLETED) {
      return player.isPlaying();
    }
    return false;
  }

  public void setLooping(boolean looping) {
    if (state == State.STATE_IDLE || state == State.STATE_INITIALIZED || state == State.STATE_PREPARED || state == State.STATE_STARTED
        || state == State.STATE_PAUSED || state == State.STATE_STOPPED || state == State.STATE_COMPLETED) {
      player.setLooping(looping);
    }
  }

  public boolean isLooping() {
    return player.isLooping();
  }

  public void setVolume(float left, float right) {
    if (state == State.STATE_IDLE || state == State.STATE_INITIALIZED || state == State.STATE_PREPARED || state == State.STATE_STARTED
        || state == State.STATE_PAUSED || state == State.STATE_STOPPED || state == State.STATE_COMPLETED) {
      player.setVolume(left, right);
    }
  }

  @TargetApi(VERSION_CODES.M)
  public void setPlaybackParams(PlaybackParams params) {
    this.playbackParameters = params;
    if (state == State.STATE_INITIALIZED || state == State.STATE_PREPARED || state == State.STATE_STARTED
        || state == State.STATE_PAUSED || state == State.STATE_COMPLETED || state == State.STATE_ERROR) {
      player.setPlaybackParams(params);
    }
  }

  public void seekTo(int msec) {
    if (debug) Logger.d("seekTo " + msec);
    if (state == State.STATE_PREPARED || state == State.STATE_STARTED || state == State.STATE_PAUSED || state == State.STATE_COMPLETED) {
      seeking = true;
      player.seekTo(msec);
    }
  }

  public boolean isSeeking() {
    return seeking;
  }

  public int getCurrentPosition() {
    if (debug) Logger.d("getCurrentPosition ");
    if (/*state == State.STATE_IDLE || */state == State.STATE_INITIALIZED || state == State.STATE_PREPARED || state == State.STATE_STARTED
        || state == State.STATE_PAUSED || state == State.STATE_STOPPED || state == State.STATE_COMPLETED) {
      return player.getCurrentPosition();
    }
    return 0;
  }

  public int getDuration() {
    if (debug) Logger.d("getDuration ");
    if (state == State.STATE_PREPARED || state == State.STATE_STARTED
        || state == State.STATE_PAUSED || state == State.STATE_STOPPED || state == State.STATE_COMPLETED) {
      return player.getDuration();
    }
    return 0;
  }

  public void setDisplay(SurfaceHolder sh) {
    player.setDisplay(sh);
  }

  public void setSurface(Surface surface) {
    player.setSurface(surface);
  }
}
