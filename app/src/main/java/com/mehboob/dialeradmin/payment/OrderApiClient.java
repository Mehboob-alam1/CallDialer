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

public class OrderApiClient {
    private static final String TAG = "OrderApiClient";
    
    // API Headers
    private static final String CLIENT_ID_HEADER = "x-client-id";
    private static final String CLIENT_SECRET_HEADER = "x-client-secret";
    private static final String API_VERSION_HEADER = "x-api-version";
    
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public interface OrderCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    public void createOrder(String orderId, String amount, String customerId, String phoneNumber, OrderCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Create order request body as per Cashfree API documentation
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("order_id", orderId);
            orderRequest.put("order_amount", amount);
            orderRequest.put("order_currency", Config.CURRENCY);
            
            // Customer details
            JSONObject customerDetails = new JSONObject();
            customerDetails.put("customer_id", customerId);
            customerDetails.put("customer_name", "Admin User");
            customerDetails.put("customer_email", "admin@dialerapp.com");
            customerDetails.put("customer_phone", phoneNumber != null ? phoneNumber : "9999999999");
            orderRequest.put("customer_details", customerDetails);
            
            // Order meta
            JSONObject orderMeta = new JSONObject();
            orderMeta.put("return_url", Config.RETURN_URL + "?order_id=" + orderId);
            orderRequest.put("order_meta", orderMeta);
            
            // Order note
            orderRequest.put("order_note", "Premium subscription for " + Config.APP_NAME);

            Log.d(TAG, "Creating order: " + orderRequest.toString());

            RequestBody body = RequestBody.create(JSON, orderRequest.toString());

            Request request = new Request.Builder()
                    .url(Config.CASHFREE_BASE_URL)
                    .addHeader(CLIENT_ID_HEADER, Config.CASHFREE_APP_ID)
                    .addHeader(CLIENT_SECRET_HEADER, Config.CASHFREE_SECRET_KEY)
                    .addHeader(API_VERSION_HEADER, Config.CASHFREE_API_VERSION)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override 
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Network error creating order", e);
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onError("Network Error: " + e.getMessage()));
                }
                
                @Override 
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Order creation response: " + responseBody);
                    
                    try {
                        JSONObject responseJson = new JSONObject(responseBody);
                        
                        if (response.isSuccessful()) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    callback.onSuccess(responseJson));
                        } else {
                            String errorMessage = "API Error: " + response.code();
                            if (responseJson.has("message")) {
                                errorMessage = responseJson.getString("message");
                            }
                            new Handler(Looper.getMainLooper()).post(() ->
                                    callback.onError(errorMessage));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response", e);
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onError("Parse Error: " + e.getMessage()));
                    }
                }
            });
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating order request", e);
            callback.onError("JSON Error: " + e.getMessage());
        }
    }

    // Overloaded method for backward compatibility
    public void createOrder(String orderId, String amount, String customerId, OrderCallback callback) {
        createOrder(orderId, amount, customerId, "9999999999", callback);
    }

    /**
     * Check order status
     */
    public void checkOrderStatus(String orderId, OrderCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Config.CASHFREE_BASE_URL + "/" + orderId)
                .addHeader(CLIENT_ID_HEADER, Config.CASHFREE_APP_ID)
                .addHeader(CLIENT_SECRET_HEADER, Config.CASHFREE_SECRET_KEY)
                .addHeader(API_VERSION_HEADER, Config.CASHFREE_API_VERSION)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override 
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network error checking order status", e);
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError("Network Error: " + e.getMessage()));
            }
            
            @Override 
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, "Order status response: " + responseBody);
                
                try {
                    JSONObject responseJson = new JSONObject(responseBody);
                    
                    if (response.isSuccessful()) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onSuccess(responseJson));
                    } else {
                        String errorMessage = "API Error: " + response.code();
                        if (responseJson.has("message")) {
                            errorMessage = responseJson.getString("message");
                        }
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onError(errorMessage));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing response", e);
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onError("Parse Error: " + e.getMessage()));
                }
            }
        });
    }
}
