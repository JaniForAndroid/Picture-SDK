package com.namibox.hfx.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import com.example.picsdk.R;
import com.namibox.commonlib.activity.BaseActivity;


/**
 * Create time: 2015/8/25.
 */
public class MyWorkActivity extends BaseActivity {

  static final String TAG = "MyWorkActivity";
  public static final String ARG_TAB = "tab";
  public static final int TAB_MAKING = 0;
  public static final int TAB_CHECKING = 1;
  public static final int TAB_BLOCK = 2;
  private static final String CURRENTTAB = "currentTab";
  TabLayout tabLayout;
  ViewPager tabVp;
  private int currentTab;
  private PageAdapter adapter;
  private int newTab;


  private TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
      currentTab = tab.getPosition();
      tabVp.setCurrentItem(currentTab);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
  };

  public static void openMyWork(Context context, int tab) {
    Intent intent = new Intent(context, MyWorkActivity.class);
    intent.putExtra(MyWorkActivity.ARG_TAB, tab);
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle(R.string.hfx_my_work);
    Intent intent = getIntent();
    currentTab = intent.getIntExtra(ARG_TAB, TAB_MAKING);
    setContentView(R.layout.hfx_activity_my_work);
    initView();

    adapter = new PageAdapter(getSupportFragmentManager());
    tabVp.setAdapter(adapter);
    tabLayout.setTabMode(TabLayout.MODE_FIXED);
    tabLayout.setupWithViewPager(tabVp);
    tabLayout.setOnTabSelectedListener(tabSelectedListener);
    tabLayout.getTabAt(currentTab).select();

  }

  private void initView() {
    tabLayout = findViewById(R.id.tabs);
    tabVp = findViewById(R.id.tab_vp);
  }


  private class PageAdapter extends FragmentPagerAdapter {

    FragmentManager fm;
    BaseWorkFragment current;
    private String tabTitles[] = new String[]{"制作中", "审核中", "审核未过"};

    public PageAdapter(FragmentManager fm) {
      super(fm);
      this.fm = fm;

    }


    @Override
    public Fragment getItem(int position) {
      if (position > 2) {
        throw new IllegalArgumentException("invalid position: " + position);
      } else {
        return BaseWorkFragment.newInstance(position);
      }
    }

    public BaseWorkFragment getCurrent() {
      return current;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
      super.setPrimaryItem(container, position, object);
      current = (BaseWorkFragment) object;

    }

    @Override
    public int getCount() {
      return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return tabTitles[position];
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    newTab = intent.getIntExtra(ARG_TAB, TAB_MAKING);
    if (currentTab != newTab) {

      tabLayout.getTabAt(newTab).select();


    } else {
      refreshCurrentFragment();
    }


  }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == TAB_MAKING) {
//            refreshCurrentFragment();
//        }
//    }

  private void refreshCurrentFragment() {
    adapter.getCurrent().refreshData(true);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(CURRENTTAB, tabLayout.getSelectedTabPosition());
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    tabVp.setCurrentItem(savedInstanceState.getInt(CURRENTTAB));
  }

}

