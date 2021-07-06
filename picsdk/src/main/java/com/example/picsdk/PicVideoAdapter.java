package com.example.picsdk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.exoaudioplayer.video.util.PlayerUtils;
import com.example.picsdk.model.VideoPicInfo;
import com.example.picsdk.view.AspectRatioImageView;
import com.namibox.tools.GlideUtil;

import java.util.List;

public class PicVideoAdapter extends RecyclerView.Adapter<PicVideoAdapter.ViewImageHolder> {

  private List<VideoPicInfo> items;
  private Context mContext;
  private PicVideoAdapter.OnItemClickListener onItemClickListener;

  public void setOnItemClickListener(PicVideoAdapter.OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public void setItems(List<VideoPicInfo> items) {
    this.items = items;
    notifyDataSetChanged();
  }

  public PicVideoAdapter(Context context){
    this.mContext = context;
  }

  @NonNull
  @Override
  public ViewImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_wk_video_pic, parent, false);
    return new ViewImageHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewImageHolder holder, int position) {
    VideoPicInfo item = items.get(position);
    GlideUtil.loadImage(mContext, item.thumb_url, R.drawable.default_pic, R.drawable.default_pic,
        false, GlideUtil.DATA, holder.iv_pic);

    holder.tv_title.setText(item.video_name);
    String time = holder.tv_duration.getContext().getString(R.string.book_video_time);
    holder.tv_duration.setText(time +": " + PlayerUtils.stringForTime(item.duration * 1000));
    holder.itemView.setOnClickListener(v -> {
      if (onItemClickListener != null) {
        onItemClickListener.onItemClick(position);
      }
    });

    if (item.isPlay) {
      holder.isPlayView.setVisibility(View.VISIBLE);
    } else {
      holder.isPlayView.setVisibility(View.GONE);
    }
  }

  @Override
  public int getItemCount() {
    return items == null ? 0 : items.size();
  }

  static class ViewImageHolder extends ViewHolder {
    AspectRatioImageView iv_pic;
    TextView tv_title, tv_duration;
    View isPlayView;

    ViewImageHolder(View itemView) {
      super(itemView);
      iv_pic = itemView.findViewById(R.id.iv_pic);
      tv_title = itemView.findViewById(R.id.tv_title);
      tv_duration = itemView.findViewById(R.id.tv_duration);
      isPlayView = itemView.findViewById(R.id.isPlayView);
    }
  }

  public interface OnItemClickListener {
    void onItemClick(int position);
  }
}
