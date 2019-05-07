package com.example.mecpalmo.healyourmic;

import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.security.spec.EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

public class Repairing extends AppCompatActivity {

    Button repairBut;
    TextView textView;
    TextView textView16;
    ListView listView2;
    String loadFileName = "";
    int current_progress;
    MediaPlayer myPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairing);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        repairBut = (Button)findViewById(R.id.repairBut);
        textView = (TextView)findViewById(R.id.textView3);
        textView16 = (TextView)findViewById(R.id.textView16);
        listView2 = (ListView)findViewById(R.id.listView2);
        myPlayer = new MediaPlayer();

        List<String> fileList = new ArrayList<String>();
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,Global.AUDIO_RECORDER_FOLDER);
        File[] fs = file.listFiles();
        fileList.clear();
        for(File f: fs){
            if(f.getName().indexOf(Global.OwnRecordFileName)!=-1 && f.getName().indexOf(Global.AUDIO_RECORDER_FILE_EXT_WAV) !=-1) {
                fileList.add(f.getName());
            }
        }
        ArrayAdapter<String> fileNameList = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,fileList);
        listView2.setAdapter(fileNameList);

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadFileName = String.valueOf(parent.getItemAtPosition(position));
                playSound(loadFileName);
                textView16.setText("Selected: " + loadFileName);
            }
        });

        if(Global.calibrating){
            textView.setText("Currently Calibrating");
        }
        if(Global.repairing){
            textView.setText("Currently Repairing");
        }
    }

    public void repairClick(View view){
        if(!Global.recording && !Global.repairing && !Global.calibrating) {
            if(loadFileName!="") {
                stopPlaying();
                textView.setText("It's Fine");

                Thread repairingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            repairingOwnFile(Global.CalibrationType);
                        }
                    }
                }, "Repairing Thread");

                repairingThread.start();
            }else{
                textView.setText("No file selected");
            }
        }else{
            textView.setText("Currently Busy");
        }
    }

    private void repairingOwnFile(int type){

        Global.repairing = true;
        byte[] byteOwnFile = WavFile.loadFileFromFolder(loadFileName);
        byteOwnFile = WavFile.removeWaveHeader(byteOwnFile);
        short[] shortOwnFile = Converter.byteToShort(byteOwnFile);
        double[] doubleOwnFile = Converter.shortToDouble(shortOwnFile);

        current_progress = 0;
        int n = Global.n;
        double[] spectrumCorrection;
        if(type == 1){
            spectrumCorrection = Global.spectrumCorrection;
            n = spectrumCorrection.length;
        }else if(type == 2){
            spectrumCorrection = Global.spectrumCorrection2;
            n = spectrumCorrection.length;
        }else{
            spectrumCorrection = new double[n];
        }
        int minFreqIndex = Global.getBottomFreqAddress();
        int maxFreqIndex = Global.getTopFreqAddress();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("Progress: "+current_progress+"%");
            }
        });

        double MAX_INCREASE = (double)Global.MAX_INCREASE; //dB
        double[] logCorrection = Converter.ampLinearToLog(spectrumCorrection);
        double max = logCorrection[minFreqIndex];
        for(int i=minFreqIndex+1;i<maxFreqIndex;i++){
            if(max < logCorrection[i]){
                max = logCorrection[i];
            }
        }
        if(max>MAX_INCREASE){
            double reduce = max - MAX_INCREASE;
            for(int i=0; i<logCorrection.length;i++){
                logCorrection[i] = logCorrection[i] - reduce;
            }
        }

        for(int i=0;i<spectrumCorrection.length;i++) {
            logCorrection[i]=logCorrection[i]*0.95;
        }

        spectrumCorrection = Converter.ampLogToLinear(logCorrection);

        int intervals = doubleOwnFile.length/n;
        double[] doubleOutput = new double[intervals*n];
        for(int i=0; i<intervals; i++) {
            double[] doubleTemp = new double[n];
            for (int j = 0; j < n; j++) {
                doubleTemp[j] = doubleOwnFile[j + (i * n)];
            }
            Complex[] fftinput = Converter.doubleToComplex(doubleTemp);
            Complex[] fftoutput = FFT.fft(fftinput);
            for (int j = minFreqIndex; j < maxFreqIndex; j++){
                fftoutput[j] = fftoutput[j].scale(spectrumCorrection[j]);
                fftoutput[n-j] = fftoutput[n-j].scale(spectrumCorrection[j]);
            }
            fftinput = FFT.ifft(fftoutput);
            doubleTemp = Converter.complexToDoubleRe(fftinput);
            for (int j = 0; j < n; j++) {
                doubleOutput[j + (i * n)] = doubleTemp[j];
            }
            if(i*100/intervals>current_progress){
                current_progress=i*100/intervals;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Progress: "+current_progress+"%");
                    }
                });
            }
        }
        short[] shortOutput = Converter.doubleToShort(doubleOutput);
        byte[] byteOutput = Converter.shortToByte(shortOutput);
        WavFile.SaveByteToWavFile(byteOutput,Global.returnRepairedRecFileName());

        Global.repairing = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("No current process running");
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Repairing.this,"Calibration Complete",Toast.LENGTH_LONG);
            }
        });
    }

    private void playSound(String name){
        try {
            if (myPlayer.isPlaying()) {
                myPlayer.stop();
                myPlayer.release();
                myPlayer = new MediaPlayer();
            }else{
                myPlayer.release();
                myPlayer = new MediaPlayer();
            }


            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Global.AUDIO_RECORDER_FOLDER+"/"+name;
            myPlayer.setDataSource(path);

            myPlayer.prepare();
            myPlayer.setVolume(1f, 1f);
            myPlayer.setLooping(false);
            myPlayer.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void stopPlaying(){
        try {
            if (myPlayer.isPlaying()) {
                myPlayer.stop();
                myPlayer.release();
                myPlayer = new MediaPlayer();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
                stopPlaying();
        }

        return super.onKeyDown(keyCode, event);
    }
}
