package com.namibox.hfx.ui;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import com.example.picsdk.R;
import com.namibox.commonlib.activity.BaseActivity;
import com.namibox.hfx.bean.ClassInfo;
import com.namibox.hfx.bean.EvalBody;
import com.namibox.hfx.bean.UploadInfo;
import com.namibox.hfx.utils.HFXWorksUtil;
import com.namibox.hfx.utils.HFXWorksUtil.ZipAndOssCallback;
import com.namibox.hfx.utils.HfxFileUtil;
import com.namibox.hfx.utils.HfxPreferenceUtil;
import com.namibox.hfx.utils.HfxUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import io.reactivex.disposables.Disposable;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by sunhapper on 2016/6/6 0006.
 */
public abstract class BaseCommitActivity extends BaseActivity {

  private static final String TAG = "BaseCommitActivity";
  protected String workId;
  protected String contentType;
  protected String introduce;
  protected String type;
  protected int duration;
  //绘本
//    protected Huiben book;
  //MP3
  protected File mp3File;
  //封面
  protected File photoFile;
  //private File zip;
  //MP3文件名分割的数组
  protected String[] strings;
  protected String audioTitle;
  protected String subType;
  //提交作品
  protected boolean isCommiting;
  protected ClassInfo classInfo;

  private Disposable uploadDisposable;

  private void commitWork(final String workType) {
    UploadInfo uploadInfo = HfxUtil.getDirectUploadInfo(this, workId);
    commitWork(uploadInfo != null && uploadInfo.direct_upload, workType);
  }

  private void commitWork(boolean directUpload, final String workType) {
    if (directUpload && workType.equals(HfxFileUtil.HUIBEN_WORK)) {
      toast("不支持的上传方式");
      return;
    }
    if (isCommiting) {
      toast("正在上传作品,请稍候");
      return;
    }
    if (uploadDisposable != null && !uploadDisposable.isDisposed()) {
      uploadDisposable.dispose();
    }
    isCommiting = true;
    showDeterminateProgress("提交作品", "正在打包作品...", "取消",
        new OnClickListener() {
          @Override
          public void onClick(View view) {
            if (uploadDisposable != null && !uploadDisposable.isDisposed()) {
              uploadDisposable.dispose();
              uploadDisposable = null;
            }
            isCommiting = false;
          }
        });
    if (directUpload) {
      uploadDisposable = HFXWorksUtil
          .startUploadAudioToOss(this, duration, workId, workType, introduce, null, contentType,
              strings, audioTitle, subType, classInfo, new UploadCallback(workType, true));
    } else {
      uploadDisposable = HFXWorksUtil
          .startZipAndUploadToOss(this, workId, workType, introduce, null, contentType,
              strings, audioTitle, subType, classInfo, false,
              new UploadCallback(workType, false));
    }

  }


  private class UploadCallback implements ZipAndOssCallback {

    private String workType;
    private boolean withoutZip;

    public UploadCallback(String workType, boolean withoutZip) {
      this.workType = workType;
      this.withoutZip = withoutZip;
    }

    @Override
    public void onError(String error) {
      hideDeterminateProgress();
      isCommiting = false;
      if (Utils.isDev(BaseCommitActivity.this)) {
        showErrorDialog(error, false);

      } else {
        showErrorDialog("上传作品失败", false);

      }

    }

    @Override
    public void onLoginError() {
      hideDeterminateProgress();
      isCommiting = false;
      login();
    }

    @Override
    public void onZipProgress(int current, int total) {
      updateDeterminateProgress("正在打包..." + "[" + current + "/" + total + "]",
          100 * current / total);
    }

    @Override
    public void onUploadProgress(String mediaType, long current, long total) {
      String c = Utils.formatLengthString(current);
      String t = Utils.formatLengthString(total);
      updateDeterminateProgress("正在上传" + mediaType
              + "..." + "[" + c + "/" + t + "]",
          (int) (100 * current / total));
    }

    @Override
    public void onSuccess() {
      isCommiting = false;
      hideDeterminateProgress();
      if (HfxFileUtil.HUIBEN_WORK.equals(workType)) {
        String user_id = Utils.getLoginUserId(BaseCommitActivity.this);
        //提交成功，肯定不在制作中,如果会有矛盾的场景，已提交的作品肯定在审核未过中，所以状态设置为true
        HfxPreferenceUtil
            .setRecordBookInWork(BaseCommitActivity.this, user_id, workId, true);
        toast("提交成功！");
      } else if (HfxFileUtil.AUDIO_WORK.equals(workType)) {
        HfxUtil.deleteWork(BaseCommitActivity.this, workId);
      }
      openMyWorkAndFinish(MyWorkActivity.TAB_CHECKING);
    }

  }


  public void startCommit(String introduce, final String workType) {
    this.introduce = introduce;
    commitWork(workType);
  }


  protected void openMyWorkAndFinish(final int tab) {
    boolean needTip = PreferenceUtil.getSharePref(this, "needWorkTips", true);
    if (needTip) {
      new AlertDialog.Builder(this)
          .setView(R.layout.hfx_layout_worker_tip)
          .setCancelable(false)
          .setPositiveButton("确定", (dialog, which) -> {
            MyWorkActivity.openMyWork(BaseCommitActivity.this, tab);
            finish();
          })
          .setNegativeButton("不再提示", (dialog, which) -> {
            PreferenceUtil.setSharePref(BaseCommitActivity.this, "needWorkTips", false);
            MyWorkActivity.openMyWork(BaseCommitActivity.this, tab);
            finish();
          })
          .create().show();
    } else {
      MyWorkActivity.openMyWork(this, tab);
      finish();
    }
  }

  protected ArrayList<EvalBody> getEvalBody() {
    return null;
  }

}
