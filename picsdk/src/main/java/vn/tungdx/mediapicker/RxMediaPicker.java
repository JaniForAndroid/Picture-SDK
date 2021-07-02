package vn.tungdx.mediapicker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import com.example.picsdk.R;
import com.namibox.commonlib.activity.AbsFunctionActivity;
import com.namibox.tools.PermissionUtil;
import com.namibox.tools.PermissionUtil.GrantedCallback;
import com.namibox.util.Utils;
import com.zhy.base.fileprovider.FileProvider7;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;
import vn.tungdx.mediapicker.rxutil.AResult;
import vn.tungdx.mediapicker.rxutil.AResultMessage;

/**
 * Created by Akkun on 2020/4/22.
 * web: http://www.zkyml.com
 * Des:
 */
public class RxMediaPicker {

  private static volatile RxMediaPicker instance;

  private RxMediaPicker() {
  }

  public static RxMediaPicker getInstance() {
    if (instance == null) {
      synchronized (RxMediaPicker.class) {
        if (instance == null) {
          instance = new RxMediaPicker();
        }
      }
    }
    return instance;
  }


  public Observable<MediaItem> chooseVideoLocalOnlySucess(Activity activity, int max,
      int min) {
    MediaOptions.Builder builder = new MediaOptions.Builder();
    MediaOptions options = builder.selectVideo().setMinVideoDuration(min).setMaxVideoDuration(max)
        .build();
    final Intent intent = new Intent(activity, MediaPickerActivity.class);
    intent.putExtra(MediaPickerActivity.EXTRA_MEDIA_OPTIONS, options);
    intent.putExtra("no_actionbar", true);
    return new AResult(activity).startForResult(intent)
        .map(new Function<AResultMessage, MediaItem>() {
          @Override
          public MediaItem apply(AResultMessage aResultMessage) throws Exception {
            MediaItem mediaItem = null;
            if (aResultMessage.isOk()) {
              ArrayList<MediaItem> mMediaSelectedList = MediaPickerActivity
                  .getMediaItemSelected(aResultMessage.getData());
              if (mMediaSelectedList == null || mMediaSelectedList.isEmpty()) {
                return null;
              }
              mediaItem = mMediaSelectedList.get(0);
            }
            return mediaItem;
          }
        });
  }

  public Observable<AResultMessage> chooseVideoLocal(Activity activity, int max, int min) {
    MediaOptions.Builder builder = new MediaOptions.Builder();
    MediaOptions options = builder.selectVideo().setMinVideoDuration(min).setMaxVideoDuration(max)
        .build();
    final Intent intent = new Intent(activity, MediaPickerActivity.class);
    intent.putExtra(MediaPickerActivity.EXTRA_MEDIA_OPTIONS, options);
    intent.putExtra("no_actionbar", true);
    return new AResult(activity).startForResult(intent);
  }

  public void chooseVideoLocal(final Activity activity, int max, int min,
      final VideoChooseListener videoChooseListener) {
    MediaOptions.Builder builder = new MediaOptions.Builder();
    MediaOptions options = builder.selectVideo().setMinVideoDuration(min).setMaxVideoDuration(max)
        .build();
    final Intent intent = new Intent(activity, MediaPickerActivity.class);
    intent.putExtra(MediaPickerActivity.EXTRA_MEDIA_OPTIONS, options);
    intent.putExtra("no_actionbar", true);
    new AResult(activity).startForResult(intent)
        .subscribe(new Consumer<AResultMessage>() {
          @Override
          public void accept(AResultMessage aResultMessage) throws Exception {
            if (aResultMessage.isOk()) {
              ArrayList<MediaItem> mMediaSelectedList = MediaPickerActivity
                  .getMediaItemSelected(aResultMessage.getData());
              if (mMediaSelectedList == null) {
                return;
              }
              Uri videoUri = mMediaSelectedList.get(0).getUriOrigin();
              String path = mMediaSelectedList.get(0).getPathOrigin(activity);
              if (videoChooseListener != null) {
                videoChooseListener.onVideoChoose(videoUri, path);
              }

            } else if (aResultMessage.isCancel()) {
              if (videoChooseListener != null) {
                videoChooseListener.onError(VideoChooseListener.CANCEL, "取消选择视频");
              }
            } else {
              if (videoChooseListener != null) {
                videoChooseListener.onError(VideoChooseListener.PARSER_FAIL, "解析视频失败");
              }
            }
          }
        }).isDisposed();
  }

  public void intoVideoSelector(final Activity activity, int min,
      final VideoChooseListener videoChooseListener) {

    MediaOptions.Builder builder = new MediaOptions.Builder();
    MediaOptions options = builder.selectVideo().setMinVideoDuration(min)
        .build();
    final Intent intent = new Intent(activity, MediaPickerActivity.class);
    intent.putExtra(MediaPickerActivity.EXTRA_MEDIA_OPTIONS, options);
    new AResult(activity).startForResult(intent)
        .subscribe(new Consumer<AResultMessage>() {
          @Override
          public void accept(AResultMessage aResultMessage) throws Exception {
            if (aResultMessage.isOk()) {
              ArrayList<MediaItem> mMediaSelectedList = MediaPickerActivity
                  .getMediaItemSelected(aResultMessage.getData());
              if (mMediaSelectedList == null) {
                return;
              }
              Uri videoUri = mMediaSelectedList.get(0).getUriOrigin();
              String path = mMediaSelectedList.get(0).getPathOrigin(activity);
              if (videoChooseListener != null) {
                videoChooseListener.onVideoChoose(videoUri, path);
              }

            } else if (aResultMessage.isCancel()) {
              if (videoChooseListener != null) {
                videoChooseListener.onError(VideoChooseListener.CANCEL, "取消选择视频");
              }
            } else {
              if (videoChooseListener != null) {
                videoChooseListener.onError(VideoChooseListener.PARSER_FAIL, "解析视频失败");
              }
            }
          }
        }).isDisposed();
  }

  public void intoVideoRecorder(final AbsFunctionActivity activity, int maxtime,
      final VideoChooseListener videoChooseListener) {

    try {
      // 跳转到系统照相机
      Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
      takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, maxtime);
      if (takeVideoIntent.resolveActivity(activity.getPackageManager()) != null) {
        // 设置系统相机拍照后的输出路径
        // 创建临时文件
        final File mTempFile = createRecorderFile();
        Uri fileUri = FileProvider7.getUriForFile(activity, mTempFile);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        new AResult(activity).startForResult(takeVideoIntent)
            .subscribe(new Consumer<AResultMessage>() {
              @Override
              public void accept(AResultMessage aResultMessage) throws Exception {
                if (aResultMessage.isOk()) {
                  if (mTempFile != null && mTempFile.exists()) {
                    Uri videoUri = Uri
                        .fromFile(mTempFile);//FileProvider7.getUriForFile(this, mTempFile);
                    String path = mTempFile.getAbsolutePath();
                    int checkResult = Utils.checkValidVideo(activity, videoUri);
                    if (checkResult == -2) {
                      if (videoChooseListener != null) {
                      }
                      videoChooseListener.onError(VideoChooseListener.PARSER_FAIL, "解析视频失败");
                    } else if (checkResult == -1) {
                      if (videoChooseListener != null) {
                        videoChooseListener.onError(VideoChooseListener.LESS_THAN_MIN, "视频小于10秒");
                      }
                    } else if (checkResult == 1) {
                      if (videoChooseListener != null) {
                        videoChooseListener.onVideoChoose(videoUri, path);
                      }
                    }
                  }

                } else if (aResultMessage.isCancel()) {
                  if (videoChooseListener != null) {
                    videoChooseListener.onError(VideoChooseListener.CANCEL, "取消选择视频");
                  }
                } else {
                  if (videoChooseListener != null) {
                    videoChooseListener.onError(VideoChooseListener.PARSER_FAIL, "解析视频失败");
                  }
                }
              }
            }).isDisposed();
      } else {
        activity.toast(activity.getString(R.string.msg_no_camera));
      }
    } catch (IOException e) {
      e.printStackTrace();
      activity.toast(activity.getString(R.string.msg_no_camera));
    }
  }


  private File createRecorderFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
    String imageFileName = "RECORDER_" + timeStamp + ".mp4";
    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    if (!storageDir.exists()) {
      if (!storageDir.mkdir()) {
        throw new IOException();
      }
    }
    return new File(storageDir, imageFileName);
  }


  public void chooseVideo(final AbsFunctionActivity activity,
      final VideoChooseListener videoChooseListener) {
    PermissionUtil.requestPermission(activity, new GrantedCallback() {
      @Override
      public void action() {
        final BottomSheetDialog bottomSheet = new BottomSheetDialog(activity);
        bottomSheet.setTitle("选择视频");
        View view = LayoutInflater.from(activity)
            .inflate(R.layout.fragment_choose_video_dialog, null);
        view.findViewById(R.id.fromRecord).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            bottomSheet.dismiss();
            RxMediaPicker.getInstance()
                .intoVideoRecorder(activity, Utils.MAXTIME, videoChooseListener);
          }
        });
        view.findViewById(R.id.fromPhone).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            bottomSheet.dismiss();
            RxMediaPicker.getInstance()
                .intoVideoSelector(activity, 10 * 1000, videoChooseListener);
          }
        });
        bottomSheet.setContentView(view);
        bottomSheet.show();
      }
    }, Manifest.permission.CAMERA);
  }

}
