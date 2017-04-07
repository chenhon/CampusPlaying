package com.android.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.guide.GlobalApplication;
import com.android.model.Statuses;
import com.android.tool.DataUtils;
import com.android.tool.MyImageRequest;
import com.android.tool.PictureView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.android.R.id.activity_image;
import static com.android.R.id.user_avatar;

/**
 * Created by Administrator on 2017/3/8 0008.
 * 动态适配器
 * 图像和用户数据需要再次加载
 * 由于attach_type不会超过10可以以 attach_type + attach_obj.id 唯一标识动态
 */

public class StatusesListAdapter extends BaseAdapter {
    private List<Integer> mAttachTypes; //动态类型
    private List<JSONObject> mAttachObjs;//动态详细信息，对应的Json数据
    private Map<Integer,Bitmap> mAvatarBitmaps;//头像，需要另外加载
    private Map<Integer,Bitmap> mActivityImages;//活动图像，需要另外加载
    private Map<Integer,List<Bitmap>> mPictures;//图片
    private Activity mActivity;
    private RequestQueue mQueue;

    public StatusesListAdapter(Activity activity) {
        this.mActivity = activity;
        this.mAttachTypes = new ArrayList<>();
        this.mAttachObjs = new ArrayList<>();
        this.mAvatarBitmaps = new HashMap<>();
        this.mActivityImages = new HashMap<>();
        this.mPictures = new HashMap<>();
        mQueue = GlobalApplication.get().getRequestQueue();
    }

    private int getIdentifier(int position){  //获取动态的唯一标识符
        try {
            return mAttachTypes.get(position) + 10*mAttachObjs.get(position).getInt("id");
        }catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }
        return 0;
    }
    public Bitmap getAvatar(int position) {
        return mAvatarBitmaps.get(getIdentifier(position));
    }
    public Bitmap getActivityImage(int position) {
        return mActivityImages.get(getIdentifier(position));
    }
    public int getAttachType(int position) {
        return mAttachTypes.get(position);
    }
    public JSONObject getAttachObj(int position) {
        return mAttachObjs.get(position);
    }
    @Override
    public int getCount() {
        return mAttachObjs.size();
    }

    @Override
    public Object getItem(int position) {
        return mAttachObjs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        switch(mAttachTypes.get(position)) {
            case Statuses.ACTIVITY_TYPE:
                view = mActivity.getLayoutInflater().inflate(R.layout.activity_listitem, null);
                setActivityView(position, view);
                break;
            case Statuses.NOTIFICATION_TYPE:
                view = mActivity.getLayoutInflater().inflate(R.layout.notification_listitem, null);
                setNotificationView(position, view);
                break;
            case Statuses.PHOTO_TYPE:
                view = mActivity.getLayoutInflater().inflate(R.layout.picture_listitem, null);
                setPictureView(position, view);
                break;
        }

        return view;
    }

    private void setActivityView(final int position,View view) {
        try{
            TextView userName = (TextView) view.findViewById(R.id.user_name);//发布人名称
            userName.setText(mAttachObjs.get(position).getJSONObject("creator_obj").getString("name"));
            TextView time = (TextView) view.findViewById(R.id.published_time); //发布时间
            time.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE2, mAttachObjs.get(position).getLong("created_at")));
            TextView title = (TextView) view.findViewById(R.id.activity_title); //活动标题
            title.setText(mAttachObjs.get(position).getString("title"));
            TextView content = (TextView) view.findViewById(R.id.activity_content); //活动内容
            content.setText(mAttachObjs.get(position).getString("content"));
            TextView wisherCount = (TextView) view.findViewById(R.id.wisher_count); //活动热度
            wisherCount.setText(mAttachObjs.get(position).getString("wisher_count"));
            TextView participantCount = (TextView) view.findViewById(R.id.participant_count); //活动内容
            participantCount.setText(mAttachObjs.get(position).getString("participant_count"));
            //活动进行状态
            TextView activityStatusText = (TextView)view.findViewById(R.id.activity_status_text);//活动状态
            ImageView activityStatusImage = (ImageView)view.findViewById(R.id.activity_status_image);//活动状态
            switch(mAttachObjs.get(position).getInt("state")) {
                case 0:
                    activityStatusText.setText("发起中");
                    activityStatusImage.setImageResource(R.drawable.detail_sign_up);
                    break;
                case 1:
                    activityStatusText.setText("进行中");
                    activityStatusImage.setImageResource(R.drawable.detail_processing);
                    break;
                case 2:
                    activityStatusText.setText("已结束");
                    activityStatusImage.setImageResource(R.drawable.detail_end);
                    break;

            }

            ImageView activityShare = (ImageView)view.findViewById(R.id.activity_share);//活动分享
            activityShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mActivity, "点击分享! " + position, Toast.LENGTH_SHORT).show();
                }
            });

            //图像请求
            //获取用户头像
            final CircleImageView userAvatar = (CircleImageView) view.findViewById(user_avatar);
            if(mAvatarBitmaps.get(getIdentifier(position))!=null) {
                userAvatar.setImageBitmap(mAvatarBitmaps.get(getIdentifier(position)));
            } else {
                MyImageRequest avatarImageRequest = new MyImageRequest(
                        mActivity.getResources().getString(R.string.ROOT) + "media/" + mAttachObjs.get(position).getJSONObject("creator_obj").getInt("avatar")
                        , new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mAvatarBitmaps.put(getIdentifier(position), response);
                        userAvatar.setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        userAvatar.setImageResource(R.drawable.campus_playing_app_icon);
                    }
                });
                mQueue.add(avatarImageRequest);
            }
            //获取活动图像
            final ImageView activityImage = (ImageView) view.findViewById(activity_image);
            if(mActivityImages.get(getIdentifier(position))!=null) {
                activityImage.setImageBitmap(mActivityImages.get(getIdentifier(position)));
            } else {
                MyImageRequest activityImageRequest = new MyImageRequest(
                        mActivity.getResources().getString(R.string.ROOT) + "media/" + mAttachObjs.get(position).getInt("image")
                        , new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mActivityImages.put(getIdentifier(position), response);
                        activityImage.setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        activityImage.setImageResource(R.drawable.campus_playing_app_icon);
                    }
                });
                mQueue.add(activityImageRequest);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }
    }
    private void setNotificationView(final int position,View view) {
        try {
            TextView userName = (TextView) view.findViewById(R.id.user_name);//发布人名称
            userName.setText(mAttachObjs.get(position).getJSONObject("creator_obj").getString("name"));
            TextView time = (TextView) view.findViewById(R.id.published_time); //发布时间
            time.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE2, mAttachObjs.get(position).getLong("created_at")));
            TextView title = (TextView) view.findViewById(R.id.notification_title); //活动标题
            title.setText(mAttachObjs.get(position).getString("title"));
            TextView content = (TextView) view.findViewById(R.id.notification_content); //活动内容
            content.setText(mAttachObjs.get(position).getString("content"));
            //获取用户头像
            final CircleImageView userAvatar = (CircleImageView) view.findViewById(user_avatar);
            if(mAvatarBitmaps.get(getIdentifier(position))!=null) {
                userAvatar.setImageBitmap(mAvatarBitmaps.get(getIdentifier(position)));
            } else {
                MyImageRequest avatarImageRequest = new MyImageRequest(
                        mActivity.getResources().getString(R.string.ROOT) + "media/" + mAttachObjs.get(position).getJSONObject("creator_obj").getInt("avatar")
                        , new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mAvatarBitmaps.put(getIdentifier(position), response);
                        userAvatar.setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        userAvatar.setImageResource(R.drawable.campus_playing_app_icon);
                    }
                });
                mQueue.add(avatarImageRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }
    }
    private void setPictureView(final int position,View view) {
        try {
            TextView userName = (TextView) view.findViewById(R.id.user_name);//发布人名称
            userName.setText(mAttachObjs.get(position).getJSONObject("creator_obj").getString("name"));
            TextView time = (TextView) view.findViewById(R.id.published_time); //发布时间
            time.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE2, mAttachObjs.get(position).getLong("created_at")));
            TextView description = (TextView) view.findViewById(R.id.picture_description); //活动内容
            description.setText(mAttachObjs.get(position).getString("description"));


            //获取用户头像
            final CircleImageView userAvatar = (CircleImageView) view.findViewById(user_avatar);
            if(mAvatarBitmaps.get(getIdentifier(position))!=null) {
                userAvatar.setImageBitmap(mAvatarBitmaps.get(getIdentifier(position)));
            } else {
                MyImageRequest avatarImageRequest = new MyImageRequest(
                        mActivity.getResources().getString(R.string.ROOT) + "media/" + mAttachObjs.get(position).getJSONObject("creator_obj").getInt("avatar")
                        , new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mAvatarBitmaps.put(getIdentifier(position), response);
                        userAvatar.setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        userAvatar.setImageResource(R.drawable.campus_playing_app_icon);
                    }
                });
                mQueue.add(avatarImageRequest);
            }

            //获取photo
            final PictureView pictureGroup = (PictureView)view.findViewById(R.id.picture_group);
            if(mPictures.get(getIdentifier(position))!=null) {
                List<Bitmap> bitmaps = mPictures.get(getIdentifier(position));
                for(Bitmap bitmap:bitmaps){
                    ImageView image = new ImageView(mActivity);
                    image.setImageBitmap(bitmap);
                    pictureGroup.addView(image);
                }
            } else {
                final List<Bitmap> bitmaps = new ArrayList<>();
                MyImageRequest pictureRequest = new MyImageRequest(
                        mActivity.getResources().getString(R.string.ROOT) + "media/" + mAttachObjs.get(position).getInt("media_id")
                        , new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        bitmaps.add(response);
                        mPictures.put(getIdentifier(position),bitmaps);//暂时这么弄
                        ImageView image = new ImageView(mActivity);
                        image.setImageBitmap(response);
                        pictureGroup.addView(image);
                    }
                }, 0, 0, Bitmap.Config.RGB_565
                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ImageView image = new ImageView(mActivity);
                        image.setImageResource(R.drawable.campus_playing_app_icon);
                        pictureGroup.addView(image);
                    }
                });
                mQueue.add(pictureRequest);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }
    }
    /**
     * 添加数据列表项
     */
    public void addStatusListItem(int type, JSONObject json) {
        mAttachTypes.add(type);
        mAttachObjs.add(json);
    }
    /**
     * 添加数据列表项
     */
    public void addStatusListItem(int position, int type, JSONObject json) {
        mAttachTypes.add(position,type);
        mAttachObjs.add(position,json);
    }
    /**
     * 添加数据列表项开头位置
     */
    public void addHeaderStausesListItem(int type, JSONObject json) {
        mAttachTypes.add(0, type);
        mAttachObjs.add(0, json);
    }

}
