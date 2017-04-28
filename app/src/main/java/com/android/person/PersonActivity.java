package com.android.person;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.GlobalApplication;
import com.android.R;
import com.android.activity.ListActivity;
import com.android.model.User;
import com.android.person.edit.PersonEdit;
import com.android.status.RecentStatusActivity;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.ReportUtil;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class PersonActivity extends AppCompatActivity {
    private static final int REFRESH_PERSON = 0;
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title_name)
    TextView mTitleName;
    @BindView(R.id.person_manage)
    TextView mPersonManage;
    @BindView(R.id.user_portrait)
    CircleImageView mUserPortrait;
    @BindView(R.id.user_name)
    TextView mUserName;
    @BindView(R.id.iv_gender)
    ImageView mIvGender;
    @BindView(R.id.user_description)
    TextView mUserDescription;
    @BindView(R.id.add_attention_btn)
    Button mAddAttentionBtn;
    @BindView(R.id.private_btn)
    Button mPrivateBtn;
    @BindView(R.id.join_activity_num)
    TextView mJoinActivityNum;
    @BindView(R.id.to_join_activity)
    LinearLayout mToJoinActivity;
    @BindView(R.id.attention_person_num)
    TextView mAttentionPersonNum;
    @BindView(R.id.to_attention_person)
    LinearLayout mToAttentionPerson;
    @BindView(R.id.fans_num)
    TextView mFansNum;
    @BindView(R.id.to_fans)
    LinearLayout mToFans;
    @BindView(R.id.interested_activity)
    LinearLayout mInterestedActivity;
    @BindView(R.id.published_activity)
    LinearLayout mPublishedActivity;
    @BindView(R.id.to_album)
    LinearLayout mToAlbum;
    @BindView(R.id.recent_status)
    LinearLayout mRecentStatus;

    private String json;
    private User user;
    private ProgressHUD mProgressHUD;
    private NetworkConnectStatus networkStatus;//网络连接状态

    private boolean isRefreshing = false;

    public static void startActivity(Activity activity, String jsonStr) {
        Bundle bundle = new Bundle();
        bundle.putString("jsonStr", jsonStr); //传递活动id
        Intent intent = new Intent(activity, PersonActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        ButterKnife.bind(this);
        networkStatus = new NetworkConnectStatus(this);
        showData();
        setListener();
    }

    private void showData() {
        Bundle bundle = this.getIntent().getExtras();
        json = bundle.getString("jsonStr");
        try {
            JSONObject jsonObject = new JSONObject(json);
            user = new User();
            user.setId(jsonObject.getInt("id"));
            user.setUser(jsonObject.getString("user"));
            user.setName(jsonObject.getString("name"));
            user.setAvatar(jsonObject.getInt("avatar"));
            user.setGender(jsonObject.getInt("gender"));
            user.setDescription(jsonObject.getString("description"));
            user.setFollowersCount(jsonObject.getInt("followers_count"));
            user.setFansCount(jsonObject.getInt("fans_count"));
            user.setActivitysCount(jsonObject.getInt("activities_count"));
            user.setRelation(jsonObject.getString("relation"));

            mUserName.setText(user.getName());
            if (user.getGender() == 0) { //男生
                mIvGender.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.male_icon));
            } else {
                mIvGender.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.female_icon));
            }
            mUserDescription.setText(user.getDescription());
            mJoinActivityNum.setText(String.valueOf(user.getActivitysCount()));
            mAttentionPersonNum.setText(String.valueOf(user.getFollowersCount()));
            mFansNum.setText(String.valueOf(user.getFansCount()));
            //用户头像
            BitmapLoaderUtil.getInstance().getImage(mUserPortrait, BitmapLoaderUtil.TYPE_ORIGINAL, user.getAvatar());
            if("myself".equals(user.getRelation())) {
                mTitleName.setText("我的主页");
                mPersonManage.setText("编辑");
                mAddAttentionBtn.setVisibility(View.GONE); //影藏关注按钮
                mPrivateBtn.setVisibility(View.GONE);//影藏私信按钮
            } else {
                mTitleName.setText(user.getName() + "的主页");
                if ("follower".equals(user.getRelation()) || "friend".equals(user.getRelation())) {//是当前用户关注的人
                    mAddAttentionBtn.setText("取 关");
                }
            }
        } catch(Exception e) {
            Log.d("getTIMELINE:TAG", "出错");
            Log.d("getTIMELINE:TAG", e.getMessage(),e);
        }
    }
    /**
     * 设置监听事件
     */
    private void setListener() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mPersonManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("myself".equals(user.getRelation())){ //编辑功能
                    Intent intent1 = new Intent(PersonActivity.this, PersonEdit.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PersonActivity.this.startActivity(intent1);
                    PersonActivity.this.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                } else {   //举报
                    new ReportUtil(PersonActivity.this, ReportUtil.TYPE_USER, user.getId()).doReport();
                }
            }
        });
        mAddAttentionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("follower".equals(user.getRelation())||"friend".equals(user.getRelation())) {   //取关操作
                    doAttention(false);
                } else{     //关注操作
                    doAttention(true);
                }
                refreshPersonInfo(user.getId());//刷新
            }
        });
        mPrivateBtn.setOnClickListener(new View.OnClickListener() {   //私信
            @Override
            public void onClick(View v) {
                CommunicateActivity.startActivityForResult(PersonActivity.this, REFRESH_PERSON,user.getId(), user.getAvatar(), user.getName());
            }
        });
        mToAttentionPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonListActivity.startActivityForResult(PersonActivity.this, REFRESH_PERSON, PersonListActivity.RELATION_OTHER, PersonListActivity.DIRECTION_ATTENTION, user.getId());
            }
        });

        mToFans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonListActivity.startActivityForResult(PersonActivity.this, REFRESH_PERSON, PersonListActivity.RELATION_OTHER, PersonListActivity.DIRECTION_FANS, user.getId());
            }
        });

        mToJoinActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListActivity.startActivityForResult(PersonActivity.this, REFRESH_PERSON,ListActivity.RELATION_OTHER, ListActivity.TYPE_PARTICIPATED, user.getId());
            }
        });
        mInterestedActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListActivity.startActivityForResult(PersonActivity.this, REFRESH_PERSON,ListActivity.RELATION_OTHER, ListActivity.TYPE_INTERESTED, user.getId());
            }
        });
        mPublishedActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListActivity.startActivityForResult(PersonActivity.this, REFRESH_PERSON,ListActivity.RELATION_OTHER, ListActivity.TYPE_PUBLISHED, user.getId());
            }
        });
        mRecentStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecentStatusActivity.startActivityForResult(PersonActivity.this, REFRESH_PERSON,RecentStatusActivity.RELATION_OTHER, user.getId());
            }
        });
        mToAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumActivity.startActivity(PersonActivity.this, AlbumActivity.RELATION_OTHER, user.getId());
            }
        });
    }

    private void doAttention(final boolean isAttention) {
        int requestMethod = isAttention? Request.Method.POST:Request.Method.DELETE;
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(this, "提交中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept","application/json"); // 数据格式设置为json
            MyStringRequest mStringRequest = new MyStringRequest(requestMethod, getResources().getString(R.string.ROOT) + "user/~me/follower/" + user.getId(), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Log.d("getACTIVITY:TAG", response);
                            mProgressHUD.dismiss();
                            if(isAttention) {   //关注成功
                                user.setRelation("follower");
                                mAddAttentionBtn.setText("取 关");
                            } else {   //取关成功
                                user.setRelation("stranger");
                                mAddAttentionBtn.setText("+ 关注");
                            }
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            /*Log.d("getACTIVITY:TAG", "出错");
                            Log.d("getACTIVITY:TAG", error.getMessage(),error);*/
                            mProgressHUD.dismiss();
                        }
                    });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            GlobalApplication.get().getRequestQueue().add(mStringRequest);
        }else {
            mProgressHUD.dismiss();
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 刷新个人信息
     * @param uid
     */
    public void refreshPersonInfo(int uid) {
        if(isRefreshing) {
            return;
        }
        isRefreshing = true;
        VolleyRequestParams headerParams = new VolleyRequestParams()
                .with("token", GlobalApplication.getToken())
                .with("Accept","application/json"); // 数据格式设置为json
        //Toast.makeText(MainActivity.this, "getUserInfo", Toast.LENGTH_SHORT).show();
        MyStringRequest mStringRequest = new MyStringRequest(Request.Method.GET, getResources().getString(R.string.ROOT)+"user/"+uid, headerParams, null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e("PersonInfo:login:TAG", response);
                            JSONObject jsonObject= new JSONObject(response);
                            user = new User();
                            user.setId(jsonObject.getInt("id"));
                            user.setUser(jsonObject.getString("user"));
                            user.setName(jsonObject.getString("name"));
                            user.setAvatar(jsonObject.getInt("avatar"));
                            user.setGender(jsonObject.getInt("gender"));
                            user.setDescription(jsonObject.getString("description"));
                            user.setFollowersCount(jsonObject.getInt("followers_count"));
                            user.setFansCount(jsonObject.getInt("fans_count"));
                            user.setActivitysCount(jsonObject.getInt("activities_count"));
                            user.setRelation(jsonObject.getString("relation"));

                            mUserName.setText(user.getName());
                            if (user.getGender() == 0) { //男生
                                mIvGender.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.male_icon));
                            } else {
                                mIvGender.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.female_icon));
                            }
                            mUserDescription.setText(user.getDescription());
                            mJoinActivityNum.setText(String.valueOf(user.getActivitysCount()));
                            mAttentionPersonNum.setText(String.valueOf(user.getFollowersCount()));
                            mFansNum.setText(String.valueOf(user.getFansCount()));
                            //用户头像
                            if(user.getId() == GlobalApplication.getMySelf().getId()) {
                                 BitmapLoaderUtil.getInstance().getImage(mUserPortrait, BitmapLoaderUtil.TYPE_ORIGINAL, user.getAvatar());

                            }
                            isRefreshing = false;

                        } catch (Exception e) {
                            e.printStackTrace();
                            isRefreshing = false;
                          //  Toast.makeText(PersonActivity.this, "登录失败1".toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isRefreshing = false;
                Log.d("getTIMELINE:TAG", "出错");
                Log.d("getTIMELINE:TAG", error.getMessage(),error);
            }
        });
        mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
        GlobalApplication.get().getRequestQueue().add(mStringRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REFRESH_PERSON:        //刷新个人主页
                if(resultCode == RESULT_OK) {
                    refreshPersonInfo(user.getId());
                }
                break;
            default:
                break;
        }
    }

}
