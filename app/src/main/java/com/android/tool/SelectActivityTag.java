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
import com.android.wheelview.ArrayWheelAdapter;
import com.android.wheelview.ScreenInfo;
import com.android.wheelview.WheelView;

import static com.android.R.id.tv_ensure;

/**
 * Created by Administrator on 2017/4/1 0001.
 */

public class SelectActivityTag implements View.OnClickListener {

    TextView mTvPopTitle;
    WheelView mActivityTag;
    TextView mTvEnsure;

    private PopupWindow mPopupWindow;
    private MyCallBack mCallBack;
    private Activity mActitity;

    public SelectActivityTag(final Activity actitity, String title, MyCallBack callBack) {

        this.mActitity = actitity;
        this.mCallBack = callBack;
        View mMenuView = LayoutInflater.from(actitity).inflate(R.layout.select_tag_popup_window, null);

        mActivityTag = (WheelView) mMenuView.findViewById(R.id.activity_tag);
        mTvEnsure = (TextView) mMenuView.findViewById(R.id.tv_ensure);
        mTvPopTitle = (TextView) mMenuView.findViewById(R.id.tv_pop_title);
        mTvPopTitle.setText(title);
        mTvEnsure.setOnClickListener(this);

        ScreenInfo screenInfoDate = new ScreenInfo(actitity);

        String[] mItems = mActitity.getResources().getStringArray(R.array.activityTags);
        mActivityTag.TEXT_SIZE = screenInfoDate.getHeight()/ 140 * 4;
        mActivityTag.setAdapter(new ArrayWheelAdapter<String>(
                mItems, 6));
        mActivityTag.setCyclic(true);// 可循环滚动
       // mActivityTag.setLabel("年");// 添加文字
        mActivityTag.setCurrentItem(1);// 初始化时显示的数据

        mPopupWindow = new PopupWindow(mMenuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
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
     *
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
            case tv_ensure:
                mCallBack.handleTime(mActivityTag.getCurrentItem());
                break;
            default:
                break;
        }
    }

    public interface MyCallBack {
        void handleTime(int index);
    }
}
