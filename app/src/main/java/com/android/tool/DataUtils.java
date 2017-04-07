package com.android.tool;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/3/16 0016.
 */

public class DataUtils {
    public static final String DATA_TYPE1 = "yyyy-MM-dd HH:mm:ss";
    public static final String DATA_TYPE2 = "MM-dd HH:mm";
    public static final String DATA_TYPE3 = "yyyy-MM-dd";
    public static final String DATA_TYPE4 = "yyyy年MM月dd日 HH:mm";
    public static final String DATA_TYPE5 = "yyyyMM";
    public static final String DATA_TYPE6 = "yyyy-MM-dd HH:mm";
    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String dataType, long stamp){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dataType);
        Date date = new Date(stamp);
        res = simpleDateFormat.format(date);
        return res;
    }
    /*
     * 将时间转换为时间戳
     */
    public static long dateToStamp(String s) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = simpleDateFormat.parse(s);
        return date.getTime();
    }
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
}

