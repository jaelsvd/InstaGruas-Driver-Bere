package com.umer.towtruckdriver.Service;


import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.umer.towtruckdriver.interfaces.TaskListener;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by MuhammadUmer on 9/29/2016.
 */
public class ExecuteTask {

    TaskListener listener;
    private Context context;
    private Map<String,String> obj;
    String url;

    public ExecuteTask(TaskListener listener,Map<String,String> object,String url,Context context){
        this.obj = object;
        this.url = url;
        this.listener = listener;
        this.context = context;

        sendData(this.url,this.obj,this.context);
    }


    public void sendData(String url,Map<String,String> params,Context context){

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                listener.onSuccess(response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError response) {
                listener.onFailure(response.toString());
            }

        });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        jsObjRequest.setShouldCache(false);

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.start();
        queue.add(jsObjRequest);
    }
}
