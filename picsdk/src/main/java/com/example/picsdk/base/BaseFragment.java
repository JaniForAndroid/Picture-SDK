package com.example.picsdk.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.namibox.util.Utils;

import io.reactivex.disposables.CompositeDisposable;

/**
 * author : feng
 * creation time : 19-9-18下午2:09
 */
public class BaseFragment extends Fragment {

  public BaseActivity activity;
  private boolean viewCreated;
  private boolean isVisible;
  private boolean dataInit;

  public CompositeDisposable compositeDisposable = new CompositeDisposable();

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    activity = (BaseActivity) getActivity();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewCreated = true;
    lazyLoad();
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    isVisible = isVisibleToUser;
    if (isVisibleToUser) {
      lazyLoad();
    }
  }

  private void lazyLoad() {
    if (!dataInit && viewCreated && isVisible) {
      dataInit = true;
      initData();
    }
  }

  public void initData() {

  }

  public void toast(String msg) {
    if (activity != null) {
      Utils.toast(activity, msg);
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    activity = null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    compositeDisposable.dispose();
  }
}
