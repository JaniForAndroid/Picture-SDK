package com.namibox.hfx.utils;

import android.content.Context;
import com.namibox.util.MD5Util;
import com.namibox.util.Utils;
import java.io.File;
import java.io.IOException;

/**
 * Created by sunha on 2016/12/29 0029.
 */

public class DubFileUtil {

  public static File getRawPcmFile(Context context, String itemId) {
    File dir = getDubItemDir(context, itemId);
    return new File(dir, itemId + ".raw");
  }

  public static File getScoreFile(Context context, String itemId) {
    File dir = getDubItemDir(context, itemId);
    return new File(dir, itemId + ".score");
  }

  public static File getRawPcmTemp(Context context, String itemId) {
    File dir = getDubItemDir(context, itemId);
    return new File(dir, itemId + "_temp.raw");
  }

  public static File getAACFile(Context context, String itemId) {
    File dir = getDubItemDir(context, itemId);
    return new File(dir, itemId + ".aac");
  }

  public static File getM4aFile(Context context, String itemId) {
    File dir = getDubItemDir(context, itemId);
    return new File(dir, itemId + ".m4a");
  }

  public static File getMixedPcmFile(Context context, String itemId) {
    File dir = getDubItemDir(context, itemId);
    return new File(dir, itemId + "_mixed.raw");
  }

  public static File getMixedMp4File(Context context, String itemId) {
    File dir = getDubItemDir(context, itemId);
    return new File(dir, itemId + "_mixed.mp4");
  }

  public static File getWavFileById(Context context, String itemId, int index) {
    File dir = getDubItemDir(context, itemId);
    return new File(dir, itemId + "_" + index + ".wav");
  }

  public static File getPcmItemFileById(Context context, String itemId, int index) {
    File dir = getDubItemDir(context, itemId);
    return new File(dir, itemId + "_" + index + ".raw");
  }


  public static File getMp3FileByIndex(Context context, String itemId, int index) {
    File dir = getDubItemDir(context, itemId);
    return new File(dir, itemId + "_" + index + ".mp3");
  }

  public static File getMp4File(Context context, String itemId) {
    File dir = getDubItemDir(context, itemId);
    return new File(dir, itemId + ".mp4");
  }


  public static File getCacheFile(Context context, String url) {
    File dir = getDubResDir(context);
    return new File(dir, MD5Util.md5(url));
  }

  //视频配音的目录
  public static File getDubDir(Context context) {
    String user_id = Utils.getLoginUserId(context);
    File dir = new File(HfxFileUtil.getCPWorkDir(context), user_id + "dub");
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  public static File getDubResDir(Context context) {
    File dir = new File(HfxFileUtil.getCPWorkDir(context), "video_dub_md5");
    if (!dir.exists()) {
      dir.mkdirs();
      File nomedia = new File(dir, ".nomedia");
      if (!nomedia.exists()) {
        try {
          nomedia.createNewFile();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    }
    return dir;
  }

  public static File getDubItemDir(Context context, String itemId) {
    File dir = new File(getDubDir(context), itemId);
    if (!dir.exists()) {
      dir.mkdirs();
      File nomedia = new File(dir, ".nomedia");
      if (!nomedia.exists()) {
        try {
          nomedia.createNewFile();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    }
    return dir;
  }

  public static File getDownloadTempFile(Context context) {
    return new File(getDubResDir(context), "download_temp");
  }


}
