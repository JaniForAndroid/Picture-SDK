package com.namibox.commonlib.listener;

/**
 * @Description 日志上传回调接口
 * @CreateTime: 2020/5/20 18:54
 * @Author: zhangkx
 */
public interface LogUploadListener {

  /**
   * 日志上传成功
   */
  void onSuccess();

  /***
   * 日志上传失败
   */
  void onError();
}
