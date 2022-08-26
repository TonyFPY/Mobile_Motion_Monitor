package com.example.android_motion_sensor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_motion_sensor.Util.FileUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Activity_Data_Collection extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
    private Context context;

    private Button DataBackBtn;
    private Button DataCollectionBtn;
    private TextView DataLogView;

    private Spinner stateSpinner;
    private String[] stateItems;
    private ArrayAdapter<String> stateAdapter;

    private Spinner sampleSizeSpinner;
    private String[] sampleSizeItems;
    private ArrayAdapter<String> sampleSizeAdapter;

    private SensorManager sensorManager;
    private Sensor accSensor;
    float[] prevValues = {0.0f, 0.0f, 0.0f};
    float[] currValues = new float[3];
    private float[] motionData = new float[4];
    private String motionDataLog = "";
    private FileUtils fileUtils;

    // True means it allows the user to click the button and begin collect data
    // False means the data collection is finished.
    private Boolean allowsCollecting = true;

    private int SAMPLE_RATE = 3;
    //        SensorManager.SENSOR_DELAY_FASTEST 0
    //        SensorManager.SENSOR_DELAY_GAME    1
    //        SensorManager.SENSOR_DELAY_UI      2
    //        SensorManager.SENSOR_DELAY_NORMAL  3
    private int MAX_NUM_OF_SAMPLEs = 100;
    private String motionLabel = "1";
    private int NUM_OF_SAMPLES = 10;
    private int curNumOfSamples = NUM_OF_SAMPLES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);
        context = this;
        fileUtils = new FileUtils(this);

        // Binding all elements
        DataBackBtn = (Button) findViewById(R.id.data_back);
        DataBackBtn.setOnClickListener(this);

        DataCollectionBtn = (Button) findViewById(R.id.data_collection);
        DataCollectionBtn.setBackground(getResources().getDrawable(R.drawable.btn_sleep));
        DataCollectionBtn.setOnClickListener(this);

        DataLogView = findViewById(R.id.data_log);

        stateSpinner = findViewById(R.id.motion_states_spinner);
        stateItems = getResources().getStringArray(R.array.motionState);
        stateAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, stateItems);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(stateAdapter);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                motionLabel = adapterView.getItemAtPosition(i).toString().split("\\s+")[0];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {;}
        });

        sampleSizeSpinner = findViewById(R.id.num_of_samples_spinner);
        sampleSizeItems = getResources().getStringArray(R.array.numOfSamples);
        sampleSizeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, sampleSizeItems);
        sampleSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sampleSizeSpinner.setAdapter(sampleSizeAdapter);
        sampleSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                NUM_OF_SAMPLES = Integer.valueOf(adapterView.getItemAtPosition(i).toString()) + 1;
                // + 1 的原因是初始数据有偏差，不记录这组数据，从下一组数据开始记录
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {;}
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.data_back:
                intent = new Intent(this, Activity_Main.class);
                break;
            case R.id.data_collection:
                if(allowsCollecting) {
                    curNumOfSamples = NUM_OF_SAMPLES;
                    DataLogView.setText("");
                    motionDataLog = "";
                    Arrays.fill(prevValues,1.0f);

                    // Changing the btn color
                    btnAwake(getResources().getString(R.string.start_collecting_data));

                    // Instantiating a sensor manager and an accelerometer sensor
                    sensorInstantiate(SAMPLE_RATE);
                } else {
                    // Changing the btn color
                    btnSleep(getResources().getString(R.string.stop_collecting_data));

                    // Disposing the sensor manager and the accelerometer sensor
                    sensorDispose();

                    // write the data into csv file
                    fileUtils.write2csv(motionDataLog);
                }
                break;
            default:
                intent = null;
                break;
        }
        if(intent != null) {
            if(!allowsCollecting) {
                // write the data into csv file
                fileUtils.write2csv(motionDataLog);

                // Changing the btn color
                btnSleep(getResources().getString(R.string.stop_collecting_data));
//                Toast.makeText(this, "Data hasn't been recorded!", Toast.LENGTH_SHORT).show();
            }

            curNumOfSamples = NUM_OF_SAMPLES;
            DataLogView.setText("");
            motionDataLog = "";
            Arrays.fill(prevValues,0.0f);

            // Releasing the sensor activity
            sensorDispose();

            startActivity(intent);
            this.finish();
            overridePendingTransition(0,0);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (curNumOfSamples == 0 || event.sensor == null || event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) { //_UNCALIBRATED
            // write the data into csv file
            fileUtils.write2csv(motionDataLog);

            curNumOfSamples = NUM_OF_SAMPLES;
            btnSleep(getResources().getString(R.string.stop_collecting_data));
            sensorDispose();
            return;
        } else if (curNumOfSamples == NUM_OF_SAMPLES) { // 不记录第一组数据
            System.arraycopy(event.values, 0, prevValues, 0, event.values.length);
            curNumOfSamples--;
        } else {
            // Comparing current values with previous values
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    System.arraycopy(event.values, 0, currValues, 0, event.values.length);
                    for(int i = 0; i < motionData.length - 1; i++){
                        motionData[i] = Math.abs(currValues[i] - prevValues[i]);
                        motionDataLog += decimalFormat.format(motionData[i]) + ',';
                    }
                    motionData[motionData.length - 1] = Integer.valueOf(motionLabel);

                    SimpleDateFormat time = new SimpleDateFormat("yy-MM-dd-HH:mm"); //yy-MM-dd-HH:mm:ss
                    Date date = new Date();
                    motionDataLog += Float.toString(motionData[motionData.length - 1])
                                     + ',' + time.format(date) + '\n';

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String startSepLine = "----------- START ----------\n";
                            String endSepLine   = "------------ END -----------";
                            DataLogView.setText(startSepLine + motionDataLog + endSepLine);
                        }
                    });

                    // Storing current values
                    System.arraycopy(currValues, 0, prevValues, 0, currValues.length);

                    curNumOfSamples--;
                }
            }).start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void sensorInstantiate(int sampleRate) {
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);// TYPE_ACCELEROMETER_UNCALIBRATED
        sensorManager.registerListener((SensorEventListener) this, accSensor, sampleRate);
    }

    private void sensorDispose() {
        if (sensorManager != null && accSensor != null) {
            sensorManager.unregisterListener(this, accSensor);
        }
    }
    
    private void btnSleep(String msg){
        allowsCollecting = true;
        DataCollectionBtn.setBackground(getResources().getDrawable(R.drawable.btn_sleep));
        DataCollectionBtn.setText(R.string.data_collection_btn_off);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void btnAwake(String msg) {
        allowsCollecting = false;
        DataCollectionBtn.setBackground(getResources().getDrawable(R.drawable.btn_awake));
        DataCollectionBtn.setText(R.string.data_collection_btn_on);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}