package com.namibox.commonlib.event;

/**
 * Created by ryan on 2015/5/20.
 */
public class MessageEvent {

  public String destViewName;
  public String message;
  public String messageName;

  public MessageEvent(String destViewName, String message, String messageName) {
    this.destViewName = destViewName;
    this.message = message;
    this.messageName = messageName;
  }
}
