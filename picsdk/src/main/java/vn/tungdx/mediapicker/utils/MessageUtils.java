package vn.tungdx.mediapicker.utils;

import android.content.Context;
import com.example.picsdk.R;
import vn.tungdx.mediapicker.MediaOptions;

/**
 * @author TUNGDX
 */

/**
 * Get warning, error message for media picker module.
 */
public class MessageUtils {

  /**
   * @param maxDuration in seconds.
   * @return message before record video.
   */
  public static String getWarningMessageVideoDuration(Context context,
      int maxDuration) {
    return context.getResources().getQuantityString(
        R.plurals.picker_video_duration_warning, maxDuration, maxDuration);
  }

  public static String getWarningMessageMinVideoDuration(Context context,
      int maxDuration) {
    return context.getResources().getQuantityString(
        R.plurals.warning_video_duration_min, maxDuration, maxDuration);
  }

  /**
   * @return message when record and select video that has duration larger than max options. {@link
   * MediaOptions.Builder#setMaxVideoDuration(int)}
   */
  public static String getInvalidMessageMaxVideoDuration(Context context,
      int maxDuration) {
    return context.getResources().getQuantityString(
        R.plurals.picker_video_duration_max, maxDuration, maxDuration);
  }

  /**
   * @return message when record and select video that has duration smaller than min options. {@link
   * MediaOptions.Builder#setMinVideoDuration(int)}
   */
  public static String getInvalidMessageMinVideoDuration(Context context,
      int minDuration) {
    return context.getResources().getQuantityString(
        R.plurals.picker_video_duration_min, minDuration, minDuration);
  }
}