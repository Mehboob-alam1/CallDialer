package com.mehboob.dialeradmin.payment;

import android.app.Activity;
import android.util.Log;

import com.cashfree.pg.api.CFPaymentGatewayService;
import com.cashfree.pg.api.CFCheckoutResponseCallback;
import com.cashfree.pg.core.api.CFSession;
import com.cashfree.pg.core.api.exception.CFException;
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutPayment;
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutTheme;
import com.cashfree.pg.core.api.upiintent.CFUPIIntentCheckout;
import com.cashfree.pg.core.api.upiintent.CFUPIIntentCheckoutPayment;
import com.cashfree.pg.core.api.upiintent.CFIntentTheme;
import com.cashfree.pg.core.api.callback.CFErrorResponse;

import java.util.Arrays;

public class CashfreePaymentService {
    private static final String TAG = "CashfreePaymentService";
    
    // Environment - Change to PRODUCTION for live payments
    private static final CFSession.Environment ENVIRONMENT = CFSession.Environment.SANDBOX;
    
    public interface PaymentCallback {
        void onPaymentSuccess(String orderId);
        void onPaymentFailure(String errorMessage, String orderId);
    }

    /**
     * Initialize Cashfree SDK
     */
    public static void initialize(Activity activity) {
        try {
            CFPaymentGatewayService.getInstance().setCheckoutCallback((CFCheckoutResponseCallback) activity);
        } catch (CFException e) {
            Log.e(TAG, "Error initializing Cashfree SDK", e);
        }
    }

    /**
     * Start Web Checkout Payment
     */
    public static void startWebCheckout(Activity activity, String orderId, String paymentSessionId, PaymentCallback callback) {
        try {
            // Create session
            CFSession cfSession = new CFSession.CFSessionBuilder()
                    .setEnvironment(ENVIRONMENT)
                    .setOrderId(orderId)
                    .setPaymentSessionID(paymentSessionId)
                    .build();

            // Create theme
            CFWebCheckoutTheme cfTheme = new CFWebCheckoutTheme.CFWebCheckoutThemeBuilder()
                    .setNavigationBarBackgroundColor("#0047AB")
                    .setNavigationBarTextColor("#FFFFFF")
                    .build();

            // Create payment object
            CFWebCheckoutPayment cfWebCheckoutPayment = new CFWebCheckoutPayment.CFWebCheckoutPaymentBuilder()
                    .setSession(cfSession)
                    .setCFWebCheckoutUITheme(cfTheme)
                    .build();

            // Start payment
            CFPaymentGatewayService.getInstance().doPayment(activity, cfWebCheckoutPayment);
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting web checkout", e);
            callback.onPaymentFailure("Error: " + e.getMessage(), orderId);
        }
    }

    /**
     * Start UPI Intent Checkout Payment
     */
    public static void startUPIIntentCheckout(Activity activity, String orderId, String paymentSessionId, PaymentCallback callback) {
        try {
            // Create session
            CFSession cfSession = new CFSession.CFSessionBuilder()
                    .setEnvironment(ENVIRONMENT)
                    .setOrderId(orderId)
                    .setPaymentSessionID(paymentSessionId)
                    .build();

            // Create UPI checkout
            CFUPIIntentCheckout cfupiIntentCheckout = new CFUPIIntentCheckout.CFUPIIntentBuilder()
                    .setOrder(Arrays.asList(
                            CFUPIIntentCheckout.CFUPIApps.BHIM,
                            CFUPIIntentCheckout.CFUPIApps.PHONEPE,
                            CFUPIIntentCheckout.CFUPIApps.GPAY,
                            CFUPIIntentCheckout.CFUPIApps.PAYTM
                    ))
                    .build();

            // Create theme
            CFIntentTheme cfTheme = new CFIntentTheme.CFIntentThemeBuilder()
                    .setPrimaryTextColor("#000000")
                    .setBackgroundColor("#FFFFFF")
                    .build();

            // Create payment object
            CFUPIIntentCheckoutPayment cfupiIntentCheckoutPayment = new CFUPIIntentCheckoutPayment.CFUPIIntentPaymentBuilder()
                    .setSession(cfSession)
                    .setCfUPIIntentCheckout(cfupiIntentCheckout)
                    .setCfIntentTheme(cfTheme)
                    .build();

            // Start payment
            CFPaymentGatewayService.getInstance().doPayment(activity, cfupiIntentCheckoutPayment);
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting UPI intent checkout", e);
            callback.onPaymentFailure("Error: " + e.getMessage(), orderId);
        }
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
    public static void onPaymentFailure(CFErrorResponse cfErrorResponse, String orderId, PaymentCallback callback) {
        String errorMessage = "Payment failed";
        if (cfErrorResponse != null) {
            errorMessage = cfErrorResponse.getMessage();
        }
        Log.e(TAG, "Payment failed for order: " + orderId + ", Error: " + errorMessage);
        callback.onPaymentFailure(errorMessage, orderId);
    }

    /**
     * Get environment string for API calls
     */
    public static String getEnvironmentString() {
        return ENVIRONMENT == CFSession.Environment.PRODUCTION ? "PRODUCTION" : "SANDBOX";
    }
}
