package com.example.mecpalmo.healyourmic;

import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LoadCorrectionFile extends AppCompatActivity {

    ListView listView;
    Button loadBut;
    String FileName = "";
    TextView textView18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_correction_file);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        listView = (ListView)findViewById(R.id.listView3);
        loadBut = (Button)findViewById(R.id.load2But);
        textView18 = (TextView)findViewById(R.id.textView18);

        List<String> fileList = new ArrayList<String>();
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,Global.AUDIO_RECORDER_FOLDER);
        File[] fs = file.listFiles();
        fileList.clear();
        for(File f: fs){
            if((f.getName().indexOf("Points")!=-1 || f.getName().indexOf("Bands")!=-1) && f.getName().indexOf(".txt") !=-1) {
                fileList.add(f.getName());
            }
        }
        ArrayAdapter<String> fileNameList = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,fileList);
        listView.setAdapter(fileNameList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileName = String.valueOf(parent.getItemAtPosition(position));
                textView18.setText("Selected: " + FileName);
            }
        });
    }

    public void loadCorFile(View view){
        if(FileName != ""){
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Global.AUDIO_RECORDER_FOLDER;
            File file = new File(path+"/"+FileName);
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

            double[] newCorrection = new double[array.length];
            for(int j=0;j<array.length;j++){
                newCorrection[j] = Double.parseDouble(array[j]);
            }

            int n = newCorrection.length;
            if(n==1024 || n==2048 || n==4096){
                if(FileName.indexOf("Points")!=-1){
                    Global.spectrumCorrection = newCorrection;
                }
                if(FileName.indexOf("Bands")!=-1){
                    Global.spectrumCorrection2 = newCorrection;
                }
                Toast.makeText(this,"Loaded",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Error: Not Proper File",Toast.LENGTH_LONG).show();
            }
        }
    }
}
