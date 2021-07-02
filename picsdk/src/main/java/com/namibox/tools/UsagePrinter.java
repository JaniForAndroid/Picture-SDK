package com.namibox.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import com.namibox.util.Logger;
import java.io.RandomAccessFile;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Create time: 19-5-9.
 */
public class UsagePrinter implements Runnable {

  private volatile static UsagePrinter instance = null;
  private ScheduledExecutorService scheduler;
  private Future future;
  private ActivityManager activityManager;
  private long freq;
  private Long lastCpuTime;
  private Long lastAppCpuTime;
  private RandomAccessFile procStatFile;
  private RandomAccessFile appStatFile;

  private UsagePrinter() {
    scheduler = Executors.newSingleThreadScheduledExecutor();
  }

  public static UsagePrinter getInstance() {
    if (instance == null) {
      synchronized (UsagePrinter.class) {
        if (instance == null) {
          instance = new UsagePrinter();
        }
      }
    }
    return instance;
  }

  // freq为采样周期
  public void init(Context context, long freq) {
    activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    this.freq = freq;
  }

  public void start() {
    future = scheduler.scheduleWithFixedDelay(this, 0L, freq, TimeUnit.MILLISECONDS);
  }

  public void stop() {
    future.cancel(false);
  }

  @Override
  public void run() {
    double cpu = sampleCPU();
    double mem = sampleMemory();
    Logger.d("CPU[" + cpu + "%]" + "    Memory[" + mem + "MB]");
  }

  private double sampleCPU() {
    long cpuTime;
    long appTime;
    double sampleValue = 0.0D;
    try {
      if (procStatFile == null || appStatFile == null) {
        procStatFile = new RandomAccessFile("/proc/stat", "r");
        appStatFile = new RandomAccessFile("/proc/" + android.os.Process.myPid() + "/stat", "r");
      } else {
        procStatFile.seek(0L);
        appStatFile.seek(0L);
      }
      String procStatString = procStatFile.readLine();
      String appStatString = appStatFile.readLine();
      String procStats[] = procStatString.split(" ");
      String appStats[] = appStatString.split(" ");
      cpuTime = Long.parseLong(procStats[2]) + Long.parseLong(procStats[3])
          + Long.parseLong(procStats[4]) + Long.parseLong(procStats[5])
          + Long.parseLong(procStats[6]) + Long.parseLong(procStats[7])
          + Long.parseLong(procStats[8]);
      appTime = Long.parseLong(appStats[13]) + Long.parseLong(appStats[14]);
      if (lastCpuTime == null && lastAppCpuTime == null) {
        lastCpuTime = cpuTime;
        lastAppCpuTime = appTime;
        return sampleValue;
      }
      sampleValue = ((double) (appTime - lastAppCpuTime) / (double) (cpuTime - lastCpuTime)) * 100D;
      lastCpuTime = cpuTime;
      lastAppCpuTime = appTime;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return sampleValue;
  }

  private double sampleMemory() {
    double mem = 0.0D;
    try {
      // 统计进程的内存信息 totalPss
      final Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(new int[]{android.os.Process.myPid()});
      if (memInfo.length > 0) {
        // TotalPss = dalvikPss + nativePss + otherPss, in KB
        final int totalPss = memInfo[0].getTotalPss();
        if (totalPss >= 0) {
          // Mem in MB
          mem = totalPss / 1024.0D;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return mem;
  }
}
