package com.android.status;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.android.activity.DetailActivity;
import com.android.activity.PublishActivity;
import com.android.adapter.ActivityListAdapter;
import com.android.adapter.RecommendUserAdapter;
import com.android.GlobalApplication;
import com.android.model.Activity;
import com.android.model.User;
import com.android.search.SearchActivity;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.ListViewUtility;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;

/**
 * 推荐页面
 */
public class RecommendFragment extends Fragment {
    private static final int LOAD_DATA_COUNT = 6;//每页加载10条数据
    private static final int LOAD_USER_COUNT = 6;//每页加载用户数
    private static final int HOT_NUM = 4;//呈现的推广活动数
    @BindView(R.id.iv_search)
    ImageView mIvSearch;
    @BindView(R.id.iv_refresh)
    ImageView mIvRefresh;
    @BindView(R.id.llSearchFrame)
    LinearLayout mLlSearchFrame;
    @BindView(R.id.viewpager)
    ViewPager mViewpager;
    @BindView(R.id.ll)
    LinearLayout mLl;
    @BindView(R.id.change_users_page)
    TextView mChangeUsersPage;
    @BindView(R.id.rv_user)
    RecyclerView mRvUser;
    @BindView(R.id.ll_recommend_user)
    LinearLayout mLlRecommendUser;
    @BindView(R.id.lv_recommend_activity)
    ListView mLvRecommendActivity;
    @BindView(R.id.swipyrefreshlayout)
    SwipyRefreshLayout mSwipyrefreshlayout;
    @BindView(R.id.add_activity_button)
    FloatingActionButton mAddActivityButton;
    @BindView(R.id.tv_emptyview)
    TextView mTvEmptyview;


    private List<View> mViews;
    private PagerAdapter mPagerAdapter;
    private ImageView[] mDots;
    private int mCurrentPos;

    private RecommendUserAdapter userAdapter;
    private ActivityListAdapter activityAdapter;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private String rootString;

    private List<JSONObject> mHotActivityJsons;//热门活动信息，对应的Json数据
    private Map<Integer, Bitmap> mImageBitmaps;//活动图片，需要另外加载
    private Map<Integer, Bitmap> mAvatarBitmaps;//活动图片，需要另外加载

//    private int[] hotActivityId = new int[HOT_NUM];
//    private String[] hotTitles = new String[HOT_NUM];//热门活动标题
    private ImageView[] hotImageViews = new ImageView[HOT_NUM];//热门活动照片容器
    private TextView[] hotTextViews = new TextView[HOT_NUM];//热门活动标题容器

    private int loadPage = 0;     //推荐活动
    private int recommentTotal;

    private int loadUserPage = 0; //推广活动
    private int recommentUserTotal;

    private int loadHotPage = 0;  //热门活动
    private int hotTotal;


    private Boolean hasInited = false;

    public RecommendFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.recommend_fragment, container, false);
        ButterKnife.bind(this, view);
        rootString = getResources().getString(R.string.ROOT);
        mQueue = GlobalApplication.get().getRequestQueue();

        networkStatus = new NetworkConnectStatus(getActivity());

        mHotActivityJsons = new ArrayList<>();
        mImageBitmaps = new HashMap<>();
        mAvatarBitmaps = new HashMap<>();
        initViewPager();
        getHotActivityData(); //推广活动

        setListener();
        initUserList();
        getUserListData();
        activityAdapter = new ActivityListAdapter(getActivity());
        mLvRecommendActivity.setAdapter(activityAdapter);//设置适配器
        getActivityListData();

        return view;
    }


    private void initUserList() {
        userAdapter = new RecommendUserAdapter(getActivity());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvUser.setLayoutManager(layoutManager);
        mRvUser.setAdapter(userAdapter);
    }

    /**
     * 获取推荐用户
     */
    private void getUserListData() {
        if ((recommentUserTotal != 0) && (loadUserPage * LOAD_DATA_COUNT >= recommentUserTotal)) {//加载完最后一页了
            loadUserPage = 0;//全部加载完一遍则又循环加载
        }
        if (networkStatus.isConnectInternet()) {
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("page", String.valueOf(loadUserPage + 1))
                    .with("count", String.valueOf(LOAD_USER_COUNT)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString + "recommend/user", urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getUser:TAG", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                recommentUserTotal = jsonObject.getInt("total");//总条数
                                if (recommentUserTotal == 0) {
                                    Toast.makeText(getActivity(), "暂无推荐用户".toString(), Toast.LENGTH_SHORT).show();
                                    mLlRecommendUser.setVisibility(View.GONE);
                                    return;
                                }
                                JSONArray jsonArr = jsonObject.getJSONArray("recommend");
                                Log.d("getUsers:TAG", String.valueOf(jsonArr.length()));
                                userAdapter.removeAllItem();//先清除掉所有数据
                                for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                                    //适配器中添加数据项
                                    JSONObject jo = jsonArr.getJSONObject(i);
                                    User user = new User();
                                    user.setId(jo.getInt("id"));
                                    user.setUser(jo.getString("user"));
                                    user.setName(jo.getString("name"));
                                    user.setAvatar(jo.getInt("avatar"));
                                    user.setGender(jo.getInt("gender"));
                                    user.setDescription(jo.getString("description"));
                                    userAdapter.addUserListItem(user);//进行覆盖处理
                                }
                                if (jsonArr.length() > 0) {
                                    loadUserPage++;
                                    userAdapter.notifyDataSetChanged();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("getFOLLOW:TAG", e.toString());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getACTIVITY:TAG", "出错");
                            Log.d("getACTIVITY:TAG", error.getMessage(), error);
                        }
                    });
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        } else {
            mSwipyrefreshlayout.setRefreshing(false);
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取推荐活动
     */
    private void getActivityListData() {
        if ((recommentTotal != 0) && (loadPage * LOAD_DATA_COUNT >= recommentTotal)) {
            Toast.makeText(getActivity(), "推荐活动已加载完".toString(), Toast.LENGTH_SHORT).show();
      /*      mSwipyrefreshlayout.setRefreshing(false);
            return;*/
        }
        if (networkStatus.isConnectInternet()) {
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("page", String.valueOf(loadPage + 1))    //加载下一页
                    .with("count", String.valueOf(LOAD_DATA_COUNT)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json    //rootString + "recommend/activity"
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString + "recommend/activity/hot", urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mSwipyrefreshlayout.setRefreshing(false);
                            Log.d("getACTIVITY:TAG", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                recommentTotal = jsonObject.getInt("total");//总条数
                                if (recommentTotal == 0) {
                                    Toast.makeText(getActivity(), "暂无推荐活动".toString(), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                JSONArray jsonArr = jsonObject.getJSONArray("recommend");

                                if (jsonArr.length() > 0) {
                                    loadPage++;
                                }
                                for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                                    JSONObject jo = jsonArr.getJSONObject(i);
                                    Log.d("getACTIVITY:TAG", jo.toString());
                                    //适配器中添加数据项
                                    Activity activity = new Activity();
                                    activity.setId(jo.getInt("id"));
                                    activity.setCreatorId(jo.getInt("creator"));
                                    activity.setCreatorName(jo.getJSONObject("creator_obj").getString("name"));
                                    activity.setAvatarId(jo.getJSONObject("creator_obj").getInt("avatar"));
                                    activity.setTitle(jo.getString("title"));
                                    activity.setContent(jo.getString("content"));
                                    activity.setImageId(jo.getInt("image"));
                                    activity.setTime(jo.getLong("created_at"));
                                    activity.setWisherCount(jo.getInt("wisher_count"));
                                    activity.setParticipantCount(jo.getInt("participant_count"));
                                    activity.setVerifyStatus(jo.getInt("verify_state"));
                                    activity.setState(jo.getInt("state"));
                                    activityAdapter.addActivityListItem(activity);
                                }
                                activityAdapter.notifyDataSetChanged();
                                ListViewUtility.setListViewHeightBasedOnChildren(mLvRecommendActivity);//重新计算listView的高度
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("getFOLLOW:TAG", e.toString());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mSwipyrefreshlayout.setRefreshing(false);
                            Log.d("getACTIVITY:TAG", "出错");
                            Log.d("getACTIVITY:TAG", error.getMessage(), error);
                        }
                    });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        } else {
            mSwipyrefreshlayout.setRefreshing(false);
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取推广活动
     */
    private void getHotActivityData() {
        if (networkStatus.isConnectInternet()) {
            if ((hotTotal != 0) && (loadHotPage * HOT_NUM >= hotTotal)) {//加载完最后一页了
                loadUserPage = 0;//全部加载完一遍则又循环加载
            }
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("page",String.valueOf(loadHotPage+1))    //获取下一页数据
                    .with("count", String.valueOf(HOT_NUM)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json    //rootString + "recommend/activity/hot"
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString + "recommend/activity", urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getHOTACTIVITY:TAG", response);
                            mSwipyrefreshlayout.setRefreshing(false);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                hotTotal = jsonObject.getInt("total");//总条数
                                JSONArray jsonArr = jsonObject.getJSONArray("recommend");
                                for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                                    JSONObject jo = jsonArr.getJSONObject(i);
                                    final int aid = jo.getInt("id");
                                    final int creator = jo.getInt("creator");
                                    //hotActivityId[i] = aid;
                                    hotTextViews[i].setText(jo.getString("title"));//热门活动标题容器
                                    BitmapLoaderUtil.getInstance().getImage(hotImageViews[i],BitmapLoaderUtil.TYPE_MEDIAN,jo.getInt("image"));//活动图片
                                    hotImageViews[i].setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(getActivity(), com.android.activity.DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            Bundle bundle = new Bundle();
                                            bundle.putInt("aid", aid);       //动态id
                                            bundle.putInt("creatorId", creator); //发布活动者id
                                            intent.putExtras(bundle);  //传入详细信息
                                            getActivity().startActivity(intent);
                                        }
                                    });
                                }

                                if(jsonArr.length()>0){
                                    loadHotPage++;
                                }
                               // new GetDataTask().execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getACTIVITY:TAG", error.getMessage(), error);
                            mSwipyrefreshlayout.setRefreshing(false);
                        }
                    });
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        } else {
            mSwipyrefreshlayout.setRefreshing(false);
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initViewPager() {
        mViews = new ArrayList<View>();
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for(int i = 0; i < HOT_NUM; i++) {
            View v = inflater.inflate(R.layout.hot_activity_listitem, null);
            hotImageViews[i] = (ImageView) v.findViewById(R.id.hot_activity_picture);
            hotTextViews[i] = (TextView) v.findViewById(R.id.hot_activity_title);
            mViews.add(v);
        }

        PagerAdapter mPagerAdapter = new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return mViews.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView(mViews.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mViews.get(position));
                return mViews.get(position);//把当前新增视图的位置（position）作为Key传过去
            }
        };
        mViewpager.setAdapter(mPagerAdapter);
        mViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
                mSwipyrefreshlayout.setEnabled(true);
                Log.d(TAG, "--------changed:" + arg0);
            }

            @Override    //初始化时会调用一次
            public void onPageScrolled(int arg0, float arg1, int arg2) { //滑动过程
                Log.d(TAG, "-------scrolled arg0:" + arg0);
                Log.d(TAG, "-------scrolled arg1:" + arg1);
                Log.d(TAG, "-------scrolled arg2:" + arg2);
                if (!hasInited) {
                    hasInited = true;
                } else {
                    mSwipyrefreshlayout.setEnabled(false);//设置不可上下滑动
                }
            }

            @Override
            public void onPageSelected(int arg0) {
                Log.d(TAG, "------selected:" + arg0);//当前页面的position
                mSwipyrefreshlayout.setEnabled(true);
                setCurrentDot(arg0);
            }
        });
        initDots();
    }

    /**
     * 设置引导界面的圆点
     *
     * @param
     */
    private void setCurrentDot(int index) {
        if (index < 0 || index > mViews.size() - 1 || mCurrentPos == index) {
            return;
        }
        mDots[mCurrentPos].setEnabled(true);
        mDots[index].setEnabled(false);
        mCurrentPos = index;
    }

    /**
     * 初始化圆点
     */
    private void initDots() {
        mDots = new ImageView[mViews.size()];
        for (int i = 0; i < mViews.size(); i++) {
            mDots[i] = (ImageView) mLl.getChildAt(i);
            mDots[i].setEnabled(true);
        }
        mCurrentPos = 0;
        mDots[mCurrentPos].setEnabled(false);
    }

    /**
     * 设置事件监听
     */
    private void setListener() {

        //搜索条点击事件监听
        mIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Toast.makeText(getContext(), "跳转到搜索界面!", Toast.LENGTH_SHORT).show();
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

        //刷新按钮事件监听
        mIvRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  mSwipeRefresh.setRefreshing(true);//这里只是设置进度条是否显示出来
             //   doRefresh();
            }
        });

        //上下拉刷新事件监听
        mSwipyrefreshlayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
                    getActivityListData();
                } else {  //下拉刷新
                    getHotActivityData();
                    getUserListData();

                    loadPage = 0;
                    recommentTotal = 0;
                    activityAdapter.clearListData();
                    getActivityListData();
                }
//                Toast.makeText(getContext(),
//                        "Refresh triggered at "
//                                + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"), Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "Refresh triggered at "
                        + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"));
            }
        });

        //换一组推荐用户
        mChangeUsersPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserListData();
            }
        });

        //跳转活动详情页
        mLvRecommendActivity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                System.out.println("position-1->>" + (position));
                bundle.putInt("aid", activityAdapter.getAid(position)); //活动id
                bundle.putInt("creatorId", activityAdapter.getUid(position)); //发布活动着id
                Intent intent = new Intent(getActivity(), DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);  //传入详细信息
                getActivity().startActivity(intent);
            }
        });
    }


    /**
     * 执行刷新处理
     */
/*    private void doRefresh() {
        Toast.makeText(getContext(), "执行刷新!", Toast.LENGTH_SHORT).show();
    }*/

/*    *//**
     * 获取数据时先等待2S
     *//*
    private class GetDataTask extends AsyncTask<Void, Integer, String[]> {

        *//**
         * 子线程中执行
         *
         * @param params
         * @return
         *//*
        @Override
        protected String[] doInBackground(Void... params) {
            for (int i = 0; i < 4; i++) {
                try {
                    Thread.sleep(2000);
                    publishProgress(i);
                } catch (InterruptedException e) {
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            getHotPicture(values[0]);
        }

        *//**
         * 主线程中执行
         *
         * @param result
         *//*
        @Override
        protected void onPostExecute(String[] result) {

            super.onPostExecute(result);
        }
    }*/

}
