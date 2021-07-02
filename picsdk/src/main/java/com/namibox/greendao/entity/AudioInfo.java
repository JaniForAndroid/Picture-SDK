package com.namibox.greendao.entity;

import com.google.gson.JsonObject;

/**
 * author : feng
 * creation time : 19-12-24下午4:05
 */
public class AudioInfo {

  public Long id;

  public String audioId;
  public String albumId;
  //封面
  public String cover;
  public String title;
  public String content_id;
  public String content_type;
  public String album_title;
  public String album_cover;
  public String subtitle;
  public String tag;
  public String play_number;
  public String downloadUrl;
  public boolean is_tape_audio;
  public String source_image;
  public String source_description;
  public String source_groupName;
  public JsonObject source_action;
  //播放时长
  public long duration;
  //书页
  public int index;
  //下载状态
  public int state;
  public boolean playing;
  //播放进度
  public int progress;
  public boolean support_download;
  public JsonObject wx_share;

  //music
  public String content;
  public long size;
  public String media_url;

  public AudioInfo(Long id, String audioId, String albumId, String cover,
          String title, String content_id, String content_type,
          String album_title, String album_cover, String subtitle,
          String downloadUrl, long duration, int index, int progress,
          boolean support_download, String content, long size, String media_url) {
      this.id = id;
      this.audioId = audioId;
      this.albumId = albumId;
      this.cover = cover;
      this.title = title;
      this.content_id = content_id;
      this.content_type = content_type;
      this.album_title = album_title;
      this.album_cover = album_cover;
      this.subtitle = subtitle;
      this.downloadUrl = downloadUrl;
      this.duration = duration;
      this.index = index;
      this.progress = progress;
      this.support_download = support_download;
      this.content = content;
      this.size = size;
      this.media_url = media_url;
  }

  public AudioInfo() {
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getMedia_url() {
    return media_url;
  }

  public void setMedia_url(String media_url) {
    this.media_url = media_url;
  }

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAudioId() {
    return this.audioId;
  }

  public void setAudioId(String audioId) {
    this.audioId = audioId;
  }

  public String getAlbumId() {
    return this.albumId;
  }

  public void setAlbumId(String albumId) {
    this.albumId = albumId;
  }

  public String getCover() {
    return this.cover;
  }

  public void setCover(String cover) {
    this.cover = cover;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSubtitle() {
    return this.subtitle;
  }

  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  public String getDownloadUrl() {
    return this.downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }


  public int getIndex() {
    return this.index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public int getState() {
    return this.state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public boolean getPlaying() {
    return this.playing;
  }

  public void setPlaying(boolean playing) {
    this.playing = playing;
  }

  public int getProgress() {
    return this.progress;
  }

  public void setProgress(int progress) {
    this.progress = progress;
  }

  public String getAlbum_title() {
    return this.album_title;
  }

  public void setAlbum_title(String album_title) {
    this.album_title = album_title;
  }

  public String getAlbum_cover() {
    return this.album_cover;
  }

  public void setAlbum_cover(String album_cover) {
    this.album_cover = album_cover;
  }

  public boolean getSupport_download() {
    return this.support_download;
  }

  public void setSupport_download(boolean support_download) {
    this.support_download = support_download;
  }

  public long getDuration() {
    return this.duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public String getContent_id() {
      return this.content_id;
  }

  public void setContent_id(String content_id) {
      this.content_id = content_id;
  }

  public String getContent_type() {
      return this.content_type;
  }

  public void setContent_type(String content_type) {
      this.content_type = content_type;
  }
}

