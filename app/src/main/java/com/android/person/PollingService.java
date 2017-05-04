package com.android.person;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;

public class PollingService extends Service {
    public static final String ACTION = "com.android.person.service.PollingService";
    private GetNewPrivateListener mListener;
    private PrivateBind mBind = new PrivateBind();

    class PrivateBind extends Binder{
        public PollingService getMyService()  //获取当前服务的实例
        {
            return PollingService.this;
        }
    }

    public void setListener(GetNewPrivateListener listener) {
       // System.out.println("设置setListener");
        mListener = listener;
       // mListener.getNewPrivate();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBind;
    }
    @Override
    public void onCreate() {
        System.out.println("PollingService:onCreate运行");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("PollingService:onStartCommand运行");

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        int seconds = 2*1000;
        //触发服务的起始时间
        long triggerAtTime = SystemClock.elapsedRealtime() + seconds;
        Intent i = new Intent(this, PollingService.class);
        PendingIntent pi = PendingIntent.getService(this, 0 , i , 0);
        if(mListener!= null) {
          //  new PollingThread().start();
            mListener.getNewPrivate();
            manager.cancel(pi);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }
        else{
            System.out.println("PollingService:为空");
            manager.cancel(pi);
        }
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * Polling thread
     * 模拟向Server轮询的异步线程
     * @Author Ryan
     * @Create 2013-7-13 上午10:18:34
     */
    class PollingThread extends Thread {
        @Override
        public void run() {
            mListener.getNewPrivate();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("PollingService:Service:onDestroy");
    }

    /**
     * 获取新的私信的接口
     */
    public interface GetNewPrivateListener {
        void getNewPrivate();
    }

}
