package com.mehboob.dialeradmin.payment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.mehboob.dialeradmin.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OrderApiClient {
    private static final String TAG = "OrderApiClient";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient buildClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d(TAG, message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }

    public interface OrderCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    /**
     * Calls your backend: POST /create-order
     * Expected backend JSON response: { order_id: string, payment_session_id: string, payment_link?: string }
     */
    public void createOrder(String orderId, String amount, String customerId, String phoneNumber, String customerName, String customerEmail, OrderCallback callback) {
        OkHttpClient client = buildClient();

        try {
            double orderAmount;
            try { orderAmount = Double.parseDouble(amount); } catch (Exception e) { orderAmount = 0d; }

            JSONObject payload = new JSONObject();
            payload.put("order_id", orderId);
            payload.put("order_amount", orderAmount);
            payload.put("order_currency", Config.CURRENCY);

            JSONObject customer = new JSONObject();
            customer.put("customer_id", customerId);
            customer.put("customer_name", customerName);
            customer.put("customer_email", customerEmail);
            customer.put("customer_phone", phoneNumber);
            payload.put("customer_details", customer);

            payload.put("is_production", Config.IS_PRODUCTION);

            String url = Config.BACKEND_BASE_URL + "/create-order";
            Log.d(TAG, "Calling backend create-order: " + url);

            RequestBody body = RequestBody.create(JSON, payload.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Network error creating order", e);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError("Network Error: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    int code = response.code();
                    Log.d(TAG, "Backend /create-order code=" + code + ", body(first 600)=" + (responseBody.length()>600?responseBody.substring(0,600)+"...":responseBody));
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        if (response.isSuccessful()) {
                            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(json));
                        } else {
                            String msg = json.optString("message", "API Error: " + code);
                            String finalMsg = msg;
                            new Handler(Looper.getMainLooper()).post(() -> callback.onError(finalMsg));
                        }
                    } catch (JSONException ex) {
                        Log.e(TAG, "Parse error", ex);
                        String finalMsg = "Parse Error: " + ex.getMessage();
                        new Handler(Looper.getMainLooper()).post(() -> callback.onError(finalMsg));
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "JSON build error", e);
            callback.onError("JSON Error: " + e.getMessage());
        }
    }

    /**
     * Calls your backend: GET /order-status/{orderId}
     * Expected backend JSON response: { order_status: string }
     */
    public void checkOrderStatus(String orderId, OrderCallback callback) {
        OkHttpClient client = buildClient();
        String url = Config.BACKEND_BASE_URL + "/order-status/" + orderId;
        Log.d(TAG, "Calling backend order-status: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network error checking order status", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Network Error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                int code = response.code();
                Log.d(TAG, "Backend /order-status code=" + code + ", body(first 600)=" + (responseBody.length()>600?responseBody.substring(0,600)+"...":responseBody));
                try {
                    JSONObject json = new JSONObject(responseBody);
                    if (response.isSuccessful()) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(json));
                    } else {
                        String msg = json.optString("message", "API Error: " + code);
                        String finalMsg = msg;
                        new Handler(Looper.getMainLooper()).post(() -> callback.onError(finalMsg));
                    }
                } catch (JSONException ex) {
                    Log.e(TAG, "Parse error", ex);
                    String finalMsg = "Parse Error: " + ex.getMessage();
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(finalMsg));
                }
            }
        });
    }
}
