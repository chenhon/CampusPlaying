package com.android.guide;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.android.GlobalApplication;
import com.android.R;
import com.android.bottomnavigation.MainNavigationActivity;
import com.android.tool.CyptoUtils;
import com.android.tool.MyImageRequest;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.VolleyRequestParams;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 登陆界面
 */
public class MainActivity extends BaseActivity {

    private static final String PREFS_NAME = "UserInfo";//SharedPreferences的名称
    private String PREFS_ISSAVED_KEY;
    private String PREFS_ACCOUNT_KEY;
    private String PREFS_PASSWORD_KEY;
    private String DEC_KEY;

    private EditText username, password;
    private CheckBox statusSave;
    private Button loginButton;
    private Button registerButton;

    private String rootString;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private ProgressHUD mProgressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PREFS_ISSAVED_KEY = getResources().getString(R.string.PREFS_ISSAVED_KEY);   //相关字段获取
        PREFS_ACCOUNT_KEY = getResources().getString(R.string.PREFS_ACCOUNT_KEY);
        PREFS_PASSWORD_KEY = getResources().getString(R.string.PREFS_PASSWORD_KEY);
        DEC_KEY = getResources().getString(R.string.DEC_KEY);

        rootString = getResources().getString(R.string.ROOT)+"token";
        networkStatus = new NetworkConnectStatus(this);

        initView();
        loadAccountData();
        setListener();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        username = (EditText)findViewById(R.id.user_name);
        password = (EditText)findViewById(R.id.pass_word);
        statusSave = (CheckBox)findViewById(R.id.status_save);
        loginButton = (Button)findViewById(R.id.login_but);
        registerButton = (Button)findViewById(R.id.register_but);
    }

    /**
     * 载入已记录的账号数据
     */
    private void loadAccountData() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userPassword = sp.getString(PREFS_PASSWORD_KEY, "");
        String userName = sp.getString(PREFS_ACCOUNT_KEY, "");
        if (sp.getBoolean(PREFS_ISSAVED_KEY, false)) { //之前保存了登录信息
            password.setText(CyptoUtils.decode(DEC_KEY, userPassword));
            username.setText(CyptoUtils.decode(DEC_KEY, userName));
            statusSave.setChecked(true);
            statusSave.setTextColor(getResources().getColor(R.color.login_saved));
        }else {
            username.setText(CyptoUtils.decode(DEC_KEY, userName));
            statusSave.setChecked(false);
            statusSave.setTextColor(getResources().getColor(R.color.login_unsaved));
        }

        //免登录用
        String token = GlobalApplication.getToken();
        int uid = GlobalApplication.getUserId();
        if((uid != 0)&& (!"".equals(token))) {
            GlobalApplication.getMySelf().setId(uid);//保存用户ID
            getUserInfo(uid);
            mProgressHUD = ProgressHUD.show(MainActivity.this, "登录中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });
        }
    }

    /**
     * 保存账号信息
     */
    private void SaveAccountData() {
        // 载入配置文件
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // 写入配置文件
        SharedPreferences.Editor spEd = sp.edit();
        spEd.putBoolean(PREFS_ISSAVED_KEY, statusSave.isChecked());
        if (statusSave.isChecked()) {
            //保存账号与密码
            spEd.putString(PREFS_PASSWORD_KEY, CyptoUtils.encode(DEC_KEY, password.getText().toString()));
            spEd.putString(PREFS_ACCOUNT_KEY, CyptoUtils.encode(DEC_KEY, username.getText().toString()));
        } else {
            //只保存密码
            spEd.putString(PREFS_PASSWORD_KEY, CyptoUtils.encode(DEC_KEY, ""));
            spEd.putString(PREFS_ACCOUNT_KEY, CyptoUtils.encode(DEC_KEY, username.getText().toString()));
        }
        spEd.commit();
    }

    private void setListener() {

        //设置checkbox的点击事件
        statusSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) { //改变字体颜色
                    buttonView.setTextColor(getResources().getColor(R.color.login_saved));
                }else {
                    buttonView.setTextColor(getResources().getColor(R.color.login_unsaved));
                }
            }
        });

        //设置登录按钮点击事件
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.startActivity(MainActivity.this);
            }
        });
    }


    /**
     * 执行登录
     * 暂时没有做与服务器的交互
     */
    private void doLogin() {
        SaveAccountData(); //数据保存
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(MainActivity.this, "登录中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });
            VolleyRequestParams bodyParams = new VolleyRequestParams()
                    .with("user", username.getText().toString())
                    .with("password", password.getText().toString())
                    .with("grant_type", "password");
            VolleyRequestParams headerParams = new VolleyRequestParams()
                   // .with("token", GlobalApplication.getToken())
                    .with("Accept","application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                    new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if ("null".equals(response) || null == response) {
                        //登录未获取响应，登录失败
                        Toast.makeText(MainActivity.this, "登录失败".toString(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        Log.e("MainActivity:login:TAG", response);
                        GlobalApplication.setPassword(password.getText().toString()); //密码保存在本地
                        handleResult(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, errorListener());
            executeRequest(mStringRequest);
//            mQueue = GlobalApplication.get().getRequestQueue();
//            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
//            mQueue.add(mStringRequest);
        } else{
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleResult(String response) {
        try {
            JSONObject jsonObj = new JSONObject(response);
            //Toast.makeText(MainActivity.this, "user_id"+jsonObj.getInt("user_id"), Toast.LENGTH_SHORT).show();
            GlobalApplication.setToken(jsonObj.getString("access_token"));//保存token
            GlobalApplication.setUserId(jsonObj.getInt("user_id"));
            GlobalApplication.getMySelf().setId(jsonObj.getInt("user_id"));//保存用户ID
            //存储在本地则可以做免登录处理
            getUserInfo(jsonObj.getInt("user_id"));

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "登录失败".toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取账号信息
     * @param userId
     */
    private void getUserInfo(int userId) {
        VolleyRequestParams headerParams = new VolleyRequestParams()
                .with("token", GlobalApplication.getToken())
                .with("Accept","application/json"); // 数据格式设置为json
        //Toast.makeText(MainActivity.this, "getUserInfo", Toast.LENGTH_SHORT).show();
        mStringRequest = new MyStringRequest(Request.Method.GET, getResources().getString(R.string.ROOT)+"user/"+userId, headerParams, null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    try {
                        Log.e("MainActivity:login:TAG", response);
                        JSONObject jsObject= new JSONObject(response);
                        GlobalApplication.getMySelf().setDescription(jsObject.getString("description"));
                        GlobalApplication.getMySelf().setUser(jsObject.getString("user"));
                        GlobalApplication.getMySelf().setName(jsObject.getString("name"));
                        GlobalApplication.getMySelf().setAvatar(jsObject.getInt("avatar"));
                        GlobalApplication.getMySelf().setGender(jsObject.getInt("gender"));
                        GlobalApplication.getMySelf().setFollowersCount(jsObject.getInt("followers_count"));
                        GlobalApplication.getMySelf().setFansCount(jsObject.getInt("fans_count"));
                        GlobalApplication.getMySelf().setActivitysCount(jsObject.getInt("activities_count"));
                       // GlobalApplication.setMyAvatar(mUploadAvatar);
                        getAvatar(jsObject.getInt("avatar"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "登录失败1".toString(), Toast.LENGTH_SHORT).show();
                    }
                    }
                }, errorListener());
        executeRequest(mStringRequest);
    }

    /**
     * 获取用户头像
     * @param mediaId
     */
    private void getAvatar(int mediaId) {
        mQueue = GlobalApplication.get().getRequestQueue();
        MyImageRequest avatarImageRequest = new MyImageRequest(
                getResources().getString(R.string.ROOT) + "media/" + mediaId
                , new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                try {
                    mProgressHUD.dismiss();
                    GlobalApplication.setMyAvatar(response);
                    //应该有一个错误提醒字段
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, MainNavigationActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    MainActivity.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "登录失败2".toString(), Toast.LENGTH_SHORT).show();

                }
            }
        }, 0, 0, Bitmap.Config.RGB_565
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "登录失败3".toString(), Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(avatarImageRequest);
    }

    /**
     * 功能描述： onTouchEvent事件中，点击android系统的软键盘外的其他地方，可隐藏软键盘，以免遮挡住输入框
     * @param event
     *            当前的触控事件
     * @return boolean类型的标记位。当用户点击后才会隐藏软键盘。
     */
    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        return imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }
}
