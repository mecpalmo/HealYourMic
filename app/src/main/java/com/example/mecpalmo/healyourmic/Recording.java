package com.example.mecpalmo.healyourmic;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class Recording extends AppCompatActivity {

    Button recBut, stopBut;
    AudioRecorder recorder;
    TextView textView9, textView7;
    private Timer myTimer;
    private int currentTime;
    String timeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        recBut = (Button)findViewById(R.id.rec2But);
        stopBut = (Button)findViewById(R.id.stop2But);
        textView9 = (TextView)findViewById(R.id.textView9);
        textView7 = (TextView)findViewById(R.id.textView7);

        recorder = new AudioRecorder();
        stopBut.setEnabled(false);
        currentTime = 0;
    }

    public void rec2Click(View view){
        if(!Global.recording) {
            recBut.setEnabled(false);
            recorder.createFilePath(Global.returnOwnRecordFileName());
            recorder.startRecording();
            stopBut.setEnabled(true);
            textView9.setText("Recording");
            currentTime = 0;
            startTimeCounting();
        }else{
            textView9.setText("Unable to record");
        }
    }

    public void stop2Click(View view){
        stopTheRec();
    }

    private void stopTheRec(){
        recorder.stopRecording();
        stopBut.setEnabled(false);
        recBut.setEnabled(true);
        textView9.setText("Free to Record");
        myTimer.cancel();
    }

    private void countingTime(){
        int sec = currentTime%60;
        int min = currentTime/60;
        if(sec>=10) {
            timeText = min + ":" + sec;
        }else{
            timeText = min + ":0" + sec;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView7.setText(timeText);
            }
        });
        currentTime++;
    }

    private void startTimeCounting() {
        myTimer = new Timer();
        myTimer.scheduleAtFixedRate(new ScheduleTask(), 0, 1000); //okres wynosi 1000 ms czyli 1s
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
