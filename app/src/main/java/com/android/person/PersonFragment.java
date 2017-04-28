package com.android.person;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.GlobalApplication;
import com.android.R;
import com.android.activity.ListActivity;
import com.android.bottomnavigation.MainNavigationActivity;
import com.android.guide.MainActivity;
import com.android.person.edit.PersonEdit;
import com.android.status.RecentStatusActivity;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class PersonFragment extends Fragment {

    @BindView(R.id.title_name)
    TextView mTitleName;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.user_portrait)
    CircleImageView mUserPortrait;
    @BindView(R.id.user_name)
    TextView mUserName;
    @BindView(R.id.iv_gender)
    ImageView mIvGender;
    @BindView(R.id.user_description)
    TextView mUserDescription;
    @BindView(R.id.join_activity_num)
    TextView mJoinActivityNum;
    @BindView(R.id.to_join_activity)
    LinearLayout mToJoinActivity;
    @BindView(R.id.attention_person_num)
    TextView mAttentionPersonNum;
    @BindView(R.id.to_attention_person)
    LinearLayout mToAttentionPerson;
    @BindView(R.id.fans_num)
    TextView mFansNum;
    @BindView(R.id.to_fans)
    LinearLayout mToFans;
    @BindView(R.id.interested_activity)
    LinearLayout mInterestedActivity;
    @BindView(R.id.published_activity)
    LinearLayout mPublishedActivity;
    @BindView(R.id.to_album)
    LinearLayout mToAlbum;
    @BindView(R.id.recent_status)
    LinearLayout mRecentStatus;
    private String rootString;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private ProgressHUD mProgressHUD;

    public PersonFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.person_fragment, container, false);
        ButterKnife.bind(this, view);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        setHasOptionsMenu(true);
        rootString = getResources().getString(R.string.ROOT) + "token";
        networkStatus = new NetworkConnectStatus(getActivity());
        setListener();
        initView();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.tool_menu, menu);
    }

    /**
     * 菜单栏事件处理
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.person_set://个人设置
                Intent intent1 = new Intent(getActivity(), PersonEdit.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivityForResult(intent1, MainNavigationActivity.REFRESH_PERSON_INFO);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.exit_account:    //登出账号
                if (networkStatus.isConnectInternet()) {   //有网络连接则释放token
                    VolleyRequestParams headerParams = new VolleyRequestParams()
                            .with("token", GlobalApplication.getToken());
                    mStringRequest = new MyStringRequest(Request.Method.DELETE, rootString, headerParams, null,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        Log.e("MainActivity:login:TAG", response);
                                        GlobalApplication.setToken("");
                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        getActivity().startActivity(intent);
                                        getActivity().finish();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getTIMELINE:TAG", "出错");
                            Log.d("getTIMELINE:TAG", error.getMessage(), error);
                        }
                    });

                    mQueue = GlobalApplication.get().getRequestQueue();
                    mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
                    mQueue.add(mStringRequest);
                } else {
                    GlobalApplication.setToken("");
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }

                break;
        }
        return true;
    }

    private void initView() {
        mToolbar.setTitle("");
        refreshPersonInfo(GlobalApplication.getMySelf().getId());
    }

    /**
     * 设置监听事件
     */
    private void setListener() {
        mToAttentionPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonListActivity.startActivityForResult(getActivity(),  MainNavigationActivity.REFRESH_PERSON_INFO, PersonListActivity.RELATION_MYSLEF, PersonListActivity.DIRECTION_ATTENTION, GlobalApplication.getMySelf().getId());
            }
        });

        mToFans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonListActivity.startActivityForResult(getActivity(),  MainNavigationActivity.REFRESH_PERSON_INFO, PersonListActivity.RELATION_MYSLEF, PersonListActivity.DIRECTION_FANS, GlobalApplication.getMySelf().getId());
            }
        });

        mToJoinActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListActivity.startActivityForResult(getActivity(), MainNavigationActivity.REFRESH_PERSON_INFO,ListActivity.RELATION_MYSLEF, ListActivity.TYPE_PARTICIPATED, GlobalApplication.getMySelf().getId());
            }
        });
        mInterestedActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListActivity.startActivityForResult(getActivity(), MainNavigationActivity.REFRESH_PERSON_INFO,ListActivity.RELATION_MYSLEF, ListActivity.TYPE_INTERESTED, GlobalApplication.getMySelf().getId());
            }
        });
        mPublishedActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListActivity.startActivityForResult(getActivity(),  MainNavigationActivity.REFRESH_PERSON_INFO,ListActivity.RELATION_MYSLEF, ListActivity.TYPE_PUBLISHED, GlobalApplication.getMySelf().getId());
            }
        });
        mRecentStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecentStatusActivity.startActivityForResult(getActivity(),MainNavigationActivity.REFRESH_PERSON_INFO, RecentStatusActivity.RELATION_MYSLEF, GlobalApplication.getMySelf().getId());
            }
        });
        mToAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumActivity.startActivity(getActivity(), AlbumActivity.RELATION_MYSELF, GlobalApplication.getMySelf().getId());
            }
        });
    }

    /**
     * 刷新个人信息
     * @param uid
     */
    public void refreshPersonInfo(int uid) {
        VolleyRequestParams headerParams = new VolleyRequestParams()
                .with("token", GlobalApplication.getToken())
                .with("Accept","application/json"); // 数据格式设置为json
        //Toast.makeText(MainActivity.this, "getUserInfo", Toast.LENGTH_SHORT).show();
        mStringRequest = new MyStringRequest(Request.Method.GET, getResources().getString(R.string.ROOT)+"user/"+uid, headerParams, null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e("PersonInfo:login:TAG", response);
                            JSONObject jsObject= new JSONObject(response);
                            GlobalApplication.getMySelf().setDescription(jsObject.getString("description"));
                            GlobalApplication.getMySelf().setUser(jsObject.getString("user"));
                            GlobalApplication.getMySelf().setName(jsObject.getString("name"));
                            GlobalApplication.getMySelf().setAvatar(jsObject.getInt("avatar"));
                            GlobalApplication.getMySelf().setGender(jsObject.getInt("gender"));
                            GlobalApplication.getMySelf().setFollowersCount(jsObject.getInt("followers_count"));
                            GlobalApplication.getMySelf().setFansCount(jsObject.getInt("fans_count"));
                            GlobalApplication.getMySelf().setActivitysCount(jsObject.getInt("activities_count"));
                            // GlobalApplication.setMyAvatar(mUploadAvatar);

                            mUserName.setText(GlobalApplication.getMySelf().getName());
                            if (GlobalApplication.getMySelf().getGender() == 0) { //男生
                                mIvGender.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.male_icon));
                            } else {
                                mIvGender.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.female_icon));
                            }
                            mUserDescription.setText(GlobalApplication.getMySelf().getDescription());
                            mJoinActivityNum.setText(String.valueOf(GlobalApplication.getMySelf().getActivitysCount()));
                            mAttentionPersonNum.setText(String.valueOf(GlobalApplication.getMySelf().getFollowersCount()));
                            mFansNum.setText(String.valueOf(GlobalApplication.getMySelf().getFansCount()));
                            BitmapLoaderUtil.getInstance().getImage(mUserPortrait, BitmapLoaderUtil.TYPE_ORIGINAL, jsObject.getInt("avatar"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "登录失败1".toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("getTIMELINE:TAG", "出错");
                Log.d("getTIMELINE:TAG", error.getMessage(),error);
            }
        });
        mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
        GlobalApplication.get().getRequestQueue().add(mStringRequest);
    }

}
