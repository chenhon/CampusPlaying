package com.android.model;

/**
 * 动态
 */

public class Statuses {
    public static final int ACTIVITY_TYPE = 1;
    public static final int PHOTO_TYPE = 2;
    public static final int NOTIFICATION_TYPE = 3;

    private int attach_type;//动态类型
    private Object attach_obj;

    public void setAttach_type(int attach_type) {
        this.attach_type = attach_type;
    }

    public void setAttach_obj(Object attach_obj) {
        this.attach_obj = attach_obj;
    }

    public int getAttach_type() {
        return attach_type;
    }

    public Object getAttach_obj() {
        return attach_obj;
    }
}
