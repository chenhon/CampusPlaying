package com.android.Image;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.R;

/**
 * 个人相册页
 * 根据发表的月份来分布相册
 * 这里只提供一个LinearLayout容器
 */
public class PersonAlbumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_album);
    }
}
