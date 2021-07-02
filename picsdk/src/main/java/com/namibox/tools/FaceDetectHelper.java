package com.namibox.tools;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.namibox.util.Logger;

/**
 * @author: Shelter
 * Create time: 2018/12/11, 13:52.
 */
public class FaceDetectHelper {
//
//  public static final int DEFAULT = 0;
//  public static final int TOO_CLOSE = 1;
//  public static final int HEAD_ASKEW = 2;
//  public static final int HEAD_TURN = 3;
//  public static final int SCREEN_DOWN = 4;
//  public static final int LEAVE = 5;
//  public static final int NOT_IMMEDIATELY = 5;
//  public static final int BUFFERING = 6;
//  public static final int PLAY_ERROR = 7;
//  public static final int BACK = 99;
//
//  public static final int ROTATE_0 = OrientationConfig.ROTATE_0;
//  public static final int ROTATE_90 = OrientationConfig.ROTATE_90;
//  public static final int ROTATE_180 = OrientationConfig.ROTATE_180;
//  public static final int ROTATE_270 = OrientationConfig.ROTATE_270;
//
//
//  private int mOrientation = -1;
//
//  private FaceCamera faceCamera;
//  private CameraPreview mPreview;
//
//  public void updatePreview(int orientation) {
//    String model = android.os.Build.MODEL;
//    if ("Lenovo TAB 2 A10-70F".equals(model)) {
//      orientation -= 90;
//      if (orientation < 0) {
//        orientation += 360;
//      }
//    }
//    if (this.mOrientation != orientation) {
//      setOrientation(orientation);
//      mOrientation = orientation;
//      if (checkNull()) {
//        mPreview.post(new Runnable() {
//          @Override
//          public void run() {
//            //防止onDestory时崩溃
//            Logger.i( "updatePreview, orientation=" + mOrientation);
//            setCameraAngle(CameraUtil.getCameraAngle(mOrientation, getCameraId()));
//          }
//        });
//      }
//    }
//  }
//
//  public boolean init(Activity activity) {
//    if (faceCamera == null) {
//      faceCamera = new FaceCamera(500);
//    }
//    return init(activity, null);
//  }
//
//  public boolean init(Activity activity, AspectRatioFrameLayout aspectRatioFrameLayout) {
//    if (faceCamera == null) {
//      faceCamera = new FaceCamera(500);
//    }
//    if (aspectRatioFrameLayout == null) {
//      if (faceCamera.init(activity)) {
//        initOrientationSensor(activity);
//        return true;
//      } else {
//        return false;
//      }
//    }
//    if (faceCamera.init(activity)) {
//      if (!hasCameraPreview()) {
//        newCameraPreview(activity);
//        setDefaultWidthAndHeight(640, 480);
//        setZOrderMediaOverlay(true);
//        addCameraPreview(aspectRatioFrameLayout);
//      } else {
//        setCamera();
//      }
//      initOrientationSensor(activity);
//      return true;
//    } else {
//      return false;
//    }
//  }
//
//  public void setSupportOrientationConfig(int config) {
//    if (faceCamera != null)
//      faceCamera.setOrientationConfig(config);
//  }
//
//
//  private void initOrientationSensor(Activity activity){
//    updatePreview(activity.getRequestedOrientation());
//    OrientationSensor.getInstance().start(activity, new OrientationListener() {
//      @Override
//      public void orientation(int degree) {
//        if (degree == 0) {
//          updatePreview(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        } else if (degree == 90) {
//          updatePreview(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//        } else if(degree == 180){
//          updatePreview(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
//        } else if (degree == 270) {
//          updatePreview(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
//      }
//    });
//  }
//
//  public void addCameraPreview(AspectRatioFrameLayout aspectRatioFrameLayout) {
//    if (mPreview != null) {
//      aspectRatioFrameLayout.addView(mPreview);
//    }
//  }
//
//  public Camera getCamera() {
//    if (faceCamera == null) {
//      return null;
//    }
//    return faceCamera.getCamera();
//  }
//
//  public boolean hasCamera() {
//    return faceCamera != null;
//  }
//
//  public boolean hasCameraPreview() {
//    return mPreview != null;
//  }
//
//  public void newCameraPreview(Context context) {
//    mPreview = new CameraPreview(context, faceCamera);
//  }
//
//  public void setDefaultWidthAndHeight(int width, int height) {
//    if (mPreview != null) {
//      mPreview.setDefaultWidthAndHeight(width, height);
//    }
//  }
//
//  public void setZOrderMediaOverlay(boolean isMediaOverlay) {
//    if (mPreview != null) {
//      mPreview.setZOrderMediaOverlay(isMediaOverlay);
//    }
//  }
//
//  public void postRunnable(final int angle) {
//    if (mPreview != null && faceCamera != null) {
//      mPreview.post(new Runnable() {
//        @Override
//        public void run() {
//          //防止onDestory时崩溃
//          mPreview.setCameraAngle(angle);
//        }
//      });
//    }
//  }
//
//  public void setCamera() {
//    if (mPreview != null && faceCamera != null) {
//      mPreview.setCamera(faceCamera);
//    }
//  }
//
//  public boolean checkNull() {
//    return mPreview != null && faceCamera != null;
//  }
//
//
//  public void setCameraAngle(int angle) {
//    if (mPreview != null && faceCamera != null) {
//      mPreview.setCameraAngle(angle);
//    }
//  }
//
//  public void resumeDetect() {
//    if (hasCamera()) {
//      faceCamera.resumeDetect();
//    }
//  }
//
//  public void pauseDetect() {
//    if (faceCamera != null) {
//      faceCamera.PauseDetect();
//    }
//  }
//
//  public void stopCamera() {
//    if (faceCamera != null) {
//      faceCamera.stopPreview();
//      faceCamera.setPreviewCallback(null);
//      faceCamera.release();
//      faceCamera = null;
//    }
//    OrientationSensor.getInstance().stop();
//  }
//
//  public void stopOrientationSensor(){
//    OrientationSensor.getInstance().stop();
//  }
//
//  public int getCameraId() {
//    if (faceCamera != null) {
//      return faceCamera.getCameraId();
//    } else {
//      return 1;
//    }
//  }
//
//  public void setOrientation(int orientation) {
//    if (faceCamera != null) {
//      faceCamera.setOrientation(orientation);
//    }
//  }
//
//  public void setFaceCallback(FaceCallback faceCallback) {
//    this.faceCallback = faceCallback;
//    if (faceCamera == null) {
//      faceCamera = new FaceCamera();
//    }
//    faceCamera.setFaceListener(new FaceListener() {
//      @Override
//      public void onError(String s) {
//        if (FaceDetectHelper.this.faceCallback != null) {
//          FaceDetectHelper.this.faceCallback.onError(s);
//        }
//      }
//
//      @Override
//      public void onEvent(YmEvent ymEvent) {
//        if (FaceDetectHelper.this.faceCallback != null) {
//          FaceDetectHelper.this.faceCallback.onEvent(ymEvent.status);
//        }
//      }
//    });
//  }
//
//  public boolean statTrackNoPreview(Activity activity, SurfaceTexture blankSurfaceTexture) {
//    if (faceCamera != null) {
//      initOrientationSensor(activity);
//      return faceCamera.statTrackNoPreview(activity, blankSurfaceTexture);
//    }
//    return false;
//  }
//
//  public boolean stopTrackNoPreview() {
//    if (faceCamera != null) {
//      stopOrientationSensor();
//      return faceCamera.stopTrackNoPreview();
//    }
//    return false;
//  }
//
//  private FaceCallback faceCallback;
//
//  public void removeCameraView(AspectRatioFrameLayout aspectRatioFrameLayout) {
//    if (mPreview != null) {
//      aspectRatioFrameLayout.removeView(mPreview);
//    }
//  }
//
//  public interface FaceCallback {
//
//    void onError(String error);
//
//    void onEvent(int status);
//  }
//
//  private ErrCallback errCallback;
//
//  public void setErrCallback(ErrCallback errCallback) {
//    this.errCallback = errCallback;
//    if (mPreview != null) {
//      mPreview.setErrorListener(new ErrorListener() {
//        @Override
//        public void onStarCameraError() {
//          if (FaceDetectHelper.this.errCallback != null) {
//            FaceDetectHelper.this.errCallback.onStarCameraError();
//          }
//        }
//      });
//      mPreview.setSizeChangeListener(new SizeChangeListener() {
//        @Override
//        public void onSizeChange(int w, int h, int orientation) {
//          if (FaceDetectHelper.this.errCallback != null) {
//            FaceDetectHelper.this.errCallback.onSizeChange(w, h, orientation);
//          }
//        }
//      });
//    }
//  }
//
//  public int getCameraAngle() {
//    return faceCamera.getCameraAngle();
//  }
//
//  public interface ErrCallback {
//
//    void onStarCameraError();
//
//    void onSizeChange(int w, int h, int orientation);
//  }
//
//  private FaceInfoCallBack mFaceInfoCallBack;
//
//  public interface FaceInfoCallBack {
//    void faceInfo(int width, int height, Rect rect, byte[] bytes);
//  }
//
//  public void setFaceInfoCallBack(final FaceInfoCallBack faceCallBack) {
//    mFaceInfoCallBack = faceCallBack;
//    faceCamera.setFaceInfoListener(new FaceInfoListener() {
//      @Override
//      public void faceInfo(int i, int i1, Rect rect, byte[] bytes) {
//        mFaceInfoCallBack.faceInfo(i, i1, rect, bytes);
//      }
//    });
//  }
}
