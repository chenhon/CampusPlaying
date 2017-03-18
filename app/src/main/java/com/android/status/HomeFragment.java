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
import com.android.activity.ActivityDetailActivity;
import com.android.activity.PublishActivity;
import com.android.adapter.StatusesListAdapter;
import com.android.guide.GlobalApplication;
import com.android.model.Activity;
import com.android.model.Notification;
import com.android.model.Photo;
import com.android.model.Statuses;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.android.R.id.statusesPullListView;



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
    TextView mTvEmptyview;

    private static final int LOAD_DATA_COUNT = 10;//每页加载10条数据
    private String rootString;

    List<Statuses> mStatuses;
    private ListView statusesListView;
    private StatusesListAdapter statusesAdapter;

    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;

    private int totalStatusCount;//总条数
    private int currentPage;//获取的当前页数
    private int startOrder; //当前页的起始序号
    private int endOrder;//结束序号

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //rootString = getActivity().getResources().getString(R.string.ROOT) + "user/~me/timeline/home.json";
        rootString = getActivity().getResources().getString(R.string.ROOT) + "user/~me/timeline/home";
        networkStatus = new NetworkConnectStatus(getActivity());
        View view = inflater.inflate(R.layout.home_fragment, null);
        ButterKnife.bind(this, view);
        mStatuses = new ArrayList<>();
        initListView();
        setListener();
        getListData();


        //暂时的测试----------------------------------------------------------------------

//        mStatuses.add(new Statuses());
//        statusesAdapter = new StatusesListAdapter(getActivity(), mStatuses); //初始化adapter
//        statusesListView.setAdapter(statusesAdapter);//设置适配器
        //--------------------------------------------------------------------------------

        return view;
    }

    private void initListView() {
        statusesListView = mStatusesPullListView.getRefreshableView();//获取动态列表控件
        statusesListView.setCacheColorHint(00000000);//此设置使得listview在滑动过程中不会出现黑色的背景
        statusesListView.setDivider(null);
    }

    String root;
    private void setListener() {

        //刷新按钮事件监听
        mIvRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //执行下拉刷新操作
                mStatusesPullListView.setRefreshing(true);
                Toast.makeText(getContext(), "执行刷新!", Toast.LENGTH_SHORT).show();
                mStatusesPullListView.onRefreshComplete();

            }
        });

        //搜索条点击事件监听
        mIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到搜索界面
                Toast.makeText(getContext(), "跳转到搜索界面!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "item"+position + mStatuses.size(), Toast.LENGTH_SHORT).show();
                String statusId = null;
                final int type = mStatuses.get(position-1).getAttach_type();
                switch (type) {
                    case Statuses.ACTIVITY_TYPE:
                        Activity activity = (Activity)mStatuses.get(position-1).getAttach_obj();
                        //statusId = activity.getId();
                        statusId = "111";
                        root = getActivity().getResources().getString(R.string.ROOT) + "activity/" + statusId;
                        break;
                    case Statuses.NOTIFICATION_TYPE:
                        Notification notification = (Notification)mStatuses.get(position-1).getAttach_obj();
                        //statusId = notification.getId();
                        statusId = "111";
                        root = getActivity().getResources().getString(R.string.ROOT)+"notification/"+statusId;
                        break;
                    case Statuses.PHOTO_TYPE:
                        Photo photo = (Photo)mStatuses.get(position-1).getAttach_obj();
                       // statusId = photo.getId();
                        statusId = "111";
                        root = getActivity().getResources().getString(R.string.ROOT) + "activity/" + statusId;
                        break;
                    default: break;
                }

                if (networkStatus.isConnectInternet()) {

                    VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                            .with("token", GlobalApplication.getToken());
                    VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                            .with("Accept","application/json"); // 数据格式设置为json

                    mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(root, urlParams), headerParams, null,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d("getDetail:TAG", response);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("json", response);

                                    if (type == Statuses.NOTIFICATION_TYPE) {
//                                        Intent intent = new Intent(get, csdc.activity.info.news.DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                        intent.putExtras(bundle);
//                                        activity.startActivityForResult(intent, SEND_NEWS_REQUEST);
                                    } else {
                                        Intent intent = new Intent(getActivity(), ActivityDetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtras(bundle);
                                        //getActivity().startActivityForResult(intent, SEND_NEWS_REQUEST);
                                        getActivity().startActivity(intent);
                                    }

                                }},
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("getTIMELINE:TAG", "出错");
                                    Log.d("getTIMELINE:TAG", error.getMessage(),error);
                                }
                            });
                    mQueue = GlobalApplication.get().getRequestQueue();
                    mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
                    mQueue.add(mStringRequest);
                }else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
                }

                }

            }
        );

        mStatusesPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            //下拉刷新
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                new GetDataTask().execute();
//                //暂时的测试----------------------------------------------------------------------
//                statusesAdapter.addHeaderStausesListItem(new Statuses());
//                statusesAdapter.notifyDataSetChanged();//适配器更新数据
//
//                mStatusesPullListView.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mStatusesPullListView.onRefreshComplete();
//                    }
//                }, 1000);


//                RequestQueue ueue = Volley.newRequestQueue(getActivity());
//                StringRequest stringRequest = new StringRequest("https://www.baidu.com",
//                        new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String response) {
//                                Log.d("getTIMELINE:TAG", response);
//                                Log.d("getTIMELINE:TAG", "是的");
//                            }
//                        }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e("getTIMELINE:TAG", error.getMessage(), error);
//                        Log.d("getTIMELINE:TAG", "不是的");
//                    }
//                });
//                ueue.add(stringRequest);
                //--------------------------------------------------------------------------------
            }

            //上拉加载更多
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                statusLoadMoreData();
                //暂时的测试----------------------------------------------------------------------
//                statusesAdapter.addStatusListItem(new Statuses());
//                statusesAdapter.notifyDataSetChanged();//适配器更新数据
//                //属性太快会有bug
//                mStatusesPullListView.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mStatusesPullListView.onRefreshComplete();
//                    }
//                }, 1000);
                //--------------------------------------------------------------------------------
            }
        });

    }


    private void getListData() {
        if (networkStatus.isConnectInternet()) {

            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("token","123") // .with("token", GlobalApplication.getToken())
                    .with("page","4")    //获取第二页的数据
                    .with("count",String.valueOf(LOAD_DATA_COUNT)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("Accept","application/json"); // 数据格式设置为json


            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    Log.d("getTIMELINE:TAG", response);
                    parseStatusJson(response); //将数据填入到List中去
                    mStatusesPullListView.onRefreshComplete();
                }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getTIMELINE:TAG", "出错");
                            Log.d("getTIMELINE:TAG", error.getMessage(),error);
                        }
                    });
            mQueue = GlobalApplication.get().getRequestQueue();
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        }else {
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * json数据转换成状态item并设置adapter
     * @param json
     *
     *
     *
     *
     * //            totalStatusCount =jsonObject.getInt("total");
    //            currentPage =jsonObject.getInt("page");//获取的当前页数
    //            startOrder = jsonObject.getInt("start") ; //当前页的起始序号
     //这些注释部分由于数据库中暂时没有数据，直接获取会有异常
     */
    private void parseStatusJson(String json) {
        //mStatuses = new ArrayList<>();
        Log.d("getTIMELINE:TAG", "parseStatusJson进来了");
        try {
            Log.d("getTIMELINE:TAG", "parseStatusJson进来了222");
            JSONObject jsonObject = new JSONObject(json);
//            totalStatusCount =jsonObject.getInt("total");
//            currentPage =jsonObject.getInt("page");//获取的当前页数
//            startOrder = jsonObject.getInt("start") ; //当前页的起始序号
            JSONArray jsonArr = jsonObject.getJSONArray("statues");

            Log.d("getTIMELINE:TAG", "totalStatusCount"+totalStatusCount + jsonArr.length());
            for (int i = 0; i < jsonArr.length(); i++) {//前10条数据

                Statuses status = new Statuses();
                JSONObject jsonObject1 = jsonArr.getJSONObject(i);
                status.setAttach_type(jsonObject1.getInt("attach_type"));
                switch(status.getAttach_type()) {
                    case Statuses.ACTIVITY_TYPE:
                        Activity activity = new Activity();
                        //activity.set    暂时还不知道获取哪些数据
                        status.setAttach_obj(activity);
                        break;
                    case Statuses.NOTIFICATION_TYPE:
                        Notification notification = new Notification();
                        status.setAttach_obj(notification);
                        break;
                    case Statuses.PHOTO_TYPE:
                        Photo photo = new Photo();
                        status.setAttach_obj(photo);
                        break;
                    default: break;
                }
                mStatuses.add(0,status);
            }
            endOrder = startOrder + jsonArr.length();
            statusesAdapter = new StatusesListAdapter(getActivity(), mStatuses); //初始化adapter
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
        VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                .with("token","123") // .with("token", GlobalApplication.getToken())
                .with("count",String.valueOf(LOAD_DATA_COUNT)); //设置每页条数
        VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                .with("Accept","application/json"); // 数据格式设置为json

        if((endOrder - startOrder) < LOAD_DATA_COUNT) {   //上次页面未加载完整
            urlParams.with("page",String.valueOf(currentPage));    //设置获取之前页
        } else {                                          //上次页面加载完整
            urlParams.with("page",String.valueOf(currentPage+1));    //设置获取下一页
        }
        if (networkStatus.isConnectInternet()) {   //有网络连接
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {   //request请求回调
                            Log.d("getTIMELINE:TAG", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                totalStatusCount =jsonObject.getInt("total");
                                if(totalStatusCount == endOrder) { //数据已加载完
                                    return;
                                }
                                currentPage =jsonObject.getInt("page");//获取的当前页数
                                startOrder = jsonObject.getInt("start") ; //当前页的起始序号
                                JSONArray jsonArr = jsonObject.getJSONArray("statues");
                                for (int i = (endOrder%LOAD_DATA_COUNT); i < jsonArr.length(); i++) {//剩余数据
                                    Statuses status = new Statuses();
                                    JSONObject jsonObject1 = jsonArr.getJSONObject(i);
                                    status.setAttach_type(jsonObject1.getInt("attach_type"));
                                    switch(status.getAttach_type()) {
                                        case Statuses.ACTIVITY_TYPE:
                                            Activity activity = new Activity();
                                            //activity.set    暂时还不知道获取哪些数据
                                            status.setAttach_obj(activity);
                                            break;
                                        case Statuses.NOTIFICATION_TYPE:
                                            Notification notification = new Notification();
                                            status.setAttach_obj(notification);
                                            break;
                                        case Statuses.PHOTO_TYPE:
                                            Photo photo = new Photo();
                                            status.setAttach_obj(photo);
                                            break;
                                        default: break;
                                    }
                                    statusesAdapter.addHeaderStausesListItem(status);
                                }
                                endOrder = startOrder + jsonArr.length();
                                statusesAdapter.notifyDataSetChanged();//适配器更新数据

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getTIMELINE:TAG", "出错");
                            Log.d("getTIMELINE:TAG", error.getMessage(),error);
                        }
                    });
            mQueue = GlobalApplication.get().getRequestQueue();
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        }else {
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 动态加载更多
     */
    private void statusLoadMoreData() {
        int frontPage;
        frontPage = (endOrder - statusesAdapter.getCount()) / LOAD_DATA_COUNT;

        if(frontPage > 0) {
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("page",String.valueOf(frontPage))// .with("token", GlobalApplication.getToken())
                    .with("token","123")
                    .with("count",String.valueOf(LOAD_DATA_COUNT)); //设置每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("Accept","application/json"); // 数据格式设置为json

            if (networkStatus.isConnectInternet()) {   //有网络连接
                mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {   //request请求回调
                                Log.d("getTIMELINE:TAG", response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    totalStatusCount =jsonObject.getInt("total");
                                    if(totalStatusCount == endOrder) { //数据已加载玩
                                        return;
                                    }
                                    currentPage =jsonObject.getInt("page");//获取的当前页数
                                    startOrder = jsonObject.getInt("start") ; //当前页的起始序号
                                    JSONArray jsonArr = jsonObject.getJSONArray("statues");
                                    for (int i = 0; i < jsonArr.length(); i++) {//剩余数据
                                        Statuses status = new Statuses();
                                        JSONObject jsonObject1 = jsonArr.getJSONObject(i);
                                        status.setAttach_type(jsonObject1.getInt("attach_type"));
                                        switch(status.getAttach_type()) {
                                            case Statuses.ACTIVITY_TYPE:
                                                Activity activity = new Activity();
                                                //activity.set    暂时还不知道获取哪些数据
                                                status.setAttach_obj(activity);
                                                break;
                                            case Statuses.NOTIFICATION_TYPE:
                                                Notification notification = new Notification();
                                                status.setAttach_obj(notification);
                                                break;
                                            case Statuses.PHOTO_TYPE:
                                                Photo photo = new Photo();
                                                status.setAttach_obj(photo);
                                                break;
                                            default: break;
                                        }
                                        statusesAdapter.addStatusListItem(status);
                                    }
                                    endOrder = startOrder + jsonArr.length();
                                    statusesAdapter.notifyDataSetChanged();//适配器更新数据

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }},
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("getTIMELINE:TAG", "出错");
                                Log.d("getTIMELINE:TAG", error.getMessage(),error);
                            }
                        });
                mQueue = GlobalApplication.get().getRequestQueue();
                mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
                mQueue.add(mStringRequest);
            }else {
                Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
            }
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
            statusAddNewData();
            super.onPostExecute(result);
        }
    }

}
