package com.namibox.dub;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chivox.EvalResult.Detail;
import com.example.picsdk.R;
import java.util.List;

/**
 * Created by Roy.chen on 2017/6/26.
 */

class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordHolder> {

  private final List<Detail> words;
  private Context context;
  private boolean isTranscript;

  WordAdapter(List<Detail> words, boolean isTranscript) {
    this.words = words;
    this.isTranscript = isTranscript;
  }


  @NonNull
  @Override
  public WordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);
    if (isTranscript) {
      View inflate = inflater.inflate(R.layout.evaluation_layout_transcript_word, parent, false);
      return new WordHolder(inflate);
    } else {
      View inflate = inflater.inflate(R.layout.evaluation_layout_word, parent, false);
      return new WordHolder(inflate);
    }
  }

  @Override
  public void onBindViewHolder(WordHolder holder, int position) {
    Detail detail = words.get(position);
    int score = Integer.parseInt(detail.score);
    int scoreColor;
    if (score >=75) {
      scoreColor = context.getResources().getColor(R.color.hfx_score_exl);
      holder.tvWordScore.setBackgroundResource(R.drawable.evaluation_word_exl);
    } else if (score > 60) {
      scoreColor = context.getResources().getColor(R.color.hfx_score_fine);
      holder.tvWordScore.setBackgroundResource(R.drawable.evaluation_word_normal);
    } else {
      scoreColor = context.getResources().getColor(R.color.hfx_score_fail);
      holder.tvWordScore.setBackgroundResource(R.drawable.evaluation_word_fail);
    }
    holder.tvWord.setText(detail.word);
    holder.tvWord.setTextColor(scoreColor);
    holder.tvWordScore.setText(String.valueOf(score));
    if (isTranscript) {
      holder.tvWordScore.setVisibility(View.VISIBLE);
      holder.tvWord.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
    }else{
      holder.tvWord.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
      holder.tvWordScore.setVisibility(View.GONE);
    }
  }

  @Override
  public int getItemCount() {
    return words == null ? 0 : words.size();
  }

  class WordHolder extends RecyclerView.ViewHolder {

    TextView tvWordScore;
    TextView tvWord;

    WordHolder(View itemView) {
      super(itemView);
      tvWord = itemView.findViewById(R.id.tvWord);
      tvWordScore = itemView.findViewById(R.id.tvWordScore);
    }
  }
}
