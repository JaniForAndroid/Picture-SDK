package com.namibox.commonlib.view.emojicon.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.picsdk.R;
import com.namibox.commonlib.view.emojicon.TimSmileUtils;
import com.namibox.commonlib.view.emojicon.domain.TimEmojicon;
import com.namibox.commonlib.view.emojicon.domain.TimEmojicon.Type;
import java.util.List;

public class EmojiconGridAdapter extends ArrayAdapter<TimEmojicon> {

  private Type emojiconType;


  public EmojiconGridAdapter(Context context, int textViewResourceId, List<TimEmojicon> objects,
      Type emojiconType) {
    super(context, textViewResourceId, objects);
    this.emojiconType = emojiconType;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      if (emojiconType == Type.BIG_EXPRESSION) {
        convertView = View.inflate(getContext(), R.layout.common_emoj_row_big_expression, null);
      } else {
        convertView = View.inflate(getContext(), R.layout.common_emoj_row_expression, null);
      }
    }

    ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_expression);
    TextView textView = (TextView) convertView.findViewById(R.id.tv_name);
    TimEmojicon emojicon = getItem(position);
    if (textView != null && emojicon.getName() != null) {
      textView.setText(emojicon.getName());
    }
    if (TimSmileUtils.DELETE_KEY.equals(emojicon.getEmojiText())) {
      imageView.setImageResource(R.drawable.common_emoj_delete_expression);
    } else {
      if (emojicon.getIcon() != 0) {
        imageView.setImageResource(emojicon.getIcon());
      } else if (emojicon.getIconPath() != null) {
        Glide.with(getContext())
            .load(emojicon.getIconPath())
            .apply(new RequestOptions().placeholder(R.drawable.common_emoj_smile_default))
            .into(imageView);
      } else if (emojicon.getIconSet() != null) {
        Glide.with(getContext())
            .load(emojicon.getIconSet().iconPng)
            .apply(new RequestOptions().placeholder(R.drawable.common_emoj_smile_default))
            .into(imageView);
      }
    }

    return convertView;
  }

}
