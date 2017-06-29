package com.speedata.yilang;

import android.content.Context;
import android.os.SystemClock;

import com.speedata.libutils.DataConversionUtils;
import com.speedata.libutils.MyLogger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import speedatacom.a3310libs.PsamManager;
import speedatacom.a3310libs.des.DesUtils;
import speedatacom.a3310libs.inf.IPsam;

/**
 * Created by echo on 2017/6/15.
 */

public class CardManager {
    private MyLogger logger = MyLogger.jLog();
    public IPsam psam = PsamManager.getPsamIntance();
    private byte[] key = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

    /**
     * 释放PSAM设备
     */
    public void realsePsam() {
        try {
            if (psam != null)
                psam.releaseDev();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * psam 初始化
     *
     * @param context 上下文
     * @return boolean 初始化结果
     */
    public boolean initPsam(Context context) {
        try {
            psam.initDev(context);
            SystemClock.sleep(1000);
            psam.resetDev();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 获取随机数 RNG:0084000008
     * 外部认证 DACK1:0082 0001 08 ENC(RNG，DACK1) 读取文件:00B085 00 40
     */
    public byte[] getUserInfor() {
//        changeDir(new byte[]{(byte) 0xad, (byte) 0xf1});
        byte[] bytes = new byte[0];
        if (RenZheng((byte) 0x05, (byte) 0x01)) {
            try {
                bytes = psam.WriteCmd(new byte[]{0x00, (byte) 0xb0, (byte) 0x85, 0x00, 0x40}, IPsam.PowerType.Psam2);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            logger.d("getUserInfor" + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
            return bytes;
        } else {
            return bytes;
        }
    }

    /**
     * 获取随机数 RNG:0084000008
     * 外部认证 DACK1:0082 0001 08 ENC(RNG，DACK2) 更新文件:00D685 00 40 DATA[0-64]
     *
     * @param user_data
     * @return
     */
    public boolean updateUserInfor(byte[] user_data) {
        if (user_data.length > 64) {
            logger.d("userdata update len error" + user_data.length);
            return false;
        }
        if (RenZheng((byte) 0x05, (byte) 0x02)) {
            byte[] bytes = new byte[5 + user_data.length];
            System.arraycopy(user_data, 0, bytes, 5, user_data.length);
            bytes[0] = 0x00;
            bytes[1] = (byte) 0xd6;
            bytes[2] = (byte) 0x85;
            bytes[3] = 0x00;
            bytes[4] = 0x40;
            try {
                logger.d("updateUserInfor send> " + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
                bytes = psam.WriteCmd(bytes, IPsam.PowerType.Psam2);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }

            if (isNULL(bytes)) {
                return false;
            }
            logger.d("updateUserInfor" + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
            if (bytes.length >= 2 && bytes[0] == (byte) 0x90 && bytes[1] == (byte) 0x00) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }


    /**
     * 照片信息/指纹信息更新流程:
     * 获取随机数 RNG:0084000008
     * 外部认证 DACK3:0082 0001 08 ENC(RNG，DACK3)
     * 更新文件:(单次更新 200 字节，直到 2048/1024 字节更新完毕)
     * 00A4 00 00 02 0006 /[0007-0010]
     * 00D6 00 00 C8 DATA[0-199]
     * 00D6 00 C8 C8 DATA[200-399]
     * 00D6 01 90 C8 DATA[400-599]
     * ... ...
     */
    public boolean updatePhotoInfor(byte[] or_data) {
        if (RenZheng((byte) 0x06, (byte) 0x03)) {
            //获取数据长度
            byte[] len = DataConversionUtils.intToByteArray1(or_data.length);
            byte[] data = new byte[or_data.length + 2];
            data[0] = len[1];
            data[1] = len[0];
            //将长度放到数据头部，2byte
            System.arraycopy(or_data, 0, data, 2, or_data.length);
            int cecle = (data.length) / 200;
            int yushu = (data.length) % 200;
            logger.d("cecle=" + cecle);
            try {
                //每次最多写入200bytes
                for (int i = 0; i < cecle; i++) {
                    int offset = 0xc8 * i;
                    byte[] temp1 = DataConversionUtils.intToByteArray1(offset);
                    logger.d(DataConversionUtils.byteArrayToStringLog(temp1, temp1.length));
                    byte[] bytes1 = new byte[205];
                    bytes1[0] = 0x00;
                    bytes1[1] = (byte) 0xd6;
                    //偏移量
                    bytes1[2] = temp1[1];
                    bytes1[3] = temp1[0];
                    bytes1[4] = (byte) 0xc8;
                    System.arraycopy(data, offset, bytes1, 5, 200);
                    logger.d("updatephoto send>" + DataConversionUtils.byteArrayToStringLog(bytes1, bytes1.length));
                    byte[] result = psam.WriteCmd(bytes1, IPsam.PowerType.Psam2);
                    if (!isNULL(result))
                        logger.d("updatephoto len=" + result.length + "   " + DataConversionUtils.byteArrayToStringLog(result, result.length));
                    SystemClock.sleep(10);
                }
                //写入余数数据
                if (yushu > 0) {
                    byte[] temp1 = DataConversionUtils.intToByteArray1(0xc8 * cecle);
                    byte[] temp = new byte[yushu + 5];
                    temp[0] = 0x00;
                    temp[1] = (byte) 0xd6;
                    temp[2] = temp1[1];
                    temp[3] = temp1[0];
                    temp[4] = (byte) yushu;
                    System.arraycopy(data, 0xc8 * cecle, temp, 5, yushu);
                    logger.d("updatephoto send>" + DataConversionUtils.byteArrayToStringLog(temp, temp.length));
                    byte[] result = psam.WriteCmd(temp, IPsam.PowerType.Psam2);
                    if (!isNULL(result))
                        logger.d("updatephoto len=" + result.length + "   " + DataConversionUtils.byteArrayToStringLog(result, result.length));

                    SystemClock.sleep(10);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }


    /**
     * 照片信息/指纹信息读取流程:
     * 获取随机数 RNG:0084000008
     * 外部认证 DACK4:0082 0001 08 ENC(RNG，DACK4)
     * 读取文件:(单次 读取 200 字节，直到 2048/1024 字节读取完毕)
     * 00A4 00 00 02 0006 /[0007-0010]
     * 00B0 00 00 C8 DATA[0-199]
     * 00B0 00 C8 C8 DATA[200-399]
     * 00B0 01 90 C8 DATA[400-599]
     * ... ...
     */
    public byte[] getPhotoData() {
        byte[] result = new byte[0];//= new byte[2048];
        if (RenZheng((byte) 0x06, (byte) 0x04)) {
            byte[] bytes1 = {0x00, (byte) 0xb0, (byte) 0x00, 0x00, (byte) 0x02};
//            logger.d("send>" + DataConversionUtils.byteArrayToStringLog(bytes1, bytes1.length));
            byte[] bytes = new byte[0];

            try {
                logger.d("send>" + DataConversionUtils.byteArrayToStringLog(bytes1, bytes1.length));
                bytes = psam.WriteCmd(bytes1, IPsam.PowerType.Psam2);
                if (!isNULL(bytes)) {
                    logger.d("getPhotoDatalen =" + bytes.length + "   " + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
                    int pic_len = DataConversionUtils.byteArrayToInt(new byte[]{bytes[0], bytes[1]});
                    result = new byte[pic_len];
                    //去掉前两个字节（长度） 和后面的9000
//                    System.arraycopy(bytes, 2, result, 0, 198);
                    logger.d("pic_len=" + pic_len);
                    int cecle = (pic_len) / 200;
                    int yushu = (pic_len) % 200;

                    for (int i = 0; i < cecle; i++) {
                        byte[] temp1 = DataConversionUtils.intToByteArray1((0xc8 * i) + 2);
                        logger.d(DataConversionUtils.byteArrayToStringLog(temp1, temp1.length));
                        if (i == 0) {
                            bytes1[2] = 0x00;
                            bytes1[3] = 0x00;
                        }

                        bytes1[2] = temp1[1];
                        bytes1[3] = temp1[0];
                        bytes1[4] = (byte) 0xc8;

                        logger.d("send>" + DataConversionUtils.byteArrayToStringLog(bytes1, bytes1.length));
                        bytes = psam.WriteCmd(bytes1, IPsam.PowerType.Psam2);
                        logger.d("getPhotoDatalen=i=" + i + " " + bytes.length + "   " + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
                        System.arraycopy(bytes, 0, result, 200 * i, 200);
                    }
                    // 取余数数据
                    if (yushu > 0) {
                        byte[] temp1 = DataConversionUtils.intToByteArray1((0xc8 * cecle) + 2);
                        bytes1[2] = temp1[1];
                        bytes1[3] = temp1[0];
                        bytes1[4] = (byte) yushu;
                        logger.d("get yuxia send>" + DataConversionUtils.byteArrayToStringLog(bytes1, bytes1.length));
                        bytes = psam.WriteCmd(bytes1, IPsam.PowerType.Psam2);
                        logger.d("getPhotoDatalen last=" + bytes.length + "   " + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
                        System.arraycopy(bytes, 0, result, (0xc8 * cecle), bytes.length - 2);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            return result;
        } else {
            return result;
        }
    }

//    public void psamReadSpeedTest(){
//        //测试读取100个字节的速度
//        byte[] bytes1 = {0x00, (byte) 0xb0, (byte) 0x00, 0x00, (byte) 0x64};
//
//        try {
//            logger.d("====test start==");
//            byte[] bytes = psam.WriteCmd(bytes1, IPsam.PowerType.Psam2);
//            logger.d("test 384=" + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
//            logger.d("====test end==");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 获取指纹信息
     *
     * @param order 文件 范围 0x07~0x10
     * @return
     */
    public byte[] getFingerData(byte order) {
        if (order > (byte) 0x10 || order < (byte) 0x07) {
            return null;
        }
        byte[] result = new byte[0];
        if (RenZheng((byte) order, (byte) 0x04)) {
            byte[] bytes1 = {0x00, (byte) 0xb0, (byte) 0x00, 0x00, (byte) 0x02};
            byte[] bytes = new byte[0];

            try {
                logger.d("send>" + DataConversionUtils.byteArrayToStringLog(bytes1, bytes1.length));
                bytes = psam.WriteCmd(bytes1, IPsam.PowerType.Psam2);
                if (!isNULL(bytes)) {
                    logger.d("getFingerDatalen =" + bytes.length + "   " + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
                    int pic_len = DataConversionUtils.byteArrayToInt(new byte[]{bytes[0], bytes[1]});
                    if (pic_len > 1024)
                        return null;
                    result = new byte[pic_len];
                    logger.d("getFingerDatalen=" + pic_len);
                    int cecle = (pic_len) / 200;
                    int yushu = (pic_len) % 200;
                    for (int i = 0; i < cecle; i++) {
                        byte[] temp1 = DataConversionUtils.intToByteArray1((0xc8 * i) + 2);
                        logger.d(DataConversionUtils.byteArrayToStringLog(temp1, temp1.length));
                        if (i == 0) {
                            bytes1[2] = 0x00;
                            bytes1[3] = 0x00;
                        }
                        bytes1[2] = temp1[1];
                        bytes1[3] = temp1[0];
                        bytes1[4] = (byte) 0xc8;

                        logger.d("send>" + DataConversionUtils.byteArrayToStringLog(bytes1, bytes1.length));
                        bytes = psam.WriteCmd(bytes1, IPsam.PowerType.Psam2);
                        logger.d("getFingerDatalen=i=" + i + " " + bytes.length + "   " + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
                        System.arraycopy(bytes, 0, result, 200 * i, 200);
                    }
                    // 取余数数据
                    if (yushu > 0) {
                        byte[] temp1 = DataConversionUtils.intToByteArray1((0xc8 * cecle) + 2);
                        bytes1[2] = temp1[1];
                        bytes1[3] = temp1[0];
                        bytes1[4] = (byte) yushu;
                        logger.d("get yuxia send>" + DataConversionUtils.byteArrayToStringLog(bytes1, bytes1.length));
                        bytes = psam.WriteCmd(bytes1, IPsam.PowerType.Psam2);
                        logger.d("getFingerDatalen last=" + bytes.length + "   " + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
                        System.arraycopy(bytes, 0, result, (0xc8 * cecle), bytes.length - 2);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            bytes1[2] = 0x00;
            bytes1[3] = (byte) 0x00;
            bytes1[4] = (byte) 0x64;//测试读取100个字节速度
            try {
                logger.d("====test start==");
                bytes = psam.WriteCmd(bytes1, IPsam.PowerType.Psam2);
                logger.d("test 384=" + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
                logger.d("====test end==");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return result;
        } else {
            return result;
        }
    }

    /**
     * 指纹信息更新流程:
     * 获取随机数 RNG:0084000008
     * 外部认证 DACK3:0082 0001 08 ENC(RNG，DACK3)
     * 更新文件:(单次更新 200 字节，直到 2048/1024 字节更新完毕)
     * 00A4 00 00 02 [0007-0010]
     * 00D6 00 00 C8 DATA[0-199]
     * 00D6 00 C8 C8 DATA[200-399]
     * 00D6 01 90 C8 DATA[400-599]
     * ... ...
     *
     * @param finger_data 指纹数据 长度不得超过1022
     * @param order       0x07~0x10
     * @return boolean
     */
    public boolean updateFinger(byte[] finger_data, byte order) {
        if (finger_data.length > 1022)
            return false;
        if (RenZheng(order, (byte) 0x03)) {

//            byte[] len=new byte[2];
            byte[] len = DataConversionUtils.intToByteArray1(finger_data.length);
            byte[] data = new byte[finger_data.length + 2];
//            System.arraycopy(len,0,data,0,2);
            data[0] = len[1];
            data[1] = len[0];
            logger.d("update finger len=" + data.length);
            System.arraycopy(finger_data, 0, data, 2, finger_data.length);
            int cecle = (data.length) / 200;
            int yushu = (data.length) % 200;
            logger.d("cecle=" + cecle);
            try {
                for (int i = 0; i < cecle; i++) {
                    int offset = 0xc8 * i;
                    byte[] temp1 = DataConversionUtils.intToByteArray1(offset);
                    logger.d(DataConversionUtils.byteArrayToStringLog(temp1, temp1.length));
                    byte[] bytes1 = new byte[0];

                    bytes1 = new byte[205];
                    bytes1[0] = 0x00;
                    bytes1[1] = (byte) 0xd6;
                    bytes1[2] = temp1[1];
                    bytes1[3] = temp1[0];
                    bytes1[4] = (byte) 0xc8;
                    System.arraycopy(data, offset, bytes1, 5, 200);
                    logger.d("updatefinger send>" + DataConversionUtils.byteArrayToStringLog(bytes1, bytes1.length));
                    byte[] result = psam.WriteCmd(bytes1, IPsam.PowerType.Psam2);
                    if (!isNULL(result))
                        logger.d("updatefinger len=" + result.length + "   " + DataConversionUtils.byteArrayToStringLog(result, result.length));
                    SystemClock.sleep(10);
                }
                if (yushu > 0) {
                    byte[] temp1 = DataConversionUtils.intToByteArray1(0xc8 * cecle);
                    byte[] temp = new byte[yushu + 5];
                    temp[0] = 0x00;
                    temp[1] = (byte) 0xd6;
                    temp[2] = temp1[1];
                    temp[3] = temp1[0];
                    temp[4] = (byte) yushu;
                    System.arraycopy(data, 0xc8 * cecle, temp, 5, yushu);
                    logger.d("updatefinger send>" + DataConversionUtils.byteArrayToStringLog(temp, temp.length));
                    byte[] result = psam.WriteCmd(temp, IPsam.PowerType.Psam2);
                    if (!isNULL(result))
                        logger.d("updatefinger len=" + result.length + "   " + DataConversionUtils.byteArrayToStringLog(result, result.length));

                    SystemClock.sleep(10);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }


    /**
     * 切换DF目录
     */
    private void changeADF1() {
        //00 a4 00 00 02 00 ad f1
        //00A4 0000 02 ADF1
        byte[] cmd_dir = {0x00, (byte) 0xA4, 0x00, 0x00, 0x02, (byte) 0xDF, (byte) 0x01};
        try {
            logger.d("send changedf01 dir>" + DataConversionUtils.byteArrayToStringLog(cmd_dir, cmd_dir.length));
            byte[] result = psam.WriteCmd(cmd_dir, IPsam.PowerType.Psam2);
            if (!isNULL(result)) {
                logger.d("change changedf01 rece>" + DataConversionUtils.byteArrayToStringLog(result, result.length));
            } else {
                logger.d("change changedf01 rece>null");
            }
            if (result.length >= 2 && result[0] == (byte) 0x61) {
                result = psam.WriteCmd(cmd_dir, IPsam.PowerType.Psam2);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /**
     * 是否为空
     *
     * @param object
     * @return boolean
     */
    public boolean isNULL(Object object) {
        if (object == null)
            return true;
        else return false;
    }

    /**
     * 获取随机数
     *
     * @return byte[]
     */
    private byte[] getRomdon() {
        try {
            return psam.WriteCmd(new byte[]{0x00, (byte) 0x84, 0x00, 0x00, 0x08}, IPsam.PowerType.Psam2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @param file 要读取的文件
     * @param dack 秘钥索引
     * @return 认证结果
     */
    private boolean RenZheng(byte file, byte dack) {

        psam.PsamPower(IPsam.PowerType.Psam2);
        changeADF1();
        changeDir(new byte[]{file});
        //谁波特率位115200
        //aa bb 06 00 00 00 02 01 07 03
        //80FC00011000002379ECB20000EB5D200D0305070B
//        byte[] tt = {(byte) 0x80, (byte) 0xFC, 0x00, 0x01, 0x10, 0x00, 0x00, 0x23, 0x79, (byte) 0xEC, (byte) 0xB2, 0x00, 0x00, (byte) 0xEB, 0x5D,
//                0x20, 0x0D, 0x03, 0x05, 0x07, 0x0B};
//        byte[] tt={(byte)0xaa, (byte)0xbb, 0x06, 0x00,0x00, 0x00, 0x02, 0x01, 0x07, 0x04};
//
//        try {
//            logger.d("test");
//            byte[] test = psam.WriteOriginalCmd(tt, IPsam.PowerType.Psam2);
//            logger.d("test" + DataConversionUtils.byteArrayToStringLog(test, test.length));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        byte[] random = getRomdon();
        if (!isNULL(random)) {
            try {
                logger.d("random=" + DataConversionUtils.byteArrayToStringLog(random, random.length));
                //获取3DES加密cbc数据
                byte[] temp = new byte[random.length - 2];
                System.arraycopy(random, 0, temp, 0, temp.length);
                byte[] enc = DesUtils.encryptBy3DesEcb(temp, key);

                byte[] cmd = {0x00, (byte) 0x82, 0x00, 0x04, 0x08};
                cmd[3] = dack;

                if (!isNULL(enc)) {
                    logger.d("enc=" + DataConversionUtils.byteArrayToStringLog(enc, enc.length));
                    byte[] finalcmd = new byte[cmd.length + enc.length];
                    System.arraycopy(cmd, 0, finalcmd, 0, cmd.length);
                    System.arraycopy(enc, 0, finalcmd, cmd.length, enc.length);
                    logger.d("renzheng send>" + DataConversionUtils.byteArrayToStringLog(finalcmd, finalcmd.length));
                    byte[] renzheng = psam.WriteCmd(finalcmd, IPsam.PowerType.Psam2);
                    if (isNULL(renzheng))
                        return false;
                    logger.d("renzheng rece>" + DataConversionUtils.byteArrayToStringLog(renzheng, renzheng.length));
                    if (renzheng.length >= 2 && renzheng[0] == (byte) 0x90 && renzheng[1] == (byte) 0x00) {
                        return true;
                    }
                    return false;
                } else {
                    logger.d("enc==null");
                    return false;
                }
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                return false;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }

    }

    /**
     * @param dir 目录文件
     * @return boolean
     */
    private boolean changeDir(byte[] dir) {
        byte[] cmd_dir = new byte[6 + dir.length];
        cmd_dir[0] = 0x00;
        cmd_dir[1] = (byte) 0xA4;
        cmd_dir[2] = 0x00;
        cmd_dir[3] = 0x00;
        cmd_dir[4] = 0x02;
        cmd_dir[5] = 0x00;
        System.arraycopy(dir, 0, cmd_dir, 6, dir.length);
        try {
            logger.d("send change dir>" + DataConversionUtils.byteArrayToStringLog(cmd_dir, cmd_dir.length));
            byte[] result = psam.WriteCmd(cmd_dir, IPsam.PowerType.Psam2);
            if (!isNULL(result)) {
                logger.d("change dir rece>" + DataConversionUtils.byteArrayToStringLog(result, result.length));
            } else {
                logger.d("change dir rece>null");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return
                false;
    }


}
