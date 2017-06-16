package com.speedata.yilang;

import android.os.SystemClock;

import com.speedata.libutils.DataConversionUtils;

import java.io.UnsupportedEncodingException;

import speedatacom.a3310libs.PsamManager;
import speedatacom.a3310libs.inf.IPsam;

/**
 * Created by echo on 2017/6/16.
 */

public class Test {
    private boolean isBlankCard = false;//是否是白卡
    private IPsam psamIntance = PsamManager.getPsamIntance();
    /**
     * PSAM 测试流程
     */
    private void testPsam() {
        byte[] data = psamIntance.PsamPower(IPsam.PowerType.Psam2);
        if (data == null) {
            System.out.println("test====PSAM2 failed");
            psamIntance.resetDev();
            SystemClock.sleep(1000);
            data = psamIntance.PsamPower(IPsam.PowerType.Psam2);
            if (data == null) {
                System.out.println("test====PSAM2 failed second");
                return;
            }
        }
        //白卡需要创建MF文件
        if (isBlankCard)
            ceratMF();
        System.out.println("test====" + DataConversionUtils.byteArrayToStringLog(data, data.length));
        try {
            data = psamIntance.WriteCmd(Cmd.CMD_DIR_3F00, IPsam.PowerType.Psam2);
            if (data == null) {
                System.out.println("test====CMD_DIR_3F00=null");
                return;
            }
            System.out.println("test====CMD_DIR_3F00=" + DataConversionUtils.byteArrayToStringLog(data, data.length));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            data = psamIntance.WriteCmd(Cmd.CMD_84, IPsam.PowerType.Psam2);
            if (data == null) {
                System.out.println("test====CMD_84=null");
                return;
            }
            System.out.println("test====CMD_84=" + DataConversionUtils.byteArrayToStringLog(data, data.length));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (isBlankCard)
            ceratMFKey();
    }

    /**
     * 创建MF文件
     */
    private void ceratMF() {

        try {
            byte[] data = psamIntance.WriteCmd(Cmd.CMD_CREAT_3F00, IPsam.PowerType.Psam2);
            if (data == null) {
                System.out.println("test====CMD_CREAT_3F00=null");
                return;
            }
            System.out.println("test====CMD_CREAT_3F00=" + DataConversionUtils.byteArrayToStringLog(data, data.length));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /**
     * 创建MF文件
     */
    private void ceratMFKey() {

        try {
            byte[] data = psamIntance.WriteCmd(Cmd.CMD_MF_KEY, IPsam.PowerType.Psam2);
            if (data == null) {
                System.out.println("test====CMD_MF_KEY=null");
                return;
            }
            System.out.println("test====CMD_MF_KEY=" + DataConversionUtils.byteArrayToStringLog(data, data.length));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
