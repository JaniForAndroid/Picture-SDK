package com.namibox.commonlib.view.emojicon.domain;

import java.io.File;

public class TimEmojicon {

  public TimEmojicon() {
  }

  /**
   * 构造函数
   *
   * @param icon 静态图片resource id
   * @param emojiText 表情emoji文本内容
   */
  public TimEmojicon(int icon, String emojiText) {
    this.icon = icon;
    this.emojiText = emojiText;
    this.type = Type.NORMAL;
  }

  public TimEmojicon(File iconPng, File iconGif, String emojiText) {
    this.iconSet = new IconSet(iconGif, iconPng);
    this.emojiText = emojiText;
    this.type = Type.NORMAL;
  }

  /**
   * 构造函数
   *
   * @param icon 静态图片resource id
   * @param emojiText emojiText 表情emoji文本内容
   * @param type 表情类型
   */
  public TimEmojicon(int icon, String emojiText, Type type) {
    this.icon = icon;
    this.emojiText = emojiText;
    this.type = type;
  }


  /**
   * 唯一识别号
   */
  private String identityCode;

  /**
   * static icon resource id
   */
  private IconSet iconSet;
  private int icon;

  /**
   * dynamic icon resource id
   */
  private int bigIcon;

  /**
   * 表情emoji文本内容,大表情此项内容可以为null
   */
  private String emojiText;

  /**
   * 表情所对应的名称
   */
  private String name;

  /**
   * 普通or大表情
   */
  private Type type;

  /**
   * 表情静态图片地址
   */
  private String iconPath;

  /**
   * 大表情图片地址
   */
  private String bigIconPath;


  public IconSet getIconSet() {
    return iconSet;
  }

  public void setIconSet(IconSet iconSet) {
    this.iconSet = iconSet;
  }

  /**
   * 获取静态图片(小图片)资源id
   */
  public int getIcon() {
    return icon;
  }


  /**
   * 设置静态图片资源id
   */
  public void setIcon(int icon) {
    this.icon = icon;
  }


  /**
   * 获取大图片资源id
   */
  public int getBigIcon() {
    return bigIcon;
  }


  /**
   * 设置大图片资源id
   */
  public void setBigIcon(int dynamicIcon) {
    this.bigIcon = dynamicIcon;
  }


  /**
   * 获取emoji文本内容
   */
  public String getEmojiText() {
    return emojiText;
  }


  /**
   * 设置emoji文本内容
   */
  public void setEmojiText(String emojiText) {
    this.emojiText = emojiText;
  }

  /**
   * 获取表情名称
   */
  public String getName() {
    return name;
  }

  /**
   * 设置表情名称
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * 获取表情类型
   */
  public Type getType() {
    return type;
  }


  /**
   * 设置表情类型
   */
  public void setType(Type type) {
    this.type = type;
  }


  /**
   * 获取静态图片地址
   */
  public String getIconPath() {
    return iconPath;
  }


  /**
   * 设置静态图片地址
   */
  public void setIconPath(String iconPath) {
    this.iconPath = iconPath;
  }


  /**
   * 获取大图(动态地址)地址()
   */
  public String getBigIconPath() {
    return bigIconPath;
  }


  /**
   * 设置大图(动态地址)地址
   */
  public void setBigIconPath(String bigIconPath) {
    this.bigIconPath = bigIconPath;
  }

  /**
   * 获取识别码
   */
  public String getIdentityCode() {
    return identityCode;
  }

  /**
   * 设置识别码
   */
  public void setIdentityCode(String identityCode) {
    this.identityCode = identityCode;
  }

  public static final String newEmojiText(int codePoint) {
    if (Character.charCount(codePoint) == 1) {
      return String.valueOf(codePoint);
    } else {
      return new String(Character.toChars(codePoint));
    }
  }


  public enum Type {
    /**
     * 普通表情，可以一次输入多个到edittext
     */
    NORMAL,
    /**
     * 大表情，点击之后直接发送
     */
    BIG_EXPRESSION
  }
}
