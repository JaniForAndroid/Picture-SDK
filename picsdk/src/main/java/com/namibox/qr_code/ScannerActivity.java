package com.namibox.qr_code;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import com.example.picsdk.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.namibox.util.Logger;
import com.namibox.util.Utils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


/**
 * Create time: 2015/9/24.
 */
public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

  static final String TAG = "ScannerActivity";
  private ZXingScannerView mScannerView;
  public static final String RESULT = "result";
  public static final String RESULT_FORMAT = "result_format";
  public static final String ORIENTATION = "orientation";
  private static final int REQUEST_QRCODE = 10000;

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      getWindow().setStatusBarColor(0x00000000);
    }
    Intent intent = getIntent();
    int orientation = intent.getIntExtra(ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    setRequestedOrientation(orientation);
    setResult(RESULT_CANCELED);
    List<BarcodeFormat> formats = new ArrayList<>();
    formats.add(BarcodeFormat.QR_CODE);
    formats.add(BarcodeFormat.EAN_13);
    setContentView(R.layout.activity_scanner);
    findViewById(R.id.tvAlbum).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        openAlbum();
      }
    });
    mScannerView = findViewById(R.id.scanner);
    mScannerView.setFormats(formats);
    findViewById(R.id.back).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackPressed();
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
    mScannerView.startCamera();          // Start camera on resume
    mScannerView.setAutoFocus(true);
  }

  @Override
  public void onPause() {
    super.onPause();
    mScannerView.stopCamera();           // Stop camera on pause
  }

  private void openAlbum() {
    Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
    intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
    startActivityForResult(intentToPickPic, REQUEST_QRCODE);
  }

  @Override
  public void handleResult(Result rawResult) {
    Intent data = new Intent();
    data.putExtra(RESULT, rawResult.getText());
    data.putExtra(RESULT_FORMAT, rawResult.getBarcodeFormat().toString());
    setResult(RESULT_OK, data);
    finish();
    overridePendingTransition(0, 0);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (resultCode == RESULT_OK) {
      Uri uri = intent.getData();
      if (uri != null) {
        String filePath = getPhotoPathFromContentUri(uri);
        Logger.d(TAG, "filePath = " + filePath);
        parsePhoto(filePath);
      }
    }
  }

  public String getPhotoPathFromContentUri(Uri uri) {
    String photoPath = "";
    if (uri == null) {
      return photoPath;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract
            .isDocumentUri(this, uri)) {
      String docId = DocumentsContract.getDocumentId(uri);
      if (isExternalStorageDocument(uri)) {
        String[] split = docId.split(":");
        if (split.length >= 2) {
          String type = split[0];
          if ("primary".equalsIgnoreCase(type)) {
            photoPath = Environment.getExternalStorageDirectory() + "/" + split[1];
          }
        }
      } else if (isDownloadsDocument(uri)) {
        Uri contentUri = ContentUris
                .withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
        photoPath = getDataColumn(contentUri, null, null);
      } else if (isMediaDocument(uri)) {
        String[] split = docId.split(":");
        if (split.length >= 2) {
          String type = split[0];
          Uri contentUris = null;
          if ("image".equals(type)) {
            contentUris = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
          } else if ("video".equals(type)) {
            contentUris = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
          } else if ("audio".equals(type)) {
            contentUris = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
          }
          String selection = MediaStore.Images.Media._ID + "=?";
          String[] selectionArgs = new String[]{split[1]};
          photoPath = getDataColumn(contentUris, selection, selectionArgs);
        }
      }
    } else if ("file".equalsIgnoreCase(uri.getScheme())) {
      photoPath = uri.getPath();
    } else {
      photoPath = getDataColumn(uri, null, null);
    }

    return photoPath;
  }

  private boolean isExternalStorageDocument(Uri uri) {
    return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }

  private boolean isDownloadsDocument(Uri uri) {
    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }

  private boolean isMediaDocument(Uri uri) {
    return "com.android.providers.media.documents".equals(uri.getAuthority());
  }

  private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
    Cursor cursor = null;
    String column = MediaStore.Images.Media.DATA;
    String[] projection = {column};
    try {
      cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
      if (cursor != null && cursor.moveToFirst()) {
        int index = cursor.getColumnIndexOrThrow(column);
        return cursor.getString(index);
      }
    } finally {
      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
      }
    }
    return null;
  }

  /**
   * 启动线程解析二维码图片
   *
   * @param path
   */
  private void parsePhoto(String path) {
    //启动线程完成图片扫码
    new QrCodeAsyncTask(this, path).execute(path);
  }

  /**
   * 处理图片二维码解析的数据
   *
   * @param s
   */
  public void handleQrCode(String s) {
    if (null == s) {
      Utils.toast(this, getString(R.string.scan_no_code));
    } else {
      handleResult(s);
    }
  }

  private void handleResult(String result) {
    //处理二维码结果
    Intent data = new Intent();
    data.putExtra(RESULT, result);
    data.putExtra(RESULT_FORMAT, BarcodeFormat.QR_CODE);
    setResult(RESULT_OK, data);
    finish();
    overridePendingTransition(0, 0);
  }

  /**
   * AsyncTask 静态内部类，防止内存泄漏
   */
  static class QrCodeAsyncTask extends AsyncTask<String, Integer, String> {
    private WeakReference<Activity> mWeakReference;
    private String path;

    public QrCodeAsyncTask(Activity activity, String path) {
      mWeakReference = new WeakReference<>(activity);
      this.path = path;
    }

    @Override
    protected String doInBackground(String... strings) {
      // 解析二维码/条码
      return QrUtil.syncDecodeQRCode(path);
    }

    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);
      //识别出图片二维码/条码，内容为s
      ScannerActivity activity = (ScannerActivity) mWeakReference.get();
      if (activity != null) {
        activity.handleQrCode(s);
      }
    }
  }
}
