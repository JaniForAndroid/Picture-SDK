package com.namibox.hfx.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.exoaudioplayer.aduio.base.AbstractAudioPlayer;
import com.example.exoaudioplayer.aduio.base.AudioCallBack;
import com.example.exoaudioplayer.aduio.base.AudioPlayerFactory;
import com.example.exoaudioplayer.aduio.base.Constants;
import com.example.picsdk.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.namibox.commonlib.activity.AbsFunctionActivity;
import com.namibox.commonlib.view.CircleImageView;
import com.namibox.hfx.bean.CommitInfo;
import com.namibox.hfx.bean.FreeAudio;
import com.namibox.hfx.bean.MatchInfo;
import com.namibox.hfx.utils.HfxFileUtil;
import com.namibox.hfx.utils.HfxUtil;
import com.namibox.util.FileUtil;
import com.namibox.util.ImageUtil;
import com.namibox.util.NetworkUtil;
import com.namibox.util.WeakAsyncTask;
import java.io.File;
import okhttp3.CacheControl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sunha on 2015/10/29 0029.
 */
public class HfxPlayAudioActivity extends AbsFunctionActivity implements View.OnClickListener {

  public static final String ARG_JSON_URL = "json_url";
  public static final String AUDIO_TYPE = "audio_type";
  public static final String TAG = "AbsPlayActivity";
  private static final String NET_AUDIO = "net_audio";
  private static final String LOCAL_AUDIO = "local_audio";
  private static final String AUDIO_ID = "audio_id";
  ImageView bgImg;
  TextView toolbarTitle;
  ImageView backView;
  TextView menuText1;
  FrameLayout menu1;
  ImageView menuImg1;
  private View statusBar;
  CircleImageView circleImg;
  ImageButton simplePause;
  TextView audioCurrent;
  SeekBar mSeekBar;
  TextView audioDuration;
  LinearLayout progress;


  protected FreeAudio freeAudio;
  private String jsonUrl;
  private long tempTime;
  //private boolean urlHasInit = false;
  private String audioType;
  private String audioId;
  private File audioFile;
  private File corerFile;
  private int playState;
  private AbstractAudioPlayer exoAudioPlayer;


  private <T extends View> T find(int resId) {
    return (T) findViewById(resId);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      getWindow().setStatusBarColor(0x00000000);
    }
    setContentView(R.layout.hfx_play_audio_activity);
    findView();
    LinearLayout.LayoutParams statusBarLp = (LinearLayout.LayoutParams) statusBar
        .getLayoutParams();
    statusBarLp.height = com.namibox.util.Utils.getStatusBarHeight(this);
    toolbarTitle.setTextColor(0xffffffff);
    backView.setImageResource(R.drawable.ic_arrow_back_white);
    backView.setOnClickListener(v -> finish());
    setDarkStatusIcon(false);

    Intent intent = getIntent();
    jsonUrl = intent.getStringExtra(ARG_JSON_URL);
    audioType = intent.getStringExtra(AUDIO_TYPE);
//        jsonUrl = "http://w.namibox.com/api/app/free_work?cid=000245";
    initExoAudioPlayer();
    if (NET_AUDIO.equals(audioType)) {
      loadJsonData(jsonUrl);
    } else if (LOCAL_AUDIO.equals(audioType)) {
      audioId = intent.getStringExtra(AUDIO_ID);
      audioFile = HfxFileUtil.getStoryAudioFile(this, audioId);
      corerFile = HfxFileUtil.getCoverFile(this, audioId);
      simplePause.setImageResource(R.drawable.hfx_ic_audio_play);
      RequestOptions options = new RequestOptions()
          .skipMemoryCache(true)
          //.placeholder(R.drawable.default_cover)
          .error(R.drawable.hfx_default_cover);
      Glide.with(this)
          .asBitmap()
          .load(corerFile)
          .apply(options)
          .into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
              circleImg.setImageBitmap(resource);
              Bitmap bitmap = ImageUtil.doBlur(resource, 4, 4f, 0x80333333);
              bgImg.setImageBitmap(bitmap);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
              circleImg.setImageDrawable(errorDrawable);
              if (errorDrawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) errorDrawable;
                if (bitmapDrawable.getBitmap() != null) {
                  Bitmap bitmap = ImageUtil.doBlur(bitmapDrawable.getBitmap(), 4, 4f, 0x80333333);
                  bgImg.setImageBitmap(bitmap);
                }
              }
            }
          });
      CommitInfo info = HfxUtil.getCommitInfo(this, audioId);
      if (null != info && !TextUtils.isEmpty(info.bookname)) {
        toolbarTitle.setText(info.bookname);
      } else {
        toolbarTitle.setText("故事秀预览");
      }
      playAudio(true);
      mSeekBar.setMax(1000);
      mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
      setMenu("上传作品", v -> {
        MatchInfo matchInfo = HfxUtil.getMatchInfo(HfxPlayAudioActivity.this, audioId);
        if (matchInfo != null && !TextUtils.isEmpty(matchInfo.realUrl)) {
          openView(matchInfo.realUrl);
        } else {
          Intent intent1 = new Intent(HfxPlayAudioActivity.this, SaveAudioActivity.class);
          intent1.putExtra(SaveAudioActivity.AUDIO_ID, audioId);
          startActivity(intent1);
        }
        finish();
      });
    }
  }

  private void initExoAudioPlayer() {
    exoAudioPlayer = AudioPlayerFactory
        .getInstance().createPlayer(getApplicationContext(), Constants.EXO);
    exoAudioPlayer.setPlayerCallBack(new AudioCallBack() {
      @Override
      public void playUpdate(long currentTime, long bufferTime, long totalTime) {
        int progress = totalTime <= 0 ? 0 : (int) (1000 * currentTime / totalTime);
        int secprogress = totalTime <= 0 ? 0 : (int) (1000 * bufferTime / totalTime);
        mSeekBar.setSecondaryProgress(secprogress);
        audioCurrent.setText(com.namibox.util.Utils.makeTimeString((int) currentTime));
        audioDuration.setText(com.namibox.util.Utils.makeTimeString((int) totalTime));
        if (!isTracking) {
          mSeekBar.setProgress(progress);
        }

      }

      @Override
      public void playStateChange(boolean playWhenReady, int playbackState) {
        playState = playbackState;
        if (playbackState == ExoPlayer.STATE_READY && playWhenReady) {
          simplePause.setImageResource(R.drawable.hfx_ic_audio_pause);
          playAnim();
        } else {
          simplePause.setImageResource(R.drawable.hfx_ic_audio_play);
          pauseAnim();
        }
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (exoAudioPlayer != null) {
      exoAudioPlayer.releasePlayer();
      exoAudioPlayer.setPlayerCallBack(null);
      exoAudioPlayer = null;
    }
  }

  public void setMenu(String title, View.OnClickListener listener) {
    menu1.setVisibility(View.VISIBLE);
    menuImg1.setVisibility(View.GONE);
    menuText1.setText(title);
    menuText1.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
    menu1.setOnClickListener(listener);
    int[] attrs = new int[]{R.attr.selectableItemBackground};
    TypedArray typedArray = obtainStyledAttributes(attrs);
    int backgroundResource = typedArray.getResourceId(0, 0);
    typedArray.recycle();
    menuText1.setBackgroundResource(backgroundResource);

  }

  protected void onNewIntent(Intent intent) {
    Log.e(TAG, "onNewIntent");
    super.onNewIntent(intent);

    setIntent(intent);//must store the new intent unless getIntent() will return the old one
    jsonUrl = intent.getStringExtra(ARG_JSON_URL);
    loadJsonData(jsonUrl);
  }

  private void findView() {
    bgImg = find(R.id.bgImg);
    toolbarTitle = find(R.id.title);
    backView = find(R.id.back);
    statusBar = find(R.id.status_bar_layout);
    circleImg = find(R.id.circleImg);
    simplePause = find(R.id.simplePause);
    audioCurrent = find(R.id.audio_current);
    mSeekBar = find(R.id.mSeekBar);
    audioDuration = find(R.id.audio_duration);
    progress = find(R.id.progress);
    simplePause.setOnClickListener(this);

    menuText1 = findViewById(R.id.menu_text1);
    menu1 = findViewById(R.id.menu1);
    menuImg1 = findViewById(R.id.menu_img1);

  }

  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.simplePause) {
      long currentTime = System.currentTimeMillis();
      long delayTime = currentTime - tempTime;
      if (delayTime < 400) {
        toast("请勿频繁操作");
        return;
      }
      tempTime = currentTime;
      if (playState == ExoPlayer.STATE_READY) {
        exoAudioPlayer.playPause();
      } else if (playState == ExoPlayer.STATE_ENDED || playState == ExoPlayer.STATE_IDLE) {
        playAudio(true);
      }
    }


  }


  private boolean isTracking;
  private long mLastSeekEventTime;
  private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
      isTracking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
      isTracking = false;
      long now = SystemClock.elapsedRealtime();
      if (now - mLastSeekEventTime > 100) {
        mLastSeekEventTime = now;
        long p = exoAudioPlayer.getPlayer().getDuration() * seekBar.getProgress() / 1000;
        exoAudioPlayer.seekTo(p);
      }
    }
  };

  private void onLoadDone(FreeAudio audio) {
    hideProgress();
    if (audio == null || TextUtils.isEmpty(audio.userwork)) {
      toast("无法打开");
      finish();
    } else {
      freeAudio = audio;
      simplePause.setImageResource(R.drawable.hfx_ic_audio_play);
      loadImg();
      toolbarTitle.setText(freeAudio.workname);
      playAudio(freeAudio.autoplay);
      mSeekBar.setMax(1000);
      mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);


    }
  }

  private void playAudio(boolean autoPlay) {
    //urlHasInit = true;
    if (exoAudioPlayer == null) {
      return;
    }
    if (NET_AUDIO.equals(audioType)) {
      exoAudioPlayer.play(Uri.parse(freeAudio.userwork), autoPlay);
    } else if (LOCAL_AUDIO.equals(audioType)) {
      exoAudioPlayer.play(Uri.fromFile(audioFile), autoPlay);
    }

  }

  private void loadImg() {
    if (!TextUtils.isEmpty(freeAudio.icon)) {
      RequestOptions options = new RequestOptions()
          .skipMemoryCache(true)
          //.placeholder(R.drawable.img_tu)
          .error(R.drawable.hfx_default_cover);
      Glide.with(this)
          .asBitmap()
          .load(com.namibox.util.Utils.encodeString(freeAudio.icon))
          .apply(options)
          .into(new SimpleTarget<Bitmap>() {
            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
              circleImg.setImageDrawable(errorDrawable);
              if (errorDrawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) errorDrawable;
                if (bitmapDrawable.getBitmap() != null) {
                  Bitmap bitmap = ImageUtil.doBlur(bitmapDrawable.getBitmap(), 4, 4f, 0x80333333);
                  bgImg.setImageBitmap(bitmap);
                }
              }
            }

            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
              circleImg.setImageBitmap(resource);
              Bitmap bitmap = ImageUtil.doBlur(resource, 4, 4f, 0x80333333);
              bgImg.setImageBitmap(bitmap);
            }

          });
    }
  }


  private void loadJsonData(String url) {
    showProgress("正在加载...");
    new LoadTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
  }


  private static class LoadTask extends
      WeakAsyncTask<String, Void, FreeAudio, HfxPlayAudioActivity> {

    public LoadTask(HfxPlayAudioActivity playerActivity) {
      super(playerActivity);
    }

    @Override
    protected FreeAudio doInBackground(HfxPlayAudioActivity playerActivity, String... params) {
      Context context = playerActivity.getApplicationContext();
      String url = com.namibox.util.Utils.encodeString(params[0]);
      final File jsonFile = FileUtil.getCachedFile(context, url);
      Request request = new Request.Builder()
          .cacheControl(CacheControl.FORCE_NETWORK)
          .url(url)
          .build();
      if (NetworkUtil.isNetworkAvailable(context)) {
        try {
          Response response = playerActivity.getOkHttpClient().newCall(request).execute();
          Log.e("response", response + "");
          if (response != null && response.isSuccessful()) {
            String body = response.body().string();
            FreeAudio freeAudio = com.namibox.util.Utils.parseJsonString(body, FreeAudio.class);
            if (freeAudio != null) {
              FileUtil.StringToFile(body, jsonFile, "utf-8");
              return freeAudio;
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (jsonFile.exists()) {
        return com.namibox.util.Utils.parseJsonFile(jsonFile, FreeAudio.class);
      }
      return null;
    }


    @Override
    protected void onPostExecute(HfxPlayAudioActivity playerActivity, FreeAudio data) {
      if (playerActivity != null && !playerActivity.isFinishing()) {
        playerActivity.onLoadDone(data);
      }
    }
  }


  /**
   * 播放
   */
  public void playAnim() {
    circleImg.startRotate();
  }

  /**
   * 暂停
   */
  public void pauseAnim() {
    circleImg.stopRotate();
  }
}
