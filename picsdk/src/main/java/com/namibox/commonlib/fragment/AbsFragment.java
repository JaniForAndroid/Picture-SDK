package com.namibox.commonlib.fragment;

import android.Manifest.permission;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chivox.EvalResult;
import com.example.picsdk.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namibox.commonlib.activity.AbsFunctionActivity;
import com.namibox.commonlib.event.CloseViewEvent;
import com.namibox.commonlib.event.CloseViewTabsEvent;
import com.namibox.commonlib.event.CsMessageEvent;
import com.namibox.commonlib.event.MainTabEvent;
import com.namibox.commonlib.event.MessageEvent;
import com.namibox.commonlib.event.PushDialogCloseEvent;
import com.namibox.commonlib.event.RefreshEvent;
import com.namibox.commonlib.event.SetCalendarStateEvent;
import com.namibox.commonlib.event.ViewStateEvent;
import com.namibox.commonlib.jsbridge.JSBridge;
import com.namibox.commonlib.jsbridge.JSCallback;
import com.namibox.commonlib.jsbridge.JSCommand;
import com.namibox.commonlib.jsbridge.JSHost;
import com.namibox.commonlib.jsbridge.JSMessage;
import com.namibox.commonlib.model.BaseCmd;
import com.namibox.commonlib.model.CmdAudioScore;
import com.namibox.commonlib.model.CmdCloseView;
import com.namibox.commonlib.model.CmdConfig;
import com.namibox.commonlib.model.CmdMenuControl;
import com.namibox.commonlib.model.CmdPlayInfo;
import com.namibox.commonlib.model.CmdRefreshView;
import com.namibox.commonlib.model.CmdReg;
import com.namibox.commonlib.model.SysConfig.GradeItem;
import com.namibox.commonlib.view.RecordView;
import com.namibox.commonlib.view.headers.NamiboxHeader;
import com.namibox.tools.GlideUtil;
import com.namibox.tools.MobUtil;
import com.namibox.tools.PermissionUtil;
import com.namibox.tools.ThinkingAnalyticsHelper;
import com.namibox.tools.WebViewUtil;
import com.namibox.util.FileUtil;
import com.namibox.util.ImageUtil;
import com.namibox.util.Logger;
import com.namibox.util.PreferenceUtil;
import com.namibox.util.Utils;
import com.namibox.util.network.NetWorkHelper;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.BezierRadarHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tarek360.instacapture.Instacapture;
import com.tarek360.instacapture.listener.ScreenCaptureListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Create time: 2018/6/5.
 */
public abstract class AbsFragment extends Fragment implements Handler.Callback,
    JSHost<AbsFragment> {

  private View contentView;
  protected AbsFunctionActivity activity;
  protected View videoLayout;
  protected float videoTransY;
  protected boolean videoScroll;//视频是否伴随滚动
  protected boolean isPort = true;
  protected int videoHeight;
  protected View toolbarLayout;
  private boolean ignoreThisListener;//忽略本次tab选中回调，避免循环
  private TabLayout tabLayout;
  private View titleLayout;
  private View titlebarLayout;
  private EditText searchView;
  private View statusBar;
  private View topShadowView;
  protected TextView titleView;
  private ImageView backView;
  protected ImageView ivBackTop;
  private ImageView simpleBack;
  private SmartRefreshLayout smartRefreshLayout;
  protected View divider;
  protected View errorPage;
  protected TextView errorText;
  protected TextView errorText2;
  private TextView errorBtn;
  private ImageView errorImage;
  //private GifDrawable errorDrawable;
  private GifDrawable gifDrawable;
  private boolean canPullToRefresh = false;
  protected int currentTopOffset = 0;
  private boolean isMIUI;
  private int statusBarColor = -1;
  private String viewName = "";
  private String tabName = "";
  private String tagName = "";
  private String tabId;
  private String originUrl;
  private int alpha;
  private boolean isDarkStyle;
  private long delayInit = -1;
  private boolean isViewCreated;
  private boolean isInited;
  private int mMode;
  private Map<String, String> configs = new HashMap<>();
  private JsonObject backTips;
  private String backAction;
  private boolean app_handle;
  private String searchUrl;
  private List<String> registerMessages = new ArrayList<>();
  private String shareType;
  private ViewStub engineLayoutStub;
  private ConstraintLayout engineLayout;
  private RecordView recordView;
  private RelativeLayout.LayoutParams layoutParams;
  protected Handler handler = new Handler(this);
  private boolean isPrimary;//是否当前选中，在tab中有用
  private boolean isDefault;
  private long enterTime = -1;//view进入时间
  private long stayTime = 0;//view停留时间
  private long tabEnterTime = -1;//tab进入时间
  private long tabStayTime = 0;//tab停留时间
  protected String mLanmuId;//跳转栏目但当前fragment未初始化，保存id
  JSBridge jsBridge;

  private static final int MSG_INIT = 10000;
  private static final int MSG_VIEW_ENTER = 10001;
  private static final int MSG_VIEW_EXIT = 10002;
  protected static final int MSG_REFRESH_DATA = 10004;
  protected static final int MSG_SCROLL_CLICK_READ = 1005;

  public static final int MODE_NORMAL = 0x00000001;
  public static final int MODE_FULLSCREEN = 0x00000002;
  public static final int MODE_FULLSCREEN_WITH_BACK = 0x00000004;//全屏带返回箭头
  public static final int MODE_FULLSCREEN_WITH_CONTENT = 0x00000008;//全屏带内容渐变
  public static final int MODE_FULLSCREEN_WITH_BACK2 = 0x00000010;//全屏带返回箭头

  public static final int MODE_EXTRA_BACK = 0x00000100;
  public static final int MODE_EXTRA_TITLE = 0x00000200;//渐变标题样式
  public static final int MODE_EXTRA_TITLE2 = 0x00000400;//显示、隐藏标题样式
  public static final int MODE_EXTRA_SEARCH = 0x00000800;
  public static final int MODE_EXTRA_SHADOW = 0x00001000;//是否显示阴影
  public static final int MODE_EXTRA_STATUSBAR = 0x00002000;//是否渐变状态栏颜色

  public static final int REFRESH_HEADER_STYLE_DEFAULT = 0x00000001;//默认风格
  public static final int REFRESH_HEADER_STYLE_BEZIER = 0x00000002;//贝塞尔风格
  public static final int REFRESH_HEADER_STYLE_NAMIBOX = 0x00000004;//纳米盒风格

  private static final String MENU_REFRESH = "10010";
  public static final String MENU_SHARE = "10011";
  public static final String MENU_SETTING = "10012";
  public static final String MENU_SIGN_SETTING = "10013";
  public static final String MENU_REFRESH_BOOK = "30001";
  private boolean loadingEnabled = true;
  private int style = REFRESH_HEADER_STYLE_DEFAULT;
  public boolean clickGuideClicked;
  private boolean showBookShortcut;
  private int titlePrevVisibility;
  private ViewGroup contentLayout;
  protected TextView tv_loading;

  private static class MenuItem {

    public static final int ID_LEFT = 0;
    public static final int ID_MENU1 = 1;
    public static final int ID_MENU2 = 2;
    public static final int ID_MENU3 = 3;//快捷方式
    public int id;
    public CmdMenuControl.Menu menu;
    public View menuView;
    public TextView menuTextView;
    public ImageView menuImageView;

    MenuItem(int id) {
      this.id = id;
    }
  }

  private MenuItem[] menuItems;

  private CommonListener commonListener;

  public void setCommonListener(CommonListener commonListener) {
    this.commonListener = commonListener;
  }

  public interface CloseViewListener {

    void closeSelf();
  }

  private CloseViewListener closeViewListener;

  public void setCloseViewListener(CloseViewListener closeViewListener) {
    this.closeViewListener = closeViewListener;
  }

  public interface VideoLayoutListener {

    View createVideoLayout();
  }

  private VideoLayoutListener videoLayoutListener;

  public void setVideoLayoutListener(VideoLayoutListener videoLayoutListener) {
    this.videoLayoutListener = videoLayoutListener;
  }

  public interface ShortcutListener {

    void onAddShortcut(boolean fromTips);

    void onShortcutTipsClose();

    void onShortcutShow();

    void onShortcutHide();
  }

  protected ShortcutListener shortcutListener;

  public void setShortcutListener(ShortcutListener shortcutListener) {
    this.shortcutListener = shortcutListener;
  }

  public void setVideoScroll(boolean videoScroll) {
    this.videoScroll = videoScroll;
  }

  public void setPortVideo() {
    isPort = true;
    LayoutParams lp = videoLayout.getLayoutParams();
    lp.height = videoHeight;
    videoLayout.setLayoutParams(lp);
    videoLayout.setTranslationY(videoTransY);
    toolbarLayout.setVisibility(View.VISIBLE);
  }

  public void setLandVideo() {
    isPort = false;
    LayoutParams lp = videoLayout.getLayoutParams();
    lp.height = LayoutParams.MATCH_PARENT;
    videoLayout.setLayoutParams(lp);
    videoLayout.setTranslationY(0);
    toolbarLayout.setVisibility(View.GONE);
  }

  public void setPrimary(boolean primary) {
    if (isPrimary == primary) {
      Logger.d(getViewName() + " isPrimary已经是" + primary);
      return;
    }
    isPrimary = primary;
    Message message = handler.obtainMessage(isPrimary ? MSG_VIEW_ENTER : MSG_VIEW_EXIT, 0, 0);
    handler.sendMessageDelayed(message, 0);
  }

  public boolean isPrimary() {
    return isPrimary;
  }

  public void setDefault(boolean aDefault) {
    isDefault = aDefault;
  }

  public boolean isDefault() {
    return isDefault;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    this.activity = (AbsFunctionActivity) getActivity();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public void onDetach() {
    if (activity != null) {
      this.activity = null;
    }
    super.onDetach();
  }

  @Override
  public void onPause() {
    super.onPause();
    Logger.d("[" + getViewName() + " " + getTagName() + "] onPause isPrimary=" + isPrimary);
    postViewState("pause");
    if (isPrimary()) {
      Message message = handler.obtainMessage(MSG_VIEW_EXIT, 0, 0);
      handler.sendMessageDelayed(message, 0);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Logger.d("[" + getViewName() + " " + getTagName() + "] onResume isPrimary=" + isPrimary);
    postViewState("resume");
    if (isPrimary()) {
      Message message = handler.obtainMessage(MSG_VIEW_ENTER, 0, 0);
      handler.sendMessageDelayed(message, 0);
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("url", originUrl);
    outState.putString("viewName", viewName);
    outState.putString("tabName", tabName);
    outState.putBoolean("canPullToRefresh", canPullToRefresh);
    outState.putInt("mode", mMode);
  }

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.what == MSG_INIT) {
      handler.removeMessages(MSG_INIT);
      init(msg.arg1 == 1);
      return true;
    } else if (msg.what == MSG_VIEW_ENTER) {
      handler.removeMessages(MSG_VIEW_ENTER);
      handler.removeMessages(MSG_VIEW_EXIT);
      if (enterTime == -1) {
        enterTime = System.currentTimeMillis();
        EventBus.getDefault()
            .post(new ViewStateEvent(ViewStateEvent.ENTER, isDefault, getTagName()));
      }
      if (tabEnterTime == -1) {
        tabEnterTime = System.currentTimeMillis();
      }
      if (onViewEnterAndExitListener != null) {
        onViewEnterAndExitListener.onViewEnter(tabEnterTime);
      }
      return true;
    } else if (msg.what == MSG_VIEW_EXIT) {
      handler.removeMessages(MSG_VIEW_ENTER);
      handler.removeMessages(MSG_VIEW_EXIT);
      if (enterTime > 0) {
        stayTime = System.currentTimeMillis() - enterTime;
        enterTime = -1;
        EventBus.getDefault()
            .post(new ViewStateEvent(ViewStateEvent.EXIT, isDefault, getTagName()));
        if (stayTime > 3000) {
          EventBus.getDefault()
              .post(new ViewStateEvent(ViewStateEvent.STAY, isDefault, getTagName(), stayTime));
        }
      }
      if (onViewEnterAndExitListener != null) {
        onViewEnterAndExitListener.onViewExit(AbsFragment.this);
      }
      if (tabEnterTime > 0) {
        tabStayTime = System.currentTimeMillis() - tabEnterTime;
        final long tempTabStayTime = tabStayTime;
        getContentView().postDelayed(new Runnable() {
          @Override
          public void run() {
            if (activity != null && !activity.isStopped()) {
              tabEnterTime = -1;
              if (tempTabStayTime > 3000) {
                EventBus.getDefault()
                    .post(new ViewStateEvent(ViewStateEvent.TAB_STAY, isDefault, getTagName(),
                        tabStayTime));
              }
            }
          }
        }, 1000);
      }
      return true;
    }
    return false;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      originUrl = savedInstanceState.getString("url");
      viewName = savedInstanceState.getString("viewName");
      tabName = savedInstanceState.getString("tabName");
      canPullToRefresh = savedInstanceState.getBoolean("canPullToRefresh");
      mMode = savedInstanceState.getInt("mode");
    }
    isMIUI = Utils.isMIUI();
    statusBarColor = ContextCompat.getColor(activity, R.color.statusbar_color);
    menuItems = new MenuItem[4];
    menuItems[0] = new MenuItem(MenuItem.ID_LEFT);
    menuItems[1] = new MenuItem(MenuItem.ID_MENU1);
    menuItems[2] = new MenuItem(MenuItem.ID_MENU2);
    menuItems[3] = new MenuItem(MenuItem.ID_MENU3);
    jsBridge = new JSBridge();
    jsBridge.register(this);
    jsBridge.register(activity);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    handler.removeCallbacksAndMessages(null);
    jsBridge.unregister(this);
    jsBridge.unregister(activity);
  }

  @Override
  public WebView getWebView() {
    return null;
  }

  @Override
  public AbsFragment getJsHost() {
    return this;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = createLayout(inflater, container);
    contentLayout = view.findViewById(R.id.contentLayout);
    contentView = createContentView(inflater, container);
    ViewGroup.MarginLayoutParams lp = getLayoutParams();
    //contentLayout子view顺序：gif背景 > webview/recyclerview > (可选视频区域) > title layout, error, engine ...
    int[] viewIndex = getViewIndex();
    contentLayout.addView(contentView, viewIndex[0], lp);
    if (videoLayoutListener != null) {
      videoLayout = videoLayoutListener.createVideoLayout();
      videoHeight = videoLayout.getLayoutParams().height;
      contentLayout.addView(videoLayout, viewIndex[1]);
      //视频不联合网页滚动，网页内容从视频下面开始
      if (!videoScroll) {
        lp.topMargin = videoHeight;
        contentView.setLayoutParams(lp);
      }
    }
    setFragmentBackground(contentLayout);
    return view;
  }

  protected void setFragmentBackground(ViewGroup contentLayout) {
    contentLayout.setBackgroundResource(R.drawable.web_content_bg);
  }

  protected void setBackgroundColor(int color) {
    contentLayout.setBackgroundColor(color);
  }


  protected int[] getViewIndex() {
    return new int[]{1, 2};
  }

  protected ViewGroup.MarginLayoutParams getLayoutParams() {
    if (layoutParams != null) {
      return layoutParams;
    }
    return new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT);
  }

  public void setLayoutParams(RelativeLayout.LayoutParams layoutParams) {
    this.layoutParams = layoutParams;
  }

  @Override
  public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(v, savedInstanceState);
    menuItems[0].menuView = v.findViewById(R.id.left_menu);
    menuItems[0].menuImageView = v.findViewById(R.id.left_menu_img1);
    menuItems[0].menuTextView = v.findViewById(R.id.left_menu_text1);
    menuItems[1].menuView = v.findViewById(R.id.menu1);
    menuItems[1].menuImageView = v.findViewById(R.id.menu_img1);
    menuItems[1].menuTextView = v.findViewById(R.id.menu_text1);
    menuItems[2].menuView = v.findViewById(R.id.menu2);
    menuItems[2].menuImageView = v.findViewById(R.id.menu_img2);
    menuItems[2].menuTextView = v.findViewById(R.id.menu_text2);
    menuItems[3].menuView = v.findViewById(R.id.menu3);
    menuItems[3].menuImageView = v.findViewById(R.id.menu_img3);
    menuItems[3].menuTextView = v.findViewById(R.id.menu_text3);
    engineLayoutStub = v.findViewById(R.id.engine_stub);
    toolbarLayout = v.findViewById(R.id.toolbar_layout);
    tabLayout = v.findViewById(R.id.tablayout);
    titleLayout = v.findViewById(R.id.titleLayout);
    titlebarLayout = v.findViewById(R.id.title_bar_layout);
    searchView = v.findViewById(R.id.search_edittext);
    searchView.setInputType(EditorInfo.TYPE_NULL);
    searchView.setFocusable(false);
    statusBar = v.findViewById(R.id.status_bar_layout);
    topShadowView = v.findViewById(R.id.top_shadow);
    titleView = v.findViewById(R.id.title);
    backView = v.findViewById(R.id.back);
    smartRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);
    setCanPullToRefresh(canPullToRefresh);
//    setHeaderStyle();
    smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
      @Override
      public void onRefresh(RefreshLayout refreshlayout) {
        refresh(false);

      }
    });
    smartRefreshLayout.setEnableLoadMore(false);
    divider = v.findViewById(R.id.divider);
    simpleBack = v.findViewById(R.id.simple_back);
    errorPage = v.findViewById(R.id.error_layout);
    errorText = v.findViewById(R.id.error_text);
    errorText2 = v.findViewById(R.id.error_text2);
    errorBtn = v.findViewById(R.id.error_btn);
    errorImage = v.findViewById(R.id.error_icon);
    errorPage.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        showRefresh();
        //refresh(true);
      }
    });
    errorBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        openDeviceSetting();
      }
    });
    ivBackTop = v.findViewById(R.id.iv_back_top);
    ivBackTop.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackTopClick();
      }
    });
    ImageView loadingAnimView = v.findViewById(R.id.loading_anim);
    tv_loading = v.findViewById(R.id.tv_loading);
    if (!loadingEnabled) {
      loadingAnimView.setVisibility(View.GONE);
      tv_loading.setVisibility(View.GONE);
    }
    try {
      //gifDrawable = new GifDrawable(getActivity().getAssets(), "loading_anim.gif");
      gifDrawable = new GifDrawable(activity.getResources(), R.drawable.loading_anim);
      gifDrawable.setLoopCount(0);
      gifDrawable.start();
      loadingAnimView.setImageDrawable(gifDrawable);
      //customWebView.setVisibility(View.INVISIBLE);
    } catch (Exception e) {
      e.printStackTrace();
      loadingAnimView.setImageResource(R.drawable.loading_anim);
    }
    initToolBar();
    initContentView();
    setContentScrollListener();
    isViewCreated = true;
    handler.removeMessages(MSG_INIT);
    Message message = handler.obtainMessage(MSG_INIT);
    message.arg1 = delayInit > 0 ? 0 : 1;//shouldBeVisible
    handler.sendMessageDelayed(message, delayInit > 0 ? delayInit : 100);
    EventBus.getDefault().register(this);
    Logger.d("[" + viewName + "] onViewCreated");
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void handleMainTabEvent(MainTabEvent event) {

  }

  public void onActivityResume(boolean resume) {

  }

  public void setHeaderStyle() {
    switch (style) {
      case REFRESH_HEADER_STYLE_DEFAULT:
      case REFRESH_HEADER_STYLE_NAMIBOX:
        smartRefreshLayout.setRefreshHeader(new NamiboxHeader(activity));
        break;
      case REFRESH_HEADER_STYLE_BEZIER:
        smartRefreshLayout
            .setRefreshHeader(new BezierRadarHeader(activity).setEnableHorizontalDrag(true));
        break;
    }
  }

  public void setRefreshHeadStyle(int style) {
    this.style = style;
  }

  public void setClickGuideClicked() {
    clickGuideClicked = true;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    onUnregister(null);
    EventBus.getDefault().unregister(this);
    isViewCreated = false;
    isInited = false;
    handler.removeMessages(MSG_INIT);
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    Logger.d("setUserVisibleHint [" + viewName + "] isVisibleToUser=" + isVisibleToUser);
    super.setUserVisibleHint(isVisibleToUser);
    handler.removeMessages(MSG_INIT);
    Message message = handler.obtainMessage(MSG_INIT);
    message.arg1 = 1;//shouldBeVisible
    handler.sendMessageDelayed(message, 100);
  }

  private void init(boolean shouldBeVisible) {
    Logger.d("init [" + viewName + "] getUserVisibleHint=" + getUserVisibleHint()
        + "  shouldBeVisible=" + shouldBeVisible);
    if (shouldBeVisible && !getUserVisibleHint()) {
      return;
    }
    if (isViewCreated && !isInited) {
      initDefaultMenus();
      refreshMenuView();
      doLoadData(originUrl, false);
      isInited = true;
    }
  }

  //forceNet 是否强制刷新网络数据
  protected abstract void doLoadData(String url, boolean forceNet);

  public void setCanPullToRefresh(boolean canPullToRefresh) {
    this.canPullToRefresh = canPullToRefresh;
    if (smartRefreshLayout != null) {
      smartRefreshLayout.setEnableRefresh(canPullToRefresh && currentTopOffset == 0);
    }
  }

  public boolean isInited() {
    return isInited;
  }

  public void setMode(int mMode) {
    this.mMode = mMode;
  }

  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  public String getViewName() {
    return viewName;
  }

  public void setTabName(String tabName) {
    this.tabName = tabName;
  }

  public String getTabName() {
    return tabName;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  public String getTagName() {
    return tagName;
  }

  public void setTabId(String tabId) {
    this.tabId = tabId;
  }

  public String getTabId() {
    return tabId;
  }

  public String getOriginUrl() {
    return originUrl;
  }

  public String getUrl() {
    return originUrl;
  }

  public void setOriginUrl(String originUrl) {
    this.originUrl = originUrl;
  }

  public void setDelayInit(long delayInit) {
    this.delayInit = delayInit;
  }

  public void setParentViewName(String parentViewName) {
    configs.put("parent_view_name", parentViewName);
  }

  public boolean shouldDisplayDarkStatusBar() {
    if ((mMode & MODE_FULLSCREEN_WITH_CONTENT) != 0
        && (mMode & MODE_EXTRA_SHADOW) == 0
        && (mMode & MODE_EXTRA_STATUSBAR) == 0) {
      return true;
    } else {
      return isDarkStyle;
    }
  }

  public void setDarkStyle(boolean darkStyle) {
    isDarkStyle = darkStyle;
  }

  public View getContentView() {
    return contentView;
  }

  public JsonElement getFloatingActionbar() {
    return null;
  }

  protected View createLayout(LayoutInflater inflater, ViewGroup container) {
    return inflater.inflate(R.layout.fragment_web_view, container, false);
  }

  protected abstract View createContentView(LayoutInflater inflater, ViewGroup container);

  protected abstract void initContentView();

  protected abstract void setContentScrollListener();

  public void handleGradeChange(GradeItem gradeItem) {

  }

  public void moveToPositionById(String id) {
  }

  public void moveToPosition(int position) {
  }

  public void checkRefresh() {
  }

  public void refresh(boolean original) {
    Logger.d(" refresh view: " + getViewName());
    hideErrorPage();
    doLoadData(getOriginUrl(), true);
  }

  public final void refreshView(String name, String url) {
    if (viewName.equals(name)) {
      if (!TextUtils.isEmpty(url)) {
        if (url.startsWith("/")) {
          url = NetWorkHelper.getInstance().getBaseUrl() + url;
        }
        originUrl = url;
      }
      refresh(true);
      Logger.d("refresh view: " + viewName + ", url: " + url);
    }
  }

  protected final void showRefresh() {
    if (smartRefreshLayout.isEnableRefresh()) {
      smartRefreshLayout.autoRefresh();
    } else {
      refresh(false);
    }
  }

  protected final void hideRefresh() {
    smartRefreshLayout.finishRefresh();
  }

  protected final void hideRefresh(int delay) {
    smartRefreshLayout.finishRefresh(delay);
  }

  protected void noDataToShow() {

  }

  protected void onBackTopClick() {
  }

  protected void showErrorPage() {
//    if (errorDrawable == null) {
//      try {
//        errorDrawable = new GifDrawable(activity.getResources(), R.drawable.ic_html_page_error);
//        errorDrawable.setLoopCount(0);
//        errorDrawable.start();
//        errorImage.setImageDrawable(errorDrawable);
//        //customWebView.setVisibility(View.INVISIBLE);
//      } catch (Exception e) {
//        e.printStackTrace();
//        errorImage.setImageResource(R.drawable.ic_html_page_error);
//      }
//    }
    GlideUtil.loadGif(activity, R.drawable.ic_html_page_error, errorImage);
    errorPage.setVisibility(View.VISIBLE);
    errorBtn.requestFocus();
  }

  protected void hideErrorPage() {
    errorPage.setVisibility(View.GONE);
  }

  protected void startLoadGif() {
    if (gifDrawable != null && !gifDrawable.isRunning()) {
      gifDrawable.start();
    }
  }

  protected void stopLoadGif() {
    if (gifDrawable != null && gifDrawable.isRunning()) {
      gifDrawable.stop();
    }
  }

  public void onSetTitle(String title) {
    if (titleView != null) {
      titleView.setText(title);
    }
  }

  private void initToolBar() {
    if ((mMode & MODE_FULLSCREEN) != 0) {
      //啥也不显示
//      ConstraintLayout.LayoutParams webLp = (ConstraintLayout.LayoutParams) contentLayout
//          .getLayoutParams();
//      webLp.topToTop =  R.id.toolbar_layout;
//      webLp.topToBottom = -1;
//      contentLayout.setLayoutParams(webLp);
    } else if ((mMode & MODE_FULLSCREEN_WITH_BACK) != 0) {
//      ConstraintLayout.LayoutParams webLp = (ConstraintLayout.LayoutParams) contentLayout
//          .getLayoutParams();
//      webLp.topToTop =  R.id.toolbar_layout;
//      webLp.topToBottom = -1;
//      contentLayout.setLayoutParams(webLp);
      toolbarLayout.setVisibility(View.VISIBLE);
      simpleBack.setVisibility(View.VISIBLE);
      titlebarLayout.setVisibility(View.GONE);
      setStatusBarHeight();
      simpleBack.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          onBackControl();
        }
      });
    } else if ((mMode & MODE_FULLSCREEN_WITH_BACK2) != 0) {
      toolbarLayout.setVisibility(View.VISIBLE);
      simpleBack.setVisibility(View.VISIBLE);
      titlebarLayout.setVisibility(View.GONE);
      setStatusBarHeight();
      statusBar.setBackgroundColor(0xff000000);
      RelativeLayout.LayoutParams webLp = (RelativeLayout.LayoutParams) getContentView()
          .getLayoutParams();
      webLp.topMargin = Utils.getStatusBarHeight(activity);
      getContentView().setLayoutParams(webLp);
      simpleBack.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          onBackControl();
        }
      });
    } else if ((mMode & MODE_FULLSCREEN_WITH_CONTENT) != 0) {
//      ConstraintLayout.LayoutParams webLp = (ConstraintLayout.LayoutParams) contentLayout
//          .getLayoutParams();
//      webLp.topToTop =  R.id.toolbar_layout;
//      webLp.topToBottom = -1;
//      contentLayout.setLayoutParams(webLp);
      toolbarLayout.setVisibility(View.VISIBLE);
      if ((mMode & MODE_EXTRA_SHADOW) != 0) {
        topShadowView.setVisibility(View.VISIBLE);
      }
      backView.setVisibility(View.GONE);
      titleView.setVisibility(View.GONE);
      setStatusBarHeight();
      if ((mMode & MODE_EXTRA_BACK) != 0) {
        backView.setVisibility(View.VISIBLE);
        backView.setImageResource(R.drawable.ic_arrow_back_white);
        backView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            onBackControl();
          }
        });
      }
      if ((mMode & MODE_EXTRA_TITLE) != 0) {
        titleView.setTextColor(0xffffffff);
        titleView.setVisibility(View.VISIBLE);
      } else if ((mMode & MODE_EXTRA_TITLE2) != 0) {
        titleView.setTextColor(0xff333333);
        titleView.setVisibility(View.GONE);//默认不显示
      }
      if ((mMode & MODE_EXTRA_SEARCH) != 0) {
        searchView.setVisibility(View.VISIBLE);
        searchView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            WebViewUtil.openView(searchUrl);
          }
        });
      }
    } else {
      isDarkStyle = true;
      toolbarLayout.setVisibility(View.VISIBLE);
      backView.setVisibility(View.GONE);
      titleView.setVisibility(View.GONE);
      RelativeLayout.LayoutParams webLp = (RelativeLayout.LayoutParams) getContentView()
          .getLayoutParams();
      webLp.addRule(RelativeLayout.BELOW, R.id.toolbar_layout);
      getContentView().setLayoutParams(webLp);
      toolbarLayout.setBackgroundColor(0xffffffff);
      activity.setDarkStatusIcon(true);
      divider.setVisibility(View.VISIBLE);
      setStatusBarHeight();
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && !isMIUI) {
        statusBar.setBackgroundColor(statusBarColor);
      }
      if ((mMode & MODE_EXTRA_BACK) != 0) {
        backView.setVisibility(View.VISIBLE);
        backView.setImageResource(R.drawable.ic_arrow_back_black);
        backView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            onBackControl();
          }
        });
      }
      if ((mMode & MODE_EXTRA_TITLE) != 0) {
        titleView.setTextColor(0xff333333);
        titleView.setVisibility(View.VISIBLE);
      }

    }
  }

  private void setStatusBarHeight() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      LayoutParams statusBarLp = statusBar.getLayoutParams();
      statusBarLp.height = Utils.getStatusBarHeight(activity);
    }
  }

  protected boolean onMenuClick(CmdMenuControl.Menu menu) {
    switch (menu.id) {
      case MENU_REFRESH:
        showRefresh();
        return true;
      case MENU_SHARE:
        requestShare(null);
        return true;
      default:
        if (commonListener != null) {
          return commonListener.onMenuClick(menu.id);
        }
        return false;
    }
  }

  public void requestShare(String type) {
    shareType = type;
  }

  private void initDefaultMenus() {
    menuItems[0].menu = null;
    menuItems[1].menu = null;
    menuItems[2].menu = null;
//    menuItems.clear();
//    if (TextUtils.isEmpty(viewName)) {
//      return;
//    }
//    if (commonListener != null && commonListener.hasDefaultMenu(viewName)) {
//
//      Cmd.Menu menu = new Cmd.Menu();
//      menu.id = MENU_REFRESH;
//      menu.isIconStyle = true;
//      menu.iconResId = R.drawable.ic_refresh;
//      menu.iconDarkResId = R.drawable.ic_refresh_black;
//      menuItems.add(menu);
//            case "main_user":
//                Cmd.Menu menu2 = new Cmd.Menu();
//                menu2.id = AbsWebViewActivity.MENU_SETTING;
//                menu2.name = "设置";
//                menus.put(AbsWebViewActivity.MENU_SETTING, menu2);
//                break;
//    }
  }

  private void refreshMenuView() {
    for (MenuItem menuItem : menuItems) {
      if (menuItem.menu != null) {
        menuItem.menuView.setVisibility(View.VISIBLE);
        final CmdMenuControl.Menu menu = menuItem.menu;
        if (!TextUtils.isEmpty(menu.image)) {
          menuItem.menuTextView.setVisibility(View.GONE);
          menuItem.menuImageView.setVisibility(View.VISIBLE);
          String image = isDarkStyle || TextUtils.isEmpty(menuItem.menu.selectimage) ?
              menuItem.menu.image : menuItem.menu.selectimage;
          loadImage(menuItem.menuImageView, image);
        } else {
          menuItem.menuTextView.setVisibility(View.VISIBLE);
          menuItem.menuImageView.setVisibility(View.GONE);
          menuItem.menuTextView.setText(menu.name);
          if (!TextUtils.isEmpty(menu.leftimage)) {
            String leftImage =
                isDarkStyle || TextUtils.isEmpty(menuItem.menu.left_selectimage) ? menu.leftimage
                    : menu.left_selectimage;
            loadTextDrawable(menuItem.menuTextView, leftImage, 0);
          }
          if (!TextUtils.isEmpty(menu.rightimage)) {
            String rightImage =
                isDarkStyle || TextUtils.isEmpty(menuItem.menu.right_selectimage) ? menu.rightimage
                    : menu.right_selectimage;
            loadTextDrawable(menuItem.menuTextView, rightImage, 1);
          }
          if (isDarkStyle) {
            if (!TextUtils.isEmpty(menuItem.menu.foregroundcolor)) {
              menuItem.menuTextView.setTextColor(Color.parseColor(menuItem.menu.foregroundcolor));
            } else {
              menuItem.menuTextView.setTextColor(0xff333333);
            }
            if (!TextUtils.isEmpty(menuItem.menu.backgroundcolor)) {
              int color = Color.parseColor(menuItem.menu.backgroundcolor);
              GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(activity,
                  menuItem.menu.strokeStyle ? R.drawable.toolbar_menu_item_bg2
                      : R.drawable.toolbar_menu_item_bg);
              if (menuItem.menu.strokeStyle) {
                drawable.setColor(0x00000000);
                drawable.setStroke(Utils.dp2px(activity, 1f), color);
              } else {
                drawable.setColor(color);
              }
              if (!TextUtils.isEmpty(menuItem.menu.layer_corner_radius)) {
                int radiusPx = Utils.dp2px(activity, Integer.parseInt(menuItem.menu.layer_corner_radius));
                drawable.setCornerRadius(radiusPx);
                final MenuItem tmpMenuItem = menuItem;
                tmpMenuItem.menuTextView.setPadding(Utils.dp2px(activity, 10),
                    Utils.dp2px(activity, 2),
                    Utils.dp2px(activity, 10),
                    Utils.dp2px(activity, 2));
                tmpMenuItem.menuTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tmpMenuItem.menuView.post(new Runnable() {
                  @Override
                  public void run() {
                    LayoutParams layoutParams = tmpMenuItem.menuView.getLayoutParams();
                    if (!TextUtils.isEmpty(tmpMenuItem.menu.frame_size_height)) {
                      layoutParams.height = Utils
                          .dp2px(activity, Integer.parseInt(tmpMenuItem.menu.frame_size_height));
                    } else {
                      layoutParams.height = Utils.dp2px(activity, 24);
                    }
                    if (!TextUtils.isEmpty(tmpMenuItem.menu.frame_size_width)) {
                      layoutParams.width = Utils
                          .dp2px(activity, Integer.parseInt(tmpMenuItem.menu.frame_size_width));
                    } else {
                      layoutParams.width = Utils.dp2px(activity, 52);
                    }
                    tmpMenuItem.menuView.setLayoutParams(layoutParams);
                  }
                });
              }
              menuItem.menuTextView.setBackground(drawable);
            } else {
              menuItem.menuTextView.setBackground(null);
            }
          } else {
            int color = 0xffffffff;
            if (!TextUtils.isEmpty(menuItem.menu.foregroundcolor_select)) {
              menuItem.menuTextView
                  .setTextColor(Color.parseColor(menuItem.menu.foregroundcolor_select));
            } else {
              menuItem.menuTextView.setTextColor(color);
            }
            if (!TextUtils.isEmpty(menuItem.menu.backgroundcolor)) {
              GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(activity,
                  menuItem.menu.strokeStyle ? R.drawable.toolbar_menu_item_bg2
                      : R.drawable.toolbar_menu_item_bg);
              if (menuItem.menu.strokeStyle) {
                drawable.setColor(0x00000000);
                drawable.setStroke(Utils.dp2px(activity, 1f), color);
              } else {
                drawable.setColor(color);
              }
              if (!TextUtils.isEmpty(menuItem.menu.layer_corner_radius)) {
                int radiusPx = Utils.dp2px(activity, Integer.parseInt(menuItem.menu.layer_corner_radius));
                drawable.setCornerRadius(radiusPx);
                final MenuItem tmpMenuItem = menuItem;
                tmpMenuItem.menuTextView.setPadding(Utils.dp2px(activity, 10),
                    Utils.dp2px(activity, 2),
                    Utils.dp2px(activity, 10),
                    Utils.dp2px(activity, 2));
                tmpMenuItem.menuTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tmpMenuItem.menuView.post(new Runnable() {
                  @Override
                  public void run() {
                    LayoutParams layoutParams = tmpMenuItem.menuView.getLayoutParams();
                    if (!TextUtils.isEmpty(tmpMenuItem.menu.frame_size_height)) {
                      layoutParams.height = Utils
                          .dp2px(activity, Integer.parseInt(tmpMenuItem.menu.frame_size_height));
                    } else {
                      layoutParams.height = Utils.dp2px(activity, 24);
                    }
                    if (!TextUtils.isEmpty(tmpMenuItem.menu.frame_size_width)) {
                      layoutParams.width = Utils
                          .dp2px(activity, Integer.parseInt(tmpMenuItem.menu.frame_size_width));
                    } else {
                      layoutParams.width = Utils.dp2px(activity, 52);
                    }
                    tmpMenuItem.menuView.setLayoutParams(layoutParams);
                  }
                });
              }
              menuItem.menuTextView.setBackground(drawable);
            } else {
              menuItem.menuTextView.setBackground(null);
            }
          }
        }
        menuItem.menuView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            onMenuClick(menu);
          }
        });
      } else if (menuItem.id == MenuItem.ID_MENU3) {
        if (isDarkStyle) {
          menuItem.menuTextView.setTextColor(0xff333333);
          menuItem.menuImageView.setImageResource(R.drawable.ic_add_to_shortcut_dark);
        } else {
          menuItem.menuTextView.setTextColor(0xffffffff);
          menuItem.menuImageView.setImageResource(R.drawable.ic_add_to_shortcut);
        }
        menuItem.menuView.setVisibility(showBookShortcut ? View.VISIBLE : View.GONE);
        menuItem.menuView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (shortcutListener != null) {
              shortcutListener.onAddShortcut(false);
            }
          }
        });
      } else {
        menuItem.menuView.setVisibility(View.GONE);
      }
    }
  }

  protected View getMenu3View() {
    return menuItems[3].menuView;
  }

  /**
   * 设置textView icon
   *
   * @param orientation 0: left 1:right
   */
  private void loadTextDrawable(final TextView textView, String url, final int orientation) {
    RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA);
    Glide.with(activity)
        .load(url)
        .apply(options)
        .into(new SimpleTarget<Drawable>() {
          @Override
          public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
            int horizontalPadding = Utils.dp2px(activity, 10);
            int verticalPadding = Utils.dp2px(activity, 4);
            textView
                .setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            textView.setCompoundDrawablePadding(Utils.dp2px(activity, 8));
            resource.setBounds(0, 0, Utils.dp2px(activity, 24), Utils.dp2px(activity, 24));
            if (orientation == 0) {
              textView.setCompoundDrawables(resource, null, null, null);
            } else if (orientation == 1) {
              textView.setCompoundDrawables(null, null, resource, null);
            }
          }
        });
  }

  private void loadImage(ImageView menuView, String image) {
    //image = "https://r.namibox.com/tina/static/app/icon/v3/me/works.png";
    if (image.startsWith("http")) {
      RequestOptions options = new RequestOptions()
          .fitCenter()
          .diskCacheStrategy(DiskCacheStrategy.DATA);
      Glide.with(activity)
          .load(image)
          .apply(options)
          .into(menuView);
    } else {
      File icon = new File(FileUtil.getLocalAppCacheDir(activity),
          "r.namibox.com/menuicons/" + image + ".png");
      RequestOptions options = new RequestOptions()
          .fitCenter()
          .diskCacheStrategy(DiskCacheStrategy.NONE);
      Glide.with(activity)
          .load(icon)
          .apply(options)
          .into(menuView);
      //menuImageView[i].setImageURI(Uri.fromFile(icon));
    }
  }

  protected void onContentScroll(int offset, int maxOffset) {
    onContentScroll(offset, -1, maxOffset);
  }

  //内容滚动回调，进行标题渐变等处理
  protected void onContentScroll(int offset, int lastOffset, int maxOffset) {
    if (smartRefreshLayout != null) {
      if (offset == 0) {
        smartRefreshLayout.setEnableRefresh(canPullToRefresh);
      } else {
        if (smartRefreshLayout.isEnableRefresh()) {
          smartRefreshLayout.setEnableRefresh(false);
        }
      }
    }
    if (tabLayout.getVisibility() == View.VISIBLE) {
      for (int i = tabLayout.getTabCount() - 1; i >= 0; i--) {
        Tab tab = tabLayout.getTabAt(i);
        int tabOffset = (int) tab.getTag();
        if (offset >= tabOffset) {
          if (!tab.isSelected()) {
            //offset > lastOffset表示向下滚动
            ignoreThisListener = offset < lastOffset;
            tab.select();
          }
          break;
        }
      }
    }

    if ((mMode & MODE_FULLSCREEN_WITH_CONTENT) == 0) {
      return;
    }
    if (videoLayout != null) {
      if (!videoScroll) {
        return;
      }
      scrollVideoLayout(offset);
    }
    onViewScroll(offset, maxOffset);
  }

  /**
   * 设置actionbar背景、字体的颜色和透明度
   */
  protected void onViewScroll(int offset, int maxOffset) {
    int distance = Math.min(Math.abs(offset), maxOffset);
    alpha = 0xFF * distance / maxOffset;
    int alpha_color = ColorUtils.setAlphaComponent(0xffffffff, alpha);
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && !isMIUI) {
      int alpha_status_color = ColorUtils.setAlphaComponent(statusBarColor, alpha);
      statusBar.setBackgroundColor(alpha_status_color);
    }
    if (tabLayout.getVisibility() == View.VISIBLE) {
      tabLayout.setAlpha(alpha / 255f);
    }
    toolbarLayout.setBackgroundColor(alpha_color);
    boolean darkStyle = alpha > 127;
    if (isDarkStyle != darkStyle) {
      isDarkStyle = darkStyle;
      if ((mMode & MODE_EXTRA_TITLE) != 0) {
        titleView.setTextColor(isDarkStyle ? 0xff333333 : 0xffffffff);
      } else if ((mMode & MODE_EXTRA_TITLE2) != 0) {
        titleView.setVisibility(isDarkStyle ? View.VISIBLE : View.GONE);
      } else if ((mMode & MODE_EXTRA_SEARCH) != 0) {
        searchView.setBackgroundResource(
            isDarkStyle ? R.drawable.search_view_bg_dark : R.drawable.search_view_bg);
      }
      backView.setImageResource(
          isDarkStyle ? R.drawable.ic_arrow_back_black : R.drawable.ic_arrow_back_white);
      if ((mMode & MODE_EXTRA_STATUSBAR) != 0) {
        activity.setDarkStatusIcon(isDarkStyle);
      }
      divider.setVisibility(isDarkStyle ? View.VISIBLE : View.GONE);
      refreshMenuView();
    }
  }

  protected void scrollVideoLayout(int offset) {
    videoTransY = Math.max(-videoHeight + 1, -offset);
    videoLayout.setTranslationY(isPort ? videoTransY : 0);
  }

  @JSCommand(command = BaseCmd.CMD_CLOSE_VIEW)
  public void onCloseView(@JSMessage CmdCloseView cmd) {
    String destViewName = cmd == null ? null : cmd.view_name;
    if (TextUtils.isEmpty(destViewName) || this.viewName.equals(destViewName)) {
      Logger.d("close self " + viewName);
      if (closeViewListener != null) {
        closeViewListener.closeSelf();
      }
    } else {
      EventBus.getDefault().post(new CloseViewEvent(destViewName));
    }
  }

  @JSCommand(command = BaseCmd.CMD_CLOSE_VIEW_TABS)
  public void onCloseViewTabs(@JSMessage CmdCloseView cmd) {
    if (TextUtils.isEmpty(cmd.view_name) || this.viewName.equals(cmd.view_name)) {
      Logger.d("close tab self: " + viewName);
      if (closeViewListener != null) {
        closeViewListener.closeSelf();
      }
    } else {
      EventBus.getDefault().post(new CloseViewTabsEvent(cmd.view_name));
    }
  }

  @JSCommand(command = BaseCmd.CMD_CLOSE_VIEW_TABS)
  public void onCloseViewAll(@JSMessage CmdCloseView cmd) {
    //todo 找一个合适的方法
  }

  @JSCommand(command = BaseCmd.CMD_MESSAGE)
  public void onCmdMessage(@JSMessage JsonObject jsonObject) {
    if (!TextUtils.isEmpty(viewName)) {
      jsonObject.addProperty("source_view_name", viewName);
    }
    String dest_view_name = jsonObject.has("dest_view_name") ?
        jsonObject.get("dest_view_name").getAsString() : null;
    String message = jsonObject.get("message").getAsString();
    EventBus.getDefault().post(new MessageEvent(dest_view_name, jsonObject.toString(), message));
  }

  @JSCommand(command = BaseCmd.CMD_CS_MESSAGE)
  public void onCSMessage(@JSMessage JsonObject jsonObject) {
    String command = jsonObject.get("command").getAsString();
    EventBus.getDefault().post(new CsMessageEvent(command, jsonObject.toString()));
  }

  @JSCommand(command = BaseCmd.CMD_QUERY_CSMSG)
  public String onQueryCsMsg() {
    HashMap<String, Object> map = new HashMap<>();
    int unread = PreferenceUtil
        .getUnreadKefuMsg(activity, Utils.getLoginUserId(activity));
    int imUnread = PreferenceUtil
        .getUnreadImMsg(activity, Utils.getLoginUserId(activity));
    map.put("command", "query_csmsg");
    map.put("unread", unread);
    map.put("im_unread", imUnread);
    return new Gson().toJson(map);
  }

  @JSCommand(command = BaseCmd.CMD_BOOK_SHORTCUT)
  public void showBookShortcut() {
    showBookShortcut = true;
    refreshMenuView();
    if (showBookShortcut) {
      titlePrevVisibility = titleView.getVisibility();
      titleView.setVisibility(View.GONE);
      //延迟一会
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          if (shortcutListener != null) {
            shortcutListener.onShortcutShow();
          }
        }
      }, 1000);
    } else {
      titleView.setVisibility(titlePrevVisibility);
      if (shortcutListener != null) {
        shortcutListener.onShortcutHide();
      }
    }
  }

  @JSCommand(command = BaseCmd.CMD_STATUSBARHEIGHT)
  public String getStatusbarheight() {
    int statusBarHeight = Utils.getStatusBarHeight(activity);
    int height = Utils.px2dip(activity, statusBarHeight);
    return "{\"statusBarHeight\":" + height + "}";
  }

  @JSCommand(command = BaseCmd.CMD_SEARCH_CONTROL)
  public void onSetSearchInfo(@JSMessage JsonObject jsonObject) {
    searchUrl = jsonObject.has("url") ?
        jsonObject.get("url").getAsString() : null;
    String placeholder = jsonObject.has("placeholder") ?
        jsonObject.get("placeholder").getAsString() : null;
    searchView.setText(placeholder);
  }

  public String getSearchUrl() {
    return searchUrl;
  }

  public void setSearchUrl(String searchUrl) {
    this.searchUrl = searchUrl;
  }

  @JSCommand(command = BaseCmd.CMD_BACK_SHOW_CONTROL)
  public void onBackShowControl(@JSMessage JsonObject jsonObject) {
    boolean show = jsonObject.has("show") && jsonObject.get("show").getAsBoolean();
    if ((mMode & MODE_FULLSCREEN_WITH_BACK) != 0 || (mMode & MODE_FULLSCREEN_WITH_BACK2) != 0) {
      simpleBack.setVisibility(show ? View.VISIBLE : View.GONE);
    }
  }

  @JSCommand(command = BaseCmd.CMD_STATUSBAR_CONTROL)
  public void onStatusBarControl(@JSMessage JsonObject jsonObject) {
    boolean dark = jsonObject.has("dark") && jsonObject.get("dark").getAsBoolean();
    if ((mMode & MODE_FULLSCREEN_WITH_BACK) != 0) {
      setDarkStyle(dark);
      activity.setDarkStatusIcon(isDarkStyle);
      if (dark && Build.VERSION.SDK_INT < Build.VERSION_CODES.M && !Utils.isMIUI()) {
        statusBar.setBackgroundColor(statusBarColor);
      } else {
        statusBar.setBackgroundColor(0x00000000);
      }
    } else if ((mMode & MODE_FULLSCREEN_WITH_BACK2) != 0) {
      setDarkStyle(dark);
      activity.setDarkStatusIcon(isDarkStyle);
      statusBar.setBackgroundColor(0xff000000);
    }
  }

  @JSCommand(command = BaseCmd.CMD_MENU_CONTROL)
  public void onMenuControl(@JSMessage CmdMenuControl cmd) {
    onSetMenu(cmd.left_action, cmd.menu_action);
  }

  public void onSetMenu(CmdMenuControl.Menu[] left_action, CmdMenuControl.Menu[] menu_action) {
    menuItems[0].menu = null;
    menuItems[1].menu = null;
    menuItems[2].menu = null;
    int index = 0;
    if (left_action != null) {
      for (CmdMenuControl.Menu menu : left_action) {
        if (TextUtils.isEmpty(menu.action) || !menu.action.equals("hide")) {
          menuItems[index].menu = applyMenuChange(menu);
          backView.setVisibility(View.GONE);
          break;
        }
      }
    } else {
      if ((mMode & MODE_EXTRA_BACK) != 0) {
        backView.setVisibility(View.VISIBLE);
      }
    }
    index++;
    if (menu_action != null) {
      setCanPullToRefresh(false);
      for (CmdMenuControl.Menu menu : menu_action) {
        if (TextUtils.isEmpty(menu.action) || !menu.action.equals("hide")) {
          if (menu.id.equals(MENU_REFRESH)) {
            //刷新特殊处理，不占用menu
            setCanPullToRefresh(true);
            continue;
          }
          menuItems[index++].menu = applyMenuChange(menu);
        }
        if (index > 2) {
          break;
        }
      }
    } else {
      initDefaultMenus();
    }
    refreshMenuView();
  }

  private CmdMenuControl.Menu applyMenuChange(CmdMenuControl.Menu menu) {
    if (menu.id.equals(MENU_SHARE)) {
      menu.image = "分享";
      menu.selectimage = "分享选中";
    }
    return menu;
  }

  @JSCommand(command = BaseCmd.CMD_SAVE_CONFIG)
  public void onSaveConfig(@JSMessage CmdConfig cmd) {
    for (String key : cmd.maps.keySet()) {
      PreferenceUtil.saveConfig(activity, key, cmd.maps.get(key));
    }
  }

  @JSCommand(command = BaseCmd.CMD_QUERY_CONFIG)
  public String onQueryConfig(@JSMessage CmdConfig cmd) {
    for (String key : cmd.maps.keySet()) {
      String value = PreferenceUtil.getConfig(activity, key);
      cmd.maps.put(key, value);
    }
    Gson gson = new Gson();
    return gson.toJson(cmd.maps);
  }

  @JSCommand(command = BaseCmd.CMD_SAVE_VIEW_CONFIG)
  public void onSaveViewConfig(@JSMessage CmdConfig cmd) {
    configs.putAll(cmd.maps);
  }

  @JSCommand(command = BaseCmd.CMD_QUERY_VIEW_CONFIG)
  public String onQueryViewConfig(@JSMessage CmdConfig cmd) {
    for (String key : cmd.maps.keySet()) {
      String value = configs.get(key);
      if (value != null) {
        cmd.maps.put(key, value);
      }
    }
    Gson gson = new Gson();
    return gson.toJson(cmd.maps);
  }

  @JSCommand(command = BaseCmd.CMD_REGISER)
  public void onCmdRegister(@JSMessage CmdReg cmd) {
    onRegister(cmd.message_name);
  }

  @JSCommand(command = BaseCmd.CMD_UNREGISTER)
  public void onCmdUnRegister(@JSMessage CmdReg cmd) {
    onUnregister(cmd.message_name);
  }

  protected void onRegister(String[] message_name) {
    if (message_name != null) {
      for (String msg : message_name) {
        if (!registerMessages.contains(msg)) {
          registerMessages.add(msg);
        }
      }
    }
  }

  protected void onUnregister(String[] message_name) {
    if (message_name == null || message_name.length == 0) {
      registerMessages.clear();
    } else {
      for (String msg : message_name) {
        if (registerMessages.contains(msg)) {
          registerMessages.remove(msg);
        }
      }
    }
  }

  public void onBackControl() {
    if ("page_quit".equals(backAction)) {
      backControlClick(2);
    } else if ("tip_to_quit".equals(backAction) && backTips != null) {
      if (activity != null) {
        String title = backTips.get("title").getAsString();
        String text = backTips.get("text").getAsString();
        String confirm = backTips.get("confirm").getAsString();
        String cancel = backTips.get("cancel").getAsString();
        activity.showDialog(title, text, cancel, new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (app_handle) {
              backControlClick(0);
            }
          }
        }, confirm, new OnClickListener() {
          @Override
          public void onClick(View view) {
            if (app_handle) {
              backControlClick(1);
            } else {
              if (commonListener != null) {
                commonListener.onBackControl();
              }
            }
          }
        }, new OnClickListener() {
          @Override
          public void onClick(View v) {

          }
        });
      }
    } else {
      if (commonListener != null) {
        commonListener.onBackControl();
      }
    }
  }

  protected void backControlClick(int chooseIndex) {
  }

  @JSCommand(command = BaseCmd.CMD_REFRESH_VIEW)
  public void onRefreshView(@JSMessage CmdRefreshView cmd) {
    if (TextUtils.isEmpty(cmd.view_name)) {
      refreshView(viewName, cmd.refresh_url);
      Logger.d("self refresh");
    } else {
      EventBus.getDefault().post(new RefreshEvent(cmd.view_name, cmd.refresh_url, cmd.hidden));
    }
  }

  public static final String SETTING = "setting";
  public static final String OPEN_MARKET = "open_market";
  public static final String CHECK_UPDATE = "checkupdate";
//  public static final String SHARE = "share";
//  public static final String SHARE_FRIEND = "sharefriend";
//  public static final String SHARE_TIMELINE = "sharetimeline";
//  public static final String SHARE_QQ = "qq";
//  public static final String SHARE_QZONE = "qzone";
//  public static final String SHARE_WEIBO = "weibo";
//  public static final String SHARE_DIARY = "diary";
//  public static final String SHARE_IM = "im";
//  public static final String ALIPAY = "alipay";

  @JSCommand(command = BaseCmd.CMD_CONTEXT)
  public void onAppOperation(@JSMessage JsonObject jsonObj) {
//    JsonElement operation = jsonObj.get("operation");
//    if (operation == null) {
//      Logger.e("empty operation");
//      return;
//    }
//    String type = operation.getAsString();
//    switch (type) {
//      case Const.SHARE:
//      case Const.SHARE_FRIEND:
//      case Const.SHARE_TIMELINE:
//      case Const.SHARE_QQ:
//      case Const.SHARE_QZONE:
//      case Const.SHARE_WEIBO:
//      case Const.SHARE_DIARY:
//      case Const.SHARE_IM:
//        JsonElement share = jsonObj.get("wxshare");
//        if (share != null) {
//          shareType = type;
//          onShowShare(share.getAsJsonObject());
//        } else {
//          requestShare(type);
//        }
//        break;
//      case OPEN_MARKET:
//        openMarket();
//        break;
//    }
  }

  @JSCommand(command = BaseCmd.CMD_GET_WXSHARE_CONTENT)
  public void onGetShareContent(@JSMessage JsonObject jsonObj) {
    onShowShare(jsonObj);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void handleMessage(MessageEvent event) {
    if (!TextUtils.isEmpty(viewName) && viewName.equals(event.destViewName)) {
      Logger.d(viewName + " 处理指定viewname消息: " + event.message);
      doSendMessage(event.message);
    } else if (!TextUtils.isEmpty(event.messageName) && registerMessages
        .contains(event.messageName)) {
      Logger.d(viewName + " 处理注册消息: " + event.message);
      doSendMessage(event.message);
    }
  }

  @JSCommand(command = BaseCmd.CMD_WEBVIEW_POP)
  public void onWebPop() {

  }

  public void doSendMessage(String message) {
  }

  public void postVideoComplete(String url, String type, String duration) {
  }

  public void payResult(String type, String return_code, String description, String return_url,
      String order_info) {
  }

  public void postVideoPlay(String url, String type, String duration, boolean result) {
  }

  public void notifyInterrupt(int dataId, int[] size) {
  }

  public void notifyInterruptHide() {
  }

  public void notifyReply(JsonObject replyMsg, String replyContent) {

  }

  public void notifyStatus(String url, String status) {
  }

  public void notifyPlayUpdate(long current, String url) {
  }

  public void notifyPlayControl(int index, boolean forward) {
  }

  @JSCommand(command = BaseCmd.CMD_APP_READY_SUCC)
  public void appReadySucc() {
  }

  @JSCommand(command = BaseCmd.CMD_BACK_CONTROL)
  public void onBackControl(@JSMessage JsonObject jsonObj) {
    this.backAction = jsonObj.get("action").getAsString();
    this.backTips = jsonObj.has("tip") ? jsonObj.get("tip").getAsJsonObject() : null;
    this.app_handle = jsonObj.has("app_handle") && jsonObj.get("app_handle").getAsBoolean();
  }

  @JSCommand(command = BaseCmd.CMD_PLAY_INFO)
  public void onSetPlayInfo(@JSMessage CmdPlayInfo cmd) {
    if (playListener != null) {
      playListener.onSetPlayInfo(cmd);
    }
  }

  @JSCommand(command = BaseCmd.CMD_HIDE_INTERRUPT)
  public void onHideInterrupt() {
    if (interruptListener != null) {
      interruptListener.onHideInterrupt();
    }
  }

  @JSCommand(command = BaseCmd.CMD_TITLE_CHANGED)
  public void onInterruptTitleChange(@JSMessage JsonObject jsonObject) {
    String title = jsonObject.get("title").getAsString();
    if (interruptListener != null) {
      interruptListener.onTitleChanged(title);
    }
  }

  @JSCommand(command = BaseCmd.CMD_NOTICE_POLICY)
  public String onNotifyPolicy() {
    NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(activity);
    return "{\"app_notice\":\"" + mNotificationManager.areNotificationsEnabled() + "\"}";
  }

  @JSCommand(command = BaseCmd.CMD_CHANGE_NOTICE_POLICY)
  public void changeNotifyPolicy() {
    PermissionUtil.openNotificationSetting(activity);
  }

  @JSCommand(command = BaseCmd.CMD_OPEN_DEVICE_SETTING)
  public void openDeviceSetting() {
  }

  @JSCommand(command = BaseCmd.CMD_OPEN_MARKET)
  public void openMarket() {
    Utils.openMarket(activity);
  }

  @JSCommand(command = BaseCmd.CMD_CLOSE_WEB_DIALOG)
  public void closeWebDialog() {
    EventBus.getDefault().post(new PushDialogCloseEvent());
  }

  public String handleJsonMessage(String message) {
    String s = "{}";
    if (message == null) {
      return s;
    }
    try {
      JsonObject jsonObj;
      try {
        jsonObj = new JsonParser().parse(message).getAsJsonObject();
      } catch (Exception e) {
        e.printStackTrace();
        return s;
      }
      String command = jsonObj.has("command") ?
          jsonObj.get("command").getAsString() : BaseCmd.CMD_OPEN_VIEW;
      //统计事件处理
      if (jsonObj.has("mobclick")) {
        JsonObject clickObject = jsonObj.get("mobclick").getAsJsonObject();
        mobClickEvent(clickObject);
      }
      //数数埋点处理
      if (jsonObj.has("tga_event")) {
        JsonObject clickObject = jsonObj.get("tga_event").getAsJsonObject();
        thinkingAnalyticsClickEvent(clickObject);
      }
      return jsBridge.callJava(this, jsonObj, command);
    } catch (Exception e) {
      if (commonListener != null) {
        commonListener.onMessageError(e);
      }
    }
    return s;
  }

  /**
   * 检测日历权限是否开启
   */
  @JSCommand(command = BaseCmd.CMD_CHECKCALENDARSTATE)
  public String checkCalendarStatus() {
    boolean isOpen = checkCalendarPermission() && PreferenceUtil.isCalendarOpen(activity);
    return "{\"isOpen\":\"" + isOpen + "\"}";
  }

  @JSCommand(command = BaseCmd.CMD_OPEN_CALENDAR, callback = BaseCmd.CMD_OPEN_CALENDAR)
  public String openCalendar(final JSCallback jsCallback) {
    PreferenceUtil.setOpenCalendar(activity, true);
    //权限被禁止了
    if (!PermissionUtil.checkPermission(activity, permission.READ_CALENDAR)) {
      List<String> permissions = new ArrayList<>();
      permissions.add(permission.READ_CALENDAR);
      PermissionUtil.showPermissionSettingDialog(permissions, activity);
    } else if (!PermissionUtil.checkPermission(activity, permission.WRITE_CALENDAR)) {
      List<String> permissions = new ArrayList<>();
      permissions.add(permission.WRITE_CALENDAR);
      PermissionUtil.showPermissionSettingDialog(permissions, activity);
    }
    EventBus.getDefault().post(new SetCalendarStateEvent(checkCalendarPermission()));
    return "{\"isOpen\":\"" + checkCalendarPermission() + "\"}";
  }

  @JSCommand(command = BaseCmd.CMD_CLOSE_CALENDAR)
  public String closeCalendar() {
    PreferenceUtil.setOpenCalendar(activity, false);
    EventBus.getDefault().post(new SetCalendarStateEvent(false));
    return "{\"isOpen\":\"" + false + "\"}";
  }

  /**
   * 检测日历权限是否开启
   */
  private boolean checkCalendarPermission() {
    return PermissionUtil.checkPermission(activity, permission.READ_CALENDAR)
        && PermissionUtil.checkPermission(activity, permission.WRITE_CALENDAR);
  }

  @JSCommand(command = BaseCmd.CMD_TAB_LAYOUT)
  public void showTabLayout(@JSMessage JsonObject jsonObject) {
    JsonArray jsonArray = jsonObject.get("indexes").getAsJsonArray();
    if (jsonArray.size() == 0) {
      return;
    }
    tabLayout.setVisibility(View.VISIBLE);
    if ((mMode & MODE_FULLSCREEN_WITH_CONTENT) != 0) {
      tabLayout.setAlpha(alpha / 255f);
    }
    tabLayout.removeAllTabs();
    titleLayout.setVisibility(View.GONE);
    for (JsonElement element : jsonArray) {
      JsonObject item = element.getAsJsonObject();
      String text = item.get("title").getAsString();
      //这里给的是dp
      int offset = item.get("index").getAsInt();
      offset = Utils.dp2px(activity, offset);
      if ((mMode & MODE_FULLSCREEN_WITH_CONTENT) != 0) {
        int h = toolbarLayout.getHeight();
        offset = Math.max(0, offset - h);
      }
      tabLayout.addTab(tabLayout.newTab().setText(text).setTag(offset));
    }
    tabLayout.setTabMode(jsonArray.size() >= 5 ?
        TabLayout.MODE_SCROLLABLE : TabLayout.MODE_FIXED);
    tabLayout.addOnTabSelectedListener(new OnTabSelectedListener() {
      @Override
      public void onTabSelected(Tab tab) {
        if (ignoreThisListener) {
          Logger.d("忽略一次onTabSelected：" + tab.getPosition());
          ignoreThisListener = false;
        } else {
          int offset = (int) tab.getTag();
          scrollToOffset(offset);
        }
      }

      @Override
      public void onTabUnselected(Tab tab) {

      }

      @Override
      public void onTabReselected(Tab tab) {

      }
    });
  }

  protected void scrollToOffset(int offset) {
  }

  /** 自定义事件上报 */
  @JSCommand(command = BaseCmd.CMD_MOB_CLICK)
  public void mobClickEvent(@JSMessage JsonObject jsonObject) {
    String eventId = "";
    HashMap<String, String> hashMap = new HashMap<>();
    if (jsonObject.has("eventId")) {
      eventId = jsonObject.get("eventId").getAsString();
    }
    if (jsonObject.has("attributes")) {
      JsonObject attObject = jsonObject.get("attributes").getAsJsonObject();
      hashMap = new Gson().fromJson(attObject.toString(), HashMap.class);
    }
    MobUtil.sendMobClick(activity, eventId, hashMap);
  }

  /** 数数自定义事件上报 */
  @JSCommand(command = BaseCmd.CMD_TA_CLICK)
  public void thinkingAnalyticsClickEvent(@JSMessage JsonObject jsonObject) {
    String eventId = "";
    HashMap<String, String> hashMap = new HashMap<>();
    if (jsonObject.has("event_name")) {
      eventId = jsonObject.get("event_name").getAsString();
    }
    if (jsonObject.has("properties")) {
      JsonObject attObject = jsonObject.get("properties").getAsJsonObject();
      hashMap = new Gson().fromJson(attObject.toString(), HashMap.class);
    }
    ThinkingAnalyticsHelper.trackEvent(eventId, hashMap);
  }

  public void onShowAudioScore(CmdAudioScore cmd, RecordView.Callback callback) {
    if (engineLayout != null) {
      engineLayout.setVisibility(View.VISIBLE);
    } else {
      engineLayout = (ConstraintLayout) engineLayoutStub.inflate();
      recordView = engineLayout.findViewById(R.id.record_view);
    }
    recordView.showText("正在初始化，请稍候...");
    recordView.setCallback(callback);
    recordView.setBgAlpha(cmd.alpha);
    ConstraintSet cs = new ConstraintSet();
    cs.clone(engineLayout);
    cs.setGuidelinePercent(R.id.h_guideline1, cmd.topMargin);
    cs.setGuidelinePercent(R.id.v_guideline1, cmd.leftMargin);
    cs.setGuidelinePercent(R.id.h_guideline2, cmd.topMargin + cmd.heightPercent);
    cs.setGuidelinePercent(R.id.v_guideline2, cmd.leftMargin + cmd.widthPercent);
    cs.applyTo(engineLayout);
  }

  public void onHideAudioScore() {
    if (engineLayout != null) {
      engineLayout.setVisibility(View.GONE);
    }
  }

  public void onShowShare(JsonObject jsonObj) {
//    JsonElement screenshot = jsonObj.get("screenshot");
//    if (screenshot != null && screenshot.getAsBoolean()) {
//      screenShot();
//    } else {
////      activity.showShare(shareType, jsonObj, new AbsFunctionActivity.ShareCallback() {
////        @Override
////        public void onResult(boolean success, String type) {
////          shareResult(success, type);
////        }
////      });
//
//      CommonShareHelper.commonShare(activity, shareType, jsonObj, new ShareCallback() {
//        @Override
//        public void onResult(boolean isSuccess, String msg) {
//          shareResult(isSuccess, msg);
//        }
//      });
//    }

  }

  protected void screenShot() {
//    Bitmap bmp = ImageUtil.getScreenShot(this);
//    File imgFile = new File(
//        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//        "bdc_share_bmp");
//    ImageUtil.compressBmpToFile(bmp, 85, imgFile);
//    showShareImage(imgFile);

//    Instacapture.capture(activity, new ScreenCaptureListener() {
//      @Override
//      public void onCaptureStarted() {
//      }
//
//      @Override
//      public void onCaptureFailed(Throwable throwable) {
//        activity.toast("截图失败");
//      }
//
//      @Override
//      public void onCaptureComplete(Bitmap bmp) {
////        Bitmap bmp = ImageUtil.getScreenShot(this);
//        File imgFile = new File(
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//            "bdc_share_bmp");
//        ImageUtil.compressBmpToFile(bmp, 85, imgFile);
////        activity.showShareImage(imgFile);
//
//        CommonShareHelper.shareSingleImgFile(activity, imgFile);
//      }
//    });

  }

  public void shareResult(boolean success, String type) {
  }

  public void postViewState(String state) {
  }

  public void onAudioScoreResult(EvalResult result) {
    if (recordView != null) {
      recordView.setEnabled(true);
      recordView.stop();
    }
  }

  public void onAudioScoreCancel(boolean userCanceled, EvalResult result) {
    result.result_type = userCanceled ? "cancel" : "extinterrupt";
    onAudioScoreResult(result);
  }

  public void onAudioScoreInitResult(boolean success) {
    if (recordView != null) {
      if (success) {
        recordView.setEnabled(true);
      } else {
        recordView.showText("初始化失败");
      }
    }
  }

  public void onAudioScoreVolume(int volume) {
    if (recordView != null) {
      recordView.setVolume(volume);
    }
  }

  public void showAudioScoreText(String text) {
    if (recordView != null) {
      recordView.showText(text);
    }
  }

  @JSCommand(command = BaseCmd.CMD_SWITCH_MEMBER)
  public void switchMemberCard(@JSMessage JsonObject jsonObject) {

  }

  public void updateGroups(String name, int type) {

  }

  public String getAudioName() {
    return null;
  }

  public void setSubPageTitle(String subPageTitle) {
  }

  public void onAudioScoreUploadResult(boolean success, String activityId) {
  }

  public interface PlayListener {

    void onSetPlayInfo(CmdPlayInfo cmd);
  }

  private PlayListener playListener;

  public void setPlayListener(PlayListener playListener) {
    this.playListener = playListener;
  }

  public interface InterruptListener {

    void onHideInterrupt();

    void onTitleChanged(String title);
  }

  public void setLoadingEnabled(boolean enabled) {
    loadingEnabled = enabled;
  }

  private InterruptListener interruptListener;

  public void setInterruptListener(InterruptListener interruptListener) {
    this.interruptListener = interruptListener;
  }

  public interface OnViewEnterAndExitListener {

    void onViewEnter(long enterTime);

    void onViewExit(AbsFragment absFragment);
  }

  private OnViewEnterAndExitListener onViewEnterAndExitListener;

  public void setOnViewEnterAndExitListener(OnViewEnterAndExitListener onViewEnterAndExitListener) {
    this.onViewEnterAndExitListener = onViewEnterAndExitListener;
  }

  public SmartRefreshLayout getSmartRefreshLayout() {
    return smartRefreshLayout;
  }
}
