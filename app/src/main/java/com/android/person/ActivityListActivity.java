package com.android.person;

import android.app.Activity;
import android.content.Intent;
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
import com.android.guide.BaseActivity;
import com.android.guide.GlobalApplication;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.RequestManager;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActivityListActivity extends BaseActivity {

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
        bundle.putLong("UID", uid);
        Intent intent = new Intent(activity, ActivityListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);  //传入详细信息
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

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
                if(relation == RELATION_MYSLEF) {
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
                if(relation == RELATION_MYSLEF) {
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
                if(relation == RELATION_MYSLEF) {
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
        getListData();
        setListener();
    }

    /**
     * 初始化列表的listView
     */
    private void initListView() {
        activityListView = mActivityPullListView.getRefreshableView();//获取动态列表控件
        activityListView.setCacheColorHint(00000000);//此设置使得listview在滑动过程中不会出现黑色的背景
        activityListView.setDivider(null);
    }

    /**
     * 设置监听事件
     */
    private void setListener() {

        //设置item点击时间    这里position是从1开始的
        activityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ActivityListActivity.this,"按了"+position, Toast.LENGTH_SHORT).show();
//                Bundle bundle = new Bundle();
//                GlobalApplication.setUserAvatar(privateAdapter.getAvatar(position-1));//传递对方头像
//                bundle.putString("jsonStr", privateAdapter.getJsonObj(position-1).toString()); //传递私信的json数据
//                Intent intent = new Intent(getActivity(), CommunicateActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.putExtras(bundle);  //传入详细信息
//                getActivity().startActivity(intent);
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 获取关注用户列表
     */
    private void getListData() {
        activityAdapter = new ActivityListAdapter(this);
        mActivityPullListView.setRefreshing(true);
        if (networkStatus.isConnectInternet()) {

            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    // .with("page","4")    //获取第二页的数据
                    .with("count",String.valueOf(10)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token","123") // .with("token", GlobalApplication.getToken())
                    .with("Accept","application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Log.d("getACTIVITY:TAG", response);
                            parseStatusJson(response); //将数据填入到List中去
                            activityListView.setAdapter(activityAdapter);//设置适配器
                            mActivityPullListView.onRefreshComplete();
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getACTIVITY:TAG", "出错");
                            Log.d("getACTIVITY:TAG", error.getMessage(),error);
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
//            totalStatusCount =jsonObject.getInt("total");//总条数
//            currentPage =jsonObject.getInt("page");//获取的当前页数
//            startOrder = jsonObject.getInt("start") ; //当前页的起始序号
//            pageCount = jsonObject.getInt("count") ; //当前页的起始序号
            JSONArray jsonArr = jsonObject.getJSONArray("activities");


            for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                //适配器中添加数据项
                activityAdapter.addActivityListItem(jsonArr.getJSONObject(i));
            }
            //endOrder = startOrder + jsonArr.length();
            //statusesAdapter = new StatusesListAdapter(getActivity()); //初始化adapter

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getFOLLOW:TAG", e.toString());
        }

    }
}
