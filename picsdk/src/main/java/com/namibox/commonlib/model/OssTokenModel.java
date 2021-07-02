package com.namibox.commonlib.model;

/**
 * Created by sunhapper on 2016/6/16 0016.
 */
public class OssTokenModel implements Cloneable {

  public int errcode;
  public String description;
  public OssToken data;

  @Override
  public OssTokenModel clone() throws CloneNotSupportedException {
    return (OssTokenModel) super.clone();
  }

  @Override
  public String toString() {
    return "OssToken{" +
        "errcode=" + errcode +
        ", description='" + description + '\'' +
        ", AccessKeySecret='" + data.AccessKeySecret + '\'' +
        ", SecurityToken='" + data.SecurityToken + '\'' +
        ", Expiration='" + data.Expiration + '\'' +
        ", AccessKeyId='" + data.AccessKeyId + '\'' +
        ", bucketName='" + data.bucketName + '\'' +
        ", objectKey='" + data.objectKey + '\'' +
        ", endpoint='" + data.endpoint + '\'' +
        ", uploadFile=" + data.uploadFile +
        '}';
  }
}
