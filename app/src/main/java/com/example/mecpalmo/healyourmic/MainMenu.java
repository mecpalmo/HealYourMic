package com.example.mecpalmo.healyourmic;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainMenu extends AppCompatActivity {

    Button calibrateBut, recordBut, repairBut, settingsBut;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        calibrateBut = (Button)findViewById(R.id.calibrateBut);
        recordBut = (Button)findViewById(R.id.recordBut);
        repairBut = (Button)findViewById(R.id.repairBut);
        settingsBut = (Button)findViewById(R.id.settingsBut);
        textView = (TextView)findViewById(R.id.textView);
        Global.spectrumCorrection = new double[Global.n];
        Global.spectrumCorrection2 = new double[Global.n];

        Thread preparingBaseThread = new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (this) {
                    loadingBaseFile();
                }
            }
        },"BaseFile Thread");

        preparingBaseThread.start();

        Log.i("OMFG", "Main menu created");
    }

    public void calibrateClick(View view){
        Intent i = new Intent(this, Calibrating.class);
        startActivity(i);
    }

    public void soundlevelClick(View view){
        Intent i = new Intent(this,Measuring.class);
        startActivity(i);
    }

    public void recordClick(View view){
        Intent i = new Intent(this, Recording.class);
        startActivity(i);
    }

    public void repairClick(View view){
        Intent i = new Intent(this, Repairing.class);
        startActivity(i);
    }

    public void settingsClick(View view){
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }

    private void loadingBaseFile(){

        boolean wasLoaded;
        Global.baseFilePreparing = true;

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Global.AUDIO_RECORDER_FOLDER;
        File file = new File(path + "/MAIN_" + "White_Noise_" + Global.n + ".txt");

        if(file.exists()){
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(file);
            }
            catch (FileNotFoundException e) {e.printStackTrace();}
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String test;
            int anzahl=0;
            try{
                while ((test=br.readLine()) != null){
                    anzahl++;
                }
            }catch (IOException e) {e.printStackTrace();}

            try{
                fis.getChannel().position(0);
            }catch (IOException e) {e.printStackTrace();}

            String[] array = new String[anzahl];

            String line;
            int i = 0;
            try{
                while((line=br.readLine())!=null){
                    array[i] = line;
                    i++;
                }
            }catch (IOException e) {e.printStackTrace();}

            double[] newSpectrum = new double[array.length];
            Log.i("OMFG","Strings:"+array[0]+" "+array[1]);
            for(int j=0;j<array.length;j++){
                newSpectrum[j] = Double.parseDouble(array[j]);
            }
            Log.i("OMFG","doubles:"+newSpectrum[0]+" "+newSpectrum[1]);
            if(newSpectrum.length == Global.n) {
                Global.baseFileSpectrum = newSpectrum;
                wasLoaded = true;
                Log.i("OMFG","i managed to load it, length:"+newSpectrum.length);
            }else{
                wasLoaded = false;
            }
        }else{
            wasLoaded = false;
        }

        if(!wasLoaded) {

            int n = Global.n;
            double[] baseFileSpectrum = new double[n];

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] byteBaseFile = new byte[1];
            try {
                InputStream in = getAssets().open("WhiteNoise.wav");
                int size = in.available();
                byteBaseFile = new byte[size];
                in.read(byteBaseFile);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteBaseFile = WavFile.removeWaveHeader(byteBaseFile);
            short[] shortBaseFile = Converter.byteToShort(byteBaseFile);
            double[] doubleBaseFile = Converter.shortToDouble(shortBaseFile);
            double[] doubleInput = new double[n];

            for (int i = 0; i < n; i++) {
                baseFileSpectrum[i] = 0.0;
            }

            int intervals = doubleBaseFile.length / n;
            for (int i = 0; i < intervals; i++) {
                Complex[] fftinput;
                for (int j = 0; j < n; j++) {
                    doubleInput[j] = doubleBaseFile[j + (i * n)];
                }
                doubleInput = Windowing.Hanning(doubleInput);
                fftinput = Converter.doubleToComplex(doubleInput);
                Complex[] fftoutput = FFT.fft(fftinput);
                double[] doubleSpectrum = Converter.complexToDoubleAbs(fftoutput);
                for (int j = 0; j < n; j++) {
                    baseFileSpectrum[j] = baseFileSpectrum[j] + (doubleSpectrum[j] / (double) intervals);
                }
            }

            Global.baseFileSpectrum = baseFileSpectrum;

            String[] saveText = new String[baseFileSpectrum.length];
            for (int i = 0; i < baseFileSpectrum.length; i++) {
                saveText[i] = baseFileSpectrum[i] + "";
            }
            SaveFile(file, saveText);
        }


        Log.i("OMFG", "base file completed");
        Global.baseFilePreparing = false;
    }

    private void SaveFile(File file, String[] data){
        FileOutputStream fos = null;

        try{
            fos = new FileOutputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}

        try{
            try{

                for (int i = 0; i<data.length; i++) {
                    fos.write(data[i].getBytes());
                    if (i < data.length-1) {
                        fos.write("\n".getBytes());
                    }
                }
            }catch (IOException e) {e.printStackTrace();}
        }finally{
            try{
                fos.close();
            }catch (IOException e) {e.printStackTrace();}
        }
    }
}
