package com.mehboob.dialeradmin;

import android.annotation.SuppressLint;
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
        String plan=MyApplication.getInstance().getActivePlanName();
        Toast.makeText(this, "Plan is "+plan, Toast.LENGTH_SHORT).show();

        LinearLayout[] buttons = {btn1, btn2, btn3, btn4};
        if (MyApplication.getInstance().isPremiumActive()){
//            String plan=MyApplication.getInstance().getActivePlanName();
//            Toast.makeText(this, "Plan is "+plan, Toast.LENGTH_SHORT).show();
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
            } else {
                activatePlan(selectedPlan);
            }
        });
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
                .getReference("admins") // ✅ use correct node for your app
                .child(userId);

        ref.child("isPremium").setValue(true);
        ref.child("planType").setValue(planType);
        ref.child("planActivatedAt").setValue(now);
        ref.child("planExpiryAt").setValue(expiry);

        Toast.makeText(this, "Plan activated: " + planType, Toast.LENGTH_SHORT).show();
    }
}
