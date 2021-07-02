package com.chivox;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Roy.chen on 2017/6/23.
 */

public class XSResult {

  /**
   * recordId : 11e757db5065540293ed3813 tokenId : 1498198511 applicationId : t132 audioUrl :
   * http://files.cloud.ssapi.cn:8080/t132/11e757db5065540293ed3813 dtLastResponse : 2017-06-23
   * 14:15:15:198 params : {"app":{"timestamp":"1498198511","sig":"5c7571c1e5b06b1904e20d2f9c927fe1615fa55c","applicationId":"t132","userId":"guest","clientId":""},"request":{"coreType":"en.sent.score","tokenId":"1498198511","refText":"hello，i
   * am liu tao","rank":100,"attachAudioUrl":1},"audio":{"sampleRate":16000,"channel":1,"sampleBytes":2,"audioType":"ogg"}}
   * eof : 1 refText : hello，i am liu tao result : {"useref":1,"statics":[{"char":"hh","count":1,"score":98},{"char":"ax","count":1,"score":97},{"char":"l","count":2,"score":95},{"char":"ow","count":1,"score":94},{"char":"ae","count":1,"score":13},{"char":"m","count":1,"score":98},{"char":"y","count":1,"score":74},{"char":"uh","count":1,"score":88},{"char":"t","count":1,"score":97},{"char":"aw","count":1,"score":40}],"version":"0.0.80.2017.6.7.17:15:05","rank":100,"res":"eng.snt.online.1.0","integrity":100,"fluency":{"pause":0,"overall":79,"speed":154},"pron":76,"delaytime":90,"textmode":0,"systime":3559,"pretime":168,"info":{"volume":2696,"clip":0.017781,"snr":7.786804,"tipId":0},"usehookw":0,"overall":81,"rhythm":{"stress":75,"overall":94,"sense":100,"tone":100},"wavetime":2990,"accuracy":76,"details":[{"fluency":100,"dur":440,"score":97,"start":640,"liaisonscore":0,"toneref":0,"stressscore":0,"stressref":0,"char":"hello","sensescore":0,"senseref":0,"tonescore":0,"end":1080,"liaisonref":0},{"fluency":31,"dur":560,"score":56,"start":1080,"liaisonscore":0,"toneref":0,"stressscore":1,"stressref":0,"char":"am","sensescore":0,"senseref":0,"tonescore":0,"end":1640,"liaisonref":0},{"fluency":99,"dur":210,"score":85,"start":1640,"liaisonscore":0,"toneref":0,"stressscore":0,"stressref":0,"char":"liu","sensescore":0,"senseref":0,"tonescore":0,"end":1850,"liaisonref":0},{"fluency":85,"dur":350,"score":68,"start":1850,"liaisonscore":0,"toneref":0,"stressscore":0,"stressref":0,"char":"tao","sensescore":1,"senseref":1,"tonescore":0,"end":2200,"liaisonref":0}],"forceout":0,"precision":1}
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
     * app : {"timestamp":"1498198511","sig":"5c7571c1e5b06b1904e20d2f9c927fe1615fa55c","applicationId":"t132","userId":"guest","clientId":""}
     * request : {"coreType":"en.sent.score","tokenId":"1498198511","refText":"hello，i am liu
     * tao","rank":100,"attachAudioUrl":1} audio : {"sampleRate":16000,"channel":1,"sampleBytes":2,"audioType":"ogg"}
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
       * timestamp : 1498198511
       * sig : 5c7571c1e5b06b1904e20d2f9c927fe1615fa55c
       * applicationId : t132
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
       * coreType : en.sent.score
       * tokenId : 1498198511
       * refText : hello，i am liu tao
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
     * useref : 1 statics : [{"char":"hh","count":1,"score":98},{"char":"ax","count":1,"score":97},{"char":"l","count":2,"score":95},{"char":"ow","count":1,"score":94},{"char":"ae","count":1,"score":13},{"char":"m","count":1,"score":98},{"char":"y","count":1,"score":74},{"char":"uh","count":1,"score":88},{"char":"t","count":1,"score":97},{"char":"aw","count":1,"score":40}]
     * version : 0.0.80.2017.6.7.17:15:05 rank : 100 res : eng.snt.online.1.0 integrity : 100
     * fluency : {"pause":0,"overall":79,"speed":154} pron : 76 delaytime : 90 textmode : 0 systime
     * : 3559 pretime : 168 info : {"volume":2696,"clip":0.017781,"snr":7.786804,"tipId":0} usehookw
     * : 0 overall : 81 rhythm : {"stress":75,"overall":94,"sense":100,"tone":100} wavetime : 2990
     * accuracy : 76 details : [{"fluency":100,"dur":440,"score":97,"start":640,"liaisonscore":0,"toneref":0,"stressscore":0,"stressref":0,"char":"hello","sensescore":0,"senseref":0,"tonescore":0,"end":1080,"liaisonref":0},{"fluency":31,"dur":560,"score":56,"start":1080,"liaisonscore":0,"toneref":0,"stressscore":1,"stressref":0,"char":"am","sensescore":0,"senseref":0,"tonescore":0,"end":1640,"liaisonref":0},{"fluency":99,"dur":210,"score":85,"start":1640,"liaisonscore":0,"toneref":0,"stressscore":0,"stressref":0,"char":"liu","sensescore":0,"senseref":0,"tonescore":0,"end":1850,"liaisonref":0},{"fluency":85,"dur":350,"score":68,"start":1850,"liaisonscore":0,"toneref":0,"stressscore":0,"stressref":0,"char":"tao","sensescore":1,"senseref":1,"tonescore":0,"end":2200,"liaisonref":0}]
     * forceout : 0 precision : 1
     */

    private int useref;
    private String version;
    private int rank;
    private String res;
    private int integrity;
    private FluencyBean fluency;
    private int pron;
    private int delaytime;
    private int textmode;
    private int systime;
    private int pretime;
    private InfoBean info;
    private int usehookw;
    private int overall;
    private RhythmBean rhythm;
    private int wavetime;
    private int accuracy;
    private int forceout;
    private int precision;
    private List<StaticsBean> statics;
    private List<DetailsBean> details;

    public int getUseref() {
      return useref;
    }

    public void setUseref(int useref) {
      this.useref = useref;
    }

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
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

    public int getIntegrity() {
      return integrity;
    }

    public void setIntegrity(int integrity) {
      this.integrity = integrity;
    }

    public FluencyBean getFluency() {
      return fluency;
    }

    public void setFluency(FluencyBean fluency) {
      this.fluency = fluency;
    }

    public int getPron() {
      return pron;
    }

    public void setPron(int pron) {
      this.pron = pron;
    }

    public int getDelaytime() {
      return delaytime;
    }

    public void setDelaytime(int delaytime) {
      this.delaytime = delaytime;
    }

    public int getTextmode() {
      return textmode;
    }

    public void setTextmode(int textmode) {
      this.textmode = textmode;
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

    public InfoBean getInfo() {
      return info;
    }

    public void setInfo(InfoBean info) {
      this.info = info;
    }

    public int getUsehookw() {
      return usehookw;
    }

    public void setUsehookw(int usehookw) {
      this.usehookw = usehookw;
    }

    public int getOverall() {
      return overall;
    }

    public void setOverall(int overall) {
      this.overall = overall;
    }

    public RhythmBean getRhythm() {
      return rhythm;
    }

    public void setRhythm(RhythmBean rhythm) {
      this.rhythm = rhythm;
    }

    public int getWavetime() {
      return wavetime;
    }

    public void setWavetime(int wavetime) {
      this.wavetime = wavetime;
    }

    public int getAccuracy() {
      return accuracy;
    }

    public void setAccuracy(int accuracy) {
      this.accuracy = accuracy;
    }

    public int getForceout() {
      return forceout;
    }

    public void setForceout(int forceout) {
      this.forceout = forceout;
    }

    public int getPrecision() {
      return precision;
    }

    public void setPrecision(int precision) {
      this.precision = precision;
    }

    public List<StaticsBean> getStatics() {
      return statics;
    }

    public void setStatics(List<StaticsBean> statics) {
      this.statics = statics;
    }

    public List<DetailsBean> getDetails() {
      return details;
    }

    public void setDetails(List<DetailsBean> details) {
      this.details = details;
    }

    public static class FluencyBean {

      /**
       * pause : 0
       * overall : 79
       * speed : 154
       */

      private int pause;
      private int overall;
      private int speed;

      public int getPause() {
        return pause;
      }

      public void setPause(int pause) {
        this.pause = pause;
      }

      public int getOverall() {
        return overall;
      }

      public void setOverall(int overall) {
        this.overall = overall;
      }

      public int getSpeed() {
        return speed;
      }

      public void setSpeed(int speed) {
        this.speed = speed;
      }
    }

    public static class InfoBean {

      /**
       * volume : 2696
       * clip : 0.017781
       * snr : 7.786804
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

    public static class RhythmBean {

      /**
       * stress : 75
       * overall : 94
       * sense : 100
       * tone : 100
       */

      private int stress;
      private int overall;
      private int sense;
      private int tone;

      public int getStress() {
        return stress;
      }

      public void setStress(int stress) {
        this.stress = stress;
      }

      public int getOverall() {
        return overall;
      }

      public void setOverall(int overall) {
        this.overall = overall;
      }

      public int getSense() {
        return sense;
      }

      public void setSense(int sense) {
        this.sense = sense;
      }

      public int getTone() {
        return tone;
      }

      public void setTone(int tone) {
        this.tone = tone;
      }
    }

    public static class StaticsBean {

      /**
       * char : hh
       * count : 1
       * score : 98
       */

      @SerializedName("char")
      private String charX;
      private int count;
      private int score;

      public String getCharX() {
        return charX;
      }

      public void setCharX(String charX) {
        this.charX = charX;
      }

      public int getCount() {
        return count;
      }

      public void setCount(int count) {
        this.count = count;
      }

      public int getScore() {
        return score;
      }

      public void setScore(int score) {
        this.score = score;
      }
    }

    public static class DetailsBean {

      /**
       * fluency : 100
       * dur : 440
       * score : 97
       * start : 640
       * liaisonscore : 0
       * toneref : 0
       * stressscore : 0
       * stressref : 0
       * char : hello
       * sensescore : 0
       * senseref : 0
       * tonescore : 0
       * end : 1080
       * liaisonref : 0
       */

      private int fluency;
      private int dur;
      private int score;
      private int start;
      private int liaisonscore;
      private int toneref;
      private int stressscore;
      private int stressref;
      @SerializedName("char")
      private String charX;
      private String chn_char;
      private int sensescore;
      private int senseref;
      private int tonescore;
      private int end;
      private int liaisonref;

      public String getChn_char() {
        return chn_char;
      }

      public void setChn_char(String chn_char) {
        this.chn_char = chn_char;
      }

      public int getFluency() {
        return fluency;
      }

      public void setFluency(int fluency) {
        this.fluency = fluency;
      }

      public int getDur() {
        return dur;
      }

      public void setDur(int dur) {
        this.dur = dur;
      }

      public int getScore() {
        return score;
      }

      public void setScore(int score) {
        this.score = score;
      }

      public int getStart() {
        return start;
      }

      public void setStart(int start) {
        this.start = start;
      }

      public int getLiaisonscore() {
        return liaisonscore;
      }

      public void setLiaisonscore(int liaisonscore) {
        this.liaisonscore = liaisonscore;
      }

      public int getToneref() {
        return toneref;
      }

      public void setToneref(int toneref) {
        this.toneref = toneref;
      }

      public int getStressscore() {
        return stressscore;
      }

      public void setStressscore(int stressscore) {
        this.stressscore = stressscore;
      }

      public int getStressref() {
        return stressref;
      }

      public void setStressref(int stressref) {
        this.stressref = stressref;
      }

      public String getCharX() {
        return charX;
      }

      public void setCharX(String charX) {
        this.charX = charX;
      }

      public int getSensescore() {
        return sensescore;
      }

      public void setSensescore(int sensescore) {
        this.sensescore = sensescore;
      }

      public int getSenseref() {
        return senseref;
      }

      public void setSenseref(int senseref) {
        this.senseref = senseref;
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

      public int getLiaisonref() {
        return liaisonref;
      }

      public void setLiaisonref(int liaisonref) {
        this.liaisonref = liaisonref;
      }
    }
  }
}
