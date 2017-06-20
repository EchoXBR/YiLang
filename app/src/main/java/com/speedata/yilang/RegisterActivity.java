package com.speedata.yilang;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.speedata.utils.ProgressDialogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int TAKE_PHOTO = 1;
    private static final int CROP_PHOTO = 2;
    private static final int CHOOSE_PHOTO = 3;
    private EditText edvName;
    private ImageButton imageButtonGetPhoto;
    private ImageView imgPhoto;
    private Button btnRegister;

    String Sex = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        edvName = (EditText) findViewById(R.id.edv_name);
        btnRegister = (Button) findViewById(R.id.btn_register);
        imageButtonGetPhoto = (ImageButton) findViewById(R.id.imbtn_get_photo);
        imgPhoto = (ImageView) findViewById(R.id.img_photo);
        btnRegister.setOnClickListener(this);
        imageButtonGetPhoto.setOnClickListener(this);
        //根据ID找到RadioGroup实例
        RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
        //绑定一个匿名监听器
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                //获取变更后的选中项的ID
                int radioButtonId = arg0.getCheckedRadioButtonId();
                //根据ID获取RadioButton的实例
                RadioButton rb = (RadioButton) RegisterActivity.this.findViewById(radioButtonId);
                //更新文本内容，以符合选中项
//                                 tv.setText("您的性别是：" + rb.getText());
                Sex = rb.getText().toString();
            }
        });
        RadioGroup selectPhoto = (RadioGroup) this.findViewById(R.id.radio_photo);
        //绑定一个匿名监听器
        selectPhoto.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                //获取变更后的选中项的ID
                int radioButtonId = arg0.getCheckedRadioButtonId();
                //根据ID获取RadioButton的实例
                RadioButton rb = (RadioButton) RegisterActivity.this.findViewById(radioButtonId);
                //更新文本内容，以符合选中项
//                                 tv.setText("您的性别是：" + rb.getText());
                String name = rb.getText().toString();

                switch (name) {
                    case "张三":
                        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.photo_1);
                        resPhotoID = R.mipmap.photo_1;
                        imgPhoto.setImageBitmap(bitmap);
                        break;
                    case "李四":
                        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.photo_2);
                        resPhotoID = R.mipmap.photo_2;
                        imgPhoto.setImageBitmap(bitmap);
                        break;

                }
            }
        });
        cardManager = new CardManager();
        cardManager.initPsam(this);
    }

    private int resPhotoID;
    CardManager cardManager;
    Uri imageUri;

    @Override
    public void onClick(View v) {
        if (v == btnRegister) {
            //TODO 用户信息写入卡中
            ProgressDialogUtils.showProgressDialog(RegisterActivity.this, "正在更新");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String name = edvName.getText().toString();
                    if (name.equals("") || bitmap == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, "请输入有效信息", Toast.LENGTH_LONG).show();

                            }
                        });
                        ProgressDialogUtils.dismissProgressDialog();
                        return;
                    }
                    final boolean result1 = cardManager.updateUserInfor(JSEscape.getNameByte(name));
                    //TODO 更新性别

                    final boolean result2 = cardManager.updatePhotoInfor(ImgCompress.compressBitmap
                            (ImgCompress.decodeSampledBitmapFromResource(getResources(),
                                    resPhotoID, 100, 100)));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result1 && result2) {
                                Toast.makeText(RegisterActivity.this, "更新成功", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(RegisterActivity.this, RegisterFingerActivity.class);
                                startActivity(intent);
                            }
                            ProgressDialogUtils.dismissProgressDialog();
                        }
                    });

                }
            }).start();


        } else if (v == imageButtonGetPhoto) {
            // 获取图像并显示
            File imageFile = new File(Environment
                    .getExternalStorageDirectory(), "outputImage.jpg");
            if (imageFile.exists()) {
                imageFile.delete();
            }
            try {
                imageFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            //转换成Uri
            imageUri = Uri.fromFile(imageFile);
            //开启选择呢绒界面
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            //设置可以缩放
            intent.putExtra("scale", true);
            //设置可以裁剪
            intent.putExtra("crop", true);
            intent.setType("image/*");
            //设置输出位置
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            //开始选择
            startActivityForResult(intent, CHOOSE_PHOTO);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // 从拍照界面返回
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 设置intent为启动裁剪程序
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    // 设置Data为刚才的imageUri和Type为图片类型
                    intent.setDataAndType(imageUri, "image/*");
                    // 设置可缩放
                    intent.putExtra("scale", true);
                    // 设置输出地址为imageUri
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    // 开启裁剪,设置requestCode为CROP_PHOTO
                    startActivityForResult(intent, CROP_PHOTO);
                }

                break;
            // 从裁剪界面返回
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap;
                    try {
                        //通过BitmapFactory将imageUri转化成bitmap
                        bitmap = BitmapFactory.decodeStream(getContentResolver()
                                .openInputStream(imageUri));
                        //设置显示
                        imgPhoto.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    handleImageOnKitkat(data);
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitkat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri
                    .getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri
                    .getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果不是document类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        }
        displayImage(imagePath); // 根据图片路径显示图片
        System.err.println(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null,
                null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    Bitmap bitmap;

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            bitmap = BitmapFactory.decodeFile(imagePath);
            imgPhoto.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cardManager.realsePsam();
    }
}
