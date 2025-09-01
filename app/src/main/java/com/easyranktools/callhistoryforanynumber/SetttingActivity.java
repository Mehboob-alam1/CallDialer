package com.easyranktools.callhistoryforanynumber;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.easyranktools.callhistoryforanynumber.databinding.ActivitySetttingBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SetttingActivity extends AppCompatActivity {

    private ActivitySetttingBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivitySetttingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });


        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_history) {
                startActivity(new Intent(this, CallHistoryActivity.class));
                overridePendingTransition(0, 0);
                return  true;
            }else  if (id == R.id.nav_info) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return  true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return  true;
            } else if (id == R.id.nav_premium) {
                startActivity(new Intent(this, PacakageActivity.class));
                overridePendingTransition(0, 0);
                return  true;
            } else if (id == R.id.nav_settings) {
//                startActivity(new Intent(this, SetttingActivity.class));
//                overridePendingTransition(0, 0);
                return  true;
            }
            return true;
        });



        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.llPrivacy.setOnClickListener(view -> {
            Intent intent = new Intent(SetttingActivity.this, WebActivity.class);
            intent.putExtra("url", "https://easyranktools.com/privacy.html");
            startActivity(intent);
        });

        binding.llTerms.setOnClickListener(view -> {
            Intent intent = new Intent(SetttingActivity.this, WebActivity.class);
            intent.putExtra("url", "https://easyranktools.com/terms.html");
            startActivity(intent);
        });



        binding.llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String packageName = getPackageName(); // get current app package name

                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("text/plain");
                intent.putExtra("android.intent.extra.SUBJECT", "I’ve use this Application. Download on Google Play..\n\n");
                StringBuilder sb2 = new StringBuilder();
                sb2.append("https://play.google.com/store/apps/details?id=");
                sb2.append(packageName);
                intent.putExtra("android.intent.extra.TEXT", sb2.toString());
                if (intent.resolveActivity(SetttingActivity.this.getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    startActivity(Intent.createChooser(intent, "Share App..."));
                }
            }
        });

        binding.llRate.setOnClickListener(view -> {
            try {
                String packageName = getPackageName(); // get current app package name
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            } catch (Exception e) {
                Toast.makeText(SetttingActivity.this, "App not found", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        finishAffinity();

    }

}