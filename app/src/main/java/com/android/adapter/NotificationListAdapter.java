package com.android.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.R;
import com.android.model.Notification;
import com.android.person.PersonOnClickListenerImpl;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.DataUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2017/3/8 0008.
 * 通知适配器
 */

public class NotificationListAdapter extends BaseAdapter {
    private List<Notification> mNotifications; //通知
    private Activity mActivity;

    public NotificationListAdapter(Activity activity) {
        this.mActivity = activity;
        this.mNotifications = new ArrayList<>();
    }

    public void clearListData() {
        mNotifications.clear();
    }
    public int getNotificationId(int position) {
        return mNotifications.get(position).getId();
    }

    @Override
    public int getCount() {
        return mNotifications.size();
    }

    @Override
    public Object getItem(int position) {
        return mNotifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view;
        final NotificationListAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            view = mActivity.getLayoutInflater().inflate(R.layout.notification_listitem, null);
            viewHolder = new NotificationListAdapter.ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (NotificationListAdapter.ViewHolder)view.getTag();
        }
        try {
            viewHolder.mUserName.setText(mNotifications.get(position).getName());//发布人名称
            viewHolder.mPublishedTime.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE2, mNotifications.get(position).getCreatedTime()));//发布时间
            viewHolder.mNotificationTitle.setText(mNotifications.get(position).getTitle());//活动标题
            viewHolder.mNotificationContent.setText(mNotifications.get(position).getContent());//活动内容
            BitmapLoaderUtil.getInstance().getImage(viewHolder.mUserAvatar,BitmapLoaderUtil.TYPE_ORIGINAL,mNotifications.get(position).getAvatarId());
            viewHolder.mUserAvatar.setOnClickListener(new PersonOnClickListenerImpl(mActivity,mNotifications.get(position).getCreatorId()));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }


        return view;
    }


    /**
     * 添加数据列表项
     */
    public void addNotificationListItem(Notification notification) {
        mNotifications.add(notification);
    }

    static class ViewHolder {
        @BindView(R.id.user_avatar)
        CircleImageView mUserAvatar;
        @BindView(R.id.user_name)
        TextView mUserName;
        @BindView(R.id.published_time)
        TextView mPublishedTime;
        @BindView(R.id.notification_title)
        TextView mNotificationTitle;
        @BindView(R.id.notification_content)
        TextView mNotificationContent;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
