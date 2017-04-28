package com.android.guide;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.android.R;

public class SplashActivity extends BaseActivity {

    private static final int GO_LOGIN = 1;//进入用户登录页
    private static final int SPLASH_DELAY_MILLIS = 1000; //1s延时

    private ImageView logoImage;//Logo图标
    private AlphaAnimation alphaAnimation;//图标显示动画

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GO_LOGIN:
                    goLogin();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_splash);
        initView();
        setAnimation();
        setListener();
    }

    private void initView() {
        logoImage = (ImageView) findViewById(R.id.logo);
    }

    //给图标绑定动画-> 2s从10%到完全显示
    private void setAnimation() {
        alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(2000);
        logoImage.startAnimation(alphaAnimation);
    }

    private void setListener(){

        //监听动画
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            //动画结束后，若连接网络则进入登录界面
            @Override
            public void onAnimationEnd(Animation animation) {
                if(true){  //网络连接判断
                    mHandler.sendEmptyMessageDelayed(GO_LOGIN, SPLASH_DELAY_MILLIS);
                }

            }
        });
    }

    //进入登录界面
    private void goLogin() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        //Intent intent = new Intent(SplashActivity.this, CommunicateActivity.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();

        //开始轮询
        //PollingUtils.startPollingService(this, 5, PollingService.class, PollingService.ACTION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Stop polling service
//        System.out.println("Stop polling service...");
//        PollingUtils.stopPollingService(this, PollingService.class, PollingService.ACTION);
    }

}
