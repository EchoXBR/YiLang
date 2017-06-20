package com.speedata.yilang;

/**
 * Created by echo on 2017/6/20.
 */

public class UserInfor {
    public String Name;
    public String Sex;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getSex() {
        return Sex;
    }

    public void setSex(String sex) {
        Sex = sex;
    }

    @Override
    public String
    toString() {
        return "UserInfor{" +
                "Name='" + Name + '\'' +
                ", Sex='" + Sex + '\'' +
                '}';
    }
}
