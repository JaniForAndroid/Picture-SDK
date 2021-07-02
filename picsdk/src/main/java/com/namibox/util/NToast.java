package com.namibox.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.picsdk.R;

public class NToast extends FrameLayout {

  private View contentView;
  private TextView textView;
  private ViewGroup parent;
  private boolean anim;
  public static final int TYPE_1 = 0;
  public static final int TYPE_2 = 1;

  public NToast(Activity activity, int type) {
    super(activity);

    parent = activity.findViewById(android.R.id.content);
    if (type == TYPE_1) {
      addContentView1();
    } else {
      addContentView2();
    }
    LayoutParams l = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
    l.gravity = Gravity.CENTER;
    contentView.setLayoutParams(l);
    addView(contentView);
    l = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    setLayoutParams(l);
    parent.addView(this);
  }

  private void addContentView1() {
    textView = new TextView(getContext());
    textView.setBackgroundResource(R.drawable.toast_bg);
    textView.setGravity(Gravity.CENTER);
    int padding1 = Utils.dp2px(getContext(), 10);
    int padding2 = Utils.dp2px(getContext(), 18);
    textView.setPadding(padding2, padding1, padding2, padding1);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.toast_size));
    textView.setVisibility(INVISIBLE);
    textView.setTextColor(0xffffffff);
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      textView.setElevation(Utils.dp2px(getContext(), 6));
    }
    contentView = textView;
  }

  private void addContentView2() {
    textView = new TextView(getContext());
    textView.setBackgroundResource(R.drawable.toast_bg2);
    textView.setGravity(Gravity.CENTER);
    int padding = Utils.dp2px(getContext(), 16);
    textView.setPadding(padding, padding, padding, padding);
    textView.setTextSize(14);
    textView.setVisibility(INVISIBLE);
    textView.setTextColor(0xffffffff);
    textView.setCompoundDrawablePadding(padding);
    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.toast_icon, 0, 0);
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      textView.setElevation(Utils.dp2px(getContext(), 6));
    }
    contentView = textView;
  }

  public void show(String text) {
    setVisibility(VISIBLE);
    contentView.setVisibility(VISIBLE);
    textView.setText(text);
    if (anim) {
      return;
    }
    contentView.setAlpha(0f);
    anim = true;
    contentView.animate()
        .alpha(1f)
        .setDuration(500)
        .setStartDelay(0)
        .setInterpolator(new LinearInterpolator())
        .setListener(null)
        .setListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            contentView.animate()
                .alpha(0f)
                .setDuration(500)
                .setInterpolator(new LinearInterpolator())
                .setStartDelay(1500)
                .setListener(null)
                .setListener(new AnimatorListenerAdapter() {
                  @Override
                  public void onAnimationEnd(Animator animation) {
                    contentView.setVisibility(GONE);
                    setVisibility(GONE);
                    anim = false;
                  }
                })
                .start();
          }
        })
        .start();
  }

  public static void toast(Context context, String msg) {
    try {
      TextView textView = new TextView(context.getApplicationContext());
      textView.setBackgroundResource(R.drawable.toast_bg);
      textView.setGravity(Gravity.CENTER);
      int padding1 = Utils.dp2px(context, 10);
      int padding2 = Utils.dp2px(context, 18);
      textView.setPadding(padding2, padding1, padding2, padding1);
      textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.toast_size));
      textView.setTextColor(0xffffffff);
      textView.setText(msg);
      Toast toast = new Toast(context.getApplicationContext());
      toast.setDuration(Toast.LENGTH_SHORT);
      toast.setGravity(Gravity.CENTER, 0, 0);
      toast.setView(textView);
      toast.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
