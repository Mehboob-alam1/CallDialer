package com.mehboob.dialeradmin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mehboob.dialeradmin.databinding.ActivitySuccessBinding;
import com.mehboob.dialeradmin.models.AdminModel;

public class SuccessActivity extends AppCompatActivity {

    private ActivitySuccessBinding binding;

    private boolean isHistoryExist=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySuccessBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());



        if (!isHistoryExist){

        }else{

        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               startActivity(new Intent(SuccessActivity.this, PacakageActivity.class));
            }
        },5000);


//        String phoneNumber = getIntent().getStringExtra("phoneNumber");
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
//
//        userRef.get().addOnSuccessListener(snapshot -> {
//            boolean isPremium = snapshot.child("isPremium").getValue(Boolean.class);
//
//            if (!isPremium) {
//                // Not premium → go to plans
//                Intent planIntent = new Intent(this, PackageActivity.class);
//                planIntent.putExtra("phoneNumber", phoneNumber);
//                startActivity(planIntent);
//                finish();
//            } else {
//                // Premium → check call history
//                checkCallHistory(phoneNumber);
//            }
//        });

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);

        ref.get().addOnSuccessListener(snapshot -> {
            AdminModel admin = snapshot.getValue(AdminModel.class);

            if (admin == null || !admin.isActivated()) {
                Toast.makeText(this, "Your account is not activated", Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                finish();
                return;
            }

            if (!admin.isPremium() || System.currentTimeMillis() > admin.getPlanExpiryAt()) {
                Toast.makeText(this, "Your premium plan has expired", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, PacakageActivity.class));
                finish();
                return;
            }

            // ✅ User is premium and activated → allow access
        });


    }
}