package com.android.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.R;
import com.android.model.Statuses;
import com.android.person.PersonListActivity;
import com.android.person.PersonOnClickListenerImpl;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.DataUtils;
import com.android.tool.ShareUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.android.R.id.activity_image;
import static com.android.R.id.user_avatar;

/**
 * Created by Administrator on 2017/3/8 0008.
 * 动态适配器
 * 图像和用户数据需要再次加载
 * 由于attach_type不会超过10可以以 attach_type + attach_obj.id 唯一标识动态
 */

public class StatusesListAdapter extends BaseAdapter {
    private List<Integer> mAttachTypes; //动态类型
    private List<JSONObject> mAttachObjs;//动态详细信息，对应的Json数据
    private Activity mActivity;

    public StatusesListAdapter(Activity activity) {
        this.mActivity = activity;
        this.mAttachTypes = new ArrayList<>();
        this.mAttachObjs = new ArrayList<>();
    }

    public void clearListData() {
        mAttachTypes.clear();
        mAttachObjs.clear();
    }

    public int getAttachType(int position) {
        return mAttachTypes.get(position);
    }
    public JSONObject getAttachObj(int position) {
        return mAttachObjs.get(position);
    }
    public int getStatusId(int position) {   //获取动态id
        int id = -1;
        try {
            id = mAttachObjs.get(position).getInt("id");
        } catch(Exception e){}
        return id;
    }
    public int  getCreatorId(int position){  //获取发布者id
        int id = -1;
        try {
            id = getAttachObj(position).getJSONObject("creator_obj").getInt("id");
        } catch(Exception e){}
        return id;
    }

    @Override
    public int getCount() {
        return mAttachObjs.size();
    }

    @Override
    public Object getItem(int position) {
        return mAttachObjs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        switch(mAttachTypes.get(position)) {
            case Statuses.ACTIVITY_TYPE:
                view = mActivity.getLayoutInflater().inflate(R.layout.activity_listitem, null);
                setActivityView(position, view);
                break;
            case Statuses.NOTIFICATION_TYPE:
                view = mActivity.getLayoutInflater().inflate(R.layout.notification_listitem, null);
                setNotificationView(position, view);
                break;
            case Statuses.PHOTO_TYPE:
                view = mActivity.getLayoutInflater().inflate(R.layout.picture_listitem, null);
                setPictureView(position, view);
                break;
        }

        return view;
    }

    private void setActivityView(final int position,View view) {
        try{
            TextView userName = (TextView) view.findViewById(R.id.user_name);//发布人名称
            userName.setText(mAttachObjs.get(position).getJSONObject("creator_obj").getString("name"));
            TextView time = (TextView) view.findViewById(R.id.published_time); //发布时间
            time.setText("活动发布于" + DataUtils.stampToDate(DataUtils.DATA_TYPE2, mAttachObjs.get(position).getLong("created_at")));
            TextView title = (TextView) view.findViewById(R.id.activity_title); //活动标题
            title.setText(mAttachObjs.get(position).getString("title"));
            TextView content = (TextView) view.findViewById(R.id.activity_content); //活动内容
            content.setText(mAttachObjs.get(position).getString("content"));
            TextView wisherCount = (TextView) view.findViewById(R.id.wisher_count); //活动热度
            wisherCount.setText(mAttachObjs.get(position).getString("wisher_count"));
            TextView participantCount = (TextView) view.findViewById(R.id.participant_count); //活动内容
            participantCount.setText(mAttachObjs.get(position).getString("participant_count"));
            //活动进行状态
            TextView activityStatusText = (TextView)view.findViewById(R.id.activity_status_text);//活动状态
            ImageView activityStatusImage = (ImageView)view.findViewById(R.id.activity_status_image);//活动状态
            switch(mAttachObjs.get(position).getInt("state")) {
                case 0:
                    activityStatusText.setText("发起中");
                    activityStatusImage.setImageResource(R.drawable.detail_sign_up);
                    break;
                case 1:
                    activityStatusText.setText("进行中");
                    activityStatusImage.setImageResource(R.drawable.detail_processing);
                    break;
                case 2:
                    activityStatusText.setText("已结束");
                    activityStatusImage.setImageResource(R.drawable.detail_end);
                    break;

            }

            final int uid =  mAttachObjs.get(position).getInt("id");
            final String aTitle = mAttachObjs.get(position).getString("title");
            ImageView activityShare = (ImageView)view.findViewById(R.id.activity_share);//活动分享
            activityShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(mActivity, "点击分享! " + position, Toast.LENGTH_SHORT).show();
                    ShareUtil.showShare(mActivity,uid,aTitle);
                }
            });

            //图像请求
            //获取用户头像
            final CircleImageView userAvatar = (CircleImageView) view.findViewById(user_avatar);
            BitmapLoaderUtil.getInstance().getImage(userAvatar,BitmapLoaderUtil.TYPE_ORIGINAL,mAttachObjs.get(position).getJSONObject("creator_obj").getInt("avatar"));
            //设置照片点击事件
            userAvatar.setOnClickListener(new PersonOnClickListenerImpl(mActivity,getAttachObj(position).getInt("creator")));
            //查看活动参与者
            ImageView mParticipateUser = (ImageView)view.findViewById(R.id.participate_user);
            mParticipateUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PersonListActivity.startActivity(mActivity, 0 ,PersonListActivity.ACTIVITY_PARTICIPATE,uid);
                }
            });
            //获取活动图像
            final ImageView activityImage = (ImageView) view.findViewById(activity_image);
            BitmapLoaderUtil.getInstance().getImage(activityImage,BitmapLoaderUtil.TYPE_MEDIAN,mAttachObjs.get(position).getInt("image"));

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }
    }
    private void setNotificationView(final int position,View view) {
        try {
            TextView userName = (TextView) view.findViewById(R.id.user_name);//发布人名称
            userName.setText(mAttachObjs.get(position).getJSONObject("creator_obj").getString("name"));
            TextView time = (TextView) view.findViewById(R.id.published_time); //发布时间
            time.setText("通知发布于" + DataUtils.stampToDate(DataUtils.DATA_TYPE2, mAttachObjs.get(position).getLong("created_at")));
            TextView title = (TextView) view.findViewById(R.id.notification_title); //活动标题
            title.setText(mAttachObjs.get(position).getString("title"));
            TextView content = (TextView) view.findViewById(R.id.notification_content); //活动内容
            content.setText(mAttachObjs.get(position).getString("content"));
            //获取用户头像
            final CircleImageView userAvatar = (CircleImageView) view.findViewById(user_avatar);
            BitmapLoaderUtil.getInstance().getImage(userAvatar,BitmapLoaderUtil.TYPE_ORIGINAL,mAttachObjs.get(position).getJSONObject("creator_obj").getInt("avatar"));
            userAvatar.setOnClickListener(new PersonOnClickListenerImpl(mActivity,getAttachObj(position).getInt("creator")));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }
    }
    private void setPictureView(final int position,View view) {
        try {
            TextView userName = (TextView) view.findViewById(R.id.user_name);//发布人名称
            userName.setText(mAttachObjs.get(position).getJSONObject("creator_obj").getString("name"));
            TextView time = (TextView) view.findViewById(R.id.published_time); //发布时间
            time.setText("照片发布于" + DataUtils.stampToDate(DataUtils.DATA_TYPE2, mAttachObjs.get(position).getLong("created_at")));
            TextView description = (TextView) view.findViewById(R.id.picture_description); //活动内容
            description.setText(mAttachObjs.get(position).getString("description"));


            //获取用户头像
            CircleImageView userAvatar = (CircleImageView) view.findViewById(user_avatar);
            BitmapLoaderUtil.getInstance().getImage(userAvatar,BitmapLoaderUtil.TYPE_ORIGINAL,mAttachObjs.get(position).getJSONObject("creator_obj").getInt("avatar"));
            userAvatar.setOnClickListener(new PersonOnClickListenerImpl(mActivity,getAttachObj(position).getInt("creator")));

            //获取photo
            ImageView PictureImage = (ImageView) view.findViewById(R.id.image);
            BitmapLoaderUtil.getInstance().getImage(PictureImage,BitmapLoaderUtil.TYPE_MEDIAN,mAttachObjs.get(position).getInt("media_id"));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }
    }
    /**
     * 添加数据列表项
     */
    public void addStatusListItem(int type, JSONObject json) {
        mAttachTypes.add(type);
        mAttachObjs.add(json);
    }
    /**
     * 添加数据列表项开头位置
     */
    public void addHeaderStausesListItem(int type, JSONObject json) {
        mAttachTypes.add(0, type);
        mAttachObjs.add(0, json);
    }

}
