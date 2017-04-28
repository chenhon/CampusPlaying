package com.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.android.model.User;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baidu.mapapi.SDKInitializer;

/**
 * Created by Administrator on 2017/3/7 0007.
 */

public class GlobalApplication extends Application {
    public static final int MALE = 0;
    public static final int FEMALE = 1;
    private static GlobalApplication instance;

    private RequestQueue requestQueue;

    private static final String GLOBAL_PREFS_NAME = "GLOBAL_VARIABLE";//SharedPreferences的名称
    private static SharedPreferences sharedPreferences; //存放token
    private static int user_id;
    private static Bitmap myAvatar;   //存放本用户的头像
    private static User mySelf= new User();;//存放登录账号信息
    private static Bitmap userAvatar;  //临时存放头像
    private static Bitmap activityImage;//临时存放活动图片

    public static GlobalApplication get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());//百度地图SDK初始化
        instance = this;
        sharedPreferences = getSharedPreferences(GLOBAL_PREFS_NAME, Context.MODE_PRIVATE);
        if( this.requestQueue == null) {
            this.requestQueue = Volley.newRequestQueue( this.getApplicationContext());
        }

    }
    @Override
    public void onTerminate(){
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {

        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
      //  editor.clear().commit();
        super.onLowMemory();
    }
    public RequestQueue getRequestQueue() {
        return requestQueue;
    }
    /*
    * 在注册或者登陆时调用来存储token
    * */
    public static final void setToken(String tokenStr) {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putString("token", tokenStr);
        prefEditor.commit();
    }

    public static final String getPassword() {
        return sharedPreferences.getString("password","");
    }
    public static final void setPassword(String tokenStr) {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putString("password", tokenStr);
        prefEditor.commit();
    }

    public static final String getToken() {
        return sharedPreferences.getString("token","");
    }

    public static void setUserId(int id) {
        user_id = id;
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putInt("uid", id);
        prefEditor.commit();
    }
    public static int getUserId() {
        //return user_id;
        return sharedPreferences.getInt("uid",0);
    }

    public static void setMyAvatar(Bitmap myAvatar) {
        GlobalApplication.myAvatar = myAvatar;
    }

    public static Bitmap getMyAvatar() {
        return myAvatar;
    }

    public static void setUserAvatar(Bitmap avatar) {
        userAvatar = avatar;
    }

    public static Bitmap getUserAvatar() {
        return userAvatar;
    }

    public static void setActivityImage(Bitmap activityImage) {
        GlobalApplication.activityImage = activityImage;
    }

    public static Bitmap getActivityImage() {
        return activityImage;
    }

    public static User getMySelf() {
        return mySelf;
    }
}
