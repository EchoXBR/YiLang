package com.speedata.yilang;

import android.app.admin.DeviceAdminInfo;
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

//        byte[] data = psam.PsamPower(IPsam.PowerType.Psam2);
//        data = psam.PsamPower(IPsam.PowerType.Psam2);
//        data = psam.PsamPower(IPsam.PowerType.Psam2);
//        data = psam.PsamPower(IPsam.PowerType.Psam2);

//        if (data == null) {
//            logger.d("test====PSAM2 failed");
//            return false;
//        }
//        return true;
    }


    /**
     * 获取随机数 RNG:0084000008
     * 外部认证 DACK1:0082 0001 08 ENC(RNG，DACK1) 读取文件:00B085 00 40
     */
    public byte[] getUserInfor() {
//        changeDir(new byte[]{(byte) 0xad, (byte) 0xf1});
        if (RenZheng((byte) 0x05, (byte) 0x01)) {
            byte[] bytes = new byte[0];
            try {
                bytes = psam.WriteCmd(new byte[]{0x00, (byte) 0xb0, (byte) 0x85, 0x00, 0x40}, IPsam.PowerType.Psam2);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            logger.d("getUserInfor" + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
            return bytes;


        } else {
            return null;
        }
    }


    public void changeADF1() {
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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

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
    public byte[] updatePhotoInfor(byte[] data) {
        byte[] random = getRomdon();
        if (!isNULL(random)) {
            try {
                logger.d("random=" + DataConversionUtils.byteArrayToStringLog(random, random.length));
                //获取3DES加密cbc数据
                byte[] enc = DesUtils.encryptBy3DesCbc(random, key);

                byte[] cmd = {0x00, (byte) 0x82, 0x00, 0x01, 0x08};
                if (!isNULL(enc)) {
                    logger.d("enc=" + DataConversionUtils.byteArrayToStringLog(enc, enc.length));
                    byte[] finalcmd = new byte[cmd.length + enc.length];
                    System.arraycopy(cmd, 0, finalcmd, 0, cmd.length);
                    System.arraycopy(enc, 0, finalcmd, cmd.length, enc.length);
                    logger.d("renzheng send>" + DataConversionUtils.byteArrayToStringLog(finalcmd, finalcmd.length));
                    byte[] renzheng = psam.WriteCmd(finalcmd, IPsam.PowerType.Psam2);
                    logger.d("renzheng rece>" + DataConversionUtils.byteArrayToStringLog(renzheng, renzheng.length));
                    if (renzheng.length >= 2 && renzheng[0] == 0x90 && renzheng[1] == 0x00) {
                        return psam.WriteCmd(new byte[]{0x00, (byte) 0xb0, (byte) 0x85, 0x00, 0x40}, IPsam.PowerType.Psam2);
                    }
                    return renzheng;
                } else {
                    return null;
                }
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                return null;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }

        } else {
            return null;
        }
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
        byte[] result = new byte[2048];
        if (RenZheng((byte) 0x06, (byte) 0x04)) {
            byte[] bytes1 = {0x00, (byte) 0xb0, (byte) 0x00, 0x00, (byte) 0xC8};
            logger.d("send>" + DataConversionUtils.byteArrayToStringLog(bytes1, bytes1.length));
            byte[] bytes = new byte[0];
            int cecle = result.length / 200;
            int yushu = result.length % 200;
            if (yushu != 0)
                cecle = +1;
            for (; cecle < 0; cecle--) {

            }
            try {
                bytes = psam.WriteCmd(bytes1, IPsam.PowerType.Psam2);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            logger.d("getPhotoData" + DataConversionUtils.byteArrayToStringLog(bytes, bytes.length));
            return bytes;
        } else {
            return null;
        }
    }

    /**
     * @param file 要读取的文件
     * @param dack 秘药索引
     * @return 认证结果
     */
    private boolean RenZheng(byte file, byte dack) {
        psam.PsamPower(IPsam.PowerType.Psam2);
        changeADF1();
        changeDir(new byte[]{file});
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

    public boolean changeDir(byte[] dir) {
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


    /**
     * 获取指纹信息
     *
     * @return byte[]
     */
    public byte[] getFingerData(int order) {


        return null;
    }


}
