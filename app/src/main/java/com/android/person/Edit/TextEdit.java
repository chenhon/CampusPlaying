package com.android.person.edit;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TextEdit extends AppCompatActivity {

    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.sure)
    TextView mSure;
    @BindView(R.id.et)
    EditText mEt;
    @BindView(R.id.sure_btn)
    TextView mSureBtn;
    private String rootString;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private ProgressHUD mProgressHUD;

    private String mEditTitle;
    private String mEditContent;

    /**
     * 启动该活动
     *
     * @param activity
     * @param title    标题  （修改昵称、   修改签名）
     * @param content  内容
     */
    public static void startActivityForResult(Activity activity, String title, String content, int requestCode) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title); //标题
        bundle.putString("content", content); //内容
        Intent intent = new Intent(activity, TextEdit.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        // activity.startActivity(intent);
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_edit);
        ButterKnife.bind(this);

        setResult(RESULT_OK);   //一定要放在finish前面
        Bundle bundle = this.getIntent().getExtras();
        mEditTitle = bundle.getString("title");
        mTitle.setText(mEditTitle);
        mEditContent = bundle.getString("content");
        mEt.setText(mEditContent);
        mEt.setSelection(mEditContent.length());

        rootString = getResources().getString(R.string.ROOT) + "user/~me";
        networkStatus = new NetworkConnectStatus(this);

        setListener();
    }

    private void setListener() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextEdit.this.finish();
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

    public void doChange() {
        String content = mEt.getText().toString();
        if ((content == null) || (content.length() == 0)) {
            Toast.makeText(this, "内容不能为空".toString(), Toast.LENGTH_SHORT).show();
            return;
        } else if (content.equals(mEditContent)) {//未改变信息
            return;
        }
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(this, "保存中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });

            VolleyRequestParams bodyParams = new VolleyRequestParams();
            if ("修改昵称".equals(mEditTitle)) {
                bodyParams.with("name", content);//修改昵称
            } else if ("修改签名".equals(mEditTitle)) {
                bodyParams.with("description", content);//修改签名
            }

            VolleyRequestParams headerParams = new VolleyRequestParams()
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            MyStringRequest mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //记录用户信息
                            try {
                                Log.e("TextEdit:TAG", response);
                                mProgressHUD.dismiss();

                                JSONObject jsObject = new JSONObject(response);
                                GlobalApplication.getMySelf().setDescription(jsObject.getString("description")); //记录修改后的个人签名和昵称
                                GlobalApplication.getMySelf().setName(jsObject.getString("name"));
                               // setResult(RESULT_OK);
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
                    Toast.makeText(TextEdit.this, "保存失败".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(400 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            GlobalApplication.get().getRequestQueue().add(mStringRequest);
        } else {
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }


}
