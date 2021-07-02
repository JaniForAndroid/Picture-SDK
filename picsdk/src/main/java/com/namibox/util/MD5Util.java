package com.namibox.util;

import android.support.annotation.Keep;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A utility class for computing MD5 hashes.
 *
 * @author Cyril Mottier
 */
@Keep
public class MD5Util {

  private static MessageDigest sMd5MessageDigest;
  public static StringBuilder sStringBuilder;

  static {
    try {
      sMd5MessageDigest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      // TODO Cyril: I'm quite sure about my "MD5" algorithm
      // but this is not a correct way to handle an exception ...
    }
    sStringBuilder = new StringBuilder();
  }

  private MD5Util() {
  }

  /**
   * Return a hash according to the MD5 algorithm of the given String.
   *
   * @param s The String whose hash is required
   * @return The MD5 hash of the given String
   */
  public static synchronized String md5(String s) {
    try {
      return md5(s.getBytes());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

//  public static synchronized String md5(File file) {
//    try {
//      FileInputStream in = new FileInputStream(file);
//      ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
//      byte[] buff = new byte[10240];
//      int rc = 0;
//      while ((rc = in.read(buff, 0, 10240)) != -1) {
//        swapStream.write(buff, 0, rc);
//      }
//      return md5(swapStream.toByteArray());
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//    return "";
//  }

  public static synchronized String md5(File file) {
    String value = null;
    FileInputStream in = null;
    try {
      in = new FileInputStream(file);
      MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.update(byteBuffer);
      BigInteger bi = new BigInteger(1, md5.digest());
      value = bi.toString(16);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if(null != in) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return value;
  }

  public static synchronized String md5(byte[] bytes) {
    sMd5MessageDigest.reset();
    sMd5MessageDigest.update(bytes);
    byte digest[] = sMd5MessageDigest.digest();

    sStringBuilder.setLength(0);
    for (byte d : digest) {
      final int b = d & 255;
      if (b < 16) {
        sStringBuilder.append('0');
      }
      sStringBuilder.append(Integer.toHexString(b));
    }
    return sStringBuilder.toString();
  }

  public static String byteArrayToHex(byte[] digest) {
    sStringBuilder.setLength(0);
    for (byte d : digest) {
      final int b = d & 255;
      if (b < 16) {
        sStringBuilder.append('0');
      }
      sStringBuilder.append(Integer.toHexString(b));
    }
    return sStringBuilder.toString();
  }

  /**
   * @param decript 要加密的字符串
   * @return 加密的字符串 SHA1加密
   */
  public static String SHA1(String decript) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      digest.update(decript.getBytes());
      byte messageDigest[] = digest.digest();
      // Create Hex String
      StringBuffer hexString = new StringBuffer();
      // 字节数组转换为 十六进制 数
      for (int i = 0; i < messageDigest.length; i++) {
        String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
        if (shaHex.length() < 2) {
          hexString.append(0);
        }
        hexString.append(shaHex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return "";
  }

}
