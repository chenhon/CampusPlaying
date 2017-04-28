package com.android.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.R;
import com.android.GlobalApplication;
import com.android.model.Private;
import com.android.person.PersonOnClickListenerImpl;
import com.android.tool.BitmapLoaderUtil;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2016/12/11 0011.
 *  对话界面的list适配器
 * 这里应该还要有一个获取图片的函数
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private List<Private> msgs = new ArrayList();
    private int AvatarId; //对方的头像id
    private int uid;//对方的id
    private Activity mActivity;
    static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView user_avatar;
        LinearLayout receivedLayout;
        LinearLayout sendLayout;
        TextView receivedMsg;
        TextView sendMsg;
        CircleImageView leftAvatar;
        CircleImageView rightAvatar;
        public ViewHolder (View view) {
            super(view);
            receivedLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            sendLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            receivedMsg = (TextView) view.findViewById(R.id.received_msg);
            sendMsg = (TextView) view.findViewById(R.id.send_msg);
            leftAvatar = (CircleImageView) view.findViewById(R.id.user_avatar_left);
            rightAvatar = (CircleImageView) view.findViewById(R.id.user_avatar_right);
        }

    }
    public MsgAdapter(Activity activity, List<Private> list, int uid, int uAvatar) {
        this.mActivity = activity;
        msgs = list;
        this.AvatarId = uAvatar;
        this.uid = uid;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Private msg = msgs.get(position);
        if(msg.getDirection() == Private.GET_TYPE) { //接收的消息
            BitmapLoaderUtil.getInstance().getImage(holder.leftAvatar, BitmapLoaderUtil.TYPE_ORIGINAL, AvatarId);
            holder.receivedLayout.setVisibility(View.VISIBLE);
            holder.sendLayout.setVisibility(View.GONE);
            holder.receivedMsg.setText(msg.getContent());
            holder.leftAvatar.setOnClickListener(new PersonOnClickListenerImpl(mActivity,uid));
        }else if(msg.getDirection() == Private.SEND_TYPE) {
            holder.rightAvatar.setImageBitmap(GlobalApplication.getMyAvatar());
            holder.rightAvatar.setOnClickListener(new PersonOnClickListenerImpl(mActivity,GlobalApplication.getMySelf().getId()));
            holder.receivedLayout.setVisibility(View.GONE);
            holder.sendLayout.setVisibility(View.VISIBLE);
            holder.sendMsg.setText(msg.getContent());
        }

    }

    @Override
    public int getItemCount() {
        return msgs.size();
    }
}
