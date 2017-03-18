package com.android.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.R;

import java.util.List;

/**
 * Created by Administrator on 2016/12/28 0028.
 */

public class RecommendHotActivityAdapter extends RecyclerView.Adapter<RecommendHotActivityAdapter.ViewHolder> {
    private List<com.android.model.Activity> mRecommendActivityList;
    private Activity mParentActivity;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView activityPicture;


        public ViewHolder(View view) {
            super(view);
            activityPicture = (ImageView) view.findViewById(R.id.hot_activity_picture);
        }
    }

    public RecommendHotActivityAdapter(Activity parentActivity, List<com.android.model.Activity> recommendActivityList) {
        mParentActivity = parentActivity;//记录依附的活动
        mRecommendActivityList = recommendActivityList;
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        com.android.model.Activity activity = mRecommendActivityList.get(position);
       // holder.activityPicture.setImageResource(activity.getActivityPicture());
        //活动图
        //Glide.with(mParentActivity).load(activity.getImage()).into(holder.activityPicture);
    }

    @Override
    public int getItemCount() {
        return mRecommendActivityList.size();
    }
}


