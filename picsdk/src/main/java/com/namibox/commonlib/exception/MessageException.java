package com.namibox.commonlib.exception;

/**
 * Created by sunha on 2017/5/19 0019.
 */

public class MessageException extends Exception {

  public boolean isFinishActivity;

  public MessageException(String message) {
    super(message);
    isFinishActivity = false;
  }

  public MessageException(String message, boolean finishActivity) {
    super(message);
    isFinishActivity = finishActivity;
  }
}
