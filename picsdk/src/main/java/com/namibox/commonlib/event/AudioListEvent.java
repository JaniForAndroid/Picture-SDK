package com.namibox.commonlib.event;

import com.google.gson.JsonElement;

/**
 * @Description
 * @CreateTime: 2020/1/4 15:28
 * @Author: zhangkx
 */
public class AudioListEvent {

  public JsonElement jsonElement;

  public AudioListEvent(JsonElement jsonElement) {
    this.jsonElement = jsonElement;
  }
}
