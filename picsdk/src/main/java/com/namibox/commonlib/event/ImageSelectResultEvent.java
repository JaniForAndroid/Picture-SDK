package com.namibox.commonlib.event;

import com.namibox.commonlib.model.Result;
import java.util.List;

/**
 * @author: Shelter
 * Create time: 2019/9/4, 16:06.
 */
public class ImageSelectResultEvent {

  public List<Result> results;
  public int requestCode;

  public ImageSelectResultEvent(List<Result> results, int requestCode) {
    this.requestCode = requestCode;
    this.results = results;
  }
}
