package com.android.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.R;
import com.android.GlobalApplication;
import com.android.model.Comment;
import com.android.person.PersonOnClickListenerImpl;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.DataUtils;
import com.android.tool.MyStringRequest;
import com.android.tool.PopCommentSelectMenu;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.android.R.id.discuss_content;
import static com.android.R.id.discuss_time;
import static com.android.R.id.do_recommend;

/**
 * Created by Administrator on 2016/12/11 0011.
 *
 * 评论列表适配器
 * 评论项做成平铺的形式
 */

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {
    private List<Comment> mComments = new ArrayList();
    private Activity mActivity;
    private RequestQueue mQueue;
    private MyItemHandle mMyItemHandle; //对应长按操作
    private int mAttachCreatorId;//发布活动/通知/照片者的id

    static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView userAvatar;
        TextView userName;
        TextView discussContent;
        TextView discussTime;
        ImageView doRecommend;

        public ViewHolder (View view) {
            super(view);
            userAvatar = (CircleImageView) view.findViewById(R.id.user_avatar);
            userName = (TextView) view.findViewById(R.id.user_name);
            discussTime = (TextView) view.findViewById(discuss_time);
            discussContent = (TextView) view.findViewById(discuss_content);
            doRecommend = (ImageView) view.findViewById(do_recommend);
        }

    }
    public CommentListAdapter(Activity activity, int attachCreatorId, List<Comment> list, MyItemHandle itemHandle) {
        this.mMyItemHandle = itemHandle;
        mAttachCreatorId = attachCreatorId;
        mActivity = activity;
        mComments = list;
        mQueue = GlobalApplication.get().getRequestQueue();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_listitem, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.doRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMyItemHandle.handleReply(viewHolder.getAdapterPosition());
                //Toast.makeText(mActivity,"点了评论图像进行评论", Toast.LENGTH_SHORT).show();
            }
        });

        viewHolder.userAvatar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {    //注意这里的处理
                        new PersonOnClickListenerImpl(mActivity, mComments.get(viewHolder.getAdapterPosition()).getCreatorId()).onClick(v);
                    }
                }
        );

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {    //长按弹出选项
                final int position = viewHolder.getAdapterPosition();
                Boolean canDelete = (mComments.get(position).getCreatorId() == GlobalApplication.getMySelf().getId())//评论的发布者
                                     ||(mAttachCreatorId == GlobalApplication.getMySelf().getId());//发布活动/通知/照片者
                new PopCommentSelectMenu(mActivity, canDelete, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.pop_reply:
                                mMyItemHandle.handleReply(position);
                                break;
                            case R.id.pop_delete:
                                mMyItemHandle.handleDelete(position);
                                break;
                            case R.id.pop_report:
                                mMyItemHandle.handleReport(position);
                                break;
                            default: break;

                        }

                    }
                }
                ).show();

                return true;
            }
        });
/*        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                Toast.makeText(mActivity,"单击进行评论  "+position, Toast.LENGTH_SHORT).show();
            }
        });*/

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Comment comment = mComments.get(position);
        holder.userName.setText(comment.getName());//发布人名称
        holder.discussTime.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE2,comment.getCreatedTime()));

        //回复内容
        if(comment.getParentId() == 0) {  //没有付评论
            holder.discussContent.setText(comment.getContent());
        } else { //有父评论
            VolleyRequestParams headerParams = new VolleyRequestParams()
                    .with("Accept","application/json"); // 数据格式设置为json
            MyStringRequest mStringRequest = new MyStringRequest(Request.Method.GET, mActivity.getResources().getString(R.string.ROOT) + "msg/comment/"+comment.getParentId(), headerParams, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Log.d("getTIMELINE:TAG", response);
                            try{
                                JSONObject jsonObject= new JSONObject(response);
                                holder.discussContent.setText("回复" + jsonObject.getJSONObject("creator_obj").getString("name")+"的评论:" + comment.getContent());
                            } catch (Exception e) {

                            }
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("getTIMELINE:TAG", "出错");
                            Log.d("getTIMELINE:TAG", error.getMessage(),error);
                        }
                    });
            mQueue = GlobalApplication.get().getRequestQueue();
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
            mQueue.add(mStringRequest);
        }
        //图像请求
        //获取用户头像
        BitmapLoaderUtil.getInstance().getImage(holder.userAvatar, BitmapLoaderUtil.TYPE_ORIGINAL, mComments.get(position).getAvatarId());//发布者头像
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public interface MyItemHandle{
        /**
         * 处理回复
         */
        void handleReply(int position);

        /**
         * 处理删除
         */
        void handleDelete(int position);

        /**
         * 处理举报
         */
        void handleReport(int position);
    }

}
