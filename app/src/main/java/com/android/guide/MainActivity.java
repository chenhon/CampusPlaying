package com.android.guide;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.android.R;
import com.android.bottomnavigation.MainNavigationActivity;
import com.android.tool.CyptoUtils;
import com.android.tool.MyStringRequest;
import com.android.tool.VolleyRequestParams;
import com.android.volley.Request;
import com.android.volley.Response;

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

    private String rootString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PREFS_ISSAVED_KEY = getResources().getString(R.string.PREFS_ISSAVED_KEY);   //相关字段获取
        PREFS_ACCOUNT_KEY = getResources().getString(R.string.PREFS_ACCOUNT_KEY);
        PREFS_PASSWORD_KEY = getResources().getString(R.string.PREFS_PASSWORD_KEY);
        DEC_KEY = getResources().getString(R.string.DEC_KEY);

        rootString = getResources().getString(R.string.ROOT);

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
              //  doLogin();
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MainNavigationActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //     intent.setClass(MainActivity.this, CommunicateActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
                MainActivity.this.finish();
            }
        });
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

    /**
     * 执行登录
     * 暂时没有做与服务器的交互
     */
    private void doLogin() {
        SaveAccountData(); //数据保存
        VolleyRequestParams params = new VolleyRequestParams().with("user",username.getText().toString())
                .with("password",password.getText().toString())
                .with("grant_type","password");
        executeRequest(new MyStringRequest(Request.Method.POST, rootString + "token", params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if ("null".equals(response) || null == response) {
                    //登录未获取响应，登录失败
                    return;
                }
                try {
                    Log.e("MainActivity:login:TAG", response);
                    JSONObject jsonObj = new JSONObject(response);
                    GlobalApplication.setToken(jsonObj.getString("access_token"));

                    handleResult(jsonObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },errorListener()));


    }

    private void handleResult(JSONObject jsonObj) {

        try {
            GlobalApplication.setUserId(jsonObj.getString("user_id"));//保存用户ID
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, MainNavigationActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        MainActivity.this.finish();
    }
}
