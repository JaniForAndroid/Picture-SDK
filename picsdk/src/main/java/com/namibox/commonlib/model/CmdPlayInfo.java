package com.namibox.commonlib.model;

/**
 * Create time: 2020/4/13.
 */
public class CmdPlayInfo extends BaseCmd {
  public String playurl;
  public String m3u8url;
  public String thumburl;
  public int seektime;
  public float duration;
  public long size;
  public boolean auto_play;
  public boolean mute_play;
  public String title;
  public String itemid;
  public String changetime;
  public String parentid;
  public boolean readsense;
  public Interrupt interrupt;

  public static class Interrupt {

    public String web_url;
    public InterruptData[] data;
  }

  public static class InterruptData {

    public int data_id;
    public long interrupt_time;
  }

}
