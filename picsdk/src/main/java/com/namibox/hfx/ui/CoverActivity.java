package com.namibox.hfx.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import com.example.picsdk.R;
import com.namibox.commonlib.activity.BaseActivity;
import com.namibox.hfx.utils.HfxFileUtil;
import com.namibox.hfx.utils.HfxPreferenceUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sunha on 2016/2/2 0002.
 */
public class CoverActivity extends BaseActivity {

  public static final String TAG = "CorverActivity";
  public static final String VIDEO_ID = "VIDEO_ID";
  public static final int RESULT_ERROR = 123;
  public static final String RESULT_IMGPATH = "RESULT_IMGPATH";
  public static final String RESULT_TIME = "RESULT_TIME";
  protected Uri contentUri;
  protected File mp4File;
  protected File photoFile;
  protected int mDuration;
  protected String videoId;
  SeekBar chooserbar;
  ImageView corverImg;
  private MediaMetadataRetriever retriever;
  private int coverTime;
  private Bitmap resizeBmp;
  private CorverHandler handler;
  private boolean isGettingBitmap;


  public static void openCoverActivity(Activity context, String videoId, int requestCode) {
    Intent intent;
    intent = new Intent(context, CoverActivity.class);
    intent.putExtra(VIDEO_ID, videoId);
    context.startActivityForResult(intent, requestCode);
  }

  @Override
  protected void setThemeColor() {
    super.setThemeColor();
    statusbarColor = toolbarColor = ContextCompat.getColor(this, R.color.hfx_gray_bg);
    toolbarContentColor = ContextCompat.getColor(this, R.color.hfx_white);
    darkStatusIcon = false;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.hfx_activity_select_corver);
    initView();
    Intent intent = getIntent();
    handler = new CorverHandler();
    videoId = intent.getStringExtra(VIDEO_ID);
    if (videoId == null) {
      setResult(RESULT_ERROR);
      finish();
    } else {
      mp4File = HfxFileUtil.getVideoFile(this, videoId);
      photoFile = HfxFileUtil.getCoverFile(this, videoId);
      if (mp4File == null || !mp4File.exists()) {
        setResult(RESULT_ERROR);
        finish();
      } else {
        contentUri = Uri.parse(mp4File.getAbsolutePath());
        coverTime = HfxPreferenceUtil.getVideoCoverTime(this, videoId);
        setTitle("选择封面");
        setMenu("确定", false, new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            done();
          }
        });
        retriever = new MediaMetadataRetriever();
        String time;
        try {
          retriever.setDataSource(mp4File.getAbsolutePath());
          time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
          //保留尾部1秒不给取
          mDuration = Integer.parseInt(time) - 1000;
          // 取得视频的长度(单位为毫秒)
        } catch (Exception e) {
          e.printStackTrace();
          //9秒,少一秒
          mDuration = 9000;
        }

        chooserbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {

          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            coverTime = (int) (progress / 10000f * mDuration);
            getBitmapsFromVideo(coverTime);


          }
        });
        if (coverTime > 0) {
          chooserbar.setProgress((int) (coverTime * 10000f / mDuration));
          getBitmapsFromVideo(coverTime);
        } else {
          coverTime = 0;
          getBitmapsFromVideo(0);
        }

      }
    }
  }

  private void initView() {
    chooserbar = findViewById(R.id.chooserbar);
    corverImg = findViewById(R.id.corverImg);
  }

  private void done() {
    if (resizeBmp != null) {
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(photoFile);
        resizeBmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        fos.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      HfxPreferenceUtil.saveVideoCoverTime(this, videoId, coverTime);
      Intent intent = new Intent();
      intent.putExtra(RESULT_TIME, coverTime);
      intent.putExtra(RESULT_IMGPATH, photoFile.getAbsolutePath());
      setResult(Activity.RESULT_OK, intent);
      finish();
    } else {
      Intent intent = new Intent();
      setResult(RESULT_ERROR, intent);
      finish();
    }
  }

  @Override
  public void onBackPressed() {
    setResult(Activity.RESULT_CANCELED);
    finish();
  }

  public void getBitmapsFromVideo(final int coverTime) {
    if (coverTime >= 0) {
      if (!isGettingBitmap) {
        new Thread(new Runnable() {
          @Override
          public void run() {
            isGettingBitmap = true;
            Bitmap bitmap = retriever.getFrameAtTime(coverTime * 1000);
            Matrix matrix = new Matrix();
            matrix.postScale(0.4f, 0.4f); //长和宽放大缩小的比例
            if (bitmap != null) {
              resizeBmp = Bitmap
                  .createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
            if (bitmap != null && !bitmap.isRecycled()) {
              bitmap.recycle();
              bitmap = null;
            }
            isGettingBitmap = false;
            handler.sendEmptyMessage(coverTime);
          }
        }).start();
      }

    }


  }


  class CorverHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      loadBitmap();
      int time = msg.what;
      if (time != coverTime) {
        getBitmapsFromVideo(coverTime);
      }
    }
  }

  private void loadBitmap() {
    if (resizeBmp != null) {
      corverImg.setImageBitmap(resizeBmp);
    }
  }


}
