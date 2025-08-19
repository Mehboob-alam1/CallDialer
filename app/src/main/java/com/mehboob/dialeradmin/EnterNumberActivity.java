package com.mehboob.dialeradmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EnterNumberActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enter_number);

        EditText editTextNumber = findViewById(R.id.etPhoneNumber); // your EditText ID
        Button submitButton = findViewById(R.id.btnSubmit); // your Button ID

        submitButton.setOnClickListener(v -> {
            String rawNumber = editTextNumber.getText().toString().trim();

            if (isValidIndianNumber(rawNumber)) {
                String formattedNumber = "+91" + rawNumber;
                enforceLimitAndSave(formattedNumber);
            } else {
                Toast.makeText(this, "Please enter a valid 10-digit Indian number", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void enforceLimitAndSave(String formattedNumber) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        int maxAllowed = MyApplication.getInstance().getMaxTrackableNumbers();
        if (maxAllowed <= 0) {
            Toast.makeText(this, "No active plan. Please subscribe to add numbers.", Toast.LENGTH_LONG).show();
            return;
        }

        DatabaseReference adminRef = FirebaseDatabase.getInstance()
                .getReference("admins")
                .child(uid)
                .child("childNumbers");

        adminRef.get().addOnSuccessListener(snapshot -> {
            long currentCount = snapshot.getChildrenCount();
            boolean alreadyExists = false;
            for (DataSnapshot child : snapshot.getChildren()) {
                String num = String.valueOf(child.getValue());
                if (formattedNumber.equals(num)) {
                    alreadyExists = true;
                    break;
                }
            }
            if (alreadyExists) {
                Toast.makeText(this, "This number is already tracked.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentCount >= maxAllowed && maxAllowed != Integer.MAX_VALUE) {
                Toast.makeText(this, "Limit reached for your plan. Max allowed: " + maxAllowed, Toast.LENGTH_LONG).show();
                return;
            }

            // Save to SharedPreferences for local usage
            saveNumberToPrefs(formattedNumber);

            // Save under childNumbers as a set-like map and also keep legacy field childNumber for compatibility
            adminRef.push().setValue(formattedNumber);
            FirebaseDatabase.getInstance().getReference("admins")
                    .child(uid)
                    .child("childNumber")
                    .setValue(formattedNumber);

            Toast.makeText(this, "Number saved: " + formattedNumber, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SelectHistoryActivity.class));
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to read current numbers: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    // Validation method
    private boolean isValidIndianNumber(String phone) {
        return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches() && phone.length() == 10;
    }

    // Save to SharedPreferences
    private void saveNumberToPrefs(String number) {
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("phone_number", number);
        editor.apply();
    }

}