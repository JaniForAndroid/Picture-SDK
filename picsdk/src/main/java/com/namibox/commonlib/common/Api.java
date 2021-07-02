package com.namibox.commonlib.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namibox.commonlib.model.BaseNetResult;
import com.namibox.commonlib.model.BookAuth;
import com.namibox.commonlib.model.ChineseEvalBody;
import com.namibox.commonlib.model.ErrBody;
import com.namibox.commonlib.model.EvaluationBody;
import com.namibox.commonlib.model.EvaluationNetResult;
import com.namibox.commonlib.model.Exercise;
import com.namibox.commonlib.model.FeedbackList;
import com.namibox.commonlib.model.FeedbackResult;
import com.namibox.commonlib.model.FriendResult;
import com.namibox.commonlib.model.GetGroupNameResult;
import com.namibox.commonlib.model.LoginResult;
import com.namibox.commonlib.model.NetResponse;
import com.namibox.commonlib.model.NetResult;
import com.namibox.commonlib.model.OssToken;
import com.namibox.commonlib.model.OssTokenModel;
import com.namibox.commonlib.model.PhotoViewInfo;
import com.namibox.commonlib.model.QiniuToken;
import com.namibox.commonlib.model.ReportResponse;
import com.namibox.commonlib.model.ResultBody;
import com.namibox.commonlib.model.SetObjResult;
import com.namibox.commonlib.model.ShareBean;
import com.namibox.commonlib.model.ShareResult;
import com.namibox.commonlib.model.SysConfig;
import com.namibox.commonlib.model.Token;
import com.namibox.commonlib.model.UserIpInfo;
import com.namibox.commonlib.model.Work;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * @author: Shelter
 * Create time: 2018/9/28, 9:14.
 */
public interface Api {

//    @Multipart
//    @POST("/fanshow/reading/upload_works_by_sftp")
//    Call<BaseNetResult> uploadWorkInfo(@Part("content_type") RequestBody content_type, @Part("introduce") RequestBody introduce,
//                                   @Part("bookid") RequestBody bookid, @Part("file_size") RequestBody file_size, @Part("title") RequestBody title,
//                                   @Part("subtype") RequestBody subtype, @Part("parameters") RequestBody parameters);

  //    @Multipart
//    @POST("/fanshow/reading/upload_works_by_sftp")
//    Call<BaseNetResult> uploadWorkInfoWithCallBack(@Part("content_type") RequestBody content_type, @Part("introduce") RequestBody introduce,
//                                               @Part("bookid") RequestBody bookid, @Part("file_size") RequestBody file_size,
//                                               @Part("title") RequestBody title, @Part("subtype") RequestBody subtype,
//                                               @Part("persistent_id") RequestBody persistent_id, @Part("parameters") RequestBody parameters);
  @GET("/api/app/getfeedbacklist")
  Observable<FeedbackList> getFeedbackList(@Query("feature") String feature);

  @POST("/api/app/feedback")
  Observable<FeedbackResult> postFeedback(@Body Map<String, String> body);

  @GET
  Flowable<ShareResult> getShareResult(@Url String url);

  @POST("/fanshow/reading/upload_works_by_sftp")
  Flowable<BaseNetResult> uploadWorkInfo(@Body MultipartBody body);

  @POST("/api/report_picdub_progress")
  Flowable<JsonObject> uploadPicWorkInfo(@Body MultipartBody body);

  @GET("/api/app/get_post_info")
  Call<PhotoViewInfo> getPhotoViewInfo(@Query("object_id") String object_id);

  @POST("/book/shareinfo")
  Flowable<ShareBean> getShareData(@Query("bookid") String bookId);
//    @GET("/api/app/quick_reply")
//    Call<BaseNetResult> quickReply(@Query("comment") String comment, @Query("object_id") String object_id,
//                                   @Query("object_subtype") String object_subtype);

  @POST("/api/app/quick_reply")
  Call<BaseNetResult> quickReply(@Body RequestBody jsonBady);

  @GET("/api/app/set_obj")
  Call<SetObjResult> setObj(@Query("obj_id") String obj_id, @Query("operater") String operater,
      @Query("obj_type") String obj_type, @Query("fav_type") String fav_type);

//    @Multipart
//    @POST("/fanshow/reading/personalSaving")
//    Call<NetResult> saveInfo(@Part("alias") RequestBody alias, @Part("introduce") RequestBody introduce,
//                             @Part("avatar") RequestBody avatar, @Part("user_banner") RequestBody user_banner,
//                             @Part("sys_banner") RequestBody sys_banner);

  @POST
  Call<NetResponse> uploadFile(@Url String url, @Body RequestBody body);

  @POST("/api/app/access_token")
  Observable<Token> accessToken(@Body Token body);

//  @POST("/fanshow/reading/personalSaving")
//  Call<BaseNetResult> saveInfo(@Body MultipartBody body);

  @GET("/api/app/get_user_ipinfo")
  Call<UserIpInfo> getUserIpInfo();

  @POST("/api/app/cdnreporter")
  Flowable<BaseNetResult> uploadCdnInfo(@Body JsonObject jsonObject);

  @GET("/vschool/del_capture")
  Call<BaseNetResult> delCapture(@Query("capture_id") int capture_id);

  @POST
  Observable<SysConfig> sysconfig(@Url String url, @Body Map<String, Object> map);

//  @GET
//  Observable<WeikeCacheResult> getDownloadUrl(@Url String url);

  @GET("/vschool/get_oss_utoken")
  Flowable<OssToken> getOssUtoken(@Query("objectKey") String objectKey);

  @POST("/vschool/classschedule")
  Observable<JsonElement> postClassSchedule(@Body JsonElement vs_class_ids);

  //一个常用的请求接口，不需要重复定义其它接口
  @GET
  Flowable<ResponseBody> commonRequest2(@Url String url);

  //一个常用的请求接口，不需要重复定义其它接口
  @GET
  Observable<ResponseBody> commonRequest(@Url String url);

  @POST
  Observable<ResponseBody> commonRequest(@Url String url, @Body JsonElement body);

  //一个常用的请求接口，返回json格式，不需要重复定义其它接口
  @GET
  Observable<JsonObject> commonJsonGet(@Url String url);

  @GET
  Flowable<JsonElement> commonJsonElementGet(@Url String url);

  @POST
  Flowable<JsonElement> commonJsonElementPost(@Url String url, @Body JsonElement body);

  @POST
  Flowable<OssToken> OssTokenPost(@Url String url, @Body JsonElement body);

  @POST
  @FormUrlEncoded
  Flowable<OssTokenModel> OssTokenPost(@Url String url, @FieldMap Map<String, Object> field);

  //一个常用的post接口，参数json对象，返回json格式，不需要重复定义其它接口
  @POST
  Observable<JsonElement> commonJsonPost(@Url String url);

  @POST
  Observable<JsonElement> commonJsonPost(@Url String url, @Body JsonElement body);

  @POST
  Observable<JsonObject> commonJsonObjectPost(@Url String url, @Body JsonObject body);

  //直播
  @POST
  Observable<JsonObject> commonLiveJsonObjectPost(@Url String url, @Body JsonObject body,
      @Header("nbappid") String nbappid, @Header("nbtoken") String nbtoken);

  //直播
  @GET
  Observable<JsonObject> commonLiveJsonGet(@Url String url, @Header("nbappid") String nbappid,
      @Header("nbtoken") String nbtoken);

  //一个常用的post接口，参数表单，返回json格式，不需要重复定义其它接口
  @POST
  @FormUrlEncoded
  Observable<JsonObject> commonFieldPost(@Url String url, @FieldMap Map<String, Object> field);

  //一个常用的post接口，参数表单，返回json格式，不需要重复定义其它接口
  @POST
  @FormUrlEncoded
  Flowable<JsonObject> commonFieldPost2(@Url String url, @FieldMap Map<String, Object> field);

  /**
   * 上报host  ip de 接口
   */
  @POST("app/uploaddnsinfo")
  Flowable<ReportResponse> reportHostIP(@Body JsonObject body);

  @POST("/api/app/oss_upload_error")
  Flowable<BaseNetResult> uploadOssError(@Body JsonObject body);

  @POST("/api/app/oss_upload_error")
  Flowable<BaseNetResult> uploadOssError(@Body RequestBody body);

  @GET
  Call<ResponseBody> commonStringGet(@Url String url);

  @POST
  Call<ResponseBody> commonStringPost(@Url String url, @Body JsonElement body);

  //口语评测
  @POST
  Flowable<EvaluationNetResult> uploadData(@Url String url, @Body EvaluationBody body);

  @POST
  Flowable<BaseNetResult> uploadErr(@Url String url, @Body ErrBody body);

  @GET
  Flowable<Exercise> getPageContent(@Url String url);

  @POST
  Flowable<BaseNetResult> uploadChineseData(@Url String url, @Body ChineseEvalBody body);

  @GET("/vschool/get_oss_utoken/?objectKey=user")
  Flowable<OssToken> getOssUtokenWithBookId(@Query("book_id") String bookId,
      @Query("share_image") String shareImg);

  //班级圈直播
  @POST("/api/app/update_cast_banner")
  Observable<NetResponse> uploadFile(@Body ResultBody body);

  //盒粉秀
  @FormUrlEncoded
  @POST("/fanshow/get_upload_token")
  Flowable<QiniuToken> getUploadToken(@Field("file_name") String file_name,
      @Field("file_format") String file_format,
      @Field("thumbnail") String thumbnail, @Field("cut_time") String cut_time);

  @FormUrlEncoded
  @POST("/fanshow/get_upload_token")
  Flowable<QiniuToken> getUploadToken4Vr(@Field("file_name") String file_name,
      @Field("file_format") String file_format,
      @Field("thumbnail") String thumbnail, @Field("cut_time") String cut_time, @Field("extra") String extra);

  @GET("/fanshow/get_works_token")
  Flowable<OssToken> ossInfoObsevable();

  @GET("/fanshow/fanshow_upload_token")
  Flowable<OssToken> ossPicInfoObsevable();

  @GET("/fanshow/get_freeaudio_upload_token")
  Flowable<OssToken> ossInfoAudioObsevable();

  @GET("/fanshow/reading/check_work_exits")
  Call<NetResult> checkBook(@Query("bookid") String bookid, @Query("extra") String extra);

  @GET("/fanshow/reading/delete_block_works")
  Call<BaseNetResult> deleteWork(@Query("id") String id);

  @GET("/fanshow/reading/get_myworks")
  Call<Work> getMyWork(@Query("step") String step);

  @GET("/fanshow/reading/get_freeaudio_subtype")
  Call<String[]> getSubtype(@Query("type") String type);

//  @GET("/fanshow/reading/get_personal_info")
//  Call<User> getUserInfo();

  @POST("/lesson/video_dub_step3_submit")
  Flowable<NetResult> dubVideoSubmit(@Body RequestBody requestBody);

  @GET("/fanshow/get_file_upload_token")
  Flowable<QiniuToken> getFileUploadToken(@Query("file_name") String file_name,
      @Query("file_format") String file_format);

  /*************************IMSDK接口************************************************/
  @GET("/zone/friend")
  Flowable<FriendResult> deleteFriend(@Query("uid") String uid, @Query("opt") String del);

  @GET("/zone/friend")
  Flowable<FriendResult> addFriend(@Query("uid") String uid, @Query("opt") String req,
      @Query("msg") String msg);

  @GET("/friend/friend")
  Flowable<FriendResult> deleteYxsFriend(@Query("uid") String uid, @Query("opt") String del);

  @GET("/api/app/get_grp_info")
  Call<GetGroupNameResult> getGroupName(@Query("id") String id);

//  @GET("/vschool/class/{groupId}/members?data=json")
//  Call<GetGroupMemberResult> getGroupMembers(@Path("groupId") String groupId);

  /*************************IMSDK接口************************************************/
  @GET("/auth/get_cap_uk/?api_source=app")
  Observable<BaseNetResult> get_cap_uk();

  //captcha_login 验证码登录的时候发送的【文本】验证码
  //captcha_pwd 修改密码的时候发送的【文本】验证码
  //captcha_voice_login 验证码登录的时候发送的【语音】验证码
  @POST("/auth/sendcaptcha/{path}/?api_source=app")
  Observable<LoginResult> sendcaptcha(@Path("path") String path, @Body Map<String, String> body,
      @Query("source") String source, @Query("is_first") String is_first);

  @POST("/auth/captcha_login?api_source=app")
  Observable<JsonObject> captcha_login(@Body Map<String, String> body);

  @POST("/auth/login?api_source=app")
  Observable<JsonObject> login(@Body Map<String, String> body);

  /*快捷登录手机号码校验*/
  @POST("/auth/namibox_quick_login/valid_phone")
  Observable<JsonObject> phoneValid(@Body Map<String, String> body);

  /*快捷登录*/
  @POST("/auth/namibox_quick_login/login")
  Observable<JsonObject> quickLogin(@Body Map<String, String> body);

  @GET("/auth/wechat_ajax?api_source=app")
  Observable<JsonObject> wechat_ajax(@QueryMap Map<String, String> options);

  @POST("/auth/binding/phone?api_source=app")
  Observable<JsonObject> bindPhone(@Body Map<String, String> body,
      @Query("is_first") String is_first);

  @POST("/auth/checkuser/?api_source=app")
  Observable<JsonObject> checkuser(@Body Map<String, String> body);

  @POST("/auth/chmobile/?api_source=app")
  Observable<JsonObject> chmobile(@Body Map<String, String> body, @Query("dev_id") String dev_id);

  @POST("/auth/checkuser_ajax/?api_source=app")
  Observable<JsonObject> checkuser_ajax(@Body Map<String, String> body);

  @POST("/auth/shanyan/login/")
  Observable<JsonObject> syLogin(@Body Map<String, String> body);

  @POST("/auth/shanyan/login2/")
  Observable<JsonObject> syLogin2(@Body Map<String, String> body);

  @POST("/auth/sign_in/huawei")
  Observable<JsonObject> huaweiLogin(@Body Map<String, String> body);

  @POST("/auth/binding_huawei_account/")
  Observable<JsonObject> huaweiBind(@Body JsonObject body);

  @POST("/auth/huawei/binding/phone")
  Observable<JsonObject> bindPhone_huawei(@Body Map<String, String> body, @Query("is_first") String is_first);

  @GET("auth/namibox_pwd_info")
  Observable<JsonObject> requsetPwdInfo();

  @POST("/sdk/loginbysid")
  Observable<JsonObject> namiboxLogin(@Body JsonObject body);

  @GET("/book/bookauth")
  Observable<BookAuth> bookAuth(@Query("bookid") String bookid, @Query("deviceid") String deviceid,
      @Query("authdevid") String authdevid, @Query("timestamp") String timestamp, @Query("sign") String sign);

  @GET("/book/rjbookerror")
  Observable<JsonObject> rjbookerror(@Query("bookid") String bookid, @Query("deviceid") String deviceid,
      @Query("authdevid") String authdevid, @Query("timestamp") String timestamp,
      @Query("sign") String sign, @Query("recover") int recover);

  @GET("/book/choicesdk")
  Observable<JsonObject> checkSdk(@Query("bookid") String bookid);

}
