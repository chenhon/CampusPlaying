package com.android.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.R;
import com.android.person.PersonListActivity;
import com.android.person.PersonOnClickListenerImpl;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.DataUtils;
import com.android.tool.PictureView;
import com.android.tool.ShareUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

public class ActivityListAdapter extends BaseAdapter {
    private List<com.android.model.Activity> mActivitys;
    private Activity mActivity;

    public ActivityListAdapter(Activity activity) {
        this.mActivity = activity;
        mActivitys = new ArrayList();
    }

    public void clearListData() {
        mActivitys.clear();
    }

    public int getUid(int position) {
        return mActivitys.get(position).getCreatorId();
    }

    public int getAid(int position) {
        return mActivitys.get(position).getId();
    }

    public void clearAllItem() {
        mActivitys.clear();
    }

    @Override
    public int getCount() {
        return mActivitys.size();
    }

    @Override
    public Object getItem(int postion) {
        return mActivitys.get(postion);
    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        final ViewHolder viewHolder;
        if (convertView == null) {
            view = mActivity.getLayoutInflater().inflate(R.layout.activity_listitem, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        setActivityView(position, viewHolder);


        return view;
    }

    private void setActivityView(final int position, final ViewHolder viewHolder) {
        try {

            viewHolder.mUserName.setText(mActivitys.get(position).getCreatorName());//发布人名称
            viewHolder.mPublishedTime.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE2, mActivitys.get(position).getTime()));//发布时间
            viewHolder.mActivityTitle.setText(mActivitys.get(position).getTitle());//活动标题
            viewHolder.mActivityContent.setText(mActivitys.get(position).getContent());//活动内容
            viewHolder.mWisherCount.setText(mActivitys.get(position).getWisherCount() + "");//活动热度
            viewHolder.mParticipantCount.setText(mActivitys.get(position).getParticipantCount() + "");//已参与人数
            switch (mActivitys.get(position).getState()) { //活动进行状态
                case 0:
                    viewHolder.mActivityStatusText.setText("发起中");
                    viewHolder.mActivityStatusImage.setImageResource(R.drawable.detail_sign_up);
                    break;
                case 1:
                    viewHolder.mActivityStatusText.setText("进行中");
                    viewHolder.mActivityStatusImage.setImageResource(R.drawable.detail_processing);
                    break;
                case 2:
                    viewHolder.mActivityStatusText.setText("已结束");
                    viewHolder.mActivityStatusImage.setImageResource(R.drawable.detail_end);
                    break;

            }
            //点击头像进入用户主页
            viewHolder.mUserAvatar.setOnClickListener(new PersonOnClickListenerImpl(mActivity, mActivitys.get(position).getCreatorId()));
            BitmapLoaderUtil.getInstance().getImage(viewHolder.mUserAvatar, BitmapLoaderUtil.TYPE_ORIGINAL, mActivitys.get(position).getAvatarId());
            //获取活动图像
            BitmapLoaderUtil.getInstance().getImage(viewHolder.mActivityImage, BitmapLoaderUtil.TYPE_MEDIAN, mActivitys.get(position).getImageId());
            //活动分享
            viewHolder.mActivityShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShareUtil.showShare(mActivity, mActivitys.get(position).getId(), mActivitys.get(position).getTitle());
                }
            });
            //查看活动参与者
            viewHolder.mParticipateUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PersonListActivity.startActivity(mActivity, 0 ,PersonListActivity.ACTIVITY_PARTICIPATE,mActivitys.get(position).getId());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }
    }

    /**
     * 添加数据列表项
     *
     * @param json
     */
    public void addActivityListItem(com.android.model.Activity json) {
        mActivitys.add(json);
    }

    /**
     * 添加数据列表项
     *
     * @param json
     */
    public void addHeaderActivityListItem(com.android.model.Activity json) {
        mActivitys.add(0, json);
    }


    static class ViewHolder {
        @BindView(R.id.user_avatar)
        CircleImageView mUserAvatar;
        @BindView(R.id.user_name)
        TextView mUserName;
        @BindView(R.id.published_time)
        TextView mPublishedTime;
        @BindView(R.id.activity_title)
        TextView mActivityTitle;
        @BindView(R.id.activity_content)
        TextView mActivityContent;
        @BindView(R.id.activity_image)
        ImageView mActivityImage;
        @BindView(R.id.picture_group)
        PictureView mPictureGroup;
        @BindView(R.id.participate_user)
        ImageView mParticipateUser;
        @BindView(R.id.participant_count)
        TextView mParticipantCount;
        @BindView(R.id.wisher_count)
        TextView mWisherCount;
        @BindView(R.id.activity_status_image)
        ImageView mActivityStatusImage;
        @BindView(R.id.activity_status_text)
        TextView mActivityStatusText;
        @BindView(R.id.activity_share)
        ImageView mActivityShare;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

