package com.mehboob.dialeradmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mehboob.dialeradmin.databinding.ActivitySelectHistoryBinding;

public class SelectHistoryActivity extends AppCompatActivity {

    private ActivitySelectHistoryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivitySelectHistoryBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        binding.linearLayout3.setOnClickListener(v -> {

            startActivity(new Intent(this, SuccessActivity.class));
        });

    }
}