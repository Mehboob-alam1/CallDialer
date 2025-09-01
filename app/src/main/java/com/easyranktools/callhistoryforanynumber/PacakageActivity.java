package com.easyranktools.callhistoryforanynumber;

import static android.view.View.GONE;

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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cashfree.pg.api.CFPaymentGatewayService;
import com.cashfree.pg.core.api.CFSession;
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback;
import com.cashfree.pg.core.api.exception.CFException;
import com.cashfree.pg.core.api.utils.CFErrorResponse;
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutPayment;
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutTheme;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.easyranktools.callhistoryforanynumber.payment.OrderApiClient;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Orders-only plans screen with:
 * - Active plan highlighting
 * - Smart Subscribe button states (Already Subscribed / enabled)
 * - Upgrade button when user selects a higher-priced plan
 */
public class PacakageActivity extends AppCompatActivity implements CFCheckoutResponseCallback {

    private static final String TAG = "PacakageActivity";

    private Button btnSubscribe;
    private Button btnUpgrade;
    private Button btnRestore;

    private LinearLayout cardYearly, cardMonthly, cardWeekly, card3Months;

    private String selectedPlan = null;
    private String lastOrderId = null;

    // cached state for quick decisions
    private boolean hasActivePlan;
    private boolean isExpired;
    private String  currentPlan;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pacakage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        try {
            CFPaymentGatewayService.getInstance().setCheckoutCallback(this);
        } catch (CFException e) {
            Log.e(TAG, "Failed to set checkout callback", e);
        }

        // ---- find views
        cardYearly   = findViewById(R.id.l1);
        cardMonthly  = findViewById(R.id.l2);
        cardWeekly   = findViewById(R.id.l3);
        card3Months  = findViewById(R.id.l4);

        btnSubscribe = findViewById(R.id.btnSubscribe);
        btnUpgrade   = findViewById(R.id.btnUpgrade);
        btnRestore   = findViewById(R.id.btnRestore);

        // ---- initial state from MyApplication
        hasActivePlan = MyApplication.getInstance().hasActivePlan();
        isExpired     = MyApplication.getInstance().isPlanExpired();
        currentPlan   = MyApplication.getInstance().getCurrentPlanType();

        // If there is a non-expired plan, highlight it; preselect it
        if (hasActivePlan && !isExpired && currentPlan != null && !currentPlan.isEmpty()) {
            selectedPlan = normalize(currentPlan);
            highlightCards(selectedPlan, /*activePlan*/ normalize(currentPlan));
        } else {
            // default selection: none (user taps to select)
            highlightCards(/*selected*/ null, /*active*/ normalize(currentPlan));
        }

        // Upgrade button only visible when user has some active plan
        btnUpgrade.setVisibility(hasActivePlan && !isExpired ? View.VISIBLE : GONE);
        btnRestore.setVisibility(View.VISIBLE);

        // apply initial CTA states
        refreshCtas();

        // ---- plan card clicks
        View.OnClickListener select = v -> {
            if (v == cardYearly)    selectedPlan = Config.PLAN_YEARLY;
            if (v == cardMonthly)   selectedPlan = Config.PLAN_MONTHLY;
            if (v == cardWeekly)    selectedPlan = Config.PLAN_WEEKLY;
            if (v == card3Months)   selectedPlan = Config.PLAN_3MONTHS;

            highlightCards(selectedPlan, normalize(currentPlan));
            refreshCtas();
        };

        cardYearly.setOnClickListener(select);
        cardMonthly.setOnClickListener(select);
        cardWeekly.setOnClickListener(select);
        card3Months.setOnClickListener(select);

        // ---- Subscribe (new purchase / renewal)
        btnSubscribe.setOnClickListener(v -> {
            if (selectedPlan == null) {
                Toast.makeText(this, "Please select a plan first", Toast.LENGTH_SHORT).show();
                return;
            }
            // if already on this plan (and not expired) => do nothing (button will be disabled anyway)
            if (hasActivePlan && !isExpired && normalize(selectedPlan).equals(normalize(currentPlan))) {
                return;
            }
            if (!hasPhoneOnProfile()) {
                Toast.makeText(this, "Phone number is required for payment. Please update your profile.", Toast.LENGTH_LONG).show();
                return;
            }
            createOrderForPlan(selectedPlan, null);
        });

        // ---- Upgrade (buy higher plan)
        btnUpgrade.setOnClickListener(v -> {
            if (selectedPlan == null) {
                Toast.makeText(this, "Select a higher plan to upgrade", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!(hasActivePlan && !isExpired)) {
                Toast.makeText(this, "You don't have an active plan to upgrade from.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isHigherPlan(selectedPlan, currentPlan)) {
                Toast.makeText(this, "Pick a higher plan to upgrade", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!hasPhoneOnProfile()) {
                Toast.makeText(this, "Phone number is required for payment. Please update your profile.", Toast.LENGTH_LONG).show();
                return;
            }
            createOrderForPlan(selectedPlan, null);
        });

        // ---- Restore (reload admin from Firebase)
        btnRestore.setOnClickListener(v -> {
            if (!MyApplication.getInstance().isUserAuthenticated()) {
                Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            setLoading(btnRestore, true);
            MyApplication.getInstance().loadAdminData(uid, new MyApplication.OnAdminLoadedListener() {
                @Override public void onAdminLoaded(com.easyranktools.callhistoryforanynumber.models.AdminModel admin) {
                    setLoading(btnRestore, false);
                    // update caches
                    hasActivePlan = MyApplication.getInstance().hasActivePlan();
                    isExpired     = MyApplication.getInstance().isPlanExpired();
                    currentPlan   = MyApplication.getInstance().getCurrentPlanType();
                    // update UI
                    highlightCards(selectedPlan, normalize(currentPlan));
                    refreshCtas();
                    Toast.makeText(PacakageActivity.this, "Restored plan info.", Toast.LENGTH_SHORT).show();
                }
                @Override public void onAdminLoadFailed(String error) {
                    setLoading(btnRestore, false);
                    Toast.makeText(PacakageActivity.this, "Restore failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });



        // ---- bottom nav (unchanged)
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navC);
        if(!MyApplication.getInstance().hasActivePlan()){
bottomNav.setVisibility(GONE);
        }
        bottomNav.setSelectedItemId(R.id.nav_premium);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_history) {
                startActivity(new Intent(this, CallHistoryActivity.class)); overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_info) {
                startActivity(new Intent(this, MainActivity.class)); overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class)); overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_premium) {
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SetttingActivity.class)); overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });
    }

    // ==================== ORDER FLOW ====================

    private void createOrderForPlan(String planType, String amountOverrideOrNull) {
        String amount = amountOverrideOrNull != null ? amountOverrideOrNull : getAmountForPlan(planType);
        String orderId = "order_" + System.currentTimeMillis();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String phone  = MyApplication.getInstance().getCurrentAdmin() != null ? MyApplication.getInstance().getCurrentAdmin().getPhoneNumber() : "";
        String name   = MyApplication.getInstance().getCurrentAdmin() != null ? MyApplication.getInstance().getCurrentAdmin().getName() : null;
        String email  = MyApplication.getInstance().getCurrentAdmin() != null ? MyApplication.getInstance().getCurrentAdmin().getEmail() : null;

        lastOrderId = orderId;

        setLoading(btnSubscribe, true);
        OrderApiClient client = new OrderApiClient();
        client.createOrder(orderId, amount, userId, phone, name, email, new OrderApiClient.OrderCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                setLoading(btnSubscribe, false);
                String paymentSessionId = response.optString("payment_session_id", null);
                if (paymentSessionId == null || paymentSessionId.isEmpty()) {
                    Toast.makeText(PacakageActivity.this, "Payment details missing.", Toast.LENGTH_LONG).show();
                    return;
                }
                startSdkCheckout(orderId, paymentSessionId);
            }
            @Override
            public void onError(String error) {
                setLoading(btnSubscribe, false);
                Log.d(TAG,error);
                Toast.makeText(PacakageActivity.this, "Error han "+error, Toast.LENGTH_LONG).show();
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

            CFPaymentGatewayService.getInstance().doPayment(this, cfWebCheckoutPayment);
        } catch (Exception e) {
            Log.e(TAG, "Error starting SDK checkout", e);
            Toast.makeText(this, "Failed to open payment UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ---- Cashfree SDK callbacks (orders)
    @Override
    public void onPaymentVerify(String orderID) {
        Log.d(TAG, "onPaymentVerify order=" + orderID);
        verifyOrderThenActivate(selectedPlan, orderID);
    }

    @Override
    public void onPaymentFailure(CFErrorResponse cfErrorResponse, String orderID) {
        String err = cfErrorResponse != null ? cfErrorResponse.getMessage() : "Unknown error";
        Log.e(TAG, "onPaymentFailure order=" + orderID + ": " + err);
        Toast.makeText(this, "Payment failed: " + err, Toast.LENGTH_SHORT).show();
    }

    private void verifyOrderThenActivate(String planType, String orderId) {
        OrderApiClient client = new OrderApiClient();
        client.checkOrderStatus(orderId, new OrderApiClient.OrderCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String orderStatus = response.getString("order_status"); // "PAID"
                    if ("PAID".equalsIgnoreCase(orderStatus)) {
                        boolean isUpgradeFlow = shouldTreatAsUpgrade(planType);
                        activatePlan(planType, isUpgradeFlow);
                    } else {
                        Toast.makeText(PacakageActivity.this, "Payment not confirmed: " + orderStatus, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(PacakageActivity.this, "Error checking payment status", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(String error) {
                Toast.makeText(PacakageActivity.this, "Error checking status: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean shouldTreatAsUpgrade(String newPlanType) {
        if (!(hasActivePlan && !isExpired)) return false;
        if (currentPlan == null) return false;
        double cur = safeAmt(getAmountForPlan(currentPlan));
        double tgt = safeAmt(getAmountForPlan(newPlanType));
        return tgt > cur;
    }

    /**
     * Orders-only policy:
     * expiry = max(now, currentExpiry) + duration(newPlan)
     */
    private void activatePlan(String newPlanType, boolean isUpgradeFlow) {
        long now = System.currentTimeMillis();

        long currentExpiry = MyApplication.getInstance().getCurrentAdmin() != null
                ? MyApplication.getInstance().getCurrentAdmin().getPlanExpiryAt()
                : 0L;

        int days = getDaysForPlan(newPlanType);
        long add  = days * 24L * 60L * 60L * 1000L;

        long newExpiry = Math.max(currentExpiry, now) + add;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_ADMINS_NODE)
                .child(userId);

        long activatedAt = now;

        ref.child("isPremium").setValue(true);
        ref.child("planType").setValue(newPlanType);
        ref.child("planActivatedAt").setValue(activatedAt);
        ref.child("planExpiryAt").setValue(newExpiry);

        if (MyApplication.getInstance().getCurrentAdmin() != null) {
            MyApplication.getInstance().getCurrentAdmin().setIsPremium(true);
            MyApplication.getInstance().getCurrentAdmin().setPlanType(newPlanType);
            MyApplication.getInstance().getCurrentAdmin().setPlanActivatedAt(activatedAt);
            MyApplication.getInstance().getCurrentAdmin().setPlanExpiryAt(newExpiry);
        }

        // refresh cached state & UI
        hasActivePlan = true;
        isExpired = false;
        currentPlan = newPlanType;
        highlightCards(selectedPlan, normalize(currentPlan));
        refreshCtas();

        Toast.makeText(this,
                (isUpgradeFlow ? "Upgraded to: " : "Plan activated: ") + newPlanType,
                Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    // ==================== UI helpers ====================

    private void refreshCtas() {
        // Compute what to do with Subscribe
        boolean sameAsActive = hasActivePlan && !isExpired
                && selectedPlan != null
                && normalize(selectedPlan).equals(normalize(currentPlan));

        if (sameAsActive) {
            // disable Subscribe
            setButtonDisabled(btnSubscribe, "Already Subscribed");
            // Upgrade only makes sense if user selects higher plan (handled when selecting)
            btnUpgrade.setVisibility(View.VISIBLE);
        } else {
            // enable Subscribe
            setButtonEnabled(btnSubscribe, hasActivePlan && !isExpired && isHigherPlan(selectedPlan, currentPlan)
                    ? "Pay & Upgrade"
                    : "Buy Selected Plan");
            // Show upgrade button only when user has active plan and selected is higher
            boolean showUpgrade = hasActivePlan && !isExpired && isHigherPlan(selectedPlan, currentPlan);
            btnUpgrade.setVisibility(showUpgrade ? View.VISIBLE : GONE);
        }
    }

    private void highlightCards(String selected, String activePlan) {
        // default -> subtle background
        setCardBackground(cardYearly,   R.drawable.bg_card_default);
        setCardBackground(cardMonthly,  R.drawable.bg_card_default);
        setCardBackground(cardWeekly,   R.drawable.bg_card_default);
        setCardBackground(card3Months,  R.drawable.bg_card_default);

        // active plan -> glowing border
        if (activePlan != null) {
            if (activePlan.equals(normalize(Config.PLAN_YEARLY)))   setCardBackground(cardYearly,  R.drawable.bg_card_active);
            if (activePlan.equals(normalize(Config.PLAN_MONTHLY)))  setCardBackground(cardMonthly, R.drawable.bg_card_active);
            if (activePlan.equals(normalize(Config.PLAN_WEEKLY)))   setCardBackground(cardWeekly,  R.drawable.bg_card_active);
            if (activePlan.equals(normalize(Config.PLAN_3MONTHS)))  setCardBackground(card3Months, R.drawable.bg_card_active);
        }

        // selected plan -> thicker border
        if (selected != null) {
            if (selected.equals(normalize(Config.PLAN_YEARLY)))   setCardBackground(cardYearly,  R.drawable.bg_card_selected);
            if (selected.equals(normalize(Config.PLAN_MONTHLY)))  setCardBackground(cardMonthly, R.drawable.bg_card_selected);
            if (selected.equals(normalize(Config.PLAN_WEEKLY)))   setCardBackground(cardWeekly,  R.drawable.bg_card_selected);
            if (selected.equals(normalize(Config.PLAN_3MONTHS)))  setCardBackground(card3Months, R.drawable.bg_card_selected);
        }
    }

    private void setCardBackground(LinearLayout card, int drawableRes) {
        try {
            card.setBackgroundResource(drawableRes);
        } catch (Exception ignored) {}
    }

    private boolean isHigherPlan(String candidate, String base) {
        if (candidate == null || base == null) return false;
        double cur = safeAmt(getAmountForPlan(base));
        double tgt = safeAmt(getAmountForPlan(candidate));
        return tgt > cur;
    }

    private String normalize(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }

    private void setButtonDisabled(Button b, String text) {
        b.setEnabled(false);
        b.setAlpha(1f);
        b.setText(text);
        // gray background (same shape as your bg_button but disabled tone)
        b.setBackgroundResource(R.drawable.bg_button_disabled);
    }

    private void setButtonEnabled(Button b, String text) {
        b.setEnabled(true);
        b.setAlpha(1f);
        b.setText(text);
        b.setBackgroundResource(R.drawable.bg_button); // your existing primary button
    }

    // ==================== misc helpers ====================

    private boolean hasPhoneOnProfile() {
        return MyApplication.getInstance().getCurrentAdmin() != null
                && MyApplication.getInstance().getCurrentAdmin().getPhoneNumber() != null
                && !MyApplication.getInstance().getCurrentAdmin().getPhoneNumber().isEmpty();
    }

    private double safeAmt(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private int getDaysForPlan(String planType) {
        switch (planType) {
            case Config.PLAN_YEARLY:  return 365;
            case Config.PLAN_3MONTHS: return 90;
            case Config.PLAN_MONTHLY: return 30;
            case Config.PLAN_WEEKLY:  return 7;
            default: return 30;
        }
    }

    private String getAmountForPlan(String planType) {
        switch (planType) {
            case Config.PLAN_YEARLY:  return Config.AMOUNT_YEARLY;
            case Config.PLAN_3MONTHS: return Config.AMOUNT_3MONTHS;
            case Config.PLAN_MONTHLY: return Config.AMOUNT_MONTHLY;
            case Config.PLAN_WEEKLY:  return Config.AMOUNT_WEEKLY;
            default: return "0";
        }
    }

    private void setLoading(Button b, boolean loading) {
        b.setEnabled(!loading);
        b.setAlpha(loading ? 0.5f : 1f);
    }

    @Override
    public void onBackPressed() { finishAffinity(); }
}
