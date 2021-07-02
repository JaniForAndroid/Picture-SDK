package com.example.exoaudioplayer.video.component;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.exoaudioplayer.video.base.Constants;
import com.example.exoaudioplayer.video.controller.ControlWrapperI;
import com.example.exoaudioplayer.video.controller.IControlComponent;
import com.example.exoaudioplayer.video.util.PlayerUtils;
import com.example.picsdk.R;

/**
 * 自动播放完成界面
 */
public class PlayerCompleteView extends FrameLayout implements IControlComponent {

    private ControlWrapperI mControlWrapper;

    private ImageView mStopFullscreen;

    public PlayerCompleteView(@NonNull Context context) {
        super(context);
    }

    public PlayerCompleteView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerCompleteView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.player_layout_complete_view, this, true);
        findViewById(R.id.iv_replay).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mControlWrapper.replay(true);
            }
        });
        mStopFullscreen = findViewById(R.id.stop_fullscreen);
        mStopFullscreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mControlWrapper.isFullScreen()) {
                    Activity activity = PlayerUtils.scanForActivity(getContext());
                    if (activity != null && !activity.isFinishing()) {
                        if (mControlWrapper.isAlwaysFullScreen()) {
                            activity.onBackPressed();
                        } else {
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            mControlWrapper.stopFullScreen();
                        }
                    }
                }
            }
        });
        setClickable(true);
    }

    @Override
    public void attach(@NonNull ControlWrapperI controlWrapper) {
        mControlWrapper = controlWrapper;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {

    }

    @Override
    public void onPlayStateChanged(int playState) {
        boolean isPlaying = mControlWrapper.isPlaying();
        if (playState == Constants.STATE_PLAYBACK_COMPLETED) {
            setVisibility(VISIBLE);
            mStopFullscreen.setVisibility(mControlWrapper.isFullScreen() ? VISIBLE : GONE);
            bringToFront();
        } else {
            setVisibility(GONE);
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        if (playerState == Constants.PLAYER_FULL_SCREEN) {
            mStopFullscreen.setVisibility(VISIBLE);
        } else if (playerState == Constants.PLAYER_NORMAL) {
            mStopFullscreen.setVisibility(GONE);
        }
    }

    @Override
    public void setProgress(int duration, int position) {

    }

    @Override
    public void onLockStateChanged(boolean isLock) {

    }
}
