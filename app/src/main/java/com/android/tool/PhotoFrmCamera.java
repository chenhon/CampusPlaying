package com.android.tool;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.android.GlobalApplication;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by ChenHong on 2017/4/25 0025.
 */

public class PhotoFrmCamera {
    public File tempFile;
    private static final String IMAGE_FILE_LOCATION = "file:///sdcard/camera_temp.jpg";//temp file
    private Uri imageUri = Uri.parse(IMAGE_FILE_LOCATION);//The Uri to store the big bitmap
    private Uri mUriCamera;    //存放拍照的图片
    private Activity mActivity;
    private int mRequestCode;
    private int mCropCode;

    //不进行剪切
    public PhotoFrmCamera(Activity activity, int requestCode){
        mActivity = activity;
        mRequestCode = requestCode;
    }

    //进行剪切
    public PhotoFrmCamera(Activity activity, int requestCode, int cropCode){
        mActivity = activity;
        mRequestCode = requestCode;
        mCropCode = cropCode;
    }
    /**
     * 拍照图片
     */
    public void getPhotoFrmCamera(){
        //獲取系統版本
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        // 激活相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断存储卡是否可以用，可用进行存储
        if (hasSdcard()) {
            String filename = DataUtils.stampToDate(DataUtils.DATA_TYPE7, DataUtils.getCurrentTime());
            tempFile = new File(Environment.getExternalStorageDirectory(),
                    filename + ".jpg");
            if (currentapiVersion < 24) {
                // 从文件中创建uri
                mUriCamera = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriCamera);
            } else {
                //兼容android7.0 使用共享文件的形式
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, tempFile.getAbsolutePath());
                mUriCamera = GlobalApplication.get().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriCamera);
            }
        }
        mActivity.startActivityForResult(intent, mRequestCode);
    }
    /**
     * 打开相机拍照
     *
     * @param activity
     */
    public void openCamera(Activity activity, int requestCode) {
        //獲取系統版本
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        // 激活相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断存储卡是否可以用，可用进行存储
        if (hasSdcard()) {
            String filename = DataUtils.stampToDate(DataUtils.DATA_TYPE7, DataUtils.getCurrentTime());
            tempFile = new File(Environment.getExternalStorageDirectory(),
                    filename + ".jpg");
            if (currentapiVersion < 24) {
                // 从文件中创建uri
                Uri uri = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            } else {
                //兼容android7.0 使用共享文件的形式
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, tempFile.getAbsolutePath());
                Uri uri = GlobalApplication.get().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            }
        }
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CAREMA
        activity.startActivityForResult(intent, requestCode);
    }
    public void cutImage(int width, int height) {
        Intent intent = new Intent("com.android.camera.action.CROP");  //裁剪
        intent.setDataAndType(mUriCamera,"image/*");
        intent.putExtra("crop", "true");
        if(width == height){
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        } else {
            intent.putExtra("aspectX", 4);
            intent.putExtra("aspectY", 3);
        }

        intent.putExtra("outputX", width); //裁剪获得的图片宽度（像素点）
        intent.putExtra("outputY", height);  //高度
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        mActivity.startActivityForResult(intent,mCropCode);
    }
    /**
     * 获取经剪切的图片
     * @return
     */
    public Bitmap getCutImage() {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(mActivity.getContentResolver().openInputStream(imageUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    /**
     * 判断sdcard是否被挂载
     */
    public boolean hasSdcard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
    /**
     * 获取未经剪切的图片
     * @return
     */
    public Bitmap getImage() {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(mActivity.getContentResolver().openInputStream(mUriCamera));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
