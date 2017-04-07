package com.android.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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

/**
 * Created by Administrator on 2016/12/28 0028.
 */

public class RecommendUserAdapter extends RecyclerView.Adapter<RecommendUserAdapter.ViewHolder> {
    private List<JSONObject> mUserJsons;//推荐用户信息，对应的Json数据
    private Map<Integer,Bitmap> mAvatarBitmaps;//头像，需要另外加载，key为userid
    private Activity mActivity;
    private RequestQueue mQueue;
    private UserItemClickListener mUserItemClickListener;
    public int getId(int position){
        int id = 0;
        try {
            id = mUserJsons.get(position).getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }
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

    public RecommendUserAdapter(Activity parentActivity, UserItemClickListener userItemClickListener) {
        this.mActivity = parentActivity;
        this.mUserItemClickListener = userItemClickListener;
        this.mUserJsons = new ArrayList<>();
        this.mAvatarBitmaps = new HashMap<>();
        mQueue = GlobalApplication.get().getRequestQueue();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recommend_user_listitem, parent, false);
        final ViewHolder holder = new ViewHolder(view);
       // final int position = holder.getAdapterPosition(); //不能这么写？？
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserItemClickListener.itemClick(holder.getAdapterPosition());
            }
        });

        holder.closeButton.setOnClickListener(new View.OnClickListener() {//点击关闭按钮
            @Override
            public void onClick(View v) {
                mUserItemClickListener.closeBtnClick(holder.getAdapterPosition());
//                Toast.makeText(v.getContext(), "you click closeButton" + user.getName() , Toast.LENGTH_SHORT).show();
//                mRecommendUserList.remove(position);
//                notifyItemRemoved(position);
            }
        });
        holder.addAttentionButton.setOnClickListener(new View.OnClickListener() {  //点击添加按钮
            @Override
            public void onClick(View v) {
                mUserItemClickListener.addAttentionBtnClick(holder.getAdapterPosition());
//                User user = mRecommendUserList.get(position);
//                Toast.makeText(v.getContext(), "you click addAttentionButton" + user.getName() , Toast.LENGTH_SHORT).show();
//                mRecommendUserList.remove(position);
//                notifyItemRemoved(position);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        try {
            holder.userName.setText(mUserJsons.get(position).getString("name"));

            //获取用户头像
            if(mAvatarBitmaps.containsKey(getId(position))) {
                holder.userPortrait.setImageBitmap(mAvatarBitmaps.get(getId(position)));
            } else {
                MyImageRequest avatarImageRequest = new MyImageRequest(
                        mActivity.getResources().getString(R.string.ROOT) + "media/" + mUserJsons.get(position).getInt("avatar")
                        , new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mAvatarBitmaps.put(getId(position), response);
                        holder.userPortrait.setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        holder.userPortrait.setImageResource(R.drawable.campus_playing_app_icon);
                    }
                });
                mQueue.add(avatarImageRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mUserJsons.size();
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

    /**
     * 添加数据列表项
     */
    public void addUserListItem(JSONObject json) {
        mUserJsons.add(json);

    }
    /**
     * 添加数据列表项
     */
    public void addUserListItem(int position, JSONObject json) {
        mUserJsons.add(position,json);
    }
    /**
     * 添加数据列表项开头位置
     */
    public void addUserPrivateListItem(JSONObject json) {
        mUserJsons.add(0, json);
    }
}
