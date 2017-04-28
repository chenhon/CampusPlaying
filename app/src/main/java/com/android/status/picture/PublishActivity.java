package com.android.status.picture;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.GlobalApplication;
import com.android.R;
import com.android.guide.BaseActivity;
import com.android.tool.BottomPopSelectMenu;
import com.android.tool.ImageUtils;
import com.android.tool.MultipartEntity;
import com.android.tool.MultipartRequest;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.PhotoFrmAlbum;
import com.android.tool.PhotoFrmCamera;
import com.android.tool.ProgressHUD;
import com.android.tool.VolleyRequestParams;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PublishActivity extends BaseActivity {
    private static final int GET_PHOTO_FRM_CAMERA = 0; //拍照获取照片
    private static final int GET_PHOTO_FRM_ALBUM = 1;  //相册获取照片
    @BindView(R.id.cancel)
    TextView mCancel;
    @BindView(R.id.page_title)
    TextView mPageTitle;
    @BindView(R.id.post)
    TextView mPost;
    @BindView(R.id.picture_description)
    EditText mPictureDescription;
    @BindView(R.id.add_picture)
    TextView mAddPicture;
    @BindView(R.id.picture)
    ImageView mPicture;
    @BindView(R.id.rootview)
    LinearLayout mRootview;

    private BottomPopSelectMenu bottomPopMenu;
    private String rootString;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private ProgressHUD mProgressHUD;
    private Bitmap upLoadPicture;

    private Boolean isUploadPhoto = false;
    private int aid;//活动的id
    private PhotoFrmAlbum mPhotoFrmAlbum;
    private PhotoFrmCamera mPhotoFrmCamera;

    /**
     *
     * @param activity
     * @param aid  活动的id
     */
    public static void startActivity(Activity activity, int aid) {
        Bundle bundle1 = new Bundle();
        bundle1.putInt("aid", aid); //传递活动id
        Intent intent = new Intent(activity, PublishActivity.class);
        intent.putExtras(bundle1);  //传入详细信息
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_picture);
        ButterKnife.bind(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            this.aid = bundle.getInt("aid");//活动id
        }
        mPhotoFrmAlbum = new PhotoFrmAlbum(this, GET_PHOTO_FRM_ALBUM);
        mPhotoFrmCamera = new PhotoFrmCamera(this, GET_PHOTO_FRM_CAMERA);
        rootString = getResources().getString(R.string.ROOT) + "activity/" + aid + "/photo";
        networkStatus = new NetworkConnectStatus(this);
        mQueue = GlobalApplication.get().getRequestQueue();
        setListener();
    }
    private void setListener() {
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPublish();
            }
        });

        mAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(PublishActivity.this.getCurrentFocus().getWindowToken(), 0);
                bottomPopMenu = new BottomPopSelectMenu(PublishActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.pop_select_fr_phone:
                               // Toast.makeText(PublishActivity.this, "从手机中选择图片", Toast.LENGTH_SHORT).show();
                                //openAlbum();//从相册中获取图片
                              //  GetPhotoFrmAlbum.openAlbum(PublishActivity.this, CHOOSE_PHOTO);
                                mPhotoFrmAlbum.choosePhotoFrmAlbum();
                                break;
                            case R.id.pop_take_photo:
                                //Toast.makeText(PublishActivity.this, "拍照获取图片", Toast.LENGTH_SHORT).show();
                                // openCamera(); //拍照获取图片
                             //   GetPhotoFrmCamera.openCamera(PublishActivity.this, TAKE_PHOTO);
                                mPhotoFrmCamera.getPhotoFrmCamera();
                                break;
                        }
                    }
                });
                bottomPopMenu.show();
            }
        });
    }
    /**
     * 判断发布内容是否完整
     */
    private Boolean isContentsIntact() {
        String description = mPictureDescription.getText().toString();
        if (description == null || "".equals(description)) {
            //   if (isRemind) {
            Toast.makeText(this, "情描述照片！", Toast.LENGTH_SHORT).show();
            //    }
            return false;
        }
        if (!isUploadPhoto) {
            Toast.makeText(this, "请上传图片！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void doPublish() {
        if (!isContentsIntact()) {
            return;
        }
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(PublishActivity.this, "图片上传中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });
            File file = new File(getExternalCacheDir(), "my_Picture.jpg");//将要保存图片的路径
            ImageUtils.bitmapToFile(file, upLoadPicture);

            mQueue = GlobalApplication.get().getRequestQueue();
            MultipartRequest multipartRequest = new MultipartRequest(
                    getResources().getString(R.string.ROOT) + "media", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("PublishPicture:TAG", " response : " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        int mediaId = jsonObject.getInt("media_id");

                        VolleyRequestParams bodyParams = new VolleyRequestParams()
                                .with("media_id", String.valueOf(mediaId))//图片的id
                                .with("description", String.valueOf(mPictureDescription.getText().toString()));

                        VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                                .with("token", GlobalApplication.getToken())
                                .with("Accept", "application/json"); // 数据格式设置为json
                        mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            Log.d("PublishPicture:TAG", response);
                                            mProgressHUD.dismiss();
                                            Toast.makeText(PublishActivity.this, "图片上传成功".toString(), Toast.LENGTH_SHORT).show();
                                            PublishActivity.this.finish();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                           /* Log.e("PublishActivity:TAG", error.getMessage(), error);
                                            byte[] htmlBodyBytes = error.networkResponse.data;
                                            Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);*/
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                mProgressHUD.dismiss();
                                Log.e("PublishPicture:TAG", error.getMessage(), error);
                                byte[] htmlBodyBytes = error.networkResponse.data;
                                Log.e("PublishPicture:TAG", new String(htmlBodyBytes), error);
                                Toast.makeText(PublishActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        executeRequest(mStringRequest);
                    } catch (Exception e) {
                        mProgressHUD.dismiss();
                        Toast.makeText(PublishActivity.this, "图片上传失败".toString(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
            multipartRequest.addHeader("Accept", "application/json");
            // 通过MultipartEntity来设置参数
            MultipartEntity multi = multipartRequest.getMultiPartEntity();
            multi.addFilePart("file", file);
            mQueue.add(multipartRequest);
        } else {
            Toast.makeText(PublishActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 运行时权限申请回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:       //打开相册会有权限的申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 //   GetPhotoFrmAlbum.openAlbumForResult(PublishActivity.this, CHOOSE_PHOTO);
                } else {
                    Toast.makeText(this, "You denied the permission that opening album", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GET_PHOTO_FRM_ALBUM:
                if(resultCode == RESULT_OK) { //相册选择照片
                    //;(1280, 960);
                    isUploadPhoto = true;
                    upLoadPicture = ImageUtils.comp(mPhotoFrmAlbum.getImage(data),0,1280,960);//对获取的图片进行压缩
                    mPicture.setImageBitmap(upLoadPicture);
                } else {
                    Toast.makeText(this, "你没有选择照片", Toast.LENGTH_SHORT).show();
                }
                break;
            case GET_PHOTO_FRM_CAMERA:
                if(resultCode == RESULT_OK) { //骗招获取照片
                    //;(1280, 960);
                    isUploadPhoto = true;
                    upLoadPicture = ImageUtils.comp(mPhotoFrmCamera.getImage(),0,1280,960);//对获取的图片进行压缩
                    mPicture.setImageBitmap(upLoadPicture);
                } else {
                    Toast.makeText(this, "你没有拍照", Toast.LENGTH_SHORT).show();
                }
                break;
           /* case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    isUploadPhoto = true;
                    upLoadPicture = ImageUtils.comp(GetPhotoFrmCamera.getPhoto(PublishActivity.this),400,800,480);//对获取的图片进行压缩
                    mPicture.setImageBitmap(upLoadPicture);
                    //  addPicture(GetPhotoFrmCamera.getPhoto(PublishActivity.this));
                } else {
                    Toast.makeText(this, "你没有拍照", Toast.LENGTH_SHORT).show();
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    isUploadPhoto = true;
                    upLoadPicture = ImageUtils.comp(GetPhotoFrmAlbum.getPhoto(PublishActivity.this, data),400,800,480);//对获取的图片进行压缩
                    mPicture.setImageBitmap(upLoadPicture);
                    //  addPicture(GetPhotoFrmAlbum.getPhoto(PublishActivity.this, data));
                } else {
                    Toast.makeText(this, "你没有选择任何照片", Toast.LENGTH_SHORT).show();
                }
                break;*/
            default:
                break;
        }
    }

    /**
     * 功能描述： onTouchEvent事件中，点击android系统的软键盘外的其他地方，可隐藏软键盘，以免遮挡住输入框
     *
     * @param event 当前的触控事件
     * @return boolean类型的标记位。当用户点击后才会隐藏软键盘。
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //mPageTitle.requestFocus();
       // Toast.makeText(this, "点击软键盘以外部分", Toast.LENGTH_SHORT).show();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        return imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }
}
