package com.android.tool;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.android.R;
import com.android.GlobalApplication;
import com.android.volley.Response;

import org.json.JSONObject;

import java.io.File;

/**
 * 说明：上传照片
 * Created by ChenHong
 */

public class UploadImageUtil {
    private ProgressHUD mProgressHUD;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private Activity mActivity;
    private Bitmap mUploadBitmap;
    private CallBackWithMediaId myCallBackWithMediaId;

    public UploadImageUtil(Activity activity, Bitmap bitmap, CallBackWithMediaId callBackWithMediaId){
        mActivity = activity;
        mUploadBitmap = bitmap;
        myCallBackWithMediaId = callBackWithMediaId;
        networkStatus = new NetworkConnectStatus(activity);//网络连接状态
    }
    public void upLoadImage() {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(mActivity, "上传中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });
            MultipartRequest multipartRequest = new MultipartRequest(
                    mActivity.getResources().getString(R.string.ROOT) + "media", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("UPLODE_TAG", " response : " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        int mediaId = jsonObject.getInt("media_id");
                        myCallBackWithMediaId.handlerWithMediaId(mediaId);
                        mProgressHUD.dismiss();

                    } catch (Exception e) {
                        mProgressHUD.dismiss();
                        Toast.makeText(mActivity, "上传失败".toString(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
            multipartRequest.addHeader("Accept", "application/json");
            // 通过MultipartEntity来设置参数
            MultipartEntity multi = multipartRequest.getMultiPartEntity();
            File file = new File(mActivity.getExternalCacheDir(), "my_upload_image.jpg");//将要保存图片的路径
            ImageUtils.bitmapToFile(file, mUploadBitmap);//将bitmap转换成文件
            multi.addFilePart("file", file);
            GlobalApplication.get().getRequestQueue().add(multipartRequest);
        } else {
            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }

    }

     public interface CallBackWithMediaId{
        void handlerWithMediaId(int mediaId);
    }
}
