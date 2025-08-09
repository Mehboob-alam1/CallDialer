package com.mehboob.dialeradmin;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mehboob.dialeradmin.models.AdminModel;

public class MyApplication extends Application {

    private AdminModel currentAdmin;
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);

        loadCurrentAdmin();
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public AdminModel getCurrentAdmin() {
        return currentAdmin;
    }

    public void setCurrentAdmin(AdminModel admin) {
        this.currentAdmin = admin;
    }

    public boolean isPremiumActive() {
        return currentAdmin != null
                && currentAdmin.isPremium()
                && System.currentTimeMillis() <= currentAdmin.getPlanExpiryAt();
    }

    public boolean isAdminActivated() {
        return currentAdmin != null && currentAdmin.isActivated();
    }

    public String getActivePlanName() {
        return currentAdmin != null ? currentAdmin.getPlanType() : null;
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        currentAdmin = null;
    }

    public void loadCurrentAdmin() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference adminRef = FirebaseDatabase.getInstance()
                    .getReference("admins")
                    .child(firebaseUser.getUid());

            adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentAdmin = snapshot.getValue(AdminModel.class);
                        Log.d("MyApplication", "Admin loaded: " + currentAdmin.getEmail());
                    } else {
                        currentAdmin = null;
                        Log.w("MyApplication", "No admin data found.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    currentAdmin = null;
                    Log.e("MyApplication", "Error loading admin", error.toException());
                }
            });
        }
    }
}
