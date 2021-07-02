package com.namibox.tools;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.picsdk.R;
import com.namibox.commonlib.view.PermissionItemView;
import com.namibox.util.Utils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunha on 2017/8/15 0015.
 */

public class PermissionUtil {

  private static final String TAG = "PermissionUtil";
  private static final String NOTIFICATION_PERMISSION = "notification_permission";

  public static void requestPermission(Activity activity, GrantedCallback callback
      , String... permissions) {
    requestPermission(activity, false, true, callback, null, permissions);
  }

  public static void requestPermission(Activity activity, GrantedCallback callback
      , DialogListener dialogListener, String... permissions) {
    requestPermission(activity, false, true, callback, dialogListener, permissions);
  }

  public static void requestPermissionFirst(Activity activity, GrantedCallback callback,
      String... permissions) {
    requestPermission(activity, true, false, callback, null, permissions);
  }

  public static void requestPermissionWithFinish(Activity activity, GrantedCallback callback,
      String... permissions) {
    requestPermission(activity, true, true, callback, null, permissions);
  }

  public static boolean checkPermission(Activity activity, String permission) {
    return PermissionChecker.checkSelfPermission(activity, permission)
        == PERMISSION_GRANTED;
  }

  public static void requestPermission(Activity activity, GrantedCallback callback,
      UngrantedCallback ungrantedCallback, String... permissions) {
    requestPermission(activity, false, true, callback, ungrantedCallback, null, permissions);
  }

  private static void requestPermission(final Activity activity, final boolean finish,
      final boolean showClose, final GrantedCallback grantedCallback,
      final UngrantedCallback ungrantedCallback, final DialogListener dialogListener,
      final String... permissions) {
    if (Build.VERSION.SDK_INT >= 23) {
      Observable.fromArray(permissions)
          .concatMap(new Function<String, Observable<Boolean>>() {

            @Override
            public Observable<Boolean> apply(@NonNull String s) throws Exception {
              return new RxPermissions(activity).request(s);
            }
          })
          .toList()
          .map(new Function<List<Boolean>, List<String>>() {
            @Override
            public List<String> apply(@NonNull List<Boolean> booleen) throws Exception {
              List<String> permissionList = new ArrayList<>();
              for (String permission : permissions) {
                if (!checkPermission(activity, permission)) {
                  permissionList.add(permission);
                }
              }
//              for (int i = 0; i < permissions.length; i++) {
//                if (!booleen.get(i)) {
//                  permissionList.add(permissions[i]);
//                }
//              }
              return permissionList;
            }
          })
          .subscribe(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> permissions) throws Exception {
              if (permissions.size() == 0) {
                if (grantedCallback != null) {
                  grantedCallback.action();
                }
              } else {
                if (ungrantedCallback != null) {
                  ungrantedCallback.action();
                }
                if (!"com.jinxin.appstudent".equals(activity.getPackageName())) {
                  showPermissionView(permissions, activity, showClose, finish, false, dialogListener);
                }
              }
            }
          });
    } else {
      if (grantedCallback != null) {
        grantedCallback.action();
      }
    }
  }


  public static void requestBDPermission(final Activity activity, final GrantedCallback grantedCallback,
      final UngrantedCallback ungrantedCallback, final String... permissions) {
    if (Build.VERSION.SDK_INT >= 23) {
      Observable.fromArray(permissions)
          .concatMap(new Function<String, Observable<Boolean>>() {

            @Override
            public Observable<Boolean> apply(@NonNull String s) throws Exception {
              return new RxPermissions(activity).request(s);
            }
          })
          .toList()
          .map(new Function<List<Boolean>, List<String>>() {
            @Override
            public List<String> apply(@NonNull List<Boolean> booleen) throws Exception {
              List<String> permissionList = new ArrayList<>();
              for (String permission : permissions) {
                if (!checkPermission(activity, permission)) {
                  permissionList.add(permission);
                }
              }
//              for (int i = 0; i < permissions.length; i++) {
//                if (!booleen.get(i)) {
//                  permissionList.add(permissions[i]);
//                }
//              }
              return permissionList;
            }
          })
          .subscribe(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> permissions) throws Exception {
              if (permissions.size() == 0) {
                if (grantedCallback != null) {
                  grantedCallback.action();
                }
              } else {
                if (ungrantedCallback != null) {
                  ungrantedCallback.action();
                }
              }
            }
          });
    } else {
      if (grantedCallback != null) {
        grantedCallback.action();
      }
    }
  }

  @SuppressLint("CheckResult")
  public static void requestGroupPermission(final Activity activity, final GrantedCallback grantedCallback,
      final UngrantedCallback ungrantedCallback, final String... permissions) {
    if (Build.VERSION.SDK_INT >= 23) {
      new RxPermissions(activity).request(permissions)
          .subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean granted) throws Exception {
              if (granted) {
                if (grantedCallback != null) {
                  grantedCallback.action();
                }
              } else {
                if (ungrantedCallback != null) {
                  ungrantedCallback.action();
                }
              }
            }
          });


    } else {
      if (grantedCallback != null) {
        grantedCallback.action();
      }
    }
  }

  private static void requestPermission(final Activity activity, final boolean finish,
      final boolean showClose, final GrantedCallback callback, final DialogListener dialogListener,
      final String... permissions) {
    requestPermission(activity, finish, showClose, callback, null, dialogListener, permissions);
  }

  public static void showNotifyPermissionView(final Activity activity, final DialogListener dialogListener) {
//    showPermissionView(Collections.singletonList(NOTIFICATION_PERMISSION),
//        activity, true, false, true, dialogListener);
    View alertView = LayoutInflater.from(activity).inflate(R.layout.permission_notify_layout, null);
    final AlertDialog alertDialog = new AlertDialog.Builder(activity,
        R.style.common_corner_dialog)
        .setView(alertView)
        .setCancelable(false)
        .create();
    setImageApplication(activity, alertView, 1);
    alertDialog.show();

    TextView openSetting = alertView.findViewById(R.id.openSetting);
    openSetting.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (dialogListener != null) {
          dialogListener.onDialogDismiss();
        }
        alertDialog.dismiss();
        try {
          openNotificationSetting(activity);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    ImageView close = alertView.findViewById(R.id.close);
    close.setVisibility(View.VISIBLE);
    close.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        alertDialog.dismiss();
        if (dialogListener != null) {
          dialogListener.onDialogDismiss();
        }
      }
    });
  }


  /**
   * 自定义权限申请弹框
   */
  public static void showPermissionSettingDialog(List<String> permissions, final Activity activity) {
    if (Build.VERSION.SDK_INT >= 23) {
      showPermissionView(permissions, activity, false, true, false, null);
    }
  }

  private static void showPermissionView(List<String> permissions, final Activity activity,
      boolean showClose, final boolean finish,
      final boolean isNotifycationPermisson, final DialogListener dialogListener) {
    View alertView = LayoutInflater.from(activity).inflate(R.layout.permission_layout, null);
    final AlertDialog alertDialog = new AlertDialog.Builder(activity,
        R.style.common_corner_dialog)
        .setView(alertView)
        .setCancelable(false)
        .create();
    setImageApplication(activity, alertView, 2);
    alertDialog.show();
    alertDialog.setOnDismissListener(new OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialog) {
        if (dialogListener != null) {
          dialogListener.onDialogDismiss();
        }
        if (finish) {
          activity.finish();
        }
      }
    });
    LinearLayout permissionList = alertView.findViewById(R.id.permissionItemList);
    TextView openSetting = alertView.findViewById(R.id.openSetting);
    TextView tip = alertView.findViewById(R.id.tip);

    openSetting.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        alertDialog.dismiss();
        try {
          if (isNotifycationPermisson) {
            openNotificationSetting(activity);
          } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
          }

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    if (showClose) {
      ImageView close = alertView.findViewById(R.id.close);
      close.setVisibility(View.VISIBLE);
      close.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          alertDialog.dismiss();
        }
      });
    }
    for (String permission : permissions) {
      permissionList.addView(new PermissionItemView(activity, permission));
    }
    StringBuilder rationalTip = new StringBuilder();
    String application = getApplication(activity);
    for (String permission : permissions) {
      rationalTip.append(getRationalTip(permission, application));
    }
    tip.setText(rationalTip.toString());
  }

  private static void setImageApplication(Activity activity, View alertView, int type) {
    String packageName = activity.getPackageName();
    if (packageName.contains("booksapp")) {
      if (type == 1) {
        ImageView notify_img_two = alertView.findViewById(R.id.notify_img_two);
        ImageView notify_img_one = alertView.findViewById(R.id.notify_img_one);
        if (notify_img_two != null) {
          notify_img_two.setImageDrawable(activity.getResources().getDrawable(R.drawable.notify_img_2_pic));
        }
        if (notify_img_one != null) {
          notify_img_one.setImageDrawable(activity.getResources().getDrawable(R.drawable.notify_img_1_pic));
        }
      } else if (type == 2) {
        LinearLayout permissionItemList = alertView.findViewById(R.id.permissionItemList);
        if (permissionItemList != null) {
          permissionItemList.setBackgroundResource(R.drawable.permission_list_bg_pic);
        }
      }
    }
  }

  private static String getApplication(Activity activity) {
    ApplicationInfo applicationInfo = activity.getApplicationInfo();
    return applicationInfo.loadLabel(activity.getPackageManager()).toString();
  }

  public static String getPermissonTip(String permission) {
    switch (permission) {
      case Manifest.permission.CAMERA:
        return "相机";
      case Manifest.permission.READ_EXTERNAL_STORAGE:
        return "手机存储";
      case Manifest.permission.READ_PHONE_STATE:
        return "手机/电话";
      case Manifest.permission.ACCESS_COARSE_LOCATION:
        return "位置信息";
      case Manifest.permission.RECORD_AUDIO:
        return "录音/麦克风";
      case Manifest.permission.WRITE_EXTERNAL_STORAGE:
        return "手机存储";
      case Manifest.permission.READ_CALENDAR:
        return "日历";
      case Manifest.permission.WRITE_CALENDAR:
        return "日历";
      case NOTIFICATION_PERMISSION:
        return "通知";
      default:
        return "其他";
    }
  }

  private static String getRationalTip(String permission, String application) {
    switch (permission) {
      case Manifest.permission.CAMERA:
        return application + "需要获取您的相机权限，以便扫描二维码和拍摄照片视频";
      case Manifest.permission.READ_EXTERNAL_STORAGE:
        return "手机存储";
      case Manifest.permission.READ_PHONE_STATE:
        return application + "需要获取您的电话权限，以便保证您的账户安全";
      case Manifest.permission.ACCESS_COARSE_LOCATION:
        return application + "需要获取您的地理位置权限，以便记录您分享时的位置状态";
      case Manifest.permission.RECORD_AUDIO:
        return application + "需获取您的麦克风权限，以便您完成语音评测和分享您讲的故事";
      case Manifest.permission.WRITE_EXTERNAL_STORAGE:
        return application + "需要获取您的手机储存权限，以便视频能正常的播放与下载";
      case NOTIFICATION_PERMISSION:
        return application + "需要获取您的通知权限，以便我们能及时告知您重要信息";
      case Manifest.permission.READ_CALENDAR:
        return application + "需要获取您的日历权限，以便我们能够通过日历定时提醒您上课";
      case Manifest.permission.WRITE_CALENDAR:
        return application + "需要获取您的日历权限，以便我们能够通过日历定时提醒您上课";
      default:
        return "其他";
    }
  }

  public interface UngrantedCallback {

    void action();
  }

  public interface GrantedCallback {

    void action();
  }

  public static void openNotificationSetting(Activity activity) {
    try {
      Intent intent = new Intent();
      intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
      intent.putExtra("app_package", activity.getPackageName());
      intent.putExtra("app_uid", activity.getApplicationInfo().uid);
      // for Android O
      intent.putExtra("android.provider.extra.APP_PACKAGE", activity.getPackageName());
      activity.startActivity(intent);
    } catch (Exception e) {
      try {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
      } catch (Exception ex) {
        Utils.toast(activity, "无法打开设置界面，请手动前往系统设置中开启通知权限");
      }
    }
  }

  public interface DialogListener {

    void onDialogDismiss();
  }

}
