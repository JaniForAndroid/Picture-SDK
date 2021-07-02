package com.namibox.hfx.utils;

import android.content.Context;
import android.util.Log;
import com.namibox.hfx.bean.RxEvent;
import com.namibox.util.MediaUtils;
import io.microshow.rxffmpeg.FfmpegUtil;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Cancellable;
import java.io.File;


/**
 * Created by sunha on 2016/12/19 0019.
 */

public class RxFFmpeg {

  private static final String TAG = "RxFFmpeg";

//  /**
//   * @param inFile 输入文件
//   * @param outFile 输出文件
//   * @param crf 码率控制(0-51)0最好 23默认
//   * @param width 输出宽度
//   * @param height 输出高度
//   */
//  public static Flowable<Integer> getTranscodeObservable(final String inFile,
//      final String outFile, final int crf, final int width, final int height) {
//    return Flowable.create(new FlowableOnSubscribe<Integer>() {
//      @Override
//      public void subscribe(@NonNull final FlowableEmitter<Integer> integerEmitter)
//          throws Exception {
//        FFmpegCallBack callBack = new FFmpegCallBack() {
//          @Override
//          public void onProgress(int currentTime) {
//            integerEmitter.onNext(currentTime);
//          }
//
//          @Override
//          public void onFailure() {
//            integerEmitter.onError(new Exception("ffmpeg_fail"));
//          }
//
//          @Override
//          public void onSuccess() {
//            integerEmitter.onComplete();
//          }
//        };
//        integerEmitter.setCancellable(new Cancellable() {
//          @Override
//          public void cancel() throws Exception {
//
//          }
//        });
//        new FFmpegCmd().transCode(inFile, outFile, crf, width, height, callBack);
//      }
//    }, BackpressureStrategy.LATEST);
//
//  }

  public static Flowable<Integer> getCutVideoObservable(final double startTime,
      final double endTime, final String inFile, final String outFile) {
    return Flowable.create(new FlowableOnSubscribe<Integer>() {
      @Override
      public void subscribe(@NonNull final FlowableEmitter<Integer> integerEmitter)
          throws Exception {
        RxFFmpegSubscriber callBack = new RxFFmpegSubscriber() {
          @Override
          public void onFinish() {
            integerEmitter.onComplete();
          }

          @Override
          public void onProgress(int progress, long progressTime) {

          }

          @Override
          public void onCancel() {

          }

          @Override
          public void onError(String message) {
            integerEmitter.onError(new Exception("ffmpeg_fail"));
          }
        };
        FfmpegUtil.cutVideo(startTime, endTime, inFile, outFile, callBack);
      }
    }, BackpressureStrategy.LATEST);
  }


  public static Flowable<Integer> getComposeVideoObservable(final String videoFile,
      final String audioFile, final String outFile) {
    return Flowable.create(new FlowableOnSubscribe<Integer>() {
      @Override
      public void subscribe(@NonNull final FlowableEmitter<Integer> integerEmitter)
          throws Exception {
        RxFFmpegSubscriber callBack = new RxFFmpegSubscriber() {
          @Override
          public void onFinish() {
            integerEmitter.onComplete();
          }

          @Override
          public void onProgress(int progress, long progressTime) {

          }

          @Override
          public void onCancel() {

          }

          @Override
          public void onError(String message) {
            integerEmitter.onError(new Exception("ffmpeg_fail"));
          }
        };
        FfmpegUtil.composeVideo2(videoFile, audioFile, outFile, callBack);
      }
    }, BackpressureStrategy.LATEST);
  }

  public static Flowable<RxEvent> getVideo2PcmObservable(Context context, final File inFile,
      final File outFile, final int sampleRate, final boolean isBigEndian) {

    final long duration = MediaUtils.getDuration(context, inFile.getAbsolutePath());
    return Flowable.create(new FlowableOnSubscribe<RxEvent>() {
      @Override
      public void subscribe(@NonNull final FlowableEmitter<RxEvent> emitter) throws Exception {
        RxFFmpegSubscriber callBack = new RxFFmpegSubscriber() {
          @Override
          public void onFinish() {
            emitter.onComplete();
          }

          @Override
          public void onProgress(int progress, long progressTime) {
            RxEvent rxEvent = new RxEvent();
            rxEvent.type = 2;
            rxEvent.progress = (int) (progressTime * 100f / duration);
            Log.i(TAG, "onProgress: " + rxEvent);
            emitter.onNext(rxEvent);
          }

          @Override
          public void onCancel() {

          }

          @Override
          public void onError(String message) {
            emitter.onError(new Exception("ffmpeg_fail"));
          }
        };
        emitter.setCancellable(new Cancellable() {
          @Override
          public void cancel() throws Exception {

          }
        });
        FfmpegUtil.video2Pcm(inFile.getAbsolutePath(), outFile.getAbsolutePath(), sampleRate, isBigEndian,
            callBack);
      }
    }, BackpressureStrategy.LATEST);
  }

}
