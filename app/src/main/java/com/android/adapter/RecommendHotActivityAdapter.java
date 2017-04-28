package com.android.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.R;
import com.android.GlobalApplication;
import com.android.tool.MyImageRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/28 0028.
 */

public class RecommendHotActivityAdapter extends RecyclerView.Adapter<RecommendHotActivityAdapter.ViewHolder> {
    private List<JSONObject> mHotActivityJsons;//热门活动信息，对应的Json数据
    private Map<Integer,Bitmap> mAvatarBitmaps;//头像，需要另外加载，key为活动id
    private Activity mActivity;
    private RequestQueue mQueue;

    public int getId(int position){
        int id = 0;
        try {
            id = mHotActivityJsons.get(position).getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public String getActivityTitle(int position) {
        String title = "";
        try {
            title = mHotActivityJsons.get(position).getString("title");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return title;
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView activityPicture;


        public ViewHolder(View view) {
            super(view);
            activityPicture = (ImageView) view.findViewById(R.id.hot_activity_picture);
        }
    }

    public RecommendHotActivityAdapter(Activity activity) {
        this.mActivity = activity;
        this.mHotActivityJsons = new ArrayList<>();
        this.mAvatarBitmaps = new HashMap<>();
        mQueue = GlobalApplication.get().getRequestQueue();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hot_activity_listitem, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.activityPicture.setOnClickListener(new View.OnClickListener() {//点击关闭按钮
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
             //   com.android.model.Activity activity = mRecommendActivityList.get(position);
              //  Toast.makeText(v.getContext(), "you click closeButton" + activity.getTitle() , Toast.LENGTH_SHORT).show();
//                mRecommendActivityList.remove(position);
//                notifyItemRemoved(position);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //获取热门活动的图片
        if(mAvatarBitmaps.containsKey(getId(position))) {
            holder.activityPicture.setImageBitmap(mAvatarBitmaps.get(getId(position)));
        } else {
            try{
                MyImageRequest avatarImageRequest = new MyImageRequest(
                        mActivity.getResources().getString(R.string.ROOT) + "media/" + mHotActivityJsons.get(position).getInt("image")
                        , new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mAvatarBitmaps.put(getId(position), response);
                        holder.activityPicture.setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        holder.activityPicture.setImageResource(R.drawable.campus_playing_app_icon);
                    }
                });
                mQueue.add(avatarImageRequest);
            }catch (Exception e) {
                    e.printStackTrace();
            }
    }
        //活动图
        //Glide.with(mParentActivity).load(activity.getImage()).into(holder.activityPicture);
    }

    @Override
    public int getItemCount() {
        return mHotActivityJsons.size();
    }
}


