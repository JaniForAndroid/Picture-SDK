package com.example.picsdk.music;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.picsdk.R;
import com.example.picsdk.learn.BookManager;
import com.namibox.commonlib.view.NBSoundView;
import com.namibox.greendao.entity.AudioInfo;
import java.util.List;
import me.drakeet.multitype.ItemViewBinder;

public class AudioHistoryViewBinder extends
    ItemViewBinder<AudioInfo, AudioHistoryViewBinder.AudioViewHolder> {

  private OnAudioClickListener onAudioClickListener;

  public AudioHistoryViewBinder(OnAudioClickListener onAudioClickListener) {
    this.onAudioClickListener = onAudioClickListener;
  }

  @NonNull
  @Override
  protected AudioViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater,
      @NonNull ViewGroup parent) {
    View itemView = inflater.inflate(R.layout.pic_item_audio_history, parent, false);
    return new AudioViewHolder(itemView);
  }

  @Override
  protected void onViewAttachedToWindow(@NonNull AudioViewHolder holder) {
  }

  @Override
  protected void onViewDetachedFromWindow(@NonNull AudioViewHolder holder) {
  }

  @Override
  protected void onBindViewHolder(@NonNull AudioViewHolder holder, @NonNull AudioInfo item) {
    Context context = holder.itemView.getContext();
    Drawable drawable = context.getResources().getDrawable(R.drawable.default_icon);
    holder.tv_title.setText(item.title);
    String index;
    if (item.index > 0 && item.index < 10) {
      index = "0" + item.index;
    } else {
      index = item.index + "";
    }
    holder.tv_tag.setText(index);
    updatePlayingState(holder, item);
    holder.itemView.setOnClickListener(v -> {
      if (onAudioClickListener != null) {
        onAudioClickListener.onAudioPlay(item);
      }
    });
  }

  @Override
  protected void onBindViewHolder(@NonNull AudioViewHolder holder, @NonNull AudioInfo item,
      @NonNull List<Object> payloads) {
    if (payloads.isEmpty()) {
      onBindViewHolder(holder, item);
    } else {
      updatePlayingState(holder, item);
    }
  }

  private void updatePlayingState(@NonNull AudioViewHolder holder, @NonNull AudioInfo item) {
    String currentAudioId = BookManager.getInstance().getCurrentAudio().audioId;
    if (TextUtils.equals(item.audioId, currentAudioId)) {
      holder.tv_title.setTextColor(Color.parseColor("#00B9FF"));
      holder.tv_tag.setTextColor(Color.parseColor("#00B9FF"));
      holder.nb_sound.setVisibility(View.VISIBLE);
      holder.nb_sound.setAnimator(true);
    } else {
      holder.tv_title.setTextColor(Color.parseColor("#333333"));
      holder.tv_tag.setTextColor(Color.parseColor("#333333"));
      holder.nb_sound.setVisibility(View.GONE);
    }
  }

  static class AudioViewHolder extends RecyclerView.ViewHolder {

    final TextView tv_title;
    final NBSoundView nb_sound;
    final TextView tv_tag;

    AudioViewHolder(View itemView) {
      super(itemView);
      tv_title = itemView.findViewById(R.id.tv_title);
      nb_sound = itemView.findViewById(R.id.nb_sound);
      tv_tag = itemView.findViewById(R.id.tv_tag);
    }
  }

}
