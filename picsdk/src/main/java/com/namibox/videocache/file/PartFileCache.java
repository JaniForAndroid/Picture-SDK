package com.namibox.videocache.file;

import com.namibox.videocache.Cache;
import com.namibox.videocache.ProxyCacheException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Create time: 2018/2/10.
 */

public class PartFileCache implements Cache {
  private final DiskUsage diskUsage;
  public File file;
  private RandomAccessFile dataFile;

  public PartFileCache(File file) throws ProxyCacheException {
    this(file, new UnlimitedDiskUsage());
  }

  public PartFileCache(File file, DiskUsage diskUsage) throws ProxyCacheException {
    try {
      if (diskUsage == null) {
        throw new NullPointerException();
      }
      this.diskUsage = diskUsage;
      File directory = file.getParentFile();
      Files.makeDir(directory);
      this.file = file;
      this.dataFile = new RandomAccessFile(this.file, "rw");
    } catch (IOException e) {
      throw new ProxyCacheException("Error using file " + file + " as disc cache", e);
    }
  }

  @Override
  public synchronized long available() throws ProxyCacheException {
    try {
      return (int) dataFile.length();
    } catch (IOException e) {
      try {
        dataFile.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      throw new ProxyCacheException("Error reading length of file " + file, e);
    }
  }

  @Override
  public synchronized int read(byte[] buffer, long offset, int length) throws ProxyCacheException {
    try {
      dataFile.seek(offset);
      return dataFile.read(buffer, 0, length);
    } catch (IOException e) {
      try {
        dataFile.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      String format = "Error reading %d bytes with offset %d from file[%d bytes] to buffer[%d bytes]";
      throw new ProxyCacheException(String.format(format, length, offset, available(), buffer.length), e);
    }
  }

  @Override
  public synchronized void append(byte[] data, int length) throws ProxyCacheException {
    try {
      if (isCompleted()) {
        throw new ProxyCacheException("Error append cache: cache file " + file + " is completed!");
      }
      long available = available();
      dataFile.seek(available);
      //Log.e("tag", "write file: " + available + "+" + length);
      dataFile.write(data, 0, length);
    } catch (IOException e) {
      try {
        dataFile.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      String format = "Error writing %d bytes to %s from buffer with size %d";
      throw new ProxyCacheException(String.format(format, length, dataFile, data.length), e);
    }
  }

  @Override
  public synchronized void close() throws ProxyCacheException {
    try {
      dataFile.close();
      diskUsage.touch(file);
    } catch (IOException e) {
      throw new ProxyCacheException("Error closing file " + file, e);
    }
  }

  @Override
  public synchronized void complete() throws ProxyCacheException {
    if (isCompleted()) {
      return;
    }

    close();
    try {
      dataFile = new RandomAccessFile(file, "r");
      diskUsage.touch(file);
    } catch (IOException e) {
      throw new ProxyCacheException("Error opening " + file + " as disc cache", e);
    }
  }

  @Override
  public synchronized boolean isCompleted() {
    return false;
  }

  /**
   * Returns file to be used fo caching. It may as original file passed in constructor as some temp file for not completed cache.
   *
   * @return file for caching.
   */
  public File getFile() {
    return file;
  }

}
