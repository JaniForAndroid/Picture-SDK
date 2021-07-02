package com.example.picsdk;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.picsdk.model.Book;
import com.example.picsdk.util.AppPicUtil;
import com.example.picsdk.view.AutoOrientationPicImageView;
import com.namibox.tools.GlideUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Create time: 2019/9/18.
 */
public class ReadBookPageAdapter extends PagerAdapter {
  private ReadBookActivity activity;
  private List<Book.BookPage> data;

  public ReadBookPageAdapter(ReadBookActivity activity, ArrayList<Book.BookPage> data) {
    this.activity = activity;
    this.data = data;
  }

  @Override
  public int getCount() {
    return data.size() + 1;
  }


  @NonNull
  @Override
  public Object instantiateItem(@NonNull ViewGroup container, int position) {
    if (position == data.size()) {
      View view = new View(container.getContext());
      container.addView(view);
      return view;
    }
    View v = LayoutInflater.from(container.getContext()).inflate(R.layout.layout_book_image_item, container, false);
    AutoOrientationPicImageView image = v.findViewById(R.id.image);
    image.setCallback(activity);
    Book.BookPage item = data.get(position);
    File pageFile = AppPicUtil.getBookResource(activity, item.page_url, activity.milesson_item_id);
    GlideUtil.loadImage(activity, pageFile, image);
    container.addView(v);
    return v;
  }

  @Override
  public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    View view = (View) object;
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
