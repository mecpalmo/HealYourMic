package com.example.mecpalmo.healyourmic;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
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

public class Settings extends AppCompatActivity{

    ListView settingList;
    Button saveButton;

    String[] Titles = {"FFT Intensity","Way of Calibration","Spectrum Width","Max Amplification","Select Calibration File","Load Correction from file","Save Current Corrections","Delete all current corrections","Help"};
    String[] Descriptions = new String[Titles.length];

    private int N = Global.n;
    private int CAL_TYPE = Global.CalibrationType;
    private int NUM_OF_BANDS = Global.BandsAmount;
    private int START_FREQ = Global.bottomFreq;
    private int STOP_FREQ = Global.topFreq;
    private int REDUCTION = (int)Global.Reduction*100;
    private String CAL_FILE_NAME = Global.CalFileName;
    private int MaxAmplification = Global.MAX_INCREASE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        settingList = (ListView)findViewById(R.id.listView);
        saveButton = (Button)findViewById(R.id.saveBut);
        fillDescriptions();

        CustomAdapter myAdapter = new CustomAdapter();

        settingList.setAdapter(myAdapter);

        Log.i("OMFG", "Settings Activity Created");

        settingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    switch (N){
                        case 1024:
                            N=2048;
                            break;
                        case 2048:
                            N=4096;
                            break;
                        case 4096:
                            N=1024;
                            break;
                    }
                    fillDescriptions();
                    CustomAdapter myAdapter = new CustomAdapter();
                    settingList.setAdapter(myAdapter);
                }
                if(position==1){
                    switch (CAL_TYPE){
                        case 1:
                            CAL_TYPE = 2;
                            NUM_OF_BANDS = 6;
                            break;
                        case 2:
                            switch (NUM_OF_BANDS){
                                case 6:
                                    NUM_OF_BANDS = 12;
                                    break;
                                case 12:
                                    NUM_OF_BANDS = 18;
                                    break;
                                case 18:
                                    NUM_OF_BANDS = 24;
                                    break;
                                case 24:
                                    CAL_TYPE = 1;
                                    NUM_OF_BANDS = 6;
                                    break;
                            }
                            break;
                    }
                    fillDescriptions();
                    CustomAdapter myAdapter = new CustomAdapter();
                    settingList.setAdapter(myAdapter);
                }
                if(position==2){
                    switch (START_FREQ){
                        case 20:
                            START_FREQ = 40;
                            STOP_FREQ = 13000;
                            break;
                        case 40:
                            START_FREQ = 70;
                            STOP_FREQ = 11000;
                            break;
                        case 70:
                            START_FREQ = 100;
                            STOP_FREQ = 8000;
                            break;
                        case 100:
                            START_FREQ = 20;
                            STOP_FREQ = 16000;
                            break;
                    }
                    fillDescriptions();
                    CustomAdapter myAdapter = new CustomAdapter();
                    settingList.setAdapter(myAdapter);
                }
                if(position==3){
                    switch(MaxAmplification){
                        case 30:
                            MaxAmplification = 20;
                            break;
                        case 20:
                            MaxAmplification = 15;
                            break;
                        case 15:
                            MaxAmplification = 10;
                            break;
                        case 10:
                            MaxAmplification = 5;
                        case 5:
                            MaxAmplification = 30;
                            break;
                    }
                    fillDescriptions();
                    CustomAdapter myAdapter = new CustomAdapter();
                    settingList.setAdapter(myAdapter);
                }
                if(position==4){
                    switch (CAL_FILE_NAME){
                        case "WhiteNoise.wav":
                            CAL_FILE_NAME = "PinkNoise.wav";
                            break;
                        case "PinkNoise.wav":
                            CAL_FILE_NAME = "WhiteNoise.wav";
                            break;
                    }
                    fillDescriptions();
                    CustomAdapter myAdapter = new CustomAdapter();
                    settingList.setAdapter(myAdapter);
                }
                if(position==5){
                    openLoadCorrectionFile();
                }
                if(position==6){
                    saveCorrections();
                }
                if(position==7){
                    Global.spectrumCorrection = new double[Global.n];
                    Global.spectrumCorrection2 = new double[Global.n];
                    Toast.makeText(Settings.this,"Corrections Deleted",Toast.LENGTH_LONG).show();
                }
                if(position==8){
                    openHelp();
                }
            }
        });
    }

    public void openLoadCorrectionFile(){
        Intent i = new Intent(this,LoadCorrectionFile.class);
        startActivity(i);
    }

    public void openHelp(){
        Intent i = new Intent(this,Help.class);
        startActivity(i);
    }

    public void saveSettings(View view){

        Global.BandsAmount = NUM_OF_BANDS;
        Global.CalibrationType = CAL_TYPE;
        Global.topFreq = STOP_FREQ;
        Global.bottomFreq = START_FREQ;
        Global.Reduction = (double)REDUCTION/100;
        Global.MAX_INCREASE = MaxAmplification;

        if(Global.CalFileName != CAL_FILE_NAME || Global.n != N){
            Global.CalFileName = CAL_FILE_NAME;
            Global.n = N;

            Thread preparingBaseThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    synchronized (this) {
                        loadMainFile();
                    }
                }
            },"BaseFile Thread");

            preparingBaseThread.start();
        }

        Toast.makeText(Settings.this,"Saved Settings",Toast.LENGTH_SHORT).show();
    }

    private void fillDescriptions(){
        Descriptions[0] = "N = " + N;

        switch (CAL_TYPE){
            case 1:
                Descriptions[1] = "All DFT Points Correction";
                break;
            case 2:
                Descriptions[1] = "In BandPass Correction (n.o. filters: " + NUM_OF_BANDS + ")";
                break;
            default:
        }

        Descriptions[2] = START_FREQ + " Hz - " + STOP_FREQ + " Hz";

        Descriptions[3] = MaxAmplification + " dB";

        Descriptions[4] = CAL_FILE_NAME;
        Descriptions[5] = "";
        Descriptions[6] = "";
        Descriptions[7] = "";
        Descriptions[8] = "";
    }

    private void saveCorrections(){
        if(!(Global.spectrumCorrection[0]==0.0 || Global.spectrumCorrection==null)){
            double[] corr = Global.spectrumCorrection;
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Global.AUDIO_RECORDER_FOLDER;
            File file = new File(path+"/Points_"+corr.length+"_"+System.currentTimeMillis()+".txt");
            String[] saveText = new String[corr.length];
            for(int i=0;i<corr.length;i++){
                saveText[i] = corr[i]+"";
            }
            SaveFile(file,saveText);
            Toast.makeText(Settings.this,"Saved Points Correction",Toast.LENGTH_SHORT).show();
        }
        if(!(Global.spectrumCorrection2[0]==0.0 || Global.spectrumCorrection2==null)){
            double[] corr = Global.spectrumCorrection2;
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Global.AUDIO_RECORDER_FOLDER;
            File file = new File(path+"/Bands_"+corr.length+"_"+System.currentTimeMillis()+".txt");
            String[] saveText = new String[corr.length];
            for(int i=0;i<corr.length;i++){
                saveText[i] = corr[i]+"";
            }
            SaveFile(file,saveText);
            Toast.makeText(Settings.this,"Saved Bands Correction",Toast.LENGTH_SHORT).show();
        }
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

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return Titles.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.item_layout,null);

            TextView textView_Title = (TextView)convertView.findViewById(R.id.textView14);
            TextView textView_Description = (TextView)convertView.findViewById(R.id.textView15);

            textView_Title.setText(Titles[position]);
            textView_Description.setText(Descriptions[position]);

            return convertView;
        }
    }

    private void loadMainFile(){
        Global.baseFilePreparing = true;
        boolean wasLoaded;


        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Global.AUDIO_RECORDER_FOLDER;

        String Name;
        if(Global.CalFileName == "PinkNoise.wav"){
            Name = "Pink_Noise";
        }else{
            Name = "White_Noise";
        }

        File file = new File(path + "/MAIN_" + Name +"_" + Global.n + ".txt");

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
            for(int j=0;i<array.length;j++){
                newSpectrum[j] = Double.parseDouble(array[j]);
            }
            if(newSpectrum.length == Global.n) {
                Global.baseFileSpectrum = new double[Global.n];
                Global.baseFileSpectrum = newSpectrum;
                wasLoaded = true;
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
                InputStream in = getAssets().open(Global.CalFileName);
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
}
