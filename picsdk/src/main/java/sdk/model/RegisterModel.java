package sdk.model;

import android.support.annotation.Keep;

@Keep
public class RegisterModel {

  public String access_token;
  public int product_id;//产品ID
  public int content_id;//内容ID（单本绘本节点ID）
  public int partner_id;//合作方ID
  public String cellphone;//用户明文手机号

  public RegisterModel(String access_token, int product_id, int content_id, int partner_id,
      String cellphone) {
    this.access_token = access_token;
    this.product_id = product_id;
    this.content_id = content_id;
    this.partner_id = partner_id;
    this.cellphone = cellphone;
  }

  public void setAccess_token(String access_token) {
    this.access_token = access_token;
  }

  public int getProduct_id() {
    return product_id;
  }

  public void setProduct_id(int product_id) {
    this.product_id = product_id;
  }

  public int getContent_id() {
    return content_id;
  }

  public void setContent_id(int content_id) {
    this.content_id = content_id;
  }

  public int getPartner_id() {
    return partner_id;
  }

  public void setPartner_id(int partner_id) {
    this.partner_id = partner_id;
  }

  public String getCellphone() {
    return cellphone;
  }

  public void setCellphone(String cellphone) {
    this.cellphone = cellphone;
  }

  public String getAccess_token() {
    return access_token;
  }
}
