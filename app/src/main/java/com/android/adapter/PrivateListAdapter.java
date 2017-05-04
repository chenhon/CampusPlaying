package com.android.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.R;
import com.android.model.PrivateMsg;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.DataUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2016/12/11 0011.
 *
 * 私信列表适配器，这个类似于QQ的消息界面条目
 */

public class PrivateListAdapter extends BaseAdapter {
    private List<PrivateMsg> mPrivates;
    private Activity mActivity;

    public PrivateListAdapter(Activity activity) {
        this.mActivity = activity;
        this.mPrivates = new ArrayList<>();
    }

    public int getTargetId(int position) {
        return mPrivates.get(position).getTargetId();
    }
    public String getTargetName(int position) {
        return mPrivates.get(position).getTargetName();
    }
    public int getTargetAvatarId(int position) {
        return mPrivates.get(position).getAvatarId();
    }

    @Override
    public int getCount() {
        return mPrivates.size();
    }

    @Override
    public Object getItem(int position) {
        return mPrivates.get(position);
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
            view = mActivity.getLayoutInflater().inflate(R.layout.msg_listitem, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.userName.setText(mPrivates.get(position).getTargetName());   //对方昵称
        viewHolder.msgContent.setText(mPrivates.get(position).getRecentContent());//最近一条私信内容
        viewHolder.msgTime.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE2,
                mPrivates.get(position).getRecentTime()));  //最近一条私信的时间
        int noReadCnt = mPrivates.get(position).getNoReadCount();  //未读消息条数
        if(0 == noReadCnt) {
            viewHolder.noReadNum.setVisibility(View.GONE);
        } else if(noReadCnt <= 99){
            viewHolder.noReadNum.setVisibility(View.VISIBLE);
            viewHolder.noReadNum.setText(String.valueOf(noReadCnt));//有未读消息
        } else {
            viewHolder.noReadNum.setVisibility(View.VISIBLE);
            viewHolder.noReadNum.setText("99+");//有未读消息
        }
        BitmapLoaderUtil.getInstance().getImage(viewHolder.userAvatar,BitmapLoaderUtil.TYPE_ORIGINAL,mPrivates.get(position).getAvatarId());

        return view;
    }

    class ViewHolder {
        CircleImageView userAvatar;
        TextView userName;
        TextView msgContent;
        TextView msgTime;
        TextView noReadNum;

        public ViewHolder (View view) {
            userAvatar = (CircleImageView) view.findViewById(R.id.user_avatar);
            userName = (TextView) view.findViewById(R.id.user_name);
            msgContent = (TextView) view.findViewById(R.id.msg_content);
            msgTime = (TextView) view.findViewById(R.id.msg_time);
            noReadNum = (TextView) view.findViewById(R.id.msg_no_read_num);
        }

    }

    public void clearPrivateListItem() {
        mPrivates.clear();
    }
    /**
     * 添加数据列表项
     */
    public void addPrivateListItem(PrivateMsg privateMsg) {
        mPrivates.add(privateMsg);
    }
}
