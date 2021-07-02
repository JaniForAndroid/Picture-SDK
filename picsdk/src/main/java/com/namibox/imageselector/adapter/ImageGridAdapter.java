package com.namibox.imageselector.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.picsdk.R;
import com.namibox.imageselector.bean.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片Adapter
 * Created by Nereo on 2015/4/7.
 */
public class ImageGridAdapter extends BaseAdapter {

  private static final int TYPE_CAMERA = 0;
  private static final int TYPE_NORMAL = 1;
  private final int maxSelectNum;
  private final GridView mGridView;

  private Context mContext;

  private LayoutInflater mInflater;
  private boolean showCamera = true;
  private boolean showSelectIndicator = true;

  private List<Image> mImages = new ArrayList<>();
  private List<Image> mSelectedImages = new ArrayList<>();

  private int mItemSize;
  private GridView.LayoutParams mItemLayoutParams;
  private Image preImage;

  public ImageGridAdapter(Context context, boolean showCamera, int maxSelectNum,
      GridView gridView) {
    mContext = context;
    mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.showCamera = showCamera;
    mItemLayoutParams = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT,
        GridView.LayoutParams.MATCH_PARENT);
    this.maxSelectNum = maxSelectNum;
    this.mGridView = gridView;
  }

  /**
   * 显示选择指示器
   */
  public void showSelectIndicator(boolean b) {
    showSelectIndicator = b;
  }

  public void setShowCamera(boolean b) {
    if (showCamera == b) {
      return;
    }

    showCamera = b;
    notifyDataSetChanged();
  }

  public boolean isShowCamera() {
    return showCamera;
  }

  /**
   * 选择某个图片，改变选择状态
   */
  public void select(Image image, GridView gridView) {
    if (mSelectedImages.contains(image)) {
      mSelectedImages.remove(image);
    } else {
      if (maxSelectNum == 1 && mSelectedImages.size() == maxSelectNum) {
        mSelectedImages.clear();
      }
      mSelectedImages.add(image);
    }
    //notifyDataSetChanged();
    int position = mImages.indexOf(image);
    if (showCamera) {
      position++;
    }
    int visiblePosition = gridView.getFirstVisiblePosition();
    if (position - visiblePosition >= 0) {
      //得到要更新的item的view
      View view = gridView.getChildAt(position - visiblePosition);
      ViewHolder holder = view == null ? null : (ViewHolder) view.getTag();
      if (holder != null) {
        holder.updateIndicator(image, false);
      }
    }
  }

  /**
   * 通过图片路径设置默认选择
   */
  public void setDefaultSelected(ArrayList<String> resultList) {
    for (String path : resultList) {
      Image image = getImageByPath(path);
      if (image != null) {
        mSelectedImages.add(image);
      }
    }
    if (mSelectedImages.size() > 0) {
      notifyDataSetChanged();
    }
  }

  public Image getImageByPath(String path) {
    if (mImages != null && mImages.size() > 0) {
      for (Image image : mImages) {
        if (image.path.equalsIgnoreCase(path)) {
          return image;
        }
      }
    }
    return null;
  }

  public ArrayList<String> getImagePath() {
    if (mImages != null && mImages.size() > 0) {
      ArrayList<String> result = new ArrayList<>();
      for (Image image : mImages) {
        result.add(image.path);
      }
      return result;
    }
    return null;
  }

  /**
   * 设置数据集
   */
  public void setData(List<Image> images) {
    mSelectedImages.clear();

    if (images != null && images.size() > 0) {
      mImages = images;
    } else {
      mImages.clear();
    }
    notifyDataSetChanged();
  }

  /**
   * 重置每个Column的Size
   */
  public void setItemSize(int columnWidth) {

    if (mItemSize == columnWidth) {
      return;
    }

    mItemSize = columnWidth;

    mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);

    notifyDataSetChanged();
  }

  @Override
  public int getViewTypeCount() {
    return 2;
  }

  @Override
  public int getItemViewType(int position) {
    if (showCamera) {
      return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
    }
    return TYPE_NORMAL;
  }

  @Override
  public int getCount() {
    return showCamera ? mImages.size() + 1 : mImages.size();
  }

  @Override
  public Image getItem(int i) {
    if (showCamera) {
      if (i == 0) {
        return null;
      }
      return mImages.get(i - 1);
    } else {
      return mImages.get(i);
    }
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  @Override
  public View getView(int i, View view, ViewGroup viewGroup) {
    int type = getItemViewType(i);
    if (type == TYPE_CAMERA) {
      view = mInflater.inflate(R.layout.list_item_camera, viewGroup, false);
      view.setTag(null);
    } else if (type == TYPE_NORMAL) {
      ViewHolder holder;
      if (view == null) {
        view = mInflater.inflate(R.layout.list_item_image, viewGroup, false);
        holder = new ViewHolder(view);
      } else {
        holder = (ViewHolder) view.getTag();
        if (holder == null) {
          view = mInflater.inflate(R.layout.list_item_image, viewGroup, false);
          holder = new ViewHolder(view);
        }
      }
      holder.bindData(i);
    }

    /** Fixed View Size */
    GridView.LayoutParams lp = (GridView.LayoutParams) view.getLayoutParams();
    if (lp.height != mItemSize) {
      view.setLayoutParams(mItemLayoutParams);
    }

    return view;
  }

  class ViewHolder {

    ImageView image;
    CheckBox indicator;
    View mask;

    ViewHolder(View view) {
      image = (ImageView) view.findViewById(R.id.image);
      indicator = (CheckBox) view.findViewById(R.id.checkmark);
      mask = view.findViewById(R.id.mask);
      view.setTag(this);
    }

    void updateIndicator(final Image data, boolean isAuto) {
      if (showSelectIndicator) {
        indicator.setVisibility(View.VISIBLE);
        if (mSelectedImages.contains(data)) {

          if (maxSelectNum == 1) {
            if (preImage != null && !isAuto) {
              //notifyDataSetChanged();
              int position = mImages.indexOf(preImage);
              if (showCamera) {
                position++;
              }
              int visiblePosition = mGridView.getFirstVisiblePosition();
              if (position - visiblePosition >= 0) {
                //得到要更新的item的view
                View view = mGridView.getChildAt(position - visiblePosition);
                ViewHolder holder = view == null ? null : (ViewHolder) view.getTag();
                if (holder != null) {
                  holder.indicator.setChecked(false);
                  holder.mask.setVisibility(View.GONE);
                }
              }
            }
            preImage = data;
          }
          // 设置选中状态
          indicator.setChecked(true);
          mask.setVisibility(View.VISIBLE);
        } else {
          // 未选择
          indicator.setChecked(false);
          mask.setVisibility(View.GONE);
          if (preImage != null && !isAuto) {
            preImage = null;
          }
        }
        indicator.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (callback != null) {
              if (maxSelectNum == mSelectedImages.size()) {
                indicator.setChecked(false);
              }
              callback.onItemClick(data);
            }
          }
        });
      } else {
        indicator.setVisibility(View.GONE);
      }
    }

    void bindData(int position) {
      Image data = getItem(position);
      if (data == null) {
        return;
      }
      // 处理单选和多选状态
      updateIndicator(data, true);
      File imageFile = new File(data.path);

      if (mItemSize > 0) {
        // 显示图片
        RequestOptions options = new RequestOptions()
            .placeholder(R.drawable.default_error)
            .error(R.drawable.default_error)
            .centerCrop()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE);
        Glide.with(mContext)
            .asBitmap()
            .load(imageFile)
            .apply(options)
            .into(image);
      }
    }
  }

  public interface Callback {

    void onItemClick(Image data);
  }

  private Callback callback;

  public void setCallback(Callback callback) {
    this.callback = callback;
  }
}
