package com.android.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.GlobalApplication;
import com.android.R;
import com.android.person.PersonOnClickListenerImpl;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.DataUtils;
import com.android.volley.RequestQueue;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2017/3/8 0008.
 * 活动相册适配器
 */

public class PictureListAdapter extends BaseAdapter {
    private List<JSONObject> mObjs;//动态详细信息，对应的Json数据

    private Activity mActivity;
    private RequestQueue mQueue;


    public PictureListAdapter(Activity activity) {
        this.mActivity = activity;
        this.mObjs = new ArrayList<>();
        mQueue = GlobalApplication.get().getRequestQueue();
    }

    public int getPid(int position){
        int pid = 0;
        try {
            pid = mObjs.get(position).getInt("id");
        } catch (Exception e){

        }
        return pid;
    }
    public int getCreatorId(int position){
        int uid = 0;
        try {
            uid = mObjs.get(position).getInt("creator");
        } catch (Exception e){

        }
        return uid;
    }

    public JSONObject getObj(int position) {
        return mObjs.get(position);
    }

    @Override
    public int getCount() {
        return mObjs.size();
    }

    @Override
    public Object getItem(int position) {
        return mObjs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view;
        final ViewHolder viewHolder;
        if (convertView == null) {
            view = mActivity.getLayoutInflater().inflate(R.layout.picture_listitem, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        try {
            viewHolder.mUserName.setText(mObjs.get(position).getJSONObject("creator_obj").getString("name"));//发布人名称
            viewHolder.mPublishedTime.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE2, mObjs.get(position).getLong("created_at")));//发布时间
            viewHolder.mPictureDescription.setText(mObjs.get(position).getString("description"));//照片描述

            viewHolder.mUserAvatar.setOnClickListener(new PersonOnClickListenerImpl(mActivity, mObjs.get(position).getInt("creator")));
            //获取用户头像
            BitmapLoaderUtil.getInstance().getImage(viewHolder.mUserAvatar,BitmapLoaderUtil.TYPE_ORIGINAL,mObjs.get(position).getJSONObject("creator_obj").getInt("avatar"));

            BitmapLoaderUtil.getInstance().getImage(viewHolder.mImage,BitmapLoaderUtil.TYPE_MEDIAN,mObjs.get(position).getInt("media_id"));

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }


        return view;
    }


    /**
     * 添加数据列表项
     */
    public void addPictureListItem(JSONObject json) {
        mObjs.add(json);
    }

    /**
     * 添加数据列表项
     */
    public void addPictureListItem(int position, JSONObject json) {
        mObjs.add(position, json);
    }

    /**
     * 添加数据列表项开头位置
     */
    public void addHeaderPictureListItem(JSONObject json) {
        mObjs.add(0, json);
    }


    static class ViewHolder {
        @BindView(R.id.user_avatar)
        CircleImageView mUserAvatar;
        @BindView(R.id.user_name)
        TextView mUserName;
        @BindView(R.id.published_time)
        TextView mPublishedTime;
        @BindView(R.id.picture_description)
        TextView mPictureDescription;
        @BindView(R.id.image)
        ImageView mImage;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
