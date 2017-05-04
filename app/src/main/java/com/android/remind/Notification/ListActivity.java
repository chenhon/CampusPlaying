package com.android.remind.notification;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.adapter.NotificationListAdapter;
import com.android.BaseActivity;
import com.android.GlobalApplication;
import com.android.model.Notification;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.RequestManager;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
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

public class ListActivity extends BaseActivity {
    private static final int LOAD_DATA_COUNT = 5;//每页加载条数
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title_name)
    TextView mTitleName;
    @BindView(R.id.add_btn)
    TextView mAddBtn;
    @BindView(R.id.fl_title)
    FrameLayout mFlTitle;
    @BindView(R.id.line)
    View mLine;
    @BindView(R.id.lv_notification)
    ListView mLvNotification;
    @BindView(R.id.swipyrefreshlayout)
    SwipyRefreshLayout mSwipyrefreshlayout;
    @BindView(R.id.tv_emptyview)
    TextView mTvEmptyview;


    private NotificationListAdapter notificationAdapter;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private String rootString;
    private int aid;//依附的活动的id
    private int creatorId;//活动发起人id

    private int loadPage = 0;
    private int notificationTotal;


    /**
     * 启动该活动
     * @param activity
     * @param aid        通知依附的id
     * @param creatorId  通知发布者id
     */
    public static void startActivity(Activity activity, int aid, int creatorId) {
        Bundle bundle = new Bundle();
        bundle.putInt("aid", aid); //传递活动id
        bundle.putInt("creatorId", creatorId); //活动发起人id
        Intent intent = new Intent(activity, ListActivity.class);
        intent.putExtras(bundle);  //传入详细信息
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);
        ButterKnife.bind(this);


        mQueue = GlobalApplication.get().getRequestQueue();

        networkStatus = new NetworkConnectStatus(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            this.aid = bundle.getInt("aid");//活动id
            this.creatorId = bundle.getInt("creatorId");//用户id
            if (this.creatorId == GlobalApplication.getMySelf().getId()) {
                mAddBtn.setVisibility(View.VISIBLE); //活动发布者可以添加通知
            }
        }
        rootString = getResources().getString(R.string.ROOT);

        notificationAdapter = new NotificationListAdapter(this);
        mLvNotification.setAdapter(notificationAdapter);
        setListener();
      //  getNotificationListData();
    }

    @Override
    protected void onResume() {    //切换回来后，列表要进行刷新
        super.onResume();
        refreshData();
    }

    /**
     * 获取推荐用户
     */
    private void getNotificationListData() {
        if ((notificationTotal != 0) && (loadPage * LOAD_DATA_COUNT >= notificationTotal)) {
            mSwipyrefreshlayout.setRefreshing(false);
            Toast.makeText(this, "通知已加载完", Toast.LENGTH_SHORT).show();
            return;
        }
        if (networkStatus.isConnectInternet()) {
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("page", String.valueOf(loadPage + 1))
                    .with("count", String.valueOf(LOAD_DATA_COUNT)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString + "activity/" + aid + "/notification", urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getNOTIFICATION:TAG", response);
                            mSwipyrefreshlayout.setRefreshing(false);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                notificationTotal = jsonObject.getInt("total");//总条数
                                if (notificationTotal == 0) {
                                    mTvEmptyview.setVisibility(View.VISIBLE);
                                    return;
                                }
                                mTvEmptyview.setVisibility(View.GONE);
                                JSONArray jsonArr = jsonObject.getJSONArray("notifications");
                                Log.d("getNOTIFICATION:TAG", String.valueOf(jsonArr.length()));
                                for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                                    //适配器中添加数据项
                                    JSONObject jo = jsonArr.getJSONObject(i);
                                    Notification noti = new Notification();
                                    noti.setId(jo.getInt("id"));  //通知的id
                                    noti.setName(jo.getJSONObject("creator_obj").getString("name"));//发布人名称
                                    noti.setTitle(jo.getString("title"));//通知标题
                                    noti.setAvatarId(jo.getJSONObject("creator_obj").getInt("avatar"));//发布者头像
                                    noti.setCreatorId(jo.getJSONObject("creator_obj").getInt("id"));//发布者id
                                    noti.setContent(jo.getString("content"));//通知内容
                                    notificationAdapter.addNotificationListItem(noti);
                                }
                                notificationAdapter.notifyDataSetChanged();
                                if (jsonArr.length() > 0) {
                                    loadPage++;
                                    Log.d("getNOTIFICATION:TAG", "通知更新");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("getNOTIFICATION:TAG", "出错");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mSwipyrefreshlayout.setRefreshing(false);
                            NetworkResponse response = error.networkResponse;
                            if(response != null){
                                String responseStr = new String(response.data);
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(responseStr);
                                    Integer errorCode = jsonObject.getInt("code");
                                    String errorMsg = jsonObject.getString("msg");
                                    Log.d("getNOTIFICATION:TAG", errorCode + ": " + errorMsg);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        } else {
            mSwipyrefreshlayout.setRefreshing(false);
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setListener() {
        //上下拉刷新事件监听
        mSwipyrefreshlayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
                    getNotificationListData();
                } else if(direction == SwipyRefreshLayoutDirection.TOP){
                    refreshData();
                }
//                Toast.makeText(getContext(),
//                        "Refresh triggered at "
//                                + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"), Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "Refresh triggered at "
                        + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"));
            }
        });

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//添加通知
                PublishActivity.startActivity(ListActivity.this, aid);
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//返回
                ListActivity.this.finish();
            }
        });

        //跳转到通知详情
        mLvNotification.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //        System.out.println("NotiListPosition:" + position);
                Intent intent = new Intent(ListActivity.this, DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Bundle bundle = new Bundle();
                bundle.putInt("aid", notificationAdapter.getNotificationId(position));//通知的id
                bundle.putInt("creatorId", creatorId); //发布活动者id（也就是发布通知者的id）
                intent.putExtras(bundle);  //传入详细信息
                ListActivity.this.startActivity(intent);
            }
        });
    }
    /**
     * 刷新，直接清掉所有数据然后再重新加载
     */
    private void refreshData() {
        notificationAdapter.clearListData();//清楚数据
        loadPage = 0;
        notificationTotal = 0;
        getNotificationListData();
    }


}
