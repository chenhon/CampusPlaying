package com.android.tool;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/4/29 0029.
 */

public class CustomTextView extends TextView {

    public boolean linkHit;//内部链接是否被点击

    public CustomTextView(Context context) {

        super(context);

    }

    public CustomTextView(Context context, AttributeSet attrs) {

        super(context, attrs);

    }

    public CustomTextView(Context context, AttributeSet attrs,int defStyle) {

        super(context, attrs, defStyle);

    }

    @Override

    public boolean performClick() {

        if(linkHit){   //屏蔽后续的onClickListener

            return true;

        }

        return super.performClick();

    }

    @Override

    public boolean onTouchEvent(MotionEvent event) {

        linkHit =false;

        return super.onTouchEvent(event);

    }

}