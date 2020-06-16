package com.example.morta.where;

import android.util.Log;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpConnection {
    private OkHttpClient client;
    private  static HttpConnection instance = new HttpConnection();
    public static HttpConnection getInstance()
    {
        return instance;
    }

    private HttpConnection(){this.client = new OkHttpClient();}

    public void requestWebServer(String parameter, String parameter2, Callback callback)
    {
        RequestBody body = new FormBody.Builder()
                .add("parameter", parameter)
                .add("parameter2", parameter2)
                .build();
        Request request = new Request.Builder()
                .url("https://asia-east2-inlaid-theater-275012.cloudfunctions.net/function-1")
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
