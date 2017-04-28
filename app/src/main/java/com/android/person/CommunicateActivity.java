package com.android.person;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.adapter.MsgAdapter;
import com.android.guide.BaseActivity;
import com.android.GlobalApplication;
import com.android.model.Private;
import com.android.tool.DataUtils;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.RequestManager;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 私信
 * 进入私信界面时获取到对方的 头像 和 id
 */
public class CommunicateActivity extends BaseActivity {
    private static final int LOAD_DATA_COUNT = 10;
    private static final int BACKWARD = 1; //加载更多 （默认）
    private static final int FORWARD = 0;   //刷新动态

    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.user_name)
    TextView mUserName;
    @BindView(R.id.msg_recycle_view)
    RecyclerView mMsgRecycleView;
    @BindView(R.id.input_text)
    EditText mInputText;
    @BindView(R.id.send_btn)
    TextView mSendBtn;
    @BindView(R.id.user_center)
    ImageView mUserCenter;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    private MsgAdapter adapter;
    private List<Private> msgs = new ArrayList();
    private PollingService.PrivateBind mBind;
    private PollingService mService;
    private NetworkConnectStatus networkStatus;//网络连接状态

    private ProgressHUD mProgressHUD;
    private int avatarId; //对方的头像id
    private int uid;//对方的id
    private String uName;//对方的昵称
    private String rootString;
    private Boolean isLoadData = false;//记录是否已经加载了数据
    private boolean isStartPolling = false;
    private long frontStamp;//最前面的时间戳  小
    private long endStamp; //最近加载动态的一条时间戳 大
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // System.out.println("onServiceConnected:运行");
            mBind = (PollingService.PrivateBind) service;
            mService = mBind.getMyService();    //获取服务的实例
            mService.setListener(new PollingService.GetNewPrivateListener() {
                @Override
                public void getNewPrivate() {
                    CommunicateActivity.this.getPrivateList(FORWARD);//轮询检测新的消息
                    // CommunicateActivity.this.getPrivateList(BACKWARD);//有数据可测试
                    // System.out.println("Service:onDestroy");
                }
            });
            //调用startService会开始运行onStartCommand
            //回调函数在onStartCommand中调用，所以，回调函数要在startService之前设置
            Intent bindIntent = new Intent(CommunicateActivity.this, PollingService.class);
            startService(bindIntent);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    public static void startActivity(Activity activity, int uid, int avatarId, String uName) {
        Bundle bundle = new Bundle();
        bundle.putInt("uid", uid); //传递用户id
        bundle.putInt("avatarId", avatarId); //传递用户头像id
        bundle.putString("uName", uName); //传递用户昵称
        Intent intent = new Intent(activity, CommunicateActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
    }
    public static void startActivityForResult(Activity activity, int request,int uid, int avatarId, String uName) {
        Bundle bundle = new Bundle();
        bundle.putInt("uid", uid); //传递用户id
        bundle.putInt("avatarId", avatarId); //传递用户头像id
        bundle.putString("uName", uName); //传递用户昵称
        Intent intent = new Intent(activity, CommunicateActivity.class);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent,request);
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicate);
        ButterKnife.bind(this);

        initData();
    }
    private void initData() {
        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null) {
            uid = bundle.getInt("uid"); //获取对方的id
            avatarId = bundle.getInt("avatarId"); //获取对方的头像id
            uName = bundle.getString("uName"); //获取对方的昵称
        }
        networkStatus = new NetworkConnectStatus(this);
        rootString = getResources().getString(R.string.ROOT) + "msg/private/"+uid;
        networkStatus = new NetworkConnectStatus(this);

        initView();
        setListener();

        getPrivateList(BACKWARD);//拉取最新消息
        //startCommunicate();//开始轮询
    }

    private void initView(){
        mUserName.setText(uName);
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mMsgRecycleView.setLayoutManager(layoutManager);
        msgs = new ArrayList();
        adapter = new MsgAdapter(this, msgs, uid, avatarId);
        mMsgRecycleView.setAdapter(adapter);
    }
    void getPrivateList(int direction){   //向后加载数据
        final int dir;
        System.out.println("拉取数据");
     //   mSwipeRefresh.setRefreshing(true);//转圈
        long cursor;
        if(!isLoadData) {     //没有加载过数据那么一定是加载最新的数据，向后加载
            cursor = DataUtils.getCurrentTime();
            dir = BACKWARD;
        } else {
            if(direction == BACKWARD) {  //向后加载
                dir = BACKWARD;
                cursor = frontStamp;//小的时间戳
           } else {     //其他情况向前
                dir = FORWARD;
                cursor = endStamp;//大的时间戳
            }
        }
        if (networkStatus.isConnectInternet()) {
            //默认向后
            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("type","cursor")//选择游标形式
                    .with("count",String.valueOf(LOAD_DATA_COUNT))//条数
                    .with("cursor",String.valueOf(cursor)) //时间戳游标
                    .with("direction",String.valueOf(direction)); //加载方向，为加载最新
            VolleyRequestParams headerParams = new VolleyRequestParams()
                    .with("token", GlobalApplication.getToken())
                    .with("Accept","application/json"); // 数据格式设置为json
            MyStringRequest mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            //isLoadData = true;
                            Log.d("CommunicateActivity:TAG", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonArr = jsonObject.getJSONArray("items");
                                if(dir == BACKWARD){
                                    for (int i = 0; i < jsonArr.length() ; i++) {
                                        JSONObject jsonObject1 = jsonArr.getJSONObject(i);
                                        Private pri = new Private();
                                        pri.setDirection(jsonObject1.getInt("direction"));
                                        pri.setContent(jsonObject1.getString("content"));
                                        pri.setCreated_at(jsonObject1.getLong("created_at"));
                                        msgs.add(0,pri);
                                        if(jsonArr.length()-1 == i) {//记录小的时间戳
                                            frontStamp = pri.getCreated_at();
                                        }
                                        if ((!isLoadData) && (0==i)){//记录大的时间戳
                                            endStamp = pri.getCreated_at();
                                        }
                                    }
                                } else {
                                    for (int i = jsonArr.length()-1; i >= 0  ; i--) {
                                        JSONObject jsonObject1 = jsonArr.getJSONObject(i);
                                        Private pri = new Private();
                                        pri.setDirection(jsonObject1.getInt("direction"));
                                        pri.setContent(jsonObject1.getString("content"));
                                        pri.setCreated_at(jsonObject1.getLong("created_at"));
                                        msgs.add(pri);
                                        if (0==i){//记录大的时间戳
                                            endStamp = pri.getCreated_at();
                                        }
                                    }
                                }
                                if(jsonArr.length() > 0) {
                                    isLoadData = true;
                                    adapter.notifyDataSetChanged();
                                    if(dir == FORWARD){
                                        mMsgRecycleView.scrollToPosition(msgs.size()-1);
                                    }
                                } else {
                                 //   Toast.makeText(CommunicateActivity.this, "对话已加载完".toString(), Toast.LENGTH_SHORT).show();
                                }
                                if(!isStartPolling){
                                    isStartPolling = true;
                                    mMsgRecycleView.scrollToPosition(msgs.size()-1);
                                    startCommunicate();//开始轮询
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("getTIMELINE:TAG", e.toString());
                            }
                            //list中添加数据
                            mSwipeRefresh.setRefreshing(false);//结束转圈
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("CommunicateActivity:TAG", "出错");
                            Log.d("CommunicateActivity:TAG", error.getMessage(),error);
                            mSwipeRefresh.setRefreshing(false);//结束转圈
                        }
                    });
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            GlobalApplication.get().getRequestQueue().add(mStringRequest);
        }else {
            mSwipeRefresh.setRefreshing(false);//结束转圈
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setListener() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = mInputText.getText().toString();
                if (!"".equals(msg)) {
                    sedMsg(msg);
                    mInputText.setText("");
                }
            }
        });
        mUserCenter.setOnClickListener(new PersonOnClickListenerImpl(CommunicateActivity.this,uid));//进入对方主页
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {//下拉加载更多
                getPrivateList(BACKWARD);
                System.out.println("手动下拉加载。。。");
            }
        });
    }
    /**
     * 启动轮询
     */
    private void startCommunicate() {
        Intent bindIntent = new Intent(this, PollingService.class);
        bindService(bindIntent, mConnection, BIND_AUTO_CREATE); //绑定后只会运行onCreate()
        // startService(bindIntent);
    }

    /**
     * 发送一条消息
     * @param content  消息内容
     */
    private void sedMsg(String content) {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(this, "发送中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });

            VolleyRequestParams bodyParams = new VolleyRequestParams()
                    .with("content", content);

            VolleyRequestParams headerParams = new VolleyRequestParams()
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            MyStringRequest mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Private pri = new Private();
                                pri.setDirection(jsonObject.getInt("direction"));
                                pri.setContent(jsonObject.getString("content"));
                                pri.setCreated_at(jsonObject.getLong("created_at"));
                                msgs.add(pri);
                                adapter.notifyDataSetChanged();
                                endStamp = pri.getCreated_at();
                                if(!isLoadData) {
                                    isLoadData = true;
                                    frontStamp = endStamp;
                                }
                                mMsgRecycleView.scrollToPosition(msgs.size()-1);
                                getPrivateList(BACKWARD);//加载最新数据
                            } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("getTIMELINE:TAG", e.toString());
                        }
                            mProgressHUD.dismiss();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Toast.makeText(CommunicateActivity.this, "网络繁忙请稍后再试".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(400*1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            GlobalApplication.get().getRequestQueue().add(mStringRequest);
        } else {
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Stop polling service
        System.out.println("Stop polling service...");
        unbindService(mConnection);

        Intent bindIntent = new Intent(this, PollingService.class);
        stopService(bindIntent);
        // PollingUtils.stopPollingService(this, PollingService.class, PollingService.ACTION);
    }

    /**
     * 功能描述： onTouchEvent事件中，点击android系统的软键盘外的其他地方，可隐藏软键盘，以免遮挡住输入框
     * @param event
     *            当前的触控事件
     * @return boolean类型的标记位。当用户点击后才会隐藏软键盘。
     */
    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        return imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }
}
