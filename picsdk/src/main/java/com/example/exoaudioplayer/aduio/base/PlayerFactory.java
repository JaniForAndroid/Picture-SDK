package com.example.exoaudioplayer.aduio.base;

import android.content.Context;
import com.example.exoaudioplayer.aduio.exo.ExoAudioPlayer;

/**
 * 此接口使用方法：
 * 1.继承{@link AbstractAudioPlayer}扩展自己的播放器。
 * 2.继承此接口并实现{@link #createPlayer(Context, int)}，返回步骤1中的播放器。
 * 可参照{@link ExoAudioPlayer}和{@link AudioPlayerFactory}的实现。
 */
public abstract class PlayerFactory<P extends AbstractAudioPlayer> {

    public abstract P createPlayer(Context context, int type);
}
