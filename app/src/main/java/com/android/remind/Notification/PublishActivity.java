package com.android.remind.notification;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.guide.BaseActivity;
import com.android.GlobalApplication;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.VolleyRequestParams;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PublishActivity extends BaseActivity {


    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.publish_btn)
    Button mPublishBtn;
    @BindView(R.id.notification_title)
    EditText mNotificationTitle;
    @BindView(R.id.notification_content)
    EditText mNotificationContent;
    private String rootString;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private ProgressHUD mProgressHUD;
    private MyStringRequest mStringRequest;
    private int aid;

    /**
     * 启动该活动
     *
     * @param activity
     */
    public static void startActivity(Activity activity, int aid) {
        Bundle bundle1 = new Bundle();
        bundle1.putInt("aid", aid); //传递活动id
        Intent intent = new Intent(activity, PublishActivity.class);
        intent.putExtras(bundle1);  //传入详细信息
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_edit);
        ButterKnife.bind(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            this.aid = bundle.getInt("aid");//活动id
            rootString = getResources().getString(R.string.ROOT) + "activity/" + aid + "/notification";
        }

        networkStatus = new NetworkConnectStatus(this);
        setListener();
    }

    private void setListener() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mPublishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPublish();
            }
        });
    }

    private void doPublish() {
        final String title;
        title = mNotificationTitle.getText().toString();
        if (null == title || "".equals(title)) {
            Toast.makeText(this, "请填写通知标题", Toast.LENGTH_SHORT).show();
            return;
        }
        final String content;
        content = mNotificationContent.getText().toString();
        if (null == content || "".equals(content)) {
            Toast.makeText(this, "请填写通知内容", Toast.LENGTH_SHORT).show();
            return;
        }

        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(PublishActivity.this, "发布中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });

            VolleyRequestParams bodyParams = new VolleyRequestParams()
                    .with("title", title)
                    .with("content", content);

            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if ("null".equals(response) || null == response) {
                                //登录未获取响应，登录失败
                                mProgressHUD.dismiss();
                                Toast.makeText(PublishActivity.this, "发布失败".toString(), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            try {
                                Log.e("NotificationEdit:TAG", response);
                                Toast.makeText(PublishActivity.this, "发布成功".toString(), Toast.LENGTH_SHORT).show();
                                mProgressHUD.dismiss();
                                PublishActivity.this.finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Log.e("PublishActivity:TAG", error.getMessage(), error);
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                    Toast.makeText(PublishActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);

        }
    }
}
