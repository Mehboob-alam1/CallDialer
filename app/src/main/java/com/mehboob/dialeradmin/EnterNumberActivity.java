package com.mehboob.dialeradmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mehboob.dialeradmin.models.AdminModel;
import com.mehboob.dialeradmin.Config;

public class EnterNumberActivity extends AppCompatActivity {
    private static final String TAG = "EnterNumberActivity";
    
    private Toolbar toolbar;
    private EditText editTextNumber;
    private Button submitButton;
    private ProgressBar progressBar;
    private TextView tvPlanInfo;
    
    private AdminModel currentAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_number);
        
        initViews();
        setupToolbar();
        loadAdminData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        editTextNumber = findViewById(R.id.editTextNumber);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);
        tvPlanInfo = findViewById(R.id.tvPlanInfo);
        
        submitButton.setOnClickListener(v -> validateAndSubmit());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add Child Number");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadAdminData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adminRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_ADMINS_NODE)
                .child(uid);
        
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentAdmin = snapshot.getValue(AdminModel.class);
                    if (currentAdmin != null) {
                        updatePlanInfo();
                        checkPlanStatus();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EnterNumberActivity.this, "Error loading admin data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePlanInfo() {
        if (currentAdmin == null) return;
        
        String planType = currentAdmin.getPlanType();
        int maxNumbers = Config.getMaxTrackableNumbers(planType);
        int currentCount = currentAdmin.getChildNumbersCount();
        
        String planInfo = "Plan: " + planType.substring(0, 1).toUpperCase() + planType.substring(1) + "\n";
        planInfo += "Numbers: " + currentCount + "/" + (maxNumbers == Integer.MAX_VALUE ? "Unlimited" : maxNumbers);
        
        tvPlanInfo.setText(planInfo);
    }

    private void checkPlanStatus() {
        if (currentAdmin == null) return;
        
        // Check if user has an active plan
        if (!currentAdmin.isPremium() || !currentAdmin.isPlanActive()) {
            showNoPlanDialog();
            return;
        }
        
        // Check if user has reached the limit
        int currentCount = currentAdmin.getChildNumbersCount();
        int maxAllowed = Config.getMaxTrackableNumbers(currentAdmin.getPlanType());
        
        if (currentCount >= maxAllowed) {
            showLimitReachedDialog(maxAllowed);
            return;
        }
        
        // User can add more numbers
        submitButton.setEnabled(true);
        editTextNumber.setEnabled(true);
    }

    private void validateAndSubmit() {
        String rawNumber = editTextNumber.getText().toString().trim();
        
        if (rawNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!isValidIndianNumber(rawNumber)) {
            Toast.makeText(this, "Please enter a valid 10-digit Indian number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check plan status again before submitting
        if (!currentAdmin.isPremium() || !currentAdmin.isPlanActive()) {
            showNoPlanDialog();
            return;
        }
        
        int currentCount = currentAdmin.getChildNumbersCount();
        int maxAllowed = Config.getMaxTrackableNumbers(currentAdmin.getPlanType());
        
        if (currentCount >= maxAllowed) {
            showLimitReachedDialog(maxAllowed);
            return;
        }
        
        submitNumber(rawNumber);
    }

    private void submitNumber(String rawNumber) {
        String formattedNumber = "+91" + rawNumber;
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);
        
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adminRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_ADMINS_NODE)
                .child(uid);
        DatabaseReference childNumbersRef = adminRef.child("childNumbers");
        
        // Check for duplicate number
        childNumbersRef.orderByValue().equalTo(formattedNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(EnterNumberActivity.this, "Number already added", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Add the new number
                childNumbersRef.push().setValue(formattedNumber)
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(View.GONE);
                            submitButton.setEnabled(true);
                            
                            // Also update the single childNumber field for backward compatibility
                            adminRef.child("childNumber").setValue(formattedNumber);
                            
                            Toast.makeText(EnterNumberActivity.this, "Number added successfully: " + formattedNumber, Toast.LENGTH_SHORT).show();
                            
                            // Show success dialog with app sharing option
                            showSuccessDialog(formattedNumber);
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            submitButton.setEnabled(true);
                            Toast.makeText(EnterNumberActivity.this, "Failed to add number: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                Toast.makeText(EnterNumberActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog(String number) {
        new AlertDialog.Builder(this)
                .setTitle("Number Added Successfully!")
                .setMessage("The number " + number + " has been added to your tracking list.\n\n" +
                        "To start tracking calls, the child user needs to download the app. Would you like to share the app link now?")
                .setPositiveButton("Share App Link", (dialog, which) -> {
                    shareAppLink(number);
                    finish();
                })
                .setNegativeButton("Done", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void shareAppLink(String number) {
        String message = "Hi! I'm using " + Config.APP_NAME + " to track calls. " +
                        "Please download the app to enable call tracking for your number: " + number + "\n\n" +
                        "App Link: https://play.google.com/store/apps/details?id=" + getPackageName();
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Download " + Config.APP_NAME);
        
        startActivity(Intent.createChooser(shareIntent, "Share App Link"));
    }

    private void showNoPlanDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Active Plan")
                .setMessage("You need an active premium plan to track child numbers. Please subscribe to a plan.")
                .setPositiveButton("Get Plans", (dialog, which) -> {
                    startActivity(new Intent(this, PacakageActivity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showLimitReachedDialog(int maxAllowed) {
        new AlertDialog.Builder(this)
                .setTitle("Tracking Limit Reached")
                .setMessage("You have reached the maximum number of trackable numbers (" + maxAllowed + ") for your current plan. Please upgrade your plan to track more numbers.")
                .setPositiveButton("Upgrade Plan", (dialog, which) -> {
                    startActivity(new Intent(this, PacakageActivity.class));
                    finish();
                })
                .setNegativeButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private boolean isValidIndianNumber(String number) {
        // Remove any spaces or special characters
        number = number.replaceAll("[\\s\\-()]", "");
        
        // Check if it's a 10-digit number starting with 6, 7, 8, or 9
        return number.length() == 10 && number.matches("^[6-9]\\d{9}$");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}