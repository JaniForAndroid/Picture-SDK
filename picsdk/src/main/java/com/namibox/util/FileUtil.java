package com.namibox.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.lingala.zip4j.core.ZipFile;


/**
 * Created by sunha on 2017/5/31 0031.
 */

public class FileUtil {

  private static final String FILE_CACHE_DIR = "file_cache";
  private static final String STATIC_HTML_DIR_DEV = "static_html_dev";
  private static final String STATIC_HTML_DIR = "static_html";
  private static final String APP_CACHE = "app_cache";
  private static final String APP_PAGE = "app_page";
  private static final String LOCAL_APP_PAGE = "local_app_page";
  private static final String NATIVE_APP_PAGE = "native_app_page";
  private static final String LOCAL_APP_CACHE = "local_app_cache";
  private static final String TAG = "FileUtil";
  private static final long SEVEN_DAYS_MILLIS = 7 * 24 * 60 * 60 * 1000;
  public static final String BASE_DIR = "namibox";
  public static final String LOG_FILE = "picture_log";

  /**
   * 私有sd卡基础目录
   * @param context
   * @return
   */
  public static File getBaseFile(Context context){
    return new File(context.getExternalFilesDir(null).getParentFile(),BASE_DIR);
  }

  /**
   * 日志存放目录
   * @param context
   * @return
   */
  public static File getLogFile(Context context){
    String dir = PreferenceUtil.getLogDirString(context);
    return new File(getBaseFile(context), dir);
  }

  public static void cleanDirectory(File file) throws IOException {
    if (file == null || !file.exists()) {
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

  public static boolean deleteEvalDir(File dir, boolean isEval) {
    if (dir.isDirectory()) {
      File[] files = dir.listFiles();
      if (files != null) {
        for (File file : files) {
          if (isEval != file.getName().startsWith("eval")) {
            continue;
          }
          boolean success = deleteDir(file);
          Log.d("delete", success + "---" + "path = " + file.getAbsolutePath());
          if (!success) {
            return false;
          }
        }
      }
    }

    // The directory is now empty so delete it
    return dir.delete();
  }

  public static boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
      File[] files = dir.listFiles();
      if (files != null) {
        for (File file : files) {
          boolean success = deleteDir(file);
          if (!success) {
            return false;
          }
        }
      }
    }

    // The directory is now empty so delete it
    return dir.delete();
  }


  public static void unzip(File zipFile, File targetDirectory, boolean md5Name) throws Exception {
    java.util.zip.ZipFile archive = new java.util.zip.ZipFile(zipFile);
    Enumeration<? extends ZipEntry> e = archive.entries();
    while (e.hasMoreElements()) {
      ZipEntry entry = e.nextElement();
      File file = new File(targetDirectory, entry.getName());
      if (entry.isDirectory()) {
        file.mkdirs();
      } else {
        if (!file.getParentFile().exists()) {
          file.getParentFile().mkdirs();
        }
        File newFile = md5Name ? new File(file.getParentFile(), MD5Util.md5(file.getName())) : file;
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
          in = archive.getInputStream(entry);
          out = new BufferedOutputStream(new FileOutputStream(newFile));
          inputStreamToOutputStream(in, out);
        } catch (Exception ex) {
          ex.printStackTrace();
        } finally {
          if (in != null) {
            try {
              in.close();
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
          if (out != null) {
            try {
              out.close();
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        }
      }
    }
  }

  public static void unzipAllFile(File zip, String dir) throws Exception {
    Log.d(TAG, "unzip: " + zip + " -> " + dir);
    ZipFile zipFile = new ZipFile(zip);
    zipFile.setFileNameCharset("utf-8");
    zipFile.extractAll(dir);
  }

  public static void compress(File srcFile, File dstFile) throws IOException {
    if (!srcFile.exists()) {
      throw new FileNotFoundException(srcFile + "不存在！");
    }

    FileOutputStream out = null;
    ZipOutputStream zipOut = null;
    try {
      out = new FileOutputStream(dstFile);
      CheckedOutputStream cos = new CheckedOutputStream(out, new CRC32());
      zipOut = new ZipOutputStream(cos);
      String baseDir = "";
      compress(srcFile, zipOut, baseDir);
    } finally {
      if (null != zipOut) {
        zipOut.close();
        out = null;
      }

      if (null != out) {
        out.close();
      }
    }
  }

  private static void compress(File file, ZipOutputStream zipOut, String baseDir)
      throws IOException {
    if (file.isDirectory()) {
      compressDirectory(file, zipOut, baseDir);
    } else {
      compressFile(file, zipOut, baseDir);
    }
  }

  /**
   * 压缩一个目录
   */
  private static void compressDirectory(File dir, ZipOutputStream zipOut, String baseDir)
      throws IOException {
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      compress(files[i], zipOut, baseDir + dir.getName() + "/");
    }
  }

  /**
   * 压缩一个文件
   */
  private static void compressFile(File file, ZipOutputStream zipOut, String baseDir)
      throws IOException {
    if (!file.exists()) {
      return;
    }

    BufferedInputStream bis = null;
    try {
      bis = new BufferedInputStream(new FileInputStream(file));
      ZipEntry entry = new ZipEntry(baseDir + file.getName());
      zipOut.putNextEntry(entry);
      int count;
      byte data[] = new byte[8192];
      while ((count = bis.read(data, 0, 8192)) != -1) {
        zipOut.write(data, 0, count);
      }

    } finally {
      if (null != bis) {
        bis.close();
      }
    }
  }

  public static boolean renameFile(File oldFile, File newFile) {
    if (oldFile == null || !oldFile.exists()) {
      Log.e(TAG, "rename fail, file not exists: " + oldFile);
      return false;
    }
    if (newFile == null) {
      Log.e(TAG, "rename fail, new file is null");
      return false;
    }
    if (oldFile.equals(newFile)) {
      Log.e(TAG, "rename fail, same file needn't rename: " + oldFile);
      return false;
    }
    if (newFile.exists() && !newFile.delete()) {
      Log.e(TAG, "rename fail, new file exists and can't delete: " + newFile);
      return false;
    }
    return oldFile.renameTo(newFile);
  }


  public static void inputStreamToFile(InputStream is, File file) throws IOException {
    if (!file.exists()) {
      file.createNewFile();
    }
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    inputStreamToOutputStream(is, fileOutputStream);
    fileOutputStream.close();
  }

  public static File getFileCacheDir(Context c) {
    File f = new File(c.getApplicationContext().getFilesDir(), FILE_CACHE_DIR);
    if (!f.exists()) {
      if (!f.mkdirs()) {
        Log.e(TAG, "can't create dir: " + f.getAbsolutePath());
        return c.getApplicationContext().getCacheDir();
      }
    }
    return f;
  }

  public static File getCachedFile(Context context, String url) {
    return new File(getFileCacheDir(context), MD5Util.md5(url));
  }

  public static boolean isFileExpired(File file) {
    long current = System.currentTimeMillis();
    long lastModify = file.lastModified();
    return current - lastModify > SEVEN_DAYS_MILLIS;
  }

  public static boolean isFileExpired(File file, long duration) {
    long current = System.currentTimeMillis();
    long lastModify = file.lastModified();
    return current - lastModify > duration;
  }

  public static long getFreeSize(File path) {
    StatFs sf = new StatFs(path.getPath());
    if (Build.VERSION.SDK_INT >= 18) {
      long blockSize = sf.getBlockSizeLong();
      long freeBlocks = sf.getAvailableBlocksLong();
      return freeBlocks * blockSize;  //单位Byte
    } else {
      long blockSize = sf.getBlockSize();
      long freeBlocks = sf.getAvailableBlocks();
      return freeBlocks * blockSize;  //单位Byte
    }
  }

  public static long getDirSize(File dir) {
    if (dir == null) {
      return 0;
    }
    if (!dir.isDirectory()) {
      return 0;
    }
    long dirSize = 0;
    File[] files = dir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile()) {
          dirSize += file.length();
        } else if (file.isDirectory()) {
          //dirSize += file.length();
          dirSize += getDirSize(file); // 如果遇到目录则通过递归调用继续统计
        }
      }
    }
    return dirSize;
  }

  public static File getFileByUrl(File parent, String url) {
    return getFileByUrl(parent.getAbsolutePath(), url);
  }

  public static File getFileByUrl(String parent, String url) {
    return new File(parent, MD5Util.md5(url));
//    URL u = null;
//    try {
//      u = new URL(url);
//      String host = u.getHost();
//      String path = URLDecoder.decode(u.getPath(), "UTF-8");
//      File dir = new File(parent, host);
//      return new File(dir, path);
//    } catch (MalformedURLException e) {
//      e.printStackTrace();
//    } catch (UnsupportedEncodingException e) {
//      e.printStackTrace();
//    }
//    return null;
  }

  /**
   * copy files and directory in srcDir to dstDir
   */
  public static void copyFiles(File src, File dst) throws Exception {
    if (!src.isDirectory()) {
      throw new IllegalStateException("src isn't a directory");
    }
    if (!dst.exists() && !dst.mkdirs()) {
      throw new IllegalStateException("can't create dst dir");
    }

    File[] files = src.listFiles();
    if (files == null) {
      Log.e(TAG, "no file to move");
      return;
    }

    for (File file : files) {
      if (file.isDirectory()) {
        File dstDir = new File(dst, file.getName());
        copyFiles(file, dstDir);
      } else {
        copyFile(file, dst, null);
      }
    }
  }

  public static void copyFile(File file, File dstDir, String name) throws Exception {
    copyFile(file, dstDir, name, true);
  }

  public static void copyFile(File file, File dstDir, String name, boolean overWrite)
      throws Exception {
    if (!file.exists()) {
      return;
    }
    File dst = new File(dstDir, name == null ? file.getName() : name);
    if (dst.exists() && !overWrite) {
      Log.w(TAG, "needn't copy, file exists: " + dst);
      return;
    }
    if (!dst.exists() && !dst.createNewFile()) {
      return;
    }
    FileInputStream input = new FileInputStream(file);
    FileOutputStream output = new FileOutputStream(dst);
    byte[] b = new byte[1024 * 2];
    int len;
    while ((len = input.read(b)) != -1) {
      output.write(b, 0, len);
    }
    output.flush();
    output.close();
    input.close();
  }

  //todo find a better way
  public static boolean canWrite(File dir) {
    if (!dir.exists() || !dir.canWrite()) {
      return false;
    }
    File temp = new File(dir, "namibox_test");
    try {
      if (temp.exists() && !temp.delete()) {
        return false;
      }
      return temp.createNewFile() && temp.delete();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public static void inputStreamToOutputStream(InputStream is, OutputStream os) throws IOException {
    byte[] buffer = new byte[8 * 1024];
    int count;
    while ((count = is.read(buffer)) > 0) {
      os.write(buffer, 0, count);
    }
  }


  public static String inputStreamToString(InputStream in, String encoding) throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    byte[] data = new byte[8 * 1024];
    int count;
    while ((count = in.read(data, 0, data.length)) != -1) {
      outStream.write(data, 0, count);
    }
    String s = new String(outStream.toByteArray(), encoding);
    outStream.close();
    return s;
  }

  public static String FileToString(File file, String encoding) throws IOException {
    InputStream is = new FileInputStream(file);
    return inputStreamToString(is, encoding);
  }

  public static void StringToFile(String string, File file, String encoding) throws IOException {
    ByteArrayInputStream stream = new ByteArrayInputStream(string.getBytes(encoding));
    inputStreamToFile(stream, file);
    stream.close();
  }

  public static void bytesToFile(byte[] bytes, File file) throws IOException {
    ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
    inputStreamToFile(stream, file);
    stream.close();
  }

  public static byte[] FiletoByteArray(File f) {
    if (!f.exists()) {
      return null;
    }

    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
      int buf_size = 1024;
      byte[] buffer = new byte[buf_size];
      int len = 0;
      while (-1 != (len = in.read(buffer, 0, buf_size))) {
        bos.write(buffer, 0, len);
      }
      byte[] bytes = bos.toByteArray();
      in.close();
      bos.close();
      return bytes;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static byte[] InputStreamToByte(InputStream in) throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    byte[] data = new byte[4096];
    int count;
    while ((count = in.read(data, 0, data.length)) != -1) {
      outStream.write(data, 0, count);
    }
    return outStream.toByteArray();

  }

  public static File getStaticHtmlDir(Context c) {
    File f = new File(c.getFilesDir(), Utils.isDev(c) ? STATIC_HTML_DIR_DEV : STATIC_HTML_DIR);
    if (!f.exists()) {
      if (!f.mkdirs()) {
        Log.e(TAG, "can't create dir: " + f.getAbsolutePath());
      }
    }
    return f;
  }

  public static File getLocalAppCacheDir(Context c) {
    File f = new File(getStaticHtmlDir(c), LOCAL_APP_CACHE);
    if (!f.exists()) {
      if (!f.mkdirs()) {
        Log.e(TAG, "can't create dir: " + f.getAbsolutePath());
      }
    }
    return f;
  }

  public static File getLocalAppPageDir(Context c) {
    File f = new File(getStaticHtmlDir(c), LOCAL_APP_PAGE);
    if (!f.exists()) {
      if (!f.mkdirs()) {
        Log.e(TAG, "can't create dir: " + f.getAbsolutePath());
      }
    }
    return f;
  }

  public static File getStaticAppCacheDir(Context c) {
    File f = new File(getStaticHtmlDir(c), APP_CACHE);
    if (!f.exists()) {
      if (!f.mkdirs()) {
        Log.e(TAG, "can't create dir: " + f.getAbsolutePath());
      }
    }
    return f;
  }

  public static File getStaticAppPageDir(Context c) {
    File f = new File(FileUtil.getStaticHtmlDir(c), APP_PAGE);
    if (!f.exists()) {
      if (!f.mkdirs()) {
        Log.e(TAG, "can't create dir: " + f.getAbsolutePath());
      }
    }
    return f;
  }

  public static String getPage(File dir, String url) {
    if (TextUtils.isEmpty(url)) {
      return null;
    }
    String httpUrl = url.startsWith("https") ? url.replaceFirst("https", "http") : "";
    try {
      File[] pages = dir.listFiles();
      for (File page : pages) {
        if (page.isDirectory()) {
          File ini = new File(page, "ver.ini");
          if (ini.exists()) {
            IniReader iniReader = new IniReader(ini.getAbsolutePath());
            String u = iniReader.get("SYSTEM", "URL").get(0);
            if (url.equals(u) || httpUrl.equals(u)) {
              return page.getName();
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


  public static class StorageInfo {

    public String path;
    public String state;
    public boolean isRemovable;
    public boolean isPrimary;

    StorageInfo(String path) {
      this.path = path;
    }

    boolean isMounted() {
      return "mounted".equals(state);
    }
  }

  public static String getStorageState(File path) {
    if (Build.VERSION.SDK_INT >= 19) {
      return Environment.getStorageState(path);
    }

    try {
      final String canonicalPath = path.getCanonicalPath();
      final String canonicalExternal = Environment.getExternalStorageDirectory()
          .getCanonicalPath();

      if (canonicalPath.startsWith(canonicalExternal)) {
        return Environment.getExternalStorageState();
      }
    } catch (IOException e) {
      Log.w(TAG, "Failed to resolve canonical path: " + e);
    }

    return "unknown";
  }

  public static void setStorageDir(Context context) {
    String selected = PreferenceUtil.getSelectedStorage(context);
    //call this to create dirs
    if (Build.VERSION.SDK_INT >= 19) {
      try {
        context.getExternalFilesDirs(null);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (TextUtils.isEmpty(selected)) {
      Log.d(TAG, "storage not set yet");
      ArrayList<StorageInfo> storageInfos = listAvailableStorage(context);
      if (storageInfos != null) {
        String maxFreePath = null;
        long maxFree = 0;
        for (StorageInfo storageInfo : storageInfos) {
          File file = new File(storageInfo.path);
          String state = getStorageState(file);
          if ((state.equals("unknown") || state.equals(Environment.MEDIA_MOUNTED))
              && file.canWrite()) {
            String p;
            if (canWrite(file)) {
              p = storageInfo.path;
            } else {
              p = storageInfo.path + getExtDataDir(context);
            }
            long free = file.getFreeSpace();
            if (free > maxFree) {
              maxFreePath = p;
              maxFree = free;
            }
          }
        }
        if (!TextUtils.isEmpty(maxFreePath)) {
          Log.d(TAG, "set storage to: " + maxFreePath);
          PreferenceUtil.setSelectedStorage(context, maxFreePath);
        } else {
          String internal = Environment.getExternalStorageDirectory().getAbsolutePath();
          PreferenceUtil.setSelectedStorage(context, internal);
          Log.d(TAG, "set default storage: " + internal);
        }
      }
    } else {
      File dir = new File(selected);
      if (!canWrite(dir)) {
        String internal = Environment.getExternalStorageDirectory().getAbsolutePath();
        PreferenceUtil.setSelectedStorage(context, internal);
        Log.w(TAG, selected + " can't access, set to default: " + internal);
      } else {
        Log.d(TAG, "storage: " + selected);
      }
    }
  }

  public static ArrayList<StorageInfo> listAvailableStorage(Context context) {
    ArrayList<StorageInfo> storages = new ArrayList<>();
    StorageManager storageManager = (StorageManager) context
        .getSystemService(Context.STORAGE_SERVICE);
    try {
      Class<?>[] paramClasses = {};
      Method getVolumeList = StorageManager.class.getMethod("getVolumeList", paramClasses);
      getVolumeList.setAccessible(true);
      Object[] params = {};
      Object[] invokes = (Object[]) getVolumeList.invoke(storageManager, params);
      if (invokes != null) {
        StorageInfo info;
        for (Object obj : invokes) {
          Method getPath = obj.getClass().getMethod("getPath", new Class[0]);
          String path = (String) getPath.invoke(obj, new Object[0]);
          File file = new File(path);
          if (file.exists() && file.isDirectory() && file.canWrite()) {
            info = new StorageInfo(path);
          } else {
            file = new File(path + getExtDataDir(context));
            if (file.exists() && file.isDirectory() && file.canWrite()) {
              info = new StorageInfo(path + getExtDataDir(context));
            } else {
              continue;
            }
          }
          Method isRemovable = obj.getClass().getMethod("isRemovable", new Class[0]);
          Method isPrimary = obj.getClass().getMethod("isPrimary", new Class[0]);
          String state;
          try {
            Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
            state = (String) getVolumeState.invoke(storageManager, info.path);
            info.state = state;
          } catch (Exception e) {
            e.printStackTrace();
          }

          if (info.isMounted()) {
            info.isRemovable = ((Boolean) isRemovable.invoke(obj, new Object[0])).booleanValue();
            info.isPrimary = ((Boolean) isPrimary.invoke(obj, new Object[0])).booleanValue();
            storages.add(info);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    storages.trimToSize();

    return storages;
  }

  public static String getExtDataDir(Context context) {
    return "/Android/data/" + context.getPackageName() + "/files";
  }

  /**
   * 将设备状态更新为老设备
   */
  public static void setDeviceOld() {
    saveDeviceFile("device:old", "deviceState.txt");
  }

  /**
   * 获取设备状态更新为老设备
   */
  public static boolean isNewDevice() {
    String content = getDeviceFile("deviceState.txt");
    return !"device:old".equals(content);
  }

  public static void deleteFile(String fileName) {
    try {
      //获取保存的设备状态文件
      File file = new File(Environment.getExternalStorageDirectory(), fileName);
      if (file.exists()) {
        file.delete();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void saveDeviceFile(String content, String fileName) {
    try {
      File file = new File(Environment.getExternalStorageDirectory(), fileName);
      //if (file.exists()) {
      //  return;
      //}
      RandomAccessFile raf = new RandomAccessFile(file, "rwd");
      //raf.seek(file.length());
      raf.write(content.getBytes());
      raf.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void deleteDeviceFile(String fileName) {
    File file = new File(Environment.getExternalStorageDirectory(), fileName);
    //noinspection ResultOfMethodCallIgnored
    file.delete();
  }

  public static String getDeviceFile(String fileName) {
    try {
      //获取保存的设备状态文件
      File file = new File(Environment.getExternalStorageDirectory(), fileName);
      if (file.exists()) {
        return FileToString(file, "utf-8");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
