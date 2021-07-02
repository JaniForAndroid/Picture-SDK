package com.example.exoaudioplayer.aduio.base;

import android.content.Context;
import android.media.AudioManager;
import com.example.exoaudioplayer.aduio.exo.ExoAudioPlayer;
import com.example.exoaudioplayer.aduio.media.AndroidAudioPlayer;
import com.namibox.util.network.NetWorkHelper;
import java.util.Collections;
import okhttp3.Protocol;

public class AudioPlayerFactory extends PlayerFactory<AbstractAudioPlayer> {

  public static AudioPlayerFactory getInstance() {
    return new AudioPlayerFactory();
  }

  @Override
  public AbstractAudioPlayer createPlayer(Context context, int type) {
    if (type == Constants.EXO) {
      return new ExoAudioPlayer(context,
          NetWorkHelper.getOkHttpBuilder().protocols(Collections.singletonList(Protocol.HTTP_1_1))
              .build(),
          NetWorkHelper.getInstance().getUa());
    } else if (type == Constants.MEDIAPLAYER) {
      return new AndroidAudioPlayer(context, AudioManager.STREAM_MUSIC);
    } else {
      return new ExoAudioPlayer(context,
          NetWorkHelper.getOkHttpBuilder().protocols(Collections.singletonList(Protocol.HTTP_1_1))
              .build(),
          NetWorkHelper.getInstance().getUa());
    }
  }
}
