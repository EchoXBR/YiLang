package com.speedata.yilang;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.speedata.libutils.DataConversionUtils;
import com.speedata.libutils.MyLogger;
import com.speedata.utils.ProgressDialogUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnTest;
    private TextView tvTimes;
    private ImageView imageView;
    private Button btnVer, btnRegister;


    CardManager cardManager = new CardManager();
    private MyLogger logger = MyLogger.jLog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTest = (Button) findViewById(R.id.btn_start_test);
        btnVer = (Button) findViewById(R.id.btn_ver);
        btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(this);
        btnVer.setOnClickListener(this);
        tvTimes = (TextView) findViewById(R.id.tv_time);
        imageView = (ImageView) findViewById(R.id.img_test);

        if (!cardManager.initPsam(this)) {
            Toast.makeText(this, "初始化失败", Toast.LENGTH_SHORT).show();
            tvTimes.setText("初始化失败");
            return;
        }

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialogUtils.showProgressDialog(MainActivity.this, "请稍后");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //用户信息写入读取测试
                        testUserData();

                        // 测试更新07指纹
                        byte[] finger1 = new byte[1022];
                        finger1[0] = 0x11;
                        finger1[198] = 0x22;
                        finger1[398] = 0x33;
                        finger1[598] = 0x44;
                        finger1[798] = 0x55;
                        finger1[998] = 0x66;
                        finger1[1021] = 0x77;
                        cardManager.updateFinger(finger1, (byte) 0x07);
                        //测试读取07指纹
                        byte[] finger_data = cardManager.getFingerData((byte) 0x07);
                        if (!cardManager.isNULL(finger_data))
                            logger.d("finger_data=" + DataConversionUtils.byteArrayToStringLog(finger_data, finger_data.length));
                        //更新相片测试
                        final byte[] data = getPicBytes();
                        cardManager.updatePhotoInfor(data);
                        //读取相片测试
                        final byte[] photoData = cardManager.getPhotoData();
                        if (!cardManager.isNULL(photoData))
                            logger.d("photoData=" + DataConversionUtils.byteArrayToStringLog(photoData, photoData.length));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //显示照片
                                imageView.setImageBitmap(Bytes2Bimap(photoData));
                                ProgressDialogUtils.dismissProgressDialog();

                            }
                        });

//                        int len = name_byte.length / 200;
//                        for (int i = 0; i < len; i++) {
//                            byte[] tt = new byte[200];
//                            System.arraycopy(name_byte, 200 * i, tt, 0, 200);
//                            logger.d("getPhotoDatalen = " + DataConversionUtils.byteArrayToStringLog(tt, tt.length));
//                        }
//                        byte[] ttt = new byte[137];
//                        System.arraycopy(name_byte, 200 * 9, ttt, 0, 137);
//                        logger.d("getPhotoDatalen = " + DataConversionUtils.byteArrayToStringLog(ttt, ttt.length));
//
//                        int len_ = photoData.length / 200;
//                        for (int i = 0; i < len_; i++) {
//                            byte[] tt = new byte[200];
//                            System.arraycopy(photoData, 200 * i, tt, 0, 200);
//                            logger.d("getPhotoDataleA = " + DataConversionUtils.byteArrayToStringLog(tt, tt.length));
//                        }
//                        byte[] t = new byte[137];
//                        System.arraycopy(photoData, 200 * 9, t, 0, 137);
//                        logger.d("getPhotoDatalenlast = " + DataConversionUtils.byteArrayToStringLog(t, t.length));
//                        FileUtils.writeFileToSD(DataConversionUtils.byteArrayToString(name_byte), "or_data");
//                        FileUtils.writeFileToSD(DataConversionUtils.byteArrayToString(photoData), "read_data");
                    }
                }).start();
            }
        });
//        btnTest.callOnClick();
    }

    private void testUserData() {
        //测试写入用户信息
        byte[] temp = new byte[64];
        cardManager.updateUserInfor(temp);
        String name = "许百茹";
        byte[] name_byte = JSEscape.getNameByte(name);
//                            logger.d("name name_byte=" + DataConversionUtils.byteArrayToStringLog(name_byte, name_byte.length));
        if (name_byte.length < 64) {
            System.arraycopy(name_byte, 0, temp, 0, name_byte.length);
            for (int i = name_byte.length; i < 64; i++) {
                //不够64 ff补全
                temp[i] = (byte) 0xff;
            }
            boolean up = cardManager.updateUserInfor(temp);
            logger.d("userdata updata=" + up);
        } else if (name_byte.length == 64)//最大64byte
            cardManager.updateUserInfor(name_byte);

        //测试读取用户信息
        final byte[] user_data = cardManager.getUserInfor();
        if (!cardManager.isNULL(user_data)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTimes.setText("NAME:" + ParseIdinfor.stringFilter(JSEscape.parseName(user_data)));
                }
            });

        }
    }

    /**
     * 替换字符串空白部分为空格
     *
     * @param code
     * @return
     */
    public static String cutCode(String code) {
        String result = code;
        String temporary;
        for (int i = 0; i < code.length() / 4; i++) {
            temporary = code.substring(i * 4, (i + 1) * 4).replace("FFFF",
                    "2020").replace("20FF", "2020").replace("FF20", "2020")
                    .replace("F20F", "2020").replace("\uF8F5\uF8F5\uF8F5\uF8F5", "2020");
            result = result.substring(0, i * 4) + temporary
                    + result.substring((i + 1) * 4);
        }
        return result;

    }

    private byte[] getPicBytes() {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.photo_1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        // 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        while (baos.toByteArray().length > 2048) {
            baos.reset();// 重置baos即清空baos
            bmp.compress(Bitmap.CompressFormat.JPEG, 20, baos);// 这里压缩50%，把压缩后的数据存放到baos中
            System.out.println("compressBitmap len="+baos.toByteArray().length);

        }
        final byte[] data = baos.toByteArray();
        logger.d("pic len=" + data.length);
        return data;
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

    Bitmap Bytes2Bimap(byte[] b) {
        if (b != null && b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnRegister) {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        } else if (v == btnVer) {
            Intent intent = new Intent(MainActivity.this, VerifyActivity.class);
            startActivity(intent);
        }
    }
}
