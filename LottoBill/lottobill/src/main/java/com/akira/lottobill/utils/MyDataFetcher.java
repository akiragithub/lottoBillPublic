package com.akira.lottobill.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.akira.lottobill.Config;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MyDataFetcher {

    private static String DATA_LINK = "https://api.kai58a.com/data/jndpc28/last.json";

    public static void fetchData(final Context context)
    {
        Log.d(Config.LOG_TAG,"fetching data");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, DATA_LINK,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Config.LOG_TAG,"first response is : "+response);
                        sendResult(context, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Config.LOG_TAG,"first error is : "+error);
                        sendResult(context,null);
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    private static void sendResult(Context context, String result)
    {
        Intent intent = new Intent();
        intent.setAction(Config.INTENT_FILTER_ACTION);
        intent.putExtra("response",result);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
