package com.android.status;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.android.activity.ActivityDetailActivity;
import com.android.activity.PublishActivity;
import com.android.adapter.ActivityListAdapter;
import com.android.adapter.RecommendUserAdapter;
import com.android.guide.GlobalApplication;
import com.android.search.SearchActivity;
import com.android.tool.ListViewUtility;
import com.android.tool.MyImageRequest;
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
    @BindView(R.id.iv_search)
    ImageView mIvSearch;
    @BindView(R.id.iv_refresh)
    ImageView mIvRefresh;
    @BindView(R.id.llSearchFrame)
    LinearLayout mLlSearchFrame;
    @BindView(R.id.viewpager)
    ViewPager mViewpager;
    @BindView(R.id.hot_activity_title)
    TextView mHotActivityTitle;
    @BindView(R.id.ll)
    LinearLayout mLl;
    @BindView(R.id.rv_user)
    RecyclerView mRvUser;
    @BindView(R.id.ll_recommend_user)
    LinearLayout mLlRecommendUser;
    @BindView(R.id.swipyrefreshlayout)
    SwipyRefreshLayout mSwipyrefreshlayout;
    @BindView(R.id.add_activity_button)
    FloatingActionButton mAddActivityButton;
    @BindView(R.id.tv_emptyview)
    TextView mTvEmptyview;
    @BindView(R.id.change_users_page)
    TextView mChangeUsersPage;
    @BindView(R.id.lv_recommend_activity)
    ListView mLvRecommendActivity;
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
        getHotActivityData();
        setListener();
        getUserListData();
        getActivityListData();

        return view;
    }

    /**
     * 获取推荐用户
     */
    private void getHotActivityData() {
        mHotActivityJsons = new ArrayList<>();
        mImageBitmaps = new HashMap<>();
        if (networkStatus.isConnectInternet()) {
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    // .with("page","4")    //获取第二页的数据
                    .with("count", String.valueOf(10)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString+"recommend/activity/hot", urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getHOTACTIVITY:TAG", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                JSONArray jsonArr = jsonObject.getJSONArray("recommend");
                                Log.d("getHOTACTIVITY:TAG", String.valueOf(jsonArr.length()));
                                for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                                    mHotActivityJsons.add(jsonArr.getJSONObject(i));
                                }
                                initViewPager();
                                getHotPicture(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getACTIVITY:TAG", error.getMessage(),error);
                        }
                    });
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        }else {
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 获取推荐用户
     */
    private void getUserListData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvUser.setLayoutManager(layoutManager);
        userAdapter = new RecommendUserAdapter(getActivity(), new RecommendUserAdapter.UserItemClickListener() {
            @Override
            public void closeBtnClick(int position) {
                Toast.makeText(getContext(), "你点击了关闭按钮", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void addAttentionBtnClick(int position) {
                Toast.makeText(getContext(), "你点击了关注按钮", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void itemClick(int position) {
                Toast.makeText(getContext(), "你点击了item", Toast.LENGTH_SHORT).show();
            }
        });

        if (networkStatus.isConnectInternet()) {
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    // .with("page","4")    //获取第二页的数据
                    .with("count", String.valueOf(10)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString+"recommend/user", urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getACTIVITY:TAG", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                JSONArray jsonArr = jsonObject.getJSONArray("recommend");
                                Log.d("getACTIVITY:TAG", String.valueOf(jsonArr.length()));
                                for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                                    //适配器中添加数据项
                                    userAdapter.addUserListItem(jsonArr.getJSONObject(i));
                                }
                                mRvUser.setAdapter(userAdapter);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("getFOLLOW:TAG", e.toString());
                            }
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getACTIVITY:TAG", "出错");
                            Log.d("getACTIVITY:TAG", error.getMessage(),error);
                        }
                    });
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        }
    }
    /**
     * 获取推荐活动
     */
    private void getActivityListData() {
        activityAdapter = new ActivityListAdapter(getActivity());
        if (networkStatus.isConnectInternet()) {
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    // .with("page","4")    //获取第二页的数据
                    .with("count",String.valueOf(10)); //每页条数
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept","application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString+"recommend/activity", urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                    Log.d("getACTIVITY:TAG", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
//            totalStatusCount =jsonObject.getInt("total");//总条数
//            currentPage =jsonObject.getInt("page");//获取的当前页数
//            startOrder = jsonObject.getInt("start") ; //当前页的起始序号
//            pageCount = jsonObject.getInt("count") ; //当前页的起始序号
                        JSONArray jsonArr = jsonObject.getJSONArray("recommend");

                        for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                            //适配器中添加数据项
                            activityAdapter.addActivityListItem(jsonArr.getJSONObject(i));
                        }
                        //endOrder = startOrder + jsonArr.length();
                        //statusesAdapter = new StatusesListAdapter(getActivity()); //初始化adapter

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("getFOLLOW:TAG", e.toString());
                    }
                    mLvRecommendActivity.setAdapter(activityAdapter);//设置适配器
                    ListViewUtility.setListViewHeightBasedOnChildren(mLvRecommendActivity);//重新计算listView的高度
                }},
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("getACTIVITY:TAG", "出错");
                    Log.d("getACTIVITY:TAG", error.getMessage(),error);
                }
            });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        }else {
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }
    private void initViewPager() {
        mViews = new ArrayList<View>();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mViews.add(inflater.inflate(R.layout.hot_activity_listitem, null));
        mViews.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "点击图片0!", Toast.LENGTH_SHORT).show();
            }
        });
        mViews.add(inflater.inflate(R.layout.hot_activity_listitem, null));
        mViews.add(inflater.inflate(R.layout.hot_activity_listitem, null));
        mViews.add(inflater.inflate(R.layout.hot_activity_listitem, null));
       // mPagerAdapter = new ViewPagerAdapter(mViews, getActivity());
        PagerAdapter mPagerAdapter = new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                // TODO Auto-generated method stub
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return mViews.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                // TODO Auto-generated method stub
                container.removeView(mViews.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                // TODO Auto-generated method stub
                container.addView(mViews.get(position));
                return mViews.get(position);//把当前新增视图的位置（position）作为Key传过去
            }
        };
        mViewpager.setAdapter(mPagerAdapter);
        mViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
                Log.d(TAG, "--------changed:" + arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                Log.d(TAG, "-------scrolled arg0:" + arg0);
                Log.d(TAG, "-------scrolled arg1:" + arg1);
                Log.d(TAG, "-------scrolled arg2:" + arg2);
            }

            @Override
            public void onPageSelected(int arg0) {
                Log.d(TAG, "------selected:" + arg0);//当前页面的position
                setCurrentDot(arg0);
                getHotPicture(arg0);
            }
        });
        initDots();
    }

    /**
     * 获取热门活动的图片
     * @param position 图片位置
     */
    private void getHotPicture(final int position) {
        final ImageView activityPictrue = (ImageView) mViews.get(position).findViewById(R.id.hot_activity_picture);
        try {
            mHotActivityTitle.setText(mHotActivityJsons.get(position).getString("title")+ position);
            // mHotActivityTitle.setText("活动" + position);
            //设置图片
            if (mImageBitmaps.containsKey(position)) {
                activityPictrue.setImageBitmap(mImageBitmaps.get(position));
            } else if (networkStatus.isConnectInternet()) {

                MyImageRequest imageRequest = new MyImageRequest(
                        getResources().getString(R.string.ROOT) + "media/" + mHotActivityJsons.get(position).getString("image")
                        , new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mImageBitmaps.put(position, response);
                        activityPictrue.setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        activityPictrue.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.campus_playing_app_icon));
                    }
                });
                mQueue.add(imageRequest);
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getHOTACTIVITY:TAG", e.toString());
        }
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
                //跳转到搜索界面
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

        //刷新按钮事件监听
        mIvRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  mSwipeRefresh.setRefreshing(true);//这里只是设置进度条是否显示出来
                doRefresh();
            }
        });

        //上下拉刷新事件监听
        mSwipyrefreshlayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                Toast.makeText(getContext(),
                        "Refresh triggered at "
                                + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"), Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "Refresh triggered at "
                        + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"));
            }
        });

        mLvRecommendActivity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                GlobalApplication.setUserAvatar(activityAdapter.getAvatar(position));
                GlobalApplication.setActivityImage(activityAdapter.getActivityImage(position));
                bundle.putString("jsonStr", activityAdapter.getAttachObj(position).toString()); //传递活动详情的json数据
                Intent intent = new Intent(getActivity(), ActivityDetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);  //传入详细信息
                getActivity().startActivity(intent);
            }
        });
    }


    /**
     * 执行刷新处理
     */
    private void doRefresh() {
        Toast.makeText(getContext(), "执行刷新!", Toast.LENGTH_SHORT).show();
    }


}
