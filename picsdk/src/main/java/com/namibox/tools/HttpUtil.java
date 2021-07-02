package com.namibox.tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.namibox.commonlib.common.ApiHandler;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Create time: 2018/11/9.
 */
public class HttpUtil {
  public static String httpGet(String url) throws IOException {
    Response<ResponseBody> response = ApiHandler.getBaseApi().commonStringGet(url).execute();
    String body = response.body().string();
    return body;
  }

  public static String httpPost(String url, String json) throws IOException {
    JsonParser parser = new JsonParser();
    JsonElement parse = parser.parse(json);
    Response<ResponseBody> response = ApiHandler.getBaseApi().commonStringPost(url, parse).execute();
    return response.body().string();
  }
}
