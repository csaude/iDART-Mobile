package mz.org.fgh.idartlite.rest.helper;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

public class ObjectRequest<T> extends JsonRequest<T> {

    private static final Gson gson = new Gson();
    private final Class clazz;
    private final Response.Listener<T> listener;
    private int mMethod;
    private String mUrl;
    private JSONObject mRequestBody;
    private static final String PROTOCOL_CHARSET="utf-8";
    private static final String TAG="ObjectRequest";
    private Map<String, String> mHeaders;
    private Map<String, Object> mParams;
    private JSONObject jParams;

//    public ObjectRequest(int method, String url, Map<String, Object> params, Class clazz, Response.Listener<T> listener, Response.ErrorListener errorListener) {
//        super(method, url, gson.toJson(params), listener, errorListener);
//        this.clazz = clazz;
//        this.listener = listener;
//        if(params!=null){
//            mParams = params;
//            mRequestBody = gson.toJson(params);
//        }
//        this.mMethod = method;
//        this.mUrl = url;
//    }

    public ObjectRequest(int method, String url, JSONObject params, Class clazz, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, gson.toJson(params), listener, errorListener);
        this.clazz = clazz;
        this.listener = listener;
        if(params!=null){
            jParams = params;
            mRequestBody = params;
        }
        this.mMethod = method;
        this.mUrl = url;
    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    public String getUrl() {
        if(mMethod == Method.GET && mUrl != null) {
            StringBuilder stringBuilder = new StringBuilder(mUrl);
            if (mParams != null) {
                Iterator<Map.Entry<String, Object>> iterator = mParams.entrySet().iterator();
                int i = 1;
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> entry = iterator.next();
                    if(i == 1) {
                        stringBuilder.append("?" + entry.getKey() + "=" + entry.getValue());
                    } else {
                        stringBuilder.append("&" + entry.getKey() + "=" + entry.getValue());
                    }
                    iterator.remove(); // avoids a ConcurrentModificationException
                    i++;
                }
                mUrl = stringBuilder.toString();
            }
        }
        return mUrl;
    }

    public void setHeaders(Map<String, String> headers) {
        mHeaders = headers;
    }

    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null? null : mRequestBody.toString().getBytes(PROTOCOL_CHARSET);
        }catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported Encoding while trying to get the bytes of " + mRequestBody + "using " + PROTOCOL_CHARSET);
            return null;
        }
    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        try {

            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            if(json.isEmpty()) {
                return Response.success(response.headers, HttpHeaderParser.parseCacheHeaders(response));
            }

            if (clazz.getName().equals(JSONObject.class.getName())) {
                try {
                    JSONObject object = new JSONObject(json);
                    return Response.success(object, HttpHeaderParser.parseCacheHeaders(response));
                }catch (JSONException ex) {
                    return Response.error(new ParseError(ex));
                }

            } else if (clazz.getName().equals(JSONArray.class.getName())) {
                try {
                    JSONArray object = new JSONArray(json);
                    return Response.success(object, HttpHeaderParser.parseCacheHeaders(response));
                }catch (JSONException ex) {
                    return Response.error(new ParseError(ex));
                }
            } else {
                return Response.success(gson.fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
            }
        }catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (mHeaders == null || mHeaders.isEmpty()) {
            return super.getHeaders();
        }else{
            return mHeaders;
        }
    }
}
