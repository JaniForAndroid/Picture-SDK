package com.namibox.hfx.bean;

import com.chivox.EvalResult.Detail;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Roy.chen on 2017/7/6.
 */

public class EvalBody implements Serializable{
    public int exercise_id;
    public int pron;
    public int fluency;
    public int integrity;
    public String text;
    public int score;
    public String mp3name;
    public String engine_used;
    public List<Detail> details;
}
