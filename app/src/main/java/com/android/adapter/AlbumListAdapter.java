package com.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.R;
import com.android.status.picture.DetailActivity;
import com.android.tool.BitmapLoaderUtil;
import com.android.tool.PictureView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    private Activity mActivity;

    public AlbumListAdapter(Activity activity) {
        this.mActivity = activity;
        this.dates = new ArrayList<>();
        this.mAlbumJsons = new ArrayList<>();
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

    public void clearListData() {
        dates.clear();
        mAlbumJsons.clear();
    }
    public List getItemJson(int datStr) {
        if(!dates.contains(datStr)) {
            dates.add(datStr);
            mAlbumJsons.add(new ArrayList<JSONObject>());
            return mAlbumJsons.get(mAlbumJsons.size()-1);
        } else {
            return mAlbumJsons.get(dates.indexOf(datStr));
        }

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
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                BitmapLoaderUtil.getInstance().getImage(imageView, BitmapLoaderUtil.TYPE_MEDIAN, pictureJson.getInt("media_id"));
                viewHolder.mPictureContainer.addView(imageView, i);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Bundle bundle = new Bundle();
                        int pid = 0;
                        int creatorId = 0;
                        try {
                            pid = pictureJson.getInt("id");
                            creatorId = pictureJson.getInt("creator");
                            bundle.putInt("aid", pid);       //动态id
                            bundle.putInt("creatorId", creatorId); //发布活动者id
                            intent.putExtras(bundle);  //传入详细信息
                            mActivity.startActivity(intent);
                        } catch (Exception e) {

                        }
                    }
                });
                /*if(mAlbumBitmaps.containsKey(pictureJson.getInt("id"))) {//图片已经加载过了
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
                }*/
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
