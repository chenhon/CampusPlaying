package com.android.bottomnavigation;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.BaseActivity;
import com.android.GlobalApplication;
import com.android.R;
import com.android.person.PersonFragment;
import com.android.remind.RemindFragment;
import com.android.status.HomeFragment;
import com.android.status.RecommendFragment;
import com.android.tool.NetworkConnectStatus;
import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

public class MainNavigationActivity extends BaseActivity implements BottomNavigationBar.OnTabSelectedListener{
    public static final int REFRESH_PERSON_INFO = 0;    //改变个人主页信息
    public static final int REFRESH_REMIND_INFO = 1;    //改变个人消息
    private static final int INTERVAL_TIME = 4000;
    private long exitTime;

    private NetworkConnectStatus networkStatus;//网络连接状态
    private FrameLayout mContentFrame;
    private BottomNavigationBar mNavigationView;
 //   private Receiver mRecevier;
    private boolean isGettingNewMag = false; //是否正在获取新消息数目

    private Fragment []mFragments = {new HomeFragment(),new RecommendFragment()
                        , new RemindFragment(),new PersonFragment()};
    private int currentFrag = 0;//指示当前的fragment
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_navigation_frame);
        networkStatus = new NetworkConnectStatus(this);
        initView();
        bottomBarInit();

        // 注册广播接收者
/*        mRecevier = new Receiver();
        IntentFilter intentFilter = new IntentFilter("com.android.newcount");
        registerReceiver(mRecevier, intentFilter);
        PollingUtils.startPollingService(this, 3, PollingGetMsgService.class, PollingGetMsgService.ACTION);//开始轮询*/
    }

    private void initView() {
        mContentFrame = (FrameLayout) findViewById(R.id.content_frame);
        mNavigationView = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
    }

    BadgeItem numberBadgeItem;
    BottomNavigationItem remindItem;
    private void bottomBarInit() {
        numberBadgeItem = new BadgeItem()
                .setBorderWidth(4)
                .setBackgroundColor(Color.RED);
         //.setHideOnSelect(true);
             //   .hide();     //点击后隐藏
        remindItem = new BottomNavigationItem(R.drawable.global_remind_icon, "提醒").setActiveColorResource(R.color.bottom_navigation_color);

        BottomNavigationBar bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.global_home_icon, "主页").setActiveColorResource(R.color.bottom_navigation_color))
                .addItem(new BottomNavigationItem(R.drawable.global_recommend_icon, "推荐").setActiveColorResource(R.color.bottom_navigation_color))
                .addItem(remindItem.setBadgeItem(numberBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.global_person_icon, "个人").setActiveColorResource(R.color.bottom_navigation_color))
                .setFirstSelectedPosition(0)
                .initialise();
        bottomNavigationBar.setTabSelectedListener(this); //设置监听事件
        setDefaultFragment();
        hideBadge();


      //  remindItem.setBadgeItem(numberBadgeItem);
      //  bottomNavigationBar.getChildAt(2);

    }
    private void setDefaultFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.content_frame, mFragments[0]);
        transaction.commit();
        currentFrag = 0;
    }

    /**
     * 设置提醒数字
     * @param i
     */
    public void setBadgeNum(int i) {
        String badgeShow;
        if(i == 0) {
            hideBadge();
            return ;
        } else if(i <= 99) {
            badgeShow = String.valueOf(i);
        } else {
            badgeShow = "99+";
        }
        numberBadgeItem.setText(badgeShow).show();
    }

    /**
     * 隐藏提醒项
     */
    public void hideBadge() {
        numberBadgeItem.hide();
    }

    /**
     *底部标签标签切换
     */
    @Override
    public void onTabSelected(int position) {
        if (mFragments != null) {
            if (position < mFragments.length) {
                currentFrag = position;//记录当前fragment

    /*            if(position == 0) {
                    setBadgeNum(0);
                } else if(position == 1) {
                    setBadgeNum(1);
                } else if(position == 2) {
                }else if(position == 3) {
                    hideBadge();
                }*/
                //动态切换fragment
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment fragment = mFragments[position];
                if (!fragment.isAdded()) { // 没有加载过则加载
                    ft.add(R.id.content_frame, fragment).commit(); // 隐藏当前的fragment，add下一个到Activity中

                } else { //否则直接显示
                    if(fragment instanceof PersonFragment) {  //界面切换也要刷新个人主页
                        ((PersonFragment) fragment).refreshPersonInfo(GlobalApplication.getMySelf().getId());
                    } else if(fragment instanceof RemindFragment) {   //刷新个人消息列表
                        ((RemindFragment) fragment).refreshPrivateListData();
                    }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REFRESH_PERSON_INFO:        //刷新个人主页
                if(resultCode == RESULT_OK) {
                    if(mFragments[3].isAdded()) {
                        ((PersonFragment) mFragments[3]).refreshPersonInfo(GlobalApplication.getMySelf().getId());
                    }
                }
                break;
            case REFRESH_REMIND_INFO:        //刷新个人消息列表
                if(resultCode == RESULT_OK) {
                    ((RemindFragment) mFragments[2]).refreshPrivateListData();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
/*        //终止轮询
        PollingUtils.stopPollingService(this, PollingGetMsgService.class, PollingGetMsgService.ACTION);
        //注销广播
        unregisterReceiver(mRecevier);*/
    }

/*    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
           // String name = intent.getExtras().getString("name");
            Log.i("Recevier", "接收到:");
          //  getNewMsgCount();
         //   ((RemindFragment)mFragments[2]).getNewMsgCount();
            //abortBroadcast();   //Receiver1接收到广播后中断广播
        }

    }*/

/*    int ceshi = 0;
    public void getNewMsgCount() {
        if(isGettingNewMag){
            return ;
        }
        isGettingNewMag = true;
        if (networkStatus.isConnectInternet()) {
            String rootString = getResources().getString(R.string.ROOT) + "msg/count";
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            MyStringRequest mStringRequest = new MyStringRequest(Request.Method.GET, rootString, headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            isGettingNewMag = false;
                            Log.d("getRemindNum:TAG", response);
                            try{
                                JSONObject jo = new JSONObject(response);
                                int privateCount = jo.getInt("privates_count");
                                int commentCount = jo.getInt("coments_count");
                                setBadgeNum(privateCount + commentCount + ceshi++);
                            } catch (Exception e){}
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            isGettingNewMag = false;
                            Log.d("getRemindNum:TAG", "出错");

                        }
                    });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            GlobalApplication.get().getRequestQueue().add(mStringRequest);
        } else {
            isGettingNewMag = false;
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }*/
}
