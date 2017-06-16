package com.speedata.yilang;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.speedata.libutils.DataConversionUtils;
import com.speedata.libutils.MyLogger;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button btnTest;
    private TextView tvTimes;


    CardManager cardManager = new CardManager();
    private MyLogger logger = MyLogger.jLog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTest = (Button) findViewById(R.id.btn_start_test);
        tvTimes = (TextView) findViewById(R.id.tv_time);
        if (!cardManager.initPsam(this)) {
            Toast.makeText(this, "初始化失败", Toast.LENGTH_SHORT).show();
            tvTimes.setText("初始化失败");
            return;
        }
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        // 测试读持卡人信息
                        byte[] data = cardManager.getPhotoData();
                        cardManager.getUserInfor();
                        if (!cardManager.isNULL(data)) {
                            logger.d(DataConversionUtils.byteArrayToStringLog(data, data.length));
                        }
                    }
                }).start();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cardManager != null) {
            try {
                cardManager.psam.releaseDev();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
