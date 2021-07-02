package com.namibox.commonlib.dialog;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.START;

import android.animation.ValueAnimator;
import android.arch.lifecycle.LifecycleOwner;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.picsdk.R;
import com.namibox.commonlib.dialog.NamiboxNiceDialog.CancelListener;
import com.namibox.commonlib.fragment.AbsFragment;
import com.namibox.commonlib.fragment.AbsWebViewFragment;
import com.namibox.commonlib.fragment.AbsWebViewFragment.PageListener;
import com.namibox.commonlib.model.MemberAlertDialogData;
import com.namibox.tools.GlideUtil;
import com.namibox.tools.TextViewUtil;
import com.namibox.util.Utils;
import com.othershe.nicedialog.BaseNiceDialog;
import com.othershe.nicedialog.ViewConvertListener;
import com.othershe.nicedialog.ViewHolder;
import java.util.ArrayList;
import java.util.List;

/**
 * Create time: 2017/9/27.
 */

public class DialogUtil {

  //普通对话框1按钮
  public static void showButtonDialog(AppCompatActivity activity, final String title,
      final CharSequence content,
      final String action1, final OnClickListener listener1, final OnClickListener cancelListener) {
    showButtonDialog(activity, title, content, action1, listener1,
        null, null, null, null, cancelListener);
  }

  //普通对话框2按钮
  public static void showButtonDialog(AppCompatActivity activity, final String title,
      final CharSequence content,
      final String action1, final OnClickListener listener1,
      final String action2, final OnClickListener listener2, final OnClickListener cancelListener) {
    showButtonDialog(activity, title, content, action1, listener1,
        null, null, action2, listener2, cancelListener);
  }

  public static class Action {

    public OnClickListener listener;
    public String action;

  }

  //普通对话框3按钮，内容居中
  public static void showButtonDialog(AppCompatActivity activity, String title,
      CharSequence content,
      String action1, OnClickListener listener1, String action2, OnClickListener listener2,
      String action3, OnClickListener listener3, OnClickListener cancelListener) {
    showButtonDialog(activity, title, content, action1, listener1, action2, listener2, action3,
        listener3, cancelListener, CENTER);
  }

  //特殊对话框1按钮，内容居中
  public static void showExtraButtonDialog(AppCompatActivity activity, String title, CharSequence content,
                                           String action1, OnClickListener listener1, String action2, OnClickListener listener2, OnClickListener cancelListener) {
    showExtraButtonDialog(activity, title, content, action1, listener1, action2, listener2, cancelListener, CENTER);
  }

  //内容居左
  public static void showButtonDialog2(AppCompatActivity activity, String title,
      CharSequence content,
      String action1, OnClickListener listener1, String action2, OnClickListener listener2,
      String action3, OnClickListener listener3, OnClickListener cancelListener) {
    showButtonDialog(activity, title, content, action1, listener1, action2, listener2, action3,
        listener3, cancelListener, START);
  }

  public static NamiboxNiceDialog showButtonsDialog(final AppCompatActivity activity,
      final String title,
      final CharSequence content,
      final List<Action> actions) {
    return showButtonsDialog(activity, title, content, true, actions, null);
  }

  public static NamiboxNiceDialog showButtonsDialog(final AppCompatActivity activity,
      final String title,
      final CharSequence content, boolean isNeedDefaultAnim,
      final List<Action> actions, final OnDialogCloseListener closeListener) {

    NamiboxNiceDialog dialog = (NamiboxNiceDialog) NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_with_buttons)
        .setConvertListener(new ViewConvertListener() {
          @Override
          protected void convertView(ViewHolder holder, final BaseNiceDialog baseNiceDialog) {
            TextView tvTitle = holder.getView(R.id.tv_title);
            TextView tvContent = holder.getView(R.id.tv_content);
            final ImageView ivClose = holder.getView(R.id.iv_close);
            RecyclerView recyclerView = holder.getView(R.id.rv);
            if (!TextUtils.isEmpty(title)) {
              tvTitle.setText(title);
            }
            if (!TextUtils.isEmpty(content)) {
              tvContent.setText(content);
            } else {
              tvContent.setVisibility(View.GONE);
            }

            ivClose.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                baseNiceDialog.dismiss();
                if (closeListener != null) {
                  closeListener.onClose();
                }
              }
            });

            class ViewHolder extends RecyclerView.ViewHolder {

              public ViewHolder(View itemView) {
                super(itemView);
              }
            }
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.setAdapter(new Adapter<ViewHolder>() {
              @NonNull
              @Override
              public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View rootView = LayoutInflater.from(activity)
                    .inflate(R.layout.rv_item_button, parent, false);
                return new ViewHolder(rootView);
              }

              @Override
              public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
                ((Button) holder.itemView).setText(actions.get(position).action);
                holder.itemView.setOnClickListener(new OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    baseNiceDialog.dismissAllowingStateLoss();
                    if (actions.get(position).listener != null) {
                      actions.get(position).listener.onClick(v);
                    }
                  }
                });
              }

              @Override
              public int getItemCount() {
                return actions == null ? 0 : actions.size();
              }

            });
          }
        })
        .setOutCancel(false)
        .setWidth(activity.getResources().getInteger(R.integer.dialog_width));
    if (isNeedDefaultAnim) {
      dialog.setAnimStyle(R.style.DialogEnterExitAnimation);
    }

    return (NamiboxNiceDialog) dialog.show(activity.getSupportFragmentManager());
  }

  public static NamiboxNiceDialog getCommonDialog(final AppCompatActivity activity,
      final String title,
      final CharSequence content,
      final String action1, final OnClickListener listener1, final String action2,
      final OnClickListener listener2,
      final String action3, final OnClickListener listener3, final OnClickListener cancelListener) {
    return (NamiboxNiceDialog) NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_with_button)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            TextView titleview = holder.getView(R.id.title);
            TextView contentview = holder.getView(R.id.content);
            contentview.setHighlightColor(Color.TRANSPARENT);
            contentview.setMovementMethod(LinkMovementMethod.getInstance());
            LinearLayout horizontalBtnLayout = holder.getView(R.id.horizontalBtnLayout);
            LinearLayout verticalBtnLayout = holder.getView(R.id.verticalBtnLayout);
            ImageView closeImg = holder.getView(R.id.dialog_close);
            final List<Action> actions = new ArrayList<>();
            if (!TextUtils.isEmpty(action1)) {
              Action action = new Action();
              action.action = action1;
              action.listener = listener1;
              actions.add(action);
            }
            if (!TextUtils.isEmpty(action2)) {
              Action action = new Action();
              action.action = action2;
              action.listener = listener2;
              actions.add(action);
            }
            if (!TextUtils.isEmpty(action3)) {
              Action action = new Action();
              action.action = action3;
              action.listener = listener3;
              actions.add(action);
            }
            if (cancelListener != null) {
              closeImg.setVisibility(View.VISIBLE);
              closeImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  dialog.dismissAllowingStateLoss();
                  cancelListener.onClick(v);
                }
              });
            }
            if (actions.size() == 2) {
              horizontalBtnLayout.setVisibility(View.VISIBLE);
              verticalBtnLayout.setVisibility(View.GONE);
              holder.getView(R.id.iv_v).setVisibility(View.GONE);
              holder.getView(R.id.iv_h).setVisibility(View.VISIBLE);
              traversalButton(dialog, horizontalBtnLayout, actions);
            } else {
              horizontalBtnLayout.setVisibility(View.GONE);
              verticalBtnLayout.setVisibility(View.VISIBLE);
              holder.getView(R.id.iv_v).setVisibility(View.VISIBLE);
              holder.getView(R.id.iv_h).setVisibility(View.GONE);
              traversalButton(dialog, verticalBtnLayout, actions);
            }
            titleview.setText(title);
            contentview.setText(content);
          }
        })
        .setOutCancel(false)
        .setWidth(activity.getResources().getInteger(R.integer.dialog_width))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());
  }

  //普通对话框3按钮
  private static void showButtonDialog(final AppCompatActivity activity, final String title,
      final CharSequence content,
      final String action1, final OnClickListener listener1, final String action2,
      final OnClickListener listener2,
      final String action3, final OnClickListener listener3, final OnClickListener cancelListener,
      final int contentGravity) {

    NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_with_button)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            TextView titleview = holder.getView(R.id.title);
            TextView contentview = holder.getView(R.id.content);
            contentview.setHighlightColor(Color.TRANSPARENT);
            contentview.setMovementMethod(LinkMovementMethod.getInstance());
//            Button btn1 = holder.getView(R.id.btn1);
//            Button btn2 = holder.getView(R.id.btn2);
//            Button btn3 = holder.getView(R.id.btn3);
//            Button btnHorizontal1 = holder.getView(R.id.btnHorizontal1);
//            Button btnHorizontal2 = holder.getView(R.id.btnHorizontal2);
            LinearLayout horizontalBtnLayout = holder.getView(R.id.horizontalBtnLayout);
            LinearLayout verticalBtnLayout = holder.getView(R.id.verticalBtnLayout);
            ImageView closeImg = holder.getView(R.id.dialog_close);
            //OnClickListener cancelListener = null;
            //boolean hasCancel = false;
            final List<Action> actions = new ArrayList<>();
            if (!TextUtils.isEmpty(action1)) {
              //不需要对取消按钮文字做过滤处理 暂时注释 后面不需要改动后删除 下同
//              if (action1.equals("取消")) {
//                cancelListener = listener1;
//                hasCancel = true;
//              } else {
//
//              }
              Action action = new Action();
              action.action = action1;
              action.listener = listener1;
              actions.add(action);
            }
            if (!TextUtils.isEmpty(action2)) {
//              if (action2.equals("取消")) {
//                cancelListener = listener2;
//                hasCancel = true;
//              } else {
//
//              }
              Action action = new Action();
              action.action = action2;
              action.listener = listener2;
              actions.add(action);
            }
            if (!TextUtils.isEmpty(action3)) {
//              if (action3.equals("取消")) {
//                cancelListener = listener3;
//                hasCancel = true;
//              } else {
//
//              }
              Action action = new Action();
              action.action = action3;
              action.listener = listener3;
              actions.add(action);
            }
            if (cancelListener != null) {
              closeImg.setVisibility(View.VISIBLE);
              //final OnClickListener finalCancelListener = cancelListener;
              closeImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  dialog.dismissAllowingStateLoss();
                  cancelListener.onClick(v);
                }
              });
            }
//            if (hasCancel) {
//
//            }

            if (actions.size() == 2) {
              horizontalBtnLayout.setVisibility(View.VISIBLE);
              verticalBtnLayout.setVisibility(View.GONE);
              holder.getView(R.id.iv_v).setVisibility(View.GONE);
              holder.getView(R.id.iv_h).setVisibility(View.VISIBLE);
              traversalButton(dialog, horizontalBtnLayout, actions);
            } else {
              horizontalBtnLayout.setVisibility(View.GONE);
              verticalBtnLayout.setVisibility(View.VISIBLE);
              holder.getView(R.id.iv_v).setVisibility(View.VISIBLE);
              holder.getView(R.id.iv_h).setVisibility(View.GONE);
              traversalButton(dialog, verticalBtnLayout, actions);
            }
            titleview.setText(title);
            contentview.setText(content);
          }
        })
        .setOutCancel(false)
        .setWidth(activity.getResources().getInteger(R.integer.dialog_width))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());

  }

  //特殊对话框1按钮
  private static void showExtraButtonDialog(final AppCompatActivity activity, final String title,
                                            final CharSequence content,
                                            final String action1, final OnClickListener listener1, final String action2,
                                            final OnClickListener listener2,
                                            final OnClickListener cancelListener,
                                            final int contentGravity) {

    NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_with_button_extra)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            TextView titleview = holder.getView(R.id.title);
            TextView contentview = holder.getView(R.id.content);
            contentview.setHighlightColor(Color.TRANSPARENT);
            contentview.setMovementMethod(LinkMovementMethod.getInstance());
            ImageView closeImg = holder.getView(R.id.dialog_close);

            Button bt1 = holder.getView(R.id.btn1);
            bt1.setText(action1);
            bt1.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                dialog.dismissAllowingStateLoss();
                if (listener1 != null) {
                  listener1.onClick(v);
                }
              }
            });

            Button bt2 = holder.getView(R.id.btn2);
            bt2.setText(action2);
            bt2.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                dialog.dismissAllowingStateLoss();
                if (listener2 != null) {
                  listener2.onClick(v);
                }
              }
            });

            if (cancelListener != null) {
              closeImg.setVisibility(View.VISIBLE);
              //final OnClickListener finalCancelListener = cancelListener;
              closeImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  dialog.dismissAllowingStateLoss();
                  cancelListener.onClick(v);
                }
              });
            }

            titleview.setText(title);
            contentview.setText(content);
          }
        })
        .setOutCancel(false)
//            .setWidth(activity.getResources().getInteger(R.integer.dialog_width))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());

  }

  /**
   * 隐私弹框专用
   *
   * @param activity 必须是AppCompatActivity
   * @param title 标题
   * @param content 内容
   * @param targetContent 高显及点击区域内容
   * @param privacyListener 隐私弹框点击监听
   * @param action1 按钮1
   * @param listener1 按钮1的点击监听
   * @param action2 按钮2
   * @param listener2 按钮2 的点击监听
   * @param exit 退出文案
   * @param exitListener 退出文案的点击监听
   * @param action3 按钮3
   * @param listener3 按钮3的点击监听
   * @param cancelListener 对话框关闭的点击事件
   * @return 对话框实列
   */
  public static NamiboxNiceDialog showPrivacyDialog(final AppCompatActivity activity,
      final String title,
      final String content, final String targetContent, final OnClickListener privacyListener,
      final String action1, final OnClickListener listener1, final String action2,
      final OnClickListener listener2, final String exit, final OnClickListener exitListener,
      final String action3, final OnClickListener listener3, final OnClickListener cancelListener) {

    return (NamiboxNiceDialog) NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_with_button_privacy)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            TextView titleview = holder.getView(R.id.title);
            TextView cancelExit = holder.getView(R.id.cancel_exit);
            TextView contentview = holder.getView(R.id.content);
            contentview.setHighlightColor(Color.TRANSPARENT);
            contentview.setMovementMethod(LinkMovementMethod.getInstance());
            LinearLayout horizontalBtnLayout = holder.getView(R.id.horizontalBtnLayout);
            ImageView closeImg = holder.getView(R.id.dialog_close);
            final List<Action> actions = new ArrayList<>();
            if (!TextUtils.isEmpty(action1)) {
              Action action = new Action();
              action.action = action1;
              action.listener = listener1;
              actions.add(action);
            }
            if (!TextUtils.isEmpty(action2)) {
              Action action = new Action();
              action.action = action2;
              action.listener = listener2;
              actions.add(action);
            }
            if (!TextUtils.isEmpty(action3)) {
              Action action = new Action();
              action.action = action3;
              action.listener = listener3;
              actions.add(action);
            }
            if (cancelListener != null) {
              closeImg.setVisibility(View.VISIBLE);
              closeImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  dialog.dismissAllowingStateLoss();
                  cancelListener.onClick(v);
                }
              });
            }
            if (!TextUtils.isEmpty(exit)) {
              cancelExit.setVisibility(View.VISIBLE);
              cancelExit.setText(exit);
              cancelExit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  if (exitListener != null) {
                    exitListener.onClick(v);
                  }
                  dialog.dismissAllowingStateLoss();
                }
              });
            } else {
              cancelExit.setVisibility(View.GONE);
            }
            horizontalBtnLayout.setVisibility(View.VISIBLE);
            traversalButton(dialog, horizontalBtnLayout, actions);
            titleview.setText(title);
            try {
              int startIndex = content.indexOf(targetContent);
              int endIndex = startIndex + targetContent.length();
              SpannableStringBuilder spannableString = new SpannableStringBuilder(content);
              spannableString.setSpan(new ForegroundColorSpan(0xff00b9ff), startIndex, endIndex,
                  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
              spannableString.setSpan(new ClickableSpan() {
                                        @Override
                                        public void onClick(@NonNull View widget) {
                                          privacyListener.onClick(widget);
                                        }

                                        @Override
                                        public void updateDrawState(@NonNull TextPaint ds) {
                                          super.updateDrawState(ds);
                                          ds.setUnderlineText(false);
                                        }
                                      }, startIndex, endIndex,
                  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
              contentview.setText(spannableString);
            } catch (Exception e) {
              e.printStackTrace();
              contentview.setText(content);
              contentview.setOnClickListener(privacyListener);
            }
          }
        })
        .setOutCancel(false)
        .setWidth(activity.getResources().getInteger(R.integer.dialog_width))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());

  }


  public static void showOttDialog(final AppCompatActivity activity, final String title,
      final CharSequence content,
      final String action1, final OnClickListener listener1, final String action2,
      final OnClickListener listener2,
      final String action3, final OnClickListener listener3, final OnClickListener cancelListener) {
    NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_ott_with_button)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            TextView titleview = holder.getView(R.id.title);
            TextView contentview = holder.getView(R.id.content);
            contentview.setHighlightColor(Color.TRANSPARENT);
            contentview.setMovementMethod(LinkMovementMethod.getInstance());
            LinearLayout horizontalBtnLayout = holder.getView(R.id.horizontalBtnLayout);
            LinearLayout verticalBtnLayout = holder.getView(R.id.verticalBtnLayout);
            ImageView closeImg = holder.getView(R.id.dialog_close);
            final List<Action> actions = new ArrayList<>();
            if (!TextUtils.isEmpty(action1)) {
              Action action = new Action();
              action.action = action1;
              action.listener = listener1;
              actions.add(action);
            }
            if (!TextUtils.isEmpty(action2)) {
              Action action = new Action();
              action.action = action2;
              action.listener = listener2;
              actions.add(action);
            }
            if (!TextUtils.isEmpty(action3)) {
              Action action = new Action();
              action.action = action3;
              action.listener = listener3;
              actions.add(action);
            }
            if (cancelListener != null) {
              closeImg.setVisibility(View.VISIBLE);
              closeImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  dialog.dismissAllowingStateLoss();
                  cancelListener.onClick(v);
                }
              });
            }
            if (actions.size() == 2) {
              horizontalBtnLayout.setVisibility(View.VISIBLE);
              verticalBtnLayout.setVisibility(View.GONE);
              traversalButton(dialog, horizontalBtnLayout, actions);
            } else {
              horizontalBtnLayout.setVisibility(View.GONE);
              verticalBtnLayout.setVisibility(View.VISIBLE);
              traversalButton(dialog, verticalBtnLayout, actions);
            }
            titleview.setText(title);
            contentview.setText(content);
          }
        })
        .setOutCancel(false)
        .setMargin(0)
        .setHeight(Utils.getScreenHeightDp(activity))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());

  }

  public static NamiboxNiceDialog showEnvelopeDialog(final AppCompatActivity activity,
      final String title, final String content,
      final Action action, final OnClickListener closeListener) {
    int layoutId;
    if (action != null) {
      layoutId = R.layout.dialog_with_envelope_1;
    } else {
      layoutId = R.layout.dialog_with_envelope_2;
    }
    return (NamiboxNiceDialog) NamiboxNiceDialog.init()
        .setLayoutId(layoutId)
        .setConvertListener(new ViewConvertListener() {
          @Override
          protected void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            TextView tv_title = holder.getView(R.id.tv_title);
            TextView tv_content = holder.getView(R.id.tv_content);
            ImageView iv_close = holder.getView(R.id.iv_close);
            if (!TextUtils.isEmpty(title)) {
              tv_title.setText(title);
            } else {
              tv_title.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(content)) {
              tv_content.setText(content);
            } else {
              tv_content.setVisibility(View.GONE);
            }
            if (action != null) {
              Button button = holder.getView(R.id.button);
              button.setText(action.action);
              button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  action.listener.onClick(v);
                  if (closeListener != null) {
                    closeListener.onClick(v);
                  }
                  dialog.dismissAllowingStateLoss();
                }
              });
            }
            iv_close.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                if (closeListener != null) {
                  closeListener.onClick(v);
                }
                dialog.dismissAllowingStateLoss();
              }
            });
          }
        })
        .setOutCancel(false)
        .setWidth(activity.getResources().getInteger(R.integer.dialog_width))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());
  }

  public static NamiboxNiceDialog showMemberDialog(final AppCompatActivity activity,
      final MemberAlertDialogData data, final Action action) {
    return (NamiboxNiceDialog) NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_with_member)
        .setConvertListener(new ViewConvertListener() {
          @Override
          protected void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            int radius = Utils.dp2px(activity, 12);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.parseColor(data.title_bg_color));
            gd.setCornerRadii(new float[]{radius, radius, radius, radius, 0, 0, 0, 0});
            holder.getView(R.id.ll_title).setBackground(gd);
            TextView tvTitle = holder.getView(R.id.tv_title);
            RecyclerView recyclerView = holder.getView(R.id.recycler_view);
            ImageView ivRule = holder.getView(R.id.iv_rule_icon);
            if (!TextUtils.isEmpty(data.title)) {
              TextViewUtil.handleHtmlJson(tvTitle, data.title);
            }
            if (data.rule_msg != null && data.rule_msg.size() > 0) {

              class ViewHolder extends RecyclerView.ViewHolder {

                TextView tvNumber, tvContent;

                public ViewHolder(View itemView) {
                  super(itemView);
                  tvNumber = itemView.findViewById(R.id.tv_number);
                  tvContent = itemView.findViewById(R.id.tv_content);
                }
              }
              class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

                @NonNull
                @Override
                public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                  View view = LayoutInflater.from(parent.getContext())
                      .inflate(R.layout.layout_item_member_dialog, parent, false);
                  return new ViewHolder(view);
                }

                @Override
                public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                  holder.tvNumber.setText(position + 1 + ".");
                  if (!TextUtils.isEmpty(data.rule_msg.get(position))) {
                    holder.tvContent.setText(data.rule_msg.get(position));
                  }
                }

                @Override
                public int getItemCount() {
                  return data.rule_msg.size();
                }
              }
              recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
              recyclerView.setFocusableInTouchMode(false);
              recyclerView.setAdapter(new MyAdapter());
            }

            if (!TextUtils.isEmpty(data.rule_icon)) {
              GlideUtil.loadImage(activity, data.rule_icon, ivRule);
            }
            holder.getView(R.id.iv_close).setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                dialog.dismissAllowingStateLoss();
              }
            });
          }
        })
        .setOutCancel(false)
        .setWidth(activity.getResources().getInteger(R.integer.dialog_width))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());
  }

  public static NamiboxNiceDialog showImageDialog(final AppCompatActivity activity,
      final Drawable headImage,
      final String title, final String content, final Drawable contentImage,
      final Action action, final Action action1, final Action action2,
      final OnClickListener closeListener) {
    return (NamiboxNiceDialog) NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_with_image)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            TextView tv_title = holder.getView(R.id.tv_title);
            TextView tv_content = holder.getView(R.id.tv_content);
            ImageView iv_content = holder.getView(R.id.iv_content);
            Button button = holder.getView(R.id.button);
            Button button1 = holder.getView(R.id.button1);
            Button button2 = holder.getView(R.id.button2);
            ImageView iv_close = holder.getView(R.id.iv_close);
            if (!TextUtils.isEmpty(title)) {
              tv_title.setText(title);
            } else {
              tv_title.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(content)) {
              tv_content.setText(content);
            } else {
              tv_content.setVisibility(View.GONE);
            }
            if (contentImage != null) {
              iv_content.setImageDrawable(contentImage);
            } else {
              iv_content.setVisibility(View.GONE);
            }
            if (action != null) {
              button1.setVisibility(View.GONE);
              button2.setVisibility(View.GONE);
              button.setText(action.action);
              button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  action.listener.onClick(v);
                  if (closeListener != null) {
                    closeListener.onClick(v);
                  }
                  dialog.dismissAllowingStateLoss();
                }
              });
            } else {
              button.setVisibility(View.GONE);
            }
            if (action1 != null && action2 != null) {
              button.setVisibility(View.GONE);
              button1.setText(action1.action);
              button1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  action1.listener.onClick(v);
                  if (closeListener != null) {
                    closeListener.onClick(v);
                  }
                  dialog.dismissAllowingStateLoss();
                }
              });
              button2.setText(action2.action);
              button2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  action2.listener.onClick(v);
                  if (closeListener != null) {
                    closeListener.onClick(v);
                  }
                  dialog.dismissAllowingStateLoss();
                }
              });
            } else {
              button1.setVisibility(View.GONE);
              button2.setVisibility(View.GONE);
            }
            iv_close.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                if (closeListener != null) {
                  closeListener.onClick(v);
                }
                dialog.dismissAllowingStateLoss();
              }
            });
          }
        })
        .setOutCancel(false)
        .setWidth(activity.getResources().getInteger(R.integer.dialog_width))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());

  }

  public static NamiboxNiceDialog showBigImageDialog(final AppCompatActivity activity,
      final Object largeImage, final Action action, final OnClickListener closeListener) {
    return (NamiboxNiceDialog) NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_with_big_image)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            ImageView image_large = holder.getView(R.id.image_large);
            ImageView close_large = holder.getView(R.id.close_large);
            GlideUtil.loadRoundedCornersImage(activity, image_large, largeImage,
                Utils.dp2px(activity, 6));
            if (action != null) {
              image_large.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  action.listener.onClick(v);
                  dialog.dismissAllowingStateLoss();
                  if (closeListener != null) {
                    closeListener.onClick(v);
                  }
                }
              });
            }
            close_large.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                dialog.dismissAllowingStateLoss();
                if (closeListener != null) {
                  v.setTag(true);
                  closeListener.onClick(v);
                }
              }
            });
          }
        })
        .setOutCancel(false)
        .setDimAmount(0.2f)
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());
  }

  public static NamiboxNiceDialog showQrImageDialog(final AppCompatActivity activity,
      final Object largeImage, final OnClickListener closeListener) {
    return (NamiboxNiceDialog) NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_with_qr_image)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            ImageView image_large = holder.getView(R.id.image_large);
            View layout = holder.getView(R.id.image_qr_layout);
            GlideUtil.loadImage(activity, largeImage, image_large);
            layout.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                dialog.dismissAllowingStateLoss();
                if (closeListener != null) {
                  closeListener.onClick(v);
                }
              }
            });
          }
        })
        .setOutCancel(true)
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());
  }

  public static NamiboxNiceDialog showSmallImageDialog(final AppCompatActivity activity,
      final Drawable bgDrawable,
      final Drawable smallImage, final String title, final String content,
      final Action action, final OnClickListener closeListener) {
    NamiboxNiceDialog dialog = NamiboxNiceDialog.init();
    dialog.setGravity(Gravity.TOP)
        .setLayoutId(R.layout.layout_push_small)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            View convertView = holder.getConvertView();
            View pushCardView = holder.getView(R.id.push_card);
            TextView pushTitleView = holder.getView(R.id.push_title);
            TextView pushContentView = holder.getView(R.id.push_content);
            ImageView pushImageSmall = holder.getView(R.id.push_image_small);
            RelativeLayout pushImageBg = holder.getView(R.id.push_image_bg);
            ImageView pushCloseSmall = holder.getView(R.id.push_close_small);
            if (bgDrawable != null) {
              pushImageBg.setBackground(bgDrawable);
            }
            if (!TextUtils.isEmpty(title)) {
              pushTitleView.setText(title);
            } else {
              pushTitleView.setVisibility(View.INVISIBLE);
            }
            if (!TextUtils.isEmpty(content)) {
              pushContentView.setText(content);
            } else {
              pushContentView.setVisibility(View.INVISIBLE);
            }
            pushImageSmall.setBackground(smallImage);
            if (action != null) {
              pushCardView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  action.listener.onClick(v);
                  if (closeListener != null) {
                    closeListener.onClick(v);
                  }
                  dialog.dismissAllowingStateLoss();
                }
              });
            }
            pushCloseSmall.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                if (closeListener != null) {
                  closeListener.onClick(v);
                }
                dialog.dismissAllowingStateLoss();
              }
            });
          }
        })
        .setOutCancel(true)
        //.setHeight(Utils.getScreenWidth(activity)[1])
        .setMargin(16)
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());
    dialog.setCancelListener(new CancelListener() {
      @Override
      public void onCancel() {
        if (closeListener != null) {
          closeListener.onClick(null);
        }
      }
    });
    return dialog;
  }

  //两行最多六个按钮
  public static BaseNiceDialog showTwoLineSixBtnDialog(final AppCompatActivity activity,
      final String title,
      final String content, final String[] actions, final OnClickListener[] listeners,
      final boolean hasCancel, final String headerImgResId, final int defaultResId) {

    return NamiboxNiceDialog.init()
        .setLayoutId(R.layout.layout_dialog_two_line_six_btn)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            TextView titleview = holder.getView(R.id.title);
            TextView contentview = holder.getView(R.id.content);
            ImageView headerImage = holder.getView(R.id.header);
            ImageView closeImg = holder.getView(R.id.dialog_close);
            GlideUtil
                .loadCircleImage(dialog.getActivity(), headerImage, headerImgResId, defaultResId,
                    defaultResId);
            titleview.setText(title);
            contentview.setText(content);
            closeImg.setVisibility(View.VISIBLE);
            if (hasCancel) {
              closeImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  dialog.dismissAllowingStateLoss();
                }
              });
            } else {
              closeImg.setVisibility(View.GONE);

            }
            TextView[] textViews = new TextView[6];
            textViews[0] = holder.getView(R.id.text1);
            textViews[1] = holder.getView(R.id.text2);
            textViews[2] = holder.getView(R.id.text3);
            textViews[3] = holder.getView(R.id.text4);
            textViews[4] = holder.getView(R.id.text5);
            textViews[5] = holder.getView(R.id.text6);
            for (int i = 0; i < actions.length && i < 6; i++) {
              if (TextUtils.isEmpty(actions[i])) {
                textViews[i].setVisibility(View.GONE);
              } else {
                textViews[i].setVisibility(View.VISIBLE);
                textViews[i].setText(actions[i]);
                final int finalI = i;
                textViews[i].setOnClickListener(new OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    dialog.dismissAllowingStateLoss();
                    if (listeners == null) {
                      return;
                    }
                    if (listeners[finalI] != null) {
                      listeners[finalI].onClick(v);
                    }

                  }
                });
              }
            }

          }
        })
        .setOutCancel(false)
        .setWidth(activity.getResources().getInteger(R.integer.dialog_width))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());


  }


  private static void traversalButton(final BaseNiceDialog dialog, LinearLayout verticalBtnLayout,
      final List<Action> actions) {
    for (int i = 0; i < actions.size(); i++) {
      Button button = (Button) verticalBtnLayout.getChildAt(i);
      button.setText(actions.get(i).action);
      button.setVisibility(View.VISIBLE);
      if (i == 0) {
        button.requestFocus();
      }
      final int finalI = i;
      button.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          dialog.dismissAllowingStateLoss();
          if (actions.get(finalI).listener != null) {
            actions.get(finalI).listener.onClick(v);
          }
        }
      });
    }
  }

  public interface Callback {

    boolean onTextAccept(CharSequence text);
  }

  //带编辑框对话框
  public static void showEditDialog(final AppCompatActivity activity, final String title,
      final String text, final String editText,
      final String action1, final Callback listener1, final String action2,
      final Callback listener2) {
    NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_with_edit)
        .setConvertListener(new ViewConvertListener() {
          @Override
          protected void convertView(ViewHolder viewHolder, final BaseNiceDialog dialog) {
            TextView titleView = viewHolder.getView(R.id.title);
            final EditText contentView = viewHolder.getView(R.id.content);
            TextView btn1 = viewHolder.getView(R.id.btn1);
            TextView btn2 = viewHolder.getView(R.id.btn2);
            titleView.setText(title);
            if (!TextUtils.isEmpty(text)) {
              contentView.setHint(text);
            }
            if (!TextUtils.isEmpty(editText)) {
              contentView.setText(editText);
              contentView.setSelection(editText.length());
            }
            contentView.postDelayed(new Runnable() {
              @Override
              public void run() {
                Utils.showKeyboard(dialog.getActivity(), contentView);
              }
            }, 150);

            btn1.setText(action1);
            btn2.setText(action2);
            btn1.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View view) {
                if (listener1 != null && !listener1.onTextAccept(contentView.getText())) {
                  return;
                }
                dialog.dismissAllowingStateLoss();
              }
            });
            btn2.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View view) {
                if (listener2 != null && !listener2.onTextAccept(contentView.getText())) {
                  return;
                }
                dialog.dismissAllowingStateLoss();
              }
            });
          }
        })
        .setOutCancel(false)
        .setWidth(activity.getResources().getInteger(R.integer.dialog_width))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());
  }

  //底部弹出的文字选项对话框
  public static void showOptionDialog(AppCompatActivity activity, final List<String> items,
      final List<OnClickListener> listeners) {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("number of items must > 0");
    }
    NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_text_options)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            TextView[] textViews = new TextView[5];
            textViews[0] = holder.getView(R.id.item1);
            textViews[1] = holder.getView(R.id.item2);
            textViews[2] = holder.getView(R.id.item3);
            textViews[3] = holder.getView(R.id.item4);
            textViews[4] = holder.getView(R.id.item5);
            for (int i = 0; i < items.size(); i++) {
              textViews[i].setVisibility(View.VISIBLE);
              textViews[i].setText(items.get(i));
              final int index = i;
              textViews[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                  dialog.dismissAllowingStateLoss();
                  if (listeners != null && listeners.size() > index
                      && listeners.get(index) != null) {
                    listeners.get(index).onClick(view);
                  }
                }
              });
            }
          }
        })
        .setShowBottom(true)
        .show(activity.getSupportFragmentManager());
  }

  public static class LoadingDialog extends BaseNiceDialog {

    private String text;
    private boolean showCancel;
    private TextView textView;
    private View closeView;
    private OnCancelListener onCancelListener;

    public static LoadingDialog newInstance(String text, boolean showCancel) {
      Bundle bundle = new Bundle();
      bundle.putString("text", text);
      bundle.putBoolean("showCancel", showCancel);
      LoadingDialog dialog = new LoadingDialog();
      dialog.setArguments(bundle);
      return dialog;
    }

    public void setText(String text) {
      if (textView == null) {
        return;
      }
      if (TextUtils.isEmpty(text)) {
        textView.setVisibility(View.GONE);
      } else {
        textView.setVisibility(View.VISIBLE);
        textView.setText(text);
      }
    }

    public LoadingDialog setCancelListener(OnCancelListener listener) {
      onCancelListener = listener;
      return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Bundle bundle = getArguments();
      if (bundle == null) {
        return;
      }
      text = bundle.getString("text");
      showCancel = bundle.getBoolean("showCancel");
    }

    @Override
    public void onStart() {
      super.onStart();
      if (onCancelListener != null) {
        getDialog().setOnCancelListener(onCancelListener);
      }
    }

    @Override
    public int intLayoutId() {
      return R.layout.dialog_simple_loading;
    }

    @Override
    public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
      textView = holder.getView(R.id.text);
      closeView = holder.getView(R.id.close);
      closeView.setVisibility(showCancel ? View.VISIBLE : View.GONE);
      closeView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          getDialog().cancel();
        }
      });
      setText(text);
    }
  }

  //加载对话框
  public static LoadingDialog showLoadingDialog(AppCompatActivity activity, final String text,
      boolean showCancel, OnCancelListener listener) {
    return (LoadingDialog) LoadingDialog.newInstance(text, showCancel)
        .setCancelListener(listener)
        .setOutCancel(false)
        .setWidth(-1)
        .setHeight(0)
        .setDimAmount(0)
        .show(activity.getSupportFragmentManager());
  }

  public static class ProgressDialog extends BaseNiceDialog {

    private String title;
    private String text;
    private String action;
    private TextView titleView;
    private TextView textView;
    private ProgressBar progressBar;
    private Button button;
    private OnClickListener onClickListener;

    public static ProgressDialog newInstance(String title, String text, String action) {
      Bundle bundle = new Bundle();
      bundle.putString("title", title);
      bundle.putString("text", text);
      bundle.putString("action", action);
      ProgressDialog dialog = new ProgressDialog();
      dialog.setArguments(bundle);
      return dialog;
    }

    public void setText(String text) {
      textView.setText(text);
    }

    public void setProgress(int progress) {
      if (progress < 0 || progress > 100) {
        return;
      }
      progressBar.setProgress(progress);
    }

    public ProgressDialog setAction(OnClickListener listener) {
      onClickListener = listener;
      return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Bundle bundle = getArguments();
      if (bundle == null) {
        return;
      }
      text = bundle.getString("text");
      title = bundle.getString("title");
      action = bundle.getString("action");
    }

    @Override
    public int intLayoutId() {
      return R.layout.dialog_with_progress;
    }

    @Override
    public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
      titleView = holder.getView(R.id.title);
      textView = holder.getView(R.id.text);
      progressBar = holder.getView(R.id.progressBar);
      button = holder.getView(R.id.button);
      View divider = holder.getView(R.id.divider);
      titleView.setText(title);
      setText(text);
      if (TextUtils.isEmpty(action)) {
        divider.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
      } else {
        button.setText(action);
      }
      button.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          dismissAllowingStateLoss();
          if (onClickListener != null) {
            onClickListener.onClick(view);
          }
        }
      });
      progressBar.setMax(100);
    }
  }

  public static ProgressDialog showProgressDialog(AppCompatActivity activity, String title,
      String text,
      String action, OnClickListener listener) {
    return (ProgressDialog) ProgressDialog.newInstance(title, text, action)
        .setAction(listener)
        .setOutCancel(false)
        .setWidth(activity.getResources().getInteger(R.integer.dialog_width))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());
  }

  public static class ProgressDialog1 extends BaseNiceDialog {

    private String title;
    private String text;
    private String action;
    private TextView tv_progress;
    private TextView textView;
    private ProgressBar progressBar;
    private ImageView button;
    private OnClickListener onClickListener;

    public static ProgressDialog1 newInstance(String title, String text, String action) {
      Bundle bundle = new Bundle();
      bundle.putString("title", title);
      bundle.putString("text", text);
      bundle.putString("action", action);
      ProgressDialog1 dialog = new ProgressDialog1();
      dialog.setArguments(bundle);
      return dialog;
    }

    public void setText(String text) {
      if (tv_progress != null)
        tv_progress.setText(text);
    }

    public void setProgress(int progress) {
      if (progress < 0 || progress > 100) {
        return;
      }
      if (progressBar != null)
        progressBar.setProgress(progress);
    }

    public ProgressDialog1 setAction(OnClickListener listener) {
      onClickListener = listener;
      return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Bundle bundle = getArguments();
      if (bundle == null) {
        return;
      }
      text = bundle.getString("text");
      title = bundle.getString("title");
      action = bundle.getString("action");
    }

    @Override
    public int intLayoutId() {
      return R.layout.dialog_with_progress1;
    }

    @Override
    public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
      tv_progress = holder.getView(R.id.tv_progress);
      textView = holder.getView(R.id.text);
      progressBar = holder.getView(R.id.progressBar);
      button = holder.getView(R.id.button);
      button.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          dismissAllowingStateLoss();
          if (onClickListener != null) {
            onClickListener.onClick(view);
          }
        }
      });
      progressBar.setMax(100);
    }
  }

  public static ProgressDialog1 showProgressDialog1(AppCompatActivity activity, String title,
                                                    String text,
                                                    String action, OnClickListener listener, int width, int height) {
    return (ProgressDialog1) ProgressDialog1.newInstance(title, text, action)
        .setAction(listener)
        .setOutCancel(false)
        .setWidth(width)
        .setHeight(height)
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());
  }

  public interface OnItemClickedListener {

    void onItemClicked(int index);
  }

  public interface OnDialogCloseListener {

    void onClose();
  }

  private static class ListAdapter extends RecyclerView.Adapter<ListViewHolder> {

    List<String> data;
    private int selectedPosition = -1;
    private int layoutRes;
    OnItemClickedListener listener;

    ListAdapter(List<String> data, OnItemClickedListener listener, int layoutRes) {
      this.data = data;
      this.listener = listener;
      this.layoutRes = layoutRes;
    }

    public int getSelectedPosition() {
      return selectedPosition;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
      final ListViewHolder viewHolder = new ListViewHolder(v);
      viewHolder.itemView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (listener != null) {
            listener.onItemClicked(viewHolder.position);
          } else {
            selectedPosition = viewHolder.position;
            notifyDataSetChanged();
          }
        }
      });
      return viewHolder;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
      String text = data.get(holder.getAdapterPosition());
      holder.textView.setText(text);
      if (holder.textViewCheck != null) {
        holder.textViewCheck.setVisibility(
            holder.getAdapterPosition() == selectedPosition ? View.VISIBLE : View.GONE);
      }
      holder.position = holder.getAdapterPosition();
    }

    @Override
    public int getItemCount() {
      return data.size();
    }
  }

  public static class ListViewHolder extends RecyclerView.ViewHolder {

    public TextView textView;
    public ImageView textViewCheck;
    public int position;

    public ListViewHolder(View itemView) {
      super(itemView);
      textView = itemView.findViewById(R.id.list_item);
      textViewCheck = itemView.findViewById(R.id.list_item_check);
    }
  }


  //单选文字列表对话框
  public static void showListDialog(AppCompatActivity activity, final List<String> items,
      final OnItemClickedListener listener) {
    NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_with_list)
        .setConvertListener(new ViewConvertListener() {
          @Override
          protected void convertView(ViewHolder viewHolder, final BaseNiceDialog baseNiceDialog) {
            ImageView closeView = viewHolder.getView(R.id.dialog_close);
            closeView.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                baseNiceDialog.dismissAllowingStateLoss();
              }
            });
            RecyclerView recyclerView = viewHolder.getView(R.id.dialog_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(baseNiceDialog.getContext()));
            OnItemClickedListener onItemClickedListener = new OnItemClickedListener() {
              @Override
              public void onItemClicked(int index) {
                listener.onItemClicked(index);
                baseNiceDialog.dismissAllowingStateLoss();
              }
            };
            recyclerView.setAdapter(
                new ListAdapter(items, onItemClickedListener, R.layout.layout_dialog_item));
          }
        })
        .setOutCancel(false)
        .setWidth(activity.getResources().getInteger(R.integer.dialog_width))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());
  }

  //底部弹出的单选带按钮对话框
  public static NamiboxNiceDialog showOptionDialog2(AppCompatActivity activity, final String title,
      final List<String> items,
      final String action1, final OnItemClickedListener listener1, final String action2,
      final OnItemClickedListener listener2) {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("number of items must > 0");
    }
    return (NamiboxNiceDialog) NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_list_options)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {
            TextView titleView = holder.getView(R.id.title);
            titleView.setText(title);
            RecyclerView recyclerView = holder.getView(R.id.dialog_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(dialog.getContext()));
            final ListAdapter adapter = new ListAdapter(items, null,
                R.layout.layout_list_dialog_item);
            recyclerView.setAdapter(adapter);
            TextView btn1 = holder.getView(R.id.btn1);
            TextView btn2 = holder.getView(R.id.btn2);
            btn1.setText(action1);
            btn2.setText(action2);
            btn1.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                dialog.dismissAllowingStateLoss();
                if (listener1 != null) {
                  listener1.onItemClicked(adapter.getSelectedPosition());
                }
              }
            });
            btn2.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                dialog.dismissAllowingStateLoss();
                if (listener2 != null) {
                  listener2.onItemClicked(adapter.getSelectedPosition());
                }
              }
            });
          }
        })
        .setShowBottom(true)
        .show(activity.getSupportFragmentManager());
  }

  public static void showWebViewDialog(final FragmentActivity activity, final String url, final boolean is_opacity) {
    final BaseNiceDialog dialog = NamiboxNiceDialog.init()
        .setLayoutId(R.layout.dialog_webview)
        .setConvertListener(new ViewConvertListener() {
          @Override
          protected void convertView(ViewHolder viewHolder, final BaseNiceDialog baseNiceDialog) {
            final ImageView closeView = viewHolder.getView(R.id.iv_close);
            closeView.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                baseNiceDialog.dismissAllowingStateLoss();
              }
            });
            final View frame = viewHolder.getView(R.id.frame);
            AbsWebViewFragment fragment = new AbsWebViewFragment();
            fragment.setMode(AbsFragment.MODE_FULLSCREEN);
            fragment.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
            fragment.setOriginUrl(url);
            fragment.setPageListener(new PageListener() {
              @Override
              public void onPageFinished() {
                if (!is_opacity) {
                  frame.setBackgroundColor(Color.TRANSPARENT);
                  closeView.setVisibility(View.GONE);
                }
              }

              @Override
              public void onPageLoadError() {

              }
            });
            fragment.setContentHeightCallBack(new AbsWebViewFragment.ContentHeightCallBack() {
              @Override
              public void contentHeight(int height) {
                ValueAnimator valueAnimator = ValueAnimator
                    .ofInt(frame.getHeight(), height + Utils.dp2px(activity, 20));
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                  @Override
                  public void onAnimationUpdate(ValueAnimator animation) {
                    frame.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                    frame.requestLayout();
                  }
                });
                valueAnimator.setDuration(150);
                valueAnimator.start();
              }
            });
            fragment.setCloseViewListener(new AbsFragment.CloseViewListener() {
              @Override
              public void closeSelf() {
                baseNiceDialog.dismissAllowingStateLoss();
              }
            });
            baseNiceDialog.getChildFragmentManager().beginTransaction().add(R.id.frame, fragment)
                .commit();
          }
        })
        .setOutCancel(false)
        .setWidth(Utils.isTablet(activity) ? 380 : 290)
        .setHeight(Utils.getScreenHeightDp(activity))
        .setAnimStyle(R.style.DialogEnterExitAnimation)
        .show(activity.getSupportFragmentManager());
  }

  //底部弹出的仿ios删除弹框
  public static NamiboxNiceDialog showBottomIOSDeleteDialog(LifecycleOwner obj, final OnClickListener deleteListener) {

    int layoutRes = R.layout.dialog_bottom_ios_layout;

    FragmentManager manager = null;
    if (obj instanceof Fragment) {
      manager = ((Fragment) obj).getChildFragmentManager();
    } else if (obj instanceof AppCompatActivity) {
      manager = ((AppCompatActivity) obj).getSupportFragmentManager();
    }
    if (manager == null) {
      return null;
    }

    return (NamiboxNiceDialog) NamiboxNiceDialog.init()
        .setGravity(Gravity.BOTTOM)
        .setLayoutId(layoutRes)
        .setConvertListener(new ViewConvertListener() {
          @Override
          public void convertView(ViewHolder holder, final BaseNiceDialog dialog) {

            TextView tv_delete = holder.getView(R.id.tv_delete);
            TextView tv_cancel = holder.getView(R.id.tv_cancel);

            tv_delete.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                dialog.dismissAllowingStateLoss();
                if (deleteListener != null) {
                  deleteListener.onClick(v);
                }
              }
            });
            tv_cancel.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                dialog.dismissAllowingStateLoss();
              }
            });
          }
        })
        .setShowBottom(true)
        .setOutCancel(false)
        .show(manager);
  }

}
