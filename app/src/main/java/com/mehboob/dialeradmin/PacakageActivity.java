package com.mehboob.dialeradmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mehboob.dialeradmin.payment.CashfreePaymentHelper;
import com.mehboob.dialeradmin.payment.CheckOrderStatusApiClient;
import com.mehboob.dialeradmin.payment.OrderApiClient;

import org.json.JSONException;
import org.json.JSONObject;

public class PacakageActivity extends AppCompatActivity {

    private Button btnSubscribe;
    private String selectedPlan = null; // store which plan is selected

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pacakage);

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
            if (v == btn1) selectedPlan = "yearly";
            else if (v == btn2) selectedPlan = "monthly";
            else if (v == btn3) selectedPlan = "weekly";
            else if (v == btn4) selectedPlan = "3months";
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
                
                startUPIIntentCheckout();
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

    public void startUPIIntentCheckout() {
        startActivity(new Intent(this, UPIIntentActivity.class));
    }
    
    private void activatePlan(String planType) {
        long now = System.currentTimeMillis();
        long expiry;

        switch (planType) {
            case "yearly":
                expiry = now + (365L * 24 * 60 * 60 * 1000);
                break;
            case "monthly":
                expiry = now + (30L * 24 * 60 * 60 * 1000);
                break;
            case "weekly":
                expiry = now + (7L * 24 * 60 * 60 * 1000);
                break;
            case "3months":
                expiry = now + (90L * 24 * 60 * 60 * 1000);
                break;
            default:
                expiry = now;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("admins")
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

        Toast.makeText(this, "Plan activated: " + planType, Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private String getAmountForPlan(String planType) {
        switch (planType) {
            case "yearly": return "2499";    // 2499 INR
            case "monthly": return "399";    // 399 INR
            case "weekly": return "149";     // 149 INR
            case "3months": return "999";    // 999 INR
            default: return "0";
        }
    }

    private void verifyOrderThenActivate(String planType, String orderId) {
        new CheckOrderStatusApiClient().checkStatus(orderId, new CheckOrderStatusApiClient.StatusCallback() {
            @Override public void onSuccess(String orderStatus) {
                if ("PAID".equalsIgnoreCase(orderStatus)) {
                    activatePlan(planType);
                } else {
                    Toast.makeText(PacakageActivity.this,
                            "Payment not confirmed: " + orderStatus, Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onError(String error) {
                Toast.makeText(PacakageActivity.this,
                        "Error checking status: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOrderForPlan(String planType) {
        String amount = getAmountForPlan(planType);
        String orderId = String.valueOf(System.currentTimeMillis());

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String phoneNumber = MyApplication.getInstance().getCurrentAdmin() != null ? 
                           MyApplication.getInstance().getCurrentAdmin().getPhoneNumber() : "";

        OrderApiClient client = new OrderApiClient();
        client.createOrder(orderId, amount, userId, phoneNumber, new OrderApiClient.OrderCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String paymentSessionId = response.getString("payment_session_id");
                    CashfreePaymentHelper.startPayment(PacakageActivity.this, orderId, paymentSessionId);
                } catch (Exception e) {
                    Toast.makeText(PacakageActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PacakageActivity.this, "Order create failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getExtras() != null) {
            String txStatus = data.getStringExtra("txStatus");
            String orderId = data.getStringExtra("orderId");

            if ("SUCCESS".equalsIgnoreCase(txStatus)) {
                verifyOrderThenActivate(selectedPlan, orderId);
            } else {
                Toast.makeText(this, "Payment Failed: " + txStatus, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
