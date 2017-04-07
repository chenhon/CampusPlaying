package com.android.tool;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class MyStringRequest extends StringRequest {

	private Map<String, String> mHeaderParams ;  //URL上的参数
    private final Map<String, String> mBodyParams;    //body上的参数
	/**
     * @param method
     * @param url
     * @param bodyParams
     *            A {@link HashMap} to post with the request. Null is allowed
     *            and indicates no parameters will be posted along with request.
     * @param listener
     * @param errorListener
     */
    public MyStringRequest(int method, String url, Map<String, String> bodyParams, Listener<String> listener,
                           ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        mBodyParams = bodyParams;//键值对参数
    }

    public MyStringRequest(int method, String url, Map<String, String> HeaderParams, Map<String, String> params, Listener<String> listener,
                           ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        mHeaderParams = HeaderParams;
        mBodyParams = params;//键值对参数
    }
    /**
     * 发出POST请求的时候，
     * Volley会尝试调用StringRequest的父类——Request中的getParams()方法来获取POST参数
     * @return
     */
    @Override
    protected Map<String, String> getParams() {
        return mBodyParams;
    }

/*    @Override
    protected void deliverResponse(String response) {
        super.deliverResponse(new String(response, HTTP.ASCII));
    }*/

    /* (non-Javadoc)
         * @see com.android.volley.toolbox.StringRequest#parseNetworkResponse(com.android.volley.NetworkResponse)
         */
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {

    	Log.e("TAG", "parseNetworkResponse->response.headers:" + response.headers);

        try {//jsonObject要和前面的类型一致,此处都是String
            String jsonObject = new String(
                    new String(response.data, "UTF-8"));
            return        Response.success(jsonObject, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (Exception je) {
            return Response.error(new ParseError(je));
        }
    }

    /* (non-Javadoc)
     * @see com.android.volley.Request#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
   //     return mHeaderParams;
        if(mHeaderParams != null) {
            return mHeaderParams;
        } else {
            return super.getHeaders();
        }
//        return  new VolleyRequestParams() //URL上的参数
//                .with("Accept","application/json");
    	//Map<String, String> headers = super.getHeaders(); //要添加的token
//			if (headers == null || headers.equals(Collections.emptyMap())) {
//	            headers = new HashMap<String, String>();
//	        }
      //  headers.put("Content-Type", "application/x-javascript");
    //    Map<String, String> headers = new HashMap<String, String>();
//        headers.put("Content-Type", "application/json; charset=utf-8");
//        headers.put("token","123");
//        headers.put("page","4");
//        headers.put("count","6");
    //    headers.put("Accept","application/json");
          //  GlobalApplication.addToken(headers); //token加入到header中
      //  return headers;
    }

}
