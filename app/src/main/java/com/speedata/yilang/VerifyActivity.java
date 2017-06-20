package com.speedata.yilang;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.dpfj.ImporterImpl;
import com.mylibrary.realize.TCS1GRealize;
import com.speedata.utils.ProgressDialogUtils;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * TODO 显示读卡信息  验证指纹
 */
public class VerifyActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnRead, btnComparison;
    private CardManager cardManager = new CardManager();
    private ImageView imgPhoto;
    private TextView tvUserInfor;
    private byte[] fingerData;//指纹缓存数据
    private TCS1GRealize tcs1GRealize = null;
    private DeviceControl deviceControl;
    private DeviceControl deviceControl2;
    private Fmd fingerFmd1, fingerFmd2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        btnRead = (Button) findViewById(R.id.btn_read_card);
        btnRead.setOnClickListener(this);
        cardManager.initPsam(this);
        imgPhoto = (ImageView) findViewById(R.id.img_photo);
        tvUserInfor = (TextView) findViewById(R.id.tv_user_infor);
        btnComparison = (Button) findViewById(R.id.btn_Comparisons);
        btnComparison.setOnClickListener(this);
        try {
            deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN, 63);
            deviceControl2 = new DeviceControl(DeviceControl.PowerType.MAIN, 93);
            deviceControl.PowerOnDevice();
            deviceControl2.PowerOnDevice();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tcs1GRealize = new TCS1GRealize(VerifyActivity.this, VerifyActivity.this, handler);

        tcs1GRealize.openReader();//打开指纹寻找reader


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
                    if ((Boolean) msg.obj) {
                        Toast.makeText(VerifyActivity.this, "指纹初始成功", Toast.LENGTH_SHORT).show();
                    } else {
                        tcs1GRealize.openReader();
                        Toast.makeText(VerifyActivity.this, "指纹初始失败，重新初始中", Toast.LENGTH_SHORT).show();
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
                        dialog("通过验证");
                    } else {
                        dialog("验证失败");
                    }
                    break;
            }
        }
    };

    protected void dialog(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(VerifyActivity.this);
        builder.setMessage(s);
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
            ProgressDialogUtils.showProgressDialog(VerifyActivity.this, "正在读卡");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap = CardParse.PaseBitmap(cardManager.getPhotoData());
                    final UserInfor userInfor = CardParse.PaseUserInfor(cardManager.getUserInfor());
                    //注册的哪个文件就读哪个文件
                    fingerData = cardManager.getFingerData((byte) 0x07);
                    if (fingerData != null) {
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
                        }
                    });

                }
            }).start();

        } else if (v == btnComparison) {
            tcs1GRealize.createTemplate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cardManager.realsePsam();
        tcs1GRealize.closeReader();
    }
}
