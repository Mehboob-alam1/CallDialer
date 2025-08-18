package com.mehboob.dialeradmin.payment;

// CheckOrderStatusApiClient.java

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CheckOrderStatusApiClient {
    private static final String BASE_URL = "https://sandbox.cashfree.com/pg/";
    private final OkHttpClient client = new OkHttpClient();

    public interface StatusCallback {
        void onSuccess(String orderStatus);
        void onError(String error);
    }

    public void checkStatus(String orderId, StatusCallback callback) {
        HttpUrl url = HttpUrl.parse(BASE_URL).newBuilder()
                .addQueryParameter("order_id", orderId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                try {
                    JSONObject obj = new JSONObject(resp);
                    String status = obj.optString("order_status", "UNKNOWN");
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(status));
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                }
            }
        });
    }
}
