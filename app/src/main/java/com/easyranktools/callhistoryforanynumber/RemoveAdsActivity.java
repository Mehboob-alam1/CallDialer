package com.easyranktools.callhistoryforanynumber;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RemoveAdsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_remove_ads);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnPurchase = findViewById(R.id.btnPurchase);
        Button btnRestore = findViewById(R.id.btnRestore);

        btnPurchase.setOnClickListener(v -> {
            // Dummy: In real flow, trigger Google Play Billing
            Toast.makeText(this, "Purchase flow", Toast.LENGTH_SHORT).show();

           AdManager.removeAds();
            finish();
        });

        btnRestore.setOnClickListener(v -> {
            Toast.makeText(this, "Restore purchases ", Toast.LENGTH_SHORT).show();
        });
    }
}