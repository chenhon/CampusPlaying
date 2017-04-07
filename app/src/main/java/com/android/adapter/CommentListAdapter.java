package com.android.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.guide.GlobalApplication;
import com.android.model.Comment;
import com.android.tool.DataUtils;
import com.android.tool.MyImageRequest;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<Integer,Bitmap> mAvatarBitmaps;//头像，需要另外加载
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
        mAvatarBitmaps = new HashMap<>();
        mQueue = GlobalApplication.get().getRequestQueue();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_listitem, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.doRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity,"点了评论图像进行评论", Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity,"跳转到用户详情界面", Toast.LENGTH_SHORT).show();
            }
        });
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
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                Toast.makeText(mActivity,"单击进行评论  "+position, Toast.LENGTH_SHORT).show();
            }
        });

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
        if(mAvatarBitmaps.containsKey(mComments.get(position).getAvatarId())) {
            holder.userAvatar.setImageBitmap(mAvatarBitmaps.get(mComments.get(position).getAvatarId()));
        } else {
            MyImageRequest avatarImageRequest = new MyImageRequest(
                    mActivity.getResources().getString(R.string.ROOT) + "media/" + comment.getAvatarId()
                    , new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    mAvatarBitmaps.put(mComments.get(position).getAvatarId(), response);
                    holder.userAvatar.setImageBitmap(response);
                }
            }, 0, 0, Bitmap.Config.RGB_565
                    , new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    holder.userAvatar.setImageResource(R.drawable.campus_playing_app_icon);
                }
            });
            mQueue.add(avatarImageRequest);
        }
/*        // holder.userAvatar.setImageResource();  //图标设置

        //图片的设置要根据图片的ID在请求一次来获取
       holder.userName.setText(msg.getName());  //名字设置
        holder.msgContent.setText(msg.getRecentContent());  //消息内容设置
        holder.msgTime.setText(String.valueOf(msg.getRecentTime())); //消息时间设置

        if(msg.getNoReadCount() > 0) {     //未读消息数设置
            holder.noReadNum.setText(String.valueOf(msg.getNoReadCount()));
            holder.noReadNum.setVisibility(View.VISIBLE);
        } else {            //没有未读消息则不显示红点
            holder.noReadNum.setVisibility(View.GONE);
        }*/

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
