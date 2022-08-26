package com.example.android_motion_sensor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/*
    Created on Nov.12th, 2021
    Authored by Pinyuan Feng
    Reference: https://riptutorial.com/android/example/16602/decide-if-your-device-is-static-or-not--using-the-accelerometer
 */
public class Activity_Main extends AppCompatActivity implements View.OnClickListener {
    private Button motionBtn;
    private Button infoBtn;
    private Button motionDataBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        motionBtn = (Button) findViewById(R.id.motion_detector);
        motionDataBtn = (Button) findViewById(R.id.sensor_data);
        infoBtn = (Button) findViewById(R.id.sensor_info);
        motionBtn.setOnClickListener(this);
        motionDataBtn.setOnClickListener(this);
        infoBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.motion_detector:
                intent = new Intent(this, Activity_Motion.class);
                break;
            case R.id.sensor_data:
                 intent = new Intent(this, Activity_Data_Collection.class);
                break;
            case R.id.sensor_info:
                intent = new Intent(this, Activity_Info.class);
                break;
            default:
                intent = null;
                break;
        }
        if(intent != null) {
            startActivity(intent);
            this.finish();
            overridePendingTransition(0,0);
        }
    }
}