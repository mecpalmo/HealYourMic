package com.example.mecpalmo.healyourmic;

import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class Calibrating extends AppCompatActivity {

    Button recBut, stopBut, loadBut;
    TextView textView, textView8;
    AudioRecorder recorder;
    Switch switcher;
    private Timer myTimer;
    private int currentTime;
    String timeText;
    private int current_progress;
    private boolean ShouldPlay = false;
    MediaPlayer myPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrating);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        recBut = (Button)findViewById(R.id.rec2But);
        stopBut = (Button)findViewById(R.id.stop2But);
        loadBut = (Button)findViewById(R.id.loadBut);
        textView = (TextView)findViewById(R.id.textView2);
        textView8 = (TextView)findViewById(R.id.textView8);
        switcher = (Switch)findViewById(R.id.switch2);
        myPlayer = new MediaPlayer();

        recorder = new AudioRecorder();
        stopBut.setEnabled(false);
        loadBut.setEnabled(false);
        currentTime = 0;
        if(Global.calibrating){
            textView.setText("Currently Calibrating");
        }
        initiateListener();
    }

    public void recClick(View view){
        if(!Global.recording) {
            textView.setText("It's Fine");
            if(ShouldPlay){
                playSound();
            }
            recBut.setEnabled(false);
            loadBut.setEnabled(false);
            stopBut.setEnabled(true);
            recorder.createFilePath(Global.returnCalibratingFileName());
            recorder.startRecording();
            currentTime = 0;
            startTimeCounting();
        }else{
            textView.setText("Currently Busy");
        }
    }

    public void stopClick(View view){
        stopTheRec();
    }

    private void stopTheRec(){
        if(myPlayer.isPlaying()){
            myPlayer.stop();
            myPlayer.release();
            myPlayer = new MediaPlayer();
        }
        stopBut.setEnabled(false);
        recorder.stopRecording();
        recBut.setEnabled(true);
        loadBut.setEnabled(true);
        myTimer.cancel();
    }

    public void loadClick(View view){
        if(!Global.baseFilePreparing && !Global.recording && !Global.calibrating && !Global.repairing) {
            textView.setText("It's Fine");

            Thread calibratingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        if(Global.CalibrationType==1){
                            loadingCalibrateFile();
                        }
                        if(Global.CalibrationType==2){
                            loadingCalibrateFile2();
                        }
                    }
                }
            },"Calibrating Thread");

            calibratingThread.start();
        }else{
            textView.setText("Currently Busy");
        }
    }

    private void loadingCalibrateFile(){
        Global.calibrating = true;
        current_progress = 0;
        double[] baseFileSpectrum = Global.baseFileSpectrum;
        int n = Global.n;
        n = baseFileSpectrum.length;
        int minFreqIndex = Global.getBottomFreqAddress();
        int maxFreqIndex = Global.getTopFreqAddress();


        byte[] byteCalibrateFile = WavFile.loadFileFromFolder(recorder.getFileName());
        byteCalibrateFile = WavFile.removeWaveHeader(byteCalibrateFile);
        short[] shortCalibrateFile = Converter.byteToShort(byteCalibrateFile);
        double[] doubleCalibrateFile = Converter.shortToDouble(shortCalibrateFile);
        double[] doubleInput = new double[n];

        double[] calibrateFileSpectrum = new double[n];
        for(int i=0; i<n; i++){
            calibrateFileSpectrum[i] = 0.0;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("Progress: "+current_progress+"%");
            }
        });

        int intervals = doubleCalibrateFile.length / n;
        for(int i=0; i<intervals; i++){
            for(int j=0; j<n; j++){
                doubleInput[j] = doubleCalibrateFile[j+(i*n)];
            }
            doubleInput = Windowing.Hanning(doubleInput);
            Complex [] fftinput = Converter.doubleToComplex(doubleInput);
            Complex[] fftoutput = FFT.fft(fftinput);
            double[] doubleSpectrum = Converter.complexToDoubleAbs(fftoutput);
            for(int j=0; j<n; j++){
                calibrateFileSpectrum[j] = calibrateFileSpectrum[j] + (doubleSpectrum[j] / (double)intervals);
            }
            if((i*100/intervals)>current_progress){
                current_progress = i*100/intervals;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Progress: "+current_progress+"%");
                    }
                });
            }
        }

        double[] logCalibrateSpectrum = Converter.ampLinearToLog(calibrateFileSpectrum);
        double[] logBaseSpectrum = Converter.ampLinearToLog(baseFileSpectrum);

        double[] logDifference = new double[n];

        for(int i=0; i<n; i++){
            logDifference[i] = (logBaseSpectrum[i] - logCalibrateSpectrum[i]);
        }

        double logMinDif = logDifference[minFreqIndex];
        for(int i=minFreqIndex; i<maxFreqIndex; i++){
            if(logDifference[i] < logMinDif){
                logMinDif = logDifference[i];
            }
        }

        if(logMinDif > 0){
            for(int i=0; i<n;i++){
                logDifference[i] = logDifference[i] - logMinDif;
            }
        }

        Global.spectrumCorrection = new double[n];
        Global.spectrumCorrection = Converter.ampLogToLinear(logDifference);

        Global.calibrating = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("No current process running");
            }
        });
        Log.i("OMFG", "calibrating complete");
    }

    private void playSound(){
        try {
            if (myPlayer.isPlaying()) {
                myPlayer.stop();
                myPlayer.release();
                myPlayer = new MediaPlayer();
            }

            AssetFileDescriptor descriptor = getAssets().openFd(Global.CalFileName);
            myPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            myPlayer.prepare();
            myPlayer.setVolume(1f, 1f);
            myPlayer.setLooping(true);
            myPlayer.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadingCalibrateFile2(){

        Global.calibrating = true;
        current_progress = 0;
        double[] baseFileSpectrum = Global.baseFileSpectrum;
        int n = baseFileSpectrum.length;
        int minFreqIndex = Global.getBottomFreqAddress();
        int maxFreqIndex = Global.getTopFreqAddress();

        BandPass MyBands = new BandPass(Global.BandsAmount,n);
        byte[] byteCalibrateFile = WavFile.loadFileFromFolder(recorder.getFileName());
        byteCalibrateFile = WavFile.removeWaveHeader(byteCalibrateFile);
        short[] shortCalibrateFile = Converter.byteToShort(byteCalibrateFile);
        double[] doubleCalibrateFile = Converter.shortToDouble(shortCalibrateFile);
        double[] doubleInput = new double[n];

        double[] calibrateFileSpectrum = new double[n];
        for(int i=0; i<n; i++){
            calibrateFileSpectrum[i] = 0.0;
        }

        int intervals = doubleCalibrateFile.length / n;
        for(int i=0; i<intervals; i++){
            for(int j=0; j<n; j++){
                doubleInput[j] = doubleCalibrateFile[j+(i*n)];
            }
            doubleInput = Windowing.Hanning(doubleInput);
            Complex[] fftinput = Converter.doubleToComplex(doubleInput);
            Complex[] fftoutput = FFT.fft(fftinput);
            double[] doubleSpectrum = Converter.complexToDoubleAbs(fftoutput);
            for(int j=0; j<n; j++){
                calibrateFileSpectrum[j] = calibrateFileSpectrum[j] + (doubleSpectrum[j] / (double)intervals);
            }
            if((i*100/intervals)>current_progress){
                current_progress = i*100/intervals;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Progress: "+current_progress+"%");
                    }
                });
            }
        }

        double[] logCalibrateSpectrum = Converter.ampLinearToLog(calibrateFileSpectrum);
        double[] logBaseSpectrum = Converter.ampLinearToLog(baseFileSpectrum);

        double[] logDifference = new double[n];
        double[] BaseBands = new double[MyBands.returnAmount()];
        double[] CalibrateBands = new double[MyBands.returnAmount()];

        for(int i=0;i<MyBands.returnAmount();i++){
            double CalPower = 0;
            double BasePower = 0;
            int count = MyBands.borderValue(i+1) - MyBands.borderValue(i);
            for(int j=MyBands.borderValue(i);j<MyBands.borderValue(i+1);j++){
                CalPower = CalPower + logCalibrateSpectrum[j]/count;
                BasePower = BasePower + logBaseSpectrum[j]/count;
            }
            CalibrateBands[i] = CalPower;
            BaseBands[i] = BasePower;
        }

        for (int i=0;i<MyBands.borderValue(0);i++){
            logDifference[i] = 0.0;
        }
        for (int i=MyBands.borderValue(MyBands.returnAmount());i<n;i++){
            logDifference[i] = 0.0;
        }

        for(int i=0; i<MyBands.returnAmount(); i++){
            for(int j=MyBands.borderValue(i);j<=MyBands.borderValue(i+1);j++) {
                logDifference[j] = (BaseBands[i] - CalibrateBands[i]);
            }
        }

        double logMinDif = logDifference[minFreqIndex];
        for(int i=minFreqIndex; i<maxFreqIndex; i++){
            if(logDifference[i] < logMinDif){
                logMinDif = logDifference[i];
            }
        }

        if(logMinDif > 0.0){
            for(int i=minFreqIndex; i<=maxFreqIndex;i++){
                logDifference[i] = logDifference[i] - logMinDif;
            }
        }

        Global.spectrumCorrection2 = new double[n];
        Global.spectrumCorrection2 = Converter.ampLogToLinear(logDifference);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("No current process running");
            }
        });
        Log.i("OMFG", "calibrating complete");
        Global.calibrating = false;
    }

    private void countingTime(){
        int sec = currentTime%60;
        int min = currentTime/60;
        if(sec>=10) {
            timeText = min + ":" + sec;
        }else{
            timeText = min + ":0" + sec;
        }
        int pol = 76;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView8.setText(timeText);
            }
        });
        currentTime++;
    }

    private void startTimeCounting() {
        myTimer = new Timer();
        myTimer.scheduleAtFixedRate(new ScheduleTask(), 0, 1000); //okres wynosi 1000 ms czyli 1s
    }

    private void initiateListener(){

        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = switcher.isChecked();

                if(isChecked){
                    ShouldPlay = true;
                }else{
                    ShouldPlay = false;
                }
            }
        });
    }

    private class ScheduleTask extends TimerTask {
        @Override
        public void run() {
            countingTime();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(Global.recording == true){
                stopTheRec();
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
