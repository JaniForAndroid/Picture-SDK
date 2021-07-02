package com.example.picsdk.base;

import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.Nullable;
import com.example.exoaudioplayer.aduio.base.AbstractAudioPlayer;
import com.example.exoaudioplayer.aduio.base.AudioCallBack;
import com.example.exoaudioplayer.aduio.base.AudioPlayerFactory;
import com.example.picsdk.util.AppPicUtil;
import com.namibox.tools.LoggerUtil;
import com.namibox.util.FileUtil;

import java.io.File;
import java.util.List;

public abstract class BaseExerciseFragment extends BaseFragment {

  private AbstractAudioPlayer exoAudioPlayer;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initExoAudioPlayer();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (exoAudioPlayer != null) {
      exoAudioPlayer.releasePlayer();
      exoAudioPlayer.setPlayerCallBack(null);
      exoAudioPlayer = null;
    }
  }

  private void initExoAudioPlayer() {
    exoAudioPlayer = AudioPlayerFactory.getInstance().createPlayer(getActivity(), com.example.exoaudioplayer.aduio.base.Constants.EXO);
    exoAudioPlayer.setPlayerCallBack(new AudioCallBack() {
      @Override
      public void playUpdate(long currentTime, long bufferTime, long totalTime) {
      }

      @Override
      public void playStateChange(boolean playWhenReady, int playbackState) {
        super.playStateChange(playWhenReady, playbackState);
      }
    });
  }

  public void playAudio(String url) {
    if (activity == null || !AppPicUtil.isForeground(activity)) {
      return;
    }
    LoggerUtil.d("playAudio: " + url);
    File file = FileUtil.getCachedFile(activity, url);
    exoAudioPlayer.play(file.exists() ? Uri.fromFile(file) : Uri.parse(url));
  }

  public void playAudioList(List<String> urls) {
    if (activity == null /*|| activity.isPaused*/ || urls.isEmpty()) {
      return;
    }
    Uri[] uris = new Uri[urls.size()];
    for (int i = 0; i < uris.length; i++) {
      String url = urls.get(i);
      LoggerUtil.d("playAudioList: " + url);
      File file = FileUtil.getCachedFile(activity, url);
      uris[i] = file.exists() ? Uri.fromFile(file) : Uri.parse(url);
    }
    exoAudioPlayer.play(uris);
  }

  public void stopAudio() {
    exoAudioPlayer.stop();
  }
}
