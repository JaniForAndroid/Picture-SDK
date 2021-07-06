package com.example.picsdk;


import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.exoaudioplayer.aduio.base.AbstractAudioPlayer;
import com.example.exoaudioplayer.aduio.base.AudioCallBack;
import com.example.exoaudioplayer.aduio.base.AudioPlayerFactory;
import com.example.picsdk.base.BaseActivity;
import com.example.picsdk.event.ShowWarnLoginEvent;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.model.ProductItem;
import com.example.picsdk.model.ProductItem.BookLearning.Property;
import com.example.picsdk.model.ProductItem.Challenge;
import com.example.picsdk.util.AppPicUtil;
import com.example.picsdk.util.PicturePreferenceUtil;
import com.example.picsdk.view.AspectRatioImageView;
import com.example.picsdk.view.CommonDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.dialog.NamiboxNiceDialog;
import com.namibox.commonlib.event.SaveLocalReadingEvent;
import com.namibox.dub.DubVideoNewActivity;
import com.namibox.hfx.ui.EvalActivity;
import com.namibox.hfx.ui.RecordActivity;
import com.namibox.hfx.utils.HfxUtil;
import com.namibox.tools.GlideUtil;
import com.namibox.tools.PermissionUtil;
import com.namibox.util.AppUtil;
import com.namibox.util.Logger;
import com.namibox.util.NetworkUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import com.othershe.nicedialog.BaseNiceDialog;
import com.othershe.nicedialog.ViewConvertListener;
import com.othershe.nicedialog.ViewHolder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.util.List;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * author : feng
 * description ：绘本导读页
 * creation time : 19-9-9下午3:21
 */
public class PicGuideActivity extends BaseActivity implements ChallengeAdapter.OnItemClickListener {

  public static String CHALLENGE_STUDY = "绘本学习";
  public static String CHALLENGE_WORD = "词汇挑战";
  public static String CHALLENGE_READ = "阅读理解";
  public static String CHALLENGE_PLAY = "趣味配音";
  public static String CHALLENGE_PIC = "评测绘本";

  private TextView tv_name_en;
  private TextView tv_name_cn;
  private AspectRatioImageView iv_cover;
  private ImageView iv_lock;
  private TextView[] tv_nums;
  private TextView[] tv_units;
  private TextView[] tv_descs;
  private Group gp_summary;
  private ChallengeAdapter challengeAdapter;
  private Button bt_purchase;

  private BookManager bookManager;

  private ProductItem.Introduction introduction;
  private List<Challenge> challenges;
  private ProductItem.BookLearning bookLearning;
  private ProductItem.Bottom bottom;
  boolean isForeground = false;
  boolean isFirstLogin = true;
  private SoundPool soundPool;
  private int soundID, clickID, buyID;
  private CommonDialog netWorkDialog;
  public static long startTime = System.currentTimeMillis();
  private AbstractAudioPlayer exoAudioPlayer;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_guide);
    CHALLENGE_STUDY = getString(R.string.book_booklearn_title);
    CHALLENGE_WORD = getString(R.string.book_wordchallenge_title);
    CHALLENGE_READ = getString(R.string.book_readunderstand_title);
    CHALLENGE_PLAY = getString(R.string.book_dubbing_title);
    CHALLENGE_PIC = getString(R.string.book_pic_eval_title);
    initExoAudioPlayer();
    initView();
    if (PicturePreferenceUtil.getLongLoginUserId(this) == -1L &&
        PreferenceUtil.getSharePref(this, "first_login_guide", true)) {
      initDialog();
      PreferenceUtil.setSharePref(this, "first_login_guide", false);
    }
    isFirstLogin = true;
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

      if (wifiNetInfo.isConnected()) {
        if (netWorkDialog != null) {
          netWorkDialog.dismiss();
          toast("已连接WiFi网络");
        }
      }
    }
  };

  private void initExoAudioPlayer() {
    exoAudioPlayer = AudioPlayerFactory.getInstance()
        .createPlayer(getApplicationContext(), com.example.exoaudioplayer.aduio.base.Constants.EXO);
    exoAudioPlayer.setPlayerCallBack(new AudioCallBack() {
      @Override
      public void playUpdate(long currentTime, long bufferTime, long totalTime) {
      }

      @Override
      public void playStateChange(boolean playWhenReady, int playbackState) {
        super.playStateChange(playWhenReady, playbackState);
      }
    });
  }

  @SuppressLint("NewApi")
  private void initSound() {
    soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    soundID = soundPool.load(this, R.raw.study_all_then_enter, 1);
    clickID = soundPool.load(this, R.raw.click_voice, 1);
    buyID = soundPool.load(this, R.raw.buy_help, 1);
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

  public void initDialog() {
    showDialog("提示", getString(R.string.book_registeralertcontent_title),
        "退出", v -> finish(), "继续使用", v -> {
        });
  }

//  @Subscribe(threadMode = ThreadMode.MAIN)
//  public void refreshGuideInfo(RefreshGuideInfo event) {
//    initData();
//  }

  private void initView() {
    initActionBar();
    setActionFeature(R.drawable.icon_detail, v -> {
      if (introduction != null && bookLearning != null) {
        showBookProfile(bookLearning.text, introduction.learning_topic,
            introduction.story_introduction);
      }
    });
    tv_name_en = findViewById(R.id.tv_name_en);
    tv_name_cn = findViewById(R.id.tv_name_cn);
    iv_cover = findViewById(R.id.iv_cover);
    iv_cover.setRoundRadius(0, 10, 10, 0);
    iv_cover.setOnClickListener(v -> {
      if (bookLearning != null) {
        if (bookLearning.is_buy == 0 && bottom != null) {
          playSound(buyID);
          eventClickPush();
          handleAction(bottom.action);
        } else if (bookLearning.is_locked > 0) {
          toast(getResources().getString(R.string.book_guidesteploack_title));
        } else {
          goLoading();
        }
      }
    });
    iv_lock = findViewById(R.id.iv_lock);
    tv_nums = new TextView[3];
    tv_nums[0] = findViewById(R.id.tv_num1);
    tv_nums[1] = findViewById(R.id.tv_num2);
    tv_nums[2] = findViewById(R.id.tv_num3);
    tv_units = new TextView[3];
    tv_units[0] = findViewById(R.id.tv_unit1);
    tv_units[1] = findViewById(R.id.tv_unit2);
    tv_units[2] = findViewById(R.id.tv_unit3);
    tv_descs = new TextView[3];
    tv_descs[0] = findViewById(R.id.tv_desc1);
    tv_descs[1] = findViewById(R.id.tv_desc2);
    tv_descs[2] = findViewById(R.id.tv_desc3);
    gp_summary = findViewById(R.id.gp_summary);
    RecyclerView rv_stage = findViewById(R.id.rv_stage);
    rv_stage.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    challengeAdapter = new ChallengeAdapter();
    challengeAdapter.setOnItemClickListener(this);
    rv_stage.setAdapter(challengeAdapter);
    bt_purchase = findViewById(R.id.bt_purchase);
  }

  private void initData() {
    bookManager = BookManager.getInstance();
    Intent intent = getIntent();
    String url = intent.getStringExtra("url");
    Disposable disposable = ApiHandler.getBaseApi().commonJsonGet(url)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          String retcode = jsonObject.get("retcode").getAsString();
          if (TextUtils.equals(retcode, "SUCC")) {
            parseData(jsonObject);
          }
        }, throwable -> {
          Logger.e(throwable, throwable.toString());
        });
    compositeDisposable.add(disposable);
  }

  private void parseData(JsonObject jsonObject) {
    ProductItem productItem = new Gson().fromJson(jsonObject.get("data"), ProductItem.class);
    bookManager.setMilesson_id(productItem.milesson_id);
    bookManager.setMilesson_item_id(productItem.milesson_item_id);
    bookManager.setProductName(productItem.product_name);
    //导读音频
    if (!TextUtils.isEmpty(productItem.guide_audio) && isForeground && isFirstLogin) {
      Uri uri = Uri.parse(productItem.guide_audio);
      exoAudioPlayer.play(uri);
      isFirstLogin = false;
    }
    //绘本学习
    bookLearning = productItem.book_learning;
    bookManager.setLink_num(bookLearning.link_num);
    bookManager.setLesson_name(bookLearning.text);
    tv_name_en.setText(bookLearning.text);
    tv_name_cn.setText(bookLearning.chinese_name);
    GlideUtil.loadImage(this, bookLearning.thumb_url, iv_cover);
    List<Property> property_list = bookLearning.property_list;
    if (property_list.size() == 3) {
      for (int i = 0; i < property_list.size(); i++) {
        Property property = property_list.get(i);
        tv_nums[i].setText(property.num_str);
        tv_units[i].setText(property.unit);
        tv_descs[i].setText(property.text);
        if (i == 0) {
          bookManager.setWordNumber(property.num_str);
        }
      }
    } else if (property_list.size() == 2) {
      gp_summary.setVisibility(View.GONE);
      for (int i = 0; i < property_list.size(); i++) {
        Property property = property_list.get(i);
        if (i == 0) {
          bookManager.setWordNumber(property.num_str);
          tv_nums[i].setText(property.num_str);
          tv_units[i].setText(property.unit);
          tv_descs[i].setText(property.text);
        } else {
          tv_nums[i + 1].setText(property.num_str);
          tv_units[i + 1].setText(property.unit);
          tv_descs[i + 1].setText(property.text);
        }
      }

    }
    //挑战列表
    challenges = productItem.challenge_list;
    challenges.add(0, bookLearning);
    challengeAdapter.setChallenges(challenges);
    bookManager.setChallenges(challenges);

    //简介
    introduction = productItem.introduction;
    //底部按钮
    bottom = productItem.bottom;
    bottom.is_free = 1;
    for (Challenge challenge : challenges) {
      challenge.is_buy = bottom.is_buy | bottom.is_free;
    }
    iv_lock.setVisibility(
        bookLearning.is_buy == 0 || bookLearning.is_locked > 0 ? View.VISIBLE : View.GONE);
    if (bottom.is_buy > 0 || bottom.is_free > 0) {
      bt_purchase.setVisibility(View.GONE);
    } else {
      bt_purchase.setText(bottom.btn_text);
      bt_purchase.setOnClickListener(v -> {
        playSound(buyID);
        eventClickPush();
        handleAction(bottom.action);
      });

    }
//    AppUtil.TagEventEnterPush(true, bookLearning.text, "绘本导读页面", bookManager.getProductName());
  }

  @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
  public void saveLocalReadingEvent(SaveLocalReadingEvent event) {
    if (PicturePreferenceUtil.getLongLoginUserId(this) == -1L) {
      AppUtil.saveLoaclReading(bookManager, this);
    }
  }

  public void eventClickPush() {
    JsonObject mainObj = new JsonObject();
    JsonObject tga_event = new JsonObject();
    JsonObject object = new JsonObject();
    object.addProperty("page", "绘本导读页面");
    object.addProperty("button", "立即购买");
    object.addProperty("title", bookLearning.text);
    object.addProperty("product_name", bookManager.getProductName());
    tga_event.add("properties", object);
    tga_event.addProperty("event_name", "nb_app_click");
    mainObj.add("tga_event", tga_event);
    AppUtil.TagEventClickPush(mainObj);
  }

  @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
  public void showWarnLoginEvent(ShowWarnLoginEvent event) {
    if (AppUtil.isForeground(this, PicGuideActivity.class.getName())) {
//      ARouter.getInstance().build("/namibox/SYOnkeyLogin")
//          .withString("redirect", event.redirect)
//          .navigation();
      // TODO: 2021/6/29 登录
    }
  }

  @Override
  public void onItemClick(int position) {
    Challenge challenge = challenges.get(position);
    if (challenge.is_buy == 0 && bottom != null) {
      playSound(buyID);
      eventClickPush();
      handleAction(bottom.action);
      return;
    }

    //local prefence
    int lock = challenge.is_locked;
    if (PicturePreferenceUtil.getLongLoginUserId(this) == -1L) {
      if (TextUtils.equals(challenge.task_type, CHALLENGE_WORD)) {
        if (PreferenceUtil
            .getSharePref(this, BookManager.getInstance().getMilesson_item_id() + "progress", 0)
            == 3) {
          lock = 0;
        }
      } else if (TextUtils.equals(challenge.task_type, CHALLENGE_READ)) {
        if (PreferenceUtil
            .getSharePref(this,
                BookManager.getInstance().getMilesson_item_id() + CHALLENGE_WORD + "star", 0)
            >= 1) {
          lock = 0;
        }
      } else if (TextUtils.equals(challenge.task_type, CHALLENGE_PLAY)) {
        if (PreferenceUtil
            .getSharePref(this,
                BookManager.getInstance().getMilesson_item_id() + CHALLENGE_READ + "star", 0)
            >= 1) {
          lock = 0;
        }
      }
//      switch (challenge.task_type) {
//        case CHALLENGE_WORD:
//          if (PreferenceUtil
//              .getSharePref(this, BookManager.getInstance().getMilesson_item_id() + "progress", 0)
//              == 3) {
//            lock = 0;
//          }
//          break;
//        case CHALLENGE_READ:
//          if (PreferenceUtil
//              .getSharePref(this,
//                  BookManager.getInstance().getMilesson_item_id() + CHALLENGE_WORD + "star", 0)
//              >= 1) {
//            lock = 0;
//          }
//          break;
//        case CHALLENGE_PLAY:
//          if (PreferenceUtil
//              .getSharePref(this,
//                  BookManager.getInstance().getMilesson_item_id() + CHALLENGE_READ + "star", 0)
//              >= 1) {
//            lock = 0;
//          }
//          break;
//        default:
//          break;
//      }
    }

    if (lock == 1) {
      toast(getString(R.string.book_guidesteploack_title));
      playSound(soundID);
      return;
    }
    playSound(clickID);
    if (TextUtils.equals(challenge.task_type, CHALLENGE_STUDY)) {
      goLoading();
    } else if (TextUtils.equals(challenge.task_type, CHALLENGE_WORD)) {
      if (NetworkUtil.isNetworkConnected(this)) {
        gotoWordChallenge();
      } else {
        toast(getString(R.string.common_check_network_tips));
      }
    } else if (TextUtils.equals(challenge.task_type, CHALLENGE_READ)) {
      if (NetworkUtil.isNetworkConnected(this)) {
        gotoReadChallenge();
      } else {
        toast(getString(R.string.common_check_network_tips));
      }
    } else if (TextUtils.equals(challenge.task_type, CHALLENGE_PLAY)) {
      if (NetworkUtil.isNetworkConnected(this)) {
        gotoPlayChallenge(challenge.url);
      } else {
        toast(getString(R.string.common_check_network_tips));
      }
    } else if (TextUtils.equals(challenge.task_type, CHALLENGE_PIC)) {
      if (NetworkUtil.isNetworkConnected(this)) {
        gotoPicChallenge(challenge);
      } else {
        toast(getString(R.string.common_check_network_tips));
      }
    }
//    switch (challenge.task_type) {
//      case CHALLENGE_STUDY:
//        goLoading();
//        break;
//      case CHALLENGE_WORD:
//        if (NetworkUtil.isNetworkConnected(this)) {
//          gotoWordChallenge();
//        } else {
//          toast(getString(R.string.common_check_network_tips));
//        }
//        break;
//      case CHALLENGE_READ:
//        if (NetworkUtil.isNetworkConnected(this)) {
//          gotoReadChallenge();
//        } else {
//          toast(getString(R.string.common_check_network_tips));
//        }
//        break;
//      case CHALLENGE_PLAY:
//        if (NetworkUtil.isNetworkConnected(this)) {
//          gotoPlayChallenge(challenge.url);
//        } else {
//          toast(getString(R.string.common_check_network_tips));
//        }
//        break;
//      case CHALLENGE_PIC:
//        if (NetworkUtil.isNetworkConnected(this)) {
//          gotoPicChallenge(challenge);
//        } else {
//          toast(getString(R.string.common_check_network_tips));
//        }
//        break;
//      default:
//        break;
//    }
  }

  public void goLoading() {
    String typeName = AppUtil.getNetWorkType(this);
    if (typeName.equalsIgnoreCase("4G")) {
      netWorkDialog = new CommonDialog(this);
      netWorkDialog.setMessage(getString(R.string.book_dubnotwifialert_title))
//                .setImageResId(R.mipmap.ic_launcher)
          .setTitle(getString(R.string.common_network_title))
          .setPositive("确认")
          .setNegtive("取消")
//                .setSingle(true)
          .setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
            @Override
            public void onPositiveClick() {
              netWorkDialog.dismiss();
              PermissionUtil
                  .requestPermission(PicGuideActivity.this, () -> gotoLoading(false),
                      permission.RECORD_AUDIO);
            }

            @Override
            public void onNegtiveClick() {
              netWorkDialog.dismiss();
            }

            @Override
            public void onMessagelick() {
            }
          }).show();
//          showDialog("网络提醒", "当前网络无WI-FI，继续使用可以能会产生相关流量费用", "确认",
//                  v -> PermissionUtil.requestPermission(PicGuideActivity.this, this::gotoLoading, permission.RECORD_AUDIO), "取消", null);
    } else {
      PermissionUtil.requestPermission(PicGuideActivity.this, () -> gotoLoading(true),
          permission.RECORD_AUDIO);
    }
  }

  private void gotoLoading(boolean isShow) {
    Intent intent = new Intent(this, PicLoadingActivity.class);
    intent.putExtra("isShowWifiDialog", isShow);
    startActivity(intent);
  }

  private void gotoWordChallenge() {
    Intent intent = new Intent(this, ExerciseChallengeActivity.class);
    intent.putExtra("exercise_type", CHALLENGE_WORD);
    startActivity(intent);
  }

  private void gotoReadChallenge() {
    Intent intent = new Intent(this, ExerciseChallengeActivity.class);
    intent.putExtra("exercise_type", CHALLENGE_READ);
    startActivity(intent);
  }

  private void gotoPlayChallenge(String url) {
    if (!Utils.checkIsX86() && Utils.checkSupportV7a()) {
      Intent intent = new Intent(this, DubVideoNewActivity.class);
      intent.putExtra("title", bookLearning.text);
      intent.putExtra("product_name", bookManager.getProductName());
      intent.putExtra("json_url", url);
      intent.putExtra("userid", Utils.getLoginUserId(this));
      intent.putExtra("ossTokenUrl", AppUtil.getBaseUrl() + "/api/get_oss_token");
      intent.putExtra("reportUrl", AppUtil.getBaseUrl() + "/api/report_fundubbing_progress");
      intent.putExtra("milesson_id", bookManager.getMilesson_id());
      intent.putExtra("book_id", bookManager.getMilesson_item_id());
      startActivity(intent);
    } else {
      showErrorDialog("检测到您的设备过于陈旧，不支持本功能", false);
    }
  }

  private void gotoPicChallenge(Challenge challenge) {
    saveExtInfo(challenge);
    Intent intent = new Intent();
    intent.setClass(this, EvalActivity.class);
    intent.putExtra(RecordActivity.ARG_RECORD_URL, challenge.url);
    intent.putExtra(RecordActivity.ARG_BOOK_ID, challenge.command_map.book_id);
    intent.putExtra(RecordActivity.ARG_CONTENT_TYPE, challenge.command_map.content_type);
    if (!TextUtils.isEmpty(challenge.command_map.matchid)) {
      intent.putExtra("match_id", challenge.command_map.matchid);
    }
    startActivity(intent);
  }

  private void saveExtInfo(Challenge challenge) {
    try {
      HfxUtil.saveMatchInfo(this, challenge.command_map.book_id, challenge.command_map.matchid,
          challenge.command_map.matchname, AppUtil.getBaseUrl() + challenge.command_map.submiturl);
    } catch (IOException e) {
      toast("保存活动信息失败,将作为普通作品提交");
      e.printStackTrace();
    }
    try {
      HfxUtil.saveClassInfo(this, challenge.command_map.book_id,
          challenge.command_map.transmissionparam, 0);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      HfxUtil.saveDirectUploadInfo(this, challenge.command_map.book_id, false);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      HfxUtil.saveExtraInfo(this, challenge.command_map.book_id, challenge.command_map.extra);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void showBookProfile(String title, String topic, String brief) {
    NamiboxNiceDialog.init()
        .setLayoutId(R.layout.layout_guide_detail)
        .setConvertListener(new ViewConvertListener() {
          @Override
          protected void convertView(ViewHolder viewHolder, BaseNiceDialog baseNiceDialog) {
            ImageView iv_close = viewHolder.getView(R.id.iv_close);
            iv_close.setOnClickListener(v -> baseNiceDialog.dismissAllowingStateLoss());
            TextView tv_title = viewHolder.getView(R.id.tv_title);
            TextView tv_topic_detail = viewHolder.getView(R.id.tv_topic_detail);
            TextView tv_brief_detail = viewHolder.getView(R.id.tv_brief_detail);
            tv_title.setText(title);
            tv_topic_detail.setText(topic);
            tv_brief_detail.setText(brief);
          }
        })
        .setShowBottom(true)
        .show(getSupportFragmentManager());
  }

  @Override
  protected void onResume() {
    super.onResume();
    isForeground = true;
    initData();

    //JsonObject 无法序列化，且links是在本界面获取的，回到导读页面时清空links
    bookManager.setLinks(null);
  }

  @Override
  protected void onPause() {
    super.onPause();
    isForeground = false;
    exoAudioPlayer.stop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (bookLearning != null && bookLearning.text != null) {
      AppUtil.TagEventEnterPush(false, bookLearning.text, "绘本导读页面", bookManager.getProductName());
    }
    bookManager.setChallenges(null);
    bookManager.setLinks(null);
    bookManager.setVideoPicInfos(null);

    if (connectionReceiver != null) {
      unregisterReceiver(connectionReceiver);
    }
    AppPicUtil.setStartTime(0L);

    if (soundPool != null) {
      soundPool.release();
    }

    if (exoAudioPlayer != null) {
      exoAudioPlayer.releasePlayer();
      exoAudioPlayer.setPlayerCallBack(null);
      exoAudioPlayer = null;
    }
  }
}
