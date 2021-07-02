package com.namibox.commonlib.event;

public class MessageWorkEvent {

  public String result;
  public long homework_id;


  public MessageWorkEvent(String result, long homework_id) {
    this.result = result;
    this.homework_id = homework_id;
  }
}
