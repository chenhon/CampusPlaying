package com.android.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.GlobalApplication;
import com.android.model.User;
import com.android.person.PersonOnClickListenerImpl;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2016/12/28 0028.
 */

public class RecommendUserAdapter extends RecyclerView.Adapter<RecommendUserAdapter.ViewHolder> {
    private List<User> mUsers;//推荐用户信息
    private Activity mActivity;
    private ProgressHUD mProgressHUD;
    private NetworkConnectStatus networkStatus;//网络连接状态
    private UserItemClickListener mUserItemClickListener;

    public int getId(int position) {  //获取用户的id
        return mUsers.get(position).getId();
    }

    public RecommendUserAdapter(Activity parentActivity) {
        this.mActivity = parentActivity;
        this.mUsers = new ArrayList<>();
        networkStatus = new NetworkConnectStatus(this.mActivity);
    }
    public RecommendUserAdapter(Activity parentActivity, UserItemClickListener userItemClickListener) {
        this.mActivity = parentActivity;
        this.mUserItemClickListener = userItemClickListener;
        this.mUsers = new ArrayList<>();
        networkStatus = new NetworkConnectStatus(this.mActivity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recommend_user_listitem, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.mUserPortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//跳转至用户主页
                new PersonOnClickListenerImpl(mActivity, mUsers.get(holder.getAdapterPosition()).getId()).onClick(v);
            }
        });
        holder.mCloseButton.setOnClickListener(new View.OnClickListener() {//点击关闭按钮
            @Override
            public void onClick(View v) { //删掉该项
               // mUserItemClickListener.closeBtnClick(holder.getAdapterPosition());
                mUsers.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            }
        });
        holder.mAddAttention.setOnClickListener(new View.OnClickListener() {  //点击添加按钮
            @Override
            public void onClick(View v) { //添加关注
                //  mUserItemClickListener.addAttentionBtnClick(holder.getAdapterPosition());
                doAttention(holder.getAdapterPosition());
            }
        });
        return holder;
    }
    private void doAttention(final int position) {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(mActivity, "提交中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });
            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept","application/json"); // 数据格式设置为json
            MyStringRequest mStringRequest = new MyStringRequest(Request.Method.POST, mActivity.getResources().getString(R.string.ROOT) + "user/~me/follower/" + mUsers.get(position).getId(), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Log.d("getUser:TAG", response);
                            Toast.makeText(mActivity, "关注成功".toString(), Toast.LENGTH_SHORT).show();
                            mProgressHUD.dismiss();
                            mUsers.remove(position);
                            notifyItemRemoved(position);
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mProgressHUD.dismiss();
                        }
                    });

            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            GlobalApplication.get().getRequestQueue().add(mStringRequest);
        }else {
            mProgressHUD.dismiss();
            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mUserName.setText(mUsers.get(position).getName()); //用户名
        if (mUsers.get(position).getGender() == 0) { //男生
            holder.mIvGender.setImageBitmap(BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.male_icon));
        } else {
            holder.mIvGender.setImageBitmap(BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.female_icon));
        }
        //用户头像
        BitmapLoaderUtil.getInstance().getImage(holder.mUserPortrait, BitmapLoaderUtil.TYPE_ORIGINAL, mUsers.get(position).getAvatar());
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
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
    public void addUserListItem(User user) {
        mUsers.add(user);

    }

    public void removeAllItem() {
        mUsers.clear();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.close_button)
        ImageButton mCloseButton;
        @BindView(R.id.user_portrait)
        CircleImageView mUserPortrait;
        @BindView(R.id.iv_gender)
        ImageView mIvGender;
        @BindView(R.id.user_name)
        TextView mUserName;
        @BindView(R.id.add_attention)
        Button mAddAttention;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
