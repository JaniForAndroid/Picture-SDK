package com.namibox.voice_engine_interface;

import android.content.Context;
import com.namibox.util.FileUtil;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Create time: 2020/4/17.
 */
public class EngineUtils {

  public static long calTextLength(String text) {
    String[] words = text.trim().split("\\s+");
    //return words.length * 600 + 2000;
    float y = (float) (words.length * 0.5 + 2);
    return (int) Math.ceil(y) * 1000;
  }

  public static long calCnTextLength(String text) {
    if (text == null) {
      return 0;
    }
    return text.length() * 1000;
  }

  public static String initWavPath(Context context) {
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
    return new File(FileUtil.getFileCacheDir(context), "record_" + timeStamp + ".wav")
        .getAbsolutePath();
  }

}
