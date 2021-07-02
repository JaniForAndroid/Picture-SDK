package com.example.picsdk.model;

import java.util.ArrayList;

/**
 * Author: ryancheng
 * Create time: 2014/12/23 17:51
 */
public class Book {

  public BookAudio bookaudio;
  public ArrayList<BookPage> bookpage;

  public static class BookAudio {
    public ArrayList<BookItem> bookitem;
    public String bookid;
    public String bookname;
    public String itemname;
    public String memo;
    public String subtitle;
    public String booktype;
    public String thumb_url;
  }

  public static class BookItem {
    public boolean clickread;
    public float duration;
    public String mp3name;
    public String mp3url;
    public String mp3url_hiq;
    public int page;
    public String title;
    public String unit;
    public int[] pageindex;

    @Override
    public String toString() {
      return title;
    }
  }

  public static class BookPage {
    public String page_url;
    public String page_name;
    public ArrayList<TrackInfo> track_info;
  }

  public static class TrackInfo implements Comparable<TrackInfo> {

    public String mp3name;
    public String mp3url;
    public String mp3url_hiq;
    public float track_auend;
    public float track_austart;
    public int track_id;
    public String track_genre;
    public String track_txt;
    public int page_index;


    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TrackInfo trackInfo = (TrackInfo) o;

      return track_id == trackInfo.track_id;

    }

    @Override
    public int hashCode() {
      return track_id;
    }

    @Override
    public int compareTo(TrackInfo another) {
      if (another == null) {
        return 1;
      } else if (track_id > another.track_id) {
        return 1;
      } else if (track_id < another.track_id) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public String toString() {
      return track_id + "[" + track_austart + "->" + track_auend + "]";
    }
  }
}
