package com.example.mecpalmo.healyourmic;

public class Global {

    public static final int RECORDER_BPP = 16;
    public static final int channels = 1;
    public static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    public static final String AUDIO_RECORDER_FOLDER = "HealYourMic";
    public static final int RECORDER_SAMPLERATE = 44100; //Hz
    private static final String CalibratingFileName = "cal";
    public static final String OwnRecordFileName = "own";
    private static final String RepairedRecFileName = "rep";
    public static int n = 2048;
    public static int bottomFreq = 100; //Hz
    public static int topFreq = 8000; // Hz
    public static int BandsAmount = 6;
    public static int CalibrationType = 1;
    public static double Reduction = 1;
    public static int MAX_INCREASE = 30; //dB
    public static double[] spectrumCorrection; //lin
    public static double[] spectrumCorrection2; //lin
    public static double[] baseFileSpectrum; //lin
    public static boolean calibrating = false;
    public static boolean baseFilePreparing = false;
    public static boolean recording = false;
    public static boolean repairing = false;
    public static int measStateDif = 0;
    public static String CalFileName = "WhiteNoise.wav";



    public static int getBottomFreqAddress(){
        int a = (int)Math.ceil((double)(n*bottomFreq)/RECORDER_SAMPLERATE);
        return a;
    }

    public static int getTopFreqAddress(){
        int b = (int) Math.floor((double)(n*topFreq)/RECORDER_SAMPLERATE);
        return b;
    }

    public static String returnCalibratingFileName(){
        return (CalibratingFileName + "_" + System.currentTimeMillis());
    }

    public static String returnOwnRecordFileName(){
        return (OwnRecordFileName + "_" + System.currentTimeMillis());
    }

    public static String returnRepairedRecFileName(){
        return (RepairedRecFileName + "_" + System.currentTimeMillis());
    }
}
