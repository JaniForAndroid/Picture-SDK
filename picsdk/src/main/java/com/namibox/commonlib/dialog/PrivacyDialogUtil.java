package com.namibox.commonlib.dialog;

import android.support.v7.app.AppCompatActivity;
import android.view.View.OnClickListener;

/**
 * @Description 隐私弹框对话框工具类
 * @CreateTime: 2019/12/18 19:46
 * @Author: zhangkx
 */
public class PrivacyDialogUtil {

  public static final String PRIVACY_POLICY_ONE = "欢迎来到纳米盒:\n"
      + "       为了更透明地呈现纳米盒手机和使用您的个人信息情况，以及您享有的个人信息控制权，请您知悉阅读并充分理解《纳米盒用户协议及隐私政策》。\n"
      + "       如您同意本隐私政策内容，请点击“同意“开始使用我们的产品与服务。纳米盒将严格按照法律法规要求，采取相应的安全保护措施，尽全力保护您的个人信息安全。\n"
      + "       您也可以选择“仅浏览”进入浏览模式，但是将无法使用点读获取、观看直播/回放、答题、查看学习资料等功能。";
  public static final String PRIVACY_POLICY_TWO = "欢迎登录纳米盒:\n"
      + "       为了更透明地呈现纳米盒手机和使用您的个人信息情况，以及您享有的个人信息控制权，请您知悉阅读并充分理解《纳米盒用户协议及隐私政策》。\n"
      + "       如您同意本隐私政策内容，请点击“同意“开始使用我们的产品与服务。纳米盒将严格按照法律法规要求，采取相应的安全保护措施，尽全力保护您的个人信息安全。\n"
      + "       如果不同意我们将无法使用您的信息进行登录，请您理解。";
  public static final String PRIVACY_TITLE = "温馨提示";
  public static final String PRIVACY_TARGET = "《纳米盒用户协议及隐私政策》";
  public static final String PRIVACY_EXIT = "不同意直接退出";


  public static void showPrivacyDialog(AppCompatActivity activity, String title, String privacy,
      String target, OnClickListener clickListener, String action1, OnClickListener clickListener1,
      String action2, OnClickListener clickListener2, String exit, OnClickListener exitListener) {
    DialogUtil
        .showPrivacyDialog(activity, title, privacy, target, clickListener, action1, clickListener1,
            action2, clickListener2, exit, exitListener, "", null, null);
  }
}
