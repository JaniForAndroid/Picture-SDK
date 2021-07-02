package com.namibox.commonlib.model;

import com.google.gson.JsonObject;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author: Shelter
 * Create time: 2020/2/19, 15:39.
 */
public class EvaluationNetResult  {

  /**
   * retcode : SUCCESS
   * total_count : 2
   * rank : 80
   * score_range : [{"value":0.07,"desc":"<60"},{"value":0.16,"desc":"60-70"},{"value":0.23,"desc":"70-80"},{"inscope":true,"value":0.5,"desc":"80-90"},{"value":0.04,"desc":">90"}]
   * result_url : https://wweb.namibox.com/lesson/oral_test_result?_app_template=fullscreen&user_id=3127953&book_id=tape3a_000002&page_id=2&match_id=
   * teacher_comment : 你的发音太棒了！这就是一段测试文案，产品给定文案后更新
   * error_desc : 上报成绩成功！
   * wx_share : {"url_link": "https://wweb.namibox.com/lesson/oral_test_share?img_result=NB2HI4DTHIXS653GFZXGC3LJMJXXQLTDN5WS65LTMVZC6MZRGI3TSNJTF5XXEYLML52GK43UL5UW2YLHMVPXG2DBOJSS65DBOBSTGYK7GAYDAMBQGIXTCNJYGIYDSOBUGQ4DGOBSFZYG4ZY=&book_id=tape3a_000002&page_id=2&score=79&uid=3127953", "share_friend": "\u6211\u5728\u53e3\u8bed\u8bc4\u6d4b\u4e2d\u5f97\u5230\u4e86 79 \u5206\uff0c\u4f60\u4e5f\u6765\u8bd5\u8bd5\u5427\uff01", "report_url": "https://wweb.namibox.com/auth/points", "share_title": "\u7eb3\u7c73\u76d2\u53e3\u8bed\u8bc4\u6d4b - \u6700\u6709\u6548\u7684\u82f1\u8bed\u53e3\u8bed\u968f\u5802\u7ec3\u4e60", "url_image": "https://vsra.namibox.com/tina/static/app/icon/v2/school/oral_test.png", "share_content": "\u6211\u5728\u53e3\u8bed\u8bc4\u6d4b\u4e2d\u5f97\u5230\u4e86 79 \u5206\uff0c\u4f60\u4e5f\u6765\u8bd5\u8bd5\u5427\uff01"}
   * error_code : 0
   * description : Result Reported!
   */

  public String retcode;
  public int total_count;
  public int rank;
  public String result_url;
  public String teacher_comment;
  public String error_desc;
  public String wx_share;
  public int error_code;
  public String description;
  public ArrayList<ScoreRangeBean> score_range;

  //云校
  public JsonObject data;

  public static class ScoreRangeBean implements Serializable {

    /**
     * value : 0.07
     * desc : <60
     * inscope : true
     */

    public double value;
    public String desc;
    public boolean inscope;

    @Override
    public String toString() {
      return "ScoreRangeBean{" +
          "value=" + value +
          ", desc='" + desc + '\'' +
          ", inscope=" + inscope +
          '}';
    }
  }

  @Override
  public String toString() {
    return "EvaluationNetResult{" +
        "retcode='" + retcode + '\'' +
        ", total_count=" + total_count +
        ", rank=" + rank +
        ", result_url='" + result_url + '\'' +
        ", teacher_comment='" + teacher_comment + '\'' +
        ", error_desc='" + error_desc + '\'' +
        ", wx_share='" + wx_share + '\'' +
        ", error_code=" + error_code +
        ", description='" + description + '\'' +
        ", score_range=" + score_range +
        '}';
  }
}
