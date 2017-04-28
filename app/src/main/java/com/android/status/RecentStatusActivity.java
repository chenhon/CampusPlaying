package com.android.status;

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
import com.android.adapter.StatusesListAdapter;
import com.android.guide.BaseActivity;
import com.android.GlobalApplication;
import com.android.model.Statuses;
import com.android.status.picture.DetailActivity;
import com.android.tool.DataUtils;
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
    private static final int LOAD_DATA_COUNT = 1;//每页加载10条数据
    private static final int BACKWARD = 1; //加载更多 （默认）  ，这里只向后加载就可以了
    private static final int FORWARD = 0;   //刷新动态
    public static final int RELATION_MYSLEF = 1;
    public static final int RELATION_OTHER = 2;


    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title_name)
    TextView mTitleName;
    @BindView(R.id.statusPullListView)
    PullToRefreshListView mStatusPullListView;

    int relation;//与当前用户的关系 1为自己， 2为他人
    int uid;//用户id
    private String rootString;

    private ListView statusesListView;
    private StatusesListAdapter statusesAdapter;

    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;

    private int listTotal;//评论总数
    private int loadPage = 0;
    private long frontStamp;//最前面的时间戳  小
    private Boolean isLoadData = false;

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
    public static void startActivityForResult(Activity activity, int request, int relation, int uid) {
        Bundle bundle = new Bundle();
        bundle.putInt("RELATION", relation);
        bundle.putInt("UID", uid);
        Intent intent = new Intent(activity, RecentStatusActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);  //传入详细信息
        activity.startActivityForResult(intent, request);
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
        statusesAdapter = new StatusesListAdapter(this);
        statusesListView.setAdapter(statusesAdapter);//设置适配器
    }

    private void setListener() {
        //ListView的item项点击事件
        statusesListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  //positoin是从1开始的
                       // switch (statusesAdapter.getAttachType(position-1)) {
                            Intent intent = null;
                            switch (statusesAdapter.getAttachType(position-1)) {
                                case Statuses.ACTIVITY_TYPE:   //进入活动详情页
                                    intent = new Intent(RecentStatusActivity.this, com.android.activity.DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    break;
                                case Statuses.NOTIFICATION_TYPE: //进入通知详情页
                                    intent = new Intent(RecentStatusActivity.this, com.android.remind.notification.DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    break;
                                case Statuses.PHOTO_TYPE:  //进入照片详情页
                                    intent = new Intent(RecentStatusActivity.this, DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    break;
                                default: break;
                            }
                            Bundle bundle = new Bundle();
                            System.out.println("p11-" + statusesAdapter.getStatusId(position-1) + "-"+ statusesAdapter.getCreatorId(position-1));
                            bundle.putInt("aid", statusesAdapter.getStatusId(position-1));       //动态id
                            bundle.putInt("creatorId", statusesAdapter.getCreatorId(position-1)); //发布活动者id
                            intent.putExtras(bundle);  //传入详细信息
                            RecentStatusActivity.this.startActivity(intent);
                            /*
                            case Statuses.ACTIVITY_TYPE:   //进入活动详情页
                                Bundle bundle = new Bundle();
                                bundle.putInt("aid", statusesAdapter.getStatusId(position-1));       //动态
                                bundle.putInt("creatorId", statusesAdapter.getCreatorId(position-1)); //发布活动者id
                                Intent intent = new Intent(RecentStatusActivity.this, com.android.activity.DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtras(bundle);  //传入详细信息
                                RecentStatusActivity.this.startActivity(intent);
                                break;
                            case Statuses.NOTIFICATION_TYPE: //进入通知详情页
                                Bundle bundle1 = new Bundle();
                                bundle1.putString("jsonStr", statusesAdapter.getAttachObj(position-1).toString()); //传递通知详情的json数据
                                Intent intent1 = new Intent(RecentStatusActivity.this, com.android.remind.notification.DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent1.putExtras(bundle1);  //传入详细信息
                                RecentStatusActivity.this.startActivity(intent1);
                                break;
                            case Statuses.PHOTO_TYPE:
                                Bundle bundle2 = new Bundle();
                                bundle2.putString("jsonStr", statusesAdapter.getAttachObj(position-1).toString()); //传递通知详情的json数据
                                Intent intent2 = new Intent(RecentStatusActivity.this, DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent2.putExtras(bundle2);  //传入详细信息
                                RecentStatusActivity.this.startActivity(intent2);
                                break;
                            default: break;*/
                        }

             //       }

                }
        );

        mStatusPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            //下拉刷新
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

            }

            //上拉加载更多
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                new GetDataTask().execute();
            }
        });
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }


    private void getListData() {
/*
        if ((listTotal != 0) && (loadPage * LOAD_DATA_COUNT >= listTotal)) {
            Toast.makeText(this, "动态列表已加载完！", Toast.LENGTH_SHORT).show();
            mStatusPullListView.onRefreshComplete();
            return;
        }
*/


        if (networkStatus.isConnectInternet()) {
            if(!isLoadData){
                frontStamp = DataUtils.getCurrentTime();
            }
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("cursor",String.valueOf(frontStamp)) //时间戳游标
                    .with("count", String.valueOf(LOAD_DATA_COUNT)) //每页条数
                    .with("direction",String.valueOf(BACKWARD)); //加载方向，为加载最新
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
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
           // listTotal = jsonObject.getInt("total");//总条数
/*            if(0 == listTotal) {
                Toast.makeText(this, "暂无相关都能动态".toString(), Toast.LENGTH_SHORT).show();
                return;
            }*/

            JSONArray jsonArr = jsonObject.getJSONArray("statues");

            for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                JSONObject jsonObject1 = jsonArr.getJSONObject(i);
                if(i == jsonArr.length()-1) {
                    frontStamp = jsonObject1.getJSONObject("attach_obj").getLong("created_at");
                }

                //适配器中添加数据项
                statusesAdapter.addStatusListItem(jsonObject1.getInt("attach_type")
                        , jsonObject1.getJSONObject("attach_obj"));
            }
            if(jsonArr.length() > 0) {
                isLoadData = true;
                statusesAdapter.notifyDataSetChanged();
            } else {
                if(isLoadData) {
                    Toast.makeText(this, "动态列表已加载完".toString(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "暂无动态".toString(), Toast.LENGTH_SHORT).show();
                }
            }
/*            if ((listTotal != 0) && (loadPage * LOAD_DATA_COUNT >= listTotal)) {
                Toast.makeText(this, "动态列表已加载完！", Toast.LENGTH_SHORT).show();
                return;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }

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
            getListData();
            super.onPostExecute(result);
        }
    }
}
