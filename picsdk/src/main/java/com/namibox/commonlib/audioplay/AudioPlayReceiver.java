package com.namibox.commonlib.audioplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.namibox.commonlib.lockscreen.AudioPlayEvent;
import org.greenrobot.eventbus.EventBus;

public class AudioPlayReceiver extends BroadcastReceiver {

  public static final String PREVIOUS = "previous";
  public static final String NEXT = "next";
  public static final String PLAY_PAUSE = "play_pause";
  public static final String CLEAR = "clear";

  @Override
  public void onReceive(Context context, Intent intent) {
    String audioCode = intent.getAction();
    switch (audioCode) {
      case PREVIOUS:
        EventBus.getDefault().post(new AudioPlayEvent(AudioPlayEvent.PREVIOUS, 0));
        break;
      case PLAY_PAUSE:
        EventBus.getDefault().post(new AudioPlayEvent(AudioPlayEvent.PLAY_PAUSE, 0));
        break;
      case NEXT:
        EventBus.getDefault().post(new AudioPlayEvent(AudioPlayEvent.NEXT, 0));
        break;
      case CLEAR:
        EventBus.getDefault().post(new AudioPlayEvent(AudioPlayEvent.CLEAR, 0));
        break;
      default:
        break;
    }
  }
}
