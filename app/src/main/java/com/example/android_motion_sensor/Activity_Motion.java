package com.example.android_motion_sensor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Activity_Motion extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private Button motionBackBtn;
    private Button motionDetectorBtn;
    private TextView motionInfoView;

    private SensorManager sensorManager;
    private Sensor accSensor;
    final float movementThreshold = 2.5f;  // 阈值
    boolean isMoving = false;
    float[] prevValues = {1.0f, 1.0f, 1.0f};
    float[] currValues = new float[3];

    // True means it allows the user to click the button and begin detecting motion
    // False means the motion sensor is detecting.
    private Boolean motionIsBeingDetected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion);

        // Binding all elements
        motionBackBtn = (Button) findViewById(R.id.motion_back);
        motionBackBtn.setOnClickListener(this);

        motionDetectorBtn = (Button) findViewById(R.id.motion_sensor);
        motionDetectorBtn.setBackground(getResources().getDrawable(R.drawable.btn_sleep));
        motionDetectorBtn.setOnClickListener(this);

        motionInfoView = findViewById(R.id.motion_info);
        motionInfoView.setBackground(getResources().getDrawable(R.drawable.view_static));
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.motion_back:
                intent = new Intent(this, Activity_Main.class);
                break;
            case R.id.motion_sensor:
                if(motionIsBeingDetected) {
                    // Changing the btn color
                    btnAwake(getResources().getString(R.string.motion_sensor_open));

                    // Instantiating a sensor manager and an accelerometer sensor
                    sensorInstantiate();

                } else {
                    // Changing the btn color
                    btnSleep(getResources().getString(R.string.motion_sensor_close));

                    isStatic();

                    // Disposing the sensor manager and the accelerometer sensor
                    sensorDispose();
                }
                break;
            default:
                intent = null;
                break;
        }
        if(intent != null) {
            if(!motionIsBeingDetected) {
                // Changing the btn color
                btnSleep(getResources().getString(R.string.motion_sensor_close));

                isStatic();
            }

            sensorDispose();

            startActivity(intent);
            this.finish();
            overridePendingTransition(0,0);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null || event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) {
            btnSleep(getResources().getString(R.string.motion_sensor_close));
            return;
        }

        if (event.sensor == accSensor) {
            // Comparing current values with previous values
            System.arraycopy(event.values, 0, currValues, 0, event.values.length);
            if ((Math.abs(currValues[0] - prevValues[0]) > movementThreshold) ||
                (Math.abs(currValues[1] - prevValues[1]) > movementThreshold) ||
                (Math.abs(currValues[2] - prevValues[2]) > movementThreshold)) {
                isDynamic();
            } else {
                isStatic();
            }

            // Storing current values
            System.arraycopy(currValues, 0, prevValues, 0, currValues.length);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        ;
    }

    private void sensorInstantiate() {
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);// TYPE_GRAVITY
        sensorManager.registerListener((SensorEventListener) this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void sensorDispose() {
        if (sensorManager != null && accSensor != null) {
            sensorManager.unregisterListener(this, accSensor);
        }
    }

    private void btnSleep(String msg) {
        motionIsBeingDetected = true;
        motionDetectorBtn.setBackground(getResources().getDrawable(R.drawable.btn_sleep));
        motionDetectorBtn.setText(R.string.motion_btn_off);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void btnAwake(String msg) {
        motionIsBeingDetected = false;
        motionDetectorBtn.setBackground(getResources().getDrawable(R.drawable.btn_awake));
        motionDetectorBtn.setText(R.string.motion_btn_on);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void isStatic() {
        isMoving = false;
        motionInfoView.setText(R.string.static_state);
        motionInfoView.setBackground(getResources().getDrawable(R.drawable.view_static));
    }

    private void isDynamic() {
        isMoving = true;
        motionInfoView.setText(R.string.dynamic_state);
        motionInfoView.setBackground(getResources().getDrawable(R.drawable.view_dynamic));
    }

}