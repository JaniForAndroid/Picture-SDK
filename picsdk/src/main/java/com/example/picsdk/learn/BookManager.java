package com.example.picsdk.learn;

import android.os.Parcelable;
import android.text.TextUtils;

import com.example.picsdk.model.PicType;
import com.example.picsdk.model.ProductItem;
import com.example.picsdk.model.ProductItem.BookLearning;
import com.example.picsdk.model.ProductItem.Challenge;
import com.example.picsdk.model.VideoPicInfo;
import com.namibox.greendao.entity.AudioInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * author : feng
 * creation time : 19-11-1上午9:28
 */
public class BookManager {

  public static BookManager getInstance() {
    return Holder.instance;
  }

  private static class Holder {
    private static BookManager instance = new BookManager();
  }

  public static void setBookManager(BookManager bookManager) {
    Holder.instance = bookManager;
  }

  private long milesson_id;
  private long milesson_item_id;

  private List<ProductItem.Challenge> challenges;

  private List<BookLearning.Link> links;

  private String productName;
  private String wordNumber;

  private ArrayList<PicType> typeList;
  private int index = 0;
  private boolean isHomeWork = false;
  private boolean isHomeWorkWatch = false;
  private String match_id = "learn";
  private long homeworkId;
  private long stu_hw_id;
  private int link_num;
  private String lesson_name;

  private List<AudioInfo> playlist = new ArrayList<>();
  private List<VideoPicInfo> videoPicInfos = new ArrayList<>();
  private AudioInfo currentAudio;

  public String getLesson_name() {
    return lesson_name;
  }

  public void setLesson_name(String lesson_name) {
    this.lesson_name = lesson_name;
  }

  public int getLink_num() {
    return link_num;
  }

  public void setLink_num(int link_num) {
    this.link_num = link_num;
  }

  public List<VideoPicInfo> getVideoPicInfos() {
    return videoPicInfos;
  }

  public void setVideoPicInfos(List<VideoPicInfo> videoPicInfos) {
    this.videoPicInfos = videoPicInfos;
  }

  public List<AudioInfo> getPlaylist() {
    return playlist;
  }

  public void setPlaylist(List<AudioInfo> playlist) {
    this.playlist = playlist;
  }

  public AudioInfo getCurrentAudio() {
    if (currentAudio == null) {
      if (playlist.size() == 0) {
        return null;
      }
      return playlist.get(0);
    }
    return currentAudio;
  }

  /**
   * 返回下一个音频，null表示没有下一首
   *
   * @return 下一个音频
   */
  public AudioInfo getNextAudio() {
    if (playlist.size() == 0) {
      return null;
    }
    if (currentAudio == null) {
      return playlist.get(0);
    }
    int currentIndex = 0;
    for (int i = 0; i < playlist.size(); i++) {
      AudioInfo audio = playlist.get(i);
      if (TextUtils.equals(audio.audioId, currentAudio.audioId)) {
        currentIndex = i;
        break;
      }
    }
    int nextIndex = currentIndex + 1;
    if (nextIndex > playlist.size() - 1) {
      return null;
    }
    return playlist.get(nextIndex);
  }

  /**
   * 返回上一个音频，null表示没有上一首
   *
   * @return 上一个音频
   */
  public AudioInfo getPreAudio() {
    if (playlist.size() == 0) {
      return null;
    }
    if (currentAudio == null) {
      return playlist.get(0);
    }
    int currentIndex = 0;
    for (int i = 0; i < playlist.size(); i++) {
      AudioInfo audio = playlist.get(i);
      if (TextUtils.equals(audio.audioId, currentAudio.audioId)) {
        currentIndex = i;
        break;
      }
    }
    int nextIndex = currentIndex - 1;
    if (nextIndex < 0) {
      return null;
    }
    return playlist.get(nextIndex);
  }

  public void setCurrentAudio(AudioInfo currentAudio) {
    this.currentAudio = currentAudio;
  }

  public long getStu_hw_id() {
    return stu_hw_id;
  }

  public void setStu_hw_id(long stu_hw_id) {
    this.stu_hw_id = stu_hw_id;
  }

  public long getHomeworkId() {
    return homeworkId;
  }

  public void setHomeworkId(long homeworkId) {
    this.homeworkId = homeworkId;
  }

  public String getMatch_id() {
    return match_id;
  }

  public void setMatch_id(String match_id) {
    this.match_id = match_id;
  }

  public ArrayList<PicType> getTypeList() {
    return typeList;
  }

  public void setTypeList(ArrayList<PicType> typeList) {
    this.typeList = typeList;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public boolean isHomeWork() {
    return isHomeWork;
  }

  public void setHomeWork(boolean homeWork) {
    isHomeWork = homeWork;
  }

  public boolean isHomeWorkWatch() {
    return isHomeWorkWatch;
  }

  public void setHomeWorkWatch(boolean homeWorkWatch) {
    isHomeWorkWatch = homeWorkWatch;
  }

  public String getWordNumber() {
    return wordNumber;
  }

  public void setWordNumber(String wordNumber) {
    this.wordNumber = wordNumber;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public void setMilesson_id(long milesson_id) {
    this.milesson_id = milesson_id;
  }

  public long getMilesson_item_id() {
    return milesson_item_id;
  }

  public void setMilesson_item_id(long milesson_item_id) {
    this.milesson_item_id = milesson_item_id;
  }

  public void setChallenges(List<Challenge> challenges) {
    this.challenges = challenges;
  }

  public void setLinks(List<BookLearning.Link> links) {
    this.links = links;
  }

  public long getMilesson_id() {
    return milesson_id;
  }

  public List<Challenge> getChallenges() {
    return challenges;
  }

  public Challenge getBookLearning() {
    if (challenges != null && challenges.size() == 4) {
      return challenges.get(0);
    }
    return null;
  }

  public Challenge getWordChallenge() {
    if (challenges != null && challenges.size() == 4) {
      return challenges.get(1);
    }
    return null;
  }

  public Challenge getReadChallenge() {
    if (challenges != null && challenges.size() == 4) {
      return challenges.get(2);
    }
    return null;
  }

  public Challenge getPlayChallenge() {
    if (challenges != null && challenges.size() == 4) {
      return challenges.get(3);
    }
    return null;
  }

  public List<BookLearning.Link> getLinks() {
    return links;
  }

  public int linkIndex(String type) {
    if (links != null) {
      for (int i = 0; i < links.size(); i++) {
        if (type.equals(links.get(i).type)) {
          return i;
        }
      }
    }
    return -1;
  }
}
