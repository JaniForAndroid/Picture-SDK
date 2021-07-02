package com.namibox.hfx.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.model.Work;
import com.namibox.hfx.utils.HfxPreferenceUtil;
import com.namibox.tools.WebViewUtil;
import com.namibox.util.Utils;
import com.namibox.util.WeakAsyncTask;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

/**
 * Created by sunha on 2016/2/25 0025.
 */
public class OtherWorkerFragment extends BaseWorkFragment {

  private static final String TAG = "OtherWorkerFragment";
  private LoadTask loadTask;


  @Override
  public void refreshData(boolean cleanOldData) {
    Log.i(TAG, "refreshData: " + cleanOldData);
    super.refreshData(cleanOldData);
    if (!isParepared) {
      return;
    }
    if (cleanOldData) {
      data = null;
      adapter.notifyDataSetChanged();

    }
    if (data != null && !data.isEmpty()) {
      //数据不为空就静默刷新
//            progressBar.setVisibility(View.GONE);
      Log.i(TAG, "swipyrefreshlayout: " + false);
      swipyrefreshlayout.setRefreshing(false);
      recyclerViewHelper.hideProgress();
    } else {
      Log.i(TAG, "swipyrefreshlayout: " + true);
//            progressBar.setVisibility(View.VISIBLE);
      swipyrefreshlayout.setRefreshing(true);
      recyclerViewHelper.showProgress();
    }
    cancelLoadTask();
    loadTask = new LoadTask(OtherWorkerFragment.this);
    loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentTab);
  }


  @Override
  protected void cancelLoadTask() {
    if (loadTask != null) {
      loadTask.cancel(true);
    }
  }

  void onLoadDone(List<Item> mData, boolean result) {
    if (isParepared) {
      swipyrefreshlayout.setRefreshing(false);
      recyclerViewHelper.hideProgress();
      if (result) {
        recyclerViewHelper.whenEmptyShowNoData();
        data = mData;
        if (mData == null) {
          adapter.notifyDataSetChanged();
          return;
        }
        adapter.notifyDataSetChanged();
      } else {
        recyclerViewHelper.whenEmptyShowOfflineError();
        adapter.notifyDataSetChanged();
      }
    }
  }


  @Override
  protected void itemClick(Item item) {
    switch (currentTab) {
      case TAB_BLOCK:
        if (item.sectionEntity != null && !TextUtils.isEmpty(item.sectionEntity.link_url)) {
          openView(item.sectionEntity.link_url);
        }
        break;
      case TAB_CHECKING:
//                toast("作品审核中请稍后再试");
//                return;
      case TAB_PASS:

        if (item.sectionEntity != null && !TextUtils.isEmpty(item.sectionEntity.link_url)) {
          if (item.sectionEntity.openview) {
            if (TextUtils.isEmpty(item.sectionEntity.template)) {
              openView(item.sectionEntity.link_url);
            } else {
              if (item.sectionEntity.video_set != null) {
                WebViewUtil.openView(item.sectionEntity.link_url, item.sectionEntity.template,
                    item.sectionEntity.video_set.template_ratio,
                    item.sectionEntity.title, "", null,
                    item.sectionEntity.video_set.keeplight,
                    item.sectionEntity.video_set.lighteness);
              } else {
                WebViewUtil.openView(item.sectionEntity.link_url, item.sectionEntity.template,
                    item.sectionEntity.template_ratio,
                    item.sectionEntity.title, "", null,
                    item.sectionEntity.keeplight, item.sectionEntity.lighteness);
              }

            }

          } else if (item.sectionEntity.content_type.equals("freeaudio")) {
            activity.openPlayOnlineAudio(item.sectionEntity.link_url);
          } else if (item.sectionEntity.content_type.equals("freevideo")) {
            activity.onVideoPlay(item.sectionEntity.mp4_url, item.sectionEntity.title);
          } else {
            Intent intent = new Intent(activity, ReadBookActivity.class);
            intent.putExtra(ReadBookActivity.ARG_JSON_URL, item.sectionEntity.link_url);
            startActivity(intent);
          }

        } else {
          toast("作品待审核,请下拉刷新");
        }

        break;
        default:
          break;
    }
  }

  @Override
  protected void itemDelete(Item item) {
    switch (currentTab) {
      case TAB_BLOCK:
        if (item.sectionEntity != null) {
          deleteWorkDialog(item.sectionEntity.bookid, item.sectionEntity.id);
        }
        break;
      default:
        break;
    }
  }


  private static class LoadTask extends WeakAsyncTask<Integer, Void, Boolean, OtherWorkerFragment> {

    List<Item> data;
    boolean result = true;

    public LoadTask(OtherWorkerFragment myWorkFragment) {
      super(myWorkFragment);
    }

    @Override
    protected Boolean doInBackground(OtherWorkerFragment myWorkFragment, Integer... params) {

      Context context = myWorkFragment.activity.getApplicationContext();
      data = new ArrayList<>();
      int position = params[0];
      switch (position) {
        case TAB_MAKING:
          break;
        case TAB_CHECKING:
          loadMyWork(context, "doing");
          break;
        case TAB_BLOCK:
          loadMyWork(context, "block");
          break;
        case TAB_PASS:
          loadMyWork(context, "pass");
          break;
      }
      return result;
    }


    private void loadMyWork(Context context, String step) {
      Work work = null;
      try {
        Response<Work> response = ApiHandler.getBaseApi().getMyWork(step).execute();
        if (response.isSuccessful()) {
          work = response.body();
        }
      } catch (Exception e) {
        e.printStackTrace();
        result = false;
      }
      if (work != null && work.works != null) {
        for (Work.WorksEntity w : work.works) {
          if (w.section != null && w.section.size() > 0) {
            boolean hasCommitContent = false;
            Item section = new Item(w.sectionname);
            data.add(section);
            for (Work.SectionEntity s : w.section) {
              String bookid = s.bookid;
              String userId = Utils.getLoginUserId(context);
              //根据本地保存的值决定审核未过的作品显示不显示
              boolean isCommit = HfxPreferenceUtil
                  .isRecordBookInWorkDefTrue(context, userId, bookid);
              if (isCommit) {
                Item item = new Item(s);
                data.add(item);
                hasCommitContent = true;
              }
            }
            if (!hasCommitContent) {
              data.remove(section);
            }
          }
        }
      }
    }

    @Override
    protected void onPostExecute(OtherWorkerFragment myWorkFragment, Boolean result) {

      if (myWorkFragment != null && !myWorkFragment.activity.isFinishing()) {
        myWorkFragment.onLoadDone(data, result);
      }


    }
  }


}
