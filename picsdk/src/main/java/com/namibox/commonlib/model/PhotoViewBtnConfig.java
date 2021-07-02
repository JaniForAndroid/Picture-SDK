package com.namibox.commonlib.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sunha on 2017/5/8 0008.
 */

public class PhotoViewBtnConfig implements Parcelable {

  public String name;
  public int count;
  public boolean is_like;
  public boolean is_fav;
  public String fav_type;
  public String object_type;


  protected PhotoViewBtnConfig(Parcel in) {
    name = in.readString();
    count = in.readInt();
    is_like = in.readByte() != 0;
    is_fav = in.readByte() != 0;
    fav_type = in.readString();
    object_type = in.readString();
  }

  public static final Creator<PhotoViewBtnConfig> CREATOR = new Creator<PhotoViewBtnConfig>() {
    @Override
    public PhotoViewBtnConfig createFromParcel(Parcel in) {
      return new PhotoViewBtnConfig(in);
    }

    @Override
    public PhotoViewBtnConfig[] newArray(int size) {
      return new PhotoViewBtnConfig[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeInt(count);
    dest.writeByte((byte) (is_like ? 1 : 0));
    dest.writeByte((byte) (is_fav ? 1 : 0));
    dest.writeString(fav_type);
    dest.writeString(object_type);
  }
}
