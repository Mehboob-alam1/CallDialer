package com.easyranktools.callhistoryforanynumber;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class DialerActivity extends AppCompatActivity {
    private static final String TAG = "DialerActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    private Toolbar toolbar;
    private EditText etPhoneNumber;
    private Button[] numberButtons = new Button[10];
    private Button btnCall, btnDelete, btnClear;
    
    private StringBuilder phoneNumber = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);
        
        initViews();
        setupToolbar();
        setupNumberButtons();
        setupActionButtons();
        requestPermissions();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        
        // Initialize number buttons
        numberButtons[0] = findViewById(R.id.btn0);
        numberButtons[1] = findViewById(R.id.btn1);
        numberButtons[2] = findViewById(R.id.btn2);
        numberButtons[3] = findViewById(R.id.btn3);
        numberButtons[4] = findViewById(R.id.btn4);
        numberButtons[5] = findViewById(R.id.btn5);
        numberButtons[6] = findViewById(R.id.btn6);
        numberButtons[7] = findViewById(R.id.btn7);
        numberButtons[8] = findViewById(R.id.btn8);
        numberButtons[9] = findViewById(R.id.btn9);
        
        btnCall = findViewById(R.id.btnCall);
        btnDelete = findViewById(R.id.btnDelete);
        btnClear = findViewById(R.id.btnClear);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dialer");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void setupNumberButtons() {
        for (int i = 0; i < 10; i++) {
            final int number = i;
            numberButtons[i].setOnClickListener(v -> addNumber(String.valueOf(number)));
        }
    }

    private void setupActionButtons() {
        btnCall.setOnClickListener(v -> makeCall());
        btnDelete.setOnClickListener(v -> deleteLastDigit());
        btnClear.setOnClickListener(v -> clearNumber());
    }

    private void addNumber(String digit) {
        phoneNumber.append(digit);
        updateDisplay();
    }

    private void deleteLastDigit() {
        if (phoneNumber.length() > 0) {
            phoneNumber.deleteCharAt(phoneNumber.length() - 1);
            updateDisplay();
        }
    }

    private void clearNumber() {
        phoneNumber.setLength(0);
        updateDisplay();
    }

    private void updateDisplay() {
        etPhoneNumber.setText(phoneNumber.toString());
        
        // Enable/disable call button based on number length
        btnCall.setEnabled(phoneNumber.length() > 0);
    }

    private void makeCall() {
        if (phoneNumber.length() == 0) {
            Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Call permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        String number = phoneNumber.toString();
        if (number.length() == 10) {
            // Add country code for Indian numbers
            number = "+91" + number;
        } else if (number.length() == 11 && number.startsWith("0")) {
            // Remove leading 0 and add country code
            number = "+91" + number.substring(1);
        } else if (!number.startsWith("+")) {
            // Add + if no country code
            number = "+" + number;
        }

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        
        try {
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermissions() {
        String[] permissions = {
            Manifest.permission.CALL_PHONE
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Call permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Call permission required for dialing", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // In dialer mode, back button should exit the app
        finishAffinity();
    }
}
