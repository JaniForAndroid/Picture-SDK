package com.namibox.commonlib.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.othershe.nicedialog.NiceDialog;

/**
 * Created by sunha on 2017/11/2 0002.
 */

public class NamiboxNiceDialog extends NiceDialog {

  private static final String TAG = "MyNiceDialog";

  private int gravity = -1;

  public interface CancelListener {

    void onCancel();
  }

  private CancelListener cancelListener;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    if (savedInstanceState != null) {
      dismissAllowingStateLoss();
    }

  }

  @Override
  public void onStart() {
    super.onStart();
    Window window = this.getDialog().getWindow();
    if (window != null) {
      LayoutParams lp = window.getAttributes();
      if (gravity != -1) {
        lp.gravity = gravity;
      }
      window.setAttributes(lp);
    }
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    super.onCancel(dialog);
    if (cancelListener != null) {
      cancelListener.onCancel();
    }
  }

  public static NamiboxNiceDialog init() {
    return new NamiboxNiceDialog();
  }

  public void setCancelListener(CancelListener cancelListener) {
    this.cancelListener = cancelListener;
  }

  public NamiboxNiceDialog setGravity(int gravity) {
    this.gravity = gravity;
    return this;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }
}
