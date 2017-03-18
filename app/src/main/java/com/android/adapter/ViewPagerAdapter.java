package com.android.adapter;

import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

	private List<View> mViews;
	private Activity mActivity;

	public ViewPagerAdapter(List<View> views, Activity activity) {
		mViews = views;
		mActivity = activity;
	}

	@Override
	public int getCount() {
		if (mViews != null) {
			return mViews.size();
		}
		return 0;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#destroyItem(android.view.ViewGroup, int, java.lang.Object)
	 */
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager)container).removeView(mViews.get(position));
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#saveState()
	 */
	@Override
	public Parcelable saveState() {
		return super.saveState();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#instantiateItem(android.view.ViewGroup, int)
	 */
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(mViews.get(position), 0);
		return mViews.get(position);
	}

}
