package com.example.picsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.exoaudioplayer.video.base.Constants;
import com.example.exoaudioplayer.video.base.VideoView;
import com.example.exoaudioplayer.video.component.PlayerControlView;
import com.example.exoaudioplayer.video.component.StandardMediaController;
import com.example.exoaudioplayer.video.fragment.VideoFragment;
import com.example.exoaudioplayer.video.model.MediaBuilder;
import com.example.picsdk.base.BaseActivity;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.model.ProductItem;
import com.example.picsdk.model.VideoPicInfo;
import com.example.picsdk.util.AppPicUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.List;

//@Route(path = "/namiboxpic/video")
public class VideoActivity extends BaseActivity implements PicVideoAdapter.OnItemClickListener {

  private ConstraintLayout tool_bar;
  private TextView tv_title;
  private TextView tv_toast, tv_skip;

  private File mFile;
  private String videoUrl;
  private String title;
  private BookManager bookManager;
  private VideoFragment mVideoFragment;
  private ConstraintLayout sectionLayout;
  private RecyclerView recyclerView;
  private int seektime;
  private boolean isHideSkip = false;
  private boolean isShowBottom = false;
  private PicVideoAdapter adapter;
  public List<VideoPicInfo> lists;
  private String mCurrentUrl;
  private boolean isShare = false;
  private ImageView iv_share;

  private String doclink;
  private String friendtitile;
  private String groupcontent;
  private String grouptitile;
  private String imgurl;

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (bookManager != null && bookManager.getBookLearning() != null) {
      AppPicUtil
          .TagEventEnterPush(false, ((ProductItem.BookLearning) bookManager.getBookLearning()).text,
              "看动画", bookManager.getProductName());
    }
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Window window = getWindow();
    window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_video_pic);
    initView();
    initData();

    if (bookManager != null && bookManager.getBookLearning() != null) {
      AppPicUtil
          .TagEventEnterPush(true, ((ProductItem.BookLearning) bookManager.getBookLearning()).text,
              "看动画", bookManager.getProductName());
    }
  }

  private void initData() {
    Intent intent = getIntent();
    videoUrl = intent.getStringExtra("video");
    title = intent.getStringExtra("title");
    isHideSkip = intent.getBooleanExtra("isHideSkip", false);
    isShare = intent.getBooleanExtra("isShare", false);

    doclink = intent.getStringExtra("doclink");
    friendtitile = intent.getStringExtra("friendtitile");
    groupcontent = intent.getStringExtra("groupcontent");
    grouptitile = intent.getStringExtra("grouptitile");
    imgurl = intent.getStringExtra("imgurl");

    if (isShare) {
      iv_share.setVisibility(View.VISIBLE);
      tv_skip.setVisibility(View.GONE);
    } else {
      iv_share.setVisibility(View.GONE);
      tv_skip.setVisibility(View.VISIBLE);
    }

    tv_title.setText(title);
    if (isHideSkip) {
      tv_skip.setVisibility(View.GONE);
    }

    if (videoUrl != null && !videoUrl.isEmpty()) {
      initVideo(videoUrl);
      return;
    }

    bookManager = BookManager.getInstance();
    ProductItem.BookLearning bookLearning = (ProductItem.BookLearning) bookManager
        .getBookLearning();
    if (bookLearning == null) {
      toast("数据异常，无法观看动画");
      finish();
      return;
    }
    if (!TextUtils.isEmpty(bookLearning.text)) {
      tv_title.setText(bookLearning.text);
    }

    if (bookManager != null && bookManager.getVideoPicInfos() != null
        && bookManager.getVideoPicInfos().size() != 0) {
      initVideo(bookManager.getVideoPicInfos().get(0).video_url);
      if (bookManager != null && bookManager.getVideoPicInfos().size() != 0) {
        lists = bookManager.getVideoPicInfos();
        lists.get(0).isPlay = true;
        adapter.setItems(lists);
      }
      return;
    }

    Disposable disposable = Observable.just(bookLearning.milesson_item_id)
        .map(item_id -> AppPicUtil
            .getBookResource(getApplicationContext(), PicLoadingActivity.BOOK_LINKS_VIDEO, item_id))
        .map(file -> {
          mFile = file;
          String json = FileUtil.FileToString(mFile, "utf-8");
          JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
          return jsonObject.get("video_url").getAsString();
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::initVideo, throwable -> {
          Logger.e(throwable, throwable.toString());
          toast("资源准备异常");
        });
    compositeDisposable.add(disposable);
  }

  private void initView() {
    tool_bar = findViewById(R.id.tool_bar);
    ImageView iv_back = findViewById(R.id.iv_back);
    tv_title = findViewById(R.id.tv_title);
    tv_skip = findViewById(R.id.tv_skip_pic);
    tv_toast = findViewById(R.id.tv_toast);
    recyclerView = findViewById(R.id.recyclerView);
    sectionLayout = findViewById(R.id.sectionLayout);
    iv_back.setOnClickListener(v -> onBackPressed());
    tv_skip.setOnClickListener(v -> skip());
    sectionLayout.setOnClickListener(view -> {
      if (isShowBottom) {
        showBottom();
      }
    });
    recyclerView.setFocusableInTouchMode(false);
    adapter = new PicVideoAdapter(this);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    recyclerView.setLayoutManager(linearLayoutManager);
    adapter.setOnItemClickListener(this);
    recyclerView.setAdapter(adapter);

    iv_share = findViewById(R.id.iv_share);
    iv_share.setOnClickListener(v -> share());
  }

  private void share() {
//    JsonObject jsonObject = new JsonObject();
//    jsonObject.addProperty("url_image", imgurl);
//    jsonObject.addProperty("url_link", videoUrl);
//    jsonObject.addProperty("share_title", grouptitile);
//    jsonObject.addProperty("share_friend", friendtitile);
//    jsonObject.addProperty("share_content", groupcontent);
//
//    CommonShareHelper.commonShare(this, "", jsonObject, new ShareCallback() {
//      @Override
//      public void onResult(boolean isSuccess, String msg) {
//      }
//    });
  }

  private void initVideo(String url) {
    mCurrentUrl = url;
    Fragment f = getSupportFragmentManager().findFragmentByTag("video_fragment");
    if (f != null) {
      mVideoFragment = (VideoFragment) f;
      if (url.equals(mVideoFragment.getVideoView().getUri())) {
        mVideoFragment.getVideoView().seekTo(seektime * 1000);
        return;
      } else {
        getSupportFragmentManager().beginTransaction()
            .remove(f).commit();
      }
    }

    seektime = 0;

    MediaBuilder mediaBuilder = new MediaBuilder.Builder()
        .setType(Constants.PIC_FRAGMENT)
        .setUri(url)
        .setAutoPlay(false)
        .setSeekTime(seektime)
        .setCanSave(false)
        .setNotifyUrl(null)
        .build();
    mVideoFragment = new VideoFragment(mediaBuilder);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.video_frame, mVideoFragment, "video_fragment")
        .commit();

    mVideoFragment.getFragmentManager().registerFragmentLifecycleCallbacks(
        new android.support.v4.app.FragmentManager.FragmentLifecycleCallbacks() {
          @Override
          public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v,
              Bundle savedInstanceState) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState);
            //        playerControlView = mVideoFragment.getPlayerControlView();
            mVideoFragment.getController().setOnControlVisibleCallBack(
                new StandardMediaController.OnControlVisibleCallBack() {
                  @Override
                  public void onVisible(boolean visible) {
                    if (visible) {
                      tool_bar.setVisibility(View.VISIBLE);
                    } else {
                      tool_bar.setVisibility(View.GONE);
                    }
                  }
                });

            if (mVideoFragment.getVideoView() != null) {
              mVideoFragment.getVideoView()
                  .setOnStateChangeListener(new VideoView.OnStateChangeListener() {
                    @Override
                    public void onPlayerStateChanged(int playerState) {
                    }

                    @Override
                    public void onPlayStateChanged(int playState) {
                      switch (playState) {
                        case Constants.STATE_PLAYBACK_COMPLETED:
                          if (isHideSkip) {

                          } else {
                            if (bookManager != null && bookManager.getVideoPicInfos() != null
                                && bookManager.getVideoPicInfos().size() > 1) {
                              int currentPosition = 0;
                              for (int i = 0; i < lists.size(); i++) {
                                if (lists.get(i).video_url.equals(mCurrentUrl)) {
                                  currentPosition = i;
                                }
                              }

                              if (currentPosition + 1 < lists.size()) {
                                initVideo(lists.get(currentPosition + 1).video_url);
                                for (VideoPicInfo video : lists) {
                                  if (video.video_url
                                      .equals(lists.get(currentPosition + 1).video_url)) {
                                    video.isPlay = true;
                                  } else {
                                    video.isPlay = false;
                                  }
                                }
                                adapter.notifyDataSetChanged();
                              } else {
                                skip();
                              }
                              return;
                            }
                            skip();
                          }
                          break;
                        case Constants.STATE_ERROR:
                          break;
                      }
                    }
                  });
            }

            if (mVideoFragment.getPlayerControlView() != null && bookManager != null
                && bookManager.getVideoPicInfos() != null
                && bookManager.getVideoPicInfos().size() > 0) {
              mVideoFragment.getPlayerControlView()
                  .setOnChooseLessonCallBack(new PlayerControlView.OnChooseLessonCallBack() {
                    @Override
                    public void onChoose() {
                      showBottom();
                      mVideoFragment.getController().hide();
                    }
                  });
            }
          }
        }, false);
  }

  public void showBottom() {
    isShowBottom = !isShowBottom;
    if (isShowBottom) {
      sectionLayout.setVisibility(View.VISIBLE);
    } else {
      sectionLayout.setVisibility(View.GONE);
    }
  }

  private void skip() {
    Intent intent = new Intent(this, ResultActivity.class);
    intent.putExtra(ResultActivity.ARG_LINK, PicLoadingActivity.BOOK_LINKS_VIDEO);
    startActivity(intent);
    finish();
  }

  @Override
  public void onItemClick(int position) {
    if (mCurrentUrl.equals(lists.get(position).video_url)) {
      showBottom();
      return;
    }
    showBottom();
    initVideo(lists.get(position).video_url);
    for (VideoPicInfo video : lists) {
      if (video.video_url.equals(lists.get(position).video_url)) {
        video.isPlay = true;
      } else {
        video.isPlay = false;
      }
    }
    adapter.notifyDataSetChanged();
  }
}
