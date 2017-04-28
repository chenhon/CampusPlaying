package com.android.person;


import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.R;
import com.android.GlobalApplication;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

/**
 * 跳转个人主页实现类
 */

public class PersonOnClickListenerImpl implements View.OnClickListener {

    private Activity mActivity;
    private int uid;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private String rootString;
    private ProgressHUD mProgressHUD;

    public PersonOnClickListenerImpl(Activity activity, int uid){
        this.mActivity = activity;
        this.uid = uid;
        networkStatus = new NetworkConnectStatus(activity);
    }

    @Override
    public void onClick(View v) {
        rootString = mActivity.getResources().getString(R.string.ROOT)+"user/"+uid;
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(mActivity, "加载中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });

            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            MyStringRequest stringRequest = new MyStringRequest(Request.Method.GET, rootString, headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getPerson:TAG", response);
                            PersonActivity.startActivity(mActivity, response);
                            mProgressHUD.dismiss();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mProgressHUD.dismiss();
                           Toast.makeText(mActivity, "加载失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        }
                    });
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            GlobalApplication.get().getRequestQueue().add(stringRequest);
        }
    }
}
