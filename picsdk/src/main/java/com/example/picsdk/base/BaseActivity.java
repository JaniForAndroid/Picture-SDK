package com.example.picsdk.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import android.support.annotation.Nullable;
import com.example.picsdk.R;
import com.google.gson.JsonObject;
import com.namibox.commonlib.activity.AbsFunctionActivity;
import com.namibox.tools.DensityUtils;
import com.namibox.util.Utils;

import io.reactivex.disposables.CompositeDisposable;

/**
 * author : feng
 * creation time : 19-9-9上午9:59
 */
@SuppressLint("Registered")
public class BaseActivity extends AbsFunctionActivity {

  private static final String CMD_OPEN_VIEW = "openview";
  private static final String CMD_CLOSE_VIEW = "closeview";

  private ImageView iv_action_back;
  private TextView tv_action_title;
  private ImageView iv_action_feature;
  private TextView tv_right_text;

  public CompositeDisposable compositeDisposable = new CompositeDisposable();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    DensityUtils.setEnable(false);
    super.onCreate(savedInstanceState);
    setDarkStatusIcon(true);
  }

  public void initActionBar() {
    iv_action_back = findViewById(R.id.iv_action_back);
    tv_action_title = findViewById(R.id.tv_action_title);
    iv_action_feature = findViewById(R.id.iv_action_feature);
    if (iv_action_back != null) {
      iv_action_back.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          onActionBack();
        }
      });
    }
    tv_right_text = findViewById(R.id.tv_right_text);
    tv_right_text.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        onRightTextClick();
      }
    });
  }

  public void onRightTextClick() {

  }

  public void setRightText(String rightText) {
    tv_right_text.setVisibility(View.VISIBLE);
    tv_right_text.setText(rightText);
  }

  public void disableBack() {
    if (iv_action_back != null) {
      iv_action_back.setVisibility(View.GONE);
    }
  }

  public void enableBack() {
    if (iv_action_back != null) {
      iv_action_back.setVisibility(View.VISIBLE);
    }
  }

  protected void onActionBack() {
    onBackPressed();
  }

  public void setActionBackResource(int resId) {
    iv_action_back.setImageResource(resId);
  }

  public void setActionTitle(String title) {
    tv_action_title.setVisibility(View.VISIBLE);
    tv_action_title.setText(title);
  }

  public void setActionTitle(SpannableString title) {
    tv_action_title.setVisibility(View.VISIBLE);
    tv_action_title.setText(title);
  }

  public void setActionFeature(int resId, OnClickListener listener) {
    iv_action_feature.setVisibility(View.VISIBLE);
    iv_action_feature.setImageResource(resId);
    iv_action_feature.setOnClickListener(listener);
  }

  public void handleAction(JsonObject action) {
    if (action == null) {
      return;
    }
    String command;
    if (action.has("command")) {
      command = action.get("command").getAsString();
    } else {
      command = CMD_OPEN_VIEW;
    }
    switch (command) {
      case CMD_OPEN_VIEW:
        if (action.has("url")) {
          String url = action.get("url").getAsString();
          openView(url);
        }
        break;
      case CMD_CLOSE_VIEW:
        break;
      default:
        break;
    }
  }

  public void toast(String msg) {
    Utils.toast(this, msg);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    compositeDisposable.dispose();
  }
}
