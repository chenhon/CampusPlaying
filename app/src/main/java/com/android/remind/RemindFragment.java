package com.android.remind;


import android.content.Intent;
import android.os.Bundle;
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

import com.android.R;
import com.android.adapter.PrivateListAdapter;
import com.android.guide.GlobalApplication;
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
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 通知页
 */
public class RemindFragment extends Fragment {


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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mQueue = GlobalApplication.get().getRequestQueue();
        rootString = getActivity().getResources().getString(R.string.ROOT) + "msg/private";
        networkStatus = new NetworkConnectStatus(getActivity());
        View view = inflater.inflate(R.layout.remind_fragment, container, false);
        ButterKnife.bind(this, view);
        initListView();

        setListener();
        getListData();

        return view;
    }
    private void initListView() {
        privatesListView = mMsgsPullListView.getRefreshableView();//获取动态列表控件
        privatesListView.setCacheColorHint(00000000);//此设置使得listview在滑动过程中不会出现黑色的背景
        privatesListView.setDivider(null);
    }

    private void setListener() {

        //设置item点击时间    这里position是从1开始的
        privatesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                GlobalApplication.setUserAvatar(privateAdapter.getAvatar(position-1));//传递对方头像
                bundle.putString("jsonStr", privateAdapter.getJsonObj(position-1).toString()); //传递私信的json数据
                Intent intent = new Intent(getActivity(), CommunicateActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);  //传入详细信息
                getActivity().startActivity(intent);
            }
        });
        privatesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(),"长按了"+position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
    private void getListData() {
        privateAdapter = new PrivateListAdapter(getActivity());
        mMsgsPullListView.setRefreshing(true);
        if (networkStatus.isConnectInternet()) {

            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("page","4")    //获取第二页的数据
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
                            privatesListView.setAdapter(privateAdapter);//设置适配器
                            mMsgsPullListView.onRefreshComplete();
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
            mMsgsPullListView.onRefreshComplete();
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
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
            JSONArray jsonArr = jsonObject.getJSONArray("privates");

            for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                //适配器中添加数据项
                privateAdapter.addHeaderPrivateListItem(jsonArr.getJSONObject(i));
            }
            //endOrder = startOrder + jsonArr.length();
            //statusesAdapter = new StatusesListAdapter(getActivity()); //初始化adapter

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }

    }

}
