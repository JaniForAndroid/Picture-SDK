package com.namibox.commonlib.view;

import android.content.Context;
import android.os.Parcel;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import java.util.ArrayList;
import java.util.List;

public class SpEditText extends AppCompatEditText {

  private OnMentionInputListener mOnMentionInputListener;

  public SpEditText(Context context) {
    super(context);
    initView();
  }

  public SpEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
    if (isInEditMode()) {
      return;
    }
    initView();
  }


  /**
   * 初始化控件,一些监听
   */
  private void initView() {
    this.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count,
          int after) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int start, int before,
          int count) {
        changeText(charSequence, start, count);
      }

      @Override
      public void afterTextChanged(Editable s) {
//        resolveDeleteSpecialStr();
      }
    });

    /**
     * 监听删除键 <br/>
     * 1.光标在话题后面,将整个话题内容删除 <br/>
     * 2.光标在普通文字后面,删除一个字符
     *
     */
    this.setOnKeyListener(new OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
          int selectionStart = getSelectionStart();
          int selectionEnd = getSelectionEnd();

          PrivateSpan[] spanneds = getText().getSpans(0, getText().length(), PrivateSpan.class);
          if (spanneds != null && spanneds.length > 0) {
            int start = getText().getSpanStart(spanneds[0]);
            int end = getText().getSpanEnd(spanneds[0]);
            if (selectionStart == end) {
              getEditableText().delete(start, selectionEnd);
              return true;
            }
          }

          List<Range> ranges = getCustomSpanRanges();

          // 遍历判断光标的位置
          for (int i = 0; i < ranges.size(); i++) {
            Range range = ranges.get(i);
            int rangeStart = range.start;
            if (selectionStart == range.end) {
              getEditableText().delete(rangeStart, selectionEnd);
              return true;
            }

          }
        }

        return false;
      }
    });
  }

  protected void changeText(CharSequence charSequence, int start, int count) {
    if (count == 1 && !TextUtils.isEmpty(charSequence)) {
      char mentionChar = charSequence.toString().charAt(start);
      if ("@".equals(String.valueOf(mentionChar)) && mOnMentionInputListener != null) {
        mOnMentionInputListener.onAtMotion();
      }
    }
  }

  public List<Range> getCustomSpanRanges() {
    Editable editable = getText();
    AtSpan[] spanneds = editable.getSpans(0, getText().length(), AtSpan.class);
    List<Range> ranges = new ArrayList<>();
    for (AtSpan atSpan : spanneds) {
      int start = editable.getSpanStart(atSpan);
      int end = editable.getSpanEnd(atSpan);
      if (start != -1 && end != -1) {
        ranges.add(new Range(start, end));
      }
    }
    return ranges;
  }

  public AtSpan[] getCustomSpans() {
    Editable editable = getText();
    AtSpan[] spanneds = editable.getSpans(0, getText().length(), AtSpan.class);
    List<Range> ranges = new ArrayList<>();
    for (AtSpan atSpan : spanneds) {
      int start = editable.getSpanStart(atSpan);
      int end = editable.getSpanEnd(atSpan);
      if (start != -1 && end != -1) {
        ranges.add(new Range(start, end));
      }
    }
    return spanneds;
  }

  /**
   * 监听光标的位置,若光标处于话题内容中间则移动光标到话题结束位置
   */
  @Override
  protected void onSelectionChanged(int selStart, int selEnd) {
    super.onSelectionChanged(selStart, selEnd);
    PrivateSpan[] spanneds = getText().getSpans(0, getText().length(), PrivateSpan.class);
    if (spanneds != null && spanneds.length > 0) {

      int start = getText().getSpanStart(spanneds[0]);
      int end = getText().getSpanEnd(spanneds[0]);
      changeSelection(selStart, selEnd, start, end, true);
      return;
    }

    List<Range> ranges = getCustomSpanRanges();
    if (ranges == null || ranges.size() == 0) {
      return;
    }
    for (int i = 0; i < ranges.size(); i++) {
      Range range = ranges.get(i);
      int startPostion = range.start;
      int endPostion = range.end;
      if (changeSelection(selStart, selEnd, startPostion, endPostion, false)) {
        return;
      }
    }
  }

  public class Range {


    public int start;
    public int end;

    public Range(int start, int end) {
      this.start = start;
      this.end = end;
    }
  }

  private boolean isPrivateMessage() {
    PrivateSpan[] spanneds = getText().getSpans(0, getText().length(), PrivateSpan.class);
    return spanneds != null && spanneds.length > 0;
  }


  private boolean changeSelection(int selStart, int selEnd, int startPostion, int endPostion,
      boolean toEnd) {

    boolean hasChange = false;
    if (selStart == selEnd) {
      if (startPostion != -1 && startPostion < selStart && selStart < endPostion) {
        if (toEnd) {
          setSelection(endPostion);
        } else {
          setSelection(startPostion);
        }
        hasChange = true;
      }
    } else {
      if (startPostion != -1 && startPostion < selStart && selStart < endPostion) {
        if (toEnd) {
          setSelection(endPostion, selEnd);
        } else {
          setSelection(startPostion, selEnd);
        }

        hasChange = true;
      }
      if (endPostion != -1 && startPostion < selEnd && selEnd < endPostion) {
        setSelection(selStart, endPostion);
        hasChange = true;
      }
    }
    return hasChange;
  }


  public void insertPrivateMessage(long id, String name) {

    String showContent;
    if (TextUtils.isEmpty(name)) {
      showContent = "(私信):";
    } else {
      showContent = "To " + name + "(私信):";
    }
    PrivateSpan foregroundColorSpan = new PrivateSpan(getCurrentTextColor());
    foregroundColorSpan.setName(name);
    foregroundColorSpan.setId(id);
    SpannableString spannableString = new SpannableString(showContent);
    spannableString
        .setSpan(foregroundColorSpan, 0, spannableString.length(),
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
    setText(spannableString);
    setSelection(spannableString.length());

  }


  public void insertSpecialStr(String id, String insertContent, boolean rollBack) {

    if (TextUtils.isEmpty(insertContent)) {
      return;
    }
    String showContent = "@" + insertContent + " ";
//    insertModelList.add(insertModel);

    //将特殊字符插入到EditText 中显示
    int index = getSelectionStart();//光标位置
    Editable editable = getText();//原先内容

    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(editable);
    AtSpan foregroundColorSpan = new AtSpan(getCurrentTextColor());
    foregroundColorSpan.setInsertContent(insertContent);
    foregroundColorSpan.setId(id);
    SpannableString spannableString = new SpannableString(showContent);
    spannableString
        .setSpan(foregroundColorSpan, 0, spannableString.length(),
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
    if (rollBack) {
      spannableStringBuilder.delete(index - 1, index);
      index--;
    }
    spannableStringBuilder.insert(index, spannableString);
    setText(spannableStringBuilder);
    setSelection(index + spannableString.length());
    if (requestFocus()) {
      postDelayed(new Runnable() {
        @Override
        public void run() {
          InputMethodManager imm = (InputMethodManager) getContext()
              .getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.showSoftInput(SpEditText.this, InputMethodManager.SHOW_FORCED);
        }
      }, 100);

    }
  }


  public interface OnMentionInputListener {


    void onAtMotion();
  }

  public void setmOnMentionInputListener(
      OnMentionInputListener mOnMentionInputListener) {
    this.mOnMentionInputListener = mOnMentionInputListener;
  }

  public class PrivateSpan extends ForegroundColorSpan {

    private long id;
    private String name;

    public long getId() {
      return id;
    }

    public void setId(long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public PrivateSpan(int color) {
      super(color);
    }

    public PrivateSpan(Parcel src) {
      super(src);
    }
  }

  public static void sortSpans(Editable editable, AtSpan[] spanneds, int left, int right) {
    if (left >= right) {
      return;
    }
    int i = left;
    int j = right;
    AtSpan keySpan = spanneds[left];
    int key = editable.getSpanStart(spanneds[left]);
    while (i < j) {
      while (i < j && key <= editable.getSpanStart(spanneds[j])) {
        j--;
      }
      spanneds[i] = spanneds[j];
      while (i < j && key >= editable.getSpanStart(spanneds[i])) {
        i++;
      }

      spanneds[j] = spanneds[i];
    }

    spanneds[i] = keySpan;
    sortSpans(editable, spanneds, left, i - 1);
    sortSpans(editable, spanneds, i + 1, right);

  }

  public class AtSpan extends ForegroundColorSpan {

    private String id;
    private String insertContent;


    public String getInsertContent() {
      return insertContent;
    }

    public void setInsertContent(String insertContent) {
      this.insertContent = insertContent;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public AtSpan(int color) {
      super(color);
    }

    public AtSpan(Parcel src) {
      super(src);
    }
  }
}
