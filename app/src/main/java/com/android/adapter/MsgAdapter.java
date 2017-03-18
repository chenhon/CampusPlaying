package com.android.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.R;
import com.android.model.Private;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2016/12/11 0011.
 *
 * 这里应该还要有一个获取图片的函数
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private List<Private> msgs = new ArrayList();
    static class ViewHolder extends RecyclerView.ViewHolder{
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
    public MsgAdapter(List<Private> list) {
        msgs = list;
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
           // holder.leftAvatar.setImageResource(); //这里要进行图标的设置
            holder.receivedLayout.setVisibility(View.VISIBLE);
            holder.sendLayout.setVisibility(View.GONE);
            holder.receivedMsg.setText(msg.getContent());
        }else if(msg.getDirection() == Private.SEND_TYPE) {
            // holder.leftAvatar.setImageResource(); //这里要进行图标的设置
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
