package com.namibox.hfx.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.picsdk.R;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.model.BaseNetResult;
import com.namibox.commonlib.model.Work.SectionEntity;
import com.namibox.hfx.utils.HfxFileUtil;
import com.namibox.hfx.utils.HfxPreferenceUtil;
import com.namibox.tools.RecyclerViewHelper;
import com.namibox.tools.RecyclerViewHelper.RefreshListener;
import com.namibox.util.FileUtil;
import com.namibox.util.Utils;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout.OnRefreshListener;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import java.io.File;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sunha on 2016/2/27 0027.
 */
public abstract class BaseWorkFragment extends Fragment {

  private static final String TAG = "BaseWorkFragment";
  public static final String CURRENTTAB = "currentTab";
  public static final int TAB_MAKING = 0;
  public static final int TAB_CHECKING = 1;
  public static final int TAB_BLOCK = 2;
  public static final int TAB_PASS = 3;
  protected int currentTab;
  RecyclerView recyclerview;
  SwipyRefreshLayout swipyrefreshlayout;
  FrameLayout frameLayout;

  protected Adapter adapter;
  protected View view;
  protected MyWorkActivity activity;
  protected List<Item> data;
  protected boolean isVisible = false;
  protected boolean isParepared = false;
  protected RecyclerViewHelper recyclerViewHelper;

  public static BaseWorkFragment newInstance(int tab) {
    Bundle args = new Bundle();
    args.putInt(CURRENTTAB, tab);
    BaseWorkFragment fragment;
    if (tab == 0) {
      fragment = new MakingWorkFragment();
    } else {
      fragment = new OtherWorkerFragment();
    }

    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    if (args != null) {
      currentTab = args.getInt(CURRENTTAB);
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.hfx_fragment_my_work, null);
    initView(view);
    recyclerViewHelper = new RecyclerViewHelper(activity, recyclerview, frameLayout);
    recyclerview.setLayoutManager(new LinearLayoutManager(activity));
    adapter = new Adapter();
    recyclerViewHelper.setNoDataTip("没有作品记录");
    recyclerViewHelper.setRefreshListener(() -> refreshData(false));
    recyclerViewHelper.setAdapter(adapter);
    swipyrefreshlayout.setOnRefreshListener(direction -> refreshData(false));
    isParepared = true;
    refreshData(false);
    Log.i(TAG + currentTab, "onCreateView: ");
    return view;
  }

  private void initView(View view) {
    recyclerview = view.findViewById(R.id.recyclerview);
    swipyrefreshlayout = view.findViewById(R.id.swipyrefreshlayout);
    frameLayout = view.findViewById(R.id.frameLayout);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    isParepared = false;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    cancelLoadTask();
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    Log.i(TAG + currentTab, "setUserVisibleHint: " + isVisibleToUser);
    if (getUserVisibleHint()) {
      isVisible = true;
      refreshData(false);
    } else {
      isVisible = false;
      cancelLoadTask();

    }
  }

  protected abstract void cancelLoadTask();

  protected void refreshData(boolean cleanOldData) {
    Log.i(TAG + currentTab, "refreshData: visible" + isVisible + "  prepare:" + isParepared);

  }


  static class Item {

    boolean isSection;
    boolean isAudio = false;
    File audoFile;
    boolean isVideo = false;
    File videoFile;
    SectionEntity sectionEntity;
    String icon;
    String title;
    String info;
    String bookId;
    File iconFile;
    String tempVideoName;
    String matchName;

    public Item(String title) {
      this.isSection = true;
      this.title = title;
    }

    public Item(String bookId, String title, String info) {
      this.isSection = false;
      this.bookId = bookId;
      this.title = title;
      this.info = info;
    }


    public Item(String bookId, String icon, String title, String info) {
      this.isSection = false;
      this.bookId = bookId;
      this.icon = icon;
      this.title = title;
      this.info = info;
    }

    public Item(SectionEntity sectionEntity) {
      this.isSection = false;
      this.sectionEntity = sectionEntity;
    }
  }

  protected void openRecord(String bookId) {
    Intent intent = new Intent(activity, RecordActivity.class);
    intent.putExtra(RecordActivity.ARG_BOOK_ID, bookId);
    intent.putExtra(RecordActivity.ARG_IS_MAKING, true);
    intent.putExtra(RecordActivity.ARG_NEED_CHECK, true);
    startActivity(intent);
  }

  public void toast(String msg) {
    activity.toast(msg);
  }

  public void openView(String url) {
    activity.openView(url);
  }

  protected void deleteWorkDialog(final String bookId) {
    activity.showDialog("提示", "确认要删除该作品吗？", "确定", new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String user_id = Utils.getLoginUserId(activity);
        //删除作品时置成true，乍看感觉下次制作作品的时候这个值为true，会导致没法制作
        //但这个值只会在check时去使用，check如果没有提交过该作品，那么可以直接往下制作，不会取这个值进行判断
        HfxPreferenceUtil.setRecordBookInWork(activity, user_id, bookId, true);
        deleteLocalWork(bookId);
      }
    }, "取消", null);

  }

  protected void deleteWorkDialog(final String bookId, final String id) {
    activity.showDialog("提示", "确认要删除该作品吗？", "确定", new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        deleteWork(bookId, id);
      }
    }, "取消", null);
  }

  protected void deleteLocalWork(String bookId) {
    if (TextUtils.isEmpty(bookId)) {
      return;
    }
    File dir = HfxFileUtil.getUserWorkDir(activity, bookId);
    if (FileUtil.deleteDir(dir)) {
      refreshData(true);
      toast("删除成功");
    }
  }

  protected void deleteWork(final String bookId, String id) {
    activity.showProgress("正在删除...");
    ApiHandler.getBaseApi()
        .deleteWork(id)
        .enqueue(new Callback<BaseNetResult>() {
          @Override
          public void onResponse(Call<BaseNetResult> call, Response<BaseNetResult> response) {
            if (response.isSuccessful()) {
              if (activity != null && !activity.isFinishing()) {
                activity.hideProgress();
                BaseNetResult deleteResult = response.body();
                if (deleteResult != null && deleteResult.errcode == 0) {
                  deleteLocalWork(bookId);

                } else {
                  activity.showErrorDialog("删除失败", false);
                }
              }
            } else {
              if (activity != null && !activity.isFinishing()) {
                activity.hideProgress();
                activity.showErrorDialog("删除失败", false);
              }
            }
          }

          @Override
          public void onFailure(Call<BaseNetResult> call, Throwable t) {
            if (activity != null && !activity.isFinishing()) {
              activity.hideProgress();
              activity.showErrorDialog("删除失败", false);
            }
          }
        });
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = (MyWorkActivity) activity;

  }

  @Override
  public void onDetach() {
    super.onDetach();
    this.activity = null;
  }

  protected class Adapter extends RecyclerView.Adapter<ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      if (viewType == 0) {
        View v = LayoutInflater.from(activity)
            .inflate(R.layout.hfx_layout_work_section, parent, false);
        return new SectionViewHolder(v);
      } else {
        View v = LayoutInflater.from(activity)
            .inflate(R.layout.hfx_layout_work_item, parent, false);
        return new ItemViewHolder(v, BaseWorkFragment.this);
      }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      Item item = data.get(position);
      if (item.isSection) {
        SectionViewHolder vh = (SectionViewHolder) holder;
        vh.sectionView.setText(item.title);
      } else {
        ItemViewHolder vh = (ItemViewHolder) holder;
        vh.item = item;
        switch (currentTab) {
          case TAB_MAKING:
            vh.stateView.setVisibility(View.GONE);
            vh.deleteView.setVisibility(View.VISIBLE);
            vh.infoView.setVisibility(View.GONE);
            vh.introduceView.setVisibility(View.VISIBLE);
            vh.ratingBar.setVisibility(View.GONE);
            vh.titleView.setText(item.title);
            vh.introduceView.setText(item.info);
            if (item.isAudio || item.isVideo) {
              RequestOptions options = new RequestOptions()
                  .skipMemoryCache(true)
                  .diskCacheStrategy(DiskCacheStrategy.NONE)
                  .placeholder(R.drawable.hfx_default_icon)
                  .error(R.drawable.hfx_default_icon);
              Glide.with(activity)
                  .asBitmap()
                  .load(item.iconFile)
                  .apply(options)
                  .into(vh.iconView);
            } else {
              RequestOptions options = new RequestOptions()
                  .skipMemoryCache(true)
                  .placeholder(R.drawable.hfx_default_icon)
                  .error(R.drawable.hfx_default_icon);
              Glide.with(activity)
                  .asBitmap()
                  .load(Utils.encodeString(item.icon))
                  .apply(options)
                  .into(vh.iconView);
            }

            if (!TextUtils.isEmpty(item.matchName)) {
              vh.matchName.setVisibility(View.VISIBLE);
              vh.matchName.setText(item.matchName);
            } else {
              vh.matchName.setVisibility(View.GONE);
            }

            break;
          case TAB_CHECKING:
            vh.stateView.setVisibility(View.VISIBLE);
            vh.stateView.setText(item.sectionEntity.status);
            vh.deleteView.setVisibility(View.GONE);
            vh.infoView.setVisibility(View.GONE);
            vh.introduceView.setVisibility(View.VISIBLE);
            vh.ratingBar.setVisibility(View.GONE);
            vh.titleView.setText(item.sectionEntity.title);
            vh.introduceView.setText(item.sectionEntity.introduce);
            RequestOptions options = new RequestOptions()
                .skipMemoryCache(true)
                .placeholder(R.drawable.hfx_default_icon)
                .error(R.drawable.hfx_default_icon);
            Glide.with(activity)
                .asBitmap()
                .load(Utils.encodeString(item.sectionEntity.img_src))
                .apply(options)
                .into(vh.iconView);
            if (!TextUtils.isEmpty(item.sectionEntity.matchname)) {
              vh.matchName.setVisibility(View.VISIBLE);
              vh.matchName.setText(item.sectionEntity.matchname);
            } else {
              vh.matchName.setVisibility(View.GONE);
            }
            break;
          case TAB_BLOCK:
            vh.stateView.setVisibility(View.GONE);
            vh.deleteView.setVisibility(View.VISIBLE);
            vh.infoView.setVisibility(View.GONE);
            vh.introduceView.setVisibility(View.VISIBLE);
            vh.ratingBar.setVisibility(View.GONE);
            vh.titleView.setText(item.sectionEntity.title);
            vh.introduceView.setText(item.sectionEntity.introduce);
            RequestOptions options2 = new RequestOptions()
                .skipMemoryCache(true)
                .placeholder(R.drawable.hfx_default_icon)
                .error(R.drawable.hfx_default_icon);
            Glide.with(activity)
                .asBitmap()
                .load(Utils.encodeString(item.sectionEntity.img_src))
                .apply(options2)
                .into(vh.iconView);
            if (!TextUtils.isEmpty(item.sectionEntity.matchname)) {
              vh.matchName.setVisibility(View.VISIBLE);
              vh.matchName.setText(item.sectionEntity.matchname);
            } else {
              vh.matchName.setVisibility(View.GONE);
            }
            break;
          case TAB_PASS:
            vh.stateView.setVisibility(View.VISIBLE);
            vh.stateView.setText(item.sectionEntity.status);
            vh.deleteView.setVisibility(View.GONE);
            vh.introduceView.setVisibility(View.VISIBLE);
            vh.introduceView.setText(item.sectionEntity.introduce);
            vh.infoView.setVisibility(View.VISIBLE);
            if (item.sectionEntity.star > 0) {
              vh.ratingBar.setVisibility(View.VISIBLE);
              vh.ratingBar.setRating(item.sectionEntity.star);
            } else {
              vh.ratingBar.setVisibility(View.GONE);
            }
            String commentcount = Utils
                .formatCount(activity, item.sectionEntity.commentcount);
            String readcount = Utils
                .formatCount(activity, item.sectionEntity.readcount);
            String commentString = "";
            if (item.sectionEntity.commentcount > 0) {
              commentString += commentcount + "评";
            }
            if (item.sectionEntity.readcount > 0) {
              commentString += " " + readcount + "阅";
            }
            vh.infoView.setText(commentString);
            vh.titleView.setText(item.sectionEntity.title);
            RequestOptions options3 = new RequestOptions()
                .skipMemoryCache(true)
                .placeholder(R.drawable.hfx_default_icon)
                .error(R.drawable.hfx_default_icon);
            Glide.with(activity)
                .asBitmap()
                .load(Utils.encodeString(item.sectionEntity.img_src))
                .apply(options3)
                .into(vh.iconView);
            if (!TextUtils.isEmpty(item.sectionEntity.matchname)) {
              vh.matchName.setVisibility(View.VISIBLE);
              vh.matchName.setText(item.sectionEntity.matchname);
            } else {
              vh.matchName.setVisibility(View.GONE);
            }
            break;
        }

      }
    }

    @Override
    public int getItemViewType(int position) {
      Item item = data.get(position);
      return item.isSection ? 0 : 1;
    }

    @Override
    public int getItemCount() {
      return data == null ? 0 : data.size();
    }
  }

  static class SectionViewHolder extends ViewHolder {

    TextView sectionView;

    public SectionViewHolder(View itemView) {
      super(itemView);
      sectionView = itemView.findViewById(R.id.section);
    }
  }

  static class ItemViewHolder extends ViewHolder {

    ImageView iconView;
    TextView titleView;
    TextView stateView;
    TextView introduceView;
    TextView deleteView;
    TextView infoView;
    RatingBar ratingBar;
    TextView matchName;
    Item item;

    public ItemViewHolder(View itemView, final BaseWorkFragment myWorkFragment) {
      super(itemView);
      initView(itemView);
      itemView.setOnClickListener(v -> myWorkFragment.itemClick(item));
      deleteView.setOnClickListener(v -> myWorkFragment.itemDelete(item));
    }

    private void initView(View view) {
      iconView = view.findViewById(R.id.icon);
      titleView = view.findViewById(R.id.title);
      stateView = view.findViewById(R.id.state);
      introduceView = view.findViewById(R.id.introduce);
      deleteView = view.findViewById(R.id.delete);
      infoView = view.findViewById(R.id.info);
      ratingBar = view.findViewById(R.id.ratingbar);
      matchName = view.findViewById(R.id.matchName);
    }
  }

  protected abstract void itemDelete(Item item);

  protected abstract void itemClick(Item item);
}
