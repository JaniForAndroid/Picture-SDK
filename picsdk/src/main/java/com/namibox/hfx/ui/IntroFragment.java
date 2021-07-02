package com.namibox.hfx.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.example.picsdk.R;

/**
 * Created by sunha on 2015/10/21 0021.
 */
public class IntroFragment extends Fragment {

  private static final String ARG_TITLE = "title";
  private static final String ARG_DESC = "desc";
  private static final String ARG_DRAWABLE = "drawable";
  private static final String ARG_COLOUR = "colour";
  ImageView image;

  public static IntroFragment newInstance(int imageDrawable) {
    IntroFragment sampleSlide = new IntroFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_DRAWABLE, imageDrawable);
    sampleSlide.setArguments(args);

    return sampleSlide;
  }

  private int drawable;


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null && getArguments().size() != 0) {
      drawable = getArguments().getInt(ARG_DRAWABLE);
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.hfx_fragment_intro_img, container, false);
    image = v.findViewById(R.id.image);
    image.setImageResource(drawable);
    return v;
  }
}
