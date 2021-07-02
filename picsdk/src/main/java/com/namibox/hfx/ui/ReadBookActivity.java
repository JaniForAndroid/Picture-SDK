package com.namibox.hfx.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.exoaudioplayer.aduio.base.AbstractAudioPlayer;
import com.example.exoaudioplayer.aduio.base.AudioCallBack;
import com.example.exoaudioplayer.aduio.base.AudioPlayerFactory;
import com.example.exoaudioplayer.aduio.base.Constants;
import com.example.picsdk.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.namibox.commonlib.activity.AbsFunctionActivity;
import com.namibox.hfx.bean.Huiben;
import com.namibox.hfx.utils.HfxPreferenceUtil;
import com.namibox.util.FileUtil;
import com.namibox.util.ImageUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.WeakAsyncTask;
import java.io.File;
import java.util.List;
import okhttp3.CacheControl;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ReadBookActivity extends AbsFunctionActivity {

  private static final String TAG = "AbsReadBookActivity";
  public static final String ARG_JSON_URL = "json_url";
  private Huiben huiben;
  private ViewPager mViewPager;
  private TextView autoPlayView;
  private View mMenuLayout;
  private int mMenuViewSize;
  private ReadAdapter mAdapter;
  private boolean isAutoPlay;
  private String currentMp3;
  private Toolbar mToolbar;
  private ImageButton playBtn;
  private int mBrightness;
  private int mToolbarSize;
  private static final int DISPLAY_TIME = 5000;
  private static final int ANIMATE_TIME = 150;
  private boolean menuShowing = true;
  private ViewStub mBrightnessViewStub;
  private View mBrightnessView;
  private SeekBar mBrightnessSeekBar;
  private AbstractAudioPlayer exoAudioPlayer;

  private Runnable hideBrightnessLayoutRunnable = new Runnable() {
    @Override
    public void run() {
      hideBrightness();
    }
  };

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
        mToolbar.setVisibility(View.VISIBLE);
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
        mMenuLayout.setVisibility(View.GONE);
        mToolbar.setVisibility(View.GONE);
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
      if (v.getId() == R.id.read_menu_catalog) {
        showJumpDialog();
      } else if (v.getId() == R.id.read_menu_continue) {
        setAutoPlay(!isAutoPlay);
      } else if (v.getId() == R.id.read_menu_brightness) {
        showBrightness();
      }
      hideNavigation();
    }
  };

  private void setAutoPlay(boolean autoPlay) {
    int d =
        autoPlay ? R.drawable.hfx_read_menu_auto_flip_on : R.drawable.hfx_read_menu_auto_flip_off;
    autoPlayView.setCompoundDrawablesWithIntrinsicBounds(0, d, 0, 0);
    autoPlayView.setText(autoPlay ? "自动翻页：开" : "自动翻页：关");
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
    setContentView(R.layout.hfx_activity_read);
    mToolbar = (Toolbar) findViewById(R.id.tool_bar);
    mToolbarSize = getResources().getDimensionPixelSize(R.dimen.hfx_actionbar_size);
    setSupportActionBar(mToolbar);
    mViewPager = (ViewPager) findViewById(R.id.pager);
    mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {

      }

      @Override
      public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_DRAGGING) {
          mViewPager.removeCallbacks(nextPageRunnable);
        }
      }
    });
    playBtn = (ImageButton) findViewById(R.id.audio_simple_playpause);
    playBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        exoAudioPlayer.playPause();
      }
    });
    //init menu
    mMenuViewSize = getResources().getDimensionPixelSize(R.dimen.hfx_read_menu_size);
    mMenuLayout = LayoutInflater.from(this).inflate(R.layout.hfx_layout_menu_read_book, null);
    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    addContentView(mMenuLayout, lp);
    autoPlayView = (TextView) mMenuLayout.findViewById(R.id.read_menu_continue);
    autoPlayView.setOnClickListener(clickListener);
    setAutoPlay(isAutoPlay);
    mMenuLayout.findViewById(R.id.read_menu_catalog).setOnClickListener(clickListener);
    mMenuLayout.findViewById(R.id.read_menu_brightness).setOnClickListener(clickListener);
    mBrightnessViewStub = (ViewStub) findViewById(R.id.brightness_container);

    Intent intent = getIntent();
    String jsonUrl = intent.getStringExtra(ARG_JSON_URL);
    loadJsonData(jsonUrl);
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
        }
        boolean playing = playbackState == ExoPlayer.STATE_READY && playWhenReady;
        playBtn.setImageResource(playing ?
            R.drawable.hfx_ic_player_pause_dark : R.drawable.hfx_ic_player_play_dark);
      }
    });
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
      if (huiben != null && mViewPager.getCurrentItem() != huiben.bookpage.size() - 1) {
        HfxPreferenceUtil.saveLastRead(this, huiben.bookid, mViewPager.getCurrentItem());
      }
    }
    if (exoAudioPlayer != null) {
      exoAudioPlayer.releasePlayer();
      exoAudioPlayer.setPlayerCallBack(null);
      exoAudioPlayer = null;
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

  private static class LoadTask extends WeakAsyncTask<String, Void, Huiben, ReadBookActivity> {

    LoadTask(ReadBookActivity readActivity) {
      super(readActivity);
    }

    @Override
    protected Huiben doInBackground(ReadBookActivity readActivity, String... params) {
      Context context = readActivity.getApplicationContext();
      String url = params[0];
      final File jsonFile = FileUtil.getCachedFile(context, url);
      Logger.d(TAG, "request: " + url);
      Request request = new Request.Builder()
          .cacheControl(CacheControl.FORCE_NETWORK)
          .url(com.namibox.util.Utils.encodeString(url))
          .build();
      if (NetworkUtil.isNetworkAvailable(context)) {
        try {
          Response response = readActivity.getOkHttpClient().newCall(request).execute();
          if (response != null && response.isSuccessful()) {
            String body = response.body().string();
            Huiben book = com.namibox.util.Utils.parseJsonString(body, Huiben.class);
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
        return com.namibox.util.Utils.parseJsonFile(jsonFile, Huiben.class);
      }
      return null;
    }

    @Override
    protected void onPostExecute(ReadBookActivity readActivity, Huiben data) {
      if (readActivity != null && !readActivity.isFinishing()) {
        readActivity.onLoadDone(data);
      }
    }
  }

  private void onLoadDone(Huiben data) {
    hideProgress();
    if (data == null || data.bookpage == null || data.bookpage.isEmpty()) {
      toast(getString(R.string.hfx_error_read));
      finish();
    } else {
      huiben = data;
      invalidateOptionsMenu();
      //add comment page
      if (!TextUtils.isEmpty(huiben.comment_url)) {
        Huiben.BookPage bookPage = new Huiben.BookPage();
        bookPage.isCommentPage = true;
        huiben.bookpage.add(bookPage);
      }
      delayHideNavigation(500);
      setAutoPlay(huiben.autoplay);
      setTitle(huiben.bookname);
      if (huiben.workuser != null && !huiben.workuser.isEmpty()) {
        showSpeakers();
      } else {
        checkShowContinueDialog();
      }
    }
  }

  private void checkShowContinueDialog() {
    final int jumpTo = HfxPreferenceUtil.getLastRead(this, huiben.bookid);
    if (jumpTo > 0) {
      showDialog("提示", getString(R.string.hfx_continue_read_message, jumpTo + 1), "继续阅读",
          new OnClickListener() {
            @Override
            public void onClick(View v) {
              HfxPreferenceUtil.saveLastRead(ReadBookActivity.this, huiben.bookid, 0);
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
  }

  private void showSpeakers() {
    View v = LayoutInflater.from(this).inflate(R.layout.hfx_layout_speaker, null);
    ListView listView = (ListView) v.findViewById(R.id.list);
    ImageView title = (ImageView) v.findViewById(R.id.title);
    if (!TextUtils.isEmpty(huiben.banner_image)) {
      Glide.with(this)
          .asBitmap()
          .load(huiben.banner_image)
          .into(title);
    }
    listView.setAdapter(new SpeakerAdapter(this, huiben.workuser));
    final AlertDialog dialog = new AlertDialog.Builder(this)
        .setView(v)
        .setPositiveButton(R.string.hfx_jump, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            checkShowContinueDialog();
          }
        }).create();
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        loadJsonData(huiben.workuser.get(position).url);
        dialog.dismiss();
      }
    });
    dialog.setCancelable(false);
    dialog.show();
  }

  private void showJumpDialog() {
    View v = LayoutInflater.from(this).inflate(R.layout.hfx_layout_page_jump, null);
    final SeekBar seekBar = (SeekBar) v.findViewById(R.id.seekbar);
    final TextView pageNumber = (TextView) v.findViewById(R.id.page_number);
    final int size = huiben.bookpage.size();
    seekBar.setMax(size - 1);
    seekBar.setProgress(mViewPager.getCurrentItem());
    String text = getString(R.string.hfx_jump_page_number_text, mViewPager.getCurrentItem() + 1,
        size);
    pageNumber.setText(text);
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        String text = getString(R.string.hfx_jump_page_number_text, progress + 1, size);
        pageNumber.setText(text);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
    new AlertDialog.Builder(this)
        .setView(v)
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            jumpPage(seekBar.getProgress());
          }
        })
        .setOnDismissListener(new DialogInterface.OnDismissListener() {
          @Override
          public void onDismiss(DialogInterface dialog) {
            hideNavigation();
          }
        })
        .create().show();
  }

  private class PlayRunnable implements Runnable {

    int position;

    PlayRunnable(int position) {
      this.position = position;
    }

    @Override
    public void run() {
      tryPlayMp3(position);
    }
  }

  private PlayRunnable playRunnable;

  private void delayPlayMp3(int position) {
    if (playRunnable != null) {
      mViewPager.removeCallbacks(playRunnable);
    }
    playRunnable = new PlayRunnable(position);
    mViewPager.postDelayed(playRunnable, 500);
  }

  private void tryPlayMp3(int position) {
    if (isFinishing()) {
      return;
    }
    mViewPager.removeCallbacks(nextPageRunnable);
    int mp3_index = huiben.bookpage.get(position).mp3_index;
    if (mp3_index != -1) {
      playBtn.setVisibility(View.VISIBLE);
      String url = huiben.bookaudio.get(mp3_index).mp3_url;
      if (!TextUtils.isEmpty(currentMp3) && currentMp3.equals(url)) {
        Logger.w(TAG, "current mp3, just return");
      } else {
        //int duration = (int) (huiben.bookaudio.get(mp3_index).duration * 1000);
        //setDuration(duration);
        currentMp3 = url;
        exoAudioPlayer.play(Uri.parse(url));
      }
    } else {
      playBtn.setVisibility(View.GONE);
      currentMp3 = null;
      mViewPager.postDelayed(nextPageRunnable, 3000);
    }
  }

  private void jumpPage(int position) {
    int currentIndex = mViewPager.getCurrentItem();
    if (currentIndex != position && position >= 0 && position < huiben.bookpage.size()) {
      mViewPager.setCurrentItem(position);
      //tryPlayMp3(pageIndex);
    }
  }

  private Runnable nextPageRunnable = new Runnable() {
    @Override
    public void run() {
      int currentIndex = mViewPager.getCurrentItem();
      if (isAutoPlay && currentIndex < huiben.bookpage.size() - 1) {
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
  }

  private static class ReadAdapter extends PagerAdapter {

    private ViewHolder[] viewHolders;
    private ReadBookActivity activity;
    private int currentPosition = -1;

    ReadAdapter(ReadBookActivity activity) {
      this.activity = activity;
      viewHolders = new ViewHolder[activity.huiben.bookpage.size()];
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
      return activity.huiben.bookpage.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      container.removeView((View) object);
      viewHolders[position] = null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
      if (activity.huiben.bookpage.get(position).isCommentPage) {
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
        String commentNumber = com.namibox.util.Utils
            .formatCount(activity, activity.huiben.comment);
        commentView.setText(activity.getString(R.string.hfx_comment_text, commentNumber));
        v.findViewById(R.id.comment_btn).setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            activity.openView(activity.huiben.comment_url);
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
            .inflate(R.layout.hfx_layout_huiben_item, container, false);
        container.addView(v);
        final ViewHolder viewHolder = new ViewHolder();
        viewHolders[position] = viewHolder;
        viewHolder.position = position;
        viewHolder.activity = activity;
        viewHolder.mContentView = (PhotoView) v.findViewById(R.id.huiben_page);
        viewHolder.mErrorLayout = (ViewGroup) v.findViewById(R.id.read_error_layout);
        v.findViewById(R.id.read_error_btn).setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            viewHolder.loadImage();
          }
        });
        viewHolder.mContentView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
          @Override
          public void onPhotoTap(View view, float x, float y) {
            activity.hideBrightness();
            activity.toggleNavigation();
          }
        });
        viewHolder.loadImage();
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
      if (!activity.huiben.bookpage.get(position).isCommentPage) {
        if (viewHolders[position] != null && viewHolders[position].resPrepared) {
          viewHolders[position].setOrientation(true);
        }
      }
    }

  }

  private static class ViewHolder {

    ReadBookActivity activity;
    ViewGroup mErrorLayout;
    PhotoView mContentView;
    boolean isPortrait = true;
    boolean resPrepared;
    int position;

    void setOrientation(boolean delayPlay) {
      if (position == activity.mAdapter.getCurrentPosition()) {
        activity.setScreenOrientation(isPortrait);
        if (delayPlay) {
          activity.delayPlayMp3(position);
        } else {
          activity.tryPlayMp3(position);
        }
      }
    }

    void loadImage() {
      mErrorLayout.setVisibility(View.GONE);
      String pageUrl = activity.huiben.bookpage.get(position).page_url;
      final File pageFile = FileUtil.getCachedFile(activity, pageUrl);
      if (!pageFile.exists() || FileUtil.isFileExpired(pageFile)) {
        RequestOptions options = new RequestOptions()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE);
        Glide.with(activity)
            .asBitmap()
            .load(com.namibox.util.Utils.encodeString(pageUrl))
            .apply(options)
            .listener(new RequestListener<Bitmap>() {
              @Override
              public boolean onLoadFailed(@Nullable GlideException e, Object model,
                  Target<Bitmap> target,
                  boolean isFirstResource) {
                mErrorLayout.setVisibility(View.VISIBLE);
                return false;
              }

              @Override
              public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
                  DataSource dataSource, boolean isFirstResource) {
                Logger.d("save image file " + position);
                ImageUtil.compressBmpToFile(resource, 90, pageFile);
                isPortrait = resource.getHeight() >= resource.getWidth();
                mErrorLayout.setVisibility(View.GONE);
                resPrepared = true;
                setOrientation(false);
                return false;
              }
            })
            .into(mContentView);
      } else {
        RequestOptions options = new RequestOptions()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE);
        Glide.with(activity)
            .asBitmap()
            .load(pageFile)
            .apply(options)
            .listener(new RequestListener<Bitmap>() {
              @Override
              public boolean onLoadFailed(@Nullable GlideException e, Object model,
                  Target<Bitmap> target,
                  boolean isFirstResource) {
                mErrorLayout.setVisibility(View.VISIBLE);
                pageFile.delete();
                return false;
              }

              @Override
              public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
                  DataSource dataSource, boolean isFirstResource) {
                Logger.d("load from cache file " + position);
                isPortrait = resource.getHeight() >= resource.getWidth();
                mErrorLayout.setVisibility(View.GONE);
                resPrepared = true;
                setOrientation(false);
                return false;
              }
            })
            .into(mContentView);
      }
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
      Glide.with(convertView.getContext())
          .asBitmap()
          .load(speaker.headimage)
          //.skipMemoryCache(true)
          .apply(options)
          .into(header);
      title.setText(speaker.alias);
      info.setText(speaker.introduce);
      ratingBar.setRating(speaker.starankcount);
      ratingBar.setVisibility(speaker.starankcount == 0 ? View.INVISIBLE : View.VISIBLE);
      String comment = com.namibox.util.Utils.formatCount(getContext(), speaker.commentcount);
      String read = com.namibox.util.Utils.formatCount(getContext(), speaker.readcount);
      comments.setText(getString(R.string.hfx_speaker_info, comment, read));
      return convertView;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (huiben != null && huiben.wxshare != null && huiben.wxshare.doclink != null) {
      menu.add(0, 100, 100, "分享")
          .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
          .setVisible(true);
      return super.onCreateOptionsMenu(menu);
    } else {
      menu.removeGroup(0);
      return super.onCreateOptionsMenu(menu);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == 100) {
      if (huiben.wxshare != null) {
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("url_image", huiben.wxshare.imgurl);
//        jsonObject.addProperty("url_link", huiben.wxshare.doclink);
//        jsonObject.addProperty("share_title", huiben.wxshare.grouptitile);
//        jsonObject.addProperty("share_friend", huiben.wxshare.friendtitile);
//        jsonObject.addProperty("share_content", huiben.wxshare.groupcontent);
//        CommonShareHelper.commonShare(ReadBookActivity.this, "", jsonObject, null);
      }
      return true;
    }
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
