package com.android.person.edit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.guide.ActivityCollector;
import com.android.guide.BaseActivity;
import com.android.GlobalApplication;
import com.android.guide.MainActivity;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PasswordEdit extends BaseActivity {
    private static final int GO_LOGIN = 1;//进入用户登录页
    private static final int SPLASH_DELAY_MILLIS = 3000; //1s延时
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.tv_user)
    TextView mTvUser;
    @BindView(R.id.et_current_password)
    EditText mEtCurrentPassword;
    @BindView(R.id.et_new_password)
    EditText mEtNewPassword;
    @BindView(R.id.et_sure_password)
    EditText mEtSurePassword;
    @BindView(R.id.sure_btn)
    TextView mSureBtn;


    private String rootString;
    private NetworkConnectStatus networkStatus;//网络连接状态
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
        setContentView(R.layout.activity_password_edit);
        ButterKnife.bind(this);


        mTvUser.setText("用户名:" + GlobalApplication.getMySelf().getUser());
        rootString = getResources().getString(R.string.ROOT) + "user/~me";
        networkStatus = new NetworkConnectStatus(this);

        setListener();
    }

    private void setListener() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordEdit.this.finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
        mSureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doChange();
            }
        });
    }

    private String currentPassword, newPassword, surePassword;

    private boolean checkInputPassword() {
        //  Toast.makeText(this, getPassword(), Toast.LENGTH_SHORT).show();
        currentPassword = mEtCurrentPassword.getText().toString();
        if ((currentPassword == null) || (currentPassword.length() == 0)) {
            Toast.makeText(this, "请输入当前密码".toString(), Toast.LENGTH_SHORT).show();
            return false;
        }
        newPassword = mEtNewPassword.getText().toString();
        if ((newPassword == null) || (newPassword.length() == 0)) {
            Toast.makeText(this, "请输入新密码".toString(), Toast.LENGTH_SHORT).show();
            return false;
        }
        surePassword = mEtSurePassword.getText().toString();
        if ((surePassword == null) || (surePassword.length() == 0)) {
            Toast.makeText(this, "确认密码有误".toString(), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (currentPassword.equals(GlobalApplication.getPassword())
                && newPassword.equals(surePassword)) {
            return true;
        }
        Toast.makeText(this, "确认密码有误".toString(), Toast.LENGTH_SHORT).show();
        return false;
    }

    public void doChange() {

        if (!checkInputPassword()) {
            return;
        }
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(this, "修改中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });

            VolleyRequestParams bodyParams = new VolleyRequestParams()
                    .with("old_password", currentPassword)
                    .with("new_password", newPassword);

            VolleyRequestParams headerParams = new VolleyRequestParams()
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json

            System.out.println(GlobalApplication.getToken());
            MyStringRequest mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //记录用户信息
                            try {
                                Log.e("TextEdit:TAG", response);
                                mProgressHUD.dismiss();
                                GlobalApplication.setPassword(newPassword);
                                GlobalApplication.setToken("");
                                GlobalApplication.setUserId(0);
                                mHandler.sendEmptyMessageDelayed(GO_LOGIN, SPLASH_DELAY_MILLIS);
                                Toast.makeText(PasswordEdit.this, "修改成功，请重新登录".toString(), Toast.LENGTH_SHORT).show();
                                /*setResult(RESULT_OK);
                                finish();   //修改成功
                                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);*/
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Toast.makeText(PasswordEdit.this, "修改失败".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(400 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            GlobalApplication.get().getRequestQueue().add(mStringRequest);
        } else {
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    //进入登录界面
    private void goLogin() {
        Intent intent = new Intent(PasswordEdit.this, MainActivity.class);
        //Intent intent = new Intent(SplashActivity.this, CommunicateActivity.class);
        PasswordEdit.this.startActivity(intent);
        ActivityCollector.finishAll();

    }
}
