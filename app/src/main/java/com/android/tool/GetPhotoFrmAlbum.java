package com.android.tool;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/3/9 0009.
 */

public class GetPhotoFrmAlbum {
    /**
     * 从手机相册中获取图片
     * 打开相册需要 授权程序SD卡读写的权利
     * 首先判断是否得到授权，得到则直接打开相册进行处理， 结果回调onActivityResult 函数来完成相关操作
     * 否则进行运行时权限请求，权限授权情况由回调 onRequestPermissionsResult处理
     */
    public static void openAlbum(Activity activity, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,        //运行时权限申请
                    new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
        } else { //已经获取到权限
            openAlbumForResult(activity, requestCode);
        }
    }


    public static void openAlbumForResult(Activity activity, int requestCode) {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }
    public static Bitmap getPhoto(Activity activity, Intent data) {
        //判断手机版本
        if (Build.VERSION.SDK_INT >= 19) {
            //4.4以上系统
            return handleImageOnKitKat(activity, data);
        } else {
            return handleImageBeforeKitKat(activity, data);
        }
    }
    @TargetApi(19)
    private static Bitmap handleImageOnKitKat(Activity activity, Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(activity, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(activity, contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(activity, uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        //displayImage(imagePath); // 根据图片路径显示图片
        if (imagePath != null) {
            Bitmap bitMap = BitmapFactory.decodeFile(imagePath);
            return bitMap;
        } else {
            Toast.makeText(activity, "获取图片失败", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private static Bitmap handleImageBeforeKitKat(Activity activity, Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(activity, uri, null);//根据Uri查询到图片数据
        //displayImage(imagePath);
        if (imagePath != null) {
            Bitmap bitMap = BitmapFactory.decodeFile(imagePath);//解析成位图
            return bitMap;
        } else {
            Toast.makeText(activity, "获取图片失败", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private static String getImagePath(Activity activity, Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = activity.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
