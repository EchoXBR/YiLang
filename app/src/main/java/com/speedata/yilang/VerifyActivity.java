package com.speedata.yilang;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.serialport.DeviceControl;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.dpfj.ImporterImpl;
import com.mylibrary.realize.TCS1GRealize;
import com.speedata.utils.PlaySoundPool;
import com.speedata.utils.ProgressDialogUtils;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * TODO 显示读卡信息  验证指纹
 */
public class VerifyActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG = "YANZHENG";
    private Button btnRead, btnEnrolment, btnClear;
    private CardManager cardManager = new CardManager();
    private ImageView imgPhoto;
    private TextView tvUserInfor;
    private byte[] fingerData = new byte[0];//指纹缓存数据
    private TCS1GRealize tcs1GRealize = null;
    private DeviceControl deviceControl;
    private DeviceControl deviceControl2;
    private Fmd fingerFmd1 = null;
    private Fmd fingerFmd2 = null;

    private Button btnComparison;
    private DialogShow dialogShow;
    private PlaySoundPool playSoundPool;
    private boolean isflag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

//        getActionBar().setTitle("Verify Info");
        btnEnrolment = (Button) findViewById(R.id.btn_Enrolment);
        btnEnrolment.setOnClickListener(this);
        btnRead = (Button) findViewById(R.id.btn_read_card);
        btnRead.setOnClickListener(this);
        Button button = (Button) findViewById(R.id.dakai);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            tcs1GRealize.openReader();
            }
        });
        btnClear = (Button) findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(this);
        imgPhoto = (ImageView) findViewById(R.id.img_photo);
        tvUserInfor = (TextView) findViewById(R.id.tv_user_infor);
        btnComparison = (Button) findViewById(R.id.btn_Comparisons);
        btnComparison.setOnClickListener(this);
        playSoundPool = PlaySoundPool.getPlaySoundPool(VerifyActivity.this);

        cardManager.initPsam(this);
        initFinger();
        dialogShow = new DialogShow(this);
        Log.i(TAG, "onCreate: ");

    }

    private void initFinger() {
        try {
            deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN_AND_EXPAND, 63,5,6);
//            deviceControl2 = new DeviceControl(DeviceControl.PowerType.MAIN, 93);
            deviceControl.PowerOnDevice();
//            deviceControl2.PowerOnDevice();

        } catch (IOException e) {
            e.printStackTrace();
        }
        tcs1GRealize = new TCS1GRealize(VerifyActivity.this, VerifyActivity.this, handler);
//        if (tcs1GRealize != null) {
//            Log.i(TAG, "initFinger: openreader");
////            tcs1GRealize.openReader();//打开指纹寻找reader
//        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (tcs1GRealize != null) {
            SystemClock.sleep(1000);
            Log.i(TAG, "initFinger: openreader");
            tcs1GRealize.openReader();//打开指纹寻找reader
        }
        fingerFmd1 = null;
        fingerFmd2 = null;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Toast.makeText(VerifyActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    isflag = (boolean) msg.obj;
                    if (isflag) {
                        Log.i(TAG, "finger1:"+isflag);
                        Toast.makeText(VerifyActivity.this, "Init Success", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "finger2:"+isflag);
//                        tcs1GRealize.openReader();
//                        Toast.makeText(VerifyActivity.this, "指纹初始失败，重新初始中", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 5:
                    fingerFmd2 = (Fmd) msg.obj;
                    if (fingerFmd2 != null) {
                        tcs1GRealize.comparisonFinger(fingerFmd1, fingerFmd2);
                    }
                    break;
                case 6:
                    int mScore = (Integer) msg.obj;
                    DecimalFormat formatting = new DecimalFormat("##.######");
                    String comparison = "Dissimilarity Score: " + String.valueOf(mScore) + ", False match rate: "
                            + Double.valueOf(formatting.format((double) mScore / 0x7FFFFFFF)) +
                            " (" + (mScore < (0x7FFFFFFF / 100000) ? "match" : "no match") + ")";
                    Log.i("result", comparison);
                    if (mScore < (0x7FFFFFFF / 100000)) {
//                        dialog("通过验证");

                        playSoundPool.playLaser();
                        dialogShow.DialogShow(R.drawable.pass);
                    } else {

                        dialogShow.DialogShow(R.drawable.fail);
                        playSoundPool.playError();
//                        dialog("验证失败");
                    }
                    break;
            }
        }
    };

    protected void dialog(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(VerifyActivity.this);
//        builder.setMessage(s);
        builder.setIcon(R.drawable.pass);
        builder.setTitle("验证结果");
        builder.setPositiveButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onClick(View v) {
        if (v == btnRead) {
            //TODO 读卡
            ProgressDialogUtils.showProgressDialog(VerifyActivity.this, getString(R.string.verify_reading_card));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap = CardParse.PaseBitmap(cardManager.getPhotoData());
                    final UserInfor userInfor = CardParse.PaseUserInfor(cardManager.getUserInfor());
                    //注册的哪个文件就读哪个文件
                    fingerData = cardManager.getFingerData((byte) 0x07);
                    if (fingerData.length != 0) {
                        //转回指纹FMD特征
                        ImporterImpl importer = new ImporterImpl();
                        try {
                            fingerFmd1 = importer.ImportFmd(fingerData, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
                        } catch (UareUException e) {
                            e.printStackTrace();
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ProgressDialogUtils.dismissProgressDialog();
                            imgPhoto.setImageBitmap(bitmap);
                            tvUserInfor.setText(userInfor.getName());
                            if (fingerData.length == 0 && bitmap == null && userInfor == null) {
                                btnEnrolment.setVisibility(View.VISIBLE);
                                ProgressDialogUtils.dismissProgressDialog();
                            }
                        }
                    });

                }
            }).start();

        } else if (v == btnComparison) {
//            if (isflag) {

            tcs1GRealize.createTemplate();
//            } else {
//                tcs1GRealize.openReader();
//            }

        } else if (v == btnEnrolment) {
            Intent intent = new Intent(VerifyActivity.this, RegisterActivity.class);
            startActivity(intent);
        } else if (v == btnClear) {
            ProgressDialogUtils.showProgressDialog(VerifyActivity.this, getString(R.string.cleraing_card));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final boolean b1 = cardManager.updateUserInfor(new byte[64]);
                    final boolean b2 = cardManager.updatePhotoInfor(new byte[2044]);
                    final boolean b3 = cardManager.updateFinger(new byte[1022], (byte) 0x07);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (b1 && b2 && b3) {
                                Toast.makeText(VerifyActivity.this, getString(R.string.clera_card), Toast.LENGTH_SHORT).show();
                                fingerFmd1 = null;
                                fingerFmd2 = null;
                            }
                            ProgressDialogUtils.dismissProgressDialog();
//                            finish();
                        }
                    });

                }
            }).start();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cardManager.realsePsam();
        if (tcs1GRealize != null) {
            tcs1GRealize.closeReader();
        }
        System.out.print("onDestroy");
        Log.i(TAG, "onDestroy: ");
        try {
            deviceControl.PowerOffDevice();
//            deviceControl2.PowerOffDevice();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
