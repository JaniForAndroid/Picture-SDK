package com.example.exoaudioplayer.aduio.listener;

import com.google.android.exoplayer2.ExoPlaybackException;

public interface AudioPlayerListener {

    void onFocusChange(boolean hasFocus);

    //exo callback
    void playUpdate(long currentTime, long bufferTime, long totalTime);

    void playStateChange(boolean playWhenReady, int playbackState);

    void playError(ExoPlaybackException error);

    void speedChanged(float speed);

    //media callback
    void onPrepared();

    void onCompletion();

    void onError(int what, int extra);

    void onInfo(int what, int extra);

    void onSeekComplete();
}
