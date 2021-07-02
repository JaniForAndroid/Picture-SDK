package com.namibox.commonlib.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Akkun on 2020/4/22.
 * web: http://www.zkyml.com
 * Des:
 */
public class Result implements Parcelable {

  public String path;
  public String model;
  public String make;
  public String dateTime;
  public String gps;
  public int width;
  public int height;
  public long size;


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.path);
    dest.writeString(this.model);
    dest.writeString(this.make);
    dest.writeString(this.dateTime);
    dest.writeString(this.gps);
    dest.writeInt(this.width);
    dest.writeInt(this.height);
    dest.writeLong(this.size);
  }

  public Result() {
  }

  protected Result(Parcel in) {
    this.path = in.readString();
    this.model = in.readString();
    this.make = in.readString();
    this.dateTime = in.readString();
    this.gps = in.readString();
    this.width = in.readInt();
    this.height = in.readInt();
    this.size = in.readLong();
  }

  public static final Creator<Result> CREATOR = new Creator<Result>() {
    @Override
    public Result createFromParcel(Parcel source) {
      return new Result(source);
    }

    @Override
    public Result[] newArray(int size) {
      return new Result[size];
    }
  };
}
