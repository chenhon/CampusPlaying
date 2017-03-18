package com.android.tool;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkConnectStatus {
	
	private Activity activity;
	
	public NetworkConnectStatus(Activity activity) {
		this.activity = activity;
	}
	
	public boolean isConnectInternet() {
		boolean netSataus = false;
		ConnectivityManager conManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
		if(networkInfo != null) { // 注意，这个判断一定要
			netSataus = networkInfo.isAvailable();
		}
		return netSataus;
	}

}
