package com.android.guide;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Administrator on 2017/3/7 0007.
 */

public class GlobalApplication extends Application {

    private static GlobalApplication instance;

    private RequestQueue requestQueue;

    private static final String GLOBAL_PREFS_NAME = "GLOBAL_VARIABLE";//SharedPreferences的名称
    private static SharedPreferences sharedPreferences; //存放token
    private static String user_id;

    public static GlobalApplication get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
        editor.clear().commit();
        super.onLowMemory();
    }
    public RequestQueue getRequestQueue() {
        return requestQueue;
    }
    /*
    * 在注册或者登陆是调用来存储token
    * */
    public static final void setToken(String tokenStr) {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putString("token", tokenStr);
        prefEditor.commit();
    }

    public static final String getToken() {
       // return sharedPreferences.getString(GLOBAL_PREFS_NAME,"");
        return "123";  //测试用
    }

    public static void setUserId(String id) {
        user_id = id;
    }
    public static String getUserId() {
        return user_id;
    }

}
