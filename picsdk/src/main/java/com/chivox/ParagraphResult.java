package com.chivox;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Roy.chen on 2017/7/24.
 */

public class ParagraphResult {

  /**
   * recordId : 11e7703594782eae93522f61
   * tokenId : 1500876109
   * applicationId : a132
   * audioUrl : http://files.cloud.ssapi.cn:8080/a132/11e7703594782eae93522f61
   * dtLastResponse : 2017-07-24 14:02:46:209
   * params : {"app":{"timestamp":"1500876109","sig":"2a805bbd2279177cc65ac94ed4537385f447d81a","applicationId":"a132","userId":"guest","clientId":""},"request":{"coreType":"en.pred.score","tokenId":"1500876109","refText":"Grandma Makes Our Home .Grandma helps me wash.  I love Grandma.Grandma helps me dress.  I love Grandma.Grandma helps me clean.  I love Grandma.Grandma helps me share.  I love Grandma.Grandma plays with us!  We love Grandma!","rank":100,"attachAudioUrl":1},"audio":{"sampleRate":16000,"channel":1,"sampleBytes":2,"audioType":"ogg"}}
   * eof : 1
   * refText : Grandma Makes Our Home .Grandma helps me wash.  I love Grandma.Grandma helps me dress.  I love Grandma.Grandma helps me clean.  I love Grandma.Grandma helps me share.  I love Grandma.Grandma plays with us!  We love Grandma!
   * result : {"fluency":73,"pron":64,"version":"0.0.80.2017.7.11.18:11:53","textmode":0,"delaytime":46,"rank":100,"res":"eng.snt.online.1.0","info":{"volume":90,"clip":0,"snr":12.741999,"tipId":0},"overall":65,"integrity":71,"systime":57744,"pretime":131,"useref":1,"details":[{"snt_details":[{"char":"Grandma","score":41},{"char":"Makes","score":97},{"char":"Our","score":87},{"char":"Home","score":99},{"char":"Grandma","score":89},{"char":"helps","score":94},{"char":"me","score":97},{"char":"wash","score":59}],"score":80,"text":"Grandma Makes Our Home Grandma helps me wash"},{"snt_details":[{"char":"I","score":52},{"char":"love","score":95},{"char":"Grandma.Grandma","score":48},{"char":"helps","score":0},{"char":"me","score":0},{"char":"dress","score":0}],"score":34,"text":"I love Grandma.Grandma helps me dress"},{"snt_details":[{"char":"I","score":0},{"char":"love","score":0},{"char":"Grandma.Grandma","score":44},{"char":"helps","score":81},{"char":"me","score":89},{"char":"clean","score":98}],"score":57,"text":"I love Grandma.Grandma helps me clean"},{"snt_details":[{"char":"I","score":92},{"char":"love","score":99},{"char":"Grandma.Grandma","score":35},{"char":"helps","score":91},{"char":"me","score":92},{"char":"share","score":92}],"score":68,"text":"I love Grandma.Grandma helps me share"},{"snt_details":[{"char":"I","score":0},{"char":"love","score":77},{"char":"Grandma.Grandma","score":32},{"char":"plays","score":90},{"char":"with","score":96},{"char":"us","score":99}],"score":61,"text":"I love Grandma.Grandma plays with us"},{"snt_details":[{"char":"We","score":90},{"char":"love","score":97},{"char":"Grandma","score":94}],"score":94,"text":"We love Grandma"}],"wavetime":57590,"precision":1}
   */

  private String recordId;
  private String tokenId;
  private String applicationId;
  private String audioUrl;
  private String dtLastResponse;
  private ParamsBean params;
  private int eof;
  private String refText;
  private ResultBean result;

  public String getRecordId() {
    return recordId;
  }

  public void setRecordId(String recordId) {
    this.recordId = recordId;
  }

  public String getTokenId() {
    return tokenId;
  }

  public void setTokenId(String tokenId) {
    this.tokenId = tokenId;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public String getAudioUrl() {
    return audioUrl;
  }

  public void setAudioUrl(String audioUrl) {
    this.audioUrl = audioUrl;
  }

  public String getDtLastResponse() {
    return dtLastResponse;
  }

  public void setDtLastResponse(String dtLastResponse) {
    this.dtLastResponse = dtLastResponse;
  }

  public ParamsBean getParams() {
    return params;
  }

  public void setParams(ParamsBean params) {
    this.params = params;
  }

  public int getEof() {
    return eof;
  }

  public void setEof(int eof) {
    this.eof = eof;
  }

  public String getRefText() {
    return refText;
  }

  public void setRefText(String refText) {
    this.refText = refText;
  }

  public ResultBean getResult() {
    return result;
  }

  public void setResult(ResultBean result) {
    this.result = result;
  }

  public static class ParamsBean {

    /**
     * app : {"timestamp":"1500876109","sig":"2a805bbd2279177cc65ac94ed4537385f447d81a","applicationId":"a132","userId":"guest","clientId":""}
     * request : {"coreType":"en.pred.score","tokenId":"1500876109","refText":"Grandma Makes Our Home .Grandma helps me wash.  I love Grandma.Grandma helps me dress.  I love Grandma.Grandma helps me clean.  I love Grandma.Grandma helps me share.  I love Grandma.Grandma plays with us!  We love Grandma!","rank":100,"attachAudioUrl":1}
     * audio : {"sampleRate":16000,"channel":1,"sampleBytes":2,"audioType":"ogg"}
     */

    private AppBean app;
    private RequestBean request;
    private AudioBean audio;

    public AppBean getApp() {
      return app;
    }

    public void setApp(AppBean app) {
      this.app = app;
    }

    public RequestBean getRequest() {
      return request;
    }

    public void setRequest(RequestBean request) {
      this.request = request;
    }

    public AudioBean getAudio() {
      return audio;
    }

    public void setAudio(AudioBean audio) {
      this.audio = audio;
    }

    public static class AppBean {

      /**
       * timestamp : 1500876109
       * sig : 2a805bbd2279177cc65ac94ed4537385f447d81a
       * applicationId : a132
       * userId : guest
       * clientId :
       */

      private String timestamp;
      private String sig;
      private String applicationId;
      private String userId;
      private String clientId;

      public String getTimestamp() {
        return timestamp;
      }

      public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
      }

      public String getSig() {
        return sig;
      }

      public void setSig(String sig) {
        this.sig = sig;
      }

      public String getApplicationId() {
        return applicationId;
      }

      public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
      }

      public String getUserId() {
        return userId;
      }

      public void setUserId(String userId) {
        this.userId = userId;
      }

      public String getClientId() {
        return clientId;
      }

      public void setClientId(String clientId) {
        this.clientId = clientId;
      }
    }

    public static class RequestBean {

      /**
       * coreType : en.pred.score
       * tokenId : 1500876109
       * refText : Grandma Makes Our Home .Grandma helps me wash.  I love Grandma.Grandma helps me dress.  I love Grandma.Grandma helps me clean.  I love Grandma.Grandma helps me share.  I love Grandma.Grandma plays with us!  We love Grandma!
       * rank : 100
       * attachAudioUrl : 1
       */

      private String coreType;
      private String tokenId;
      private String refText;
      private int rank;
      private int attachAudioUrl;

      public String getCoreType() {
        return coreType;
      }

      public void setCoreType(String coreType) {
        this.coreType = coreType;
      }

      public String getTokenId() {
        return tokenId;
      }

      public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
      }

      public String getRefText() {
        return refText;
      }

      public void setRefText(String refText) {
        this.refText = refText;
      }

      public int getRank() {
        return rank;
      }

      public void setRank(int rank) {
        this.rank = rank;
      }

      public int getAttachAudioUrl() {
        return attachAudioUrl;
      }

      public void setAttachAudioUrl(int attachAudioUrl) {
        this.attachAudioUrl = attachAudioUrl;
      }
    }

    public static class AudioBean {

      /**
       * sampleRate : 16000
       * channel : 1
       * sampleBytes : 2
       * audioType : ogg
       */

      private int sampleRate;
      private int channel;
      private int sampleBytes;
      private String audioType;

      public int getSampleRate() {
        return sampleRate;
      }

      public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
      }

      public int getChannel() {
        return channel;
      }

      public void setChannel(int channel) {
        this.channel = channel;
      }

      public int getSampleBytes() {
        return sampleBytes;
      }

      public void setSampleBytes(int sampleBytes) {
        this.sampleBytes = sampleBytes;
      }

      public String getAudioType() {
        return audioType;
      }

      public void setAudioType(String audioType) {
        this.audioType = audioType;
      }
    }
  }

  public static class ResultBean {

    /**
     * fluency : 73
     * pron : 64
     * version : 0.0.80.2017.7.11.18:11:53
     * textmode : 0
     * delaytime : 46
     * rank : 100
     * res : eng.snt.online.1.0
     * info : {"volume":90,"clip":0,"snr":12.741999,"tipId":0}
     * overall : 65
     * integrity : 71
     * systime : 57744
     * pretime : 131
     * useref : 1
     * details : [{"snt_details":[{"char":"Grandma","score":41},{"char":"Makes","score":97},{"char":"Our","score":87},{"char":"Home","score":99},{"char":"Grandma","score":89},{"char":"helps","score":94},{"char":"me","score":97},{"char":"wash","score":59}],"score":80,"text":"Grandma Makes Our Home Grandma helps me wash"},{"snt_details":[{"char":"I","score":52},{"char":"love","score":95},{"char":"Grandma.Grandma","score":48},{"char":"helps","score":0},{"char":"me","score":0},{"char":"dress","score":0}],"score":34,"text":"I love Grandma.Grandma helps me dress"},{"snt_details":[{"char":"I","score":0},{"char":"love","score":0},{"char":"Grandma.Grandma","score":44},{"char":"helps","score":81},{"char":"me","score":89},{"char":"clean","score":98}],"score":57,"text":"I love Grandma.Grandma helps me clean"},{"snt_details":[{"char":"I","score":92},{"char":"love","score":99},{"char":"Grandma.Grandma","score":35},{"char":"helps","score":91},{"char":"me","score":92},{"char":"share","score":92}],"score":68,"text":"I love Grandma.Grandma helps me share"},{"snt_details":[{"char":"I","score":0},{"char":"love","score":77},{"char":"Grandma.Grandma","score":32},{"char":"plays","score":90},{"char":"with","score":96},{"char":"us","score":99}],"score":61,"text":"I love Grandma.Grandma plays with us"},{"snt_details":[{"char":"We","score":90},{"char":"love","score":97},{"char":"Grandma","score":94}],"score":94,"text":"We love Grandma"}]
     * wavetime : 57590
     * precision : 1
     */

    private int fluency;
    private int pron;
    private String version;
    private int textmode;
    private int delaytime;
    private int rank;
    private String res;
    private InfoBean info;
    private int overall;
    private int integrity;
    private int systime;
    private int pretime;
    private int useref;
    private int wavetime;
    private int precision;
    private List<DetailsBean> details;

    public int getFluency() {
      return fluency;
    }

    public void setFluency(int fluency) {
      this.fluency = fluency;
    }

    public int getPron() {
      return pron;
    }

    public void setPron(int pron) {
      this.pron = pron;
    }

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }

    public int getTextmode() {
      return textmode;
    }

    public void setTextmode(int textmode) {
      this.textmode = textmode;
    }

    public int getDelaytime() {
      return delaytime;
    }

    public void setDelaytime(int delaytime) {
      this.delaytime = delaytime;
    }

    public int getRank() {
      return rank;
    }

    public void setRank(int rank) {
      this.rank = rank;
    }

    public String getRes() {
      return res;
    }

    public void setRes(String res) {
      this.res = res;
    }

    public InfoBean getInfo() {
      return info;
    }

    public void setInfo(InfoBean info) {
      this.info = info;
    }

    public int getOverall() {
      return overall;
    }

    public void setOverall(int overall) {
      this.overall = overall;
    }

    public int getIntegrity() {
      return integrity;
    }

    public void setIntegrity(int integrity) {
      this.integrity = integrity;
    }

    public int getSystime() {
      return systime;
    }

    public void setSystime(int systime) {
      this.systime = systime;
    }

    public int getPretime() {
      return pretime;
    }

    public void setPretime(int pretime) {
      this.pretime = pretime;
    }

    public int getUseref() {
      return useref;
    }

    public void setUseref(int useref) {
      this.useref = useref;
    }

    public int getWavetime() {
      return wavetime;
    }

    public void setWavetime(int wavetime) {
      this.wavetime = wavetime;
    }

    public int getPrecision() {
      return precision;
    }

    public void setPrecision(int precision) {
      this.precision = precision;
    }

    public List<DetailsBean> getDetails() {
      return details;
    }

    public void setDetails(List<DetailsBean> details) {
      this.details = details;
    }

    public static class InfoBean {

      /**
       * volume : 90
       * clip : 0
       * snr : 12.741999
       * tipId : 0
       */

      private int volume;
      private double clip;
      private double snr;
      private int tipId;

      public int getVolume() {
        return volume;
      }

      public void setVolume(int volume) {
        this.volume = volume;
      }

      public double getClip() {
        return clip;
      }

      public void setClip(double clip) {
        this.clip = clip;
      }

      public double getSnr() {
        return snr;
      }

      public void setSnr(double snr) {
        this.snr = snr;
      }

      public int getTipId() {
        return tipId;
      }

      public void setTipId(int tipId) {
        this.tipId = tipId;
      }
    }

    public static class DetailsBean {

      /**
       * snt_details : [{"char":"Grandma","score":41},{"char":"Makes","score":97},{"char":"Our","score":87},{"char":"Home","score":99},{"char":"Grandma","score":89},{"char":"helps","score":94},{"char":"me","score":97},{"char":"wash","score":59}]
       * score : 80
       * text : Grandma Makes Our Home Grandma helps me wash
       */

      private int score;
      private String text;
      private List<SntDetailsBean> snt_details;

      public int getScore() {
        return score;
      }

      public void setScore(int score) {
        this.score = score;
      }

      public String getText() {
        return text;
      }

      public void setText(String text) {
        this.text = text;
      }

      public List<SntDetailsBean> getSnt_details() {
        return snt_details;
      }

      public void setSnt_details(List<SntDetailsBean> snt_details) {
        this.snt_details = snt_details;
      }

      public static class SntDetailsBean implements Serializable {

        /**
         * char : Grandma
         * score : 41
         */

        @SerializedName("char")
        private String charX;
        private int score;

        public String getCharX() {
          return charX;
        }

        public void setCharX(String charX) {
          this.charX = charX;
        }

        public int getScore() {
          return score;
        }

        public void setScore(int score) {
          this.score = score;
        }
      }
    }
  }



}
