package com.android.tool;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

import com.android.R;

public class BottomPopSelectMenu implements OnClickListener,OnTouchListener{

	private PopupWindow popupWindow;
    private Button btnCancel, btnSelectFrmPhone, btnTakePhoto;
    private View mMenuView;
    private Activity mContext;
    private OnClickListener clickListener;

    public BottomPopSelectMenu(Activity context, OnClickListener clickListener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        this.clickListener=clickListener;
        mContext=context;
        mMenuView = inflater.inflate(R.layout.pop_bottom_select_menu, null);
        btnSelectFrmPhone = (Button) mMenuView.findViewById(R.id.pop_select_fr_phone);
        btnTakePhoto = (Button) mMenuView.findViewById(R.id.pop_take_photo);
        btnCancel = (Button) mMenuView.findViewById(R.id.pop_cancel);
        btnCancel.setOnClickListener(this);
        btnSelectFrmPhone.setOnClickListener(this);
        btnTakePhoto.setOnClickListener(this);
        popupWindow=new PopupWindow(mMenuView,LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT,true);
        popupWindow.setAnimationStyle(R.style.popwin_anim_style);
        ColorDrawable dw = new ColorDrawable(context.getResources().getColor(R.color.ccc));
        popupWindow.setBackgroundDrawable(dw);
        mMenuView.setOnTouchListener(this);
    }

    /**
     * 菜单显示
     */
    public void show(){
    	View rootView=((ViewGroup)mContext.findViewById(android.R.id.content)).getChildAt(0);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); 
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
     * @param arg0
     * @param event
     * @return
     */
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		int height = mMenuView.findViewById(R.id.pop_layout).getTop();
        int y=(int) event.getY();
        if(event.getAction()==MotionEvent.ACTION_UP){
            if(y<height){
            	popupWindow. dismiss();
            }
        }
        return true;
	}
 
}
