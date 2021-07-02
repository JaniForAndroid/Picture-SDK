// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.namibox.imageselector.cropper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.picsdk.R;
import com.namibox.imageselector.BaseBuryPointActivity;
import java.io.File;
import java.io.IOException;

/**
 * Built-in activity for image cropping.<br>
 * Use {@link CropImage#activity(Uri)} to create a builder to start this activity.
 */
public class AvatarCropActivity extends BaseBuryPointActivity implements
    CropImageView.OnSetImageUriCompleteListener, CropImageView.OnSaveCroppedImageCompleteListener,
    OnClickListener {

  /**
   * The crop image view library widget used in the activity
   */
  private CropImageView mCropImageView;

  /**
   * the options that were set for the crop image
   */
  private CropImageOptions mOptions;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.avatar_crop__activity);

    mCropImageView = findViewById(R.id.cropImageView);
    TextView tvCancel =  findViewById(R.id.tvCancel);
    TextView tvConfirm = findViewById(R.id.tvConfirm);
    ImageView ivRotate = findViewById(R.id.ivRotate);
    tvCancel.setOnClickListener(this);
    tvConfirm.setOnClickListener(this);
    ivRotate.setOnClickListener(this);

    Intent intent = getIntent();
    Uri source = intent.getParcelableExtra(CropImage.CROP_IMAGE_EXTRA_SOURCE);
    mOptions = intent.getParcelableExtra(CropImage.CROP_IMAGE_EXTRA_OPTIONS);

    if (savedInstanceState == null) {
      mCropImageView.setImageUriAsync(source);
    }
  }

  @Override
  protected String getPage() {
    return "编辑界面";
  }

  @Override
  protected void getChannel() {
    channel = "头像";
  }

  @Override
  public void onClick(View v) {
    int viewId = v.getId();
    if (viewId == R.id.tvCancel) {
      onButtonClick("取消");
      onBackPressed();
    } else if (viewId == R.id.tvConfirm) {
      onButtonClick("完成");
      cropImage();
    } else if (viewId == R.id.ivRotate) {
      rotateImage();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    mCropImageView.setOnSetImageUriCompleteListener(this);
    mCropImageView.setOnSaveCroppedImageCompleteListener(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    mCropImageView.setOnSetImageUriCompleteListener(null);
    mCropImageView.setOnSaveCroppedImageCompleteListener(null);
  }


  @Override
  public void onBackPressed() {
    super.onBackPressed();
    setResultCancel();
  }

  @Override
  public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
    if (error == null) {
      if (mOptions.initialCropWindowRectangle != null) {
        mCropImageView.setCropRect(mOptions.initialCropWindowRectangle);
      }
      if (mOptions.initialRotation > -1) {
        mCropImageView.setRotatedDegrees(mOptions.initialRotation);
      }
    } else {
      setResult(null, error);
    }
  }

  @Override
  public void onSaveCroppedImageComplete(CropImageView view, Uri uri, Exception error) {
    setResult(uri, error);
  }

  //region: Private methods

  /**
   * Execute crop image and save the result tou output uri.
   */
  protected void cropImage() {
    if (mOptions.noOutputImage) {
      setResult(null, null);
    } else {
      Uri outputUri = getOutputUri();
      mCropImageView.saveCroppedImageAsync(outputUri,
          mOptions.outputCompressFormat,
          mOptions.outputCompressQuality,
          mOptions.outputRequestWidth,
          mOptions.outputRequestHeight);
    }
  }

  /**
   * Rotate the image in the crop image view.
   */
  protected void rotateImage() {
    mCropImageView.rotateImage(90);
  }

  /**
   * Get Android uri to save the cropped image into.<br>
   * Use the given in options or create a temp file.
   */
  protected Uri getOutputUri() {
    Uri outputUri = mOptions.outputUri;
    if (outputUri.equals(Uri.EMPTY)) {
      try {
        String ext = mOptions.outputCompressFormat == Bitmap.CompressFormat.JPEG ? ".jpg" :
            mOptions.outputCompressFormat == Bitmap.CompressFormat.PNG ? ".png" : ".wepb";
        outputUri = Uri.fromFile(File.createTempFile("cropped", ext, getCacheDir()));
      } catch (IOException e) {
        throw new RuntimeException("Failed to create temp file for output image", e);
      }
    }
    return outputUri;
  }

  /**
   * Result with cropped image data or error if failed.
   */
  protected void setResult(Uri uri, Exception error) {
    int resultCode = error == null ? RESULT_OK : CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
    setResult(resultCode, getResultIntent(uri, error));
    finish();
  }

  /**
   * Cancel of cropping activity.
   */
  protected void setResultCancel() {
    setResult(RESULT_CANCELED);
    finish();
  }

  /**
   * Get intent instance to be used for the result of this activity.
   */
  protected Intent getResultIntent(Uri uri, Exception error) {
    CropImage.ActivityResult result = new CropImage.ActivityResult(uri,
        error,
        mCropImageView.getCropPoints(),
        mCropImageView.getCropRect(),
        mCropImageView.getRotatedDegrees());
    Intent intent = new Intent();
    intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result);
    //回传图片是否被裁剪过的布尔值
    intent.putExtra("hasCropped", error == null);
    return intent;
  }

  /**
   * Update the color of a specific menu item to the given color.
   */
  private void updateMenuItemIconColor(Menu menu, int itemId, int color) {
    MenuItem menuItem = menu.findItem(itemId);
    if (menuItem != null) {
      Drawable menuItemIcon = menuItem.getIcon();
      if (menuItemIcon != null) {
        try {
          menuItemIcon.mutate();
          menuItemIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
          menuItem.setIcon(menuItemIcon);
        } catch (Exception e) {
        }
      }
    }
  }
  //endregion
}

