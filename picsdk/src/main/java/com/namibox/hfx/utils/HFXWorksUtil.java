package com.namibox.hfx.utils;

import static com.namibox.hfx.utils.HfxFileUtil.AUDIO_TYPE;
import static com.namibox.hfx.utils.HfxFileUtil.PHOTO_TYPE;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.namibox.commonlib.common.ApiHandler;
import com.namibox.commonlib.event.OssEvent;
import com.namibox.commonlib.event.OssEvent.OssEventType;
import com.namibox.commonlib.event.QiniuEvent;
import com.namibox.commonlib.exception.MessageException;
import com.namibox.commonlib.model.OssToken;
import com.namibox.commonlib.model.QiniuToken;
import com.namibox.hfx.bean.AudioInfo;
import com.namibox.hfx.bean.ClassInfo;
import com.namibox.hfx.bean.EvalBody;
import com.namibox.hfx.event.ZipEvent;
import com.namibox.hfx.event.ZipEvent.ZipEventType;
import com.namibox.tools.OssUploadUtil;
import com.namibox.tools.QiniuUploadUtil;
import com.namibox.util.FileUtil;
import com.namibox.util.Logger;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import io.microshow.rxffmpeg.FfmpegUtil;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DefaultSubscriber;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import okhttp3.MultipartBody;
import org.json.JSONException;
import org.reactivestreams.Publisher;

/**
 * Create time: 2017/6/14.
 */

public class HFXWorksUtil {

  private static final String TAG = "HFXWorksUtil";

  public interface VideoTransCallback {

    void onTranscodeProgress(int currentTime);

    void onTranscodeFinished(boolean success);
  }

  public static void startVideoTransCode(int videoWidth, int videoHeight, String inFilePath,
                                         String outFilePath, final VideoTransCallback callback) {
    FfmpegUtil.transcode(inFilePath, outFilePath, 23, videoWidth, videoHeight)
        .subscribe(new RxFFmpegSubscriber() {
          @Override
          public void onFinish() {
            callback.onTranscodeFinished(true);
          }

          @Override
          public void onProgress(int progress, long progressTime) {
            callback.onTranscodeProgress((int) progressTime);
          }

          @Override
          public void onCancel() {
          }

          @Override
          public void onError(String message) {
            callback.onTranscodeFinished(false);
          }
        });
  }

  private static class NeedLoginException extends RuntimeException {

    @Override
    public String getMessage() {
      return "need login";
    }
  }

  public interface UploadQiNiuCallback {

    void onError(String error);

    void onLoginError();

    void onUploadProgress(double progress);

    void onSuccess(String persistentId);
  }

  public static void cancelUpload(boolean cancel) {
    QiniuUploadUtil.cancel(cancel);
  }

  /**
   * 视频秀上传到七牛
   *
   * @param coverTime 七牛截取封面的时间点，ms，通过api传给后端生成的token里面会带这个信息
   */
  public static void startUploadQiNiu(Context context, int coverTime, String id,
                                      final File upLoadFile, final UploadQiNiuCallback callback) {
    String cut_time = coverTime / 1000 + "." + coverTime % 1000;
    final String dir = FileUtil.getFileCacheDir(context).getAbsolutePath();
    ApiHandler.getBaseApi()
        .getUploadToken(id, "mp4", id, cut_time)
        .flatMap(new Function<QiniuToken, Flowable<QiniuEvent>>() {
          @Override
          public Flowable<QiniuEvent> apply(@NonNull QiniuToken qiniuToken) throws Exception {
            if (qiniuToken.errcode == 1001) {
              throw new NeedLoginException();
            }
            return QiniuUploadUtil.upload(dir, upLoadFile,
                qiniuToken.key, qiniuToken.token);
          }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DefaultSubscriber<QiniuEvent>() {
          @Override
          public void onComplete() {

          }

          @Override
          public void onError(Throwable e) {
            e.printStackTrace();
            if (e instanceof NeedLoginException) {
              callback.onLoginError();
            } else {
              callback.onError("文件上传失败: " + e.getMessage());
            }
          }

          @Override
          public void onNext(QiniuEvent event) {
            if (event.type == QiniuEvent.QiniuEventType.RESULT) {
              if (event.info.isOK()) {
                String persistentId = null;
                try {
                  persistentId = event.response.getString("persistentId");
                } catch (JSONException e) {
                  e.printStackTrace();
                }
                if (!TextUtils.isEmpty(persistentId)) {
                  callback.onSuccess(persistentId);
                } else {
                  callback.onError("文件上传失败");
                }
              } else {
                String error;
                if (event.info.isCancelled()) {
                  error = "文件上传取消";
                } else if (event.info.isNetworkBroken()) {
                  error = "网络出现问题,上传失败";
                } else if (event.info.isServerError()) {
                  error = "服务器出现问题,上传失败";
                } else {
                  error = "上传失败";
                }
                callback.onError(error);

              }
            } else if (event.type == QiniuEvent.QiniuEventType.PROGRESS) {
              callback.onUploadProgress(event.progress);
            }
          }
        });
  }

  private static Flowable<ZipEvent> getZipObservable(final Context context, final String id,
                                                     final String workType, final boolean isEval) {
    return Flowable.create(new FlowableOnSubscribe<ZipEvent>() {
      @Override
      public void subscribe(@NonNull FlowableEmitter<ZipEvent> zipEventEmitter) throws Exception {
        try {
          //int progress = 0;
          File zip = HfxFileUtil.getUserAudioZipFile(context, id);
          ZipParameters parameters = new ZipParameters();
          parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
          parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

          if (zip.exists() && zip.delete()) {
            Logger.d(TAG, "delete old zip");
          }
          Logger.d(TAG, "start zip");
          ZipFile zipFile = new ZipFile(zip);

          if (HfxFileUtil.HUIBEN_WORK.equals(workType)) {
            File[] audios = HfxFileUtil.getAllBookAudioFile(context, id, isEval);
            for (int i = 0; i < audios.length; i++) {
              File audio = audios[i];
              if (audio.exists() && audio.length() > 0) {
                Logger.d(TAG, "zip: " + audio);

                zipFile.addFile(audio, parameters);

              }
              int current = i + 1;
              int total = audios.length;
              zipEventEmitter.onNext(new ZipEvent(current, total));
            }
          } else if (HfxFileUtil.AUDIO_WORK.equals(workType)) {

            File mp3File = HfxFileUtil.getStoryAudioFile(context, id);
            File photoFile = HfxFileUtil.getCoverFile(context, id);
            if (mp3File.exists() && mp3File.length() > 4096 && mp3File.length() < 157286400) {
              Logger.d(TAG, "zip: " + mp3File);
              zipFile.addFile(mp3File, parameters);
            } else {
              if (!zipEventEmitter.isCancelled()) {
                zipEventEmitter.onError(new MessageException("音频文件异常"));
              }
            }
            if (photoFile.exists() && photoFile.length() > 0) {
              Logger.d(TAG, "zip: " + photoFile);
              zipFile.addFile(photoFile, parameters);
            } else {
              if (!zipEventEmitter.isCancelled()) {
                zipEventEmitter.onError(new MessageException("封面文件异常"));
              }
            }
          }
          if (zipFile.isValidZipFile() && zip.exists() && zip.length() > 0) {
            zipEventEmitter.onNext(new ZipEvent(zip));
            zipEventEmitter.onComplete();
          }

        } catch (ZipException e) {
          e.printStackTrace();
          if (!zipEventEmitter.isCancelled()) {
            zipEventEmitter.onError(new MessageException("文件压缩异常"));
          }
        }
      }
    }, BackpressureStrategy.LATEST);
  }

  public interface ZipAndOssCallback {

    void onError(String error);

    void onLoginError();

    void onZipProgress(int current, int total);

    void onUploadProgress(String mediaType, long current, long total);

    void onSuccess();
  }

  private static class LogoutException extends Exception {

    @Override
    public String getMessage() {
      return "未登录";
    }
  }

  /**
   * 故事秀、绘本压缩上传
   */
  public static Disposable startZipAndUploadToOss(final Context context, final String bookId,
                                                  final String workType,
                                                  final String introduce, final String params, final String contentType, final String[] strings,
                                                  final String audioTitle, final String subType, final ClassInfo classInfo,
                                                  final boolean isEval,
                                                  final ZipAndOssCallback callback) {
    return getZipObservable(context, bookId, workType, isEval)//压缩
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(new Consumer<ZipEvent>() {
          @Override
          public void accept(ZipEvent zipEvent) throws Exception {
            if (zipEvent.type == ZipEventType.PROGRESS) {
              callback.onZipProgress(zipEvent.current, zipEvent.total);//回调压缩进度
            }
          }
        })
        .filter(new Predicate<ZipEvent>() {
          @Override
          public boolean test(@NonNull ZipEvent zipEvent) throws Exception {
            return zipEvent.type == ZipEventType.RESULT;//进度已经在上面处理了，事件不需要往下传递，只传递压缩成功事件
          }
        })
        .observeOn(Schedulers.io())
        .zipWith(ApiHandler.getBaseApi()
                //替换获取osstoken接口
                .ossPicInfoObsevable()//获取token
//                .ossInfoObsevable()
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<OssToken, Flowable<OssToken>>() {
                  @Override
                  public Flowable<OssToken> apply(@NonNull OssToken ossToken) throws Exception {
                    //对获取token的结果进行处理，主要是为了处理未登录的情况
                    //因为要返回错误，所以用flatMap而不用map，map只能转化事件，而不能将一个事件转化成一个error
                    if (ossToken.errcode == 0) {
                      return Flowable.just(ossToken);
                    } else if (ossToken.errcode == 1001) {
                      return Flowable.error(new LogoutException());
                    } else {
                      return Flowable.error(new MessageException("网络出现问题"));
                    }
                  }
                })
            , new BiFunction<ZipEvent, OssToken, OssToken>() {//这是zipWith的第二个参数。
              @Override
              public OssToken apply(@NonNull ZipEvent zipEvent, @NonNull OssToken ossToken) {
                //将zip操作和获取token操作结合产生一个新的事件，主要是把ZipEvent带的zipFile赋值给ossToken的uploadFile
                String name = "";
                if (HfxFileUtil.HUIBEN_WORK.equals(workType)) {
                  String user_id = Utils.getLoginUserId(context);
                  name = user_id + "_" + bookId + ".zip";
                } else if (HfxFileUtil.AUDIO_WORK.equals(workType)) {
                  name = bookId + ".zip";
                }
                ossToken.objectKey = ossToken.objectKey + "/" + name;
                ossToken.uploadFile = zipEvent.zipFile;
                return ossToken;
              }
            })
        .observeOn(Schedulers.io())
        .concatMap(
            new Function<OssToken, Flowable<OssEvent>>() {//这里应该只会处理一个事件，每次上传只传一个文件，所以应该也可以用flatMap(存疑，concatMap肯定也没错)
              @Override
              public Flowable<OssEvent> apply(@NonNull OssToken ossToken) {
                return OssUploadUtil.getOssObservable(context, ossToken);//真正上传的地方
              }
            })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<OssEvent>() {
          @Override
          public void accept(OssEvent ossEvent) throws Exception {
            if (ossEvent.type == OssEventType.PROGRESS) {
              callback.onUploadProgress("", ossEvent.current, ossEvent.total);
            } else if (ossEvent.type == OssEventType.RESULT) {
              uploadWorkInfo(context, 0, ossEvent.file.length(), workType, bookId, introduce,
                  params, contentType, strings, audioTitle, subType, classInfo, "", false, isEval,
                  callback);
            }
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable e) throws Exception {
            e.printStackTrace();
            if (e instanceof LogoutException) {
              callback.onLoginError();
            } else {
              callback.onError(e.getMessage());
            }
          }
        });
  }

  /**
   * 故事秀直传
   */
  public static Disposable startUploadAudioToOss(final Context context, final int duration,
                                                 final String bookId,
                                                 final String workType,
                                                 final String introduce, final String params, final String contentType, final String[] strings,
                                                 final String audioTitle,
                                                 final String subType, final ClassInfo classInfo, final ZipAndOssCallback callback) {
    //cid在回调中要使用，要定义成final，cid的赋值也是在回调中，所以只能这么写
    final String[] cid = new String[1];
    final File mp3File = HfxFileUtil.getStoryAudioFile(context, bookId);
    final long fileSize = mp3File.length();
    return ApiHandler.getBaseApi().ossInfoAudioObsevable()//取token
        .flatMap(new Function<OssToken, Publisher<OssEvent>>() {
          @Override
          public Publisher<OssEvent> apply(final OssToken ossToken) {
            File photoFile = HfxFileUtil.getCoverFile(context, bookId);
            cid[0] = ossToken.oms_cid;
            //两次使用的是同一个ossToken对象，所以记录原始的objectKey，后面拼真正的objectKey基于原始的
            final String objectKey = ossToken.objectKey;
            return Flowable.just(mp3File, photoFile).map(new Function<File, OssToken>() {
              @Override
              public OssToken apply(File file) {
                //文件格式用真实格式而不是用.m .j，后端要求的
                String name = file.getName();
                if (name.endsWith(AUDIO_TYPE)) {
                  name = name.substring(0, name.lastIndexOf("."));
                  name += ".mp3";
                } else if (name.endsWith(PHOTO_TYPE)) {
                  name = name.substring(0, name.lastIndexOf("."));
                  name += ".jpg";
                }
                ossToken.objectKey = objectKey + "/" + name;
                ossToken.uploadFile = file;
                return ossToken;
              }
            }).concatMap(new Function<OssToken, Flowable<OssEvent>>() {
              @Override
              public Flowable<OssEvent> apply(@NonNull OssToken ossToken) {
                return OssUploadUtil.getOssObservable(context, ossToken);
              }
            });
          }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<OssEvent>() {
          @Override
          public void accept(OssEvent ossEvent) {
            if (ossEvent.type == OssEventType.PROGRESS) {
              String mediaType = "";
              if (ossEvent.file != null) {
                if (ossEvent.file.getAbsolutePath().endsWith(HfxFileUtil.PHOTO_TYPE)) {
                  mediaType = "封面";
                } else if (ossEvent.file.getAbsolutePath().endsWith(AUDIO_TYPE)) {
                  mediaType = "音频";
                }
              }
              callback.onUploadProgress(mediaType, ossEvent.current, ossEvent.total);
            } else if (ossEvent.type == OssEventType.RESULT) {
              //分开上传，单次成功的结果不处理，最后处理complite事件
              Log.i(TAG, "accept: ");
            }
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable e) {
            e.printStackTrace();
            if (e instanceof LogoutException) {
              callback.onLoginError();
            } else {
              callback.onError(e.getMessage());
            }
          }
        }, new Action() {
          @Override
          public void run() {
            uploadWorkInfo(context, duration, fileSize, workType, bookId, introduce,
                params, contentType, strings, audioTitle, subType, classInfo, cid[0], true, false,
                callback);
          }
        });
  }

  private static void uploadWorkInfo(final Context context, int duration, long fileSize,
                                     final String workType,
                                     String bookId, String introduce,
                                     String params, String contentType, String[] strings, String audioTitle, String subType,
                                     ClassInfo classInfo, String cid, boolean direct_upload, boolean isEval,
                                     final ZipAndOssCallback callback) {
    MultipartBody.Builder builder = new MultipartBody.Builder();
    builder.setType(MultipartBody.FORM);
    if (duration > 0) {
      builder.addFormDataPart("duration", String.valueOf(duration));
    }
    if (!TextUtils.isEmpty(introduce)) {//introduce用来区分是页面上传还是原生，原生有introduce，页面需要parameters字段
      if (HfxFileUtil.HUIBEN_WORK.equals(workType)) {
        builder.addFormDataPart("content_type", contentType);
        builder.addFormDataPart("introduce", introduce);
        builder.addFormDataPart("bookid", bookId);//创作绘本时从接口取到的id是什么这里就是什么
        builder.addFormDataPart("file_size", String.valueOf(fileSize));
      } else if (HfxFileUtil.AUDIO_WORK.equals(workType)) {
        builder.addFormDataPart("content_type", "freeaudio");
        builder.addFormDataPart("introduce", introduce);
        builder.addFormDataPart("bookid", strings[1] + "_" + strings[2]);//不带userId，规则就这样
        builder.addFormDataPart("file_size", String.valueOf(fileSize));
        builder.addFormDataPart("title", audioTitle);
        builder.addFormDataPart("subtype", subType);
      }
    } else {
      if (HfxFileUtil.HUIBEN_WORK.equals(workType)) {
        String extra = HfxUtil.getExtraInfo(context, bookId);
        builder.addFormDataPart("content_type", contentType);
        builder.addFormDataPart("bookid", bookId);
        builder.addFormDataPart("file_size", String.valueOf(fileSize));
        builder.addFormDataPart("parameters", params);
        builder.addFormDataPart("extra", extra);
      } else if (HfxFileUtil.AUDIO_WORK.equals(workType)) {
        builder.addFormDataPart("content_type", "freeaudio");
        builder.addFormDataPart("bookid", strings[1] + "_" + strings[2]);
        builder.addFormDataPart("file_size", String.valueOf(fileSize));
        builder.addFormDataPart("parameters", params);
      }
    }
    if (isEval) {
      PreferenceUtil.setSharePref(context, bookId + "cost_time", 0L);
      File file = new File(FileUtil.getFileCacheDir(context),
          Utils.getLoginUserId(context) + "_eval_" + bookId);
      Object readObject = null;
      if (file.exists()) {
        try {
          FileInputStream fis = new FileInputStream(file);
          ObjectInputStream ois = new ObjectInputStream(fis);
          readObject = ois.readObject();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (readObject != null) {
        ArrayList<EvalBody> evalBodies = (ArrayList<EvalBody>) readObject;
        builder.addFormDataPart("recognize_datas", new Gson().toJson(evalBodies));
      }
    }
    classInfo = HfxUtil.getClassInfo(context, bookId);
    //是否提交到班级圈相关
    if (classInfo != null) {
      if (!TextUtils.isEmpty(classInfo.transmissionParam)) {
        builder.addFormDataPart("transmissionparam", classInfo.transmissionParam);
      }
      if (classInfo.classCheck != 0) {
        builder.addFormDataPart("classcheckresult", classInfo.classCheck + "");
      }
    }
    //直传需要
    if (direct_upload) {
      builder.addFormDataPart("direct_upload", String.valueOf(true));
      builder.addFormDataPart("oms_cid", cid);
      if (HfxFileUtil.AUDIO_WORK.equals(workType)) {
        AudioInfo audioInfo = HfxUtil
            .getAudioInfo(context, strings[0] + "_" + strings[1] + "_" + strings[2]);
        if (audioInfo != null && audioInfo.duration > 0) {
          builder.addFormDataPart("duration", String.valueOf(audioInfo.duration));
        }
      }
    }
    MultipartBody multipartBody = builder.build();
    ApiHandler.getBaseApi()
        //切换上报接口
        .uploadPicWorkInfo(multipartBody)
//        .uploadWorkInfo(multipartBody)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DefaultSubscriber<JsonObject>() {

          @Override
          public void onComplete() {

          }

          @Override
          public void onError(Throwable e) {
            Logger.e(e, "提交信息出错");
            callback.onError("提交信息出错!");
          }

          @Override
          public void onNext(JsonObject result) {
            if (result != null) {
              callback.onSuccess();
//              ARouter.getInstance().build("/pic/voiceshare")
//                  .withString("json_url", result.toString())
//                  .navigation();
            } else {
              callback.onError("提交信息出错!");
            }
          }
        });
  }

}
