package com.example.exoaudioplayer.video.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.exoaudioplayer.video.controller.ControlWrapperI;
import com.example.exoaudioplayer.video.controller.IControlComponent;
import com.example.picsdk.R;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 微课进度星级显示
 */
public class PlayerLogoView extends FrameLayout implements IControlComponent {

  //logo配置
  private static final int LOGO_TYPE_VIDEO = 0x01;
  private static final int LOGO_TYPE_IMG = 0x02;
  private String logo_url;
  private int logo_type;
  private float logo_x;
  private float logo_y;
  private float logo_width;
  private float logo_height;
  private JsonObject remoteLogo;
  private boolean remoteLocalAdded;


  public PlayerLogoView(@NonNull Context context) {
    super(context);
  }

  public PlayerLogoView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public PlayerLogoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  {
    setVisibility(VISIBLE);
    LayoutInflater.from(getContext()).inflate(R.layout.player_layout_logo_view, this, true);
  }

  public void setupLogo(String remoteLogo, boolean localLogo) {
    if (localLogo) {
      findViewById(R.id.wx_logo).setVisibility(VISIBLE);
      return;
    }
    if (remoteLogo != null) {
      this.remoteLogo = new JsonParser().parse(remoteLogo).getAsJsonObject();
      logo_type = this.remoteLogo.get("assets_type").getAsInt();
      logo_url = this.remoteLogo.get("assets_url").getAsString();
      JsonObject position = this.remoteLogo.get("point").getAsJsonObject();
      logo_x = position.get("x").getAsFloat();
      logo_y = position.get("y").getAsFloat();
      logo_width = position.get("width").getAsFloat();
      logo_height = position.get("height").getAsFloat();
    }
  }

  @Override
  protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    if (!remoteLocalAdded && remoteLogo != null) {
      if (logo_type == LOGO_TYPE_VIDEO) {
        int width = (int) (w * logo_width);
        int height = (int) (h * logo_height);
        MarginLayoutParams layoutParams = new MarginLayoutParams(width, height);
        layoutParams.leftMargin = (int) (w * logo_x);
        layoutParams.topMargin = (int) (h * logo_y);
        VideoView logoVideoView = new VideoView(getContext());
        addView(logoVideoView, layoutParams);
        logoVideoView.setVideoURI(Uri.parse(logo_url));
        logoVideoView.start();
        logoVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

          @Override
          public void onPrepared(MediaPlayer mp) {
            mp.start();
            mp.setLooping(true);
          }
        });
      } else if (logo_type == LOGO_TYPE_IMG) {
        Glide.with(getContext()).load(logo_url).into(new SimpleTarget<Drawable>() {
          @Override
          public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
            int intrinsicWidth = resource.getIntrinsicWidth();
            int intrinsicHeight = resource.getIntrinsicHeight();
            int width = (int) (w * logo_width);
            float imageRatio = intrinsicHeight * 1.0f / intrinsicWidth;
            int height = (int) ((imageRatio) * width);
            MarginLayoutParams layoutParams = new MarginLayoutParams(width, height);
            layoutParams.leftMargin = (int) (w * logo_x);
            layoutParams.topMargin = (int) (h * logo_y);
            ImageView logoImage = new ImageView(getContext());
            logoImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            addView(logoImage, layoutParams);
            Glide.with(getContext()).load(logo_url).into(logoImage);
          }
        });
      }
      remoteLocalAdded = true;
    }
  }

  @Override
  public void attach(@NonNull ControlWrapperI controlWrapper) {
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
  }

  @Override
  public void onPlayerStateChanged(int playerState) {
  }

  @Override
  public void setProgress(int duration, int position) {

  }

  @Override
  public void onLockStateChanged(boolean isLock) {
  }
}
