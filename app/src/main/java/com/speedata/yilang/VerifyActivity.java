package com.speedata.yilang;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedata.utils.ProgressDialogUtils;

/**
 * TODO 显示读卡信息  验证指纹
 */
public class VerifyActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnRead;
    private CardManager cardManager = new CardManager();
    private ImageView imgPhoto;
    private TextView tvUserInfor;
    private byte[] fingerData;//指纹缓存数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        btnRead = (Button) findViewById(R.id.btn_read_card);
        btnRead.setOnClickListener(this);
        cardManager.initPsam(this);
        imgPhoto = (ImageView) findViewById(R.id.img_photo);
        tvUserInfor = (TextView) findViewById(R.id.tv_user_infor);
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

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cardManager.realsePsam();
    }
}
