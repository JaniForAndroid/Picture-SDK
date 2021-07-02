package sdk.model;

import android.support.annotation.Keep;

@Keep
public class DubbingResultBean {

  // 本次配音得分
  private int score_avg;
  // 完整度
  private int integrity_avg;
  //流畅度
  private int fluency_avg;
  //准确度
  private int pron_avg;
  //合成视频oss地址
  private String video_key;
  // 题目总数
  private int answer_num;
  // 答题正确个数
  private int answer_right_num;
  // 答题正确率
  private int answer_rate;
  // 书本名称
  private String text;
  // 书本中文名称
  private String chinese_name;
  // 学习报告标题
  private String title;
  // 学习单词数
  private int word_num;
  // 配音得分
  private int dubbing_score;
  // 已读绘本数
  private int pb_num;
  // 书本Id
  private int milesson_item_id;
  // 书本封面
  private String thumb_url;

  public DubbingResultBean(int score_avg, int integrity_avg, int fluency_avg, int pron_avg,
      String video_key) {
    this.score_avg = score_avg;
    this.integrity_avg = integrity_avg;
    this.fluency_avg = fluency_avg;
    this.pron_avg = pron_avg;
    this.video_key = video_key;
  }

  public int getScore_avg() {
    return score_avg;
  }

  public void setScore_avg(int score_avg) {
    this.score_avg = score_avg;
  }

  public int getIntegrity_avg() {
    return integrity_avg;
  }

  public void setIntegrity_avg(int integrity_avg) {
    this.integrity_avg = integrity_avg;
  }

  public int getFluency_avg() {
    return fluency_avg;
  }

  public void setFluency_avg(int fluency_avg) {
    this.fluency_avg = fluency_avg;
  }

  public int getPron_avg() {
    return pron_avg;
  }

  public void setPron_avg(int pron_avg) {
    this.pron_avg = pron_avg;
  }

  public String getVideo_key() {
    return video_key;
  }

  public void setVideo_key(String video_key) {
    this.video_key = video_key;
  }

  public int getAnswer_num() {
    return answer_num;
  }

  public void setAnswer_num(int answer_num) {
    this.answer_num = answer_num;
  }

  public int getAnswer_right_num() {
    return answer_right_num;
  }

  public void setAnswer_right_num(int answer_right_num) {
    this.answer_right_num = answer_right_num;
  }

  public int getAnswer_rate() {
    return answer_rate;
  }

  public void setAnswer_rate(int answer_rate) {
    this.answer_rate = answer_rate;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getChinese_name() {
    return chinese_name;
  }

  public void setChinese_name(String chinese_name) {
    this.chinese_name = chinese_name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getWord_num() {
    return word_num;
  }

  public void setWord_num(int word_num) {
    this.word_num = word_num;
  }

  public int getDubbing_score() {
    return dubbing_score;
  }

  public void setDubbing_score(int dubbing_score) {
    this.dubbing_score = dubbing_score;
  }

  public int getPb_num() {
    return pb_num;
  }

  public void setPb_num(int pb_num) {
    this.pb_num = pb_num;
  }

  public int getMilesson_item_id() {
    return milesson_item_id;
  }

  public void setMilesson_item_id(int milesson_item_id) {
    this.milesson_item_id = milesson_item_id;
  }

  public String getThumb_url() {
    return thumb_url;
  }

  public void setThumb_url(String thumb_url) {
    this.thumb_url = thumb_url;
  }

  @Override
  public String toString() {
    return "DubbingResultBean{" +
        "score_avg=" + score_avg +
        ", integrity_avg=" + integrity_avg +
        ", fluency_avg=" + fluency_avg +
        ", pron_avg=" + pron_avg +
        ", video_key='" + video_key + '\'' +
        ", answer_num=" + answer_num +
        ", answer_right_num=" + answer_right_num +
        ", answer_rate=" + answer_rate +
        ", text='" + text + '\'' +
        ", chinese_name='" + chinese_name + '\'' +
        ", title='" + title + '\'' +
        ", word_num=" + word_num +
        ", dubbing_score=" + dubbing_score +
        ", pb_num=" + pb_num +
        ", milesson_item_id=" + milesson_item_id +
        ", thumb_url='" + thumb_url + '\'' +
        '}';
  }
}