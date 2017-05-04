package com.android.login;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.BaseActivity;
import com.android.GlobalApplication;
import com.android.R;
import com.android.bottomnavigation.MainNavigationActivity;
import com.android.tool.BottomPopSelectMenu;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.PhotoFrmAlbum;
import com.android.tool.PhotoFrmCamera;
import com.android.tool.PictureView;
import com.android.tool.ProgressHUD;
import com.android.tool.UploadImageUtil;
import com.android.tool.VolleyRequestParams;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 用户名
 * 昵称
 * 密码
 * 性别
 * 头像
 * 描述
 */

public class RegisterActivity extends BaseActivity {

    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.tvUser)
    TextView mTvUser;
    @BindView(R.id.etUser)
    EditText mEtUser;
    @BindView(R.id.tvName)
    TextView mTvName;
    @BindView(R.id.etName)
    EditText mEtName;
    @BindView(R.id.tvPassword)
    TextView mTvPassword;
    @BindView(R.id.etPassword)
    EditText mEtPassword;
    @BindView(R.id.tvPasswordConfirm)
    TextView mTvPasswordConfirm;
    @BindView(R.id.etPasswordConfirm)
    EditText mEtPasswordConfirm;
    @BindView(R.id.tvDescription)
    TextView mTvDescription;
    @BindView(R.id.edDescription)
    EditText mEdDescription;
    @BindView(R.id.tvGender)
    TextView mTvGender;
    @BindView(R.id.rbrgMale)
    RadioButton mRbrgMale;
    @BindView(R.id.rbrgFemale)
    RadioButton mRbrgFemale;
    @BindView(R.id.rgSex)
    RadioGroup mRgSex;
    @BindView(R.id.tvAvatar)
    TextView mTvAvatar;
    @BindView(R.id.picture_group)
    PictureView mPictureGroup;
    @BindView(R.id.register_but)
    Button mRegisterBut;

    private static final int MALE = 0;
    private static final int FEMALE = 1;
    private static final int GET_PHOTO_FRM_CAMERA = 0; //拍照获取照片
    private static final int CUT_PHOTO_FRM_CAMERA = 1;  //从相册选取照片
    private static final int GET_PHOTO_FRM_ALBUM = 2;
    private static final int CUT_PHOTO_FRM_ALBUM = 3;
    private int gender = MALE;//默认男生
    private int mediaId;

    private PhotoFrmAlbum mPhotoFrmAlbum;
    private PhotoFrmCamera mPhotoFrmCamera;
    private BottomPopSelectMenu bottomPopMenu;
    private String rootString;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private Bitmap mUploadAvatar;
    private ProgressHUD mProgressHUD;
    /**
     * 启动该活动
     *
     * @param activity
     */
    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, RegisterActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        mPhotoFrmAlbum = new PhotoFrmAlbum(this, GET_PHOTO_FRM_ALBUM, CUT_PHOTO_FRM_ALBUM);
        mPhotoFrmCamera = new PhotoFrmCamera(this, GET_PHOTO_FRM_CAMERA, CUT_PHOTO_FRM_CAMERA);
        rootString = getResources().getString(R.string.ROOT)+"register";
        networkStatus = new NetworkConnectStatus(this);
        mQueue = GlobalApplication.get().getRequestQueue();
        setListener();
    }

    private void setListener() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        mRbrgMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = MALE;
            }
        });

        mRbrgFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = FEMALE;
            }
        });

        mPictureGroup.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//                ImageView iv = new ImageView(getApplicationContext());
//                iv.setImageResource(R.drawable.pic);
//                pictureGroup.addView(iv, pictureGroup.getChildCount()-1);
            bottomPopMenu = new BottomPopSelectMenu(RegisterActivity.this, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch(v.getId()) {
                        case R.id.pop_select_fr_phone:
                            //Toast.makeText(RegisterActivity.this, "从手机中选择图片", Toast.LENGTH_SHORT).show();
                            mPhotoFrmAlbum.choosePhotoFrmAlbum();
                            //openAlbum();//从相册中获取图片
                            //GetPhotoFrmAlbum.openAlbum(RegisterActivity.this,CHOOSE_PHOTO);
                            break;
                        case R.id.pop_take_photo:
                            //Toast.makeText(RegisterActivity.this, "拍照获取图片", Toast.LENGTH_SHORT).show();
                            // openCamera(); //拍照获取图片
                            //GetPhotoFrmCamera.openCamera(RegisterActivity.this,TAKE_PHOTO);
                            mPhotoFrmCamera.getPhotoFrmCamera();
                            break;
                    }
                }
            });
            bottomPopMenu.show();
        }
    });

    //注册点击事件
    mRegisterBut.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            doRegister();
                                        }
                                    });
    }

    String description;
    String user;
    String password;
    String name;

private void doRegister() {       //可以不传头像，那么会使用系统给的默认头像
/*    if(mUploadAvatar == null) {
        Toast.makeText(RegisterActivity.this, "请上传头像".toString(), Toast.LENGTH_SHORT).show();
        return ;
    }*/
    //对注册数据排错
    user = mEtUser.getText().toString();
    if((user == null)||(user.length() == 0)) {
        Toast.makeText(RegisterActivity.this, "用户名不能为空".toString(), Toast.LENGTH_SHORT).show();
        return;
    }
    name = mEtName.getText().toString();
    if((user == null)||(user.length() == 0)) {
        Toast.makeText(RegisterActivity.this, "昵称不能为空".toString(), Toast.LENGTH_SHORT).show();
        return;
    }
    password = mEtPassword.getText().toString();
    if((password == null)||(password.length() == 0)) {
        Toast.makeText(RegisterActivity.this, "密码不能为空".toString(), Toast.LENGTH_SHORT).show();
        return;
    }
    final String passwordConfirm = mEtPasswordConfirm.getText().toString();
    if((passwordConfirm == null)||(passwordConfirm.length() == 0)||(!passwordConfirm.equals(password))) {
        Toast.makeText(RegisterActivity.this, "输入密码不一致".toString(), Toast.LENGTH_SHORT).show();
        return;
    }
    description = mEdDescription.getText().toString();
    if((description == null)||(description.length() == 0)) {
        Toast.makeText(RegisterActivity.this, "请填写签名".toString(), Toast.LENGTH_SHORT).show();
        return;
    }

    if (networkStatus.isConnectInternet()) {
        mProgressHUD = ProgressHUD.show(RegisterActivity.this, "注册中...", true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mProgressHUD.dismiss();
            }
        });
        mQueue = GlobalApplication.get().getRequestQueue();
        if(mUploadAvatar != null) {
            File Avatarfile=new File(getExternalCacheDir(),"my_upload_avatar.jpg");//将要保存图片的路径
            try {
                if (Avatarfile.exists()) {
                    Avatarfile.delete();
                }
                Avatarfile.createNewFile();
            } catch(IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(Avatarfile));
                mUploadAvatar.compress(Bitmap.CompressFormat.JPEG, 100, bos);//要对图片进行压缩
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new UploadImageUtil(RegisterActivity.this, mUploadAvatar, new UploadImageUtil.CallBackWithMediaId() {
                @Override
                public void handlerWithMediaId(final int mediaId) {   //获取到mediaId后回调
                    VolleyRequestParams bodyParams = new VolleyRequestParams()
                            .with("user", user)
                            .with("name", name)
                            .with("password", password)
                            .with("description", description)
                            .with("gender", String.valueOf(gender))
                            .with("avatar", String.valueOf(mediaId));//头像的id

                    VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                            .with("Accept", "application/json"); // 数据格式设置为json
                    mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.e("RegisterActivity:TAG", response);
                                    mProgressHUD.dismiss();
                                    handleResponse(response);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mProgressHUD.dismiss();
                            Log.e("PublishActivity:TAG", error.getMessage(), error);
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                            Toast.makeText(RegisterActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    executeRequest(mStringRequest);
                }
            }).upLoadImage();
        } else {   //没有头像
            VolleyRequestParams bodyParams = new VolleyRequestParams()
                    .with("user", user)
                    .with("name", name)
                    .with("password", password)
                    .with("description", description)
                    .with("gender", String.valueOf(gender));

            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e("RegisterActivity:TAG", response);
                            mProgressHUD.dismiss();
                            handleResponse(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Log.e("PublishActivity:TAG", error.getMessage(), error);
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                    Toast.makeText(RegisterActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);
        }
    } else{
        Toast.makeText(RegisterActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
    }

}

private void handleResponse(String response) {
    try {
        JSONObject jsObject= new JSONObject(response);

        //记录用户信息
        GlobalApplication.getMySelf().setId(jsObject.getInt("user_id"));
        GlobalApplication.setUserId(jsObject.getInt("user_id"));//保存在本地
        GlobalApplication.getMySelf().setDescription(description);
        GlobalApplication.getMySelf().setUser(user);
        GlobalApplication.setPassword(password); //密码保存在本地
        GlobalApplication.getMySelf().setName(name);
        GlobalApplication.getMySelf().setAvatar(mediaId);
        GlobalApplication.getMySelf().setGender(gender);
        GlobalApplication.setToken(jsObject.getString("access_token"));   //获取到的token
        GlobalApplication.setMyAvatar(mUploadAvatar);  //这个还是实时获取

        Toast.makeText(RegisterActivity.this, "注册成功".toString(), Toast.LENGTH_SHORT).show();
        //应该有一个错误提醒字段
        Intent intent = new Intent();
        intent.setClass(RegisterActivity.this, MainNavigationActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        RegisterActivity.this.finish();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    /**
     * 运行时权限申请回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:       //打开相册会有权限的申请
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 //   GetPhotoFrmAlbum.openAlbumForResult(RegisterActivity.this,CHOOSE_PHOTO);
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
        switch(requestCode) {
            case GET_PHOTO_FRM_CAMERA:
                if(resultCode == RESULT_OK) {
                    mPhotoFrmCamera.cutImage(150, 150);
                } else {
                    Toast.makeText(this, "你没有拍照", Toast.LENGTH_SHORT).show();
                }
                break;
            case CUT_PHOTO_FRM_CAMERA:
                if (resultCode == RESULT_OK) {
                    mUploadAvatar = mPhotoFrmCamera.getCutImage();//获得裁剪后的头像
                    mPictureGroup.removeAllViews();
                    ImageView iv = new ImageView(getApplicationContext());
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    iv.setImageBitmap(mUploadAvatar);
                    mPictureGroup.addView(iv);
                }
                break;
            case GET_PHOTO_FRM_ALBUM:
                if (resultCode == RESULT_OK) {
                    mPhotoFrmAlbum.cutImage(data, 150, 150);//裁剪照片,大小为150*150
                } else {
                    Toast.makeText(this, "你没有选择照片", Toast.LENGTH_SHORT).show();
                }
                break;
            case CUT_PHOTO_FRM_ALBUM:
                if (resultCode == RESULT_OK) {
                    mUploadAvatar = mPhotoFrmAlbum.getCutImage();//获得裁剪后的头像
                    mPictureGroup.removeAllViews();
                    ImageView iv = new ImageView(getApplicationContext());
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    iv.setImageBitmap(mUploadAvatar);
                    mPictureGroup.addView(iv);
                }
                break;
            default:
                break;
        }
    }
    /**
     * 功能描述： onTouchEvent事件中，点击android系统的软键盘外的其他地方，可隐藏软键盘，以免遮挡住输入框
     * @param event
     *            当前的触控事件
     * @return boolean类型的标记位。当用户点击后才会隐藏软键盘。
     */
    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        return imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }
}
