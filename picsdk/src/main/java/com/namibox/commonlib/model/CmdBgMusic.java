package com.namibox.commonlib.model;

/**
 * Create time: 2020/4/13.
 */
public class CmdBgMusic extends BaseCmd {
  public int repeats = 1;
  public String url;
  public float volume = 1f;
  public String operation;
  public int soundid;
  public boolean vibrate;
}
