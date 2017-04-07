package com.android.activity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.guide.BaseActivity;
import com.android.guide.GlobalApplication;
import com.android.model.Comment;
import com.android.status.CommentFragment;
import com.android.tool.DataUtils;
import com.android.tool.FlowLayout;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.ReplyDialog;
import com.android.tool.RequestManager;
import com.android.tool.VolleyRequestParams;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActivityDetailActivity extends BaseActivity {
    private static final int LOAD_DATA_COUNT = 10;//每页加载10条数据
    private static final int WISH_YES = 1;//对活动感兴趣
    private static final int WISH_NO = -1;//对活动不感兴趣
    private static final int WISH_NO_VOTE = 0;//对活动没有投票
    private static final int PARTICIPATE_YES = 1;//已参与
    private static final int PARTICIPATE_NO = 0;//未参与

    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.page_title)
    TextView mPageTitle;
    @BindView(R.id.share_btn)
    ImageView mShareBtn;
    @BindView(R.id.activity_image)
    ImageView mActivityImage;
    @BindView(R.id.created_time)
    TextView mCreatedTime;
    @BindView(R.id.activity_title)
    TextView mActivityTitle;
    @BindView(R.id.wisher_info)
    TextView mWisherInfo;
    @BindView(R.id.no_join_activity)
    CheckBox mNoJoinActivity;
    @BindView(R.id.join_activity)
    CheckBox mJoinActivity;
    @BindView(R.id.join_btn)
    Button mJoinBtn;
    @BindView(R.id.activity_time)
    TextView mActivityTime;
    @BindView(R.id.activity_address)
    TextView mActivityAddress;
    @BindView(R.id.ll_to_map)
    LinearLayout mLlToMap;
    @BindView(R.id.activity_fee)
    TextView mActivityFee;
    @BindView(R.id.ll_to_creater_page)
    LinearLayout mLlToCreaterPage;
    @BindView(R.id.notification_count)
    TextView mNotificationCount;
    @BindView(R.id.ll_to_notify)
    LinearLayout mLlToNotify;
    @BindView(R.id.ll_to_comment)
    LinearLayout mLlToComment;
    @BindView(R.id.activity_tag)
    FlowLayout mActivityTag;
    @BindView(R.id.ll_to_picture)
    LinearLayout mLlToPicture;
    @BindView(R.id.activity_content)
    TextView mActivityContent;
    @BindView(R.id.publisher_name)
    TextView mPublisherName;
    @BindView(R.id.comment_count)
    TextView mCommentCount;
    @BindView(R.id.do_comment)
    ImageView mDoComment;
    @BindView(R.id.commend_content)
    FrameLayout mCommendContent;

    private CommentFragment mCommentFragment;//评论碎片
    private JSONObject mJsonobject;//活动详情的json数据
    private Bitmap userAvatar; //发布者头像
    private Bitmap activityImage;//活动图像
    private int aid;
    private int creatorId;//活动id

    private Boolean hasParticipate;
    private int wishType;

    private ProgressHUD mProgressHUD;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private String rootString;
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;

    private int commenTotal;//评论总数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        Bundle bundle = this.getIntent().getExtras();
        aid = bundle.getInt("aid");
        creatorId = bundle.getInt("creatorId"); //活动id
        //初始化评论
        mCommentFragment = CommentFragment.newInstance(Comment.ACTIVITY_TYPE, aid, creatorId);//指定评论类型
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.commend_content, mCommentFragment);
        transaction.commit();
/*        try {
            mJsonobject = new JSONObject(bundle.getString("jsonStr"));
            setData(mJsonobject);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        rootString = getResources().getString(R.string.ROOT) + "activity/" + aid;
        networkStatus = new NetworkConnectStatus(this);
        mQueue = GlobalApplication.get().getRequestQueue();
        getDetail();
        setListener();
    }

    private void getDetail() {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(ActivityDetailActivity.this, "加载中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });

            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("count", String.valueOf(LOAD_DATA_COUNT))//条数
                    .with("page", String.valueOf(1)); //指定页数，默认第一页
            VolleyRequestParams headerParams = new VolleyRequestParams()
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if ("null".equals(response) || null == response) {
                                //登录未获取响应，登录失败
                                mProgressHUD.dismiss();
                                Toast.makeText(ActivityDetailActivity.this, "加载失败".toString(), Toast.LENGTH_SHORT).show();

                                return;
                            }
                            try {
                                Log.d("ActivityDetail:TAG", response);
                                mProgressHUD.dismiss();
                                JSONObject jsObject = new JSONObject(response);
                                setData(jsObject);

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
                    Log.e("PublishActivity:TAG", error.getMessage(), error);
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                    Toast.makeText(ActivityDetailActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);
        } else {

            Toast.makeText(ActivityDetailActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private Boolean checkBoxClickable = true;
    private int wishStatus;

    private void setListener() {
        //设置checkbox的点击事件
        mJoinActivity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //   Toast.makeText(ActivityDetailActivity.this, "是check".toString(), Toast.LENGTH_SHORT).show();

                if (!checkBoxClickable) {
                    checkBoxClickable = true;
                } else {
                    if (buttonView.isChecked()) { //改变字体颜色
                        // Toast.makeText(ActivityDetailActivity.this, "投是".toString(), Toast.LENGTH_SHORT).show();
                        doVote(true);//wishType

                        wisherCount = wisherCountStart + (wishType == WISH_YES ? 0 : 1);
                        wisherTotal = wisherTotalStart + (wishType == WISH_NO_VOTE ? 1 : 0);
                        setJoinIfo(wisherCount, wisherTotal, participantCount);
                        buttonView.setTextColor(getResources().getColor(R.color.check_selected));
                        //checkBoxClickable = false;
                        if (wishStatus == WISH_NO) {
                            checkBoxClickable = false;
                        }
                        mNoJoinActivity.setChecked(false);
                        mNoJoinActivity.setTextColor(getResources().getColor(R.color.check_unselected));
                        wishStatus = WISH_YES;
                    } else {
                        cancelVote(true);
                        wishStatus = WISH_NO_VOTE;
                        //  Toast.makeText(ActivityDetailActivity.this, "取消投是".toString(), Toast.LENGTH_SHORT).show();
                        wisherCount = wisherCountStart - (wishType == WISH_YES ? 1 : 0);
                        wisherTotal = wisherTotalStart - (wishType == WISH_NO_VOTE ? 0 : 1);
                        setJoinIfo(wisherCount, wisherTotal, participantCount);
                        buttonView.setTextColor(getResources().getColor(R.color.check_unselected));
                    }
                }
            }
        });
        mNoJoinActivity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Toast.makeText(ActivityDetailActivity.this, "否check".toString(), Toast.LENGTH_SHORT).show();
                if (!checkBoxClickable) {
                    checkBoxClickable = true;
                } else {
                    if (buttonView.isChecked()) { //改变字体颜色
                        //  Toast.makeText(ActivityDetailActivity.this, "投否".toString(), Toast.LENGTH_SHORT).show();
                        doVote(false);

                        wisherCount = wisherCountStart - (wishType == WISH_YES ? 1 : 0);
                        wisherTotal = wisherTotalStart + (wishType == WISH_NO_VOTE ? 1 : 0);
                        setJoinIfo(wisherCount, wisherTotal, participantCount);
                        buttonView.setTextColor(getResources().getColor(R.color.check_selected));
                        if (wishStatus == WISH_YES) {
                            checkBoxClickable = false;
                        }
                        mJoinActivity.setChecked(false);
                        mJoinActivity.setTextColor(getResources().getColor(R.color.check_unselected));
                        wishStatus = WISH_NO;
                    } else {
                        //  Toast.makeText(ActivityDetailActivity.this, "取消投否".toString(), Toast.LENGTH_SHORT).show();
                        cancelVote(false);
                        wishStatus = WISH_NO_VOTE;
                        wisherCount = wisherCountStart - (wishType == WISH_YES ? 1 : 0);
                        wisherTotal = wisherTotalStart - (wishType == WISH_NO_VOTE ? 0 : 1);
                        setJoinIfo(wisherCount, wisherTotal, participantCount);
                        buttonView.setTextColor(getResources().getColor(R.color.check_unselected));
                    }
                }
            }
        });
        //返回
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mJoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasParticipate) {   //执行取消参与操作
                    cancelParticipate();
                } else {
                    doParticipate();
                }
            }
        });
        mDoComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ReplyDialog replyDialog = new ReplyDialog(ActivityDetailActivity.this);
                replyDialog.setHintText("回复某人的评论...")
                        .setOnBtnCommitClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {   //回复键的回调函数
                                replyDialog.dismiss();
                                Log.d("XXX", replyDialog.getContent());
                                mCommentFragment.addMyComment(0, replyDialog.getContent());
                                //tvShow.setText(Html.fromHtml(replyDialog.getContent(), mFaceImageGetter, null));
                            }
                        })
                        .show();
            }
        });
    }

    /**
     * 投票
     *
     * @param isWisher true--投感兴趣
     *                 false--投不感兴趣
     */
    private void doVote(Boolean isWisher) {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(ActivityDetailActivity.this, "投票中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });
            String urlStr;
            if (isWisher) {
                urlStr = getResources().getString(R.string.ROOT) + "/activity/" + aid + "/wisher";
            } else {
                urlStr = getResources().getString(R.string.ROOT) + "/activity/" + aid + "/unwisher";
            }

            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken());
            mStringRequest = new MyStringRequest(Request.Method.POST, urlStr, headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mProgressHUD.dismiss();

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Log.e("PublishActivity:TAG", error.getMessage(), error);
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                    Toast.makeText(ActivityDetailActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);
        } else {
            Toast.makeText(ActivityDetailActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelVote(Boolean isWisher) {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(ActivityDetailActivity.this, "取消参与中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });
            String urlStr;
            if (isWisher) {
                urlStr = getResources().getString(R.string.ROOT) + "/activity/" + aid + "/wisher";
            } else {
                urlStr = getResources().getString(R.string.ROOT) + "/activity/" + aid + "/unwisher";
            }
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken());
            mStringRequest = new MyStringRequest(Request.Method.DELETE, urlStr, headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mProgressHUD.dismiss();
                            /*hasParticipate = true;
                            mJoinBtn.setText("取消参与");*/
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Log.e("PublishActivity:TAG", error.getMessage(), error);
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                    Toast.makeText(ActivityDetailActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);
        } else {
            Toast.makeText(ActivityDetailActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void doParticipate() {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(ActivityDetailActivity.this, "请求参与中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken());
            mStringRequest = new MyStringRequest(Request.Method.POST, getResources().getString(R.string.ROOT) + "/activity/" + aid + "/participant", headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mProgressHUD.dismiss();
                            hasParticipate = true;
                            participantCount++;
                            setJoinIfo(wisherCount, wisherTotal, participantCount);
                            mJoinBtn.setText("取消参与");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Log.e("PublishActivity:TAG", error.getMessage(), error);
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                    Toast.makeText(ActivityDetailActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);
        } else {
            Toast.makeText(ActivityDetailActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelParticipate() {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(ActivityDetailActivity.this, "取消参与中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken());
            mStringRequest = new MyStringRequest(Request.Method.DELETE, getResources().getString(R.string.ROOT) + "/activity/" + aid + "/participant", headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mProgressHUD.dismiss();
                            participantCount--;
                            setJoinIfo(wisherCount, wisherTotal, participantCount);

                            hasParticipate = false;
                            mJoinBtn.setText("要参与");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Log.e("PublishActivity:TAG", error.getMessage(), error);
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                    Toast.makeText(ActivityDetailActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);
        } else {
            Toast.makeText(ActivityDetailActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private int wisherCountStart;//开始时的
    private int wisherTotalStart;
    private int wisherCount;
    private int wisherTotal;
    private int participantCount;

    private void setJoinIfo(int wisherCnt, int wisherTot, int participantCnt) {
        String joinInfo = wisherTot + "人中有"
                + wisherCnt + "人感兴趣（"
                + (wisherTot == 0 ? 0 : wisherCnt * 100 / wisherTot)
                + "%)/ "
                + participantCnt
                + "人要参加";
        mWisherInfo.setText(joinInfo);
    }

    private void setData(JSONObject jsonobject) {
        try {
//            userAvatar = (Bitmap) bundle.getParcelable("avatar");//传递用户头像
//            activityImage = (Bitmap) bundle.getParcelable("image");//传递活动图片
            userAvatar = GlobalApplication.getUserAvatar();
            activityImage = GlobalApplication.getActivityImage();
            mActivityImage.setImageBitmap(activityImage); //活动图片
            mCreatedTime.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE3
                    , jsonobject.getLong("created_at")));//发布时间
            mActivityTitle.setText(jsonobject.getString("title"));//活动标题
            wisherCountStart = jsonobject.getInt("wisher_count");  //活动参与信息
            wisherTotalStart = jsonobject.getInt("wisher_total");
            participantCount = jsonobject.getInt("participant_count");
            setJoinIfo(wisherCountStart, wisherTotalStart, participantCount);
            wisherCount = wisherCountStart;
            wisherTotal = wisherTotalStart;
            //投票信息
            wishStatus = jsonobject.getInt("wish_relation");
            switch (wishStatus) {
                case WISH_YES:
                    wishType = WISH_YES;
                    checkBoxClickable = false;
                    mJoinActivity.setChecked(true);
                    mJoinActivity.setTextColor(getResources().getColor(R.color.check_selected));
                    break;
                case WISH_NO:
                    wishType = WISH_NO;
                    checkBoxClickable = false;
                    mNoJoinActivity.setChecked(true);
                    mNoJoinActivity.setTextColor(getResources().getColor(R.color.check_selected));
                    break;
                case WISH_NO_VOTE:
                    wishType = WISH_NO_VOTE;
                    break;
            }
            //按钮状态
            switch (jsonobject.getInt("participate_relation")) {
                case PARTICIPATE_YES:
                    hasParticipate = true;
                    mJoinBtn.setText("取消参与");
                    break;
                case PARTICIPATE_NO:
                    hasParticipate = false;
                    mJoinBtn.setText("要参与");
                    break;
            }

            mActivityTime.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE2, jsonobject.getLong("beginTime"))
                    + " —— " + DataUtils.stampToDate(DataUtils.DATA_TYPE2, jsonobject.getLong("endTime")));//活动时间范围

            mActivityAddress.setText(jsonobject.getString("address"));//地点
            mActivityFee.setText(jsonobject.getInt("fee") + "元");//活动费用
            mPublisherName.setText(jsonobject.getJSONObject("creator_obj").getString("name"));//发起人
            int notificationCount = jsonobject.getInt("notification_count");//通知数量
            if (notificationCount == 0) {
                mNotificationCount.setText("暂无通知");
            } else {
                mNotificationCount.setText(String.valueOf(notificationCount));
            }
            mActivityContent.setText(jsonobject.getString("content"));//活动详情
            JSONArray tagsJsonArray = jsonobject.getJSONArray("tags");//标签设置
            for (int i = 0; i < tagsJsonArray.length(); i++) {
                View v = LayoutInflater.from(ActivityDetailActivity.this).inflate(R.layout.text_tag_unit, mActivityTag, false);
                TextView tg = (TextView) v.findViewById(R.id.tag_name);
                tg.setText(tagsJsonArray.getString(i));
                mActivityTag.addView(v);
            }

            mCommentFragment.addData(jsonobject);//评论列表中加入数据
            commenTotal = jsonobject.getInt("total");
            setCommentCount(commenTotal);//评论数设置

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setCommentCount(int cnt) {//评论数设置
        mCommentCount.setText("评论（" + cnt + "）");
    }

}
