package com.namibox.hfx.ui;

import android.os.Bundle;
import android.view.WindowManager;
import com.example.picsdk.R;
import com.github.paolorotolo.appintro.AppIntro;


/**
 * Created by sunha on 2015/10/21 0021.
 */
public class IntroActivity extends AppIntro {

  @Override
  public void init(Bundle savedInstanceState) {
    full(false);
    addSlide(IntroFragment.newInstance(R.drawable.hfx_intro_1));
    addSlide(IntroFragment.newInstance(R.drawable.hfx_intro_8));
    addSlide(IntroFragment.newInstance(R.drawable.hfx_intro_2));
    addSlide(IntroFragment.newInstance(R.drawable.hfx_intro_3));
    addSlide(IntroFragment.newInstance(R.drawable.hfx_intro_4));
    addSlide(IntroFragment.newInstance(R.drawable.hfx_intro_5));
    addSlide(IntroFragment.newInstance(R.drawable.hfx_intro_6));
    addSlide(IntroFragment.newInstance(R.drawable.hfx_intro_7));

    showSkipButton(true);
    showDoneButton(true);
    setSkipText(getString(R.string.hfx_skip));
    setDoneText(getString(R.string.hfx_done));
    setSeparatorColor(0x00000000);
    setProgressIndicator();

  }

  @Override
  public void onSkipPressed() {
    finish();

  }

  @Override
  public void onDonePressed() {
    finish();
  }

  private void full(boolean enable) {
    if (enable) {
      WindowManager.LayoutParams lp = getWindow().getAttributes();
      lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
      getWindow().setAttributes(lp);
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    } else {
      WindowManager.LayoutParams attr = getWindow().getAttributes();
      attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
      getWindow().setAttributes(attr);
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
  }
}
