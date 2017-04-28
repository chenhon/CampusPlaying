package com.android.tool;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 说明：从手机相册获取照片，并且进行裁剪
 * Created by ChenHong on 2017/4/20 0020.
 */

public class PhotoFrmAlbum {
    private static final String IMAGE_FILE_LOCATION = "file:///sdcard/album_temp.jpg";//temp file
    private Uri imageUri = Uri.parse(IMAGE_FILE_LOCATION);//The Uri to store the big bitmap
    private Activity mActivity;
    private int mRequestCode;
    private int mCropCode;

    //不进行剪切
    public PhotoFrmAlbum(Activity activity, int requestCode){
        mActivity = activity;
        mRequestCode = requestCode;
    }
    //进行剪切
    public PhotoFrmAlbum(Activity activity, int requestCode, int cropCode){
        mActivity = activity;
        mRequestCode = requestCode;
        mCropCode = cropCode;
    }

    /**
     * 从相册中选择图片
     */
    public void choosePhotoFrmAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        mActivity.startActivityForResult(intent,mRequestCode);
    }

    public void cutImage(Intent data, int width, int height) {
        String path = com.android.tool.Util.getImageAbsolutePath(mActivity,data.getData());
/*        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < 24) {
            File file = new File(path);
            uri = Uri.fromFile(file);   //问题就在这个File Uri上面  ————代码语句A
        } else {     ////兼容android7.0
            //为什么Google需要对Content Uri和File Uri使用进行更改，应该是希望限制开发者对存储空间media文件的访问控制
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, path);
            uri = mActivity.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }*/
        Intent intent = new Intent("com.android.camera.action.CROP");  //裁剪
        intent.setDataAndType(getImageContentUri(mActivity,new File(path)),"image/*");
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

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
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
     * 获取未经剪切的图片
     * @return
     */
    public Bitmap getImage(Intent data) {
        Uri uri= data.getData();
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
