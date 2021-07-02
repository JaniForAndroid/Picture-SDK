package com.example.exoaudioplayer.video.base;

import android.content.Context;
import com.example.exoaudioplayer.video.media.AndroidVideoPlayer;

/**
 * 此接口使用方法：
 * 1.继承{@link AbstractVideoPlayer}扩展自己的播放器。
 * 2.继承此接口并实现{@link #createPlayer(Context,int)}，返回步骤1中的播放器。
 * 可参照{@link AndroidVideoPlayer}和{@link MediaPlayerFactory}的实现。
 */
public abstract class VideoPlayerFactory<P extends AbstractVideoPlayer> {

    public abstract P createPlayer(Context context, int type);
}
