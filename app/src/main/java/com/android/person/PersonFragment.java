package com.android.person;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.R;
import com.android.guide.GlobalApplication;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class PersonFragment extends Fragment {
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.page_title)
    TextView mPageTitle;
    @BindView(R.id.more_btn)
    ImageView mMoreBtn;
    @BindView(R.id.user_portrait)
    CircleImageView mUserPortrait;
    @BindView(R.id.user_name)
    TextView mUserName;
    @BindView(R.id.add_attention_btn)
    Button mAddAttentionBtn;
    @BindView(R.id.home)
    TextView mHome;
    @BindView(R.id.to_album)
    TextView mToAlbum;
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
    TextView mInterestedActivity;
    @BindView(R.id.published_activity)
    TextView mPublishedActivity;
    @BindView(R.id.recent_published_activity)
    TextView mRecentPublishedActivity;

    public PersonFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.person_fragment, container, false);
        ButterKnife.bind(this, view);

        setListener();
        initView();
        return view;
    }

    private void initView() {
        mUserPortrait.setImageBitmap(GlobalApplication.getMyAvatar());
        mUserName.setText(GlobalApplication.getMySelf().getName());
        mJoinActivityNum.setText(String.valueOf(GlobalApplication.getMySelf().getActivitysCount()));
        mAttentionPersonNum.setText(String.valueOf(GlobalApplication.getMySelf().getFollowersCount()));
        mFansNum.setText(String.valueOf(GlobalApplication.getMySelf().getFansCount()));
    }

    /**
     * 设置监听事件
     */
    private void setListener() {
        mToAttentionPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FollowActivity.startActivity(getActivity(), FollowActivity.RELATION_MYSLEF, FollowActivity.DIRECTION_ATTENTION, GlobalApplication.getMySelf().getId());
            }
        });

        mToFans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FollowActivity.startActivity(getActivity(), FollowActivity.RELATION_MYSLEF, FollowActivity.DIRECTION_FANS, GlobalApplication.getMySelf().getId());
            }
        });

        mToJoinActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityListActivity.startActivity(getActivity(), ActivityListActivity.RELATION_MYSLEF, ActivityListActivity.TYPE_PARTICIPATED, GlobalApplication.getMySelf().getId());
            }
        });
        mInterestedActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityListActivity.startActivity(getActivity(), ActivityListActivity.RELATION_MYSLEF, ActivityListActivity.TYPE_INTERESTED, GlobalApplication.getMySelf().getId());
            }
        });
        mPublishedActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityListActivity.startActivity(getActivity(), ActivityListActivity.RELATION_MYSLEF, ActivityListActivity.TYPE_PUBLISHED, GlobalApplication.getMySelf().getId());
            }
        });
        mRecentPublishedActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecentStatusActivity.startActivity(getActivity(), RecentStatusActivity.RELATION_MYSLEF, GlobalApplication.getMySelf().getId());
            }
        });
        mToAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumActivity.startActivity(getActivity(), AlbumActivity.RELATION_MYSELF, GlobalApplication.getMySelf().getId());
            }
        });
    }

}
