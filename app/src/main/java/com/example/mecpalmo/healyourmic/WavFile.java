package com.example.mecpalmo.healyourmic;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WavFile {

    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

    public static String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,Global.AUDIO_RECORDER_FOLDER);
        if(!file.exists()){
            file.mkdirs();
        }
        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    public static void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    public static void copyWaveFile(String inFilename,String outFilename, int bufferSize){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = Global.RECORDER_SAMPLERATE;
        long byteRate = Global.RECORDER_BPP * Global.RECORDER_SAMPLERATE * Global.channels/8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Log.i("OMG", "totalAudioLen= " + totalAudioLen);
            Log.i("OMG", "totalDataLen= " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, Global.channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = Global.RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    public static void SaveByteToWavFile(byte[] bytes, String name){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,Global.AUDIO_RECORDER_FOLDER);
        if(!file.exists()){
            file.mkdirs();
        }

        String filename = file.getAbsolutePath() + "/" + name + Global.AUDIO_RECORDER_FILE_EXT_WAV;
        FileOutputStream os = null;

        long byteRate = Global.RECORDER_BPP * Global.RECORDER_SAMPLERATE * Global.channels/8;

        try {
            os = new FileOutputStream(filename);
            WriteWaveFileHeader(os, bytes.length,bytes.length+36, Global.RECORDER_SAMPLERATE, Global.channels, byteRate);
            os.write(bytes);
            os.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] loadFileFromFolder(String filename){
        byte[] bufferTemp = new byte[1];
        if(isExternalStorageReadable()){
            try{
                String filepath = Environment.getExternalStorageDirectory().getPath();
                File file = new File(filepath,Global.AUDIO_RECORDER_FOLDER+"/"+filename);
                FileInputStream fis = new FileInputStream(file);

                if(fis != null){
                    //InputStreamReader isr = new InputStreamReader(fis);
                    int size = fis.available();
                    bufferTemp = new byte[size];
                    fis.read(bufferTemp);
                    fis.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bufferTemp;
    }

    public static byte[] removeWaveHeader(byte[] bytes){
        byte[] buffer = new byte[bytes.length - 44];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = bytes[i + 44];
        }
        return buffer;
    }

    private static boolean isExternalStorageReadable(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())||Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())){
            Log.i("State","Yes, it is, readable!");
            return true;
        } else{
            return false;
        }
    }
}
