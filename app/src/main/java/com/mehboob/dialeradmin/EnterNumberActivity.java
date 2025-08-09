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
                saveNumberToPrefs(formattedNumber);
                Toast.makeText(this, "Number saved: " + formattedNumber, Toast.LENGTH_SHORT).show();
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("admins") // ✅ use correct node for your app
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                ref.child("childNumber").setValue(formattedNumber);

                startActivity(new Intent(this, SelectHistoryActivity.class));
            } else {
                Toast.makeText(this, "Please enter a valid 10-digit Indian number", Toast.LENGTH_SHORT).show();
            }
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