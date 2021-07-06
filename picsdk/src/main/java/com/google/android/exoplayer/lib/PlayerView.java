package com.google.android.exoplayer.lib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.picsdk.R;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import java.util.List;

/**
 * Displays a video stream.
 */
@TargetApi(16)
public final class PlayerView extends FrameLayout {

  private final View surfaceView;
  private final ImageView shutterView;
  private final AspectRatioFrameLayout layout;
  private final PlayControlView controller;
  private final ComponentListener componentListener;
  private SimpleExoPlayer player;
  private View volumeLayout;
  private View lockLayout;
  private boolean showLock;
  private ImageView lockView;
  private boolean isLocked;
  private boolean isFullScreen;
  private ProgressBar volumeProgress;
  private TextView volumeTitle;
  private View brightnessLayout;
  private ProgressBar brightnessProgress;
  private TextView brightnessTitle;
  private TextView seekTitle;
  private boolean useController = true;
  private GestureDetector gestureDetector;
  private ViewConfiguration viewConfiguration;
  private AudioManager am;
  private int mBrightness = -1;
  private boolean fastforward;
  private static final int SEEK_SEC = 10;

  public PlayerView(Context context) {
    this(context, null);
  }

  public PlayerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    int surfaceType = 0;
    if (attrs != null) {
      TypedArray a = context.getTheme()
          .obtainStyledAttributes(attrs, R.styleable.SimpleExoPlayerView, 0, 0);
      try {
        useController = a.getBoolean(R.styleable.SimpleExoPlayerView_use_controller, useController);
        surfaceType = a.getInt(R.styleable.SimpleExoPlayerView_surface_type, surfaceType);
      } finally {
        a.recycle();
      }
    }

    LayoutInflater.from(context).inflate(R.layout.player_view, this);
    componentListener = new ComponentListener();
    layout = findViewById(R.id.video_frame);
    layout.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
    controller = findViewById(R.id.control);
    shutterView = findViewById(R.id.shutter);
    volumeLayout = findViewById(R.id.volume_layout);
    volumeProgress = findViewById(R.id.volume);
    volumeTitle = findViewById(R.id.volume_title);
    brightnessLayout = findViewById(R.id.brightness_layout);
    brightnessProgress = findViewById(R.id.brightness);
    brightnessTitle = findViewById(R.id.brightness_title);
    seekTitle = findViewById(R.id.seek_title);
    lockView = findViewById(R.id.lock_unlock);
    lockLayout = findViewById(R.id.lock_unlock_layout);
    lockLayout.setVisibility(showLock ? VISIBLE : GONE);
    lockView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        setLocked(!isLocked);
        if (isLocked) {
          controller.hide();
          showLockView();
        } else {
          controller.show();
          removeCallbacks(hideRunnable);
        }
      }
    });

    View view = surfaceType == 2 ? new TextureView(context) : new SurfaceView(context);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    view.setLayoutParams(params);
    surfaceView = view;
    layout.addView(surfaceView, 0);
    gestureDetector = new GestureDetector(context, gestureListener);
    viewConfiguration = ViewConfiguration.get(context);
    if (!isInEditMode()) {
      am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
    controller.setFastForwardIncrementMs(SEEK_SEC * 1000);
    controller.setRewindIncrementMs(SEEK_SEC * 1000);
    controller.setVisibilityListener(new PlayControlView.VisibilityListener() {
      @Override
      public void onVisibilityChange(boolean visible) {
        if (visibilityListener != null) {
          visibilityListener.onVisibilityChange(visible);
        }
        if (visible) {
          if (isFullScreen) {
            lockView.setVisibility(VISIBLE);
          }
        } else {
          lockView.setVisibility(GONE);
        }
      }
    });
  }

  private void setLocked(boolean locked) {
    isLocked = locked;
    //lockView.setCompoundDrawablesWithIntrinsicBounds(0, isLocked ? R.drawable.ic_lock_open : R.drawable.ic_lock_open, 0, 0);
    lockView.setImageResource(isLocked ? R.drawable.ic_lock : R.drawable.ic_lock_open);
    //lockView.setText(isLocked ? "解锁" : "锁定");
  }

  public boolean isLocked() {
    return isLocked;
  }

  public PlayControlView getController() {
    return controller;
  }

  public void toggleFullScreen(boolean fullScreen) {
    isFullScreen = fullScreen;
    getController().toggleFullScreen(fullScreen);
    if (isFullScreen) {
      lockView.setVisibility(VISIBLE);
    } else {
      lockView.setVisibility(GONE);
      setLocked(false);
    }
  }

  /**
   * Returns the player currently set on this view, or null if no player is set.
   */
  public SimpleExoPlayer getPlayer() {
    return player;
  }

  /**
   * Set the {@link SimpleExoPlayer} to use. The {@link SimpleExoPlayer#setTextOutput} and
   * {@link SimpleExoPlayer#setVideoListener} method of the player will be called and previous
   * assignments are overridden.
   *
   * @param player The {@link SimpleExoPlayer} to use.
   */
  public void setPlayer(SimpleExoPlayer player) {
    if (this.player == player) {
      return;
    }
    if (this.player != null) {
      this.player.removeVideoListener(componentListener);
      this.player.removeTextOutput(componentListener);
      this.player.removeListener(componentListener);
      this.player.setVideoSurface(null);
    }
    this.player = player;
    if (player != null) {
      if (surfaceView instanceof TextureView) {
        player.setVideoTextureView((TextureView) surfaceView);
      } else if (surfaceView instanceof SurfaceView) {
        player.setVideoSurfaceView((SurfaceView) surfaceView);
      }
      player.addVideoListener(componentListener);
      player.addListener(componentListener);
      player.addTextOutput(componentListener);
    } else {
      shutterView.setVisibility(VISIBLE);
    }
    if (useController) {
      controller.setPlayer(player);
    }
  }

  public ImageView getShutterView() {
    return shutterView;
  }

  /**
   * Set the {@code useController} flag which indicates whether the playback control view should
   * be used or not. If set to {@code false} the controller is never visible and is disconnected
   * from the player.
   *
   * @param useController If {@code false} the playback control is never used.
   */
  public void setUseController(boolean useController) {
    if (this.useController == useController) {
      return;
    }
    this.useController = useController;
    if (useController) {
      controller.setPlayer(player);
    } else {
      controller.setPlayer(null);
      controller.setVisibility(GONE);
    }
  }

  /**
   * Sets the resize mode which can be of value {@link AspectRatioFrameLayout#RESIZE_MODE_FIT},
   * {@link AspectRatioFrameLayout#RESIZE_MODE_FIXED_HEIGHT} or
   * {@link AspectRatioFrameLayout#RESIZE_MODE_FIXED_WIDTH}.
   *
   * @param resizeMode The resize mode.
   */
  public void setResizeMode(int resizeMode) {
    layout.setResizeMode(resizeMode);
  }

  public interface OnVideoListener {

    void onVideoSizeChanged(int width, int height);
  }

  private OnVideoListener videoListener;

  public void setVideoListener(OnVideoListener videoListener) {
    this.videoListener = videoListener;
  }

  public interface VisibilityListener {

    void onVisibilityChange(boolean visible);
  }

  private VisibilityListener visibilityListener;

  public void setVisibilityListener(VisibilityListener listener) {
    visibilityListener = listener;
  }

  /**
   * Get the view onto which video is rendered. This is either a {@link SurfaceView} (default)
   * or a {@link TextureView} if the {@code use_texture_view} view attribute has been set to true.
   *
   * @return either a {@link SurfaceView} or a {@link TextureView}.
   */
  public View getVideoSurfaceView() {
    return surfaceView;
  }

  public Bitmap captureVideoShot() {
    if (surfaceView instanceof TextureView) {
      return ((TextureView) surfaceView).getBitmap();
    } else {
      return null;
    }
  }

  public void showLockLayout(boolean show) {
    showLock = show;
  }

  private Runnable hideRunnable = new Runnable() {
    @Override
    public void run() {
      lockView.setVisibility(GONE);
    }
  };

  private void showLockView() {
    if (!isFullScreen) {
      return;
    }
    removeCallbacks(hideRunnable);
    lockView.setVisibility(VISIBLE);
    postDelayed(hideRunnable, 3000);
  }

  private int scrollState = SCROLL_NONE;
  private int scrollIndex = 0;

  private static final int SCROLL_NONE = 0;
  private static final int SCROLL_V = 1;
  private static final int SCROLL_H = 2;

  private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
    @Override
    public boolean onDown(MotionEvent e) {
      return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      if (isLocked) {
        if (lockView.getVisibility() == VISIBLE) {
          lockView.setVisibility(GONE);
        } else {
          showLockView();
        }
        return true;
      }
      if (controller.isVisible()) {
        controller.hide();
      } else {
        controller.show();
      }
      return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      if (isLocked) {
        return true;
      }
      if (scrollState == SCROLL_NONE) {
        if (Math.abs(distanceX) > Math.abs(distanceY)) {
          scrollState = SCROLL_H;
        } else {
          scrollState = SCROLL_V;
        }
        startScroll();
      } else if (scrollState == SCROLL_H) {
        doScroll(e1, distanceX);
      } else {
        doScroll(e1, distanceY);
      }
      return true;
    }
  };

  private void startScroll() {
    Log.e("onScroll", "start scroll: " + scrollState);
    scrollIndex = 0;
  }

  private void doScroll(MotionEvent downEvent, float distance) {
    int sample = scrollIndex % 8;
    scrollIndex++;
    if (sample == 0) {
      return;
    }
    if (Math.abs(distance) >= viewConfiguration.getScaledTouchSlop() / 4) {
      if (scrollState == SCROLL_V) {
        if (downEvent.getX() < getWidth() / 2) {
          scrollVolume(distance > 0);
        } else {
          scrollBrightness(distance > 0);
        }
      } else {
        scrollProgress(distance < 0);
      }
    }
  }

  private void scrollProgress(boolean fastforward) {
    this.fastforward = fastforward;
    if (!seekTitle.isShown()) {
      seekTitle.setVisibility(VISIBLE);
    }
    String text1 = seekTitle.getContext().getString(R.string.player_forward);
    String text2 = seekTitle.getContext().getString(R.string.player_back);
    seekTitle.setText(fastforward ? text1 : text2);
    int d = fastforward ? R.drawable.ic_fast_forward : R.drawable.ic_fast_rewind;
    seekTitle.setCompoundDrawablesWithIntrinsicBounds(d, 0, 0, 0);
  }

  private void scrollVolume(boolean add) {
    am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
        add ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, 0);
    if (!volumeLayout.isShown()) {
      volumeLayout.setVisibility(VISIBLE);
    }
    int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    volumeProgress.setMax(max);
    int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);
    volumeProgress.setProgress(current);
    int d = current <= 0 ? R.drawable.ic_volume_off : R.drawable.ic_volume_up;
    volumeTitle.setCompoundDrawablesWithIntrinsicBounds(d, 0, 0, 0);
  }

  private void scrollBrightness(boolean add) {
    if (!brightnessLayout.isShown()) {
      brightnessLayout.setVisibility(VISIBLE);
    }
    if (mBrightness < 0 || mBrightness > 255) {
      mBrightness = getSystemBrightness();
    }
    mBrightness += add ? 10 : -10;
    if (mBrightness < 0) {
      mBrightness = 0;
    }
    if (mBrightness > 255) {
      mBrightness = 255;
    }
    setScreenBrightness(mBrightness);
    brightnessProgress.setMax(255);
    brightnessProgress.setProgress(mBrightness);
    int d;
    if (mBrightness < 85) {
      d = R.drawable.ic_brightness_low;
    } else if (mBrightness < 170) {
      d = R.drawable.ic_brightness_medium;
    } else {
      d = R.drawable.ic_brightness_high;
    }
    brightnessTitle.setCompoundDrawablesWithIntrinsicBounds(d, 0, 0, 0);
  }

  private void setScreenBrightness(int brightness) {
    if (brightness < 1) {
      return;
    }
    Context context = getContext();
    if (context instanceof Activity) {
      Window localWindow = ((Activity) context).getWindow();
      WindowManager.LayoutParams lp = localWindow.getAttributes();
      lp.screenBrightness = brightness / 255.0f;
      localWindow.setAttributes(lp);
    }
  }

  private int getSystemBrightness() {
    try {
      return Settings.System
          .getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  private void stopScroll() {
    Log.e("onScroll", "stop scroll: " + scrollState);
    if (scrollState == SCROLL_H) {
      if (fastforward) {
        controller.fastForward();
      } else {
        controller.rewind();
      }
    }
    scrollState = SCROLL_NONE;
    if (volumeLayout.isShown()) {
      volumeLayout.setVisibility(GONE);
    }
    if (brightnessLayout.isShown()) {
      brightnessLayout.setVisibility(GONE);
    }
    if (seekTitle.isShown()) {
      seekTitle.setVisibility(GONE);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (useController) {
      if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
        stopScroll();
      }
      return gestureDetector.onTouchEvent(ev);
    }
    return super.onTouchEvent(ev);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    return useController ? controller.dispatchKeyEvent(event) : super.dispatchKeyEvent(event);
  }

  private final class ComponentListener implements SimpleExoPlayer.VideoListener,
      TextRenderer.Output, Player.EventListener {

    // TextRenderer.Output implementation

    @Override
    public void onCues(List<Cue> cues) {
      //subtitleLayout.onCues(cues);
    }

    // SimpleExoPlayer.VideoListener implementation

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
        float pixelWidthHeightRatio) {
      layout.setAspectRatio(height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
      if (videoListener != null) {
        videoListener.onVideoSizeChanged(width, height);
      }
    }

    @Override
    public void onRenderedFirstFrame() {
      shutterView.setVisibility(GONE);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    // ExoPlayer.EventListener implementation

    @Override
    public void onLoadingChanged(boolean isLoading) {
      // Do nothing.
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      if (useController) {
        if (playbackState == Player.STATE_ENDED) {
          setLocked(false);
          controller.show(0);
//                } else if (playbackState == ExoPlayer.STATE_READY) {
//                    if (!isLocked)
//                        controller.show();
        }
      }
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
      // Do nothing.
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPositionDiscontinuity() {
      // Do nothing.
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
      // Do nothing.
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
      // Do nothing.
    }

  }


  public void setLockViewVisibility(int visibility) {
    lockView.setVisibility(visibility);
  }
}
