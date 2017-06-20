package com.speedata.yilang;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by echo on 2017/6/20.
 */

public class CardParse {
    /**
     * 图片解析
     *
     * @param data
     * @return
     */
    public static Bitmap PaseBitmap(byte[] data) {

        if (data != null && data.length != 0) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } else {
            return null;
        }
    }

    /**
     * 用户信息解析
     *
     * @param data
     * @return UserInfor
     */
    public static UserInfor PaseUserInfor(byte[] data) {

        UserInfor userInfor = new UserInfor();
        String name = stringFilter(JSEscape.parseName(data));
        userInfor.setName(name);
        return userInfor;
    }

    /**
     * 过滤字符串乱码
     *
     * @param str
     * @return
     */
    public static String stringFilter(String str) {
        str = str
                .replaceAll(
                        "[^(\u4e00-\u9fa5)|(\u0030-\u0039)|(\u0061-\u007a)|(\u0041-\u005a)|(\u0028-\u003A)]",
                        "");// 过滤
        return str;
    }
}
