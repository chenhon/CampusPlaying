package com.android.tool;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.android.GlobalApplication;
import com.android.volley.RequestQueue;
import com.android.volley.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Administrator on 2017/3/9 0009.
 */

public class GetPhotoFrmCamera {
    /**
     * 不需要权限申请
     * 拍照来获取图片，
     * 首先申明一个存放图片的路径
     * 在打开拍照程序完成拍照
     * 照片结果回调 onActivityResult 函数来完成相关操作
     */
    private static Uri imageUri;

    private static File outputImage;
    public static void openCamera(Activity activity, int requestCode) {
        outputImage = new File(activity.getExternalCacheDir(),"output_image.jpg"); //创建file对象
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch(IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) { //可以选择性的将封装过得Uri共享给外部，是特殊的内容提供器
            imageUri = FileProvider.getUriForFile(activity,//内容提供器
                    "com.android", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage); //低版本直接将file转换成Uri对象
        }
        //启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        activity.startActivityForResult(intent, requestCode);
    }
    public static Bitmap getPhoto(Activity activity) {
        try {
            Bitmap bitMap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(imageUri));

            RequestQueue mQueue = GlobalApplication.get().getRequestQueue();
            MultipartRequest multipartRequest = new MultipartRequest(
                    "http://develop.doris.work:8888/media", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("UPLODE_TAG", " response : " + response);
                }

            });
            // 通过MultipartEntity来设置参数
            MultipartEntity multi = multipartRequest.getMultiPartEntity();
            multi.addFilePart("file", outputImage);

            // 将请求添加到队列中
            mQueue.add(multipartRequest);
            return bitMap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
