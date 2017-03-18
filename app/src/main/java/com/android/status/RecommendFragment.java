package com.android.status;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.activity.ActivityDetailActivity;
import com.android.activity.PublishActivity;
import com.android.adapter.RecommendUserAdapter;
import com.android.adapter.ViewPagerAdapter;
import com.android.model.Activity;
import com.android.model.User;

import java.util.ArrayList;
import java.util.List;

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
    @BindView(R.id.rv_user)
    RecyclerView mRvUser;
    @BindView(R.id.ll_recommend_user)
    LinearLayout mLlRecommendUser;
    @BindView(R.id.ll_recommend_activity)
    LinearLayout mLlRecommendActivity;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.add_activity_button)
    FloatingActionButton mAddActivityButton;
    @BindView(R.id.tv_emptyview)
    TextView mTvEmptyview;
    @BindView(R.id.viewpager)
    ViewPager mViewpager;
    @BindView(R.id.ll)
    LinearLayout mLl;

    private List<View> mViews;
    private ViewPagerAdapter mPagerAdapter;
    private ImageView[] mDots;
    private int mCurrentPos;

    public RecommendFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.recommend_fragment, container, false);
        ButterKnife.bind(this, view);
        //mRvUser = (RecyclerView) view.findViewById(R.id.rv_user);

        initViewPager();
        initDots();
       // initRecommendHotActivity();
        initRecommendUser();
        setListener();
        return view;
    }

    private void initViewPager() {
        mViews = new ArrayList<View>();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mViews.add(inflater.inflate(R.layout.hot_activity_listitem, null));
        mViews.add(inflater.inflate(R.layout.hot_activity_listitem, null));
        mViews.add(inflater.inflate(R.layout.hot_activity_listitem, null));
        mViews.add(inflater.inflate(R.layout.hot_activity_listitem, null));
        mPagerAdapter = new ViewPagerAdapter(mViews, getActivity());
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
                Log.d(TAG, "------selected:" + arg0);
                setCurrentDot(arg0);
            }
        });
        ;
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

    private void setListener() {

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

        //刷新按钮事件监听
        mIvRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipeRefresh.setRefreshing(true);//这里只是设置进度条是否显示出来
                doRefresh();
            }
        });

        //下拉刷新事件监听
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });
    }

    /**
     * 测试用
     */
    private List<User> userList = new ArrayList<>();

    private void initRecommendUser() {
        initUser();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvUser.setLayoutManager(layoutManager);
        RecommendUserAdapter adapter = new RecommendUserAdapter(getActivity(), userList, new RecommendUserAdapter.UserItemClickListener() {
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
        mRvUser.setAdapter(adapter);
        //   mRvUser.setAdapter(adapter);
    }

    //初始化推荐用户列表
    private void initUser() {
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setAvatar(R.drawable.campus_playing_app_icon);
            user.setName("陈洪" + i);
            userList.add(user);
        }
    }

    private List<Activity> hotActivityList = new ArrayList<>();

//    private void initRecommendHotActivity() {
//        initHotActivity();
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        mRvHotActivity.setLayoutManager(layoutManager);
//        RecommendHotActivityAdapter adapter = new RecommendHotActivityAdapter(getActivity(), hotActivityList);
//        mRvHotActivity.setAdapter(adapter);
//        //   mRvUser.setAdapter(adapter);
//    }
//
//    //初始化推荐用户列表
//    private void initHotActivity() {
//        for (int i = 0; i < 10; i++) {
//            Activity user = new Activity();
//            hotActivityList.add(user);
//        }
//    }

    /**
     * 执行刷新处理
     */
    private void doRefresh() {
        Toast.makeText(getContext(), "执行刷新!", Toast.LENGTH_SHORT).show();
        addActivity();///////////////////////////////////测试加入活动
        mSwipeRefresh.setRefreshing(false);
    }

    /**
     * 主页面添加活动
     * 活动可能没有图片？？？？？？？
     */
    public void addActivity() {
        //            com.android.model.Activity activity =
//                    (com.android.model.Activity) mStatuses.get(position).getAttach_obj();
//            TextView activitySourse = (TextView) view.findViewById(R.id.activity_source);
//            activitySourse.setText(activity.getCreator().getName()+"发布了活动");
//            CircleImageView userAvatar = (CircleImageView) view.findViewById(user_avatar);
//            userAvatar.setImageResource(activity.getCreator().getAvatar());
//            TextView activityTitle = (TextView) view.findViewById(R.id.activity_title);
//            activityTitle.setText(activity.getTitle());
//            TextView activityContent = (TextView) view.findViewById(R.id.activity_content);
//            activityContent.setText(activity.getContent());
//            ImageView activityImage = (ImageView) view.findViewById(R.id.activity_image);
//            activityImage.setImageResource(activity.getImage());//这里应该是要判断是否有图像的
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.activity_listitem, mLlRecommendActivity, false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //item点击事件
                //————————————仅仅是看布局用————————————————————————
                Intent intent = new Intent();
                intent.setClass(getActivity(), ActivityDetailActivity.class);
                startActivity(intent);
                //————————————仅仅是看布局用————————————————————————
            }
        });

        // mLlRecommendActivity.addView(v,0);//将加载的布局放在首位置
        mLlRecommendActivity.addView(v);
    }
}
