package com.example.mecpalmo.healyourmic;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecorder {

    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private String filePath;
    private String fileName;

    public String getFileName(){
        return fileName;
    }

    public void createFilePath(String name){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,Global.AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        fileName = name + Global.AUDIO_RECORDER_FILE_EXT_WAV;

        filePath = file.getAbsolutePath() + "/" + name + Global.AUDIO_RECORDER_FILE_EXT_WAV;
    }

    public void startRecording(){

        bufferSize = AudioRecord.getMinBufferSize(Global.RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING);

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                Global.RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSize);

        int i = recorder.getState();
        if(i==1)
            recorder.startRecording();

        Global.recording = true;

        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread");

        recordingThread.start();
    }

    private void writeAudioDataToFile(){
        byte data[] = new byte[bufferSize];
        String filename = WavFile.getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;

        if(null != os){
            while(Global.recording){
                read = recorder.read(data, 0, bufferSize);
                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRecording(){
        if(null != recorder){

            Global.recording = false;

            int i = recorder.getState();
            if(i==1)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        WavFile.copyWaveFile(WavFile.getTempFilename(),filePath,bufferSize);
        WavFile.deleteTempFile();
    }
}
