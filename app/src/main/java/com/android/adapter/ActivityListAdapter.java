package com.android.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

public class ActivityListAdapter extends BaseAdapter {
    private List<com.android.model.Activity> mActivityList;
    private Activity mActivity;

    public ActivityListAdapter(Activity activity, List<com.android.model.Activity> activityList) {
        this.mActivity = activity;
        mActivityList = activityList;
    }

    @Override
    public int getCount() {
        return mActivityList.size();
    }

    @Override
    public Object getItem(int postion) {
        return mActivityList.get(postion);
    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        com.android.model.Activity activity = mActivityList.get(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null) {
            view = mActivity.getLayoutInflater().inflate(R.layout.activity_listitem, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.mActivityTitle.setText("吃吃吃吃吃！");

//        //凡是涉及到图片的都是图片的ID，需要在一次请求再获得图片，所以图片的暂时无法赋值
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


        return view;
    }

    /**
     * 添加数据列表项
     *
     * @param activity
     */
    public void addActivityListItem(com.android.model.Activity activity) {
        mActivityList.add(activity);
    }

    /**
     * 添加数据列表项
     *
     * @param activity
     */
    public void addHeaderActivityListItem(com.android.model.Activity activity) {
        mActivityList.add(0, activity);
    }

    class ViewHolder {
        @BindView(R.id.user_avatar)
        CircleImageView mUserAvatar;
        @BindView(R.id.activity_source)
        TextView mActivitySource;
        @BindView(R.id.activity_title)
        TextView mActivityTitle;
        @BindView(R.id.activity_content)
        TextView mActivityContent;
        @BindView(R.id.activity_image)
        ImageView mActivityImage;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
