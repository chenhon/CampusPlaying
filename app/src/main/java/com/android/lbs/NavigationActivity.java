package com.android.lbs;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.NaviPara;
import com.android.R;


/**
 * AMapV1地图中简单介绍poisearch搜索
 */
public class NavigationActivity extends FragmentActivity implements
		OnMarkerClickListener, InfoWindowAdapter {
	private AMap aMap;
	private double endLongitude;
	private double endLatitude;
	private String endAddress;
	/**
	 * 启动该活动
	 * @param activity
	 * @param longitude   终点的经度
	 * @param latitude    终点的纬度
	 * @param addressName 终点的地名
	 */
	public static void startActivity(Activity activity, double longitude, double latitude, String addressName) {
		Bundle bundle = new Bundle();
		bundle.putDouble("longitude", longitude); //经度
		bundle.putDouble("latitude", latitude); //纬度
		bundle.putString("addressName", addressName);//地名
		Intent intent = new Intent(activity, NavigationActivity.class);
		intent.putExtras(bundle);  //传入详细信息
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);//动画设置，从屏幕右边进入
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navigation_activity);
		init();
	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			this.endLongitude = bundle.getDouble("longitude");
			this.endLatitude = bundle.getDouble("latitude");
			this.endAddress = bundle.getString("addressName");
		}
		if (aMap == null) {
			aMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			setUpMap();
		}
	}

	/**
	 * 设置页面监听
	 */
	private void setUpMap() {

	//	aMap.setOnMarkerClickListener(this);// 添加点击marker监听事件
		aMap.setInfoWindowAdapter(this);// 添加显示infowindow监听事件

		//导航测试
		LatLng latLng = new LatLng(this.endLatitude,this.endLongitude);
		Marker marker = aMap.addMarker(new MarkerOptions().position(latLng).title(this.endAddress));
		marker.showInfoWindow();
		aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng));
		/*	MarkerOptions option = new MarkerOptions()    //初始化标记覆盖物
				.position(latLng)
				.zIndex(9)  //设置marker所在层级
				.draggable(true);  //设置手势拖拽
		ArrayList markerArray = new ArrayList<MarkerOptions>();
		markerArray.add(option);
		//getInfoWindow(option);

		aMap.addMarkers(markerArray, true);*/
		//startAMapNavi(marker);
	}


	@Override
	public boolean onMarkerClick(Marker marker) {
		marker.showInfoWindow();
		return false;
	}

	@Override
	public View getInfoContents(Marker marker) {
		return null;
	}

	@Override
	public View getInfoWindow(final Marker marker) {
		View view = getLayoutInflater().inflate(R.layout.navigation_begin_uri,
				null);
		TextView title = (TextView) view.findViewById(R.id.title);
		title.setText(marker.getTitle());

		ImageButton button = (ImageButton) view
				.findViewById(R.id.start_amap_app);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startAMapNavi(marker);

			}
		});
		return view;
	}

	/**
	 * 调起高德地图导航功能，如果没安装高德地图，会进入异常，可以在异常中处理，调起高德地图app的下载页面
	 */
	public void startAMapNavi(Marker marker) {
		// 构造导航参数
		NaviPara naviPara = new NaviPara();
		// 设置终点位置
		naviPara.setTargetPoint(marker.getPosition());
		// 设置导航策略，这里是避免拥堵
		naviPara.setNaviStyle(NaviPara.DRIVING_AVOID_CONGESTION);

		// 调起高德地图导航
		try {
			AMapUtils.openAMapNavi(naviPara, getApplicationContext());
		} catch (com.amap.api.maps.AMapException e) {

			// 如果没安装会进入异常，调起下载页面
			AMapUtils.getLatestAMapApp(getApplicationContext());

		}

	}

	/**
	 * 判断高德地图app是否已经安装
	 */
	public boolean getAppIn() {
		PackageInfo packageInfo = null;
		try {
			packageInfo = this.getPackageManager().getPackageInfo(
					"com.autonavi.minimap", 0);
		} catch (NameNotFoundException e) {
			packageInfo = null;
			e.printStackTrace();
		}
		// 本手机没有安装高德地图app
		if (packageInfo != null) {
			return true;
		}
		// 本手机成功安装有高德地图app
		else {
			return false;
		}
	}

	/**
	 * 获取当前app的应用名字
	 */
	public String getApplicationName() {
		PackageManager packageManager = null;
		ApplicationInfo applicationInfo = null;
		try {
			packageManager = getApplicationContext().getPackageManager();
			applicationInfo = packageManager.getApplicationInfo(
					getPackageName(), 0);
		} catch (NameNotFoundException e) {
			applicationInfo = null;
		}
		String applicationName = (String) packageManager
				.getApplicationLabel(applicationInfo);
		return applicationName;
	}

}
