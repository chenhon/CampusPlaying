package com.android.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.BaseActivity;
import com.android.GlobalApplication;
import com.android.R;
import com.android.bottomnavigation.MainNavigationActivity;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.RequestManager;
import com.android.tool.VolleyRequestParams;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

public class SplashActivity extends BaseActivity {

    private static final int GO_LOGIN = 1;//进入用户登录页
    private static final int SPLASH_DELAY_MILLIS = 1000; //1s延时

    private ImageView logoImage;//Logo图标
    private AlphaAnimation alphaAnimation;//图标显示动画

    private String rootString;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private ProgressHUD mProgressHUD;

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

        rootString = getResources().getString(R.string.ROOT)+"token";
        networkStatus = new NetworkConnectStatus(this);
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

    private boolean canAutoLogin() {
        String token = GlobalApplication.getToken();
        int uid = GlobalApplication.getUserId();
        if((uid != 0)&& (!"".equals(token))) {
            GlobalApplication.getMySelf().setId(uid);//保存用户ID
            return true;
        }
        return false;
    }
    public void autoLogin() {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(SplashActivity.this, "登录中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
            mProgressHUD.dismiss();
            RequestManager.cancelAll(this);//取消所有请求
                }
            });
        VolleyRequestParams headerParams = new VolleyRequestParams()
                .with("token", GlobalApplication.getToken())
                .with("Accept","application/json"); // 数据格式设置为json
        //Toast.makeText(MainActivity.this, "getUserInfo", Toast.LENGTH_SHORT).show();
        mStringRequest = new MyStringRequest(Request.Method.GET, getResources().getString(R.string.ROOT)+"user/"+GlobalApplication.getMySelf().getId(), headerParams, null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mProgressHUD.dismiss();
                        try {
                            Log.e("login:TAG", response);
                            JSONObject jsObject= new JSONObject(response);
                            GlobalApplication.getMySelf().setDescription(jsObject.getString("description"));
                            GlobalApplication.getMySelf().setUser(jsObject.getString("user"));
                            GlobalApplication.getMySelf().setName(jsObject.getString("name"));
                            GlobalApplication.getMySelf().setAvatar(jsObject.getInt("avatar"));
                            GlobalApplication.getMySelf().setGender(jsObject.getInt("gender"));
                            GlobalApplication.getMySelf().setFollowersCount(jsObject.getInt("followers_count"));
                            GlobalApplication.getMySelf().setFansCount(jsObject.getInt("fans_count"));
                            GlobalApplication.getMySelf().setActivitysCount(jsObject.getInt("activities_count"));
                            Intent intent = new Intent();    //登录成功，进入平台页面
                            intent.setClass(SplashActivity.this, MainNavigationActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            SplashActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(SplashActivity.this, "登录失败".toString(), Toast.LENGTH_SHORT).show();
                            toMainActivity();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showVolleyError(error);
                mProgressHUD.dismiss();
                Toast.makeText(SplashActivity.this, "登录超时请检查网络".toString(), Toast.LENGTH_SHORT).show();
                toMainActivity();
            }
        });
        executeRequest(mStringRequest);
        } else{
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
            toMainActivity();
        }
    }

    private void toMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }
    //进入登录界面
    private void goLogin() {
/*        Intent intent = new Intent(SplashActivity.this, NavigationActivity.class);
        SplashActivity.this.startActivity(intent);*/
        if(canAutoLogin()) {  //自动登录
            autoLogin();
        } else {    //填写账号密码，手动登录
            toMainActivity();
        }

        //开始轮询
        //PollingUtils.startPollingService(this, 5, PollingService.class, PollingService.ACTION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
