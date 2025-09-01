package com.easyranktools.callhistoryforanynumber;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.easyranktools.callhistoryforanynumber.models.AdminModel;

public class AdminAuthManager {

    public interface AuthCallback {
        void onSuccess(AdminModel admin);
        void onFailure(String reason);
    }

    public static void checkAdminAccess(String uid, AuthCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("admins")
                .child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("Admin data not found.");
                    return;
                }

                AdminModel admin = snapshot.getValue(AdminModel.class);
                if (admin == null) {
                    callback.onFailure("Invalid admin data.");
                    return;
                }

                // Check activation
                if (!admin.getIsActivated()) {
                    callback.onFailure("Admin account is not activated.");
                    return;
                }

                // Check premium status
                if (admin.isPremium() && System.currentTimeMillis() > admin.getPlanExpiryAt()) {
                    callback.onFailure("Premium plan expired.");
                    return;
                }

                callback.onSuccess(admin);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }
}
