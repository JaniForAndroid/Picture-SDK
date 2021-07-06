package com.namibox.hfx.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog.Builder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.picsdk.R;
import com.namibox.hfx.utils.HfxFileUtil;
import com.namibox.hfx.utils.HfxUtil;
import com.namibox.util.NetworkUtil;
import com.namibox.util.Utils;

/**
 * Create time: 2015/8/21.
 */
public class CommitFragment extends Fragment {

  ImageView imageView;
  TextView titleView;
  TextView subtitleView;
  EditText introduceEdit;
  RecordActivity activity;
  String bookId;
  Button commitBtn;
  CheckBox classCheckBox;
  Button classubmitBtn;
  LinearLayout classLayout;

  public static CommitFragment newInstance(String bookId, String imageUrl, String title,
      String subtitle,
      String introduce) {
    Bundle args = new Bundle();
    args.putString("imageUrl", imageUrl);
    args.putString("bookid", bookId);
    args.putString("title", title);
    args.putString("subtitle", subtitle);
    args.putString("introduce", introduce);
    CommitFragment fragment = new CommitFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    activity = (RecordActivity) context;
    activity.setTitle(R.string.hfx_commit_work);
    activity.hideMenu();
  }

  @Override
  public void onDetach() {
    super.onDetach();
    activity = null;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.hfx_fragment_commit, container, false);
    initView(view);
    introduceEdit.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        saveIntroduce();
      }
    });

    return view;
  }

  private void initView(View view) {
    imageView = view.findViewById(R.id.book_image);
    titleView = view.findViewById(R.id.book_title);
    subtitleView = view.findViewById(R.id.book_subtitle);
    introduceEdit = view.findViewById(R.id.edit_introduce);
    commitBtn = view.findViewById(R.id.commit_btn);
    classCheckBox = view.findViewById(R.id.classCheckBox);
    classubmitBtn = view.findViewById(R.id.classubmitBtn);
    classLayout = view.findViewById(R.id.classLayout);

    commitBtn.setOnClickListener(v -> commit());
    classubmitBtn.setOnClickListener(v -> commit());
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Bundle args = getArguments();
    if (args != null) {
      String imageUrl = args.getString("imageUrl");
      String title = args.getString("title");
      String subtitle = args.getString("subtitle");
      String introduce = args.getString("introduce");
      bookId = args.getString("bookid");
      activity.classInfo = HfxUtil.getClassInfo(getActivity(), bookId);
      if (activity.classInfo != null && activity.classInfo.classCheck != 0) {
        commitBtn.setVisibility(View.GONE);
        classLayout.setVisibility(View.VISIBLE);
        classCheckBox.setChecked(activity.classInfo.classCheck > 0);
      } else {
        commitBtn.setVisibility(View.VISIBLE);
        classLayout.setVisibility(View.GONE);
      }
      RequestOptions options = new RequestOptions()
          .diskCacheStrategy(DiskCacheStrategy.NONE)
          .placeholder(R.drawable.hfx_default_icon)
          .error(R.drawable.hfx_default_icon);
      Glide.with(getActivity())
          .load(Utils.encodeString(imageUrl))
          .apply(options)
          .into(imageView);
      titleView.setText(title);
      subtitleView.setText(subtitle);
      if (!TextUtils.isEmpty(introduce)) {
        introduceEdit.setText(introduce);
      }
    }
  }

  private void showNetworkDialog() {
    new Builder(getActivity())
        .setTitle("提示")
        .setMessage(R.string.hfx_commit_network_message)
        .setPositiveButton("确定", new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            doCommit();
          }
        })
        .setNegativeButton("取消", null)
        .create().show();
  }

  void commit() {
    if (activity.classInfo != null && activity.classInfo.classCheck != 0) {
      if (classCheckBox.isChecked()) {
        activity.classInfo.classCheck = 1;
      } else {
        activity.classInfo.classCheck = -1;
      }
    }
    String introduce = introduceEdit.getText().toString();
    if (TextUtils.isEmpty(introduce)) {
      if (activity != null && !activity.isFinishing()) {
        activity.toast("请输入作品介绍");
      }
      return;
    }
    if (introduce.length() < 10) {
      if (activity != null && !activity.isFinishing()) {
        activity.toast("作品介绍字数太少");
      }
      return;
    }
    if (!NetworkUtil.isNetworkConnected(getActivity())) {
      if (activity != null && !activity.isFinishing()) {
        activity.toast(getString(R.string.common_network_none_tips));
      }
      return;
    }
    if (!NetworkUtil.isWiFi(getActivity())) {
      showNetworkDialog();
    } else {
      doCommit();
    }
  }

  private void doCommit() {
    String introduce = introduceEdit.getText().toString().replaceAll("[\\t\\n\\r]", "");
    RecordActivity activity = (RecordActivity) getActivity();
    activity.startCommit(introduce, HfxFileUtil.HUIBEN_WORK);
    Utils.hideKeyboard(activity, introduceEdit);
  }

  private void saveIntroduce() {
    String introduce = introduceEdit.getText().toString().replaceAll("[\\t\\n\\r]", "");
    Activity activity = getActivity();
    if (activity instanceof RecordActivity) {
      ((RecordActivity) activity).saveIntroduce(introduce);
    } else if (activity instanceof EvalActivity) {
      ((EvalActivity) activity).saveIntroduce(introduce);
    }
  }
}
