package com.namibox.hfx.ui;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.picsdk.R;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.view.CircleImageView;
import com.namibox.hfx.bean.CommitInfo;
import com.namibox.hfx.utils.HfxFileUtil;
import com.namibox.hfx.utils.HfxUtil;
import com.namibox.imageselector.RxImagePicker;
import com.namibox.imageselector.RxImagePicker.FileChooseCallback;
import com.namibox.util.FileUtil;
import com.namibox.util.NetworkUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sunha on 2015/10/13 0013.
 */
public class SaveAudioActivity extends BaseCommitActivity implements
    FileChooseCallback {

  static final String REG = "^[\u4e00-\u9fa5a-zA-Z0-9\\s*]+$";
  public static final String AUDIO_ID = "audioId";
  public static final String AUDIO_DURATION = "duration";
  public static final String TAG = "SaveAudioActivity";
  public static final int RESULT_ERROR = 1;
  public static final int RESULT_EXIT = 3;

  CircleImageView photoView;
  TextView subtypeTv;
  RecyclerView recyclerview;
  EditText titleEt;
  EditText introEt;
  Button submitBtn;
  LinearLayout categoryLayout;
  View blankSubTypeView;
  LinearLayout subTypeLayout;
  CheckBox classCheckBox;
  Button classubmitBtn;
  LinearLayout classLayout;
  private File audioDir;

  private CommitInfo info;
  private File photoTempFile;
  private int sampleRate = 44100;
  private Intent intent;
  private int spanCount;
  private Adapter adapter;
  private String[] categories;
  public static final String SUB_TYPE = "audio_subtype";
  private boolean isCached = false;

  @Override
  protected void setThemeColor() {
    super.setThemeColor();
    statusbarColor = toolbarColor = ContextCompat.getColor(this, R.color.theme_color);
    toolbarContentColor = ContextCompat.getColor(this, R.color.hfx_white);
    darkStatusIcon = false;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle("故事秀提交");
    setContentView(R.layout.hfx_activity_save_audio);
    if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
      AudioManager myAudioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
      String nativeParam = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
      sampleRate = Integer.parseInt(nativeParam);
    }
    initView();
    getSubtype();
    intent = getIntent();
    workId = intent.getStringExtra(AUDIO_ID);
    duration = intent.getIntExtra(AUDIO_DURATION, 0);

    if (TextUtils.isEmpty(workId)) {
      showErrorDialog("未知音频", true);
      return;
    } else {
      strings = workId.split("_");
//            titleEt.setText(strings[2]);
      audioDir = HfxFileUtil.getUserWorkDir(this, workId);
//            pcmFile = new File(audioDir, videoId + ".pcm");
      photoFile = HfxFileUtil.getCoverFile(SaveAudioActivity.this, workId);
      photoTempFile = new File(audioDir, workId + "temp" + HfxFileUtil.PHOTO_TYPE);
      mp3File = HfxFileUtil.getStoryAudioFile(SaveAudioActivity.this, workId);
    }
    if (!mp3File.exists()) {
      new Builder(this)
          .setTitle("错误")
          .setMessage("未找到音频文件")
          .setCancelable(false)
          .setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              FileUtil.deleteDir(audioDir);
              setResult(RESULT_ERROR, intent);
              finish();
            }
          }).create().show();
      return;
    }
    info = HfxUtil.getCommitInfo(this, workId);

    if (null != info) {
      subtypeTv.setText(info.subtype);
      titleEt.setText(info.bookname);
      introEt.setText(info.subtitle);
    }
    classInfo = HfxUtil.getClassInfo(this, workId);
    if (classInfo != null && classInfo.classCheck != 0) {
      submitBtn.setVisibility(View.GONE);
      classLayout.setVisibility(View.VISIBLE);
      classCheckBox.setChecked(classInfo.classCheck > 0);
    } else {
      submitBtn.setVisibility(View.VISIBLE);
      classLayout.setVisibility(View.GONE);
    }
    RequestManager requestManager = Glide.with(this);
    RequestOptions options = new RequestOptions()
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .error(R.color.hfx_gray_mark)
        .placeholder(R.color.hfx_gray_mark);
    if (photoFile.exists()) {
      requestManager
          .asBitmap()
          .load(photoFile)
          .apply(options)
          .into(photoView);
    }


  }

  private void initView() {
    photoView = findViewById(R.id.photo_view);
    subtypeTv = findViewById(R.id.subtypeTv);
    recyclerview = findViewById(R.id.recyclerview);
    titleEt = findViewById(R.id.titleEt);
    introEt = findViewById(R.id.introEt);
    submitBtn = findViewById(R.id.submitBtn);
    categoryLayout = findViewById(R.id.categoryLayout);
    blankSubTypeView = findViewById(R.id.blankSubTypeView);
    subTypeLayout = findViewById(R.id.subTypeLayout);
    classCheckBox = findViewById(R.id.classCheckBox);
    classubmitBtn = findViewById(R.id.classubmitBtn);
    classLayout = findViewById(R.id.classLayout);

    photoView.setOnClickListener(this::onViewClick);
    submitBtn.setOnClickListener(this::onViewClick);
    blankSubTypeView.setOnClickListener(this::onViewClick);
    classubmitBtn.setOnClickListener(this::onViewClick);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    photoTempFile.delete();
  }

  public void onViewClick(View view) {
    int i = view.getId();
    if (i == R.id.blankSubTypeView) {
      subTypeLayout.setVisibility(View.GONE);

    } else if (i == R.id.subtypeTv) {
      showCategory();

    } else if (i == R.id.photo_view) {
      RxImagePicker
          .getInstance()
          .openAppFileChooser(SaveAudioActivity.this, SaveAudioActivity.this, 1, 800);

    } else if (i == R.id.submitBtn) {
      submit();

    } else if (i == R.id.classubmitBtn) {
      if (classInfo != null && classInfo.classCheck != 0) {
        if (classCheckBox.isChecked()) {
          classInfo.classCheck = 1;
        } else {
          classInfo.classCheck = -1;
        }
      }
      submit();
    }
  }

  private void submit() {
    doSaveTemp();
    String title = titleEt.getText().toString();
    if (TextUtils.isEmpty(title)) {
      toast("请输入声音标题");
      return;
    }

    if (title.replace(" ", "").length() == 0) {
      toast("标题不能全是空格哦");
      return;
    }
    if (title.length() < 2) {
      toast("标题字数太少");
      return;
    }
    if (!title.matches(REG)) {
      toast("作品标题仅支持中英文字符，数字和空格");
      return;
    }
    String introduce = introEt.getText().toString();
    if (TextUtils.isEmpty(introduce)) {
      toast(getString(R.string.hfx_empty_story_introduce));
      return;
    }
    if (introduce.replace(" ", "").length() == 0) {
      toast("简介不能全是空格哦");
      return;
    }
    if (introduce.length() < 10) {
      toast(getString(R.string.hfx_limit_story_introduce));
      return;
    }
    if (!photoFile.exists()) {
      toast("请添加一张封面图片");
      return;
    }
    if (!NetworkUtil.isNetworkConnected(this)) {
      toast(getString(R.string.hfx_error_network));
      return;
    }
    if (!NetworkUtil.isWiFi(this)) {
      new Builder(this)
          .setTitle("提示")
          .setMessage(R.string.hfx_commit_network_message)
          .setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              doCommit();
            }
          })
          .setNegativeButton("取消", null)
          .create().show();
    } else {
      doCommit();

//                    setResult(RESULT_SUCCESS, intent);
//                    finish();
    }
  }

  private void getSubtype() {
    String tempString = PreferenceUtil.getSharePref(SaveAudioActivity.this, SUB_TYPE, "");
    if (!TextUtils.isEmpty(tempString)) {
      String[] strings = stringToStrings(tempString);
      setSubtype(strings);
      isCached = true;
    } else {
      ApiHandler.getBaseApi()
          .getSubtype("FREE_AUDIO")
          .enqueue(new Callback<String[]>() {
            @Override
            public void onResponse(Call<String[]> call, Response<String[]> response) {
              if (response.isSuccessful()) {
                setSubtype(response.body());
                String tempString = stringsToString(response.body());
                PreferenceUtil.setSharePref(SaveAudioActivity.this, SUB_TYPE, tempString);
              } else {
                categoryLayout.setVisibility(View.GONE);
                hideProgress();
              }
            }

            @Override
            public void onFailure(Call<String[]> call, Throwable t) {
              categoryLayout.setVisibility(View.GONE);
              hideProgress();
            }
          });
    }
    if (!isCached) {
      showProgress("获取分类信息");
    }
  }

  private void setSubtype(String[] strings) {
    categories = strings;
    spanCount = getResources().getInteger(R.integer.hfx_book_span_count);
    adapter = new Adapter();
    GridLayoutManager layoutManager = new GridLayoutManager(SaveAudioActivity.this, spanCount);
    layoutManager.setSpanSizeLookup(new SpanSizeLookup() {
      @Override
      public int getSpanSize(int position) {
        return adapter.getItemViewType(position) == 0 ? 1 : spanCount;
      }
    });
    recyclerview.setLayoutManager(layoutManager);
    recyclerview.setAdapter(adapter);
    hideProgress();
  }

  private String stringsToString(String[] strings) {
    String string = "";
    for (int i = 0; i < strings.length; i++) {
      string += strings[i] + ",";
    }
    if (!TextUtils.isEmpty(string)) {
      return string.substring(0, string.length() - 1);
    }

    return "";
  }

  private String[] stringToStrings(String string) {
    return string.split(",");
  }

  private void showCategory() {
    Animation myAnimation = AnimationUtils.loadAnimation(this, R.anim.hfx_slide_in_bottom);

    subTypeLayout.setVisibility(View.VISIBLE);
    recyclerview.startAnimation(myAnimation);
//        recyclerview.setVisibility(View.VISIBLE);

  }

  private void hideCategory(int position) {
    subtypeTv.setText(categories[position]);
    subTypeLayout.setVisibility(View.GONE);
  }

  private void doCommit() {

    introduce = introEt.getText().toString().replaceAll("[\\t\\n\\r]", "");
    startCommit(introduce, HfxFileUtil.AUDIO_WORK);
    Utils.hideKeyboard(this, introEt);
  }

  private void doSaveTemp() {
    if (info == null) {
      info = new CommitInfo();
    }
    info.bookid = workId;
    info.subtype = subtypeTv.getText().toString();
    info.bookname = titleEt.getText().toString();
    info.subtitle = introEt.getText().toString();
    subType = subtypeTv.getText().toString();
    audioTitle = titleEt.getText().toString();
    introduce = introEt.getText().toString();
    HfxUtil.saveCommitInfo(this, info);
    if (photoTempFile.exists()) {
      photoFile.delete();
      photoTempFile.renameTo(photoFile);
    }

  }


  @Override
  public void onFileChosed(List<File> files) {
    File file = files.get(0);
    photoTempFile.delete();
    try {
      InputStream fosfrom = new FileInputStream(file);

      OutputStream fosto = new FileOutputStream(photoTempFile);
      byte bt[] = new byte[1024];
      int c;
      while ((c = fosfrom.read(bt)) > 0) {
        fosto.write(bt, 0, c);
      }
      fosfrom.close();
      fosto.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    RequestOptions options = new RequestOptions()
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .error(R.color.hfx_gray_mark)
        .placeholder(R.color.hfx_gray_mark);
    Glide.with(this)
        .asBitmap()
        .load(photoTempFile)
        .apply(options)
        .into(photoView);
  }

  private void showExitDialog() {
    doSaveTemp();
    openMyWorkAndFinish(MyWorkActivity.TAB_MAKING);
  }

  @Override
  public void onBackPressed() {
    showExitDialog();
  }

  private class Adapter extends RecyclerView.Adapter<ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(SaveAudioActivity.this)
          .inflate(R.layout.hfx_category_item, parent, false);
      return new ViewHolder(v, SaveAudioActivity.this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      holder.title.setText(categories[position]);
      holder.position = position;
    }

    @Override
    public int getItemCount() {
      return categories.length;
    }
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    TextView title;
    int position;

    ViewHolder(View view, final SaveAudioActivity activity) {
      super(view);
      title = view.findViewById(R.id.title);
      title.setOnClickListener(v -> activity.hideCategory(position));
    }
  }
}


