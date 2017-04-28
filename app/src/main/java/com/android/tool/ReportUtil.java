package com.android.tool;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import com.android.R;
import com.android.GlobalApplication;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

/**
 * 举报工具类
 */

public class ReportUtil {
    public static final int TYPE_USER = 0;
    public static final int TYPE_ACTIVITY = 1;
    public static final int TYPE_PICTURE = 2;
    public static final int TYPE_NOTIFICATION = 3;
    public static final int TYPE_COMMENT = 4;

    private ProgressHUD mProgressHUD;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private ReportDialog reportDialog ;
    private Activity mActivity;
    private int attachType;
    private int attachId;
    public ReportUtil(Activity activity, int attachType, int attachId){
        mActivity = activity;
        this.attachId = attachId;
        this.attachType = attachType;
        networkStatus = new NetworkConnectStatus(activity);//网络连接状态
    }
    public void doReport() {
        reportDialog = new ReportDialog(mActivity);
        reportDialog.setTitle("举报")
                .setHintText("请仔细填写，以确保举报能够被处理")
                .setOnBtnCommitClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

            String reportContent = reportDialog.getContent();
            if((reportContent == null)||(reportContent.length() == 0)) {
                Toast.makeText(mActivity, "内容不能为空".toString(), Toast.LENGTH_SHORT).show();
                return;
            }
            reportDialog.dismiss();
            if (networkStatus.isConnectInternet()) {
                mProgressHUD = ProgressHUD.show(mActivity, "提交中...", true, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mProgressHUD.dismiss();
                    }
                });
                String rootString = mActivity.getResources().getString(R.string.ROOT) + "admin/report";

                VolleyRequestParams bodyParams = new VolleyRequestParams()
                        .with("attach_type", String.valueOf(attachType))//举报类型
                        .with("attach_id", String.valueOf(attachId))//评论的id
                        .with("description", String.valueOf(reportDialog.getContent()));//举报内容

                VolleyRequestParams headerParams = new VolleyRequestParams()
                        .with("token", GlobalApplication.getToken())
                        .with("Accept", "application/json"); // 数据格式设置为json
                MyStringRequest mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if ("null".equals(response) || null == response) {
                                    //登录未获取响应，登录失败
                                    mProgressHUD.dismiss();
                                    Toast.makeText(mActivity, "提交失败".toString(), Toast.LENGTH_SHORT).show();

                                    return;
                                }
                                Toast.makeText(mActivity, "举报已提交,等待审核".toString(), Toast.LENGTH_SHORT).show();
                                mProgressHUD.dismiss();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mProgressHUD.dismiss();
                       /* Log.e("PublishActivity:TAG", error.getMessage(), error);
                        byte[] htmlBodyBytes = error.networkResponse.data;
                        Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);*/
                        Toast.makeText(mActivity, "提交失败".toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                mStringRequest.setRetryPolicy(new DefaultRetryPolicy(400*1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                GlobalApplication.get().getRequestQueue().add(mStringRequest);
            } else {
                Toast.makeText(mActivity, mActivity.getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
            }
        }
    })
    .show();
    }
}

