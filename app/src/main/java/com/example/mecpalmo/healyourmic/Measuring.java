package com.example.mecpalmo.healyourmic;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.DecimalFormat;

public class Measuring extends AppCompatActivity {

    Switch onSwitch;
    TextView textView11;
    TextView textView10;
    TextView textView12;
    TextView textView13;
    Button myButton, upButton, downButton;
    RadioGroup radioGroup;
    RadioButton radioButton;
    boolean isMeasuring;
    AudioMeasurer myMeasurer;
    private int stateDifference;
    String Level;
    double MAX_VALUE;
    double MIN_VALUE;
    private int WeightType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measuring);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textView10 = (TextView)findViewById(R.id.textView10);
        onSwitch = (Switch)findViewById(R.id.switch1);
        textView11 = (TextView)findViewById(R.id.textView11);
        myButton = (Button)findViewById(R.id.applyBut);
        upButton = (Button)findViewById(R.id.upBut);
        downButton = (Button)findViewById(R.id.downBut);
        textView12 = (TextView)findViewById(R.id.textView12);
        textView13 = (TextView)findViewById(R.id.textView13);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);

        isMeasuring = false;
        stateDifference = Global.measStateDif;
        initiateListener();
        WeightType = 1;
    }

    private void switchOn(){
        Thread measuringThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    doMeasure();
                }
            }
        },"Measuring Thread");

        measuringThread.start();
    }

    private void switchOff(){
        isMeasuring = false;
        myMeasurer.cancelRecording();
    }

    public void applyClick(View view){
        Global.measStateDif = stateDifference;
    }

    public void upClick(View view){
        if(stateDifference<100) {
            stateDifference++;
            if(stateDifference>0) {
                textView10.setText("+" + stateDifference + " dB");
            }else{
                textView10.setText("" + stateDifference + " dB");
            }
        }
    }

    public void downClick(View view){
        if(stateDifference>-100){
            stateDifference--;
            if(stateDifference>0) {
                textView10.setText("+" + stateDifference + " dB");
            }else{
                textView10.setText("" + stateDifference + " dB");
            }
        }
    }

    private void doMeasure(){
        isMeasuring = true;
        myMeasurer = new AudioMeasurer();
        myMeasurer.initiateRecording();

        int n = Global.n;
        double amplitudeRef = 0.001;
        long RECORDER_SAMPLERATE = Global.RECORDER_SAMPLERATE;

        MAX_VALUE = 0.0;
        MIN_VALUE = 200.0;

        int MinIndex = (int) Math.round((double)(n * 100) / RECORDER_SAMPLERATE);
        int MaxIndex = (int) Math.round((double)(n * 8000) / RECORDER_SAMPLERATE);

        double[] spectrumCorrection;
        boolean NoCorrection;

        Log.i("OMFG","before setting correction");

        if(Global.CalibrationType==1 && Global.spectrumCorrection.length==n){
            spectrumCorrection = Global.spectrumCorrection;
            NoCorrection = false;

        }else if(Global.CalibrationType==2 && Global.spectrumCorrection2.length==n){
            spectrumCorrection = Global.spectrumCorrection2;
            NoCorrection = false;

        }else{
            spectrumCorrection = new double[n];
            NoCorrection = true;
        }
        Log.i("OMFG","before creating weighter");

        weighting weighter = new weighting(n);

        Log.i("OMFG","before while");

        while (isMeasuring){
            short[] shortSound = myMeasurer.returnSound();
            double[] doubleSound = Converter.shortToDouble(shortSound);
            double[] doubleInput = new double[n];

            int stateDif = Global.measStateDif;
            int intervals = doubleSound.length/n;

            double[] avgSpectrum = new double[n];
            for(int i=0; i<n; i++){
                avgSpectrum[i] = 0.0;
            }

            for(int i=0; i<intervals; i++){
                for(int j=0; j<n; j++){
                    doubleInput[j] = doubleSound[j+(i*n)];
                }
                doubleInput = Windowing.Hanning(doubleInput);
                Complex[] fftinput = Converter.doubleToComplex(doubleInput);
                Complex[] fftoutput = FFT.fft(fftinput);
                double[] doubleSpectrum = Converter.complexToDoubleAbs(fftoutput);
                for(int j=0; j<n; j++){
                    avgSpectrum[j] = avgSpectrum[j] + (doubleSpectrum[j] / intervals);
                }
            }

            if(spectrumCorrection[0]!=0.0 && !NoCorrection){
                for(int i = MinIndex;i < MaxIndex; i++){
                    avgSpectrum[i]*=spectrumCorrection[i];
                }
            }

            int current_index = 1;
            if(WeightType==1){
                for(int i=0;i<n/2;i++){
                    if(i<weighter.Indexes[current_index]){
                        avgSpectrum[i]*=weighter.linValuesC[current_index-1];
                    }else{

                        while(i>=weighter.Indexes[current_index]) {
                            current_index++;
                            if(current_index>=weighter.Indexes.length){
                                break;
                            }
                        }
                        if(current_index<=weighter.Indexes.length-1) {
                            avgSpectrum[i] *= weighter.linValuesC[current_index - 1];
                        }else{
                            current_index = weighter.Indexes.length-1;
                            avgSpectrum[i] *= weighter.linValuesC[current_index];
                        }
                    }
                }
            }else if(WeightType==2){
                for(int i=0;i<n/2;i++){
                    if(i<weighter.Indexes[current_index]){
                        avgSpectrum[i]*=weighter.linValuesA[current_index-1];

                    }else{
                        while(i>=weighter.Indexes[current_index]) {
                            current_index++;
                            if(current_index>=weighter.Indexes.length){
                                break;
                            }
                        }
                        if(current_index<=weighter.Indexes.length-1) {
                            avgSpectrum[i] *= weighter.linValuesA[current_index - 1];
                        }else{
                            current_index = weighter.Indexes.length-1;
                            avgSpectrum[i] *= weighter.linValuesA[current_index];
                        }
                    }
                }
            }

            double[] linSpectrum = new double[avgSpectrum.length];
            for(int i=0;i<n/2;i++){
                linSpectrum[i] = (avgSpectrum[i]/amplitudeRef)*(avgSpectrum[i]/amplitudeRef);
            }

            double linearTotalLevel = 0.0;
            for(int i=0;i<n/2;i++) {
                linearTotalLevel += linSpectrum[i];
            }
            linearTotalLevel = linearTotalLevel / ((n*n)/(2*200000));

            double logTotalLevel = Converter.powLinearToLog(linearTotalLevel) + stateDif;

            if(logTotalLevel>MAX_VALUE){
                MAX_VALUE = logTotalLevel;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DecimalFormat df = new DecimalFormat("#.#");
                        textView12.setText("Max Value: " + df.format(MAX_VALUE) + " dB");
                    }
                });
            }

            if(logTotalLevel<MIN_VALUE){
                MIN_VALUE = logTotalLevel;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DecimalFormat df = new DecimalFormat("#.#");
                        textView13.setText("Min Value: " + df.format(MIN_VALUE) + " dB");
                    }
                });
            }

            DecimalFormat df = new DecimalFormat("#.#");
            Level = df.format(logTotalLevel) + " dB";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView11.setText(Level);
                }
            });
        }
    }

    private void initiateListener(){

        onSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = onSwitch.isChecked();
                if(isChecked){
                    switchOn();
                }else{
                    switchOff();
                }
            }
        });
    }

    public void radioClick(View view){
        int radioId = radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(radioId);
        String text = radioButton.getText().toString();
        if(text == "C - weight"){
            WeightType = 1;
        }else if (text == "A - weight"){
            WeightType = 2;
        }

    }

    private class weighting{

        private int n;
        public int[] Indexes;
        public double[] freqValues = {6.3,8,10,12.5,16,20,25,31.5,40,50,63,80,100,125,160,200,250,315,400,500,630,800,1000,1250,1600,2000,2500,3150,4000,5000,6300,8000,10000,12500,16000,20000};
        public double[] dBValuesC = {-21.3,-17.7,-14.3,-11.2,-8.5,-6.2,-4.4,-3.0,-2.0,-1.3,-0.8,-0.5,-0.3,-0.2,-0.1,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,-0.1,-0.2,-0.3,-0.5,-0.8,-1.3,-2.0,-3.0,-4.4,-6.2,-8.5,-11.2};
        public double[] linValuesC;
        public double[] dBValuesA = {-85.4,-77.8,-70.4,-63.4,-56.7,-50.5,-44.7,-39.4,-34.6,-30.2,-26.2,-22.5,-19.1,-16.1,-13.4,-10.9,-8.6,-6.6,-4.8,-3.2,-1.9,-0.8,0.0,0.6,1.0,1.2,1.3,1.2,1.0,0.5,-0.1,-1.1,-2.5,-4.3,-6.6,-9.3};
        public double[] linValuesA;

        weighting(int inputN){
            n = inputN;
            createIndexes();
            createLinCValues();
            createLinAValues();
        }

        private void createIndexes(){
            int freq = Global.RECORDER_SAMPLERATE;
            Indexes = new int[freqValues.length];
            for(int i=0;i<freqValues.length;i++){
                Indexes[i]= (int)(Math.round((n*freqValues[i])/(double)freq));
            }
        }

        private void createLinCValues(){
            linValuesC = new double[dBValuesC.length];
            for(int i=0;i<dBValuesC.length;i++){
                linValuesC[i] = Converter.ampLogToLinear(dBValuesC[i]);
            }
        }

        private void createLinAValues(){
            linValuesA = new double[dBValuesA.length];
            for(int i=0;i<dBValuesA.length;i++){
                linValuesA[i] = Converter.ampLogToLinear(dBValuesA[i]);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(onSwitch.isChecked()){
                switchOff();
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
