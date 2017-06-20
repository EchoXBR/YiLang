package com.speedata.yilang;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by echo on 2017/5/17.
 */

public class FileUtils {


    public static void writeFileToSD(byte[] data,String fileName) {
        String sdStatus = Environment.getExternalStorageState();
        if(!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            Log.d("TestFile", "SD card is not avaiable/writeable right now.");
            return;
        }
        try {
            String pathName="/sdcard/test/";
//            String fileName=fileName;
            File path = new File(pathName);
            File file = new File(pathName + fileName+".txt");
            if( !path.exists()) {
                Log.d("TestFile", "Create the path:" + pathName);
                path.mkdir();
            }
            if( !file.exists()) {
                Log.d("TestFile", "Create the file:" + fileName);
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(file);
//            String s = "this is a test string writing to file.";
//            byte[] buf = s.getBytes();
            stream.write(data);
            stream.close();

        } catch(Exception e) {
            Log.e("TestFile", "Error on writeFilToSD.");
            e.printStackTrace();
        }
    }

    public static void writeFileToSD(String data,String fileName) {
        String sdStatus = Environment.getExternalStorageState();
        if(!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            Log.d("TestFile", "SD card is not avaiable/writeable right now.");
            return;
        }
        try {
            String pathName="/sdcard/test/";
//            String fileName=fileName;
            File path = new File(pathName);
            File file = new File(pathName + fileName+".txt");
            if( !path.exists()) {
                Log.d("TestFile", "Create the path:" + pathName);
                path.mkdir();
            }
            if( !file.exists()) {
                Log.d("TestFile", "Create the file:" + fileName);
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(file);
//            String s = "this is a test string writing to file.";
//            byte[] buf = s.getBytes();
            stream.write(data.getBytes());
            stream.close();

        } catch(Exception e) {
            Log.e("TestFile", "Error on writeFilToSD.");
            e.printStackTrace();
        }
    }
}
