package com.android.remind.notification;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.BaseActivity;
import com.android.GlobalApplication;
import com.android.model.Comment;
import com.android.status.CommentFragment;
import com.android.tool.DataUtils;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.ReplyDialog;
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

public class DetailActivity extends BaseActivity {
    private static final int LOAD_DATA_COUNT = 10;//每页加载10条数据
    @BindView(R.id.title_name)
    TextView mTitleName;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.notification_title)
    TextView mNotificationTitle;
    @BindView(R.id.notification_source)
    TextView mNotificationSource;
    @BindView(R.id.notification_content)
    TextView mNotificationContent;
    @BindView(R.id.comment_count)
    TextView mCommentCount;
    @BindView(R.id.do_recommend)
    ImageView mDoRecommend;
    @BindView(R.id.comment_content)
    FrameLayout mCommentContent;
    @BindView(R.id.swipyrefreshlayout)
    SwipyRefreshLayout mSwipyrefreshlayout;

    private ProgressHUD mProgressHUD;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private String rootString;
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    CommentFragment mCommentFragment;

    private int nid;      //通知的id
    private int creatorId;//发布通知者id（当然，这里发布通知的人一定是发布活动者）
    private String notificationTitle;
    private String notificationContent;


    private int commenTotal;//评论总数
    private int loadPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_detail);
        ButterKnife.bind(this);

        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null) {
            nid = bundle.getInt("aid");
            creatorId = bundle.getInt("creatorId");
        }

        //评论布局初始化
        mCommentFragment = CommentFragment.newInstance(Comment.NOTIFICATION_TYPE, nid, creatorId);//指定评论类型
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.comment_content, mCommentFragment);
        transaction.commit();
        rootString = getResources().getString(R.string.ROOT) + "notification/" + nid;
        //侧边栏的按钮
        mToolbar.setNavigationIcon(R.drawable.global_back);
        setSupportActionBar(mToolbar);
        networkStatus = new NetworkConnectStatus(this);
        mQueue = GlobalApplication.get().getRequestQueue();
    //    setData(mJsonobject);
        getNotificationDetail();
        setListener();
    }

    /**
     * 菜单栏
     * 用户发布的活动 菜单中有 编辑-删除
     * 别人发布的活动 举报
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        System.out.println("运行加载menu");
        if (creatorId == GlobalApplication.getMySelf().getId()) {    //根据用户与活动的关系来加载不同的menu
            System.out.println("运行自己活动的加载menu");
            getMenuInflater().inflate(R.menu.notification_myself_menu, menu);
        } /*else {
            getMenuInflater().inflate(R.menu.notification_other_menu, menu);
        }*/

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

           /* case R.id.report://举报
                new ReportUtil(DetailActivity.this, ReportUtil.TYPE_NOTIFICATION, nid).doReport();
                break;*/
            case R.id.edit://编辑
                EditActivity.startActivity(this, nid,notificationTitle,notificationContent);
                break;
            case R.id.delete://删除
                handleDelete();
                break;
        }
        return true;
    }

    private void handleDelete() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("确定要删除该通知？");
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
                                    Toast.makeText(DetailActivity.this, "通知已删除".toString(), Toast.LENGTH_SHORT).show();
                                    //删除处理？
                                    mProgressHUD.dismiss();
                                    DetailActivity.this.finish();//返回列表
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

    private void getNotificationDetail() {
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
                    showVolleyError(error);
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
            notificationTitle = jsonobject.getString("title");
            mNotificationTitle.setText(notificationTitle);
            notificationContent = jsonobject.getString("content");
            mNotificationContent.setText(notificationContent);
            mNotificationSource.setText(jsonobject.getJSONObject("creator_obj").getString("name")
                    + " " + DataUtils.stampToDate(DataUtils.DATA_TYPE4, jsonobject.getLong("created_at")));
         //   loadComment();
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
                            Log.d("NotificationDetail:TAG", response);
                            try {
                                loadPage++;
                                Toast.makeText(DetailActivity.this, "加载成功".toString(), Toast.LENGTH_SHORT).show();
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
        //返回
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
}
