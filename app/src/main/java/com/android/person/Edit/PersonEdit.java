package com.android.person.edit;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.guide.BaseActivity;
import com.android.GlobalApplication;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.PhotoFrmAlbum;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.SetGenderDialog;
import com.android.tool.UploadImageUtil;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PersonEdit extends BaseActivity {
    private static final int EDIT_NAME = 0;
    private static final int EDIT_DESCRIPTION = 1;
    private static final int EDIT_PASSWORD = 2;
    private static final int GET_PHOTO_FRM_ALBUM = 3;
    private static final int CUT_PHOTO = 4;
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.sure)
    TextView mSure;
    @BindView(R.id.iv_avatar)
    ImageView mIvAvatar;
    @BindView(R.id.ll_edit_avatar)
    LinearLayout mLlEditAvatar;
    @BindView(R.id.tv_name)
    TextView mTvName;
    @BindView(R.id.ll_edit_name)
    LinearLayout mLlEditName;
    @BindView(R.id.tv_gender)
    TextView mTvGender;
    @BindView(R.id.ll_edit_gender)
    LinearLayout mLlEditGender;
    @BindView(R.id.ll_edit_password)
    LinearLayout mLlEditPassword;
    @BindView(R.id.tv_description)
    TextView mTvDescription;
    @BindView(R.id.ll_edit_description)
    LinearLayout mLlEditDescription;


    private String rootString;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private ProgressHUD mProgressHUD;

    private PhotoFrmAlbum mPhotoFrmAlbum;
    private Bitmap mCutAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_edit);
        ButterKnife.bind(this);


        mPhotoFrmAlbum = new PhotoFrmAlbum(this, GET_PHOTO_FRM_ALBUM, CUT_PHOTO);
        initView();
        setListener();
    }

    private void initView() {
      //  mIvAvatar.setImageBitmap(GlobalApplication.getMyAvatar()); //设置头像
        BitmapLoaderUtil.getInstance().getImage(mIvAvatar, BitmapLoaderUtil.TYPE_ORIGINAL, GlobalApplication.getMySelf().getAvatar());
        mTvName.setText(GlobalApplication.getMySelf().getName());  //设置昵称
        setGender(GlobalApplication.getMySelf().getGender());      //设置性别
        mTvDescription.setText(GlobalApplication.getMySelf().getDescription());//设置个性签名
        rootString = getResources().getString(R.string.ROOT) + "user/~me";
        networkStatus = new NetworkConnectStatus(this);
    }

    private void setGender(int gender) {
        if (gender == 0) { //男生
            mTvGender.setText(" 男");
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.male_icon);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mTvGender.setCompoundDrawables(drawable, null, null, null);

        } else {
            mTvGender.setText(" 女");
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.female_icon);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mTvGender.setCompoundDrawables(drawable, null, null, null);
        }
    }

    private void setListener() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);   //一定要放在finish前面
                PersonEdit.this.finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
        mLlEditAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //修改头像
                mPhotoFrmAlbum.choosePhotoFrmAlbum();
            }
        });
        mLlEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //修改昵称
                TextEdit.startActivityForResult(PersonEdit.this, "修改昵称", GlobalApplication.getMySelf().getName(), EDIT_NAME);
            }
        });
        mLlEditDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //修改签名
                TextEdit.startActivityForResult(PersonEdit.this, "修改签名", GlobalApplication.getMySelf().getDescription(), EDIT_DESCRIPTION);
            }
        });
        mLlEditGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //修改性别
                final SetGenderDialog setGenderDialog = new SetGenderDialog(PersonEdit.this
                        , GlobalApplication.getMySelf().getGender());
                setGenderDialog.setClickListener(new View.OnClickListener() {//选择事件回调
                    @Override
                    public void onClick(View v) {
                        changeGender();
                        setGenderDialog.dismiss();
                    }
                });
                setGenderDialog.show();
            }
        });
        mLlEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //修改密码
                Intent intent = new Intent(PersonEdit.this, PasswordEdit.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // activity.startActivity(intent);
                PersonEdit.this.startActivityForResult(intent, EDIT_PASSWORD);
                PersonEdit.this.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
            }
        });
    }

    private void changeGender() {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(this, "保存中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });

            VolleyRequestParams bodyParams = new VolleyRequestParams()
                    .with("gender", String.valueOf(1 - GlobalApplication.getMySelf().getGender()));//修改性别

            VolleyRequestParams headerParams = new VolleyRequestParams()
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            MyStringRequest mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //记录用户信息
                            try {
                                Log.e("TextEdit:TAG", response);
                                mProgressHUD.dismiss();
                                GlobalApplication.getMySelf().setGender(1 - GlobalApplication.getMySelf().getGender()); //记录修改后的个人签名和昵称
                                setGender(GlobalApplication.getMySelf().getGender());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Toast.makeText(PersonEdit.this, "修改失败".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(400 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            GlobalApplication.get().getRequestQueue().add(mStringRequest);
        } else {
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EDIT_NAME:
                if (resultCode == RESULT_OK) {
                    mTvName.setText(GlobalApplication.getMySelf().getName());  //设置昵称
                }
                break;
            case EDIT_DESCRIPTION:
                if (resultCode == RESULT_OK) {
                    mTvDescription.setText(GlobalApplication.getMySelf().getDescription());//设置个性签名
                }
                break;
            case EDIT_PASSWORD:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "修改密码成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case GET_PHOTO_FRM_ALBUM:
                if (resultCode == RESULT_OK) {
                    mPhotoFrmAlbum.cutImage(data, 150, 150);//裁剪照片,大小为150*150
                }
                break;
            case CUT_PHOTO:
                if (resultCode == RESULT_OK) {
                    //图片应该另开线程处理
                    // mIvAvatar.setImageBitmap(mGetPhotoFrmAlbumWithCut.getCutImage());
                    new HandleBitmapTask(mPhotoFrmAlbum.getCutImage()).execute();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 获取数据时先等待2S
     */
    private class HandleBitmapTask extends AsyncTask<Void, Void, String[]> {
        private Bitmap mBitmap;

        public HandleBitmapTask(Bitmap bitmap) {
            mBitmap = bitmap;
            Log.e("TAG", "裁剪后bitmap" + mBitmap.getByteCount() / 1024 + " (宽度为" + mBitmap.getWidth() + "高度为" + mBitmap.getHeight() + "K)");
        }

        /**
         * 子线程中执行
         *
         * @param params
         * @return
         */
        @Override
        protected String[] doInBackground(Void... params) {
            // mCutAvatar = ImageUtils.comp(mBitmap, 200, 150, 150); //获取等比压缩后的头像
            mCutAvatar = mBitmap;//不压缩
            return null;
        }

        /**
         * 主线程中执行
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String[] result) {
            if (mCutAvatar != null) {
                new UploadImageUtil(PersonEdit.this, mCutAvatar, new UploadImageUtil.CallBackWithMediaId(){
                    @Override
                    public void handlerWithMediaId(int mediaId) {
                        changeAvatar(mediaId);
                    }
                }).upLoadImage();
            } else {
                Toast.makeText(PersonEdit.this, "上传失败".toString(), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }
    }

    private void changeAvatar(final int mediaId) {
        VolleyRequestParams bodyParams = new VolleyRequestParams()
                .with("avatar", String.valueOf(mediaId));//修改头像

        VolleyRequestParams headerParams = new VolleyRequestParams()
                .with("token", GlobalApplication.getToken())
                .with("Accept", "application/json"); // 数据格式设置为json
        MyStringRequest mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //记录用户信息
                        try {
                            Log.e("TextEdit:TAG", response);
                            Toast.makeText(PersonEdit.this, "修改成功".toString(), Toast.LENGTH_SHORT).show();
                            mIvAvatar.setImageBitmap(mCutAvatar);//头像框图片修改
                            GlobalApplication.getMySelf().setAvatar(mediaId);
                            GlobalApplication.setMyAvatar(mCutAvatar);//本地保存头像
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PersonEdit.this, "修改失败".toString(), Toast.LENGTH_SHORT).show();
            }
        });
        mStringRequest.setRetryPolicy(new DefaultRetryPolicy(400 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        GlobalApplication.get().getRequestQueue().add(mStringRequest);
    }

}
