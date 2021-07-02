package com.namibox.imageselector;

import android.app.Activity;
import android.content.Intent;
import com.namibox.commonlib.model.Result;
import com.namibox.imageselector.camera.AvatarCameraActivity;
import com.namibox.imageselector.camera.CameraActivity;
import com.namibox.imageselector.rxutil.AResult;
import com.namibox.imageselector.rxutil.AResultMessage;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Akkun on 2020/4/22.
 * web: http://www.zkyml.com
 * Des:
 */
public class RxImagePicker {

  private static volatile RxImagePicker instance;

  private RxImagePicker() {
  }

  public static RxImagePicker getInstance() {
    if (instance == null) {
      synchronized (RxImagePicker.class) {
        if (instance == null) {
          instance = new RxImagePicker();
        }
      }
    }
    return instance;
  }

  public Observable<List<File>> openAppFileChooser(Activity activity, int maxCount,
      int size) {
    Intent intent = new Intent(activity, ImageSelectorActivity.class);
    if (maxCount > 0) {
      intent.putExtra(ImageSelectorActivity.EXTRA_SELECT_COUNT, maxCount);
    }
    if (size > 0) {
      intent.putExtra(ImageSelectorActivity.EXTRA_SIZE, size);
    }
    return new AResult(activity).startForResult(intent).map(
        new Function<AResultMessage, List<File>>() {
          @Override
          public List<File> apply(AResultMessage aResultMessage)
              throws Exception {
            List<File> files = new ArrayList<>();
            if (aResultMessage.getData() == null) {
              return files;
            }
            ArrayList<Result> results = aResultMessage.getData()
                .getParcelableArrayListExtra(ImageSelectorActivity.RESULT_LIST);
            if (results != null && !results.isEmpty()) {
              for (Result result : results) {
                files.add(new File(result.path));
              }
            }
            return files;
          }
        });
  }

  public interface FileChooseCallback {

    void onFileChosed(List<File> results);
  }

  public void openAppFileChooser(Activity activity, final FileChooseCallback fileChooseCallback,
      int maxCount, int size) {
    Intent intent = new Intent(activity, ImageSelectorActivity.class);
    if (maxCount > 0) {
      intent.putExtra(ImageSelectorActivity.EXTRA_SELECT_COUNT, maxCount);
    }
    if (size > 0) {
      intent.putExtra(ImageSelectorActivity.EXTRA_SIZE, size);
    }
    new AResult(activity).startForResult(intent).subscribe(
        new Consumer<AResultMessage>() {
          @Override
          public void accept(AResultMessage aResultMessage) throws Exception {
            if (aResultMessage.getData() == null) {
              return;
            }
            ArrayList<Result> results = aResultMessage.getData()
                .getParcelableArrayListExtra(ImageSelectorActivity.RESULT_LIST);
            if (fileChooseCallback != null && results != null && !results.isEmpty()) {
              List<File> files = new ArrayList<>();
              for (Result result : results) {
                files.add(new File(result.path));
              }
              fileChooseCallback.onFileChosed(files);
            }
          }
        }).isDisposed();
  }

  public void openAppFileChooser(Activity activity, final FileChooseCallback fileChooseCallback,
                                 int maxCount, int size, boolean openCamera) {
    Intent intent = new Intent(activity, ImageSelectorActivity.class);
    if (maxCount > 0) {
      intent.putExtra(ImageSelectorActivity.EXTRA_SELECT_COUNT, maxCount);
    }
    if (size > 0) {
      intent.putExtra(ImageSelectorActivity.EXTRA_SIZE, size);
    }
    intent.putExtra(ImageSelectorActivity.EXTRA_TAKE_PHOTE, openCamera);
    new AResult(activity).startForResult(intent).subscribe(
        new Consumer<AResultMessage>() {
          @Override
          public void accept(AResultMessage aResultMessage) throws Exception {
            if (aResultMessage.getData() == null) {
              return;
            }
            ArrayList<Result> results = aResultMessage.getData()
                .getParcelableArrayListExtra(ImageSelectorActivity.RESULT_LIST);
            if (fileChooseCallback != null && results != null && !results.isEmpty()) {
              List<File> files = new ArrayList<>();
              for (Result result : results) {
                files.add(new File(result.path));
              }
              fileChooseCallback.onFileChosed(files);
            }
          }
        }).isDisposed();
  }

  public void openWebFileChooser(Intent intent, final FileChooseCallback fileChooseCallback,
      int maxCount, int size) {
  }

  public void chooseWebImgCamera(Activity activity, int cameraId,int maxSize,boolean showSample, int type, int requestCode) {
    Intent intent;
    //2为头像拍照，1为作业拍照
    if (type == 2) {
      intent = new Intent(activity, AvatarCameraActivity.class);
    } else {
      intent = new Intent(activity, CameraActivity.class);
    }
    intent.putExtra("type", type);
    intent.putExtra("cameraId", cameraId);
    if (maxSize > 0) {
      intent.putExtra(ImageSelectorActivity.EXTRA_SELECT_COUNT, maxSize);
    }
    intent.putExtra(CameraActivity.SHOW_SAMPLE, showSample);
    intent.putExtra(ImageSelectorActivity.REQUEST_CODE, requestCode);
    intent.putExtra(ImageSelectorActivity.EXTRA_ADD_WATERMASK, true);
   activity.startActivity(intent);
  }

  public void chooseWebImgNoCamera(Activity activity, int maxSize, int requestCode) {
    Intent intent = new Intent(activity, ImageSelectorActivity.class);
    if (maxSize > 0) {
      intent.putExtra(ImageSelectorActivity.EXTRA_SELECT_COUNT, maxSize);
    }
    intent.putExtra(ImageSelectorActivity.REQUEST_CODE, requestCode);
    intent.putExtra(ImageSelectorActivity.EXTRA_ADD_WATERMASK, true);
    activity.startActivity(intent);
  }

  public void startChooseImg(Activity activity, int maxSize,
      final FileChooseCallback fileChooseCallback) {
    Intent intent = new Intent(activity, ImageSelectorActivity.class);
    intent.putExtra(ImageSelectorActivity.EXTRA_SELECT_COUNT, maxSize);
    intent.putExtra(ImageSelectorActivity.EXTRA_ADD_WATERMASK, true);
    new AResult(activity).startForResult(intent).subscribe(
        new Consumer<AResultMessage>() {
          @Override
          public void accept(AResultMessage aResultMessage) throws Exception {
            if (aResultMessage.isOk()) {
              if (aResultMessage.getData() == null) {
                return;
              }
              ArrayList<Result> results = aResultMessage.getData()
                  .getParcelableArrayListExtra(ImageSelectorActivity.RESULT_LIST);
              if (fileChooseCallback != null && results != null && !results.isEmpty()) {
                List<File> files = new ArrayList<>();
                for (Result result : results) {
                  files.add(new File(result.path));
                }
                fileChooseCallback.onFileChosed(files);
              }
            }
          }
        }).isDisposed();
  }
}
