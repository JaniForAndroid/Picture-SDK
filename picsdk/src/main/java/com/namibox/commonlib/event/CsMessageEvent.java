package com.namibox.commonlib.event;

/**
 * Created by sunhapper on 2016/10/13 0013.
 */

public class CsMessageEvent {

  public String cmdName;
  public String message;

  public CsMessageEvent(String cmdName, String message) {
    this.cmdName = cmdName;
    this.message = message;
  }
}
