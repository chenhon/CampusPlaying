package com.android.status;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.R;
import com.android.guide.BaseActivity;
import com.android.tool.PictureView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class PictureDetailActivity extends BaseActivity {

    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.activity_image)
    PictureView mActivityImage;
    @BindView(R.id.user_avatar)
    CircleImageView mUserAvatar;
    @BindView(R.id.user_name)
    TextView mUserName;
    @BindView(R.id.picture_time)
    TextView mPictureTime;
    @BindView(R.id.picture_content)
    TextView mPictureContent;
    @BindView(R.id.attached_activity_title)
    TextView mAttachedActivityTitle;
    @BindView(R.id.comment_count)
    TextView mCommentCount;
    @BindView(R.id.recommend_content)
    FrameLayout mRecommendContent;
    CommentFragment mCommentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_detail);
        ButterKnife.bind(this);
        initDetail();
        initCommentList();
        setListener();
    }

    /**
     * 根据传入的builder设置详情信息
     */
    private void initDetail() {

    }

    /**
     * 初始化评论内容
     */
    private void initCommentList() {
        //mCommentFragment = CommentFragment.newInstance(Comment.PHOTO_TYPE, 123);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.recommend_content, mCommentFragment);
        transaction.commit();
    }
    private void setListener() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
