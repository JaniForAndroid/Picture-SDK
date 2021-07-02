package com.namibox.commonlib.event;

/**
 * Created by wzp on 2020/1/20.
 */
public class LoadShortVideoEvent {
  public int video_index;
  public int video_id;
  public boolean isLoadMore;

  public LoadShortVideoEvent(boolean isLoadMore,int video_index,int video_id) {
    this.isLoadMore = isLoadMore;
    this.video_index = video_index;
    this.video_id = video_id;
  }
}
