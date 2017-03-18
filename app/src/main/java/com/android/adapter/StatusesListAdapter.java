package com.android.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.R;
import com.android.model.Statuses;

import java.util.List;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

public class StatusesListAdapter extends BaseAdapter {
    private List<Statuses> mStatuses;
    private Activity mActivity;

    public StatusesListAdapter(Activity activity, List<Statuses> Statuses) {
        this.mActivity = activity;
        this.mStatuses = Statuses;
    }
    @Override
    public int getCount() {
        return mStatuses.size();
    }

    @Override
    public Object getItem(int postion) {
        return mStatuses.get(postion);
    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {


//        //凡是涉及到图片的都是图片的ID，需要在一次请求再获得图片，所以图片的暂时无法赋值
//        if(Statuses.ACTIVITY_TYPE == mStatuses.get(position).getAttach_type()) {
//            view = mActivity.getLayoutInflater().inflate(R.layout.activity_listitem, null);
//            com.android.model.Activity activity =
//                    (com.android.model.Activity) mStatuses.get(position).getAttach_obj();
//            TextView activitySourse = (TextView) view.findViewById(R.id.activity_source);
//            activitySourse.setText(activity.getCreator().getName()+"发布了活动");
//            CircleImageView userAvatar = (CircleImageView) view.findViewById(user_avatar);
//            userAvatar.setImageResource(activity.getCreator().getAvatar());
//            TextView activityTitle = (TextView) view.findViewById(R.id.activity_title);
//            activityTitle.setText(activity.getTitle());
//            TextView activityContent = (TextView) view.findViewById(R.id.activity_content);
//            activityContent.setText(activity.getContent());
//            ImageView activityImage = (ImageView) view.findViewById(R.id.activity_image);
//            activityImage.setImageResource(activity.getImage());//这里应该是要判断是否有图像的
//
//
//        } else if (Statuses.PHOTO_TYPE == mStatuses.get(position).getAttach_type()) {
//            view = mActivity.getLayoutInflater().inflate(R.layout.picture_listitem, null);
//            CircleImageView userAvatar = (CircleImageView) view.findViewById(R.id.user_avatar);
//            TextView pictureSource = (TextView) view.findViewById(R.id.picture_source);
//            com.android.tool.PictureView picture_group = (com.android.tool.PictureView) view.findViewById(R.id.picture_group);
//        } else if(Statuses.NOTIFICATION_TYPE == mStatuses.get(position).getAttach_type()) {
//            view = mActivity.getLayoutInflater().inflate(R.layout.notification_listitem, null);
//            CircleImageView userAvatar = (CircleImageView) view.findViewById(R.id.user_avatar);
//            TextView notificationSource = (TextView) view.findViewById(R.id.notification_source);
//            TextView notificationTitle = (TextView) view.findViewById(R.id.notification_title);
//        }

        switch(mStatuses.get(position).getAttach_type()) {
            case Statuses.ACTIVITY_TYPE:
                view = mActivity.getLayoutInflater().inflate(R.layout.activity_listitem, null);
                break;
            case Statuses.NOTIFICATION_TYPE:
                view = mActivity.getLayoutInflater().inflate(R.layout.notification_listitem, null);
                break;
            case Statuses.PHOTO_TYPE:
                view = mActivity.getLayoutInflater().inflate(R.layout.picture_listitem, null);
                break;
        }

        return view;
    }

    /**
     * 添加数据列表项
     * @param Statuses
     */
    public void addStatusListItem(Statuses Statuses) {
        mStatuses.add(Statuses);
    }

    /**
     * 添加数据列表项
     * @param Statuses
     */
    public void addHeaderStausesListItem(Statuses Statuses) {
        mStatuses.add(0, Statuses);
    }

}
