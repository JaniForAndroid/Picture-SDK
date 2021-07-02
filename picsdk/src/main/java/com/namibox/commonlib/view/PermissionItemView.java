package com.namibox.commonlib.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.picsdk.R;
import com.namibox.tools.PermissionUtil;

/**
 * Created by sunha on 2017/8/15 0015.
 */

public class PermissionItemView extends LinearLayout {

  private String permission;

  public PermissionItemView(Context context, String permission) {
    super(context);
    this.permission = permission;
    initView(context);
  }

  private void initView(Context context) {
    String permissionTip = PermissionUtil.getPermissonTip(permission);
    View view = LayoutInflater.from(context).inflate(R.layout.permission_tip_item, this);
    TextView textView = (TextView) view.findViewById(R.id.permissionTip);
    textView.setText(permissionTip);
  }


}
