package com.android.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.BaseActivity;
import com.android.GlobalApplication;
import com.android.R;
import com.android.lbs.SetLocationActivity;
import com.android.tool.AddTagDialog;
import com.android.tool.AskSavePopMenu;
import com.android.tool.BottomPopSelectMenu;
import com.android.tool.DataUtils;
import com.android.tool.FlowLayout;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.PhotoFrmAlbum;
import com.android.tool.PhotoFrmCamera;
import com.android.tool.ProgressHUD;
import com.android.tool.SelectActivityTag;
import com.android.tool.SelectDateAndTime;
import com.android.tool.UploadImageUtil;
import com.android.tool.VolleyRequestParams;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PublishActivity extends BaseActivity {
    private static final int GET_PHOTO_FRM_CAMERA = 0; //拍照获取照片
    private static final int CUT_PHOTO_FRM_CAMERA = 1;  //从相册选取照片
    private static final int GET_PHOTO_FRM_ALBUM = 2;
    private static final int CUT_PHOTO_FRM_ALBUM = 3;
    public static final int SET_ADDRESS = 4;  //设置位置
    @BindView(R.id.cancel)
    TextView mCancel;
    @BindView(R.id.page_title)
    TextView mPageTitle;
    @BindView(R.id.post)
    TextView mPost;
    @BindView(R.id.activity_title)
    EditText mActivityTitle;
    @BindView(R.id.activity_content)
    EditText mActivityContent;
    @BindView(R.id.picture_set)
    TextView mPictureSet;
    @BindView(R.id.upload_picture)
    ImageView mUploadPicture;
    @BindView(R.id.ll_activity_time)
    LinearLayout mLlActivityTime;
    @BindView(R.id.start_time_set)
    TextView mStartTimeSet;
    @BindView(R.id.end_time_set)
    TextView mEndTimeSet;
    @BindView(R.id.address_set)
    TextView mAddressSet;
    @BindView(R.id.ll_to_map)
    LinearLayout mLlToMap;
    @BindView(R.id.activity_fee_set)
    EditText mActivityFeeSet;
    @BindView(R.id.activity_type_set)
    TextView mActivityTypeSet;
    @BindView(R.id.set_tag)
    TextView mSetTag;
    @BindView(R.id.activity_tag)
    FlowLayout mActivityTag;
    @BindView(R.id.rootview)
    LinearLayout mRootview;

    private BottomPopSelectMenu bottomPopMenu;
    private AskSavePopMenu popAskSaveMenu;
    private String rootString;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private ProgressHUD mProgressHUD;

    private List<String> activityTag; //活动标签

    private double latitude; //经度
    private double longitude;//纬度
    private String address;//字符串形式的地址
    private Boolean isAddType = false;
    private int activityType;
    private Boolean isGetLocation = false;
    private Boolean isUploadPhoto = false;
    private Bitmap upLoadPicture;
    private long startTimeStamp;
    private long endTimeStamp;

    private PhotoFrmAlbum mPhotoFrmAlbum;
    private PhotoFrmCamera mPhotoFrmCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        ButterKnife.bind(this);

        mPhotoFrmAlbum = new PhotoFrmAlbum(this, GET_PHOTO_FRM_ALBUM, CUT_PHOTO_FRM_ALBUM);
        mPhotoFrmCamera = new PhotoFrmCamera(this, GET_PHOTO_FRM_CAMERA, CUT_PHOTO_FRM_CAMERA);
        activityTag = new ArrayList<>();
        rootString = getResources().getString(R.string.ROOT) + "user/~me/activity";
        networkStatus = new NetworkConnectStatus(this);
        mQueue = GlobalApplication.get().getRequestQueue();
        setListener();

    }


    private void setListener() {
        mAddressSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PublishActivity.this, SetLocationActivity.class);
                PublishActivity.this.startActivityForResult(intent, SET_ADDRESS);
            }
        });
        mActivityTypeSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SelectActivityTag(PublishActivity.this, "活动类型", new SelectActivityTag.MyCallBack() {
                    @Override
                    public void handleTime(int time) {
                        String[] mItems = getResources().getStringArray(R.array.activityTags);
                        mActivityTypeSet.setText(mItems[time]);
                        isAddType = true;
                        activityType = time;
                    }
                }
                ).show();
            }
        });
        mSetTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AddTagDialog addTagDialog = new AddTagDialog(PublishActivity.this);
                addTagDialog.setHintText("请输入活动标签")
                        .setOnBtnCommitClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addTagDialog.dismiss();
                                String tag = addTagDialog.getContent();
                                if (tag == null || "".equals(tag)) {
                                    Toast.makeText(PublishActivity.this, "输入为空！", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (!activityTag.contains(tag)) { //判断是否已经添加了该标签
                                    addTag(tag);
                                    activityTag.add(tag);
                                } else {
                                    Toast.makeText(PublishActivity.this, "该标签已经添加过", Toast.LENGTH_SHORT).show();
                                }
//                                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//                                imm.hideSoftInputFromWindow(PublishActivity.this.getCurrentFocus().getWindowToken(), 0);
                            }
                        })
                        .show();
            }
        });

        mStartTimeSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SelectDateAndTime(PublishActivity.this, "起始时间", new SelectDateAndTime.MyCallBack() {
                    @Override
                    public void handleTime(long time) {
                        if (time < DataUtils.getCurrentTime()) {
                            Toast.makeText(PublishActivity.this, "起始时间不能小于当前时间，请重新设置", Toast.LENGTH_SHORT).show();
                            return;
                        } else if ((endTimeStamp != 0) && (time > endTimeStamp)) {
                            Toast.makeText(PublishActivity.this, "起始时间不能大于结束时间，请重新设置", Toast.LENGTH_SHORT).show();
                        }
                        startTimeStamp = time;
                        mStartTimeSet.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE6, time));
                    }
                }
                ).show();
            }
        });
        mEndTimeSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SelectDateAndTime(PublishActivity.this, "终止时间", new SelectDateAndTime.MyCallBack() {
                    @Override
                    public void handleTime(long time) {
                        if (time < DataUtils.getCurrentTime()) {
                            Toast.makeText(PublishActivity.this, "结束时间不能小于当前时间，请重新设置", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (time < startTimeStamp) {
                            Toast.makeText(PublishActivity.this, "结束时间不能小于结束时间，请重新设置", Toast.LENGTH_SHORT).show();
                        }
                        endTimeStamp = time;
                        mEndTimeSet.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE6, time));
                    }
                }
                ).show();
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//直接退出活动
            /*    if (!hasContent()) {  //没有填写任何内容
                    finish();//直接退出活动
                }
                popAskSaveMenu = new AskSavePopMenu(PublishActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.pop_no_save://要进行不保存操作
                                Toast.makeText(PublishActivity.this, "不保存操作", Toast.LENGTH_SHORT).show();
                                finish();//直接退出活动
                                break;
                            case R.id.pop_save://要进行保存操作
                                Toast.makeText(PublishActivity.this, "保存操作", Toast.LENGTH_SHORT).show();
                                finish();//直接退出活动
                        }
                    }
                });
                popAskSaveMenu.show();*/
                //
                //这里应该在finish之前弹出一个保存对话框
            }
        });
        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPublish();
            }
        });

        mPictureSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ImageView iv = new ImageView(getApplicationContext());
//                iv.setImageResource(R.drawable.pic);
//                pictureGroup.addView(iv, pictureGroup.getChildCount()-1);
                bottomPopMenu = new BottomPopSelectMenu(PublishActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
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
    }

    /**
     * 加入活动标签
     *
     * @param tagStr
     */
    private void addTag(String tagStr) {
        View v = LayoutInflater.from(PublishActivity.this).inflate(R.layout.text_tag_unit, mActivityTag, false);
        TextView tg = (TextView) v.findViewById(R.id.tag_name);
        tg.setText(tagStr);
        mActivityTag.addView(v);
    }

    /**
     * 判断是否填写了内容
     *
     * @return
     */
    private Boolean hasContent() {
        String str = mActivityTitle.getText().toString();
        if (str != null && !"".equals(str)) {
            return true;
        }
        str = mActivityContent.getText().toString();
        if (str != null && !"".equals(str)) {
            return true;
        }

/*        if (pictureGroup.getChildCount() > 1) {
            return true;
        }*/
        return false;
    }

    /**
     * 判断发布内容是否完整
     */
    private Boolean isContentsIntact() {
        String title = mActivityTitle.getText().toString();
        if (title == null || "".equals(title)) {
            //   if (isRemind) {
            Toast.makeText(this, "请填写活动标题！", Toast.LENGTH_SHORT).show();
            //    }
            return false;
        }
        String content = mActivityContent.getText().toString();
        if (content == null || "".equals(content)) {
            //  if (isRemind) {
            Toast.makeText(this, "请填写活动内容！", Toast.LENGTH_SHORT).show();
            //   }
            return false;
        }
        if (!isUploadPhoto) {
            Toast.makeText(this, "请上传图片！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (0 == startTimeStamp) {
            Toast.makeText(this, "起始时间不能为空，请设置起始时间", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (0 == endTimeStamp) {
            Toast.makeText(this, "结束时间不能为空，请设置结束时间", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isGetLocation) {
            Toast.makeText(this, "请设置活动地点", Toast.LENGTH_SHORT).show();
            return false;
        }
        String fee = mActivityFeeSet.getText().toString();
        if (fee == null || "".equals(fee)) {
            //  if (isRemind) {
            Toast.makeText(this, "请填写活动费用", Toast.LENGTH_SHORT).show();
            return false;
            //   }
        }
        if (!isAddType) {
            Toast.makeText(this, "请选择活动类型", Toast.LENGTH_SHORT).show();
            return false;
        }
/*        if(0 == activityTag.size()) {  //活动标签可以为空
            Toast.makeText(this, "请设置活动标签", Toast.LENGTH_SHORT).show();
            return false;
        }*/

        return true;
    }

    private void doPublish() {
        if (!isContentsIntact()) {
            return;
        }
        if (networkStatus.isConnectInternet()) {
            new UploadImageUtil(PublishActivity.this, upLoadPicture, new UploadImageUtil.CallBackWithMediaId(){
                @Override
                public void handlerWithMediaId(int mediaId) {
                    VolleyRequestParams bodyParams = new VolleyRequestParams()
                            .with("title", mActivityTitle.getText().toString())
                            .with("image", String.valueOf(mediaId))//头像的id
                            .with("beginTime", String.valueOf(startTimeStamp))
                            .with("endTime", String.valueOf(endTimeStamp))
                            .with("address", address)
                            .with("latitude", String.valueOf(latitude))
                            .with("longitude", String.valueOf(longitude))
                            .with("fee", mActivityFeeSet.getText().toString())
                            .with("category", String.valueOf(activityType))
                            // .with("tags", String.valueOf(gender))
                            .with("content", mActivityContent.getText().toString());

                    if (0 != activityTag.size()) {   //添加活动tag，无则不添加
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < activityTag.size(); i++) {
                            if (0 == i) {
                                sb.append(activityTag.get(i));
                            } else {
                                sb.append("&" + activityTag.get(i));
                            }
                        }
                        bodyParams.with("tags", sb.toString());
                        // bodyParams.with("tags", URLEncoder.encode(sb.toString(), "UTF-8"));
                    }

                    VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                            .with("token", GlobalApplication.getToken())
                            .with("Accept", "application/json"); // 数据格式设置为json
                    mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        Log.d("PublishActivity:TAG", response);
                                        Toast.makeText(PublishActivity.this, "发布完成,等待审核".toString(), Toast.LENGTH_SHORT).show();
                                        PublishActivity.this.finish();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("PublishActivity:TAG", error.getMessage(), error);
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                            Toast.makeText(PublishActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    executeRequest(mStringRequest);
                }
            }).upLoadImage();

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
                  //  GetPhotoFrmAlbum.openAlbumForResult(PublishActivity.this, CHOOSE_PHOTO);
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
            case SET_ADDRESS:
                if (resultCode == RESULT_OK) {
                    latitude = Double.valueOf(data.getStringExtra("latitude"));//纬度
                    longitude = Double.valueOf(data.getStringExtra("longitude"));//经度
                    address = data.getStringExtra("address");//地址
                    mAddressSet.setText(address);
                    isGetLocation = true;
                  //  Toast.makeText(this, "经度" + longitude + ",纬度" + latitude, Toast.LENGTH_SHORT).show();

                         /*       intent.putExtra("latitude", String.valueOf(option.getPosition().latitude));//纬度
    intent.putExtra("longitude", String.valueOf(option.getPosition().longitude));//经度*/
                    // addPicture(GetPhotoFrmAlbum.getPhoto(PublishActivity.this, data));
                } else {
                    Toast.makeText(this, "未重新设置地点", Toast.LENGTH_SHORT).show();
                }
                break;
            case GET_PHOTO_FRM_CAMERA:
                if(resultCode == RESULT_OK) {
                    mPhotoFrmCamera.cutImage(1280, 960);
                } else {
                    Toast.makeText(this, "你没有拍照", Toast.LENGTH_SHORT).show();
                }
                break;
            case CUT_PHOTO_FRM_CAMERA:
                if (resultCode == RESULT_OK) {
                    isUploadPhoto = true;
                    upLoadPicture = mPhotoFrmCamera.getCutImage();//对获取的图片进行压缩
                    mUploadPicture.setImageBitmap(upLoadPicture);
                }
                break;
            case GET_PHOTO_FRM_ALBUM:
                if (resultCode == RESULT_OK) {
                    mPhotoFrmAlbum.cutImage(data, 1280, 960);//裁剪照片,大小为150*150
                } else {
                    Toast.makeText(this, "你没有选择照片", Toast.LENGTH_SHORT).show();
                }
                break;
            case CUT_PHOTO_FRM_ALBUM:
                if (resultCode == RESULT_OK) {
                    isUploadPhoto = true;
                    upLoadPicture = mPhotoFrmAlbum.getCutImage();//对获取的图片进行压缩
                    mUploadPicture.setImageBitmap(upLoadPicture);
                }
                break;
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
        Toast.makeText(this, "点击软键盘以外部分", Toast.LENGTH_SHORT).show();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        return imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }
}
