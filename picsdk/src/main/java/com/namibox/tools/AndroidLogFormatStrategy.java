package com.namibox.tools;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogStrategy;
import com.orhanobut.logger.LogcatLogStrategy;

/**
 * Create time: 18-12-7.
 */
public class AndroidLogFormatStrategy implements FormatStrategy {

  @NonNull
  private final LogStrategy logStrategy;
  @Nullable
  private final String tag;

  private AndroidLogFormatStrategy(@NonNull Builder builder) {

    logStrategy = builder.logStrategy;
    tag = builder.tag;
  }

  @NonNull public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public void log(int priority, @Nullable String onceOnlyTag, @NonNull String message) {

    String tag = formatTag(onceOnlyTag);
    String content = getContent(message);

    logChunk(priority, tag, content);
  }

  private void logChunk(int priority, @Nullable String tag, @NonNull String chunk) {
    logStrategy.log(priority, tag, chunk);
  }

  private static String getContent(String content) {
    StackTraceElement targetStackTraceElement = getTargetStackTraceElement();
    if (targetStackTraceElement == null) {
      return content;
    }
    return "(" + targetStackTraceElement.getFileName() + ":"
        + targetStackTraceElement.getLineNumber() + ") " + content;
  }

  private static StackTraceElement getTargetStackTraceElement() {
    // find the target invoked method
    StackTraceElement targetStackTrace = null;
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    boolean foundLogClass = false;
    for (StackTraceElement stackTraceElement : stackTrace) {
      String fileName = stackTraceElement.getFileName();
      //String methodName = stackTraceElement.getMethodName();
      boolean isLogClass = "Logger.java".equals(fileName) || "LoggerUtil.java".equals(fileName);
      if (!foundLogClass && isLogClass) {
        foundLogClass = true;
      }
      if (foundLogClass && !isLogClass) {
        targetStackTrace = stackTraceElement;
        break;
      }
    }
    return targetStackTrace;
  }

  @Nullable private String formatTag(@Nullable String tag) {
    if (!TextUtils.isEmpty(tag) && !TextUtils.equals(this.tag, tag)) {
      return this.tag + "-" + tag;
    }
    return this.tag;
  }

  public static class Builder {
    LogStrategy logStrategy;
    String tag = "PRETTY_LOGGER";

    private Builder() {
    }


    @NonNull public Builder logStrategy(@Nullable LogStrategy val) {
      logStrategy = val;
      return this;
    }

    @NonNull public Builder tag(@Nullable String tag) {
      this.tag = tag;
      return this;
    }

    @NonNull public AndroidLogFormatStrategy build() {
      if (logStrategy == null) {
        logStrategy = new LogcatLogStrategy();
      }
      return new AndroidLogFormatStrategy(this);
    }
  }

}
