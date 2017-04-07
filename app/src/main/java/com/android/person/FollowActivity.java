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
import com.android.adapter.FollowListAdapter;
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

public class FollowActivity extends BaseActivity {

    public static final int RELATION_MYSLEF = 1;
    public static final int RELATION_OTHER = 2;
    public static final int DIRECTION_ATTENTION = 1;
    public static final int DIRECTION_FANS = 2;
    int relation;//与当前用户的关系 1为自己， 2为他人
    int direction;//是关注列表还是被关注列表 1为关注列表， 2位粉丝列表
    int uid;//用户id
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title_name)
    TextView mTitleName;
    @BindView(R.id.followPullListView)
    PullToRefreshListView mFollowPullListView;

    private FollowListAdapter followAdapter;
    private ListView followsListView;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private String rootString;

    /**
     * 启动本活动
     *
     * @param activity  启动该活动的活动
     * @param relation  与本人的关系
     * @param direction 关注列表还是被关注列表
     * @param uid       用户id
     */
    public static void startActivity(Activity activity, int relation, int direction, int uid) {
        Bundle bundle = new Bundle();
        bundle.putInt("RELATION", relation);
        bundle.putInt("DIRECTION", direction);
        bundle.putInt("UID", uid);
        Intent intent = new Intent(activity, FollowActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);  //传入详细信息
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.follow_list);
        ButterKnife.bind(this);

        mQueue = GlobalApplication.get().getRequestQueue();

        networkStatus = new NetworkConnectStatus(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            relation = bundle.getInt("RELATION");//与当前用户的关系 1为自己， 2为他人
            direction = bundle.getInt("DIRECTION");//是关注列表还是被关注列表 1为关注列表， 2位被关注列表
            uid = bundle.getInt("UID");//用户id
        }

        if(direction == DIRECTION_ATTENTION) { //关注列表
            if(relation == RELATION_MYSLEF) {
                rootString = getResources().getString(R.string.ROOT)
                        +"user/~me/follower";
                mTitleName.setText("我的关注列表");
            } else if(relation == RELATION_OTHER) {
                rootString = getResources().getString(R.string.ROOT)
                        +"user/"+uid+"/follower";
                mTitleName.setText("他的关注列表");
            }
        } else if(direction == DIRECTION_FANS) { //粉丝列表
            if(relation == RELATION_MYSLEF) {
                rootString = getResources().getString(R.string.ROOT)
                        +"user/~me/fan";
                mTitleName.setText("我的粉丝列表");
            } else if(relation == RELATION_OTHER) {
                rootString = getResources().getString(R.string.ROOT)
                        +"user/"+uid+"/fan";
                mTitleName.setText("他的粉丝列表");
            }
        }

        initListView();
        getListData();
        setListener();
    }

    /**
     * 初始化列表的listView
     */
    private void initListView() {
        followsListView = mFollowPullListView.getRefreshableView();//获取动态列表控件
        followsListView.setCacheColorHint(00000000);//此设置使得listview在滑动过程中不会出现黑色的背景
        followsListView.setDivider(null);
    }

    /**
     * 设置监听事件
     */
    private void setListener() {

        //设置item点击时间    这里position是从1开始的
        followsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(FollowActivity.this,"长按了"+position, Toast.LENGTH_SHORT).show();
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
        followAdapter = new FollowListAdapter(this);
        mFollowPullListView.setRefreshing(true);
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

                            Log.d("getUSERPRIVATE:TAG", response);
                            parseStatusJson(response); //将数据填入到List中去
                            followsListView.setAdapter(followAdapter);//设置适配器
                            mFollowPullListView.onRefreshComplete();
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getTIMELINE:TAG", "出错");
                            Log.d("getTIMELINE:TAG", error.getMessage(),error);
                        }
                    });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        }else {
            mFollowPullListView.onRefreshComplete();
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
            JSONArray jsonArr;
            if(DIRECTION_ATTENTION == direction) {
                jsonArr = jsonObject.getJSONArray("followers");
            } else{
                jsonArr = jsonObject.getJSONArray("fans");
            }

            for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                //适配器中添加数据项
                followAdapter.addFollowListItem(jsonArr.getJSONObject(i));
            }
            //endOrder = startOrder + jsonArr.length();
            //statusesAdapter = new StatusesListAdapter(getActivity()); //初始化adapter

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getFOLLOW:TAG", e.toString());
        }

    }
}
