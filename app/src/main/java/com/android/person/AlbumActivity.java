package com.android.person;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.R;
import com.android.adapter.AlbumListAdapter;
import com.android.guide.BaseActivity;
import com.android.guide.GlobalApplication;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumActivity extends BaseActivity {
    private static final int LOAD_DATA_COUNT = 10;//每页加载10条数据

    public static final int RELATION_MYSELF = 1;
    public static final int RELATION_OTHER = 2;

    int relation;//与当前用户的关系 1为自己， 2为他人
    int uid;//用户id
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.albumPullListView)
    PullToRefreshListView mAlbumPullListView;


    private AlbumListAdapter albumAdapter;
    private ListView albumListView;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private String rootString;

    private Boolean isLoadData = false;//记录当前月份是否加载到了图片
    private int addNewTotal; //第一项item的总照片数
    private int addNewCount; //第一项item照片已加载数
    private Boolean isLoadLastData = false;//记录当前月份是否加载到了上个月的数据
    private int lastMonth;//上一个月
    private int lastMonthTotal; //上个月的总照片数
    private int lastMonthCount; //上个月照片的已加载数

    private int getLastMonth(int thisMonth) {
        if(thisMonth%100 == 1) {//一月的上一个月  201701    到 201612
            return thisMonth - (201701 - 201612);
        } else {
            return thisMonth - 1;
        }
    }
    /**
     * 启动本活动
     *
     * @param activity  启动该活动的活动
     * @param relation  与本人的关系
     * @param uid       用户id
     */
    public static void startActivity(Activity activity, int relation, int uid) {
        Bundle bundle = new Bundle();
        bundle.putInt("RELATION", relation);
        bundle.putInt("UID", uid);
        Intent intent = new Intent(activity, AlbumActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);  //传入详细信息
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_album);
        ButterKnife.bind(this);

        mQueue = GlobalApplication.get().getRequestQueue();

        networkStatus = new NetworkConnectStatus(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            relation = bundle.getInt("RELATION");//与当前用户的关系 1为自己， 2为他人
            uid = bundle.getInt("UID");//用户id
        }
        if (relation == RELATION_MYSELF) {
            rootString = getResources().getString(R.string.ROOT)
                    + "user/~me/photo";
        } else if (relation == RELATION_OTHER) {
            rootString = getResources().getString(R.string.ROOT)
                    + "user/" + uid + "/photo";
        }
        lastMonth = getLastMonth(Integer.parseInt(DataUtils.stampToDate(DataUtils.DATA_TYPE5,DataUtils.getCurrentTime())));
        initListView();
        getListData();
        setListener();
    }

    /**
     * 初始化列表的listView
     */
    private void initListView() {
        albumListView = mAlbumPullListView.getRefreshableView();//获取动态列表控件
        albumListView.setCacheColorHint(00000000);//此设置使得listview在滑动过程中不会出现黑色的背景
        albumListView.setDivider(null);
    }

    /**
     * 设置监听事件
     */
    private void setListener() {

        //设置item点击时间    这里position是从1开始的
        albumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        });

        mAlbumPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            //下拉刷新
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                new AlbumActivity.GetDataTask().execute();
            }

            //上拉加载更多
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                albumLoadMoreData();
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
        albumAdapter = new AlbumListAdapter(this);
        mAlbumPullListView.setRefreshing(true);
        if (networkStatus.isConnectInternet()) {
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("filter", DataUtils.stampToDate(DataUtils.DATA_TYPE5,DataUtils.getCurrentTime()))   //获取当前月份的照片
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
                            albumListView.setAdapter(albumAdapter);//设置适配器
                            mAlbumPullListView.onRefreshComplete();
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
            mAlbumPullListView.onRefreshComplete();
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
            addNewTotal = jsonObject.getInt("total");//总条数
            if (addNewTotal == 0){//当前月份没有发布的相片
                return ;
            }

            albumAdapter.setDate(jsonObject.getInt("time"));
            JSONArray jsonArr = jsonObject.getJSONArray("photos");
            List<JSONObject> photoJson = albumAdapter.getFirstItemJson(isLoadData);
            isLoadData = true;
            for (int i = 0; i < jsonArr.length(); i++) {
                photoJson.add(jsonArr.getJSONObject(i));
            }
            addNewCount += jsonArr.length();

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getFOLLOW:TAG", e.toString());
        }
    }

    private void albumAddNewData() {
        if(!isLoadData) {   //当前月份还没有加载到数据则继续加载当前月数据
            getListData();
        } else if (networkStatus.isConnectInternet()) {
            int page = addNewCount / LOAD_DATA_COUNT+1;//加载下一页
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("filter", DataUtils.stampToDate(DataUtils.DATA_TYPE5,DataUtils.getCurrentTime()))   //获取当前月份的照片
                    .with("page",String.valueOf(page))    //加载下一页
                    .with("count", String.valueOf(LOAD_DATA_COUNT)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getPHOTO:TAG", response);

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                addNewTotal = jsonObject.getInt("total");//总条数
                                if (addNewTotal <= addNewCount){//当前月照片已加载完
                                    return ;
                                }
                                JSONArray jsonArr = jsonObject.getJSONArray("photos");
                                List<JSONObject> photoJson = albumAdapter.getFirstItemJson(isLoadData );
                                //for (int i = addNewCount%LOAD_DATA_COUNT; i < jsonArr.length(); i++) {//应该这么写
                                for (int i = 0; i < jsonArr.length(); i++) {//前10条数据

                              //      Log.d("getPHOTO:TAG", "i = " + i);
                                    photoJson.add(jsonArr.getJSONObject(i));
                              //      Log.d("getPHOTO:TAG", "photoJson.size = " + photoJson.size());
                                }
                                addNewCount += jsonArr.length();
                             //   Log.d("getPHOTO:TAG", String.valueOf(addNewCount) + "jsonArr.length()" + jsonArr.length());

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("getFOLLOW:TAG", e.toString());
                            }

                            albumAdapter.notifyDataSetChanged();//适配器更新数据
                            mAlbumPullListView.onRefreshComplete();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getTIMELINE:TAG", "出错");
                            Log.d("getTIMELINE:TAG", error.getMessage(), error);
                            mAlbumPullListView.onRefreshComplete();
                        }
                    });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        } else {
            mAlbumPullListView.onRefreshComplete();
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void albumLoadMoreData() {
        if (networkStatus.isConnectInternet()) {
            int page = lastMonthCount / LOAD_DATA_COUNT+1;//加载下一页
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("filter", String.valueOf(lastMonth))   //获取当前月份的照片
                    .with("page",String.valueOf(page))    //加载下一页
                    .with("count", String.valueOf(LOAD_DATA_COUNT)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getPHOTO:TAG", response);

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                lastMonthTotal = jsonObject.getInt("total");//总条数
                                if (lastMonthTotal <= lastMonthCount){//下月照片已加载完
                                    lastMonthCount = 0;
                                    isLoadLastData = false;
                                    lastMonth = getLastMonth(lastMonth);
                                    mAlbumPullListView.onRefreshComplete();
                                    return ;
                                }

                                JSONArray jsonArr = jsonObject.getJSONArray("photos");
                                if(!isLoadLastData){
                                    albumAdapter.setLastDate(jsonObject.getInt("time"));
                                }
                                List<JSONObject> photoJson = albumAdapter.getLastItemJson(isLoadLastData);
                                isLoadLastData = true;
                                //for (int i = lastMonthCount%LOAD_DATA_COUNT; i < jsonArr.length(); i++) {//应该这么写
                                for (int i = 0; i < jsonArr.length(); i++) {//前10条数据

                                    //      Log.d("getPHOTO:TAG", "i = " + i);
                                    photoJson.add(jsonArr.getJSONObject(i));
                                    //      Log.d("getPHOTO:TAG", "photoJson.size = " + photoJson.size());
                                }
                                lastMonthCount += jsonArr.length();
                                //   Log.d("getPHOTO:TAG", String.valueOf(addNewCount) + "jsonArr.length()" + jsonArr.length());

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("getFOLLOW:TAG", e.toString());
                            }

                            albumAdapter.notifyDataSetChanged();//适配器更新数据
                            mAlbumPullListView.onRefreshComplete();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getTIMELINE:TAG", "出错");
                            Log.d("getTIMELINE:TAG", error.getMessage(), error);
                            mAlbumPullListView.onRefreshComplete();
                        }
                    });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        } else {
            mAlbumPullListView.onRefreshComplete();
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
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
                albumAddNewData();
            }
            super.onPostExecute(result);
        }
    }
}
