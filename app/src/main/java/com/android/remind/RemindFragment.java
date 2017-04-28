package com.android.remind;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.GlobalApplication;
import com.android.R;
import com.android.adapter.PrivateListAdapter;
import com.android.bottomnavigation.MainNavigationActivity;
import com.android.model.PrivateMsg;
import com.android.person.CommunicateActivity;
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
 * 通知页
 */
public class RemindFragment extends Fragment {
    private static final int LOAD_DATA_COUNT = 10;//每页加载10条数据
    @BindView(R.id.page_title)
    TextView mPageTitle;
    @BindView(R.id.ll_to_comment)
    LinearLayout mLlToComment;
    @BindView(R.id.ll_to_system_notify)
    LinearLayout mLlToSystemNotify;
    @BindView(R.id.msgsPullListView)
    PullToRefreshListView mMsgsPullListView;

    private PrivateListAdapter privateAdapter;
    private ListView privatesListView;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private String rootString;

    private int loadPage = 0;//已加载的页数
    private int privateTotal;//私信列表总条数

    private static final int REFRESH_COMPLETE = 1;//进入用户登录页
    private static final int DELAY_MILLIS = 1000; //1s延时
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_COMPLETE:
                    mMsgsPullListView.onRefreshComplete();
                    Toast.makeText(getActivity(), "消息已加载完", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mQueue = GlobalApplication.get().getRequestQueue();
        rootString = getActivity().getResources().getString(R.string.ROOT) + "msg/private";
        networkStatus = new NetworkConnectStatus(getActivity());
        View view = inflater.inflate(R.layout.remind_fragment, container, false);
        ButterKnife.bind(this, view);

        initListView();
        getPrivateListData();
        setListener();
        return view;
    }

    private void initListView() {
        privateAdapter = new PrivateListAdapter(getActivity());
        privatesListView = mMsgsPullListView.getRefreshableView();//获取动态列表控件
        privatesListView.setCacheColorHint(00000000);//此设置使得listview在滑动过程中不会出现黑色的背景
        privatesListView.setDivider(null);
        privatesListView.setAdapter(privateAdapter);//设置适配器
    }

    private void setListener() {

        //设置item点击时间    这里position是从1开始的
        privatesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   //进入聊天界面
                CommunicateActivity.startActivityForResult(getActivity(), MainNavigationActivity.REFRESH_REMIND_INFO,privateAdapter.getTargetId(position - 1),privateAdapter.getTargetAvatarId(position - 1), privateAdapter.getTargetName(position - 1));
            }
        });
/*        privatesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "长按了" + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
        //进入系统通知页
        mLlToSystemNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SystemMessageActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivity(intent);
            }
        });
        //进入评论列表
        mLlToComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CommentListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivity(intent);
            }
        });
        mMsgsPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            //下拉刷新
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //new HomeFragment.GetDataTask().execute();
                refreshPrivateListData();
            }

            //上拉加载更多
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                getPrivateListData();
            }
        });
    }

    private void getPrivateListData() {
        if ((privateTotal != 0) && (loadPage * LOAD_DATA_COUNT >= privateTotal)) {
           // mMsgsPullListView.onRefreshComplete();       //马上调用就会有消不掉转圈的bug
            mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, DELAY_MILLIS);
            return;
        }
        mMsgsPullListView.setRefreshing(true);
        if (networkStatus.isConnectInternet()) {

            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("page", String.valueOf(loadPage+1))
                    .with("count", String.valueOf(LOAD_DATA_COUNT)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Log.d("getUSERPRIVATE:TAG", response);
                            parseStatusJson(response); //将数据填入到List中去
                            mMsgsPullListView.onRefreshComplete();//结束刷新图标
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getTIMELINE:TAG", "出错");
                            Log.d("getTIMELINE:TAG", error.getMessage(), error);
                            mMsgsPullListView.onRefreshComplete();//结束刷新图标
                        }
                    });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        } else {
            mMsgsPullListView.onRefreshComplete();
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 刷新私信消息，
     * 这里先把私信清空，再加载
     */
    public void refreshPrivateListData() {
        loadPage = 0;
        privateTotal = 0;
        privateAdapter.clearPrivateListItem();
        getPrivateListData();
    }
    /**
     * json数据转换成状态item并设置adapter
     *
     * @param json
     */
    private void parseStatusJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            privateTotal = jsonObject.getInt("total");//总数
            JSONArray jsonArr = jsonObject.getJSONArray("privates");

            for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                //适配器中添加数据项
                JSONObject jo = jsonArr.getJSONObject(i);
                if(jo.getInt("target_id") == -255){  //管理员发布的(过滤掉)
                    continue;
                }
                PrivateMsg pm = new PrivateMsg();
                pm.setAvatarId(jo.getInt("avatar")); //对方头像id
                pm.setTargetName(jo.getString("target_name"));
                pm.setNoReadCount(jo.getInt("private_count"));//私信未读条数
                pm.setRecentContent(jo.getJSONObject("recent").getString("content"));//最近的一条私信内容
                pm.setTargetId(jo.getInt("target_id")); //对方id
                pm.setTotalCount(jo.getInt("private_total_count"));//私信总数
                pm.setRecentTime(jo.getJSONObject("recent").getLong("created_at"));//最近一条私信的时间
                privateAdapter.addPrivateListItem(pm);
            }
            privateAdapter.notifyDataSetChanged();
            if(jsonArr.length() > 0) {
                loadPage++;
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("PrivateList:TAG", e.toString());
        }

    }

}
