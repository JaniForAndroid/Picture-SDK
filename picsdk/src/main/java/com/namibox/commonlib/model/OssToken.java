package com.namibox.commonlib.model;

import java.io.File;

/**
 * Created by sunhapper on 2016/6/16 0016.
 */
public class OssToken implements Cloneable {

  public int errcode;
  public String description;
  public String AccessKeySecret;
  public String SecurityToken;
  public String Expiration;
  public String AccessKeyId;
  public String bucketName;
  //objectKey实质是上传文件在oss上的相对路径
  public String objectKey;
  public String endpoint;
  //oms_cid是平台记录作品的唯一标识符，故事秀直传时需要在通知平台的api中带上该字段
  public String oms_cid;
  public File uploadFile;

  @Override
  public OssToken clone() throws CloneNotSupportedException {
    return (OssToken) super.clone();
  }

  @Override
  public String toString() {
    return "OssToken{" +
        "errcode=" + errcode +
        ", description='" + description + '\'' +
        ", AccessKeySecret='" + AccessKeySecret + '\'' +
        ", SecurityToken='" + SecurityToken + '\'' +
        ", Expiration='" + Expiration + '\'' +
        ", AccessKeyId='" + AccessKeyId + '\'' +
        ", bucketName='" + bucketName + '\'' +
        ", objectKey='" + objectKey + '\'' +
        ", endpoint='" + endpoint + '\'' +
        ", uploadFile=" + uploadFile +
        '}';
  }
}
