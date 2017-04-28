package com.android.remind;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.adapter.CommentToMeListAdapter;
import com.android.GlobalApplication;
import com.android.model.Comment;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.RequestManager;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentListActivity extends AppCompatActivity {
    private static int LOAD_DATA_COUNT = 10;
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title_name)
    TextView mTitleName;
    @BindView(R.id.commentPullListView)
    PullToRefreshListView mCommentPullListView;
    @BindView(R.id.tv_emptyview)
    TextView mTvEmptyview;

    private NetworkConnectStatus networkStatus;//网络连接状态
    private String rootString;
    private CommentToMeListAdapter mCommentListAdapter;
    private ListView commentListView;

    private int loadPage = 0;
    private int commentTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_list);
        ButterKnife.bind(this);

        networkStatus = new NetworkConnectStatus(this);
        rootString = getResources().getString(R.string.ROOT)
                + "msg/comment";//获取评论列表

        initListView();
        setListener();
        getCommentListData();
    }

    /**
     * 初始化列表的listView
     */
    private void initListView() {
        commentListView = mCommentPullListView.getRefreshableView();//获取动态列表控件
        commentListView.setCacheColorHint(00000000);//此设置使得listview在滑动过程中不会出现黑色的背景
        commentListView.setDivider(null);
        mCommentListAdapter = new CommentToMeListAdapter(this);
        commentListView.setAdapter(mCommentListAdapter);
    }

    private void setListener() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mCommentPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            //下拉刷新
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                new GetDataTask().execute();
            }

            //上拉加载更多
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                getCommentListData();
            }
        });
    }

    /**
     * 获取推荐用户
     */
    private void getCommentListData() {
        if ((commentTotal != 0) && (loadPage * LOAD_DATA_COUNT >= commentTotal)) {
            Toast.makeText(this, "评论已加载完", Toast.LENGTH_SHORT).show();
            mCommentPullListView.onRefreshComplete(); //刷新结束
            return;
        }
        mCommentPullListView.setRefreshing(true);
        if (networkStatus.isConnectInternet()) {
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("page", String.valueOf(loadPage + 1))
                    .with("count", String.valueOf(LOAD_DATA_COUNT)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            MyStringRequest mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getCommentList:TAG", response);
                            mCommentPullListView.onRefreshComplete(); //刷新结束

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                commentTotal = jsonObject.getInt("total");//总条数
                                if (commentTotal == 0) {       //没有评论
                                    mTvEmptyview.setVisibility(View.VISIBLE);
                                    return;
                                }
                                JSONArray jsonArr = jsonObject.getJSONArray("comments");
                                for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                                    //适配器中添加数据项
                                    JSONObject jo = jsonArr.getJSONObject(i);
                                    Comment comment = new Comment();
                        //还没加attach）Content
                                    comment.setId(jo.getInt("id"));
                                    comment.setAttachType(jo.getInt("attach_type"));//依附类型
                                    comment.setAttachId(jo.getInt("attach_id"));//依附id
                                    comment.setAttachCreatorId(jo.getJSONObject("attach_obj").getInt("creator"));
                                    comment.setAttachCreatorName(jo.getJSONObject("attach_obj").getJSONObject("creator_obj").getString("name"));
                                    comment.setParentId(jo.getInt("parent"));//父评论id（没有则为0）
                                    comment.setName(jo.getJSONObject("creator_obj").getString("name"));//评论人名称
                                    comment.setCreatorId(jo.getJSONObject("creator_obj").getInt("id"));//评论者id
                                    comment.setAvatarId(jo.getJSONObject("creator_obj").getInt("avatar"));//发布者头像id
                                    comment.setCreatedTime(jo.getLong("created_at")); //评论时间
                                    comment.setContent(jo.getString("content"));//评论内容
                                    if(comment.getAttachType() == Comment.ACTIVITY_TYPE) { //依附的不是通知
                                        comment.setAttachImage(jo.getJSONObject("attach_obj").getInt("image"));
                                        comment.setAttachContent(jo.getJSONObject("attach_obj").getString("title"));//活动标题
                                    } else if (comment.getAttachType() == Comment.PHOTO_TYPE) { //依附的不是通知
                                        comment.setAttachImage(jo.getJSONObject("attach_obj").getInt("media_id"));
                                        comment.setAttachContent(jo.getJSONObject("attach_obj").getString("description"));//照片的描述
                                    } else{ //依附的是通知
                                        comment.setAttachContent(jo.getJSONObject("attach_obj").getString("title"));//通知标题
                                    }


                                    mCommentListAdapter.addCommentListItem(comment);
                                }
                                if (jsonArr.length() > 0) {
                                    loadPage++;
                                    mCommentListAdapter.notifyDataSetChanged();
                                    //Log.d("getNOTIFICATION:TAG", "通知更新");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                mCommentPullListView.onRefreshComplete(); //刷新结束
                                Log.d("getCommentList:TAG", e.toString());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mCommentPullListView.onRefreshComplete(); //刷新结束
                            Log.d("getNOTIFICATION:TAG", "出错");
                            Log.d("getNOTIFICATION:TAG", error.getMessage(), error);
                        }
                    });
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            GlobalApplication.get().getRequestQueue().add(mStringRequest);
        } else {
            mCommentPullListView.onRefreshComplete(); //刷新结束
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取数据时先等待2S
     */
    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        /**
         * 子线程中执行
         * @param params
         * @return
         */
        @Override
        protected String[] doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
                mCommentListAdapter.clearListItem();//清空数据
                loadPage = 0;
                commentTotal = 0;
            } catch (InterruptedException e) {

            }
            return null;
        }

        /**
         * 主线程中执行
         * @param result
         */
        @Override
        protected void onPostExecute(String[] result) {
            getCommentListData();
            super.onPostExecute(result);
        }
    }
}
