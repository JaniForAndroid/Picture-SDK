package com.namibox.tools;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sunha on 2017/10/17 0017.
 */

public class MultiStatusViewHelper {

  private View errorView;
  private View loadingView;
  private View emptyView;
  private View customView;
  private View contentView;
  private ViewGroup parentView;
  private boolean addedError;
  private boolean addedLoading;
  private boolean addedEmpty;
  private boolean addedCustom;

  public MultiStatusViewHelper(@NonNull View contentView, @NonNull ViewGroup parentView) {
    this.contentView = contentView;
    this.parentView = parentView;
  }

  public void setErrorView(View errorView) {
    this.errorView = errorView;
  }

  public void   setLoadingView(View loadingView) {
    this.loadingView = loadingView;
  }

  public void setEmptyView(View emptyView) {
    this.emptyView = emptyView;
  }

  public void setCustomView(View customView) {
    this.customView = customView;
  }

  public void showError() {
    if (errorView == null) {
      return;
    }
    if (!addedError) {
      parentView.addView(errorView);
      addedError = true;
    }
    errorView.setVisibility(View.VISIBLE);
    hideEmpty();
    hideCustom();
    hideLoading();
    hideContent();

  }

  private void hideError() {
    if (addedError) {
      errorView.setVisibility(View.GONE);
    }
  }

  public void showLoading() {
    if (loadingView == null) {
      return;
    }
    if (!addedLoading) {
      parentView.addView(loadingView);
      addedLoading = true;
    }
    loadingView.setVisibility(View.VISIBLE);
    hideError();
    hideCustom();
    hideEmpty();
    hideContent();


  }

  private void hideLoading() {
    if (addedLoading) {
      loadingView.setVisibility(View.GONE);
    }
  }

  public void showEmpty() {
    if (emptyView == null) {
      return;
    }
    if (!addedEmpty) {
      parentView.addView(emptyView);
      addedEmpty = true;
    }
    emptyView.setVisibility(View.VISIBLE);
    hideCustom();
    hideLoading();
    hideError();
    hideContent();

  }

  private void hideEmpty() {
    if (addedEmpty) {
      emptyView.setVisibility(View.GONE);
    }
  }

  public void showCustom() {
    if (customView == null) {
      return;
    }
    if (!addedCustom) {
      parentView.addView(customView);
      addedCustom = true;
    }
    customView.setVisibility(View.VISIBLE);
    hideLoading();
    hideError();
    hideEmpty();
    hideContent();

  }

  private void hideCustom() {
    if (addedCustom) {
      customView.setVisibility(View.GONE);
    }
  }

  public void showContent() {

    contentView.setVisibility(View.VISIBLE);
    hideLoading();
    hideError();
    hideEmpty();
    hideCustom();

  }

  private void hideContent() {
    contentView.setVisibility(View.GONE);
  }

}
