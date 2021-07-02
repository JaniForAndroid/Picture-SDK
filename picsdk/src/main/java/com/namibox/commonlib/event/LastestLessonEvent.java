package com.namibox.commonlib.event;

/**
 * Created by wzp on 2019/8/8
 */
public class LastestLessonEvent {
  public String lessonJsonObj;

  public LastestLessonEvent(String lessonJsonObj) {
    this.lessonJsonObj = lessonJsonObj;
  }
}
