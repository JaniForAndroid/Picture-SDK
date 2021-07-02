package com.example.exoaudioplayer.video.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.annotation.Nullable;
import com.example.exoaudioplayer.video.base.Constants;
import com.example.exoaudioplayer.video.base.VideoView;
import com.example.exoaudioplayer.video.base.VideoViewManager;
import com.example.exoaudioplayer.video.component.NoBackMediaController;
import com.example.exoaudioplayer.video.component.PlayerCompleteView;
import com.example.exoaudioplayer.video.component.PlayerControlView;
import com.example.exoaudioplayer.video.component.PlayerErrorView;
import com.example.exoaudioplayer.video.component.PlayerExerciseView;
import com.example.exoaudioplayer.video.component.PlayerGestureView;
import com.example.exoaudioplayer.video.component.PlayerLogoView;
import com.example.exoaudioplayer.video.component.PlayerPrepareView;
import com.example.exoaudioplayer.video.component.PlayerScreenView;
import com.example.exoaudioplayer.video.component.PlayerTitleView;
import com.example.exoaudioplayer.video.component.PlayerWKStarView;
import com.example.exoaudioplayer.video.component.ReviewMediaController;
import com.example.exoaudioplayer.video.component.StandardMediaController;
import com.example.exoaudioplayer.video.model.MediaBuilder;
import com.example.picsdk.R;
import com.google.android.exoplayer2.util.Util;
import com.namibox.util.Logger;
import com.namibox.videocache.CacheInfo;
import com.namibox.videocache.CacheListener;
import com.namibox.videocache.HttpProxyCacheServer;
import com.namibox.videocache.VideoProxy;
import java.io.File;

public class VideoFragment extends Fragment {

  private MediaBuilder mediaBuilder;
  private VideoView mVideoView;
  private StandardMediaController mController;
  private PlayerWKStarView playerWKStarView;
  private PlayerControlView playerControlView;
  private PlayerTitleView playerTitleView;
  private PlayerScreenView playerScreenView;
  private PlayerExerciseView playerExerciseView;
  private HttpProxyCacheServer proxy;
  private boolean isTest = false;
  private String contentType;
  private long contentLength;

  public VideoFragment() {

  }

  @SuppressLint("ValidFragment")
  public VideoFragment(MediaBuilder builder) {
    this.mediaBuilder = builder;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.player_layout_fragment, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mVideoView = view.findViewById(R.id.player);

    if (mediaBuilder != null) {
      mVideoView.setSize(mediaBuilder.size);
      mVideoView.setDuration1(mediaBuilder.duration);

      switch (mediaBuilder.type) {
        case Constants.VIDEOSHOW_FRAGMENT:
          initVideoShow();
          break;
        case Constants.REVIEW_FRAGMENT:
          initReview();
          break;
        case Constants.NOCONTROLLER_FRAGMENT:
          initTest();
          break;
        case Constants.WK_FRAGMENT:
          initWK();
          break;
        case Constants.NORMAL_FRAGMENT:
          initNoBackNormal();
          break;
        case Constants.WXAD_FRAGMENT:
          initWXAd();
          break;
        case Constants.WK_YUNXIAO_FRAGMENT:
          initWKToB();
          break;
        case Constants.PIC_FRAGMENT:
          initPic();
          break;
        default:
          initNormal();
          break;
      }
    }

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    getActivity().registerReceiver(connectionReceiver, intentFilter);
  }

  BroadcastReceiver connectionReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
      NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
      NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

      if ((wifiNetInfo != null && !wifiNetInfo.isConnected()) && (mobNetInfo != null && mobNetInfo.isConnected())) {
        if (mVideoView != null && !mVideoView.isLocalDataSource()) {
          mVideoView.pause();
          mVideoView.setPlayState(Constants.STATE_START_ABORT);
        }
      }
    }
  };

  public StandardMediaController getController() {
    return mController;
  }

  public PlayerWKStarView getPlayerWKStarView() {
    return playerWKStarView;
  }

  public PlayerControlView getPlayerControlView() {
    return playerControlView;
  }

  public PlayerScreenView getScreenView() {
    return playerScreenView;
  }

  public PlayerExerciseView getPlayerExerciseView() {
    return playerExerciseView;
  }

  public void setOnScreenCaptureCallBack(final PlayerScreenView.OnScreenCaptureCallBack onScreenCaptureCallBack) {
    if (playerScreenView != null) {
      playerScreenView.setOnScreenCaptureCallBack(onScreenCaptureCallBack);
    }
  }

  public void setOnExerciseCallBack(final PlayerExerciseView.OnExerciseCallBack onExerciseCallBack) {
    if (playerExerciseView != null) {
      playerExerciseView.setOnExerciseCallBack(onExerciseCallBack);
    }
  }

  public void setOnControllerScreenCaptureCallBack(final ReviewMediaController.OnScreenCaptureCallBack onScreenCaptureCallBack) {
    if (mController != null) {
      ((ReviewMediaController) mController).setOnScreenCaptureCallBack(onScreenCaptureCallBack);
    }
  }

  public PlayerTitleView getTitleView() {
    return playerTitleView;
  }

  public void setOnBackCallBack(final PlayerTitleView.OnBackCallBack onBackCallBack) {
    if (playerTitleView != null) {
      playerTitleView.setOnBackCallBack(onBackCallBack);
    }
  }

  public void enterBackStage() {
    if (playerControlView != null) {
      playerControlView.dismissChangePlaySpeedWindow();
    }
  }

  public VideoView getVideoView() {
    return mVideoView;
  }

  public void initVideoShow() {
    //如果需要一直全屏 调用此方法
    mVideoView.setAlwaysFullScreen();
    setDataSource();

    mController = new StandardMediaController(getActivity());

    playerControlView = new PlayerControlView(getActivity());//点播控制条
    if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
      playerControlView.setOnChangePlaySpeedCallBack(new PlayerControlView.OnChangePlaySpeedCallBack() {
        @Override
        public void onChange(float multiple) {
          mVideoView.setSpeed(multiple);
        }
      });
    }
    if (mVideoView.isAlwaysFullScreen()) {
      playerControlView.findViewById(R.id.fullscreen).setVisibility(View.GONE);
    }
    mController.addControlComponent(playerControlView);

    PlayerPrepareView playerPrepareView = new PlayerPrepareView(getActivity());//准备播放界面
    mController.addControlComponent(playerPrepareView);

    PlayerGestureView gestureControlView = new PlayerGestureView(getActivity());
    mController.addControlComponent(gestureControlView);

    mVideoView.setVideoController(mController);

    mVideoView.setScreenScaleType(Constants.SCREEN_SCALE_16_9);
    mVideoView.start();
  }

  public void initTest() {
    isTest = true;
    //如果需要一直全屏 调用此方法
    mVideoView.setAlwaysFullScreen();
    setDataSource();

    mVideoView.setScreenScaleType(Constants.SCREEN_SCALE_16_9);
    mVideoView.start();
  }

  public void initWXAd() {
    mVideoView.setAlwaysFullScreen();
    setDataSource();

    mVideoView.setScreenScaleType(Constants.SCREEN_SCALE_16_9);
    mVideoView.start();
  }

  public void initWK() {
    //如果需要一直全屏 调用此方法
    mVideoView.setAlwaysFullScreen();
    setDataSource();

    if (mediaBuilder.seekTime != 0) {
      mVideoView.skipPositionWhenPlay(mediaBuilder.seekTime);
    }

    mController = new StandardMediaController(getActivity());

    PlayerPrepareView playerPrepareView = new PlayerPrepareView(getActivity());//准备播放界面
//    ImageView thumb = playerPrepareView.findViewById(R.id.thumb);//封面图
//    Glide.with(this).load(mediaBuilder.thumbnail).into(thumb);
    mController.addControlComponent(playerPrepareView);

    mController.addControlComponent(new PlayerCompleteView(getActivity()));//自动完成播放界面

    mController.addControlComponent(new PlayerErrorView(getActivity()));//错误界面

    playerTitleView = new PlayerTitleView(getActivity());//标题栏
    mController.addControlComponent(playerTitleView);

    playerControlView = new PlayerControlView(getActivity());//点播控制条
    if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
      playerControlView.setOnChangePlaySpeedCallBack(new PlayerControlView.OnChangePlaySpeedCallBack() {
        @Override
        public void onChange(float multiple) {
          mVideoView.setSpeed(multiple);
        }
      });
    }
    //是否显示底部进度条。默认显示
//                vodControlView.showBottomProgress(false);
    if (mVideoView.isAlwaysFullScreen()) {
      playerControlView.findViewById(R.id.fullscreen).setVisibility(View.GONE);
    }
    mController.addControlComponent(playerControlView);
    PlayerGestureView gestureControlView = new PlayerGestureView(getActivity());//滑动控制视图
    mController.addControlComponent(gestureControlView);

    playerWKStarView = new PlayerWKStarView(getActivity());//微课星级进度条
    playerWKStarView.init(mediaBuilder.heartNum, getActivity());
    mController.addControlComponent(playerWKStarView);

    //设置标题
    playerTitleView.setTitle(mediaBuilder.title);

    //如果你不需要单独配置各个组件，可以直接调用此方法快速添加以上组件
//            controller.addDefaultControlComponent(title, isLive);

    //竖屏也开启手势操作，默认关闭
//    mController.setEnableInNormal(true);
    //滑动调节亮度，音量，进度，默认开启
//    mController.setGestureEnabled(false);

    //如果你不想要UI，不要设置控制器即可
    mVideoView.setVideoController(mController);
//      "http://vfx.mtime.cn/Video/2019/03/14/mp4/190314223540373995.mp4"
    //保存播放进度
//            mVideoView.setProgressManager(new ProgressManagerImpl());
    //播放状态监听
    mVideoView.addOnStateChangeListener(mOnStateChangeListener);

    //临时切换播放核心
//  mVideoView.setPlayerType(Constants.EXO);

    mVideoView.setScreenScaleType(Constants.SCREEN_SCALE_16_9);
    mVideoView.start();
  }

  public void initWKToB() {
    mVideoView.setAlwaysFullScreen();
    setDataSource();

    if (mediaBuilder.seekTime != 0) {
      mVideoView.skipPositionWhenPlay(mediaBuilder.seekTime);
    }

    mController = new StandardMediaController(getActivity());

    PlayerPrepareView playerPrepareView = new PlayerPrepareView(getActivity());//准备播放界面
//    ImageView thumb = playerPrepareView.findViewById(R.id.thumb);//封面图
//    Glide.with(this).load(mediaBuilder.thumbnail).into(thumb);
    mController.addControlComponent(playerPrepareView);

    mController.addControlComponent(new PlayerCompleteView(getActivity()));//自动完成播放界面

    mController.addControlComponent(new PlayerErrorView(getActivity()));//错误界面

    playerExerciseView = new PlayerExerciseView(getActivity());
    mController.addControlComponent(playerExerciseView);

    playerTitleView = new PlayerTitleView(getActivity());//标题栏
    mController.addControlComponent(playerTitleView);

    playerControlView = new PlayerControlView(getActivity());//点播控制条
    if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
      playerControlView.setOnChangePlaySpeedCallBack(new PlayerControlView.OnChangePlaySpeedCallBack() {
        @Override
        public void onChange(float multiple) {
          mVideoView.setSpeed(multiple);
        }
      });
    }
    if (mVideoView.isAlwaysFullScreen()) {
      playerControlView.findViewById(R.id.fullscreen).setVisibility(View.GONE);
    }
    mController.addControlComponent(playerControlView);

    PlayerGestureView gestureControlView = new PlayerGestureView(getActivity());//滑动控制视图
    mController.addControlComponent(gestureControlView);

    playerTitleView.setTitle(mediaBuilder.title);

    mVideoView.setVideoController(mController);
    mVideoView.addOnStateChangeListener(mOnStateChangeListener);

    mVideoView.setScreenScaleType(Constants.SCREEN_SCALE_16_9);
    mVideoView.start();
  }

  public void initPic() {
    mVideoView.setAlwaysFullScreen();
    setDataSource();

    if (mediaBuilder.seekTime != 0) {
      mVideoView.skipPositionWhenPlay(mediaBuilder.seekTime);
    }

    mController = new StandardMediaController(getActivity());

    PlayerPrepareView playerPrepareView = new PlayerPrepareView(getActivity());//准备播放界面
    mController.addControlComponent(playerPrepareView);

    mController.addControlComponent(new PlayerCompleteView(getActivity()));//自动完成播放界面

    mController.addControlComponent(new PlayerErrorView(getActivity()));//错误界面

    playerControlView = new PlayerControlView(getActivity());//点播控制条
    if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
      playerControlView.setOnChangePlaySpeedCallBack(new PlayerControlView.OnChangePlaySpeedCallBack() {
        @Override
        public void onChange(float multiple) {
          mVideoView.setSpeed(multiple);
        }
      });
    }
    if (mVideoView.isAlwaysFullScreen()) {
      playerControlView.findViewById(R.id.fullscreen).setVisibility(View.GONE);
    }
    mController.addControlComponent(playerControlView);

    PlayerGestureView gestureControlView = new PlayerGestureView(getActivity());//滑动控制视图
    mController.addControlComponent(gestureControlView);

    mVideoView.setVideoController(mController);
    mVideoView.addOnStateChangeListener(mOnStateChangeListener);

    mVideoView.setScreenScaleType(Constants.SCREEN_SCALE_16_9);
    mVideoView.start();
  }

  private VideoView.OnStateChangeListener mOnStateChangeListener = new VideoView.SimpleOnStateChangeListener() {
    @Override
    public void onPlayerStateChanged(int playerState) {
    }

    @Override
    public void onPlayStateChanged(int playState) {
      switch (playState) {
        case Constants.STATE_ERROR:
          if (proxy != null && !Util.isLocalFileUri(Uri.parse(mediaBuilder.uri))) {
            File file = proxy.getCacheFile(mediaBuilder.uri);
            if (file.exists()) {
              Logger.e("delete cache file: " + file);
              file.delete();
            }
          }
          break;
      }
    }
  };

  public void initReview() {
    mVideoView.setAlwaysFullScreen();
    mVideoView.setPlayerType(Constants.EXO);
    setDataSource();

    if (mediaBuilder.seekTime != 0) {
      mVideoView.skipPositionWhenPlay(mediaBuilder.seekTime);
    }

    mController = new ReviewMediaController(getActivity());

//    playerScreenView = new PlayerScreenView(getActivity());
//    mController.addControlComponent(playerScreenView);

//    PlayerPrepareView playerPrepareView = new PlayerPrepareView(getActivity());//准备播放界面
//    mController.addControlComponent(playerPrepareView);

    mController.addControlComponent(new PlayerCompleteView(getActivity()));//自动完成播放界面

    mController.addControlComponent(new PlayerErrorView(getActivity()));//错误界面

    playerTitleView = new PlayerTitleView(getActivity());//标题栏
    mController.addControlComponent(playerTitleView);

    playerControlView = new PlayerControlView(getActivity());//点播控制条
    if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
      playerControlView.setOnChangePlaySpeedCallBack(new PlayerControlView.OnChangePlaySpeedCallBack() {
        @Override
        public void onChange(float multiple) {
          mVideoView.setSpeed(multiple);
        }
      });
    }
    if (mVideoView.isAlwaysFullScreen()) {
      playerControlView.findViewById(R.id.fullscreen).setVisibility(View.GONE);
    }
    mController.addControlComponent(playerControlView);

    PlayerGestureView gestureControlView = new PlayerGestureView(getActivity());//滑动控制视图
    mController.addControlComponent(gestureControlView);

    PlayerLogoView playerLogoView = new PlayerLogoView(getActivity());
    playerLogoView.setupLogo(mediaBuilder.remoteLogo, mediaBuilder.localLogo);
    mController.addControlComponent(playerLogoView);

    playerTitleView.setTitle(mediaBuilder.title);

    mVideoView.setVideoController(mController);

    mVideoView.setScreenScaleType(Constants.SCREEN_SCALE_16_9);
    mVideoView.start();

    VideoViewManager.instance().setPlayOnMobileNetwork(true);
  }

  public void initNormal() {
    setDataSource();

    if (mediaBuilder.seekTime != 0) {
      mVideoView.skipPositionWhenPlay(mediaBuilder.seekTime);
    }

    mController = new StandardMediaController(getActivity());
    mController.setEnableOrientation(true);

    mController.addControlComponent(new PlayerErrorView(getActivity()));//错误界面

    PlayerPrepareView playerPrepareView = new PlayerPrepareView(getActivity());//准备播放界面
    mController.addControlComponent(playerPrepareView);

    playerTitleView = new PlayerTitleView(getActivity());//标题栏
    mController.addControlComponent(playerTitleView);

    playerControlView = new PlayerControlView(getActivity());//点播控制条
    if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
      playerControlView.setOnChangePlaySpeedCallBack(new PlayerControlView.OnChangePlaySpeedCallBack() {
        @Override
        public void onChange(float multiple) {
          mVideoView.setSpeed(multiple);
        }
      });
    }
    if (mVideoView.isAlwaysFullScreen()) {
      playerControlView.findViewById(R.id.fullscreen).setVisibility(View.GONE);
    }
    mController.addControlComponent(playerControlView);

    PlayerGestureView gestureControlView = new PlayerGestureView(getActivity());//滑动控制视图
    mController.addControlComponent(gestureControlView);

    playerTitleView.setTitle(mediaBuilder.title);

    mController.setEnableInNormal(true);
    mVideoView.setVideoController(mController);

    mVideoView.setScreenScaleType(Constants.SCREEN_SCALE_16_9);
    mVideoView.start();
  }

  public void initNoBackNormal() {
    setDataSource();

    if (mediaBuilder.seekTime != 0) {
      mVideoView.skipPositionWhenPlay(mediaBuilder.seekTime);
    }

    mController = new NoBackMediaController(getActivity());
    mController.setEnableOrientation(true);

    mController.addControlComponent(new PlayerErrorView(getActivity()));//错误界面

    PlayerPrepareView playerPrepareView = new PlayerPrepareView(getActivity());//准备播放界面
    mController.addControlComponent(playerPrepareView);

    mController.addControlComponent(new PlayerCompleteView(getActivity()));//自动完成播放界面

    playerTitleView = new PlayerTitleView(getActivity());//标题栏
    mController.addControlComponent(playerTitleView);

    playerControlView = new PlayerControlView(getActivity());//点播控制条
    if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
      playerControlView.setOnChangePlaySpeedCallBack(new PlayerControlView.OnChangePlaySpeedCallBack() {
        @Override
        public void onChange(float multiple) {
          mVideoView.setSpeed(multiple);
        }
      });
    }
    if (mVideoView.isAlwaysFullScreen()) {
      playerControlView.findViewById(R.id.fullscreen).setVisibility(View.GONE);
    }
    mController.addControlComponent(playerControlView);

    PlayerGestureView gestureControlView = new PlayerGestureView(getActivity());//滑动控制视图
    mController.addControlComponent(gestureControlView);

    playerTitleView.setTitle(mediaBuilder.title);

    mController.setEnableInNormal(true);
    mVideoView.setVideoController(mController);

    mVideoView.setScreenScaleType(Constants.SCREEN_SCALE_16_9);
    mVideoView.start();
  }

  public void initNocontrol() {
    setDataSource();

    mVideoView.setScreenScaleType(Constants.SCREEN_SCALE_16_9);
    mVideoView.start();
  }

  private void setDataSource() {
    if (!Util.isLocalFileUri(Uri.parse(mediaBuilder.uri)) && !isTest) {
      proxy = VideoProxy.getProxy(getActivity());
      proxy.registerCacheListener(cacheListener, mediaBuilder.uri);
      String proxyUrl = proxy.getProxyUrl(mediaBuilder.uri);
      mVideoView.setUrl(proxyUrl);
    } else {
      mVideoView.setUrl(mediaBuilder.uri);
    }
  }

  private void setDataSourceToB() {
    mVideoView.setUrl(mediaBuilder.uri);
  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mVideoView != null) {
      mVideoView.release();
    }
    if (proxy != null) {
      proxy.unregisterCacheListener(cacheListener);
    }
    getActivity().unregisterReceiver(connectionReceiver);
  }

  private boolean isHomeBackToPlay = false;

  @Override
  public void onResume() {
    super.onResume();
    if (mVideoView != null && isHomeBackToPlay) {
      mVideoView.resume();
      isHomeBackToPlay = false;
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mVideoView != null) {
      if (mVideoView.getCurrentPlayState() == Constants.STATE_PLAYING) {
        isHomeBackToPlay = true;
      }
      mVideoView.pause();
    }
  }

  public void setPlayWhenReady(boolean playWhenReady) {
    if (mVideoView != null) {
      if (playWhenReady) {
        mVideoView.start();
      } else {
        mVideoView.pause();
      }
    }
  }

  private CacheListener cacheListener = new CacheListener() {
    @Override
    public void onContentLength(long length, String contentType) {
      VideoFragment.this.contentType = contentType;
      VideoFragment.this.contentLength = length;
    }

    @Override
    public void onCacheAvailable(CacheInfo cacheInfo) {
      if (onVideoDownloadCallback != null) {
        onVideoDownloadCallback
            .onCacheAvailable(cacheInfo.cacheFile, cacheInfo.url, cacheInfo.percentsAvailable,
                cacheInfo.host, cacheInfo.realReadBytes, cacheInfo.eTag,
                cacheInfo.responseUrl, contentLength, contentType);
      }
    }
  };

  public interface OnVideoDownloadCallback {

    void onCacheAvailable(File cacheFile, String url, int percentsAvailable, String host,
                          long realReadBytes, String eTag, String responseUrl, long contentLength,
                          String contentType);
  }

  public OnVideoDownloadCallback onVideoDownloadCallback;

  public void setOnVideoDownloadCallback(OnVideoDownloadCallback onVideoDownloadCallback) {
    this.onVideoDownloadCallback = onVideoDownloadCallback;
  }
}
