package com.example.picsdk.music;

import com.namibox.greendao.entity.AudioInfo;

public interface OnAudioClickListener {

  void onAudioPlay(AudioInfo audioInfo);

  void onAudioDownload(AudioInfo audioInfo);
}
