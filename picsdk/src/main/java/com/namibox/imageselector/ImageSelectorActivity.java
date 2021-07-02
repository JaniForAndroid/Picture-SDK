package com.namibox.imageselector;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import com.example.picsdk.R;
import com.namibox.commonlib.dialog.DialogUtil;
import com.namibox.commonlib.dialog.DialogUtil.LoadingDialog;
import com.namibox.commonlib.event.ImageSelectResultEvent;
import com.namibox.commonlib.model.Result;
import com.namibox.imageselector.adapter.FolderAdapter;
import com.namibox.imageselector.adapter.ImageGridAdapter;
import com.namibox.imageselector.bean.Folder;
import com.namibox.imageselector.bean.Image;
import com.namibox.imageselector.cropper.AvatarCropActivity;
import com.namibox.imageselector.cropper.CropImage;
import com.namibox.imageselector.cropper.CropImageView;
import com.namibox.tools.PermissionUtil;
import com.namibox.tools.PermissionUtil.GrantedCallback;
import com.namibox.util.ImageUtil;
import com.namibox.util.Utils;
import com.zhy.base.fileprovider.FileProvider7;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;


/**
 * Create time: 2015/7/17.
 */
public class ImageSelectorActivity extends AppCompatActivity {

  private static final String TAG = "ImageSelectorActivity";

  public static final String RESULT_LIST = "result_list";
  /**
   * 最大图片选择次数，int类型
   */
  public static final String EXTRA_SELECT_COUNT = "max_select_count";
  public static final String REQUEST_CODE = "request_code";
  /**
   * 是否显示相机，boolean类型
   */
  public static final String EXTRA_SHOW_CAMERA = "show_camera";
  /**
   * 点击item是否直接跳到裁剪，App6.6功能
   */
  public static final String EXTRA_DIRECT_CROP = "direct_crop";


  /**
   * 压缩后图片长边
   */
  public static final String EXTRA_SIZE = "img_size";

  /**
   * 是否直接打开拍照
   */
  public static final String EXTRA_TAKE_PHOTE = "img_take_photo";
  // 不同loader定义
  private static final int LOADER_ALL = 0;
  private static final int LOADER_CATEGORY = 1;
  // 请求加载系统照相机
  private static final int REQUEST_CAMERA = 100;
  public static final int REQUEST_PREVIEW = 200;
  public static final int REQUEST_CROP = 300;
  public static final String EXTRA_ADD_WATERMASK = "add_watermask";


  // 结果数据
  private ArrayList<String> resultList = new ArrayList<>();
  // 文件夹数据
  private ArrayList<Folder> mResultFolder = new ArrayList<>();

  // 图片Grid
  private GridView mGridView;

  private ImageGridAdapter mImageAdapter;
  private FolderAdapter mFolderAdapter;

  private ListPopupWindow mFolderPopupWindow;

  // 时间线
  private TextView mTimeLineText;
  // 类别
  private TextView mCategoryText;
  // 预览按钮
  private TextView mPreviewBtn;
  // 底部View
  private View mPopupAnchorView;

  private int maxSelectNum;

  private boolean hasFolderGened = false;
  private boolean mIsShowCamera = false;
  private boolean mOpenCamera = false;
  private boolean mDirectCrop = false;

  private int mGridWidth, mGridHeight;

  private File mTmpFile;
  private TextView sendBtn;
  private LoadingDialog mDialog;
  private int imgSize;
  private boolean addWaterMask;
  private int requestCode;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (Utils.isTablet(this)) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    setContentView(R.layout.activity_multi_image);
    setTitle(R.string.image_select);
    Intent intent = getIntent();
    maxSelectNum = intent.getIntExtra(EXTRA_SELECT_COUNT, 1);
    imgSize = intent.getIntExtra(EXTRA_SIZE, 2048);
    addWaterMask = intent.getBooleanExtra(EXTRA_ADD_WATERMASK, false);
    requestCode = intent.getIntExtra(REQUEST_CODE, -1);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
          | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
      actionBar.setCustomView(R.layout.layout_image_send_btn);
      sendBtn = (TextView) actionBar.getCustomView();
      sendBtn.setEnabled(false);
    }
    sendBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        done(resultList);
      }
    });

    mPopupAnchorView = findViewById(R.id.footer);

    mTimeLineText = (TextView) findViewById(R.id.timeline_area);
    // 初始化，先隐藏当前timeline
    mTimeLineText.setVisibility(View.GONE);

    mCategoryText = (TextView) findViewById(R.id.category_btn);
    // 初始化，加载所有图片
    mCategoryText.setText(R.string.folder_all);
    mCategoryText.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (mFolderPopupWindow == null) {
          createPopupFolderList(mGridWidth, mGridHeight);
        }

        if (mFolderPopupWindow.isShowing()) {
          mFolderPopupWindow.dismiss();
        } else {
          mFolderPopupWindow.show();
          int index = mFolderAdapter.getSelectIndex();
          index = index == 0 ? index : index - 1;
          mFolderPopupWindow.getListView().setSelection(index);
        }
      }
    });

    mPreviewBtn = (TextView) findViewById(R.id.preview);
    mPreviewBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        previewSelect();
      }
    });

    mGridView = (GridView) findViewById(R.id.grid);
    mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(AbsListView absListView, int state) {

        if (state == SCROLL_STATE_IDLE) {
          // 停止滑动，日期指示器消失
          mTimeLineText.setVisibility(View.GONE);
        } else {
          mTimeLineText.setVisibility(View.VISIBLE);
        }
      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
          int totalItemCount) {
        if (mTimeLineText.getVisibility() == View.VISIBLE) {
          int index =
              firstVisibleItem + 1 == view.getAdapter().getCount() ? view.getAdapter().getCount()
                  - 1 : firstVisibleItem + 1;
          Image image = (Image) view.getAdapter().getItem(index);
          if (image != null) {
            mTimeLineText.setText(ImageUtil.formatPhotoDate(image.path));
          }
        }
      }
    });
    // 是否显示照相机
    mIsShowCamera = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, true);
    mImageAdapter = new ImageGridAdapter(this, mIsShowCamera, maxSelectNum, mGridView);
    mDirectCrop = intent.getBooleanExtra(EXTRA_DIRECT_CROP, false);
    // 是否显示选择指示器
    mImageAdapter.showSelectIndicator(!mDirectCrop);
    mGridView.setAdapter(mImageAdapter);
    mGridView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {

            final int width = mGridView.getWidth();
            final int height = mGridView.getHeight();

            mGridWidth = width;
            mGridHeight = height;

            final int desireSize = getResources().getDimensionPixelOffset(R.dimen.image_size);
            final int numCount = width / desireSize;
            final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
            int columnWidth = (width - columnSpace * (numCount - 1)) / numCount;
            mImageAdapter.setItemSize(columnWidth);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
              mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } else {
              mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
          }
        });
    mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mImageAdapter.isShowCamera()) {
          // 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
          if (i == 0) {
            PermissionUtil
                .requestPermission(ImageSelectorActivity.this, new GrantedCallback() {
                  @Override
                  public void action() {
                    showCameraAction();
                  }
                }, Manifest.permission.CAMERA);
          } else {
            // 正常操作
            Image image = (Image) adapterView.getAdapter().getItem(i);
            previewImage(image);
          }
        } else {
          // 正常操作
          Image image = (Image) adapterView.getAdapter().getItem(i);
          previewImage(image);
        }
      }
    });
    mImageAdapter.setCallback(new ImageGridAdapter.Callback() {
      @Override
      public void onItemClick(Image data) {
        selectImageFromGrid(data);
      }
    });

    mFolderAdapter = new FolderAdapter(ImageSelectorActivity.this);
    getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
    IntentFilter filter = new IntentFilter();
    filter.addAction(ACTION_SELECT_CHANGE);
    LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

    //直接打开相机
    mOpenCamera = intent.getBooleanExtra(EXTRA_TAKE_PHOTE, false);
    if (mOpenCamera) {
      PermissionUtil
          .requestPermission(ImageSelectorActivity.this, new GrantedCallback() {
            @Override
            public void action() {
              showCameraAction();
            }
          }, Manifest.permission.CAMERA);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
  }

  static final String ACTION_SELECT_CHANGE = "action_select_change";

  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action.equals(ACTION_SELECT_CHANGE)) {
        String path = intent.getStringExtra("path");
        boolean select = intent.getBooleanExtra("select", false);
        Image image = mImageAdapter.getImageByPath(path);
        if (image != null) {
          selectImageFromGrid(image);
        }
      }
    }
  };

  /**
   * 创建弹出的ListView
   */
  private void createPopupFolderList(int width, int height) {
    mFolderPopupWindow = new ListPopupWindow(this);
    mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    mFolderPopupWindow.setAdapter(mFolderAdapter);
    mFolderPopupWindow.setContentWidth(width);
    mFolderPopupWindow.setWidth(width);
    mFolderPopupWindow.setHeight(height * 5 / 8);
    mFolderPopupWindow.setAnchorView(mPopupAnchorView);
    mFolderPopupWindow.setModal(true);
    mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        mFolderAdapter.setSelectIndex(i);

        final int index = i;
        final AdapterView v = adapterView;

        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            mFolderPopupWindow.dismiss();

            if (index == 0) {
              getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
              mCategoryText.setText(R.string.folder_all);
              if (mIsShowCamera) {
                mImageAdapter.setShowCamera(true);
              } else {
                mImageAdapter.setShowCamera(false);
              }
            } else {
              Folder folder = (Folder) v.getAdapter().getItem(index);
              if (null != folder) {
                mImageAdapter.setData(folder.images);
                mCategoryText.setText(folder.name);
                // 设定默认选择
                if (resultList != null && resultList.size() > 0) {
                  mImageAdapter.setDefaultSelected(resultList);
                }
              }
              mImageAdapter.setShowCamera(false);
            }

            // 滑动到最初始位置
            mGridView.smoothScrollToPosition(0);
          }
        }, 100);

      }
    });
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    Log.d(TAG, "on change");

    if (mFolderPopupWindow != null) {
      if (mFolderPopupWindow.isShowing()) {
        mFolderPopupWindow.dismiss();
      }
    }

    mGridView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {

            final int height = mGridView.getHeight();

            final int desireSize = getResources().getDimensionPixelOffset(R.dimen.image_size);
            Log.d(TAG, "Desire Size = " + desireSize);
            final int numCount = mGridView.getWidth() / desireSize;
            Log.d(TAG, "Grid Size = " + mGridView.getWidth());
            Log.d(TAG, "num count = " + numCount);
            final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
            int columnWidth = (mGridView.getWidth() - columnSpace * (numCount - 1)) / numCount;
            mImageAdapter.setItemSize(columnWidth);

            if (mFolderPopupWindow != null) {
              mFolderPopupWindow.setHeight(height * 5 / 8);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
              mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } else {
              mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
          }
        });

    super.onConfigurationChanged(newConfig);

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      setResult(RESULT_CANCELED);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showProgress(String message) {
    mDialog = DialogUtil.showLoadingDialog(this, message, false, null);
  }

  private void hideProgress() {
    if (mDialog != null) {
      mDialog.dismissAllowingStateLoss();
    }
  }

  private class SaveTask extends AsyncTask<String, Void, Void> {

    private ArrayList<String> resultList;
    private ArrayList<Result> results = new ArrayList<>();

    private SaveTask(ArrayList<String> resultList) {
      this.resultList = resultList;
    }

    @Override
    protected void onPreExecute() {
      showProgress(getString(R.string.image_process));
    }

    private File createImageFile(int index) {
      // Create an image file name
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
      String imageFileName = "image_" + index + "_" + timeStamp + ".jpg";
      return new File(getCacheDir(), imageFileName);
    }

    @Override
    protected Void doInBackground(String... params) {
      int i = 0;
      for (String path : resultList) {
        Result result = new Result();
        try {
          if (ImageUtil.isGif(path)) {
            Log.d(TAG, "gif: " + path);
            result.path = path;
          } else {
            File file = createImageFile(i);
            Bitmap bitmap = ImageUtil.decodeBitmapFromFile(path, imgSize);
            Log.d(TAG, "bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            Bitmap zoom = ImageUtil.zoomBitmap(bitmap, imgSize);
            Log.d(TAG, "zoom size: " + zoom.getWidth() + "x" + zoom.getHeight());
            ImageUtil.compressBmpToFile(zoom, Bitmap.CompressFormat.JPEG, 90, file);
            zoom.recycle();
            result.path = file.getAbsolutePath();
          }

          ExifInterface exifInterface = new ExifInterface(path);
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

          Bitmap bt = ImageUtil.rotateBitmap(BitmapFactory.decodeFile(path),
              ImageUtil.getBitmapDegree(path));
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

//          ExifInterface newExif = new ExifInterface(result.path);
//          newExif.setAttribute(ExifInterface.TAG_ORIENTATION, degree);
          newExif.saveAttributes();
          String attribute = newExif.getAttribute(ExifInterface.TAG_ORIENTATION);
          Log.i("TAG", "SaveTask doInBackground() attribute = " + attribute);
          if (!TextUtils.isEmpty(la) && !TextUtils.isEmpty(lo)) {
            result.gps = la + "," + lo;
          }

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
                  srcBmpWidth - newWidth - Utils.dp2px(ImageSelectorActivity.this, 5),
                  srcBmpHeight - newHeight - Utils.dp2px(ImageSelectorActivity.this, 5), paint);
              //创建路径
              String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
                  .format(new Date());
              String imageFileName = "edit_image_" + i + "_" + timeStamp + ".jpg";
              File file = new File(getCacheDir(), imageFileName);
              //更新路劲
              ImageUtil.compressBmpToFile(dst, Bitmap.CompressFormat.JPEG, 100, file);
              result.path = file.getPath();
              result.width = srcBmpWidth;
              result.height = srcBmpHeight;
              result.size = file.length();
            }
          }

          i++;
        } catch (Exception e) {
          e.printStackTrace();
          result.path = path;
        }
        results.add(result);
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      hideProgress();
      if (requestCode == -1) {
        Intent data = new Intent();
        data.putExtra(RESULT_LIST, results);
        setResult(RESULT_OK, data);
      } else {
        EventBus.getDefault().post(new ImageSelectResultEvent(results, requestCode));
      }
      finish();
    }
  }

//    private int readPictureDegree(String path) {
//        int degree = 0;
//        try {
//            ExifInterface exifInterface = new ExifInterface(path);
//
//            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    degree = 90;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    degree = 180;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    degree = 270;
//                    break;
//                default:
//                    degree = 0;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return degree;
//    }

  private void done(ArrayList<String> resultList) {
    new SaveTask(resultList).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CAMERA) {
      if (resultCode == Activity.RESULT_OK) {
        if (mTmpFile != null) {
          previewPhoto();
        }
      } else {
        if (mTmpFile != null && mTmpFile.exists()) {
          mTmpFile.delete();
        }
      }
    } else if (requestCode == REQUEST_PREVIEW) {
      if (resultCode == RESULT_OK && data != null) {
        ArrayList<String> list = data
            .getStringArrayListExtra(ImagePreviewActivity.EXTRA_SELECTED_IMAGES);
        done(list);
      }
      return;
    } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        setResult(RESULT_OK, data);
      } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
        Utils.toast(this, getString(R.string.image_not_edit));
      }
      finish();
      return;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  //图片列表，可能太大，不通过intent传递，直接取
  public static ArrayList<String> displayImages = new ArrayList<>();

  /**
   * 预览拍照
   */
  private void previewPhoto() {
    displayImages.clear();
    displayImages.add(mTmpFile.getAbsolutePath());
    Intent intent = new Intent(this, ImagePreviewActivity.class);
    intent.putExtra(ImagePreviewActivity.EXTRA_SELECTED_IMAGES, displayImages);
    intent.putExtra(ImagePreviewActivity.EXTRA_SHOW_SELECT, false);
    startActivityForResult(intent, REQUEST_PREVIEW);
  }

  /**
   * 点击图片预览
   */
  private void previewImage(Image image) {
    if (image != null) {
      if (image.path != null) {
        ArrayList<String> images = mImageAdapter.getImagePath();
        if (images != null) {
          displayImages.clear();
          displayImages.addAll(images);
          if (mDirectCrop) {
            crop(image.path);
          } else {
            Intent intent = new Intent(this, ImagePreviewActivity.class);
            intent.putExtra(ImagePreviewActivity.EXTRA_SELECTED_IMAGES, resultList);
            intent.putExtra(ImagePreviewActivity.EXTRA_POSITION, displayImages.indexOf(image.path));
            intent.putExtra(ImagePreviewActivity.EXTRA_MAX_COUNT, maxSelectNum);
            startActivityForResult(intent, REQUEST_PREVIEW);
          }
        }
      }
    }
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
        .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
        .setOutputCompressQuality(100)
        .setAspectRatio(1, 1)
        .setFixAspectRatio(true)
        .setMinCropWindowSize(minSize, minSize)
        .setAutoZoomEnabled(true)
        .setMaxZoom(10)
        .start(this);
  }

  /**
   * 预览已选择图片
   */
  private void previewSelect() {
    displayImages.clear();
    displayImages.addAll(resultList);
    Intent intent = new Intent(this, ImagePreviewActivity.class);
    intent.putExtra(ImagePreviewActivity.EXTRA_SELECTED_IMAGES, displayImages);
    intent.putExtra(ImagePreviewActivity.EXTRA_MAX_COUNT, maxSelectNum);
    startActivityForResult(intent, REQUEST_PREVIEW);
  }

  private static File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
    String imageFileName = "JPEG_" + timeStamp + ".jpg";
    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    if (!storageDir.exists()) {
      if (!storageDir.mkdir()) {
        throw new IOException();
      }
    }
    return new File(storageDir, imageFileName);
  }

  /**
   * 选择相机
   */
  private void showCameraAction() {
    try {
      // 跳转到系统照相机
      Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      if (cameraIntent.resolveActivity(getPackageManager()) != null) {
        // 设置系统相机拍照后的输出路径
        // 创建临时文件
        mTmpFile = createImageFile();
        Uri fileUri = FileProvider7.getUriForFile(this, mTmpFile);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
      } else {
        Utils.toast(ImageSelectorActivity.this, getString(R.string.msg_no_camera));
      }
    } catch (IOException e) {
      e.printStackTrace();
      Utils.toast(ImageSelectorActivity.this, getString(R.string.msg_no_camera));
    }
  }

  /**
   * 多选模式下选择图片操作
   */
  private void selectImageFromGrid(Image image) {
    if (image != null) {
      if (resultList.contains(image.path)) {
        resultList.remove(image.path);
        if (resultList.size() != 0) {
          sendBtn.setEnabled(true);
          mPreviewBtn.setEnabled(true);
          sendBtn.setText(getString(R.string.image_done_num, resultList.size(), maxSelectNum));
          mPreviewBtn.setText(getString(R.string.image_preview_num, resultList.size()));
        } else {
          sendBtn.setEnabled(false);
          mPreviewBtn.setEnabled(false);
          sendBtn.setText(R.string.send_image);
          mPreviewBtn.setText(R.string.preview);
        }
      } else {
        // 判断选择数量问题
        if (maxSelectNum == resultList.size()) {
          if (maxSelectNum == 1) {
            resultList.clear();
          } else {
            Utils.toast(ImageSelectorActivity.this, getString(R.string.msg_amount_limit));
            return;
          }
        }

        resultList.add(image.path);
        sendBtn.setEnabled(true);
        mPreviewBtn.setEnabled(true);
        sendBtn.setText(getString(R.string.image_done_num, resultList.size(), maxSelectNum));
        mPreviewBtn.setText(getString(R.string.image_preview_num, resultList.size()));
      }
      mImageAdapter.select(image, mGridView);
    }
  }

  private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

    private final String[] IMAGE_PROJECTION = {
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_MODIFIED,
        MediaStore.Images.Media._ID};

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      if (id == LOADER_ALL) {
        CursorLoader cursorLoader = new CursorLoader(ImageSelectorActivity.this,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
            null, null, IMAGE_PROJECTION[2] + " DESC");
        return cursorLoader;
      } else if (id == LOADER_CATEGORY) {
        CursorLoader cursorLoader = new CursorLoader(ImageSelectorActivity.this,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
            IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'",
            null, IMAGE_PROJECTION[2] + " DESC");
        return cursorLoader;
      }

      return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      if (data != null) {
        List<Image> images = new ArrayList<>();
        int count = data.getCount();
        if (count > 0) {
          data.moveToFirst();
          do {
            String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
            String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
            long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
            if (path != null) {
              Image image = new Image(path, name, dateTime);
              images.add(image);
              if (!hasFolderGened) {
                // 获取文件夹名称
                File imageFile = new File(path);
                File folderFile = imageFile.getParentFile();
                if (folderFile != null) {
                  Folder folder = new Folder();
                  folder.name = folderFile.getName();
                  folder.path = folderFile.getAbsolutePath();
                  folder.cover = image;
                  if (!mResultFolder.contains(folder)) {
                    List<Image> imageList = new ArrayList<>();
                    imageList.add(image);
                    folder.images = imageList;
                    mResultFolder.add(folder);
                  } else {
                    // 更新
                    Folder f = mResultFolder.get(mResultFolder.indexOf(folder));
                    f.images.add(image);
                  }
                }
              }
            }

          } while (data.moveToNext());

          mImageAdapter.setData(images);

          // 设定默认选择
          if (resultList != null && resultList.size() > 0) {
            mImageAdapter.setDefaultSelected(resultList);
          }

          mFolderAdapter.setData(mResultFolder);
          hasFolderGened = true;

        }
      }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
  };

}
