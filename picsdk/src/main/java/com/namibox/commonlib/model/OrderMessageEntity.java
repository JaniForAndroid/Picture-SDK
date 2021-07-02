package com.namibox.commonlib.model;

import android.text.TextUtils;
import com.google.gson.Gson;
import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 用户订单消息
 */
public class OrderMessageEntity implements Serializable {

  public int id;//id 是没有任何意义的，demo传这个id，只是为了知道选择的是哪张图片，应该显示哪张图片
  public String title;
  public String order_title = "订单号";
  public String price;
  public String desc;
  public String img_url;
  public String item_url;

  public OrderMessageEntity(int id, String title, String order_title, String price, String desc,
      String imgUrl, String itemUrl) {
    this.id = id;
    this.title = title;
    this.order_title = order_title;
    this.price = price;
    this.desc = desc;
    this.img_url = imgUrl;
    this.item_url = itemUrl;
  }

  public String getTitle() {
    return title;
  }

  public String getOrder_title() {
    return order_title;
  }

  public String getPrice() {
    return price;
  }

  public String getDesc() {
    return desc;
  }

  public String getImgUrl() {
    return img_url;
  }

  public String getItemUrl() {
    return item_url;
  }

  public int getId() {
    return id;
  }

  public JSONObject getJSONObject() {
    JSONObject jsonObject = new JSONObject();
    JSONObject jsonMsgType = new JSONObject();
    try {
      jsonObject.put("id", this.id);
      jsonObject.put("title", this.title);
      jsonObject.put("order_title", this.order_title);
      if (TextUtils.isEmpty(this.price)) {
        jsonObject.put("price", "");
      } else {
        jsonObject.put("price", this.price);
      }
      if (TextUtils.isEmpty(this.desc)) {
        jsonObject.put("desc", "");
      } else {
        jsonObject.put("desc", this.desc);
      }
      if (TextUtils.isEmpty(this.img_url)) {
        jsonObject.put("img_url", "");
      } else {
        jsonObject.put("img_url", this.img_url);
      }
      if (TextUtils.isEmpty(this.item_url)) {
        jsonObject.put("item_url", "");
      } else {
        jsonObject.put("item_url", this.item_url);
      }
      jsonMsgType.put("order", jsonObject);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return jsonMsgType;
  }


  public static OrderMessageEntity getEntityFromJSONObject(JSONObject jsonMsgType) {
    Gson gson = new Gson();
    try {
      JSONObject jsonOrder = jsonMsgType.getJSONObject("order");
      //            int id = jsonOrder.getInt("id");
//            String title = jsonOrder.getString("title");
//            String orderTitle = jsonOrder.getString("order_title");
//            String price = jsonOrder.getString("price");
//            String desc = jsonOrder.getString("desc");
//            String imgUrl = jsonOrder.getString("img_url");
//            String itemUrl = jsonOrder.getString("item_url");
//            OrderMessageEntity entity = new OrderMessageEntity(id, title, orderTitle, price, desc, imgUrl, itemUrl);
      return gson.fromJson(jsonOrder.toString(), OrderMessageEntity.class);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }


}
