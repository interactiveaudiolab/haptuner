package com.example.android.haptunerapp.audio;

/**
 * Created by ejmcd on 2/20/2018.
 */

import java.lang.Math;
public class NoteCalculator {

    private double freq;
    private String note;
    private double centsSharp;
    private double centsFlat;
    private String tuneStatus;

    private double freqArray[] = new double[88];
    private int ARefIndex = 48;
    private double AFreq = 440;
    private double[] chromaBins = {0, 1/11, 2/11, 3/11, 4/11, 5/11, 6/11, 7/11, 8/11, 9/11, 10/11, 1};
    private String[] chromaNoteNames = {"C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B"};

    private String noteNameArray[] = new String[88];
    private String[] noteNames = new String[]
            {"A", "A#/Bb","B", "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab"};
   /* private String[] octaveNames = new String[]
            {"0", "1", "2", "3", "4", "5", "6", "7", "8"};*/

    public double[] freqCalculator(double[] farray){
        for(int i = 0; i < 88; i++){
            int diff = i - ARefIndex;
            farray[i] = Math.pow(2, diff/12.0) * AFreq;
        }
        return farray;
    }


    public String[] noteNamePopulator(String[] noteNameArray){
        for(int i = 0; i < 88; i++){
            noteNameArray[i] = noteNames[i % 12];
            if(i < 3){
                noteNameArray[i] = noteNameArray[i]+ "0";
            }else if (i < 15){
                noteNameArray[i] = noteNameArray[i]+ "1";
            } else if (i < 27){
                noteNameArray[i] = noteNameArray[i]+ "2";
            }else if (i < 39){
                noteNameArray[i] = noteNameArray[i]+ "3";
            }else if (i < 51){
                noteNameArray[i] = noteNameArray[i]+ "4";
            }else if (i < 63){
                noteNameArray[i] = noteNameArray[i]+ "5";
            }else if (i < 75){
                noteNameArray[i] = noteNameArray[i]+ "6";
            }else if (i < 87){
                noteNameArray[i] = noteNameArray[i]+ "7";
            }else{
                noteNameArray[i] = noteNameArray[i]+ "8";
            }

        }
        return noteNameArray;
    }








    public NoteCalculator(){

    }
    public NoteCalculator(double freq){
        this.freq = freq;
        note = "";
        centsFlat= 0;
        centsSharp= 0;
        tuneStatus = "";
        freqArray = freqCalculator(freqArray);
        noteNameArray= noteNamePopulator(noteNameArray);

    }



    public int retFreqIndex(double frequen){

        int i = 1;
        //System.out.println(frequen);
        if(frequen > freqArray[87]){
            return 87;
        }
        else if (frequen < freqArray[0]){
            return 0;
        }
        else {
            while (frequen > freqArray[i]) {

                i++;
            }

            return i;
        }

    }

    public double retClosestFreq(int i, double frequen){


        if(frequen == freqArray[i]){
            tuneStatus = "inTune";
            return freqArray[i];
        }

        double left = freqArray[i-1];
        //System.out.println(left);
        double right =freqArray[i];
        //System.out.println(right);
        double midpoint = (right + left)/2;

        if(frequen <= midpoint){
            tuneStatus = "sharp";
            return left;
        }
        else{
            tuneStatus = "flat";
            return right;
        }

    }

    public String retClosestNote(int i){
        if(tuneStatus== "sharp"){
            note = noteNameArray[i-1];
            return note;
        }
        else{
            note=noteNameArray[i];
            return note;
        }
    }

    public double retChroma(double freq){
        double chroma= Math.log(freq)/Math.log(2) - Math.floor(Math.log(freq)/Math.log(2));
        System.out.println(chroma);
        return chroma;
    }

    public String retPitchClass(double chroma){
        int i = 0;
        while (chroma > chromaBins[i]){
            i++;

        }
        if(tuneStatus == "sharp"){
            if(i != 0){
                return chromaNoteNames[i-1];
            }
            else{
                return chromaNoteNames[11];
            }

        }
        else{
            return chromaNoteNames[i];
        }



    }

    public double retCentsOff(double frequen, double closeFreq){
        double centDiff = 1200 *(Math.log(frequen/closeFreq)/Math.log(2));
        if(tuneStatus == "sharp"){
            centsSharp = centDiff;
        }
        else if(tuneStatus =="flat"){
            centsFlat = centDiff;
        }

        return centDiff;
    }
    public String retTuneStatus(double f, double cf){
        double centsOff = retCentsOff(f, cf);
        if(Math.abs(centsOff) <= 5){
            return "In Tune";
        }else if(centsOff < 0){
            return "Flat";
        }
        else{
            return "Sharp";
        }
    }






}
