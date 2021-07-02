package com.namibox.commonlib.event;

/**
 * Create time: 2017/5/4.
 */

public class LoginStatusEvent {

  public boolean isLogin;

  public LoginStatusEvent(boolean isLogin) {
    this.isLogin = isLogin;
  }
}
