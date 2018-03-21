package com.example.android.haptunerapp.audio;

/**
 * Created by ejmcd on 3/13/2018.
 */

/*public class Yin {
}*/
//package org.tunebot.model.converter;

/**
 * This pitch tracker is an implementation of the YIN pitch tracker described in the paper
 * A. de Cheveigné and H. Kawahara," YIN, a fundamental frequency estimator for speech and music,"
 * The Journal of the Acoustical Society of America, 111:1917, 2002
 *
 * It is designed to track pitch in single-channel audio encoded as PCM and stored in an array of doubles.
 *
 * Example Useage:
 * 			int sampleRate = someWaveFileReader.getSampleRate('audio.wav');
 * 			double [] monoAudioBuffer = someWaveFileReader.readWavFile('audio.wav');
 * 			Yin ytracker = new Yin(sampleRate);
 *			double [][] pt = ytracker.trackPitch(monoAudioBuffer);
 *			for (int t = 0; t < pt[0].length; t++)
 *				System.out.println("freq in Hz =  " + java.lang.Double.toString(pt[0][t]) + "  time in sec = " + java.lang.Double.toString(pt[1][t]));
 *
 * This was created Nov 7, 2010
 * Copyright Bryan Pardo. All rights reserved.
 * @author pardo
 * @see <a href="http://www.ircam.fr/pcm/cheveign/pss/2002_JASA_YIN.pdf">YIN, a fundamental frequency estimator for speech and music</a>
 */

        import java.lang.ArrayIndexOutOfBoundsException;
        import java.util.Arrays;
        //import org.tunebot.servlet.TunebotServletConfiguration;

public class Yin
{
    // holds the input audio to be evaluated for a fundamental frequency (pitch)
    private double [] analysisWindow;

    // holds the output of step2 of Yin
    private double [] d;

    // holds the ouptout of step 3 of Yin
    private double [] dprime;

    // sample rate of input audio
    private int sampleRateInHz;

    // the size of the audio buffer (measuered in seconds) used to hold a window of audio and analyze it
    private double analysisWindowSizeInSec;

    // This is used to decide when not to label a buffer with a pitch. A value of 0.15 looks like it would be good, based on the paper.
    private double aperiodicityThreshold;

    // This is the distance between analysis window centers in seconds.
    private double hopSizeInSec;

    // We smooth our pitch track by picking the median pitch over a window centered on the current hop. This variable determines how
    // far away (measured in hops) to do smoothing. E.g. a value of 3 makes for a window of 7 pitch estimates, centered on the current estimate.
    private int halfSmoothingWindowSizeInHops;

    // Pitch trackers make octave errors. This value helps set a bias. if epsilon is positive, it will skew the pitch tracker
    // to pick shorter periods (positive octave bias). If epsilon is negative, it will skew things towards lower octaves.
    private double epsilon;

    // The lowest frequency Yin can detect is determined by the lngth of the analysis window. The highest is determined by
    // this value. The default is currently 4200 Hz, or just higher than the highest note on the piano.
    private double highestAllowedFrequencyInHz;

    private int shortestAllowedPeriodInFrames;

    /**
     * The simplest constructor for the Yin pitch tracker. Uses default values for everything except
     * the sample rate of the audio to be processed.
     *
     * @param sampleRateInHz is the expected sample rate of the audio to be pitch tracked.
     */
    public Yin(int sampleRateInHz)
    {
        /*this.sampleRateInHz = sampleRateInHz;
        this.analysisWindowSizeInSec = Double.parseDouble(TunebotServletConfiguration.getProperty("yinAnalysisWindowSizeInSec"));
        this.aperiodicityThreshold = Double.parseDouble(TunebotServletConfiguration.getProperty("yinAperiodicityThreshold"));
        this.hopSizeInSec = Double.parseDouble(TunebotServletConfiguration.getProperty("yinHopSizeInSec"));
        this.halfSmoothingWindowSizeInHops = Integer.parseInt(TunebotServletConfiguration.getProperty("yinHalfSmoothingWindowSizeInHops"));
        this.epsilon = Double.parseDouble(TunebotServletConfiguration.getProperty("yinEpsilon"));
        this.highestAllowedFrequencyInHz = Double.parseDouble(TunebotServletConfiguration.getProperty("yinHighestAllowedFrequencyInHz"));
        this.shortestAllowedPeriodInFrames = java.lang.Math.round((float)sampleRateInHz/(float)highestAllowedFrequencyInHz);*/
    }

    /**
     * This constructor for the Yin pitch tracker lets you controll all the details.
     *
     * @param sampleRateInHz sample rate of input audio
     * @param anaysisWindowSizeInSec the size of the audio buffer (measuered in seconds) used to hold a window of audio and analyze it
     * @param aperiodicityThreshold This is used to decide when not to label a buffer with a pitch. A value of 0.15 looks like it would be good, based on the paper.
     * @param hopSizeInSec This is the distance between analysis window centers in seconds
     * @param halfSmoothingWindowSizeInHops We smooth our pitch track by picking the median pitch over a window centered on the current hop. This
     * variable determines how far away (measured in hops) to do smoothing.
     * @param epsilon Pitch trackers make octave errors. This value helps set a bias. if epsilon is positive, it will skew the
     * pitch tracker to pick shorter periods (positive octave bias). If epsilon is negative, it will skew things towards lower octaves
     * @param highestAllowedFrequencyInHz The lowest frequency Yin can detect is determined by the lngth of the analysis window. The highest is determined
     * by this value. The default is currently 4200 Hz, or just higher than the highest note on the piano.
     */
    public Yin(int sampleRateInHz,
               double anaysisWindowSizeInSec,
               double aperiodicityThreshold,
               double hopSizeInSec,
               int halfSmoothingWindowSizeInHops,
               double epsilon,
               double highestAllowedFrequencyInHz)
    {
        this.sampleRateInHz = sampleRateInHz;
        this.analysisWindowSizeInSec = anaysisWindowSizeInSec;
        this.aperiodicityThreshold = aperiodicityThreshold;
        this.hopSizeInSec = hopSizeInSec;
        this.halfSmoothingWindowSizeInHops = halfSmoothingWindowSizeInHops;
        this.epsilon = epsilon;
        this.highestAllowedFrequencyInHz = highestAllowedFrequencyInHz;
        this.shortestAllowedPeriodInFrames = java.lang.Math.round((float)sampleRateInHz/(float)highestAllowedFrequencyInHz);
    }

    /**
     * Tracks the pitch over the the length of an array of doubles assumed to represent
     * PCM-encoded audio. The hop size between windows is currently the default value
     * of a window of analysis (i.e. analysisWindowSize).
     *
     * @param audio
     * @return an array pitchEstimate where pitchEstimate[0][i] gives the ith pitch estimate in Hz and pitchEstimate[1][i]
     *         gives the time at the ith estimate
     * @throws ArrayIndexOutOfBoundsException
     */
    public double [][] trackPitch(double[] audio) throws ArrayIndexOutOfBoundsException
    {
        // figure out how big our analysis window and hop size are in audio frames
        int hopSize = (int)java.lang.Math.round(this.hopSizeInSec * this.sampleRateInHz) ;
        int analysisWindowSizeInFrames = (int)java.lang.Math.round(this.analysisWindowSizeInSec * this.sampleRateInHz);

        int maxHop = (audio.length / hopSize) - 1;
        if(maxHop < 0)
        {
            throw new ArrayIndexOutOfBoundsException("Audio buffer passed to Yin.trackPitch is shorter than the default audio buffer");
        }

        // estimate the pitch at each hop and put that estimate in an output array.
        int startIdx, endIdx;
        double [][] pitchEstimate = new double [2][maxHop];
        for(int hop = 0; hop< maxHop; hop++)
        {
            startIdx = hop * hopSize;
            endIdx = startIdx + analysisWindowSizeInFrames -1;
            pitchEstimate [0][hop]= this.getPitchInHz(Arrays.copyOfRange(audio, startIdx, endIdx));
            pitchEstimate [1][hop]= (double)startIdx/(double)sampleRateInHz;
        }

        //apply median smoothing to estimate by picking the median of a window around the original estimate
        int distFromWinCenter = this.halfSmoothingWindowSizeInHops;
        double [] origPitch = Arrays.copyOf(pitchEstimate[0], pitchEstimate[0].length);
        for(int i = distFromWinCenter; i < (maxHop - distFromWinCenter); i++)
        {
            double [] myArray = Arrays.copyOfRange(origPitch, i-distFromWinCenter, i+distFromWinCenter);
            insertionSort(myArray);
            pitchEstimate[0][i] = myArray[distFromWinCenter];
        }

        return pitchEstimate;
    }

    /**
     * Classic insertion sort.
     * @param num
     */
    private static void insertionSort(double [] num)
    {
        int j, i;
        double key;
        for(j = 1; j < num.length; j++)
        {
            key = num[j];
            for(i = j-1; (i >= 0) && (num[i] < key); i--)
            {
                num[i+1]=num[i];
            }
            num[i+1] = key;
        }
    }

    /**
     * Takes analysisWindow and finds the best pitch estimate for it in Hz. Note the length of the buffer
     * and the sample rate determine the lowest detectable pitch.
     *
     * @param analysisWindow expected to hold linearly encoded PCM audio. Length must be a multiple of 2.
     * 					The lowest pitch detectable will have a period 1/2 the length of the analysisWindow.
     * @param sampleRateInHz the sample rate of the audio in the analysisWindow.
     * @returns a pitch estimate in Hz
     */
    public double getPitchInHz(double[] analysisWindow)
    {
        this.analysisWindow = analysisWindow;

        // do step 2 (which wraps step 1 from the paper into it) of the Yin algorithm
        differenceFunction();
        // do step 3 (which calculates a ratio of aperiodic to periodic energy)
        cumulativeMeanNormalizedDifferenceFunction();
        // do steps 4 & 6 to generate the pitch estimate (note, step 5 does little, so we leave it out)
        int periodInFrames = findBestPeriod();
        // now return the pitch we found
        double pitchInHz = -1;
        if(periodInFrames > 0)
        {
            pitchInHz = sampleRateInHz/periodInFrames;
        }

        // return the pitch we found
        return pitchInHz;
    }

    // Implements Step 2 from Yin paper. Specifically, equation 6
    private void differenceFunction()
    {
        // now do the formula from the paper.
        d = new double[(int)(analysisWindow.length/2.0)]; // <-- what if the window lenth is not divisible by 2?
        int W = d.length;
        for(int tau = 0; tau < W; tau++)
        {
            d[tau]=0;
            for(int j = 0; j< W; j++)
            {
                double diff = analysisWindow[j] - analysisWindow[j+tau];
                d[tau]= d[tau]+ diff*diff;
            }
        }
    }

    // Step 3: Cumulative mean normalized difference function. Equation 8 in the Yin paper.
    // This is supposed to capture the ratio of periodic energy (d[t][tau]) to aperiodic energy.
    private  void cumulativeMeanNormalizedDifferenceFunction()
    {
        dprime = new double[d.length];
        int W = d.length;
        dprime[0]= 1;
        for(int tau = 1; tau < W; tau++)
        {
            double dsum = 0;
            for(int j = 1; j<=tau; j++)
            {
                dsum = dsum + d[j];
            }
            dprime[tau] = tau*d[tau]/dsum;
        }
    }

    // picking the best period (tau)
    private int findBestPeriod()
    {
        int tauMin  =  shortestAllowedPeriodInFrames;
        if(tauMin < 1)
        {
            tauMin = 1;
        }

        // now do the formula from the paper
        int bestTau = 0;

        for(int tau = tauMin; tau < dprime.length; tau++)
        {
            if((dprime[tau]<(dprime[bestTau] - epsilon)) && (dprime[tau]< aperiodicityThreshold))
                bestTau = tau;
        }
        return bestTau;
    }
}