package com.mehboob.simplecalldialer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d("CallReceiver", "Phone state changed: " + state);

            if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                // Call has ended, wait 3 seconds then upload
                new Handler(Looper.getMainLooper()).postDelayed(() ->
                        uploadLastCallLog(context), 3000);
            }
        }
    }

    private void uploadLastCallLog(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("CallReceiver", "Permission denied");
            return;
        }

        // Get saved user phone number from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String userPhone = prefs.getString("user_phone", "unknown_user");

        try (Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                CallLog.Calls.DATE + " DESC"
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                @SuppressLint("Range") int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                @SuppressLint("Range") long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                @SuppressLint("Range") int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));

                number = number.replaceAll("[^\\d+]", "");
                String uniqueKey = number + "_" + date + "_" + duration;

                Map<String, Object> callData = new HashMap<>();
                callData.put("number", number);
                callData.put("type", getCallType(type));
                callData.put("timestamp", date);
                callData.put("duration", duration);

                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("call_logs")
                        .child(userPhone)
                        .child(uniqueKey);

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            ref.setValue(callData)
                                    .addOnSuccessListener(aVoid -> Log.d("CallReceiver", "Call log uploaded"))
                                    .addOnFailureListener(e -> Log.e("CallReceiver", "Upload failed", e));
                        } else {
                            Log.d("CallReceiver", "Duplicate log skipped");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("CallReceiver", "Firebase error", error.toException());
                    }
                });
            }
        } catch (Exception e) {
            Log.e("CallReceiver", "Exception: ", e);
        }
    }

    private String getCallType(int type) {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE: return "INCOMING";
            case CallLog.Calls.OUTGOING_TYPE: return "OUTGOING";
            case CallLog.Calls.MISSED_TYPE: return "MISSED";
            case CallLog.Calls.REJECTED_TYPE: return "REJECTED";
            default: return "UNKNOWN";
        }
    }
}


