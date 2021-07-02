package com.namibox.tools;

import android.view.animation.Interpolator;

public class MaterInterpolator implements Interpolator {
    private int mDefault_Type = LINEAR;  
    private float interpolatorValue;  
    private static MaterInterpolator mInterpolator;
    public static MaterInterpolator createInterpolator(){
        if (mInterpolator == null) {  
            mInterpolator = new MaterInterpolator();
        }  
        return mInterpolator;  
    }  
      
    @Override
    public float getInterpolation(float input) {  
          
        switch (mDefault_Type) {  
        case LINEAR: //线性  
            interpolatorValue = input;  
            break;  
        case ACCELERATE: //加速  
            float factor = 1.0f;  
            if (factor == 1.0) {  
                factor = input * input;  
            }else {  
                factor = (float) Math.pow(input, factor *2);
            }  
            interpolatorValue = factor;  
            break;  
        case ACCELERATEDECELERATE://先加速后减速  
            interpolatorValue = (float) ((Math.cos((input + 1) * Math.PI) /2.0) + 0.5F);
            break;  
        case ANTICIPATE:   
              
            float tension = 2.0f;  
            interpolatorValue = input * input *((tension + 1) * input - tension);  
            break;  
        case ANTICIPATEOVERSHOOT:  
            tension = 2.0f * 1.5f;  
            if (input < 0.5){  
                interpolatorValue = (float) (0.5 * (Math.pow(input * 2.0f, 2) * ((tension + 1) * input  - tension)));
            }else {  
                interpolatorValue = (float) (0.5 *(Math.pow(((input - 1) * 2.0f), 2) *((tension  + 1) *(input - 1) * 2.0f + tension)));
            }  
            break;  
        case BOUNCE://反弹  
            if (input < 0.3535) {  
                interpolatorValue = bounce(input);  
            }else if(input < 0.7408){  
                interpolatorValue = bounce(input - 0.54719f) + 0.7f;  
            }else if (input < 0.9644) {  
                interpolatorValue = bounce(input - 0.8526f) + 0.9f;   
            }else {  
                interpolatorValue = bounce(input - 1.0435f) + 0.95f;    
            }  
            break;  
        case CUBICHERMITE:  
            interpolatorValue = CubicHermite(input, 0, 1, 4, 4);  
            break;  
        case CYCLE:  
            float cycles = 1.0f;  
            interpolatorValue = (float) Math.sin(2* cycles * Math.PI *input);
            break;  
        case DECELERATE://减速  
            float divisor = 1.0f;  
            if (divisor == 1.0) {  
                interpolatorValue = (float) (1.0 - (1.0 - input ) * (1.0 - input));  
            }else {  
                interpolatorValue = (float) (1.0 - Math.pow((1.0 - input), 2 * divisor));
            }  
            break;  
        case OVERSHOOT:  
            tension = 2.0f;  
            input = input - 1.0f;  
            interpolatorValue = (float) (input *input *((tension  + 1) * input  + tension ) +1.0);  
            break;  
        case SMOOTHSTEP:  
            interpolatorValue = input * input * (3 - 2 *input );  
            break;  
        case SPRING://弹簧效果  
            factor = 0.4f;  
            interpolatorValue = (float) (
                Math.pow(2,  -10 *input) * Math.sin((input - factor /4) *(2 * Math.PI)/factor ) + 1.0);
            break;  
        default:  
            break;  
        }  
          
          
        return  interpolatorValue;  
    }  
  
  
    private float bounce(float input){    
        return input *input *8;  
    }  
    private float CubicHermite(float t, int p0,int p1,int m0,int m1){  
        float t2 = t* t;  
        float t3 = t2 * t ;  
        return  (2* t3 - 3 * t2 +1) * p0 + (t3 - 2 * t2 + t) * m0 + (-2 * t3 + 3 * t2) * p1 + (t3 - t2) *m1;  
    }  
    public int getDefault_Type() {  
        return mDefault_Type;  
    }  
    public MaterInterpolator setDefault_Type(int default_Type) {
        mDefault_Type = default_Type;  
        return mInterpolator;  
    }  
    public static final int LINEAR =0 ;  
    public static final int SMOOTHSTEP = 1;  
    public static final int ACCELERATEDECELERATE = 2;  
    public static final int BOUNCE = 3;  
    public static final int ACCELERATE = 4;  
    public static final int ANTICIPATEOVERSHOOT = 5;  
    public static final int CYCLE = 6;  
    public static final int ANTICIPATE = 7;  
    public static final int DECELERATE = 8;  
    public static final int OVERSHOOT = 9;  
    public static final int CUBICHERMITE = 10;  
    public static final int SPRING = 11;  
    public  enum MaterInterPolatorType{  
        LINEAR,SMOOTHSTEP,SPRING,CUBICHERMITE,OVERSHOOT,DECELERATE,ANTICIPATE,CYCLE,ANTICIPATEOVERSHOOT  
        ,ACCELERATE,BOUNCE,ACCELERATEDECELERATE  
    }  
} 