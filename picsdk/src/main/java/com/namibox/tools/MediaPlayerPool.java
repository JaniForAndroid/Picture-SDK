package com.namibox.tools;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Create time: 2016/1/9.
 */
public class MediaPlayerPool {

  private static final String TAG = "MediaPlayerPool";
  private HashMap<String, Player> players = new HashMap<>();

  public MediaPlayerPool() {
  }

  public void release() {
    for (Player player : players.values()) {
      player.mediaPlayer.release();
    }
  }

  public void play(String filePath, int loop, float volume) {
    Player player;
    if (!players.containsKey(filePath)) {
      player = new Player(loop, volume);
      players.put(filePath, player);
      player.init(filePath);
    } else {
      player = players.get(filePath);
      player.loop = loop;
      if (player.state == State.STOPPED) {
        Log.d(TAG, "restart:" + filePath);
        player.state = State.PREPARING;
        player.mediaPlayer.prepareAsync();
      } else if (player.state == State.ERROR || player.state == State.IDLE) {
        Log.d(TAG, "restart, need init:" + filePath);
        player.init(filePath);
      } else {
        Log.d(TAG, "already playing:" + filePath);
      }
    }
  }

  public void stop(String filePath) {
    if (players.containsKey(filePath)) {
      Player player = players.get(filePath);
      if (player.state == State.STARTED) {
        player.mediaPlayer.stop();
      }
      player.mediaPlayer.reset();
      player.state = State.IDLE;
      Log.d(TAG, "stop, reset to idle:" + filePath);
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

    public void init(String filePath) {
      try {
        Log.d(TAG, "init:" + filePath);
        FileInputStream stream = new FileInputStream(filePath);
        mediaPlayer.setDataSource(stream.getFD());
        stream.close();
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
