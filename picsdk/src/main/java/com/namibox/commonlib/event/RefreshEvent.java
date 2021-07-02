package com.namibox.commonlib.event;

/**
 * Created by ryan on 2015/5/20.
 */
public class RefreshEvent {

  public String destViewName;
  public String url;
  public int hidden;

  public RefreshEvent(String destViewName, String url, int hidden) {
    this.destViewName = destViewName;
    this.url = url;
    this.hidden = hidden;
  }
}
