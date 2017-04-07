package com.android.remind;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.adapter.SystemMsgListAdapter;
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
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 系统消息详情页
 * 系统消息可删除，无需评论
 */
public class SystemMessageActivity extends BaseActivity {

    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.title_name)
    TextView mTitleName;
    @BindView(R.id.msgPullListView)
    PullToRefreshListView mMsgPullListView;

    private static int LOAD_DATA_COUNT = 10;
    private SystemMsgListAdapter msgAdapter;
    private ListView msgListView;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private String rootString;

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, SystemMessageActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_message);
        ButterKnife.bind(this);

        mQueue = GlobalApplication.get().getRequestQueue();
        networkStatus = new NetworkConnectStatus(this);
        rootString = getResources().getString(R.string.ROOT)
                +"msg/private/"+0;//系统消息的uid为0

        initListView();
        setListener();
        getListData();
    }

    /**
     * 初始化列表的listView
     */
    private void initListView() {
        msgListView = mMsgPullListView.getRefreshableView();//获取动态列表控件
        msgListView.setCacheColorHint(00000000);//此设置使得listview在滑动过程中不会出现黑色的背景
        msgListView.setDivider(null);
    }
    /**
     * 设置监听事件
     */
    private void setListener() {

/*        //设置item点击时间    这里position是从1开始的
        msgListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(AlbumActivity.this, "按了" + position, Toast.LENGTH_SHORT).show();
//                Bundle bundle = new Bundle();
//                GlobalApplication.setUserAvatar(privateAdapter.getAvatar(position-1));//传递对方头像
//                bundle.putString("jsonStr", privateAdapter.getJsonObj(position-1).toString()); //传递私信的json数据
//                Intent intent = new Intent(getActivity(), CommunicateActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.putExtras(bundle);  //传入详细信息
//                getActivity().startActivity(intent);
            }
        });*/

        mMsgPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            //下拉刷新
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                new SystemMessageActivity.GetDataTask().execute();
            }

            //上拉加载更多
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                msgLoadMoreData();
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
        msgAdapter = new SystemMsgListAdapter(this);
        mMsgPullListView.setRefreshing(true);
        if (networkStatus.isConnectInternet()) {
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("page","1")    //加载第一页
                    .with("count", String.valueOf(LOAD_DATA_COUNT)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getPHOTO:TAG", response);
                            parseStatusJson(response); //将数据填入到List中去
                            msgListView.setAdapter(msgAdapter);//设置适配器
                            mMsgPullListView.onRefreshComplete();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getTIMELINE:TAG", "出错");
                            Log.d("getTIMELINE:TAG", error.getMessage(), error);
                        }
                    });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        } else {
            mMsgPullListView.onRefreshComplete();
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
            JSONArray jsonArr = jsonObject.getJSONArray("items");
            for (int i = 0; i < jsonArr.length(); i++) {
                msgAdapter.addMagListItem(jsonArr.getJSONObject(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getFOLLOW:TAG", e.toString());
        }
    }

    /**
     * 上拉加载更多
     */
    private void msgLoadMoreData() {

    }

    /**
     * 下拉刷新数据
     */
    private void msgAddNewData() {

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

            msgAddNewData();
            super.onPostExecute(result);
        }
    }
}
