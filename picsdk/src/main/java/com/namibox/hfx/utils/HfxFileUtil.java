package com.namibox.hfx.utils;

import android.content.Context;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Author: ryancheng
 * Create time: 2014/12/25 16:43
 */
public class HfxFileUtil {

  private static final String TAG = "FileUtil";
  private static final String VIDEO_TYPE = ".mp4";
  private static final String VIDEO_TEMP = "_tmp.mp4";
  private static final String UPLOAD_TEMP = "_uploadtmp.mp4";
  private static final String UPLOAD_TYPE = "_upload.mp4";
  private static final String IMAGE_TYPE = ".t";
  public static final String AUDIO_TYPE = ".m";
  public static final String PCM_TYPE = ".p";
  public static final String PHOTO_TYPE = ".j";
  private static final String MATCH_INFO = ".match";
  private static final String EXTRA = ".extra";
  private static final String INFO = ".info";
  private static final String TEMP = "temp";
  private static final String ZIP = "zip";
  private static final String CONFIG = "config";
  private static final String EVAL_CONFIG = "eval_config";
  private static final String PROPERTY = "property";
  public static final String AUDIO_WORK = "audio_work";
  public static final String HUIBEN_WORK = "huiben_work";
  public static final String CLASS_INFO = "class_info";
  public static final String UPLOAD_INFO = "upload_info";


  public static File getBookDir(Context context, String bookId) {
    File dir = new File(getCPStaticDir(context), bookId);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  public static File getBookConfigFile(Context context, String bookId) {
    return new File(getBookDir(context, bookId), CONFIG);
  }

  public static File getBookEvalConfigFile(Context context, String bookId) {
    return new File(getBookDir(context, bookId), EVAL_CONFIG);
  }

  public static File getBookPropFile(Context context, String bookId) {
    return new File(getBookDir(context, bookId), PROPERTY);
  }

  public static Properties getBookProp(Context context, String bookId) {
    Properties prop = new Properties();
    File file = getBookPropFile(context, bookId);
    if (file.exists()) {
      try {
        FileInputStream fis = new FileInputStream(file);
        prop.load(fis);
        fis.close();
        return prop;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  public static void putBookProp(Context context, String bookId, String key, String value) {
    Properties prop = new Properties();
    File file = getBookPropFile(context, bookId);
    try {
      FileOutputStream fos = new FileOutputStream(file, true);
      prop.setProperty(key, value);
      prop.store(fos, null);
      fos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static File getBookImageFile(Context context, String bookId, String pageName) {
    return new File(getBookDir(context, bookId),
        pageName.toLowerCase().replace(".jpg", IMAGE_TYPE));
  }

  public static File getBookAudioFile(Context context, String bookId, String mp3Name) {
    return new File(getBookDir(context, bookId), mp3Name.toLowerCase().replace(".mp3", AUDIO_TYPE));
  }

  public static File getBookAudioFileByPage(Context context, String bookId, String pageName) {
    return new File(getBookDir(context, bookId),
        pageName.toLowerCase().replace(".jpg", AUDIO_TYPE));
  }

  public static File getUserWorkDir(Context context) {
    String user_id = Utils.getLoginUserId(context);
    File dir = new File(getCPWorkDir(context), user_id);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  public static File getUserWorkDir(Context context, String bookId) {
    File dir = new File(getUserWorkDir(context), bookId);
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

  public static File getUserTempDir(Context context, String bookId) {
    File dir = new File(getUserWorkDir(context, bookId), TEMP);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  /**
   * 绘本的用户录制的音频路径
   */
  public static File getUserAudioFile(Context context, String bookId, String mp3Name) {
    return new File(getUserWorkDir(context, bookId),
        mp3Name.toLowerCase().replace(".mp3", AUDIO_TYPE));
  }

  public static File getUserAudioZipFile(Context context, String bookId) {
    return new File(getUserWorkDir(context, bookId), ZIP);
  }

  public static File getUserAudioFileByPage(Context context, String bookId, String pageName) {
    return new File(getUserWorkDir(context, bookId),
        pageName.toLowerCase().replace(".jpg", AUDIO_TYPE));
  }

  public static File[] getAllBookAudioFile(Context context, String bookId, final boolean isEval) {
    File dir = getUserWorkDir(context, bookId);
    return dir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        if (isEval) {
          return pathname.isFile() && pathname.getName().endsWith(AUDIO_TYPE) && pathname.getName()
              .contains("eval");
        } else {
          return pathname.isFile() && pathname.getName().endsWith(AUDIO_TYPE);
        }
      }
    });
  }

  public static File getUserAudioTempFileByPage(Context context, String bookId, String pageName) {
    return new File(getUserTempDir(context, bookId),
        pageName.toLowerCase().replace(".jpg", AUDIO_TYPE));
  }

  //    @SuppressWarnings("NullArgumentToVariableArgMethod")
//    public static String[] getExternalStorage(Context context) {
//        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
//        try {
//            return (String[]) sm.getClass().getMethod("getVolumePaths", new Class<?>[0]).invoke(sm, new Object[0]);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

  static final String CP_DIR = "namibox/contentProd";
  static final String CP_STATIC_DIR = "static";
  static final String CP_WORK_DIR = "work";

  public static File getCPDir(Context context) {
    String selected = PreferenceUtil.getSelectedStorage(context);
    File f = new File(selected, CP_DIR);
    if (!f.exists() && !f.mkdirs()) {
      f = new File(context.getFilesDir(), CP_DIR);
      if (!f.exists() && !f.mkdirs()) {
        throw new IllegalStateException("can't create cp dir");
      }
    }
    return f;
  }

  public static File getCPStaticDir(Context context) {
    File f = new File(getCPDir(context), CP_STATIC_DIR);
    if (!f.exists()) {
      f.mkdirs();
    }
    return f;
  }

  public static File getCPWorkDir(Context context) {
    File f = new File(getCPDir(context), CP_WORK_DIR);
    if (!f.exists()) {
      f.mkdirs();
    }
    return f;
  }

  // =========================hfx fileutil=====================

  /**
   * 获取制作中视频文件
   */
  public static File getVideoFile(Context context, String videoId) {
    File videoDir = getUserWorkDir(context, videoId);
    return new File(videoDir, videoId + VIDEO_TYPE);
  }

  /**
   * 较早之前的版本是将视频信息，宽高等写在文件名中解析的
   * 后将视频信息保存到单独的文件中，见getVideoInfoFile
   * 命名改成getVideoFile的方式
   */
  public static File getHfxVideoFile(Context context, String videoId) {

    File videoDir = getUserWorkDir(context, videoId);
    File[] videos = videoDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isFile() && pathname.getName().endsWith("temp" + VIDEO_TYPE);
      }
    });
    if (videos != null && videos.length > 0) {
      return videos[0];
    } else {
      return null;
    }
  }

  /**
   * 转码时的temp文件，转码完成才重命名为要上传的视频文件名
   */
  public static File getUploadTempFile(Context context, String videoId) {
    File videoDir = getUserWorkDir(context, videoId);
    return new File(videoDir, videoId + UPLOAD_TEMP);
  }

  /**
   * 要上传的视频文件，存在表示转码成功
   */
  public static File getUploadFile(Context context, String videoId) {
    File videoDir = getUserWorkDir(context, videoId);
    return new File(videoDir, videoId + UPLOAD_TYPE);
  }

  /**
   * 剪裁时的temp文件
   */
  public static File getCutTempVideoFile(Context context, String videoId) {
    File videoTempDir = getUserTempDir(context, videoId);
    return new File(videoTempDir, videoId + VIDEO_TEMP);
  }

  /**
   * 制作中再次制作视频，需要将制作中的视频复制出来，再次剪裁然后覆盖原制作中视频
   */
  public static File getCopyTempVideoFile(Context context, String videoId) {
    File videoDir = getUserWorkDir(context, videoId);
    return new File(videoDir, videoId + VIDEO_TEMP);
  }


  /**
   * 获取封面图片，包括故事秀视频秀
   */
  public static File getCoverFile(Context context, String id) {
    File dir = getUserWorkDir(context, id);
    return new File(dir, id + PHOTO_TYPE);

  }

  /**
   * 页面提交相关
   */
  public static File getMatchInfoFile(Context context, String id) {
    File dir = getUserWorkDir(context, id);
    return new File(dir, id + MATCH_INFO);

  }

  /**
   * 检测绘本是否已提交时用到的extra信息
   */
  public static File getExtraInfoFile(Context context, String id) {
    File dir = getUserWorkDir(context, id);
    return new File(dir, id + EXTRA);

  }

  /**
   * 是否提交到班级圈的信息
   */
  public static File getClassInfo(Context context, String id) {
    File dir = getUserWorkDir(context, id);
    return new File(dir, id + CLASS_INFO);
  }

  /**
   * 直传相关信息
   */
  public static File getDirectUploadInfo(Context context, String id) {
    File dir = getUserWorkDir(context, id);
    return new File(dir, id + UPLOAD_INFO);
  }

  /**
   * 视频相关信息，宽高duration等
   */
  public static File getVideoInfoFile(Context context, String id) {
    File dir = getUserWorkDir(context, id);
    return new File(dir, id + INFO);

  }

  public static File getAudioInfoFile(Context context, String id) {
    File dir = getUserWorkDir(context, id);
    return new File(dir, id + INFO);
  }

  /**
   * 获取故事秀音频文件
   */
  public static File getStoryAudioFile(Context context, String id) {
    File dir = getUserWorkDir(context, id);
    return new File(dir, id + AUDIO_TYPE);
  }

}
