package com.namibox.commonlib.event;

public class WorkEvent {
  public static final int OP_UPDATE = 0x01;
  public static final int OP_DELETE = 0x02;
  public long homework_id;
  public int op;
  public String status;

  public WorkEvent(long homework_id, String status) {
    this(homework_id, status, OP_UPDATE);
  }

  public WorkEvent(long homework_id, String status, int op) {
    this.homework_id = homework_id;
    this.status = status;
    this.op = op;
  }
}
