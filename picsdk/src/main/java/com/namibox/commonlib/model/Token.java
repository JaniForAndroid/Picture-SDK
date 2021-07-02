package com.namibox.commonlib.model;

import java.util.ArrayList;

/**
 * Created by ryan on 2015/3/5.
 */
public class Token {

  public ArrayList<TokenItem> token;

  public static class TokenItem {

    public String access_token;
    public String service_type;
    public String type;
    public String object_id;
    public int errcode;
    public String errmsg;
  }
}
