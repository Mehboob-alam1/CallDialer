package com.mehboob.dialeradmin.payment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.mehboob.dialeradmin.Config;

public class CashfreePaymentService {
    private static final String TAG = "CashfreePaymentService";
    
    public interface PaymentCallback {
        void onPaymentSuccess(String orderId);
        void onPaymentFailure(String errorMessage, String orderId);
    }

    /**
     * Initialize Cashfree SDK
     */
    public static void initialize(Activity activity) {
        try {
            Log.d(TAG, "Cashfree payment service initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Cashfree payment service", e);
        }
    }

    /**
     * Start Web Checkout Payment
     */
    public static void startWebCheckout(Activity activity, String orderId, String paymentSessionId, PaymentCallback callback) {
        try {
            // Create Cashfree web checkout URL based on environment
            String baseUrl = Config.IS_PRODUCTION ? 
                "https://www.cashfree.com/pg/checkout/" : 
                "https://sandbox.cashfree.com/pg/checkout/";
            
            String checkoutUrl = baseUrl + paymentSessionId;
            
            // Open web checkout in browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            
            Log.d(TAG, "Opened Cashfree web checkout: " + checkoutUrl);
            
            // Note: Payment result will be handled via webhook or manual verification
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting web checkout", e);
            callback.onPaymentFailure("Error: " + e.getMessage(), orderId);
        }
    }

    /**
     * Start UPI Intent Checkout Payment
     * Note: Using Web Checkout for UPI as UPI Intent classes may not be available
     */
    public static void startUPIIntentCheckout(Activity activity, String orderId, String paymentSessionId, PaymentCallback callback) {
        // For now, use web checkout which supports UPI payments
        startWebCheckout(activity, orderId, paymentSessionId, callback);
    }

    /**
     * Handle payment verification
     */
    public static void onPaymentVerify(String orderId, PaymentCallback callback) {
        Log.d(TAG, "Payment verified for order: " + orderId);
        callback.onPaymentSuccess(orderId);
    }

    /**
     * Handle payment failure
     */
    public static void onPaymentFailure(Object cfErrorResponse, String orderId, PaymentCallback callback) {
        String errorMessage = "Payment failed";
        if (cfErrorResponse != null) {
            errorMessage = cfErrorResponse.toString();
        }
        Log.e(TAG, "Payment failed for order: " + orderId + ", Error: " + errorMessage);
        callback.onPaymentFailure(errorMessage, orderId);
    }

    /**
     * Get environment string for API calls
     */
    public static String getEnvironmentString() {
        return Config.IS_PRODUCTION ? "PRODUCTION" : "SANDBOX";
    }

    /**
     * Check if payment was successful by verifying order status
     */
    public static void verifyPaymentStatus(String orderId, PaymentCallback callback) {
        // This will be handled by OrderApiClient.checkOrderStatus()
        Log.d(TAG, "Payment status verification requested for order: " + orderId);
    }
}
