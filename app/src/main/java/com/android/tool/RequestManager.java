package com.android.tool;

import android.content.Context;

import com.android.GlobalApplication;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;

import java.util.HashMap;

public class RequestManager {
	
	private static RequestQueue mRequestQueue;
	private static ImageLoader mImageLoader;

	private RequestManager() {
		// no instances
		
	}

	public static void init(Context context) {

//		int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
//		// Use 1/8th of the available memory for this memory cache.
//		int cacheSize = 1024 * 1024 * memClass / 8;
//		mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(cacheSize));
	}

	public static RequestQueue getRequestQueue() {
		if (mRequestQueue != null) {
			return mRequestQueue;
		} else {
			throw new IllegalStateException("RequestQueue not initialized");
		}
	}
	
	public static void addRequest(Request<?> request, Object tag) {
        if (tag != null) {
            request.setTag(tag);
        }
        mRequestQueue = GlobalApplication.get().getRequestQueue();
        request.setRetryPolicy(new DefaultRetryPolicy(400*1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(request);
    }
	
	public static void cancelAll(Object tag) {
		if(mRequestQueue!=null)
        mRequestQueue.cancelAll(tag);
    }
	
	/**
	 * Returns instance of ImageLoader initialized with {@see FakeImageCache}
	 * which effectively means that no memory caching is used. This is useful
	 * for images that you know that will be show only once.
	 * 
	 * @return
	 */
	public static ImageLoader getImageLoader() {
		if (mImageLoader != null) {
			return mImageLoader;
		} else {
			throw new IllegalStateException("ImageLoader not initialized");
		}
	}

	/**
	 * 在URL后加入参数
	 */
	public static String getURLwithParams(String url, HashMap<String, String> params) {
		int count = 0;
		StringBuilder builder = new StringBuilder(url);
		for (String key : params.keySet()) {
			if(0 == count) {     //添加的第一个参数时格式 ？key=value
				builder.append("?").append(key + "=" + params.get(key));
			} else {   //之后的参数格式 &key=value
				builder.append("&").append(key + "=" + params.get(key));
			}
			count++;
		}
		return builder.toString();
	}
}


