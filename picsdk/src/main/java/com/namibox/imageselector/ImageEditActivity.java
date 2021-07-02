package com.namibox.imageselector;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.example.picsdk.R;
import com.namibox.commonlib.dialog.DialogUtil;
import com.namibox.commonlib.dialog.DialogUtil.Callback;
import com.namibox.commonlib.dialog.DialogUtil.LoadingDialog;
import com.namibox.imageselector.cropper.CropImage;
import com.namibox.imageselector.cropper.CropImageView;
import com.namibox.imageselector.view.PaintImageView;
import com.namibox.imageselector.view.VerticalColorSeekBar;
import com.namibox.imageselector.view.VerticalScrollPaint;
import com.namibox.util.FileUtil;
import com.namibox.util.ImageUtil;
import com.namibox.util.Utils;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by ryan on 2015/2/14.
 */
public class ImageEditActivity extends AppCompatActivity {

  private static final String TAG = "ImageEditActivity";
  private PaintImageView imageView;
  private VerticalScrollPaint size_picker;
  private VerticalColorSeekBar vpbColor;
  private String path;
  private boolean hasModify;
  private LoadBitmapTask mLoadBitmapTask;
  private LoadingDialog mDialog;
  //撤销按钮
  private ImageView backOut;
  private boolean isCurrentEdit;
  //当前模式的临时变量
  private PaintImageView.Mode lastMode = PaintImageView.Mode.PAINT;
  private PaintImageView.Mode mode = PaintImageView.Mode.PAINT;
  //    //图片是否做过裁剪的布尔值
//    private boolean hasCropped;
  static final String REG = "^[^\\ud83c\\udc00-\\ud83c\\udfff\\ud83d\\udc00-\\ud83d\\udfff\\u2600-\\u27ff]+$";


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (Utils.isTablet(this)) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    setResult(RESULT_CANCELED);
    setContentView(R.layout.activity_image_edit);
    imageView = (PaintImageView) findViewById(R.id.paintview);
    backOut = (ImageView) findViewById(R.id.iv_backout);
    backOut.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        backOut();
      }
    });
    //设置PaintImageView的监听，当手指接触屏幕时隐藏选择器，离开时显示
    imageView.setOnViewTouchListener(new PaintImageView.OnViewTouchListener() {
      @Override
      public void onViewTouch(boolean isTouching) {
        //如果不是涂鸦模式，那么让颜色选择器隐藏
        if (imageView.getMode() != PaintImageView.Mode.PAINT) {
          vpbColor.setVisibility(View.INVISIBLE);
        }
        if (isTouching) {
          size_picker.setVisibility(View.INVISIBLE);
          backOut.setVisibility(View.INVISIBLE);

          if (imageView.getMode() == PaintImageView.Mode.PAINT) {
            vpbColor.setVisibility(View.INVISIBLE);
          }

        } else {
          if (imageView.getMode() != PaintImageView.Mode.TEXT) {
            size_picker.setVisibility(View.VISIBLE);
          }
          if (imageView.getMode() == PaintImageView.Mode.PAINT) {
            vpbColor.setVisibility(View.VISIBLE);
          }
          backOut.setVisibility(imageView.getLines().size() == 0 ? View.INVISIBLE : View.VISIBLE);
        }
      }
    });
    vpbColor = (VerticalColorSeekBar) findViewById(R.id.vpb_color);
    vpbColor.setOnPaintColorChangedListener(new VerticalColorSeekBar.PaintColorChangedListener() {
      @Override
      public void onPaintColorChangedListener(int color, float y) {
        imageView.setPaintColor(color);
      }
    });
    imageView.setPaintColor(Color.RED);
    imageView.setPaintSize(40);
    imageView.setMode(PaintImageView.Mode.PAINT);
    imageView.setOnCleanPathListener(new PaintImageView.OnCleanPathListener() {
      @Override
      public void setBackOutGone() {
        backOut.setVisibility(View.INVISIBLE);
      }
    });
    imageView.setOnShowDialogListener(new PaintImageView.ShowDialogListener() {
      @Override
      public void showDialog(String content) {
        showEditDialog(content);
      }
    });
    findViewById(R.id.save_btn).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        hasModify = true;
        save();
      }
    });
    findViewById(R.id.cancel_btn).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

    RadioGroup navigation = (RadioGroup) findViewById(R.id.navigation);
    navigation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        lastMode = mode;
        if (checkedId == R.id.rb_paint) {
          mode = PaintImageView.Mode.PAINT;

        } else if (checkedId == R.id.rb_crop) {
          mode = PaintImageView.Mode.CROP;

        } else if (checkedId == R.id.rb_mosaic) {
          mode = PaintImageView.Mode.MOSAIC;

        } else if (checkedId == R.id.rb_frog) {
          mode = PaintImageView.Mode.FROG;

        } else if (checkedId == R.id.rb_text) {
          mode = PaintImageView.Mode.TEXT;
        }
        showSaveDialog();
      }
    });
    navigation.check(R.id.rb_paint);
    findViewById(R.id.rb_text).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (lastMode != mode) {
          lastMode = mode;
        } else {
          showEditDialog(null);
        }
      }
    });
    Intent intent = getIntent();
    path = intent.getStringExtra("path");
    startLoadBitmap();
    size_picker = (VerticalScrollPaint) findViewById(R.id.size_picker);
    size_picker.setOnProgressChangedListener(new VerticalScrollPaint.OnProgressChangedListener() {
      @Override
      public void onProgressChanged(int radius) {
        int paintSize = radius * 2;
        imageView.setPaintSize(paintSize);
      }
    });
    vpbColor.setVisibility(View.VISIBLE);
  }

  private void showEditDialog(String content) {
    DialogUtil.showEditDialog(ImageEditActivity.this,
        getString(R.string.picker_input_text),
        getString(R.string.add_text_hint), content,
        getString(R.string.picker_cancel),
        null,
        getString(R.string.picker_confirm),
        new Callback() {
          @Override
          public boolean onTextAccept(CharSequence text) {
            String trim = text.toString().trim();
            if (TextUtils.isEmpty(trim)) {
              Utils.toast(ImageEditActivity.this,
                  getString(R.string.picker_text_empty));
              return false;
            }
            if (trim.length() > 8) {
              Utils.toast(ImageEditActivity.this,
                  getString(R.string.picker_text_limit));
              return false;
            }
            if (!trim.matches(REG)) {
              Utils.toast(ImageEditActivity.this,
                  getString(R.string.picker_text_special));
              return false;
            }
            imageView.setText(trim);
            return true;
          }
        });
  }

  /**
   * 撤销按钮的回调方法
   */
  private void backOut() {
    imageView.backOut();
    backOut.setVisibility(imageView.getLines().size() == 0 ? View.INVISIBLE : View.VISIBLE);
  }


  /**
   * 重写回调方法，接受裁剪页面完成后的数据
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
      CropImage.ActivityResult result = CropImage.getActivityResult(data);
      if (data != null) {
        hasModify = data.getBooleanExtra("hasCropped", false);
      }
      if (resultCode == RESULT_OK) {
        path = result.getUri().getPath();
        startLoadBitmap();

      } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
        Utils.toast(this, getString(R.string.picker_edit_limit));
      }
      mode = PaintImageView.Mode.PAINT;
      imageView.setMode(mode);
//            navigation.check(R.id.rb_paint);
      ((RadioButton) findViewById(R.id.rb_paint)).setChecked(true);
      vpbColor.setVisibility(View.VISIBLE);
    }
  }

  @Override
  protected void onDestroy() {
    if (mLoadBitmapTask != null) {
      mLoadBitmapTask.cancel(false);
    }
    super.onDestroy();
  }

  private void startLoadBitmap() {
    if (path != null) {
      mLoadBitmapTask = new LoadBitmapTask();
      mLoadBitmapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      cannotLoadImage();
    }
  }

  private void doneLoadBitmap(Bitmap bitmap) {
    imageView.setVisibility(View.VISIBLE);
    if (bitmap != null && bitmap.getWidth() != 0 && bitmap.getHeight() != 0) {
      imageView.setBitmap(bitmap);
    } else {
      Log.w(TAG, "could not load image for cropping");
      cannotLoadImage();
    }
  }

  private void cannotLoadImage() {
    Utils.toast(this, getString(R.string.cannot_load_image));
    finish();
  }

  private void save() {
    if (imageView.getMode() != PaintImageView.Mode.NORMAL && imageView.hasModify()
        || imageView.getTextList().size() > 0) {
      hasModify = true;
      imageView.setBitmap(imageView.saveBitmap());
      vpbColor.setVisibility(View.INVISIBLE);
      imageView.setMode(PaintImageView.Mode.NORMAL);
    } else {
      //因为要添加水印,必须重新保存一下图片
      imageView.setBitmap(imageView.saveBitmap());
    }
    new SaveTask(imageView.getBitmap())
        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  /**
   * 裁剪
   */
  private void crop() {
    Uri uri = Uri.fromFile(new File(path));
    Log.e("TAG", "srcUri = " + uri.toString());
    Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "crop_image_tmp.jpg"));
    Log.e("TAG", "destinationUrl = " + destinationUri.toString());
    int minSize = Utils.dp2px(this, 100);
    CropImage.activity(uri)
        .setGuidelines(CropImageView.Guidelines.ON)
        .setOutputUri(destinationUri)
        .setCropShape(CropImageView.CropShape.RECTANGLE)
        .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
        .setOutputCompressQuality(90)
        .setMinCropWindowSize(minSize, minSize)
        .setAutoZoomEnabled(true)
        .setMaxZoom(4)
        .start(this);
  }

  private void showSaveDialog() {
    if (imageView.getMode() != mode) {
      if (imageView.hasModify()) {
        DialogUtil.showButtonDialog(this,
            getString(R.string.picker_tip),
            getString(R.string.picker_keep_change),
            getString(R.string.picker_confirm),
            new OnClickListener() {
              @Override
              public void onClick(View view) {
                hasModify = true;
                isCurrentEdit = true;
                Bitmap saveBitmap = imageView.saveBitmap();
                new SaveTask(saveBitmap)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                imageView.setBitmap(saveBitmap);
                //保存的时候清空文字的集合
                if (!imageView.getTextList().isEmpty()) {
                  imageView.getTextList().clear();
                }
                switchMode(true);
              }
            },
            getString(R.string.picker_cancel),
            new OnClickListener() {
              @Override
              public void onClick(View view) {
                if (!imageView.getTextList().isEmpty()) {
                  imageView.getTextList().clear();
                }
                switchMode(false);
              }
            },
            new OnClickListener() {
              @Override
              public void onClick(View v) {

              }
            });
      } else {
        switchMode(false);
      }
    }
  }

  private void switchMode(boolean save) {
    imageView.setMode(mode);
    if (!save && mode == PaintImageView.Mode.CROP) {
      crop();
    } else if (mode == PaintImageView.Mode.PAINT) {
      size_picker.setVisibility(View.VISIBLE);
      vpbColor.setVisibility(View.VISIBLE);
    } else if (mode == PaintImageView.Mode.TEXT) {
      showEditDialog(null);
      size_picker.setVisibility(View.INVISIBLE);
      vpbColor.setVisibility(View.INVISIBLE);
    } else {
      size_picker.setVisibility(View.VISIBLE);
      vpbColor.setVisibility(View.INVISIBLE);
    }
  }

  private int[] getScreenImageSize() {
    DisplayMetrics outMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
    return new int[]{outMetrics.heightPixels, outMetrics.widthPixels};
  }

  public class LoadBitmapTask extends AsyncTask<String, Void, Bitmap> {

    int[] mBitmapSize;
    Context mContext;

    public LoadBitmapTask() {
      mBitmapSize = getScreenImageSize();
      mContext = getApplicationContext();
    }

    @Override
    protected Bitmap doInBackground(String... params) {
      try {

        InputStream is = mContext.getContentResolver()
            .openInputStream(Uri.fromFile(new File(path)));
        if (is != null) {
          byte[] bytes = FileUtil.InputStreamToByte(is);
          is.close();
          return ImageUtil.decodeSampledBitmapFromBytes(bytes, mBitmapSize[0], mBitmapSize[1]);
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
      doneLoadBitmap(result);
    }
  }

  private void showProgress(String message) {
    mDialog = DialogUtil.showLoadingDialog(this, message, false, null);
  }

  private void hideProgress() {
    if (mDialog != null) {
      mDialog.dismissAllowingStateLoss();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (imageView.hasModify() || hasModify || imageView.getTextList().size() > 0) {
        DialogUtil.showButtonDialog(this,
            getString(R.string.picker_tip),
            getString(R.string.picker_give_up),
            getString(R.string.picker_give_up1),
            new OnClickListener() {
              @Override
              public void onClick(View view) {
                finish();
              }
            },
            getString(R.string.picker_cancel),
            null,
            v -> { });
        return true;
      }

    }
    return super.onKeyDown(keyCode, event);
  }

  private File createImageFile() {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
    String imageFileName = "edit_image_" + timeStamp + ".jpg";
    return new File(getCacheDir(), imageFileName);
  }

  private class SaveTask extends AsyncTask<Void, Void, Boolean> {

    File file;
    Bitmap bitmap;

    SaveTask(Bitmap bitmap) {
      this.bitmap = bitmap;
    }

    @Override
    protected void onPreExecute() {
      showProgress("正在保存…");
    }

    @Override
    protected Boolean doInBackground(Void... params) {

      try {
        Log.d(TAG, "hasModify=" + hasModify);
        if (hasModify) {
          file = createImageFile();
          //更改path路径
          path = file.getPath();
          ImageUtil.compressBmpToFile(bitmap, Bitmap.CompressFormat.JPEG, 100, file);
          if (!isCurrentEdit) {
            bitmap.recycle();
          }
        }
//                else {
////                    return Utils.copyFile(new File(path), file);
//                    return true;
//                }
        return true;
      } catch (Exception e) {
        e.printStackTrace();
      }
      return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
      hideProgress();

      if (result) {
        //如果用户点击的是保存按钮
        if (!isCurrentEdit) {
          Intent intent = getIntent();
//                    intent.putExtra("path", file.getAbsolutePath());
          intent.putExtra("path", path);
          intent.putExtra("hasModify", hasModify);
          setResult(RESULT_OK, intent);
        }

      } else {
        Utils.toast(ImageEditActivity.this, "保存失败");
      }
      //如果是点击的保存按钮，那么销毁当前页面
      if (!isCurrentEdit) {
        finish();
      } else { //如果不是，那么变量重置为false
        isCurrentEdit = false;
        if (mode == PaintImageView.Mode.CROP) {
          crop();
        }
      }

    }
  }
}
