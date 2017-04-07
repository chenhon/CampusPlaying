package com.android.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.android.R.id.activity_content;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

public class ActivityListAdapter extends BaseAdapter {
    private Map<Integer, Bitmap> mAvatarBitmaps;//头像，需要另外加载  key--aid
    private Map<Integer, Bitmap> mActivityImages;//活动图像，需要另外加载
    private List<JSONObject> mActivityJsons;//关注列表详细信息，对应的Json数据
    private Activity mActivity;
    private RequestQueue mQueue;

    public ActivityListAdapter(Activity activity) {
        this.mActivity = activity;
        this.mActivityJsons = new ArrayList<>();
        this.mAvatarBitmaps = new HashMap<>();
        this.mActivityImages = new HashMap<>();
        this.mQueue = GlobalApplication.get().getRequestQueue();
    }

    public void clearAllItem() {
        mAvatarBitmaps.clear();
        mActivityImages.clear();
        mActivityJsons.clear();
    }
    public Bitmap getAvatar(int position) {
        return mAvatarBitmaps.get(getIdentifier(position));
    }
    public Bitmap getActivityImage(int position) {
        return mActivityImages.get(getIdentifier(position));
    }
    public JSONObject getAttachObj(int position) {
        return mActivityJsons.get(position);
    }
    private int getIdentifier(int position) {
        int aid = 0;
        try{
            aid = mActivityJsons.get(position).getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aid;
    }
    @Override
    public int getCount() {
        return mActivityJsons.size();
    }

    @Override
    public Object getItem(int postion) {
        return mActivityJsons.get(postion);
    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        final ViewHolder viewHolder;
        if (convertView == null) {
            view = mActivity.getLayoutInflater().inflate(R.layout.activity_listitem, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        setActivityView(position, viewHolder);


        return view;
    }

    private void setActivityView(final int position, final ViewHolder viewHolder) {
        try {

            viewHolder.mUserName.setText(mActivityJsons.get(position).getJSONObject("creator_obj").getString("name"));//发布人名称
            viewHolder.mPublishedTime.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE2, mActivityJsons.get(position).getLong("created_at")));//发布时间
            viewHolder.mActivityTitle.setText(mActivityJsons.get(position).getString("title"));//活动标题
            viewHolder.mActivityContent.setText(mActivityJsons.get(position).getString("content"));//活动内容
            viewHolder.mWisherCount.setText(mActivityJsons.get(position).getString("wisher_count"));//活动热度
            viewHolder.mParticipantCount.setText(mActivityJsons.get(position).getString("participant_count"));//已参与人数
            switch (mActivityJsons.get(position).getInt("state")) { //活动进行状态
                case 0:
                    viewHolder.mActivityStatusText.setText("发起中");
                    viewHolder.mActivityStatusImage.setImageResource(R.drawable.detail_sign_up);
                    break;
                case 1:
                    viewHolder.mActivityStatusText.setText("进行中");
                    viewHolder.mActivityStatusImage.setImageResource(R.drawable.detail_processing);
                    break;
                case 2:
                    viewHolder.mActivityStatusText.setText("已结束");
                    viewHolder.mActivityStatusImage.setImageResource(R.drawable.detail_end);
                    break;

            }

            //图像请求
            //获取用户头像
            if (mAvatarBitmaps.get(getIdentifier(position)) != null) {
                viewHolder.mUserAvatar.setImageBitmap(mAvatarBitmaps.get(getIdentifier(position)));
            } else {
                MyImageRequest avatarImageRequest = new MyImageRequest(
                        mActivity.getResources().getString(R.string.ROOT) + "media/" + mActivityJsons.get(position).getJSONObject("creator_obj").getInt("avatar")
                        , new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mAvatarBitmaps.put(getIdentifier(position), response);
                        viewHolder.mUserAvatar.setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        viewHolder.mUserAvatar.setImageResource(R.drawable.campus_playing_app_icon);
                    }
                });
                mQueue.add(avatarImageRequest);
            }

            //获取活动图像
            if (mActivityImages.get(getIdentifier(position)) != null) {
                viewHolder.mActivityImage.setImageBitmap(mActivityImages.get(getIdentifier(position)));
            } else {
                MyImageRequest activityImageRequest = new MyImageRequest(
                        mActivity.getResources().getString(R.string.ROOT) + "media/" + mActivityJsons.get(position).getInt("image")
                        , new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mActivityImages.put(getIdentifier(position), response);
                        viewHolder.mActivityImage.setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        viewHolder.mActivityImage.setImageResource(R.drawable.campus_playing_app_icon);
                    }
                });
                mQueue.add(activityImageRequest);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }
    }

    /**
     * 添加数据列表项
     *
     * @param json
     */
    public void addActivityListItem(JSONObject json) {
        mActivityJsons.add(json);
    }

    /**
     * 添加数据列表项
     *
     * @param json
     */
    public void addHeaderActivityListItem(JSONObject json) {
        mActivityJsons.add(0, json);
    }


    static class ViewHolder {
        @BindView(R.id.user_avatar)
        CircleImageView mUserAvatar;
        @BindView(R.id.user_name)
        TextView mUserName;
        @BindView(R.id.published_time)
        TextView mPublishedTime;
        @BindView(R.id.activity_title)
        TextView mActivityTitle;
        @BindView(activity_content)
        TextView mActivityContent;
        @BindView(R.id.activity_image)
        ImageView mActivityImage;
        @BindView(R.id.participant_count)
        TextView mParticipantCount;
        @BindView(R.id.wisher_count)
        TextView mWisherCount;
        @BindView(R.id.activity_status_image)
        ImageView mActivityStatusImage;
        @BindView(R.id.activity_status_text)
        TextView mActivityStatusText;
        @BindView(R.id.activity_share)
        ImageView mActivityShare;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
