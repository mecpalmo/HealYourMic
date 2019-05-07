package com.example.mecpalmo.healyourmic;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Help extends AppCompatActivity {

    TextView textView21;

    String helpText = "Hello my dear user :)\n\n" +
            "Here you can learn about this app's functions\n" +
            "1. First you need to calibrate your mic.\n" +
            "To do that, you'll need to record chosen noise in calibrate section. " +
            "Then you load it and the program use it to make your mic work better\n" +
            "2. When your mic is repaired you can use Sound Level Meter (SLM) it also has " +
            "to be calibrated, but you just need to be compared with proffesional equipment once. " +
            "You can adjust state correction for SLM and use it later anytime.\n" +
            "3. Except SLM you can also create your own recordings and later repair them. " +
            "Reparation is about amplifying sound level on missing frequencies.\n" +
            "4. You can save a calibration result as a correction and later load it from file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textView21 = (TextView)findViewById(R.id.textView21);
        textView21.setText(helpText);
    }
}
