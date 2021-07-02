package com.example.exoaudioplayer.video.base;

import static com.example.exoaudioplayer.video.base.Constants.STATE_ERROR;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.exoaudioplayer.video.controller.BaseVideoController;
import com.example.exoaudioplayer.video.controller.IMediaPlayerControl;
import com.example.exoaudioplayer.video.render.CreateRenderViewFactory;
import com.example.exoaudioplayer.video.render.IRenderView;
import com.example.exoaudioplayer.video.render.RenderViewFactory;
import com.example.exoaudioplayer.video.util.PlayerUtils;
import com.example.picsdk.R;
import com.google.android.exoplayer2.ExoPlaybackException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 播放器
 */

public class VideoView<P extends AbstractVideoPlayer> extends FrameLayout
    implements IMediaPlayerControl, AbstractVideoPlayer.PlayerEventListener {

  protected AbstractVideoPlayer mMediaPlayer;//播放器
  private int mType = Constants.MEDIAPLAYER;
  @Nullable
  protected BaseVideoController mVideoController;//控制器

  /**
   * 真正承载播放器视图的容器
   */
  protected FrameLayout mPlayerContainer;

  protected IRenderView mRenderView;
  protected RenderViewFactory mRenderViewFactory;


  protected int mCurrentScreenScaleType;

  protected int[] mVideoSize = {0, 0};

  protected boolean mIsMute;//是否静音
  protected float mSpeed = 1.0f;
  private long mSize = 0;
  private long mDuration = 0;

  //--------- data sources ---------//
  protected String mUrl;//当前播放视频的地址
  protected Map<String, String> mHeaders;//当前视频地址的请求头
  protected AssetFileDescriptor mAssetFileDescriptor;//assets文件

  protected long mCurrentPosition;//当前正在播放视频的位置


  protected int mCurrentPlayState = Constants.STATE_IDLE;//当前播放器的状态
  protected int mCurrentPlayerState = Constants.PLAYER_NORMAL;

  protected boolean mIsFullScreen;//是否处于全屏状态
  protected boolean mIsAlwaysFullScreen;//是否默认为全屏状态，且保持全屏

  protected int[] mTinyScreenSize = {0, 0};

  /**
   * 监听系统中音频焦点改变，见{@link #setEnableAudioFocus(boolean)}
   */
  protected boolean mEnableAudioFocus;
  @Nullable
  protected AudioFocusHelper mAudioFocusHelper;

  /**
   * OnStateChangeListener集合，保存了所有开发者设置的监听器
   */
  protected List<OnStateChangeListener> mOnStateChangeListeners;

  /**
   * 进度管理器，设置之后播放器会记录播放进度，以便下次播放恢复进度
   */
  @Nullable
  protected ProgressManager mProgressManager;

  /**
   * 循环播放
   */
  protected boolean mIsLooping;

  /**
   * {@link #mPlayerContainer}背景色，默认黑色
   */
  private int mPlayerBackgroundColor;

  public VideoView(@NonNull Context context) {
    this(context, null);
  }

  public VideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public VideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    //读取全局配置
    VideoViewConfig config = VideoViewManager.getConfig();
    mEnableAudioFocus = config.mEnableAudioFocus;
    mProgressManager = config.mProgressManager;
    mCurrentScreenScaleType = config.mScreenScaleType;

    //读取xml中的配置，并综合全局配置
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VideoView);
    mEnableAudioFocus = a.getBoolean(R.styleable.VideoView_enableAudioFocus, mEnableAudioFocus);
    mIsLooping = a.getBoolean(R.styleable.VideoView_looping, false);
    mCurrentScreenScaleType = a.getInt(R.styleable.VideoView_screenScaleType, mCurrentScreenScaleType);
    mPlayerBackgroundColor = a.getColor(R.styleable.VideoView_playerBackgroundColor, Color.BLACK);
    a.recycle();

    initView();
  }

  /**
   * 初始化播放器视图
   */
  protected void initView() {
    mPlayerContainer = new FrameLayout(getContext());
    mPlayerContainer.setBackgroundColor(mPlayerBackgroundColor);
    LayoutParams params = new LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    this.addView(mPlayerContainer, params);
  }

  /**
   * 初始化播放器
   */
  protected void initPlayer() {
    mMediaPlayer = MediaPlayerFactory.getInstance().createPlayer(getContext(), mType);
    mMediaPlayer.setPlayerEventListener(this);
    setInitOptions();
    mMediaPlayer.initPlayer();
    setOptions();
  }

  public AbstractVideoPlayer getPlayer() {
    return mMediaPlayer;
  }

  /**
   * 初始化之前的配置项
   */
  protected void setInitOptions() {
  }

  /**
   * 初始化之后的配置项
   */
  protected void setOptions() {
    mMediaPlayer.setLooping(mIsLooping);
  }

  /**
   * 初始化视频渲染View
   */
  protected void addDisplay() {
    if (mRenderView != null) {
      mPlayerContainer.removeView(mRenderView.getView());
      mRenderView.release();
    }
    mRenderView = CreateRenderViewFactory.getInstance().createRenderView(getContext(), Constants.TEXTURE);
    mRenderView.attachToPlayer(mMediaPlayer);
    LayoutParams params = new LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
        Gravity.CENTER);
    mPlayerContainer.addView(mRenderView.getView(), 0, params);
  }

  public View getView() {
    if (mRenderView == null)
      return null;
    return mRenderView.getView();
  }

  /**
   * 设置{@link #mPlayerContainer}的背景色
   */
  public void setPlayerBackgroundColor(int color) {
    mPlayerContainer.setBackgroundColor(color);
  }

  /**
   * 开始播放，注意：调用此方法后必须调用{@link #release()}释放播放器，否则会导致内存泄漏
   */
  @Override
  public void start() {
    boolean isStarted = false;
    if (isInIdleState() || isInStartAbortState()) {
      isStarted = startPlay();
    } else if (isInPlaybackState()) {
      startInPlaybackState();
      isStarted = true;
    }
    if (isStarted) {
      mPlayerContainer.setKeepScreenOn(true);
//      if (mAudioFocusHelper != null)
//        mAudioFocusHelper.requestFocus();
    }
  }

  /**
   * 第一次播放
   *
   * @return 是否成功开始播放
   */
  protected boolean startPlay() {
    //如果要显示移动网络提示则不继续播放
    if (showNetWarning()) {
      //中止播放
      setPlayState(Constants.STATE_START_ABORT);
      return false;
    }
    //监听音频焦点改变
    if (mEnableAudioFocus) {
      mAudioFocusHelper = new AudioFocusHelper(this);
    }
    //读取播放进度
    if (mProgressManager != null) {
      mCurrentPosition = mProgressManager.getSavedProgress(mUrl);
    }
    initPlayer();
    addDisplay();
    startPrepare(false);
    return true;
  }

  /**
   * 是否显示移动网络提示，可在Controller中配置
   */
  protected boolean showNetWarning() {
    //播放本地数据源时不检测网络
    if (isLocalDataSource()) return false;
    return mVideoController != null && mVideoController.showNetWarning();
  }

  /**
   * 判断是否为本地数据源，包括 本地文件、Asset、raw
   */
  public boolean isLocalDataSource() {
    if (mAssetFileDescriptor != null) {
      return true;
    } else if (!TextUtils.isEmpty(mUrl)) {
      Uri uri = Uri.parse(mUrl);
      return ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())
          || ContentResolver.SCHEME_FILE.equals(uri.getScheme())
          || "rawresource".equals(uri.getScheme());
    }
    return false;
  }

  /**
   * 开始准备播放（直接播放）
   */
  protected void startPrepare(boolean reset) {
    if (reset) {
      mMediaPlayer.reset();
      //重新设置option，media player reset之后，option会失效
      setOptions();
    }
    if (prepareDataSource()) {
      mMediaPlayer.prepareAsync();
      setPlayState(Constants.STATE_PREPARING);
      setPlayerState(isFullScreen() ? Constants.PLAYER_FULL_SCREEN : Constants.PLAYER_NORMAL);
      mMediaPlayer.setSpeed(mSpeed);
    }
  }

  /**
   * 设置播放数据
   *
   * @return 播放数据是否设置成功
   */
  protected boolean prepareDataSource() {
    if (mAssetFileDescriptor != null) {
      mMediaPlayer.setDataSource(mAssetFileDescriptor);
      return true;
    } else if (!TextUtils.isEmpty(mUrl)) {
      mMediaPlayer.setDataSource(mUrl, mHeaders);
      return true;
    }
    return false;
  }

  /**
   * 播放状态下开始播放
   */
  protected void startInPlaybackState() {
    mMediaPlayer.start();
    setPlayState(Constants.STATE_PLAYING);
  }

  /**
   * 暂停播放
   */
  @Override
  public void pause() {
    if (isInPlaybackState()
        && mMediaPlayer.isPlaying()) {
      mMediaPlayer.pause();
      setPlayState(Constants.STATE_PAUSED);
      if (mAudioFocusHelper != null) {
        mAudioFocusHelper.abandonFocus();
      }
      mPlayerContainer.setKeepScreenOn(false);
    }
  }

  /**
   * 继续播放
   */
  public void resume() {
    if (isInPlaybackState()
        && !mMediaPlayer.isPlaying()) {
      mMediaPlayer.start();
      setPlayState(Constants.STATE_PLAYING);
      if (mAudioFocusHelper != null) {
        mAudioFocusHelper.requestFocus();
      }
      mPlayerContainer.setKeepScreenOn(true);
    }
  }

  /**
   * 释放播放器
   */
  public void release() {
    if (!isInIdleState()) {
      //释放播放器
      if (mMediaPlayer != null) {
        mMediaPlayer.release();
        mMediaPlayer = null;
      }
      //释放renderView
      if (mRenderView != null) {
        mPlayerContainer.removeView(mRenderView.getView());
        mRenderView.release();
        mRenderView = null;
      }
      //释放Assets资源
      if (mAssetFileDescriptor != null) {
        try {
          mAssetFileDescriptor.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      //关闭AudioFocus监听
      if (mAudioFocusHelper != null) {
        mAudioFocusHelper.abandonFocus();
        mAudioFocusHelper = null;
      }
      //关闭屏幕常亮
      mPlayerContainer.setKeepScreenOn(false);
      //保存播放进度
      saveProgress();
      //重置播放进度
      mCurrentPosition = 0;
      //切换转态
      setPlayState(Constants.STATE_IDLE);
    }
  }

  /**
   * 保存播放进度
   */
  protected void saveProgress() {
    if (mProgressManager != null && mCurrentPosition > 0) {
      Log.d("VideoVIew", "saveProgress: " + mCurrentPosition);
      mProgressManager.saveProgress(mUrl, mCurrentPosition);
    }
  }

  /**
   * 是否处于播放状态
   */
  protected boolean isInPlaybackState() {
    return mMediaPlayer != null
        && mCurrentPlayState != STATE_ERROR
        && mCurrentPlayState != Constants.STATE_IDLE
        && mCurrentPlayState != Constants.STATE_PREPARING
        && mCurrentPlayState != Constants.STATE_START_ABORT
        && mCurrentPlayState != Constants.STATE_PLAYBACK_COMPLETED;
  }

  /**
   * 是否处于未播放状态
   */
  protected boolean isInIdleState() {
    return mCurrentPlayState == Constants.STATE_IDLE;
  }

  /**
   * 播放中止状态
   */
  private boolean isInStartAbortState() {
    return mCurrentPlayState == Constants.STATE_START_ABORT;
  }

  /**
   * 重新播放
   *
   * @param resetPosition 是否从头开始播放
   */
  @Override
  public void replay(boolean resetPosition) {
    if (resetPosition) {
      mCurrentPosition = 0;
    }
    addDisplay();
    startPrepare(true);
    mPlayerContainer.setKeepScreenOn(true);
  }

  /**
   * 获取视频总时长
   */
  @Override
  public long getDuration() {
    if (isInPlaybackState()) {
      return mMediaPlayer.getDuration();
    }
    return 0;
  }

  /**
   * 获取视频总时长, 当未加载视频时
   */
  @Override
  public long getDuration1() {
    return mDuration;
  }

  public void setDuration1(long duration1) {
    mDuration = duration1 * 1000;
  }

  /**
   * 获取视频大小
   */
  @Override
  public long getSize() {
    return mSize;
  }

  public void setSize(long size) {
    mSize = size;
  }

  /**
   * 获取当前播放的位置
   */
  @Override
  public long getCurrentPosition() {
    if (isInPlaybackState()) {
      mCurrentPosition = mMediaPlayer.getCurrentPosition();
      return mCurrentPosition;
    }
    return 0;
  }

  /**
   * 调整播放进度
   */
  @Override
  public void seekTo(long pos) {
    if (isInPlaybackState()) {
      mMediaPlayer.seekTo(pos);
    }
  }

  /**
   * 是否处于播放状态
   */
  @Override
  public boolean isPlaying() {
    return isInPlaybackState() && mMediaPlayer.isPlaying();
  }

  @Override
  public boolean isEnded() {
    return isInIdleState();
  }

  /**
   * 获取当前缓冲百分比
   */
  @Override
  public int getBufferedPercentage() {
    return mMediaPlayer != null ? mMediaPlayer.getBufferedPercentage() : 0;
  }

  /**
   * 设置静音
   */
  @Override
  public void setMute(boolean isMute) {
    if (mMediaPlayer != null) {
      this.mIsMute = isMute;
      float volume = isMute ? 0.0f : 1.0f;
      mMediaPlayer.setVolume(volume, volume);
    }
  }

  /**
   * 是否处于静音状态
   */
  @Override
  public boolean isMute() {
    return mIsMute;
  }

  /**
   * 视频播放出错回调
   */
  @Override
  public void onError() {
    mPlayerContainer.setKeepScreenOn(false);
    setPlayState(STATE_ERROR);

    //防止无网络情况下，部分机型的mediaplayer会疯狂请求代理缓存接口，导致卡顿
    if (mMediaPlayer != null)
      mMediaPlayer.reset();
  }

  /**
   * 视频播放完成回调
   */
  @Override
  public void onCompletion() {
    int state = getCurrentPlayState();
    if (getCurrentPlayState() != STATE_ERROR) {
      mPlayerContainer.setKeepScreenOn(false);
      mCurrentPosition = 0;
      if (mProgressManager != null) {
        //播放完成，清除进度
        mProgressManager.saveProgress(mUrl, 0);
      }
      setPlayState(Constants.STATE_PLAYBACK_COMPLETED);
    }
  }

  @Override
  public void onInfo(int what, int extra) {
    switch (what) {
      case AbstractVideoPlayer.MEDIA_INFO_BUFFERING_START:
        setPlayState(Constants.STATE_BUFFERING);
        break;
      case AbstractVideoPlayer.MEDIA_INFO_BUFFERING_END:
        setPlayState(Constants.STATE_BUFFERED);
        break;
      case AbstractVideoPlayer.MEDIA_INFO_VIDEO_RENDERING_START: // 视频开始渲染
        setPlayState(Constants.STATE_PLAYING);
        if (mAudioFocusHelper != null)
          mAudioFocusHelper.requestFocus();
        if (mPlayerContainer.getWindowVisibility() != VISIBLE) {
          pause();
        }
        break;
      case AbstractVideoPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
        if (mRenderView != null)
          mRenderView.setVideoRotation(extra);
        break;
    }
  }

  /**
   * 视频缓冲完毕，准备开始播放时回调
   */
  @Override
  public void onPrepared() {
    setPlayState(Constants.STATE_PREPARED);
    if (mCurrentPosition > 0) {
      seekTo(mCurrentPosition);
    }
  }

  /**
   * 获取当前播放器的状态
   */
  public int getCurrentPlayerState() {
    return mCurrentPlayerState;
  }

  /**
   * 获取当前的播放状态
   */
  public int getCurrentPlayState() {
    return mCurrentPlayState;
  }

  /**
   * 获取缓冲速度
   */
  @Override
  public long getTcpSpeed() {
    return mMediaPlayer != null ? mMediaPlayer.getTcpSpeed() : 0;
  }

  /**
   * 设置播放速度
   */
  @Override
  public void setSpeed(float speed) {
    mSpeed = speed;
    if (isInPlaybackState()) {
      mMediaPlayer.setSpeed(speed);
      setPlayState(Constants.STATE_PLAYING);
    }
  }

  @Override
  public float getSpeed() {
    if (isInPlaybackState()) {
      return mMediaPlayer.getSpeed();
    }
    return 1f;
  }

  /**
   * 设置视频地址
   */
  public void setUrl(String url) {
    setUrl(url, null);
  }

  /**
   * 设置包含请求头信息的视频地址
   *
   * @param url     视频地址
   * @param headers 请求头
   */
  public void setUrl(String url, Map<String, String> headers) {
    mAssetFileDescriptor = null;
    mUrl = url;
    mHeaders = headers;
  }

  public String getUri() {
    return mUrl;
  }

  /**
   * 用于播放assets里面的视频文件
   */
  public void setAssetFileDescriptor(AssetFileDescriptor fd) {
    mUrl = null;
    this.mAssetFileDescriptor = fd;
  }

  /**
   * 一开始播放就seek到预先设置好的位置
   */
  public void skipPositionWhenPlay(int position) {
    this.mCurrentPosition = position * 1000;
  }

  /**
   * 设置音量 0.0f-1.0f 之间
   *
   * @param v1 左声道音量
   * @param v2 右声道音量
   */
  public void setVolume(float v1, float v2) {
    if (mMediaPlayer != null) {
      mMediaPlayer.setVolume(v1, v2);
    }
  }

  /**
   * 设置进度管理器，用于保存播放进度
   */
  public void setProgressManager(@Nullable ProgressManager progressManager) {
    this.mProgressManager = progressManager;
  }

  /**
   * 循环播放， 默认不循环播放
   */
  public void setLooping(boolean looping) {
    mIsLooping = looping;
    if (mMediaPlayer != null) {
      mMediaPlayer.setLooping(looping);
    }
  }

  /**
   * 是否开启AudioFocus监听， 默认开启，用于监听其它地方是否获取音频焦点，如果有其它地方获取了
   * 音频焦点，此播放器将做出相应反应，具体实现见{@link AudioFocusHelper}
   */
  public void setEnableAudioFocus(boolean enableAudioFocus) {
    mEnableAudioFocus = enableAudioFocus;
  }

  /**
   * 设置播放器内核 exo or media， 默认media
   */
  public void setPlayerType(int type) {
    mType = type;
  }

  /**
   * 进入全屏
   */
  @Override
  public void startFullScreen() {
    if (mIsFullScreen)
      return;

    ViewGroup decorView = getDecorView();
    if (decorView == null)
      return;

    mIsFullScreen = true;

    //隐藏NavigationBar和StatusBar
    hideSysBar(decorView);

    //从当前FrameLayout中移除播放器视图
    this.removeView(mPlayerContainer);
    //将播放器视图添加到DecorView中即实现了全屏
    decorView.addView(mPlayerContainer);

    setPlayerState(Constants.PLAYER_FULL_SCREEN);
  }

  private void hideSysBar(ViewGroup decorView) {
    int uiOptions = decorView.getSystemUiVisibility();

//    隐藏虚拟键盘
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }
    decorView.setSystemUiVisibility(uiOptions);
    getActivity().getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }

  @Override
  public void onWindowFocusChanged(boolean hasWindowFocus) {
    super.onWindowFocusChanged(hasWindowFocus);
    if (hasWindowFocus && mIsFullScreen) {
      //重新获得焦点时保持全屏状态
      hideSysBar(getDecorView());
    }
  }

  /**
   * 退出全屏
   */
  @Override
  public void stopFullScreen() {
    if (!mIsFullScreen)
      return;

    ViewGroup decorView = getDecorView();
    if (decorView == null)
      return;

    mIsFullScreen = false;

    //显示NavigationBar和StatusBar
    showSysBar(decorView);

    //把播放器视图从DecorView中移除并添加到当前FrameLayout中即退出了全屏
    decorView.removeView(mPlayerContainer);
    this.addView(mPlayerContainer);

    setPlayerState(Constants.PLAYER_NORMAL);
  }

  private void showSysBar(ViewGroup decorView) {
    int uiOptions = decorView.getSystemUiVisibility();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      uiOptions &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      uiOptions &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }
    decorView.setSystemUiVisibility(uiOptions);
    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }

  /**
   * 获取DecorView
   */
  protected ViewGroup getDecorView() {
    Activity activity = getActivity();
    if (activity == null) return null;
    return (ViewGroup) activity.getWindow().getDecorView();
  }

  /**
   * 获取activity中的content view,其id为android.R.id.content
   */
  protected ViewGroup getContentView() {
    Activity activity = getActivity();
    if (activity == null) return null;
    return activity.findViewById(android.R.id.content);
  }

  /**
   * 获取Activity，优先通过Controller去获取Activity
   */
  protected Activity getActivity() {
    Activity activity;
    if (mVideoController != null) {
      activity = PlayerUtils.scanForActivity(mVideoController.getContext());
      if (activity == null) {
        activity = PlayerUtils.scanForActivity(getContext());
      }
    } else {
      activity = PlayerUtils.scanForActivity(getContext());
    }
    return activity;
  }

  /**
   * 判断是否处于全屏状态
   */
  @Override
  public boolean isFullScreen() {
    return mIsFullScreen;
  }

  @Override
  public boolean isAlwaysFullScreen() {
    return mIsAlwaysFullScreen;
  }

  public void setAlwaysFullScreen() {
    mIsFullScreen = true;
    mIsAlwaysFullScreen = true;
  }

  @Override
  public void onVideoSizeChanged(int videoWidth, int videoHeight) {
    mVideoSize[0] = videoWidth;
    mVideoSize[1] = videoHeight;

    if (mRenderView != null) {
      mRenderView.setScaleType(mCurrentScreenScaleType);
      mRenderView.setVideoSize(videoWidth, videoHeight);
    }
  }

  @Override
  public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

  }

  @Override
  public void onPlayerError(ExoPlaybackException error) {

  }

  /**
   * 设置控制器，传null表示移除控制器
   */
  public void setVideoController(@Nullable BaseVideoController mediaController) {
    mPlayerContainer.removeView(mVideoController);
    mVideoController = mediaController;
    if (mediaController != null) {
      mediaController.setMediaPlayer(this);
      LayoutParams params = new LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT);
      mPlayerContainer.addView(mVideoController, params);
    }
  }

  /**
   * 设置控制器，传null表示移除控制器
   */
  public void setVideoControllerOnly(@Nullable BaseVideoController mediaController) {
    mPlayerContainer.removeView(mVideoController);
    mVideoController = mediaController;
    if (mediaController != null) {
      mediaController.setMediaPlayer(this);
    }
  }

  /**
   * 设置视频比例
   */
  @Override
  public void setScreenScaleType(int screenScaleType) {
    mCurrentScreenScaleType = screenScaleType;
    if (mRenderView != null) {
      mRenderView.setScaleType(screenScaleType);
    }
  }

  /**
   * 设置镜像旋转，暂不支持SurfaceView
   */
  @Override
  public void setMirrorRotation(boolean enable) {
    if (mRenderView != null) {
      mRenderView.getView().setScaleX(enable ? -1 : 1);
    }
  }

  /**
   * 截图，暂不支持SurfaceView
   */
  @Override
  public Bitmap doScreenShot() {
    if (mRenderView != null) {
      return mRenderView.doScreenShot();
    }
    return null;
  }

  /**
   * 获取视频宽高,其中width: mVideoSize[0], height: mVideoSize[1]
   */
  @Override
  public int[] getVideoSize() {
    return mVideoSize;
  }

  /**
   * 旋转视频画面
   *
   * @param rotation 角度
   */
  @Override
  public void setRotation(float rotation) {
    if (mRenderView != null) {
      mRenderView.setVideoRotation((int) rotation);
    }
  }

  /**
   * 向Controller设置播放状态，用于控制Controller的ui展示
   */
  public void setPlayState(int playState) {
    mCurrentPlayState = playState;
    if (mVideoController != null) {
      mVideoController.setPlayState(playState);
    }
    if (mOnStateChangeListeners != null) {
      for (OnStateChangeListener l : PlayerUtils.getSnapshot(mOnStateChangeListeners)) {
        if (l != null) {
          l.onPlayStateChanged(playState);
        }
      }
    }
  }

  /**
   * 向Controller设置播放器状态，包含全屏状态和非全屏状态
   */
  protected void setPlayerState(int playerState) {
    mCurrentPlayerState = playerState;
    if (mVideoController != null) {
      mVideoController.setPlayerState(playerState);
    }
    if (mOnStateChangeListeners != null) {
      for (OnStateChangeListener l : PlayerUtils.getSnapshot(mOnStateChangeListeners)) {
        if (l != null) {
          l.onPlayerStateChanged(playerState);
        }
      }
    }
  }

  /**
   * 播放状态改变监听器
   */
  public interface OnStateChangeListener {
    void onPlayerStateChanged(int playerState);

    void onPlayStateChanged(int playState);
  }

  /**
   * OnStateChangeListener的空实现。用的时候只需要重写需要的方法
   */
  public static class SimpleOnStateChangeListener implements OnStateChangeListener {
    @Override
    public void onPlayerStateChanged(int playerState) {
    }

    @Override
    public void onPlayStateChanged(int playState) {
    }
  }

  /**
   * 添加一个播放状态监听器，播放状态发生变化时将会调用。
   */
  public void addOnStateChangeListener(@NonNull OnStateChangeListener listener) {
    if (mOnStateChangeListeners == null) {
      mOnStateChangeListeners = new ArrayList<>();
    }
    mOnStateChangeListeners.add(listener);
  }

  /**
   * 移除某个播放状态监听
   */
  public void removeOnStateChangeListener(@NonNull OnStateChangeListener listener) {
    if (mOnStateChangeListeners != null) {
      mOnStateChangeListeners.remove(listener);
    }
  }

  /**
   * 设置一个播放状态监听器，播放状态发生变化时将会调用，
   * 如果你想同时设置多个监听器，推荐 {@link #addOnStateChangeListener(OnStateChangeListener)}。
   */
  public void setOnStateChangeListener(@NonNull OnStateChangeListener listener) {
    if (mOnStateChangeListeners == null) {
      mOnStateChangeListeners = new ArrayList<>();
    } else {
      mOnStateChangeListeners.clear();
    }
    mOnStateChangeListeners.add(listener);
  }

  /**
   * 移除所有播放状态监听
   */
  public void clearOnStateChangeListeners() {
    if (mOnStateChangeListeners != null) {
      mOnStateChangeListeners.clear();
    }
  }

  /**
   * 改变返回键逻辑，用于activity
   */
  public boolean onBackPressed() {
    return mVideoController != null && mVideoController.onBackPressed();
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Log.d("VideoView", "onSaveInstanceState: " + mCurrentPosition);
    //activity切到后台后可能被系统回收，故在此处进行进度保存
    saveProgress();
    return super.onSaveInstanceState();
  }
}
