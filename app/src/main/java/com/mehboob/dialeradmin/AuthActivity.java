package com.mehboob.dialeradmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mehboob.dialeradmin.models.AdminModel;

import java.util.ArrayList;
import java.util.List;

public class AuthActivity extends AppCompatActivity {

    private EditText emailEt, passwordEt, phoneEt, nameEt;
    private Button actionBtn;
    private ProgressBar progressBar;
    private TextView toggleAuthMode;

    private boolean isLoginMode = true; // start in login mode
    private FirebaseAuth mAuth;
    private DatabaseReference adminRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        adminRef = FirebaseDatabase.getInstance().getReference("admins");

        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        phoneEt = findViewById(R.id.phoneEt);
        nameEt = findViewById(R.id.nameEt);
        actionBtn = findViewById(R.id.actionBtn);
        progressBar = findViewById(R.id.progressBar);
        toggleAuthMode = findViewById(R.id.toggleAuthMode);

        actionBtn.setOnClickListener(v -> {
            if (isLoginMode) {
                loginAdmin();
            } else {
                registerAdmin();
            }
        });

        toggleAuthMode.setOnClickListener(v -> toggleMode());
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            actionBtn.setText("Login");
            toggleAuthMode.setText("Don't have an account? Sign up");
            phoneEt.setVisibility(View.GONE);
            nameEt.setVisibility(View.GONE);
        } else {
            actionBtn.setText("Sign Up");
            toggleAuthMode.setText("Already have an account? Login");
            phoneEt.setVisibility(View.VISIBLE);
            nameEt.setVisibility(View.VISIBLE);
        }
    }

    private void registerAdmin() {
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();
        String phone = phoneEt.getText().toString().trim();
        String name = nameEt.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter full name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    long now = System.currentTimeMillis();

                    // New AdminModel with name
                    AdminModel adminModel = new AdminModel(uid, email,  phone,name, "admin", true, false, "", 0, 0, now, "");

                    adminRef.child(uid).setValue(adminModel)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Admin registered successfully", Toast.LENGTH_SHORT).show();
                                checkAdminAccess(uid);
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loginAdmin() {
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    checkAdminAccess(uid);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkAdminAccess(String uid) {
        adminRef.child(uid).get()
                .addOnSuccessListener(snapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "No admin record found", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        return;
                    }

                    try {
                        // Extract individual fields manually
                        String uidVal = snapshot.child("uid").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                        String name = snapshot.child("name").getValue(String.class);
                        String role = snapshot.child("role").getValue(String.class);
                        Boolean isActivated = snapshot.child("isActivated").getValue(Boolean.class);
                        Boolean isPremium = snapshot.child("isPremium").getValue(Boolean.class);
                        String planType = snapshot.child("planType").getValue(String.class);
                        Long planActivatedAt = snapshot.child("planActivatedAt").getValue(Long.class);
                        Long planExpiryAt = snapshot.child("planExpiryAt").getValue(Long.class);
                        Long createdAt = snapshot.child("createdAt").getValue(Long.class);
                        String childNumber = snapshot.child("childNumber").getValue(String.class);

                        // Handle childNumbers HashMap
                        List<String> childNumbersList = new ArrayList<>();
                        DataSnapshot childNumbersSnapshot = snapshot.child("childNumbers");
                        if (childNumbersSnapshot.exists()) {
                            for (DataSnapshot child : childNumbersSnapshot.getChildren()) {
                                Object value = child.getValue();
                                if (value != null) {
                                    childNumbersList.add(value.toString());
                                }
                            }
                        }

                        // Construct AdminModel manually
                        AdminModel admin = new AdminModel(
                                uidVal, email, phoneNumber, name, role,
                                isActivated != null && isActivated,
                                isPremium != null && isPremium,
                                planType, planActivatedAt != null ? planActivatedAt : 0,
                                planExpiryAt != null ? planExpiryAt : 0,
                                createdAt != null ? createdAt : 0,
                                childNumber
                        );
                        admin.setChildNumbers(childNumbersList);

                        // Check activation
                        if (admin.getIsActivated()) {
                            ((MyApplication) getApplication()).setCurrentAdmin(admin);
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Admin account is not activated", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }

                    } catch (Exception e) {
                        Log.e("AuthActivity", "Error deserializing admin data: " + e.getMessage());
                        Toast.makeText(this, "Failed to parse admin data", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


//    private void checkAdminAccess(String uid) {
//        adminRef.child(uid).get()
//                .addOnSuccessListener(snapshot -> {
//                    progressBar.setVisibility(View.GONE);
//                    if (snapshot.exists()) {
//                        try {
//                            Log.d("AuthActivity", "Raw snapshot: " + snapshot.getValue());
//
//                            AdminModel admin = snapshot.getValue(AdminModel.class);
//                            if (admin != null) {
//                                // Fix childNumbers if it's stored as HashMap (legacy format)
//                                DataSnapshot childNumbersSnapshot = snapshot.child("childNumbers");
//                                if (childNumbersSnapshot.exists()) {
//                                    List<String> childNumbers = new ArrayList<>();
//                                    for (DataSnapshot child : childNumbersSnapshot.getChildren()) {
//                                        String number = child.getValue(String.class);
//                                        if (number != null) {
//                                            childNumbers.add(number);
//                                        }
//                                    }
//                                    admin.setChildNumbers(childNumbers);
//                                }
//
//                                if (admin.getIsActivated()) {
//                                    ((MyApplication) getApplication()).setCurrentAdmin(admin);
//                                    startActivity(new Intent(this, EnterNumberActivity.class));
//                                    finish();
//                                } else {
//                                    Toast.makeText(this, "Admin account is not activated", Toast.LENGTH_SHORT).show();
//                                    mAuth.signOut();
//                                }
//                            } else {
//                                Toast.makeText(this, "Failed to parse admin data", Toast.LENGTH_SHORT).show();
//                                mAuth.signOut();
//                            }
//                        } catch (Exception e) {
//                            Log.e("AuthActivity", "Error deserializing admin data: " + e.getMessage());
//                            Toast.makeText(this, "Failed to parse admin data", Toast.LENGTH_SHORT).show();
//                            mAuth.signOut();
//                        }
//                    } else {
//                        Toast.makeText(this, "No admin record found", Toast.LENGTH_SHORT).show();
//                        mAuth.signOut();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    progressBar.setVisibility(View.GONE);
//                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }
}
