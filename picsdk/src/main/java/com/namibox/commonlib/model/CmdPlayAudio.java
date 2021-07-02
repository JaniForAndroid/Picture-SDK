package com.namibox.commonlib.model;

/**
 * Create time: 2020/4/13.
 */
public class CmdPlayAudio extends BaseCmd {
  public static final String OP_SHOW = "show";
  public static final String OP_HIDE = "hide";
  public static final String OP_PLAY = "play";
  public static final String OP_PAUSE = "pause";
  public static final String OP_STOP = "stop";
  public String playstatus;
  public int index;
  public String mode;
  public static final String MODE_FULL = "full";
  public static final String MODE_SIMPLE = "simple";
  public static final String MODE_CLICK_READ = "clickread";
  public static final String MODE_WORD_READ = "wordread";
  public String begintime;
  public String endtime;
  public String url;
  public String tipname;
  public String changetime;
  public String itemid;
}
