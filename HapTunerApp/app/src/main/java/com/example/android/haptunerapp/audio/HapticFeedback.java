package com.example.android.haptunerapp.audio;

import android.os.Vibrator;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.android.haptunerapp.MainActivity;


/**
 * Created by ejmcd on 2/27/2018.
 */

public class HapticFeedback extends MainActivity {

    private double centsOff;
    //final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    public HapticFeedback(){}
    public HapticFeedback(double cOff){
        this.centsOff = cOff;

    }

    private void constantSharp(){
        long[] timingArray = new long[]{200, 100};
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingArray, 0);

    }

    private void constantFlat(){
        long[] timingArray = new long[]{200, 300};
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingArray, 0);
    }

    private void inTune(){
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200000000);
    }

    public void constantVibrate(double cO){
        if(Math.abs(cO) <= 5){
            inTune();
        }else if(cO < 0){
            constantFlat();
        }
        else{
            constantSharp();
        }
    }

}
