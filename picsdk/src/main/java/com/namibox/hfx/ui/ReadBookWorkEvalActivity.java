package com.namibox.hfx.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.exoaudioplayer.aduio.base.AbstractAudioPlayer;
import com.example.exoaudioplayer.aduio.base.AudioCallBack;
import com.example.exoaudioplayer.aduio.base.AudioPlayerFactory;
import com.example.exoaudioplayer.aduio.base.Constants;
import com.example.picsdk.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.namibox.commonlib.activity.AbsFunctionActivity;
import com.namibox.commonlib.dialog.DialogUtil;
import com.namibox.commonlib.dialog.NamiboxNiceDialog;
import com.namibox.hfx.bean.Huiben;
import com.namibox.hfx.bean.HuibenShowWork;
import com.namibox.hfx.utils.HfxPreferenceUtil;
import com.namibox.hfx.utils.MyPageTransformer;
import com.namibox.hfx.view.WheelView;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.Utils;
import com.namibox.util.WeakAsyncTask;
import com.othershe.nicedialog.BaseNiceDialog;
import com.othershe.nicedialog.ViewConvertListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import okhttp3.CacheControl;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.senab.photoview.PhotoView;

public class ReadBookWorkEvalActivity extends AbsFunctionActivity {

  private static final String TAG = "AbsReadBookActivity";
  public static final String ARG_JSON_URL = "json_url";
  private HuibenShowWork huiben;
  private ViewPager mViewPager;
  private TextView autoPlayView;
  private View mMenuLayout;
  private int mMenuViewSize;
  private ReadAdapter mAdapter;
  private boolean isAutoPlay;
  private String currentMp3;
  private Toolbar mToolbar;
  private RelativeLayout mTopLayout;
  private int mBrightness;
  private int mToolbarSize;
  private static final int DISPLAY_TIME = 5000;
  private static final int ANIMATE_TIME = 150;
  private boolean menuShowing = true;
  private ViewStub mBrightnessViewStub;
  private View mBrightnessView;
  private SeekBar mBrightnessSeekBar;
  protected int currentPage;
  private Runnable hideBrightnessLayoutRunnable = new Runnable() {
    @Override
    public void run() {
      hideBrightness();
    }
  };
  private RecyclerView mScoreList;
  private TextView mTitle;
  private TextView mPageIndex;
  private ImageView mBack;
  private ImageView bgImag;
  private RelativeLayout mRootView;
  private ImageView mAudioPlay;
  private ImageView mAutoPlay;
  private ImageView mBright;
  private ScoreAdapter scoreAdapter;
  private boolean hasComment;
  boolean isLastPageBefore = false;
  private int targetIndex;
  private boolean playing;
  private boolean showDialog;
  private AbstractAudioPlayer exoAudioPlayer;

  private void hideBrightness() {
    if (mBrightnessView != null) {
      mBrightnessView.setVisibility(View.GONE);
    }
  }

  private void showBrightness() {
    if (mBrightnessView == null) {
      View v = mBrightnessViewStub.inflate();
      mBrightnessView = v.findViewById(R.id.brightness_layout);
      mBrightnessSeekBar = (SeekBar) v.findViewById(R.id.brightness_seekBar);
      mBrightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          setScreenBrightness(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
          mBrightnessView.removeCallbacks(hideBrightnessLayoutRunnable);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          mBrightnessView.postDelayed(hideBrightnessLayoutRunnable, DISPLAY_TIME);
        }
      });
    }
    mBrightnessView.removeCallbacks(hideBrightnessLayoutRunnable);
    mBrightnessSeekBar.setMax(255);
    mBrightnessSeekBar.setProgress(getScreenBrightness());
    mBrightnessView.setVisibility(View.VISIBLE);
    mBrightnessView.postDelayed(hideBrightnessLayoutRunnable, DISPLAY_TIME);
  }

  private Runnable hideNavigationRunnable = new Runnable() {
    @Override
    public void run() {
      hideNavigation();
    }
  };

  private void delayHideNavigation(long time) {
    mMenuLayout.postDelayed(hideNavigationRunnable, time);
  }

  private void hideNavigation() {
    hideMenuView();
    mMenuLayout.removeCallbacks(hideNavigationRunnable);
  }

  private void showNavigation() {
    showMenuView();
    mMenuLayout.postDelayed(hideNavigationRunnable, DISPLAY_TIME);
  }

  private void toggleNavigation() {
    if (menuShowing) {
      hideNavigation();
    } else {
      showNavigation();
    }
  }

  private void showMenuView() {
    if (menuShowing) {
      return;
    }
    menuShowing = true;
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet
        .playTogether(ObjectAnimator.ofFloat(mMenuLayout, View.TRANSLATION_Y, mMenuViewSize, 0),
            ObjectAnimator.ofFloat(mToolbar, View.TRANSLATION_Y, -mToolbarSize, 0));
    animatorSet.setDuration(ANIMATE_TIME);
    animatorSet.setInterpolator(new DecelerateInterpolator());
    animatorSet.addListener(new Animator.AnimatorListener() {
      @Override
      public void onAnimationStart(Animator animation) {
        mMenuLayout.setVisibility(View.VISIBLE);
        mPageIndex.setVisibility(View.GONE);

        //作业评测查看不要toolbar
        mToolbar.setVisibility(View.GONE);
        mTopLayout.setVisibility(View.GONE);
      }

      @Override
      public void onAnimationEnd(Animator animation) {
      }

      @Override
      public void onAnimationCancel(Animator animation) {

      }

      @Override
      public void onAnimationRepeat(Animator animation) {

      }
    });
    animatorSet.start();
  }

  private void hideMenuView() {
    if (!menuShowing) {
      return;
    }
    menuShowing = false;
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet
        .playTogether(ObjectAnimator.ofFloat(mMenuLayout, View.TRANSLATION_Y, 0, mMenuViewSize),
            ObjectAnimator.ofFloat(mToolbar, View.TRANSLATION_Y, 0, -mToolbarSize));
    animatorSet.setDuration(ANIMATE_TIME);
    animatorSet.addListener(new Animator.AnimatorListener() {
      @Override
      public void onAnimationStart(Animator animation) {

      }

      @Override
      public void onAnimationEnd(Animator animation) {
        mMenuLayout.setTranslationY(0);
        mToolbar.setTranslationY(0);
        mTopLayout.setTranslationY(0);
        mMenuLayout.setVisibility(View.GONE);
        mPageIndex.setVisibility(View.VISIBLE);
        mToolbar.setVisibility(View.GONE);
        mTopLayout.setVisibility(View.GONE);
      }

      @Override
      public void onAnimationCancel(Animator animation) {

      }

      @Override
      public void onAnimationRepeat(Animator animation) {

      }
    });
    animatorSet.start();
  }

  private OnClickListener clickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      if (v.getId() == R.id.iv_audio_play) {
//        showJumpDialog();
        if (mAudioPlay.isSelected()) {
          mAudioPlay.setSelected(false);
        } else {
          mAudioPlay.setSelected(true);
        }
        exoAudioPlayer.playPause();
      } else if (v.getId() == R.id.iv_auto_play) {
        if (mAutoPlay.isSelected()) {
          setAutoPlay(false);
        } else {
          setAutoPlay(true);
          mAudioPlay.setSelected(true);
          //播放完成再处理 播放中不处理
          if (TextUtils.isEmpty(huiben.data.get(currentPage).audio_url)) {
            delayPlayMp3(null);
            return;
          } else if (exoAudioPlayer != null && exoAudioPlayer.getPlayer() != null && exoAudioPlayer.getPlayer().getPlaybackState() == Player.STATE_ENDED) {
            exoAudioPlayer.playPause();
          }
        }
      } else if (v.getId() == R.id.iv_brightness) {
//        showBrightness();
        showJumpDialog();
      }
      hideNavigation();
    }
  };

  private void setAutoPlay(boolean autoPlay) {
    mAutoPlay.setSelected(autoPlay);
    isAutoPlay = autoPlay;
  }

  private void setScreenBrightness(int screenBrightness) {
    if (screenBrightness < 1) {
      return;
    }
    mBrightness = screenBrightness;
    Window localWindow = getWindow();
    WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
    localLayoutParams.screenBrightness = screenBrightness / 255.0f;
    localWindow.setAttributes(localLayoutParams);
  }

  private int getScreenBrightness() {
    if (mBrightness == 0) {
      try {
        mBrightness = Settings.System
            .getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return mBrightness;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.hfx_activity_read_work);
    mToolbarSize = getResources().getDimensionPixelSize(R.dimen.hfx_actionbar_size);
    Intent intent = getIntent();
    String jsonUrl = intent.getStringExtra(ARG_JSON_URL);

    //初始化控件
    initView();
    //设置监听时间
    initListener();
    //初始化menu
    initMenu();
    //初始化成绩列表适配器
    loadJsonData(jsonUrl);
    Logger.i(TAG, "jsonUrl = " + jsonUrl);
    //hideMenuView();
    initExoAudioPlayer();
  }

  private void initExoAudioPlayer() {
    exoAudioPlayer = AudioPlayerFactory
        .getInstance().createPlayer(getApplicationContext(), Constants.EXO);
    exoAudioPlayer.setPlayerCallBack(new AudioCallBack() {

      @Override
      public void playStateChange(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
          if (mViewPager != null) {
            mViewPager.removeCallbacks(nextPageRunnable);
            mViewPager.post(nextPageRunnable);
          }
          /**
           * 最后一个 并且是自动播放 并且需要评论 播放完成的状态 需要弹框
           */
          if (isAutoPlay && hasComment && currentPage == huiben.data.size() - 1) {
            new Handler().postDelayed(new Runnable() {
              @Override
              public void run() {
                isAutoPlay = false;
                showCommentDialog();
              }
            }, 500);
          } else if (hasComment && currentPage == huiben.data.size() - 1) {
            targetIndex = huiben.data.size();
          }
        }
        Logger.i("zkx playbackState = " + playbackState);
        playing = playbackState == ExoPlayer.STATE_READY && playWhenReady;
        mAudioPlay.setSelected(playing);
      }
    });
  }

  private void initView() {
    mRootView = findViewById(R.id.root_view);
    mToolbar = findViewById(R.id.tool_bar);
    mTopLayout = findViewById(R.id.topLayout);
    mBack = findViewById(R.id.iv_back);
    mScoreList = findViewById(R.id.scoreList);
    mTitle = findViewById(R.id.tv_title);
    mPageIndex = findViewById(R.id.tvPageIndex);
    mViewPager = findViewById(R.id.pager);

    setSupportActionBar(mToolbar);
  }

  private void initListener() {
    mBack.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
      }
    });
    mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {


      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {
        if (currentPage != position) {
          currentPage = position;
          if (scoreAdapter != null) {
            scoreAdapter.lightItemByIndex(position);
            mScoreList.smoothScrollToPosition(position);
          }
          updatePagerIndex(position);
        }

      }

      @Override
      public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_DRAGGING) {
          if (currentPage == huiben.data.size() - 1) {
            isLastPageBefore = true;
          }
          mViewPager.removeCallbacks(nextPageRunnable);
        }
        if (state == ViewPager.SCROLL_STATE_IDLE) {
          if (!isAutoPlay || TextUtils.isEmpty(huiben.data.get(currentPage).audio_url)) {
            if (hasComment && currentPage == huiben.data.size() - 1) {
              if (targetIndex == huiben.data.size() && isLastPageBefore) {
                new Handler().postDelayed(new Runnable() {
                  @Override
                  public void run() {
                    if (showDialog) {
                      return;
                    }
                    showCommentDialog();
                  }
                }, 100);
              }
              Logger.i("zkx onPageSelected " + targetIndex);
            }
          }
        }
      }
    });
  }

  /**
   * 弹评论框
   */
  private void showCommentDialog() {
    showDialog = true;
    DialogUtil.showButtonDialog(this, "看完了，发表评论把", "评分 ："
            + getString(R.string.hfx_score_text, huiben.starank), "取消", dialogCancelClick,
        "确定", dialogConfirmClick, dialogCloseClick);
  }

  /**
   * 取消操作
   */
  private OnClickListener dialogCancelClick = new OnClickListener() {
    @Override
    public void onClick(View view) {
      showDialog = false;
    }
  };
  /**
   * 取消操作
   */
  private OnClickListener dialogCloseClick = new OnClickListener() {
    @Override
    public void onClick(View view) {
      showDialog = false;
    }
  };
  /**
   * 确认操作
   */
  private OnClickListener dialogConfirmClick = new OnClickListener() {
    @Override
    public void onClick(View view) {
      targetIndex = 0;
      showDialog = false;
      openView(huiben.commentUrl);
    }
  };

  private void initMenu() {
    mMenuViewSize = getResources().getDimensionPixelSize(R.dimen.hfx_read_menu_size);
    mMenuLayout = LayoutInflater.from(this).inflate(R.layout.hfx_layout_menu_read_book_show, null);
    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    addContentView(mMenuLayout, lp);
    bgImag = mMenuLayout.findViewById(R.id.bgImg);
    //底部菜单
    mAudioPlay = findViewById(R.id.iv_audio_play);
    mAutoPlay = findViewById(R.id.iv_auto_play);
    mBright = findViewById(R.id.iv_brightness);
//    autoPlayView = mMenuLayout.findViewById(R.id.iv_audio_play);
//    mMenuLayout.findViewById(R.id.iv_auto_play).setOnClickListener(clickListener);
//    mMenuLayout.findViewById(R.id.read_menu_brightness).setOnClickListener(clickListener);
    mAudioPlay.setOnClickListener(clickListener);
    mAutoPlay.setOnClickListener(clickListener);
    mBright.setOnClickListener(clickListener);
//    setAutoPlay(isAutoPlay);
    mBrightnessViewStub = findViewById(R.id.brightness_container);
  }

  /**
   * 更新页面角标
   */
  @SuppressLint("DefaultLocale")
  private void updatePagerIndex(int position) {
    mPageIndex.setText(String.format("%d/%d", position + 1, huiben.data.size()));
    if (position == huiben.data.size() - 1) {
      mAutoPlay.setSelected(false);
      mAudioPlay.setEnabled(true);
      mAudioPlay.setSelected(false);
      //如果音频为空 这里需要手动去处罚评论
      if (TextUtils.isEmpty(huiben.data.get(position).audio_url)) {
        if (isAutoPlay && hasComment && currentPage == huiben.data.size() - 1) {
          new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              showCommentDialog();
            }
          }, 3000);
        } else if (hasComment && currentPage == huiben.data.size() - 1) {
          targetIndex = huiben.data.size();
        }
        isAutoPlay = false;
      }
    } else {
      targetIndex = position;
    }
//    if (TextUtils.isEmpty(huiben.bookPage.get(position).soundRecording)) {
//      Utils.toast(this, "没有音频");
//      Logger.i("zkx position :" +position +" 没有音频");
//    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mMenuLayout != null) {
      mMenuLayout.removeCallbacks(hideNavigationRunnable);
    }
    if (mBrightnessView != null) {
      mBrightnessView.removeCallbacks(hideBrightnessLayoutRunnable);
    }
    if (mViewPager != null) {
      mViewPager.removeCallbacks(nextPageRunnable);
      mViewPager.removeCallbacks(orientationRunnable);
      if (huiben != null && mViewPager.getCurrentItem() != huiben.data.size() - 1) {
        HfxPreferenceUtil.saveLastRead(this, huiben.bookId, mViewPager.getCurrentItem());
      }
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_MENU: {
        toggleNavigation();
      }
      return true;
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  private void loadJsonData(String url) {
    showProgress(getString(R.string.hfx_loading));
    new LoadTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
  }

  private static class LoadTask extends
      WeakAsyncTask<String, Void, HuibenShowWork, ReadBookWorkEvalActivity> {

    LoadTask(ReadBookWorkEvalActivity readActivity) {
      super(readActivity);
    }

    @Override
    protected HuibenShowWork doInBackground(ReadBookWorkEvalActivity readActivity, String... params) {
      Context context = readActivity.getApplicationContext();
      String url = params[0];
      final File jsonFile = FileUtil.getCachedFile(context, url);
      Logger.d(TAG, "request: " + url);
      Request request = new Request.Builder()
          .cacheControl(CacheControl.FORCE_NETWORK)
          .url(Utils.encodeString(url))
          .build();
      if (NetworkUtil.isNetworkAvailable(context)) {
        try {
          Response response = readActivity.getOkHttpClient().newCall(request).execute();
          if (response != null && response.isSuccessful()) {
            String body = response.body().string();
            HuibenShowWork book = Utils.parseJsonString(body, HuibenShowWork.class);
            if (book != null) {
              Logger.d(TAG, "save cache: " + jsonFile);
              FileUtil.StringToFile(body, jsonFile, "utf-8");
              return book;
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (jsonFile.exists()) {
        Logger.d(TAG, "read cache: " + jsonFile);
        return Utils.parseJsonFile(jsonFile, HuibenShowWork.class);
      }
      return null;
    }

    @Override
    protected void onPostExecute(ReadBookWorkEvalActivity readActivity, HuibenShowWork data) {
      if (readActivity != null && !readActivity.isFinishing()) {
        readActivity.onLoadDone(data);
      }
    }
  }

  private void onLoadDone(HuibenShowWork data) {
    hideProgress();
    if (data == null || data.data == null || data.data.isEmpty()) {
      toast(getString(R.string.hfx_error_read));
      finish();
    } else {
      huiben = data;
      invalidateOptionsMenu();
      //add comment page
      if (!TextUtils.isEmpty(huiben.commentUrl)) {
        hasComment = true;
      }
      final int jumpTo = HfxPreferenceUtil.getLastRead(this, huiben.bookId);
      updatePagerIndex(0);
      //初始化分数列表
      initScoreList();
      delayHideNavigation(500);
//      checkShowContinueDialog();
      initAdapter();
    }
  }

  private void initScoreList() {
    if (huiben != null && huiben.data != null && huiben.data.size() > 0) {
      boolean hasScore = scoreIsEmpty();
      //分数为空  隐藏成绩列表及页数角标 显示title 否则反之
      if (hasScore) {
        updateTitle();
      } else {
        mPageIndex.setVisibility(View.VISIBLE);
        mScoreList.setVisibility(View.VISIBLE);
        mTitle.setVisibility(View.GONE);
        initTopScoreList();
      }
    } else {
      //更新title
      updateTitle();
    }
  }

  private void initTopScoreList() {
    if (scoreAdapter == null) {
      scoreAdapter = new ScoreAdapter();
      mScoreList.setAdapter(scoreAdapter);
      mScoreList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    } else {
      scoreAdapter.notifyDataSetChanged();
    }
  }

  /**
   * 判断分数是否为空
   */
  private boolean scoreIsEmpty() {
    for (HuibenShowWork.PagerDetail page : huiben.data) {
      if (!page.isCommentPage) {
        if (TextUtils.isEmpty(page.score)) {
          return true;
        }
      }

    }
    return false;
  }

  /**
   * 更新title显示
   */
  private void updateTitle() {
    mPageIndex.setVisibility(View.GONE);
    mScoreList.setVisibility(View.GONE);
    mTitle.setVisibility(View.VISIBLE);
    mTitle.setText(TextUtils.isEmpty(huiben.bookName) ? "" : huiben.bookName);
  }

  private void checkShowContinueDialog() {
    final int jumpTo = HfxPreferenceUtil.getLastRead(this, huiben.bookId);
    if (jumpTo > 0) {
      showDialog("提示", getString(R.string.hfx_continue_read_message, jumpTo + 1), "继续阅读",
          new OnClickListener() {
            @Override
            public void onClick(View v) {
              HfxPreferenceUtil.saveLastRead(ReadBookWorkEvalActivity.this, huiben.bookId, 0);
              initAdapter();
              jumpPage(jumpTo);
            }
          }, "从头开始", new OnClickListener() {
            @Override
            public void onClick(View v) {
              initAdapter();
            }
          });
    } else {
      initAdapter();
    }
  }

  private void initAdapter() {
    mAdapter = new ReadAdapter(this);
    mViewPager.setAdapter(mAdapter);
    mViewPager.setPageTransformer(false, new MyPageTransformer());
    mViewPager.setPageMargin(Utils.dp2px(this, 20));
  }

  private void showSpeakers() {
    View v = LayoutInflater.from(this).inflate(R.layout.hfx_layout_speaker, null);
    ListView listView = (ListView) v.findViewById(R.id.list);
    ImageView title = (ImageView) v.findViewById(R.id.title);
//    if (!TextUtils.isEmpty(huiben.banner_image)) {
//      Glide.with(this)
//          .asBitmap()
//          .load(huiben.banner_image)
//          .into(title);
//    }
//    listView.setAdapter(new SpeakerAdapter(this, huiben.workuser));
//    final AlertDialog dialog = new AlertDialog.Builder(this)
//        .setView(v)
//        .setPositiveButton(R.string.hfx_jump, new DialogInterface.OnClickListener() {
//          @Override
//          public void onClick(DialogInterface dialog, int which) {
//            checkShowContinueDialog();
//          }
//        }).create();
//    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//      @Override
//      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        loadJsonData(huiben.workuser.get(position).url);
//        dialog.dismiss();
//      }
//    });
//    dialog.setCancelable(false);
//    dialog.show();
  }

  private AlertDialog dialog;

  private void showJumpDialog() {
    int width;
    int height;
    if (Utils.isTablet(this)) {
      width = 211;
    } else {
      width = 160;
    }
    NamiboxNiceDialog.init()
        .setLayoutId(R.layout.wheel_view)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(com.othershe.nicedialog.ViewHolder holder, final BaseNiceDialog dialog) {
            final int size = huiben.data.size();
            List<String> integers = new ArrayList<>();
            for (int i = 1; i < size + 1; i++) {
              integers.add(i + "");
            }
            WheelView wv = holder.getView(R.id.wheel_view_wv);
            wv.setOffset(2);
            wv.setItems(integers);
            wv.setSeletion(currentPage);
            wv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
              @Override
              public void onSelected(int selectedIndex, String item) {
                jumpPage(selectedIndex - 2);
                dialog.dismiss();
                Log.d(TAG, "[Dialog]selectedIndex: " + selectedIndex + ", item: " + item);
              }
            });
          }
        })
        .setOutCancel(true)
        .setWidth(width)
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(getSupportFragmentManager());
  }

  private class PlayRunnable implements Runnable {

    String url;

    PlayRunnable(String url) {
      this.url = url;
    }

    @Override
    public void run() {
      tryPlayMp3(url);
    }
  }

  private PlayRunnable playRunnable;

  private void delayPlayMp3(String url) {
    if (playRunnable != null) {
      mViewPager.removeCallbacks(playRunnable);
    }
    playRunnable = new PlayRunnable(url);
    mViewPager.postDelayed(playRunnable, 500);
  }

  private void tryPlayMp3(String recordingUrl) {
    mAudioPlay.setEnabled(!TextUtils.isEmpty(recordingUrl));
    if (isFinishing()) {
      return;
    }
    mViewPager.removeCallbacks(nextPageRunnable);
    if (!TextUtils.isEmpty(recordingUrl)) {
      if (!TextUtils.isEmpty(currentMp3) && currentMp3.equals(recordingUrl)) {
        Logger.w(TAG, "current mp3, just return");
      } else {
        currentMp3 = recordingUrl;
        exoAudioPlayer.play(Uri.parse(recordingUrl));
      }
    } else {
      currentMp3 = null;
      mViewPager.postDelayed(nextPageRunnable, 3000);
    }
  }

  private void jumpPage(int position) {
    int currentIndex = mViewPager.getCurrentItem();
    if (currentIndex != position && position >= 0 && position < huiben.data.size()) {
      mViewPager.setCurrentItem(position);
      tryPlayMp3(huiben.data.get(position).audio_url);
    }
  }

  private Runnable nextPageRunnable = new Runnable() {
    @Override
    public void run() {
      int currentIndex = mViewPager.getCurrentItem();
      if (isAutoPlay && currentIndex < huiben.data.size() - 1) {
        jumpPage(currentIndex + 1);
      }
    }
  };

  private class OrientationRunnable implements Runnable {

    boolean portrait;

    @Override
    public void run() {
      setRequestedOrientation(portrait ?
          ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
  }

  private OrientationRunnable orientationRunnable = new OrientationRunnable();

  private void setScreenOrientation(boolean portrait) {
    mViewPager.removeCallbacks(orientationRunnable);
    orientationRunnable.portrait = portrait;
    //BUG[android v2.x 0000210], delay set orientation
    mViewPager.postDelayed(orientationRunnable, 500);

//    bgImag.setBackgroundResource(portrait ? R.drawable.np_huiben_show_portrait : R.drawable.np_huiben_show);
//    mRootView.setBackgroundResource(portrait ? R.drawable.hfx_ic_eval_bg : R.drawable.hfx_ic_eval_bg);
  }

  private static class ReadAdapter extends PagerAdapter {

    private ViewHolder[] viewHolders;
    private ReadBookWorkEvalActivity activity;
    private int currentPosition = -1;

    ReadAdapter(ReadBookWorkEvalActivity activity) {
      this.activity = activity;
      viewHolders = new ViewHolder[activity.huiben.data.size()];
    }

    int getCurrentPosition() {
      return currentPosition;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
      return view == object;
    }

    @Override
    public int getCount() {
      return activity.huiben.data.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      container.removeView((View) object);
      viewHolders[position] = null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
      if (activity.huiben.data.get(position).isCommentPage) {
        View v = LayoutInflater.from(activity)
            .inflate(R.layout.hfx_layout_comment_page, container, false);
        container.addView(v);
        TextView scoreView = (TextView) v.findViewById(R.id.score_text);
        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating);
        TextView commentView = (TextView) v.findViewById(R.id.comment_text);
        ratingBar.setRating(activity.huiben.starank);
        ratingBar.setVisibility(activity.huiben.starank == 0 ? View.GONE : View.VISIBLE);
        scoreView.setText(activity.getString(R.string.hfx_score_text, activity.huiben.starank));
        scoreView.setVisibility(activity.huiben.starank == 0 ? View.GONE : View.VISIBLE);
        String commentNumber = Utils
            .formatCount(activity, (int) activity.huiben.comment);
        commentView.setText(activity.getString(R.string.hfx_comment_text, commentNumber));
        v.findViewById(R.id.comment_btn).setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            activity.openView(activity.huiben.commentUrl);
            activity.finish();
          }
        });
        v.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            activity.toggleNavigation();
          }
        });
        return v;
      } else {
        View v = LayoutInflater.from(container.getContext())
            .inflate(R.layout.hfx_layout_huiben_item_work, container, false);
        container.addView(v);
        final ViewHolder viewHolder = new ViewHolder();
        viewHolders[position] = viewHolder;
        viewHolder.position = position;
        viewHolder.activity = activity;
        viewHolder.mContentView = (PhotoView) v.findViewById(R.id.huiben_page);
        viewHolder.iv_head = (ImageView) v.findViewById(R.id.iv_head);
        viewHolder.tv_content = (TextView) v.findViewById(R.id.tv_content);
        viewHolder.tv_score = (TextView) v.findViewById(R.id.tv_score);

        viewHolder.rootView = v.findViewById(R.id.parent_view);
        viewHolder.mContentView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
            activity.hideBrightness();
            activity.toggleNavigation();
          }
        });
        viewHolder.loadImage();

        viewHolder.tv_content.setText(activity.huiben.data.get(position).exercise_text);
        viewHolder.tv_score.setText(activity.huiben.data.get(position).score);
        return v;
      }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
      super.setPrimaryItem(container, position, object);
      if (currentPosition == position) {
        return;
      }
      Logger.d(TAG, "setPrimaryItem: " + position);
      currentPosition = position;

      //设置横竖屏自适应
      if (!activity.huiben.data.get(position).isCommentPage) {
//        if (viewHolders[position] != null && viewHolders[position].resPrepared) {
        viewHolders[position].setOrientation(true);
//        }
      }
    }

  }

  private static class ViewHolder {

    ReadBookWorkEvalActivity activity;
    PhotoView mContentView;
    ImageView iv_head;
    TextView tv_content, tv_score;
    ConstraintLayout rootView;
    boolean isPortrait = true;
    boolean resPrepared;
    int position;

    void setOrientation(boolean delayPlay) {
      if (position == activity.mAdapter.getCurrentPosition()) {
        //默认横屏
        activity.setScreenOrientation(false);
        if (delayPlay) {
          activity.delayPlayMp3(activity.huiben.data.get(position).audio_url);
        } else {
          activity.tryPlayMp3(activity.huiben.data.get(position).audio_url);
        }
      }
    }

    void loadImage() {
      String pageUrl = activity.huiben.data.get(position).page_bg_img;
      //TODO 背景随position变化
      int pos = position % 3;
      switch (pos) {
        case 0:
          mContentView.setBackgroundResource(R.drawable.hfx_evalution_item_bg_0);
          break;
        case 1:
          mContentView.setBackgroundResource(R.drawable.hfx_evalution_item_bg_1);
          break;
        case 2:
          mContentView.setBackgroundResource(R.drawable.hfx_evalution_item_bg_2);
          break;
      }

//      final File pageFile = FileUtil.getCachedFile(activity, pageUrl);
//      if (!pageFile.exists() || FileUtil.isFileExpired(pageFile)) {
//        RequestOptions options = new RequestOptions()
//            .skipMemoryCache(true)
//            .diskCacheStrategy(DiskCacheStrategy.NONE);
//        Glide.with(activity)
//            .asBitmap()
//            .load(Utils.encodeString(pageUrl))
//            .apply(options)
//            .listener(new RequestListener<Bitmap>() {
//              @Override
//              public boolean onLoadFailed(@Nullable GlideException e, Object model,
//                                          Target<Bitmap> target,
//                                          boolean isFirstResource) {
//                mErrorLayout.setVisibility(View.VISIBLE);
//                return false;
//              }
//
//              @Override
//              public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
//                                             DataSource dataSource, boolean isFirstResource) {
//                Logger.d("save image file " + position);
//                ImageUtil.compressBmpToFile(resource, 90, pageFile);
//                isPortrait = resource.getHeight() >= resource.getWidth();
//                mErrorLayout.setVisibility(View.GONE);
//                resPrepared = true;
//                setOrientation(false);
//                return false;
//              }
//            })
//            .into(mContentView);
//      } else {
//        RequestOptions options = new RequestOptions()
//            .skipMemoryCache(true)
//            .diskCacheStrategy(DiskCacheStrategy.NONE);
//        Glide.with(activity)
//            .asBitmap()
//            .load(pageFile)
//            .apply(options)
//            .listener(new RequestListener<Bitmap>() {
//              @Override
//              public boolean onLoadFailed(@Nullable GlideException e, Object model,
//                                          Target<Bitmap> target,
//                                          boolean isFirstResource) {
//                mErrorLayout.setVisibility(View.VISIBLE);
//                pageFile.delete();
//                return false;
//              }
//
//              @Override
//              public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
//                                             DataSource dataSource, boolean isFirstResource) {
//                Logger.d("load from cache file " + position);
//                isPortrait = resource.getHeight() >= resource.getWidth();
//                mErrorLayout.setVisibility(View.GONE);
//                resPrepared = true;
//                setOrientation(false);
//                return false;
//              }
//            })
//            .into(mContentView);
//      }
    }
  }

  private class SpeakerAdapter extends ArrayAdapter<Huiben.WorkUser> {

    LayoutInflater mInflater;

    SpeakerAdapter(Context context, List<Huiben.WorkUser> objects) {
      super(context, 0, objects);
      mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = mInflater.inflate(R.layout.hfx_layout_speaker_item, parent, false);
      }
      Huiben.WorkUser speaker = getItem(position);
      ImageView header = (ImageView) convertView.findViewById(R.id.speaker_header);
      TextView title = (TextView) convertView.findViewById(R.id.speaker_title);
      TextView info = (TextView) convertView.findViewById(R.id.speaker_info);
      RatingBar ratingBar = (RatingBar) convertView.findViewById(R.id.speaker_rating);
      TextView comments = (TextView) convertView.findViewById(R.id.speaker_comment);
      RequestOptions options = new RequestOptions()
          .placeholder(R.drawable.hfx_ic_action_account_circle)
          .error(R.drawable.hfx_ic_action_account_circle);
      Glide.with(ReadBookWorkEvalActivity.this)
          .asBitmap()
          .load(speaker.headimage)
          //.skipMemoryCache(true)
          .apply(options)
          .into(header);
      title.setText(speaker.alias);
      info.setText(speaker.introduce);
      ratingBar.setRating(speaker.starankcount);
      ratingBar.setVisibility(speaker.starankcount == 0 ? View.INVISIBLE : View.VISIBLE);
      String comment = Utils.formatCount(getContext(), speaker.commentcount);
      String read = Utils.formatCount(getContext(), speaker.readcount);
      comments.setText(getString(R.string.hfx_speaker_info, comment, read));
      return convertView;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
//    if (huiben != null && huiben.wxshare != null && huiben.wxshare.doclink != null) {
//      menu.add(0, 100, 100, "分享")
//          .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
//          .setVisible(true);
//      return super.onCreateOptionsMenu(menu);
//    } else {
//      menu.removeGroup(0);
//      return super.onCreateOptionsMenu(menu);
//    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
//    if (item.getItemId() == 100) {
//      if (huiben.wxshare != null) {
//        showShare(null, huiben.wxshare.imgurl, huiben.wxshare.doclink,
//            huiben.wxshare.grouptitile, huiben.wxshare.friendtitile,
//            huiben.wxshare.groupcontent, null);
//      }
//      return true;
//    }
//    if (item.getItemId() == android.R.id.home) {
//      finish();
//      return true;
//    }
    return super.onOptionsItemSelected(item);
  }

  class ScoreAdapter extends RecyclerView.Adapter<ScoreHolder> {

    ScoreHolder[] scoreHolders;
    int currentPosition;

    ScoreAdapter() {
      currentPosition = currentPage;
      scoreHolders = new ScoreHolder[huiben == null || huiben.data == null ? 0 : huiben.data.size()];
    }

    @NonNull
    @Override
    public ScoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View itemView = getLayoutInflater().inflate(R.layout.layout_hfx_score_item, parent, false);
      return new ScoreHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreHolder holder, final int position) {
      scoreHolders[position] = holder;
      holder.itemScoreRoot.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          mViewPager.setCurrentItem(position);
        }
      });
      HuibenShowWork.PagerDetail pagerDetail = huiben.data.get(position);
      if (pagerDetail != null) {
        holder.ivItemRecord.setVisibility(View.GONE);
        holder.tvEvalScore.setText(TextUtils.isEmpty(pagerDetail.score) ? "" : pagerDetail.score);
      }
      holder.ivEvalItemSelectBg.setVisibility(currentPosition == position ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreHolder holder, int position,
                                 @NonNull List<Object> payloads) {
      if (!payloads.isEmpty()) {
        int score = (int) payloads.get(0);
        holder.ivItemRecord.setVisibility(View.GONE);
        if (score == -1) {
        } else {
          holder.tvEvalScore.setVisibility(View.VISIBLE);
          holder.tvEvalScore.setText(String.valueOf(score));
        }
      } else {
        super.onBindViewHolder(holder, position, payloads);
      }
    }

    @Override
    public int getItemCount() {
      return huiben == null || huiben.data == null ? 0 : huiben.data.size();
    }

    public void lightItemByIndex(int position) {
      if (currentPosition != position) {
        ScoreHolder scoreHolder = scoreHolders[currentPosition];
        if (scoreHolder != null) {
          scoreHolder.ivEvalItemSelectBg.setVisibility(View.GONE);
        }
        ScoreHolder currentHolder = scoreHolders[position];
        if (currentHolder != null) {
          currentPosition = position;
          currentHolder.ivEvalItemSelectBg.setVisibility(View.VISIBLE);
        }
      }
    }
  }

  private static class ScoreHolder extends RecyclerView.ViewHolder {

    ImageView ivEvalItemSelectBg;
    ImageView ivItemRecord;
    TextView tvEvalScore;
    View itemScoreRoot;

    ScoreHolder(View itemView) {
      super(itemView);
      itemScoreRoot = itemView.findViewById(R.id.itemScoreRoot);
      ivEvalItemSelectBg = itemView.findViewById(R.id.ivEvalItemSelectBg);
      ivItemRecord = itemView.findViewById(R.id.ivItemRecord);
      tvEvalScore = itemView.findViewById(R.id.tvEvalScore);
    }
  }

}
