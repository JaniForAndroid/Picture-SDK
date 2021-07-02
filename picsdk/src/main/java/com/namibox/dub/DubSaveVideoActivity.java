package com.namibox.dub;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import cn.nekocode.rxlifecycle.RxLifecycle;
import com.example.exoaudioplayer.video.base.Constants;
import com.example.exoaudioplayer.video.component.PlayerTitleView;
import com.example.exoaudioplayer.video.fragment.VideoFragment;
import com.example.exoaudioplayer.video.model.MediaBuilder;
import com.example.picsdk.R;
import com.google.gson.Gson;
import com.namibox.commonlib.activity.BaseActivity;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.event.MessageEvent;
import com.namibox.commonlib.event.QiniuEvent;
import com.namibox.commonlib.event.QiniuEvent.QiniuEventType;
import com.namibox.commonlib.model.NetResult;
import com.namibox.commonlib.model.QiniuToken;
import com.namibox.tools.QiniuUploadUtil;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.Utils;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DefaultSubscriber;
import java.io.File;
import java.util.HashMap;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.greenrobot.eventbus.EventBus;


/**
 * Created by sunha on 2016/12/26 0026.
 */

public class DubSaveVideoActivity extends BaseActivity {

  private static final String TAG = "DubSaveVideoActivity";
  FrameLayout videoLayout;
  EditText introEt;
  TextView videoTitle;
  private AudioManager mAm;
  private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
    @Override
    public void onAudioFocusChange(int focusChange) {
      if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
        abandonFocus();
        // Stop playback
        //stopNow();
      }
    }
  };
  private File videoFile;
  private Uri contentUri;
  private String itemId;
  private String subtype;
  private String type;
  private String video_name;
  private String thumb_url;
  private String relation_id;
  private QiniuToken qiniuToken;
  private Disposable upLoadDisposable;
  private VideoFragment mVideoFragment;

  //是否提交,防止重复点击
  private boolean isSubmiting = false;

  private boolean requestFocus() {
    // Request audio focus for playback
    int result = mAm.requestAudioFocus(audioFocusChangeListener,
        // Use the music stream.
        AudioManager.STREAM_MUSIC,
        // Request permanent focus.
        AudioManager.AUDIOFOCUS_GAIN);
    return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  }

  private int abandonFocus() {
    return mAm.abandonAudioFocus(audioFocusChangeListener);
  }


  public static void openLocalVideo(Context context, File videoFile, String itemId,
      String subtype, String type, String video_name, String thumb_url, String relationId) {

    Intent intent = new Intent(context, DubSaveVideoActivity.class)
        .setData(Uri.fromFile(videoFile))
        .putExtra("video_file", videoFile.getAbsolutePath())
        .putExtra("itemId", itemId)
        .putExtra("subtype", subtype)
        .putExtra("type", type)
        .putExtra("video_name", video_name)
        .putExtra("thumb_url", thumb_url)
        .putExtra("tutorable_relation_id", relationId);
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAm = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    requestFocus();
    setContentView(R.layout.hfx_activity_save_dubvideo);
    initView();
    contentUri = getIntent().getData();
    Intent intent = getIntent();
    String videoFilePath = intent.getStringExtra("video_file");
    itemId = intent.getStringExtra("itemId");
    subtype = intent.getStringExtra("subtype");
    type = intent.getStringExtra("type");
    video_name = intent.getStringExtra("video_name");
    thumb_url = intent.getStringExtra("thumb_url");
    relation_id = intent.getStringExtra("tutorable_relation_id");

    if (!TextUtils.isEmpty(videoFilePath)) {
      videoFile = new File(videoFilePath);
    } else {
      showErrorDialog("未找到视频文件", true);
    }
    Point point = new Point();
    getWindowManager().getDefaultDisplay().getSize(point);
    int width = point.x;
    float template_ratio = 16f / 9;
    int videoHeight = (int) (width / template_ratio);
    ViewGroup.LayoutParams layoutParams = videoLayout.getLayoutParams();
    layoutParams.height = videoHeight;
    videoLayout.setLayoutParams(layoutParams);
    videoTitle.setText(video_name);
    initVideoFragment();


  }

  private void initView() {
    videoLayout = findViewById(R.id.video_layout);
    introEt = findViewById(R.id.introEt);
    videoTitle = findViewById(R.id.videoTitle);
    View back_btn = findViewById(R.id.back_btn);
    View saveBtn = findViewById(R.id.saveBtn);
    View submitBtn = findViewById(R.id.submitBtn);
    back_btn.setOnClickListener(this::onViewClick);
    saveBtn.setOnClickListener(this::onViewClick);
    submitBtn.setOnClickListener(this::onViewClick);
  }


  private void initVideoFragment() {
    MediaBuilder mediaBuilder = new MediaBuilder.Builder()
        .setType(Constants.VIDEOSHOW_FRAGMENT)
        .setUri(contentUri.toString())
        .setTitle("")
        .build();
    mVideoFragment = new VideoFragment(mediaBuilder);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.video_fragment, mVideoFragment, "video_fragment")
        .commit();

    mVideoFragment.getFragmentManager()
        .registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
          @Override
          public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v,
              Bundle savedInstanceState) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState);
            mVideoFragment.setOnBackCallBack(new PlayerTitleView.OnBackCallBack() {
              @Override
              public void onBack() {
                onBackPressed();
              }
            });
          }
        }, false);
  }

  @Override
  public void onBackPressed() {
    Configuration config = getResources().getConfiguration();
    if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      return;
    }
    super.onBackPressed();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    abandonFocus();
  }


  public void onViewClick(View view) {
    int i = view.getId();
    if (i == R.id.back_btn) {
      finish();

    } else if (i == R.id.saveBtn) {
      showProgress("保存中...");
      mVideoFragment.setPlayWhenReady(false);
      Flowable.create(new FlowableOnSubscribe<Integer>() {
        @Override
        public void subscribe(@NonNull FlowableEmitter<Integer> emitter) throws Exception {
          File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
          if (path.exists() || path.mkdirs()) {
            String fileName = videoFile.getName();
            try {
              FileUtil.copyFile(videoFile, path, fileName);
              sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                  Uri.parse("file://" + path.getAbsolutePath() + File.separator + fileName)));
              emitter.onComplete();
            } catch (Exception e) {
              e.printStackTrace();
              if (!emitter.isCancelled()) {
                emitter.onError(e);
              }
            }
          }
        }
      }, BackpressureStrategy.LATEST)
          .compose(RxLifecycle.bind(this).<Integer>withFlowable())
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onError(Throwable e) {
              hideProgress();
              toast("保存失败");
              e.printStackTrace();
            }

            @Override
            public void onComplete() {
              hideProgress();
              toast("保存成功");
            }


            @Override
            public void onNext(Integer integer) {

            }
          })

      ;


    } else if (i == R.id.submitBtn) {
      if (!isSubmiting) {
        showDeterminateProgress("请稍后", "视频上传中...", "取消",
            new OnClickListener() {
              @Override
              public void onClick(View view) {
                upLoadDisposable.dispose();
                QiniuUploadUtil.cancel(true);
                isSubmiting = false;
              }
            });
        isSubmiting = true;
        mVideoFragment.setPlayWhenReady(false);
        upLoadDisposable = ApiHandler.getBaseApi().getFileUploadToken(itemId, "mp4")
            .flatMap(new Function<QiniuToken, Flowable<QiniuEvent>>() {

              @Override
              public Flowable<QiniuEvent> apply(@NonNull QiniuToken token)
                  throws Exception {
                qiniuToken = token;
//                                    isUploading = true;
                String dir = FileUtil.getFileCacheDir(DubSaveVideoActivity.this)
                    .getAbsolutePath();
                return QiniuUploadUtil.upload(dir, videoFile, token.key, token.token);
              }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(new Consumer<QiniuEvent>() {
              @Override
              public void accept(QiniuEvent event) throws Exception {
                if (event.type == QiniuEventType.RESULT) {
                  Logger.i(TAG, event.key + ",\r\n " + event.info + ",\r\n " + event.response);
                  if (event.info == null || !event.info.isOK()) {
                    uploadFail();
                  }
                } else if (event.type == QiniuEventType.PROGRESS) {
                  updateDeterminateProgress(
                      "视频上传中... [" + (int) (event.progress * 100) + "/" + 100 + "]",
                      (int) (event.progress * 100));
                } else if (event.type == QiniuEventType.ERROR) {
                  if (Utils.isDev(DubSaveVideoActivity.this)) {
                    toast(event.message);
                  }
                  Logger.e(TAG, "ERROR: " + event.message);
                  uploadFail();
                }
              }
            })
            .filter(new Predicate<QiniuEvent>() {
              @Override
              public boolean test(@NonNull QiniuEvent qiniuEvent) throws Exception {
                return qiniuEvent.type == QiniuEventType.RESULT;
              }
            })
            .observeOn(Schedulers.io())
            .flatMap(new Function<QiniuEvent, Flowable<NetResult>>() {
              @Override
              public Flowable<NetResult> apply(@NonNull QiniuEvent event) throws Exception {
                Logger.i(TAG, "apply: getSubmitObservable");
                return getSubmitObservable();
              }
            })
            .compose(RxLifecycle.bind(this).<NetResult>withFlowable())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<NetResult>() {
              @Override
              public void accept(NetResult result) throws Exception {
                Logger.i(TAG, "onCompleted: " + result.errcode);
                if (result.errcode == 0) {
                  isSubmiting = false;
                  hideDeterminateProgress();
                  HashMap<String, Object> map = new HashMap<>();
                  //make_finsh是后端定的,不是拼写错误
                  map.put("command", "make_finsh");
                  EventBus.getDefault()
                      .post(new MessageEvent("", new Gson().toJson(map), "make_finsh"));
//                  ARouter.getInstance().build("/shareLib/dubShare")
//                      .withString("share_imgUrl", result.wxshare.imgurl)
//                      .withString("share_webpageUrl", result.wxshare.doclink)
//                      .withString("share_title", result.wxshare.grouptitile)
//                      .withString("share_titleFriend", result.wxshare.friendtitile)
//                      .withString("share_content", result.wxshare.groupcontent)
//                      .withInt("share_hearts", result.hearts)
//                      .withTransition(com.namibox.commonlib.R.anim.fade_in,
//                          com.namibox.commonlib.R.anim.fade_out)
//                      .navigation(DubSaveVideoActivity.this);
                  finish();
                } else {
                  uploadFail();
                }
              }
            }, new Consumer<Throwable>() {
              @Override
              public void accept(Throwable throwable) throws Exception {
                uploadFail();
                Logger.e(throwable, "onError");
              }
            });
      }

    }
  }

  private void uploadFail() {
    isSubmiting = false;
    hideDeterminateProgress();
    showErrorDialog("提交失败,请重试!", false);
  }


  public Flowable<NetResult> getSubmitObservable() {
    HashMap<String, Object> map = new HashMap<>();
    String content = introEt.getText().toString();
    if (TextUtils.isEmpty(content)) {
      content = "";
    }
    if (TextUtils.isEmpty(relation_id)) {
      relation_id = "";
    }
    map.put("video_key", qiniuToken.key);
    map.put("itemid", itemId);
    map.put("subtype", subtype);
    map.put("type", type);
    map.put("video_name", video_name);
    map.put("thumb_url", thumb_url);
    map.put("tutorable_relation_id", relation_id);
    map.put("introduce", content);
    Gson gson = new Gson();
    String json = gson.toJson(map);
    RequestBody requestBody = RequestBody
        .create(MediaType.parse("application/json; charset=utf-8"), json);
    return ApiHandler.getBaseApi().dubVideoSubmit(requestBody);

  }

}
