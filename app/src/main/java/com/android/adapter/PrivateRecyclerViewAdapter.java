package com.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.model.PrivateMsg;
import com.android.tool.PopDeleteMenu;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2016/12/11 0011.
 *
 * 私信列表适配器，这个类似于QQ的消息界面条目
 */

public class PrivateRecyclerViewAdapter extends RecyclerView.Adapter<PrivateRecyclerViewAdapter.ViewHolder> {
    private List<PrivateMsg> msgs = new ArrayList();
    private Context mContext;
    static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView userAvatar;
        TextView userName;
        TextView msgContent;
        TextView msgTime;
        TextView noReadNum;

        public ViewHolder (View view) {
            super(view);
            userAvatar = (CircleImageView) view.findViewById(R.id.user_avatar);
            userName = (TextView) view.findViewById(R.id.user_name);
            msgContent = (TextView) view.findViewById(R.id.msg_content);
            msgTime = (TextView) view.findViewById(R.id.msg_time);
            noReadNum = (TextView) view.findViewById(R.id.msg_no_read_num);
        }

    }
    public PrivateRecyclerViewAdapter(Context context, List<PrivateMsg> list) {
        mContext = context;
        msgs = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_listitem, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {    //长按弹出删除选项
                int position = viewHolder.getAdapterPosition();
                Toast.makeText(mContext,"长按  "+position, Toast.LENGTH_SHORT).show();
                new PopDeleteMenu(mContext, v, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext,"按了  "+"删除", Toast.LENGTH_SHORT).show();
                        msgs.remove(viewHolder.getAdapterPosition());
                        notifyItemRemoved(viewHolder.getAdapterPosition());
                        notifyDataSetChanged();
                    }
                }).show();

                return true;
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                Toast.makeText(mContext,"按了  "+position, Toast.LENGTH_SHORT).show();
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
             PrivateMsg msg = msgs.get(position);

/*        // holder.userAvatar.setImageResource();  //图标设置
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
        return msgs.size();
    }


//    private void  showDialog(View view, final int position){
//       // if(null==popupWindow){
//            final PopupWindow popupWindow;
//            View popView = LayoutInflater.from(mContext).inflate(R.layout.layout_long_click_dialog, null);
//            TextView tvDelete=(TextView) popView.findViewById(R.id.tv_delete);
//
//            popupWindow = new PopupWindow(popView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
//           // popupWindow.setAnimationStyle(R.style.popwin_anim_style);
//            popupWindow.setOutsideTouchable(true);
//            popupWindow.setFocusable(true);    //避免外面的View的事件还是可以触发
//            popupWindow.setBackgroundDrawable(new BitmapDrawable());
//        tvDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(mContext,"按了  "+"删除", Toast.LENGTH_SHORT).show();
//                msgs.remove(position);
//                notifyItemRemoved(position);
//                notifyDataSetChanged();
//                popupWindow.dismiss();
//
//            }
//        });
//
//        //在View的中间显示
//        popupWindow.showAsDropDown(view,(view.getWidth()-tvDelete.getWidth())/2,-view.getHeight());
//
//       //  popupWindow.showAsDropDown(view,(view.getWidth()-tvDelete.getWidth())/2,-view.getHeight()-tvDelete.getHeight());
//    }
}
