package com.example.picsdk.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;

import com.example.picsdk.R;
import com.example.picsdk.model.Exercise;
import com.example.picsdk.model.Exercise.Sequence;
import com.namibox.util.Spanny;
import com.namibox.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextInputView extends android.support.v7.widget.AppCompatTextView {
  private int mHighLightPosition;
  private static final String UNDERLINE = "_______";//[]挖空+
  private List<Exercise.Sequence> mOriginalSourceSequence = new ArrayList<>();
  private List<Exercise.Sequence> mCorrectSequence = new ArrayList<>();
  private List<Exercise.Sequence> mSourceSequence;
  private List<Exercise.Sequence> mDestinationSequence;
  private ArrayList<String> mUser_Answer = new ArrayList<>();
  private boolean mIsShowResult;
  private boolean mIsClickAble = true;
  private boolean isAnswerCorrect = true;

  public TextInputView(Context context) {
    super(context);
  }

  public TextInputView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setData(List<Exercise.Sequence> sourceSequence, List<Exercise.Sequence> destinationSequence) {
    mDestinationSequence = destinationSequence;
    mSourceSequence = sourceSequence;
    initData();
  }

  private void initData() {
    Pattern pattern = Pattern.compile("\\[([^\\[\\\\\\]]*)\\]");
    Matcher matcher;
    for (Exercise.Sequence sequence : mSourceSequence) {
      matcher = pattern.matcher(sequence.content);
      if (matcher.find()) {
        sequence.correctStart = matcher.start();
      }
      Exercise.Sequence sequence1 = new Exercise.Sequence();
      sequence1.content = sequence.content;
      mOriginalSourceSequence.add(sequence1);
    }
    mCorrectSequence.addAll(mDestinationSequence);
    Collections.sort(mCorrectSequence, (o1, o2) -> o1.index > o2.index ? 1 : -1);
    setSpanny();
  }

  public void setSpanny() {
    Pattern pattern = Pattern.compile("\\[([^\\[\\\\\\]]*)\\]");
    Matcher matcher;
    Spanny spanny = new Spanny();
    TextPaint textPaint = new TextPaint();
    textPaint.setStrokeWidth(Utils.dp2px(getContext(), 6));
    textPaint.setColor(0xff333333);
    textPaint.setFakeBoldText(true);
    UnderlineSpan underlineSpan = new UnderlineSpan();
    underlineSpan.updateDrawState(textPaint);
    for (int i = 0; i < mSourceSequence.size(); i++) {
      int correctStart = mSourceSequence.get(i).correctStart;
      String content = mSourceSequence.get(i).content;
      matcher = pattern.matcher(content);
      if (matcher.find()) {
        int start = matcher.start();
        int end = matcher.end();
        spanny.append(content.substring(0, start));
        final int finalI = i;
        for (int j = 0; j < 5; j++) {
          ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
              if (mIsClickAble)
                if (finalI != mHighLightPosition) {
                  mHighLightPosition = finalI;
                  setSpanny();
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
              ds.setColor(0xff333333);
              ds.setUnderlineText(false);
            }
          };
          spanny.append("是", new LineReplaceSpace(getContext(), i == mHighLightPosition, false), clickableSpan);
        }
        if (end < content.length()) {
          spanny.append(content.substring(end));
        }
      } else {
        for (Exercise.Sequence sequence : mDestinationSequence) {
          String tempContent = content.substring(correctStart);
          if (tempContent.indexOf(sequence.content) == 0) {
            int index = content.indexOf(sequence.content);
            spanny.append(content.substring(0, index));
            final int finalI = i;
            final String finalContent = sequence.content;
            for (int j = 0; j < sequence.content.length(); j++) {
              ClickableSpan clickableSpan = new ClickableSpan() {

                @Override
                public void onClick(@NonNull View widget) {
                  if (mIsClickAble)
                    for (int j = 0; j < mDestinationSequence.size(); j++) {
                      if (TextUtils.equals(finalContent, mDestinationSequence.get(j).content)) {
                        mHighLightPosition = finalI;
                        mSourceSequence.get(finalI).content = mOriginalSourceSequence.get(finalI).content;
                        setSpanny();
                        if (mOnTextInPutListener != null) {
                          mOnTextInPutListener.clearText(finalContent);
                        }
                      }
                    }
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                  ds.setUnderlineText(true);
                  ds.setColor(0xff333333);
                }
              };
              boolean isCorrect = false;
              if (mIsShowResult) {
                isCorrect = content.contains(mCorrectSequence.get(i).content) ? true : false;
              }
              String ch = String.valueOf(sequence.content.charAt(j));
              if (j == 0) {
                spanny.append("__", clickableSpan, new LineReplaceSpace(getContext(), false, false));
              }
              spanny.append(ch, clickableSpan, new LineReplaceSpace(getContext(), false, true));
              if (j == sequence.content.length() - 1) {
                spanny.append("__", clickableSpan, new LineReplaceSpace(getContext(), false, false));
                if (mIsShowResult) {
                  if (!mUser_Answer.contains(tempContent))
                    mUser_Answer.add(tempContent);
                  if (!isCorrect) {
                    isAnswerCorrect = false;
                  }
                  Bitmap bitmap = BitmapFactory.decodeResource(getResources(), isCorrect ? R.drawable.ic_sort_correct : R.drawable.ic_sort_error);
                  spanny.append("____", clickableSpan, new DrawableReplaceSpace(getContext(), bitmap));
                }
              }
            }
            int end = index + sequence.content.length();
            if (end < content.length()) {
              spanny.append(content.substring(end));
            }
            break;
          }
        }
      }
    }
    setText(spanny);
  }

  public ArrayList<String> getUserAnswer() {
    return mUser_Answer;
  }

  public boolean getUserCorrect() {
    return isAnswerCorrect;
  }

  private void nextHighLightText() {
    Pattern pattern = Pattern.compile("\\[([^\\[\\\\\\]]*)\\]");
    Matcher matcher;
    boolean isFinish = true;
    for (int i = 0; i < mSourceSequence.size(); i++) {
      matcher = pattern.matcher(mSourceSequence.get(i).content);
      if (matcher.find()) {
        mHighLightPosition = i;
        setSpanny();
        isFinish = false;
        break;
      }
    }
    boolean result = false;
    if (isFinish) {
      for (int i = 0; i < mSourceSequence.size(); i++) {
        Sequence sequence = mSourceSequence.get(i);
        if (sequence.content.indexOf(mCorrectSequence.get(i).content) != sequence.correctStart) {
          break;
        }
        if (i == mSourceSequence.size() - 1) {
          result = true;
        }
      }
    }
    if (mOnTextInPutListener != null) {
      mOnTextInPutListener.progress(isFinish, result);
    }
  }

  public void inputText(String text) {
    Pattern pattern = Pattern.compile("\\[([^\\[\\\\\\]]*)\\]");
    mSourceSequence.get(mHighLightPosition).content = mSourceSequence.get(mHighLightPosition).content.replaceFirst(pattern.pattern(), text);
    setSpanny();
    nextHighLightText();
  }

  public void clearText(String text) {
    for (int i = 0; i < mSourceSequence.size(); i++) {
      if (mSourceSequence.get(i).content.contains(text)) {
        mSourceSequence.get(i).content = mOriginalSourceSequence.get(i).content;
        mHighLightPosition = i;
        nextHighLightText();
        setSpanny();
        break;
      }
    }
  }

  public void showResult(boolean isShowResult) {
    mIsShowResult = isShowResult;
    setSpanny();
  }

  public void showClickAble(boolean isClick) {
    mIsClickAble = isClick;
  }

  private OnTextInPutListener mOnTextInPutListener;

  public void setOnTextInPutListener(OnTextInPutListener onTextInPutListener) {
    mOnTextInPutListener = onTextInPutListener;
  }

  public interface OnTextInPutListener {
    void clearText(String text);

    void progress(boolean isFinish, boolean result);

  }

}
