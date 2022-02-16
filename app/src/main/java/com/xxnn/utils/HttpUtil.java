package com.xxnn.utils;

import okhttp3.*;

import java.io.IOException;

/**
 * @author weiguan
 * @desc:
 * @date 2022/2/16 14:15
 */
public class HttpUtil {
    public static void post(String url, byte[] data) {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(data);
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
            }
        });
    }

    public static void get(String url, byte[] data) {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(data);
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
            }
        });
    }
}
