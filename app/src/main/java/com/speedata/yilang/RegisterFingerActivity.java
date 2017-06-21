package com.speedata.yilang;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.serialport.DeviceControl;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalpersona.uareu.Fmd;
import com.mylibrary.realize.TCS1GRealize;

import java.io.IOException;

/**
 * TODO 指纹注册
 */
public class RegisterFingerActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnEnrollment, btnOpen;
    private TextView tvMsg;
    private ImageView imageView;
    private TCS1GRealize tcs1GRealize = null;
    private byte[] fingerTemplebytes;
    private CardManager cardManager = null;
    private DeviceControl deviceControl;
    private DeviceControl deviceControl2;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_finger);
//        getActionBar().setTitle("Enrollment FingerPrint");
        try {
            deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN, 63);
            deviceControl2 = new DeviceControl(DeviceControl.PowerType.MAIN, 93);
            deviceControl.PowerOnDevice();
            deviceControl2.PowerOnDevice();
//            SystemClock.sleep(200);
        } catch (IOException e) {
            e.printStackTrace();
        }
        tcs1GRealize = new TCS1GRealize(RegisterFingerActivity.this, RegisterFingerActivity.this, handler);
        if (tcs1GRealize != null) {
            tcs1GRealize.openReader();
        }
        Log.i(TAG, "openReader");
        cardManager = new CardManager();
        cardManager.initPsam(this);
        initGui();
    }

    String TAG = "finger";

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");

    }

    private void initGui() {
        btnEnrollment = (Button) findViewById(R.id.btn_zhuce);
        btnOpen = (Button) findViewById(R.id.btn_open);
        imageView = (ImageView) findViewById(R.id.image);
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        btnEnrollment.setOnClickListener(this);
        btnOpen.setOnClickListener(this);

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    tvMsg.setText((String) msg.obj);
                    break;
                case 1:
                    if ((Boolean) msg.obj) {
                        tvMsg.setText("Init success ");
                    } else {
//                        tvMsg.setText("指纹初始失败，重新初始中");
                        tcs1GRealize.openReader();
                        Log.i(TAG, "@@@@@  openReader");
                    }
                    break;
                case 3:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        tvMsg.setText(getString(R.string.get_image_fail));
                    }
                    break;
                case 5:
                    Fmd fmd1 = (Fmd) msg.obj;
                    fingerTemplebytes = fmd1.getData();//获取指纹特征函数
                    cardManager.updateFinger(fingerTemplebytes, (byte) 0x07);//更新指纹特征函数到卡片
//                    Intent intent = new Intent(RegisterFingerActivity.this, VerifyActivity.class);
//                    startActivity(intent);
                    finish();
                    tvMsg.setText("Enrollment Success");
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v == btnEnrollment) {
            tcs1GRealize.createTemplate();
        } else if (v == btnOpen) {
            tcs1GRealize.openReader();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy: ");
        if (tcs1GRealize != null) {
            tcs1GRealize.closeReader();
        }
        try {
            deviceControl.PowerOffDevice();
            deviceControl2.PowerOffDevice();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
