package com.android.guide;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.android.tool.RequestManager;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
public class BaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("BaseActivity",getClass().getSimpleName());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityCollector.addActivity(this);
    }
    /**
     * 停止网络请求
     */
    @Override
    public void onStop() {
        super.onStop();
        RequestManager.cancelAll(this);
    }
    /**
     * 开始网络请求
     */
    public void executeRequest(Request<?> request) {
        RequestManager.addRequest(request, this);
    }

    public Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
				/*NetworkConnectStatus networkConnectStatus = new NetworkConnectStatus(activity);
				if (networkConnectStatus.isConnectInternet()) {
				Toast.makeText(activity, "网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
			}else 	*/
                Toast.makeText(BaseActivity.this, "请检查网络连接", Toast.LENGTH_SHORT).show();
                Log.e("getTIMELINE:TAG", error.getMessage(), error);
                byte[] htmlBodyBytes = error.networkResponse.data;
                Log.e("getTIMELINE:TAG", new String(htmlBodyBytes), error);
                //}
            }
        };
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

}