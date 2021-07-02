package com.example.exoaudioplayer.video.component;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.exoaudioplayer.video.base.Constants;
import com.example.exoaudioplayer.video.controller.ControlWrapperI;
import com.example.exoaudioplayer.video.controller.IControlComponent;
import com.example.picsdk.R;
import com.namibox.util.PreferenceUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import java.util.concurrent.TimeUnit;

/**
 * 微课进度星级显示
 */
public class PlayerWKStarView extends FrameLayout implements IControlComponent {

  private ControlWrapperI mControlWrapper;

  private ImageView heart_img_one;
  private ImageView heart_img_two;
  private ImageView heart_img_three;
  private TextView tv_guide;
  private RelativeLayout heartLayout;
  private int guideNum;

  public PlayerWKStarView(@NonNull Context context) {
    super(context);
  }

  public PlayerWKStarView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public PlayerWKStarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  {
    setVisibility(GONE);
    LayoutInflater.from(getContext()).inflate(R.layout.player_layout_wkstar_view, this, true);
    heart_img_one = findViewById(R.id.heart_img_one);
    heart_img_two = findViewById(R.id.heart_img_two);
    heart_img_three = findViewById(R.id.heart_img_three);
    tv_guide = findViewById(R.id.tv_guide);
    heartLayout = findViewById(R.id.heart_layout);
  }

  @Override
  public void attach(@NonNull ControlWrapperI controlWrapper) {
    mControlWrapper = controlWrapper;
  }

  @Override
  public View getView() {
    return this;
  }

  @Override
  public void onVisibilityChanged(boolean isVisible, Animation anim) {

  }

  @Override
  public void onPlayStateChanged(int playState) {
  }

  @Override
  public void onPlayerStateChanged(int playerState) {
  }

  @Override
  public void setProgress(int duration, int position) {

  }

  @Override
  public void onLockStateChanged(boolean isLock) {
//    if (isLock) {
//      setVisibility(GONE);
//    } else {
//      setVisibility(VISIBLE);
//    }
  }

  public void init(int heartNum, Context context) {
    if (-1 == heartNum) {
      setVisibility(GONE);
    } else {
      setVisibility(VISIBLE);
      //处理心的显示情况
      changeHeartImg(heartNum);
      showGuide(context);
    }
  }

  /**
   * 处理 ❤ 的显示问题
   *
   * @param part 心个数
   */
  public void changeHeartImg(int part) {
    switch (part) {
      case 0:
        heart_img_one.setBackgroundResource(R.drawable.player_ic_heart_normal);
        heart_img_two.setBackgroundResource(R.drawable.player_ic_heart_normal);
        heart_img_three.setBackgroundResource(R.drawable.player_ic_heart_normal);
        break;
      case 1:
        heart_img_one.setBackgroundResource(R.drawable.player_ic_heart_selected);
        heart_img_two.setBackgroundResource(R.drawable.player_ic_heart_normal);
        heart_img_three.setBackgroundResource(R.drawable.player_ic_heart_normal);
        break;
      case 2:
        heart_img_one.setBackgroundResource(R.drawable.player_ic_heart_selected);
        heart_img_two.setBackgroundResource(R.drawable.player_ic_heart_selected);
        heart_img_three.setBackgroundResource(R.drawable.player_ic_heart_normal);
        break;
      case 3:
      case 6:
        heart_img_one.setBackgroundResource(R.drawable.player_ic_heart_selected);
        heart_img_two.setBackgroundResource(R.drawable.player_ic_heart_selected);
        heart_img_three.setBackgroundResource(R.drawable.player_ic_heart_selected);
        break;
      default:
        break;
    }
  }

  private void showGuide(final Context context) {
    guideNum = PreferenceUtil.get(context).getSP(Constants.GUIDE).getInt(Constants.HEART_GUIDE, 0);
    if (guideNum > 2) {
      return;
    }
    tv_guide.postDelayed(new Runnable() {
      @Override
      public void run() {
        tv_guide.setVisibility(View.VISIBLE);
        startAnim(tv_guide);
        Observable.timer(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<Long>() {
              @Override
              public void accept(Long aLong) throws Exception {
                tv_guide.setVisibility(View.GONE);
                guideNum++;
                PreferenceUtil.get(context).getSP(Constants.GUIDE).edit().putInt(Constants.HEART_GUIDE, guideNum)
                    .apply();
              }
            });
      }
    }, 3000);
  }

  private void startAnim(final View view) {
    ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 1.05f, 1f);
    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        float scale = (float) animation.getAnimatedValue();
        view.setScaleX(scale);
        view.setScaleY(scale);
      }
    });
    valueAnimator.setRepeatCount(2);
    valueAnimator.setDuration(1500);
    valueAnimator.start();
  }
}
