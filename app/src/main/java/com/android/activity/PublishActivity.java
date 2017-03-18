package com.android.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.android.tool.AskSavePopMenu;
import com.android.tool.BottomPopSelectMenu;
import com.android.tool.GetPhotoFrmAlbum;
import com.android.tool.GetPhotoFrmCamera;
import com.android.tool.PictureView;

public class PublishActivity extends AppCompatActivity {
    public static final int TAKE_PHOTO = 0; //拍照获取照片
    public final int CHOOSE_PHOTO = 1;  //从相册选取照片
    private TextView cancel;
    private TextView post;
    private EditText activityTile;
    private EditText activityContent;
    private ImageButton addPictureButton;
    private PictureView pictureGroup;
    private BottomPopSelectMenu bottomPopMenu;
    private AskSavePopMenu popAskSaveMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        initView();
        setLisener();

    }

    private void initView() {
        cancel = (TextView) findViewById(R.id.cancel);
        post = (TextView) findViewById(R.id.post);
        activityTile = (EditText) findViewById(R.id.activity_title);
        activityContent = (EditText) findViewById(R.id.activity_content);
        addPictureButton = (ImageButton) findViewById(R.id.add_picture_button); //图片添加按钮
        pictureGroup = (PictureView) findViewById(R.id.picture_group);//放置图片的容器
    }

    private void setLisener() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hasContent()) {  //没有填写任何内容
                    finish();//直接退出活动
                }
                popAskSaveMenu = new AskSavePopMenu(PublishActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch(v.getId()) {
                            case R.id.pop_no_save://要进行不保存操作
                                Toast.makeText(PublishActivity.this, "不保存操作", Toast.LENGTH_SHORT).show();
                                finish();//直接退出活动
                                break;
                            case R.id.pop_save://要进行保存操作
                                Toast.makeText(PublishActivity.this, "保存操作", Toast.LENGTH_SHORT).show();
                                finish();//直接退出活动
                        }
                    }
                });
                popAskSaveMenu.show();
                //
                //这里应该在finish之前弹出一个保存对话框
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isContentsIntact(true)) {
                    Toast.makeText(PublishActivity.this, "这里发布活动出去", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ImageView iv = new ImageView(getApplicationContext());
//                iv.setImageResource(R.drawable.pic);
//                pictureGroup.addView(iv, pictureGroup.getChildCount()-1);
                bottomPopMenu = new BottomPopSelectMenu(PublishActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch(v.getId()) {
                            case R.id.pop_select_fr_phone:
                                Toast.makeText(PublishActivity.this, "从手机中选择图片", Toast.LENGTH_SHORT).show();
                                //openAlbum();//从相册中获取图片
                                GetPhotoFrmAlbum.openAlbum(PublishActivity.this,CHOOSE_PHOTO);
                                break;
                            case R.id.pop_take_photo:
                                Toast.makeText(PublishActivity.this, "拍照获取图片", Toast.LENGTH_SHORT).show();
                               // openCamera(); //拍照获取图片
                                GetPhotoFrmCamera.openCamera(PublishActivity.this,TAKE_PHOTO);
                                break;
                        }
                    }
                });
                bottomPopMenu.show();
            }
        });
    }

    /**
     * 判断是否填写了内容
     * @return
     */
    private Boolean hasContent() {
        String str = activityTile.getText().toString();
        if(str != null && !"".equals(str)) {
            return true;
        }
        str = activityContent.getText().toString();
        if(str != null && !"".equals(str)) {
            return true;
        }

        if(pictureGroup.getChildCount() > 1) {
            return true;
        }
        return false;
    }
    /**
     * 判断发布内容是否完整
     */
    private Boolean isContentsIntact(Boolean isRemind) {
        String str = activityTile.getText().toString();
        if(str == null || "".equals(str)) {
            if(isRemind) {
                Toast.makeText(this, "请填写活动标题！", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        str = activityContent.getText().toString();
        if(str == null || "".equals(str)) {
            if(isRemind) {
                Toast.makeText(this, "请填写活动内容！", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }
    /**
     * 9宫格图片放置
     * @param bitMap
     */
    private void addPicture(Bitmap bitMap) {
        if(bitMap != null) {
            ImageView iv = new ImageView(getApplicationContext());
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImageBitmap(bitMap);
            pictureGroup.addView(iv, pictureGroup.getChildCount() - 1);
        }
    }
    /**
     * 运行时权限申请回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //有结果
                    GetPhotoFrmAlbum.openAlbumForResult(PublishActivity.this,CHOOSE_PHOTO);
                } else {
                    Toast.makeText(this, "You denied the permission that opening album", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case TAKE_PHOTO:
                if(resultCode == RESULT_OK) {
                    addPicture(GetPhotoFrmCamera.getPhoto(PublishActivity.this));
                } else {
                    Toast.makeText(this, "你没有拍照", Toast.LENGTH_SHORT).show();
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    addPicture(GetPhotoFrmAlbum.getPhoto(PublishActivity.this,data));
                } else {
                    Toast.makeText(this, "你没有选择任何照片", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
