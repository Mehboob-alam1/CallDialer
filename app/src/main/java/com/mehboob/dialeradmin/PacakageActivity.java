package com.mehboob.dialeradmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mehboob.dialeradmin.payment.CashfreePaymentService;
import com.mehboob.dialeradmin.payment.OrderApiClient;
import com.cashfree.pg.core.api.exception.CFException;
import com.cashfree.pg.api.CFCheckoutResponseCallback;
import com.cashfree.pg.core.api.exception.CFErrorResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class PacakageActivity extends AppCompatActivity implements CFCheckoutResponseCallback {

    private static final String TAG = "PacakageActivity";
    private static final String PREFS = "payment_prefs";
    private static final String KEY_PENDING_ORDER = "pending_order_id";
    private static final String KEY_PENDING_PLAN = "pending_plan";

    private Button btnSubscribe;
    private String selectedPlan = null; // store which plan is selected
    private CashfreePaymentService.PaymentCallback paymentCallback;

    // Checkout watchdog
    private boolean activityPaused = false;
    private String lastSessionId = null;
    private String lastOrderId = null;
    private String lastPaymentLink = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pacakage);

        // Initialize Cashfree SDK
        CashfreePaymentService.initialize(this);

        LinearLayout btn1 = findViewById(R.id.l1); // yearly
        LinearLayout btn2 = findViewById(R.id.l2); // monthly
        LinearLayout btn3 = findViewById(R.id.l3); // weekly
        LinearLayout btn4 = findViewById(R.id.l4); // 3months
        btnSubscribe = findViewById(R.id.btnSubscribe);
        
        String plan = MyApplication.getInstance().getActivePlanName();
        if (plan != null && !plan.isEmpty()) {
            Toast.makeText(this, "Current Plan: " + plan, Toast.LENGTH_SHORT).show();
        }

        // Show configuration status
        Toast.makeText(this, Config.getConfigStatus(), Toast.LENGTH_LONG).show();

        LinearLayout[] buttons = {btn1, btn2, btn3, btn4};
        if (MyApplication.getInstance().isPremiumActive()){
            showPremiumActiveDialog();
            return;
        }
        
        // Selection logic
        View.OnClickListener singleSelectListener = v -> {

            for (LinearLayout btn : buttons) {
                btn.setSelected(btn == v);
            }

            // set selected plan name
            if (v == btn1) selectedPlan = Config.PLAN_YEARLY;
            else if (v == btn2) selectedPlan = Config.PLAN_MONTHLY;
            else if (v == btn3) selectedPlan = Config.PLAN_WEEKLY;
            else if (v == btn4) selectedPlan = Config.PLAN_3MONTHS;

            Log.d(TAG, "Selected plan: " + selectedPlan);
        };

        btn1.setOnClickListener(singleSelectListener);
        btn2.setOnClickListener(singleSelectListener);
        btn3.setOnClickListener(singleSelectListener);
        btn4.setOnClickListener(singleSelectListener);

        // Subscribe button click
        btnSubscribe.setOnClickListener(v -> {
            if (selectedPlan == null) {
                Toast.makeText(this, "Please select a plan first", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check if admin has phone number
            if (MyApplication.getInstance().getCurrentAdmin() != null && 
                MyApplication.getInstance().getCurrentAdmin().getPhoneNumber() != null &&
                !MyApplication.getInstance().getCurrentAdmin().getPhoneNumber().isEmpty()) {
                
                Log.d(TAG, "Creating order for plan: " + selectedPlan);
                createOrderForPlan(selectedPlan);
            } else {
                Toast.makeText(this, "Phone number is required for payment. Please update your profile.", Toast.LENGTH_LONG).show();
            }
        });

        // Setup payment callback
        setupPaymentCallback();

        try {
            // Per docs, set checkout callback in onCreate
            com.cashfree.pg.api.CFPaymentGatewayService.getInstance().setCheckoutCallback(this);
        } catch (CFException e) {
            Log.e(TAG, "Failed to set checkout callback", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused = true; // if checkout UI starts, we'll be paused shortly after doPayment
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityPaused = false;
        // Verify any pending order if user returned from checkout
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String pendingOrder = sp.getString(KEY_PENDING_ORDER, null);
        String pendingPlan = sp.getString(KEY_PENDING_PLAN, null);
        if (pendingOrder != null && pendingPlan != null) {
            Log.d(TAG, "Found pending order onResume: " + pendingOrder + ", verifying...");
            verifyOrderThenActivate(pendingPlan, pendingOrder);
        }
    }

    private void setupPaymentCallback() {
        paymentCallback = new CashfreePaymentService.PaymentCallback() {
            @Override
            public void onPaymentSuccess(String orderId) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Payment successful (callback). Verifying order: " + orderId);
                    Toast.makeText(PacakageActivity.this, "Payment successful!", Toast.LENGTH_SHORT).show();
                    verifyOrderThenActivate(selectedPlan, orderId);
                });
            }

            @Override
            public void onPaymentFailure(String errorMessage, String orderId) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Payment failed for order " + orderId + ": " + errorMessage);
                    Toast.makeText(PacakageActivity.this, "Payment failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    clearPendingOrder();
                });
            }
        };
    }

    private void showPremiumActiveDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Premium Active")
                .setMessage("You already have an active premium plan. Your current plan: " + 
                           MyApplication.getInstance().getActivePlanName())
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void createOrderForPlan(String planType) {
        // First check if Cashfree is configured
        if (!Config.isCashfreeConfigured()) {
            Toast.makeText(this, Config.getConfigStatus(), Toast.LENGTH_LONG).show();
            return;
        }
        
        String amount = getAmountForPlan(planType);
        String orderId = "order_" + System.currentTimeMillis();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String phoneNumber = MyApplication.getInstance().getCurrentAdmin() != null ? 
                           MyApplication.getInstance().getCurrentAdmin().getPhoneNumber() : "";
        String customerName = MyApplication.getInstance().getCurrentAdmin() != null ?
                MyApplication.getInstance().getCurrentAdmin().getName() : null;
        String customerEmail = MyApplication.getInstance().getCurrentAdmin() != null ?
                MyApplication.getInstance().getCurrentAdmin().getEmail() : null;

        // Show loading message
        Toast.makeText(this, "Creating payment order...", Toast.LENGTH_SHORT).show();

        OrderApiClient client = new OrderApiClient();
        client.createOrder(orderId, amount, userId, phoneNumber, customerName, customerEmail, new OrderApiClient.OrderCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String paymentSessionId = response.optString("payment_session_id", null);
                    String paymentLink = response.optString("payment_link", null);
                    Log.d(TAG, "Order created: orderId=" + orderId + ", session=" + paymentSessionId + ", link=" + paymentLink);
                    savePendingOrder(orderId, planType);

                    lastSessionId = paymentSessionId;
                    lastOrderId = orderId;
                    lastPaymentLink = paymentLink;

                    if (paymentSessionId != null && !paymentSessionId.isEmpty()) {
                        Log.d(TAG, "Starting SDK Web Checkout now");
                        CashfreePaymentService.startWebCheckout(
                                PacakageActivity.this,
                                orderId,
                                paymentSessionId,
                                paymentCallback
                        );

                        // Watchdog: if activity didn't pause within 2s, fallback to browser
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (!activityPaused) {
                                Log.w(TAG, "SDK Web Checkout likely didn't open; falling back to browser");
                                if (lastPaymentLink != null && !lastPaymentLink.isEmpty()) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(lastPaymentLink)));
                                } else {
                                    // construct from session id
                                    String base = Config.IS_PRODUCTION ? "https://www.cashfree.com/pg/checkout/" : "https://sandbox.cashfree.com/pg/checkout/";
                                    String url = base + lastSessionId;
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                }
                            }
                        }, 2000);
                    } else if (paymentLink != null && !paymentLink.isEmpty()) {
                        Log.d(TAG, "No session id. Opening payment_link in browser: " + paymentLink);
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(paymentLink)));
                    } else {
                        Log.e(TAG, "Neither payment_session_id nor payment_link present in response");
                        Toast.makeText(PacakageActivity.this, "Payment details missing in response.", Toast.LENGTH_LONG).show();
                        clearPendingOrder();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling order create success", e);
                    Toast.makeText(PacakageActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Order creation failed: " + error);
                // Show detailed error message
                String errorMsg = "Order creation failed: " + error;
                if (error.contains("401")) {
                    errorMsg = "Authentication failed. Please check your Cashfree credentials.";
                } else if (error.contains("400")) {
                    errorMsg = "Invalid request. Please check the order details.";
                } else if (error.contains("403")) {
                    errorMsg = "Access denied. Please check your Cashfree account permissions.";
                } else if (error.contains("Non-JSON response")) {
                    errorMsg = error + ". If on PRODUCTION, ensure your return_url is a valid HTTPS domain and PG is activated.";
                } else if (error.contains("Network Error")) {
                    errorMsg = "Network error. Please check your internet connection.";
                }
                Toast.makeText(PacakageActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                clearPendingOrder();
            }
        });
    }

    private void showPaymentInstructions(String paymentSessionId, String orderId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Payment Instructions")
                .setMessage("Your payment session has been created. Click 'Pay Now' to complete the payment.")
                .setPositiveButton("Pay Now", (dialog, which) -> {
                    Log.d(TAG, "Launching Web Checkout for order " + orderId);
                    // Start web checkout
                    CashfreePaymentService.startWebCheckout(
                            PacakageActivity.this, 
                            orderId, 
                            paymentSessionId, 
                            paymentCallback
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void activatePlan(String planType) {
        long now = System.currentTimeMillis();
        long expiry;

        switch (planType) {
            case Config.PLAN_YEARLY:
                expiry = now + (365L * 24 * 60 * 60 * 1000);
                break;
            case Config.PLAN_MONTHLY:
                expiry = now + (30L * 24 * 60 * 60 * 1000);
                break;
            case Config.PLAN_WEEKLY:
                expiry = now + (7L * 24 * 60 * 60 * 1000);
                break;
            case Config.PLAN_3MONTHS:
                expiry = now + (90L * 24 * 60 * 60 * 1000);
                break;
            default:
                expiry = now;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_ADMINS_NODE)
                .child(userId);

        ref.child("isPremium").setValue(true);
        ref.child("planType").setValue(planType);
        ref.child("planActivatedAt").setValue(now);
        ref.child("planExpiryAt").setValue(expiry);

        // Update local admin model
        if (MyApplication.getInstance().getCurrentAdmin() != null) {
            MyApplication.getInstance().getCurrentAdmin().setIsPremium(true);
            MyApplication.getInstance().getCurrentAdmin().setPlanType(planType);
            MyApplication.getInstance().getCurrentAdmin().setPlanActivatedAt(now);
            MyApplication.getInstance().getCurrentAdmin().setPlanExpiryAt(expiry);
        }

        clearPendingOrder();
        Toast.makeText(this, "Plan activated: " + planType, Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private String getAmountForPlan(String planType) {
        switch (planType) {
            case Config.PLAN_YEARLY: return Config.AMOUNT_YEARLY;
            case Config.PLAN_MONTHLY: return Config.AMOUNT_MONTHLY;
            case Config.PLAN_WEEKLY: return Config.AMOUNT_WEEKLY;
            case Config.PLAN_3MONTHS: return Config.AMOUNT_3MONTHS;
            default: return "0";
        }
    }

    private void savePendingOrder(String orderId, String planType) {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        sp.edit()
                .putString(KEY_PENDING_ORDER, orderId)
                .putString(KEY_PENDING_PLAN, planType)
                .apply();
        Log.d(TAG, "Saved pending order " + orderId + " for plan " + planType);
    }

    private void clearPendingOrder() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        sp.edit()
                .remove(KEY_PENDING_ORDER)
                .remove(KEY_PENDING_PLAN)
                .apply();
        Log.d(TAG, "Cleared pending order state");
    }

    private void verifyOrderThenActivate(String planType, String orderId) {
        Log.d(TAG, "Verifying order status for " + orderId);
        OrderApiClient client = new OrderApiClient();
        client.checkOrderStatus(orderId, new OrderApiClient.OrderCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String orderStatus = response.getString("order_status");
                    Log.d(TAG, "Order status for " + orderId + ": " + orderStatus);
                    if ("PAID".equalsIgnoreCase(orderStatus)) {
                        activatePlan(planType);
                    } else {
                        Toast.makeText(PacakageActivity.this,
                                "Payment not confirmed: " + orderStatus, Toast.LENGTH_SHORT).show();
                        clearPendingOrder();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error reading order_status JSON", e);
                    Toast.makeText(PacakageActivity.this,
                            "Error checking payment status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error checking status: " + error);
                Toast.makeText(PacakageActivity.this,
                        "Error checking status: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Handle payment result if needed
        if (data != null && data.getExtras() != null) {
            String txStatus = data.getStringExtra("txStatus");
            String orderId = data.getStringExtra("orderId");
            Log.d(TAG, "onActivityResult: txStatus=" + txStatus + ", orderId=" + orderId);

            if ("SUCCESS".equalsIgnoreCase(txStatus)) {
                verifyOrderThenActivate(selectedPlan, orderId);
            } else {
                Toast.makeText(this, "Payment Failed: " + txStatus, Toast.LENGTH_SHORT).show();
                clearPendingOrder();
            }
        }
    }

    @Override
    public void onPaymentVerify(String orderID) {
        Log.d(TAG, "CF SDK callback onPaymentVerify for order=" + orderID);
        verifyOrderThenActivate(selectedPlan, orderID);
    }

    @Override
    public void onPaymentFailure(CFErrorResponse cfErrorResponse, String orderID) {
        String err = cfErrorResponse != null ? cfErrorResponse.getMessage() : "Unknown error";
        Log.e(TAG, "CF SDK callback onPaymentFailure order=" + orderID + ": " + err);
        Toast.makeText(this, "Payment failed: " + err, Toast.LENGTH_SHORT).show();
        clearPendingOrder();
    }
}
