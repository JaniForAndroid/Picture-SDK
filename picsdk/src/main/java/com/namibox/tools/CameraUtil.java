package com.namibox.tools;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.Surface;
import com.namibox.util.Logger;

/**
 * Created by sunha on 2017/12/22 0022.
 */

public class CameraUtil {

  private static final String TAG = "CameraUtil";

  /**
   * 获取照相机旋转角度
   */
  public static int getCameraAngle(Activity activity, int cameraId) {
    int rotateAngle;
    CameraInfo info = new CameraInfo();
    Camera.getCameraInfo(cameraId, info);
    int screenRotation = activity.getRequestedOrientation();
    Logger.i(TAG, "screenRotation:camera " + screenRotation);
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    Logger.i(TAG, "rotation:camera " + rotation);
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
    Logger.i(TAG, "degrees:camera " + degrees);
    if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
      rotateAngle = (info.orientation + degrees) % 360;
      // compensate the mirror
      rotateAngle = (360 - rotateAngle) % 360;
    } else { // back-facing
      rotateAngle = (info.orientation - degrees + 360) % 360;
    }
    Logger.i(TAG, "rotateAngle:camera " + rotateAngle);
    return rotateAngle;
  }

  public static int getCameraAngle(int orientation, int cameraId) {
    int rotateAngle;
    CameraInfo info = new CameraInfo();
    Camera.getCameraInfo(cameraId, info);
    Logger.i(TAG, "orientation:camera " + orientation);
    int degrees = 0;
    switch (orientation) {
      case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
        degrees = 0;
        break;
      case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
        degrees = 90;
        break;
      case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
        degrees = 180;
        break;
      case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
        degrees = 270;
        break;
      default:
        break;
    }
    Logger.i(TAG, "degrees:camera " + degrees);
    if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
      rotateAngle = (info.orientation + degrees) % 360;
      // compensate the mirror
      rotateAngle = (360 - rotateAngle) % 360;
    } else { // back-facing
      rotateAngle = (info.orientation - degrees + 360) % 360;
    }
    Logger.i(TAG, "rotateAngle:camera " + rotateAngle);
    return rotateAngle;
  }


}
