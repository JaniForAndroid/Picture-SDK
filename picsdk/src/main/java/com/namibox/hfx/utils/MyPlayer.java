package com.namibox.hfx.utils;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import java.io.IOException;

/**
 * Created by sunha on 2015/10/10 0010.
 */
public class MyPlayer {

  private MediaPlayer mediaPlayer = new MediaPlayer();

  public void setDataSource(AssetFileDescriptor fileDescriptor) {
    mediaPlayer.reset();

    try {
      mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
          fileDescriptor.getLength());
      mediaPlayer.prepare();
    } catch (IOException e) {
      e.printStackTrace();
    }
    mediaPlayer.setLooping(true);
    mediaPlayer.start();

  }

  public void start() throws IllegalStateException {
    mediaPlayer.start();
  }

  public void pause() throws IllegalStateException {
    mediaPlayer.pause();
  }

  public void stop() throws IllegalStateException {
    mediaPlayer.stop();
  }

  public void setVolume(float volume) {
    mediaPlayer.setVolume(volume, volume);
  }

  public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  public void release() {
    mediaPlayer.release();
  }
}
