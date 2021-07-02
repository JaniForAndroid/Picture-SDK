package com.namibox.commonlib.view.headers;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.picsdk.R;
import com.namibox.util.Logger;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;

/**
 * @Description 纳米box header
 * @CreateTime: 2019/11/9 16:49
 * @Author: zhangkx
 */
public class NamiboxHeader extends RelativeLayout implements RefreshHeader {
  private Context context;
  public static String REFRESH_HEADER_PULLDOWN = "下拉刷新";
  public static String REFRESH_HEADER_REFRESHING = "加载中";
  public static String REFRESH_HEADER_RELEASE = "松手刷新";
  private ImageView loadingImg;
  private TextView loadingText;
  private Animation loadingAnimation;

  public NamiboxHeader(Context context) {
    super(context, null);
    initView(context);
  }

  public NamiboxHeader(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs, 0);
    initView(context);
  }

  public NamiboxHeader(Context context,@Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView(context);
  }

  private void initView(Context context) {
    this.context = context;
    this.setGravity(Gravity.CENTER_HORIZONTAL| Gravity.BOTTOM);
    View view = LayoutInflater.from(context).inflate(R.layout.layout_loading, null);
    loadingImg = view.findViewById(R.id.loading_img);
    loadingText = view.findViewById(R.id.loading_text);
    this.addView(view);
  }

  @NonNull
  @Override
  public View getView() {
    return this;
  }

  @NonNull
  @Override
  public SpinnerStyle getSpinnerStyle() {
    return SpinnerStyle.Translate;
  }

  @Override
  public void setPrimaryColors(int... colors) {

  }

  @Override
  public void onInitialized(@NonNull RefreshKernel kernel, int height, int maxDragHeight) {

  }

  @Override
  public void onMoving(boolean isDragging, float percent, int offset, int height,
      int maxDragHeight) {

  }

  @Override
  public void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {
  }

  @Override
  public void onStartAnimator(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {

  }

  @Override
  public int onFinish(@NonNull RefreshLayout refreshLayout, boolean success) {
    Logger.e("zkx onFinish");
    if (loadingAnimation != null) {
      loadingAnimation.cancel();
      loadingImg.clearAnimation();
    }
    return 0;
  }

  @Override
  public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

  }

  @Override
  public boolean isSupportHorizontalDrag() {
    return false;
  }

  @Override
  public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState,
      @NonNull RefreshState newState) {
    switch (newState) {
      case None:
        break;
      case PullDownToRefresh:
        loadingText.setText(REFRESH_HEADER_PULLDOWN);
        break;
      case PullUpToLoad:
        break;
      case ReleaseToRefresh:
        loadingText.setText(REFRESH_HEADER_RELEASE);
        break;
      case Refreshing:
        loadingText.setText(REFRESH_HEADER_REFRESHING);
        startLoadingAnimation();
        break;
      case Loading:
        break;
    }

  }

  private void startLoadingAnimation() {
     loadingAnimation  = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    LinearInterpolator lin = new LinearInterpolator();
    loadingAnimation.setInterpolator(lin);
    loadingAnimation.setDuration(1000);//设置动画持续周期
    loadingAnimation.setRepeatCount(-1);//设置重复次数
    loadingAnimation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
    loadingAnimation.setStartOffset(10);//执行前的等待时间
    loadingAnimation.setInterpolator(new LinearInterpolator());
    loadingImg.setAnimation(loadingAnimation);
    loadingImg.startAnimation(loadingAnimation);

  }

  /**
   * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
   */
  public static int dip2px(Context context,float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }
}
