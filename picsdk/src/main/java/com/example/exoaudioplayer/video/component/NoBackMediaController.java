package com.example.exoaudioplayer.video.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.picsdk.R;

public class NoBackMediaController extends StandardMediaController implements View.OnClickListener {

  private ImageView back;

  public NoBackMediaController(@NonNull Context context) {
    this(context, null);
  }

  public NoBackMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public NoBackMediaController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.player_layout_standard_controller;
  }

  @Override
  protected void initView() {
    super.initView();
    back = findViewById(R.id.back);
    back.setVisibility(GONE);
  }

  @Override
  public void onClick(View v) {
    int i = v.getId();
    if (i == R.id.lock) {
      mControlWrapper.toggleLockState();
    }
  }
}
