package com.namibox.imageselector.camera;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.example.picsdk.R;
import com.namibox.imageselector.camera.CameraPreview.OnSurfaceCreatedListener;
import com.namibox.util.Logger;

/**
 * @author: Shelter
 * Create time: 2019/9/3, 10:18.
 */
public class CameraFragment extends Fragment {

  private CameraPreview mCameraPreview;
  private Camera mCamera;
  private int mCameraId;
  public boolean safeToTakePic;
  private int cameraPosition = 1;
  private ViewGroup cameraFrame;
  private OnCameraSwitchListener onCameraSwitchListener;

  public CameraFragment() {
  }

  public static CameraFragment newInstance() {
    return new CameraFragment();
  }

  public void setCamera(Camera mCamera) {
    this.mCamera = mCamera;
  }

  public void setCameraId(int mCameraId) {
    this.mCameraId = mCameraId;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View inflate = inflater.inflate(R.layout.layout_camera_fragment, container, false);
    cameraFrame = inflate.findViewById(R.id.cameraFrame);
    mCameraPreview = new CameraPreview(getContext(), mCamera, mCameraId,
        () -> safeToTakePic = true);
    mCameraPreview.setOnClickListener(view -> {
      if (onPreviewClickListener != null) {
        onPreviewClickListener.onPreviewClick();
      }
    });
    cameraFrame.addView(mCameraPreview);
    return inflate;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

  }

  private void restartPreview(Camera camera) {
    cameraFrame.removeView(mCameraPreview);
    if (onCameraSwitchListener != null) {
      onCameraSwitchListener.onCameraSwitch(camera, cameraPosition);
    }
    mCamera = camera;
    mCameraPreview = new CameraPreview(getContext(), mCamera, mCameraId,
        new OnSurfaceCreatedListener() {
          @Override
          public void onSurfaceCreated() {
            safeToTakePic = true;
          }
        });
    cameraFrame.addView(mCameraPreview);
  }

  public void switchCamera() {
    //切换前后摄像头
    int cameraCount = 0;
    CameraInfo cameraInfo = new CameraInfo();
    //得到摄像头的个数
    cameraCount = Camera.getNumberOfCameras();

    for (int i = 0; i < cameraCount; i++) {
      //得到每一个摄像头的信息
      Camera.getCameraInfo(i, cameraInfo);
      if (cameraPosition == 1) {
        //现在是后置，变更为前置
        //代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
          mCameraPreview.releaseCamera();
          //打开当前选中的摄像头
          Camera camera = Camera.open(i);
          //mCamera.setPreviewDisplay(mCameraPreview.getHolder());
          mCameraId = i;
          //mCamera.startPreview();//开始预览
          cameraPosition = 0;
          restartPreview(camera);
          break;
        }
      } else {
        //现在是前置， 变更为后置
        //代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
          mCameraPreview.releaseCamera();
          Camera camera = Camera.open(i);
          mCameraId = i;
          //mCamera.setPreviewDisplay(mCameraPreview.getHolder());
          cameraPosition = 1;
          restartPreview(camera);
          break;
        }
      }

    }
  }

  public int getBitmapRotation() {
    CameraInfo cameraInfo = new CameraInfo();
    Camera.getCameraInfo(mCameraId, cameraInfo);
    if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
      return 90;
    } else if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT){
      return 270;
    }
    return 0;
  }

  public boolean isFrontCamera() {
    CameraInfo cameraInfo = new CameraInfo();
    Camera.getCameraInfo(mCameraId, cameraInfo);
    return cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mCameraPreview.releaseCamera();
  }

  public void startPreview() {
    try {
      mCamera.startPreview();
    } catch (Exception e) {
      Logger.d("CameraPreview startPreview exception: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void takePicture(Camera.PictureCallback callback) {
    try {
      mCamera.takePicture(null, null, callback);
    } catch (Exception e) {
      safeToTakePic = true;
      e.printStackTrace();
    }
  }

  public interface OnPreviewClickListener {

    void onPreviewClick();
  }

  OnPreviewClickListener onPreviewClickListener;

  public void setOnPreviewClickListener(
      OnPreviewClickListener onPreviewClickListener) {
    this.onPreviewClickListener = onPreviewClickListener;
  }

  interface OnCameraSwitchListener {
    void onCameraSwitch(Camera camera, int cameraPosition);
  }

  public void setOnCameraSwitchListener(OnCameraSwitchListener listener) {
    this.onCameraSwitchListener = listener;
  }
}
