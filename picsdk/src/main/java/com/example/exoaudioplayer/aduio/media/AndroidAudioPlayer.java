package com.example.exoaudioplayer.aduio.media;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;
import com.example.exoaudioplayer.aduio.base.AbstractAudioPlayer;
import com.example.exoaudioplayer.aduio.base.AudioPlayer;

/**
 * 封装了mediaPlayer相关方法，加入了状态检测
 * Create time: 2018/10/19.
 */
public class AndroidAudioPlayer extends AbstractAudioPlayer {
  private static final String TAG = "AndroidAudioPlayer";
  private boolean debug = false;
  private AudioPlayer player;
  private Context context;
  private int streamType;
  private boolean isReadyPlay = false;
  /**
   * 资源是否准备完成
   */
  private boolean isPrepare;

  private AudioManager mAm;
  private boolean hasFocus;
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

  public void play(Uri uri) {
    if (player == null)
      initializePlayer();
    if (player.getState() == AudioPlayer.State.STATE_PAUSED) {
      player.start();
    } else {
      // 重置mediaPlayer
      player.reset();
      // 重新加载音频资源
//         Uri uri = Uri.parse(musics[current_item]);
//         mediaPlayer.setDataSource(this, uri);
      player.setDataSource(context, uri);
      // 准备播放（同步）-预期准备，因为setDataSource()方法之后，MediaPlayer并未真正的去装载那些音频文件，需要调用prepare()这个方法去准备音频
//         mediaPlayer.prepare();
      // 准备播放（异步）
      player.prepareAsync();
      isReadyPlay = true;
    }
  }

  @Override
  public void pause() {
    if (player != null) {
      player.pause();
    }
  }

  @Override
  public void stop() {
    if (player != null) {
      player.stop();
    }
  }

  public void reset() {
    if (player != null) {
      player.reset();
    }
  }

  @Override
  public boolean isPlaying() {
    if (player != null) {
      return player.isPlaying();
    }
    return false;
  }

  @Override
  public void seekTo(long time) {
    if (player != null) {
      player.seekTo((int) time);
    }
  }

  @Override
  public void releasePlayer() {
    if (player != null) {
      player.release();
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

  @Override
  public void setPlayWhenReady(boolean playWhenReady) {

  }

  @Override
  public void backward() {
    if (player != null) {
      long seekToPosition = player.getCurrentPosition() - 3000;
      if (seekToPosition < 0) {
        seekToPosition = 0;
      }
      seekTo(seekToPosition);
    }
  }

  @Override
  public void fastForward() {
    if (player != null) {
      long seekToPosition = player.getCurrentPosition() + 3000;
      if (seekToPosition > player.getDuration()) {
        seekToPosition = player.getDuration();
      }
      seekTo(seekToPosition);
    }
  }

  @Override
  public void setSpeed(float speed) {

  }

  public AndroidAudioPlayer(Context context, int streamType) {
    this.context = context;
    this.streamType = streamType;
    mAm = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    initializePlayer();
  }

  public void initializePlayer() {
    player = new AudioPlayer(streamType, new AudioPlayer.Callback() {
      @Override
      public void onBufferingUpdate(int percent) {
      }

      @Override
      public void onCompletion() {
        tryGiveUpFocus();

        if (callback != null) {
          callback.onCompletion();
        }
      }

      @Override
      public void onError(int what, int extra) {
        if (callback != null) {
          callback.onError(what, extra);
        }
      }

      @Override
      public void onInfo(int what, int extra) {
        if (callback != null) {
          callback.onInfo(what, extra);
        }
      }

      @Override
      public void onPrepared() {
        if (player != null && isReadyPlay) {
          isReadyPlay = false;
          player.start();
        }

        tryRequestFocus();

        if (callback != null) {
          callback.onPrepared();
        }
      }

      @Override
      public void onSeekComplete() {
        if (callback != null) {
          callback.onSeekComplete();
        }
      }

      @Override
      public void onVideoSizeChanged(int width, int height) {

      }
    });
  }
}
