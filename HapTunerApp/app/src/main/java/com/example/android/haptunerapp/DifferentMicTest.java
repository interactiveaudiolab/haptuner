package com.example.android.haptunerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.app.Activity;
import android.content.pm.ActivityInfo;
//import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.example.android.haptunerapp.audio.AudioCalculator;
import com.example.android.haptunerapp.audio.Callback;
import com.example.android.haptunerapp.audio.NoteCalculator;
import com.example.android.haptunerapp.audio.Recorder;
import com.example.android.haptunerapp.audio.NoteCalculator;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class DifferentMicTest extends AppCompatActivity {

    private Recorder recorder;
    private AudioCalculator audioCalculator;
    private Handler handler;

    private TextView textAmplitude;
    private TextView textDecibel;
    private TextView textFrequency;

    private TextView CurrFreq;
    private TextView ClosestNote;
    private TextView TuneStatus;
    private TextView CentsOff;


    private NoteCalculator noteCalc;

    HashMap<Double,String> pianoHzKeys;




    /*double  pianoKeysHz[] = new double[]
            {27.5, 29.1352, 30.8677, 32.7032, 34.6478, 36.7081, 38.8909, 41.2034, 41.2034, 43.6535, 46.2493, 48.9994, 51.9131
            55, 58.2705, 61.7354, 65.4064, 69.2957, 73.4162, 77.7817, 82.40};
*/



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_different_mic_test);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        recorder = new Recorder(callback);
        audioCalculator = new AudioCalculator();
        handler = new Handler(Looper.getMainLooper());

        textAmplitude = (TextView) findViewById(R.id.textAmplitude);
        textDecibel = (TextView) findViewById(R.id.textDecibel);
        textFrequency = (TextView) findViewById(R.id.textFrequency);


        CurrFreq = (TextView) findViewById(R.id.Frequency);
        ClosestNote = (TextView) findViewById(R.id.ClosestNote);
        TuneStatus = (TextView) findViewById(R.id.TuneStatus);
        CentsOff = (TextView) findViewById(R.id.CentsOff);




    }







    private Callback callback = new Callback() {

        @Override
        public void onBufferAvailable(byte[] buffer) {
            audioCalculator.setBytes(buffer);
            int amplitude = audioCalculator.getAmplitude();
            double decibel = audioCalculator.getDecibel();
            double frequency = audioCalculator.getFrequency();

            noteCalc = new NoteCalculator(frequency);

            int FreqIndex = noteCalc.retFreqIndex(frequency);
            double closeF = noteCalc.retClosestFreq(FreqIndex, frequency);
            String closeN = noteCalc.retClosestNote(FreqIndex);
            double centsO = noteCalc.retCentsOff(frequency, closeF);



            final String amp = String.valueOf(amplitude + " Amp");
            final String db = String.valueOf(decibel + " db");
            final String hz = String.valueOf(frequency + " Hz");
            final String cN = String.valueOf("Closest Note: " + closeN);
            final String tS = String.valueOf("Tune Status: " + noteCalc.retTuneStatus());
            final String cO = String.valueOf("Cents Off: " + centsO);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    textAmplitude.setText(amp);
                    textDecibel.setText(db);
                    textFrequency.setText(hz);
                    CurrFreq.setText(hz);
                    ClosestNote.setText(cN);
                    TuneStatus.setText(tS);
                    CentsOff.setText(cO);

                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        recorder.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        recorder.stop();
    }
}
