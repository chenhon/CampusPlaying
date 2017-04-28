package com.android.tool;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.ImageRequest;

import java.util.HashMap;
import java.util.Map;
/*
* type # [small/median/large/orig] # 默认为median
*  small宽度为100, median宽度为640, large宽度为1280, 等比压缩
*/
public class MyImageRequest extends ImageRequest {
    public static final String SIZE_SMALL = "small";  //宽度100
    public static final String SIZE_MEDIAN = "median";//宽度640
    public static final String SIZE_LARGE = "large";  //宽度1280
    public static final String SIZE_ORIGNAL = "orig";


    private Map<String, String> mHeaderParams = new HashMap<>();
	public MyImageRequest(String url, Listener<Bitmap> listener, int maxWidth, int maxHeight, Config decodeConfig, ErrorListener errorListener) {
		super(url, listener, maxWidth, maxHeight, decodeConfig, errorListener);
	}

	/* (non-Javadoc)
     * @see com.android.volley.toolbox.StringRequest#parseNetworkResponse(com.android.volley.NetworkResponse)
     */
    @Override
    protected Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
        // since we don't know which of the two underlying network vehicles
        // will Volley use, we have to handle and store session cookies manually
    	//GlobalApplication.checkSessionCookie(response.headers);
    	Log.e("TAG", "parseNetworkResponse->response.headers:" + response.headers);
        return super.parseNetworkResponse(response);
    }

    /* (non-Javadoc)
     * @see com.android.volley.Request#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
    	
/*    	Map<String, String> headers = super.getHeaders();

        if (headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
        }*/
		
        return mHeaderParams;
    }

    public void addHeader(String key, String value) {
        mHeaderParams.put(key,value);
    }

    public void setImageSiza(String size) {
        switch(size) {
            case SIZE_SMALL:
            case SIZE_MEDIAN:
            case SIZE_LARGE:
            case SIZE_ORIGNAL:  mHeaderParams.put("type ",size);
                break;
            default:
                break;
        }
    }
}
