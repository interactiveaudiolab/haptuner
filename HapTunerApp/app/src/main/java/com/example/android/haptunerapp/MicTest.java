package com.example.android.haptunerapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static java.lang.Math.sqrt;
import static java.lang.Math.log10;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MicTest extends AppCompatActivity {
    // code demo from https://stackoverflow.com/questions/8499042/android-audiorecord-example

    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_8BIT;
    public AudioRecord recorder = null;
    private Thread recordingThread = null;
    public boolean isRecording = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mic_test);

        setButtonHandlers();
        enableButtons(false);

        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        Log.d("buffSize", bufferSize +"instantiated");


    }

    private void checkRecordPermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    123);
        }
    }

    private void setButtonHandlers() {
        ((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);


    }

    private void enableButton(int id, boolean isEnable) {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }
    private void enableTextView(int id, boolean isEnable){
        ((TextView) findViewById(id)).setEnabled(isEnable);
    }
    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnStop, isRecording);
    }

    int BufferElements2Rec = 1024;
    int BytesPerElement = 2;

 /*   private static int[] mSampleRates = new int[]{8000, 11025, 22050, 44100};

    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[]{AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT}) {
                for (short channelConfig : new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
                    try {
                        //Log.d(C.TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                        // + channelConfig);
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e(rate + "Exception, keep trying.", "", e);
                    }
                }
            }
        }
        return null;
    }*/

    public void getValidSampleRates() {
        for (int rate : new int[] {44100, 22050, 11025, 16000, 8000}) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                // buffer size is valid, Sample rate supported
                Log.d("sampleRate", rate + "succeeded");

            }
            else{
                Log.d("sampleRate", rate + "failed");
            }
        }
    }
    private void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
        RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING,
         BufferElements2Rec * BytesPerElement);
        checkRecordPermission();
        //recorder = findAudioRecord();
        getValidSampleRates();
        recorder.startRecording();
        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();

            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private byte[] short2byte(short[] sData) {
        int shortArrSize = sData.length;
        byte[] bytes = new byte[shortArrSize * 2];
        for (int i = 0; i < shortArrSize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    private double rms_db(short[] buffer, int ref){
        double sum = 0;
        int numSigVals = buffer.length;
        for(int i = 0; i < numSigVals; i++){
            sum = sum + ((double)buffer[i] * (double)buffer[i]);
        }
        double ms = sum/numSigVals;
        double rms = sqrt(ms);

        double decibel = 20 * log10(rms/ref);
        return decibel;


    }
    private void setText(final TextView text, final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }
    //changing function
    private void writeAudioDataToFile() {
        //String filePath = "/voice8k16bitmono.pcm";
        short sData[] = new short[BufferElements2Rec];

       /* FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
*/
        while (isRecording) {
            recorder.read(sData, 0, BufferElements2Rec);
            //System.out.println(sData);
            //TextView micReadout = (TextView)findViewById(R.id.micReadout);
            System.out.println("New Buffer");
            for(int i = 0; i < sData.length; i++){
                sData[i] = (short)(sData[i] + 32768);
                System.out.println(sData[i]);
            }

            double dB = rms_db(sData, 1);
            String data = (dB+ "dB");
            setText((TextView)findViewById(R.id.micReadout), data);
            //micReadout.setText(dB +"dB");

            //System.out.println(dB);

//            System.out.println("Short writing to file" + sData.toString());
//            try {
//                byte bData[] = short2byte(sData);
//                os.write(bData, 0, BufferElements2Rec * BytesPerElement);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
//        try {
//            os.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        }
    }

    private void stopRecording() {
        if (null != recorder) {
            isRecording = false;

            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStart: {
                    //enableTextView(R.id.micReadout, true);
                    enableButtons(true);
                    startRecording();
                    break;
                }
                case R.id.btnStop: {
                    //enableTextView(R.id.micReadout, true);
                    enableButtons(false);
                    stopRecording();
                    break;
                }
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


}
