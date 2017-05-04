package com.android.status.picture;

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

import com.android.GlobalApplication;
import com.android.R;
import com.android.adapter.PictureListAdapter;
import com.android.BaseActivity;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.RequestManager;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.json.JSONArray;
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
    @BindView(R.id.lv_picture)
    ListView mLvPicture;
    @BindView(R.id.swipyrefreshlayout)
    SwipyRefreshLayout mSwipyrefreshlayout;
    @BindView(R.id.tv_emptyview)
    TextView mTvEmptyview;

    private PictureListAdapter pictureAdapter;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private String rootString;
    private int aid;//照片依附的活动的id
    private int activityStatus;//活动当前的状态 0发起中   1进行中     2已结束
    private Boolean relation;//用户与活动之间的关系 true为活动参与者
    private int loadPage = 0;
    private int notificationTotal;


    /**
     *  启动该活动
     *  只有当活动结束时 活动的参与者才有资格上传照片
     * @param activity  跳转到该活动的活动
     * @param aid       活动的aid  （获取活动照片列表用）
     * @param status    活动状态
     * @param relation  当前活动与用户之间的关系
     */
    public static void startActivity(Activity activity, int aid, int status, Boolean relation) {
        Bundle bundle1 = new Bundle();
        bundle1.putInt("aid", aid); //传递活动id
        bundle1.putInt("status", status); //活动当前的状态
        bundle1.putBoolean("relation", relation); //用户与活动的关系
        Intent intent = new Intent(activity, ListActivity.class);
        intent.putExtras(bundle1);  //传入详细信息
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_list);
        ButterKnife.bind(this);
        mQueue = GlobalApplication.get().getRequestQueue();

        networkStatus = new NetworkConnectStatus(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            this.aid = bundle.getInt("aid");//活动id
            this.activityStatus = bundle.getInt("status");//活动的状态
            this.relation = bundle.getBoolean("relation");//活动与用户的关系
            if(relation == true) { //活动参与者可以上传照片
                mAddBtn.setVisibility(View.VISIBLE); //活动发布者可以添加通知
            } else {
                mAddBtn.setVisibility(View.GONE);
            }
        }
        rootString = getResources().getString(R.string.ROOT);

        pictureAdapter = new PictureListAdapter(this);
        mLvPicture.setAdapter(pictureAdapter);
        setListener();
    //    getPictureListData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    /**
     * 获取推荐用户
     */
    private void getPictureListData() {
        if ((notificationTotal != 0) && (loadPage * LOAD_DATA_COUNT >= notificationTotal)) {
          //  mSwipyrefreshlayout.setRefreshing(false);
            Toast.makeText(this, "通知已加载完", Toast.LENGTH_SHORT).show();
          //  return;
        }
        if (networkStatus.isConnectInternet()) {
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("page", String.valueOf(loadPage + 1))
                    .with("count", String.valueOf(LOAD_DATA_COUNT)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString + "activity/" + aid + "/photo", urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getPICTURE:TAG", response);
                            mSwipyrefreshlayout.setRefreshing(false);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                notificationTotal = jsonObject.getInt("total");//总条数
                                if (notificationTotal == 0) {
                                    // Toast.makeText(NotificationListActivity.this, "暂无推荐用户".toString(), Toast.LENGTH_SHORT).show();
                                    mTvEmptyview.setVisibility(View.VISIBLE);
                                    return;
                                }
                                mTvEmptyview.setVisibility(View.GONE);
                                JSONArray jsonArr = jsonObject.getJSONArray("photos");
                                Log.d("getPICTURE:TAG", String.valueOf(jsonArr.length()));
                                for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                                    //适配器中添加数据项
                                    pictureAdapter.addPictureListItem(jsonArr.getJSONObject(i));
                                }
                                if (jsonArr.length() > 0) {
                                    loadPage++;
                                    pictureAdapter.notifyDataSetChanged();
                                   // ListViewUtility.setListViewHeightBasedOnChildren(mLvPicture);
                                    Log.d("getPICTURE:TAG", "通知更新");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mSwipyrefreshlayout.setRefreshing(false);
                           /* Log.d("getPICTURE:TAG", "出错");
                            Log.d("getPICTURE:TAG", error.getMessage(), error);*/
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
                    getPictureListData();
                } else if (direction == SwipyRefreshLayoutDirection.TOP) {
                    refreshData();
                }
//                Toast.makeText(getContext(),
//                        "Refresh triggered at "
//                                + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"), Toast.LENGTH_SHORT).show();
                Log.d("getPICTURE", "Refresh triggered at "
                        + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"));
            }
        });

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//上传照片
                PublishActivity.startActivity(ListActivity.this,aid);
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//添加通知
                ListActivity.this.finish();
            }
        });

        //跳转到通知详情
        mLvPicture.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DetailActivity.startActivity(ListActivity.this, pictureAdapter.getPid(position), pictureAdapter.getCreatorId(position));
            }
        });
    }

    /**
     * 刷新，直接清掉所有数据然后再重新加载
     */
    private void refreshData() {
        pictureAdapter.clearListData();//清楚数据
        loadPage = 0;
        notificationTotal = 0;
        getPictureListData();
    }

}
