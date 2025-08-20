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
import okhttp3.Credentials;
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

    private boolean isLikelyJsonResponse(Response response, String body) {
        String contentType = response.header("Content-Type", "").toLowerCase();
        String trimmed = body == null ? "" : body.trim();
        // Consider JSON if header indicates json OR body looks like a JSON object/array
        return contentType.contains("application/json") || contentType.contains("json")
                || (trimmed.startsWith("{") || trimmed.startsWith("["));
    }

    private void handleNonJsonError(Response response, String responseBody, OrderCallback callback) {
        String ct = response.header("Content-Type", "unknown");
        int code = response.code();
        String snippet = responseBody == null ? "" : responseBody.replaceAll("\n", " ");
        if (snippet.length() > 200) snippet = snippet.substring(0, 200) + "...";

        String hint;
        if (code == 401) {
            hint = "Authentication failed. Check Cashfree keys and environment.";
        } else if (code == 403) {
            hint = "Access forbidden. In production, ensure app integrity (Play Store) and allowed origins.";
        } else if (code == 400) {
            hint = "Bad request. Verify payload fields (amount numeric, order_id unique).";
        } else {
            hint = "HTTP " + code + ".";
        }

        String finalMsg = "Non-JSON response from server: " + hint + " Content-Type=" + ct + ", body=\"" + snippet + "\"";
        new Handler(Looper.getMainLooper()).post(() -> callback.onError(finalMsg));
    }

    public interface OrderCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    public void createOrder(String orderId, String amount, String customerId, String phoneNumber, String customerName, String customerEmail, OrderCallback callback) {
        if (Config.USE_DIRECT_CASHFREE) {
            createOrderDirect(orderId, amount, customerId, phoneNumber, customerName, customerEmail, callback);
        } else {
            createOrderBackend(orderId, amount, customerId, phoneNumber, customerName, customerEmail, callback);
        }
    }

    private void createOrderBackend(String orderId, String amount, String customerId, String phoneNumber, String customerName, String customerEmail, OrderCallback callback) {
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

                    if (!isLikelyJsonResponse(response, responseBody)) {
                        handleNonJsonError(response, responseBody, callback);
                        return;
                    }

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

    private void createOrderDirect(String orderId, String amount, String customerId, String phoneNumber, String customerName, String customerEmail, OrderCallback callback) {
        OkHttpClient client = buildClient();

        try {
            double orderAmount;
            try { orderAmount = Double.parseDouble(amount); } catch (Exception e) { orderAmount = 0d; }

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("order_id", orderId);
            orderRequest.put("order_amount", orderAmount);
            orderRequest.put("order_currency", Config.CURRENCY);

            JSONObject customerDetails = new JSONObject();
            customerDetails.put("customer_id", customerId);
            customerDetails.put("customer_name", customerName);
            customerDetails.put("customer_email", customerEmail);
            customerDetails.put("customer_phone", phoneNumber);
            orderRequest.put("customer_details", customerDetails);

            Log.d(TAG, "Creating order DIRECT: " + orderRequest);

            RequestBody body = RequestBody.create(JSON, orderRequest.toString());
            Request request = new Request.Builder()
                    .url(Config.CASHFREE_ORDERS_URL)
                    .addHeader("x-client-id", Config.CASHFREE_CLIENT_ID)
                    .addHeader("x-client-secret", Config.CASHFREE_CLIENT_SECRET)
                    .addHeader("x-api-version", Config.CASHFREE_API_VERSION)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Network error creating order (direct)", e);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError("Network Error: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    int code = response.code();
                    Log.d(TAG, "CF /orders code=" + code + ", body(first 600)=" + (responseBody.length()>600?responseBody.substring(0,600)+"...":responseBody));

                    if (!isLikelyJsonResponse(response, responseBody)) {
                        handleNonJsonError(response, responseBody, callback);
                        return;
                    }

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

    public void checkOrderStatus(String orderId, OrderCallback callback) {
        if (Config.USE_DIRECT_CASHFREE) {
            checkOrderStatusDirect(orderId, callback);
            return;
        }
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

                if (!isLikelyJsonResponse(response, responseBody)) {
                    handleNonJsonError(response, responseBody, callback);
                    return;
                }

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

    private void checkOrderStatusDirect(String orderId, OrderCallback callback) {
        OkHttpClient client = buildClient();
        String url = Config.CASHFREE_ORDERS_URL + "/" + orderId;
        Log.d(TAG, "CF order-status DIRECT: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("x-client-id", Config.CASHFREE_CLIENT_ID)
                .addHeader("x-client-secret", Config.CASHFREE_CLIENT_SECRET)
                .addHeader("x-api-version", Config.CASHFREE_API_VERSION)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network error checking order status (direct)", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Network Error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                int code = response.code();
                Log.d(TAG, "CF /orders/{id} code=" + code + ", body(first 600)=" + (responseBody.length()>600?responseBody.substring(0,600)+"...":responseBody));

                if (!isLikelyJsonResponse(response, responseBody)) {
                    handleNonJsonError(response, responseBody, callback);
                    return;
                }

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
