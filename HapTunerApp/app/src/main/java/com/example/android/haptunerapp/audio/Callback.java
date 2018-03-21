package com.example.android.haptunerapp.audio;

/**
 * Created by ejmcd on 2/19/2018.
 */

public interface Callback {

    double[] onBufferAvailable(byte[] buffer);
}

