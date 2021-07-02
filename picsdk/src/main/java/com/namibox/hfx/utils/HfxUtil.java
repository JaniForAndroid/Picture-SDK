package com.namibox.hfx.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.namibox.hfx.bean.AudioInfo;
import com.namibox.hfx.bean.ClassInfo;
import com.namibox.hfx.bean.CommitInfo;
import com.namibox.hfx.bean.MatchInfo;
import com.namibox.hfx.bean.UploadInfo;
import com.namibox.hfx.bean.VideoInfo;
import com.namibox.util.FileUtil;
import com.namibox.util.Utils;
import java.io.File;
import java.io.IOException;

/**
 * Created by sunha on 2017/8/9 0009.
 */

public class HfxUtil {

  public static void saveMatchInfo(Context context, String id, String matchId, String matchName,
      String submitUrl) throws IOException {
    File jsonFile = HfxFileUtil.getMatchInfoFile(context, id);
    if (!TextUtils.isEmpty(submitUrl)) {
      Gson gson = new Gson();
      MatchInfo match = new MatchInfo();
      match.id = id;
      match.matchId = matchId;
      match.matchName = matchName;
      match.realUrl =
          submitUrl + "?" + "id=" + id + "&matchid=" + matchId + "&matchname=" + matchName;
      String matchString = gson.toJson(match);
      FileUtil.StringToFile(matchString, jsonFile, "UTF-8");
    } else {
      jsonFile.delete();
    }
  }

  public static void saveExtraInfo(Context context, String id, String extra) throws IOException {
    File jsonFile = HfxFileUtil.getExtraInfoFile(context, id);
    if (!TextUtils.isEmpty(extra)) {
      FileUtil.StringToFile(extra, jsonFile, "UTF-8");
    } else {
      jsonFile.delete();
    }
  }

  public static String getExtraInfo(Context context, String id) {
    File file = HfxFileUtil.getExtraInfoFile(context, id);
    if (file.exists() && file.length() > 0) {
      try {
        return FileUtil.FileToString(file, "UTF-8");
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }

  public static void saveClassInfo(Context context, String id, String transmissionParm,
      int classCheck) throws IOException {
    File jsonFile = HfxFileUtil.getClassInfo(context, id);
    Gson gson = new Gson();
    ClassInfo classInfo = new ClassInfo();
    classInfo.id = id;
    classInfo.classCheck = classCheck;
    classInfo.transmissionParam = transmissionParm;
    String classString = gson.toJson(classInfo);
    FileUtil.StringToFile(classString, jsonFile, "UTF-8");
  }

  public static MatchInfo getMatchInfo(Context context, String id) {
    File file = HfxFileUtil.getMatchInfoFile(context, id);
    if (file.exists() && file.length() > 0) {
      return Utils.parseJsonFile(file, MatchInfo.class);
    } else {
      return null;
    }
  }


  public static void saveDirectUploadInfo(Context context, String id, boolean direct_upload)
      throws IOException {
    File jsonFile = HfxFileUtil.getDirectUploadInfo(context, id);
    Gson gson = new Gson();
    UploadInfo uploadInfo = new UploadInfo();
    uploadInfo.id = id;
    uploadInfo.direct_upload = direct_upload;
    String classString = gson.toJson(uploadInfo);
    FileUtil.StringToFile(classString, jsonFile, "UTF-8");
  }

  public static UploadInfo getDirectUploadInfo(Context context, String id) {
    File file = HfxFileUtil.getDirectUploadInfo(context, id);
    if (file.exists() && file.length() > 0) {
      return Utils.parseJsonFile(file, UploadInfo.class);
    } else {
      return null;
    }
  }

  public static ClassInfo getClassInfo(Context context, String id) {
    File file = HfxFileUtil.getClassInfo(context, id);
    if (file.exists() && file.length() > 0) {
      return Utils.parseJsonFile(file, ClassInfo.class);
    } else {
      return null;
    }
  }

  public static void deleteWork(Context context, String id) {
    File workDir = HfxFileUtil.getUserWorkDir(context, id);
    FileUtil.deleteDir(workDir);
  }

  public static void saveCommitInfo(Context context, String workId, String subtype,
      String work_name, String introduce, String icon) {

    File workDir = HfxFileUtil.getUserWorkDir(context, workId);
    File configFile = new File(workDir, workId + ".config");
    CommitInfo info = new CommitInfo();
    info.bookid = workId;
    info.icon = icon;
    info.subtype = subtype;
    info.bookname = work_name;
    info.subtitle = introduce;
    Gson gson = new Gson();
    String config = gson.toJson(info);
    try {
      FileUtil.StringToFile(config, configFile, "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void saveCommitInfo(Context context, CommitInfo info) {
    File workDir = HfxFileUtil.getUserWorkDir(context, info.bookid);
    File configFile = new File(workDir, info.bookid + ".config");
    Gson gson = new Gson();
    String config = gson.toJson(info);
    try {
      FileUtil.StringToFile(config, configFile, "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static CommitInfo getCommitInfo(Context context, String workId) {
    File workDir = HfxFileUtil.getUserWorkDir(context, workId);
    File configFile = new File(workDir, workId + ".config");
    if (configFile.exists()) {
      return Utils.parseJsonFile(configFile, CommitInfo.class);
    } else {
      return null;
    }
  }

  public static int getVideoDuration(File videoFile) {
    int duration = -1;
    try {
      // 取得视频的长度(单位为毫秒)
      MediaMetadataRetriever retriever = new MediaMetadataRetriever();
      retriever.setDataSource(videoFile.getAbsolutePath());
      String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
      duration = Integer.parseInt(time);


    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
    return duration;
  }


  public static void saveVideoInfo(Context context, VideoInfo info) {
    try {
      File jsonFile = HfxFileUtil.getVideoInfoFile(context, info.videoId);
      Gson gson = new Gson();
      String infoString = gson.toJson(info);
      FileUtil.StringToFile(infoString, jsonFile, "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static VideoInfo getVideoInfo(Context context, String id) {
    File file = HfxFileUtil.getVideoInfoFile(context, id);
    if (file.exists() && file.length() > 0) {
      return Utils.parseJsonFile(file, VideoInfo.class);
    } else {
      return null;
    }
  }


  public static void saveAudioInfo(Context context, AudioInfo info) {
    try {
      File jsonFile = HfxFileUtil.getAudioInfoFile(context, info.audioId);
      Gson gson = new Gson();
      String infoString = gson.toJson(info);
      FileUtil.StringToFile(infoString, jsonFile, "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static AudioInfo getAudioInfo(Context context, String id) {
    File file = HfxFileUtil.getAudioInfoFile(context, id);
    if (file.exists() && file.length() > 0) {
      return Utils.parseJsonFile(file, AudioInfo.class);
    } else {
      return null;
    }
  }




}
