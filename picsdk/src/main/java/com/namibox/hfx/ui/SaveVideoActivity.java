package com.namibox.hfx.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
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
import com.namibox.commonlib.model.BaseNetResult;
import com.namibox.commonlib.view.CircleImageView;
import com.namibox.hfx.bean.CommitInfo;
import com.namibox.hfx.bean.VideoInfo;
import com.namibox.hfx.utils.HFXWorksUtil;
import com.namibox.hfx.utils.HFXWorksUtil.UploadQiNiuCallback;
import com.namibox.hfx.utils.HFXWorksUtil.VideoTransCallback;
import com.namibox.hfx.utils.HfxFileUtil;
import com.namibox.hfx.utils.HfxUtil;
import com.namibox.util.FileUtil;
import com.namibox.util.NetworkUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DefaultSubscriber;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sunha on 2015/10/13 0013.
 */
public class SaveVideoActivity extends BaseCommitActivity {

  protected static final int REQUEST_APP_IMAGE_CHOOSER = 200;
  protected static final int REQUEST_VIDEO_IMAGE_CHOOSER = 700;

  static final String REG = "^[\u4e00-\u9fa5a-zA-Z0-9\\s*]+$";
  public static final String VIDEO_ID = "videoId";
  public static final String TAG = "SaveVideoActivity";
  CircleImageView photoView;
  EditText titleEt;
  TextView subtypeTv;
  LinearLayout categoryLayout;
  EditText introEt;
  RecyclerView recyclerview;
  LinearLayout subTypeLayout;
  Button submitBtn;
  CheckBox classCheckBox;
  Button classubmitBtn;
  LinearLayout classLayout;
  private File videoDir;
  private File mp4File;
  private File photoFile;
  private File upLoadTempFile;
  private File upLoadFile;
  private CommitInfo info;
  String videoId;
  private Intent intent;
  private int spanCount;
  private Adapter adapter;
  private String[] categories;
  public static final String SUB_TYPE = "video_subtype";
  private boolean isCached = false;
  private String introduce;
  private String title;
  private String subtype;
  private String persistentId;
  private int coverTime = -1;
  private VideoInfo videoInfo;
  DecimalFormat decimalFormat = new DecimalFormat("#0.00");

  public static void openSaveVideo(Activity activity, String videoId) {
    Intent intent = new Intent(activity, SaveVideoActivity.class);
    intent.putExtra(VIDEO_ID, videoId);
    activity.startActivity(intent);
  }

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
    setTitle("视频秀提交");
    getWindow().setFlags(LayoutParams.FLAG_KEEP_SCREEN_ON,
        LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.hfx_activity_save_video);
    initView();
    getSubtype();
    intent = getIntent();
    videoId = intent.getStringExtra(VIDEO_ID);
    videoInfo = HfxUtil.getVideoInfo(this, videoId);
    if (videoInfo != null) {
      strings = videoId.split("_");
      videoDir = HfxFileUtil.getUserWorkDir(this, videoId);
      photoFile = new File(videoDir, videoId + HfxFileUtil.PHOTO_TYPE);
      mp4File = HfxFileUtil.getVideoFile(this, videoId);
      upLoadTempFile = videoInfo.getUpLoadTempFile(this);
      upLoadFile = videoInfo.getUpLoadFile(this);
      if (!mp4File.exists()) {
        FileUtil.deleteDir(videoDir);
        showErrorDialog("未找到视频文件", true);
        return;
      } else {
        initData();
        doSaveTemp();
      }
    } else {
      showErrorDialog("获取视频信息失败,请退出重试或重新制作", true);
      return;
    }
    classInfo = HfxUtil.getClassInfo(this, videoId);
    if (classInfo != null && classInfo.classCheck != 0) {
      submitBtn.setVisibility(View.GONE);
      classLayout.setVisibility(View.VISIBLE);
      classCheckBox.setChecked(classInfo.classCheck > 0);
    } else {
      submitBtn.setVisibility(View.VISIBLE);
      classLayout.setVisibility(View.GONE);
    }

  }

  private void initView() {
    photoView = findViewById(R.id.photoView);
    titleEt = findViewById(R.id.titleEt);
    subtypeTv = findViewById(R.id.subtypeTv);
    categoryLayout = findViewById(R.id.categoryLayout);
    introEt = findViewById(R.id.introEt);
    recyclerview = findViewById(R.id.recyclerview);
    subTypeLayout = findViewById(R.id.subTypeLayout);
    submitBtn = findViewById(R.id.submitBtn);
    classCheckBox = findViewById(R.id.classCheckBox);
    classubmitBtn = findViewById(R.id.classubmitBtn);
    classLayout = findViewById(R.id.classLayout);

    photoView.setOnClickListener(this::onViewClick);
    classubmitBtn.setOnClickListener(this::onViewClick);
    submitBtn.setOnClickListener(this::onViewClick);
    subtypeTv.setOnClickListener(this::onViewClick);
    View blankSubTypeView = findViewById(R.id.blankSubTypeView);
    blankSubTypeView.setOnClickListener(this::onViewClick);
  }

  private void initData() {
    info = HfxUtil.getCommitInfo(this, videoId);
    if (null != info) {
      subtypeTv.setText(info.subtype);
      titleEt.setText(info.bookname);
      introEt.setText(info.subtitle);
    }
  }

  public void getBitmapsFromVideo(File videoFile, File corverFile, int corverTime) {
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    retriever.setDataSource(videoFile.getAbsolutePath());
    Bitmap bitmap = retriever
        .getFrameAtTime(corverTime * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
    String path = corverFile.getAbsolutePath();
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(path);
      bitmap.compress(CompressFormat.JPEG, 80, fos);
      fos.close();
    } catch (Exception e) {
      e.printStackTrace();
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
    } else {
      requestManager.load(R.drawable.hfx_circle_gray_mark)
          .apply(options)
          .into(photoView);
    }


  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_VIDEO_IMAGE_CHOOSER) {
      if (resultCode == Activity.RESULT_OK) {
        String path = data.getStringExtra(CoverActivity.RESULT_IMGPATH);
        if (!TextUtils.isEmpty(path)) {
          photoFile = new File(path);
        }
        coverTime = data.getIntExtra(CoverActivity.RESULT_TIME, 500);
        if (coverTime >= 0) {
          getBitmapsFromVideo(mp4File, photoFile, coverTime);
        }
      }
      return;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  public void onViewClick(View view) {
    int i = view.getId();
    if (i == R.id.photoView) {
      CoverActivity.openCoverActivity(this, videoId, REQUEST_VIDEO_IMAGE_CHOOSER);

    } else if (i == R.id.blankSubTypeView) {
      subTypeLayout.setVisibility(View.GONE);

    } else if (i == R.id.subtypeTv) {
      showCategory();

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
    if (isCommiting) {
      toast("正在上传作品,请稍候");
      return;
    }
//                pcmFile.renameTo(pFile);
    title = titleEt.getText().toString();
    if (TextUtils.isEmpty(title)) {
      toast("请输入视频标题");
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
    if (coverTime < 0) {
      toast("请选择封面");
      return;
    }
    subtype = subtypeTv.getText().toString();
    introduce = introEt.getText().toString();
    if (TextUtils.isEmpty(introduce)) {
      toast(getString(R.string.hfx_empty_video_introduce));
      return;
    }
    if (introduce.replace(" ", "").length() == 0) {
      toast("简介不能全是空格哦");
      return;
    }
    if (introduce.length() < 10) {
      toast(getString(R.string.hfx_limit_video_introduce));
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
              startVideoProcess();
            }
          })
          .setNegativeButton("取消", null)
          .create().show();
    } else {
      startVideoProcess();
    }
  }


  private void getSubtype() {
    String tempString = PreferenceUtil.getSharePref(SaveVideoActivity.this, SUB_TYPE, "");
    if (!TextUtils.isEmpty(tempString)) {
      String[] strings = stringToStrings(tempString);
      setSubtype(strings);
      isCached = true;
    } else {
      ApiHandler.getBaseApi()
          .getSubtype("FREE_VIDEO")
          .enqueue(new Callback<String[]>() {
            @Override
            public void onResponse(Call<String[]> call, Response<String[]> response) {
              if (response.isSuccessful()) {
                setSubtype(response.body());
                String tempString = stringsToString(response.body());
                PreferenceUtil.setSharePref(SaveVideoActivity.this, SUB_TYPE, tempString);
                isCached = true;
              } else {
                if (!isCached) {
                  categoryLayout.setVisibility(View.GONE);
                  hideProgress();
                }
              }
            }

            @Override
            public void onFailure(Call<String[]> call, Throwable t) {
              if (!isCached) {
                categoryLayout.setVisibility(View.GONE);
                hideProgress();
              }
            }
          });
    }
    if (!isCached) {
      showProgress("获取分类信息");
    }
    ApiHandler.getBaseApi().getSubtype("FREE_VIDEO")
        .enqueue(new Callback<String[]>() {
          @Override
          public void onResponse(Call<String[]> call, Response<String[]> response) {
            if (response.isSuccessful()) {
              setSubtype(response.body());
              String tempString = stringsToString(response.body());
              PreferenceUtil.setSharePref(SaveVideoActivity.this, SUB_TYPE, tempString);
              isCached = true;
            } else {
              if (!isCached) {
                categoryLayout.setVisibility(View.GONE);
                hideProgress();
              }
            }
          }

          @Override
          public void onFailure(Call<String[]> call, Throwable t) {
            if (!isCached) {
              categoryLayout.setVisibility(View.GONE);
              hideProgress();
            }
          }
        });
  }

  private void setSubtype(String[] strings) {
    categories = strings;
    spanCount = getResources().getInteger(R.integer.hfx_book_span_count);
    adapter = new Adapter();
    GridLayoutManager layoutManager = new GridLayoutManager(SaveVideoActivity.this, spanCount);
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

  private void startVideoProcess() {
    setUploadEnable(true);
    doSaveTemp();
    if (!TextUtils.isEmpty(persistentId)) {
      doCommit();
    } else if (upLoadFile != null && upLoadFile.exists() && upLoadFile.length() > 0) {
      doUpload();
    } else {
      doTranscode();
    }
  }

  void setUploadEnable(boolean isCommiting) {
    this.isCommiting = isCommiting;
    submitBtn.setEnabled(!isCommiting);
    if (isCommiting) {
      submitBtn.setText("作品提交中...");
    } else {
      submitBtn.setText("请点击重试");
    }

  }

  private void doUpload() {
    if (upLoadFile != null && upLoadFile.exists() && upLoadFile.length() > 0) {
      showDeterminateProgress("提交作品", "正在上传...", "取消", new OnClickListener() {
        @Override
        public void onClick(View view) {
          HFXWorksUtil.cancelUpload(true);
        }
      });
      HFXWorksUtil.startUploadQiNiu(this, coverTime, videoId, upLoadFile,
          new UploadQiNiuCallback() {
            @Override
            public void onError(String error) {
              hideDeterminateProgress();
              toast(error);
              setUploadEnable(false);
            }

            @Override
            public void onLoginError() {
              login();
              finish();
            }

            @Override
            public void onUploadProgress(double progress) {
              updateDeterminateProgress(
                  "正在上传..." + Utils.formatFloatString((float) (progress * 100)) + "%",
                  (int) (progress * 100));
            }

            @Override
            public void onSuccess(String persistentId) {
              SaveVideoActivity.this.persistentId = persistentId;
              if (videoInfo != null) {
                doCommit();
              }
            }
          });
    } else {
      upLoadTempFile.delete();
      showErrorDialog("转码失败", false);
    }
  }

  private void doSaveTemp() {
    if (info == null) {
      info = new CommitInfo();
    }
    info.bookid = videoId;
    info.subtype = subtypeTv.getText().toString();
    info.bookname = titleEt.getText().toString();
    info.subtitle = introEt.getText().toString();
    HfxUtil.saveCommitInfo(this, info);
  }


  private void showExitDialog() {
    doSaveTemp();
    openMyWorkAndFinish(MyWorkActivity.TAB_MAKING);
  }

  void doTranscode() {
    Utils.hideKeyboard(this, introEt);
    if (mp4File != null && mp4File.exists() && mp4File.length() > 0) {
      showDeterminateProgress("请耐心等候", "初始化编解码器...");
      HFXWorksUtil.startVideoTransCode(videoInfo.videoWidth, videoInfo.videoHeight,
          mp4File.getAbsolutePath(), upLoadTempFile.getAbsolutePath(),
          new VideoTransCallback() {

            @Override
            public void onTranscodeProgress(int currentTime) {
              if (videoInfo.duration <= currentTime) {

                updateDeterminateProgress("转码中..." + "(已处理" + currentTime / 1000 + "秒)", 0);
              } else {
                float i = currentTime / 1000f;
                String progressString = decimalFormat.format(i);
                int progress = (int) (100f * currentTime / videoInfo.duration);
                updateDeterminateProgress(
                    "转码中..." + "（" + progressString + "秒/"
                        + videoInfo.duration / 1000 + "秒）",
                    progress);
              }
            }

            @Override
            public void onTranscodeFinished(boolean success) {
              if (success && FileUtil.renameFile(upLoadTempFile, upLoadFile)) {
                doUpload();
              } else {
                setUploadEnable(false);
                upLoadTempFile.delete();
                hideDeterminateProgress();
                showErrorDialog("转码失败", false);
              }
            }
          });
    } else {
      FileUtil.deleteDir(videoDir);
      showErrorDialog("视频文件丢失", true);
    }
  }


  @Override
  public void onBackPressed() {
    showExitDialog();
  }

  private void doCommit() {
    long filSize = upLoadFile.length();
    MultipartBody.Builder builder = new MultipartBody.Builder();
    builder.addFormDataPart("content_type", "freevideo");
    builder.addFormDataPart("introduce", introduce);
    builder.addFormDataPart("bookid", strings[1] + "_" + strings[2]);
    builder.addFormDataPart("file_size", String.valueOf(filSize));
    builder.addFormDataPart("title", title);
    builder.addFormDataPart("subtype", subtype);
    builder.addFormDataPart("persistent_id", persistentId);
    if (classInfo != null) {
      if (!TextUtils.isEmpty(classInfo.transmissionParam)) {
        builder.addFormDataPart("transmissionparam", classInfo.transmissionParam);
      }
      if (classInfo.classCheck != 0) {
        builder.addFormDataPart("classcheckresult", classInfo.classCheck + "");
      }
    }
    builder.setType(MultipartBody.FORM);
    MultipartBody multipartBody = builder.build();
    ApiHandler.getBaseApi()
        .uploadWorkInfo(multipartBody)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DefaultSubscriber<BaseNetResult>() {
          @Override
          public void onComplete() {

          }

          @Override
          public void onError(Throwable e) {
            e.printStackTrace();
            setUploadEnable(false);
            hideDeterminateProgress();
            showErrorDialog("作品提交失败,请重试", false);
          }

          @Override
          public void onNext(BaseNetResult result) {
            hideDeterminateProgress();
            if (result != null && result.errcode == 0) {
              toast("提交成功！");
              FileUtil.deleteDir(videoDir);
              openMyWorkAndFinish(MyWorkActivity.TAB_CHECKING);
            } else if (result != null && result.errcode == 1001) {
              setUploadEnable(false);
              login();
              finish();
            } else {
              showErrorDialog("作品提交失败,请重试", false);
              setUploadEnable(false);
            }
          }
        });
  }

  private class Adapter extends RecyclerView.Adapter<ViewHolder> {


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(SaveVideoActivity.this)
          .inflate(R.layout.hfx_category_item, parent, false);

      return new ViewHolder(v, SaveVideoActivity.this);
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

    ViewHolder(View view, final SaveVideoActivity activity) {
      super(view);
      title = view.findViewById(R.id.title);
      title.setOnClickListener(v -> activity.hideCategory(position));

    }
  }


}


