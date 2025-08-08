package com.mehboob.dialeradmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PhoneNumberActivity extends AppCompatActivity {

    EditText phoneInput;
    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        phoneInput = findViewById(R.id.phone_input);
        btnContinue = findViewById(R.id.btn_continue);

        btnContinue.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();
            if (isValidPhoneNumber(phone)) {
                savePhoneNumber(phone);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidPhoneNumber(String phone) {
        return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches() && phone.length() >= 10;
    }

    private void savePhoneNumber(String phone) {
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        prefs.edit().putString("user_phone", phone).apply();
    }
}
