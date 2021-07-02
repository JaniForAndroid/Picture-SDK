package com.chivox;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Create time: 2018/4/16.
 */
public class ChiShengWordResult {

  /**
   * recordId : 5ad47d69f3067e7de200d675
   * tokenId : 5ad47d76332793263c000021
   * applicationId : 144150708600000a
   * audioUrl : download.cloud.chivox.com:8002/5ad47d69f3067e7de200d675
   * dtLastResponse : 2018-04-16 18:39:39:931
   * params : {"app":{"timestamp":"1523875190","sig":"0153cc64c0ee81cef65ad8875e3b849ed30889f6","applicationId":"144150708600000a","userId":"5631218","clientId":"880ed1c330d5e9ca"},"request":{"coreType":"en.word.score","rank":100,"tokenId":"5ad47d76332793263c000021","refText":"frog","precision":0.5,"attachAudioUrl":1,"client_params":{"ext_subitem_rank4":0}},"audio":{"sampleRate":16000,"channel":1,"sampleBytes":2,"audioType":"ogg"}}
   * eof : 1
   * refText : frog
   * result : {"useref":1,"version":"0.0.80.2018.1.18.15:21:41","rank":100,"res":"eng.wrd.G4.N1.0.2","forceout":0,"pron":89,"delaytime":180,"textmode":0,"systime":660,"pretime":81,"usehookw":0,"overall":89,"is_en":1,"wavetime":2380,"details":[{"phone":[{"char":"f","score":100},{"char":"r","score":100},{"char":"oh","score":100},{"char":"g","score":87}],"endindex":3,"beginindex":0,"stress":[{"char":"f_r_oh_g","ref":1,"score":1}],"end":2000,"dur":840,"char":"frog","start":1160,"indict":1,"score":89}],"info":{"snr":19.642969,"trunc":0,"clip":0.02631,"volume":6166,"tipId":10005},"en_prob":1}
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
     * app : {"timestamp":"1523875190","sig":"0153cc64c0ee81cef65ad8875e3b849ed30889f6","applicationId":"144150708600000a","userId":"5631218","clientId":"880ed1c330d5e9ca"}
     * request : {"coreType":"en.word.score","rank":100,"tokenId":"5ad47d76332793263c000021","refText":"frog","precision":0.5,"attachAudioUrl":1,"client_params":{"ext_subitem_rank4":0}}
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
       * timestamp : 1523875190
       * sig : 0153cc64c0ee81cef65ad8875e3b849ed30889f6
       * applicationId : 144150708600000a
       * userId : 5631218
       * clientId : 880ed1c330d5e9ca
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
       * coreType : en.word.score
       * rank : 100
       * tokenId : 5ad47d76332793263c000021
       * refText : frog
       * precision : 0.5
       * attachAudioUrl : 1
       * client_params : {"ext_subitem_rank4":0}
       */

      private String coreType;
      private int rank;
      private String tokenId;
      private String refText;
      private double precision;
      private int attachAudioUrl;
      private ClientParamsBean client_params;

      public String getCoreType() {
        return coreType;
      }

      public void setCoreType(String coreType) {
        this.coreType = coreType;
      }

      public int getRank() {
        return rank;
      }

      public void setRank(int rank) {
        this.rank = rank;
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

      public double getPrecision() {
        return precision;
      }

      public void setPrecision(double precision) {
        this.precision = precision;
      }

      public int getAttachAudioUrl() {
        return attachAudioUrl;
      }

      public void setAttachAudioUrl(int attachAudioUrl) {
        this.attachAudioUrl = attachAudioUrl;
      }

      public ClientParamsBean getClient_params() {
        return client_params;
      }

      public void setClient_params(ClientParamsBean client_params) {
        this.client_params = client_params;
      }

      public static class ClientParamsBean {

        /**
         * ext_subitem_rank4 : 0
         */

        private int ext_subitem_rank4;

        public int getExt_subitem_rank4() {
          return ext_subitem_rank4;
        }

        public void setExt_subitem_rank4(int ext_subitem_rank4) {
          this.ext_subitem_rank4 = ext_subitem_rank4;
        }
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
     * useref : 1
     * version : 0.0.80.2018.1.18.15:21:41
     * rank : 100
     * res : eng.wrd.G4.N1.0.2
     * forceout : 0
     * pron : 89
     * delaytime : 180
     * textmode : 0
     * systime : 660
     * pretime : 81
     * usehookw : 0
     * overall : 89
     * is_en : 1
     * wavetime : 2380
     * details : [{"phone":[{"char":"f","score":100},{"char":"r","score":100},{"char":"oh","score":100},{"char":"g","score":87}],"endindex":3,"beginindex":0,"stress":[{"char":"f_r_oh_g","ref":1,"score":1}],"end":2000,"dur":840,"char":"frog","start":1160,"indict":1,"score":89}]
     * info : {"snr":19.642969,"trunc":0,"clip":0.02631,"volume":6166,"tipId":10005}
     * en_prob : 1
     */

    private int useref;
    private String version;
    private int rank;
    private String res;
    private int forceout;
    private int pron;
    private int delaytime;
    private int textmode;
    private int systime;
    private int pretime;
    private int usehookw;
    private int overall;
    private int is_en;
    private int wavetime;
    private InfoBean info;
    private int en_prob;
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

    public int getIs_en() {
      return is_en;
    }

    public void setIs_en(int is_en) {
      this.is_en = is_en;
    }

    public int getWavetime() {
      return wavetime;
    }

    public void setWavetime(int wavetime) {
      this.wavetime = wavetime;
    }

    public InfoBean getInfo() {
      return info;
    }

    public void setInfo(InfoBean info) {
      this.info = info;
    }

    public int getEn_prob() {
      return en_prob;
    }

    public void setEn_prob(int en_prob) {
      this.en_prob = en_prob;
    }

    public List<DetailsBean> getDetails() {
      return details;
    }

    public void setDetails(List<DetailsBean> details) {
      this.details = details;
    }

    public static class InfoBean {

      /**
       * snr : 19.642969
       * trunc : 0
       * clip : 0.02631
       * volume : 6166
       * tipId : 10005
       */

      private double snr;
      private int trunc;
      private double clip;
      private int volume;
      private int tipId;

      public double getSnr() {
        return snr;
      }

      public void setSnr(double snr) {
        this.snr = snr;
      }

      public int getTrunc() {
        return trunc;
      }

      public void setTrunc(int trunc) {
        this.trunc = trunc;
      }

      public double getClip() {
        return clip;
      }

      public void setClip(double clip) {
        this.clip = clip;
      }

      public int getVolume() {
        return volume;
      }

      public void setVolume(int volume) {
        this.volume = volume;
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
       * phone : [{"char":"f","score":100},{"char":"r","score":100},{"char":"oh","score":100},{"char":"g","score":87}]
       * endindex : 3
       * beginindex : 0
       * stress : [{"char":"f_r_oh_g","ref":1,"score":1}]
       * end : 2000
       * dur : 840
       * char : frog
       * start : 1160
       * indict : 1
       * score : 89
       */

      private int endindex;
      private int beginindex;
      private int end;
      private int dur;
      @SerializedName("char")
      private String charX;
      private int start;
      private int indict;
      private int score;
      private List<PhoneBean> phone;
      private List<StressBean> stress;

      public int getEndindex() {
        return endindex;
      }

      public void setEndindex(int endindex) {
        this.endindex = endindex;
      }

      public int getBeginindex() {
        return beginindex;
      }

      public void setBeginindex(int beginindex) {
        this.beginindex = beginindex;
      }

      public int getEnd() {
        return end;
      }

      public void setEnd(int end) {
        this.end = end;
      }

      public int getDur() {
        return dur;
      }

      public void setDur(int dur) {
        this.dur = dur;
      }

      public String getCharX() {
        return charX;
      }

      public void setCharX(String charX) {
        this.charX = charX;
      }

      public int getStart() {
        return start;
      }

      public void setStart(int start) {
        this.start = start;
      }

      public int getIndict() {
        return indict;
      }

      public void setIndict(int indict) {
        this.indict = indict;
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

      public List<StressBean> getStress() {
        return stress;
      }

      public void setStress(List<StressBean> stress) {
        this.stress = stress;
      }

      public static class PhoneBean {

        /**
         * char : f
         * score : 100
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

      public static class StressBean {

        /**
         * char : f_r_oh_g
         * ref : 1
         * score : 1
         */

        @SerializedName("char")
        private String charX;
        private int ref;
        private int score;

        public String getCharX() {
          return charX;
        }

        public void setCharX(String charX) {
          this.charX = charX;
        }

        public int getRef() {
          return ref;
        }

        public void setRef(int ref) {
          this.ref = ref;
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
