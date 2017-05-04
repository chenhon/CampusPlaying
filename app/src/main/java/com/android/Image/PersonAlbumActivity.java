package com.android.Image;

import android.os.Bundle;

import com.android.R;
import com.android.BaseActivity;

/**
 * 个人相册页
 * 根据发表的月份来分布相册
 * 这里只提供一个LinearLayout容器
 */
public class PersonAlbumActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_album);
    }
}
