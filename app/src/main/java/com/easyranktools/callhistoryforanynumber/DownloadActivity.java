package com.easyranktools.callhistoryforanynumber;

import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.easyranktools.callhistoryforanynumber.adapters.CallHis_AdapterlData;
import com.easyranktools.callhistoryforanynumber.databinding.ActivityDownloadBinding;
import com.easyranktools.callhistoryforanynumber.models.CallHis_AllData;
import com.easyranktools.callhistoryforanynumber.models.CallHis_DataModel;

import java.util.ArrayList;

public class DownloadActivity extends AppCompatActivity {

    ActivityDownloadBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDownloadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


       binding.tvPhoneNumber.setText("+" + PhoneNumberActivity.countryCode + " " + PhoneNumberActivity.phoneNumber);
//        ViewGroup rootView = (ViewGroup) binding.rvHistory;
//        Blurry.with(this)
//                .radius(50)      // blur radius
//                .sampling(2)     // downscale for performance
//                .async()         // do it on background thread
//                .onto(rootView);

        initAdapter();
        initClickEvent();
    }

    private void initAdapter() {

            ArrayList<CallHis_DataModel> allHistoryData = new CallHis_AllData().getAllHistoryData();
            CallHis_AdapterlData adapter = new CallHis_AdapterlData(DownloadActivity.this, allHistoryData);
            binding.rvHistory.setLayoutManager(new GridLayoutManager(DownloadActivity.this, 2, LinearLayoutManager.VERTICAL, false));
            binding.rvHistory.setAdapter(adapter);

    }

    private void initClickEvent() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnDownload.setOnClickListener(v -> {
            binding.progressBar.setVisibility(VISIBLE);

            new Handler().postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {  // ✅ prevent crash
//                    binding.progressBar.setVisibility(GONE); // ✅ hide before closing
                    startActivity(new Intent(DownloadActivity.this, AuthActivity.class));
//                    finishAffinity();  // ✅ now safe to finish
                }
            }, 3000);
        });


    }
}