package com.example.android.haptunerapp;

import android.media.AudioRecord;
import android.os.Process;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.app.Activity;
import android.content.pm.ActivityInfo;
//import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;

import com.example.android.haptunerapp.audio.AudioCalculator;
import com.example.android.haptunerapp.audio.Callback;
import com.example.android.haptunerapp.audio.NoteCalculator;
import com.example.android.haptunerapp.audio.Recorder;
import com.example.android.haptunerapp.audio.Yin;
import com.example.android.haptunerapp.audio.NoteCalculator;
//import com.example.android.haptunerapp.audio.HapticFeedback;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class DifferentMicTest extends AppCompatActivity {

    double[] averageFrequency;
    int aveFreqInd;
    boolean currentlyRecording;
    Button startStop;
    HashMap<Double, String> pianoHzKeys;
    private Recorder recorder;
    private AudioCalculator audioCalculator;
    private Handler handler;
    private Handler VibHandler;
    private TextView textAmplitude;
    private TextView textDecibel;
    private TextView textFrequency;
    private TextView CurrFreq;
    private TextView ClosestNote;
    private TextView TuneStatus;
    private TextView CentsOff;
    private TextView ChromaVal;
    private TextView ChromaClass;
    Handler vibHandler;
    Runnable vibRunnable;
    // private HapticFeedback haptFeed;
    private NoteCalculator noteCalc;

    private Yin yinPitchTracker;


    /*double  pianoKeysHz[] = new double[]
            {27.5, 29.1352, 30.8677, 32.7032, 34.6478, 36.7081, 38.8909, 41.2034, 41.2034, 43.6535, 46.2493, 48.9994, 51.9131
            55, 58.2705, 61.7354, 65.4064, 69.2957, 73.4162, 77.7817, 82.40};

*/
    private double[] intToDouble(int[] intArray){
        double[] newDoubleArray = new double[intArray.length];
        for (int i = 0; i < intArray.length; i++){
            newDoubleArray[i] = intArray[i];
        }
        return newDoubleArray;
    }
    private Callback callback = new Callback() {
        int buffInt = 0;
        int buffSize = 8;
        double[] buffArray = new double[buffSize];


        //NoteCalculator noteCalc = new NoteCalculator(frequency);
        @Override

        public double[] onBufferAvailable(byte[] buffer) {
            audioCalculator.setBytes(buffer);

            int amplitude = audioCalculator.getAmplitude();
            double decibel = audioCalculator.getDecibel();
            int[] nonBytesAudio = audioCalculator.getAmplitudes();
            double[] doublesAudio = intToDouble(nonBytesAudio);
            double yinFreq = yinPitchTracker.getPitchInHz( doublesAudio);
            System.out.println("Yin Freq" + yinFreq);
            double frequency = audioCalculator.getFrequency();
            noteCalc = new NoteCalculator(frequency);
            buffInt++;

           // buffArray[buffInt%buffSize] = noteCalc.retCentsOff(frequency, noteCalc.retClosestFreq(noteCalc.retFreqIndex(frequency), frequency));

           /* if(buffInt%buffSize == buffSize-1){
                System.out.println("Should Be A vib call rn");

                if(Math.abs(median(buffArray)) <= 5){
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(650);
                }
                if(median(buffArray)> 0 && median(buffArray)> 5){
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(new long[] {550, 100}, 1);
                }
                if(median(buffArray) < 0 && median(buffArray) < -5){
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(new long[] {350, 300}, 1);
                }


            }*/



            /*averageFrequency[(aveFreqInd % averageFrequency.length)] = frequency;
            aveFreqInd++;

            double aveFreq = 0;
            for(int i = 0; i < averageFrequency.length; i++){
                aveFreq = aveFreq + averageFrequency[i];
            }
            aveFreq = aveFreq/averageFrequency.length;*/


            //noteCalc = new NoteCalculator(frequency);

            int FreqIndex = noteCalc.retFreqIndex(frequency);
            double closeF = noteCalc.retClosestFreq(FreqIndex, frequency);
            String closeN = noteCalc.retClosestNote(FreqIndex);
            final double centsO = noteCalc.retCentsOff(frequency, closeF);

            double[] returnArray = new double[]{frequency, (double) FreqIndex, centsO};
           /* if(Math.abs(centsO) <= 5)
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate( 10);
            }*/

            //unmodVib(centsO);
            //haptFeed.constantVibrate(centsO);
            /*handler.post(new Runnable() {
                @Override
                public void run() {
                    unmodVib(centsO);


                }
            });*/





            /*final String amp = String.valueOf(amplitude + " Amp");
            final String db = String.valueOf(decibel + " db");
            final String hz = String.valueOf(frequency + " Hz");
            final String cN = String.valueOf("Closest Note: " + closeN);
            final String tS = String.valueOf("Tune Status: " + noteCalc.retTuneStatus(frequency, closeF));
            final String cO = String.valueOf("Cents Off: " + centsO);*/


            return returnArray;
        }
    };
    private Recorder rcdr = new Recorder() {
        @Override
        public void start() {
            if (thread != null) return;
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

                    int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding);
                    //System.out.println(minBufferSize);

                    //buffer size 3584
                    AudioRecord recorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioEncoding, minBufferSize);


                    if (recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                        Thread.currentThread().interrupt();
                        return;
                    } else {
                        Log.i(Recorder.class.getSimpleName(), "Started.");
                        //callback.onStart();
                    }
                    byte[] buffer = new byte[minBufferSize];
                    recorder.startRecording();

                    int buffNum = 0;
                    int buffSize = 8;
                    boolean arrayFull = false;
                    double[] buffFreqArray = new double[buffSize];
                    double[] buffFreqIndexArray = new double[buffSize];
                    double[] buffCentsArray = new double[buffSize];
                    double[] rawDataForYin = new double[buffSize*(minBufferSize/2)];
                    //System.out.println(rawDataForYin.length);
                    double[] buffChromaArray = new double[buffSize];


                    while (thread != null && !thread.isInterrupted() && recorder.read(buffer, 0, minBufferSize) > 0) {
                        double[] retArray = callback.onBufferAvailable(buffer);
                        buffNum++;
                        buffFreqArray[buffNum % buffSize] = retArray[0];
                        buffFreqIndexArray[buffNum % buffSize] = retArray[1];
                        buffCentsArray[buffNum % buffSize] = retArray[2];
                        buffChromaArray[buffNum % buffSize] = noteCalc.retChroma(retArray[0]);
                        int[] nonBytesAudio = audioCalculator.getAmplitudes();
                        double[] doublesAudio = intToDouble(nonBytesAudio);
                        System.out.println(doublesAudio.length);
                        for(int i = 0; i <doublesAudio.length; i++) {
                            rawDataForYin[i * (buffNum % buffSize + 1)] = doublesAudio[i];
                        }


                        //unmodVib(median(buffCentsArray));
                        if (buffNum % buffSize == (buffSize - 1)) {
                            /*double aveFreq= 0;
                            double aveIndex= 0;
                            double aveCents= 0;
                            for(int i = 0; i < buffSize; i++){
                                aveCents = aveCents + buffCentsArray[i];
                                aveFreq = aveFreq + buffFreqArray[i];
                                aveIndex = aveIndex + buffFreqIndexArray[i];
                            }
                            aveCents = aveCents/buffSize;
                            System.out.println(aveCents);
                            aveFreq = aveFreq/buffSize;
                            System.out.println(aveFreq);
                            aveIndex = round(aveIndex/buffSize);
                            System.out.println(aveIndex);*/

                            double aveFreq = median(buffFreqArray);
                            double aveIndex = median(buffFreqIndexArray);
                            final double aveCents = median(buffCentsArray);
                            double aveChroma = median(buffChromaArray);
                            //double yinFreq = yinPitchTracker.getPitchInHz(rawDataForYin);
                            //System.out.println(yinFreq);
                            if(buffNum%buffSize == buffSize-1){
                                //System.out.println("Should Be A vib call rn");

     /*                           if(Math.abs(aveCents) <= 5){
                                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(650);
                                }
                                if(aveCents> 0 && aveCents> 5){
                                //    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(new long[] {550, 100}, 1);
                                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
                                }
                                if(aveCents < 0 && aveCents < -5){
                                    //((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(new long[] {350, 300}, 1);
                                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
                                }
*/
                            }

                            /*//unmodVib(aveCents);
                            String avFrStr = Double.toString(aveFreq);
                            String avCeStr = Double.toString(aveCents);
                            if(avFrStr.length()>8){
                                avFrStr.substring(0, 8);
                            }
                            if(avCeStr.length()>8){
                                avCeStr.substring(0, 8);
                            }*/

                            //final String amp = String.valueOf(amplitude + " Amp");
                            //final String db = String.valueOf(decibel + " db");
                            final String hz = String.valueOf(aveFreq + " Hz");
                            final String cN = String.valueOf("Closest Note: " + noteCalc.retClosestNote((int) aveIndex));
                            final String tS = String.valueOf("Tune Status: " + noteCalc.retTuneStatus(aveFreq, noteCalc.retClosestFreq((int) aveIndex, aveFreq)));
                            final String cO = String.valueOf("Cents Off: " + aveCents);
                            final String cV = String.valueOf("Chroma Val" + aveChroma);
                            final String cC = String.valueOf("Chroma Class" + noteCalc.retPitchClass(aveChroma));


                            //String tuneStatus = noteCalc.retTuneStatus(aveFreq, noteCalc.retClosestFreq((int)aveIndex, aveFreq));

                           /* VibHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    unmodVib(aveCents);
                                }
                            });
*/

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //textAmplitude.setText(amp);
                                    //textDecibel.setText(db);
                                    //textFrequency.setText(hz);
                                    //unmodVib(aveCents);

                                    CurrFreq.setText(hz);
                                    ClosestNote.setText(cN);
                                    TuneStatus.setText(tS);
                                    CentsOff.setText(cO);
                                    ChromaVal.setText(cV);
                                    ChromaClass.setText(cC);

                                }
                            });




                           /* vibHandler = new Handler();
                            vibRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    unmodVib(aveCents);
                                }
                            };
                            vibHandler.post(vibRunnable);*/
                          /*  if(tuneStatus == "In Tune"){
                                inTune();
                            }else if(tuneStatus == "Sharp"){
                                constantSharp();
                            }
                            else if(tuneStatus == "Flat"){
                                constantFlat();
                            }*/


                        }


                    }
                    recorder.stop();
                    recorder.release();
                }
            }, Recorder.class.getName());
            thread.start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_different_mic_test);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        recorder = new Recorder(callback);
        audioCalculator = new AudioCalculator();
        handler = new Handler(Looper.getMainLooper());
        yinPitchTracker = new Yin(44100,
                3584/44100,
                .15,
                3584/44100,
                1,
                .1,
                4200);
        //noteCalc = new NoteCalculator();
        //haptFeed = new HapticFeedback();

        textAmplitude = (TextView) findViewById(R.id.textAmplitude);
        textDecibel = (TextView) findViewById(R.id.textDecibel);
        textFrequency = (TextView) findViewById(R.id.textFrequency);


        CurrFreq = (TextView) findViewById(R.id.Frequency);
        ClosestNote = (TextView) findViewById(R.id.ClosestNote);
        TuneStatus = (TextView) findViewById(R.id.TuneStatus);
        CentsOff = (TextView) findViewById(R.id.CentsOff);
        ChromaVal = (TextView) findViewById(R.id.aveChroma);
        ChromaClass = (TextView) findViewById(R.id.ChromaClass);

        //averageFrequency = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        aveFreqInd = 0;


        currentlyRecording = true;
        startStop = (Button) findViewById(R.id.startStopButton);


        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentlyRecording == false) {
                    startStop.setText("Stop Tuning");

                    CurrFreq.setVisibility(View.VISIBLE);
                    ClosestNote.setVisibility(View.VISIBLE);
                    TuneStatus.setVisibility(View.VISIBLE);
                    CentsOff.setVisibility(View.VISIBLE);
                    ChromaVal.setVisibility(View.VISIBLE);
                    ChromaClass.setVisibility(View.VISIBLE);


                    onResume();

                    currentlyRecording = true;

                } else {
                    startStop.setText("Start Tuning");

                    CurrFreq.setVisibility(View.INVISIBLE);
                    ClosestNote.setVisibility(View.INVISIBLE);
                    TuneStatus.setVisibility(View.INVISIBLE);
                    CentsOff.setVisibility(View.INVISIBLE);
                    ChromaVal.setVisibility(View.INVISIBLE);
                    ChromaClass.setVisibility(View.INVISIBLE);

                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).cancel();

                    currentlyRecording = false;


                    onPause();

                }
            }
        });


    }

    private void Insert(double[] a, int posn, double value) {
        int i;
        for (i = posn - 1; i >= 0 && a[i] > value; i--) {
            a[i + 1] = a[i];
        }
        a[i + 1] = value;

    }

    private double[] insertionSort(double[] a) {
        for (int i = 1; i < a.length; i++) {
            Insert(a, i, a[i]);
        }
        return a;
    }

    private void constantSharp() {
        long[] timingArray = new long[]{550, 100};
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingArray, 0);

    }

    private void constantFlat() {
        long[] timingArray = new long[]{350, 300};
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingArray, 0);
    }

   /* public class VibrationRunnable implements Runnable{
        @Override
        public void run(){
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);


        }

    }*/

    private void inTune() {
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(2);
    }

    public void unmodVib(double cO) {
        if (Math.abs(cO) <= 5) {
            inTune();
        } else if (cO < 0) {
            constantFlat();
        } else {
            constantSharp();
        }
    }

    private double median(double[] a) {
        double[] sortedA = insertionSort(a);
        return (sortedA[sortedA.length / 2] + sortedA[(sortedA.length - 1) / 2]) / 2;

    }

    @Override
    protected void onResume() {
        super.onResume();
        rcdr.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        rcdr.stop();
    }
}
