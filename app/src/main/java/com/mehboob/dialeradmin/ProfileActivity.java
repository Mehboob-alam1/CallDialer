package com.mehboob.dialeradmin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mehboob.dialeradmin.adapters.ChildNumberAdapter;
import com.mehboob.dialeradmin.models.AdminModel;
import com.mehboob.dialeradmin.Config;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    
    private Toolbar toolbar;
    private TextView tvName, tvEmail, tvPhone, tvPlanStatus, tvPlanType, tvPlanExpiry;
    private TextView tvChildNumbersCount, tvMaxNumbers;
    private Button btnAddNumber, btnUpgradePlan;
    private CardView llPlanInfo, llNoPlan;
    private RecyclerView rvChildNumbers;
    private ChildNumberAdapter childNumberAdapter;
    
    private AdminModel currentAdmin;
    private List<String> childNumbers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        initViews();
        setupToolbar();
        loadAdminData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvPlanStatus = findViewById(R.id.tvPlanStatus);
        tvPlanType = findViewById(R.id.tvPlanType);
        tvPlanExpiry = findViewById(R.id.tvPlanExpiry);
        tvChildNumbersCount = findViewById(R.id.tvChildNumbersCount);
        tvMaxNumbers = findViewById(R.id.tvMaxNumbers);
        btnAddNumber = findViewById(R.id.btnAddNumber);
        btnUpgradePlan = findViewById(R.id.btnUpgradePlan);
        llPlanInfo = findViewById(R.id.llPlanInfo);
        llNoPlan = findViewById(R.id.llNoPlan);
        rvChildNumbers = findViewById(R.id.rvChildNumbers);
        
        // Setup RecyclerView
        childNumberAdapter = new ChildNumberAdapter(childNumbers, this::onChildNumberClick);
        rvChildNumbers.setLayoutManager(new LinearLayoutManager(this));
        rvChildNumbers.setAdapter(childNumberAdapter);
        
        // Button click listeners
        btnAddNumber.setOnClickListener(v -> {
            if (currentAdmin != null && currentAdmin.isPlanActive()) {
                startActivity(new Intent(this, EnterNumberActivity.class));
            } else {
                showUpgradePlanDialog();
            }
        });
        
        btnUpgradePlan.setOnClickListener(v -> {
            startActivity(new Intent(this, PacakageActivity.class));
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
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
                    try {
                        currentAdmin = snapshot.getValue(AdminModel.class);
                        if (currentAdmin != null) {
                            // Fix childNumbers if it's stored as HashMap (legacy format)
                            DataSnapshot childNumbersSnapshot = snapshot.child("childNumbers");
                            if (childNumbersSnapshot.exists()) {
                                List<String> childNumbers = new ArrayList<>();
                                for (DataSnapshot child : childNumbersSnapshot.getChildren()) {
                                    String number = child.getValue(String.class);
                                    if (number != null) {
                                        childNumbers.add(number);
                                    }
                                }
                                currentAdmin.setChildNumbers(childNumbers);
                            }
                            
                            updateUI();
                            loadChildNumbers();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error deserializing admin data: " + e.getMessage());
                        // Try manual deserialization for legacy data
                        try {
                            currentAdmin = new AdminModel();
                            currentAdmin.setUid(snapshot.child("uid").getValue(String.class));
                            currentAdmin.setEmail(snapshot.child("email").getValue(String.class));
                            currentAdmin.setPhoneNumber(snapshot.child("phoneNumber").getValue(String.class));
                            currentAdmin.setName(snapshot.child("name").getValue(String.class));
                            currentAdmin.setRole(snapshot.child("role").getValue(String.class));
                            currentAdmin.setIsActivated(snapshot.child("isActivated").getValue(Boolean.class) != null ? 
                                                       snapshot.child("isActivated").getValue(Boolean.class) : false);
                            currentAdmin.setIsPremium(snapshot.child("isPremium").getValue(Boolean.class) != null ? 
                                                     snapshot.child("isPremium").getValue(Boolean.class) : false);
                            currentAdmin.setPlanType(snapshot.child("planType").getValue(String.class));
                            currentAdmin.setPlanActivatedAt(snapshot.child("planActivatedAt").getValue(Long.class) != null ? 
                                                           snapshot.child("planActivatedAt").getValue(Long.class) : 0L);
                            currentAdmin.setPlanExpiryAt(snapshot.child("planExpiryAt").getValue(Long.class) != null ? 
                                                        snapshot.child("planExpiryAt").getValue(Long.class) : 0L);
                            currentAdmin.setCreatedAt(snapshot.child("createdAt").getValue(Long.class) != null ? 
                                                     snapshot.child("createdAt").getValue(Long.class) : 0L);
                            currentAdmin.setChildNumber(snapshot.child("childNumber").getValue(String.class));
                            
                            // Handle childNumbers (could be HashMap or List)
                            DataSnapshot childNumbersSnapshot = snapshot.child("childNumbers");
                            if (childNumbersSnapshot.exists()) {
                                List<String> childNumbers = new ArrayList<>();
                                for (DataSnapshot child : childNumbersSnapshot.getChildren()) {
                                    String number = child.getValue(String.class);
                                    if (number != null) {
                                        childNumbers.add(number);
                                    }
                                }
                                currentAdmin.setChildNumbers(childNumbers);
                            }
                            
                            updateUI();
                            loadChildNumbers();
                            
                        } catch (Exception manualError) {
                            Log.e(TAG, "Manual deserialization also failed: " + manualError.getMessage());
                            Toast.makeText(ProfileActivity.this, "Failed to parse admin data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error loading profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChildNumbers() {
        if (currentAdmin == null) return;
        
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference childNumbersRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_ADMINS_NODE)
                .child(uid)
                .child("childNumbers");
        
        childNumbersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childNumbers.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String number = child.getValue(String.class);
                    if (number != null) {
                        childNumbers.add(number);
                    }
                }
                childNumberAdapter.notifyDataSetChanged();
                updateChildNumbersUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error loading child numbers: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (currentAdmin == null) return;
        
        // Basic info
        tvName.setText(currentAdmin.getName() != null ? currentAdmin.getName() : "N/A");
        tvEmail.setText(currentAdmin.getEmail());
        tvPhone.setText(currentAdmin.getPhoneNumber() != null ? currentAdmin.getPhoneNumber() : "N/A");
        
        // Plan info
        if (currentAdmin.isPremium() && currentAdmin.isPlanActive()) {
            llPlanInfo.setVisibility(View.VISIBLE);
            llNoPlan.setVisibility(View.GONE);
            
            tvPlanStatus.setText("Active");
            tvPlanStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            
            tvPlanType.setText(currentAdmin.getPlanType().substring(0, 1).toUpperCase() + 
                            currentAdmin.getPlanType().substring(1) + " Plan");
            tvPlanExpiry.setText("Expires: " + currentAdmin.getPlanExpiryDateText());
            
            int maxNumbers = Config.getMaxTrackableNumbers(currentAdmin.getPlanType());
            tvMaxNumbers.setText("Max Numbers: " + (maxNumbers == Integer.MAX_VALUE ? "Unlimited" : maxNumbers));
            
            // Show/hide add number button based on plan limits
            if (currentAdmin.getChildNumbersCount() < maxNumbers) {
                btnAddNumber.setVisibility(View.VISIBLE);
                btnAddNumber.setText("Add Number (" + currentAdmin.getChildNumbersCount() + "/" + maxNumbers + ")");
            } else {
                btnAddNumber.setVisibility(View.GONE);
            }
        } else {
            llPlanInfo.setVisibility(View.GONE);
            llNoPlan.setVisibility(View.VISIBLE);
            btnAddNumber.setVisibility(View.GONE);
        }
    }

    private void updateChildNumbersUI() {
        if (currentAdmin == null) return;
        
        int currentCount = childNumbers.size();
        int maxAllowed = Config.getMaxTrackableNumbers(currentAdmin.getPlanType());
        
        tvChildNumbersCount.setText("Child Numbers: " + currentCount);
        
        if (currentAdmin.isPlanActive()) {
            if (currentCount < maxAllowed) {
                btnAddNumber.setVisibility(View.VISIBLE);
                btnAddNumber.setText("Add Number (" + currentCount + "/" + maxAllowed + ")");
            } else {
                btnAddNumber.setVisibility(View.GONE);
            }
        }
    }

    private void onChildNumberClick(String number) {
        showChildNumberOptions(number);
    }

    private void showChildNumberOptions(String number) {
        String[] options = {"View Call History", "Share App Link", "Remove Number"};
        
        new AlertDialog.Builder(this)
                .setTitle("Number: " + number)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // View call history for this number
                            Intent intent = new Intent(this, CallHistoryActivity.class);
                            intent.putExtra("child_number", number);
                            startActivity(intent);
                            break;
                        case 1:
                            // Share app link
                            shareAppLink(number);
                            break;
                        case 2:
                            // Remove number
                            showRemoveNumberDialog(number);
                            break;
                    }
                })
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

    private void showRemoveNumberDialog(String number) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Number")
                .setMessage("Are you sure you want to remove " + number + " from tracking?")
                .setPositiveButton("Remove", (dialog, which) -> removeChildNumber(number))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeChildNumber(String number) {
        if (currentAdmin == null) return;
        
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference childNumbersRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_ADMINS_NODE)
                .child(uid)
                .child("childNumbers");
        
        // Find and remove the specific number
        childNumbersRef.orderByValue().equalTo(number).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    child.getRef().removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Number removed: " + number, Toast.LENGTH_SHORT).show();
                        // Update local list
                        childNumbers.remove(number);
                        childNumberAdapter.notifyDataSetChanged();
                        updateChildNumbersUI();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Failed to remove number: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    break; // Remove only the first match
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error removing number: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpgradePlanDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Active Plan")
                .setMessage("You need an active premium plan to track child numbers. Please subscribe to a plan.")
                .setPositiveButton("Get Plans", (dialog, which) -> {
                    startActivity(new Intent(this, PacakageActivity.class));
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from other activities
        if (currentAdmin != null) {
            loadAdminData();
        }
    }
}
