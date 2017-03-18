package com.android.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.R;
import com.android.tool.FlowLayout;
import com.android.tool.PictureView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActivityDetailActivity extends AppCompatActivity {


    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.page_title)
    TextView mPageTitle;
    @BindView(R.id.share_btn)
    ImageView mShareBtn;
    @BindView(R.id.activity_image)
    PictureView mActivityImage;
    @BindView(R.id.created_time)
    TextView mCreatedTime;
    @BindView(R.id.activity_title)
    TextView mActivityTitle;
    @BindView(R.id.wisher_info)
    TextView mWisherInfo;
    @BindView(R.id.no_join_activity)
    CheckBox mNoJoinActivity;
    @BindView(R.id.join_activity)
    CheckBox mJoinActivity;
    @BindView(R.id.join_btn)
    Button mJoinBtn;
    @BindView(R.id.activity_time)
    TextView mActivityTime;
    @BindView(R.id.activity_address)
    TextView mActivityAddress;
    @BindView(R.id.ll_to_map)
    LinearLayout mLlToMap;
    @BindView(R.id.activity_fee)
    TextView mActivityFee;
    @BindView(R.id.wisher_total)
    TextView mWisherTotal;
    @BindView(R.id.ll_to_creater_page)
    LinearLayout mLlToCreaterPage;
    @BindView(R.id.notification_count)
    TextView mNotificationCount;
    @BindView(R.id.ll_to_notify)
    LinearLayout mLlToNotify;
    @BindView(R.id.ll_to_comment)
    LinearLayout mLlToComment;
    @BindView(R.id.activity_tag)
    FlowLayout mActivityTag;
    @BindView(R.id.ll_to_picture)
    LinearLayout mLlToPicture;
    @BindView(R.id.activity_content)
    TextView mActivityContent;
    private String json;
    private JSONObject jsonobject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        showData();
    }

    private void showData() {
        try {
            Bundle bundle = this.getIntent().getExtras();
            json = bundle.getString("json");
            jsonobject = new JSONObject(json);

            mActivityTitle.setText(jsonobject.getString("title"));
            //参与信息没设置
            JSONArray jsonArray = jsonobject.getJSONArray("tags");
            for (int i = 0; i < jsonArray.length(); i++) {
                View v = LayoutInflater.from(ActivityDetailActivity.this).inflate(R.layout.text_tag_unit, mActivityTag, false);
                TextView tg = (TextView) v.findViewById(R.id.tag_name);
                tg.setText(jsonArray.getString(i));
                mActivityTag.addView(v);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setTag(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            View v = LayoutInflater.from(ActivityDetailActivity.this).inflate(R.layout.text_tag_unit, mActivityTag, false);
            TextView tg = (TextView) v.findViewById(R.id.tag_name);
            try {
                tg.setText(jsonArray.getString(i));
                mActivityTag.addView(v);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
