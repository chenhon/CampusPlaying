package com.android.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.R;
import com.android.guide.GlobalApplication;
import com.android.tool.DataUtils;
import com.android.tool.MyImageRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2016/12/11 0011.
 *
 * 私信列表适配器，这个类似于QQ的消息界面条目
 */

public class PrivateListAdapter extends BaseAdapter {
    private List<JSONObject> mMsgJsons;//消息头详细信息，对应的Json数据
    private Map<Integer,Bitmap> mAvatarBitmaps;//头像，需要另外加载，key为userid
    private Activity mActivity;
    private RequestQueue mQueue;

    public PrivateListAdapter(Activity activity) {
        this.mActivity = activity;
        this.mMsgJsons = new ArrayList<>();
        this.mAvatarBitmaps = new HashMap<>();
        mQueue = GlobalApplication.get().getRequestQueue();
    }

    public Bitmap getAvatar(int position) { //应该是userid
        return mAvatarBitmaps.get(position);
    }
    public JSONObject getJsonObj(int position) {
        return mMsgJsons.get(position);
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
            view = mActivity.getLayoutInflater().inflate(R.layout.msg_listitem, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        try {
            //发布人还获取不到
            viewHolder.msgContent.setText(mMsgJsons.get(position).getJSONObject("recent").getString("content"));//私信内容
            viewHolder.msgTime.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE2,
                    mMsgJsons.get(position).getJSONObject("recent").getLong("created_at"))); //私信时间
            viewHolder.noReadNum.setText(mMsgJsons.get(position).getString("private_count"));//未读数

            if(mAvatarBitmaps.get(position)!=null) {
                viewHolder.userAvatar.setImageBitmap(mAvatarBitmaps.get(position));
            } else {
                MyImageRequest avatarImageRequest = new MyImageRequest(
                        mActivity.getResources().getString(R.string.ROOT) + "media/" + mMsgJsons.get(position).getInt("avatar")
                        , new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mAvatarBitmaps.put(position, response);
                        viewHolder.userAvatar.setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        viewHolder.userAvatar.setImageResource(R.drawable.campus_playing_app_icon);
                    }
                });
                mQueue.add(avatarImageRequest);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }
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
    /**
     * 添加数据列表项
     */
    public void addPrivateListItem(JSONObject json) {
        mMsgJsons.add(json);

    }
    /**
     * 添加数据列表项
     */
    public void addPrivateListItem(int position, JSONObject json) {
        mMsgJsons.add(position,json);
    }
    /**
     * 添加数据列表项开头位置
     */
    public void addHeaderPrivateListItem(JSONObject json) {
        mMsgJsons.add(0, json);
    }
}
