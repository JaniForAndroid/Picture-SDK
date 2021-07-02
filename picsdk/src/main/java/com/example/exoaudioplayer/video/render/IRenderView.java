package com.example.exoaudioplayer.video.render;

import android.graphics.Bitmap;
import android.view.View;
import android.support.annotation.NonNull;
import com.example.exoaudioplayer.video.base.AbstractVideoPlayer;


public interface IRenderView {

    /**
     * 关联AbstractPlayer
     */
    void attachToPlayer(@NonNull AbstractVideoPlayer player);

    /**
     * 设置视频宽高
     * @param videoWidth 宽
     * @param videoHeight 高
     */
    void setVideoSize(int videoWidth, int videoHeight);

    /**
     * 设置视频旋转角度
     * @param degree 角度值
     */
    void setVideoRotation(int degree);

    /**
     * 设置screen scale type
     * @param scaleType 类型
     */
    void setScaleType(int scaleType);

    /**
     * 获取真实的RenderView
     */
    View getView();

    /**
     * 截图
     */
    Bitmap doScreenShot();

    /**
     * 释放资源
     */
    void release();

}