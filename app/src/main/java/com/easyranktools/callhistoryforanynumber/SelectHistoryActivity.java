package com.easyranktools.callhistoryforanynumber;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.easyranktools.callhistoryforanynumber.databinding.ActivitySelectHistoryBinding;


public class SelectHistoryActivity extends AppCompatActivity {

    private ActivitySelectHistoryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding= ActivitySelectHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        binding.one.setOnClickListener(view -> {
            startActivity(new Intent(SelectHistoryActivity.this,SuccessActivity.class));
        });

        binding.two.setOnClickListener(view -> {
            startActivity(new Intent(SelectHistoryActivity.this,SuccessActivity.class));
        });

        binding.llLocationHistory.setOnClickListener(view -> startActivity(new Intent(SelectHistoryActivity.this,SuccessActivity.class)));
        binding.btnCallHistoryApp.setOnClickListener(view -> startActivity(new Intent(SelectHistoryActivity.this,SuccessActivity.class)));

        binding.btnSmsHistory.setOnClickListener(view -> startActivity(new Intent(SelectHistoryActivity.this,SuccessActivity.class)));
        binding.btnWhatsappHistory.setOnClickListener(view -> startActivity(new Intent(SelectHistoryActivity.this,SuccessActivity.class)));
        binding.llCallerid.setOnClickListener(view -> startActivity(new Intent(SelectHistoryActivity.this,SuccessActivity.class)));
        binding.btnAllHistory.setOnClickListener(view -> startActivity(new Intent(SelectHistoryActivity.this,SuccessActivity.class)));




    }
}