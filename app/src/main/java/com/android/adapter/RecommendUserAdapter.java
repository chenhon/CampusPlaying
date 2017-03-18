package com.android.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.R;
import com.android.model.User;

import java.util.List;

/**
 * Created by Administrator on 2016/12/28 0028.
 */

public class RecommendUserAdapter extends RecyclerView.Adapter<RecommendUserAdapter.ViewHolder> {
    private List<User> mRecommendUserList;
    private Activity mparentActivity;
    private UserItemClickListener mUserItemClickListener;
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton closeButton;
        Button addAttentionButton;
        ImageView userPortrait;
        TextView userName;

        public ViewHolder(View view) {
            super(view);
            closeButton = (ImageButton) view.findViewById(R.id.close_button);
            addAttentionButton = (Button) view.findViewById(R.id.add_attention);
            userPortrait = (ImageView) view.findViewById(R.id.user_portrait);
            userName = (TextView) view.findViewById(R.id.user_name);

        }
    }

    public RecommendUserAdapter(Activity parentActivity, List<User> recommendUserList, UserItemClickListener userItemClickListener) {
        mparentActivity = parentActivity;
        mRecommendUserList = recommendUserList;
        mUserItemClickListener = userItemClickListener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recommend_user_listitem, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        final int position = holder.getAdapterPosition();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserItemClickListener.itemClick(position);
            }
        });

        holder.closeButton.setOnClickListener(new View.OnClickListener() {//点击关闭按钮
            @Override
            public void onClick(View v) {
                mUserItemClickListener.closeBtnClick(position);
//                Toast.makeText(v.getContext(), "you click closeButton" + user.getName() , Toast.LENGTH_SHORT).show();
//                mRecommendUserList.remove(position);
//                notifyItemRemoved(position);
            }
        });
        holder.addAttentionButton.setOnClickListener(new View.OnClickListener() {  //点击添加按钮
            @Override
            public void onClick(View v) {
                mUserItemClickListener.addAttentionBtnClick(position);
//                User user = mRecommendUserList.get(position);
//                Toast.makeText(v.getContext(), "you click addAttentionButton" + user.getName() , Toast.LENGTH_SHORT).show();
//                mRecommendUserList.remove(position);
//                notifyItemRemoved(position);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = mRecommendUserList.get(position);
        holder.userName.setText(user.getName());
        holder.userPortrait.setImageResource(user.getAvatar());
    }

    @Override
    public int getItemCount() {
        return mRecommendUserList.size();
    }

    /**
     * 处理推荐用户item组件相关的点击事件
     */
    public interface UserItemClickListener {
        /**
         * 关闭按钮点击事件
         */
        void closeBtnClick(int position);

        /**
         * 关注按钮点击事件
         */
        void addAttentionBtnClick(int position);

        /**
         * 点击其他位置事件，执行跳转操作
         */
        void itemClick(int position);
    }
}
