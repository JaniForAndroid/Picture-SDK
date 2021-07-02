package com.namibox.commonlib.view.emojicon.domain;


import android.text.TextUtils;
import com.namibox.tools.CommonHelper;
import com.namibox.util.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimDefaultEmojiconDatas {

  private static final String TAG = "EaseDefaultEmojiconData";

  private static String[] emojiList = new String[]{
      "[微笑]", "[撇嘴]", "[色]", "[发呆]", "[得意]", "[流泪]", "[害羞]",
      "[闭嘴]", "[睡]", "[大哭]", "[尴尬]", "废弃", "[调皮]", "[龇牙]",
      "[惊讶]", "[难过]", "[酷]", "[冷汗]", "废弃", "[吐]",

      "[偷笑]", "[可爱]", "[白眼]", "废弃", "[饥饿]", "[困]", "废弃",
      "[流汗]", "[憨笑]", "[大兵]", "[奋斗]", "废弃", "[疑问]", "[嘘…]",
      "[晕]", "废弃", "[衰]", "废弃", "废弃", "[再见]",

      "[擦汗]", "[抠鼻]", "[鼓掌]", "[糗大了]", "[坏笑]", "[左哼哼]", "[右哼哼]",
      "[哈欠]", "废弃", "[委屈]", "[快哭了]", "[阴险]", "[亲亲]", "废弃",
      "[可怜]", "废弃", "[西瓜]", "[啤酒]", "[篮球]", "[乒乓球]",

      "[咖啡]", "[饭]", "废弃", "[玫瑰]", "[凋谢]", "[示爱]", "[爱心]",
      "[心碎]", "[蛋糕]", "废弃", "废弃", "废弃", "[足球]", "[瓢虫]",
      "废弃", "[月亮]", "[太阳]", "[礼物]", "[拥抱]", "[强]",

      "废弃", "[握手]", "[胜利]", "[抱拳]", "废弃", "[拳头]", "废弃",
      "[爱你]", "[NO]", "[OK]", "[爱情]", "[飞吻]", "[跳跳]", "[发抖]",
      "[怄火]", "[转圈]", "[磕头]", "[回头]", "[跳绳]", "[挥手]"
  };
  private static List<File> emojiPngs = new ArrayList<>();
  private static List<File> emojiGifs = new ArrayList<>();

  static {
    for (int i = 1; i < 6; i++) {
      for (int j = 0; j < 3; j++) {
        for (int k = 0; k < 7; k++) {
          if (j == 2 && k == 6) {
            continue;
          }
          File pngFile;
          File gifFile;

          pngFile = new File(
              FileUtil.getLocalAppCacheDir(CommonHelper.getInstance().getContext()),
              "/r.namibox.com/static/e/e" + i + "/" + k + "_" + j + ".png");
          gifFile = new File(
              FileUtil.getLocalAppCacheDir(CommonHelper.getInstance().getContext()),
              "/r.namibox.com/static/e/e" + i + "/" + k + "_" + j + ".gif");
          emojiPngs.add(pngFile);
          emojiGifs.add(gifFile);
        }
      }
    }
  }


  private static final TimEmojicon[] DATA = createData();

  private static TimEmojicon[] createData() {
    TimEmojicon[] datas = new TimEmojicon[emojiList.length];
    List<TimEmojicon> list = new ArrayList<>();
    for (int i = 0; i < emojiList.length; i++) {
      datas[i] = new TimEmojicon(emojiPngs.get(i), emojiGifs.get(i), emojiList[i]);
      if (!TextUtils.equals("废弃",emojiList[i])) {
        list.add(new TimEmojicon(emojiPngs.get(i), emojiGifs.get(i), emojiList[i]));
      }
    }
    TimEmojicon[] realDatas = new TimEmojicon[list.size()];
    for (int i = 0; i < list.size(); i++) {
      realDatas[i] = list.get(i);
    }
    return realDatas;
  }

  public static TimEmojicon[] getData() {
    return DATA;
  }

  public static List<String> getEmojis(){
    return Arrays.asList(emojiList);
  }
}
