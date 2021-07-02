package com.namibox.videocache;

import android.content.Context;
import com.namibox.util.Logger;
import java.io.File;
import java.io.IOException;

/**
 * Create time: 2017/1/7.
 */

public class VideoProxy {
  private static int maxFileCount = 50;
  private static long maxCacheSize = 20 * 1024 * 1024 * 1024L;
  private static boolean isFlies;//是否使用cache同级的files目录
  private static HttpProxyCacheServer httpProxyCacheServer;
  private static HttpProxyCacheServer shortVideoCacheServer;

  public static void init(int maxFileCount, long maxCacheSize) {
    VideoProxy.maxFileCount = maxFileCount;
    VideoProxy.maxCacheSize = maxCacheSize;
  }
  public static void init(int maxFileCount, long maxCacheSize,boolean isFiles) {
    VideoProxy.maxFileCount = maxFileCount;
    VideoProxy.maxCacheSize = maxCacheSize;
    VideoProxy.isFlies = isFiles;
  }

  public static HttpProxyCacheServer getProxy(Context context) {
    if (httpProxyCacheServer == null) {
      httpProxyCacheServer = new HttpProxyCacheServer.Builder(context.getApplicationContext())
          .cacheDirectory(getVideoCacheDir(context))
          .maxCacheFilesCount(maxFileCount)
          .maxCacheSize(maxCacheSize)
          .build();
    }
    return httpProxyCacheServer;
  }

  public static HttpProxyCacheServer getShortVideoProxy(Context context) {
    if (shortVideoCacheServer == null) {
      shortVideoCacheServer = new HttpProxyCacheServer.Builder(context.getApplicationContext(),true)
          .cacheDirectory(getShortVideoCacheDir(context))
          .maxCacheFilesCount(maxFileCount)
          .build();
    }
    return shortVideoCacheServer;
  }

  public static File getVideoCacheDir(Context context) {
    File cacheFile;
    if (isFlies) {
      cacheFile = new File(context.getExternalFilesDir(null) == null ? context.getFilesDir() : context.getExternalFilesDir(null),
          "video-cache");
    }else{
      cacheFile = new File(context.getExternalCacheDir() == null ? context.getCacheDir() : context.getExternalCacheDir(),
          "video-cache");
    }
    return cacheFile;
  }

  public static File getShortVideoCacheDir(Context context) {
    File cacheFile = new File(context.getExternalCacheDir() == null ? context.getCacheDir() : context.getExternalCacheDir(),
        "short_video-cache");
    return cacheFile;
  }

  public static void cleanVideoCacheDir(Context context) {
    File videoCacheDir = VideoProxy.getVideoCacheDir(context);
    try {
      Logger.d("cleanVideoCacheDir: " + videoCacheDir);
      cleanDirectory(videoCacheDir);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void cleanShortVideoCacheDir(Context context) {
    File videoCacheDir = VideoProxy.getShortVideoCacheDir(context);
    try {
      Logger.d("cleanShortVideoCacheDir: " + videoCacheDir);
      cleanDirectory(videoCacheDir);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void cleanDirectory(File file) throws IOException {
    if (!file.exists()) {
      return;
    }
    File[] contentFiles = file.listFiles();
    if (contentFiles != null) {
      for (File contentFile : contentFiles) {
        delete(contentFile);
      }
    }
  }

  private static void delete(File file) throws IOException {
    if (file.isFile() && file.exists()) {
      deleteOrThrow(file);
    } else {
      cleanDirectory(file);
      deleteOrThrow(file);
    }
  }

  private static void deleteOrThrow(File file) throws IOException {
    if (file.exists()) {
      boolean isDeleted = file.delete();
      if (!isDeleted) {
        throw new IOException(String.format("File %s can't be deleted", file.getAbsolutePath()));
      }
    }
  }

  public static void shutdown() {
    Logger.d("shutdown: " + httpProxyCacheServer);
    if (httpProxyCacheServer != null) {
      httpProxyCacheServer.shutdown();
      httpProxyCacheServer = null;
    }
  }

  public static void shutDownShortVideo(){
    if (shortVideoCacheServer != null) {
      shortVideoCacheServer.shutdown();
      shortVideoCacheServer = null;
    }
  }
}
