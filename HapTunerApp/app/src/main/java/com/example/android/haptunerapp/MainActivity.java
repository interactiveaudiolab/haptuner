package com.example.android.haptunerapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;


public class MainActivity extends AppCompatActivity {
    public int vibrationModulation(int cents) {
        return (int) (255 * java.lang.Math.sin(.016 * (double) cents));
    }

    public int flatTiming(int cents) {
        return (int) (9 * cents + 100);
    }


    public int sharpTiming(int cents) {
        return (int) (1.8 * cents + 10);
    }


    public int inverseFlatTiming(int cents) {
        return (int) (-9 * cents + 100);
    }


    public int inverseSharpTiming(int cents) {
        return (int) (5 * cents + 100);
    }

    /*SeekBar vibSeekBar;
    SeekBar timeSeekBar;
    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SeekBar vibSeekBar;
        final SeekBar timeSeekBar;
        final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        final Button micButton;
        final Button otherMicButton;


        vibSeekBar = (SeekBar) findViewById(R.id.vibrationIntensitySeekbar);
        timeSeekBar = (SeekBar) findViewById(R.id.timeIndicatorSeekbar);
        micButton = (Button) findViewById(R.id.micTestButton);
        otherMicButton = (Button) findViewById(R.id.diffMicTestButton);




        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startMicTest = new Intent(MainActivity.this, MicTest.class);
                startActivity(startMicTest);
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).cancel();
            }
        });


        otherMicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startdiffMicTest = new Intent(MainActivity.this, DifferentMicTest.class);
                startActivity(startdiffMicTest);
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).cancel();
            }
        });



        vibSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*int progressChangedValue = 0;*/
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 100) {
                    //long[] timingArray = new long[]{500, flatTiming(progress)};
                    long timingArray[] = new long[]{inverseFlatTiming((50 - progress)), 400};


                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingArray, 0);

                   /* if (Build.VERSION.SDK_INT >= 26) {
                        v.vibrate(VibrationEffect.createWaveform(timingArray, amplitudeArray, 20));
                    } else {
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingArray, 20);

                    }*/

                } else if ((100 - progress) <= 5 && (100 - progress) >= -5) {
                    //long timingArrary[] = new long[]{0, 500};
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).cancel();
                } else {
                    long[] timingArray2 = new long[]{inverseSharpTiming((100 - progress)), 100};
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingArray2, 0);

                  /*  if (Build.VERSION.SDK_INT >= 26) {
                        v.vibrate(VibrationEffect.createWaveform(timingArray, amplitudeArray, 20));
                    } else {
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingArray, 20);

                    }*/
                }
            }

            /* *//*progressChangedValue = progress;*//*
                if (Build.VERSION.SDK_INT >= 26) {

                    v.vibrate(VibrationEffect.createOneShot(500, vibrationModulation(vibSeekBar.getProgress())));
                } else {
                    long[] timingTest = new long[]{500, 100};
                    ;
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(vibrationModulation(vibSeekBar.getProgress()));
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingTest, 0);
                }
            }
*/
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {

            }


        });

        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //int[] amplitudeArray = new int[]{150, 0};
                if (progress < 100) {
                    //long[] timingArray = new long[]{500, flatTiming(progress)};
                    long timingArray[] = new long[]{flatTiming((50 - progress)), 400};


                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingArray, 0);

                   /* if (Build.VERSION.SDK_INT >= 26) {
                        v.vibrate(VibrationEffect.createWaveform(timingArray, amplitudeArray, 20));
                    } else {
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingArray, 20);

                    }*/

                } else if ((100 - progress) <= 5 && (100 - progress) >= -5) {
                    //long timingArrary[] = new long[]{0, 500};
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200000);
                } else {
                    long[] timingArray2 = new long[]{sharpTiming((progress - 50)), 100};
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingArray2, 0);

                  /*  if (Build.VERSION.SDK_INT >= 26) {
                        v.vibrate(VibrationEffect.createWaveform(timingArray, amplitudeArray, 20));
                    } else {
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(timingArray, 20);

                    }*/
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {

            }

        });
        vibSeekBar.callOnClick();
        timeSeekBar.callOnClick();

       /* if (Build.VERSION.SDK_INT >= 26) {

            v.vibrate(VibrationEffect.createOneShot(500, vibrationModulation(vibSeekBar.getProgress())));
        } else{
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(vibrationModulation(vibSeekBar.getProgress()));
        }*/


        //setContentView(R.layout.activity_main);


    }
}
