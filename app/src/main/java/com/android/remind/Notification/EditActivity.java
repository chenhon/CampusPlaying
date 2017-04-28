package com.android.remind.notification;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 通知修改界面
 */
public class EditActivity extends AppCompatActivity {


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
    private int nid;    //通知id
    private String originalTitle;  //原标题
    private String originalContent;//原内容

    /**
     * 启动活动编辑
     *
     * @param activity
     * @param nid      通知id
     * @param title    通知标题
     * @param content  通知内容
     */
    public static void startActivity(Activity activity, int nid, String title, String content) {
        Bundle bundle = new Bundle();
        bundle.putInt("nid", nid);
        bundle.putString("title", title);
        bundle.putString("content", content);
        Intent intent = new Intent(activity, EditActivity.class);
        intent.putExtras(bundle);
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
            this.nid = bundle.getInt("nid");
            this.originalTitle = bundle.getString("title");
            this.originalContent = bundle.getString("content");

        }
        rootString = getResources().getString(R.string.ROOT) + "notification/" + nid;
        mTitle.setText("修改通知");
        mPublishBtn.setText("确定");
        mNotificationTitle.setText(originalTitle);
        mNotificationContent.setText(originalContent);
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
        if (title.equals(originalTitle) && content.equals(originalContent)) {
            Toast.makeText(this, "通知未进行修改", Toast.LENGTH_SHORT).show();
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
                    .with("title", title)
                    .with("content", content);

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
                                Toast.makeText(EditActivity.this, "修改成功".toString(), Toast.LENGTH_SHORT).show();
                                /*setResult(RESULT_OK);*/
                                finish();   //修改成功
                                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Toast.makeText(EditActivity.this, "修改失败，请稍后再试".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(400 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            GlobalApplication.get().getRequestQueue().add(mStringRequest);
        } else {
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

}


