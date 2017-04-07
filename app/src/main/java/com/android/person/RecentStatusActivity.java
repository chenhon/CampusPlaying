package com.android.person;

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
import com.android.activity.ActivityDetailActivity;
import com.android.adapter.StatusesListAdapter;
import com.android.guide.BaseActivity;
import com.android.guide.GlobalApplication;
import com.android.model.Statuses;
import com.android.status.NotificationDetailActivity;
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

public class RecentStatusActivity extends BaseActivity {
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title_name)
    TextView mTitleName;
    @BindView(R.id.statusPullListView)
    PullToRefreshListView mStatusPullListView;

    public static final int RELATION_MYSLEF = 1;
    public static final int RELATION_OTHER = 2;

    int relation;//与当前用户的关系 1为自己， 2为他人
    int uid;//用户id
    private static final int LOAD_DATA_COUNT = 10;//每页加载10条数据
    private String rootString;

    private ListView statusesListView;
    private StatusesListAdapter statusesAdapter;

    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;

    private int totalStatusCount;//总条数
    private int currentPage;//获取的当前页数
    private int pageCount;//每页的条数
    private int startOrder; //当前页的起始序号
    private int endOrder;//结束序号

    /**
     * 启动当前活动
     * @param activity
     * @param relation
     * @param uid
     */
    public static void startActivity(Activity activity, int relation, int uid) {
        Bundle bundle = new Bundle();
        bundle.putInt("RELATION", relation);
        bundle.putInt("UID", uid);
        Intent intent = new Intent(activity, RecentStatusActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);  //传入详细信息
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_status);
        ButterKnife.bind(this);
        networkStatus = new NetworkConnectStatus(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            relation = bundle.getInt("RELATION");//与当前用户的关系 1为自己， 2为他人
            uid = bundle.getInt("UID");//用户id
        }

        if(relation == RELATION_MYSLEF) {
            rootString = getResources().getString(R.string.ROOT) + "user/~me/timeline/user";
            mTitleName.setText("我的动态");
        } else {
            rootString = getResources().getString(R.string.ROOT) + "user/"+ uid+ "/timeline/user";
            mTitleName.setText("他的动态");
        }

        initListView();
        setListener();
        getListData();
    }



    private void initListView() {
        statusesListView = mStatusPullListView.getRefreshableView();//获取动态列表控件
        statusesListView.setCacheColorHint(00000000);//此设置使得listview在滑动过程中不会出现黑色的背景
        statusesListView.setDivider(null);
    }

    private void setListener() {
        //ListView的item项点击事件
        statusesListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  //positoin是从1开始的
                        //Toast.makeText(getContext(), "item"+position + mStatuses.size(), Toast.LENGTH_SHORT).show();
                        switch (statusesAdapter.getAttachType(position - 1)) {
                            case Statuses.ACTIVITY_TYPE:   //进入活动详情页
                                Bundle bundle = new Bundle();
                                //应该是bitmap文件过大会传输失败
//                        bundle.putParcelable("avatar", statusesAdapter.getAvatar(position-1)); //传递用户头像
//                        bundle.putParcelable("image", statusesAdapter.getActivityImage(position-1)); //传递活动图片
                                GlobalApplication.setUserAvatar(statusesAdapter.getAvatar(position - 1));
                                GlobalApplication.setActivityImage(statusesAdapter.getActivityImage(position - 1));
                                bundle.putString("jsonStr", statusesAdapter.getAttachObj(position - 1).toString()); //传递活动详情的json数据
                                Intent intent = new Intent(RecentStatusActivity.this, ActivityDetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtras(bundle);  //传入详细信息
                                RecentStatusActivity.this.startActivity(intent);
                                break;
                            case Statuses.NOTIFICATION_TYPE: //进入通知详情页
                                Bundle bundle1 = new Bundle();
                                bundle1.putString("jsonStr", statusesAdapter.getAttachObj(position - 1).toString()); //传递通知详情的json数据
                                Intent intent1 = new Intent(RecentStatusActivity.this, NotificationDetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent1.putExtras(bundle1);  //传入详细信息
                                RecentStatusActivity.this.startActivity(intent1);
                                break;
                            case Statuses.PHOTO_TYPE:

                                break;
                            default:
                                break;
                        }

                    }

                }
        );

        mStatusPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            //下拉刷新
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                new GetDataTask().execute();
            }

            //上拉加载更多
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                statusLoadMoreData();
            }
        });
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void getListData() {
        statusesAdapter = new StatusesListAdapter(this);
        mStatusPullListView.setRefreshing(true);
        if (networkStatus.isConnectInternet()) {

            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("page", "4")    //获取第二页的数据
                    .with("count", String.valueOf(LOAD_DATA_COUNT)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", "123") // .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Log.d("getTIMELINE:TAG", response);
                            parseStatusJson(response); //将数据填入到List中去
                            mStatusPullListView.onRefreshComplete();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getTIMELINE:TAG", "出错");
                            Log.d("getTIMELINE:TAG", error.getMessage(), error);
                        }
                    });
            mQueue = GlobalApplication.get().getRequestQueue();
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        } else {
            mStatusPullListView.onRefreshComplete();
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * json数据转换成状态item并设置adapter
     *
     * @param json
     */
    private void parseStatusJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            totalStatusCount = jsonObject.getInt("total");//总条数
            currentPage = jsonObject.getInt("page");//获取的当前页数
            startOrder = jsonObject.getInt("start"); //当前页的起始序号
            pageCount = jsonObject.getInt("count"); //当前页的起始序号
            JSONArray jsonArr = jsonObject.getJSONArray("statues");

            for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                JSONObject jsonObject1 = jsonArr.getJSONObject(i);

                //适配器中添加数据项
                statusesAdapter.addHeaderStausesListItem(jsonObject1.getInt("attach_type")
                        , jsonObject1.getJSONObject("attach_obj"));
            }
            endOrder = startOrder + jsonArr.length();
            //statusesAdapter = new StatusesListAdapter(getActivity()); //初始化adapter
            statusesListView.setAdapter(statusesAdapter);//设置适配器

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }

    }

    /**
     * 列表头加载数据
     * 这里需要判断上次加载的数据是不是完整页
     * 如果不是完整的一页，则应该继续访问上次页面
     */
    private void statusAddNewData() {

    }

    /**
     * 动态加载更多
     */
    private void statusLoadMoreData() {

    }

    /**
     * 获取数据时先等待2S
     */
    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        /**
         * 子线程中执行
         *
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
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String[] result) {
            statusAddNewData();
            super.onPostExecute(result);
        }
    }
}
