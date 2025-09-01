package com.easyranktools.callhistoryforanynumber;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.easyranktools.callhistoryforanynumber.databinding.ActivitySuccessBinding;


public class SuccessActivity extends AppCompatActivity {

    private ActivitySuccessBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding=ActivitySuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               startActivity(new Intent(SuccessActivity.this, DownloadActivity.class));
               finish();
            }
        },5000);



    }

}