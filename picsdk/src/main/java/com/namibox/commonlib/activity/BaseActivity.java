package com.namibox.commonlib.activity;

import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.picsdk.R;
import com.namibox.util.Utils;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.sevenheaven.segmentcontrol.SegmentControl;

/**
 * Create time: 2017/2/7.
 */

public abstract class BaseActivity extends AbsFunctionActivity implements Handler.Callback{

  protected SegmentControl segmentControl;
  private View dividerView;
  private ImageView backView;
  private TextView titleView, subTitleView;
  private View menuView1;
  private View titleLayout;
  private TextView menuTextView1;
  private ImageView menuImageView1;
  private SystemBarTintManager tintManager;
  protected boolean darkStatusIcon = true;
  protected int themeColor;
  protected int statusbarColor;
  protected int toolbarColor;
  protected int toolbarContentColor;
  protected int screenMode;
  private boolean showStatusBar = true;
  protected Handler handler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
      //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
      tintManager = new SystemBarTintManager(this);
      tintManager.setStatusBarTintColor(statusbarColor);
      tintManager.setStatusBarTintEnabled(true);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().setStatusBarColor(statusbarColor);
    }
    setDarkStatusIcon(darkStatusIcon);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
          ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_CUSTOM
              | ActionBar.DISPLAY_SHOW_TITLE);
      View v = LayoutInflater.from(this).inflate(R.layout.layout_custom_actionbar, null);
      backView = (ImageView) v.findViewById(R.id.back);
      titleView = (TextView) v.findViewById(R.id.title);
      subTitleView = (TextView) v.findViewById(R.id.subtitle);
      dividerView = v.findViewById(R.id.divider);
      titleLayout = v.findViewById(R.id.titleLayout);
      segmentControl = (SegmentControl) v.findViewById(R.id.segment_control);
      titleView.setTextColor(toolbarContentColor);
      subTitleView.setTextColor(toolbarContentColor);
      backView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          onBackPressed();
        }
      });
      backView.setImageResource(
          darkStatusIcon ? R.drawable.ic_arrow_back_black : R.drawable.ic_arrow_back_white);
      menuView1 = v.findViewById(R.id.menu1);
      menuTextView1 = (TextView) v.findViewById(R.id.menu_text1);
      menuImageView1 = (ImageView) v.findViewById(R.id.menu_img1);
      ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT);
      getSupportActionBar().setCustomView(v, lp);
      getSupportActionBar().setBackgroundDrawable(new ColorDrawable(toolbarColor));
      menuTextView1.setTextColor(toolbarContentColor);
      dividerView.setVisibility(darkStatusIcon ? View.VISIBLE : View.GONE);
    }
    handler = new Handler(this);
  }

  protected void setTintManagerStatus(boolean status) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      tintManager.setStatusBarTintEnabled(status);
    }

  }

  @Override
  protected void setThemeColor() {
    //自定义主题色
    themeColor = Utils.getThemeColor(this, R.color.theme_color);
    toolbarColor = ContextCompat.getColor(this, R.color.toolbar_color);
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && !Utils.isMIUI()) {
      statusbarColor = ContextCompat.getColor(this, R.color.statusbar_color);
    } else {
      statusbarColor = toolbarColor;
    }
    toolbarContentColor = ContextCompat.getColor(this, R.color.toolbar_content_color);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return false;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    return false;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return true;
  }

  @Override
  public void setTitle(CharSequence title) {
    titleView.setText(title);
  }

  public void setSubTitle(CharSequence title) {
    subTitleView.setText(title);
    subTitleView.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
  }

  public void setImgMenu(int resId, View.OnClickListener listener) {
    menuView1.setVisibility(View.VISIBLE);
    menuTextView1.setVisibility(View.GONE);
    int padding = Utils.dp2px(this, 0);
    menuImageView1.setPadding(padding, padding, padding, padding);
    menuImageView1.setVisibility(View.VISIBLE);
    menuImageView1.setImageResource(resId);
    menuView1.setOnClickListener(listener);
  }

  public void hideImageMenu() {
    menuImageView1.setVisibility(View.GONE);
  }

  public void showImageMenu() {
    menuImageView1.setVisibility(View.VISIBLE);
  }

  public void setMenu(String title, boolean border, View.OnClickListener listener) {
    menuView1.setVisibility(View.VISIBLE);
    menuImageView1.setVisibility(View.GONE);
    menuTextView1.setText(title);
    menuTextView1.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
    menuView1.setOnClickListener(listener);
    if (border) {
      if (darkStatusIcon) {
        menuTextView1.setBackgroundResource(R.drawable.menu_item_dark_bg);
      } else {
        menuTextView1.setBackgroundResource(R.drawable.menu_item_light_bg);
      }

    } else {
      int[] attrs = new int[]{R.attr.selectableItemBackground};
      TypedArray typedArray = obtainStyledAttributes(attrs);
      int backgroundResource = typedArray.getResourceId(0, 0);
      typedArray.recycle();
      menuTextView1.setBackgroundResource(backgroundResource);
    }
  }

  public void hideMenu() {
    menuView1.setVisibility(View.GONE);
  }

  public void showMenu(){
    menuView1.setVisibility(View.VISIBLE);
  }

  public void initSegmentControl(
      SegmentControl.OnSegmentControlClickListener onSegmentControlClickListener, String... texts) {
    initSegmentControl(onSegmentControlClickListener, 0, texts);
  }

  public void initSegmentControl(
      SegmentControl.OnSegmentControlClickListener onSegmentControlClickListener, int selectedIndex,
      String... texts) {
    if (texts.length > 0) {
      titleLayout.setVisibility(View.GONE);
      segmentControl.setVisibility(View.VISIBLE);
      segmentControl.setText(texts);
      segmentControl.setOnSegmentControlClickListener(onSegmentControlClickListener);
      if (selectedIndex < texts.length) {
        segmentControl.setSelectedIndex(selectedIndex);
      } else {
        segmentControl.setSelectedIndex(0);
      }
    }
  }

  public SystemBarTintManager getTintManager() {
    return tintManager;
  }

  public int getScreenMode() {
    return screenMode;
  }

  protected void setScreenMode(int mode) {
    screenMode = mode;
  }

  //显示隐藏状态栏
  public void showStatusBar(boolean show) {
    if (showStatusBar == show) {
      return;
    }
    showStatusBar = show;
    if (show) {
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    } else {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    handler.removeCallbacksAndMessages(null);
  }

  @Override
  public boolean handleMessage(Message msg) {
    return false;
  }
}
