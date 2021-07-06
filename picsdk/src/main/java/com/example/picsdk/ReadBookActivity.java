package com.example.picsdk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.exoaudioplayer.aduio.base.AbstractAudioPlayer;
import com.example.exoaudioplayer.aduio.base.AudioCallBack;
import com.example.exoaudioplayer.aduio.base.AudioPlayerFactory;
import com.example.picsdk.base.BaseActivity;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.model.Book;
import com.example.picsdk.model.ProductItem;
import com.example.picsdk.util.AppPicUtil;
import com.example.picsdk.view.AutoOrientationPicImageView;
import com.google.android.exoplayer2.Player;
import com.namibox.commonlib.view.HackyViewPager;
import com.namibox.util.Logger;
import com.namibox.util.Utils;
import java.io.File;
import java.util.ArrayList;


public class ReadBookActivity extends BaseActivity implements AutoOrientationPicImageView.Callback {

  ImageView back;
  ImageView action;
  LinearLayout action_layout;
  TextView action_text;
  View title_bg;
  TextView title;
  View play;
  CheckedTextView trans;
  ViewPager pager;
  HackyViewPager imagePager;
  TextView pagerIndex;
  private boolean autoFlip = false;
  private Book book;
  private ArrayList<Book.TrackInfo> trackInfos;
  private ArrayList<Book.BookPage> bookPages;
  private ReadBookAdapter adapter;
  private ReadBookPageAdapter imageAdapter;
  private Book.TrackInfo mCurrentTrackInfo;
  private boolean isFinished;
  long milesson_item_id;
  private ArrayList<String> pageNames;
  private int mPlaybackState;
  private Handler handler;
  static final int MSG_AUTO_FLIP = 100;
  float x1;
  float x2;
  private BookManager bookManager;
  private boolean isAudioPause = false;
  private ImageView imageView;
  private ImageView[] imageViews;
  //包裹点点的LinearLayout
  private ViewGroup group;
  private AbstractAudioPlayer exoAudioPlayer;

  private Handler.Callback callback = new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      if (msg.what == MSG_AUTO_FLIP) {
        if (autoFlip && pager.getCurrentItem() < adapter.getCount() - 1) {
          Logger.d("切换下一页: " + (pager.getCurrentItem() + 1));
          mCurrentTrackInfo = null;
          pager.setCurrentItem(pager.getCurrentItem() + 1);
        }
        return true;
      }
      return false;
    }
  };

  @Override
  public void onBackPressed() {
    backPressed();
  }

  void backPressed() {
    finish();
  }

  void actionPressed() {
    autoFlip = !autoFlip;
    action.setImageResource(autoFlip ? R.drawable.icon_auto : R.drawable.icon_hand);
    action_text.setText(autoFlip ? getString(R.string.book_learnmanual_title) : getString(R.string.book_learnauto_title));
    toast(autoFlip ? getString(R.string.book_changeautopage_tips) : getString(R.string.book_changemanualpage_tips));
    if (mPlaybackState == Player.STATE_IDLE || mPlaybackState == Player.STATE_ENDED
        || mCurrentTrackInfo == null || noMp3(mCurrentTrackInfo) || !exoAudioPlayer.getPlayer()
        .getPlayWhenReady()) {
      handler.sendEmptyMessageDelayed(MSG_AUTO_FLIP, 3000);
    }
  }

  void playPressed() {
    if (mCurrentTrackInfo == null) {
      return;
    }
    playTrack(mCurrentTrackInfo, true);
  }

  void transPressed() {
    trans.setChecked(!trans.isChecked());
    if (adapter != null) {
      adapter.refresh();
    }
  }

  public boolean isTransOn() {
    return trans.isChecked();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initExoAudioPlayer();
    Intent intent = getIntent();
    bookManager = BookManager.getInstance();
    ProductItem.Challenge challenge = bookManager.getBookLearning();
    if (challenge == null) {
      toast("数据异常，无法打开绘本");
      finish();
      return;
    }
    milesson_item_id = challenge.milesson_item_id;
    pageNames = intent.getStringArrayListExtra("cartoon");
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    setContentView(R.layout.activity_read_book);
    viewInit();
    File file = AppPicUtil
        .getBookResource(getApplicationContext(), PicLoadingActivity.BOOK_LINKS_READ,
            milesson_item_id);
    book = Utils.parseJsonFile(file, Book.class);
    if (book == null || book.bookaudio == null || book.bookpage == null) {
      toast("请先完成【绘本学习-读绘本】");
      finish();
      return;
    }
    handler = new Handler(callback);
    initView();
    if (bookManager != null && bookManager.getBookLearning() != null) {
      AppPicUtil
          .TagEventEnterPush(true, ((ProductItem.BookLearning) bookManager.getBookLearning()).text,
              "读绘本", bookManager.getProductName());
    }
    initSound();
  }

  private void viewInit() {
    back = findViewById(R.id.back);
    action = findViewById(R.id.action);
    action_layout = findViewById(R.id.action_layout);
    action_text = findViewById(R.id.action_text);
    title_bg = findViewById(R.id.title_bg);
    title = findViewById(R.id.title);
    play = findViewById(R.id.play);
    trans = findViewById(R.id.trans);
    pager = findViewById(R.id.pager);
    imagePager = findViewById(R.id.image_pager);
    pagerIndex = findViewById(R.id.page_index);

    back.setOnClickListener(v -> backPressed());
    action_layout.setOnClickListener(v -> actionPressed());
    play.setOnClickListener(v -> playPressed());
    trans.setOnClickListener(v -> transPressed());
  }

  private void initExoAudioPlayer() {
    exoAudioPlayer = AudioPlayerFactory.getInstance()
        .createPlayer(getApplicationContext(), com.example.exoaudioplayer.aduio.base.Constants.EXO);
    exoAudioPlayer.setPlayerCallBack(new AudioCallBack() {
      @Override
      public void playUpdate(long currentTime, long bufferTime, long totalTime) {
        Logger.d("playUpdate " + currentTime + "/" + totalTime);
        if (mCurrentTrackInfo == null || noMp3(mCurrentTrackInfo)) {
          return;
        }
        int trackEnd = (int) (mCurrentTrackInfo.track_auend * 1000);
        if (trackEnd <= currentTime) {
          Logger.d("停止播放");
          exoAudioPlayer.stop();
          play.setSelected(false);
          play.setEnabled(!noMp3(mCurrentTrackInfo));
          isAudioStop = true;
          if (autoFlip && pager.getCurrentItem() < adapter.getCount() - 1) {
            new Handler().postDelayed(new Runnable() {
              public void run() {
                Logger.d("切换下一页: " + (pager.getCurrentItem() + 1));
                mCurrentTrackInfo = null;
                pager.setCurrentItem(pager.getCurrentItem() + 1);
              }
            }, 3000);
          }
        }
      }

      @Override
      public void playStateChange(boolean playWhenReady, int playbackState) {
        super.playStateChange(playWhenReady, playbackState);
        if (mPlaybackState != playbackState) {
          if (playbackState == Player.STATE_ENDED) {
            Logger.d("播放完毕");
            play.setSelected(false);
            play.setEnabled(!noMp3(mCurrentTrackInfo));
            isAudioStop = true;
            if (autoFlip && pager.getCurrentItem() < adapter.getCount() - 1) {
              new Handler().postDelayed(new Runnable() {
                public void run() {
                  Logger.d("切换下一页: " + (pager.getCurrentItem() + 1));
                  mCurrentTrackInfo = null;
                  pager.setCurrentItem(pager.getCurrentItem() + 1);
                }
              }, 3000);
            }
          }
        }
        mPlaybackState = playbackState;
      }
    });
  }

  private SoundPool soundPool;
  private int changePager;

  @SuppressLint("NewApi")
  private void initSound() {
    soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    changePager = soundPool.load(this, R.raw.change_pager, 1);
  }

  private void playSound(int id) {
    soundPool.play(
        id,
        1,      //左耳道音量【0~1】
        1,      //右耳道音量【0~1】
        2,         //播放优先级【0表示最低优先级】
        0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
        1          //播放速度【1是正常，范围从0~2】
    );
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (bookManager != null && bookManager.getBookLearning() != null) {
      AppPicUtil
          .TagEventEnterPush(false, ((ProductItem.BookLearning) bookManager.getBookLearning()).text,
              "读绘本", bookManager.getProductName());
    }
    if (handler != null) {
      handler.removeCallbacksAndMessages(null);
    }
    if (soundPool != null) {
      soundPool.release();
    }
    if (exoAudioPlayer != null) {
      exoAudioPlayer.releasePlayer();
      exoAudioPlayer.setPlayerCallBack(null);
      exoAudioPlayer = null;
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (!isAudioPause) {
      if (exoAudioPlayer.getPlayer() != null && exoAudioPlayer.getPlayer().getPlayWhenReady()) {
        exoAudioPlayer.setPlayWhenReady(false);
        play.setSelected(false);
        isAudioPause = true;
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (isAudioPause) {
      if (exoAudioPlayer.getPlayer() != null && !exoAudioPlayer.getPlayer().getPlayWhenReady()) {
        exoAudioPlayer.setPlayWhenReady(true);
        play.setSelected(true);
        isAudioPause = false;
      }
    }
  }

  @Override
  public void onImageSize(int w, int h) {
    float ratio = 1f * w / h;
    Logger.d("w=" + w + " h=" + h + " ratio=" + ratio);
    if (ratio >= 1.3f) {
      setOrientation(false);
    } else {
      setOrientation(true);
    }
  }

  private void initView() {
    title.setText(book.bookaudio.bookname);
    action_text.setText(autoFlip ? getString(R.string.book_learnmanual_title) : getString(R.string.book_learnauto_title));

    trackInfos = new ArrayList<>();
    bookPages = new ArrayList<>();
    int track_id = 0;
    for (int i = 0; i < book.bookpage.size(); i++) {
      Book.BookPage bookPage = book.bookpage.get(i);
      if (pageNames != null && !pageNames.contains(bookPage.page_name)) {
        action_layout.setVisibility(View.GONE);
        continue;
      }
      if (pageNames != null && pageNames.contains(bookPage.page_name)) {
        bookPages.add(bookPage);
      }
      if (bookPage.track_info == null) {
        bookPage.track_info = new ArrayList<>();
      }
      if (bookPage.track_info.isEmpty()) {
        Book.TrackInfo empty = new Book.TrackInfo();
        bookPage.track_info.add(empty);
      }
      for (Book.TrackInfo trackInfo : bookPage.track_info) {
        trackInfo.page_index = i;
        trackInfo.track_id = track_id++;
      }
      trackInfos.addAll(bookPage.track_info);
    }

    if (trackInfos.size() == 0) {
      finish();
      toast("数据错误");
      return;
    }
    imageAdapter = new ReadBookPageAdapter(this, book.bookpage);
    imagePager.setLocked(true);
    imagePager.setAdapter(imageAdapter);
    imagePager.addOnPageChangeListener(new OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        if (!isFinished && position < imageAdapter.getCount() - 2 && positionOffset > 0.2) {
//          selectedTrack(position);
//        }
//        if (!isFinished && position == imageAdapter.getCount() - 2 && positionOffset > 0.2) {
//          isFinished = true;
//          Intent intent = new Intent(ReadBookActivity.this, ResultActivity.class);
////          intent.putExtra("title", book_title);
//          intent.putExtra("milesson_item_id", milesson_item_id);
//          intent.putExtra(ResultActivity.ARG_LINK, LoadingActivity.BOOK_LINKS_READ);
//          startActivity(intent);
//          finish();
//        }
      }

      @Override
      public void onPageSelected(int position) {
        if (position < book.bookpage.size()) {
          selectPage(position);
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });
    imagePager.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
          case MotionEvent.ACTION_DOWN:
            x1 = motionEvent.getX();
            break;
          case MotionEvent.ACTION_UP:
            x2 = motionEvent.getX();
            //left
            if (x1 - x2 > 50) {
              if (!isFinished && mCurrentTrackInfo.page_index == imageAdapter.getCount() - 2
                  && pageNames == null && pager.getCurrentItem() + 1 == trackInfos.size()) {
                isFinished = true;
                Intent intent = new Intent(ReadBookActivity.this, ResultActivity.class);
//          intent.putExtra("title", book_title);
                intent.putExtra("milesson_item_id", milesson_item_id);
                intent.putExtra(ResultActivity.ARG_LINK, PicLoadingActivity.BOOK_LINKS_READ);
                startActivity(intent);
                finish();
              } else {
                int textPosition = pager.getCurrentItem() + 1;
                if (trackInfos.size() > 1 && textPosition < trackInfos.size()) {
                  if (trackInfos.get(textPosition).page_index == mCurrentTrackInfo.page_index) {
                    pager.setCurrentItem(textPosition);
                  } else {
                    selectedTrack(textPosition);
                  }
                }
              }
            } else if (x2 - x1 > 50) {
              int textPosition = pager.getCurrentItem() - 1;
              if (textPosition >= 0 && textPosition < trackInfos.size()) {
                if (trackInfos.get(textPosition).page_index == mCurrentTrackInfo.page_index) {
                  pager.setCurrentItem(textPosition);
                } else {
                  selectedTrack(textPosition);
                }
              }
            }
            break;
        }
        return true;
      }
    });
    adapter = new ReadBookAdapter(this, trackInfos, pageNames == null);
    pager.setAdapter(adapter);
    pager.addOnPageChangeListener(new OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (!isFinished && pageNames == null && position == adapter.getCount() - 2
            && positionOffset > 0.2) {
          isFinished = true;
          Intent intent = new Intent(ReadBookActivity.this, ResultActivity.class);
          intent.putExtra("milesson_item_id", milesson_item_id);
          intent.putExtra(ResultActivity.ARG_LINK, PicLoadingActivity.BOOK_LINKS_READ);
          startActivity(intent);
          finish();
        }
      }

      @Override
      public void onPageSelected(int position) {
        selectedTrack(position);
        playSound(changePager);
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });

    group = findViewById(R.id.viewGroup);
    selectedTrack(0);
    if (pageNames == null) {
      initViewPagerTab(0);
    } else {
      initViewPagerTab(bookPages.get(0).track_info.get(0).page_index);
    }
  }

  //pageView监听器
  class GuidePageChangeListener implements OnPageChangeListener {

    @Override
    public void onPageScrollStateChanged(int arg0) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
      // TODO Auto-generated method stub

    }

    @Override
    //如果切换了，就把当前的点点设置为选中背景，其他设置未选中背景
    public void onPageSelected(int arg0) {
      if (arg0 < trackInfos.size()) {
        initViewPagerTab(trackInfos.get(arg0).page_index);
        int position = 0;
        if (pageNames == null) {
          for (int i = 0; i < trackInfos.get(arg0).page_index; i++) {
            position += book.bookpage.get(i).track_info.size();
          }
        } else {
          for (int i = 0; i < bookPages.size(); i++) {
            if (trackInfos.get(arg0).page_index > bookPages.get(i).track_info.get(0).page_index) {
              position += bookPages.get(i).track_info.size();
            }
          }
        }
        int realPosition = arg0 - position;
        if (realPosition < imageViews.length && realPosition >= 0) {
          for (int i = 0; i < imageViews.length; i++) {
            imageViews[realPosition].setBackgroundResource(R.drawable.page_indicator_focused);
            if (realPosition != i) {
              imageViews[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }
          }
        }
      }
    }
  }

  private void initViewPagerTab(int position) {
    group.removeAllViews();
    imageViews = new ImageView[book.bookpage.get(position).track_info.size()];
    for (int i = 0; i < book.bookpage.get(position).track_info.size(); i++) {
      imageView = new ImageView(ReadBookActivity.this);
      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      lp.setMarginEnd(Utils.dp2px(this, 10));
      imageView.setLayoutParams(lp);
      imageViews[i] = imageView;

      //默认第一张图显示为选中状态
      if (i == 0) {
        imageViews[i].setBackgroundResource(R.drawable.page_indicator_focused);
      } else {
        imageViews[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
      }

      group.addView(imageViews[i]);
    }
    if (book.bookpage.get(position).track_info.size() > 1) {
      group.setVisibility(View.VISIBLE);
    } else {
      group.setVisibility(View.GONE);
    }
    pager.setOnPageChangeListener(new GuidePageChangeListener());

  }

  private void selectPage(int position) {
    pagerIndex.setText(Utils.format("%d/%d", position + 1, book.bookpage.size()));
    ArrayList<Book.TrackInfo> trackInfoArrayList = book.bookpage.get(position).track_info;
    if (trackInfoArrayList != null && !trackInfoArrayList.isEmpty()) {
      Book.TrackInfo trackInfo = trackInfoArrayList.get(0);
      int index = trackInfos.indexOf(trackInfo);
      pager.setCurrentItem(index);
    }
  }

  private void selectedTrack(int position) {
    mCurrentTrackInfo = adapter.getTrackInfo(position);
//    handler.removeMessages(MSG_AUTO_FLIP);
    if (mCurrentTrackInfo == null) {
      return;
    }
    if (noMp3(mCurrentTrackInfo)) {
      handler.sendEmptyMessageDelayed(MSG_AUTO_FLIP, 3000);
    }
    pagerIndex
        .setText(Utils.format("%d/%d", mCurrentTrackInfo.page_index + 1, book.bookpage.size()));
    imagePager.setCurrentItem(mCurrentTrackInfo.page_index);
    play.setEnabled(!noMp3(mCurrentTrackInfo));
    playTrack(mCurrentTrackInfo, false);

  }

  private void playTrack(Book.TrackInfo trackInfo, boolean isClick) {
    if (noMp3(trackInfo)) {
      exoAudioPlayer.stop();
      play.setSelected(false);
      return;
    }

    File audioFile = AppPicUtil
        .getBookResource(getApplicationContext(), trackInfo.mp3url, milesson_item_id);
    File hqAudioFile = AppPicUtil
        .getBookResource(getApplicationContext(), trackInfo.mp3url_hiq, milesson_item_id);
    Uri uri = hqAudioFile.exists() ? Uri.fromFile(hqAudioFile) : Uri.fromFile(audioFile);

    if (!isClick) {
      exoAudioPlayer.play(uri, (long) (trackInfo.track_austart * 1000));
      play.setSelected(true);
    } else {
      if (isAudioStop) {
        exoAudioPlayer.play(uri, (long) (trackInfo.track_austart * 1000));
        play.setSelected(true);
        isAudioStop = false;
        return;
      }
      if (exoAudioPlayer.getPlayer() != null && exoAudioPlayer.getPlayer().getPlayWhenReady()) {
        exoAudioPlayer.setPlayWhenReady(false);
        play.setSelected(false);
      } else {
        exoAudioPlayer.setPlayWhenReady(true);
        play.setSelected(true);
      }
    }
  }

  private boolean isAudioStop = false;

  private boolean noMp3(Book.TrackInfo trackInfo) {
    return trackInfo.mp3url == null && trackInfo.mp3url_hiq == null;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
      back.setElevation(0);
      action_layout.setElevation(0);
      ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) back.getLayoutParams();
      lp.leftMargin = 0;
      lp.topMargin = 0;
      back.setLayoutParams(lp);
      ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) action_layout
          .getLayoutParams();
      lp2.rightMargin = 0;
      lp2.topMargin = 0;
      action_layout.setLayoutParams(lp2);
      title_bg.setVisibility(View.VISIBLE);
      title.setVisibility(View.VISIBLE);
      ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams) pager.getLayoutParams();
      lp3.dimensionRatio = "375:144";
      action_layout.setLayoutParams(lp2);
      if (adapter != null) {
        adapter.refresh();
      }
    } else {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
      back.setElevation(Utils.dp2px(this, 3));
      action_layout.setElevation(Utils.dp2px(this, 3));
      ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) back.getLayoutParams();
      lp.leftMargin = Utils.dp2px(this, 15);
      lp.topMargin = Utils.dp2px(this, 25);
      back.setLayoutParams(lp);
      ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) action_layout
          .getLayoutParams();
      lp2.rightMargin = Utils.dp2px(this, 15);
      lp2.topMargin = Utils.dp2px(this, 25);
      action_layout.setLayoutParams(lp2);
      title_bg.setVisibility(View.GONE);
      title.setVisibility(View.GONE);
      ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams) pager.getLayoutParams();
      lp3.dimensionRatio = "667:97";
      action_layout.setLayoutParams(lp2);
      if (adapter != null) {
        adapter.refresh();
      }
    }
  }
}
