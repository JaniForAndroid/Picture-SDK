package com.namibox.hfx.bean;

import android.content.Context;
import com.namibox.hfx.utils.HfxFileUtil;
import java.io.File;
import java.io.Serializable;

/**
 * Created by sunhapper on 2016/4/21 0021.
 */
public class VideoInfo implements Serializable {

  public String videoId;
  public int videoHeight;
  public int videoWidth;
  public int duration;
  private File videoDir;
  private File mp4File;
  private File upLoadFile;
  private File upLoadTempFile;

  public File getVideoDir(Context context) {
    return HfxFileUtil.getUserWorkDir(context, videoId);
  }

  public File getMp4File(Context context) {
    return HfxFileUtil.getVideoFile(context, videoId);
  }

  public File getUpLoadFile(Context context) {
    return HfxFileUtil.getUploadFile(context, videoId);
  }

  public File getUpLoadTempFile(Context context) {
    return HfxFileUtil.getUploadTempFile(context, videoId);
  }

  public VideoInfo(String videoId, int videoWidth, int videoHeight, int duration) {
    this.videoId = videoId;
    this.videoHeight = videoHeight;
    this.videoWidth = videoWidth;
    this.duration = duration;
  }
}
