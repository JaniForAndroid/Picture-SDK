package com.namibox.imageselector;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.namibox.commonlib.activity.AbsFunctionActivity;
import com.namibox.commonlib.constant.Events;
import com.namibox.tools.ThinkingAnalyticsHelper;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Shelter
 * Create time: 2020/6/8, 14:16.
 */
public abstract class BaseBuryPointActivity extends AbsFunctionActivity {
  protected String channel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getChannel();
    onPageEnter();
  }

  protected void getChannel() {
    Intent intent = getIntent();
    int type = intent.getIntExtra("type", 0);
    if (type == 2) {
      channel = "头像";
    } else {
      channel = "其他";
    }
  }

  /**
   * 编辑页面、拍照页面
   * @return page name
   */
  protected abstract String getPage();


  protected void onButtonClick(String buttonName) {
    Map<String, Object> map = new HashMap<>();
    map.put("page", getPage());
    map.put("button", buttonName);
    map.put("channel", channel);
    ThinkingAnalyticsHelper.trackEvent(Events.TA_EVENT_NB_APP_CLICK, map);
  }

  protected void onPageEnter() {
    Map<String, Object> map = new HashMap<>();
    map.put("page", getPage());
    map.put("channel", channel);
    ThinkingAnalyticsHelper.trackEvent(Events.TA_EVENT_NB_APP_VIEW_ENTER, map);
  }
}
