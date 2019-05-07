package com.example.mecpalmo.healyourmic;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioMeasurer {
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private int bufferMultiplier = 1;

    public void initiateRecording(){

        int n = 8192;
        bufferSize = 2*n;
        //bufferSize = AudioRecord.getMinBufferSize(Global.RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, Global.RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

        if(recorder.getState()==1)
            recorder.startRecording();

        Global.recording = true;
    }

    public short[] returnSound(){
        byte data[] = new byte[bufferMultiplier*bufferSize];

        for(int i=0;i<bufferMultiplier;i++){
            recorder.read(data, 0, bufferSize*bufferMultiplier);
        }

        short[] shortData = Converter.byteToShort(data);
        return shortData;
    }

    public void cancelRecording(){
        if(null != recorder){

            Global.recording = false;
            int i = recorder.getState();
            if(i==1)
                recorder.stop();

            recorder.release();
            recorder = null;
        }
    }
}
