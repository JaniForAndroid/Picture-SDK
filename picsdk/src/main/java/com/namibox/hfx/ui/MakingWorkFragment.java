package com.namibox.hfx.ui;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.example.picsdk.R;
import com.namibox.hfx.bean.Huiben;
import com.namibox.hfx.bean.MatchInfo;
import com.namibox.hfx.utils.HfxFileUtil;
import com.namibox.hfx.utils.HfxPreferenceUtil;
import com.namibox.hfx.utils.HfxUtil;
import com.namibox.util.FileUtil;
import com.namibox.util.Utils;
import com.namibox.util.WeakAsyncTask;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DefaultSubscriber;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunha on 2016/2/25 0025.
 */
public class MakingWorkFragment extends BaseWorkFragment {

  private static final String TAG = "MakingWorkFragment";
  private LoadTask loadTask;


  @Override
  public void refreshData(boolean cleanOldData) {
    super.refreshData(cleanOldData);
    if (!isParepared) {
      return;
    }
    data = null;
    adapter.notifyDataSetChanged();
//        progressBar.setVisibility(View.VISIBLE);

    if (data != null && !data.isEmpty()) {
//            progressBar.setVisibility(View.GONE);
      swipyrefreshlayout.setRefreshing(false);
      recyclerViewHelper.hideProgress();
    } else {
//            progressBar.setVisibility(View.VISIBLE);
      swipyrefreshlayout.setRefreshing(true);
      recyclerViewHelper.showProgress();
    }
    cancelLoadTask();
    loadTask = new LoadTask(MakingWorkFragment.this);
    loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }


  @Override
  protected void cancelLoadTask() {
    if (loadTask != null) {
      loadTask.cancel(true);
    }
  }

  void onLoadDone(List<Item> data) {
    if (isParepared) {
      recyclerViewHelper.whenEmptyShowNoData();
      swipyrefreshlayout.setRefreshing(false);
      recyclerViewHelper.hideProgress();
//            progressBar.setVisibility(View.GONE);
      if (data == null) {
        adapter.notifyDataSetChanged();
        return;
      }
      this.data = data;
      adapter.notifyDataSetChanged();
    }

  }


  @Override
  protected void itemClick(final Item item) {
    switch (currentTab) {
      case TAB_MAKING:
        if (item.isAudio) {
          activity.openPlayLocalAudio(item.bookId);
        } else if (item.isVideo) {
          activity.showProgress("复制视频文件");
          //每次制作中点击视频都需要重新剪裁，剪裁的目标文件通过getVideoFile
          //不复制一份的话会造成剪裁的目标文件和源文件相同，会有问题
          Flowable.create(new FlowableOnSubscribe<File>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<File> emitter) throws Exception {
              try {
                File videoFile = HfxFileUtil.getVideoFile(activity, item.bookId);
                File tempVideo = HfxFileUtil.getCopyTempVideoFile(activity, item.bookId);
                if (videoFile != null && videoFile.exists() && videoFile.length() > 0) {
                  FileUtil.copyFile(videoFile, tempVideo.getParentFile(), tempVideo.getName());
                  emitter.onNext(tempVideo);
                } else {
                  emitter.onError(new Exception("file dont exists"));
                }
              } catch (Exception e) {
                emitter.onError(e);
              }
              emitter.onComplete();
            }
          }, BackpressureStrategy.BUFFER)
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new DefaultSubscriber<File>() {
                @Override
                public void onComplete() {
                  activity.hideProgress();
                }

                @Override
                public void onError(Throwable e) {
                  refreshData(true);
                  activity.toast("获取视频文件失败,请重新制作视频秀");
                  activity.hideProgress();

                }

                @Override
                public void onNext(File result) {
                  CutVideoActivity
                      .openVideoActivity(activity, Uri.fromFile(result), result.getAbsolutePath(),
                          item.bookId);
                }
              });
        } else {
          openRecord(item.bookId);
        }
        break;
    }
  }

  @Override
  protected void itemDelete(Item item) {
    switch (currentTab) {
      case TAB_MAKING:
        deleteWorkDialog(item.bookId);
        break;
    }
  }

  private static class LoadTask extends WeakAsyncTask<Integer, Void, Void, MakingWorkFragment> {

    List<Item> data;

    public LoadTask(MakingWorkFragment myWorkFragment) {
      super(myWorkFragment);
    }

    @Override
    protected Void doInBackground(MakingWorkFragment myWorkFragment, Integer... params) {

      Context context = myWorkFragment.activity.getApplicationContext();
      data = new ArrayList<>();
      loadMaking(context);
      return null;
    }

    private void loadMaking(Context context) {
      File workDir = HfxFileUtil.getUserWorkDir(context);
      File[] dirs = workDir.listFiles();
      String user_id = Utils.getLoginUserId(context);
      List<Item> type1 = new ArrayList<>();
      List<Item> type2 = new ArrayList<>();
      List<Item> type3 = new ArrayList<>();
      List<Item> type4 = new ArrayList<>();
      List<Item> type5 = new ArrayList<>();
      if (dirs != null) {
        for (File dir : dirs) {
          String bookId = dir.getName();
          File temp = HfxFileUtil.getUserTempDir(context, bookId);
          //这里isRecordBookInWork默认值是false，表示在未主动写入该值时，制作中应当显示该作品
          //有问题的情况：审核未过中的绘本，点击了修改作品，然后删除app，再次安装，制作中会出现该作品
          //但是点击进去之后会在check的时候用的默认值是true的方法获取，会提示跳到审核未过
          //此场景概率极低，且造成影响不大
          //如果使用isRecordBookInWorkDefTrue,默认值是true，则用户进行上述操作，应该显示在制作中的作品也显示不了
          boolean commited = HfxPreferenceUtil.isRecordBookInWork(context, user_id, bookId);
          File[] audios = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
              return pathname.isFile() && pathname.getName()
                  .endsWith(HfxFileUtil.AUDIO_TYPE) && !pathname.getName().startsWith("eval_");
            }
          });
          File[] temp_audios = temp.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
              return pathname.isFile() && pathname.getName()
                  .endsWith(HfxFileUtil.AUDIO_TYPE) && !pathname.getName().startsWith("eval_");
            }
          });

          if (!commited && ((audios != null && audios.length > 0)
              || (temp_audios != null && temp_audios.length > 0))) {
            File configFile = HfxFileUtil.getBookConfigFile(context, bookId);
            Huiben book = Utils.parseJsonFile(configFile, Huiben.class);
            if (book != null) {
              if (!TextUtils.isEmpty(book.content_type)) {
                Item item = new Item(bookId, book.icon, book.bookname, book.subtitle);
                switch (book.content_type) {
                  case "audiobook": {

                    type1.add(item);
                    break;
                  }
                  case "englishbook": {
                    type2.add(item);
                    break;
                  }
                  case "lianhuanhua": {
                    type3.add(item);
                    break;
                  }
                }

                setMatchNameToItem(context, bookId, item);
              }
            }
          }

          String[] titleStrings = bookId.split("_");
          //之前的路径是getHfxVideoFile(在文件名中保存了视频宽高时长)
          // 后面改成了getVideoFile
          //这里是兼容处理，很早之前的代码了，现在没必要做这个兼容
          File videoFile = HfxFileUtil.getVideoFile(context, bookId);
          File hfxFile = HfxFileUtil.getHfxVideoFile(context, bookId);
          if (hfxFile != null && hfxFile.exists() && !videoFile.exists()) {
            try {
              FileUtil.copyFile(hfxFile, videoFile.getParentFile(), videoFile.getName());
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
          if (videoFile.exists()) {

            File configFile = new File(dir, bookId + ".config");
            Huiben book = Utils.parseJsonFile(configFile, Huiben.class);
            if (book != null) {
              //显示副标题的逻辑，最初是没有的，都是写死的
              //后面加上并做了兼容
              String bookName = TextUtils.isEmpty(book.bookname) ? titleStrings[2] : book.bookname;
              String subtitle = TextUtils.isEmpty(book.subtitle) ? "制作中的视频秀" : book.subtitle;
              Item item = new Item(bookId, bookName, subtitle);
              setMatchNameToItem(context, bookId, item);
              item.isVideo = true;
              item.iconFile = new File(dir, bookId + HfxFileUtil.PHOTO_TYPE);
              type5.add(item);
              continue;
            } else {
              Item item = new Item(bookId, titleStrings[2], "制作中的视频秀");
              setMatchNameToItem(context, bookId, item);
              item.isVideo = true;
              item.iconFile = new File(dir, bookId + HfxFileUtil.PHOTO_TYPE);
              type5.add(item);
              continue;
            }


          } else if (titleStrings.length == 3 && titleStrings[1].equals("freevideo")) {
            //清理视频秀脏数据
            FileUtil.deleteDir(dir);
          }

          File mFile = HfxFileUtil.getStoryAudioFile(context, bookId);
          if (mFile.exists()) {
            File configFile = new File(dir, bookId + ".config");
            Huiben book = Utils.parseJsonFile(configFile, Huiben.class);

            if (book != null) {
              String bookName = TextUtils.isEmpty(book.bookname) ? titleStrings[2] : book.bookname;
              String subtitle = TextUtils.isEmpty(book.subtitle) ? "制作中的故事秀" : book.subtitle;
              Item item = new Item(bookId, bookName, subtitle);
              setMatchNameToItem(context, bookId, item);
              item.isAudio = true;
              item.iconFile = new File(dir, bookId + HfxFileUtil.PHOTO_TYPE);
              item.audoFile = mFile;
              type4.add(item);
            } else {
              Item item = new Item(bookId, titleStrings[2], "制作中的故事秀");
              setMatchNameToItem(context, bookId, item);
              item.isAudio = true;
              item.iconFile = new File(dir, bookId + HfxFileUtil.PHOTO_TYPE);
              item.audoFile = mFile;
              type4.add(item);
            }

          } else if (titleStrings.length == 3 && titleStrings[1].equals("freeaudio")) {
            //清理故事秀的脏数据
            FileUtil.deleteDir(dir);
          }


        }
      }
      if (!type1.isEmpty()) {
        data.add(new Item(context.getString(R.string.hfx_book_type1)));
        data.addAll(type1);
      }
      if (!type2.isEmpty()) {
        data.add(new Item(context.getString(R.string.hfx_book_type2)));
        data.addAll(type2);
      }
      if (!type3.isEmpty()) {
        data.add(new Item(context.getString(R.string.hfx_book_type3)));
        data.addAll(type3);
      }

      if (!type4.isEmpty()) {
        data.add(new Item(context.getString(R.string.hfx_book_type4)));
        data.addAll(type4);
      }
      if (!type5.isEmpty()) {
        data.add(new Item(context.getString(R.string.hfx_book_type5)));
        data.addAll(type5);
      }
    }

    /**
     * 显示活动名称
     */
    private void setMatchNameToItem(Context context, String bookId, Item item) {
      MatchInfo matchInfo = HfxUtil.getMatchInfo(context, bookId);
      if (matchInfo != null && !TextUtils.isEmpty(matchInfo.realUrl)) {
        item.matchName = matchInfo.matchName;
      }
    }

    @Override
    protected void onPostExecute(MakingWorkFragment myWorkFragment, Void aVoid) {
      if (myWorkFragment != null && !myWorkFragment.activity.isFinishing()) {
        myWorkFragment.onLoadDone(data);
      }
    }
  }


}
