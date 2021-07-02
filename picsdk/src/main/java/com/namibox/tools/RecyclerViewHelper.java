package com.namibox.tools;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.picsdk.R;

/**
 * Created by sunha on 2017/10/14 0014.
 */

public class RecyclerViewHelper {

  private Context context;
  private AdapterDataObserver mObserver;
  private RecyclerView recyclerView;
  private String netWorkErrorTip = "当前无网络，请检查网络连接！";
  private String noDataTip = "没有数据";
  private EmptyType status = EmptyType.DEFAULT;
  //  private boolean refreshEnable = true;
  private boolean showProgress = false;
  private ImageView networkErrImg;
  private TextView networkErrText;
  private Button freshBtn;
  private ViewGroup parentView;
  private RefreshListener mRefreshListener;
  private MultiStatusViewHelper multiStatusViewHelper;
  private int netWorkErrImgRes = R.drawable.common_network_error_gif;
  private int noDataErrImgRes = R.drawable.common_nodata_gif;
  private boolean hasHeadView = false;


  public enum EmptyType {DEFAULT, NETWORK_ERROR, NO_DATA}

  public RecyclerViewHelper(Context context, RecyclerView recyclerView, ViewGroup parentView) {
    this.parentView = parentView;
    this.recyclerView = recyclerView;
    this.context = context;
    multiStatusViewHelper = new MultiStatusViewHelper(recyclerView, parentView);
    init();
  }

  public RecyclerViewHelper(Context context, RecyclerView recyclerView, ViewGroup parentView,
      boolean hasHeadView) {
    this.parentView = parentView;
    this.recyclerView = recyclerView;
    this.context = context;
    this.hasHeadView = hasHeadView;
    multiStatusViewHelper = new MultiStatusViewHelper(recyclerView, parentView);
    init();
  }

  private void init() {
    View nerwokErrorView = LayoutInflater.from(context)
        .inflate(R.layout.common_error_view, parentView, false);
    View progressView = LayoutInflater.from(context)
        .inflate(R.layout.common_loading_view, parentView, false);
    multiStatusViewHelper.setErrorView(nerwokErrorView);
    multiStatusViewHelper.setLoadingView(progressView);
    networkErrImg = nerwokErrorView.findViewById(R.id.networkErrImg);
    networkErrText = nerwokErrorView.findViewById(R.id.networkErrText);
    freshBtn = nerwokErrorView.findViewById(R.id.freshBtn);
    freshBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mRefreshListener != null) {
          mRefreshListener.onRefresh();
        }
      }
    });
    mObserver = new AdapterDataObserver() {
      @Override
      public void onChanged() {
        Adapter adapter = recyclerView.getAdapter();
        int start = hasHeadView ? 1 : 0;
        if (adapter.getItemCount() == start) {
          if (showProgress) {
            multiStatusViewHelper.showLoading();
          } else {
            multiStatusViewHelper.showError();
            if (status == EmptyType.DEFAULT) {
              multiStatusViewHelper.showContent();
            } else {
              if (mRefreshListener != null) {
                freshBtn.setVisibility(VISIBLE);
              } else {
                freshBtn.setVisibility(GONE);
              }
              if (status == EmptyType.NETWORK_ERROR) {
                networkErrText.setText(netWorkErrorTip);
                Glide.with(context).load(netWorkErrImgRes).into(networkErrImg);
              } else if (status == EmptyType.NO_DATA) {
                networkErrText.setText(noDataTip);
                Glide.with(context).load(noDataErrImgRes).into(networkErrImg);
              }
            }
          }
        } else {
          multiStatusViewHelper.showContent();
        }
      }

      @Override
      public void onItemRangeChanged(int positionStart, int itemCount) {
        onChanged();
      }

      @Override
      public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        onChanged();
      }

      @Override
      public void onItemRangeRemoved(int positionStart, int itemCount) {
        onChanged();
      }

      @Override
      public void onItemRangeInserted(int positionStart, int itemCount) {
        onChanged();
      }

      @Override
      public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        onChanged();
      }
    };
  }


  public void setAdapter(RecyclerView.Adapter adapter) {
    if (recyclerView.getAdapter() != null) {
      recyclerView.getAdapter().unregisterAdapterDataObserver(mObserver);
    }
    recyclerView.setAdapter(adapter);
    adapter.registerAdapterDataObserver(mObserver);
    mObserver.onChanged();
  }

  /**
   * 没网络时的提示
   */
  public void setNetWorkErrorTip(String netWorkErrorTip) {
    this.netWorkErrorTip = netWorkErrorTip;
  }

  /**
   * 没数据时的提示
   */
  public void setNoDataTip(String noDataTip) {
    this.noDataTip = noDataTip;
  }

  /**
   * 网络错误的提示图片
   */
  public void setNetWorkErrImgRes(int netWorkErrImgRes) {
    this.netWorkErrImgRes = netWorkErrImgRes;
  }

  /**
   * 没有数据的提示图片
   */
  public void setNoDataErrImgRes(int noDataErrImgRes) {
    this.noDataErrImgRes = noDataErrImgRes;
  }

  public void whenEmptyShowOfflineError() {
    this.status = EmptyType.NETWORK_ERROR;
    mObserver.onChanged();
  }

  public void whenEmptyShowNoData() {
    this.status = EmptyType.NO_DATA;
    mObserver.onChanged();
  }

  public void whenEmptyShowContent() {
    this.status = EmptyType.DEFAULT;
    mObserver.onChanged();
  }

  public void setRefreshListener(RefreshListener refreshListener) {
    this.mRefreshListener = refreshListener;

  }

  public void showProgress() {
    this.showProgress = true;
    mObserver.onChanged();
  }

  public void hideProgress() {
    this.showProgress = false;
    mObserver.onChanged();
  }


  public interface RefreshListener {

    void onRefresh();
  }

}
