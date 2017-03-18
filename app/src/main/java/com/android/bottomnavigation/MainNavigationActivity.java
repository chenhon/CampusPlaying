package com.android.bottomnavigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.R;
import com.android.person.PersonFragment;
import com.android.remind.RemindFragment;
import com.android.status.HomeFragment;
import com.android.status.RecommendFragment;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

public class MainNavigationActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener{
    private static final int INTERVAL_TIME = 4000;
    private long exitTime;
    private FrameLayout mContentFrame;
    private BottomNavigationBar mNavigationView;

    private Fragment []mFragments = {new HomeFragment(),new RecommendFragment()
                        , new RemindFragment(),new PersonFragment()};
    private int currentFrag = 0;//指示当前的fragment
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_navigation_frame);
        initView();
        bottomBarInit();
    }

    private void initView() {
        mContentFrame = (FrameLayout) findViewById(R.id.content_frame);
        mNavigationView = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
    }

    private void bottomBarInit() {
        //        BadgeItem numberBadgeItem = new BadgeItem()
//                .setBorderWidth(4)
//                .setBackgroundColor(Color.RED)
//                .setText("5")
//                .setHideOnSelect(true);
        BottomNavigationBar bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.global_home_icon, "主页").setActiveColorResource(R.color.button_Color))
                .addItem(new BottomNavigationItem(R.drawable.global_recommend_icon, "推荐").setActiveColorResource(R.color.button_Color))    //.setBadgeItem(numberBadgeItem)
                .addItem(new BottomNavigationItem(R.drawable.global_remind_icon, "提醒").setActiveColorResource(R.color.button_Color))
                .addItem(new BottomNavigationItem(R.drawable.global_person_icon, "个人").setActiveColorResource(R.color.button_Color))
                .setFirstSelectedPosition(0)
                .initialise();
        bottomNavigationBar.setTabSelectedListener(this); //设置监听事件
        setDefaultFragment();
    }
    private void setDefaultFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.content_frame, mFragments[0]);
        transaction.commit();
        currentFrag = 0;
    }
    /**
     *底部标签标签切换
     */
    @Override
    public void onTabSelected(int position) {
        if (mFragments != null) {
            if (position < mFragments.length) {
                currentFrag = position;//记录当前fragment
                //动态切换fragment
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment fragment = mFragments[position];
                if (!fragment.isAdded()) { // 没有加载过则加载
                    ft.add(R.id.content_frame, fragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
                } else { //否则直接显示
                    ft.show(fragment).commit(); // 隐藏当前的fragment，显示下一个
                }
                //ft.replace(R.id.lay_frame, fragment);

             //   Toast.makeText(this, "切换至碎片"+position, Toast.LENGTH_SHORT).show();

            }
        }

    }

    @Override
    public void onTabUnselected(int position) {   //此函数是接在onTabSelected后面运行的
       // Toast.makeText(this, "不选择了", Toast.LENGTH_SHORT).show();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = mFragments[position];
        ft.hide(fragment).commit();      //隐藏
    }

    @Override
    public void onTabReselected(int position) {
       // Toast.makeText(this, "按本身", Toast.LENGTH_SHORT).show();
    }

    /**
     * 重写onBackPressed()方法，处理手机上的返回键响应事件。
     * 第一次按返回键，给出提示"再按一次退出应用"，在INTERVAL_TIME内按第二次按钮，直接退出整个应用。
     */
    @Override
    public void onBackPressed() {
        if((System.currentTimeMillis() - exitTime) > INTERVAL_TIME){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.LoginOutHint).toString(), Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            //推出应用时，将各SharedPreferences数据清空
           // clearSharedPreferences();
           // ((GlobalApplication)getApplication()).setBitmap(null);
            super.onBackPressed();
        }
    }

}
