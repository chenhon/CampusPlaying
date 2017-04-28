package com.android.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.GlobalApplication;
import com.android.R;
import com.android.model.Comment;
import com.android.model.Statuses;
import com.android.person.PersonOnClickListenerImpl;
import com.android.status.picture.DetailActivity;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.DataUtils;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.ReplyDialog;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 评论我的 列表
 */

public class CommentToMeListAdapter extends BaseAdapter {
    private List<Comment> mComments;
    private Activity mActivity;

    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private ProgressHUD mProgressHUD;

    public CommentToMeListAdapter(Activity activity) {
        mActivity = activity;
        mComments = new ArrayList();
        mQueue = GlobalApplication.get().getRequestQueue();
        networkStatus = new NetworkConnectStatus(mActivity);
    }

    @Override
    public int getCount() {
        return mComments.size();
    }

    @Override
    public Object getItem(int position) {
        return mComments.get(position);
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
            view = mActivity.getLayoutInflater().inflate(R.layout.my_coment_listitem, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.mTvReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {    //进行回复
                final ReplyDialog replyDialog = new ReplyDialog(mActivity);
                replyDialog.setHintText("回复评论...")
                        .setOnBtnCommitClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {   //回复键的回调函数
                                replyDialog.dismiss();
                                Log.d("XXX", replyDialog.getContent());
                                addMyComment(position, replyDialog.getContent());
                            }
                        })
                        .show();
            }
        });
        //发布者头像加载
        BitmapLoaderUtil.getInstance().getImage(viewHolder.mUserAvatar, BitmapLoaderUtil.TYPE_ORIGINAL, mComments.get(position).getAvatarId());
        //头像点击进入个人主页
        viewHolder.mUserAvatar.setOnClickListener(new PersonOnClickListenerImpl(mActivity, mComments.get(position).getCreatorId()));
        viewHolder.mUserName.setText(mComments.get(position).getName());//昵称
        viewHolder.mCommentTime.setText(              //评论创建时间
                DataUtils.stampToDate(DataUtils.DATA_TYPE1, mComments.get(position).getCreatedTime()));

        viewHolder.mCommentContent.setText(mComments.get(position).getContent());//评论内容

        SpannableStringBuilder spannable = new SpannableStringBuilder(
                mComments.get(position).getAttachCreatorName() + ": " + mComments.get(position).getAttachContent());
        viewHolder.mAttachedContent.setMovementMethod(LinkMovementMethod.getInstance());
        spannable.setSpan(new TextClick(){                         //点击名称跳转到用户主页
                              @Override
                              public void onClick(View widget) {
                                  super.onClick(widget);
                                  new PersonOnClickListenerImpl(mActivity,mComments.get(position).getCreatorId()).onClick(widget);
                              }
                          }
                ,0,mComments.get(position).getAttachCreatorName().length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewHolder.mAttachedContent.setText(spannable);

        switch (mComments.get(position).getAttachType()) {   //依附的动态或者通知
            case Comment.ACTIVITY_TYPE:
                viewHolder.mRlPicture.setVisibility(View.VISIBLE);
                viewHolder.mAttachedType.setText("活动");
                BitmapLoaderUtil.getInstance().getImage(viewHolder.mPicture, BitmapLoaderUtil.TYPE_MEDIAN, mComments.get(position).getAttachImage());
                break;
            case Comment.NOTIFICATION_TYPE:
                viewHolder.mRlPicture.setVisibility(View.GONE);
                // viewHolder.mAttachedType.setText("通知");
                break;
            case Comment.PHOTO_TYPE:
                viewHolder.mRlPicture.setVisibility(View.VISIBLE);
                BitmapLoaderUtil.getInstance().getImage(viewHolder.mPicture, BitmapLoaderUtil.TYPE_MEDIAN, mComments.get(position).getAttachImage());
                viewHolder.mAttachedType.setText("照片");
                break;
            default:
                break;
        }
        viewHolder.mLlAttached.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                switch (mComments.get(position).getAttachType()) {
                    case Statuses.ACTIVITY_TYPE:   //进入活动详情页
                        intent = new Intent(mActivity, com.android.activity.DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        break;
                    case Statuses.NOTIFICATION_TYPE: //进入通知详情页
                        intent = new Intent(mActivity, com.android.remind.notification.DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        break;
                    case Statuses.PHOTO_TYPE:  //进入照片详情页
                        intent = new Intent(mActivity, DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        break;
                    default: break;
                }
                Bundle bundle = new Bundle();
                bundle.putInt("aid", mComments.get(position).getAttachId());       //动态id
                bundle.putInt("creatorId", mComments.get(position).getCreatorId()); //发布者id
                intent.putExtras(bundle);  //传入详细信息
                mActivity.startActivity(intent);      //调转到依附界面
            }
        });


        return view;
    }

    private class TextClick extends ClickableSpan {
        @Override
        public void onClick(View widget) {
            //在此处理点击事件
        }
        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(0xFF23527c);
        }
    }
    /**
     * 回复内容
     *
     * @param content
     */
    //这里的回复对象有点问题
    public void addMyComment(int position, String content) {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(mActivity, "发布中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });
            String rootString = mActivity.getResources().getString(R.string.ROOT) + "msg/comment";
            VolleyRequestParams bodyParams = new VolleyRequestParams()
                    .with("attach_type", String.valueOf(mComments.get(position).getAttachType()))
                    .with("attach_id", String.valueOf(mComments.get(position).getAttachId()))
                    .with("parent", String.valueOf(mComments.get(position).getId()))
                    .with("content", String.valueOf(content));

            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mProgressHUD.dismiss();
                            Toast.makeText(mActivity, "回复成功".toString(), Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Log.e("PublishActivity:TAG", error.getMessage(), error);
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                    Toast.makeText(mActivity, "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            mQueue = GlobalApplication.get().getRequestQueue();
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(400 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(mStringRequest);
        } else {
            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void addCommentListItem(Comment comment) {
        mComments.add(comment);
    }

    public void clearListItem() {
        mComments.clear();
    }

    static class ViewHolder {
        @BindView(R.id.user_avatar)
        CircleImageView mUserAvatar;
        @BindView(R.id.user_name)
        TextView mUserName;
        @BindView(R.id.comment_time)
        TextView mCommentTime;
        @BindView(R.id.comment_content)
        TextView mCommentContent;
        @BindView(R.id.tv_replay)
        TextView mTvReplay;
        @BindView(R.id.picture)
        ImageView mPicture;
        @BindView(R.id.attached_type)
        TextView mAttachedType;
        @BindView(R.id.rl_picture)
        RelativeLayout mRlPicture;
        @BindView(R.id.attached_content)
        TextView mAttachedContent;
        @BindView(R.id.ll_attached)
        LinearLayout mLlAttached;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
