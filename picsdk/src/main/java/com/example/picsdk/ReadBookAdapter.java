package com.example.picsdk;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.picsdk.model.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Create time: 2019/9/18.
 */
public class ReadBookAdapter extends PagerAdapter {
  private ReadBookActivity activity;
  private View[] items;
  private List<Book.TrackInfo> data;
  private boolean scrollEnd;

  public ReadBookAdapter(ReadBookActivity activity, ArrayList<Book.TrackInfo> data, boolean scrollEnd) {
    this.activity = activity;
    this.data = data;
    this.scrollEnd = scrollEnd;
    items = new View[data.size() + 1];
  }

  @Override
  public int getCount() {
    return scrollEnd ? data.size() + 1 : data.size();
  }

  public Book.TrackInfo getTrackInfo(int position) {
    return scrollEnd && position >= data.size() ? null : data.get(position);
  }

  public void refresh() {
    for (View v : items) {
      if (v == null) {
        continue;
      }
      TextView text2 = v.findViewById(R.id.text2);
      text2.setVisibility(activity.isTransOn() ? View.VISIBLE : View.GONE);
      ImageView shadow = v.findViewById(R.id.shadow);
      shadow.setImageResource(activity.isPortrait() ? R.drawable.book_page_shadow2 : R.drawable.book_page_shadow);
    }
  }

  @NonNull
  @Override
  public Object instantiateItem(@NonNull ViewGroup container, int position) {
    if (scrollEnd && position == data.size()) {
      View view = new View(container.getContext());
      container.addView(view);
      return view;
    }
    View v = LayoutInflater.from(container.getContext()).inflate(R.layout.layout_book_text_item, container, false);
    TextView text1 = v.findViewById(R.id.text1);
    TextView text2 = v.findViewById(R.id.text2);
    text2.setVisibility(activity.isTransOn() ? View.VISIBLE : View.GONE);
    ImageView shadow = v.findViewById(R.id.shadow);
    shadow.setImageResource(activity.isPortrait() ? R.drawable.book_page_shadow2 : R.drawable.book_page_shadow);
    Book.TrackInfo item = data.get(position);
    if (item.track_txt != null) {
      text1.setText(item.track_txt);
    }
    if (item.track_genre != null) {
      text2.setText(item.track_genre);
    }
    container.addView(v);
    items[position] = v;
    return v;
  }

  @Override
  public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    View view = (View) object;
    items[position] = null;
    container.removeView(view);
  }

  @Override
  public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    super.setPrimaryItem(container, position, object);
  }

  @Override
  public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
    return view == object;
  }
}
