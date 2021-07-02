package com.namibox.imageselector.camera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import com.namibox.util.Logger;
import java.io.IOException;
import java.util.List;

/**
 * @author: Shelter
 * Create time: 2019/9/3, 10:20.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

  private static final String TAG = "Shelter";
  private Camera mCamera;
  private int mCurrentCameraId;
  private SurfaceHolder mHolder;

  public CameraPreview(Context context, Camera camera, int cameraId,
      OnSurfaceCreatedListener onSurfaceCreatedListener) {
    super(context);
    mCamera = camera;
    mCurrentCameraId = cameraId;
    this.onSurfaceCreatedListener = onSurfaceCreatedListener;
    getHolder().addCallback(this);
  }

  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {
    mHolder = surfaceHolder;
    Logger.d(TAG, "surfaceCreated");
    startPreview(surfaceHolder);
    if (onSurfaceCreatedListener != null) {
      onSurfaceCreatedListener.onSurfaceCreated();
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    if (surfaceHolder.getSurface() == null) {
      return;
    }
    startPreview(surfaceHolder);
//    //停止预览效果
//    mCamera.stopPreview();
//    //重新设置预览效果
//    try {
//      mCamera.setPreviewDisplay(mHolder);
//      Logger.d(TAG, "surfaceChanged: mHolder = " + mHolder + ", surfaceHolder = " + surfaceHolder);
//      mCamera.startPreview();
//    } catch (Exception e) {
//      Logger.d("CameraPreview surfaceChanged error: " + e.getMessage());
//      e.printStackTrace();
//    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
  }

  private void startPreview(SurfaceHolder surfaceHolder) {
    try {
      mCamera.stopPreview();
      mCamera.setPreviewDisplay(surfaceHolder);
      final CameraInfo cameraInfo = new CameraInfo();
      Camera.getCameraInfo(mCurrentCameraId, cameraInfo);
      //获取相机参数
      final Parameters parameters = mCamera.getParameters();
      //设置对焦模式
      List<String> supportedFocusModes = parameters.getSupportedFocusModes();
      if (supportedFocusModes != null && supportedFocusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
        parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
      }
      //设置闪光模式
      List<String> supportedFlashModes = parameters.getSupportedFlashModes();
      if (supportedFlashModes != null && supportedFlashModes.contains(Parameters.FLASH_MODE_AUTO)) {
        parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
      }

      mCamera.setDisplayOrientation(getCameraAngle(mCurrentCameraId));

      int screenWidth = getScreenWidth()[0];
      int screenHeight = getScreenWidth()[1];
      int previewDimension = Math.min(screenWidth, screenHeight);
      Size bestPreviewSize = getBestSize(previewDimension, previewDimension,
          parameters.getSupportedPreviewSizes());
      int cameraWidth = bestPreviewSize.width;
      int cameraHeight = bestPreviewSize.height;
      Logger.d(TAG, "choose preview size width =" + cameraWidth + ", height = " + cameraHeight);
      parameters.setPreviewSize(cameraWidth, cameraHeight);
      //以屏幕宽高来计算最佳的图片尺寸 满足全屏预览的需求
      Size bestPictureSize = getBestSize(previewDimension, previewDimension,
          parameters.getSupportedPictureSizes());
      Logger.d(TAG, "use picture size width =" + bestPictureSize.width + ", height = "
          + bestPictureSize.height);
      parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);

      //设置相机参数
      mCamera.setParameters(parameters);
      //开启预览
      mCamera.startPreview();

    } catch (IOException error) {
      error.printStackTrace();
      Logger.d(TAG, "Error setting camera preview: " + error.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      Logger.d(TAG, "Error starting camera preview: " + e.getMessage());
    }
  }


  /**
   * 获取照相机旋转角度
   */
  private int getCameraAngle(int cameraId) {
    int rotateAngle;
    CameraInfo info = new CameraInfo();
    Camera.getCameraInfo(cameraId, info);
    int rotation = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
        .getDefaultDisplay()
        .getRotation();
    int degrees = 0;
    switch (rotation) {
      case Surface.ROTATION_0:
        degrees = 0;
        break;
      case Surface.ROTATION_90:
        degrees = 90;
        break;
      case Surface.ROTATION_180:
        degrees = 180;
        break;
      case Surface.ROTATION_270:
        degrees = 270;
        break;
      default:
        break;
    }

    if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
      rotateAngle = (info.orientation + degrees) % 360;
      // compensate the mirror
      rotateAngle = (360 - rotateAngle) % 360;
    } else { // back-facing
      rotateAngle = (info.orientation - degrees + 360) % 360;
    }

    return rotateAngle;
  }

  //获取与指定宽高相等或最接近的尺寸
  private Size getBestSize(int targetWidth, int targetHeight, List<Size> sizeList) {
    Size bestSize = null;
    float targetRatio = 1.0f * targetHeight / targetWidth;  //目标大小的宽高比
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

  /**
   * 获得屏幕宽高度
   */
  public int[] getScreenWidth() {
    WindowManager wm = (WindowManager) getContext()
        .getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics outMetrics = new DisplayMetrics();
    wm.getDefaultDisplay().getMetrics(outMetrics);
    return new int[]{outMetrics.widthPixels, outMetrics.heightPixels};
  }

  public void releaseCamera() {
    if (mCamera != null) {
      mCamera.setPreviewCallback(null);
      mCamera.stopPreview();
      mCamera.release();
      mCamera = null;
      getHolder().removeCallback(this);
    }
  }

  public interface OnSurfaceCreatedListener {

    void onSurfaceCreated();
  }

  public OnSurfaceCreatedListener onSurfaceCreatedListener;

}
