package com.mehboob.dialeradmin;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mehboob.dialeradmin.adapters.CallLogAdapterF;
import com.mehboob.dialeradmin.models.AdminModel;
import com.mehboob.dialeradmin.models.CallLogModel;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private EditText editPhone;
    private Button btnAdd;
    private RecyclerView recyclerView;
    private CallLogAdapterF callLogAdapter;
    private List<CallLogModel> callLogs = new ArrayList<>();

    private Button btnCall;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        btnCall=findViewById(R.id.button_call_history);


        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();


        if (currentUser != null) {
            Toast.makeText(this, "CUrrrent user is "+currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            String uid = currentUser.getUid();
            btnCall.setVisibility(GONE);


            AdminAuthManager.checkAdminAccess(uid, new AdminAuthManager.AuthCallback() {
                @Override
                public void onSuccess(AdminModel admin) {
                    if (admin.isPremium() && System.currentTimeMillis() < admin.getPlanExpiryAt()) {
                        // Active premium
                        startActivity(new Intent(AdminActivity.this, DashboardActivity.class));
                    } else {
                        // No active plan → go to Package screen
                        startActivity(new Intent(AdminActivity.this, PacakageActivity.class));
                    }
                    finish();
                }

                @Override
                public void onFailure(String reason) {
                    Toast.makeText(AdminActivity.this, reason, Toast.LENGTH_SHORT).show();
                    // Maybe log out or show retry option
                    FirebaseAuth.getInstance().signOut();
                }
            });
        }


        btnCall.setOnClickListener(v -> {
            // Navigate to the next screen
            startActivity(new Intent(AdminActivity.this, AuthActivity.class));
        });
    }

    private void fetchCallLogsForNumber(String phoneNumber) {
        DatabaseReference callLogsRef = FirebaseDatabase.getInstance()
                .getReference("call_logs")
                .child(phoneNumber);

        callLogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callLogs.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    CallLogModel model = snap.getValue(CallLogModel.class);
                    if (model != null) {
                        callLogs.add(model);
                    }
                }
                callLogAdapter.notifyDataSetChanged();
                if (callLogs.isEmpty()) {
                    Toast.makeText(AdminActivity.this, "No call logs found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
