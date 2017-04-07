package com.android.tool;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.android.R;

/**
 * 评论项长按弹出窗
 */
public class PopCommentSelectMenu implements OnClickListener, View.OnTouchListener{

    private PopupWindow popupWindow;
    private View mMenuView;
    private Activity mContext;
    private Button mPopReply, mPopCopy, mpopDelete,mPopReport, mPopCancel;
    private OnClickListener clickListener;

    /**
     * 构造评论长按弹出窗
     * @param context  上下文
     * @param clickListener 对回复 复制 举报按钮的监听事件
     */
    public PopCommentSelectMenu(Activity context, Boolean canDelete, OnClickListener clickListener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        this.clickListener = clickListener;
        mContext = context;
        mMenuView = inflater.inflate(R.layout.pop_commend_select_menu, null);

        mPopReply = (Button) mMenuView.findViewById(R.id.pop_reply);   //回复
        mPopCopy = (Button) mMenuView.findViewById(R.id.pop_copy);     //复制
        mpopDelete = (Button) mMenuView.findViewById(R.id.pop_delete); //删除
        mPopReport = (Button) mMenuView.findViewById(R.id.pop_report); //举报
        mPopCancel = (Button) mMenuView.findViewById(R.id.pop_cancel); //取消
        if(!canDelete) {
            mpopDelete.setVisibility(View.GONE);
        }
        mPopReply.setOnClickListener(this);
        mPopCopy.setOnClickListener(this);
        mpopDelete.setOnClickListener(this);
        mPopReport.setOnClickListener(this);
        mPopCancel.setOnClickListener(this);
        popupWindow = new PopupWindow(mMenuView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        popupWindow.setAnimationStyle(R.style.popwin_anim_style);
        ColorDrawable dw = new ColorDrawable(context.getResources().getColor(R.color.ccc));
        popupWindow.setBackgroundDrawable(dw);
        popupWindow.setOutsideTouchable(true);  //点击窗以外部分则弹出窗消息
        popupWindow.setFocusable(true);    //避免窗以外的View的事件还是可以触发

        mMenuView.setOnTouchListener(this);
    }

    /**
     * 菜单显示
     */
    public void show() {
        View rootView = ((ViewGroup) mContext.findViewById(android.R.id.content)).getChildAt(0);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);//中间显示
    }

    @Override
    public void onClick(View view) {
        popupWindow.dismiss();
        switch (view.getId()) {
            case R.id.pop_cancel:
                break;
            default:
                clickListener.onClick(view);
                break;
        }
    }

    /**
     * 点击菜单以外界面，则菜单隐藏
     *
     * @param arg0
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        int height = mMenuView.findViewById(R.id.pop_layout).getTop();
        int y = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (y < height) {
                popupWindow.dismiss();
            }
        }
        return true;
    }

}
