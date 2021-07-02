package com.namibox.tools;

import static com.orhanobut.logger.Logger.ASSERT;
import static com.orhanobut.logger.Logger.DEBUG;
import static com.orhanobut.logger.Logger.ERROR;
import static com.orhanobut.logger.Logger.INFO;
import static com.orhanobut.logger.Logger.VERBOSE;
import static com.orhanobut.logger.Logger.WARN;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.orhanobut.logger.DiskLogStrategy;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogStrategy;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Create time: 18-12-3.
 */
public class LoggerFormatStrategy implements FormatStrategy {

  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String NEW_LINE_REPLACEMENT = " <br> ";
  private static final String SEPARATOR = ",";

  @NonNull
  private final Date date;
  @NonNull private final SimpleDateFormat dateFormat;
  @NonNull private final LogStrategy logStrategy;
  @Nullable
  private final String tag;

  private LoggerFormatStrategy(@NonNull LoggerFormatStrategy.Builder builder) {

    date = builder.date;
    dateFormat = builder.dateFormat;
    logStrategy = builder.logStrategy;
    tag = builder.tag;
  }

  @NonNull public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public void log(int priority, @Nullable String onceOnlyTag, @NonNull String message) {

    String tag = formatTag(onceOnlyTag);

    date.setTime(System.currentTimeMillis());

    StringBuilder builder = new StringBuilder();

    builder.append(dateFormat.format(date));

    // level
    builder.append(SEPARATOR);
    builder.append(logLevel(priority));

    // tag
    builder.append(SEPARATOR);
    builder.append(tag);

    builder.append(SEPARATOR);
    builder.append(message);

    // new line
    builder.append(NEW_LINE);

    logStrategy.log(priority, tag, builder.toString());
  }

  private static String logLevel(int value) {
    switch (value) {
      case VERBOSE:
        return "<V>";
      case DEBUG:
        return "<D>";
      case INFO:
        return "<I>";
      case WARN:
        return "<W>";
      case ERROR:
        return "<E>";
      case ASSERT:
        return "<A>";
      default:
        return "<U>";
    }
  }

  @Nullable private String formatTag(@Nullable String tag) {
    if (!TextUtils.isEmpty(tag) && !TextUtils.equals(this.tag, tag)) {
      return this.tag + "-" + tag;
    }
    return this.tag;
  }

  public static final class Builder {
    private static final int MAX_BYTES = 500 * 1024; // 500K averages to a 4000 lines per file

    Date date;
    File folder;
    SimpleDateFormat dateFormat;
    LogStrategy logStrategy;
    String tag = "PRETTY_LOGGER";

    private Builder() {
    }

    @NonNull public Builder date(@Nullable Date val) {
      date = val;
      return this;
    }

    @NonNull public Builder dateFormat(@Nullable SimpleDateFormat val) {
      dateFormat = val;
      return this;
    }

    @NonNull public Builder logStrategy(@Nullable LogStrategy val) {
      logStrategy = val;
      return this;
    }

    @NonNull public Builder tag(@Nullable String tag) {
      this.tag = tag;
      return this;
    }

    @NonNull public Builder folder(@Nullable File dir) {
      folder = dir;
      return this;
    }

    @NonNull public LoggerFormatStrategy build() {
      if (date == null) {
        date = new Date();
      }
      if (dateFormat == null) {
        dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK);
      }
      if (logStrategy == null) {
        HandlerThread ht = new HandlerThread("Namibox.FileLogger");
        ht.start();
        Handler handler = new WriteHandler(ht.getLooper(), folder);
        logStrategy = new DiskLogStrategy(handler);
      }
      return new LoggerFormatStrategy(this);
    }
  }

  static class WriteHandler extends Handler {

    File folder;
    Date date;
    SimpleDateFormat format;

    WriteHandler(@NonNull Looper looper, File dir) {
      super(looper);
      folder = dir;
      date = new Date();
      format = new SimpleDateFormat("yyyyMMdd", Locale.US);
      if (folder.exists()) {
        File[] logs = folder.listFiles();
        if (logs != null) {
          for (File log : logs) {
            //超过7天的log删除
            if (log.lastModified() < System.currentTimeMillis() - 108 * 3600_000L) {
              log.delete();
            }
          }
        }
      }
    }

    @SuppressWarnings("checkstyle:emptyblock")
    @Override
    public void handleMessage(@NonNull Message msg) {
      String content = (String) msg.obj;

      FileWriter fileWriter = null;

      try {
        File logFile = getLogFile();
        fileWriter = new FileWriter(logFile, true);

        writeLog(fileWriter, content);

        fileWriter.flush();
        fileWriter.close();
      } catch (Exception e) {
        e.printStackTrace();
        if (fileWriter != null) {
          try {
            fileWriter.flush();
            fileWriter.close();
          } catch (IOException e1) { /* fail silently */ }
        }
      }
    }

    /**
     * This is always called on a single background thread.
     * Implementing classes must ONLY write to the fileWriter and nothing more.
     * The abstract class takes care of everything else including close the stream and catching IOException
     *
     * @param fileWriter an instance of FileWriter already initialised to the correct file
     */
    private void writeLog(@NonNull FileWriter fileWriter, @NonNull String content) throws IOException {
      fileWriter.append(content);
    }

    private File getLogFile() {

      if (!folder.exists()) {
        //TODO: What if folder is not created, what happens then?
        if (!folder.mkdirs()) {
          throw new IllegalStateException("folder create fail:" + folder);
        }
      }
      date.setTime(System.currentTimeMillis());
      String dateStr = format.format(date);
      return new File(folder, String.format(Locale.US, "log_%s.txt", dateStr));
    }
  }
}
