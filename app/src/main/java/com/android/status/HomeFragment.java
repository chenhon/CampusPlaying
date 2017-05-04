package com.android.status;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.activity.PublishActivity;
import com.android.adapter.StatusesListAdapter;
import com.android.GlobalApplication;
import com.android.model.Statuses;
import com.android.search.SearchActivity;
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

import static com.android.R.id.statusesPullListView;

/**
 * 修改，活动传递数据，现在全部修改成值传递id
 */


public class HomeFragment extends Fragment {
    @BindView(R.id.iv_search)
    ImageView mIvSearch;
    @BindView(R.id.iv_refresh)
    ImageView mIvRefresh;
    @BindView(R.id.llSearchFrame)
    LinearLayout mLlSearchFrame;
    @BindView(R.id.add_activity_button)
    FloatingActionButton mAddActivityButton;
    @BindView(statusesPullListView)
    PullToRefreshListView mStatusesPullListView;
    @BindView(R.id.tv_emptyview)
    TextView mTvEmptyView;

    private static final int LOAD_DATA_COUNT = 10;//每页加载10条数据
    private static final int BACKWARD = 1; //加载更多 （默认）
    private static final int FORWARD = 0;   //刷新动态
    private String rootString;

    private ListView statusesListView;
    private StatusesListAdapter statusesAdapter;

    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;

    private Boolean isLoadData = false;//记录是否已经加载了数据
    private long frontStamp;//最前面的时间戳  小
    private long endStamp; //最近加载动态的一条时间戳 大

    /**
     *动态呈现形式
     * 时间戳大
     * |
     * 时间戳小
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //rootString = getActivity().getResources().getString(R.string.ROOT) + "user/~me/timeline/home.json";
        rootString = getActivity().getResources().getString(R.string.ROOT) + "user/~me/timeline/home";
        networkStatus = new NetworkConnectStatus(getActivity());
        View view = inflater.inflate(R.layout.home_fragment, null);
        ButterKnife.bind(this, view);
        initListView();
        setListener();
        getListData();

        return view;
    }

    private void initListView() {
        statusesAdapter = new StatusesListAdapter(getActivity());
        statusesListView = mStatusesPullListView.getRefreshableView();//获取动态列表控件
        statusesListView.setAdapter(statusesAdapter);//设置适配器
        statusesListView.setCacheColorHint(00000000);//此设置使得listview在滑动过程中不会出现黑色的背景
        statusesListView.setDivider(null);
    }

    private void setListener() {

        //刷新按钮事件监听
        mIvRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //执行下拉刷新操作
                mStatusesPullListView.setRefreshing(true);
                Toast.makeText(getContext(), "执行刷新!", Toast.LENGTH_SHORT).show();

            }
        });

        //搜索条点击事件监听
        mIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到搜索界面
                Toast.makeText(getContext(), "跳转到搜索界面!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                // Intent intent = new Intent(SplashActivity.this, SearchActivity.class);
                getActivity().startActivity(intent);
            }
        });
        //添加活动按钮事件监听
        mAddActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), PublishActivity.class);
                startActivity(intent);
            }
        });

        //ListView的item项点击事件
        statusesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  //positoin是从1开始的
                Intent intent = null;
                switch (statusesAdapter.getAttachType(position-1)) {
                    case Statuses.ACTIVITY_TYPE:   //进入活动详情页
                        intent = new Intent(getActivity(), com.android.activity.DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        break;
                    case Statuses.NOTIFICATION_TYPE: //进入通知详情页
                        intent = new Intent(getActivity(), com.android.remind.notification.DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        break;
                    case Statuses.PHOTO_TYPE:  //进入照片详情页
                        intent = new Intent(getActivity(), DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        break;
                    default: break;
                }
                Bundle bundle = new Bundle();
                System.out.println("p11-" + statusesAdapter.getStatusId(position-1) + "-"+ statusesAdapter.getCreatorId(position-1));
                bundle.putInt("aid", statusesAdapter.getStatusId(position-1));       //动态id
                bundle.putInt("creatorId", statusesAdapter.getCreatorId(position-1)); //发布活动者id
                intent.putExtras(bundle);  //传入详细信息
                getActivity().startActivity(intent);
                }

            }
        );

        mStatusesPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            //下拉刷新
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                refreshData();
               // new GetDataTask().execute();
            }

            //上拉加载更多
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                statusLoadMoreData();
            }
        });

    }


    private void getListData() {

     //   mStatusesPullListView.setRefreshing(true);
        if (networkStatus.isConnectInternet()) {

            //默认向后
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("count",String.valueOf(LOAD_DATA_COUNT))//条数
                    .with("cursor",String.valueOf(DataUtils.getCurrentTime())) //时间戳游标
                    .with("direction",String.valueOf(BACKWARD)); //加载方向，为加载最新
            VolleyRequestParams headerParams = new VolleyRequestParams()
                    .with("token", GlobalApplication.getToken())
                    .with("Accept","application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    Log.d("getTIMELINE:TAG", response);
                    parseStatusJson(response); //将数据填入到List中去
                    mStatusesPullListView.onRefreshComplete(); //刷新结束
                   // mIvRefresh.setClickable(true);//使能点击刷新
                }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                           // showVolleyError(error);
                            mStatusesPullListView.onRefreshComplete(); //刷新结束
                            mIvRefresh.setClickable(true);//使能点击刷新
                        }
                    });
            mQueue = GlobalApplication.get().getRequestQueue();
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
            mIvRefresh.setClickable(false);//失能点击刷新
        }else {
            mStatusesPullListView.onRefreshComplete(); //刷新结束
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * json数据转换成状态item并设置adapter
     * @param json
     *
     */
    private void parseStatusJson( String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArr = jsonObject.getJSONArray("statues");

            if(jsonArr.length() > 0) {
                isLoadData = true;
            }
            for (int i = 0; i < jsonArr.length() ; i++) {//最新的动态要放在最前面
                JSONObject jsonObject1 = jsonArr.getJSONObject(i);
                if(jsonArr.length()-1 == i) {//小时间戳
                    frontStamp = jsonObject1.getJSONObject("attach_obj").getLong("created_at");
                }
                if(0 == i) {  //大时间戳
                    endStamp = jsonObject1.getJSONObject("attach_obj").getLong("created_at");
                }

                //适配器中添加数据项
                statusesAdapter.addStatusListItem(jsonObject1.getInt("attach_type")
                        , jsonObject1.getJSONObject("attach_obj"));
            }
            statusesAdapter.notifyDataSetChanged();//适配器更新数据
         //   statusesListView.setAdapter(statusesAdapter);//设置适配器
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }

    }

    /**
     *加载最新动态
     */
    private void statusAddNewData() {
        if (networkStatus.isConnectInternet()) {

            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("count",String.valueOf(LOAD_DATA_COUNT)) //条数
                    .with("cursor",String.valueOf(endStamp)) //时间戳游标
                    .with("direction",String.valueOf(FORWARD)); //加载方向，为加载最新
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept","application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getTIMELINE:TAG", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonArr = jsonObject.getJSONArray("statues");

                                for (int i = jsonArr.length()-1; i >= 0 ; i--) {//最新的动态要放在最前面
                                    JSONObject jsonObject1 = jsonArr.getJSONObject(i);
                                    if(0 == i) {  //最近一条动态的时间戳
                                        endStamp = jsonObject1.getJSONObject("attach_obj").getLong("created_at");
                                    }

                                    //适配器中添加数据项
                                    statusesAdapter.addHeaderStausesListItem(jsonObject1.getInt("attach_type")
                                            , jsonObject1.getJSONObject("attach_obj"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("getTIMELINE:TAG", e.toString());
                            }
                            statusesAdapter.notifyDataSetChanged();//适配器更新数据
                            mStatusesPullListView.onRefreshComplete(); //刷新结束
                            mIvRefresh.setClickable(true);//使能点击刷新
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getTIMELINE:TAG", "出错");
                            Log.d("getTIMELINE:TAG", error.getMessage(),error);
                            mIvRefresh.setClickable(true);//使能点击刷新
                            mStatusesPullListView.onRefreshComplete();
                        }
                    });
            mQueue = GlobalApplication.get().getRequestQueue();
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
            mIvRefresh.setClickable(false);//失能点击刷新
        }else {
            mStatusesPullListView.onRefreshComplete();
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 动态加载更多
     */
    private void statusLoadMoreData() {
        if (networkStatus.isConnectInternet()) {

            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("count",String.valueOf(LOAD_DATA_COUNT)) //每页条数
                    .with("cursor",String.valueOf(frontStamp)) //时间戳游标
                    .with("direction",String.valueOf(BACKWARD)); //加载方向，为加载更多
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept","application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getTIMELINE:TAG", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonArr = jsonObject.getJSONArray("statues");

                                for (int i = 0; i < jsonArr.length(); i++) {
                                    JSONObject jsonObject1 = jsonArr.getJSONObject(i);
                                    if(jsonArr.length()-1 == i) {  //最前面一条动态的时间戳
                                        frontStamp = jsonObject1.getJSONObject("attach_obj").getLong("created_at");
                                    }

                                    statusesAdapter.addStatusListItem(jsonObject1.getInt("attach_type")
                                            , jsonObject1.getJSONObject("attach_obj"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("getTIMELINE:TAG", e.toString());
                            }
                            statusesAdapter.notifyDataSetChanged();//适配器更新数据
                            mStatusesPullListView.onRefreshComplete(); //刷新结束
                            mIvRefresh.setClickable(true);//使能点击刷新
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getTIMELINE:TAG", "出错");
                            Log.d("getTIMELINE:TAG", error.getMessage(),error);
                            mStatusesPullListView.onRefreshComplete(); //刷新结束
                            mIvRefresh.setClickable(true);//使能点击刷新
                        }
                    });
            mQueue = GlobalApplication.get().getRequestQueue();
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
            mIvRefresh.setClickable(false);//失能点击刷新
        }else {
            mStatusesPullListView.onRefreshComplete(); //刷新结束
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
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
            if(!isLoadData) {
                getListData();
            } else {
                statusAddNewData();
            }
            super.onPostExecute(result);
        }
    }

    /**
     * 刷新，直接清掉所有数据然后再重新加载
     */
    private void refreshData() {
        isLoadData = false;//记录是否已经加载了数据
        frontStamp = 0;//最前面的时间戳  小
        endStamp = 0; //最近加载动态的一条时间戳 大
        statusesAdapter.clearListData();//清楚数据
        getListData();
    }

}
