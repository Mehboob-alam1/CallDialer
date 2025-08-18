package com.mehboob.dialeradmin.payment;

import android.app.Activity;
import android.widget.Toast;

import com.cashfree.pg.api.CFPaymentGatewayService;
import com.cashfree.pg.core.api.CFSession;
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback;
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutPayment;
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutTheme;

public class CashfreePaymentHelper {

    public static void startPayment(Activity activity, String orderId, String paymentSessionId) {
        try {
            CFSession cfSession = new CFSession.CFSessionBuilder()
                    .setEnvironment(CFSession.Environment.SANDBOX) // switch to PRODUCTION for live
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

            // Optionally set a callback in your Activity:
            if (activity instanceof CFCheckoutResponseCallback) {
                CFPaymentGatewayService.getInstance().setCheckoutCallback((CFCheckoutResponseCallback) activity);
            }

            CFPaymentGatewayService.getInstance().doPayment(activity, cfWebCheckoutPayment);
        } catch (Exception e) {
            Toast.makeText(activity, "Error initiating payment: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
