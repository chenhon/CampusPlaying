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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 相册适配器
 * item以月份为单位
 * 每个item由多张照片组成
 *
 * 1.照片加载了没有，怎么放在照片容器中
 */

public class AlbumListAdapter extends BaseAdapter {
    private List<Integer> dates;//日期
    private List<List<JSONObject>> mAlbumJsons;//照片对应的json数据
    private Map<Integer, Bitmap> mAlbumBitmaps;//照片，需要另外加载，key为照片id
    private Activity mActivity;
    private RequestQueue mQueue;

    public AlbumListAdapter(Activity activity) {
        this.mActivity = activity;
        this.dates = new ArrayList<>();
        this.mAlbumJsons = new ArrayList<>();
        this.mAlbumBitmaps = new HashMap<>();
        mQueue = GlobalApplication.get().getRequestQueue();
    }

    /**
     * 设置日期
     * @param datStr
     */
    public void setDate(int datStr) {
        dates.add(0,datStr);
    }

    /**
     * 设置日期
     * @param datStr
     */
    public void setLastDate(int datStr) {
        dates.add(datStr);
    }

    /**
     * 获取一个item对应的图片json数据
     * @return
     */
    public List getFirstItemJson(Boolean isExist) {
        if(!isExist) {
            mAlbumJsons.add(0,new ArrayList<JSONObject>());
        }
        return mAlbumJsons.get(0);
    }
    public List getLastItemJson(Boolean isExist) {
        if(!isExist) {
            mAlbumJsons.add(new ArrayList<JSONObject>());
        }
        return mAlbumJsons.get(mAlbumJsons.size()-1);
    }

    //    public JSONObject getJsonObj(int position) {
//        return mfollowJsons.get(position);
//    }
//    public long getId(int position){
//        long id = 0;
//        try {
//            id = mfollowJsons.get(position).getLong("id");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return id;
//    }
    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public Object getItem(int position) {
        return dates.get(position);
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
            view = mActivity.getLayoutInflater().inflate(R.layout.album_listitem, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        try {
            viewHolder.mPhotoTime.setText(String.valueOf(dates.get(position))); //照片月份
            //viewHolder.mPhotoTime.setText(DataUtils.stampToDate(DataUtils.DATA_TYPE5,DataUtils.getCurrentTime()));
            //加载照片
            List<JSONObject> pictureJsons = mAlbumJsons.get(position); //一个item的数据
            viewHolder.mPictureContainer.removeAllViews();      //先清除容器里的孩子
            for(int i = 0; i < pictureJsons.size(); i++) {
                final JSONObject pictureJson = pictureJsons.get(i);
                final ImageView imageView = new ImageView(mActivity);
                viewHolder.mPictureContainer.addView(imageView, i);
                if(mAlbumBitmaps.containsKey(pictureJson.getInt("id"))) {//图片已经加载过了
                    imageView.setImageBitmap(mAlbumBitmaps.get(pictureJson.getInt("id")));
                } else {
                    MyImageRequest avatarImageRequest = new MyImageRequest(
                            mActivity.getResources().getString(R.string.ROOT) + "media/" + pictureJson.getInt("media_id")
                            , new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            try {
                                mAlbumBitmaps.put(pictureJson.getInt("id"), response);
                                imageView.setImageBitmap(response);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("getMEDIA:TAG", e.toString());
                                imageView.setImageResource(R.drawable.campus_playing_app_icon);
                            }
                        }
                    }, 0, 0, Bitmap.Config.RGB_565
                            , new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            imageView.setImageResource(R.drawable.campus_playing_app_icon);
                        }
                    });
                    mQueue.add(avatarImageRequest);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("getMEDIA:TAG", e.toString());
        }
        return view;
    }


    static class ViewHolder {
        @BindView(R.id.photo_time)
        TextView mPhotoTime;
        @BindView(R.id.picture_container)
        PictureView mPictureContainer;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
