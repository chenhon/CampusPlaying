package com.android.tool;

import android.util.Log;
import android.widget.Toast;

import com.android.GlobalApplication;
import com.android.volley.VolleyError;

/**
 * Created by Administrator on 2017/4/28 0028.
 */

public class HandleVolleyError {
    public static void showVolleyError(VolleyError error) {
        Toast.makeText(GlobalApplication.get(), "请检查网络连接", Toast.LENGTH_SHORT).show();
        // Log.e("getTIMELINE:TAG", error.getMessage(), error);
        if(error.networkResponse != null){
            byte[] htmlBodyBytes = error.networkResponse.data;
            Log.e("ErrorCode:TAG", "" + error.networkResponse.statusCode);
            Log.e("VOlleyError:TAG", new String(htmlBodyBytes), error);

        }
    }
}
