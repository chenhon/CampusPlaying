package com.android.search;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.GlobalApplication;
import com.android.R;
import com.android.activity.DetailActivity;
import com.android.adapter.ActivityListAdapter;
import com.android.adapter.PersonListAdapter;
import com.android.BaseActivity;
import com.android.model.Activity;
import com.android.person.PersonOnClickListenerImpl;
import com.android.tool.FlowLayout;
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

import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends BaseActivity {
    private static final int LOAD_DATA_COUNT = 5;//每页加载10条数据
    private static final int LOAD_USER_COUNT = 1;//每页加载10条数据
    private static int SEARCH_ACTIVITY = 0;
    private static int SEARCH_USER = 1;
    private static int ORDER_TIME = 0;
    private static int ORDER_HOT = 1;
    String[] mItems;
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.spinner1)
    Spinner mSpinner1;
    @BindView(R.id.et_search_input)
    EditText mEtSearchInput;
    @BindView(R.id.iv_search_delete)
    ImageView mIvSearchDelete;
    @BindView(R.id.do_search)
    TextView mDoSearch;
    @BindView(R.id.activity_tag)
    FlowLayout mActivityTag;
    @BindView(R.id.ll_activity_tag)
    LinearLayout mLlActivityTag;
    @BindView(R.id.ll_order_byhot)
    LinearLayout mLlOrderByhot;
    @BindView(R.id.ll_order_bytime)
    LinearLayout mLlOrderBytime;
    @BindView(R.id.ll_order)
    LinearLayout mLlOrder;
    @BindView(R.id.pullListView)
    PullToRefreshListView mPullListView;
    @BindView(R.id.tv_order_byhot)
    TextView mTvOrderByhot;
    @BindView(R.id.tv_order_bytime)
    TextView mTvOrderBytime;

    private ActivityListAdapter activityAdapter;
    private PersonListAdapter followAdapter;
    private ArrayAdapter<String> spinnerAdapter;
    private ListView mListView;
    private RequestQueue mQueue;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private String rootString;

    private int searchType = SEARCH_ACTIVITY;//默认搜索活动
    private int activityType = 0; //0表示全部
    private int activityOrder = ORDER_TIME;//默认活动按时间排序
    private boolean isLoadActivity = false;
    private boolean isLoadUser = false;

    private int listTotal;//搜索总数
    private int loadPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        mItems = getResources().getStringArray(R.array.searchTypes);
        mQueue = GlobalApplication.get().getRequestQueue();
        networkStatus = new NetworkConnectStatus(this);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner1.setAdapter(spinnerAdapter);
        initListView();
        setListener();
    }

    /**
     * 初始化列表的listView
     */
    private void initListView() {
        mListView = mPullListView.getRefreshableView();//获取动态列表控件
        mListView.setCacheColorHint(00000000);//此设置使得listview在滑动过程中不会出现黑色的背景
        mListView.setDivider(null);
        activityAdapter = new ActivityListAdapter(this);
        mListView.setAdapter(activityAdapter);//默认搜索活动
        followAdapter = new PersonListAdapter(this);
    }

    /**
     * 设置监听事件
     */
    private void setListener() {

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(searchType == SEARCH_ACTIVITY){ //跳转到活动详情页
                    Bundle bundle = new Bundle();
                    bundle.putInt("aid", activityAdapter.getAid(position-1)); //活动id
                    bundle.putInt("creatorId", activityAdapter.getUid(position-1)); //发布活动着id
                    Intent intent = new Intent(SearchActivity.this, DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtras(bundle);  //传入详细信息
                    SearchActivity.this.startActivity(intent);
                } else if(searchType == SEARCH_USER) {//跳转到用户详情界面
                    new PersonOnClickListenerImpl(SearchActivity.this, followAdapter.getId(position-1)).onClick(view);
                }
            }
        });
        //选择搜索类型
        mSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if(pos == SEARCH_ACTIVITY) {
                    initActivityView();
                } else if(pos == SEARCH_USER) {
                    initUserView();
                }
                String str = mEtSearchInput.getText().toString();
                if((str != null)||(str.length() > 0)) {
                    doSearch();     //修改 2017-05-03
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // Another interface callback
            }

        });

        //对标签项监听
        for (int i = 0; i < mActivityTag.getChildCount(); i++) {
            final int position = i;
            mActivityTag.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(activityType != 0) {    //恢复上一个标签
                        ((TextView) mActivityTag.getChildAt(activityType-1)).setBackgroundColor(getResources().getColor(R.color.tag_unselected));
                    }
                    if(activityType != position + 1) { //点的不是同一个标签，则切换标签
                        ((TextView) mActivityTag.getChildAt(position)).setBackgroundColor(Color.RED);
                        activityType = position + 1;//记录选中标签的位置，没有选择标签则默认是0
                        if((lastKeyWord != null) && (lastKeyWord.length() != 0)) {
                            lastKeyWord = "";   //重新执行搜索
                            doSearch();
                        }
                    } else {      //点的是同一个标签，则消除选中状态
                        activityType = 0;
                        doSearch();     //修改 2017-05-03
                    }
                }
            });
        }

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mDoSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch();
            }
        });
        mEtSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch();//软键盘的搜索
                    return true;
                }
                return false;
            }
        });
        mLlOrderBytime.setOnClickListener(new View.OnClickListener() { //排序选择
            @Override
            public void onClick(View v) {
                if(activityOrder == ORDER_TIME) { //重复点击
                    return;
                }
                mTvOrderBytime.setTextColor(getResources().getColor(R.color.check_selected));
                mTvOrderByhot.setTextColor(getResources().getColor(R.color.check_unselected));
                activityOrder = ORDER_TIME;
                if((lastKeyWord != null) && (lastKeyWord.length() != 0)) {
                    lastKeyWord = "";   //重新执行搜索
                    doSearch();
                }
            }
        });
        mLlOrderByhot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activityOrder == ORDER_HOT) { //重复点击
                    return;
                }
                mTvOrderBytime.setTextColor(getResources().getColor(R.color.check_unselected));
                mTvOrderByhot.setTextColor(getResources().getColor(R.color.check_selected));
                activityOrder = ORDER_HOT;
                if((lastKeyWord != null) && (lastKeyWord.length() != 0)) {
                    lastKeyWord = "";   //重新执行搜索
                    doSearch();
                }
            }
        });

        mPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            //下拉刷新
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
            //上拉加载更多
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
               // 搜索过用户
                if((searchType == SEARCH_USER)&& (lastKeyWord != null) && (lastKeyWord.length() != 0)) {
                    searchUser(lastKeyWord);//加载更多
                } else if((searchType == SEARCH_ACTIVITY)&& (lastKeyWord != null) && (lastKeyWord.length() != 0)){
                    searchActivity(lastKeyWord);
                }
               /* if(isLoadActivity) {
                    searchActivity(lastKeyWord);
                } else if(isLoadUser) {
                    searchUser(lastKeyWord);
                }*/
            }
        });
    }

    /**
     * 执行搜索
     */
    String lastKeyWord = "";
    private void doSearch() {

        String keyWord = mEtSearchInput.getText().toString();
        if ((keyWord == null) || (keyWord.length() == 0)) {
            Toast.makeText(SearchActivity.this, "请输入关键字", Toast.LENGTH_SHORT).show();
            return;
        }
        if(lastKeyWord.equals(keyWord)) {  //与上次搜索的关键字一样
            return ;
        }
        if (SEARCH_ACTIVITY == searchType) {
            activityAdapter.clearAllItem();
            loadPage = 0;
            searchActivity(keyWord);
        } else if (SEARCH_USER == searchType) {
            followAdapter.clearAllItem();//先清空列表
            loadPage = 0;
            searchUser(keyWord);
        }
    }
/*********************************搜索用户相关***************************************/
    /**
     * 初始化用户搜索界面
     */
    private void initUserView() {
        //视图设置
       // mEtSearchInput.setText(""); //清空输入框
        mLlActivityTag.setVisibility(View.GONE);//隐藏类型和排序
        mLlOrder.setVisibility(View.GONE);
        activityAdapter.clearAllItem();     //清空活动列表的数据
        activityAdapter.notifyDataSetChanged();
        mListView.setAdapter(followAdapter);//设置适配器

        //数据设置
        loadPage = 0;
        listTotal = 0;
        isLoadActivity = false;
        lastKeyWord = "";//上次该该类型搜索到的关键字
        searchType = SEARCH_USER;//记录搜索类型
    }

    private void searchUser(String keyWord) {
        //   mPullListView.setRefreshing(true);
        String parse;
        try {//jsonObject要和前面的类型一致,此处都是String
            parse = URLEncoder.encode(keyWord, "UTF-8");
        } catch (Exception je) {
            parse = keyWord;
        }
        rootString = getResources().getString(R.string.ROOT) + "search/user/"
                + parse;//base64

        if (networkStatus.isConnectInternet()) {

            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("count", String.valueOf(LOAD_USER_COUNT)) //每页条数
                    .with("page", String.valueOf(loadPage+1));    //获取第二页的数据

            VolleyRequestParams headerParams = new VolleyRequestParams() //header上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json

            Log.d("getUSER:TAG", RequestManager.getURLwithParams(rootString, urlParams) + GlobalApplication.getToken());
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getUSER:TAG", response);
                            parseUserJson(response); //将数据填入到List中去
                            mPullListView.onRefreshComplete();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mPullListView.onRefreshComplete();
                            Toast.makeText(SearchActivity.this, "加载超时，请检查网络连接", Toast.LENGTH_SHORT).show();
                            /*Log.e("getTIMELINE:TAG", error.getMessage(), error);
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Log.e("getTIMELINE:TAG", new String(htmlBodyBytes), error);*/
                        }
                    });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        } else {
            mPullListView.onRefreshComplete();
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * json数据转换成状态item并设置adapter
     *
     * @param json
     */
    private void parseUserJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            listTotal = jsonObject.getInt("total");//总条数
            lastKeyWord = mEtSearchInput.getText().toString();//记录本次搜索的关键字

            JSONArray jsonArr = jsonObject.getJSONArray("results");
            for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                //适配器中添加数据项
                followAdapter.addFollowListItem(jsonArr.getJSONObject(i));
            }
            followAdapter.notifyDataSetChanged();

            if(listTotal == 0) {
                Toast.makeText(this, "未找到相匹配的用户".toString(), Toast.LENGTH_SHORT).show();
            }
            if (jsonArr.length() > 0) {
                //   isLoadUser = true;
                loadPage++;

            } else {
                Toast.makeText(this, "搜索列表已加载完".toString(), Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getFOLLOW:TAG", e.toString());
        }

    }

    /************************************搜索活动相关******************************************/

    /**
     * 初始化活动搜索界面
     */
    private void initActivityView() {
        //视图设置
     //   mEtSearchInput.setText(""); //清空输入框
        mLlActivityTag.setVisibility(View.VISIBLE);//显示类型和排序
        mLlOrder.setVisibility(View.VISIBLE);
        mTvOrderBytime.setTextColor(getResources().getColor(R.color.check_selected));
        mTvOrderByhot.setTextColor(getResources().getColor(R.color.check_unselected));
        for (int i = 0; i < mActivityTag.getChildCount(); i++) {  //设置所有标签的颜色
            ((TextView) mActivityTag.getChildAt(i)).setBackgroundColor(getResources().getColor(R.color.tag_unselected));
        }
        followAdapter.clearAllItem();    //清空用户列表数据
        followAdapter.notifyDataSetChanged();
        mListView.setAdapter(activityAdapter);//设置适配器

        //数据设置
        loadPage = 0;
        listTotal = 0;
        isLoadUser = false;
        lastKeyWord = "";//上次该类型搜索到的关键字
        searchType = SEARCH_ACTIVITY;//记录搜索类型为活动
        activityOrder = ORDER_TIME;//默认活动按时间排序
        activityType = 0; //0表示全部

    }


    private void searchActivity(String keyword) {

        String parse;
        try {//jsonObject要和前面的类型一致,此处都是String
            parse = URLEncoder.encode(keyword, "UTF-8");
        } catch (Exception je) {
            parse = keyword;
        }
        rootString = getResources().getString(R.string.ROOT) + "search/activity/"
                + parse;//Base64.encodeToString(keyword.getBytes(),Base64.DEFAULT);//base64

        if (networkStatus.isConnectInternet()) {

            VolleyRequestParams urlParams = new VolleyRequestParams() //URL上的参数
                    .with("count", String.valueOf(LOAD_DATA_COUNT)) //每页条数
                    .with("page", String.valueOf(loadPage+1))    //获取第二页的数据
                    .with("order", String.valueOf(activityOrder))     //排序
                    .with("filter", String.valueOf(activityType));    //类型

            VolleyRequestParams headerParams = new VolleyRequestParams() //header上的参数
                    //.with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            Log.d("getACTIVITY:TAG", RequestManager.getURLwithParams(rootString, urlParams) + GlobalApplication.getToken());
            mStringRequest = new MyStringRequest(Request.Method.GET, RequestManager.getURLwithParams(rootString, urlParams), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("getACTIVITY:TAG", response);
                            parseActivityJson(response); //将数据填入到List中去
                            mPullListView.onRefreshComplete();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mPullListView.onRefreshComplete();
                            /*Log.e("getTIMELINE:TAG", error.getMessage(), error);
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Log.e("getTIMELINE:TAG", new String(htmlBodyBytes), error);*/
                        }
                    });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(mStringRequest);
        } else {
            mPullListView.onRefreshComplete();
            Toast.makeText(this, getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * json数据转换成状态item并设置adapter
     *
     * @param json
     */
    private void parseActivityJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            listTotal = jsonObject.getInt("total");//总条数
            lastKeyWord = mEtSearchInput.getText().toString();//记录本次搜索的关键字
            JSONArray jsonArr = jsonObject.getJSONArray("results");

            for (int i = 0; i < jsonArr.length(); i++) {//前10条数据
                JSONObject jo = jsonArr.getJSONObject(i);
                //适配器中添加数据项
                Activity activity = new Activity();
                activity.setId(jo.getInt("id"));
                activity.setCreatorId(jo.getInt("creator"));
                activity.setCreatorName(jo.getJSONObject("creator_obj").getString("name"));
                activity.setAvatarId(jo.getJSONObject("creator_obj").getInt("avatar"));
                activity.setTitle(jo.getString("title"));
                activity.setContent(jo.getString("content"));
                activity.setImageId(jo.getInt("image"));
                activity.setTime(jo.getInt("created_at"));
                activity.setWisherCount(jo.getInt("wisher_count"));
                activity.setParticipantCount(jo.getInt("participant_count"));
                activity.setVerifyStatus(jo.getInt("verify_state"));
                activity.setState(jo.getInt("state"));
                activityAdapter.addActivityListItem(activity);
            }
            activityAdapter.notifyDataSetChanged();
            if (jsonArr.length() > 0) {
                loadPage++;
            } else {
                Toast.makeText(this, "活动已加载完".toString(), Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getActivity:TAG", e.toString());
        }

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
