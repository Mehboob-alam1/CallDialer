package com.mehboob.dialeradmin.payment;// OrderApiClient.java

import android.os.Handler;
import android.os.Looper;

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

public class OrderApiClient {

    private static final String BASE_URL = "https://sandbox.cashfree.com/pg/"; // use sandbox for testing
    private static final String APP_ID = "1050663db5989cbec31ef9036f63660501";  // from Cashfree Dashboard
    private static final String SECRET_KEY = "cfsk_ma_prod_4361e7dfba267d0079447013f14788c1_17c3912a";

    public interface OrderCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    public void createOrder(String orderId, String amount, String customerId, String phoneNumber, OrderCallback callback) {
        OkHttpClient client = new OkHttpClient();

        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("order_id", orderId);
            bodyJson.put("order_amount", amount);
            bodyJson.put("order_currency", "INR");
            bodyJson.put("customer_id", customerId);
            bodyJson.put("customer_email", "admin@dialerapp.com"); // Default email
            bodyJson.put("customer_phone", phoneNumber != null ? phoneNumber : "9999999999");
        } catch (JSONException e) {
            callback.onError("JSON Error: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), bodyJson.toString());

        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("x-client-id", APP_ID)
                .addHeader("x-client-secret", SECRET_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError("Network Error: " + e.getMessage()));
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject obj = new JSONObject(res);
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onSuccess(obj));
                } catch (JSONException e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onError("Parse Error: " + e.getMessage()));
                }
            }
        });
    }

    // Overloaded method for backward compatibility
    public void createOrder(String orderId, String amount, String customerId, OrderCallback callback) {
        createOrder(orderId, amount, customerId, "9999999999", callback);
    }

}
