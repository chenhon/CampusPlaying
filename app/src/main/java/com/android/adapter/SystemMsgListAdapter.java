package com.android.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.R;
import com.android.tool.DataUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 系统消息列表适配器
 */

public class SystemMsgListAdapter extends BaseAdapter {
    private List<JSONObject> mMsgJsons;//系统消息详细信息，对应的Json数据
    private Activity mActivity;

    public SystemMsgListAdapter(Activity activity) {
        this.mActivity = activity;
        this.mMsgJsons = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mMsgJsons.size();
    }

    @Override
    public Object getItem(int position) {
        return mMsgJsons.get(position);
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
            view = mActivity.getLayoutInflater().inflate(R.layout.system_msg_listitem, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        try {
            //发布人还获取不到
            viewHolder.mMsgTime.setText(
                    DataUtils.stampToDate(DataUtils.DATA_TYPE2,mMsgJsons.get(position).getLong("created_at")));//用户描述信息
            viewHolder.mMsgContent.setText(mMsgJsons.get(position).getString("content"));//用户名
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getMEDIA:TAG", e.toString());
        }
        return view;
    }

    static class ViewHolder {
        @BindView(R.id.user_avatar)
        CircleImageView mUserAvatar;
        @BindView(R.id.msg_time)
        TextView mMsgTime;
        @BindView(R.id.msg_content)
        TextView mMsgContent;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


    /**
     * 添加数据列表项
     */
    public void addMagListItem(JSONObject json) {
        mMsgJsons.add(json);
    }
    /**
     * 添加数据列表项
     */
    public void addMagListItem(int position, JSONObject json) {
        mMsgJsons.add(position,json);
    }
    /**
     * 添加数据列表项开头位置
     */
    public void addHeaderMagListItem(JSONObject json) {
        mMsgJsons.add(0, json);
    }
}
