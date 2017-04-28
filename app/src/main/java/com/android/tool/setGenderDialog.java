package com.android.tool;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.R;

/**
 * 设置性别对话框
 *
 * @author ChenHong
 */
public class SetGenderDialog extends Dialog {

    private ImageView mSelectMale;
    private LinearLayout mLlMale;
    private ImageView mSelectFemale;
    private LinearLayout mLlFemale;
    private Context mContext;

    private int selectType;

    public SetGenderDialog(Context context, int gender) {
        super(context, R.style.MyNoFrame_Dialog);
        mContext = context;
        init(gender);
    }


    private void init(int gender) {
        setContentView(R.layout.dialog_set_gender);

        // 设置宽度
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
/*        LayoutParams lp = window.getAttributes();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);*/

        mSelectMale = (ImageView) findViewById(R.id.select_male);
        mLlMale = (LinearLayout) findViewById(R.id.ll_male);
        mSelectFemale = (ImageView) findViewById(R.id.select_female);
        mLlFemale = (LinearLayout) findViewById(R.id.ll_female);
        setGender(gender);

    }

    private void setGender(int gender){
        if (gender == 0) { //男生
            mSelectMale.setVisibility(View.VISIBLE);
            mSelectFemale.setVisibility(View.INVISIBLE);
            selectType = 0;
        } else {//女生
            mSelectMale.setVisibility(View.INVISIBLE);
            mSelectFemale.setVisibility(View.VISIBLE);
            selectType = 1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    //获取评论框内容
    public int getContent() {
        return selectType;
    }

    //设置回复按钮回调
    public void setClickListener(
            View.OnClickListener onClickListener) {
        if(selectType == 0){ //男生
            mLlFemale.setOnClickListener(onClickListener);
            mLlMale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SetGenderDialog.this.dismiss();
                }
            });
        }else {
            mLlFemale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SetGenderDialog.this.dismiss();
                }
            });
            mLlMale.setOnClickListener(onClickListener);
        }

    }
}


