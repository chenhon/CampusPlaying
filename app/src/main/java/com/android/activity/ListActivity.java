package com.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.adapter.ActivityListAdapter;
import com.android.BaseActivity;
import com.android.GlobalApplication;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.RequestManager;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends BaseActivity {
    private static final int LOAD_DATA_COUNT = 4;//每页加载10条数据
    public static final int TYPE_PARTICIPATED = 1;  //参加的活动
    public static final int TYPE_INTERESTED = 2;    //感兴趣的活动
    public static final int TYPE_PUBLISHED = 3;     //发布的活动
    public static final int RELATION_MYSLEF = 1;
    public static final int RELATION_OTHER = 2;
    int relation;//与当前用户的关系 1为自己， 2为他人
    int activityType;//是关注列表还是被关注列表 1为关注列表， 2位粉丝列表
    int uid;//用户id

    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title_name)
    TextView mTitleName;
    @BindView(R.id.activityPullListView)
    PullToRefreshListView mActivityPullListView;

    private ActivityListAdapter activityAdapter;
    private ListView activityListView;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private String rootString;

    private int listTotal;//评论总数
    private int loadPage = 0;

    /**
     * 启动该活动
     * @param activity
     * @param relation
     * @param type
     * @param uid
     */
    public static void startActivity(Activity activity, int relation, int type, int uid) {
        Bundle bundle = new Bundle();
        bundle.putInt("RELATION", relation);
        bundle.putInt("TYPE", type);
        bundle.putInt("UID", uid);
        Intent intent = new Intent(activity, ListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);  //传入详细信息
        activity.startActivity(intent);
    }
    public static void startActivityForResult(Activity activity, int request, int relation, int type, int uid) {
        Bundle bundle = new Bundle();
        bundle.putInt("RELATION", relation);
        bundle.putInt("TYPE", type);
        bundle.putInt("UID", uid);
        Intent intent = new Intent(activity, ListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);  //传入详细信息
        activity.startActivityForResult(intent, request);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);
        setResult(RESULT_OK);
        mQueue = GlobalApplication.get().getRequestQueue();

        networkStatus = new NetworkConnectStatus(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            relation = bundle.getInt("RELATION");//与当前用户的关系 1为自己， 2为他人
            activityType = bundle.getInt("TYPE");//要进入的列表 已参与的、感兴趣的、已发布的
            uid = bundle.getInt("UID");//用户id
        }

        switch(activityType) {
            case TYPE_PARTICIPATED:
                if(relation == RELATION_MYSLEF|| uid == GlobalApplication.getMySelf().getId()) {
                    rootString = getResources().getString(R.string.ROOT)
                            + "user/~me/activity/participated";
                    mTitleName.setText("我参与的活动");
                } else {
                    rootString = getResources().getString(R.string.ROOT)
                            + "user/"+uid+"/activity/participated";
                    mTitleName.setText("他参与的活动");
                }
                break;
            case TYPE_INTERESTED:
                if(relation == RELATION_MYSLEF|| uid == GlobalApplication.getMySelf().getId()) {
                    rootString = getResources().getString(R.string.ROOT)
                            + "user/~me/activity/wished";
                    mTitleName.setText("我感兴趣的活动");
                } else {
                    rootString = getResources().getString(R.string.ROOT)
                            + "user/"+uid+"/activity/wished";
                    mTitleName.setText("他感兴趣的活动");
                }
                break;
            case TYPE_PUBLISHED:
                if(relation == RELATION_MYSLEF|| uid == GlobalApplication.getMySelf().getId()) {
                    rootString = getResources().getString(R.string.ROOT)
                            + "user/~me/activity/created";
                    mTitleName.setText("我发布的活动");
                } else {
                    rootString = getResources().getString(R.string.ROOT)
                            + "user/"+uid+"/activity/created";
                    mTitleName.setText("他发布的活动");
                }
                break;

        }

        initListView();
      //  getListData();
        setListener();
    }
    @Override
    protected void onResume() {    //切换回来后，列表要进行刷新
        super.onResume();
        refreshData();
    }
    /**
     * 初始化列表的listView
     */
    private void initListView() {
        activityListView = mActivityPullListView.getRefreshableView();//获取动态列表控件
        activityListView.setCacheColorHint(00000000);//此设置使得listview在滑动过程中不会出现黑色的背景
        activityListView.setDivider(null);
        activityAdapter = new ActivityListAdapter(this);
        activityListView.setAdapter(activityAdapter);//设置适配器
    }

    /**
     * 设置监听事件
     */
    private void setListener() {

        //设置item点击时间    这里position是从1开始的
        activityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putInt("aid", activityAdapter.getAid(position-1)); //活动id
                bundle.putInt("creatorId", activityAdapter.getUid(position-1)); //发布活动着id
                // bundle.putString("jsonStr", statusesAdapter.getAttachObj(position-1).toString()); //传递活动详情的json数据
                Intent intent = new Intent(ListActivity.this, DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);  //传入详细信息
                ListActivity.this.startActivity(intent);
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mActivityPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            //下拉刷新
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                refreshData();
            }
            //上拉加载更多
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                new ListActivity.GetDataTask().execute();
            }
        });
    }

    /**
     * 获取关注用户列表
     */
    private void getListData() {
        if ((listTotal != 0) && (loadPage * LOAD_DATA_COUNT >= listTotal)) {
            Toast.makeText(this, "活动列表已加载完！", Toast.LENGTH_SHORT).show();
            //mActivityPullListView.onRefreshComplete();
            //return;
        }
        if (networkStatus.isConnectInternet()) {
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("page",String.valueOf(loadPage+1))    //获取第二页的数据
                    .with("count",String.valueOf(LOAD_DATA_COUNT)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept","application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Log.d("getACTIVITY:TAG", response);
                            parseStatusJson(response); //将数据填入到List中去

                            mActivityPullListView.onRefreshComplete();
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            /*Log.d("getACTIVITY:TAG", "出错");
                            Log.d("getACTIVITY:TAG", error.getMessage(),error);*/
                            mActivityPullListView.onRefreshComplete();
                        }
                    });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        }else {
            mActivityPullListView.onRefreshComplete();
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * json数据转换成状态item并设置adapter
     * @param json
     *
     */
    private void parseStatusJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            listTotal = jsonObject.getInt("total");//总条数
            if(0 == listTotal) {
                Toast.makeText(this, "暂无相关活动".toString(), Toast.LENGTH_SHORT).show();
                return;
            }
            JSONArray jsonArr = jsonObject.getJSONArray("activities");

            for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                JSONObject jo = jsonArr.getJSONObject(i);
                //适配器中添加数据项
                com.android.model.Activity activity = new com.android.model.Activity();
                activity.setId(jo.getInt("id"));
                activity.setCreatorId(jo.getInt("creator"));
                activity.setCreatorName(jo.getJSONObject("creator_obj").getString("name"));
                activity.setAvatarId(jo.getJSONObject("creator_obj").getInt("avatar"));
                activity.setTitle(jo.getString("title"));
                activity.setContent(jo.getString("content"));
                activity.setImageId(jo.getInt("image"));
                activity.setTime(jo.getLong("created_at"));
                activity.setWisherCount(jo.getInt("wisher_count"));
                activity.setParticipantCount(jo.getInt("participant_count"));
                activity.setVerifyStatus(jo.getInt("verify_state"));
                activity.setState(jo.getInt("state"));
                activityAdapter.addActivityListItem(activity);
            }
            activityAdapter.notifyDataSetChanged();
            if(jsonArr.length() > 0) {
                loadPage++;
            }
            if ((listTotal != 0) && (loadPage * LOAD_DATA_COUNT >= listTotal)) {
                Toast.makeText(this, "活动列表已加载完！", Toast.LENGTH_SHORT).show();
                mActivityPullListView.onRefreshComplete();
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getFOLLOW:TAG", e.toString());
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
                Thread.sleep(2000);
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
            getListData();
            super.onPostExecute(result);
        }
    }

    /**
     * 刷新，直接清掉所有数据然后再重新加载
     */
    private void refreshData() {
        activityAdapter.clearListData();//清楚数据
        loadPage = 0;
        listTotal = 0;
        getListData();
    }
}
