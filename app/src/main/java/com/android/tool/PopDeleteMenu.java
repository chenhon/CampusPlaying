package com.android.tool;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.R;

/**
 * 删除弹出窗
 */

public class PopDeleteMenu implements View.OnClickListener {
    private PopupWindow popupWindow;
    private View popView;
    private View attachView;
    private TextView tvDelete;
    private Context mContext;
    private View.OnClickListener mClickListener;

    public PopDeleteMenu(Context context, View view, View.OnClickListener clickListener) {
        this.mClickListener = clickListener;
        this.mContext = context;
        this.attachView = view;
        popView = LayoutInflater.from(mContext).inflate(R.layout.layout_long_click_dialog, null);
        tvDelete=(TextView) popView.findViewById(R.id.tv_delete);
        tvDelete.setOnClickListener(this);

        //设置弹出窗
        popupWindow = new PopupWindow(popView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);  //点击窗以外部分则弹出窗消息
        popupWindow.setFocusable(true);    //避免窗以外的View的事件还是可以触发
        popupWindow.setBackgroundDrawable(new ColorDrawable());

    }

    //显示弹出窗
    public void show(){

        //以attachView的左下角为参考点偏移   ，这里实现在attachView的中间显示
        popupWindow.showAsDropDown(attachView,(attachView.getWidth()-tvDelete.getWidth())/2,-attachView.getHeight());

        //  popupWindow.showAsDropDown(view,(view.getWidth()-tvDelete.getWidth())/2,-view.getHeight()-tvDelete.getHeight());
    }

    @Override
    public void onClick(View v) {
        mClickListener.onClick(v);
        popupWindow.dismiss();
    }
}
