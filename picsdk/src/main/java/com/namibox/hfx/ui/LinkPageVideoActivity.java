package com.namibox.hfx.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import com.example.picsdk.R;
import com.namibox.commonlib.activity.AbsFunctionActivity;
import com.namibox.util.Utils;
import vn.tungdx.mediapicker.RxMediaPicker;
import vn.tungdx.mediapicker.VideoChooseListener;

/**
 * Created by sunha on 2015/10/24 0024.
 */
public class LinkPageVideoActivity extends AbsFunctionActivity {

  private String id;

  public static void open(Context context) {
    Intent intent = new Intent(context, LinkPageVideoActivity.class);
    context.startActivity(intent);
  }

  public static void open(Context context, String id) {
    Intent intent = new Intent(context, LinkPageVideoActivity.class);
    intent.putExtra("videoId", id);
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.hfx_activity_link_page_video);
    id = getIntent().getStringExtra("videoId");

    View tab_item_tips2 = findViewById(R.id.tab_item_tips2);
    View tab_item_text2 = findViewById(R.id.tab_item_text2);
    View back_btn = findViewById(R.id.back_btn);
    tab_item_tips2.setOnClickListener(this::onViewClick);
    tab_item_text2.setOnClickListener(this::onViewClick);
    back_btn.setOnClickListener(this::onViewClick);
  }

  public void onViewClick(View view) {
    int i = view.getId();
    if (i == R.id.tab_item_tips2 || i == R.id.tab_item_text2) {
      onCreateVideoShow(id);
    } else if (i == R.id.back_btn) {
      finish();
    }
  }


  private void onCreateVideoShow(String bookId) {
    if (!Utils.checkIsX86() && Utils.checkSupportV7a()) {
      final String videoId;
      if (TextUtils.isEmpty(bookId)) {
        videoId = "";
      } else {
        videoId = bookId;
      }
      RxMediaPicker.getInstance()
          .chooseVideo(this, new VideoChooseListener() {
            @Override
            public void onVideoChoose(Uri videoUri, String path) {
              CutVideoActivity
                  .openVideoActivity(LinkPageVideoActivity.this, videoUri, path, videoId);
              finish();
            }

            @Override
            public void onError(int status, String message) {
              if (status == VideoChooseListener.LESS_THAN_MIN) {
                toast("视频小于10秒,请拍摄大于10秒的视频");
              } else if (status == VideoChooseListener.PARSER_FAIL) {
                toast("解析视频失败,请重新选择或拍摄");
              } else if (status != VideoChooseListener.CANCEL) {
                toast("获取视频失败");
              }
            }
          });
    } else {
      showErrorDialog("检测到您的设备不支持本功能,我们正在抓紧解决这个问题", false);
    }
  }

}
