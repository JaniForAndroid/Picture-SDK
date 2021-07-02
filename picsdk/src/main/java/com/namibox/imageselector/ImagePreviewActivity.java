package com.namibox.imageselector;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.picsdk.R;
import com.namibox.commonlib.constant.Events;
import com.namibox.commonlib.dialog.DialogUtil;
import com.namibox.imageselector.camera.CameraActivity;
import com.namibox.imageselector.cropper.CropImage;
import com.namibox.tools.ThinkingAnalyticsHelper;
import com.namibox.util.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import com.yalantis.ucrop.UCrop;

/**
 * Created by ryan on 2015/2/14.
 */
public class ImagePreviewActivity extends AppCompatActivity {

  private static final String TAG = "ImagePreviewActivity";
  //public static final String EXTRA_IMAGES = "extra_images";
  public static final String EXTRA_SELECTED_IMAGES = "extra_selected_images";
  public static final String EXTRA_POSITION = "extra_position";
  public static final String EXTRA_MAX_COUNT = "extra_max_count";
  public static final String EXTRA_SHOW_SELECT = "extra_show_select";
  public static final String EXTRA_FROM_CAMERA = "extra_from_camera";
  private ArrayList<String> images;
  private ArrayList<String> selectedImages;
  private int maxCount;
  private boolean showSelect;
  private ViewPager viewPager;
  private ImageAdapter adapter;
  private View bottomBar;
  private CheckBox selectBtn;
  private TextView sendButton;
  private int width, height;
  private boolean hasModify;

  private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
      update();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (Utils.isTablet(this)) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    Intent intent = getIntent();
    int position = intent.getIntExtra(EXTRA_POSITION, 0);
    //images = intent.getStringArrayListExtra(EXTRA_IMAGES);
    boolean fromCamera = intent.getBooleanExtra(EXTRA_FROM_CAMERA, false);
    images = fromCamera ? CameraActivity.displayImages : ImageSelectorActivity.displayImages;
    selectedImages = intent.getStringArrayListExtra(EXTRA_SELECTED_IMAGES);
    maxCount = intent.getIntExtra(EXTRA_MAX_COUNT, 1);
    showSelect = intent.getBooleanExtra(EXTRA_SHOW_SELECT, true);
    setResult(RESULT_CANCELED, null);
    if (images == null || images.isEmpty()) {
      cannotLoadImage();
      return;
    }
    setContentView(R.layout.activity_image_preview);
    setTitle(R.string.image_preview);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSize();
    viewPager = (ViewPager) findViewById(R.id.viewpager);
    bottomBar = findViewById(R.id.bottom_bar);
    selectBtn = (CheckBox) findViewById(R.id.select_checkbox);
    selectBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        changeSelect(isChecked);
      }
    });
    sendButton = (TextView) findViewById(R.id.send_button);
    sendButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        done();
      }
    });
    selectBtn.setVisibility(showSelect ? View.VISIBLE : View.GONE);
    update();
    adapter = new ImageAdapter();
    viewPager.setAdapter(adapter);
    viewPager.addOnPageChangeListener(pageChangeListener);
    viewPager.setCurrentItem(position);
  }

  private void cannotLoadImage() {
    Utils.toast(this, getString(R.string.cannot_load_image));
    finish();
  }

  private void done() {

//        for (int i = 0; i < selectedImages.size(); i++) {
//            String path = selectedImages.get(i);
//            Bitmap srcBmp = BitmapFactory.decodeFile(path);
//            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            int srcBmpWidth = srcBmp.getWidth();
//            int srcBmpHeight = srcBmp.getHeight();
//            Bitmap dst = Bitmap.createBitmap(srcBmpWidth, srcBmpHeight, Bitmap.Config.ARGB_8888);
//            //创建画布，画水印
//            Canvas c = new Canvas(dst);
//            RectF rectF = new RectF(0, 0, srcBmpWidth, srcBmpHeight);
//            c.drawBitmap(srcBmp, null, rectF, paint);
//            //获取水印的bitmap
//            Bitmap waterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_water_mask);
//            int width = waterBitmap.getWidth();
//            int height = waterBitmap.getHeight();
//
//            c.drawBitmap(waterBitmap, srcBmpWidth - width - 20, srcBmpHeight - height - 20, paint);
//            //创建路径
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
//            String imageFileName = "edit_image_" + timeStamp + ".jpg";
//            File file = new File(getCacheDir(), imageFileName);
//            //更新路劲
//            Utils.compressBmpToFile(dst, Bitmap.CompressFormat.JPEG, 100, file);
//            selectedImages.set(i,file.getPath());
//        }
    Map<String, Object> map = new HashMap<>();
    map.put("page", "编辑页面");
    map.put("button", "完成");
    map.put("channel", "其他");
    ThinkingAnalyticsHelper.trackEvent(Events.TA_EVENT_NB_APP_CLICK, map);

    Intent intent = new Intent();
    intent.putExtra(EXTRA_SELECTED_IMAGES, selectedImages);
    setResult(RESULT_OK, intent);
    finish();
  }


  private void changeSelect(boolean select) {
    String path = images.get(viewPager.getCurrentItem());
    if (select) {
      if (selectedImages == null) {
        selectedImages = new ArrayList<>();
        selectedImages.add(path);
        sendIntent(path);
      } else if (!selectedImages.contains(path)) {
        if (maxCount == selectedImages.size()) {
          Utils.toast(ImagePreviewActivity.this, getString(R.string.msg_amount_limit));
          selectBtn.setChecked(false);
          return;
        }
        selectedImages.add(path);
        sendIntent(path);
      }
    } else {
      if (selectedImages != null && selectedImages.contains(path)) {
        selectedImages.remove(path);
        sendIntent(path);
      }
    }
    update();
  }

  private void sendIntent(String path) {
    Intent intent = new Intent(ImageSelectorActivity.ACTION_SELECT_CHANGE);
    intent.putExtra("path", path);
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
  }

  private void update() {
    int page = viewPager.getCurrentItem() + 1;
    setTitle(page + "/" + images.size());
    String path = images.get(viewPager.getCurrentItem());
    if (selectedImages != null && selectedImages.contains(path)) {
      selectBtn.setChecked(true);
    } else {
      selectBtn.setChecked(false);
    }
    if (selectedImages == null || selectedImages.isEmpty()) {
      sendButton.setText(R.string.send_image);
      sendButton.setEnabled(false);
    } else {
      sendButton.setEnabled(true);
      sendButton.setText(getString(R.string.image_done_num, selectedImages.size(), maxCount));
    }
  }

  private void getSize() {
    Display display = getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    width = size.x;
    height = size.y;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, 100, 1, R.string.edit_image)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      Map<String, Object> map = new HashMap<>();
      map.put("page", "编辑页面");
      map.put("button", "取消");
      map.put("channel", "其他");
      ThinkingAnalyticsHelper.trackEvent(Events.TA_EVENT_NB_APP_CLICK, map);
      onBackPressed();
      return true;
    } else if (item.getItemId() == 100) {
//            uCrop();
      //直接跳转编辑页面，不跳转裁剪页面
      Intent intent = new Intent(this, ImageEditActivity.class);
      intent.putExtra("position", viewPager.getCurrentItem());
      intent.putExtra("original_path", images.get(viewPager.getCurrentItem()));
      intent.putExtra("path", images.get(viewPager.getCurrentItem()));
      startActivityForResult(intent, REQUEST_EDIT);

      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    if (hasModify) {
      showBackDialog();
      return;
    }
    super.onBackPressed();
  }

  private void showBackDialog() {
    DialogUtil.showButtonDialog(this, getString(R.string.picker_tip),
        getString(R.string.picker_exit_tip),
        getString(R.string.picker_confirm),
        new OnClickListener() {
          @Override
          public void onClick(View view) {
            finish();
          }
        },
        getString(R.string.picker_cancel),
        null, new OnClickListener() {
          @Override
          public void onClick(View v) {

          }
        });
  }

//    private void uCrop() {
//        Uri uri = Uri.fromFile(new File(images.get(viewPager.getCurrentItem())));
//        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "crop_image_tmp.jpg"));
//        int minSize = Utils.dp2px(this, 100);
//        //返回一个ActivityBuilder
//        CropImage.activity(uri)
//                .setGuidelines(CropImageView.Guidelines.ON)
//                .setOutputUri(destinationUri)
//                .setCropShape(CropImageView.CropShape.RECTANGLE)
//                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
//                .setOutputCompressQuality(90)
//                .setMinCropWindowSize(minSize, minSize)
//                .setAutoZoomEnabled(true)
//                .setMaxZoom(4)
//                .start(this);
//        UCrop.Options options = new UCrop.Options();
//        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
//        options.setCompressionQuality(95);
//        options.setToolbarColor(ContextCompat.getColor(this, R.color.actionbar_bg));
//        options.setStatusBarColor(ContextCompat.getColor(this, R.color.statusbar_bg));
//        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.widget_color));
//        UCrop uCrop = UCrop.of(uri, destinationUri)
//                .withOptions(options);
//        uCrop.start(this);
//    }

  private static final int REQUEST_EDIT = 1000;

  private void openEdit(Uri resultUri) {
    Intent intent = new Intent(this, ImageEditActivity.class);
    intent.putExtra("position", viewPager.getCurrentItem());
    intent.putExtra("original_path", images.get(viewPager.getCurrentItem()));
    intent.putExtra("path", resultUri.getPath());
    //打印图片的url地址
    Log.e("TAG", "original_path = " + images.get(viewPager.getCurrentItem()));
    Log.e("TAG", "path = " + resultUri.getPath());
    startActivityForResult(intent, REQUEST_EDIT);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == UCrop.REQUEST_CROP) {
//            if (resultCode == RESULT_OK) {
//                final Uri resultUri = UCrop.getOutput(data);
//                if (resultUri != null) {
//                    openEdit(resultUri);
//                } else {
//                    Toast.makeText(ImagePreviewActivity.this, "无法编辑图片", Toast.LENGTH_SHORT).show();
//                }
//            } else if (resultCode == UCrop.RESULT_ERROR) {
//                Toast.makeText(ImagePreviewActivity.this, "无法编辑图片", Toast.LENGTH_SHORT).show();
//            }
    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
      CropImage.ActivityResult result = CropImage.getActivityResult(data);
      if (resultCode == RESULT_OK) {
        if (result == null || result.getUri() == null) {
          Uri uri = Uri.fromFile(new File(images.get(viewPager.getCurrentItem())));
          openEdit(uri);
        } else {
          openEdit(result.getUri());
        }
      } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
        Utils.toast(ImagePreviewActivity.this, "无法编辑图片");
      }
    } else if (requestCode == REQUEST_EDIT) {
      if (resultCode == RESULT_OK && data != null) {
        int position = data.getIntExtra("position", viewPager.getCurrentItem());
        String original_path = data.getStringExtra("original_path");
        String new_path = data.getStringExtra("path");
        hasModify = data.getBooleanExtra("hasModify", false);
        images.set(position, new_path);
        if (selectedImages != null && selectedImages.contains(original_path)) {
          int index = selectedImages.indexOf(original_path);
          selectedImages.set(index, new_path);
        }
        adapter.updateView(position);
        return;
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  private class ImageAdapter extends PagerAdapter {

    private PhotoView[] photoViews = new PhotoView[images.size()];

    @Override
    public int getCount() {
      return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
      return view == object;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
    }

    public void updateView(final int position) {
      File file = new File(images.get(position));
      RequestOptions options = new RequestOptions()
          .placeholder(R.drawable.default_error)
          .error(R.drawable.default_error)
          .skipMemoryCache(true)
          .centerInside()
          .diskCacheStrategy(DiskCacheStrategy.NONE);
      Glide.with(ImagePreviewActivity.this)
          .load(file)
          .apply(options)
          .into(photoViews[position]);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
      View view = LayoutInflater.from(ImagePreviewActivity.this)
          .inflate(R.layout.layout_image_preview_item, container, false);
      PhotoView photoView = (PhotoView) view.findViewById(R.id.photo_view);
      photoView.enable();
      photoView.setMaxScale(3);
      container.addView(view);
      photoViews[position] = photoView;
      photoViews[position].setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          toggleNavigation();
        }
      });
      updateView(position);
      return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      container.removeView((View) object);
      photoViews[position] = null;
    }
  }

  private void hideNavigation() {
    bottomBar.setVisibility(View.GONE);
    getSupportActionBar().hide();
  }

  private void showNavigation() {
    bottomBar.setVisibility(View.VISIBLE);
    getSupportActionBar().show();
  }

  private void toggleNavigation() {
    if (bottomBar.getVisibility() == View.VISIBLE) {
      hideNavigation();
    } else {
      showNavigation();
    }
  }

}
