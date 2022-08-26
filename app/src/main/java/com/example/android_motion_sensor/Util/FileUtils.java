package com.example.android_motion_sensor.Util;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Paths;

public class FileUtils {
    private Context context;
    private FileUtils fileUtils;
    private String filePath;
    private String FILE_NAME = "motion_data.csv";
    private File file;

    public FileUtils(Context context){
        this.context = context;
        filePath = context.getFilesDir().getAbsolutePath().toString() + File.separatorChar + FILE_NAME;
        file = new File(filePath);
//        if (!file.exists()) {
//            file.mkdir();
//        }
    }

    public void write2csv(String data) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath,true));
            out.write(data);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
