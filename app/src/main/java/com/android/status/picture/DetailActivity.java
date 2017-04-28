package com.android.status.picture;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.guide.BaseActivity;
import com.android.GlobalApplication;
import com.android.model.Comment;
import com.android.person.PersonOnClickListenerImpl;
import com.android.status.CommentFragment;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.DataUtils;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.PictureView;
import com.android.tool.ProgressHUD;
import com.android.tool.ReplyDialog;
import com.android.tool.ReportUtil;
import com.android.tool.RequestManager;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class DetailActivity extends BaseActivity {
    private static final int LOAD_DATA_COUNT = 10;//每页加载10条数据
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.user_avatar)
    CircleImageView mUserAvatar;
    @BindView(R.id.user_name)
    TextView mUserName;
    @BindView(R.id.picture_time)
    TextView mPictureTime;
    @BindView(R.id.picture_content)
    TextView mPictureContent;
    @BindView(R.id.attached_activity_title)
    TextView mAttachedActivityTitle;
    @BindView(R.id.image)
    ImageView mImage;
    @BindView(R.id.activity_image)
    PictureView mActivityImage;
    @BindView(R.id.comment_count)
    TextView mCommentCount;
    @BindView(R.id.do_recommend)
    ImageView mDoRecommend;
    @BindView(R.id.comment_content)
    FrameLayout mCommentContent;
    @BindView(R.id.swipyrefreshlayout)
    SwipyRefreshLayout mSwipyrefreshlayout;
    @BindView(R.id.manage_btn)
    TextView mManageBtn;


    private CommentFragment mCommentFragment;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private String rootString;
    private ProgressHUD mProgressHUD;

    private int aid;//照片依附的活动id    （这里要能够点击进入活动里面）
    private int pid;//照片的id
    private int creatorId;//发布照片者id（一定是参与活动的人）

    private int loadPage = 0;
    private int commenTotal;

    /**
     * @param activity
     * @param jsonStr  json数据包含了图片id 活动id 发布者信息、图片评论数
     */
    public static void startActivity(Activity activity, String jsonStr) {
        Bundle bundle1 = new Bundle();
        bundle1.putString("jsonStr", jsonStr); //传递活动id
        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtras(bundle1);  //传入详细信息
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
    }

    /**
     * 启动该活动
     *
     * @param activity
     * @param pid       photo 的 id
     * @param creatorId 发布者
     */
    public static void startActivity(Activity activity, int pid, int creatorId) {
        Bundle bundle1 = new Bundle();
        bundle1.putInt("aid", pid);
        bundle1.putInt("creatorId", creatorId);
        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtras(bundle1);  //传入详细信息
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_detail);
        ButterKnife.bind(this);


        Bundle bundle = this.getIntent().getExtras();
        pid = bundle.getInt("aid");
        creatorId = bundle.getInt("creatorId"); //发布者id
        mCommentFragment = CommentFragment.newInstance(Comment.PHOTO_TYPE, this.pid, this.creatorId);//指定评论类型
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.comment_content, mCommentFragment);
        transaction.commit();

        if (creatorId == GlobalApplication.getMySelf().getId()) {
            mManageBtn.setText("删除");
        } else{
            mManageBtn.setText("举报");
        }
        rootString = getResources().getString(R.string.ROOT) + "photo/" + pid;
        networkStatus = new NetworkConnectStatus(this);
        mQueue = GlobalApplication.get().getRequestQueue();
        getPictureDetail();
        setListener();
    }

    private void getPictureDetail() {
        if (networkStatus.isConnectInternet()) {
            if ((commenTotal != 0) && (loadPage * LOAD_DATA_COUNT >= commenTotal)) {
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
                            try {
                                loadPage++;
                                Toast.makeText(DetailActivity.this, "加载成功".toString(), Toast.LENGTH_SHORT).show();
                                Log.d("ActivityDetail:TAG", response);
                                mSwipyrefreshlayout.setRefreshing(false);
                                JSONObject jsObject = new JSONObject(response);
                                setData(jsObject);
                                mCommentFragment.addData(jsObject);//评论列表中加入数据
                                commenTotal = jsObject.getInt("total");
                                setCommentCount(commenTotal);//评论数设置
                                if (commenTotal == 0) {
                                    Toast.makeText(DetailActivity.this, "暂无评论！", Toast.LENGTH_SHORT).show();
                                    return;
                                }
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

    /**
     * 根据传入的builder设置详情信息
     */
    private void setData(JSONObject jsonobject) {
        try {
            BitmapLoaderUtil.getInstance().getImage(mUserAvatar, BitmapLoaderUtil.TYPE_ORIGINAL, jsonobject.getJSONObject("creator_obj").getInt("avatar"));//发布者头像
            mUserName.setText(jsonobject.getJSONObject("creator_obj").getString("name"));//发布者名称
            mPictureTime.setText("照片上传于" + DataUtils.stampToDate(DataUtils.DATA_TYPE6, jsonobject.getLong("created_at")));//发布时间
            mPictureContent.setText(jsonobject.getString("description"));//照片描述
            BitmapLoaderUtil.getInstance().getImage(mImage, BitmapLoaderUtil.TYPE_MEDIAN, jsonobject.getInt("media_id"));//发布的图片

            aid = jsonobject.getInt("activity_id");//活动的id
            mAttachedActivityTitle.setText(jsonobject.getString("activity_title"));//活动标题
            //loadComment();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setCommentCount(int cnt) {//评论数设置
        mCommentCount.setText("评论（" + cnt + "）");
    }

    private void loadComment() {
        if (networkStatus.isConnectInternet()) {

            if ((commenTotal != 0) && (loadPage * LOAD_DATA_COUNT >= commenTotal)) {
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
                                if (commenTotal == 0) {
                                    Toast.makeText(DetailActivity.this, "暂无评论！", Toast.LENGTH_SHORT).show();
                                    return;
                                }
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

    private void setListener() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mManageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("删除".equals(mManageBtn.getText().toString())){
                    handleDelete();
                } else{//举报操作
                    new ReportUtil(DetailActivity.this, ReportUtil.TYPE_PICTURE, pid).doReport();
                }
            }
        });
        mDoRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ReplyDialog replyDialog = new ReplyDialog(DetailActivity.this);
                replyDialog.setHintText("评论通知...")
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
        mUserAvatar.setOnClickListener(new PersonOnClickListenerImpl(this, creatorId));
        mAttachedActivityTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, com.android.activity.DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("aid", aid);       //动态id
                bundle.putInt("creatorId", creatorId); //发布活动者id
                intent.putExtras(bundle);  //传入详细信息
                DetailActivity.this.startActivity(intent);
            }
        });

        //上下拉刷新事件监听
        mSwipyrefreshlayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
//                Toast.makeText(ActivityDetailActivity.this,
//                        "Refresh triggered at "
//                                + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"), Toast.LENGTH_SHORT).show();
                loadComment();
            }
        });
    }
    private void handleDelete() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("确定要删除该照片？");
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
                    System.out.println(GlobalApplication.getToken());
                    VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                            .with("token", GlobalApplication.getToken());
                    mStringRequest = new MyStringRequest(Request.Method.DELETE, rootString, headerParams, null,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(DetailActivity.this, "照片已删除".toString(), Toast.LENGTH_SHORT).show();
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
}
