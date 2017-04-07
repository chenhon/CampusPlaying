package com.android.tool;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.R;
import com.android.wheelview.DateUtils;
import com.android.wheelview.JudgeDate;
import com.android.wheelview.ScreenInfo;
import com.android.wheelview.WheelMain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2017/4/1 0001.
 */

public class SelectDateAndTime implements View.OnClickListener {

    private PopupWindow mPopupWindow;
    private MyCallBack mCallBack;
    private Activity mActitity;
    TextView tv_cancle;
    TextView tv_ensure;
    TextView tv_pop_title;
    WheelMain wheelMainDate;

    private java.text.DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public SelectDateAndTime(final Activity actitity, String title, MyCallBack callBack) {

        this.mActitity = actitity;
        this.mCallBack = callBack;
        View mMenuView = LayoutInflater.from(actitity).inflate(R.layout.show_popup_window, null);

        tv_cancle = (TextView) mMenuView.findViewById(R.id.tv_cancle);
        tv_ensure = (TextView) mMenuView.findViewById(R.id.tv_ensure);
        tv_pop_title = (TextView) mMenuView.findViewById(R.id.tv_pop_title);
        tv_pop_title.setText(title);
        tv_cancle.setOnClickListener(this);
        tv_ensure.setOnClickListener(this);

        ScreenInfo screenInfoDate = new ScreenInfo(actitity);
        wheelMainDate = new WheelMain(mMenuView, true);
        wheelMainDate.screenheight = screenInfoDate.getHeight();
        String time = DateUtils.currentMonth().toString();
        Calendar calendar = Calendar.getInstance();
        if (JudgeDate.isDate(time, "yyyy-MM-DD")) {
            try {
                calendar.setTime(new Date(time));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        wheelMainDate.initDateTimePicker(year, month, day, hours, minute);
        //final String currentTime = wheelMainDate.getTime().toString();

        mPopupWindow = new PopupWindow(mMenuView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setAnimationStyle(R.style.popwin_anim_style);
        mPopupWindow.setOnDismissListener(new poponDismissListener());
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);
    }

    /**
     * 菜单显示
     */
    public void show() {
        View rootView = ((ViewGroup) mActitity.findViewById(android.R.id.content)).getChildAt(0);
        mPopupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        backgroundAlpha(0.6f);
    }

    /**
     * 设置窗口透明度
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = mActitity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        mActitity.getWindow().setAttributes(lp);
        mActitity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    class poponDismissListener implements PopupWindow.OnDismissListener {
        @Override
        public void onDismiss() {
            backgroundAlpha(1f);
        }

    }
    @Override
    public void onClick(View view) {
        mPopupWindow.dismiss();
        backgroundAlpha(1f);
        switch (view.getId()) {
            case R.id.tv_ensure:
                long tim = 0;
                try {
                    tim = DataUtils.dateToStamp(wheelMainDate.getTime().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                mCallBack.handleTime(tim);
                break;
            default: break;
        }
    }

    public interface MyCallBack {
        void handleTime(long time);
    }
}
