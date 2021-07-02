package com.namibox.qr_code;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.namibox.util.ImageUtil;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunha on 2017/8/11 0011.
 */

public class QrUtil {

  public static final Map<DecodeHintType, Object> HINTS = new EnumMap<>(DecodeHintType.class);

  static {
    List<BarcodeFormat> allFormats = new ArrayList<>();
    allFormats.add(BarcodeFormat.AZTEC);
    allFormats.add(BarcodeFormat.CODABAR);
    allFormats.add(BarcodeFormat.CODE_39);
    allFormats.add(BarcodeFormat.CODE_93);
    allFormats.add(BarcodeFormat.CODE_128);
    allFormats.add(BarcodeFormat.DATA_MATRIX);
    allFormats.add(BarcodeFormat.EAN_8);
    allFormats.add(BarcodeFormat.EAN_13);
    allFormats.add(BarcodeFormat.ITF);
    allFormats.add(BarcodeFormat.MAXICODE);
    allFormats.add(BarcodeFormat.PDF_417);
    allFormats.add(BarcodeFormat.QR_CODE);
    allFormats.add(BarcodeFormat.RSS_14);
    allFormats.add(BarcodeFormat.RSS_EXPANDED);
    allFormats.add(BarcodeFormat.UPC_A);
    allFormats.add(BarcodeFormat.UPC_E);
    allFormats.add(BarcodeFormat.UPC_EAN_EXTENSION);
    HINTS.put(DecodeHintType.TRY_HARDER, BarcodeFormat.QR_CODE);
    HINTS.put(DecodeHintType.POSSIBLE_FORMATS, allFormats);
    HINTS.put(DecodeHintType.CHARACTER_SET, "utf-8");
  }

  /**
   * 同步解析本地图片二维码。该方法是耗时操作，请在子线程中调用。
   *
   * @param picturePath 要解析的二维码图片本地路径
   * @return 返回二维码图片里的内容 或 null
   */
  public static String syncDecodeQRCode(String picturePath) {
    return syncDecodeQRCode(getDecodeAbleBitmap(picturePath));
  }

  /**
   * 同步解析bitmap二维码。该方法是耗时操作，请在子线程中调用。
   *
   * @param bitmap 要解析的二维码图片
   * @return 返回二维码图片里的内容 或 null
   */
  public static String syncDecodeQRCode(Bitmap bitmap) {
    Result result = null;
    RGBLuminanceSource source = null;
    try {
      int width = bitmap.getWidth();
      int height = bitmap.getHeight();
      int[] pixels = new int[width * height];
      bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
      source = new RGBLuminanceSource(width, height, pixels);
      result = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(source)), HINTS);
      return result.getText();
    } catch (Exception e) {
      e.printStackTrace();
      if (source != null) {
        try {
          result = new MultiFormatReader().decode(new BinaryBitmap(new GlobalHistogramBinarizer(source)), HINTS);
          return result.getText();
        } catch (Throwable e2) {
          e2.printStackTrace();
        }
      }
      return null;
    }
  }

  /**
   * 将本地图片文件转换成可解码二维码的 Bitmap。为了避免图片太大，这里对图片进行了压缩。感谢 https://github.com/devilsen 提的 PR
   *
   * @param picturePath 本地图片文件路径
   * @return
   */
  private static Bitmap getDecodeAbleBitmap(String picturePath) {
    try {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(picturePath, options);
      int sampleSize = options.outHeight / 1920;
      if (sampleSize <= 0) {
        sampleSize = 1;
      }
      options.inSampleSize = sampleSize;
      options.inJustDecodeBounds = false;
      return BitmapFactory.decodeFile(picturePath, options);
    } catch (Exception e) {
      return null;
    }
  }


  public static String getStringFromQRCode(String filePath) {
    String httpString = "";
    try {
      Bitmap bmp = ImageUtil.decodeBitmapFromFile(filePath, 1920);
      int width = bmp.getRowBytes();
      int height = bmp.getHeight();
      int[] data = new int[width * height];
      bmp.getPixels(data, 0, width, 0, 0, width, height);
      RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
      BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
      MultiFormatReader reader = new MultiFormatReader();
      Result re = reader.decode(bitmap);
      if (re != null && !TextUtils.isEmpty(re.getText())) {
        httpString = re.getText();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return httpString;
  }


  public static String getStringFromQRCode(Bitmap bmp) {
    String httpString = "";
    int width = bmp.getWidth();
    int height = bmp.getHeight();
    int[] data = new int[width * height];
    bmp.getPixels(data, 0, width, 0, 0, width, height);
    RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
    BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
    QRCodeReader reader = new QRCodeReader();
    Result re = null;
    try {
      re = reader.decode(bitmap1);
    } catch (NotFoundException | FormatException | ChecksumException e) {
      e.printStackTrace();
    }
    if (re != null && !TextUtils.isEmpty(re.getText())) {
      httpString = re.getText();
    }

    return httpString;
  }

}
