package com.namibox.imageselector.camera;

import static com.namibox.imageselector.ImageSelectorActivity.EXTRA_ADD_WATERMASK;
import static com.namibox.imageselector.ImageSelectorActivity.EXTRA_SIZE;
import static com.namibox.imageselector.ImageSelectorActivity.REQUEST_CODE;
import static com.namibox.imageselector.ImageSelectorActivity.REQUEST_CROP;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout.LayoutParams;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.example.picsdk.R;
import com.namibox.commonlib.event.ImageSelectResultEvent;
import com.namibox.commonlib.model.Result;
import com.namibox.imageselector.BaseBuryPointActivity;
import com.namibox.imageselector.ImageSelectorActivity;
import com.namibox.imageselector.camera.CameraFragment.OnCameraSwitchListener;
import com.namibox.imageselector.cropper.AvatarCropActivity;
import com.namibox.imageselector.cropper.CropImage;
import com.namibox.imageselector.cropper.CropImageView;
import com.namibox.tools.ViewUtil;
import com.namibox.util.ImageUtil;
import com.namibox.util.Logger;
import com.namibox.util.Utils;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import org.greenrobot.eventbus.EventBus;

/**
 * @author Shelter
 */
public class AvatarCameraActivity extends BaseBuryPointActivity implements OnClickListener {

  private static final String TAG = "Shelter";
  private static final int THRESHOLD = 30;
  private Camera mCamera;
  private CameraFragment cameraFragment;
  private int cameraId;
  private OrientationEventListener orientationEventListener;
  private ImageView ivTakePic;
  private ImageView ivGallery;
  private int mOrientation = -1;
  private View ivClose;
  private ImageView ivLight;
  private int currentDegree;
  private Bitmap srcBitmap;
  private int imageSize;
  private int requestCode;
  private boolean addWaterMask;
  private Disposable fixDisposable;
  private Disposable compressDisposable;
  private String filePath;
  private String timeStamp;
  private ImageView ivSwitch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_avatar_camera);
    Intent intent = getIntent();
    cameraId = intent.getIntExtra("cameraId", -1);
    requestCode = intent.getIntExtra(REQUEST_CODE, -1);
    addWaterMask = intent.getBooleanExtra(EXTRA_ADD_WATERMASK, false);
    timeStamp = String.valueOf(System.currentTimeMillis());
    if (cameraId == -1) {
      finish();
      return;
    }
    imageSize = intent.getIntExtra(EXTRA_SIZE, -1);
    try {
      mCamera = Camera.open(cameraId);
    } catch (Exception e) {
      Logger.d("CameraActivity", "open camera exception: " + e.getMessage());
      e.printStackTrace();
      finish();
      return;
    }
    if (mCamera == null) {
      finish();
      return;
    }
    initView();
    initFragment(cameraId);
    orientationEventListener = new IOrientationEventListener(this);
  }

  //获取与指定宽高相等或最接近的尺寸
  private Size getBestSize(int targetWidth, int targetHeight, List<Size> sizeList) {
    Size bestSize = null;
    //目标大小的宽高比
    float targetRatio = 1.0f * targetHeight / targetWidth;
    float minDiff = targetRatio;
    for (Size size : sizeList) {
      float supportedRatio = (1.0f * size.width / size.height);
      Logger.d(TAG, "系统支持的尺寸 : width = " + size.width + ", height = " + size.height + ", ratio = "
          + supportedRatio);
    }

    for (Size size : sizeList) {
      if (size.width == targetHeight && size.height == targetWidth) {
        bestSize = size;
        break;
      }
      float supportedRatio = (1.0f * size.width) / size.height;
      if (Math.abs(supportedRatio - targetRatio) < minDiff) {
        minDiff = Math.abs(supportedRatio - targetRatio);
        bestSize = size;
      }
    }
    Logger.d(TAG, "目标尺寸 ：targetRatio = " + targetRatio + ", width = " + targetWidth + ", height = "
        + targetHeight);
    if (bestSize != null) {
      Logger.d(TAG,
          "最优尺寸 ：bestSize width = " + bestSize.width + ", height =" + bestSize.height + ", ratio = "
              + (1.0f * bestSize.width / bestSize.height));
    }
    return bestSize;
  }

  @Override
  protected void onStart() {
    super.onStart();
    orientationEventListener.enable();
  }

  @Override
  protected void onStop() {
    super.onStop();
    orientationEventListener.disable();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (fixDisposable != null && !fixDisposable.isDisposed()) {
      fixDisposable.dispose();
    }
    if (compressDisposable != null && !compressDisposable.isDisposed()) {
      compressDisposable.dispose();
    }
  }

  private void initView() {
    FrameLayout cameraContainer = findViewById(R.id.cameraContainer);
    int[] screenWidth = Utils.getScreenWidth(this);
    int dimension = Math.min(screenWidth[0], screenWidth[1]);
    Size bestSize = getBestSize(dimension, dimension,
        mCamera.getParameters().getSupportedPreviewSizes());
    LayoutParams lp = (LayoutParams) cameraContainer.getLayoutParams();
    lp.dimensionRatio = Utils.format("h,%d:%d", bestSize.width, bestSize.height);
    cameraContainer.setLayoutParams(lp);
    ivSwitch = findViewById(R.id.ivSwitch);
    ivClose = findViewById(R.id.ivClose);
    ViewUtil.expandTouchArea(ivClose, 20);
    ivTakePic = findViewById(R.id.ivTakePic);
    ivGallery = findViewById(R.id.ivGallery);
    ivLight = findViewById(R.id.ivLight);
    ivClose.setOnClickListener(this);
    ivTakePic.setOnClickListener(this);
    ivGallery.setOnClickListener(this);
    ivLight.setOnClickListener(this);
    ivSwitch.setOnClickListener(this);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    Logger.d(TAG, "onConfigurationChanged: orientation = " + newConfig.orientation);
  }


  @Override
  public void onClick(View view) {
    int viewId = view.getId();
    if (viewId == R.id.ivClose) {
      finish();
    }
    if (viewId == R.id.ivTakePic) {
      if (!cameraFragment.safeToTakePic) {
        return;
      }
      onButtonClick("拍照");
//      checkPreviewRotation();
      cameraFragment.safeToTakePic = false;
      cameraFragment.takePicture(new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
          int[] screenWidth = getScreenWidth();
          Logger.d(TAG, "screen width = " + screenWidth[0] + ", screen height = " + screenWidth[1]);
          srcBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
          if (cameraFragment.isFrontCamera()) {
            srcBitmap = turnCurrentLayer(srcBitmap, 1, -1);
          }
          compressBitmap();
        }
      });
    } else if (viewId == R.id.ivGallery) {
      onButtonClick("相册");
      openImageSelector();
    } else if (viewId == R.id.ivLight) {
      onButtonClick("手电筒");
      openOrCloseFlashLight();
    } else if (viewId == R.id.ivSwitch) {
      onButtonClick("前后置摄像头");
      cameraFragment.switchCamera();
    }
  }

  @Override
  public void onImageSelectResult(ImageSelectResultEvent event) {
    finish();
  }

  private void fixPhotoFile(final String filePath) {
    showProgress("图片处理中...");
    Observable.fromCallable(new Callable<Result>() {
      @Override
      public Result call() throws Exception {
        Result result = new Result();
        result.path = filePath;
        ExifInterface exifInterface = new ExifInterface(filePath);
        result.model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
        if (!TextUtils.isEmpty(result.model)) {
          result.model = result.model.replace('\"', ' ');
        }
        result.make = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
        result.dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
        String lo = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String la = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        //读取原图的角度并保存到新路径
        String degree = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);

        Bitmap bt = rotateBitmap(BitmapFactory.decodeFile(filePath),
            ImageUtil.getBitmapDegree(filePath));
        File fileBt = ImageUtil.compressBmpToFile(bt, 90, result.path);

        ExifInterface newExif;
        String imgDegree;
        if (fileBt != null) {
          imgDegree = "0";
        } else {
          imgDegree = degree;
        }
        newExif = new ExifInterface(result.path);
        newExif.setAttribute(ExifInterface.TAG_ORIENTATION, imgDegree);
        newExif.saveAttributes();
        String attribute = newExif.getAttribute(ExifInterface.TAG_ORIENTATION);
        Logger.i("TAG", "SaveTask doInBackground() attribute = " + attribute);
        if (!TextUtils.isEmpty(la) && !TextUtils.isEmpty(lo)) {
          result.gps = la + "," + lo;
        }

        //加水印
        if (addWaterMask) {
          Bitmap srcBmp = BitmapFactory.decodeFile(result.path);
          Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
          int srcBmpWidth = srcBmp.getWidth();
          int srcBmpHeight = srcBmp.getHeight();
          Bitmap waterBitmap = BitmapFactory
              .decodeResource(getResources(), R.drawable.ic_water_mask);
          int width = waterBitmap.getWidth();
          int height = waterBitmap.getHeight();
          //满足宽高的条件添加水印
          if (srcBmpWidth > 300 && srcBmpHeight > 200) {

            Bitmap dst = Bitmap.createBitmap(srcBmpWidth, srcBmpHeight, Bitmap.Config.ARGB_8888);
            //创建画布，画水印
            Canvas c = new Canvas(dst);
            RectF rectF = new RectF(0, 0, srcBmpWidth, srcBmpHeight);
            c.drawBitmap(srcBmp, null, rectF, paint);
            //获取水印的bitmap
            float maxSize = srcBmpWidth / 8;
            Matrix matrix = new Matrix();
            float scale = maxSize / width;
            matrix.postScale(scale, scale);
            Bitmap bitmap = Bitmap.createBitmap(waterBitmap, 0, 0, width, height, matrix, true);
            int newWidth = bitmap.getWidth();
            int newHeight = bitmap.getHeight();
            c.drawBitmap(bitmap,
                srcBmpWidth - newWidth - Utils.dp2px(AvatarCameraActivity.this, 5),
                srcBmpHeight - newHeight - Utils.dp2px(AvatarCameraActivity.this, 5), paint);
            //创建路径
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
                .format(new Date());
            String imageFileName = "edit_image_" + timeStamp + ".jpg";
            File file = new File(getCacheDir(), imageFileName);
            //更新路劲
            ImageUtil.compressBmpToFile(dst, CompressFormat.JPEG, 100, file);
            result.path = file.getPath();
            result.width = srcBmpWidth;
            result.height = srcBmpHeight;
            result.size = file.length();
          }
        }
        return result;
      }
    }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Result>() {
          @Override
          public void onSubscribe(Disposable d) {
            fixDisposable = d;
          }

          @Override
          public void onNext(Result result) {
            List<Result> results = new ArrayList<>();
            results.add(result);
            EventBus.getDefault().post(new ImageSelectResultEvent(results, requestCode));
            hideProgress();
            finish();
          }

          @Override
          public void onError(Throwable e) {
            e.printStackTrace();
          }

          @Override
          public void onComplete() {

          }
        });
  }

  public Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
    if (degrees == 0 || null == bitmap) {
      return bitmap;
    }
    Matrix matrix = new Matrix();
    matrix.postRotate(degrees);
    Bitmap bmp = Bitmap
        .createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    bitmap.recycle();
    return bmp;
  }

  /**
   * 翻转bitmap (-1,1)上下翻转  (1,-1)左右翻转
   */
  public Bitmap turnCurrentLayer(Bitmap srcBitmap, float sx, float sy) {
    // 创建缓存像素的位图
    Bitmap cacheBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(),
        Bitmap.Config.ARGB_8888);
    int w = cacheBitmap.getWidth();
    int h = cacheBitmap.getHeight();
    //使用canvas在bitmap上面画像素
    Canvas cv = new Canvas(cacheBitmap);
    //使用矩阵 完成图像变换
    Matrix mMatrix = new Matrix();
    mMatrix.postScale(sx, sy);
    Bitmap resultBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, w, h, mMatrix, true);
    cv.drawBitmap(resultBitmap,
        new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight()),
        new Rect(0, 0, w, h), null);
    return resultBitmap;
  }

  private void compressBitmap() {
    showProgress("图片处理中...");
    Observable.fromCallable(new Callable<Bitmap>() {
      @Override
      public Bitmap call() throws Exception {
        File directory = new File(Environment.getExternalStorageDirectory(), "namibox_img");
        if (!directory.exists()) {
          directory.mkdirs();
        }
        File file = new File(directory, timeStamp + ".png");
        Bitmap bitmap = rotateBitmap(srcBitmap, cameraFragment.getBitmapRotation());
        ImageUtil.compressBmpToFile(bitmap, CompressFormat.PNG, 100, file);
        filePath = file.getAbsolutePath();
        return bitmap;
      }
    }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Bitmap>() {
          @Override
          public void onSubscribe(Disposable d) {
            compressDisposable = d;
          }

          @Override
          public void onNext(Bitmap bitmap) {
            Logger.d(TAG,
                "bitmap width = " + bitmap.getWidth() + ", height = " + bitmap.getHeight());
            hideProgress();
            cameraFragment.safeToTakePic = true;
            cameraFragment.startPreview();
            //刷新相册
            MediaScannerConnection
                .scanFile(AvatarCameraActivity.this, new String[]{filePath}, null,
                    null);
            crop(filePath);
          }

          @Override
          public void onError(Throwable t) {
            t.printStackTrace();
            Logger.d("Shelter", "图片处理异常：" + t.getMessage());
            hideProgress();
            cameraFragment.safeToTakePic = true;
          }

          @Override
          public void onComplete() {

          }
        });
  }

  /**
   * 裁剪
   */
  private void crop(String path) {
    Uri uri = Uri.fromFile(new File(path));
    Log.e("TAG", "srcUri = " + uri.toString());
    Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "crop_image_tmp.jpg"));
    Log.e("TAG", "destinationUrl = " + destinationUri.toString());
    int minSize = Utils.dp2px(this, 100);
    CropImage.activity(uri)
        .setDestActivityClass(AvatarCropActivity.class)
        .setGuidelines(CropImageView.Guidelines.ON)
        .setOutputUri(destinationUri)
        .setCropShape(CropImageView.CropShape.RECTANGLE)
        .setOutputCompressFormat(CompressFormat.JPEG)
        .setOutputCompressQuality(100)
        .setAspectRatio(1, 1)
        .setFixAspectRatio(true)
        .setMinCropWindowSize(minSize, minSize)
        .setAutoZoomEnabled(true)
        .setMaxZoom(10)
        .start(this);
  }

  private void openImageSelector() {
    Intent intent = new Intent(this, ImageSelectorActivity.class);
    intent.putExtra(ImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
    intent.putExtra(ImageSelectorActivity.EXTRA_SHOW_CAMERA, false);
    intent.putExtra(ImageSelectorActivity.EXTRA_DIRECT_CROP, true);
    intent.putExtra(ImageSelectorActivity.EXTRA_ADD_WATERMASK, true);
    if (imageSize != -1) {
      intent.putExtra(EXTRA_SIZE, imageSize);
    }
    startActivityForResult(intent, REQUEST_CROP);
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if ((requestCode == REQUEST_CROP || requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)) {
      if (resultCode == RESULT_OK && intent != null) {
        CropImage.ActivityResult result = CropImage.getActivityResult(intent);
        String path = result.getUri().getPath();
        fixPhotoFile(path);
      } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
        Utils.toast(this, "无法编辑图片");
      }
    } else {
      super.onActivityResult(requestCode, resultCode, intent);
    }
  }

  /**
   * 获得屏幕宽高度
   */
  public int[] getScreenWidth() {
    WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics outMetrics = new DisplayMetrics();
    wm.getDefaultDisplay().getMetrics(outMetrics);
    return new int[]{outMetrics.widthPixels, outMetrics.heightPixels};
  }

  private void openOrCloseFlashLight() {
    try {
      Parameters parameters = mCamera.getParameters();
      String flashMode = parameters.getFlashMode();
      if (flashMode != null) {
        if (TextUtils.equals(flashMode, Parameters.FLASH_MODE_TORCH)) {
          parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
          ivLight.setImageResource(R.drawable.ic_avatar_photo_light);
        } else {
          parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
          ivLight.setImageResource(R.drawable.ic_avatar_photo_light_on);
        }
        mCamera.setParameters(parameters);
      }
    } catch (Exception e) {
      Logger.d("CameraActivity 打开或关闭闪光灯异常：" + e.getMessage());
      e.printStackTrace();
    }
  }

  private void initFragment(int cameraId) {
    cameraFragment = CameraFragment.newInstance();
    cameraFragment.setCamera(mCamera);
    cameraFragment.setCameraId(cameraId);
    cameraFragment.setOnCameraSwitchListener(new OnCameraSwitchListener() {
      @Override
      public void onCameraSwitch(Camera camera, int cameraPosition) {
        mCamera = camera;
        if (cameraPosition == 0) {
          ivLight.setImageResource(R.drawable.ic_avatar_photo_light);
        }
      }
    });
    getSupportFragmentManager().beginTransaction().add(R.id.cameraContainer, cameraFragment)
        .commitAllowingStateLoss();
  }

  private void animButtons(int degree) {
    Logger.d(TAG, "animButtons  currentDegree = " + currentDegree + ", destDegree = " + degree);
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.setInterpolator(new LinearInterpolator());
    List<Animator> collections = new ArrayList<>();
    collections.add(ObjectAnimator.ofFloat(ivClose, "rotation", currentDegree, degree));
    collections.add(ObjectAnimator.ofFloat(ivGallery, "rotation", currentDegree, degree));
    collections.add(ObjectAnimator.ofFloat(ivTakePic, "rotation", currentDegree, degree));
    collections.add(ObjectAnimator.ofFloat(ivLight, "rotation", currentDegree, degree));
    collections.add(ObjectAnimator.ofFloat(ivSwitch, "rotation", currentDegree, degree));

    animatorSet.setDuration(300);
    animatorSet.playTogether(collections);
    animatorSet.start();
    currentDegree = degree;
  }

  private void onOrientationUpdate(int orientation) {
    if (orientation >= (90 - THRESHOLD) && orientation <= (90 + THRESHOLD)) {
      if (mOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        mOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
//        currentDegree = 270;
        animButtons(-90);
      }
    } else if ((orientation >= (270 - THRESHOLD) && orientation <= (270 + THRESHOLD))) {
      if (mOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        //        currentDegree = 270;
        animButtons(90);
      }
    } else if (orientation >= (360 - THRESHOLD) || orientation <= THRESHOLD) {
      if (mOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        //        currentDegree = 270;
        animButtons(0);
      }
    } else if (orientation >= (180 - THRESHOLD) && orientation <= (180 + THRESHOLD)) {
      if (mOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        //        currentDegree = 270;
        animButtons(0);
      }
    }
  }

  @Override
  protected String getPage() {
    return "拍照界面";
  }


  class IOrientationEventListener extends OrientationEventListener {

    IOrientationEventListener(Context context) {
      super(context);
    }

    @Override
    public void onOrientationChanged(int orientation) {
      if (ORIENTATION_UNKNOWN == orientation) {
        return;
      }
      Camera.CameraInfo info = new Camera.CameraInfo();
      Camera.getCameraInfo(cameraId, info);
      orientation = (orientation + 45) / 90 * 90;
      int rotation = 0;
      if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        rotation = (info.orientation - orientation + 360) % 360;
      } else {
        rotation = (info.orientation + orientation) % 360;
      }
      Logger.d("Shelter",
          "orientation = " + orientation + ", rotation = " + rotation + ", info.orientation = "
              + info.orientation);
      onOrientationUpdate(orientation);
    }
  }

}
