package com.namibox.hfx.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.picsdk.R;
import com.namibox.commonlib.activity.AbsFunctionActivity;

/**
 * Created by sunha on 2015/10/24 0024.
 */
public class LinkPageAudioActivity extends AbsFunctionActivity {

  private String id;

  public static void open(Context context, String id) {
    Intent intent = new Intent(context, LinkPageAudioActivity.class);
    intent.putExtra("audioId", id);
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.hfx_activity_link_page);
    id = getIntent().getStringExtra("audioId");
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
      StoryRecordActivity.openStoryRecord(this, id);
      finish();
    } else if (i == R.id.back_btn) {
      finish();

    }
  }
}
