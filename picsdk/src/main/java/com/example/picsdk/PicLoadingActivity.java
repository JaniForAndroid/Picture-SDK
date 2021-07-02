package com.example.picsdk;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.picsdk.base.BaseActivity;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.model.Book;
import com.example.picsdk.model.Book.BookPage;
import com.example.picsdk.model.Book.TrackInfo;
import com.example.picsdk.model.VideoPicInfo;
import com.example.picsdk.model.WordExercise;
import com.example.picsdk.model.WordExercise.Question;
import com.example.picsdk.music.MusicPicActivity;
import com.example.picsdk.util.AppPicUtil;
import com.example.picsdk.view.CommonDialog;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.view.RollImageView;
import com.namibox.greendao.entity.AudioInfo;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.PreferenceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pl.droidsonroids.gif.GifDrawable;

import com.example.picsdk.model.ProductItem.BookLearning;
import com.example.picsdk.model.ProductItem.BookLearning.Link;
import com.example.picsdk.model.ProductItem.Challenge;

/**
 * author : feng
 * description ：绘本加载页
 * creation time : 19-9-10下午4:50
 */
public class PicLoadingActivity extends BaseActivity implements OnClickListener {

  private static final String TAG = "LoadingActivity";

  public static final String BOOK_LINKS_WORD = "word";
  public static final String BOOK_LINKS_WORD_RATING = "word_rating";
  public static final String BOOK_LINKS_READ = "reading";
  public static final String BOOK_LINKS_VIDEO = "video";
  public static final String BOOK_LINKS_MUSIC = "audio";

  private RollImageView roll_image;
  private ImageView iv_loading;
  private ProgressBar progressBar;
  private TextView tv_loading_tip;
  private ImageView[] imageViews;
  private TextView[] textViews;
  private Group gp_progress;
  private Group gp_continue;
  private Group gp_completed;
  private Group gp_music;

  private BookManager bookManager;
  private long milesson_item_id;
  private List<BookLearning.Link> links;
  private List<AudioInfo> audioInfos;
  private List<VideoPicInfo> videoPicInfos;
  private List<String> tips;

  private int downloadCount;
  private int completedCount;
  private boolean loading = true;
  private SoundPool soundPool;
  private int clickID;
  private CommonDialog netWorkDialog;
  private boolean isShowDialog = true;
  private long startTime = 0L;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_loading_pic);
    isShowDialog = getIntent().getBooleanExtra("isShowWifiDialog", true);
    bookManager = BookManager.getInstance();

    startTime = getIntent().getLongExtra("startTime", 0L);

    AppPicUtil.setStartTime(startTime);

    initView();
    initData();
    initSound();

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    registerReceiver(connectionReceiver, intentFilter);
  }

  BroadcastReceiver connectionReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
      NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
      NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

      if (wifiNetInfo != null && !wifiNetInfo.isConnected() && mobNetInfo != null && mobNetInfo.isConnected() && loading && isShowDialog) {
        netWorkDialog = new CommonDialog(PicLoadingActivity.this);
        netWorkDialog.setMessage("当前网络无Wi-Fi，继续使用可能会产生相关流量费用，确认使用流量下载？")
//                .setImageResId(R.mipmap.ic_launcher)
            .setTitle("网络提醒")
            .setPositive("确认")
            .setNegtive("取消")
//                .setSingle(true)
            .setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
              @Override
              public void onPositiveClick() {
                netWorkDialog.dismiss();
                initData();
              }

              @Override
              public void onNegtiveClick() {
                netWorkDialog.dismiss();
                finish();
              }

              @Override
              public void onMessagelick() {
              }
            }).show();
      } else if (wifiNetInfo.isConnected()) {
        if (netWorkDialog != null) {
          netWorkDialog.dismiss();
          toast("已连接WiFi网络");
        }
      }
      isShowDialog = true;
    }
  };

  private void initView() {
    initActionBar();
    disableBack();
    roll_image = findViewById(R.id.roll_image);
    iv_loading = findViewById(R.id.iv_loading);
    progressBar = findViewById(R.id.progress_loading);
    tv_loading_tip = findViewById(R.id.tv_loading_tip);
    TextView bt_continue = findViewById(R.id.bt_continue);
    TextView bt_renew = findViewById(R.id.bt_renew);
    imageViews = new ImageView[4];
    textViews = new TextView[4];
    View[] views = new View[4];
    views[0] = findViewById(R.id.view1);
    views[1] = findViewById(R.id.view2);
    views[2] = findViewById(R.id.view3);
    views[3] = findViewById(R.id.view4);
    imageViews[0] = findViewById(R.id.iv_book_study1);
    imageViews[1] = findViewById(R.id.iv_book_study2);
    imageViews[2] = findViewById(R.id.iv_book_study3);
    imageViews[3] = findViewById(R.id.iv_book_study4);
    textViews[0] = findViewById(R.id.tv_book_study1);
    textViews[1] = findViewById(R.id.tv_book_study2);
    textViews[2] = findViewById(R.id.tv_book_study3);
    textViews[3] = findViewById(R.id.tv_book_study4);
    gp_progress = findViewById(R.id.gp_progress);
    gp_continue = findViewById(R.id.gp_continue);
    gp_completed = findViewById(R.id.gp_completed);
    gp_music = findViewById(R.id.gp_view4);

    bt_continue.setOnClickListener(this);
    bt_renew.setOnClickListener(this);
    for (View view : views) {
      view.setOnClickListener(this);
    }
    showLoading();

    try {
      GifDrawable gifDrawable = new GifDrawable(getResources(), R.drawable.airport);
      gifDrawable.setLoopCount(1000);
      gifDrawable.start();
      iv_loading.setImageDrawable(gifDrawable);
    } catch (Exception e) {
      e.printStackTrace();
      iv_loading.setImageResource(R.drawable.loading_anim);
    }
  }

  @SuppressLint("NewApi")
  private void initSound() {
    soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    clickID = soundPool.load(this, R.raw.click_voice, 1);
  }

  private void playSound(int id) {
    soundPool.play(
        id,
        1f,      //左耳道音量【0~1】
        1f,      //右耳道音量【0~1】
        0,         //播放优先级【0表示最低优先级】
        0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
        1          //播放速度【1是正常，范围从0~2】
    );
  }

  @Override
  public void onClick(View v) {
    playSound(clickID);
    if (v.getId() == R.id.bt_continue) {
      Link link2 = links.get(1);
      int progress2 = PreferenceUtil.getSharePref(this, link2.item_id + link2.type, 0);
      if (progress2 == 0) {
        gotoLink(link2.type);
      } else {
        Link link3 = links.get(2);
        gotoLink(link3.type);
      }
    } else if (v.getId() == R.id.bt_renew || v.getId() == R.id.view1) {
      gotoLink(links.get(0).type);
    } else if (v.getId() == R.id.view2) {
      gotoLink(links.get(1).type);
    } else if (v.getId() == R.id.view3) {
      gotoLink(links.get(2).type);
    } else if (v.getId() == R.id.view4) {
      gotoLink(links.get(3).type);
    }
  }

  private void initData() {
    bookManager = BookManager.getInstance();
    if (bookManager == null) {
      finish();
      return;
    }
    Challenge challenge = bookManager.getBookLearning();
    if (challenge == null) {
      return;
    }
    milesson_item_id = challenge.milesson_item_id;
    Disposable disposable = ApiHandler.getBaseApi().commonJsonGet(challenge.url)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<JsonObject>() {
          @Override
          public void accept(JsonObject jsonObject) throws Exception {
            String retcode = jsonObject.get("retcode").getAsString();
            if (TextUtils.equals(retcode, "SUCC") || TextUtils.equals(retcode, "success")) {
              PreferenceUtil.setSharePref(PicLoadingActivity.this, "study_" + milesson_item_id, jsonObject.toString());
              PicLoadingActivity.this.parseData(jsonObject);
            } else {
              String description = jsonObject.get("description").getAsString();
              PicLoadingActivity.this.toast(description);
              PicLoadingActivity.this.finish();
            }
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) throws Exception {
            Logger.e(throwable, throwable.toString());
            String jsonObjectStr = PreferenceUtil.getSharePref(PicLoadingActivity.this, "study_" + milesson_item_id, "");
            if (!jsonObjectStr.equals("")) {
              JsonObject jsonObject = new Gson().fromJson(jsonObjectStr, JsonObject.class);
              if (jsonObject != null)
                PicLoadingActivity.this.parseData(jsonObject);
            } else {
              PicLoadingActivity.this.toast("您的网络状况较差，请检查网络连接。");
            }
          }
        });
    compositeDisposable.add(disposable);
  }

  private void saveLocalPre(int progress) {
    List<BookLearning.Link> links = bookManager.getLinks();
    for (int i = 0; i < progress; i++) {
      BookLearning.Link link = links.get(i);
      PreferenceUtil.setSharePref(this, link.item_id + link.type, 1);
    }
  }

  private void parseData(JsonObject jsonObject) {
    JsonObject data = jsonObject.get("data").getAsJsonObject();
    tips = new Gson().fromJson(data.get("tips"), new TypeToken<List<String>>() {
    }.getType());
    showTips();
    links = new Gson().fromJson(data.get("list"), new TypeToken<List<BookLearning.Link>>() {
    }.getType());
    bookManager.setLinks(links);

    //saveLocal prefence not login
    if (bookManager != null && bookManager.getChallenges().size() != 0)
      saveLocalPre(bookManager.getChallenges().get(0).progress);

    if (links.size() > 2 && links.get(2).data != null) {
      videoPicInfos = new Gson().fromJson(links.get(2).data.get("data_list"), new TypeToken<List<VideoPicInfo>>() {
      }.getType());
      bookManager.setVideoPicInfos(videoPicInfos);
    }

    if (links.size() > 3 && links.get(3).data != null) {
      audioInfos = new Gson().fromJson(links.get(3).data.get("data_list"), new TypeToken<List<AudioInfo>>() {
      }.getType());
      bookManager.setPlaylist(audioInfos);
    }

    Observable<Set<String>> wordObservable = null;
    Observable<Set<String>> readObservable = null;
    Observable<Set<String>> videoObservable = null;
    for (BookLearning.Link link : links) {
      String type = link.type;
      if (TextUtils.equals(type, BOOK_LINKS_WORD)) {
        wordObservable = extractWordResource(link.data);
      } else if (TextUtils.equals(type, BOOK_LINKS_READ)) {
        readObservable = extractReadResource(link.data);
      } else if (TextUtils.equals(type, BOOK_LINKS_VIDEO)) {
        videoObservable = extractVideoResource(link.data);
      }
    }
    if (wordObservable == null || readObservable == null || videoObservable == null) {
      return;
    }
    Disposable disposable = Observable.zip(wordObservable, readObservable, videoObservable,
        (urls1, urls2, urls3) -> {
          urls1.addAll(urls2);
          //看动画视频资源在线播放
          return urls1;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::preDownload, throwable -> {
          Logger.e(throwable, throwable.toString());
          Set<String> urls = PreferenceUtil.getSharePrefSet(this, "urls_" + milesson_item_id, null);
          if (urls != null && urls.size() != 0) {
            preDownload(urls);
          } else {
            toast("您的网络状况较差，请检查网络连接。");
            finish();
          }
        });
    compositeDisposable.add(disposable);
  }

  private Observable<Set<String>> extractWordResource(JsonObject data) {
    return Observable.just(data)
        .map((Function<JsonObject, List<WordExercise>>) jsonObject -> {
          JsonElement jsonElement = jsonObject.get("exercises");
          File file = AppPicUtil.getBookResource(getApplicationContext(), BOOK_LINKS_WORD, milesson_item_id);
          FileUtil.StringToFile(jsonElement.toString(), file, "utf-8");
          //评分规则
          JsonElement ratingRule = jsonObject.get("rating_rule");
          File fileRating = AppPicUtil.getBookResource(getApplicationContext(), BOOK_LINKS_WORD_RATING, milesson_item_id);
          FileUtil.StringToFile(ratingRule.toString(), fileRating, "utf-8");
          return new Gson().fromJson(jsonElement, new TypeToken<List<WordExercise>>() {
          }.getType());
        })
        .map(wordExercises -> {
          Set<String> urls = new TreeSet<>();
          for (WordExercise wordExercise : wordExercises) {
            Question question = wordExercise.question;
            urls.add(question.image);
            urls.add(question.audio);
          }
          return urls;
        });
  }

  private Observable<Set<String>> extractReadResource(JsonObject data) {
    return Observable.just(data)
        .map(jsonObject -> jsonObject.get("json_url").getAsString())
        .flatMap((Function<String, ObservableSource<JsonObject>>) url -> ApiHandler.getBaseApi().commonJsonGet(url))
        .map(jsonObject -> {
          File file = AppPicUtil.getBookResource(getApplicationContext(), BOOK_LINKS_READ, milesson_item_id);
          FileUtil.StringToFile(jsonObject.toString(), file, "utf-8");
          Book book = new Gson().fromJson(jsonObject, Book.class);
          Set<String> urls = new TreeSet<>();
          ArrayList<BookPage> pages = book.bookpage;
          for (BookPage page : pages) {
            urls.add(page.page_url);
            ArrayList<TrackInfo> tracks = page.track_info;
            for (TrackInfo track : tracks) {
              urls.add(TextUtils.isEmpty(track.mp3url_hiq) ? track.mp3url : track.mp3url_hiq);
            }
          }
          return urls;
        });
  }

  private Observable<Set<String>> extractVideoResource(JsonObject data) {
    return Observable.just(data)
        .map(jsonObject -> {
          File file = AppPicUtil.getBookResource(getApplicationContext(), BOOK_LINKS_VIDEO, milesson_item_id);
          FileUtil.StringToFile(jsonObject.toString(), file, "utf-8");
          String url = jsonObject.get("video_url").getAsString();
          Set<String> urls = new TreeSet<>();
          urls.add(url);
          return urls;
        });
  }

  private void preDownload(Set<String> urls) {
    PreferenceUtil.setSharePrefSet(this, "urls_" + milesson_item_id, urls);
    downloadCount = urls.size();
    Logger.d(TAG, "资源总数: " + downloadCount);
    if (downloadCount == 0) {
      return;
    }
    FileDownloader fileDownloader = FileDownloader.getImpl();
    List<BaseDownloadTask> tasks = new ArrayList<>();
    for (String url : urls) {
      if (TextUtils.isEmpty(url)) {
        completedCount++;
        continue;
      }
      File resource = AppPicUtil.getBookResource(getApplicationContext(), url, milesson_item_id);
      if (!resource.exists()) {
        String path = resource.getAbsolutePath();
        tasks.add(fileDownloader.create(url).setPath(path));
      } else {
        completedCount++;
      }
    }
    if (completedCount == downloadCount) {
      //资源已经下载
      updateProgress();
    } else {
      Logger.d(TAG, "需要下载资源数: " + tasks.size());
      download(tasks);
    }
  }

  private void download(List<BaseDownloadTask> tasks) {
    FileDownloadQueueSet queueSet = new FileDownloadQueueSet(new FileDownloadSampleListener() {

      @Override
      protected void completed(BaseDownloadTask task) {
        completedCount++;
        updateProgress();
      }

      @Override
      protected void error(BaseDownloadTask task, Throwable e) {
        Logger.e(e, e.toString());
      }
    });
    queueSet.disableCallbackProgressTimes();
    queueSet.setAutoRetryTimes(3);
    queueSet.downloadTogether(tasks);
    queueSet.start();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (connectionReceiver != null) {
      unregisterReceiver(connectionReceiver);
    }
    if (soundPool != null) {
      soundPool.release();
    }
  }

  private void showLoading() {
    roll_image.setVisibility(View.VISIBLE);
    gp_progress.setVisibility(View.VISIBLE);
    iv_loading.setVisibility(View.VISIBLE);
  }

  private void showTips() {
    if (tips == null || tips.size() == 0) {
      return;
    }
    if (tips.size() == 1) {
      tv_loading_tip.setText(tips.get(0));
    } else {
      Disposable disposable = Observable.interval(0, 5, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
          .subscribe(aLong -> {
            int index = (int) (Math.random() * tips.size());
            tv_loading_tip.setText(tips.get(index));
          });
      compositeDisposable.add(disposable);
    }
  }

  private void showContinue() {
    enableBack();
    iv_loading.setVisibility(View.GONE);
    gp_progress.setVisibility(View.GONE);
    gp_continue.setVisibility(View.VISIBLE);
    roll_image.setSpeed(0);
  }

  private void showCompleted() {
    enableBack();
    roll_image.setVisibility(View.GONE);
    iv_loading.setVisibility(View.GONE);
    gp_progress.setVisibility(View.GONE);
    gp_completed.setVisibility(View.VISIBLE);
    if (links.size() == 4) {
      gp_music.setVisibility(View.VISIBLE);
    } else
      gp_music.setVisibility(View.GONE);

    setActionTitle(getResources().getString(R.string.string_book_study));
    for (int i = 0; i < links.size(); i++) {
      bindLink(links.get(i).type, links.get(i).title, i);
    }
  }

  private void updateProgress() {
    int progress = completedCount * 100 / downloadCount;
    progressBar.setProgress(progress);
    if (progress == 100) {
      loadingCompleted();
    }
    Logger.d(TAG, "已经下载资源数: " + completedCount);
  }

  private void loadingCompleted() {
    loading = false;
    if (links.size() >= 3) {
      Link link1 = links.get(0);
      if (bookManager != null && (bookManager.isHomeWork() || bookManager.isHomeWorkWatch())) {
        gotoLink(link1.type);
        return;
      }
      int progress1 = PreferenceUtil.getSharePref(this, link1.item_id + link1.type, 0);
      if (progress1 == 0) {
        gotoLink(link1.type);
        return;
      }
      Link link3 = links.get(2);
      int progress3 = PreferenceUtil.getSharePref(this, link3.item_id + link3.type, 0);
      if (progress3 == 1) {
        showCompleted();
        return;
      }
      showContinue();
    }
  }

  private void bindLink(String type, String title, int index) {
    switch (type) {
      case BOOK_LINKS_WORD:
        imageViews[index].setImageResource(R.drawable.icon_study_word);
        break;
      case BOOK_LINKS_READ:
        imageViews[index].setImageResource(R.drawable.icon_read_book);
        break;
      case BOOK_LINKS_VIDEO:
        imageViews[index].setImageResource(R.drawable.icon_look_video);
        break;
      case BOOK_LINKS_MUSIC:
        imageViews[index].setImageResource(R.drawable.icon_look_music);
        break;
      default:
        break;
    }
    textViews[index].setText(title);
  }

  private void gotoLink(String type) {
    Intent intent = null;
    switch (type) {
      case BOOK_LINKS_WORD:
        intent = new Intent(this, VocabularyActivity.class);
        break;
      case BOOK_LINKS_READ:
        intent = new Intent(this, ReadBookActivity.class);
        break;
      case BOOK_LINKS_VIDEO:
        intent = new Intent(this, VideoActivity.class);
        break;
      case BOOK_LINKS_MUSIC:
        intent = new Intent(this, MusicPicActivity.class);
        break;
      default:
        break;
    }
    if (intent != null) {
      AppPicUtil.setStartTime(System.currentTimeMillis());
      startActivity(intent);
      finish();
    }
  }

  @Override
  public void onBackPressed() {
    if (loading) {
      return;
    }
    super.onBackPressed();
  }

}
