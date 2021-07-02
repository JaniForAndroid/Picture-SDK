package com.namibox.commonlib.view.emojicon.domain;

import com.namibox.commonlib.view.emojicon.domain.TimEmojicon.Type;
import java.util.List;

/**
 * 一组表情所对应的实体类
 */
public class TimEmojiconGroupEntity {

  /**
   * 表情数据
   */
  private List<TimEmojicon> emojiconList;
  /**
   * 图片
   */
  private int icon;
  /**
   * 组名
   */
  private String name;
  /**
   * 表情类型
   */
  private Type type;

  public TimEmojiconGroupEntity() {
  }

  public TimEmojiconGroupEntity(int icon, List<TimEmojicon> emojiconList) {
    this.icon = icon;
    this.emojiconList = emojiconList;
    type = Type.NORMAL;
  }

  public TimEmojiconGroupEntity(int icon, List<TimEmojicon> emojiconList, Type type) {
    this.icon = icon;
    this.emojiconList = emojiconList;
    this.type = type;
  }

  public List<TimEmojicon> getEmojiconList() {
    return emojiconList;
  }

  public void setEmojiconList(List<TimEmojicon> emojiconList) {
    this.emojiconList = emojiconList;
  }

  public int getIcon() {
    return icon;
  }

  public void setIcon(int icon) {
    this.icon = icon;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }


}
