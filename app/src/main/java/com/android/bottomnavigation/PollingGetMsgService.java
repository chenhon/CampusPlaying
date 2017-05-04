package com.android.bottomnavigation;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.android.GlobalApplication;

public class PollingGetMsgService extends Service {
    public static final String ACTION = "com.android.bottomnavigation.service.PollingGetMsgService";
    private PrivateBind mBind = new PrivateBind();

    class PrivateBind extends Binder{
        public PollingGetMsgService getMyService()  //获取当前服务的实例
        {
            return PollingGetMsgService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBind;
    }
    @Override
    public void onCreate() {
        System.out.println("onCreate运行");
        new PollingThread().start();
    }
/*    @Override
    public void onStart(Intent intent, int startId) {

    }*/

    /**
     * Polling thread
     * 模拟向Server轮询的异步线程
     */
    class PollingThread extends Thread {
        @Override
        public void run() {

            Intent intent = new Intent();
            intent.setAction("com.android.newcount");
            GlobalApplication.get().sendBroadcast(intent); //发送广播

            System.out.println("Polling...");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Service:onDestroy");
    }
}
