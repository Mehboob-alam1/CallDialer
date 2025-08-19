package com.mehboob.dialeradmin.payment;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.cashfree.pg.api.CFPaymentGatewayService;
import com.cashfree.pg.core.api.CFSession;
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutPayment;
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutTheme;
import com.mehboob.dialeradmin.Config;

public class CashfreePaymentService {
    private static final String TAG = "CashfreePaymentService";
    
    public interface PaymentCallback {
        void onPaymentSuccess(String orderId);
        void onPaymentFailure(String errorMessage, String orderId);
    }

    public static void initialize(Activity activity) {
        try {
            CFPaymentGatewayService.getInstance();
            Log.d(TAG, "Cashfree SDK initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Cashfree SDK", e);
        }
    }

    public static void startWebCheckout(Activity activity, String orderId, String paymentSessionId, PaymentCallback callback) {
        try {
            if (paymentSessionId == null || paymentSessionId.trim().isEmpty()) {
                Log.e(TAG, "Payment session id is empty; cannot start checkout");
                Toast.makeText(activity, "Payment session is missing.", Toast.LENGTH_LONG).show();
                if (callback != null) callback.onPaymentFailure("Missing payment session", orderId);
                return;
            }

            CFSession.Environment env = Config.IS_PRODUCTION ? CFSession.Environment.PRODUCTION : CFSession.Environment.SANDBOX;

            CFSession cfSession = new CFSession.CFSessionBuilder()
                    .setEnvironment(env)
                    .setOrderId(orderId)
                    .setPaymentSessionID(paymentSessionId)
                    .build();

            CFWebCheckoutTheme cfTheme = new CFWebCheckoutTheme.CFWebCheckoutThemeBuilder()
                    .setNavigationBarBackgroundColor("#0047AB")
                    .setNavigationBarTextColor("#FFFFFF")
                    .build();

            CFWebCheckoutPayment cfWebCheckoutPayment = new CFWebCheckoutPayment.CFWebCheckoutPaymentBuilder()
                    .setSession(cfSession)
                    .setCFWebCheckoutUITheme(cfTheme)
                    .build();

            Log.d(TAG, "Starting Web Checkout: orderId=" + orderId + ", env=" + (Config.IS_PRODUCTION ? "PROD" : "SANDBOX") + ", sessionId=" + paymentSessionId);
            Toast.makeText(activity, "Opening payment UI...", Toast.LENGTH_SHORT).show();
            CFPaymentGatewayService.getInstance().doPayment(activity, cfWebCheckoutPayment);

        } catch (Exception e) {
            Log.e(TAG, "Error starting web checkout", e);
            Toast.makeText(activity, "Failed to open payment UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (callback != null) {
                callback.onPaymentFailure("Error: " + e.getMessage(), orderId);
            }
        }
    }

    public static void startUPIIntentCheckout(Activity activity, String orderId, String paymentSessionId, PaymentCallback callback) {
        // Not using UPI intent classes to avoid missing class issues; route to Web Checkout.
        startWebCheckout(activity, orderId, paymentSessionId, callback);
    }

    public static String getEnvironmentString() {
        return Config.IS_PRODUCTION ? "PRODUCTION" : "SANDBOX";
    }
}
