package com.namibox.tools;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import com.namibox.util.Logger;
import java.io.IOException;
import java.util.HashMap;

/**
 * Create time: 2016/1/9.
 */
public class AssetsMediaPlayerPool {

  private static final String TAG = "AssetsMediaPlayerPool";
  private HashMap<String, Player> players = new HashMap<>();

  public AssetsMediaPlayerPool() {
  }

  public void release() {
    Logger.d("AssetsMediaPlayerPool Player release");
    for (Player player : players.values()) {
      player.mediaPlayer.release();
    }
  }

  public void play(Context context, String assetName, int loop, float volume) {
    Player player = null;
    try {
      if (!players.containsKey(assetName)) {
        player = new Player(loop, volume);
        players.put(assetName, player);
        player.init(context, assetName);
      } else {
        player = players.get(assetName);
        player.loop = loop;
        if (player.state == State.STOPPED) {
          Log.d(TAG, "restart:" + assetName);
          player.state = State.PREPARING;
          player.mediaPlayer.prepareAsync();
        } else if (player.state == State.ERROR || player.state == State.IDLE) {
          Log.d(TAG, "restart, need init:" + assetName);
          player.init(context, assetName);
        } else {
          Log.d(TAG, "already playing:" + assetName);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (player != null) {
        player.state = State.ERROR;
      }
    }

  }

  public void stopAll() {
    for (Player player : players.values()) {
      stop(player);
    }
  }

  public void setVolumeAll(float volume) {
    for (Player player : players.values()) {
      player.mediaPlayer.setVolume(volume, volume);
    }
  }

  protected void stop(Player player) {
    try {
      if (player.state != State.INITIALIZED && player.state != State.IDLE
          && player.state != State.ERROR) {
        player.mediaPlayer.stop();
      }
      player.mediaPlayer.reset();
      player.state = State.IDLE;
    } catch (Exception e) {
      e.printStackTrace();
      Logger.d("AssetsMediaPlayerPool player reset exception");
    }
  }

  public void stop(String assetName) {
    if (players.containsKey(assetName)) {
      Player player = players.get(assetName);
      if (player.state == State.STARTED) {
        player.mediaPlayer.stop();
      }
      player.mediaPlayer.reset();
      player.state = State.IDLE;
      Log.d(TAG, "stop, reset to idle:" + assetName);
    }
  }

  public void setVolume(String filePath, float volume) {
    if (players.containsKey(filePath)) {
      Player player = players.get(filePath);
      player.mediaPlayer.setVolume(volume, volume);
    }
  }

  enum State {
    IDLE,
    INITIALIZED,
    PREPARING,
    PREPARED,
    STARTED,
    PAUSED,
    STOPPED,
    COMPLETED,
    ERROR
  }

  static class Player implements MediaPlayer.OnCompletionListener,
      MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

    MediaPlayer mediaPlayer;
    int loop;//0:no loop, -1:continue loop, N:loop time
    State state;

    public Player(int loop, float volume) {
      this.loop = loop;
      state = State.IDLE;
      mediaPlayer = new MediaPlayer();
      mediaPlayer.setVolume(volume, volume);
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mediaPlayer.setOnCompletionListener(this);
      mediaPlayer.setOnErrorListener(this);
      mediaPlayer.setOnPreparedListener(this);
    }

    public void init(Context context, String assetName) {
      try {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(assetName);
        mediaPlayer
            .setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                fileDescriptor.getLength());
        state = State.PREPARING;
        mediaPlayer.prepareAsync();
      } catch (IOException e) {
        Log.e(TAG, "error:" + e.getMessage());
        state = State.ERROR;
      }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
      if (state == State.ERROR) {
        Log.e(TAG, "error, just return");
        return;
      }
      if (loop == -1) {
        Log.d(TAG, "completed, loop");
        mp.start();
        state = State.STARTED;
      } else if (loop > 0) {
        Log.d(TAG, "completed, loop=" + loop);
        loop--;
        mp.start();
        state = State.STARTED;
      } else {
        Log.d(TAG, "completed, no loop, stop");
        mp.stop();
        state = State.STOPPED;
      }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
      Log.e(TAG, "onError: what=" + what + " extra=" + extra);
      state = State.ERROR;
      return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
      Log.d(TAG, "prepared, start");
      mp.start();
      state = State.STARTED;
    }
  }

}
