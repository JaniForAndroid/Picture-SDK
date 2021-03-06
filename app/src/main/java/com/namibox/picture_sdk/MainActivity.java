package com.namibox.picture_sdk;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import sdk.NBPictureSDK;
import sdk.SDKDemoHelper;
import com.namibox.util.MD5Util;
import java.util.ArrayList;
import java.util.List;
import sdk.callback.AccessTokenCallback;
import sdk.callback.CleanCacheCallback;
import sdk.callback.FakeLoginCallback;
import sdk.callback.GetPictureInfoCallback;
import sdk.callback.RegisterSDKInterface;
import sdk.model.DubbingResultBean;
import sdk.model.PictureBean;
import sdk.model.PictureDetailBean;
import sdk.model.RegisterModel;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private RegisterModel registerModel;
  private RecyclerView recyclerView;
  private RvAdapter rvAdapter;
  private List<PictureBean> dataList;
  private RecyclerView rv_detail;
  private RvDetailAdapter detailAdapter;
  private List<PictureDetailBean> detailBeans;
  private EditText et_appcode;
  private EditText et_phone;
  private EditText et_product_id;
  private EditText et_content_id;
  private EditText et_partner_id;
  private TextView view, view_list;
  private ConstraintLayout cl_list;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    et_appcode = findViewById(R.id.et_appcode);
    et_phone = findViewById(R.id.et_phone);
    et_product_id = findViewById(R.id.et_product_id);
    et_content_id = findViewById(R.id.et_content_id);
    et_partner_id = findViewById(R.id.et_partner_id);
    cl_list = findViewById(R.id.cl_list);

    recyclerView = findViewById(R.id.rv);
    dataList = new ArrayList<>();
    rvAdapter = new RvAdapter(dataList);
    recyclerView.setAdapter(rvAdapter);

    rv_detail = findViewById(R.id.rv_detail);
    detailBeans = new ArrayList<>();
    detailAdapter = new RvDetailAdapter(detailBeans);
    rv_detail.setAdapter(detailAdapter);

    view = findViewById(R.id.view);
    view_list = findViewById(R.id.view_list);
    cl_list.setOnClickListener(v -> cl_list.setVisibility(View.GONE));
  }

  private void toast(String msg) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  }

  public void register(View view) {
    if (registerModel == null) {
      toast("????????????token");
      return;
    }

    String cellphone = "18819439438";//16602115911
    if (!TextUtils.isEmpty(et_phone.getText())) {
      cellphone = et_phone.getText().toString().trim();
    }

    int productId = 3467;
    if (!TextUtils.isEmpty(et_product_id.getText())) {
      String productIdStr = et_product_id.getText().toString().trim();
      try {
        productId = Integer.parseInt(productIdStr);
      } catch (NumberFormatException e) {
        e.printStackTrace();
        Log.e(TAG, "??????productId?????????" + e.toString());
      }
    }

    int contentId = 112674;
    if (!TextUtils.isEmpty(et_content_id.getText())) {
      String contentIdStr = et_content_id.getText().toString().trim();
      try {
        contentId = Integer.parseInt(contentIdStr);
      } catch (NumberFormatException e) {
        e.printStackTrace();
        Log.e(TAG, "??????contentId?????????" + e.toString());
      }
    }

    int partnerId = 512;
    if (!TextUtils.isEmpty(et_partner_id.getText())) {
      String partnerIdStr = et_partner_id.getText().toString().trim();
      try {
        partnerId = Integer.parseInt(partnerIdStr);
      } catch (NumberFormatException e) {
        e.printStackTrace();
        Log.e(TAG, "??????partnerId?????????" + e.toString());
      }
    }

    registerModel.setProduct_id(productId);
    registerModel.setContent_id(contentId);
    registerModel.setPartner_id(partnerId);
    registerModel.setCellphone(cellphone);

    /**
     * ??????SDK??????????????????????????????????????????
     */
    new NBPictureSDK().register(MainActivity.this, registerModel, new RegisterSDKInterface() {
      @Override
      public void onDubbingResult(DubbingResultBean result) {
        Log.d(TAG, "dubbingResult:" + result.toString());
        toast("???????????????????????????");
      }

      @Override
      public void onError(Throwable throwable, String code) {
        toast("???????????????" + throwable.getMessage());
      }
    });
  }

  /**
   * ??????????????????????????????????????????????????????????????????
   * @param view
   */
  public void login(View view) {
    if (registerModel == null) {
      toast("????????????token");
      return;
    }
    String cellphone = "18819439438";//16602115911
    if (!TextUtils.isEmpty(et_phone.getText())) {
      cellphone = et_phone.getText().toString().trim();
    }

    int productId = 3467;
    if (!TextUtils.isEmpty(et_product_id.getText())) {
      String productIdStr = et_product_id.getText().toString().trim();
      try {
        productId = Integer.parseInt(productIdStr);
      } catch (NumberFormatException e) {
        e.printStackTrace();
        Log.e(TAG, "??????productId?????????" + e.toString());
      }
    }

    int contentId = 112674;
    if (!TextUtils.isEmpty(et_content_id.getText())) {
      String contentIdStr = et_content_id.getText().toString().trim();
      try {
        contentId = Integer.parseInt(contentIdStr);
      } catch (NumberFormatException e) {
        e.printStackTrace();
        Log.e(TAG, "??????contentId?????????" + e.toString());
      }
    }

    int partnerId = 512;
    if (!TextUtils.isEmpty(et_partner_id.getText())) {
      String partnerIdStr = et_partner_id.getText().toString().trim();
      try {
        partnerId = Integer.parseInt(partnerIdStr);
      } catch (NumberFormatException e) {
        e.printStackTrace();
        Log.e(TAG, "??????partnerId?????????" + e.toString());
      }
    }

    registerModel.setProduct_id(productId);
    registerModel.setContent_id(contentId);
    registerModel.setPartner_id(partnerId);
    registerModel.setCellphone(cellphone);

    new SDKDemoHelper().login(MainActivity.this, registerModel,
        new FakeLoginCallback() {
          @Override
          public void onSuccess() {
            toast("????????????");
          }

          @Override
          public void onFail(Throwable throwable) {
            toast("????????????");
          }
        });
  }

  /**
   * ???????????????????????????????????????
   * 1./data/user/0/{yours_package_name}/cache
   * 2./storage/emulated/0/Android/data/{yours_package_name}/cache
   * 3./data/user/0/{yours_package_name}/files/file_cache
   * @param view
   */
  public void clean(View view) {
    NBPictureSDK.cleanCache(this, new CleanCacheCallback() {
      @Override
      public void onBefore() {
        toast("????????????...");
      }

      @Override
      public void onSuccess() {
        toast("????????????");
      }

      @Override
      public void onFail(Throwable t) {
        toast("????????????");
        Log.d(TAG, "?????????????????????" + t.toString());
      }
    });
  }

  /**
   * ?????????????????????accessToken??????????????????????????????????????????
   * @param view
   */
  public void getAccessToken(View view) {
    String appCode = "424965437";// 418558986
    if (!TextUtils.isEmpty(et_appcode.getText())) {
      appCode = et_appcode.getText().toString().trim();
    }

    String cellphone = "18819439438";//16602115911
//    if (!TextUtils.isEmpty(et_phone.getText())) {
//      cellphone = et_phone.getText().toString().trim();
//    }
    int productId = 3467;
    if (!TextUtils.isEmpty(et_product_id.getText())) {
      String productIdStr = et_product_id.getText().toString().trim();
      try {
        productId = Integer.parseInt(productIdStr);
      } catch (NumberFormatException e) {
        e.printStackTrace();
        Log.e(TAG, "??????productId?????????" + e.toString());
      }
    }
    int contentId = 112674;
    if (!TextUtils.isEmpty(et_content_id.getText())) {
      String contentIdStr = et_content_id.getText().toString().trim();
      try {
        contentId = Integer.parseInt(contentIdStr);
      } catch (NumberFormatException e) {
        e.printStackTrace();
        Log.e(TAG, "??????contentId?????????" + e.toString());
      }
    }
    int partnerId = 512;
    if (!TextUtils.isEmpty(et_partner_id.getText())) {
      String partnerIdStr = et_partner_id.getText().toString().trim();
      try {
        partnerId = Integer.parseInt(partnerIdStr);
      } catch (NumberFormatException e) {
        e.printStackTrace();
        Log.e(TAG, "??????partnerId?????????" + e.toString());
      }
    }

//    String appSecret = "bbc810f7-95c6-41c3-9519-124bfa7cbf7f";
    String appSecret = "bae6fb72-f7fb-4409-8042-2cf434767169";
    long timeStamp = System.currentTimeMillis() / 1000;
    String chk = MD5Util.md5(appCode + "||" + (timeStamp + 256) + "||" + appSecret);
    Log.d(TAG, "chk:" + chk);

    String phone = Base64.encodeToString(cellphone.getBytes(), Base64.NO_WRAP);
    int finalProductId = productId;
    int finalContentId = contentId;
    int finalPartnerId = partnerId;
    String finalCellphone = cellphone;
    new SDKDemoHelper().getAccessToken(appCode, phone, timeStamp, chk, new AccessTokenCallback() {
      @Override
      public void onResult(String token) {
        registerModel = new RegisterModel(token, finalProductId, finalContentId,
            finalPartnerId, finalCellphone);
        toast("??????token??????");
      }

      @Override
      public void onError(String msg) {
        Toast.makeText(MainActivity.this, "token????????????", Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * ???????????????????????????????????????????????????????????????????????????
   * @param view
   */
  public void select(View view) {
    if (registerModel == null) {
      toast("????????????token");
      return;
    }
    new SDKDemoHelper().getPictureBookList(this, registerModel.getAccess_token(),
        new GetPictureInfoCallback() {
          @Override
          public void onDetailSuccess(List<PictureDetailBean> list) {

          }

          @Override
          public void onSuccess(List<PictureBean> list) {
            dataList.clear();
            dataList.addAll(list);
            rvAdapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
            view_list.setVisibility(View.VISIBLE);
            cl_list.setVisibility(View.VISIBLE);
          }

          @Override
          public void onError() {

          }
        });
  }

  /**
   * ?????????????????????????????????????????????????????????????????????
   * @param view
   */
  public void logout(View view) {
    new SDKDemoHelper().logout(this);
  }

  public class RvAdapter extends RecyclerView.Adapter<RvViewHolder> {

    List<PictureBean> datas;

    public RvAdapter(List<PictureBean> datas) {
      this.datas = datas;
    }

    @NonNull
    @Override
    public RvViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
      View itemView = LayoutInflater.from(MainActivity.this)
          .inflate(R.layout.item_pic, viewGroup, false);
      return new RvViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RvViewHolder rvViewHolder, int i) {
      PictureBean pictureBean = datas.get(i);
      rvViewHolder.tv.setText(pictureBean.level + "-" + pictureBean.product_id);
      rvViewHolder.itemView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          String product_id = pictureBean.product_id;
          et_product_id.setText(product_id);
          new SDKDemoHelper().getPictureDetail(MainActivity.this, registerModel.getAccess_token(),
              product_id, new GetPictureInfoCallback() {
                @Override
                public void onDetailSuccess(List<PictureDetailBean> list) {
                  detailBeans.clear();
                  detailBeans.addAll(list);
                  detailAdapter.notifyDataSetChanged();
                  rv_detail.setVisibility(View.VISIBLE);
                  view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onSuccess(List<PictureBean> list) {

                }

                @Override
                public void onError() {

                }
              });
        }
      });
    }

    @Override
    public int getItemCount() {
      return datas == null ? 0 : datas.size();
    }
  }

  public class RvDetailAdapter extends RecyclerView.Adapter<RvViewHolder> {

    List<PictureDetailBean> datas;

    public RvDetailAdapter(List<PictureDetailBean> datas) {
      this.datas = datas;
    }

    @NonNull
    @Override
    public RvViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
      View itemView = LayoutInflater.from(MainActivity.this)
          .inflate(R.layout.item_pic, viewGroup, false);
      return new RvViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RvViewHolder rvViewHolder, int i) {
      PictureDetailBean detailBean = datas.get(i);
      rvViewHolder.tv.setText(detailBean.content_id + "-" + detailBean.title);
      rvViewHolder.itemView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          int content_id = detailBean.content_id;
          et_content_id.setText(content_id + "");
          rv_detail.setVisibility(View.GONE);
          recyclerView.setVisibility(View.GONE);
          view.setVisibility(View.GONE);
          view_list.setVisibility(View.GONE);
          cl_list.setVisibility(View.GONE);
        }
      });
    }

    @Override
    public int getItemCount() {
      return datas == null ? 0 : datas.size();
    }
  }

  public class RvViewHolder extends RecyclerView.ViewHolder {

    TextView tv;

    public RvViewHolder(@NonNull View itemView) {
      super(itemView);
      tv = itemView.findViewById(R.id.tv);
    }
  }
}