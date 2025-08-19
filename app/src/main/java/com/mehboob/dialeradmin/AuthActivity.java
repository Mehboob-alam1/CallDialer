package com.mehboob.dialeradmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mehboob.dialeradmin.models.AdminModel;

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
                    AdminModel adminModel = new AdminModel(uid, email, name, phone, "admin", true, false, "", 0, 0, now, "");

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
                    if (snapshot.exists()) {
                        AdminModel admin = snapshot.getValue(AdminModel.class);
                        if (admin != null && admin.getIsActivated()) {
                            ((MyApplication) getApplication()).setCurrentAdmin(admin);
                            startActivity(new Intent(this, EnterNumberActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Admin account is not activated", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(this, "No admin record found", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
