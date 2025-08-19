package com.mehboob.dialeradmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.cashfree.pg.api.CFPaymentGatewayService;
import com.cashfree.pg.core.api.CFSession;
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback;
import com.cashfree.pg.core.api.utils.CFErrorResponse;
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutPayment;
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutTheme;
import com.cashfree.pg.core.api.exception.CFException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mehboob.dialeradmin.payment.OrderApiClient;

import org.json.JSONException;
import org.json.JSONObject;

public class PacakageActivity extends AppCompatActivity implements CFCheckoutResponseCallback {

    private static final String TAG = "PacakageActivity";

    private Button btnSubscribe;
    private String selectedPlan = null; // store which plan is selected

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pacakage);

        try {
            // Per docs, set checkout callback in onCreate
            CFPaymentGatewayService.getInstance().setCheckoutCallback(this);
        } catch (CFException e) {
            Log.e(TAG, "Failed to set checkout callback", e);
        }

        LinearLayout btn1 = findViewById(R.id.l1); // yearly
        LinearLayout btn2 = findViewById(R.id.l2); // monthly
        LinearLayout btn3 = findViewById(R.id.l3); // weekly
        LinearLayout btn4 = findViewById(R.id.l4); // 3months
        btnSubscribe = findViewById(R.id.btnSubscribe);
        
        String plan = MyApplication.getInstance().getActivePlanName();
        if (plan != null && !plan.isEmpty()) {
            Toast.makeText(this, "Current Plan: " + plan, Toast.LENGTH_SHORT).show();
        }

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
        String amount = getAmountForPlan(planType);
        String orderId = "order_" + System.currentTimeMillis();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String phoneNumber = MyApplication.getInstance().getCurrentAdmin() != null ? 
                           MyApplication.getInstance().getCurrentAdmin().getPhoneNumber() : "";
        String customerName = MyApplication.getInstance().getCurrentAdmin() != null ?
                MyApplication.getInstance().getCurrentAdmin().getName() : null;
        String customerEmail = MyApplication.getInstance().getCurrentAdmin() != null ?
                MyApplication.getInstance().getCurrentAdmin().getEmail() : null;

        Toast.makeText(this, "Creating payment order...", Toast.LENGTH_SHORT).show();

        OrderApiClient client = new OrderApiClient();
        client.createOrder(orderId, amount, userId, phoneNumber, customerName, customerEmail, new OrderApiClient.OrderCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String paymentSessionId = response.optString("payment_session_id", null);
                    Log.d(TAG, "Order created: orderId=" + orderId + ", session=" + paymentSessionId);

                    if (paymentSessionId != null && !paymentSessionId.isEmpty()) {
                        startSdkCheckout(orderId, paymentSessionId);
                    } else {
                        Log.e(TAG, "payment_session_id missing in backend response");
                        Toast.makeText(PacakageActivity.this, "Payment details missing in response.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling order create success", e);
                    Toast.makeText(PacakageActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Order creation failed: " + error);
                Toast.makeText(PacakageActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startSdkCheckout(String orderId, String paymentSessionId) {
        try {
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

            Log.d(TAG, "Starting SDK Web Checkout");
            CFPaymentGatewayService.getInstance().doPayment(this, cfWebCheckoutPayment);
        } catch (Exception e) {
            Log.e(TAG, "Error starting SDK checkout", e);
            Toast.makeText(this, "Failed to open payment UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
        ref.child("planType").setValue(selectedPlan);
        ref.child("planActivatedAt").setValue(now);
        ref.child("planExpiryAt").setValue(expiry);

        if (MyApplication.getInstance().getCurrentAdmin() != null) {
            MyApplication.getInstance().getCurrentAdmin().setIsPremium(true);
            MyApplication.getInstance().getCurrentAdmin().setPlanType(selectedPlan);
            MyApplication.getInstance().getCurrentAdmin().setPlanActivatedAt(now);
            MyApplication.getInstance().getCurrentAdmin().setPlanExpiryAt(expiry);
        }

        Toast.makeText(this, "Plan activated: " + selectedPlan, Toast.LENGTH_SHORT).show();

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

    // CFCheckoutResponseCallback
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
    }
}
