package com.android.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.R;
import com.android.guide.GlobalApplication;
import com.android.tool.MyImageRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 *
 * 关注列表适配器
 */

public class FollowListAdapter extends BaseAdapter {
    private List<JSONObject> mfollowJsons;//关注列表详细信息，对应的Json数据
    private Map<Long,Bitmap> mAvatarBitmaps;//头像，需要另外加载，key为userid
    private Activity mActivity;
    private RequestQueue mQueue;

    public FollowListAdapter(Activity activity) {
        this.mActivity = activity;
        this.mfollowJsons = new ArrayList<>();
        this.mAvatarBitmaps = new HashMap<>();
        mQueue = GlobalApplication.get().getRequestQueue();
    }

    public void clearAllItem() {
        mfollowJsons.clear();
        mAvatarBitmaps.clear();
    }
    public Bitmap getAvatar(int position) { //应该是userid
        return mAvatarBitmaps.get(getId(position));
    }
    public JSONObject getJsonObj(int position) {
        return mfollowJsons.get(position);
    }
    public long getId(int position){
        long id = 0;
        try {
            id = mfollowJsons.get(position).getLong("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }
    @Override
    public int getCount() {
        return mfollowJsons.size();
    }

    @Override
    public Object getItem(int position) {
        return mfollowJsons.get(position);
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
            view = mActivity.getLayoutInflater().inflate(R.layout.follow_listitem, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        try {
            //发布人还获取不到
            viewHolder.userDescription.setText(mfollowJsons.get(position).getString("description"));//用户描述信息
            viewHolder.userName.setText(mfollowJsons.get(position).getString("name"));//用户名

            if(mfollowJsons.get(position).getInt("gender") == 0) { //男生
                viewHolder.userGender.setImageBitmap(BitmapFactory.decodeResource(mActivity.getResources(),R.drawable.male_icon));
            } else {
                viewHolder.userGender.setImageBitmap(BitmapFactory.decodeResource(mActivity.getResources(),R.drawable.female_icon));

            }
            //获取用户头像
            if(mAvatarBitmaps.containsKey(getId(position))) {
                viewHolder.userAvatar.setImageBitmap(mAvatarBitmaps.get(position));
            } else {
                MyImageRequest avatarImageRequest = new MyImageRequest(
                        mActivity.getResources().getString(R.string.ROOT) + "media/" + mfollowJsons.get(position).getInt("avatar")
                        , new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mAvatarBitmaps.put(getId(position), response);
                        viewHolder.userAvatar.setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        viewHolder.userAvatar.setImageResource(R.drawable.campus_playing_app_icon);
                    }
                });
                mQueue.add(avatarImageRequest);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getMEDIA:TAG", e.toString());
        }
        return view;
    }

    class ViewHolder {
        CircleImageView userAvatar;
        TextView userName;
        TextView userDescription;
        ImageView userGender;//性别

        public ViewHolder (View view) {
            userAvatar = (CircleImageView) view.findViewById(R.id.user_avatar);
            userName = (TextView) view.findViewById(R.id.user_name);
            userDescription = (TextView) view.findViewById(R.id.user_description);
            userGender = (ImageView) view.findViewById(R.id.iv_gender);
        }

    }
    /**
     * 添加数据列表项
     */
    public void addFollowListItem(JSONObject json) {
        mfollowJsons.add(json);

    }
    /**
     * 添加数据列表项
     */
    public void addFollowListItem(int position, JSONObject json) {
        mfollowJsons.add(position,json);
    }
    /**
     * 添加数据列表项开头位置
     */
    public void addHeaderFollowListItem(JSONObject json) {
        mfollowJsons.add(0, json);
    }
}
