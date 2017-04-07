package com.android.status;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.R;
import com.android.guide.BaseActivity;
import com.android.tool.DataUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationDetailActivity extends BaseActivity {


    CommentFragment mCommentFragment;
    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.send_btn)
    Button mSendBtn;
    @BindView(R.id.notification_title)
    TextView mNotificationTitle;
    @BindView(R.id.notification_source)
    TextView mNotificationSource;
    @BindView(R.id.notification_content)
    TextView mNotificationContent;
    @BindView(R.id.comment_count)
    TextView mCommentCount;
    @BindView(R.id.do_recommend)
    ImageView mDoRecommend;
    @BindView(R.id.recommend_content)
    FrameLayout mRecommendContent;
    private JSONObject mJsonobject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);
        ButterKnife.bind(this);

        Bundle bundle = this.getIntent().getExtras();
        try {
            mJsonobject = new JSONObject(bundle.getString("jsonStr"));
            setData(mJsonobject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        initCommentList();
        setListener();
    }

    /**
     * 根据传入的builder设置详情信息
     */
    private void setData(JSONObject jsonobject) {
        try {
            mNotificationTitle.setText(jsonobject.getString("title"));
            mNotificationContent.setText(jsonobject.getString("content"));
            mNotificationSource.setText(jsonobject.getJSONObject("creator_obj").getString("name")
            +" "+ DataUtils.stampToDate(DataUtils.DATA_TYPE4, jsonobject.getLong("created_at")));
//            mNotificationTitle.setKeyListener(null);//设置文本不可编辑
//            mNotificationContent.setKeyListener(null);
            mCommentCount.setText("评论(" + jsonobject.getString("comment_count") + ")");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化评论内容
     */
    private void initCommentList() {
/*        try {
            mCommentFragment = CommentFragment.newInstance(Comment.NOTIFICATION_TYPE, mJsonobject.getInt("id"));

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.recommend_content, mCommentFragment);
            transaction.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
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
