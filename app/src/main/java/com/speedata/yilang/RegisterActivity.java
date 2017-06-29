package com.speedata.yilang;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
        setTitle(R.string.btn_zhuce);
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
                    case "Joan":
                        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.photo_1);
                        resPhotoID = R.mipmap.photo_1;
                        imgPhoto.setImageBitmap(bitmap);
                        break;
                    case "Tom":
                        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.photo_3);
                        resPhotoID = R.mipmap.photo_3;
                        imgPhoto.setImageBitmap(bitmap);
                        break;

                }
            }
        });
        cardManager = new CardManager();
//        cardManager.initPsam(this);
    }

    private int resPhotoID;
    CardManager cardManager;
    Uri imageUri;

    @Override
    public void onClick(View v) {
        if (v == btnRegister) {
            //TODO 用户信息写入卡中
            if (write_photo == null) {
                Toast.makeText(this, "please get photo first!", Toast.LENGTH_SHORT).show();
                return;
            }
            ProgressDialogUtils.showProgressDialog(RegisterActivity.this, getString(R.string.updata));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String name = edvName.getText().toString();
                    if (name.equals("") || bitmap == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, getString(R.string.input_info), Toast.LENGTH_LONG).show();

                            }
                        });
                        ProgressDialogUtils.dismissProgressDialog();
                        return;
                    }
                    cardManager.updateUserInfor(new byte[64]);
                    final boolean result1 = cardManager.updateUserInfor(JSEscape.getNameByte(name + "," + Sex));
                    //TODO 更新性别


                    final boolean result2 = cardManager.updatePhotoInfor(write_photo);
//                            ImgCompress.compressBitmap(
//                                    ImgCompress.decodeSampledBitmapFromResource(getResources(), resPhotoID, 100, 100)));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result1 && result2) {
                                Toast.makeText(RegisterActivity.this, getString(R.string.updata_success), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(RegisterActivity.this, RegisterFingerActivity.class);
                                startActivity(intent);
                            }
                            ProgressDialogUtils.dismissProgressDialog();
                        }
                    });

                }
            }).start();


        } else if (v == imageButtonGetPhoto) {
            showDialog();
        }
    }


    Bitmap bitmap;


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        cardManager.realsePsam();
    }


    // 拍照存放本地文件名称
    public static final String IMAGE_FILE_NAME = "faceImage.jpg";
    // 裁剪后的文件名称
    public static final String IMAGE_FILE_NAME_TEMP = "faceImage_temp.jpg";

    private File file = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);

    private File cropFile = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME_TEMP);
    private Uri imageCropUri = Uri.fromFile(cropFile);
    // 上传头像方式文字数据
    private String[] items = new String[]{"选择本地图片", "拍照"};

    // 请求码
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int RESULT_REQUEST_CODE = 3;


    /**
     * 选择是拍照还是选择本地图片
     */
    private void showDialog() {

        new AlertDialog
                .Builder(this).setTitle("设置头像").setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //从相册选择
                        Intent intent = new Intent(Intent.ACTION_PICK, null);
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(intent, IMAGE_REQUEST_CODE);
                        break;
                    case 1:
                        //拍照
                        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // 判断存储卡是否可以用，可用进行存储
                        if (hasSdcard()) {
                            intentFromCapture.putExtra("return-data", false);
                            imageUri = Uri.fromFile(file);
                            intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            intentFromCapture.putExtra("noFaceDetection", true);
                        }
                        startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);
                        break;
                }
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();

    }

    /**
     * 判断sdcard是否存在
     */
    private boolean hasSdcard() {
        // 判断Sdcard是否可用
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    /**
     * 处理回转值
     */
    @SuppressWarnings("static-access")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != this.RESULT_CANCELED) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    // 裁剪图片方法
                    startPhotoZoom(data.getData());
                    break;
                // 保存头像
                case CAMERA_REQUEST_CODE:
                    if (hasSdcard()) {
                        startPhotoZoom(imageUri);
                    } else {
                        Toast.makeText(this, "未找到存储卡，无法存储照片！", Toast.LENGTH_SHORT).show();
                    }
                    break;
                // 裁剪之后，显示头像
                case RESULT_REQUEST_CODE:
                    if (data != null) {
                        setImageToView(data);
                    }
                    break;
            }
        }
    }

    /**
     * 裁剪图片方法实现
     */
    public void startPhotoZoom(Uri uri) {
        if (uri == null) {
            Log.i("tag", "========The uri is not exist.");
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        //裁剪之后，保存在裁剪文件中，关键
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCropUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", false);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }

    private byte[] write_photo;

    /**
     * 保存裁剪之后的图片数据,并显示
     */
    private void setImageToView(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageCropUri));
            Log.d("size", bitmap.getByteCount() + "");
            write_photo = ImgCompress.getPicByte(bitmap);
            System.out.println("photo==" + write_photo.length);
            imgPhoto.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (extras != null) {
            // bitmap = extras.getParcelable("data");
            // Log.d("size",bitmap.getByteCount()+"");

            /*
             * ByteArrayOutputStream stream = new ByteArrayOutputStream();
             * bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
             * byte[] b = stream.toByteArray(); // 将图片流以字符串形式存储下来
             *
             * tp = new String(Base64Coder.encodeLines(b));
             * 这个地方大家可以写下给服务器上传图片的实现，直接把tp直接上传就可以了， 服务器处理的方法是服务器那边的事了，吼吼
             *
             * 如果下载到的服务器的数据还是以Base64Coder的形式的话，可以用以下方式转换 为我们可以用的图片类型就OK啦...吼吼
             * Bitmap dBitmap = BitmapFactory.decodeFile(tp);
             * Drawable drawable = new BitmapDrawable(dBitmap);
             */
        }
    }

}
