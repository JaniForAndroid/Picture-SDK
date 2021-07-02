package com.chivox;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Create time: 2018/4/10.
 */
public class XSWordResult {

  /**
   * recordId : 11e83c9fd8c686f0abb9a132c21484f
   * tokenId : 5acc80ed3327930000017930
   * applicationId : a132
   * audioUrl : http://files.cloud.ssapi.cn:8080/a132/11e83c9fd8c686f0abb9a132c21484f
   * dtLastResponse : 2018-04-10 17:16:29:860
   * params : {"app":{"timestamp":"1523351789","sig":"513704de22fa58145815c822943cb757a615cd00","applicationId":"a132","clientId":"","userId":"guest","connect_id":"5acc80ed3327930000035930"},"request":{"coreType":"cn.word.score","tokenId":"5acc80ed3327930000017930","refText":"学生","rank":100,"attachAudioUrl":1,"request_id":"5acc80ed3327930000027930"},"audio":{"sampleRate":16000,"channel":1,"sampleBytes":2,"audioType":"ogg"}}
   * eof : 1
   * refText : 学生
   * result : {"forceout":0,"pron":4,"version":"0.0.80.2018.2.6.17:55:39","delaytime":29,"precision":1,"res":"chn.wrd.online.1.0","tone":0,"info":{"volume":74,"clip":0,"snr":3.048076,"tipId":10004},"overall":4,"wavetime":2730,"systime":2940,"pretime":71,"details":[{"pron":8,"phone":[{"char":"x","score":0},{"char":"ve","score":16}],"phn":8,"dur":60,"tone":0,"chn_char":"学","overall":8,"start":1460,"confidence":[0,0,0,0,0],"char":"xue","tonescore":0,"end":1520,"score":8},{"pron":1,"phone":[{"char":"sh","score":1},{"char":"eng","score":0}],"phn":1,"dur":140,"tone":0,"chn_char":"生","overall":1,"start":2430,"confidence":[0,0,0,0,0],"char":"sheng","tonescore":0,"end":2570,"score":1}],"rank":100,"phn":4}
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
     * app : {"timestamp":"1523351789","sig":"513704de22fa58145815c822943cb757a615cd00","applicationId":"a132","clientId":"","userId":"guest","connect_id":"5acc80ed3327930000035930"}
     * request : {"coreType":"cn.word.score","tokenId":"5acc80ed3327930000017930","refText":"学生","rank":100,"attachAudioUrl":1,"request_id":"5acc80ed3327930000027930"}
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
       * timestamp : 1523351789
       * sig : 513704de22fa58145815c822943cb757a615cd00
       * applicationId : a132
       * clientId :
       * userId : guest
       * connect_id : 5acc80ed3327930000035930
       */

      private String timestamp;
      private String sig;
      private String applicationId;
      private String clientId;
      private String userId;
      private String connect_id;

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

      public String getClientId() {
        return clientId;
      }

      public void setClientId(String clientId) {
        this.clientId = clientId;
      }

      public String getUserId() {
        return userId;
      }

      public void setUserId(String userId) {
        this.userId = userId;
      }

      public String getConnect_id() {
        return connect_id;
      }

      public void setConnect_id(String connect_id) {
        this.connect_id = connect_id;
      }
    }

    public static class RequestBean {

      /**
       * coreType : cn.word.score
       * tokenId : 5acc80ed3327930000017930
       * refText : 学生
       * rank : 100
       * attachAudioUrl : 1
       * request_id : 5acc80ed3327930000027930
       */

      private String coreType;
      private String tokenId;
      private String refText;
      private int rank;
      private int attachAudioUrl;
      private String request_id;

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

      public String getRequest_id() {
        return request_id;
      }

      public void setRequest_id(String request_id) {
        this.request_id = request_id;
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
     * forceout : 0
     * pron : 4
     * version : 0.0.80.2018.2.6.17:55:39
     * delaytime : 29
     * precision : 1
     * res : chn.wrd.online.1.0
     * tone : 0
     * info : {"volume":74,"clip":0,"snr":3.048076,"tipId":10004}
     * overall : 4
     * wavetime : 2730
     * systime : 2940
     * pretime : 71
     * details : [{"pron":8,"phone":[{"char":"x","score":0},{"char":"ve","score":16}],"phn":8,"dur":60,"tone":0,"chn_char":"学","overall":8,"start":1460,"confidence":[0,0,0,0,0],"char":"xue","tonescore":0,"end":1520,"score":8},{"pron":1,"phone":[{"char":"sh","score":1},{"char":"eng","score":0}],"phn":1,"dur":140,"tone":0,"chn_char":"生","overall":1,"start":2430,"confidence":[0,0,0,0,0],"char":"sheng","tonescore":0,"end":2570,"score":1}]
     * rank : 100
     * phn : 4
     */

    private int forceout;
    private int pron;
    private String version;
    private int delaytime;
    private int precision;
    private String res;
    private int tone;
    private InfoBean info;
    private int overall;
    private int wavetime;
    private int systime;
    private int pretime;
    private int rank;
    private int phn;
    private List<DetailsBean> details;

    public int getForceout() {
      return forceout;
    }

    public void setForceout(int forceout) {
      this.forceout = forceout;
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

    public int getDelaytime() {
      return delaytime;
    }

    public void setDelaytime(int delaytime) {
      this.delaytime = delaytime;
    }

    public int getPrecision() {
      return precision;
    }

    public void setPrecision(int precision) {
      this.precision = precision;
    }

    public String getRes() {
      return res;
    }

    public void setRes(String res) {
      this.res = res;
    }

    public int getTone() {
      return tone;
    }

    public void setTone(int tone) {
      this.tone = tone;
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

    public int getWavetime() {
      return wavetime;
    }

    public void setWavetime(int wavetime) {
      this.wavetime = wavetime;
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

    public int getRank() {
      return rank;
    }

    public void setRank(int rank) {
      this.rank = rank;
    }

    public int getPhn() {
      return phn;
    }

    public void setPhn(int phn) {
      this.phn = phn;
    }

    public List<DetailsBean> getDetails() {
      return details;
    }

    public void setDetails(List<DetailsBean> details) {
      this.details = details;
    }

    public static class InfoBean {

      /**
       * volume : 74
       * clip : 0
       * snr : 3.048076
       * tipId : 10004
       */

      private int volume;
      private int clip;
      private double snr;
      private int tipId;

      public int getVolume() {
        return volume;
      }

      public void setVolume(int volume) {
        this.volume = volume;
      }

      public int getClip() {
        return clip;
      }

      public void setClip(int clip) {
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
       * pron : 8
       * phone : [{"char":"x","score":0},{"char":"ve","score":16}]
       * phn : 8
       * dur : 60
       * tone : 0
       * chn_char : 学
       * overall : 8
       * start : 1460
       * confidence : [0,0,0,0,0]
       * char : xue
       * tonescore : 0
       * end : 1520
       * score : 8
       */

      private int pron;
      private int phn;
      private int dur;
      private int tone;
      private String chn_char;
      private int overall;
      private int start;
      @SerializedName("char")
      private String charX;
      private int tonescore;
      private int end;
      private int score;
      private List<PhoneBean> phone;
      private List<Integer> confidence;

      public int getPron() {
        return pron;
      }

      public void setPron(int pron) {
        this.pron = pron;
      }

      public int getPhn() {
        return phn;
      }

      public void setPhn(int phn) {
        this.phn = phn;
      }

      public int getDur() {
        return dur;
      }

      public void setDur(int dur) {
        this.dur = dur;
      }

      public int getTone() {
        return tone;
      }

      public void setTone(int tone) {
        this.tone = tone;
      }

      public String getChn_char() {
        return chn_char;
      }

      public void setChn_char(String chn_char) {
        this.chn_char = chn_char;
      }

      public int getOverall() {
        return overall;
      }

      public void setOverall(int overall) {
        this.overall = overall;
      }

      public int getStart() {
        return start;
      }

      public void setStart(int start) {
        this.start = start;
      }

      public String getCharX() {
        return charX;
      }

      public void setCharX(String charX) {
        this.charX = charX;
      }

      public int getTonescore() {
        return tonescore;
      }

      public void setTonescore(int tonescore) {
        this.tonescore = tonescore;
      }

      public int getEnd() {
        return end;
      }

      public void setEnd(int end) {
        this.end = end;
      }

      public int getScore() {
        return score;
      }

      public void setScore(int score) {
        this.score = score;
      }

      public List<PhoneBean> getPhone() {
        return phone;
      }

      public void setPhone(List<PhoneBean> phone) {
        this.phone = phone;
      }

      public List<Integer> getConfidence() {
        return confidence;
      }

      public void setConfidence(List<Integer> confidence) {
        this.confidence = confidence;
      }

      public static class PhoneBean {

        /**
         * char : x
         * score : 0
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
