package com.example.picsdk;


import static com.example.picsdk.PicGuideActivity.CHALLENGE_PLAY;
import static com.example.picsdk.PicGuideActivity.CHALLENGE_READ;
import static com.example.picsdk.PicGuideActivity.CHALLENGE_WORD;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.picsdk.ChallengeAdapter.ChallengeViewHolder;
import com.example.picsdk.learn.BookManager;
import com.example.picsdk.model.ProductItem.Challenge;
import com.example.picsdk.util.PicturePreferenceUtil;
import com.namibox.tools.GlideUtil;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import com.willy.ratingbar.ScaleRatingBar;
import java.util.List;

/**
 * author : feng
 * creation time : 19-9-10上午10:38
 */
public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeViewHolder> {

  private List<Challenge> challenges;
  private OnItemClickListener onItemClickListener;

  void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  void setChallenges(List<Challenge> challenges) {
    this.challenges = challenges;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = LayoutInflater
        .from(parent.getContext()).inflate(R.layout.item_challenge, parent, false);
    return new ChallengeViewHolder(itemView, onItemClickListener);
  }

  @Override
  public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
    Context context = holder.itemView.getContext();
    RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
    lp.leftMargin = Utils.dp2px(context, position == 0 ? 16 : 8);
    lp.rightMargin = Utils.dp2px(context, position == challenges.size() - 1 ? 16 : 8);
    holder.itemView.setLayoutParams(lp);
    Challenge challenge = challenges.get(position);
    GlideUtil.loadImage(context, challenge.image, holder.iv_item_pic);
    holder.tv_item_title.setText(challenge.challenge_name);

    if (challenge.progress != -1) {
      int progress = PreferenceUtil
          .getSharePref(context, BookManager.getInstance().getMilesson_item_id() + "progress", 0);
      int maxProgress = 3;
      if (PicturePreferenceUtil.getLongLoginUserId(context) != -1L) {
        progress = challenge.progress;
        if (BookManager.getInstance() != null) {
          maxProgress = BookManager.getInstance().getLink_num();
        }
      }
      holder.group_study.setVisibility(View.VISIBLE);
      holder.rating_item.setVisibility(View.GONE);
      if (maxProgress == 3) {
        if (progress == 1) {
          holder.tv_study_progress.setText(Utils.format("%d%%", 30));
          holder.progress_study.setProgress(30);
        } else if (progress == 2) {
          holder.tv_study_progress.setText(Utils.format("%d%%", 60));
          holder.progress_study.setProgress(60);
        } else if (progress == 3) {
          holder.tv_study_progress.setText(Utils.format("%d%%", 100));
          holder.progress_study.setProgress(100);
        } else {
          holder.tv_study_progress.setText(Utils.format("%d%%", 0));
        }
      } else if (maxProgress == 4) {
        if (progress == 1) {
          holder.tv_study_progress.setText(Utils.format("%d%%", 25));
          holder.progress_study.setProgress(25);
        } else if (progress == 2) {
          holder.tv_study_progress.setText(Utils.format("%d%%", 50));
          holder.progress_study.setProgress(50);
        } else if (progress == 3) {
          holder.tv_study_progress.setText(Utils.format("%d%%", 75));
          holder.progress_study.setProgress(75);
        } else if (progress == 4) {
          holder.tv_study_progress.setText(Utils.format("%d%%", 100));
          holder.progress_study.setProgress(100);
        } else {
          holder.tv_study_progress.setText(Utils.format("%d%%", 0));
        }
      }
    } else {
      holder.group_study.setVisibility(View.GONE);
      holder.rating_item.setVisibility(View.VISIBLE);

      if (PicturePreferenceUtil.getLongLoginUserId(context) == -1L) {
        switch (challenge.task_type) {
          case CHALLENGE_WORD:
            holder.rating_item.setRating(PreferenceUtil
                .getSharePref(context,
                    BookManager.getInstance().getMilesson_item_id() + CHALLENGE_WORD + "star", 0));
            break;
          case CHALLENGE_READ:
            holder.rating_item.setRating(PreferenceUtil
                .getSharePref(context,
                    BookManager.getInstance().getMilesson_item_id() + CHALLENGE_READ + "star", 0));
            break;
          case CHALLENGE_PLAY:
            holder.rating_item.setRating(PreferenceUtil
                .getSharePref(context,
                    BookManager.getInstance().getMilesson_item_id() + CHALLENGE_PLAY + "star", 0));
            break;
          default:
            break;
        }
      } else {
        holder.rating_item.setRating(challenge.star);
      }
    }

    int lock = challenge.is_locked;
    if (PicturePreferenceUtil.getLongLoginUserId(context) == -1L) {
      switch (challenge.task_type) {
        case CHALLENGE_WORD:
          if (PreferenceUtil
              .getSharePref(context, BookManager.getInstance().getMilesson_item_id() + "progress",
                  0) == 3) {
            lock = 0;
          }
          break;
        case CHALLENGE_READ:
          if (PreferenceUtil.getSharePref(context,
              BookManager.getInstance().getMilesson_item_id() + CHALLENGE_WORD + "star", 0) >= 1) {
            lock = 0;
          }
          break;
        case CHALLENGE_PLAY:
          if (PreferenceUtil.getSharePref(context,
              BookManager.getInstance().getMilesson_item_id() + CHALLENGE_READ + "star", 0) >= 1) {
            lock = 0;
          }
          break;
        default:
          break;
      }
    }
    holder.gp_lock.setVisibility(challenge.is_buy == 0 || lock > 0 ? View.VISIBLE : View.GONE);
  }

  @Override
  public int getItemCount() {
    return challenges == null ? 0 : challenges.size();
  }

  static class ChallengeViewHolder extends ViewHolder {

    ImageView iv_item_pic;
    TextView tv_item_title;
    Group group_study;
    TextView tv_study_progress;
    ProgressBar progress_study;
    ScaleRatingBar rating_item;
    Group gp_lock;

    ChallengeViewHolder(View itemView, OnItemClickListener onItemClickListener) {
      super(itemView);
      iv_item_pic = itemView.findViewById(R.id.iv_item_pic);
      gp_lock = itemView.findViewById(R.id.gp_lock);
      tv_item_title = itemView.findViewById(R.id.tv_item_title);
      group_study = itemView.findViewById(R.id.group_study);
      tv_study_progress = itemView.findViewById(R.id.tv_study_progress);
      progress_study = itemView.findViewById(R.id.progress_study);
      rating_item = itemView.findViewById(R.id.rating_item);
      itemView.setOnClickListener(v -> {
        if (onItemClickListener != null) {
          onItemClickListener.onItemClick(getAdapterPosition());
        }
      });
    }
  }

  public interface OnItemClickListener {

    void onItemClick(int position);
  }
}
