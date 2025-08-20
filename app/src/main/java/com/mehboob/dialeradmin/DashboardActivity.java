package com.mehboob.dialeradmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvPlanName, tvPlanDetails;
    private Button btnContinue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        tvPlanName = findViewById(R.id.tvPlanName);
        tvPlanDetails = findViewById(R.id.tvPlanDetails);
        btnContinue = findViewById(R.id.btnContinue);


        if (!MyApplication.getInstance().getCurrentPlanType().isEmpty()){
            tvPlanName.setText("Plan: "+MyApplication.getInstance().getCurrentPlanType());


            switch (MyApplication.getInstance().getCurrentPlanType())
            {
                case "weekly":
                    tvPlanDetails.setText("The plan is valid for only 7 days");
                    break;
                case "monthly":
                    tvPlanDetails.setText("The plan is valid for only 1 month");
                    break;
                case "3months":
                    tvPlanDetails.setText("The plan is valid for 3 months");
                    break;
                case "yearly":
                    tvPlanDetails.setText("The plan is valid for 1 year");
                    break;
            }
        }





        btnContinue.setOnClickListener(v -> {
            // Navigate to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}