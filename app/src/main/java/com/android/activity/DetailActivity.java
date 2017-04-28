package com.android.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.GlobalApplication;
import com.android.R;
import com.android.guide.BaseActivity;
import com.android.lbs.BNMainActivity;
import com.android.model.Comment;
import com.android.person.PersonOnClickListenerImpl;
import com.android.status.CommentFragment;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.DataUtils;
import com.android.tool.FlowLayout;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.ReplyDialog;
import com.android.tool.ReportUtil;
import com.android.tool.RequestManager;
import com.android.tool.ShareUtil;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 活动详情
 * 需要获取到 活动id ，发布者id  以及活动图片 和 发布者头像
 */
public class DetailActivity extends BaseActivity {
    private static final int LOAD_DATA_COUNT = 10;//每页加载10条数据
    private static final int WISH_YES = 1;//对活动感兴趣
    private static final int WISH_NO = -1;//对活动不感兴趣
    private static final int WISH_NO_VOTE = 0;//对活动没有投票
    private static final int PARTICIPATE_YES = 1;//已参与
    private static final int PARTICIPATE_NO = 0;//未参与
    @BindView(R.id.title_name)
    TextView mTitleName;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.activity_image)
    ImageView mActivityImage;
    @BindView(R.id.created_time)
    TextView mCreatedTime;
    @BindView(R.id.activity_title)
    TextView mActivityTitle;
    @BindView(R.id.wisher_info)
    TextView mWisherInfo;
    @BindView(R.id.join_activity)
    CheckBox mJoinActivity;
    @BindView(R.id.no_join_activity)
    CheckBox mNoJoinActivity;
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
    @BindView(R.id.publisher_name)
    TextView mPublisherName;
    @BindView(R.id.ll_to_creater_page)
    LinearLayout mLlToCreaterPage;
    @BindView(R.id.notification_count)
    TextView mNotificationCount;
    @BindView(R.id.ll_to_notify)
    LinearLayout mLlToNotify;
    @BindView(R.id.ll_to_comment)
    LinearLayout mLlToComment;
    @BindView(R.id.ll_to_picture)
    LinearLayout mLlToPicture;
    @BindView(R.id.activity_tag)
    FlowLayout mActivityTag;
    @BindView(R.id.activity_content)
    TextView mActivityContent;
    @BindView(R.id.comment_count)
    TextView mCommentCount;
    @BindView(R.id.do_comment)
    ImageView mDoComment;
    @BindView(R.id.commend_content)
    FrameLayout mCommendContent;
    @BindView(R.id.swipyrefreshlayout)
    SwipyRefreshLayout mSwipyrefreshlayout;
    @BindView(R.id.activity_type)
    TextView mActivityType;


    private CommentFragment mCommentFragment;//评论碎片
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
    private int loadPage = 0;

    private int activityStatus;//活动当前的状态 0发起中   1进行中     2已结束
    private double endLongitude;
    private double endLatitude;
    private String endAddress;
    private String aTitle;

    private JSONObject activityJsObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);


        Bundle bundle = this.getIntent().getExtras();
        aid = bundle.getInt("aid");
        creatorId = bundle.getInt("creatorId"); //发布者id
        //初始化评论
        mCommentFragment = CommentFragment.newInstance(Comment.ACTIVITY_TYPE, aid, creatorId);//指定评论类型
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.commend_content, mCommentFragment);
        transaction.commit();

        rootString = getResources().getString(R.string.ROOT) + "activity/" + aid;
        networkStatus = new NetworkConnectStatus(this);
        mQueue = GlobalApplication.get().getRequestQueue();

        mToolbar.setNavigationIcon(R.drawable.global_back);
        setSupportActionBar(mToolbar);
        getDetail();
        setListener();
    }

    /**
     * 菜单栏
     * 用户发布的活动 菜单中有 分享-编辑-删除
     * 别人发布的活动 分享-举报
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        System.out.println("运行加载menu");
        if (creatorId == GlobalApplication.getMySelf().getId()) {    //根据用户与活动的关系来加载不同的menu
            System.out.println("运行自己活动的加载menu");
            getMenuInflater().inflate(R.menu.activity_myself_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.activity_other_menu, menu);
        }

        return true;
    }

    /**
     * 菜单栏事件处理
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share://分享
                ShareUtil.showShare(DetailActivity.this, aid, aTitle);
                break;
            case R.id.report://举报
                new ReportUtil(DetailActivity.this, ReportUtil.TYPE_ACTIVITY, aid).doReport();
                break;
            case R.id.edit://编辑
                EditActivity.startActivity(DetailActivity.this, activityJsObject.toString());
                break;
            case R.id.delete://删除
                handleDelete();
                break;
        }
        return true;
    }

    private void handleDelete() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("确定要删除该活动？");
        dialog.setCancelable(true);
        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (networkStatus.isConnectInternet()) {
                    mProgressHUD = ProgressHUD.show(DetailActivity.this, "删除中...", true, true, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mProgressHUD.dismiss();
                        }
                    });
                    VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                            .with("token", GlobalApplication.getToken());
                    mStringRequest = new MyStringRequest(Request.Method.DELETE, rootString, headerParams, null,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(DetailActivity.this, "活动已删除".toString(), Toast.LENGTH_SHORT).show();
                                    //删除处理？
                                    mProgressHUD.dismiss();
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mProgressHUD.dismiss();
                            Toast.makeText(DetailActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    mQueue = GlobalApplication.get().getRequestQueue();
                    mStringRequest.setRetryPolicy(new DefaultRetryPolicy(400 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    mQueue.add(mStringRequest);
                } else {
                    Toast.makeText(DetailActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }

    private void getDetail() {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(DetailActivity.this, "加载中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });

            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("count", String.valueOf(LOAD_DATA_COUNT))//条数
                    .with("page", String.valueOf(loadPage + 1)); //指定页数，默认第一页
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
                                Toast.makeText(DetailActivity.this, "加载失败".toString(), Toast.LENGTH_SHORT).show();

                                return;
                            }
                            try {
                                loadPage++;
                                Log.d("ActivityDetail:TAG", response);
                                mProgressHUD.dismiss();
                                activityJsObject = new JSONObject(response);
                                setData(activityJsObject);

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
                    Toast.makeText(DetailActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);
        } else {

            Toast.makeText(DetailActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
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
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mDoComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ReplyDialog replyDialog = new ReplyDialog(DetailActivity.this);
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
        //上下拉刷新事件监听
        mSwipyrefreshlayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
//                Toast.makeText(ActivityDetailActivity.this,
//                        "Refresh triggered at "
//                                + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"), Toast.LENGTH_SHORT).show();
                loadMorecomment();
            }
        });

        //跳转到通知列表
        mLlToNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.android.remind.notification.ListActivity.startActivity(DetailActivity.this, aid, creatorId);
            }
        });

        //跳转到活动相册
        mLlToPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //暂时不看发布照片的权限
                com.android.status.picture.ListActivity.startActivity(DetailActivity.this, aid, 2, true);
            }
        });

        //地点导航
        mLlToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BNMainActivity.startActivity(DetailActivity.this, endLongitude, endLatitude, endAddress);
            }
        });
        //发动发起人主页面
        mLlToCreaterPage.setOnClickListener(new PersonOnClickListenerImpl(DetailActivity.this, creatorId));
    }

    /**
     * 投票
     *
     * @param isWisher true--投感兴趣
     *                 false--投不感兴趣
     */
    private void doVote(Boolean isWisher) {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(DetailActivity.this, "投票中...", true, true, new DialogInterface.OnCancelListener() {
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
                    Toast.makeText(DetailActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);
        } else {
            Toast.makeText(DetailActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelVote(Boolean isWisher) {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(DetailActivity.this, "取消参与中...", true, true, new DialogInterface.OnCancelListener() {
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
                    Toast.makeText(DetailActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);
        } else {
            Toast.makeText(DetailActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void doParticipate() {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(DetailActivity.this, "请求参与中...", true, true, new DialogInterface.OnCancelListener() {
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
                    Toast.makeText(DetailActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);
        } else {
            Toast.makeText(DetailActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelParticipate() {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(DetailActivity.this, "取消参与中...", true, true, new DialogInterface.OnCancelListener() {
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
                            mJoinBtn.setText("参  与");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Log.e("PublishActivity:TAG", error.getMessage(), error);
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                    Toast.makeText(DetailActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);
        } else {
            Toast.makeText(DetailActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
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
            BitmapLoaderUtil.getInstance().getImage(mActivityImage, BitmapLoaderUtil.TYPE_MEDIAN, jsonobject.getInt("image"));//活动图片
            mCreatedTime.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE3
                    , jsonobject.getLong("created_at")));//发布时间
            aTitle = jsonobject.getString("title");
            mActivityTitle.setText(aTitle);//活动标题
            wisherCountStart = jsonobject.getInt("wisher_count");  //活动参与信息
            wisherTotalStart = jsonobject.getInt("wisher_total");
            participantCount = jsonobject.getInt("participant_count");
            setJoinIfo(wisherCountStart, wisherTotalStart, participantCount);
            wisherCount = wisherCountStart;
            wisherTotal = wisherTotalStart;

            activityStatus = jsonobject.getInt("state");//活动的状态
            if (activityStatus == 0) { //活动处于发起中
                mLlToPicture.setVisibility(View.GONE);   //影藏相册栏
            }
            mJoinBtn.setOnClickListener(new View.OnClickListener() {//根据活动状态设置参与按钮事件
                @Override
                public void onClick(View v) {
                    if (activityStatus == 0) { //活动处于发起中
                        if (hasParticipate) {   //执行取消参与操作
                            cancelParticipate();
                        } else {
                            doParticipate();
                        }
                    } else {
                        Toast.makeText(DetailActivity.this, "活动报名时间已结束！", Toast.LENGTH_SHORT).show();
                    }
                }
            });
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
                    + " — " + DataUtils.stampToDate(DataUtils.DATA_TYPE2, jsonobject.getLong("endTime")));//活动时间范围

            mActivityAddress.setText(jsonobject.getString("address"));//地点
            endLongitude = jsonobject.getJSONObject("location").getDouble("longitude");
            endLatitude = jsonobject.getJSONObject("location").getDouble("latitude");
            endAddress = jsonobject.getString("address");
            mActivityFee.setText(jsonobject.getInt("fee") + "元");//活动费用
            mPublisherName.setText(jsonobject.getJSONObject("creator_obj").getString("name"));//发起人
            int notificationCount = jsonobject.getInt("notification_count");//通知数量
            if (notificationCount == 0) {
                mNotificationCount.setText("暂无通知");
            } else {
                mNotificationCount.setText(String.valueOf(notificationCount));
            }
            mActivityContent.setText(jsonobject.getString("content"));//活动详情
            String[] mItems = getResources().getStringArray(R.array.activityTags);   //活动类型
            mActivityType.setText(mItems[jsonobject.getInt("category")]);
            JSONArray tagsJsonArray = jsonobject.getJSONArray("tags");//标签设置
            for (int i = 0; i < tagsJsonArray.length(); i++) {
                View v = LayoutInflater.from(DetailActivity.this).inflate(R.layout.text_tag_unit, mActivityTag, false);
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

    private void loadMorecomment() {
        if (networkStatus.isConnectInternet()) {

            if (loadPage * LOAD_DATA_COUNT >= commenTotal) {
                Toast.makeText(this, "评论已加载完！", Toast.LENGTH_SHORT).show();
                mSwipyrefreshlayout.setRefreshing(false);
                return;
            }
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("count", String.valueOf(LOAD_DATA_COUNT))//条数
                    .with("page", String.valueOf(loadPage + 1)); //指定页数，默认第一页
            VolleyRequestParams headerParams = new VolleyRequestParams()
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if ("null".equals(response) || null == response) {

                                Toast.makeText(DetailActivity.this, "加载失败".toString(), Toast.LENGTH_SHORT).show();
                                mSwipyrefreshlayout.setRefreshing(false);
                                return;
                            }
                            try {
                                loadPage++;
                                Toast.makeText(DetailActivity.this, "加载成功".toString(), Toast.LENGTH_SHORT).show();
                                Log.d("ActivityDetail:TAG", response);
                                mSwipyrefreshlayout.setRefreshing(false);
                                JSONObject jsObject = new JSONObject(response);
                                mCommentFragment.addData(jsObject);//评论列表中加入数据
                                commenTotal = jsObject.getInt("total");
                                setCommentCount(commenTotal);//评论数设置

                                if (loadPage * LOAD_DATA_COUNT >= commenTotal) {
                                    Toast.makeText(DetailActivity.this, "评论已加载完！", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                mSwipyrefreshlayout.setRefreshing(false);
                               /* Log.e("PublishActivity:TAG", error.getMessage(), error);
                                byte[] htmlBodyBytes = error.networkResponse.data;
                                Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);*/
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mSwipyrefreshlayout.setRefreshing(false);
                    Log.e("PublishActivity:TAG", error.getMessage(), error);
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                    Toast.makeText(DetailActivity.this, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            executeRequest(mStringRequest);
        } else {

            Toast.makeText(DetailActivity.this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void setCommentCount(int cnt) {//评论数设置
        mCommentCount.setText("评论（" + cnt + "）");
    }

}
