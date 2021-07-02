package com.example.exoaudioplayer.video.base;

import android.content.Context;
import com.example.exoaudioplayer.aduio.base.Constants;
import com.example.exoaudioplayer.video.exo.ExoMediaPlayer;
import com.example.exoaudioplayer.video.media.AndroidVideoPlayer;

/**
 * 创建{@link AndroidVideoPlayer}的工厂类，不推荐，系统的MediaPlayer兼容性较差，建议使用IjkPlayer或者ExoPlayer
 */
public class MediaPlayerFactory extends VideoPlayerFactory<AbstractVideoPlayer> {

  public static MediaPlayerFactory getInstance() {
    return new MediaPlayerFactory();
  }

  @Override
  public AbstractVideoPlayer createPlayer(Context context, int type) {
    if (type == Constants.EXO) {
      return new ExoMediaPlayer(context);
    } else if (type == Constants.MEDIAPLAYER) {
      return new AndroidVideoPlayer(context);
    } else {
      return new AndroidVideoPlayer(context);
    }
  }
}
