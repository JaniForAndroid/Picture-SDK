package com.example.exoaudioplayer.video.controller;

import android.view.View;
import android.view.animation.Animation;
import android.support.annotation.NonNull;

public interface IControlComponent{

    void attach(@NonNull ControlWrapperI controlWrapper);

    View getView();

    void onVisibilityChanged(boolean isVisible, Animation anim);

    void onPlayStateChanged(int playState);

    void onPlayerStateChanged(int playerState);

    void setProgress(int duration, int position);

    void onLockStateChanged(boolean isLocked);

}
