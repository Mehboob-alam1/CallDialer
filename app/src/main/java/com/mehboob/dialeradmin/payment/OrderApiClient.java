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

    public void createOrder(String orderId, String amount, String customerId, String phoneNumber, String customerName, String customerEmail, OrderCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build();

        try {
            // Create order request body as per Cashfree API documentation
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("order_id", orderId);
            // Ensure numeric amount in JSON
            orderRequest.put("order_amount", Double.parseDouble(amount));
            orderRequest.put("order_currency", Config.CURRENCY);
            
            // Customer details
            JSONObject customerDetails = new JSONObject();
            customerDetails.put("customer_id", customerId);
            customerDetails.put("customer_name", customerName != null && !customerName.isEmpty() ? customerName : "Admin User");
            customerDetails.put("customer_email", customerEmail != null && !customerEmail.isEmpty() ? customerEmail : "admin@dialerapp.com");
            customerDetails.put("customer_phone", phoneNumber != null && !phoneNumber.isEmpty() ? phoneNumber : "9999999999");
            orderRequest.put("customer_details", customerDetails);
            
            // Order meta
            JSONObject orderMeta = new JSONObject();
            orderMeta.put("return_url", Config.RETURN_URL + "?order_id=" + orderId);
            orderRequest.put("order_meta", orderMeta);
            
            // Order note
            orderRequest.put("order_note", "Premium subscription for " + Config.APP_NAME);

            Log.d(TAG, "Creating order with URL: " + Config.CASHFREE_BASE_URL);
            Log.d(TAG, "Env: " + (Config.IS_PRODUCTION ? "PRODUCTION" : "SANDBOX"));
            Log.d(TAG, "Order request: " + orderRequest.toString());

            RequestBody body = RequestBody.create(JSON, orderRequest.toString());

            Request request = new Request.Builder()
                    .url(Config.CASHFREE_BASE_URL)
                    .addHeader(CLIENT_ID_HEADER, Config.CASHFREE_APP_ID)
                    .addHeader(CLIENT_SECRET_HEADER, Config.CASHFREE_SECRET_KEY)
                    .addHeader(API_VERSION_HEADER, Config.CASHFREE_API_VERSION)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
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
                    String responseBody = response.body() != null ? response.body().string() : "";
                    String contentType = response.header("Content-Type", "");
                    Log.d(TAG, "Response code: " + response.code());
                    Log.d(TAG, "Response headers: " + response.headers());
                    Log.d(TAG, "Order creation raw response: " + (responseBody.length() > 400 ? responseBody.substring(0, 400) + "..." : responseBody));

                    boolean isJson = contentType.contains("application/json") || responseBody.trim().startsWith("{") || responseBody.trim().startsWith("[");
                    if (!isJson) {
                        String msg = "Non-JSON response from server (" + contentType + ") - first 200 chars: " +
                                (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody);
                        Log.e(TAG, msg);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onError(msg));
                        return;
                    }
                    
                    try {
                        JSONObject responseJson = new JSONObject(responseBody);
                        
                        if (response.isSuccessful()) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    callback.onSuccess(responseJson));
                        } else {
                            String errorMessage = "API Error: " + response.code();
                            if (responseJson.has("message")) {
                                errorMessage = responseJson.getString("message");
                            } else if (responseJson.has("error")) {
                                errorMessage = responseJson.getString("error");
                            } else if (responseJson.has("error_description")) {
                                errorMessage = responseJson.getString("error_description");
                            }
                            Log.e(TAG, "API Error: " + errorMessage);
                            new Handler(Looper.getMainLooper()).post(() ->
                                    callback.onError(errorMessage));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response", e);
                        Log.e(TAG, "Raw response: " + responseBody);
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onError("Parse Error: " + e.getMessage() + "\nResponse: " + responseBody));
                    }
                }
            });
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating order request", e);
            callback.onError("JSON Error: " + e.getMessage());
        }
    }

    // Backward compatible overloads
    public void createOrder(String orderId, String amount, String customerId, String phoneNumber, OrderCallback callback) {
        createOrder(orderId, amount, customerId, phoneNumber, null, null, callback);
    }

    public void createOrder(String orderId, String amount, String customerId, OrderCallback callback) {
        createOrder(orderId, amount, customerId, "9999999999", null, null, callback);
    }

    /**
     * Check order status
     */
    public void checkOrderStatus(String orderId, OrderCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build();

        String url = Config.CASHFREE_BASE_URL + "/" + orderId;
        Log.d(TAG, "Checking order status: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader(CLIENT_ID_HEADER, Config.CASHFREE_APP_ID)
                .addHeader(CLIENT_SECRET_HEADER, Config.CASHFREE_SECRET_KEY)
                .addHeader(API_VERSION_HEADER, Config.CASHFREE_API_VERSION)
                .addHeader("Accept", "application/json")
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
                String responseBody = response.body() != null ? response.body().string() : "";
                String contentType = response.header("Content-Type", "");
                Log.d(TAG, "Order status response code: " + response.code());
                Log.d(TAG, "Order status raw response: " + (responseBody.length() > 400 ? responseBody.substring(0, 400) + "..." : responseBody));
                
                boolean isJson = contentType.contains("application/json") || responseBody.trim().startsWith("{") || responseBody.trim().startsWith("[");
                if (!isJson) {
                    String msg = "Non-JSON response from server (" + contentType + ") - first 200 chars: " +
                            (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody);
                    Log.e(TAG, msg);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(msg));
                    return;
                }

                try {
                    JSONObject responseJson = new JSONObject(responseBody);
                    
                    if (response.isSuccessful()) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onSuccess(responseJson));
                    } else {
                        String errorMessage = "API Error: " + response.code();
                        if (responseJson.has("message")) {
                            errorMessage = responseJson.getString("message");
                        } else if (responseJson.has("error")) {
                            errorMessage = responseJson.getString("error");
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
